package com.example.domain.usecase.home

import com.example.domain.model.DashboardStats
import com.example.domain.model.DailyActivity
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
            wordRepository.getAllDueWordsFlow(now),
            historyRepository.getRecentHistoryFlow(7)
        ) { user, total, learned, dueWords, history ->
            DashboardStats(
                totalWordsCount = total,
                learnedWordsCount = learned,
                currentStreak = user?.streakCount ?: 0,
                retentionRate = calculateRetention(history),
                dueTodayCount = dueWords.size,
                newWordsTodayCount = total - learned,
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
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        // Initialize last 7 days with 0 counts
        val activitiesMap = mutableMapOf<String, Int>()
        val dayLabels = mutableListOf<String>()
        
        for (i in 6 downTo 0) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.DAY_OF_YEAR, -i)
            val label = sdf.format(tempCal.time)
            dayLabels.add(label)
            activitiesMap[label] = 0
        }

        // Aggregate history counts
        history.forEach { entry ->
            val entryDate = sdf.format(Date(entry.reviewedAt))
            if (activitiesMap.containsKey(entryDate)) {
                activitiesMap[entryDate] = activitiesMap[entryDate]!! + 1
            }
        }

        return dayLabels.map { DailyActivity(it, activitiesMap[it] ?: 0) }
    }
}
