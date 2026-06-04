package com.example.domain.usecase.vocabulary

import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.repository.VocabularyWordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetWordsInSetUseCase(private val repository: VocabularyWordRepository) {
    operator fun invoke(setId: Int?): Flow<List<VocabularyWordEntity>> {
        if (setId == null) return flowOf(emptyList())
        return repository.getWordsBySetFlow(setId)
    }
}
