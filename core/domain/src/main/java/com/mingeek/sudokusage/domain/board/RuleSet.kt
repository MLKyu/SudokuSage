package com.mingeek.sudokusage.domain.board

/**
 * Variant-specific constraint logic. Implementations are pure functions on a [Board];
 * implementing a new variant means writing a new [RuleSet] without touching existing code.
 */
interface RuleSet {
    val id: VariantId
    val boardSize: Int
    /** Symbols allowed in cells; e.g., 1..9 for classic, 1..6 for Mini6. */
    val symbols: IntRange

    /** Box height (rows). Drives thick-line rendering and box-region geometry. */
    val boxRows: Int
        get() = 3
    /** Box width (cols). */
    val boxCols: Int
        get() = 3

    /** All constraint regions for this variant on a board of [boardSize]. */
    fun regions(): List<Region>

    /** Cells currently violating any region constraint. */
    fun conflicts(board: Board): Set<CellRef>

    /** Every cell filled and no conflicts — but not necessarily matching the unique solution. */
    fun isComplete(board: Board): Boolean

    /** Convenience: is the board complete *and* identical to the supplied solution? */
    fun isSolved(board: Board, solution: Board): Boolean =
        isComplete(board) && board.cells.zip(solution.cells)
            .all { (b, s) -> b.displayValue == s.displayValue }
}
