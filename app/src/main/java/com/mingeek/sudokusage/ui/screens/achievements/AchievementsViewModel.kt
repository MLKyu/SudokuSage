package com.mingeek.sudokusage.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mingeek.sudokusage.data.repo.AchievementRepository
import com.mingeek.sudokusage.domain.achievement.AchievementProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AchievementsViewModel(
    private val repo: AchievementRepository,
) : ViewModel() {

    val state: StateFlow<List<AchievementProgress>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())
}
