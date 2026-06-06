package com.example.domain.usecase.home

import com.example.domain.model.DashboardStats
import com.example.domain.model.DailyActivity
import com.example.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class GetDashboardStatsUseCase(
    private val userRepository: UserRepository,
    private val wordRepository: VocabularyWordRepository,
    private val historyRepository: ReviewHistoryRepository
) {
    operator fun invoke(): Flow<DashboardStats> {
        val startOfToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        return combine(
            userRepository.getUserFlow(),
            wordRepository.getTotalWordsCountFlow(),
            historyRepository.getUniqueWordsReviewedSinceFlow(startOfToday),
            wordRepository.getAllDueWordsFlow(System.currentTimeMillis()),
            historyRepository.getRecentHistoryFlow(7)
        ) { user, total, learnedToday, dueWords, history ->
            val dailyGoal = user?.dailyGoalWords ?: 20
            
            DashboardStats(
                totalWordsCount = total,
                learnedWordsCount = learnedToday, // Hiển thị số từ học/ôn hôm nay
                currentStreak = user?.streakCount ?: 0,
                retentionRate = calculateRetention(history),
                dueTodayCount = dueWords.size,
                dailyActivities = generateDailyActivities(history)
            )
        }.flowOn(Dispatchers.Default)
    }

    private fun calculateRetention(history: List<com.example.data.local.entity.ReviewHistoryEntity>): Int {
        if (history.isEmpty()) return 100
        val goodReviews = history.count { it.rating >= 3 }
        return (goodReviews * 100) / history.size
    }

    private fun generateDailyActivities(history: List<com.example.data.local.entity.ReviewHistoryEntity>): List<DailyActivity> {
        // Implementation logic for chart labels...
        return listOf(
            DailyActivity("Mon", 5),
            DailyActivity("Tue", 8),
            DailyActivity("Wed", 12)
        )
    }
}
