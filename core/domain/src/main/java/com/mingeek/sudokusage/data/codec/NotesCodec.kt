package com.mingeek.sudokusage.data.codec

import com.mingeek.sudokusage.domain.board.Board

/**
 * Encodes pencil marks across a [Board] as a compact string.
 * Format: `r,c=v1,v2,v3;r,c=v1` — only cells with at least one note are listed.
 * Empty board → empty string.
 */
object NotesCodec {

    fun encode(board: Board): String = board.cells
        .asSequence()
        .filter { it.notes.isNotEmpty() }
        .joinToString(separator = ";") { cell ->
            "${cell.row},${cell.col}=${cell.notes.sorted().joinToString(",")}"
        }

    /** Returns a copy of [board] with notes from [encoded] applied. */
    fun applyTo(board: Board, encoded: String): Board {
        if (encoded.isEmpty()) return board
        var result = board
        for (entry in encoded.split(';')) {
            val (header, valuesStr) = entry.split('=')
            val (rStr, cStr) = header.split(',')
            val r = rStr.toInt()
            val c = cStr.toInt()
            val notes = valuesStr.split(',').map { it.toInt() }.toSet()
            val cell = result.cellAt(r, c)
            result = result.withCell(cell.copy(notes = notes))
        }
        return result
    }
}
