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
        return try {
            val normalizedEmail = email.trim().lowercase()
            val user = userDao.findByEmail(normalizedEmail) ?: return Result.failure(Exception("Tài khoản không tồn tại"))
            
            if (user.provider != AuthProvider.LOCAL) {
                return Result.failure(Exception("Tài khoản này được đăng nhập bằng Google"))
            }

            val isValid = PasswordHasher.verifyPassword(password, user.passwordHash ?: "")
            if (isValid) {
                val updatedUser = user.copy(lastLoginAt = System.currentTimeMillis())
                userDao.updateUser(updatedUser)
                sessionManager.saveSession(updatedUser.id, AuthProvider.LOCAL)
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Mật khẩu không chính xác"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<UserEntity> {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val existingUser = userDao.findByEmail(normalizedEmail)
            if (existingUser != null) {
                return Result.failure(Exception("Email đã tồn tại"))
            }

            val newUser = UserEntity(
                name = name,
                email = normalizedEmail,
                passwordHash = PasswordHasher.hashPassword(password),
                provider = AuthProvider.LOCAL
            )
            val id = userDao.insertUser(newUser)
            if (id <= 0) return Result.failure(Exception("Không thể tạo tài khoản"))
            
            val createdUser = newUser.copy(id = id.toInt())
            sessionManager.saveSession(createdUser.id, AuthProvider.LOCAL)
            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun googleSignIn(email: String, displayName: String, avatarUrl: String): Result<UserEntity> {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val existingUser = userDao.findByEmail(normalizedEmail)
            
            if (existingUser != null) {
                if (existingUser.provider != AuthProvider.GOOGLE) {
                    return Result.failure(Exception("Tài khoản này đã được đăng ký bằng mật khẩu. Vui lòng đăng nhập bằng Email và Password."))
                }
                // Login existing Google user
                val updatedUser = existingUser.copy(
                    lastLoginAt = System.currentTimeMillis(),
                    avatarUrl = avatarUrl.ifBlank { existingUser.avatarUrl },
                    name = displayName.ifBlank { existingUser.name }
                )
                userDao.updateUser(updatedUser)
                sessionManager.saveSession(updatedUser.id, AuthProvider.GOOGLE)
                Result.success(updatedUser)
            } else {
                // Create new Google user
                val newUser = UserEntity(
                    name = displayName,
                    email = normalizedEmail,
                    avatarUrl = avatarUrl,
                    provider = AuthProvider.GOOGLE,
                    passwordHash = null
                )
                val id = userDao.insertUser(newUser)
                if (id <= 0) return Result.failure(Exception("Không thể tạo tài khoản Google"))
                
                val createdUser = newUser.copy(id = id.toInt())
                sessionManager.saveSession(createdUser.id, AuthProvider.GOOGLE)
                Result.success(createdUser)
            }
        } catch (e: Exception) {
            // Log the error or handle it properly
            Result.failure(e)
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
