package com.mingeek.sudokusage.data.codec

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Cell

/**
 * Encodes a [Board]'s digit values as an N*N character string.
 *   '.'      → empty cell
 *   '1'..'9' → digit
 *
 * Notes are stored separately by [NotesCodec] — this codec only captures values.
 */
object BoardCodec {

    fun encodeValues(board: Board): String = buildString(board.cells.size) {
        for (cell in board.cells) {
            append(cell.displayValue?.toString() ?: ".")
        }
    }

    fun decodeAsGivens(encoded: String, size: Int = 9): Board =
        decode(encoded, size, asGivens = true)

    fun decodeAsValues(encoded: String, size: Int = 9): Board =
        decode(encoded, size, asGivens = false)

    private fun decode(encoded: String, size: Int, asGivens: Boolean): Board {
        require(encoded.length == size * size) {
            "Expected ${size * size} chars, got ${encoded.length}"
        }
        val cells = (0 until size * size).map { i ->
            val r = i / size
            val c = i % size
            val v = encoded[i].takeIf { it != '.' }?.digitToInt()
            if (asGivens) Cell(r, c, given = v) else Cell(r, c, value = v)
        }
        return Board(size, cells)
    }
}
