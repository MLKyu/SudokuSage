package com.mingeek.sudokusage.data.stats

import com.mingeek.sudokusage.data.repo.StatsRepository
import com.mingeek.sudokusage.domain.event.GameEvent
import com.mingeek.sudokusage.domain.event.GameEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Subscribes to [GameEventBus] for the lifetime of the app process and feeds
 * [StatsRepository]. Pure side-effect; no public surface.
 */
class StatsCollector(
    private val eventBus: GameEventBus,
    private val repository: StatsRepository,
    private val scope: CoroutineScope,
) {
    fun start() {
        eventBus.events
            .onEach { event ->
                when (event) {
                    is GameEvent.PuzzleStarted -> scope.launch {
                        repository.recordStarted(event.variant, event.difficulty)
                    }
                    is GameEvent.PuzzleCompleted -> scope.launch {
                        repository.recordCompleted(
                            event.variant,
                            event.difficulty,
                            event.result,
                            event.elapsedMs,
                        )
                    }
                    else -> Unit
                }
            }
            .launchIn(scope)
    }
}
