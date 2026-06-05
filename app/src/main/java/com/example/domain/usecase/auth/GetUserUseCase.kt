package com.example.domain.usecase.auth

import com.example.data.local.entity.UserEntity
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetUserUseCase(private val repository: AuthRepository) {
    fun getFlow(): Flow<UserEntity?> = repository.getCurrentUserFlow()
    
    suspend operator fun invoke(): UserEntity? = repository.getCurrentUser()
}
