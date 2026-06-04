package com.example.domain.usecase.auth

import com.example.data.local.entity.UserEntity
import com.example.domain.repository.UserRepository

class LoginUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(email: String, name: String) {
        val user = UserEntity(
            id = "local_user",
            name = name,
            email = email
        )
        repository.saveUser(user)
    }
}
