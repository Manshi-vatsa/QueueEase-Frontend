package com.masterapp.queueeaseapp.queue

import com.masterapp.queueeaseapp.model.BookingResponse
import com.masterapp.queueeaseapp.model.QueueStatusResponse
import com.masterapp.queueeaseapp.model.QueueUser

data class QueueViewData(
    val status: QueueStatusResponse,
    val users: List<QueueUser>
)

sealed class QueueUiState {
    data object Loading : QueueUiState()
    data class Success(val data: QueueViewData) : QueueUiState()
    data class Empty(val status: QueueStatusResponse?) : QueueUiState()
    data class Error(val message: String) : QueueUiState()
}

sealed class JoinQueueState {
    data object Idle : JoinQueueState()
    data object Loading : JoinQueueState()
    data class Success(val booking: BookingResponse) : JoinQueueState()
    data class Error(val message: String) : JoinQueueState()
}
