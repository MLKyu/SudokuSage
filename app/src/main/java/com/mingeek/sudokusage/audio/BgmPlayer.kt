package com.mingeek.sudokusage.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer

/**
 * Looping background music player. One [MediaPlayer] is reused across track changes.
 *
 * Resolves [BgmTrack] -> raw resource by name; missing assets are silently no-op so
 * the app behaves correctly on first build before audio is delivered.
 *
 * Lifecycle: callers should invoke [onAppStart] / [onAppStop] from the host
 * Activity / process to pause music when the app goes to background.
 */
class BgmPlayer(private val context: Context) {

    private var player: MediaPlayer? = null
    private var currentTrack: BgmTrack? = null
    private var pausedByLifecycle: Boolean = false

    @Volatile
    private var enabled: Boolean = true
    @Volatile
    private var volume: Float = 0.5f

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) stop() else currentTrack?.let { play(it) }
    }

    fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
        player?.setVolume(this.volume, this.volume)
    }

    fun play(track: BgmTrack) {
        if (!enabled) {
            currentTrack = track
            return
        }
        if (currentTrack == track && player?.isPlaying == true) return

        val resId = context.resources.getIdentifier(
            track.assetName, "raw", context.packageName
        )
        if (resId == 0) {
            currentTrack = track
            return
        }

        stop()
        currentTrack = track
        player = MediaPlayer.create(context, resId)?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            isLooping = true
            setVolume(volume, volume)
            start()
        }
    }

    fun stop() {
        player?.runCatching {
            if (isPlaying) stop()
            release()
        }
        player = null
    }

    fun onAppStart() {
        if (pausedByLifecycle) {
            pausedByLifecycle = false
            currentTrack?.let { play(it) }
        }
    }

    fun onAppStop() {
        if (player?.isPlaying == true) {
            player?.pause()
            pausedByLifecycle = true
        }
    }

    fun release() {
        stop()
        currentTrack = null
    }
}
