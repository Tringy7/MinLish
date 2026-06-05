package com.example.data.repository

import com.example.data.local.SessionManager
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.domain.model.AuthProvider
import com.example.domain.repository.AuthRepository
import com.example.utils.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : AuthRepository {

    override fun observeIsLoggedIn(): Flow<Boolean> = sessionManager.isLoggedIn

    override fun observeCurrentUserId(): Flow<Int?> = sessionManager.currentUserId
    
    override fun observeCurrentProvider(): Flow<AuthProvider?> = sessionManager.currentProvider

    override suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.findByEmail(email) ?: return Result.failure(Exception("Tài khoản không tồn tại"))
        
        if (user.provider != AuthProvider.LOCAL) {
            return Result.failure(Exception("Tài khoản này được đăng nhập bằng Google"))
        }

        val isValid = PasswordHasher.verifyPassword(password, user.passwordHash ?: "")
        return if (isValid) {
            userDao.updateUser(user.copy(lastLoginAt = System.currentTimeMillis()))
            sessionManager.saveSession(user.id, AuthProvider.LOCAL)
            Result.success(user)
        } else {
            Result.failure(Exception("Mật khẩu không chính xác"))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<UserEntity> {
        val existingUser = userDao.findByEmail(email)
        if (existingUser != null) {
            return Result.failure(Exception("Email đã tồn tại"))
        }

        val newUser = UserEntity(
            name = name,
            email = email,
            passwordHash = PasswordHasher.hashPassword(password),
            provider = AuthProvider.LOCAL
        )
        val id = userDao.insertUser(newUser)
        if (id == -1L) return Result.failure(Exception("Không thể tạo tài khoản"))
        
        val createdUser = newUser.copy(id = id.toInt())
        sessionManager.saveSession(createdUser.id, AuthProvider.LOCAL)
        return Result.success(createdUser)
    }

    override suspend fun googleSignIn(email: String, displayName: String, avatarUrl: String): Result<UserEntity> {
        val existingUser = userDao.findByEmail(email)
        
        if (existingUser != null) {
            if (existingUser.provider != AuthProvider.GOOGLE) {
                return Result.failure(Exception("Tài khoản này đã được đăng ký bằng mật khẩu. Vui lòng đăng nhập bằng Email và Password."))
            }
            // Login existing Google user
            userDao.updateUser(existingUser.copy(
                lastLoginAt = System.currentTimeMillis(),
                avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else existingUser.avatarUrl,
                name = if (displayName.isNotBlank()) displayName else existingUser.name
            ))
            sessionManager.saveSession(existingUser.id, AuthProvider.GOOGLE)
            return Result.success(existingUser)
        } else {
            // Create new Google user
            val newUser = UserEntity(
                name = displayName,
                email = email,
                avatarUrl = avatarUrl,
                provider = AuthProvider.GOOGLE,
                passwordHash = null
            )
            val id = userDao.insertUser(newUser)
            if (id == -1L) return Result.failure(Exception("Không thể tạo tài khoản Google"))
            
            val createdUser = newUser.copy(id = id.toInt())
            sessionManager.saveSession(createdUser.id, AuthProvider.GOOGLE)
            return Result.success(createdUser)
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override suspend fun getCurrentUser(): UserEntity? {
        val id = sessionManager.currentUserId.first()
        return if (id != null) userDao.getUserById(id) else null
    }

    override fun getCurrentUserFlow(): Flow<UserEntity?> {
        return sessionManager.currentUserId.flatMapLatest { id ->
            if (id != null) userDao.getUserFlow(id) else flowOf(null)
        }
    }
}
