package com.mingeek.sudokusage.variant.x

import com.mingeek.sudokusage.domain.board.BoxedRuleSet
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.DiagonalDirection
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.domain.board.VariantId

/**
 * 9×9 X-Sudoku — both diagonals must contain digits 1..9 with no repeats,
 * in addition to the standard row/column/box constraints.
 */
class XSudokuRuleSet : BoxedRuleSet(
    id = VariantId.XSudoku,
    boardSize = 9,
    boxRows = 3,
    boxCols = 3,
) {
    override fun extraRegions(): List<Region> = listOf(
        Region.Diagonal(
            direction = DiagonalDirection.Main,
            cells = (0 until boardSize).map { CellRef(it, it) },
        ),
        Region.Diagonal(
            direction = DiagonalDirection.Anti,
            cells = (0 until boardSize).map { CellRef(it, boardSize - 1 - it) },
        ),
    )
}
