package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.security.JwtService
import com.example.data.security.TokenManager
import com.example.domain.model.AuthProvider
import com.example.domain.model.EnglishLevel
import com.example.domain.repository.AuthRepository
import com.example.utils.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val tokenManager: TokenManager,
    private val jwtService: JwtService
) : AuthRepository {

    override fun observeIsLoggedIn(): Flow<Boolean> = tokenManager.accessToken.flatMapLatest { token ->
        flowOf(!token.isNullOrBlank())
    }

    override fun observeCurrentUserId(): Flow<Int?> = tokenManager.userId
    
    override fun observeCurrentProvider(): Flow<AuthProvider?> = tokenManager.provider

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
                
                val accessToken = jwtService.generateAccessToken(updatedUser.id, updatedUser.email)
                val refreshToken = jwtService.generateRefreshToken(updatedUser.id)
                
                tokenManager.saveTokens(accessToken, refreshToken, updatedUser.id, AuthProvider.LOCAL)
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Mật khẩu không chính xác"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        name: String, 
        email: String, 
        password: String,
        englishLevel: EnglishLevel,
        learningGoal: String
    ): Result<UserEntity> {
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
                provider = AuthProvider.LOCAL,
                englishLevel = englishLevel,
                learningGoal = learningGoal
            )
            val id = userDao.insertUser(newUser)
            if (id <= 0) return Result.failure(Exception("Không thể tạo tài khoản"))

            val createdUser = newUser.copy(id = id.toInt())

            val accessToken = jwtService.generateAccessToken(createdUser.id, createdUser.email)
            val refreshToken = jwtService.generateRefreshToken(createdUser.id)

            tokenManager.saveTokens(accessToken, refreshToken, createdUser.id, AuthProvider.LOCAL)
            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun googleSignIn(email: String, displayName: String, avatarUrl: String): Result<UserEntity> {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val existingUser = userDao.findByEmail(normalizedEmail)

            val userToAuth = if (existingUser != null) {
                if (existingUser.provider != AuthProvider.GOOGLE) {
                    return Result.failure(Exception("Tài khoản này đã được đăng ký bằng mật khẩu. Vui lòng đăng nhập bằng Email."))
                }
                val updatedUser = existingUser.copy(
                    lastLoginAt = System.currentTimeMillis(),
                    avatarUrl = avatarUrl.ifBlank { existingUser.avatarUrl },
                    name = displayName.ifBlank { existingUser.name }
                )
                userDao.updateUser(updatedUser)
                updatedUser
            } else {
                val newUser = UserEntity(
                    name = displayName,
                    email = normalizedEmail,
                    avatarUrl = avatarUrl,
                    provider = AuthProvider.GOOGLE,
                    passwordHash = null,
                    englishLevel = EnglishLevel.A1,
                    learningGoal = ""
                )
                val id = userDao.insertUser(newUser)
                if (id <= 0) return Result.failure(Exception("Không thể tạo tài khoản Google"))
                newUser.copy(id = id.toInt())
            }

            val accessToken = jwtService.generateAccessToken(userToAuth.id, userToAuth.email)
            val refreshToken = jwtService.generateRefreshToken(userToAuth.id)

            tokenManager.saveTokens(accessToken, refreshToken, userToAuth.id, userToAuth.provider)
            Result.success(userToAuth)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<String> {
        return try {
            val refreshToken = tokenManager.getRefreshToken() ?: return Result.failure(Exception("No refresh token"))
            if (jwtService.validateToken(refreshToken)) {
                val userId = jwtService.extractUserId(refreshToken) ?: return Result.failure(Exception("Invalid token payload"))
                val user = userDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
                
                val newAccessToken = jwtService.generateAccessToken(user.id, user.email)
                val newRefreshToken = jwtService.generateRefreshToken(user.id)
                
                tokenManager.saveTokens(newAccessToken, newRefreshToken, user.id, user.provider)
                Result.success(newAccessToken)
            } else {
                tokenManager.clearTokens()
                Result.failure(Exception("Refresh token expired"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }

    override suspend fun getCurrentUser(): UserEntity? {
        val id = tokenManager.userId.first()
        return if (id != null) userDao.getUserById(id) else null
    }

    override fun getCurrentUserFlow(): Flow<UserEntity?> {
        return tokenManager.userId.flatMapLatest { id ->
            if (id != null) userDao.getUserFlow(id) else flowOf(null)
        }
    }
}