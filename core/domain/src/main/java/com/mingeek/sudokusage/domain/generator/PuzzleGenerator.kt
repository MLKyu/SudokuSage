package com.mingeek.sudokusage.domain.generator

import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId

/**
 * One generator per variant. Implementations live in their own module/package and
 * register through [com.mingeek.sudokusage.domain.board.VariantRegistry].
 *
 * Contract:
 *   - Output puzzle has a unique solution.
 *   - Same [seed] + same [variant] + same [difficulty] = identical puzzle (reproducible
 *     daily challenges and shareable seeds).
 */
interface PuzzleGenerator {
    suspend fun generate(
        variant: VariantId,
        difficulty: Difficulty,
        seed: Long? = null,
    ): Puzzle
}
