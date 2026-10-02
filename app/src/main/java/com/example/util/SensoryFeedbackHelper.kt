package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Utilidad para retroalimentación háptica y sonora premium.
 * Proporciona micro-sensaciones táctiles y auditivas para reforzar
 * el logro de metas, hábitos completados y culminación de enfoque.
 */
object SensoryFeedbackHelper {
    private const val TAG = "SensoryFeedback"

    fun vibrateSuccess(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Doble toque ligero y refinado
                val timings = longArrayOf(0, 35, 45, 50)
                val amplitudes = intArrayOf(0, 140, 0, 200)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(60)
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo emitir vibración de éxito: ${e.message}")
        }
    }

    fun vibrateLightTick(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo emitir vibración ligera: ${e.message}")
        }
    }

    fun vibrateAlert(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 100, 80, 120)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo emitir vibración de alerta: ${e.message}")
        }
    }

    fun playFocusCompleteCelebration(context: Context) {
        try {
            // Triple pulso háptico celebratorio
            val vibrator = getVibrator(context)
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 70, 60, 90, 60, 140)
                    val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }

            // Tono sonoro armónico y breve de finalización de bloque
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo reproducir alerta de finalización: ${e.message}")
        }
    }

    fun playRewardSound(context: Context? = null) {
        try {
            if (context != null) {
                vibrateSuccess(context)
            }
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo reproducir sonido de recompensa: ${e.message}")
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
