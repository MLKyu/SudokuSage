package com.mingeek.sudokusage.domain.hint

import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Move

/**
 * Output of a [Technique]. Drives the progressive-reveal hint UX:
 *   level 1: highlight [focusCells] only ("look here")
 *   level 2: show [techniqueName] + [explanation] ("here is the technique")
 *   level 3: apply [resultMove] (placement) or [eliminations] (notes cleanup)
 *
 * If [resultMove] is null, this is a pure-elimination hint — applying it removes
 * each `(cell, digit)` pair in [eliminations] from that cell's notes.
 *
 * Strings are Korean today. M5+ will swap [techniqueName]/[explanation] for
 * resource keys + parameters when the trainer demands tighter localization.
 */
data class Hint(
    val techniqueId: String,
    val techniqueDifficulty: Int,
    val techniqueName: String,
    val explanation: String,
    val focusCells: List<CellRef>,
    val eliminations: List<Pair<CellRef, Int>> = emptyList(),
    val resultMove: Move? = null,
)
