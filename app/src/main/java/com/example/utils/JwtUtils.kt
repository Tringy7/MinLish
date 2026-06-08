package com.example.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.Date

object JwtUtils {
    // Lưu ý: Trong thực tế, SECRET_KEY không nên để hardcode như thế này
    private val SECRET_KEY = Keys.hmacShaKeyFor("your-very-long-secret-key-must-be-at-least-32-chars".toByteArray())
    private const val EXPIRATION_TIME = 864000000 // 10 ngày

    fun generateAccessToken(userId: Int, email: String): String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateRefreshToken(userId: Int): String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME * 2))
            .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
            .compact()
    }
}