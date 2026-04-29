package com.mingeek.sudokusage.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mingeek.sudokusage.audio.AudioSettings
import com.mingeek.sudokusage.audio.AudioSettingsState
import com.mingeek.sudokusage.data.preferences.GameplayPrefs
import com.mingeek.sudokusage.data.preferences.GameplaySettings
import com.mingeek.sudokusage.data.preferences.InputMode
import com.mingeek.sudokusage.data.preferences.ThemeChoice
import com.mingeek.sudokusage.data.preferences.ThemePalette
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val audio: AudioSettingsState = AudioSettingsState(),
    val gameplay: GameplayPrefs = GameplayPrefs(),
)

class SettingsViewModel(
    private val audio: AudioSettings,
    private val gameplay: GameplaySettings,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(audio.state, gameplay.state) { a, g ->
        SettingsUiState(audio = a, gameplay = g)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), SettingsUiState())

    fun toggleBgm(enabled: Boolean) = viewModelScope.launch { audio.setBgmEnabled(enabled) }
    fun toggleSfx(enabled: Boolean) = viewModelScope.launch { audio.setSfxEnabled(enabled) }
    fun toggleHaptic(enabled: Boolean) = viewModelScope.launch { audio.setHapticEnabled(enabled) }
    fun setBgmVolume(v: Float) = viewModelScope.launch { audio.setBgmVolume(v) }
    fun setSfxVolume(v: Float) = viewModelScope.launch { audio.setSfxVolume(v) }

    fun setTheme(choice: ThemeChoice) = viewModelScope.launch { gameplay.setTheme(choice) }
    fun toggleMistakeLimit(enabled: Boolean) =
        viewModelScope.launch { gameplay.setMistakeLimitEnabled(enabled) }
    fun setFontScale(scale: Float) = viewModelScope.launch { gameplay.setFontScale(scale) }
    fun toggleColorBlind(enabled: Boolean) =
        viewModelScope.launch { gameplay.setColorBlindMode(enabled) }
    fun setInputMode(mode: InputMode) = viewModelScope.launch { gameplay.setInputMode(mode) }
    fun setPalette(palette: ThemePalette) = viewModelScope.launch { gameplay.setPalette(palette) }
}
