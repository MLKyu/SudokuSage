package com.mingeek.sudokusage.platform.sync

import com.mingeek.sudokusage.domain.event.GameEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SyncState { Idle, Syncing, Error }

data class SyncStatus(
    val state: SyncState = SyncState.Idle,
    val lastSyncedAt: Long? = null,
    val errorMessage: String? = null,
)

/**
 * Bidirectional cloud sync for stats, daily completions, and achievements.
 *
 * The interface separates bulk operations (manual "sync now") from scoped push
 * driven by [GameEvent]s. Scoped pushes write only the documents the event
 * actually changed, keeping free-tier Firestore quotas survivable (~8 writes
 * per PuzzleCompleted rather than the full local snapshot).
 */
interface CloudSyncProvider {
    val status: Flow<SyncStatus>

    /** Full local → remote snapshot. Use sparingly (manual "지금 동기화"). */
    suspend fun pushAll()

    /** Full remote → local merge. Runs on app start and after sign-in. */
    suspend fun pullAll()

    /**
     * Smart per-event push. Implementations write only documents affected by
     * [event] so a single game completion costs O(1) writes instead of O(n).
     */
    suspend fun onGameEvent(event: GameEvent)
}

class NoOpCloudSyncProvider : CloudSyncProvider {
    private val _status = MutableStateFlow(SyncStatus())
    override val status: Flow<SyncStatus> = _status.asStateFlow()
    override suspend fun pushAll() = Unit
    override suspend fun pullAll() = Unit
    override suspend fun onGameEvent(event: GameEvent) = Unit
}
