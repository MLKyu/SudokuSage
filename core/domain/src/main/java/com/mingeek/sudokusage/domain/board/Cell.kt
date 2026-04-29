package com.mingeek.sudokusage.domain.board

/** Coordinate-only reference; cheap to pass around. */
data class CellRef(val row: Int, val col: Int)

/**
 * @param given the original clue (immutable). Non-null = locked, user can't change it.
 * @param value the user's current entry (only when [given] is null).
 * @param notes pencil marks (only when [value] is null).
 */
data class Cell(
    val row: Int,
    val col: Int,
    val given: Int? = null,
    val value: Int? = null,
    val notes: Set<Int> = emptySet(),
) {
    val ref: CellRef get() = CellRef(row, col)
    val isGiven: Boolean get() = given != null
    val displayValue: Int? get() = given ?: value
}
