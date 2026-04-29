package com.mingeek.sudokusage.variant.killer

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.BoxedRuleSet
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.domain.board.VariantId

/**
 * Killer Sudoku 9×9 with 3×3 boxes plus per-puzzle cages. Cage cells must:
 *   - contain unique digits (handled by [BoxedRuleSet] via [extraRegions])
 *   - sum to [Region.Cage.targetSum] when fully filled
 *
 * Cages live on the [Puzzle] / [GameState] (per-puzzle) — registry holds an empty
 * default for variant lookup; the real ruleset is built from puzzle cages.
 */
class KillerRuleSet(
    private val cages: List<Region.Cage> = emptyList(),
) : BoxedRuleSet(
    id = VariantId.Killer,
    boardSize = 9,
    boxRows = 3,
    boxCols = 3,
) {
    override fun extraRegions(): List<Region> = cages

    override fun conflicts(board: Board): Set<CellRef> {
        val base = super.conflicts(board)
        if (cages.isEmpty()) return base
        val out = base.toMutableSet()
        for (cage in cages) {
            val values = cage.cells.map { board.cellAt(it).displayValue }
            val allFilled = values.all { it != null }
            if (allFilled && values.filterNotNull().sum() != cage.targetSum) {
                out.addAll(cage.cells)
            }
        }
        return out
    }

    override fun isComplete(board: Board): Boolean {
        if (!super.isComplete(board)) return false
        return cages.all { cage ->
            cage.cells.sumOf { board.cellAt(it).displayValue ?: 0 } == cage.targetSum
        }
    }
}
