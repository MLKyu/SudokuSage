package com.mingeek.sudokusage.analytics

/**
 * Canonical event & parameter names for every screen that talks to [Analytics].
 * Centralized so Firebase dashboards stay consistent across the app and so
 * Remote Config A/B test trigger events match exactly what we log.
 */
object SudokuEvents {
    // Events
    const val PUZZLE_STARTED = "puzzle_started"
    const val PUZZLE_COMPLETED = "puzzle_completed"
    const val HINT_USED = "hint_used"
    const val DAILY_OPENED = "daily_opened"
    const val PRO_UPSELL_SHOWN = "pro_upsell_shown"
    const val PRO_PURCHASED = "pro_purchased"
    const val SCREEN_VIEW = "screen_view"
    const val ACHIEVEMENT_UNLOCKED = "achievement_unlocked"

    // Parameters
    const val PARAM_VARIANT = "variant"
    const val PARAM_DIFFICULTY = "difficulty"
    const val PARAM_RESULT = "result"
    const val PARAM_ELAPSED_SEC = "elapsed_sec"
    const val PARAM_MISTAKES = "mistakes"
    const val PARAM_HINTS_USED = "hints_used"
    const val PARAM_IS_DAILY = "is_daily"
    const val PARAM_TECHNIQUE = "technique"
    const val PARAM_SCREEN = "screen"
    const val PARAM_TRIGGER = "trigger"
    const val PARAM_ACHIEVEMENT_ID = "achievement_id"

    // User properties
    const val USER_PROP_PRO_TIER = "pro_tier"
    const val USER_PROP_THEME = "theme"
}
