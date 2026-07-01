package com.cuso.mobile.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.CustomerItem
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerUiState>(CustomerUiState.Loading)
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var currentLimit = 10
    private var currentSearch: String? = null
    private var currentType: String? = null

    init {
        loadCustomers()
    }

    fun loadCustomers(
        page: Int = currentPage,
        limit: Int = currentLimit,
        search: String? = currentSearch,
        type: String? = currentType
    ) {
        currentPage = page
        currentLimit = limit
        currentSearch = search
        currentType = type

        viewModelScope.launch {
            _uiState.update { CustomerUiState.Loading }

            val result = repository.getCustomers(
                page = page,
                limit = limit,
                search = search,
                type = type
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        CustomerUiState.Success(
                            customers = response.data,
                            total = response.total,
                            totalPages = response.totalPages
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        CustomerUiState.Error(error.message ?: "Failed to load customers")
                    }
                }
            )
        }
    }

    fun onSearch(query: String) {
        currentSearch = query.takeIf { it.isNotBlank() }
        currentPage = 1
        loadCustomers(page = 1, search = currentSearch)
    }

    fun onTypeFilterChange(type: String) {
        currentType = type.takeIf { it != "all" }
        currentPage = 1
        loadCustomers(page = 1, type = currentType)
    }

    fun onPageChange(page: Int) {
        loadCustomers(page = page)
    }

    fun onItemsPerPageChange(limit: Int) {
        currentLimit = limit
        currentPage = 1
        loadCustomers(page = 1, limit = limit)
    }

    fun refresh() {
        loadCustomers(page = 1)
    }
}

// Placeholder UI-state contracts — wire these to your real CustomerViewModel
sealed class CustomerUiState {
    data object Loading : CustomerUiState()
    data class Success(
        val customers: List<CustomerItem>,
        val total: Int,
        val totalPages: Int
    ) : CustomerUiState()
    data class Error(val message: String) : CustomerUiState()
}