package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.BranchItem
import com.cuso.mobile.model.CreateBranchRequest
import com.cuso.mobile.model.UpdateBranchRequest
import com.cuso.mobile.repository.SalesRepository
import com.cuso.mobile.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── List state (getBranches) ──
sealed class BranchUiState {
    object Loading : BranchUiState()
    data class Success(val branches: List<BranchItem>) : BranchUiState()
    data class Error(val message: String) : BranchUiState()
}

sealed class CreateBranchUiState {
    object Idle : CreateBranchUiState()
    object Loading : CreateBranchUiState()
    data class Success(val branch: BranchItem? = null, val message: String? = null) : CreateBranchUiState()
    data class Error(val message: String) : CreateBranchUiState()
}

sealed class UpdateBranchUiState {
    object Idle : UpdateBranchUiState()
    object Loading : UpdateBranchUiState()
    data class Success(val branch: BranchItem? = null, val message: String? = null) : UpdateBranchUiState()
    data class Error(val message: String) : UpdateBranchUiState()
}

@HiltViewModel
class BranchViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    // ── UI State for listing branches ──
    private val _uiState = MutableStateFlow<BranchUiState>(BranchUiState.Loading)
    val uiState: StateFlow<BranchUiState> = _uiState.asStateFlow()

    // ── Update State ──
    private val _updateState = MutableStateFlow<UpdateBranchUiState>(UpdateBranchUiState.Idle)
    val updateState: StateFlow<UpdateBranchUiState> = _updateState.asStateFlow()

    // ── Create State ──
    private val _createState = MutableStateFlow<CreateBranchUiState>(CreateBranchUiState.Idle)
    val createState: StateFlow<CreateBranchUiState> = _createState.asStateFlow()

    // ── Load Branches ──
    fun loadBranches() {
        if (_uiState.value is BranchUiState.Success) return
        launchBusy {
            _uiState.value = BranchUiState.Loading
//            delay(300000)
            salesRepository.getBranches().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = BranchUiState.Success(response.data)
                    } else {
                        _uiState.value = BranchUiState.Error("Failed to load branches")
                    }
                },
                onFailure = { e ->
                    _uiState.value = BranchUiState.Error(e.message ?: "Something went wrong")
                }
            )
        }
    }

    // ── Refresh Branches ──
    fun refresh() {
        launchBusy {
            _uiState.value = BranchUiState.Loading
            salesRepository.getBranches().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = BranchUiState.Success(response.data)
                    } else {
                        _uiState.value = BranchUiState.Error("Failed to load branches")
                    }
                },
                onFailure = { e ->
                    _uiState.value = BranchUiState.Error(e.message ?: "Something went wrong")
                }
            )
        }
    }

    // ── Update Branch ──
    fun updateBranch(branchId: String, request: UpdateBranchRequest) {
        launchBusy {
            _updateState.value = UpdateBranchUiState.Loading
            salesRepository.updateBranch(branchId, request).fold(
                onSuccess = { (updated, message) ->
                    _updateState.value = UpdateBranchUiState.Success(branch = updated, message = message)
                    val current = _uiState.value
                    if (current is BranchUiState.Success) {
                        val newList = current.branches.map {
                            if (it.id == updated.id) updated else it
                        }
                        _uiState.value = BranchUiState.Success(newList)
                    }
                },
                onFailure = { e ->
                    _updateState.value = UpdateBranchUiState.Error(e.message ?: "Update failed")
                }
            )
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateBranchUiState.Idle
    }

    // ── Create Branch ──
    fun createBranch(request: CreateBranchRequest) {
        launchBusy {
            _createState.value = CreateBranchUiState.Loading
            salesRepository.createBranch(request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _createState.value = CreateBranchUiState.Success(
                            branch = response.data,
                            message = response.message
                        )
                        val current = _uiState.value
                        if (current is BranchUiState.Success) {
                            val newList = current.branches + response.data
                            _uiState.value = BranchUiState.Success(newList)
                        } else {
                            refresh()
                        }
                    } else {
                        _createState.value = CreateBranchUiState.Error(
                            response.message ?: "Failed to create branch"
                        )
                    }
                },
                onFailure = { e ->
                    _createState.value = CreateBranchUiState.Error(
                        e.message ?: "Something went wrong"
                    )
                }
            )
        }
    }

    fun resetCreateState() {
        _createState.value = CreateBranchUiState.Idle
    }
}