package com.mingeek.sudokusage.audio

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.audioDataStore by preferencesDataStore(name = "audio_settings")

/**
 * Player-feedback preferences. Despite the file name, this also captures the
 * haptic toggle since haptics are a peer to SFX and the user expects to flip
 * them together.
 */
data class AudioSettingsState(
    val bgmEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val bgmVolume: Float = 0.5f,
    val sfxVolume: Float = 0.8f,
)

class AudioSettings(private val context: Context) {
    private object Keys {
        val BgmEnabled = booleanPreferencesKey("bgm_enabled")
        val SfxEnabled = booleanPreferencesKey("sfx_enabled")
        val HapticEnabled = booleanPreferencesKey("haptic_enabled")
        val BgmVolume = floatPreferencesKey("bgm_volume")
        val SfxVolume = floatPreferencesKey("sfx_volume")
    }

    val state: Flow<AudioSettingsState> = context.audioDataStore.data.map { p ->
        AudioSettingsState(
            bgmEnabled = p[Keys.BgmEnabled] ?: true,
            sfxEnabled = p[Keys.SfxEnabled] ?: true,
            hapticEnabled = p[Keys.HapticEnabled] ?: true,
            bgmVolume = p[Keys.BgmVolume] ?: 0.5f,
            sfxVolume = p[Keys.SfxVolume] ?: 0.8f,
        )
    }

    suspend fun setBgmEnabled(enabled: Boolean) {
        context.audioDataStore.edit { it[Keys.BgmEnabled] = enabled }
    }

    suspend fun setSfxEnabled(enabled: Boolean) {
        context.audioDataStore.edit { it[Keys.SfxEnabled] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.audioDataStore.edit { it[Keys.HapticEnabled] = enabled }
    }

    suspend fun setBgmVolume(volume: Float) {
        context.audioDataStore.edit { it[Keys.BgmVolume] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setSfxVolume(volume: Float) {
        context.audioDataStore.edit { it[Keys.SfxVolume] = volume.coerceIn(0f, 1f) }
    }
}
