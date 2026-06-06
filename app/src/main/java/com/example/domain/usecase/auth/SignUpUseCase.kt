package com.example.domain.usecase.auth

import com.example.data.local.entity.UserEntity
import com.example.domain.model.EnglishLevel
import com.example.domain.repository.AuthRepository

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        name: String, 
        email: String, 
        password: String,
        englishLevel: EnglishLevel,
        learningGoal: String
    ): Result<UserEntity> {
        return repository.register(name, email, password, englishLevel, learningGoal)
    }
}
