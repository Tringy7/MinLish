package com.example.domain.usecase.auth

import com.example.data.local.entity.UserEntity
import com.example.domain.repository.UserRepository
import com.example.utils.HashUtils

sealed class SignUpResult {
    object Success : SignUpResult()
    data class Error(val message: String) : SignUpResult()
}

class SignUpUseCase(private val repository: UserRepository) {
    /**
     * Xử lý logic đăng ký:
     * 1. Kiểm tra email đã tồn tại chưa
     * 2. Băm mật khẩu
     * 3. Tạo UserEntity mới và lưu vào Repository
     */
    suspend operator fun invoke(email: String, name: String, password: String, level: String): SignUpResult {
        val existingUser = repository.findByEmail(email)
        if (existingUser != null) {
            return SignUpResult.Error("Email này đã được đăng ký bởi người dùng khác")
        }

        val newUser = UserEntity(
            email = email,
            name = name,
            passwordHash = HashUtils.sha256(password),
            englishLevel = level
        )
        
        repository.saveUser(newUser)
        return SignUpResult.Success
    }
}
