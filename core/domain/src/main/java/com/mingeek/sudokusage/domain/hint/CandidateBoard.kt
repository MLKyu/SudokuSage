package com.mingeek.sudokusage.domain.hint

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.domain.board.RuleSet

/**
 * For each empty cell, the set of digits that don't conflict with any peer (same
 * row / column / box / etc.). The foundation for every [Technique].
 *
 * Considers only filled values, *not* the user's pencil notes — techniques work
 * on the logically-possible set, then surface eliminations the user should make
 * if they're keeping notes.
 */
typealias Candidates = Map<CellRef, Set<Int>>

fun computeCandidates(board: Board, rules: RuleSet): Candidates {
    val all: Set<Int> = rules.symbols.toSet()
    val regions = rules.regions()

    val cellToRegions: Map<CellRef, List<Region>> = buildMap<CellRef, MutableList<Region>> {
        for (region in regions) {
            for (ref in region.cells) {
                getOrPut(ref) { mutableListOf() }.add(region)
            }
        }
    }

    val out = HashMap<CellRef, Set<Int>>(board.cells.size)
    for (cell in board.cells) {
        if (cell.displayValue != null) continue
        val ref = cell.ref
        val possible = all.toMutableSet()
        for (region in cellToRegions[ref] ?: emptyList()) {
            for (peerRef in region.cells) {
                if (peerRef == ref) continue
                board.cellAt(peerRef).displayValue?.let { possible.remove(it) }
            }
        }
        out[ref] = possible
    }
    return out
}
