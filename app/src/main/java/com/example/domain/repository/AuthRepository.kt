package com.example.domain.repository

import com.example.data.local.entity.UserEntity
import com.example.domain.model.AuthProvider
import com.example.domain.model.EnglishLevel
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeIsLoggedIn(): Flow<Boolean>
    fun observeCurrentUserId(): Flow<Int?>
    fun observeCurrentProvider(): Flow<AuthProvider?>
    
    suspend fun login(email: String, password: String): Result<UserEntity>
    suspend fun register(
        name: String, 
        email: String, 
        password: String,
        englishLevel: EnglishLevel,
        learningGoal: String
    ): Result<UserEntity>
    suspend fun googleSignIn(email: String, displayName: String, avatarUrl: String): Result<UserEntity>

    suspend fun logout()
    suspend fun getCurrentUser(): UserEntity?
    fun getCurrentUserFlow(): Flow<UserEntity?>
}
