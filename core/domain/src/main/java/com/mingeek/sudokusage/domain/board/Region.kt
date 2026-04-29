package com.mingeek.sudokusage.domain.board

enum class DiagonalDirection { Main, Anti }

/**
 * A constraint group: a set of cells that must hold each symbol exactly once
 * (or, for [Cage], sum to a target).
 *
 * Variants compose their constraint set from these primitives.
 */
sealed class Region {
    abstract val cells: List<CellRef>

    data class Row(val index: Int, override val cells: List<CellRef>) : Region()
    data class Column(val index: Int, override val cells: List<CellRef>) : Region()
    data class Box(val index: Int, override val cells: List<CellRef>) : Region()
    data class Diagonal(val direction: DiagonalDirection, override val cells: List<CellRef>) : Region()
    /** Killer Sudoku: cells sum to [targetSum] and contain no duplicates. */
    data class Cage(val targetSum: Int, override val cells: List<CellRef>) : Region()
    /** Escape hatch for novel variants. */
    data class Custom(val tag: String, override val cells: List<CellRef>) : Region()
}
