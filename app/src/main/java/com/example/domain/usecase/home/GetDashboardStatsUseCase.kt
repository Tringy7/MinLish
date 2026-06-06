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
            wordRepository.getAllDueWordsFlow(now),
            historyRepository.getRecentHistoryFlow(7)
        ) { user, total, learned, dueWords, history ->
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
                dueTodayCount = dueWords.size,
                newWordsTodayCount = total - learned,
                dailyActivities = generateDailyActivities(history)
            )
        }.flowOn(Dispatchers.Default)
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
