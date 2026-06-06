package com.example.domain.usecase.auth

import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveLoginStateUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Boolean> = repository.observeIsLoggedIn()
}
