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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
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

// ─── Upload Picture State ───
sealed class UploadPictureUiState {
    object Idle : UploadPictureUiState()
    object Loading : UploadPictureUiState()
    data class Success(val message: String) : UploadPictureUiState()
    data class Error(val message: String) : UploadPictureUiState()
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

    private val _uploadPictureState = MutableStateFlow<UploadPictureUiState>(UploadPictureUiState.Idle)
    val uploadPictureState: StateFlow<UploadPictureUiState> = _uploadPictureState.asStateFlow()


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

    // ─── Upload Organization Picture ───
    fun uploadOrganizationPicture(token: String, pictureFile: java.io.File) {
        viewModelScope.launch {
            _uploadPictureState.value = UploadPictureUiState.Loading

            val result = salesRepository.uploadOrganizationPicture("Bearer $token", pictureFile)
            result.fold(
                onSuccess = { response ->
                    _uploadPictureState.value = UploadPictureUiState.Success(
                        response.message ?: "Organization picture uploaded"
                    )
                    refreshOrganization(token)   // fresh logo URL kaatikkaraduku
                },
                onFailure = { e ->
                    _uploadPictureState.value = UploadPictureUiState.Error(
                        e.message ?: "Something went wrong"
                    )
                }
            )
        }
    }

    // ─── Update Organization (text fields + optional Base64 logo, all in one JSON request) ───
    // ─── Update Organization (text fields) + Upload Picture (separate API) ───
    fun updateOrganization(
        token: String,
        request: UpdateOrganizationRequest,
        logoFile: java.io.File? = null
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateOrgUiState.Loading

            // Step 1: If a new picture was picked, upload it FIRST via the dedicated upload API
            if (logoFile != null) {
                val uploadResult = salesRepository.uploadOrganizationPicture("Bearer $token", logoFile)
                if (uploadResult.isFailure) {
                    _updateState.value = UpdateOrgUiState.Error(
                        uploadResult.exceptionOrNull()?.message ?: "Failed to upload picture"
                    )
                    return@launch   // stop here if picture upload itself failed
                }
            }

            // Step 2: Update the remaining text fields (no image part sent here anymore)
            val result = salesRepository.updateOrganization("Bearer $token", request, null)
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _updateState.value = UpdateOrgUiState.Success(
                            response.message ?: "Organization updated successfully"
                        )
                        refreshOrganization(token)   // pulls fresh org data incl. new picture URL
                    } else {
                        _updateState.value = UpdateOrgUiState.Error(
                            response.message ?: "Failed to update organization"
                        )
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
    fun resetUploadPictureState() {
        _uploadPictureState.value = UploadPictureUiState.Idle
    }
}