package com.masterapp.queueeaseapp.home

import com.masterapp.queueeaseapp.model.CenterResponse

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val centers: List<CenterResponse>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
