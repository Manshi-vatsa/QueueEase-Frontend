package com.masterapp.queueeaseapp.model

data class CenterResponse(
    val id: Long,
    val name: String?,      // ✅ nullable
    val location: String?,  // ✅ nullable
    val type: String?       // ✅ nullable
)