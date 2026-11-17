package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import com.cuso.tailor.model.settings.DesignationCreateRequest
import com.cuso.tailor.model.settings.DesignationItem
import com.cuso.tailor.model.settings.DesignationUpdateRequest
import com.cuso.tailor.repository.SalesRepository
import com.cuso.tailor.utils.PagingUiState
import com.cuso.tailor.utils.launchBusy
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

    // ── Paginated UI State for PaginatedDataList ──
    private val _designationsPagingState =
        MutableStateFlow(PagingUiState<DesignationItem>(isInitialLoading = true))
    val designationsPagingState: StateFlow<PagingUiState<DesignationItem>> =
        _designationsPagingState.asStateFlow()

    private var currentSearchQuery: String = ""
    private var currentPage: Int = 1
    private val pageSize: Int = 10

    fun onDesignationSearchChanged(query: String) {
        currentSearchQuery = query
        rebuildPagingState(resetPage = true)
    }

    fun loadMoreDesignations() {
        val current = _designationsPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return
        currentPage += 1
        rebuildPagingState(resetPage = false)
    }

    private fun rebuildPagingState(resetPage: Boolean) {
        if (resetPage) currentPage = 1

        val allDesignations = (_uiState.value as? DesignationUiState.Success)?.items ?: emptyList()
        val filtered = if (currentSearchQuery.isBlank()) {
            allDesignations
        } else {
            allDesignations.filter { d ->
                d.name.contains(currentSearchQuery, ignoreCase = true) ||
                        d.code.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        val pagedItems = filtered.take(currentPage * pageSize)
        _designationsPagingState.value = PagingUiState(
            items = pagedItems,
            isInitialLoading = false,
            isLoadingMore = false,
            canLoadMore = pagedItems.size < filtered.size
        )
    }

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
            _designationsPagingState.value = PagingUiState(isInitialLoading = true)
            repository.getDesignations()
                .onSuccess { items ->
                    _uiState.value = DesignationUiState.Success(items)
                    rebuildPagingState(resetPage = true)
                }
                .onFailure { e ->
                    _uiState.value = DesignationUiState.Error(e.message ?: "Error")
                    _designationsPagingState.value = PagingUiState(isInitialLoading = false)
                }
        }
    }

    // ── Create Designation ──
    fun createDesignation(name: String, code: String, description: String?) {
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