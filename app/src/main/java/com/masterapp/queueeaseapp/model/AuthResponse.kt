package com.masterapp.queueeaseapp.model

data class AuthResponse(
    val message: String,
    val token: String?,
    val userId: Long,
    val role: String
)