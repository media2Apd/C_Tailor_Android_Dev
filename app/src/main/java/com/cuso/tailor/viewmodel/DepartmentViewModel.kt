// DepartmentViewModel.kt
package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import com.cuso.tailor.model.settings.DepartmentCreateRequest
import com.cuso.tailor.model.settings.DepartmentItem
import com.cuso.tailor.model.settings.DepartmentUpdateRequest
import com.cuso.tailor.repository.SalesRepository
import com.cuso.tailor.utils.PagingUiState
import com.cuso.tailor.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// ── List state (getDepartments) ──
sealed class DepartmentUiState {
    object Loading : DepartmentUiState()
    data class Success(val departments: List<DepartmentItem>) : DepartmentUiState()
    data class Error(val message: String) : DepartmentUiState()
}

// ── Create state (createDepartment) ──
sealed class DesignationCreateState {
    object Idle : DesignationCreateState()
    object Loading : DesignationCreateState()
    object Success : DesignationCreateState()
    data class Error(val message: String) : DesignationCreateState()
}

// ── Update state (updateDepartment) ──
sealed class DepartmentUpdateUiState {
    object Idle : DepartmentUpdateUiState()
    object Loading : DepartmentUpdateUiState()
    object Success : DepartmentUpdateUiState()
    data class Error(val message: String) : DepartmentUpdateUiState()
}

// ── Delete state (deleteDepartment) ──
sealed class DepartmentDeleteUiState {
    object Idle : DepartmentDeleteUiState()
    object Loading : DepartmentDeleteUiState()
    object Success : DepartmentDeleteUiState()
    data class Error(val message: String) : DepartmentDeleteUiState()
}
@Suppress("unused_parameter")

