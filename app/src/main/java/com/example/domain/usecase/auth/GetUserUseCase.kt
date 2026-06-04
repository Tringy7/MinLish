package com.example.domain.usecase.auth

import com.example.data.local.entity.UserEntity
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetUserUseCase(private val repository: UserRepository) {
    fun getFlow(): Flow<UserEntity?> = repository.getUserFlow()
    suspend operator fun invoke(): UserEntity? = repository.getUser()
}
