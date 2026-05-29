package com.example.domain.usecase.auth

import com.example.data.local.entity.UserEntity
import com.example.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow

class GetUserUseCase(private val repository: VocabularyRepository) {
    fun getFlow(): Flow<UserEntity?> {
        return repository.getUserFlow()
    }

    suspend fun get(): UserEntity? {
        return repository.getUser()
    }
}
