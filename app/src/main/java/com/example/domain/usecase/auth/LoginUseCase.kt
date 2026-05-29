package com.example.domain.usecase.auth

import com.example.data.local.entity.UserEntity
import com.example.domain.repository.VocabularyRepository

class LoginUseCase(private val repository: VocabularyRepository) {
    suspend operator fun invoke(email: String, name: String) {
        repository.saveUser(
            UserEntity(
                id = "local_user",
                name = name,
                email = email,
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=256&auto=format&fit=crop",
                englishLevel = "B2 - Upper Intermediate", // default level
                streakCount = 1,
                lastStudyDate = System.currentTimeMillis()
            )
        )
    }
}
