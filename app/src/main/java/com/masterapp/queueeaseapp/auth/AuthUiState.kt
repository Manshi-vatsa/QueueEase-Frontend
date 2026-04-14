package com.masterapp.queueeaseapp.auth

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(
        val role: String? = null,
        val userId: Long? = null,
        val message: String? = null
    ) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