@HiltViewModel
class DepartmentViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    // ── UI State for listing departments ──
    private val _uiState = MutableStateFlow<DepartmentUiState>(DepartmentUiState.Loading)
    val uiState: StateFlow<DepartmentUiState> = _uiState.asStateFlow()

    // ── Paginated UI State for PaginatedDataList ──
    private val _departmentsPagingState =
        MutableStateFlow(PagingUiState<DepartmentItem>(isInitialLoading = true))
    val departmentsPagingState: StateFlow<PagingUiState<DepartmentItem>> =
        _departmentsPagingState.asStateFlow()

    private var currentSearchQuery: String = ""
    private var currentPage: Int = 1
    private val pageSize: Int = 10

    fun onDepartmentSearchChanged(query: String) {
        currentSearchQuery = query
        rebuildPagingState(resetPage = true)
    }

    fun loadMoreDepartments() {
        val current = _departmentsPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return
        currentPage += 1
        rebuildPagingState(resetPage = false)
    }

    private fun rebuildPagingState(resetPage: Boolean) {
        if (resetPage) currentPage = 1

        val allDepartments = (_uiState.value as? DepartmentUiState.Success)?.departments ?: emptyList()
        val filtered = if (currentSearchQuery.isBlank()) {
            allDepartments
        } else {
            allDepartments.filter { d ->
                d.name.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        val pagedItems = filtered.take(currentPage * pageSize)
        _departmentsPagingState.value = PagingUiState(
            items = pagedItems,
            isInitialLoading = false,
            isLoadingMore = false,
            canLoadMore = pagedItems.size < filtered.size
        )
    }

    // ── Create State ──
    private val _createState = MutableStateFlow<DesignationCreateState>(DesignationCreateState.Idle)
    val createState: StateFlow<DesignationCreateState> = _createState.asStateFlow()

    // ── Update State ──
    private val _updateState = MutableStateFlow<DepartmentUpdateUiState>(DepartmentUpdateUiState.Idle)
    val updateState: StateFlow<DepartmentUpdateUiState> = _updateState.asStateFlow()

    // ── Delete State ──
    private val _deleteState = MutableStateFlow<DepartmentDeleteUiState>(DepartmentDeleteUiState.Idle)
//    val deleteState: StateFlow<DepartmentDeleteUiState> = _deleteState.asStateFlow()

    // ── Load Departments ──
    fun loadDepartments() {
        if (_uiState.value is DepartmentUiState.Success) {
            rebuildPagingState(resetPage = false)
            return
        }
        launchBusy {
            _uiState.value = DepartmentUiState.Loading
            _departmentsPagingState.value = PagingUiState(isInitialLoading = true)
            salesRepository.getDepartments().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = DepartmentUiState.Success(response.data)
                        rebuildPagingState(resetPage = true)
                    } else {
                        _uiState.value = DepartmentUiState.Error(response.toString())
                        _departmentsPagingState.value = PagingUiState(isInitialLoading = false)
                    }
                },
                onFailure = { e ->
                    _uiState.value = DepartmentUiState.Error(e.message ?: "Something went wrong")
                    _departmentsPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
        }
    }

    // ── Refresh Departments ──
    fun refresh() {
        launchBusy {
            _uiState.value = DepartmentUiState.Loading
            _departmentsPagingState.value = PagingUiState(isInitialLoading = true)
            salesRepository.getDepartments().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = DepartmentUiState.Success(response.data)
                        rebuildPagingState(resetPage = true)
                    } else {
                        _uiState.value = DepartmentUiState.Error(response.toString())
                        _departmentsPagingState.value = PagingUiState(isInitialLoading = false)
                    }
                },
                onFailure = { e ->
                    _uiState.value = DepartmentUiState.Error(e.message ?: "Something went wrong")
                    _departmentsPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
        }
    }

    // ── Create Department ──
    fun createDepartment(
        name: String,
        description: String,
        departmentHead: String,
        status: String = "Active"
    ) {
        launchBusy {
            _createState.value = DesignationCreateState.Loading
            val request = DepartmentCreateRequest(
                name = name.trim(),
                description = description.trim(),
                departmentHeadId = departmentHead.ifBlank { null },
                status = status
            )
            salesRepository.createDepartment(request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _createState.value = DesignationCreateState.Success
                        // Add the new department to the list
                        val current = _uiState.value
                        if (current is DepartmentUiState.Success) {
                            val newList = current.departments + response.data
                            _uiState.value = DepartmentUiState.Success(newList)
                            rebuildPagingState(resetPage = false)
                        } else {
                            // If list wasn't loaded, reload it
                            refresh()
                        }
                    } else {
                        _createState.value = DesignationCreateState.Error(
                            response.message ?: "Failed to create department"
                        )
                    }
                },
                onFailure = { e ->
                    _createState.value = DesignationCreateState.Error(
                        e.message ?: "Something went wrong"
                    )
                }
            )
        }
    }

    fun resetCreateState() {
        _createState.value = DesignationCreateState.Idle
    }

    // ── Update Department ──
    fun updateDepartment(
        id: String,
        name: String,
        description: String?,
        departmentHead: String?,
        status: String?
    ) {
        launchBusy {
            _updateState.value = DepartmentUpdateUiState.Loading
            val request = DepartmentUpdateRequest(
                name = name.trim(),
                description = description?.trim(),
                departmentHeadId = departmentHead?.ifBlank { null },
                status = status
            )
            salesRepository.updateDepartment(id, request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _updateState.value = DepartmentUpdateUiState.Success
                        // Update the department in the list
                        val current = _uiState.value
                        if (current is DepartmentUiState.Success) {
                            val newList = current.departments.map {
                                if (it._id == response.data._id) response.data else it
                            }
                            _uiState.value = DepartmentUiState.Success(newList)
                            rebuildPagingState(resetPage = false)
                        }
                    } else {
                        _updateState.value = DepartmentUpdateUiState.Error(
                            response.message ?: "Failed to update department"
                        )
                    }
                },
                onFailure = { e ->
                    _updateState.value = DepartmentUpdateUiState.Error(
                        e.message ?: "Something went wrong"
                    )
                }
            )
        }
    }

    fun resetUpdateState() {
        _updateState.value = DepartmentUpdateUiState.Idle
    }



    fun resetDeleteState() {
        _deleteState.value = DepartmentDeleteUiState.Idle
    }
}