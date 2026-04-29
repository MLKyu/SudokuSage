package com.mingeek.sudokusage.game

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Cell
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.GameState
import com.mingeek.sudokusage.domain.board.GameStatus
import com.mingeek.sudokusage.domain.board.Move
import com.mingeek.sudokusage.domain.board.VariantId
import com.mingeek.sudokusage.domain.event.DefaultGameEventBus
import com.mingeek.sudokusage.variant.classic.ClassicRuleSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private val rules = ClassicRuleSet()
    private val engine = GameEngine(rules, DefaultGameEventBus(), now = { 0L })

    private fun emptyState(): GameState {
        val initial = Board.empty(9)
        // Build a solution that has 5 at (0,0) for our tests.
        val solution = Board(9, (0 until 81).map { i ->
            val r = i / 9; val c = i % 9
            // Trivial valid solution: (r * 3 + r / 3 + c) mod 9 + 1
            val v = ((r * 3 + r / 3 + c) % 9) + 1
            Cell(r, c, given = v)
        })
        return GameState(
            initial = initial,
            board = initial,
            solution = solution,
            variant = VariantId.Classic,
            difficulty = Difficulty.Easy,
            startedAt = 0L,
            seed = 0L,
        )
    }

    @Test
    fun placeWritesValueAndAddsToHistory() {
        val s0 = emptyState()
        val correctValue = s0.solution.cellAt(0, 0).displayValue!!
        val s1 = engine.apply(s0, Move.Place(CellRef(0, 0), correctValue))
        assertEquals(correctValue, s1.board.cellAt(0, 0).value)
        assertEquals(1, s1.moveHistory.size)
        assertEquals(0, s1.mistakes)
    }

    @Test
    fun placeWrongValueIncrementsMistakes() {
        val s0 = emptyState()
        val wrong = (s0.solution.cellAt(0, 0).displayValue!! % 9) + 1
        val s1 = engine.apply(s0, Move.Place(CellRef(0, 0), wrong))
        assertEquals(1, s1.mistakes)
        assertEquals(GameStatus.Playing, s1.status)
    }

    @Test
    fun mistakeLimitTriggersFailure() {
        var s = emptyState().copy(mistakeLimit = 1)
        val wrong = (s.solution.cellAt(0, 0).displayValue!! % 9) + 1
        s = engine.apply(s, Move.Place(CellRef(0, 0), wrong))
        assertEquals(GameStatus.Failed, s.status)
    }

    @Test
    fun undoRestoresPriorBoard() {
        val s0 = emptyState()
        val correct = s0.solution.cellAt(0, 0).displayValue!!
        val s1 = engine.apply(s0, Move.Place(CellRef(0, 0), correct))
        val s2 = engine.undo(s1)
        assertNull(s2.board.cellAt(0, 0).value)
        assertEquals(0, s2.moveHistory.size)
        assertEquals(1, s2.redoStack.size)
    }

    @Test
    fun redoReappliesMove() {
        val s0 = emptyState()
        val correct = s0.solution.cellAt(0, 0).displayValue!!
        val s1 = engine.apply(s0, Move.Place(CellRef(0, 0), correct))
        val s2 = engine.undo(s1)
        val s3 = engine.redo(s2)
        assertEquals(correct, s3.board.cellAt(0, 0).value)
        assertEquals(0, s3.redoStack.size)
    }

    @Test
    fun toggleNoteAddsAndRemoves() {
        val s0 = emptyState()
        val s1 = engine.apply(s0, Move.ToggleNote(CellRef(1, 1), 5))
        assertTrue(5 in s1.board.cellAt(1, 1).notes)
        val s2 = engine.apply(s1, Move.ToggleNote(CellRef(1, 1), 5))
        assertTrue(5 !in s2.board.cellAt(1, 1).notes)
    }

    @Test
    fun placingValueClearsPeerNotes() {
        val s0 = emptyState()
        // Put note "7" at (0, 5) (same row as (0, 0))
        val s1 = engine.apply(s0, Move.ToggleNote(CellRef(0, 5), 7))
        assertTrue(7 in s1.board.cellAt(0, 5).notes)
        // Place "7" at (0, 0)
        val s2 = engine.apply(s1, Move.Place(CellRef(0, 0), 7))
        // Note should be auto-cleared from the peer
        assertTrue(7 !in s2.board.cellAt(0, 5).notes)
    }

    @Test
    fun pauseAndResumeTransitionsStatus() {
        val s0 = emptyState()
        val paused = engine.pause(s0)
        assertEquals(GameStatus.Paused, paused.status)
        val resumed = engine.resume(paused)
        assertEquals(GameStatus.Playing, resumed.status)
    }

    @Test
    fun cannotModifyGivens() {
        val initial = Board(9, (0 until 81).map { i ->
            val r = i / 9; val c = i % 9
            if (r == 0 && c == 0) Cell(r, c, given = 5) else Cell(r, c)
        })
        val s0 = emptyState().copy(board = initial, initial = initial)
        val s1 = engine.apply(s0, Move.Place(CellRef(0, 0), 9))
        assertEquals(s0, s1)  // unchanged
    }
}
