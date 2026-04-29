package com.mingeek.sudokusage.variant.killer

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Cell
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.Region
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KillerRuleSetTest {

    @Test
    fun `cage sums match the solution from the bank`() = runBlocking {
        val puzzle = KillerGenerator().generate(
            variant = com.mingeek.sudokusage.domain.board.VariantId.Killer,
            difficulty = Difficulty.Easy,
            seed = 0L,
        )
        for (cage in puzzle.cages) {
            val sum = cage.cells.sumOf { puzzle.solution.cellAt(it).displayValue ?: 0 }
            assertEquals("cage sum should match", cage.targetSum, sum)
        }
    }

    @Test
    fun `cages cover every cell exactly once`() = runBlocking {
        val puzzle = KillerGenerator().generate(
            variant = com.mingeek.sudokusage.domain.board.VariantId.Killer,
            difficulty = Difficulty.Medium,
            seed = 0L,
        )
        val seen = HashSet<CellRef>()
        for (cage in puzzle.cages) {
            for (ref in cage.cells) {
                assertTrue("no cell appears twice", seen.add(ref))
            }
        }
        assertEquals(81, seen.size)
    }

    @Test
    fun `solution board satisfies the ruleset`() = runBlocking {
        val puzzle = KillerGenerator().generate(
            variant = com.mingeek.sudokusage.domain.board.VariantId.Killer,
            difficulty = Difficulty.Easy,
            seed = 0L,
        )
        val rules = KillerRuleSet(puzzle.cages)
        assertTrue(rules.isComplete(puzzle.solution))
        assertTrue(rules.isSolved(puzzle.solution, puzzle.solution))
        assertTrue(rules.conflicts(puzzle.solution).isEmpty())
    }

    @Test
    fun `wrong cage sum produces conflicts when fully filled`() {
        val cages = listOf(
            Region.Cage(targetSum = 5, cells = listOf(CellRef(0, 0), CellRef(0, 1)))
        )
        val rules = KillerRuleSet(cages)
        // 1 + 1 — duplicate, but for sum check we want to test sum specifically.
        val board = boardWithDigits(mapOf(CellRef(0, 0) to 1, CellRef(0, 1) to 9))
        // 1+9=10 ≠ 5 → both cells should be conflicts.
        val cs = rules.conflicts(board)
        assertTrue(CellRef(0, 0) in cs)
        assertTrue(CellRef(0, 1) in cs)
    }

    @Test
    fun `partial cage with no duplicates is not yet a sum conflict`() {
        val cages = listOf(
            Region.Cage(targetSum = 5, cells = listOf(CellRef(0, 0), CellRef(0, 1)))
        )
        val rules = KillerRuleSet(cages)
        val board = boardWithDigits(mapOf(CellRef(0, 0) to 1))
        // Only one cell filled — sum constraint not yet checkable. No conflicts from cage.
        assertFalse(CellRef(0, 0) in rules.conflicts(board))
    }

    private fun boardWithDigits(values: Map<CellRef, Int>): Board {
        val cells = (0 until 81).map { i ->
            val r = i / 9
            val c = i % 9
            val v = values[CellRef(r, c)]
            Cell(row = r, col = c, value = v)
        }
        return Board(size = 9, cells = cells)
    }
}
