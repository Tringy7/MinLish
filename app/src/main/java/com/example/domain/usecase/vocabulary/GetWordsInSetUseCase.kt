package com.example.domain.usecase.vocabulary

import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetWordsInSetUseCase(private val repository: VocabularyRepository) {
    operator fun invoke(setId: Int?): Flow<List<VocabularyWordEntity>> {
        return if (setId != null) repository.getWordsBySetFlow(setId) else flowOf(emptyList())
    }

    suspend fun getWordById(id: Int): VocabularyWordEntity? {
        return repository.getWordById(id)
    }
}
