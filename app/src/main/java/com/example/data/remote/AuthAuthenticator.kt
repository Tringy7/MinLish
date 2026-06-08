package com.example.data.remote

import com.example.data.security.JwtService
import com.example.data.security.TokenManager
import com.example.data.local.dao.UserDao
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthAuthenticator(
    private val tokenManager: TokenManager,
    private val jwtService: JwtService,
    private val userDao: UserDao
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null

            if (!jwtService.validateToken(refreshToken)) {
                runBlocking { tokenManager.clearTokens() }
                return null
            }

            // In a real app, you would call a Retrofit API here:
            // val newTokens = authApiService.refreshToken(refreshToken).execute()
            
            // For this implementation, we regenerate tokens based on the valid refresh token
            val userId = jwtService.extractUserId(refreshToken) ?: return null
            val user = runBlocking { userDao.getUserById(userId) } ?: return null
            
            val newAccessToken = jwtService.generateAccessToken(user.id, user.email)
            val newRefreshToken = jwtService.generateRefreshToken(user.id)
            
            runBlocking {
                tokenManager.saveTokens(newAccessToken, newRefreshToken, user.id, user.provider)
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }
    }
}