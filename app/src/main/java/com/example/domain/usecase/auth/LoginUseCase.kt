package com.example.domain.usecase.auth

import com.example.domain.repository.UserRepository
import com.example.util.HashUtils

sealed class LoginResult {
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class LoginUseCase(private val repository: UserRepository) {
    /**
     * Xử lý logic đăng nhập:
     * 1. Tìm user theo email trong Repository
     * 2. Kiểm tra sự tồn tại của user
     * 3. Băm mật khẩu đầu vào và so sánh với hash trong DB
     * 4. Trả về kết quả thành công hoặc thông báo lỗi cụ thể
     */
    suspend operator fun invoke(email: String, password: String): LoginResult {
        val user = repository.findByEmail(email)
            ?: return LoginResult.Error("Email không tồn tại trên hệ thống")

        val inputHash = HashUtils.sha256(password)
        
        return if (user.passwordHash == inputHash) {
            // Lưu lại user để duy trì session local (Flow trong repository sẽ phát tín hiệu login)
            repository.saveUser(user)
            LoginResult.Success
        } else {
            LoginResult.Error("Mật khẩu không chính xác. Vui lòng thử lại")
        }
    }
}
