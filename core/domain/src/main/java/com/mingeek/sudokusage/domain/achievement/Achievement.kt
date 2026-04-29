package com.mingeek.sudokusage.domain.achievement

/**
 * One unlockable goal. v1 achievements are binary: a single qualifying event
 * flips the unlocked flag. Cumulative achievements (use 50 hints, 30-day streak,
 * etc.) layer on by adding a `target` field and progress accumulation in M5.1.
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
)

/**
 * Persisted state. [unlockedAt] is null for still-locked achievements.
 */
data class AchievementProgress(
    val achievement: Achievement,
    val unlocked: Boolean,
    val unlockedAt: Long?,
)
