package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.DesignationCreateRequest
import com.cuso.mobile.model.DesignationItem
import com.cuso.mobile.model.DesignationUpdateRequest
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DesignationUiState {
    object Loading : DesignationUiState()
    data class Success(val items: List<DesignationItem>) : DesignationUiState()
    data class Error(val message: String) : DesignationUiState()
}

// ✅ ADD THIS - Update State
sealed class DesignationUpdateState {
    object Idle : DesignationUpdateState()
    object Loading : DesignationUpdateState()
    data class Success(val message: String) : DesignationUpdateState()
    data class Error(val message: String) : DesignationUpdateState()
}

// ✅ ADD THIS - Delete State
sealed class DesignationDeleteState {
    object Idle : DesignationDeleteState()
    object Loading : DesignationDeleteState()
    data class Success(val message: String) : DesignationDeleteState()
    data class Error(val message: String) : DesignationDeleteState()
}

@HiltViewModel
class DesignationViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DesignationUiState>(DesignationUiState.Loading)
    val uiState: StateFlow<DesignationUiState> = _uiState.asStateFlow()

    private val _createState = MutableStateFlow<DesignationCreateState>(DesignationCreateState.Idle)
    val createState: StateFlow<DesignationCreateState> = _createState.asStateFlow()

    // ✅ ADD THIS - Update State Flow
    private val _updateState = MutableStateFlow<DesignationUpdateState>(DesignationUpdateState.Idle)
    val updateState: StateFlow<DesignationUpdateState> = _updateState.asStateFlow()

    // ✅ ADD THIS - Delete State Flow
    private val _deleteState = MutableStateFlow<DesignationDeleteState>(DesignationDeleteState.Idle)
    val deleteState: StateFlow<DesignationDeleteState> = _deleteState.asStateFlow()

    // ── Load Designations ──
    fun loadDesignations() {
        viewModelScope.launch {
            _uiState.value = DesignationUiState.Loading
            repository.getDesignations()
                .onSuccess { _uiState.value = DesignationUiState.Success(it) }
                .onFailure { _uiState.value = DesignationUiState.Error(it.message ?: "Error") }
        }
    }

    // ── Create Designation ──
    fun createDesignation(name: String, code: String, description: String) {
        viewModelScope.launch {
            _createState.value = DesignationCreateState.Loading
            repository.createDesignation(DesignationCreateRequest(name, code, description))
                .onSuccess {
                    _createState.value = DesignationCreateState.Success
                    loadDesignations() // refresh list
                }
                .onFailure {
                    _createState.value = DesignationCreateState.Error(it.message ?: "Create failed")
                }
        }
    }

    // ✅ ADD THIS - Update Designation
    fun updateDesignation(id: String, name: String, code: String, description: String?) {
        viewModelScope.launch {
            _updateState.value = DesignationUpdateState.Loading
            val request = DesignationUpdateRequest(
                name = name,
                code = code,
                description = description,
                status = true
            )
            repository.updateDesignation(id, request)
                .onSuccess { response ->
                    _updateState.value = DesignationUpdateState.Success(response.message)
                    loadDesignations() // refresh list
                }
                .onFailure {
                    _updateState.value = DesignationUpdateState.Error(it.message ?: "Update failed")
                }
        }
    }

    // ✅ ADD THIS - Delete Designation
    fun deleteDesignation(id: String) {
        viewModelScope.launch {
            _deleteState.value = DesignationDeleteState.Loading
            repository.deleteDesignation(id)
                .onSuccess { response ->
                    _deleteState.value = DesignationDeleteState.Success(response.message)
                    loadDesignations() // refresh list
                }
                .onFailure {
                    _deleteState.value = DesignationDeleteState.Error(it.message ?: "Delete failed")
                }
        }
    }

    // ── Reset States ──
    fun resetCreateState() {
        _createState.value = DesignationCreateState.Idle
    }

    // ✅ ADD THIS - Reset Update State
    fun resetUpdateState() {
        _updateState.value = DesignationUpdateState.Idle
    }

    // ✅ ADD THIS - Reset Delete State
    fun resetDeleteState() {
        _deleteState.value = DesignationDeleteState.Idle
    }
}