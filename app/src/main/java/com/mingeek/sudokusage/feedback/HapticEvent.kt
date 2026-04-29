package com.mingeek.sudokusage.feedback

/**
 * Coarse haptic intents. Mapped to platform [android.os.VibrationEffect]
 * predefined effects in [HapticController]. Keep this list small — too many
 * variations make it hard to feel distinct.
 */
enum class HapticEvent {
    /** Light tap — cell selection, button press. */
    Tap,
    /** Single click — committed input, hint reveal. */
    Confirm,
    /** Doubled click — mistake, rejected input. */
    Reject,
    /** Heavy click — puzzle complete, achievement, streak. */
    Triumph,
}
