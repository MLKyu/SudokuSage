package com.mingeek.sudokusage.variant.classic

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ClassicSolverTest {

    private val solver = ClassicSolver()

    @Test
    fun solvesAKnownPuzzle() {
        // Classic example puzzle (NY Times "easy") with a unique solution.
        val puzzle = intArrayOf(
            5, 3, 0,  0, 7, 0,  0, 0, 0,
            6, 0, 0,  1, 9, 5,  0, 0, 0,
            0, 9, 8,  0, 0, 0,  0, 6, 0,

            8, 0, 0,  0, 6, 0,  0, 0, 3,
            4, 0, 0,  8, 0, 3,  0, 0, 1,
            7, 0, 0,  0, 2, 0,  0, 0, 6,

            0, 6, 0,  0, 0, 0,  2, 8, 0,
            0, 0, 0,  4, 1, 9,  0, 0, 5,
            0, 0, 0,  0, 8, 0,  0, 7, 9,
        )
        val expected = intArrayOf(
            5, 3, 4,  6, 7, 8,  9, 1, 2,
            6, 7, 2,  1, 9, 5,  3, 4, 8,
            1, 9, 8,  3, 4, 2,  5, 6, 7,

            8, 5, 9,  7, 6, 1,  4, 2, 3,
            4, 2, 6,  8, 5, 3,  7, 9, 1,
            7, 1, 3,  9, 2, 4,  8, 5, 6,

            9, 6, 1,  5, 3, 7,  2, 8, 4,
            2, 8, 7,  4, 1, 9,  6, 3, 5,
            3, 4, 5,  2, 8, 6,  1, 7, 9,
        )
        val solved = solver.solveOne(puzzle)
        assertNotNull(solved)
        assertArrayEquals(expected, solved)
    }

    @Test
    fun countsExactlyOneSolutionForUniqueClues() {
        val puzzle = intArrayOf(
            5, 3, 0,  0, 7, 0,  0, 0, 0,
            6, 0, 0,  1, 9, 5,  0, 0, 0,
            0, 9, 8,  0, 0, 0,  0, 6, 0,
            8, 0, 0,  0, 6, 0,  0, 0, 3,
            4, 0, 0,  8, 0, 3,  0, 0, 1,
            7, 0, 0,  0, 2, 0,  0, 0, 6,
            0, 6, 0,  0, 0, 0,  2, 8, 0,
            0, 0, 0,  4, 1, 9,  0, 0, 5,
            0, 0, 0,  0, 8, 0,  0, 7, 9,
        )
        assertEquals(1, solver.countSolutions(puzzle, limit = 2))
    }

    @Test
    fun emptyGridHasManySolutions() {
        val empty = IntArray(81)
        // Just verify > 1 (we cap at limit so 2 = "many")
        assertEquals(2, solver.countSolutions(empty, limit = 2))
    }

    @Test
    fun unsolvableGridReturnsNull() {
        // Two 1s in row 0 — invalid clues
        val grid = IntArray(81)
        grid[0] = 1
        grid[1] = 1
        assertNull(solver.solveOne(grid))
    }
}
