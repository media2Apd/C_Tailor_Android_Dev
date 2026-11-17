package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.model.settings.CreateBranchRequest
import com.cuso.tailor.model.settings.UpdateBranchRequest
import com.cuso.tailor.repository.SalesRepository
import com.cuso.tailor.utils.PagingUiState
import com.cuso.tailor.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // ── Paginated UI State for PaginatedDataList ──
    private val _branchesPagingState = MutableStateFlow(PagingUiState<BranchItem>(isInitialLoading = true))
    val branchesPagingState: StateFlow<PagingUiState<BranchItem>> = _branchesPagingState.asStateFlow()

    private var currentSearchQuery: String = ""
    private var currentPage: Int = 1
    private val pageSize: Int = 10

    fun onBranchSearchChanged(query: String) {
        currentSearchQuery = query
        rebuildPagingState(resetPage = true)
    }

    fun loadMoreBranches() {
        val current = _branchesPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return
        currentPage += 1
        rebuildPagingState(resetPage = false)
    }

    private fun rebuildPagingState(resetPage: Boolean) {
        if (resetPage) currentPage = 1

        val allBranches = (_uiState.value as? BranchUiState.Success)?.branches ?: emptyList()
        val filtered = if (currentSearchQuery.isBlank()) {
            allBranches
        } else {
            allBranches.filter { b ->
                (b.name?.contains(currentSearchQuery, ignoreCase = true) == true) ||
                        (b.branchId?.contains(currentSearchQuery, ignoreCase = true) == true)
            }
        }

        val pagedItems = filtered.take(currentPage * pageSize)
        _branchesPagingState.value = PagingUiState(
            items = pagedItems,
            isInitialLoading = false,
            isLoadingMore = false,
            canLoadMore = pagedItems.size < filtered.size
        )
    }

    // ── Update State ──
    private val _updateState = MutableStateFlow<UpdateBranchUiState>(UpdateBranchUiState.Idle)
    val updateState: StateFlow<UpdateBranchUiState> = _updateState.asStateFlow()

    // ── Create State ──
    private val _createState = MutableStateFlow<CreateBranchUiState>(CreateBranchUiState.Idle)
    val createState: StateFlow<CreateBranchUiState> = _createState.asStateFlow()

    // ── Load Branches ──
    fun loadBranches() {
        if (_uiState.value is BranchUiState.Success) {
            rebuildPagingState(resetPage = false)
            return
        }
        launchBusy {
            _uiState.value = BranchUiState.Loading
            _branchesPagingState.value = PagingUiState(isInitialLoading = true)
            salesRepository.getBranches().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = BranchUiState.Success(response.data)
                        rebuildPagingState(resetPage = true)
                    } else {
                        _uiState.value = BranchUiState.Error("Failed to load branches")
                        _branchesPagingState.value = PagingUiState(isInitialLoading = false)
                    }
                },
                onFailure = { e ->
                    _uiState.value = BranchUiState.Error(e.message ?: "Something went wrong")
                    _branchesPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
        }
    }

    // ── Refresh Branches ──
    fun refresh() {
        launchBusy {
            _uiState.value = BranchUiState.Loading
            _branchesPagingState.value = PagingUiState(isInitialLoading = true)
            salesRepository.getBranches().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = BranchUiState.Success(response.data)
                        rebuildPagingState(resetPage = true)
                    } else {
                        _uiState.value = BranchUiState.Error("Failed to load branches")
                        _branchesPagingState.value = PagingUiState(isInitialLoading = false)
                    }
                },
                onFailure = { e ->
                    _uiState.value = BranchUiState.Error(e.message ?: "Something went wrong")
                    _branchesPagingState.value = PagingUiState(isInitialLoading = false)
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
                        rebuildPagingState(resetPage = false)
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
                            rebuildPagingState(resetPage = false)
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