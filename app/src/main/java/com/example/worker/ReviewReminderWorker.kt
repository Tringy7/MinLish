package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.di.ServiceLocator
import com.example.utils.NotificationHelper

class ReviewReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val wordRepo = ServiceLocator.provideVocabularyWordRepository(applicationContext)
        val dueWordsCount = wordRepo.getAllDueWords(System.currentTimeMillis()).size
        
        if (dueWordsCount > 0) {
            NotificationHelper.showReviewReminder(applicationContext, dueWordsCount)
        }
        
        return Result.success()
    }
}
