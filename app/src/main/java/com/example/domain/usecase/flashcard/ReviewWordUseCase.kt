package com.example.domain.usecase.flashcard

import com.example.data.local.entity.ReviewHistoryEntity
import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.repository.ReviewHistoryRepository
import com.example.domain.repository.VocabularyWordRepository
import com.example.domain.usecase.SpacedRepetitionCalculator
import com.example.domain.usecase.profile.UpdateStreakUseCase

/**
 * Core business logic for reviewing a word.
 */
class ReviewWordUseCase(
    private val wordRepository: VocabularyWordRepository,
    private val historyRepository: ReviewHistoryRepository,
    private val updateStreakUseCase: UpdateStreakUseCase
) {
    suspend operator fun invoke(word: VocabularyWordEntity, rating: Int) {
        // Calculate algorithm results
        val sm2Result = SpacedRepetitionCalculator.calculate(
            repetitions = word.repetitions,
            previousIntervalDays = word.intervalDays,
            previousEaseFactor = word.easeFactor,
            userRating = rating
        )

        // Prepare updated entity
        val updatedWord = word.copy(
            repetitions = sm2Result.repetitions,
            intervalDays = sm2Result.intervalDays,
            easeFactor = sm2Result.easeFactor,
            nextReviewTimestamp = sm2Result.nextReviewTimestamp,
            lastReviewedTimestamp = System.currentTimeMillis(),
            lastQuality = rating
        )

        // Persistence operations
        wordRepository.updateWord(updatedWord)
        
        // Log to history
        val historyEntry = ReviewHistoryEntity(
            wordId = word.id,
            userId = word.userId,
            rating = rating,
            reviewedAt = System.currentTimeMillis()
        )
        historyRepository.insertHistory(historyEntry)

        // Business side-effect: Update user streak
        updateStreakUseCase()
    }
}
