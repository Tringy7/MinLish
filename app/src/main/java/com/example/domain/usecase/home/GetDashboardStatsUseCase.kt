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
        return combine(
            userRepository.getUserFlow(),
            wordRepository.getTotalWordsCountFlow(),
            wordRepository.getLearnedWordsCountFlow(),
            historyRepository.getRecentHistoryFlow(7)
        ) { user, total, learned, history ->
            DashboardStats(
                totalWordsCount = total,
                learnedWordsCount = learned,
                currentStreak = user?.streakCount ?: 0,
                retentionRate = calculateRetention(history),
                dueTodayCount = 0,
                dailyActivities = generateDailyActivities(history)
            )
        }.flowOn(Dispatchers.Default) // Đảm bảo tính toán chạy trên background
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
