package com.mingeek.sudokusage.data.codec

import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Region

/**
 * Encodes a Killer cage list as `sum:r,c;r,c;...|sum:r,c;...|...`.
 * Round-trips losslessly. Empty list ↔ empty string.
 */
object CageCodec {

    fun encode(cages: List<Region.Cage>): String =
        if (cages.isEmpty()) "" else cages.joinToString(separator = "|") { cage ->
            val cellsPart = cage.cells.joinToString(separator = ";") { "${it.row},${it.col}" }
            "${cage.targetSum}:$cellsPart"
        }

    fun decode(encoded: String): List<Region.Cage> {
        if (encoded.isEmpty()) return emptyList()
        return encoded.split('|').map { part ->
            val (sumStr, cellsStr) = part.split(':', limit = 2)
            val cells = cellsStr.split(';').map { rc ->
                val (rStr, cStr) = rc.split(',', limit = 2)
                CellRef(rStr.toInt(), cStr.toInt())
            }
            Region.Cage(targetSum = sumStr.toInt(), cells = cells)
        }
    }
}
