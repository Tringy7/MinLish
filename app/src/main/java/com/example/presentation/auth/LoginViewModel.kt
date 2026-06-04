package com.example.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.auth.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        // 1. Validate Input cơ bản tại ViewModel
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = LoginUiState.Error("Email không hợp lệ")
            return
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState.Error("Mật khẩu phải từ 6 ký tự trở lên")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            // 2. Gọi UseCase xử lý logic nghiệp vụ
            when (val result = loginUseCase(email, password)) {
                is LoginResult.Success -> {
                    _uiState.value = LoginUiState.Success
                }
                is LoginResult.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
            }
        }
    }

    fun signUp(email: String, name: String, password: String, level: String) {
        // 1. Validate Input
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = LoginUiState.Error("Email không đúng định dạng")
            return
        }
        if (name.isBlank()) {
            _uiState.value = LoginUiState.Error("Vui lòng nhập tên của bạn")
            return
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState.Error("Mật khẩu quá ngắn (tối thiểu 6 ký tự)")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            // 2. Gọi UseCase đăng ký
            when (val result = signUpUseCase(email, name, password, level)) {
                is SignUpResult.Success -> {
                    _uiState.value = LoginUiState.Success
                }
                is SignUpResult.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    class Factory(
        private val loginUseCase: LoginUseCase,
        private val signUpUseCase: SignUpUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(loginUseCase, signUpUseCase) as T
        }
    }
}
