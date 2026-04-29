package com.mingeek.sudokusage.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mingeek.sudokusage.data.repo.StatsRepository
import com.mingeek.sudokusage.data.repo.StatsSummary
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    private val statsRepository: StatsRepository,
) : ViewModel() {

    private val variantId: VariantId = VariantId.Classic

    val state: StateFlow<Map<Difficulty, StatsSummary>> = statsRepository
        .observeForVariant(variantId)
        .map { list -> list.associateBy { it.difficulty } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyMap())
}
