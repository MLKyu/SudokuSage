package com.mingeek.sudokusage.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-achievement progress. Today only the binary unlocked-or-not state matters;
 * [progress] is reserved for future cumulative achievements.
 */
@Entity(tableName = "achievement_progress")
data class AchievementProgressEntity(
    @PrimaryKey val achievementId: String,
    val unlocked: Boolean = false,
    val progress: Int = 0,
    val unlockedAt: Long? = null,
)
