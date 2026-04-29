package com.mingeek.sudokusage.domain.board

/**
 * Six tiers, mirroring Sudoku.com's progression. The integer score is also used
 * by the difficulty classifier (sum of techniques required to solve).
 */
enum class Difficulty(val score: Int) {
    Easy(1),
    Medium(2),
    Hard(3),
    Expert(4),
    Master(5),
    Extreme(6),
}
