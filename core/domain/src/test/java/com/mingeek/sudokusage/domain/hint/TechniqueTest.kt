package com.mingeek.sudokusage.domain.hint

import com.mingeek.sudokusage.data.codec.BoardCodec
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Move
import com.mingeek.sudokusage.domain.hint.techniques.HiddenSingleTechnique
import com.mingeek.sudokusage.domain.hint.techniques.NakedSingleTechnique
import com.mingeek.sudokusage.game.hint.HintBootstrap
import com.mingeek.sudokusage.variant.classic.ClassicRuleSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NakedSingleTechniqueTest {

    @Test
    fun firesWhenSingleCandidateRemains() {
        // Row 0 fills cols 1..8 with 1..8; (0,0) must be 9.
        val encoded = ".12345678" + ".".repeat(72)
        val board = BoardCodec.decodeAsValues(encoded)
        val hint = NakedSingleTechnique().analyze(board, ClassicRuleSet())
        assertNotNull(hint)
        assertEquals(CellRef(0, 0), hint!!.focusCells.single())
        assertEquals(Move.Place(CellRef(0, 0), 9), hint.resultMove)
    }

    @Test
    fun returnsNullOnUnconstrainedBoard() {
        val encoded = ".".repeat(81)
        val board = BoardCodec.decodeAsValues(encoded)
        assertNull(NakedSingleTechnique().analyze(board, ClassicRuleSet()))
    }
}

class HiddenSingleTechniqueTest {

    @Test
    fun firesWhenDigitFitsOneCellInRow() {
        // Place 9 in cols 1..8 at distinct rows/boxes outside row 0 & box 0,
        // so the only place 9 can sit in row 0 is (0,0).
        val encoded = buildString {
            // row 0: empty
            append(".........")
            // row 1: 9 at col 3
            append("...9.....")
            // row 2: 9 at col 6
            append("......9..")
            // row 3: 9 at col 1
            append(".9.......")
            // row 4: 9 at col 4
            append("....9....")
            // row 5: 9 at col 7
            append(".......9.")
            // row 6: 9 at col 2
            append("..9......")
            // row 7: 9 at col 5
            append(".....9...")
            // row 8: 9 at col 8
            append("........9")
        }
        assertEquals(81, encoded.length)
        val board = BoardCodec.decodeAsValues(encoded)
        val rules = ClassicRuleSet()

        // No naked single should fire — every empty cell has multiple candidates.
        assertNull(NakedSingleTechnique().analyze(board, rules))

        val hint = HiddenSingleTechnique().analyze(board, rules)
        assertNotNull(hint)
        assertEquals(CellRef(0, 0), hint!!.focusCells.single())
        assertEquals(Move.Place(CellRef(0, 0), 9), hint.resultMove)
    }
}

class HintEngineTest {

    @Test
    fun engineReturnsEasiestTechniqueFirst() {
        // Both naked-single and hidden-single can describe (0,0)→9, but the engine
        // must prefer the lower-score technique.
        val encoded = ".12345678" + ".".repeat(72)
        val board = BoardCodec.decodeAsValues(encoded)
        val engine = HintBootstrap.create()
        val hint = engine.nextHint(board, ClassicRuleSet())
        assertNotNull(hint)
        assertEquals("naked-single", hint!!.techniqueId)
    }

    @Test
    fun engineReturnsNullOnEmptyBoard() {
        val board = BoardCodec.decodeAsValues(".".repeat(81))
        val engine = HintBootstrap.create()
        assertNull(engine.nextHint(board, ClassicRuleSet()))
    }
}
