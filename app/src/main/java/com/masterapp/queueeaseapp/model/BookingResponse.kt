package com.masterapp.queueeaseapp.model

data class BookingResponse(
    val userId: Long,
    val queueNumber: Int,
    val message: String
)