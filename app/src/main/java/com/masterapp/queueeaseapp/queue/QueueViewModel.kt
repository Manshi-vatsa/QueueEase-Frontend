package com.masterapp.queueeaseapp.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterapp.queueeaseapp.model.QueueStatusResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QueueViewModel(
    private val repository: QueueRepository = QueueRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<QueueUiState>(QueueUiState.Loading)
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    private val _joinState = MutableStateFlow<JoinQueueState>(JoinQueueState.Idle)
    val joinState: StateFlow<JoinQueueState> = _joinState.asStateFlow()

    private var autoRefreshJob: Job? = null

    fun loadQueue(userId: Long, centerId: Long) {
        viewModelScope.launch {
            _uiState.value = QueueUiState.Loading
            fetchQueueData(userId, centerId)
        }
    }

    fun startAutoRefresh(userId: Long, centerId: Long) {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                fetchQueueData(userId, centerId)
                delay(10_000L)
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun joinQueue(userId: Long, centerId: Long) {
        viewModelScope.launch {
            _joinState.value = JoinQueueState.Loading
            when (val joinResult = repository.joinQueue(userId, centerId)) {
                is QueueResult.Success -> {
                    _joinState.value = JoinQueueState.Success(joinResult.data)
                    fetchQueueData(userId, centerId)
                }

                is QueueResult.Error -> _joinState.value = JoinQueueState.Error(joinResult.message)
            }
        }
    }

    fun resetJoinState() {
        _joinState.value = JoinQueueState.Idle
    }

    private suspend fun fetchQueueData(userId: Long, centerId: Long) {
        val statusResult = repository.getQueueStatus(userId, centerId)
        val listResult = repository.getQueueList(centerId)

        when {
            statusResult is QueueResult.Error -> {
                _uiState.value = QueueUiState.Error(statusResult.message)
            }

            listResult is QueueResult.Error -> {
                _uiState.value = QueueUiState.Error(listResult.message)
            }

            statusResult is QueueResult.Success && listResult is QueueResult.Success -> {
                val status = statusResult.data
                val users = listResult.data
                _uiState.value = if (users.isEmpty()) {
                    QueueUiState.Empty(status)
                } else {
                    QueueUiState.Success(QueueViewData(status = status, users = users))
                }
                maybeMarkTurnApproaching(status)
            }
        }
    }

    private fun maybeMarkTurnApproaching(status: QueueStatusResponse) {
        // Notification trigger condition is exposed through status thresholds.
        // Activity decides how to show local notification when condition is true.
    }

    override fun onCleared() {
        stopAutoRefresh()
        super.onCleared()
    }
}
