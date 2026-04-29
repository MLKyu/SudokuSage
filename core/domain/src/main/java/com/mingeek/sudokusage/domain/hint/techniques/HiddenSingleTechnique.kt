package com.mingeek.sudokusage.domain.hint.techniques

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Move
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.domain.board.RuleSet
import com.mingeek.sudokusage.domain.hint.Hint
import com.mingeek.sudokusage.domain.hint.Technique
import com.mingeek.sudokusage.domain.hint.computeCandidates

/**
 * Hidden Single — within a region (row/column/box), exactly one cell can hold
 * a particular digit. Slightly harder to spot than Naked Single because it
 * requires scanning the whole region instead of a single cell.
 */
class HiddenSingleTechnique : Technique {
    override val id: String = "hidden-single"
    override val difficultyScore: Int = 2

    override fun analyze(board: Board, rules: RuleSet): Hint? {
        val candidates = computeCandidates(board, rules)
        for (region in rules.regions()) {
            for (digit in rules.symbols) {
                val cells = region.cells.filter { ref ->
                    candidates[ref]?.contains(digit) == true
                }
                if (cells.size == 1) {
                    val ref = cells.single()
                    val regionLabel = labelOf(region)
                    return Hint(
                        techniqueId = id,
                        techniqueDifficulty = difficultyScore,
                        techniqueName = "히든 싱글",
                        explanation = "${regionLabel}에서 ${digit}이(가) 들어갈 수 있는 자리는 한 곳뿐이에요.",
                        focusCells = listOf(ref),
                        resultMove = Move.Place(ref, digit),
                    )
                }
            }
        }
        return null
    }

    private fun labelOf(region: Region): String = when (region) {
        is Region.Row -> "${region.index + 1}행"
        is Region.Column -> "${region.index + 1}열"
        is Region.Box -> "${region.index + 1}번 박스"
        is Region.Diagonal -> "대각선"
        is Region.Cage -> "케이지"
        is Region.Custom -> region.tag
    }
}
