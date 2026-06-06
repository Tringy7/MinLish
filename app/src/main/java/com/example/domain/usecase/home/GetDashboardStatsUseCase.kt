package com.example.domain.usecase.home

import com.example.data.local.entity.ReviewHistoryEntity
import com.example.domain.model.DailyActivity
import com.example.domain.model.DashboardStats
import com.example.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class GetDashboardStatsUseCase(
    private val userRepository: UserRepository,
    private val wordRepository: VocabularyWordRepository,
    private val historyRepository: ReviewHistoryRepository
) {
    operator fun invoke(): Flow<DashboardStats> {
        val now = System.currentTimeMillis()
        return combine(
            userRepository.getUserFlow(),
            wordRepository.getTotalWordsCountFlow(),
            wordRepository.getLearnedWordsCountFlow(),
            wordRepository.getDueWordsCountFlow(now),
            historyRepository.getAllHistoryFlow()
        ) { user, total, learned, due, history ->
            DashboardStats(
                totalWordsCount = total,
                learnedWordsCount = learned,
                currentStreak = user?.streakCount ?: 0,
                accuracy = calculateAccuracy(history),
                retentionRate = calculateRetention(history),
                dueTodayCount = due,
                estimatedLevel = estimateLevel(learned),
                dailyActivities = generateDailyActivities(history)
            )
        }.flowOn(Dispatchers.Default)
    }

    private fun calculateAccuracy(history: List<ReviewHistoryEntity>): Int {
        if (history.isEmpty()) return 0
        // Accuracy = % of reviews that are Good (3) or Easy (4)
        val successfulReviews = history.count { it.rating >= 3 }
        return (successfulReviews * 100) / history.size
    }

    private fun calculateRetention(history: List<ReviewHistoryEntity>): Int {
        if (history.isEmpty()) return 100
        // Retention rate over last 50 reviews
        val recentHistory = history.takeLast(50)
        val remembered = recentHistory.count { it.rating >= 2 } // Not "Again"
        return (remembered * 100) / recentHistory.size
    }

    private fun estimateLevel(learnedCount: Int): String {
        return when {
            learnedCount < 50 -> "Beginner"
            learnedCount < 200 -> "Intermediate"
            else -> "Advanced"
        }
    }

    private fun generateDailyActivities(history: List<ReviewHistoryEntity>): List<DailyActivity> {
        val dateFormat = SimpleDateFormat("EE", Locale.getDefault())
        val last7Days = mutableMapOf<String, Int>()
        
        val cal = Calendar.getInstance()
        for (i in 0..6) {
            val date = dateFormat.format(cal.time)
            last7Days[date] = 0
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        history.forEach { review ->
            val date = dateFormat.format(Date(review.reviewedAt))
            if (last7Days.containsKey(date)) {
                last7Days[date] = (last7Days[date] ?: 0) + 1
            }
        }

        return last7Days.map { DailyActivity(it.key, it.value) }.reversed()
    }
}
