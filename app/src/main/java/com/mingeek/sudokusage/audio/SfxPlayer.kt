package com.mingeek.sudokusage.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.util.concurrent.ConcurrentHashMap

/**
 * Low-latency one-shot player for UI/game sound effects, backed by [SoundPool].
 *
 * Resolves [SoundEvent] -> raw resource by name so missing assets are no-ops
 * (the catalog can grow ahead of audio delivery without breaking the build).
 */
class SfxPlayer(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    /** event -> SoundPool sample id (only present for events with a backing asset). */
    private val sampleIds = ConcurrentHashMap<SoundEvent, Int>()

    @Volatile
    private var enabled: Boolean = true
    @Volatile
    private var volume: Float = 0.8f

    fun preload() {
        SoundEvent.values().forEach { event ->
            val resId = context.resources.getIdentifier(
                event.assetName, "raw", context.packageName
            )
            if (resId != 0) {
                sampleIds[event] = soundPool.load(context, resId, 1)
            }
        }
    }

    fun setEnabled(enabled: Boolean) { this.enabled = enabled }
    fun setVolume(volume: Float) { this.volume = volume.coerceIn(0f, 1f) }

    fun play(event: SoundEvent) {
        if (!enabled) return
        val id = sampleIds[event] ?: return
        soundPool.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() {
        sampleIds.clear()
        soundPool.release()
    }

    private companion object {
        const val MAX_STREAMS = 6
    }
}
