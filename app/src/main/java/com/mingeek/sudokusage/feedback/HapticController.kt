package com.mingeek.sudokusage.feedback

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager

/**
 * Wraps the system [android.os.Vibrator] using API 31+ predefined effects.
 * Honors the user's haptic-enabled toggle and silently no-ops on devices
 * without a vibrator.
 *
 * minSdk for this app is 36, so [VibratorManager] is always present.
 */
class HapticController(context: Context) {

    private val vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator

    @Volatile
    private var enabled: Boolean = true

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun perform(event: HapticEvent) {
        if (!enabled || !vibrator.hasVibrator()) return
        val effectId = when (event) {
            HapticEvent.Tap -> VibrationEffect.EFFECT_TICK
            HapticEvent.Confirm -> VibrationEffect.EFFECT_CLICK
            HapticEvent.Reject -> VibrationEffect.EFFECT_DOUBLE_CLICK
            HapticEvent.Triumph -> VibrationEffect.EFFECT_HEAVY_CLICK
        }
        vibrator.vibrate(VibrationEffect.createPredefined(effectId))
    }
}
