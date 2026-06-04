package com.example.domain.usecase.auth

import com.example.domain.repository.UserRepository

class LogoutUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
