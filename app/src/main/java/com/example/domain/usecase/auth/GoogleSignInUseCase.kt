package com.example.domain.usecase.auth

import com.example.data.local.entity.UserEntity
import com.example.domain.repository.AuthRepository

class GoogleSignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, displayName: String, avatarUrl: String): Result<UserEntity> {
        return repository.googleSignIn(email, displayName, avatarUrl)
    }
}
