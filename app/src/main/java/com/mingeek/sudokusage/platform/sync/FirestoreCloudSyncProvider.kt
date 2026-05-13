package com.mingeek.sudokusage.platform.sync

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mingeek.sudokusage.auth.AuthSession
import com.mingeek.sudokusage.auth.AuthState
import com.mingeek.sudokusage.data.db.AchievementProgressDao
import com.mingeek.sudokusage.data.db.AchievementProgressEntity
import com.mingeek.sudokusage.data.db.DailyCompletionDao
import com.mingeek.sudokusage.data.db.DailyCompletionEntity
import com.mingeek.sudokusage.data.db.StatsDao
import com.mingeek.sudokusage.data.db.StatsEntity
import com.mingeek.sudokusage.domain.event.GameEvent
import com.mingeek.sudokusage.domain.event.PuzzleResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/**
 * Bidirectional Firestore sync for stats, achievements, and daily completions.
 *
 * Schema (collections rooted at `users/{uid}`):
 *   - `stats/{variant}_{difficulty}` — one doc per StatsEntity
 *   - `achievements/{achievementId}` — one doc per AchievementProgressEntity
 *   - `daily/{yyyy-MM-dd}` — one doc per DailyCompletionEntity
 *
 * Conflict resolution (on pull) is monotonic for counters (max), inverse for
 * best times (min), terminal-state-wins for daily, and earliest-unlock-wins
 * for achievements. Push writes local state as-is with merge=true; for the
 * common single-device case this is correct, and for multi-device it's
 * eventually-consistent as long as `pullAll()` runs before `pushAll()`.
 *
 * Designed for the Spark (free) plan: full snapshots are written in a single
 * batched commit per sync to keep daily-quota burn predictable. No realtime
 * listeners — reads happen only on explicit `pullAll()`.
 */
