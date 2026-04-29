package com.mingeek.sudokusage.domain.board

/**
 * Immutable square board. All mutations produce a new [Board] via [withCell].
 *
 * Cells are stored row-major: `cells[row * size + col]`.
 */
data class Board(
    val size: Int,
    val cells: List<Cell>,
) {
    init {
        require(cells.size == size * size) { "Expected ${size * size} cells, got ${cells.size}" }
    }

    fun cellAt(row: Int, col: Int): Cell = cells[row * size + col]
    fun cellAt(ref: CellRef): Cell = cellAt(ref.row, ref.col)

    fun withCell(cell: Cell): Board {
        val idx = cell.row * size + cell.col
        return copy(cells = cells.toMutableList().also { it[idx] = cell })
    }

    companion object {
        fun empty(size: Int): Board = Board(
            size = size,
            cells = (0 until size).flatMap { r -> (0 until size).map { c -> Cell(r, c) } }
        )
    }
}
