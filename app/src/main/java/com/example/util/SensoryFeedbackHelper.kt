package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object SensoryFeedbackHelper {
    fun vibrateLightTick(context: Context) {
        vibrate(context, 15, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun vibrateSuccess(context: Context) {
        vibrate(context, 40, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun vibrateAlert(context: Context) {
        vibrate(context, 80, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playRewardSound() {
        // Optional acoustic feedback
    }

    private fun vibrate(context: Context, durationMs: Long, amplitude: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}