class FirestoreCloudSyncProvider(
    private val firestore: FirebaseFirestore,
    private val authSession: AuthSession,
    private val statsDao: StatsDao,
    private val achievementDao: AchievementProgressDao,
    private val dailyDao: DailyCompletionDao,
    private val now: () -> Long = System::currentTimeMillis,
) : CloudSyncProvider {

    private val _status = MutableStateFlow(SyncStatus())
    override val status: StateFlow<SyncStatus> = _status.asStateFlow()

    // Serializes push/pull so two simultaneous calls don't see partially
    // written state mid-batch.
    private val mutex = Mutex()

    override suspend fun pushAll(): Unit = withContext(Dispatchers.IO) {
        val uid = currentUid() ?: run {
            Log.w(TAG, "pushAll skipped — no authenticated user")
            return@withContext
        }
        mutex.withLock {
            setState(SyncState.Syncing)
            try {
                pushLocalSnapshot(uid)
                setState(SyncState.Idle, lastSyncedAt = now())
            } catch (e: Exception) {
                Log.w(TAG, "pushAll failed: ${e.message}")
                setState(SyncState.Error, errorMessage = e.message)
            }
        }
    }

    /**
     * Pulls the cloud snapshot into local DB. If the cloud was completely
     * empty (first-ever sync for this UID), follows up with a full
     * [pushAll]-equivalent backfill so any pre-existing offline play history
     * reaches the cloud. Backfill runs only when *all three* collections are
     * empty — partial state is ambiguous and we let onGameEvent + the
     * conflict-resolution merge handle catch-up instead.
     */
    override suspend fun pullAll(): Unit = withContext(Dispatchers.IO) {
        val uid = currentUid() ?: run {
            Log.w(TAG, "pullAll skipped — no authenticated user")
            return@withContext
        }
        var cloudWasEmpty = false
        mutex.withLock {
            setState(SyncState.Syncing)
            try {
                val userRoot = firestore.collection(USERS).document(uid)

                val statsSnap = userRoot.collection(C_STATS).get().await()
                val achSnap = userRoot.collection(C_ACH).get().await()
                val dailySnap = userRoot.collection(C_DAILY).get().await()
                cloudWasEmpty = statsSnap.isEmpty && achSnap.isEmpty && dailySnap.isEmpty

                val localStats = statsDao.getAll().associateBy { it.statsKey() }
                statsSnap.documents.forEach { doc ->
                    val remote = doc.toStatsEntity() ?: return@forEach
                    val merged = localStats[remote.statsKey()]?.merge(remote) ?: remote
                    statsDao.upsert(merged)
                }

                val localAch = achievementDao.getAll().associateBy { it.achievementId }
                achSnap.documents.forEach { doc ->
                    val remote = doc.toAchievementEntity() ?: return@forEach
                    val merged = localAch[remote.achievementId]?.merge(remote) ?: remote
                    achievementDao.upsert(merged)
                }

                val localDaily = dailyDao.getAll().associateBy { it.dateKey }
                dailySnap.documents.forEach { doc ->
                    val remote = doc.toDailyEntity() ?: return@forEach
                    val merged = localDaily[remote.dateKey]?.merge(remote) ?: remote
                    dailyDao.upsert(merged)
                }

                if (cloudWasEmpty) pushLocalSnapshot(uid)
                setState(SyncState.Idle, lastSyncedAt = now())
            } catch (e: Exception) {
                Log.w(TAG, "pullAll failed: ${e.message}")
                setState(SyncState.Error, errorMessage = e.message)
            }
        }
    }

    /**
     * Scoped per-event push. Only [GameEvent.PuzzleCompleted] is meaningful
     * for cloud state — it's the moment stats/achievements/daily rows
     * actually change. Other events are no-ops to keep Firestore write
     * traffic bounded on Spark (free) quotas.
     *
     * Budget for a single completion: 1 stats doc + only the
     * unlocked-or-in-progress achievement docs (≤6) + optionally 1 daily
     * doc. Unlocked achievements are idempotent (merge keeps earliest
     * unlockedAt), so re-pushing them every completion is cheap and
     * correct; locked/empty ones don't need to exist in the cloud at all.
     *
     * Sync provider does NOT itself wait for the StatsCollector /
     * AchievementCollector writes to land — that ordering is enforced by
     * [SyncCollector] before it calls this. Failures here log + bump
     * SyncState.Error but don't propagate; transient per-event push errors
     * are expected on flaky networks and the next event's push will catch
     * up (counters are monotonic).
     */
    override suspend fun onGameEvent(event: GameEvent): Unit = withContext(Dispatchers.IO) {
        val completed = event as? GameEvent.PuzzleCompleted ?: return@withContext
        val uid = currentUid() ?: run {
            Log.w(TAG, "onGameEvent skipped — no authenticated user")
            return@withContext
        }
        mutex.withLock {
            setState(SyncState.Syncing)
            try {
                val userRoot = firestore.collection(USERS).document(uid)
                val batch = firestore.batch()

                statsDao.get(completed.variant.value, completed.difficulty.name)?.let { row ->
                    batch.set(
                        userRoot.collection(C_STATS).document(row.statsKey()),
                        row.toMap(),
                        SetOptions.merge(),
                    )
                }

                achievementDao.getAll()
                    .filter { it.unlocked || it.progress > 0 }
                    .forEach { row ->
                        batch.set(
                            userRoot.collection(C_ACH).document(row.achievementId),
                            row.toMap(),
                            SetOptions.merge(),
                        )
                    }

                if (completed.isDaily) {
                    // Don't derive the dateKey from wall-clock at push time —
                    // a player crossing local midnight (or moving time zones)
                    // would have the daily row keyed by the date the puzzle
                    // started, which we can't recover here. The most recent
                    // row is the one this event just produced.
                    dailyDao.getMostRecent()?.let { row ->
                        batch.set(
                            userRoot.collection(C_DAILY).document(row.dateKey),
                            row.toMap(),
                            SetOptions.merge(),
                        )
                    }
                }

                batch.commit().await()
                setState(SyncState.Idle, lastSyncedAt = now())
            } catch (e: Exception) {
                Log.w(TAG, "onGameEvent failed: ${e.message}")
                setState(SyncState.Error, errorMessage = e.message)
            }
        }
    }

    /** Writes the entire local DB to Firestore in one batch. Caller holds [mutex]. */
    private suspend fun pushLocalSnapshot(uid: String) {
        val userRoot = firestore.collection(USERS).document(uid)
        val batch = firestore.batch()
        statsDao.getAll().forEach { row ->
            batch.set(
                userRoot.collection(C_STATS).document(row.statsKey()),
                row.toMap(),
                SetOptions.merge(),
            )
        }
        achievementDao.getAll().forEach { row ->
            batch.set(
                userRoot.collection(C_ACH).document(row.achievementId),
                row.toMap(),
                SetOptions.merge(),
            )
        }
        dailyDao.getAll().forEach { row ->
            batch.set(
                userRoot.collection(C_DAILY).document(row.dateKey),
                row.toMap(),
                SetOptions.merge(),
            )
        }
        batch.commit().await()
    }

    private suspend fun currentUid(): String? =
        when (val s = authSession.state.value) {
            is AuthState.SignedIn -> s.uid
            else -> authSession.ensureSignedInAnonymously()
        }

    private fun setState(state: SyncState, lastSyncedAt: Long? = null, errorMessage: String? = null) {
        _status.update {
            it.copy(
                state = state,
                lastSyncedAt = lastSyncedAt ?: it.lastSyncedAt,
                errorMessage = errorMessage,
            )
        }
    }

    private companion object {
        const val TAG = "FirestoreSync"
        const val USERS = "users"
        const val C_STATS = "stats"
        const val C_ACH = "achievements"
        const val C_DAILY = "daily"
    }
}

// =============================================================================
// Mapping helpers (entity ↔ Firestore document)
// =============================================================================

private fun StatsEntity.statsKey(): String = "${variant}_${difficulty}"

private fun StatsEntity.toMap(): Map<String, Any?> = mapOf(
    "variant" to variant,
    "difficulty" to difficulty,
    "gamesStarted" to gamesStarted.toLong(),
    "gamesWon" to gamesWon.toLong(),
    "gamesFailed" to gamesFailed.toLong(),
    "bestTimeMs" to bestTimeMs,
    "totalTimeMs" to totalTimeMs,
    "currentStreak" to currentStreak.toLong(),
    "bestStreak" to bestStreak.toLong(),
    "lastPlayedAt" to lastPlayedAt,
)

