package com.mingeek.sudokusage.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gameplayDataStore by preferencesDataStore(name = "gameplay_settings")

enum class ThemeChoice { System, Light, Dark, Amoled }
enum class InputMode { CellFirst, NumberFirst }
enum class ThemePalette { Sage, Forest, Ocean, Sunset, Lavender }

data class GameplayPrefs(
    val theme: ThemeChoice = ThemeChoice.System,
    val mistakeLimitEnabled: Boolean = true,
    /** Multiplier on board glyph size. 1.0 = default; 0.8..1.4 typical range. */
    val fontScale: Float = 1.0f,
    /** When on, swap indigo↔blue and amber↔orange palette to be Deuteranopia-safe. */
    val colorBlindMode: Boolean = false,
    val inputMode: InputMode = InputMode.CellFirst,
    val palette: ThemePalette = ThemePalette.Sage,
)

class GameplaySettings(private val context: Context) {

    private object Keys {
        val Theme = stringPreferencesKey("theme_choice")
        val MistakeLimitEnabled = booleanPreferencesKey("mistake_limit_enabled")
        val FontScale = floatPreferencesKey("font_scale")
        val ColorBlindMode = booleanPreferencesKey("color_blind_mode")
        val InputMode = stringPreferencesKey("input_mode")
        val Palette = stringPreferencesKey("theme_palette")
    }

    val state: Flow<GameplayPrefs> = context.gameplayDataStore.data.map { p ->
        GameplayPrefs(
            theme = p[Keys.Theme]
                ?.let { runCatching { ThemeChoice.valueOf(it) }.getOrNull() }
                ?: ThemeChoice.System,
            mistakeLimitEnabled = p[Keys.MistakeLimitEnabled] ?: true,
            fontScale = p[Keys.FontScale]?.coerceIn(0.7f, 1.6f) ?: 1.0f,
            colorBlindMode = p[Keys.ColorBlindMode] ?: false,
            inputMode = p[Keys.InputMode]
                ?.let { runCatching { InputMode.valueOf(it) }.getOrNull() }
                ?: InputMode.CellFirst,
            palette = p[Keys.Palette]
                ?.let { runCatching { ThemePalette.valueOf(it) }.getOrNull() }
                ?: ThemePalette.Sage,
        )
    }

    suspend fun setTheme(choice: ThemeChoice) {
        context.gameplayDataStore.edit { it[Keys.Theme] = choice.name }
    }

    suspend fun setMistakeLimitEnabled(enabled: Boolean) {
        context.gameplayDataStore.edit { it[Keys.MistakeLimitEnabled] = enabled }
    }

    suspend fun setFontScale(scale: Float) {
        context.gameplayDataStore.edit { it[Keys.FontScale] = scale.coerceIn(0.7f, 1.6f) }
    }

    suspend fun setColorBlindMode(enabled: Boolean) {
        context.gameplayDataStore.edit { it[Keys.ColorBlindMode] = enabled }
    }

    suspend fun setInputMode(mode: InputMode) {
        context.gameplayDataStore.edit { it[Keys.InputMode] = mode.name }
    }

    suspend fun setPalette(palette: ThemePalette) {
        context.gameplayDataStore.edit { it[Keys.Palette] = palette.name }
    }
}
