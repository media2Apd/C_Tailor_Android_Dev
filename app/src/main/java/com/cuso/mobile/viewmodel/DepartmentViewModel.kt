package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.DepartmentItem
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DepartmentUiState {
    object Loading : DepartmentUiState()
    data class Success(val departments: List<DepartmentItem>) : DepartmentUiState()
    data class Error(val message: String) : DepartmentUiState()
}

@HiltViewModel
class DepartmentViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DepartmentUiState>(DepartmentUiState.Loading)
    val uiState: StateFlow<DepartmentUiState> = _uiState.asStateFlow()

    fun loadDepartments() {
        if (_uiState.value is DepartmentUiState.Success) return
        viewModelScope.launch {
            _uiState.value = DepartmentUiState.Loading
            salesRepository.getDepartments().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = DepartmentUiState.Success(response.data)
                    } else {
                        _uiState.value = DepartmentUiState.Error("Failed to load departments")
                    }
                },
                onFailure = { e ->
                    _uiState.value = DepartmentUiState.Error(e.message ?: "Something went wrong")
                }
            )
        }
    }

    fun refresh() {
        _uiState.value = DepartmentUiState.Loading
        loadDepartments()
    }
}