package com.mingeek.sudokusage.domain.daily

import com.mingeek.sudokusage.domain.board.Difficulty
import java.time.LocalDate

/**
 * Decides which [Difficulty] today's daily challenge will use. Pluggable so we
 * can swap to weekly rotations or remote-controlled difficulty without touching
 * the repository.
 */
fun interface DailyDifficultyStrategy {
    fun difficultyFor(date: LocalDate): Difficulty
}

class FixedDifficultyStrategy(
    private val difficulty: Difficulty = Difficulty.Medium,
) : DailyDifficultyStrategy {
    override fun difficultyFor(date: LocalDate): Difficulty = difficulty
}
