package com.example.data.repository

import com.example.data.local.AuthManager
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImpl(
    private val userDao: UserDao,
    private val authManager: AuthManager
) : UserRepository {
    
    override fun getUserFlow(): Flow<UserEntity?> {
        return authManager.currentUserId.flatMapLatest { id ->
            if (id != null) userDao.getUserFlow(id) else flowOf(null)
        }
    }

    override suspend fun getUser(): UserEntity? {
        val id = authManager.currentUserId.first()
        return if (id != null) userDao.getUserById(id) else null
    }
    
    override suspend fun findByEmail(email: String): UserEntity? = userDao.findByEmail(email)

    override suspend fun saveUser(user: UserEntity) {
        userDao.upsertUser(user)
    }

    override suspend fun logout() {
        authManager.clearAuthData()
    }
}
