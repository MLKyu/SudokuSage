package com.mingeek.sudokusage.domain.daily

import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId

/** Resolved input to the puzzle generator for a given calendar day. */
data class DailyConfig(
    val variant: VariantId,
    val difficulty: Difficulty,
    val seed: Long,
)
