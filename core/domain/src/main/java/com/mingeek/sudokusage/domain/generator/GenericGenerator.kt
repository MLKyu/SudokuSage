package com.mingeek.sudokusage.domain.generator

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Cell
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.GenericSolver
import com.mingeek.sudokusage.domain.board.RuleSet
import com.mingeek.sudokusage.domain.board.VariantId
import kotlin.random.Random

/**
 * Variant-agnostic generator. Solves an empty board with shuffled candidate
 * order to produce a varied solution, then erases cells while [solver]
 * confirms the puzzle still has a unique solution. Clue-count buckets per
 * difficulty are scaled by board size.
 */
open class GenericGenerator(
    private val rules: RuleSet,
    private val solver: GenericSolver = GenericSolver(rules),
) : PuzzleGenerator {

    override suspend fun generate(
        variant: VariantId,
        difficulty: Difficulty,
        seed: Long?,
    ): Puzzle {
        require(variant == rules.id) {
            "Generator was built for ${rules.id.value}; received ${variant.value}"
        }
        val actualSeed = seed ?: System.nanoTime()
        val random = Random(actualSeed)
        val n = rules.boardSize

        val solution = solver.solveOne(IntArray(n * n), random)
            ?: error("RuleSet ${rules.id.value} has no solution from the empty grid; constraints over-specified")
        val initial = removeCells(solution, clueCountFor(difficulty), random)

        return Puzzle(
            variant = variant,
            difficulty = difficulty,
            seed = actualSeed,
            initial = boardFromIntArray(initial),
            solution = boardFromIntArray(solution),
        )
    }

    private fun removeCells(solution: IntArray, targetClues: Int, random: Random): IntArray {
        val n = rules.boardSize
        val total = n * n
        val puzzle = solution.copyOf()
        val toRemove = (total - targetClues).coerceAtLeast(0)
        val indices = (0 until total).toMutableList().also { it.shuffle(random) }
        var removed = 0
        for (idx in indices) {
            if (removed >= toRemove) break
            val saved = puzzle[idx]
            puzzle[idx] = 0
            if (solver.countSolutions(puzzle, limit = 2) != 1) {
                puzzle[idx] = saved
            } else {
                removed++
            }
        }
        return puzzle
    }

    /**
     * Clue counts per difficulty. Tuned for plain box variants; extra-constraint
     * variants (X-Sudoku, Hyper) produce harder puzzles at the same clue count
     * because each clue propagates further. Subclasses override if needed.
     */
    protected open fun clueCountFor(difficulty: Difficulty): Int = when (rules.boardSize) {
        9 -> when (difficulty) {
            Difficulty.Easy -> 38
            Difficulty.Medium -> 32
            Difficulty.Hard -> 28
            Difficulty.Expert -> 25
            Difficulty.Master -> 23
            Difficulty.Extreme -> 21
        }
        6 -> when (difficulty) {
            Difficulty.Easy -> 22
            Difficulty.Medium -> 18
            Difficulty.Hard -> 16
            Difficulty.Expert -> 14
            Difficulty.Master -> 13
            Difficulty.Extreme -> 12
        }
        4 -> when (difficulty) {
            Difficulty.Easy -> 9
            Difficulty.Medium -> 8
            Difficulty.Hard -> 7
            Difficulty.Expert -> 6
            Difficulty.Master -> 6
            Difficulty.Extreme -> 5
        }
        else -> rules.boardSize * rules.boardSize / 3
    }

    private fun boardFromIntArray(grid: IntArray): Board {
        val n = rules.boardSize
        val cells = (0 until n * n).map { i ->
            val r = i / n
            val c = i % n
            val v = grid[i].takeIf { it != 0 }
            Cell(r, c, given = v)
        }
        return Board(n, cells)
    }
}
