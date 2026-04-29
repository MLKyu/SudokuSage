package com.mingeek.sudokusage.domain.board

import java.time.LocalDate

enum class GameStatus { Playing, Paused, Won, Failed }

/**
 * Single source of truth for an in-progress puzzle.
 *
 * [initial] is the puzzle as it was generated (immutable clues only) and is used by
 * the engine to replay-rebuild board state on undo. [board] is the current state.
 *
 * [dailyDate] tags this state as part of a daily challenge. Non-null survives
 * resume, so the daily recorder can fire even after the user backgrounds and
 * relaunches mid-game.
 */
data class GameState(
    val initial: Board,
    val board: Board,
    val solution: Board,
    val variant: VariantId,
    val difficulty: Difficulty,
    val startedAt: Long,
    val seed: Long,
    val mistakes: Int = 0,
    val mistakeLimit: Int? = 3,
    val elapsedMs: Long = 0,
    val moveHistory: List<Move> = emptyList(),
    val redoStack: List<Move> = emptyList(),
    val hintsUsed: Int = 0,
    val status: GameStatus = GameStatus.Playing,
    val dailyDate: LocalDate? = null,
    /** Killer-only: cage definitions. Empty for non-Killer variants. */
    val cages: List<Region.Cage> = emptyList(),
)
