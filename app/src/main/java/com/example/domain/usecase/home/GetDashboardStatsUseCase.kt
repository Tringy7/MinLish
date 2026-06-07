package com.example.domain.usecase.home

import com.example.data.local.entity.ReviewHistoryEntity
import com.example.domain.model.DailyActivity
import com.example.domain.model.DashboardStats
import com.example.domain.repository.ReviewHistoryRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.VocabularyWordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
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
            historyRepository.getRecentHistoryFlow(100)
        ) { user, totalWords, learnedWords, dueCount, history ->

            DashboardStats(
                totalWordsCount = totalWords,
                learnedWordsCount = learnedWords,
                currentStreak = user?.streakCount ?: 0,
                accuracy = calculateAccuracy(history),
                retentionRate = calculateRetention(history),
                dueTodayCount = dueCount,
                estimatedLevel = estimateLevel(learnedWords),
                newWordsTodayCount = totalWords - learnedWords,
                dailyActivities = generateDailyActivities(history)
            )
        }.flowOn(Dispatchers.Default)
    }

    private fun calculateAccuracy(
        history: List<ReviewHistoryEntity>
    ): Int {

        if (history.isEmpty()) return 0

        val successCount = history.count {
            it.rating >= 3
        }

        return (successCount * 100) / history.size
    }

    private fun calculateRetention(
        history: List<ReviewHistoryEntity>
    ): Int {

        if (history.isEmpty()) return 100

        val rememberedCount = history.count {
            it.rating >= 2
        }

        return (rememberedCount * 100) / history.size
    }

    private fun estimateLevel(
        learnedCount: Int
    ): String {

        return when {
            learnedCount < 50 -> "Beginner"
            learnedCount < 200 -> "Intermediate"
            else -> "Advanced"
        }
    }

    private fun generateDailyActivities(
        history: List<ReviewHistoryEntity>
    ): List<DailyActivity> {

        val sdf = SimpleDateFormat(
            "EEE",
            Locale.getDefault()
        )

        val dayLabels = mutableListOf<String>()
        val activitiesMap = mutableMapOf<String, Int>()

        for (i in 6 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            val label = sdf.format(calendar.time)

            dayLabels.add(label)
            activitiesMap[label] = 0
        }

        history.forEach { review ->

            val label = sdf.format(
                Date(review.reviewedAt)
            )

            if (activitiesMap.containsKey(label)) {
                activitiesMap[label] =
                    (activitiesMap[label] ?: 0) + 1
            }
        }

        return dayLabels.map {
            DailyActivity(
                dayLabel = it,
                count = activitiesMap[it] ?: 0
            )
        }
    }
}