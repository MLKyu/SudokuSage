package com.mingeek.sudokusage.domain.board

import kotlin.random.Random

/**
 * Variant-agnostic backtracking solver. Maintains one bitmask per region from
 * [RuleSet.regions] (rows, columns, boxes, diagonals, hyper boxes, etc.) and
 * combines them on the fly to compute candidates per cell. The MRV heuristic
 * picks the most constrained empty cell next.
 *
 * Performance: per-step cost scales with `(regions per cell) × (empty cells)`,
 * which stays in the low hundreds for any 9×9 variant we ship.
 *
 * Stateful — one instance per consumer (generator, hint engine, etc.) and not
 * thread-safe by design.
 */
open class GenericSolver(rules: RuleSet) {

    private val n: Int = rules.boardSize
    private val regionList: List<Region> = rules.regions()
    private val cellRegions: Array<IntArray>
    private val allDigitsMask: Int

    private val grid = IntArray(n * n)
    private val regionMasks = IntArray(regionList.size)

    private var mrvCount: Int = 0
    private var mrvMask: Int = 0

    init {
        val temp = Array(n * n) { mutableListOf<Int>() }
        regionList.forEachIndexed { ri, region ->
            for (ref in region.cells) {
                temp[ref.row * n + ref.col].add(ri)
            }
        }
        cellRegions = Array(n * n) { temp[it].toIntArray() }
        // bits 1..n set; bit 0 unused.
        allDigitsMask = ((1 shl (n + 1)) - 1) and 1.inv()
    }

    fun solveOne(input: IntArray, random: Random? = null): IntArray? {
        if (!loadFrom(input)) return null
        val out = IntArray(n * n)
        return if (solveOnceInto(out, random)) out else null
    }

    fun countSolutions(input: IntArray, limit: Int): Int {
        require(limit >= 1)
        if (!loadFrom(input)) return 0
        val counter = IntArray(1)
        countUpTo(counter, limit)
        return counter[0]
    }

    private fun loadFrom(input: IntArray): Boolean {
        require(input.size == n * n)
        regionMasks.fill(0)
        for (i in 0 until n * n) {
            grid[i] = input[i]
            val v = input[i]
            if (v == 0) continue
            val bit = 1 shl v
            for (ri in cellRegions[i]) {
                if (regionMasks[ri] and bit != 0) return false
                regionMasks[ri] = regionMasks[ri] or bit
            }
        }
        return true
    }

    private fun pickMrv(): Int {
        var bestIdx = -1
        var bestCount = n + 1
        var bestMask = 0
        for (i in 0 until n * n) {
            if (grid[i] != 0) continue
            var used = 0
            for (ri in cellRegions[i]) used = used or regionMasks[ri]
            val avail = allDigitsMask and used.inv()
            val cnt = Integer.bitCount(avail)
            if (cnt < bestCount) {
                bestIdx = i
                bestCount = cnt
                bestMask = avail
                if (cnt <= 1) break
            }
        }
        mrvCount = if (bestIdx == -1) 0 else bestCount
        mrvMask = bestMask
        return bestIdx
    }

    private fun solveOnceInto(out: IntArray, random: Random?): Boolean {
        val pickIdx = pickMrv()
        if (pickIdx == -1) {
            for (i in 0 until n * n) out[i] = grid[i]
            return true
        }
        if (mrvCount == 0) return false

        val regions = cellRegions[pickIdx]

        if (random != null) {
            val cands = IntArray(mrvCount)
            var k = 0
            var m = mrvMask
            while (m != 0) {
                cands[k++] = Integer.numberOfTrailingZeros(m)
                m = m and (m - 1)
            }
            for (i in cands.size - 1 downTo 1) {
                val j = random.nextInt(i + 1)
                val tmp = cands[i]; cands[i] = cands[j]; cands[j] = tmp
            }
            for (v in cands) {
                if (tryValue(pickIdx, v, regions) { solveOnceInto(out, random) }) return true
            }
        } else {
            var m = mrvMask
            while (m != 0) {
                val v = Integer.numberOfTrailingZeros(m)
                m = m and (m - 1)
                if (tryValue(pickIdx, v, regions) { solveOnceInto(out, random) }) return true
            }
        }
        return false
    }

    private fun countUpTo(counter: IntArray, limit: Int) {
        if (counter[0] >= limit) return
        val pickIdx = pickMrv()
        if (pickIdx == -1) {
            counter[0]++
            return
        }
        if (mrvCount == 0) return

        val regions = cellRegions[pickIdx]
        var m = mrvMask
        while (m != 0) {
            val v = Integer.numberOfTrailingZeros(m)
            m = m and (m - 1)
            tryValue(pickIdx, v, regions) {
                countUpTo(counter, limit)
                counter[0] >= limit
            }
            if (counter[0] >= limit) return
        }
    }

    private inline fun tryValue(idx: Int, v: Int, regions: IntArray, body: () -> Boolean): Boolean {
        val bit = 1 shl v
        grid[idx] = v
        for (ri in regions) regionMasks[ri] = regionMasks[ri] or bit
        val ok = body()
        grid[idx] = 0
        for (ri in regions) regionMasks[ri] = regionMasks[ri] and bit.inv()
        return ok
    }
}
