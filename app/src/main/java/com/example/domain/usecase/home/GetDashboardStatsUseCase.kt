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
            wordRepository.getMasteredWordsCountFlow(),
            wordRepository.getLearningWordsCountFlow(),
            wordRepository.getQuenCountFlow(),
            wordRepository.getLoMoCountFlow(),
            wordRepository.getNhoKipCountFlow(),
            wordRepository.getNhoNgayCountFlow(),
            historyRepository.getRecentHistoryFlow(100)
        ) { args ->
            val user = args[0] as com.example.data.local.entity.UserEntity?
            val totalWords = args[1] as Int
            val learnedWords = args[2] as Int
            val dueCount = args[3] as Int
            val masteredCount = args[4] as Int
            val learningCount = args[5] as Int
            val quenCount = args[6] as Int
            val loMoCount = args[7] as Int
            val nhoKipCount = args[8] as Int
            val nhoNgayCount = args[9] as Int
            val history = args[10] as List<ReviewHistoryEntity>

            val dailyGoal = user?.dailyGoalWords ?: 20

            DashboardStats(
                totalWordsCount = totalWords,
                learnedWordsCount = learnedWords,
                currentStreak = user?.streakCount ?: 0,
                accuracy = calculateAccuracy(history),
                retentionRate = calculateRetention(learnedWords, dailyGoal),
                dueTodayCount = dueCount,
                masteredWordsCount = masteredCount,
                learningWordsCount = learningCount,
                quenCount = quenCount,
                loMoCount = loMoCount,
                nhoKipCount = nhoKipCount,
                nhoNgayCount = nhoNgayCount,
                estimatedLevel = estimateLevel(learnedWords),
                newWordsTodayCount = totalWords - learnedWords,
                dailyActivities = generateDailyActivities(history, now)
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
        learnedCount: Int,
        dueCount: Int
    ): Int {
        val total = learnedCount + dueCount
        if (total == 0) return 100
        return (learnedCount * 100) / total
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
        history: List<ReviewHistoryEntity>,
        baseTime: Long
    ): List<DailyActivity> {

        val sdf = SimpleDateFormat(
            "EEE",
            Locale.getDefault()
        )

        val dayLabels = mutableListOf<String>()
        val activitiesMap = mutableMapOf<String, Int>()
        val calendar = Calendar.getInstance()

        for (i in 6 downTo 0) {
            // Reuse calendar object to reduce allocation in loop
            calendar.timeInMillis = baseTime
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