package com.masterapp.queueeaseapp.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository = HomeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchCenters()
    }

    fun fetchCenters() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            when (val result = repository.getCenters()) {
                is HomeResult.Success -> _uiState.value = HomeUiState.Success(result.data)
                is HomeResult.Error -> _uiState.value = HomeUiState.Error(result.message)
            }
        }
    }
}
