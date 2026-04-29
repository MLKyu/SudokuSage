package com.mingeek.sudokusage.ui.screens.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mingeek.sudokusage.data.repo.DailyChallengeRepository
import com.mingeek.sudokusage.domain.daily.DailyStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class DailyViewModel(
    private val repo: DailyChallengeRepository,
) : ViewModel() {

    private val today: () -> LocalDate = LocalDate::now

    val state: StateFlow<DailyStatus?> = repo.observeStatus(today())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), initialValue = null)
}
