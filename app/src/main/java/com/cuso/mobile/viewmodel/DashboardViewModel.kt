package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import com.cuso.mobile.model.sales.DashboardData
import com.cuso.mobile.repository.DashboardRepository
import com.cuso.mobile.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class DashboardUiState {
    object Idle : DashboardUiState()
    object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Idle)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        launchBusy {
            _uiState.value = DashboardUiState.Loading
//            delay(300000)


            dashboardRepository.getAdvancedDashboard()
                .onSuccess { data ->
                    _uiState.value = DashboardUiState.Success(data)
                }
                .onFailure { error ->
                    _uiState.value = DashboardUiState.Error(
                        error.message ?: "Something went wrong"
                    )
                }
        }
    }

    fun retry() {
        loadDashboard()
    }
}