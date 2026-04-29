package com.mingeek.sudokusage.variant.killer

import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Cell
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.Region
import kotlin.random.Random

/**
 * Curated Killer Sudoku puzzle bank.
 *
 * Cages are flood-fill grown across box/row/col boundaries to give the sums real
 * teeth (strictly row-aligned partitions don't enforce uniqueness on an empty
 * board). For higher difficulties (larger cages), even irregular cages don't
 * automatically pin the puzzle down to a single solution — we then sprinkle
 * minimal clue digits ("givens") from the solution until the [countSolutions]
 * helper confirms uniqueness. This keeps Killer "no givens or few givens" while
 * guaranteeing correctness, and avoids gameplay pitfalls where the user's
 * legitimate alternate solution gets flagged as wrong.
 */
internal object KillerPuzzleBank {

    /** Single classic 9×9 solution we re-cage at different granularities. */
    private val SOLUTION = arrayOf(
        intArrayOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
        intArrayOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
        intArrayOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
        intArrayOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
        intArrayOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
        intArrayOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
        intArrayOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
        intArrayOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
        intArrayOf(3, 4, 5, 2, 8, 6, 1, 7, 9),
    )

    /**
     * Per-difficulty target cage size. Larger = harder (less sum-information).
     * Cages may end up smaller than target where uniqueness/edge constraints
     * cap growth.
     */
    private val targetCageSizeByDifficulty: Map<Difficulty, Int> = mapOf(
        Difficulty.Easy to 2,
        Difficulty.Medium to 3,
        Difficulty.Hard to 3,
        Difficulty.Expert to 4,
        Difficulty.Master to 4,
        Difficulty.Extreme to 5,
    )

    fun puzzle(difficulty: Difficulty, seed: Long): KillerPuzzleData {
        val targetSize = targetCageSizeByDifficulty.getValue(difficulty)
        val cages = growCages(targetSize, seed)
        val givens = computeGivensForUniqueness(cages, seed)
        val initial = Board(
            size = 9,
            cells = (0 until 81).map { i ->
                val r = i / 9; val c = i % 9
                if (givens.contains(i)) Cell(row = r, col = c, given = SOLUTION[r][c])
                else Cell(row = r, col = c)
            },
        )
        val solution = Board(
            size = 9,
            cells = (0 until 81).map { i ->
                Cell(row = i / 9, col = i % 9, given = SOLUTION[i / 9][i % 9])
            },
        )
        return KillerPuzzleData(initial = initial, solution = solution, cages = cages)
    }

    private fun growCages(targetSize: Int, seed: Long): List<Region.Cage> {
        val n = 9
        val visited = Array(n) { BooleanArray(n) }
        val cages = mutableListOf<Region.Cage>()
        val rng = Random(seed)
        val deltas = listOf(0 to 1, 1 to 0, 0 to -1, -1 to 0)

        for (r0 in 0 until n) {
            for (c0 in 0 until n) {
                if (visited[r0][c0]) continue
                val cells = mutableListOf(CellRef(r0, c0))
                visited[r0][c0] = true
                val cageDigits = HashSet<Int>().apply { add(SOLUTION[r0][c0]) }

                while (cells.size < targetSize) {
                    val candidates = mutableListOf<CellRef>()
                    for (cell in cells) {
                        for ((dr, dc) in deltas) {
                            val nr = cell.row + dr
                            val nc = cell.col + dc
                            if (nr !in 0 until n || nc !in 0 until n) continue
                            if (visited[nr][nc]) continue
                            if (SOLUTION[nr][nc] in cageDigits) continue
                            candidates.add(CellRef(nr, nc))
                        }
                    }
                    if (candidates.isEmpty()) break
                    val pick = candidates[rng.nextInt(candidates.size)]
                    visited[pick.row][pick.col] = true
                    cells.add(pick)
                    cageDigits.add(SOLUTION[pick.row][pick.col])
                }
                val sum = cells.sumOf { SOLUTION[it.row][it.col] }
                cages.add(Region.Cage(targetSum = sum, cells = cells))
            }
        }
        return cages
    }

    /**
     * Walks cells in a deterministic shuffle, adding givens until the puzzle
     * has exactly one solution per [countSolutions]. The added givens are the
     * minimum needed for uniqueness (early cells often resolve cascades).
     */
    private fun computeGivensForUniqueness(
        cages: List<Region.Cage>,
        seed: Long,
    ): Set<Int> {
        if (countSolutions(cages, emptySet(), limit = 2) <= 1) return emptySet()

        val rng = Random(seed xor 0x1234L)
        val cellOrder = (0 until 81).toMutableList().also { it.shuffle(rng) }
        val givens = HashSet<Int>()
        for (idx in cellOrder) {
            givens.add(idx)
            if (countSolutions(cages, givens, limit = 2) <= 1) break
        }
        return givens
    }

    /** Sudoku + cage-sum backtracking solution counter. Capped by [limit]. */
    private fun countSolutions(
        cages: List<Region.Cage>,
        givens: Set<Int>,
        limit: Int,
    ): Int {
        val cells = IntArray(81)
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)
        for (idx in givens) {
            val r = idx / 9; val c = idx % 9; val b = (r / 3) * 3 + c / 3
            val v = SOLUTION[r][c]
            val bit = 1 shl v
            cells[idx] = v
            rowMask[r] = rowMask[r] or bit
            colMask[c] = colMask[c] or bit
            boxMask[b] = boxMask[b] or bit
        }
        val cellToCage = IntArray(81) { -1 }
        cages.forEachIndexed { i, cage ->
            for (ref in cage.cells) cellToCage[ref.row * 9 + ref.col] = i
        }
        val cageSum = IntArray(cages.size)
        val cageRemaining = IntArray(cages.size) { cages[it].cells.size }
        // Pre-apply givens to cage running sums.
        for (idx in givens) {
            val cIdx = cellToCage[idx]
            if (cIdx >= 0) {
                cageSum[cIdx] += SOLUTION[idx / 9][idx % 9]
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
            val r = idx / 9; val c = idx % 9; val b = (r / 3) * 3 + c / 3
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

internal data class KillerPuzzleData(
    val initial: Board,
    val solution: Board,
    val cages: List<Region.Cage>,
)
