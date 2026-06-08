package com.example.data.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

class JwtService {
    // In production, this should be stored securely (e.g., Keystore or from Server)
    private val secretString = "your-very-long-secret-key-must-be-at-least-32-chars-long"
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secretString.toByteArray())

    private val accessTokenExpiration = 15 * 60 * 1000L // 15 minutes
    private val refreshTokenExpiration = 7 * 24 * 60 * 60 * 1000L // 7 days

    fun generateAccessToken(userId: Int, email: String): String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateRefreshToken(userId: Int): String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + refreshTokenExpiration))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun extractUserId(token: String): Int? {
        return extractClaim(token, Claims::getSubject)?.toIntOrNull()
    }

    fun extractEmail(token: String): String? {
        return extractAllClaims(token)["email"] as? String
    }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
}