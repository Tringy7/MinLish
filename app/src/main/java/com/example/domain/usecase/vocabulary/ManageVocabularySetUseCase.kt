package com.example.domain.usecase.vocabulary

import com.example.data.local.entity.VocabularySetEntity
import com.example.domain.repository.VocabularySetRepository

class ManageVocabularySetUseCase(private val repository: VocabularySetRepository) {
    suspend fun addSet(
        name: String, 
        description: String, 
        tags: String, 
        level: String = "A1", 
        category: String = "General"
    ): Int {
        if (name.isBlank()) return -1
        return repository.insertSet(
            VocabularySetEntity(
                name = name,
                description = description,
                tags = tags,
                level = level,
                category = category,
                userId = null // Repository will fill this for user-created sets
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
