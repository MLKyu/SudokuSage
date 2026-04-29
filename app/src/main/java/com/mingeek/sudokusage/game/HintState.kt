package com.mingeek.sudokusage.game

import com.mingeek.sudokusage.domain.hint.Hint

/**
 * Drives the progressive hint UI:
 *   None    — no active hint
 *   Active  — a hint has been resolved and is being revealed
 *               level 1 → focus cells highlighted (silent)
 *               level 2 → explanation banner shown above the board
 *               (next tap applies the hint and returns to None)
 */
sealed interface HintState {
    data object None : HintState
    data class Active(val hint: Hint, val level: Int) : HintState
}
