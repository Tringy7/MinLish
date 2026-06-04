package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    override fun getUserFlow(): Flow<UserEntity?> = userDao.getUserFlow()
    override suspend fun getUser(): UserEntity? = userDao.getUser()
    override suspend fun saveUser(user: UserEntity) = userDao.insertUser(user)
}
