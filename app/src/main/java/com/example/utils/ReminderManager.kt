package com.example.utils

import android.content.Context
import androidx.work.*
import com.example.worker.DailyReminderWorker
import com.example.worker.ReviewReminderWorker
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderManager {
    private const val DAILY_WORK_NAME = "daily_reminder_work"
    private const val REVIEW_WORK_NAME = "review_reminder_work"

    fun scheduleDailyReminder(context: Context) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
        }

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val dailyRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(DAILY_WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyRequest
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_WORK_NAME)
    }

    fun scheduleReviewReminder(context: Context) {
        // Kiểm tra từ hạn ôn mỗi 4 tiếng
        val reviewRequest = PeriodicWorkRequestBuilder<ReviewReminderWorker>(4, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag(REVIEW_WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REVIEW_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reviewRequest
        )
    }

    fun cancelReviewReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REVIEW_WORK_NAME)
    }
}
