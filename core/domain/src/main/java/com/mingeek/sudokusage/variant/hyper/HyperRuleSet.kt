package com.mingeek.sudokusage.variant.hyper

import com.mingeek.sudokusage.domain.board.BoxedRuleSet
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.domain.board.VariantId

/**
 * 9×9 Hyper Sudoku — adds four extra 3×3 regions overlapping the standard boxes
 * at offsets (1,1), (1,5), (5,1), (5,5). Each must also hold digits 1..9 without
 * repeats.
 */
class HyperRuleSet : BoxedRuleSet(
    id = VariantId.Hyper,
    boardSize = 9,
    boxRows = 3,
    boxCols = 3,
) {
    override fun extraRegions(): List<Region> = HYPER_BOX_STARTS.mapIndexed { i, (r, c) ->
        Region.Custom(
            tag = "hyper-$i",
            cells = (0 until 3).flatMap { dr ->
                (0 until 3).map { dc -> CellRef(r + dr, c + dc) }
            },
        )
    }

    private companion object {
        val HYPER_BOX_STARTS = listOf(1 to 1, 1 to 5, 5 to 1, 5 to 5)
    }
}
