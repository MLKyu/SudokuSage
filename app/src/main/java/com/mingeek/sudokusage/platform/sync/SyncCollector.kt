package com.mingeek.sudokusage.platform.sync

import com.mingeek.sudokusage.data.db.StatsDao
import com.mingeek.sudokusage.domain.event.GameEvent
import com.mingeek.sudokusage.domain.event.GameEventBus
import com.mingeek.sudokusage.featureflags.FeatureFlags
import com.mingeek.sudokusage.featureflags.FlagKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Subscribes to [GameEventBus] for the lifetime of the app process and pushes
 * the per-event slice of cloud state to [CloudSyncProvider]. Mirrors the shape
 * of StatsCollector / AchievementCollector so all three see the same event
 * stream.
 *
 * Gated by [FlagKeys.EnableCloudSync] so the cloud path can be turned off
 * remotely without code change. Only [GameEvent.PuzzleCompleted] is forwarded;
 * the provider itself filters the rest, but checking here avoids the coroutine
 * launch cost for the path-events that don't sync.
 *
 * **Ordering with sibling collectors.** StatsCollector / AchievementCollector
 * subscribe to the same SharedFlow and each launches its own DB write, so
 * naively calling `cloudSync.onGameEvent` would race those writes and push
 * pre-completion stats. Before pushing, we wait until the stats row for this
 * (variant, difficulty) has a `lastPlayedAt` at or after the event timestamp —
 * the signal that StatsCollector's write has landed. Bounded by [WRITE_WAIT_TIMEOUT_MS]
 * so a missing stats write never blocks sync forever; on timeout we push
 * anyway and let the next completion catch up.
 */
class SyncCollector(
    private val eventBus: GameEventBus,
    private val cloudSync: CloudSyncProvider,
    private val featureFlags: FeatureFlags,
    private val statsDao: StatsDao,
    private val scope: CoroutineScope,
) {
    fun start() {
        eventBus.events
            .onEach { event ->
                if (event !is GameEvent.PuzzleCompleted) return@onEach
                if (!featureFlags.bool(FlagKeys.EnableCloudSync, default = true)) return@onEach
                scope.launch {
                    awaitStatsWrite(event)
                    cloudSync.onGameEvent(event)
                }
            }
            .launchIn(scope)
    }

    private suspend fun awaitStatsWrite(event: GameEvent.PuzzleCompleted) {
        val deadline = System.currentTimeMillis() + WRITE_WAIT_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val row = statsDao.get(event.variant.value, event.difficulty.name)
            if (row != null && (row.lastPlayedAt ?: 0L) >= event.timestamp) return
            delay(POLL_INTERVAL_MS)
        }
    }

    private companion object {
        const val WRITE_WAIT_TIMEOUT_MS = 2_000L
        const val POLL_INTERVAL_MS = 25L
    }
}
