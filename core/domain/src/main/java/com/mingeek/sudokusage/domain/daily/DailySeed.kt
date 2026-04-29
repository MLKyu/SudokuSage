package com.mingeek.sudokusage.domain.daily

import java.time.LocalDate

/**
 * Stable, deterministic seed for a calendar date. The mixing constant scrambles
 * the epoch-day so consecutive days don't produce visibly similar puzzles.
 */
object DailySeed {
    /** Knuth-style multiplicative hash; bit-pattern of the 64-bit golden ratio (0x9E3779B97F4A7C15). */
    private const val MIX: Long = -7046029254386353131L

    fun forDate(date: LocalDate): Long = date.toEpochDay() * MIX
}
