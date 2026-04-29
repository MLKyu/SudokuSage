package com.mingeek.sudokusage.domain.hint.techniques

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.domain.board.RuleSet
import com.mingeek.sudokusage.domain.hint.Hint
import com.mingeek.sudokusage.domain.hint.Technique
import com.mingeek.sudokusage.domain.hint.computeCandidates

/**
 * Pointing Pair / Triple — within a 3x3 box, a digit's candidate cells all share
 * the same row (or column). The digit must therefore land in that intersection,
 * so it can be eliminated from the remainder of the row (or column) outside the box.
 */
class PointingPairTechnique : Technique {
    override val id: String = "pointing-pair"
    override val difficultyScore: Int = 4

    override fun analyze(board: Board, rules: RuleSet): Hint? {
        val candidates = computeCandidates(board, rules)
        val regions = rules.regions()
        val rows = regions.filterIsInstance<Region.Row>().associateBy { it.index }
        val cols = regions.filterIsInstance<Region.Column>().associateBy { it.index }
        val boxes = regions.filterIsInstance<Region.Box>()
        val boxCellSets = boxes.associateWith { it.cells.toSet() }

        for (box in boxes) {
            val boxSet = boxCellSets[box]!!
            for (digit in rules.symbols) {
                val cellsWithDigit = box.cells.filter { candidates[it]?.contains(digit) == true }
                if (cellsWithDigit.size < 2) continue

                val sharedRow = cellsWithDigit.first().row.takeIf { r ->
                    cellsWithDigit.all { it.row == r }
                }
                if (sharedRow != null) {
                    val row = rows[sharedRow] ?: continue
                    val eliminations = row.cells
                        .filter { it !in boxSet && candidates[it]?.contains(digit) == true }
                        .map { it to digit }
                    if (eliminations.isNotEmpty()) {
                        return buildHint(box, sharedRow + 1, "행", digit, cellsWithDigit, eliminations)
                    }
                }

                val sharedCol = cellsWithDigit.first().col.takeIf { c ->
                    cellsWithDigit.all { it.col == c }
                }
                if (sharedCol != null) {
                    val col = cols[sharedCol] ?: continue
                    val eliminations = col.cells
                        .filter { it !in boxSet && candidates[it]?.contains(digit) == true }
                        .map { it to digit }
                    if (eliminations.isNotEmpty()) {
                        return buildHint(box, sharedCol + 1, "열", digit, cellsWithDigit, eliminations)
                    }
                }
            }
        }
        return null
    }

    private fun buildHint(
        box: Region.Box,
        lineIndex: Int,
        lineLabel: String,
        digit: Int,
        focus: List<CellRef>,
        eliminations: List<Pair<CellRef, Int>>,
    ): Hint = Hint(
        techniqueId = id,
        techniqueDifficulty = difficultyScore,
        techniqueName = "포인팅 페어",
        explanation = "${box.index + 1}번 박스의 ${digit}은(는) ${lineIndex}${lineLabel} 안에서만 가능해요. 같은 ${lineLabel}의 다른 셀에서 ${digit}을(를) 지울 수 있어요.",
        focusCells = focus,
        eliminations = eliminations,
        resultMove = null,
    )
}
