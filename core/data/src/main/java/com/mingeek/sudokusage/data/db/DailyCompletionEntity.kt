package com.mingeek.sudokusage.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per calendar day the user has played the daily challenge.
 * The primary key is the ISO date string (yyyy-MM-dd) so calendar queries
 * are direct and ordering is lexicographic-correct.
 */
@Entity(tableName = "daily_completions")
data class DailyCompletionEntity(
    @PrimaryKey val dateKey: String,
    val variant: String,
    val difficulty: String,
    val seed: Long,
    val result: String,
    val elapsedMs: Long,
    val mistakes: Int,
    val completedAt: Long,
)
