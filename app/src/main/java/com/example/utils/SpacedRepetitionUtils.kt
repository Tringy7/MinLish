package com.example.utils

import com.example.data.local.entity.VocabularyWordEntity
import java.util.Calendar
import kotlin.math.max

object SpacedRepetitionUtils {

    /**
     * Calculates the next review date and SRS parameters using SM-2 algorithm.
     * rating: 1 (Again), 2 (Hard), 3 (Good), 4 (Easy)
     */
    fun calculateNextReview(word: VocabularyWordEntity, rating: Int): VocabularyWordEntity {
        var repetitions = word.repetitions
        var easeFactor = word.easeFactor
        var intervalDays = word.intervalDays

        if (rating >= 2) { // 2: Hard, 3: Good, 4: Easy
            if (repetitions == 0) {
                intervalDays = 1
            } else if (repetitions == 1) {
                intervalDays = 6
            } else {
                intervalDays = (intervalDays * easeFactor).toInt()
            }
            repetitions++
        } else { // 1: Again
            repetitions = 0
            intervalDays = 1
        }

        // Adjust ease factor based on rating
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        // q is rating mapped to 0-5 scale. Our 1-4 maps roughly to 0, 2, 4, 5
        val q = when (rating) {
            1 -> 0 // Again
            2 -> 2 // Hard
            3 -> 4 // Good
            4 -> 5 // Easy
            else -> 3
        }
        
        easeFactor += (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        if (easeFactor < 1.3) easeFactor = 1.3

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, intervalDays)
        
        // Reset to start of day for cleaner scheduling
        calendar.set(Calendar.HOUR_OF_DAY, 4) // 4 AM is a good reset time
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        return word.copy(
            repetitions = repetitions,
            easeFactor = easeFactor,
            intervalDays = intervalDays,
            nextReviewTimestamp = calendar.timeInMillis,
            lastReviewedTimestamp = System.currentTimeMillis()
        )
    }
}
