package com.mingeek.sudokusage.ui.screens.replay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.GameState
import com.mingeek.sudokusage.domain.board.GameStatus
import com.mingeek.sudokusage.domain.board.VariantRegistry
import com.mingeek.sudokusage.domain.event.GameEvent
import com.mingeek.sudokusage.domain.event.GameEventBus
import com.mingeek.sudokusage.game.GameEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ReplayUiState(
    val game: GameState,
    val step: Int,
    val totalSteps: Int,
    val board: Board,
    val boxRows: Int,
    val boxCols: Int,
)

class ReplayViewModel(
    private val source: MutableStateFlow<GameState?>,
    private val variantRegistry: VariantRegistry,
) : ViewModel() {

    private val silentBus = object : GameEventBus {
        private val flow = MutableSharedFlow<GameEvent>()
        override val events = flow.asSharedFlow()
        override fun emit(event: GameEvent) { /* drop */ }
    }

    private val _step = MutableStateFlow(Int.MAX_VALUE)

    val state: StateFlow<ReplayUiState?> = combine(source, _step) { game, requestedStep ->
        if (game == null) return@combine null
        val total = game.moveHistory.size
        val step = requestedStep.coerceIn(0, total)
        val rules = variantRegistry.rules(game.variant)
        ReplayUiState(
            game = game,
            step = step,
            totalSteps = total,
            board = computeBoardAt(game, step),
            boxRows = rules.boxRows,
            boxCols = rules.boxCols,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    fun setStep(step: Int) {
        val total = source.value?.moveHistory?.size ?: 0
        _step.value = step.coerceIn(0, total)
    }

    fun stepForward() {
        val current = _step.value
        val total = source.value?.moveHistory?.size ?: 0
        _step.value = (current + 1).coerceAtMost(total)
    }

    fun stepBack() {
        val current = _step.value
        _step.value = (current - 1).coerceAtLeast(0)
    }

    private fun computeBoardAt(game: GameState, step: Int): Board {
        val rules = variantRegistry.rules(game.variant)
        val engine = GameEngine(rules, silentBus)
        var state = game.copy(
            board = game.initial,
            moveHistory = emptyList(),
            redoStack = emptyList(),
            mistakes = 0,
            mistakeLimit = null,
            status = GameStatus.Playing,
            elapsedMs = 0L,
        )
        for (i in 0 until step.coerceAtMost(game.moveHistory.size)) {
            state = engine.apply(state, game.moveHistory[i])
        }
        return state.board
    }
}
