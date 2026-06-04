package com.example.domain.usecase.flashcard

import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.repository.VocabularyWordRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase to retrieve words that are due for review.
 * Business logic: Filter by current system timestamp.
 */
class GetDueWordsUseCase(private val repository: VocabularyWordRepository) {
    
    /**
     * Get all due words across all sets.
     */
    operator fun invoke(): Flow<List<VocabularyWordEntity>> {
        return repository.getAllDueWordsFlow(System.currentTimeMillis())
    }

    /**
     * Get due words for a specific vocabulary set.
     */
    fun getDueWordsForSetFlow(setId: Int): Flow<List<VocabularyWordEntity>> {
        return repository.getDueWordsForSetFlow(setId, System.currentTimeMillis())
    }
}
