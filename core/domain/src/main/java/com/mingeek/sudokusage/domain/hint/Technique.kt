package com.mingeek.sudokusage.domain.hint

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.RuleSet

/**
 * One solving technique (Naked Single, Hidden Single, Pointing Pair, X-Wing, ...).
 * Implementations are pure and stateless; the engine composes them.
 *
 * Adding a new technique = new file implementing this interface + register it
 * with [HintEngine]. No edits to existing techniques.
 */
interface Technique {
    /** Stable id used in analytics, persistence, and the trainer. Never rename. */
    val id: String

    /** Roughly maps to difficulty score: easier techniques = lower numbers. */
    val difficultyScore: Int

    /** Returns a [Hint] if this technique can advance the board, else null. */
    fun analyze(board: Board, rules: RuleSet): Hint?
}
