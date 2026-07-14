package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.OrganizationDataWrapper
import com.cuso.mobile.model.UpdateOrganizationRequest
import com.cuso.mobile.repository.AuthRepository
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State for loading organization ───
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val data: OrganizationDataWrapper) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

// ─── Update State ───
sealed class UpdateOrgUiState {
    object Idle : UpdateOrgUiState()
    object Loading : UpdateOrgUiState()
    data class Success(val message: String) : UpdateOrgUiState()
    data class Error(val message: String) : UpdateOrgUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateOrgUiState>(UpdateOrgUiState.Idle)
    val updateState: StateFlow<UpdateOrgUiState> = _updateState.asStateFlow()

    // ─── Load Organization ───
    fun loadOrganization(token: String) {
        if (_uiState.value is ProfileUiState.Success) return
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = authRepository.getMyOrganization("Bearer $token")
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = ProfileUiState.Success(response.data)
                    } else {
                        _uiState.value = ProfileUiState.Error("Failed to load organization")
                    }
                },
                onFailure = { e ->
                    _uiState.value = ProfileUiState.Error(e.message ?: "Something went wrong")
                }
            )
        }
    }

    // ─── Refresh Organization (used after update, so UI shows the fresh logo URL too) ───
    fun refreshOrganization(token: String) {
        viewModelScope.launch {
            val result = authRepository.getMyOrganization("Bearer $token")
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = ProfileUiState.Success(response.data)
                    }
                },
                onFailure = { /* keep existing state if refresh fails */ }
            )
        }
    }

    // ─── Update Organization (text fields + optional Base64 logo, all in one JSON request) ───
    fun updateOrganization(token: String, request: UpdateOrganizationRequest) {
        viewModelScope.launch {
            _updateState.value = UpdateOrgUiState.Loading
            val result = salesRepository.updateOrganization("Bearer $token", request)
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _updateState.value = UpdateOrgUiState.Success(response.message ?: "Organization updated successfully")
                        refreshOrganization(token)
                    } else {
                        _updateState.value = UpdateOrgUiState.Error(response.message ?: "Failed to update organization")
                    }
                },
                onFailure = { e ->
                    _updateState.value = UpdateOrgUiState.Error(e.message ?: "Something went wrong")
                }
            )
        }
    }

    // ─── Reset Update State ───
    fun resetUpdateState() {
        _updateState.value = UpdateOrgUiState.Idle
    }
}