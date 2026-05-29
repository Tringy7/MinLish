package com.example.domain.usecase.profile

import com.example.data.local.entity.UserEntity
import com.example.domain.repository.VocabularyRepository

class UpdateEnglishLevelUseCase(private val repository: VocabularyRepository) {
    suspend operator fun invoke(level: String) {
        val user = repository.getUser() ?: UserEntity(name = "Learner", email = "learner@minlish.com")
        repository.saveUser(user.copy(englishLevel = level))
    }
}
