package com.mingeek.sudokusage.variant.classic

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Cell
import com.mingeek.sudokusage.domain.board.CellRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassicRuleSetTest {

    private val rules = ClassicRuleSet()

    @Test
    fun emptyBoardHasNoConflicts() {
        assertTrue(rules.conflicts(Board.empty(9)).isEmpty())
    }

    @Test
    fun duplicateInRowIsConflict() {
        val cells = (0 until 81).map { i ->
            val r = i / 9; val c = i % 9
            val v = when {
                r == 0 && c == 0 -> 5
                r == 0 && c == 5 -> 5
                else -> null
            }
            Cell(r, c, value = v)
        }
        val conflicts = rules.conflicts(Board(9, cells))
        assertTrue(CellRef(0, 0) in conflicts)
        assertTrue(CellRef(0, 5) in conflicts)
        assertEquals(2, conflicts.size)
    }

    @Test
    fun duplicateInBoxIsConflict() {
        val cells = (0 until 81).map { i ->
            val r = i / 9; val c = i % 9
            val v = when {
                r == 0 && c == 0 -> 7
                r == 2 && c == 2 -> 7   // same 3x3 box
                else -> null
            }
            Cell(r, c, value = v)
        }
        val conflicts = rules.conflicts(Board(9, cells))
        assertTrue(CellRef(0, 0) in conflicts)
        assertTrue(CellRef(2, 2) in conflicts)
    }

    @Test
    fun incompleteBoardIsNotComplete() {
        assertFalse(rules.isComplete(Board.empty(9)))
    }
}
