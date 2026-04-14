package com.masterapp.queueeaseapp.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: Long,
    val role: String
)