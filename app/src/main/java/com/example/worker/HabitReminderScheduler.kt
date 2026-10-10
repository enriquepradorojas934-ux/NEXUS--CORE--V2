package com.example.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object HabitReminderScheduler {
    private const val WORK_NAME = "habit_daily_reminder_work"

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun triggerImmediateTestReminder(context: Context) {
        val request = OneTimeWorkRequestBuilder<HabitReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
