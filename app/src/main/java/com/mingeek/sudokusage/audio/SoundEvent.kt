package com.mingeek.sudokusage.audio

/**
 * Catalog of every sound effect the app can emit, keyed by the resource name
 * expected in `res/raw/` (without extension).
 *
 * Drop a matching `.ogg` (preferred) or `.wav` file into `res/raw/` to enable a sound.
 * Missing files are tolerated — the player will silently skip them — so the catalog
 * can grow ahead of asset delivery without breaking builds.
 *
 * Each entry's KDoc describes when it should fire so designers and devs stay aligned.
 */
enum class SoundEvent(val assetName: String) {
    /** Generic light tap — buttons, menu rows. */
    UiTap("sfx_ui_tap"),
    /** Switch / toggle / segmented control. */
    UiToggle("sfx_ui_toggle"),
    /** System back / cancel. */
    UiBack("sfx_ui_back"),
    /** Dialog or bottom sheet appears. */
    UiOpen("sfx_ui_open"),
    /** Dialog or bottom sheet dismissed. */
    UiClose("sfx_ui_close"),

    /** Cell focus changed on the board. */
    CellSelect("sfx_cell_select"),
    /** Digit committed to a cell. */
    NumberPlace("sfx_number_place"),
    /** Cell value erased. */
    NumberErase("sfx_number_erase"),
    /** Pencil mark toggled. */
    NoteToggle("sfx_note_toggle"),
    /** Conflict / wrong digit (in mistake-limit mode). */
    Mistake("sfx_mistake"),
    /** Hint revealed by the hint engine. */
    HintReveal("sfx_hint_reveal"),

    /** Row, column, or box just got completed. */
    LineComplete("sfx_line_complete"),
    /** Puzzle solved — fanfare. */
    PuzzleComplete("sfx_puzzle_complete"),
    /** Daily streak incremented. */
    StreakUp("sfx_streak_up"),
    /** Achievement unlocked. */
    Achievement("sfx_achievement"),
}

/**
 * Background music tracks. One slot per "screen mood".
 * Drop matching files in `res/raw/` to enable. Looping is the default.
 */
enum class BgmTrack(val assetName: String) {
    /** Calm, ambient — home, settings, daily, stats. */
    Menu("bgm_menu"),
    /** Focused, sparse — active gameplay. */
    Puzzle("bgm_puzzle"),
    /** Celebratory — win screen / streak summary. */
    Win("bgm_win"),
}
