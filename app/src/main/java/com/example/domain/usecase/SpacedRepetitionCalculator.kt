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
     * @param userRating Review grade: 1 (Again), 2 (Hard), 3 (Good), 4 (Easy).
     * @return Updated SM2Result parameters.
     */
    fun calculate(
        repetitions: Int,
        previousIntervalDays: Int,
        previousEaseFactor: Double,
        userRating: Int
    ): SM2Result {
        // Map user input 1..4 to SM-2 quality grades (q) from 0..5
        // 1: Again -> q = 1 (incorrect, memory trace disappearing)
        // 2: Hard  -> q = 3 (correct, but with serious difficulty)
        // 3: Good  -> q = 4 (correct, after a hesitation)
        // 4: Easy  -> q = 5 (correct, perfect response)
        val q = when (userRating) {
            1 -> 1
            2 -> 3
            3 -> 4
            4 -> 5
            else -> 4
        }

        val nextRepetitions: Int
        val nextIntervalDays: Int
        var nextEaseFactor = previousEaseFactor

        if (q < 3) {
            // Memory lapse (Again state) – reset repetitions, set interval to 1 day
            nextRepetitions = 0
            nextIntervalDays = 1
            
            // Adjust Ease Factor down for a lapse
            nextEaseFactor = max(1.3, previousEaseFactor - 0.2)
        } else {
            // Successful recall
            nextRepetitions = repetitions + 1
            
            nextIntervalDays = when (nextRepetitions) {
                1 -> 1
                2 -> 6
                else -> {
                    val floatInterval = previousIntervalDays * previousEaseFactor
                    max(1.0, kotlin.math.round(floatInterval)).toInt()
                }
            }

            // Adjust EF based on comfort of recall: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
            val deltaEF = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)
            nextEaseFactor = max(1.3, previousEaseFactor + deltaEF)
        }

        // Calculate next review timestamp (rounded to start of hour for predictability on current day)
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
