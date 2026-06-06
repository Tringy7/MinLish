package com.example.domain.usecase.profile

import com.example.domain.model.EnglishLevel
import com.example.domain.repository.UserRepository

class UpdateEnglishLevelUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(level: EnglishLevel) {
        val user = repository.getUser()
        user?.let {
            repository.saveUser(it.copy(englishLevel = level))
        }
    }
}
