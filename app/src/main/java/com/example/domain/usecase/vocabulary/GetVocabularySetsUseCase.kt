package com.example.domain.usecase.vocabulary

import com.example.data.local.entity.VocabularySetEntity
import com.example.domain.repository.VocabularySetRepository
import kotlinx.coroutines.flow.Flow

class GetVocabularySetsUseCase(private val repository: VocabularySetRepository) {
    operator fun invoke(query: String): Flow<List<VocabularySetEntity>> {
        return if (query.isBlank()) {
            repository.getAllSetsFlow()
        } else {
            repository.searchSetsFlow(query)
        }
    }

    suspend fun getById(id: Int): VocabularySetEntity? {
        return repository.getSetById(id)
    }
}
