package com.mingeek.sudokusage.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mingeek.sudokusage.data.repo.DailyChallengeRepository
import com.mingeek.sudokusage.data.repo.GameSaveRepository
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.GameStatus
import com.mingeek.sudokusage.domain.board.VariantId
import com.mingeek.sudokusage.domain.daily.DailyOutcome
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class SavedGameSummary(
    val variant: VariantId,
    val difficulty: Difficulty,
    val elapsedMs: Long,
)

data class HomeUiState(
    val savedGame: SavedGameSummary? = null,
    val dailyOutcomeToday: DailyOutcome = DailyOutcome.NotPlayed,
    val currentStreak: Int = 0,
)

class HomeViewModel(
    private val saves: GameSaveRepository,
    private val daily: DailyChallengeRepository,
) : ViewModel() {

    private val today: () -> LocalDate = LocalDate::now

    val state: StateFlow<HomeUiState> = combine(
        saves.observe(),
        daily.observeStatus(today()),
    ) { game, dailyStatus ->
        val saved = game?.takeIf { it.status == GameStatus.Playing || it.status == GameStatus.Paused }
            ?.let { SavedGameSummary(it.variant, it.difficulty, it.elapsedMs) }
        HomeUiState(
            savedGame = saved,
            dailyOutcomeToday = dailyStatus.todayInfo.outcome,
            currentStreak = dailyStatus.currentStreak,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeUiState())
}
