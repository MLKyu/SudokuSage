package com.mingeek.sudokusage.domain.daily

import java.time.LocalDate

enum class DailyOutcome { Won, Failed, NotPlayed }

data class DailyDayInfo(
    val date: LocalDate,
    val outcome: DailyOutcome,
    val elapsedMs: Long? = null,
)

/** Snapshot the daily screen and home card render from. */
data class DailyStatus(
    val today: LocalDate,
    val todayConfig: DailyConfig,
    val todayInfo: DailyDayInfo,
    val currentStreak: Int,
    val bestStreak: Int,
    /** Most-recent first; index 0 == today. Length 35 (5 weeks) for the calendar. */
    val recentDays: List<DailyDayInfo>,
)
