package com.example.domain.usecase.profile

import com.example.domain.model.EnglishLevel
import com.example.domain.repository.UserRepository

class UpdateProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(englishLevel: EnglishLevel, learningGoal: String) {
        val user = repository.getUser()
        user?.let {
            repository.saveUser(it.copy(
                englishLevel = englishLevel,
                learningGoal = learningGoal
            ))
        }
    }
}
