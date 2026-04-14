package com.masterapp.queueeaseapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> = _registerState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            when (val result = authRepository.login(email = email, password = password)) {
                is AuthResult.Success -> {
                    _loginState.value = AuthUiState.Success(
                        role = result.data.role,
                        userId = result.data.userId,
                        message = "Login successful."
                    )
                }

                is AuthResult.Error -> {
                    _loginState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            when (val result = authRepository.register(name = name, email = email, password = password)) {
                is AuthResult.Success -> {
                    _registerState.value = AuthUiState.Success(
                        message = "Registration successful. Please log in."
                    )
                }

                is AuthResult.Error -> {
                    _registerState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthUiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthUiState.Idle
    }
}
