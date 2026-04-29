package com.mingeek.sudokusage.game

import com.mingeek.sudokusage.data.preferences.InputMode
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.GameState

sealed interface GameUiState {
    data object Loading : GameUiState

    /** Initialization failed — typically Resume with no saved game. */
    data class Error(val message: String) : GameUiState

    data class Ready(
        val game: GameState,
        val selected: CellRef? = null,
        val noteMode: Boolean = false,
        val conflicts: Set<CellRef> = emptySet(),
        /** Counts of each placed digit (1..9 -> count). For number-pad usage indicators. */
        val digitCounts: Map<Int, Int> = emptyMap(),
        val hintState: HintState = HintState.None,
        /** Box geometry for the board renderer; sourced from RuleSet. */
        val boxRows: Int = 3,
        val boxCols: Int = 3,
        /** Pro entitlement; live-updated from EntitlementGate. */
        val isPro: Boolean = false,
        /** Hint-gate dialog (free user past free-hint limit). */
        val hintGateOpen: Boolean = false,
        /** True for one upcoming hint after the user successfully watched a rewarded ad. */
        val rewardedHintAvailable: Boolean = false,
        /** Multiplier on board glyph size — accessibility setting. */
        val fontScale: Float = 1f,
        /** Swap to color-blind safe cell highlights. */
        val colorBlindMode: Boolean = false,
        val inputMode: InputMode = InputMode.CellFirst,
        /** Number-first mode: digit picked but not yet placed. */
        val selectedDigit: Int? = null,
    ) : GameUiState
}
