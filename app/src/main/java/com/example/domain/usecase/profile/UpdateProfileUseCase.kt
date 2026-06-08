package com.example.domain.usecase.profile

import com.example.domain.model.EnglishLevel
import com.example.domain.repository.UserRepository

class UpdateProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(
        name: String? = null,
        avatarUrl: String? = null,
        englishLevel: EnglishLevel? = null,
        learningGoal: String? = null,
        dailyGoalWords: Int? = null
    ) {
        val user = repository.getUser()
        user?.let {
            repository.saveUser(it.copy(
                name = name ?: it.name,
                avatarUrl = avatarUrl ?: it.avatarUrl,
                englishLevel = englishLevel ?: it.englishLevel,
                learningGoal = learningGoal ?: it.learningGoal,
                dailyGoalWords = dailyGoalWords ?: it.dailyGoalWords
            ))
        }
    }
}
