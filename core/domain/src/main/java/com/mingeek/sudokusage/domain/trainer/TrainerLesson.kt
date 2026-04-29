package com.mingeek.sudokusage.domain.trainer

/**
 * One technique-learning lesson. [samplePuzzleEncoded], when present, is a
 * hand-crafted 81-character board (`.` = empty, `1`..`9` = clue) where the
 * lesson's technique surfaces naturally. When null, the trainer falls back to
 * a fresh Classic Medium game and trusts the hint engine to surface the
 * technique opportunistically.
 */
data class TrainerLesson(
    val id: String,
    val techniqueId: String,
    val name: String,
    val tagline: String,
    val explanation: String,
    val samplePuzzleEncoded: String? = null,
)
