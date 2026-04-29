package com.mingeek.sudokusage.domain.board

/**
 * Reusable base for any "row + column + box" Sudoku variant. Subclasses pick the
 * board size and box dimensions (e.g., 9×9 with 3×3 boxes for Classic, 6×6 with
 * 2×3 boxes for Mini6). [extraRegions] is the hook for variants that layer on
 * extra constraints (X-Sudoku diagonals, Hyper boxes).
 *
 * Box index ordering is row-major across the box grid:
 *   boxesPerRow = boardSize / boxCols
 *   box index = (boxRow * boxesPerRow) + boxCol
 */
abstract class BoxedRuleSet(
    override val id: VariantId,
    final override val boardSize: Int,
    final override val boxRows: Int,
    final override val boxCols: Int,
) : RuleSet {

    init {
        require(boardSize == boxRows * boxCols) {
            "boardSize ($boardSize) must equal boxRows * boxCols ($boxRows × $boxCols)"
        }
    }

    override val symbols: IntRange = 1..boardSize

    private val cachedRegions: List<Region> by lazy {
        val rows = (0 until boardSize).map { r ->
            Region.Row(r, (0 until boardSize).map { c -> CellRef(r, c) })
        }
        val cols = (0 until boardSize).map { c ->
            Region.Column(c, (0 until boardSize).map { r -> CellRef(r, c) })
        }
        val boxesPerRow = boardSize / boxCols
        val boxes = (0 until boardSize).map { b ->
            val br = (b / boxesPerRow) * boxRows
            val bc = (b % boxesPerRow) * boxCols
            Region.Box(b, (0 until boxRows).flatMap { dr ->
                (0 until boxCols).map { dc -> CellRef(br + dr, bc + dc) }
            })
        }
        rows + cols + boxes + extraRegions()
    }

    /** Hook for extra constraints. Default: none. */
    protected open fun extraRegions(): List<Region> = emptyList()

    final override fun regions(): List<Region> = cachedRegions

    override fun conflicts(board: Board): Set<CellRef> {
        val conflicts = HashSet<CellRef>()
        for (region in cachedRegions) {
            val byValue = HashMap<Int, MutableList<CellRef>>()
            for (ref in region.cells) {
                val v = board.cellAt(ref).displayValue ?: continue
                byValue.getOrPut(v) { mutableListOf() }.add(ref)
            }
            for ((_, refs) in byValue) {
                if (refs.size > 1) conflicts.addAll(refs)
            }
        }
        return conflicts
    }

    override fun isComplete(board: Board): Boolean {
        if (board.cells.any { it.displayValue == null }) return false
        return conflicts(board).isEmpty()
    }
}
