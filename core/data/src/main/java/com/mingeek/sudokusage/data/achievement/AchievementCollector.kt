package com.mingeek.sudokusage.data.achievement

import com.mingeek.sudokusage.data.repo.AchievementRepository
import com.mingeek.sudokusage.domain.achievement.AchievementCatalog
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.event.GameEvent
import com.mingeek.sudokusage.domain.event.GameEventBus
import com.mingeek.sudokusage.domain.event.PuzzleResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Subscribes to [GameEventBus] for the lifetime of the app and unlocks any
 * achievement whose criteria the event satisfies. Idempotent — repository
 * short-circuits already-unlocked rows so re-evaluating events is harmless.
 */
class AchievementCollector(
    private val eventBus: GameEventBus,
    private val repo: AchievementRepository,
    private val scope: CoroutineScope,
) {
    fun start() {
        eventBus.events
            .onEach { event -> scope.launch { evaluate(event) } }
            .launchIn(scope)
    }

    private suspend fun evaluate(event: GameEvent) {
        when (event) {
            is GameEvent.PuzzleCompleted -> if (event.result == PuzzleResult.Won) {
                repo.unlock(AchievementCatalog.FirstWin.id)
                if (event.mistakes == 0) repo.unlock(AchievementCatalog.PerfectGame.id)
                if (event.difficulty == Difficulty.Master) repo.unlock(AchievementCatalog.MasterTier.id)
                if (event.difficulty == Difficulty.Extreme) repo.unlock(AchievementCatalog.ExtremeTier.id)
                if (event.isDaily) repo.unlock(AchievementCatalog.DailyFirst.id)
            }
            is GameEvent.HintUsed -> repo.unlock(AchievementCatalog.HintNovice.id)
            else -> Unit
        }
    }
}
