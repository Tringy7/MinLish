package com.example.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.utils.NotificationHelper

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        NotificationHelper.showDailyReminder(applicationContext)
        return Result.success()
    }
}
