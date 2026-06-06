package com.example.presentation.auth

import com.example.data.local.entity.UserEntity

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class RequireSetup(val user: UserEntity) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
