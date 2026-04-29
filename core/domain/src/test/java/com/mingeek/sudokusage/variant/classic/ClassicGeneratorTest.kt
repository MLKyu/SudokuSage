package com.mingeek.sudokusage.variant.classic

import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassicGeneratorTest {

    private val solver = ClassicSolver()
    private val generator = ClassicGenerator(solver)

    @Test
    fun generatedEasyPuzzleHasUniqueSolution() = runBlocking {
        val puzzle = generator.generate(VariantId.Classic, Difficulty.Easy, seed = 42L)
        val initialArray = IntArray(81) { i ->
            puzzle.initial.cells[i].displayValue ?: 0
        }
        assertEquals(1, solver.countSolutions(initialArray, limit = 2))
    }

    @Test
    fun generatedMediumPuzzleHasUniqueSolution() = runBlocking {
        val puzzle = generator.generate(VariantId.Classic, Difficulty.Medium, seed = 123L)
        val initialArray = IntArray(81) { i ->
            puzzle.initial.cells[i].displayValue ?: 0
        }
        assertEquals(1, solver.countSolutions(initialArray, limit = 2))
    }

    @Test
    fun sameSeedReproducesSamePuzzle() = runBlocking {
        val a = generator.generate(VariantId.Classic, Difficulty.Easy, seed = 7L)
        val b = generator.generate(VariantId.Classic, Difficulty.Easy, seed = 7L)
        val aArr = a.initial.cells.map { it.displayValue ?: 0 }
        val bArr = b.initial.cells.map { it.displayValue ?: 0 }
        assertEquals(aArr, bArr)
    }

    @Test
    fun puzzleIsAlwaysSolvable() = runBlocking {
        val puzzle = generator.generate(VariantId.Classic, Difficulty.Easy, seed = 99L)
        val solutionArray = IntArray(81) { i ->
            puzzle.solution.cells[i].displayValue ?: 0
        }
        // Solution should already be complete
        assertTrue(solutionArray.all { it in 1..9 })
    }
}
