package com.mingeek.sudokusage.domain.generator

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.domain.board.VariantId

/**
 * A generated puzzle — the initial clue board plus its unique solution.
 * Generators must guarantee uniqueness.
 */
data class Puzzle(
    val variant: VariantId,
    val difficulty: Difficulty,
    val seed: Long,
    val initial: Board,
    val solution: Board,
    /** Killer-only: cage definitions. Empty for non-Killer variants. */
    val cages: List<Region.Cage> = emptyList(),
)
