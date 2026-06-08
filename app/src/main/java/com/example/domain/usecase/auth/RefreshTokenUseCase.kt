package com.example.domain.usecase.auth

import com.example.domain.repository.AuthRepository

class RefreshTokenUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<String> {
        return repository.refreshToken()
    }
}