package com.example.domain.usecase.flashcard

import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow

class GetDueWordsUseCase(private val repository: VocabularyRepository) {
    operator fun invoke(): Flow<List<VocabularyWordEntity>> {
        return repository.getAllDueWordsFlow(System.currentTimeMillis())
    }

    fun getDueWordsForSetFlow(setId: Int): Flow<List<VocabularyWordEntity>> {
        return repository.getDueWordsForSetFlow(setId, System.currentTimeMillis())
    }
}
