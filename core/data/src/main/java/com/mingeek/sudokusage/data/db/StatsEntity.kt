package com.mingeek.sudokusage.data.db

import androidx.room.Entity

/**
 * One row per (variant, difficulty) pair. Updated by StatsCollector on
 * [com.mingeek.sudokusage.domain.event.GameEvent.PuzzleCompleted].
 */
@Entity(tableName = "puzzle_stats", primaryKeys = ["variant", "difficulty"])
data class StatsEntity(
    val variant: String,
    val difficulty: String,
    val gamesStarted: Int = 0,
    val gamesWon: Int = 0,
    val gamesFailed: Int = 0,
    val bestTimeMs: Long? = null,
    val totalTimeMs: Long = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastPlayedAt: Long? = null,
)
