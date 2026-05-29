package com.example.domain.usecase.home

import com.example.data.local.entity.ReviewHistoryEntity
import com.example.domain.model.DailyActivity
import com.example.domain.model.DashboardStats
import com.example.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.*

class GetDashboardStatsUseCase(private val repository: VocabularyRepository) {

    operator fun invoke(): Flow<DashboardStats> {
        return combine(
            repository.getTotalWordsCountFlow(),
            repository.getLearnedWordsCountFlow(),
            repository.getUserFlow(),
            repository.getAllDueWordsFlow(System.currentTimeMillis()),
            repository.getRecentHistoryFlow(100)
        ) { totalCount, learnedCount, user, dueWords, histories ->
            
            val streak = user?.streakCount ?: 0

            // Base on Good and Easy reviews accuracy rate
            val totalReviews = histories.size
            val correctReviews = histories.count { it.rating >= 3 }
            val retention = if (totalReviews > 0) {
                (correctReviews * 100) / totalReviews
            } else {
                100
            }

            val daysActivity = calculateWeeklyActivity(histories)

            DashboardStats(
                totalWordsCount = totalCount,
                learnedWordsCount = learnedCount,
                currentStreak = streak,
                retentionRate = retention,
                dueTodayCount = dueWords.size,
                dailyActivities = daysActivity
            )
        }
    }

    private fun calculateWeeklyActivity(histories: List<ReviewHistoryEntity>): List<DailyActivity> {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("EE", Locale.getDefault())
        
        val mockMap = mutableMapOf<String, Int>()
        val orderList = ArrayList<String>()
        val copyTime = calendar.timeInMillis
        for (i in 6 downTo 0) {
            calendar.timeInMillis = copyTime
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dayLabel = format.format(calendar.time)
            mockMap[dayLabel] = 0
            orderList.add(dayLabel)
        }

        for (history in histories) {
            val hCal = Calendar.getInstance()
            hCal.timeInMillis = history.reviewedAt
            
            val diffMs = System.currentTimeMillis() - history.reviewedAt
            if (diffMs < 7 * 24 * 60 * 60 * 1000L) {
                val dayLabel = format.format(hCal.time)
                mockMap[dayLabel] = (mockMap[dayLabel] ?: 0) + 1
            }
        }

        return orderList.map { label ->
            DailyActivity(label, mockMap[label] ?: 0)
        }
    }
}
