package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.BranchItem
import com.cuso.mobile.model.UpdateBranchRequest
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

// ── Update state (updateBranch) ──
sealed class UpdateBranchUiState {
    object Idle : UpdateBranchUiState()
    object Loading : UpdateBranchUiState()
    object Success : UpdateBranchUiState()
    data class Error(val message: String) : UpdateBranchUiState()
}

@HiltViewModel
class BranchViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BranchUiState>(BranchUiState.Loading)
    val uiState: StateFlow<BranchUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateBranchUiState>(UpdateBranchUiState.Idle)
    val updateState: StateFlow<UpdateBranchUiState> = _updateState.asStateFlow()

    fun loadBranches() {
        if (_uiState.value is BranchUiState.Success) return
        viewModelScope.launch {
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

    fun refresh() {
        _uiState.value = BranchUiState.Loading
        loadBranches()
    }

    // ── Update Branch ──
    fun updateBranch(branchId: String, request: UpdateBranchRequest) {
        viewModelScope.launch {
            _updateState.value = UpdateBranchUiState.Loading
            salesRepository.updateBranch(branchId, request).fold(
                onSuccess = { updated ->
                    _updateState.value = UpdateBranchUiState.Success
                    // patch the updated branch into the current list so UI reflects it immediately
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
}