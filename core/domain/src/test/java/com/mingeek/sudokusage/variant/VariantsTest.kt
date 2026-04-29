package com.mingeek.sudokusage.variant

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Cell
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.GenericSolver
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.domain.board.VariantId
import com.mingeek.sudokusage.domain.generator.GenericGenerator
import com.mingeek.sudokusage.variant.hyper.HyperRuleSet
import com.mingeek.sudokusage.variant.mini.Mini4RuleSet
import com.mingeek.sudokusage.variant.mini.Mini6RuleSet
import com.mingeek.sudokusage.variant.x.XSudokuRuleSet
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Mini6Test {
    private val rules = Mini6RuleSet()
    private val solver = GenericSolver(rules)
    private val generator = GenericGenerator(rules, solver)

    @Test
    fun boxesAreTwoByThree() {
        val boxes = rules.regions().filterIsInstance<Region.Box>()
        assertEquals(6, boxes.size)
        // Each box: 6 cells (2 rows * 3 cols)
        assertTrue(boxes.all { it.cells.size == 6 })
    }

    @Test
    fun generatedPuzzleHasUniqueSolution() = runBlocking {
        val puzzle = generator.generate(VariantId.Mini6, Difficulty.Easy, seed = 42L)
        val initial = IntArray(36) { i -> puzzle.initial.cells[i].displayValue ?: 0 }
        assertEquals(1, solver.countSolutions(initial, limit = 2))
    }
}

class Mini4Test {
    private val rules = Mini4RuleSet()
    private val solver = GenericSolver(rules)
    private val generator = GenericGenerator(rules, solver)

    @Test
    fun boxesAreTwoByTwo() {
        val boxes = rules.regions().filterIsInstance<Region.Box>()
        assertEquals(4, boxes.size)
        assertTrue(boxes.all { it.cells.size == 4 })
    }

    @Test
    fun generatedPuzzleHasUniqueSolution() = runBlocking {
        val puzzle = generator.generate(VariantId.Mini4, Difficulty.Easy, seed = 7L)
        val initial = IntArray(16) { i -> puzzle.initial.cells[i].displayValue ?: 0 }
        assertEquals(1, solver.countSolutions(initial, limit = 2))
    }
}

class XSudokuTest {
    private val rules = XSudokuRuleSet()

    @Test
    fun hasTwoDiagonalRegions() {
        val diagonals = rules.regions().filterIsInstance<Region.Diagonal>()
        assertEquals(2, diagonals.size)
    }

    @Test
    fun diagonalConflictDetected() {
        // Place two 5s on the main diagonal: (0,0) and (4,4).
        val cells = (0 until 81).map { i ->
            val r = i / 9; val c = i % 9
            val v = when {
                r == 0 && c == 0 -> 5
                r == 4 && c == 4 -> 5
                else -> null
            }
            Cell(r, c, value = v)
        }
        val conflicts = rules.conflicts(Board(9, cells))
        assertTrue(CellRef(0, 0) in conflicts)
        assertTrue(CellRef(4, 4) in conflicts)
    }
}

class HyperTest {
    private val rules = HyperRuleSet()

    @Test
    fun hasFourExtraHyperBoxes() {
        val custom = rules.regions().filterIsInstance<Region.Custom>()
        assertEquals(4, custom.size)
        assertTrue(custom.all { it.tag.startsWith("hyper-") })
        assertTrue(custom.all { it.cells.size == 9 })
    }

    @Test
    fun hyperBoxConflictDetected() {
        // Hyper box 0 is rows 1..3, cols 1..3. Place two 7s within: (1,1) and (3,3).
        // Note: also must avoid normal box / row / column conflicts; (1,1) and (3,3)
        // are in different normal boxes/rows/cols, so the only collision is the hyper box.
        val cells = (0 until 81).map { i ->
            val r = i / 9; val c = i % 9
            val v = when {
                r == 1 && c == 1 -> 7
                r == 3 && c == 3 -> 7
                else -> null
            }
            Cell(r, c, value = v)
        }
        val conflicts = rules.conflicts(Board(9, cells))
        assertTrue(CellRef(1, 1) in conflicts)
        assertTrue(CellRef(3, 3) in conflicts)
    }
}
