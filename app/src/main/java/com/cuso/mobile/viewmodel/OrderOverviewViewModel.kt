package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.AssignStageResponse
import com.cuso.mobile.model.GarmentStageDoc
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

// Stage status update state (Quick Update button) — uses the PATCH
// /assign-worker-to-stage/{orderId}/{garmentItemId}/{stageName} endpoint.
sealed class StageUpdateState {
    object Idle : StageUpdateState()
    data class Loading(val stageId: String) : StageUpdateState()
    data class Success(val stageId: String, val data: GarmentStageDoc) : StageUpdateState()
    data class Error(val stageId: String, val message: String) : StageUpdateState()
}

@HiltViewModel
class OrderOverviewViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _overviewState = MutableStateFlow<OrderOverviewState>(OrderOverviewState.Idle)
    val overviewState: StateFlow<OrderOverviewState> = _overviewState.asStateFlow()

    private val _assignWorkersState = MutableStateFlow<AssignWorkersState>(AssignWorkersState.Idle)
    val assignWorkersState: StateFlow<AssignWorkersState> = _assignWorkersState.asStateFlow()

    private val _stageUpdateState = MutableStateFlow<StageUpdateState>(StageUpdateState.Idle)
    val stageUpdateState: StateFlow<StageUpdateState> = _stageUpdateState.asStateFlow()

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
        orderId: String,
        garmentItemId: String,
        quantity: Int,
        cuttingStaffId: String,
        stitchingStaffId: String,
        qcStaffId: String
    ) {
        viewModelScope.launch {
            _assignWorkersState.value = AssignWorkersState.Loading

            val cuttingResult = repository.assignCutting(
                orderId = orderId,
                garmentItemId = garmentItemId,
                staffId = cuttingStaffId,
                quantity = quantity
            )
            val cuttingData = cuttingResult.getOrElse { e ->
                _assignWorkersState.value = AssignWorkersState.Error("Cutting assignment failed: ${e.message}")
                return@launch
            }

            val stitchingResult = repository.assignStitching(
                orderId = orderId,
                garmentItemId = garmentItemId,
                staffId = stitchingStaffId,
                quantity = quantity
            )
            val stitchingData = stitchingResult.getOrElse { e ->
                _assignWorkersState.value = AssignWorkersState.Error("Stitching assignment failed: ${e.message}")
                return@launch
            }

            val qcResult = repository.assignQc(
                orderId = orderId,
                garmentItemId = garmentItemId,
                staffId = qcStaffId,
                quantity = quantity
            )
            val qcData = qcResult.getOrElse { e ->
                _assignWorkersState.value = AssignWorkersState.Error("QC assignment failed: ${e.message}")
                return@launch
            }

            _assignWorkersState.value = AssignWorkersState.Success(
                cutting = cuttingData,
                stitching = stitchingData,
                qc = qcData
            )
        }
    }


    fun updateStage(
        orderId: String,
        garmentItemId: String,
        stageId: String,
        stageName: String,
        status: String
    ) {
        viewModelScope.launch {
            _stageUpdateState.value = StageUpdateState.Loading(stageId)

            repository.updateStage(
                orderId = orderId,
                garmentItemId = garmentItemId,
                stageName = stageName,
                status = status
            )
                .onSuccess { data ->
                    _stageUpdateState.value = StageUpdateState.Success(stageId, data)
                }
                .onFailure { e ->
                    _stageUpdateState.value = StageUpdateState.Error(stageId, e.message ?: "Failed to update stage")
                }
        }
    }

    fun resetStageUpdateState() {
        _stageUpdateState.value = StageUpdateState.Idle
    }

    fun resetAssignWorkersState() {
        _assignWorkersState.value = AssignWorkersState.Idle
    }

//    fun resetState() {
//        _overviewState.value = OrderOverviewState.Idle
//    }
}