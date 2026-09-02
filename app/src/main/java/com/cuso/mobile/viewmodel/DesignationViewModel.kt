package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import com.cuso.mobile.model.settings.DesignationCreateRequest
import com.cuso.mobile.model.settings.DesignationItem
import com.cuso.mobile.model.settings.DesignationUpdateRequest
import com.cuso.mobile.repository.SalesRepository
import com.cuso.mobile.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class DesignationUiState {
    object Loading : DesignationUiState()
    data class Success(val items: List<DesignationItem>) : DesignationUiState()
    data class Error(val message: String) : DesignationUiState()
}

//     - Update State
sealed class DesignationUpdateState {
    object Idle : DesignationUpdateState()
    object Loading : DesignationUpdateState()
    data class Success(val message: String) : DesignationUpdateState()
    data class Error(val message: String) : DesignationUpdateState()
}

//     - Delete State
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

    //     - Update State Flow
    private val _updateState = MutableStateFlow<DesignationUpdateState>(DesignationUpdateState.Idle)
    val updateState: StateFlow<DesignationUpdateState> = _updateState.asStateFlow()

    //     - Delete State Flow
    private val _deleteState = MutableStateFlow<DesignationDeleteState>(DesignationDeleteState.Idle)
    val deleteState: StateFlow<DesignationDeleteState> = _deleteState.asStateFlow()

    // ── Load Designations ──
    fun loadDesignations() {
        launchBusy {
            _uiState.value = DesignationUiState.Loading
            repository.getDesignations()
                .onSuccess { _uiState.value = DesignationUiState.Success(it) }
                .onFailure { _uiState.value = DesignationUiState.Error(it.message ?: "Error") }
        }
    }

    // ── Create Designation ──
    fun createDesignation(name: String, code: String, description: String) {
        launchBusy {
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

    //     - Update Designation
    fun updateDesignation(id: String, name: String, code: String, description: String?) {
        launchBusy {
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

    //     - Delete Designation
    fun deleteDesignation(id: String) {
        launchBusy {
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

    //     - Reset Update State
    fun resetUpdateState() {
        _updateState.value = DesignationUpdateState.Idle
    }

    //     - Reset Delete State
    fun resetDeleteState() {
        _deleteState.value = DesignationDeleteState.Idle
    }
}