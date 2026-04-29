package com.mingeek.sudokusage.game

import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId
import java.time.LocalDate

/** What [GameViewModel] should do on construction. */
sealed interface GameLaunch {
    /** Generate a fresh puzzle. Replaces any in-progress save. */
    data class New(
        val variantId: VariantId,
        val difficulty: Difficulty,
        val seed: Long? = null,
    ) : GameLaunch

    /** Reload the previously saved game. Caller must ensure a save exists. */
    data object Resume : GameLaunch

    /** Today's daily-challenge puzzle. Variant/difficulty/seed are derived from [date]. */
    data class Daily(val date: LocalDate) : GameLaunch

    /** Trainer practice puzzle keyed by [TrainerCatalog] lesson id. */
    data class Trainer(val lessonId: String) : GameLaunch
}
