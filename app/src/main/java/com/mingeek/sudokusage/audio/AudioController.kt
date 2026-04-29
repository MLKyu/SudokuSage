package com.mingeek.sudokusage.audio

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Single entry point UI code calls for audio. Hides [SfxPlayer] / [BgmPlayer] details
 * and stays in sync with [AudioSettings] (DataStore-backed, reactive).
 */
class AudioController(
    context: Context,
    private val settings: AudioSettings,
    appScope: CoroutineScope,
) {
    private val sfx = SfxPlayer(context)
    private val bgm = BgmPlayer(context)

    init {
        sfx.preload()
        settings.state
            .distinctUntilChanged()
            .onEach { s ->
                sfx.setEnabled(s.sfxEnabled)
                sfx.setVolume(s.sfxVolume)
                bgm.setEnabled(s.bgmEnabled)
                bgm.setVolume(s.bgmVolume)
            }
            .launchIn(appScope)
    }

    fun play(event: SoundEvent) = sfx.play(event)

    fun playBgm(track: BgmTrack) = bgm.play(track)

    fun stopBgm() = bgm.stop()

    fun onAppStart() = bgm.onAppStart()

    fun onAppStop() = bgm.onAppStop()

    fun release() {
        sfx.release()
        bgm.release()
    }
}