private fun DocumentSnapshot.toStatsEntity(): StatsEntity? {
    val variant = getString("variant") ?: return null
    val difficulty = getString("difficulty") ?: return null
    return StatsEntity(
        variant = variant,
        difficulty = difficulty,
        gamesStarted = getLong("gamesStarted")?.toInt() ?: 0,
        gamesWon = getLong("gamesWon")?.toInt() ?: 0,
        gamesFailed = getLong("gamesFailed")?.toInt() ?: 0,
        bestTimeMs = getLong("bestTimeMs"),
        totalTimeMs = getLong("totalTimeMs") ?: 0L,
        currentStreak = getLong("currentStreak")?.toInt() ?: 0,
        bestStreak = getLong("bestStreak")?.toInt() ?: 0,
        lastPlayedAt = getLong("lastPlayedAt"),
    )
}

private fun AchievementProgressEntity.toMap(): Map<String, Any?> = mapOf(
    "achievementId" to achievementId,
    "unlocked" to unlocked,
    "progress" to progress.toLong(),
    "unlockedAt" to unlockedAt,
)

private fun DocumentSnapshot.toAchievementEntity(): AchievementProgressEntity? {
    val id = getString("achievementId") ?: return null
    return AchievementProgressEntity(
        achievementId = id,
        unlocked = getBoolean("unlocked") ?: false,
        progress = getLong("progress")?.toInt() ?: 0,
        unlockedAt = getLong("unlockedAt"),
    )
}

private fun DailyCompletionEntity.toMap(): Map<String, Any?> = mapOf(
    "dateKey" to dateKey,
    "variant" to variant,
    "difficulty" to difficulty,
    "seed" to seed,
    "result" to result,
    "elapsedMs" to elapsedMs,
    "mistakes" to mistakes.toLong(),
    "completedAt" to completedAt,
)

private fun DocumentSnapshot.toDailyEntity(): DailyCompletionEntity? {
    val dateKey = getString("dateKey") ?: return null
    val variant = getString("variant") ?: return null
    val difficulty = getString("difficulty") ?: return null
    val result = getString("result") ?: return null
    return DailyCompletionEntity(
        dateKey = dateKey,
        variant = variant,
        difficulty = difficulty,
        seed = getLong("seed") ?: 0L,
        result = result,
        elapsedMs = getLong("elapsedMs") ?: 0L,
        mistakes = getLong("mistakes")?.toInt() ?: 0,
        completedAt = getLong("completedAt") ?: 0L,
    )
}

// =============================================================================
// Conflict resolution
// =============================================================================

/**
 * Stats merge: monotonic max for counters; min for best time; the side with
 * the more recent lastPlayedAt wins for currentStreak (since "streak now" is
 * a snapshot, not a counter and max(5, 0-after-fail) would lie).
 */
private fun StatsEntity.merge(other: StatsEntity): StatsEntity {
    val mineNewer = (lastPlayedAt ?: 0L) >= (other.lastPlayedAt ?: 0L)
    val newer = if (mineNewer) this else other
    return StatsEntity(
        variant = variant,
        difficulty = difficulty,
        gamesStarted = max(gamesStarted, other.gamesStarted),
        gamesWon = max(gamesWon, other.gamesWon),
        gamesFailed = max(gamesFailed, other.gamesFailed),
        bestTimeMs = listOfNotNull(bestTimeMs, other.bestTimeMs).minOrNull(),
        totalTimeMs = max(totalTimeMs, other.totalTimeMs),
        currentStreak = newer.currentStreak,
        bestStreak = max(bestStreak, other.bestStreak),
        lastPlayedAt = listOfNotNull(lastPlayedAt, other.lastPlayedAt).maxOrNull(),
    )
}

/** Achievement merge: once unlocked, stay unlocked, with the earliest unlockedAt. */
private fun AchievementProgressEntity.merge(other: AchievementProgressEntity): AchievementProgressEntity {
    val unlocked = unlocked || other.unlocked
    val unlockedAt = if (unlocked) {
        listOfNotNull(unlockedAt, other.unlockedAt).minOrNull()
    } else null
    return AchievementProgressEntity(
        achievementId = achievementId,
        unlocked = unlocked,
        progress = max(progress, other.progress),
        unlockedAt = unlockedAt,
    )
}

/**
 * Daily merge: Won beats Failed (the user did complete the puzzle at some
 * point that day, even if another device thought they failed); ties broken
 * by earliest completedAt.
 */
private fun DailyCompletionEntity.merge(other: DailyCompletionEntity): DailyCompletionEntity {
    val mineWon = result == PuzzleResult.Won.name
    val otherWon = other.result == PuzzleResult.Won.name
    return when {
        mineWon && otherWon -> if (completedAt <= other.completedAt) this else other
        mineWon -> this
        otherWon -> other
        else -> if (completedAt <= other.completedAt) this else other
    }
}
