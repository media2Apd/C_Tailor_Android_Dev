package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.OrganizationDataWrapper
import com.cuso.mobile.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val data: OrganizationDataWrapper) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadOrganization(token: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = authRepository.getMyOrganization(token)
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
}