package com.mingeek.sudokusage.domain.hint.techniques

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Move
import com.mingeek.sudokusage.domain.board.RuleSet
import com.mingeek.sudokusage.domain.hint.Hint
import com.mingeek.sudokusage.domain.hint.Technique
import com.mingeek.sudokusage.domain.hint.computeCandidates

/**
 * Naked Single — an empty cell where only one digit can possibly fit. The
 * easiest technique; a board with any empty cells and no Naked Singles likely
 * needs more advanced reasoning.
 */
class NakedSingleTechnique : Technique {
    override val id: String = "naked-single"
    override val difficultyScore: Int = 1

    override fun analyze(board: Board, rules: RuleSet): Hint? {
        val candidates = computeCandidates(board, rules)
        for ((ref, cands) in candidates) {
            if (cands.size == 1) {
                val digit = cands.first()
                return Hint(
                    techniqueId = id,
                    techniqueDifficulty = difficultyScore,
                    techniqueName = "내추럴 싱글",
                    explanation = "이 셀(${ref.row + 1}행 ${ref.col + 1}열)에는 ${digit}만 들어갈 수 있어요.",
                    focusCells = listOf(ref),
                    resultMove = Move.Place(ref, digit),
                )
            }
        }
        return null
    }
}
