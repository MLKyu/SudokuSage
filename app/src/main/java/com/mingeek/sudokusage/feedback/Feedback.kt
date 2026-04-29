package com.mingeek.sudokusage.feedback

import com.mingeek.sudokusage.audio.AudioController
import com.mingeek.sudokusage.audio.SoundEvent

/**
 * Couples sound effects with haptic intent. UI calls a single method per
 * domain event and gets both modalities — disabled toggles handled inside
 * each underlying controller.
 *
 * BGM is *not* routed through here; callers use [AudioController] directly
 * for music control.
 */
class Feedback(
    private val audio: AudioController,
    private val haptic: HapticController,
) {
    fun emit(event: SoundEvent) {
        audio.play(event)
        hapticFor(event)?.let(haptic::perform)
    }

    private fun hapticFor(event: SoundEvent): HapticEvent? = when (event) {
        SoundEvent.UiTap -> HapticEvent.Tap
        SoundEvent.UiToggle -> HapticEvent.Tap
        SoundEvent.UiBack -> HapticEvent.Tap
        SoundEvent.UiOpen, SoundEvent.UiClose -> null
        SoundEvent.CellSelect -> HapticEvent.Tap
        SoundEvent.NumberPlace -> HapticEvent.Confirm
        SoundEvent.NumberErase -> HapticEvent.Tap
        SoundEvent.NoteToggle -> HapticEvent.Tap
        SoundEvent.Mistake -> HapticEvent.Reject
        SoundEvent.HintReveal -> HapticEvent.Confirm
        SoundEvent.LineComplete -> HapticEvent.Confirm
        SoundEvent.PuzzleComplete -> HapticEvent.Triumph
        SoundEvent.StreakUp -> HapticEvent.Triumph
        SoundEvent.Achievement -> HapticEvent.Triumph
    }
}
