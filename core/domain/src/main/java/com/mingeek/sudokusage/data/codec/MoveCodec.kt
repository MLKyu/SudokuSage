package com.mingeek.sudokusage.data.codec

import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Move

/**
 * Encodes a [Move] sequence as a pipe-separated string. Each move is a single tag
 * char followed by comma-separated ints:
 *   `P{r},{c},{v}`   — Move.Place
 *   `E{r},{c}`       — Move.Erase
 *   `N{r},{c},{n}`   — Move.ToggleNote
 *
 * Empty list ↔ empty string.
 */
object MoveCodec {

    fun encode(moves: List<Move>): String =
        moves.joinToString(separator = "|") { encodeOne(it) }

    fun decode(encoded: String): List<Move> {
        if (encoded.isEmpty()) return emptyList()
        return encoded.split('|').map { decodeOne(it) }
    }

    private fun encodeOne(move: Move): String = when (move) {
        is Move.Place -> "P${move.ref.row},${move.ref.col},${move.value}"
        is Move.Erase -> "E${move.ref.row},${move.ref.col}"
        is Move.ToggleNote -> "N${move.ref.row},${move.ref.col},${move.note}"
    }

    private fun decodeOne(token: String): Move {
        val tag = token[0]
        val parts = token.substring(1).split(',').map { it.toInt() }
        val ref = CellRef(parts[0], parts[1])
        return when (tag) {
            'P' -> Move.Place(ref, parts[2])
            'E' -> Move.Erase(ref)
            'N' -> Move.ToggleNote(ref, parts[2])
            else -> error("Unknown move tag: '$tag' in '$token'")
        }
    }
}
