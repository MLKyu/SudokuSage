package com.mingeek.sudokusage.data.codec

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Cell
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Move
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardCodecTest {

    @Test
    fun emptyBoardRoundtrips() {
        val board = Board.empty(9)
        val encoded = BoardCodec.encodeValues(board)
        assertEquals(81, encoded.length)
        assertTrue(encoded.all { it == '.' })
        val decoded = BoardCodec.decodeAsValues(encoded)
        assertEquals(board, decoded)
    }

    @Test
    fun boardWithGivensRoundtripsAsGivens() {
        val cells = (0 until 81).map { i ->
            val r = i / 9; val c = i % 9
            val v = if (i == 0) 5 else if (i == 80) 9 else null
            Cell(r, c, given = v)
        }
        val board = Board(9, cells)
        val encoded = BoardCodec.encodeValues(board)
        val decoded = BoardCodec.decodeAsGivens(encoded)
        assertEquals(board, decoded)
    }
}

class NotesCodecTest {

    @Test
    fun roundtripWithMixedNotes() {
        val empty = Board.empty(9)
        val withNotes = empty
            .withCell(empty.cellAt(0, 0).copy(notes = setOf(1, 5, 9)))
            .withCell(empty.cellAt(4, 4).copy(notes = setOf(2)))
            .withCell(empty.cellAt(8, 8).copy(notes = setOf(3, 7)))
        val encoded = NotesCodec.encode(withNotes)
        val rebuilt = NotesCodec.applyTo(empty, encoded)
        assertEquals(setOf(1, 5, 9), rebuilt.cellAt(0, 0).notes)
        assertEquals(setOf(2), rebuilt.cellAt(4, 4).notes)
        assertEquals(setOf(3, 7), rebuilt.cellAt(8, 8).notes)
    }

    @Test
    fun emptyEncodingProducesNoNotes() {
        val board = Board.empty(9)
        assertEquals("", NotesCodec.encode(board))
        // applyTo with empty string is a no-op
        assertEquals(board, NotesCodec.applyTo(board, ""))
    }
}

class MoveCodecTest {

    @Test
    fun roundtripsAllVariants() {
        val moves = listOf<Move>(
            Move.Place(CellRef(0, 0), 5),
            Move.Erase(CellRef(3, 4)),
            Move.ToggleNote(CellRef(8, 8), 9),
            Move.Place(CellRef(1, 2), 7),
        )
        val encoded = MoveCodec.encode(moves)
        val decoded = MoveCodec.decode(encoded)
        assertEquals(moves, decoded)
    }

    @Test
    fun emptyListRoundtrips() {
        assertEquals("", MoveCodec.encode(emptyList()))
        assertEquals(emptyList<Move>(), MoveCodec.decode(""))
    }
}
