package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Programador utilitario que orquesta tareas en WorkManager para enviar
 * recordatorios diarios de hábitos a una hora específica configurada por el usuario.
 */
object HabitReminderScheduler {

    private const val TAG = "HabitReminderScheduler"

    /**
     * Programa o actualiza el recordatorio periódico diario con WorkManager.
     * @param context Contexto de la aplicación.
     * @param hour Hora del día en formato 24h (0-23).
     * @param minute Minuto de la hora (0-59).
     */
    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val initialDelay = calculateInitialDelayMillis(hour, minute)
        Log.d(TAG, "Programando recordatorio diario a las %02d:%02d. Delay inicial: %d min.".format(hour, minute, initialDelay / 1000 / 60))

        val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(HabitReminderWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HabitReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancela la tarea periódica programada en WorkManager.
     */
    fun cancelDailyReminder(context: Context) {
        Log.d(TAG, "Cancelando recordatorio diario de hábitos.")
        WorkManager.getInstance(context).cancelUniqueWork(HabitReminderWorker.WORK_NAME)
    }

    /**
     * Encola una ejecución inmediata única de prueba para verificar
     * el envío de notificaciones y la recolección de hábitos sin esperar.
     */
    fun triggerImmediateTestReminder(context: Context) {
        Log.d(TAG, "Disparando recordatorio inmediato de prueba.")
        val testRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .addTag("${HabitReminderWorker.TAG}_TEST")
            .build()

        WorkManager.getInstance(context).enqueue(testRequest)
    }

    /**
     * Calcula los milisegundos restantes hasta la próxima ocurrencia de hour:minute.
     * Si la hora ya pasó en el día actual, calcula el tiempo hasta esa misma hora mañana.
     */
    fun calculateInitialDelayMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now) || target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}
