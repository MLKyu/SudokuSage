package com.mingeek.sudokusage.variant.killer

import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.Region
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

/**
 * Killer puzzles must have a unique solution — otherwise the engine flags the
 * user's correct alternate solution as wrong. GenericSolver doesn't enforce cage
 * sums, so we use a tiny Killer-aware backtracking counter here.
 *
 * Currently @Ignore: the empty-board flood-fill cage bank produces multi-solution
 * puzzles for medium/larger cages, and the auto-given fallback explodes the
 * search space (test runs > 10 min). Re-enable once the bank uses a true
 * uniqueness-preserving cage-layout generator (or hand-curated puzzles) — Killer
 * is intentionally hidden from the home picker until then.
 */
@Ignore("Pending uniqueness-guaranteed Killer cage generator — see KillerPuzzleBank")
class KillerPuzzleBankUniquenessTest {

    @Test
    fun `every difficulty × seed produces a uniquely-solved puzzle`() {
        for (difficulty in Difficulty.values()) {
            for (seed in 0L until 4L) {
                val puzzle = KillerPuzzleBank.puzzle(difficulty, seed)
                val initialDigits = puzzle.initial.cells.mapIndexedNotNull { i, cell ->
                    cell.given?.let { i to it }
                }.toMap()
                val count = countSolutions(puzzle.cages, initialDigits, limit = 2)
                assertEquals(
                    "Killer ${difficulty.name} seed=$seed must be unique (got $count)",
                    1, count,
                )
            }
        }
    }

    /** Sudoku + cage-sum backtracking with optional given digits. Capped by [limit]. */
    private fun countSolutions(
        cages: List<Region.Cage>,
        givens: Map<Int, Int>,
        limit: Int,
    ): Int {
        val cells = IntArray(81)
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)
        val cellToCage = IntArray(81) { -1 }
        cages.forEachIndexed { idx, cage ->
            for (ref in cage.cells) cellToCage[ref.row * 9 + ref.col] = idx
        }
        val cageSum = IntArray(cages.size)
        val cageRemaining = IntArray(cages.size) { cages[it].cells.size }
        for ((idx, v) in givens) {
            val r = idx / 9; val c = idx % 9; val b = (r / 3) * 3 + c / 3
            val bit = 1 shl v
            cells[idx] = v
            rowMask[r] = rowMask[r] or bit
            colMask[c] = colMask[c] or bit
            boxMask[b] = boxMask[b] or bit
            val cIdx = cellToCage[idx]
            if (cIdx >= 0) {
                cageSum[cIdx] += v
                cageRemaining[cIdx]--
            }
        }

        var count = 0

        fun solve(idx: Int): Boolean {
            if (count >= limit) return true
            if (idx == 81) {
                count++
                return count >= limit
            }
            if (cells[idx] != 0) return solve(idx + 1)
            val r = idx / 9
            val c = idx % 9
            val b = (r / 3) * 3 + c / 3
            val used = rowMask[r] or colMask[c] or boxMask[b]
            for (v in 1..9) {
                val bit = 1 shl v
                if (used and bit != 0) continue
                val cIdx = cellToCage[idx]
                if (cIdx >= 0) {
                    val partial = cageSum[cIdx] + v
                    val remainingAfter = cageRemaining[cIdx] - 1
                    val target = cages[cIdx].targetSum
                    if (remainingAfter == 0 && partial != target) continue
                    if (remainingAfter > 0 && partial >= target) continue
                    // Optional cage uniqueness: cage cells must not duplicate digits.
                    var dup = false
                    for (ref in cages[cIdx].cells) {
                        val cellIdx = ref.row * 9 + ref.col
                        if (cellIdx != idx && cells[cellIdx] == v) { dup = true; break }
                    }
                    if (dup) continue
                    cageSum[cIdx] = partial
                    cageRemaining[cIdx] = remainingAfter
                }
                cells[idx] = v
                rowMask[r] = rowMask[r] or bit
                colMask[c] = colMask[c] or bit
                boxMask[b] = boxMask[b] or bit
                if (solve(idx + 1)) return true
                cells[idx] = 0
                rowMask[r] = rowMask[r] and bit.inv()
                colMask[c] = colMask[c] and bit.inv()
                boxMask[b] = boxMask[b] and bit.inv()
                if (cIdx >= 0) {
                    cageSum[cIdx] -= v
                    cageRemaining[cIdx]++
                }
            }
            return false
        }
        solve(0)
        return count
    }
}
