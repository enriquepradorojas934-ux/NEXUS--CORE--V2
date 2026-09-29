package com.example.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.database.AppDatabase
import com.example.domain.DailyCycleEngine
import kotlinx.coroutines.flow.first

/**
 * Worker ejecutado por WorkManager para enviar recordatorios diarios
 * al usuario sobre sus hábitos pendientes de completar en el ciclo activo.
 */
class HabitReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Ejecutando HabitReminderWorker...")

        return try {
            val database = AppDatabase.getDatabase(appContext)
            val dao = database.personalOsDao()

            // 1. Obtener ciclo diario actual
            val configs = dao.getAllConfigs().first()
            val cycleStartTime = configs.find { it.configKey == "cycle_start_time" }?.configValue
                ?: DailyCycleEngine.DEFAULT_CYCLE_START_TIME
            val cycleDate = DailyCycleEngine.getCurrentCycleDate(cycleStartTime)

            // 2. Obtener hábitos activos y logs del ciclo
            val activeHabits = dao.getActiveHabits().first()
            if (activeHabits.isEmpty()) {
                Log.d(TAG, "No hay hábitos activos para recordar.")
                return Result.success()
            }

            val habitLogs = dao.getHabitLogsByCycle(cycleDate).first()
            val completedHabitIds = habitLogs.filter { it.isCompleted }.map { it.habitId }.toSet()
            val pendingHabits = activeHabits.filter { it.id !in completedHabitIds }
            val savedLanguage = configs.find { it.configKey == "app_language" }?.configValue ?: "Español"

            // 3. Crear y emitir notificación del sistema
            sendNotification(
                pendingCount = pendingHabits.size,
                totalCount = activeHabits.size,
                sampleHabitNames = pendingHabits.take(3).map { it.title },
                language = savedLanguage
            )

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la ejecución de HabitReminderWorker: ${e.message}", e)
            Result.retry()
        }
    }

    private fun sendNotification(
        pendingCount: Int,
        totalCount: Int,
        sampleHabitNames: List<String>,
        language: String = "Español"
    ) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        createNotificationChannel(notificationManager)

        // Verificar permiso en Android 13+ (POST_NOTIFICATIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                Log.w(TAG, "Permiso POST_NOTIFICATIONS no concedido. No se puede mostrar la notificación.")
                return
            }
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val isSpanish = com.example.util.AppStrings.isSpanish(language)
        val (title, content) = if (pendingCount > 0) {
            val habitsPreview = sampleHabitNames.joinToString(", ")
            val titleText = if (isSpanish) {
                "¡Momento de tus hábitos! ($pendingCount pendientes)"
            } else {
                "Habits check-in! ($pendingCount pending)"
            }
            val contentText = if (habitsPreview.isNotBlank()) {
                if (isSpanish) "Pendientes: $habitsPreview. ¡Mantén tu racha activa hoy!"
                else "Pending: $habitsPreview. Keep your daily streak alive!"
            } else {
                if (isSpanish) "Tienes $pendingCount hábitos pendientes de completar hoy."
                else "You have $pendingCount habits pending for today."
            }
            Pair(titleText, contentText)
        } else {
            Pair(
                if (isSpanish) "¡Excelente consistencia!" else "Outstanding consistency!",
                if (isSpanish) "Has completado todos tus $totalCount hábitos del día. ¡Gran trabajo!"
                else "You have completed all $totalCount habits today. Great execution!"
            )
        }

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Hábitos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones diarias programadas para revisar y marcar hábitos en el Sistema Personal."
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val TAG = "HabitReminderWorker"
        const val WORK_NAME = "habit_daily_reminder_work"
        const val CHANNEL_ID = "channel_habit_reminders"
        const val NOTIFICATION_ID = 1001
    }
}
