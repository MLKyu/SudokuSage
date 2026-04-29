package com.mingeek.sudokusage.domain.board

/** A single user-issued action; the unit of undo/redo. */
sealed interface Move {
    val ref: CellRef

    data class Place(override val ref: CellRef, val value: Int) : Move
    data class Erase(override val ref: CellRef) : Move
    data class ToggleNote(override val ref: CellRef, val note: Int) : Move
}
