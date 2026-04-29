package com.mingeek.sudokusage.game

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.GameState
import com.mingeek.sudokusage.domain.board.GameStatus
import com.mingeek.sudokusage.domain.board.Move
import com.mingeek.sudokusage.domain.board.RuleSet
import com.mingeek.sudokusage.domain.event.GameEvent
import com.mingeek.sudokusage.domain.event.GameEventBus
import com.mingeek.sudokusage.domain.event.PuzzleResult

/**
 * Pure-ish reducer over [GameState]. The only side effect is event emission to
 * [GameEventBus]; no IO, no time queries (timestamps come from [now]).
 *
 * Undo is replay-based: history stores [Move]s, and undo replays from
 * [GameState.initial]. This keeps history compact and inverse-free.
 */
class GameEngine(
    private val rules: RuleSet,
    private val eventBus: GameEventBus,
    private val now: () -> Long = System::currentTimeMillis,
) {

    fun apply(state: GameState, move: Move): GameState =
        applyInternal(state, move, recordToHistory = true, clearRedoStack = true)

    fun undo(state: GameState): GameState {
        if (state.moveHistory.isEmpty()) return state
        val undone = state.moveHistory.last()
        val history = state.moveHistory.dropLast(1)
        val replayed = replay(state, history)
        return replayed.copy(redoStack = state.redoStack + undone)
    }

    fun redo(state: GameState): GameState {
        if (state.redoStack.isEmpty()) return state
        val redone = state.redoStack.last()
        val newRedo = state.redoStack.dropLast(1)
        val applied = applyInternal(state, redone, recordToHistory = true, clearRedoStack = false)
        return applied.copy(redoStack = newRedo)
    }

    fun pause(state: GameState): GameState {
        if (state.status != GameStatus.Playing) return state
        eventBus.emit(GameEvent.PuzzlePaused(now()))
        return state.copy(status = GameStatus.Paused)
    }

    fun resume(state: GameState): GameState {
        if (state.status != GameStatus.Paused) return state
        eventBus.emit(GameEvent.PuzzleResumed(now()))
        return state.copy(status = GameStatus.Playing)
    }

    private fun applyInternal(
        state: GameState,
        move: Move,
        recordToHistory: Boolean,
        clearRedoStack: Boolean,
    ): GameState {
        if (state.status != GameStatus.Playing) return state
        val cell = state.board.cellAt(move.ref)
        if (cell.isGiven) return state

        val updatedCell = when (move) {
            is Move.Place -> {
                if (move.value !in rules.symbols) return state
                cell.copy(value = move.value, notes = emptySet())
            }
            is Move.Erase -> cell.copy(value = null, notes = emptySet())
            is Move.ToggleNote -> {
                if (cell.value != null) return state
                if (move.note !in rules.symbols) return state
                val notes = if (move.note in cell.notes) cell.notes - move.note else cell.notes + move.note
                cell.copy(notes = notes)
            }
        }
        var newBoard = state.board.withCell(updatedCell)
        var mistakes = state.mistakes
        val ts = now()

        when (move) {
            is Move.Place -> {
                newBoard = autoClearPeerNotes(newBoard, move.ref, move.value)
                val correct = state.solution.cellAt(move.ref).displayValue
                if (correct != null && move.value != correct) {
                    mistakes++
                    eventBus.emit(GameEvent.MistakeMade(move.ref, move.value, ts))
                } else {
                    eventBus.emit(GameEvent.CellPlaced(move.ref, move.value, ts))
                }
            }
            is Move.Erase -> eventBus.emit(GameEvent.CellErased(move.ref, ts))
            is Move.ToggleNote -> eventBus.emit(GameEvent.NoteToggled(move.ref, move.note, ts))
        }

        val limit = state.mistakeLimit
        val status = when {
            limit != null && mistakes >= limit -> GameStatus.Failed
            rules.isSolved(newBoard, state.solution) -> GameStatus.Won
            else -> state.status
        }

        if (status == GameStatus.Won || status == GameStatus.Failed) {
            eventBus.emit(
                GameEvent.PuzzleCompleted(
                    variant = state.variant,
                    difficulty = state.difficulty,
                    result = if (status == GameStatus.Won) PuzzleResult.Won else PuzzleResult.Failed,
                    elapsedMs = state.elapsedMs,
                    mistakes = mistakes,
                    hintsUsed = state.hintsUsed,
                    timestamp = ts,
                    isDaily = state.dailyDate != null,
                )
            )
        }

        return state.copy(
            board = newBoard,
            mistakes = mistakes,
            status = status,
            moveHistory = if (recordToHistory) state.moveHistory + move else state.moveHistory,
            redoStack = if (clearRedoStack) emptyList() else state.redoStack,
        )
    }

    /** Rebuild board state by replaying [moves] from [GameState.initial]. */
    private fun replay(seed: GameState, moves: List<Move>): GameState {
        var s = seed.copy(
            board = seed.initial,
            mistakes = 0,
            moveHistory = emptyList(),
            redoStack = emptyList(),
            status = GameStatus.Playing,
        )
        for (m in moves) {
            s = applyInternal(s, m, recordToHistory = true, clearRedoStack = true)
        }
        return s.copy(elapsedMs = seed.elapsedMs)
    }

    private fun autoClearPeerNotes(board: Board, ref: CellRef, value: Int): Board {
        val peers: Set<CellRef> = rules.regions().asSequence()
            .filter { region -> region.cells.any { it == ref } }
            .flatMap { it.cells.asSequence() }
            .filter { it != ref }
            .toSet()
        var b = board
        for (peer in peers) {
            val cell = b.cellAt(peer)
            if (value in cell.notes) {
                b = b.withCell(cell.copy(notes = cell.notes - value))
            }
        }
        return b
    }
}
