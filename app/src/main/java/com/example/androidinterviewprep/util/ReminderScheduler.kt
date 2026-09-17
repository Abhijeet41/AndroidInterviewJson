package com.example.androidinterviewprep.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.androidinterviewprep.service.ReminderWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME = "PracticeRemindersWork"

    fun scheduleReminders(context: Context) {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(3, TimeUnit.HOURS)
            .setInitialDelay(3, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun scheduleTestReminder(context: Context) {
        val testWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(testWorkRequest)
    }
}
