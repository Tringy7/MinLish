package com.example.domain.repository

import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserFlow(): Flow<UserEntity?>
    suspend fun getUser(): UserEntity?
    suspend fun findByEmail(email: String): UserEntity?
    suspend fun saveUser(user: UserEntity)
    suspend fun logout()
}
