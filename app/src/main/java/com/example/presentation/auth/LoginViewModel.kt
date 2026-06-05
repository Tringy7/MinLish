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
    private val signUpUseCase: SignUpUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
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
            val result = loginUseCase(email, password)
            if (result.isSuccess) {
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error(result.exceptionOrNull()?.message ?: "Đăng nhập thất bại")
            }
        }
    }

    fun signUp(email: String, name: String, password: String) {
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
            val result = signUpUseCase(name, email, password)
            if (result.isSuccess) {
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error(result.exceptionOrNull()?.message ?: "Đăng ký thất bại")
            }
        }
    }

    fun googleSignIn(email: String, displayName: String, avatarUrl: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = googleSignInUseCase(email, displayName, avatarUrl)
            if (result.isSuccess) {
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error(result.exceptionOrNull()?.message ?: "Google Sign-In thất bại")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    class Factory(private val context: android.content.Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val authRepo = com.example.di.ServiceLocator.provideAuthRepository(context)
            return LoginViewModel(
                loginUseCase = LoginUseCase(authRepo),
                signUpUseCase = SignUpUseCase(authRepo),
                googleSignInUseCase = GoogleSignInUseCase(authRepo)
            ) as T
        }
    }
}
