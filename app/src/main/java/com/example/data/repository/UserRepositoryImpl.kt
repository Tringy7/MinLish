package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.security.TokenManager
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImpl(
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : UserRepository {
    
    override fun getUserFlow(): Flow<UserEntity?> {
        return tokenManager.userId.flatMapLatest { id ->
            if (id != null) userDao.getUserFlow(id) else flowOf(null)
        }
    }

    override suspend fun getUser(): UserEntity? {
        val id = tokenManager.userId.first()
        return if (id != null) userDao.getUserById(id) else null
    }
    
    override suspend fun findByEmail(email: String): UserEntity? = userDao.findByEmail(email)

    override suspend fun saveUser(user: UserEntity) {
        userDao.upsertUser(user)
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }
}