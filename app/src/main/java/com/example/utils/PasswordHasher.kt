package com.example.utils

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

object PasswordHasher {
    private const val ALGORITHM = "SHA-256"
    private const val SALT_LENGTH = 16

    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = hashWithSalt(password, salt)
        // Store as salt:hash
        return "${Base64.encodeToString(salt, Base64.NO_WRAP)}:${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false
            
            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[1], Base64.NO_WRAP)
            val actualHash = hashWithSalt(password, salt)
            
            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (e: Exception) {
            false
        }
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    private fun hashWithSalt(password: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(ALGORITHM)
        digest.reset()
        digest.update(salt)
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }
}
