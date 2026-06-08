package com.example.domain.usecase

import java.util.Calendar
import kotlin.math.max

data class SM2Result(
    val repetitions: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val nextReviewTimestamp: Long
)

object SpacedRepetitionCalculator {

    /**
     * Calculates the next review parameters based on the SM-2 algorithm.
     * 
     * @param repetitions Number of times this word has been successfully reviewed in a row.
     * @param previousIntervalDays Previous interval duration in days.
     * @param previousEaseFactor Previous Ease Factor (EF) of the card (default is 2.5).
     * @param userRating Review grade: 0 (Again), 2 (Hard), 4 (Good), 5 (Easy).
     * @return Updated SM2Result parameters.
     */
    fun calculate(
        repetitions: Int,
        previousIntervalDays: Int,
        previousEaseFactor: Double,
        userRating: Int
    ): SM2Result {
        // q is quality 0-5
        val q = userRating

        val nextEaseFactor = maxOf(1.3, previousEaseFactor + 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        
        val (nextRepetitions, nextIntervalDays) = when {
            q < 3 -> Pair(0, 1)
            repetitions == 0 -> Pair(1, 1)
            repetitions == 1 -> Pair(2, 6)
            else -> Pair(repetitions + 1, (previousIntervalDays * nextEaseFactor).let { kotlin.math.round(it).toInt() })
        }

        // Calculate next review timestamp
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, nextIntervalDays)
        // Set clock to clear hour boundaries
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return SM2Result(
            repetitions = nextRepetitions,
            intervalDays = nextIntervalDays,
            easeFactor = nextEaseFactor,
            nextReviewTimestamp = calendar.timeInMillis
        )
    }
}
