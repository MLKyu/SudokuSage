package com.mingeek.sudokusage.domain.hint

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.RuleSet

/**
 * Runs registered [Technique]s in ascending difficulty order; the first one that
 * fires wins. Used both for hints (UX) and for offline difficulty classification.
 */
class HintEngine(techniques: List<Technique>) {
    private val sorted: List<Technique> = techniques.sortedBy { it.difficultyScore }

    fun nextHint(board: Board, rules: RuleSet): Hint? =
        sorted.asSequence().mapNotNull { it.analyze(board, rules) }.firstOrNull()

    /**
     * Stub for difficulty classification: solve the puzzle step by step, sum technique
     * scores, then bucket. Real implementation lands with the technique library (M4).
     */
    fun classify(@Suppress("UNUSED_PARAMETER") board: Board, @Suppress("UNUSED_PARAMETER") rules: RuleSet): Difficulty =
        Difficulty.Easy
}
