package com.mingeek.sudokusage.domain.hint.techniques

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.RuleSet
import com.mingeek.sudokusage.domain.hint.Hint
import com.mingeek.sudokusage.domain.hint.Technique
import com.mingeek.sudokusage.domain.hint.computeCandidates

/**
 * Naked Pair — two cells in a single region whose candidate sets are identical
 * pairs `{a, b}`. Those two digits are then locked into those two cells, so
 * every other cell in the region can drop `a` and `b` from its candidate set.
 */
class NakedPairTechnique : Technique {
    override val id: String = "naked-pair"
    override val difficultyScore: Int = 3

    override fun analyze(board: Board, rules: RuleSet): Hint? {
        val candidates = computeCandidates(board, rules)
        for (region in rules.regions()) {
            val pairs = region.cells.filter { (candidates[it]?.size ?: 0) == 2 }
            for (i in pairs.indices) {
                val a = pairs[i]
                val candsA = candidates[a]!!
                for (j in i + 1 until pairs.size) {
                    val b = pairs[j]
                    if (candidates[b] != candsA) continue
                    val eliminations = mutableListOf<Pair<CellRef, Int>>()
                    for (other in region.cells) {
                        if (other == a || other == b) continue
                        val otherCands = candidates[other] ?: continue
                        for (d in candsA) {
                            if (d in otherCands) eliminations.add(other to d)
                        }
                    }
                    if (eliminations.isNotEmpty()) {
                        val pairText = candsA.sorted().joinToString(", ")
                        return Hint(
                            techniqueId = id,
                            techniqueDifficulty = difficultyScore,
                            techniqueName = "네이키드 페어",
                            explanation = "이 두 셀에는 ${pairText}만 들어갈 수 있어요. 같은 영역의 다른 셀에서 ${pairText} 노트를 지울 수 있어요.",
                            focusCells = listOf(a, b),
                            eliminations = eliminations,
                            resultMove = null,
                        )
                    }
                }
            }
        }
        return null
    }
}
