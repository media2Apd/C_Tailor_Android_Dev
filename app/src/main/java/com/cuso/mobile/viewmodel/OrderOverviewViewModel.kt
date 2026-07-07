package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.AssignStageResponse
import com.cuso.mobile.model.OrderOverviewData
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderOverviewState {
    object Idle : OrderOverviewState()
    object Loading : OrderOverviewState()
    data class Success(val data: OrderOverviewData) : OrderOverviewState()
    data class Error(val message: String) : OrderOverviewState()
}
sealed class AssignWorkersState {
    object Idle : AssignWorkersState()
    object Loading : AssignWorkersState()
    data class Success(
        val cutting: AssignStageResponse,
        val stitching: AssignStageResponse,
        val qc: AssignStageResponse
    ) : AssignWorkersState()
    data class Error(val message: String) : AssignWorkersState()
}

@HiltViewModel
class OrderOverviewViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _overviewState = MutableStateFlow<OrderOverviewState>(OrderOverviewState.Idle)
    val overviewState: StateFlow<OrderOverviewState> = _overviewState.asStateFlow()
    private val _assignWorkersState = MutableStateFlow<AssignWorkersState>(AssignWorkersState.Idle)
    val assignWorkersState: StateFlow<AssignWorkersState> = _assignWorkersState

    fun fetchSalesOverview(orderId: String) {
        viewModelScope.launch {
            _overviewState.value = OrderOverviewState.Loading
            repository.getSalesOverview(orderId)
                .onSuccess { data ->
                    _overviewState.value = OrderOverviewState.Success(data)
                }
                .onFailure { e ->
                    _overviewState.value = OrderOverviewState.Error(e.message ?: "Failed to load order overview")
                }
        }
    }

    fun assignWorkersToGarment(
        token: String,
        csrfToken: String,
        orderId: String,
        garmentItemId: String,
        quantity: Int,
        cuttingStaffId: String,
        stitchingStaffId: String,
        qcStaffId: String
    ) {
        viewModelScope.launch {
            _assignWorkersState.value = AssignWorkersState.Loading

            val cuttingResult = repository.assignCutting(token, csrfToken, orderId, garmentItemId, cuttingStaffId, quantity)
            val stitchingResult = repository.assignStitching(token, csrfToken, orderId, garmentItemId, stitchingStaffId, quantity)
            val qcResult = repository.assignQc(token, csrfToken, orderId, garmentItemId, qcStaffId, quantity)

            if (cuttingResult.isSuccess && stitchingResult.isSuccess && qcResult.isSuccess) {
                _assignWorkersState.value = AssignWorkersState.Success(
                    cuttingResult.getOrThrow(),
                    stitchingResult.getOrThrow(),
                    qcResult.getOrThrow()
                )
            } else {
                val errorMsg = listOf(cuttingResult, stitchingResult, qcResult)
                    .firstOrNull { it.isFailure }
                    ?.exceptionOrNull()?.message ?: "Assignment failed"
                _assignWorkersState.value = AssignWorkersState.Error(errorMsg)
            }
        }
    }

    fun resetAssignWorkersState() {
        _assignWorkersState.value = AssignWorkersState.Idle
    }

    fun resetState() {
        _overviewState.value = OrderOverviewState.Idle
    }
}