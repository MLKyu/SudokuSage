package com.mingeek.sudokusage.domain.event

import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.domain.board.VariantId

enum class PuzzleResult { Won, Failed }

/**
 * Domain events fan out to every listener (stats, achievements, analytics, audio).
 * New consumers subscribe; emitting code never has to know they exist.
 */
sealed interface GameEvent {
    val timestamp: Long

    data class CellPlaced(val ref: CellRef, val value: Int, override val timestamp: Long) : GameEvent
    data class CellErased(val ref: CellRef, override val timestamp: Long) : GameEvent
    data class NoteToggled(val ref: CellRef, val note: Int, override val timestamp: Long) : GameEvent
    data class MistakeMade(val ref: CellRef, val attempted: Int, override val timestamp: Long) : GameEvent
    data class HintUsed(val techniqueId: String, override val timestamp: Long) : GameEvent
    data class LineCompleted(val region: Region, override val timestamp: Long) : GameEvent
    data class CellSelected(val ref: CellRef, override val timestamp: Long) : GameEvent

    data class PuzzleStarted(
        val variant: VariantId,
        val difficulty: Difficulty,
        override val timestamp: Long,
    ) : GameEvent

    /** Fired for both Won and Failed terminal states. Use [result] to disambiguate. */
    data class PuzzleCompleted(
        val variant: VariantId,
        val difficulty: Difficulty,
        val result: PuzzleResult,
        val elapsedMs: Long,
        val mistakes: Int,
        val hintsUsed: Int,
        override val timestamp: Long,
        val isDaily: Boolean = false,
    ) : GameEvent

    data class PuzzlePaused(override val timestamp: Long) : GameEvent
    data class PuzzleResumed(override val timestamp: Long) : GameEvent
}
