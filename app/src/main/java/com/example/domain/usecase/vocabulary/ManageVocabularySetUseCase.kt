package com.example.domain.usecase.vocabulary

import com.example.data.local.entity.VocabularySetEntity
import com.example.domain.repository.VocabularyRepository

class ManageVocabularySetUseCase(private val repository: VocabularyRepository) {
    suspend fun addSet(name: String, description: String, tags: String): Int {
        if (name.isBlank()) return -1
        return repository.insertSet(
            VocabularySetEntity(
                name = name,
                description = description,
                tags = tags
            )
        )
    }

    suspend fun updateSet(set: VocabularySetEntity) {
        repository.updateSet(set)
    }

    suspend fun deleteSet(set: VocabularySetEntity) {
        repository.deleteSet(set)
    }
}
