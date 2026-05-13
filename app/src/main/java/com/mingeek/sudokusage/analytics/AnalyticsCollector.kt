package com.mingeek.sudokusage.analytics

import com.mingeek.sudokusage.domain.event.GameEvent
import com.mingeek.sudokusage.domain.event.GameEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Subscribes to [GameEventBus] and forwards game lifecycle events into
 * [Analytics]. Mirrors the pattern used by StatsCollector / AchievementCollector —
 * one app-level side-effect, no public surface.
 */
class AnalyticsCollector(
    private val eventBus: GameEventBus,
    private val analytics: Analytics,
    private val scope: CoroutineScope,
) {
    fun start() {
        eventBus.events.onEach { event ->
            when (event) {
                is GameEvent.PuzzleStarted -> analytics.track(
                    SudokuEvents.PUZZLE_STARTED,
                    mapOf(
                        SudokuEvents.PARAM_VARIANT to event.variant.value,
                        SudokuEvents.PARAM_DIFFICULTY to event.difficulty.name,
                    ),
                )

                is GameEvent.PuzzleCompleted -> analytics.track(
                    SudokuEvents.PUZZLE_COMPLETED,
                    mapOf(
                        SudokuEvents.PARAM_VARIANT to event.variant.value,
                        SudokuEvents.PARAM_DIFFICULTY to event.difficulty.name,
                        SudokuEvents.PARAM_RESULT to event.result.name,
                        SudokuEvents.PARAM_ELAPSED_SEC to (event.elapsedMs / 1000L),
                        SudokuEvents.PARAM_MISTAKES to event.mistakes,
                        SudokuEvents.PARAM_HINTS_USED to event.hintsUsed,
                        SudokuEvents.PARAM_IS_DAILY to event.isDaily,
                    ),
                )

                is GameEvent.HintUsed -> analytics.track(
                    SudokuEvents.HINT_USED,
                    mapOf(SudokuEvents.PARAM_TECHNIQUE to event.techniqueId),
                )

                else -> Unit
            }
        }.launchIn(scope)
    }
}
