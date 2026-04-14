package com.masterapp.queueeaseapp.model

data class QueueUser(
    val bookingId: Long,
    val userId: Long,
    val centerId: Long,
    val queueNumber: Int
)