package com.masterapp.queueeaseapp.model

data class QueueStatusResponse(
    val queueNumber: Int,
    val peopleAhead: Int,
    val estimatedWaitTime: Int,
    val recommendation: String,
    val currentServing: Int
)