package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.OrderManagementItem
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderManagementUiState {
    data object Idle : OrderManagementUiState()
    data object Loading : OrderManagementUiState()
    data class Success(
        val orders: List<OrderManagementItem>,
        val total: Int,
        val totalPages: Int
    ) : OrderManagementUiState()
    data class Error(val message: String) : OrderManagementUiState()
}

@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class OrderManagementViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<OrderManagementUiState>(OrderManagementUiState.Idle)
    val orderState: StateFlow<OrderManagementUiState> = _orderState.asStateFlow()

    // Pagination states
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private var currentSearch: String? = null
    private var currentStatus: String? = null
    private val loadedOrders = mutableListOf<OrderManagementItem>()

    private var fetchJob: Job? = null

    // ---------------------------------------------------------
    // Fetch Order Management List (Initial load / Refresh)
    // ---------------------------------------------------------

    fun fetchOrderManagement(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _orderState.value = OrderManagementUiState.Loading
            currentPage = page
            currentSearch = search
            currentStatus = status
            loadedOrders.clear()

            val result = repository.getOrderManagement(
                page = page,
                limit = limit,
                search = search?.takeIf { it.isNotBlank() },
                status = status?.takeIf { it.isNotBlank() }
            )

            result.onSuccess { response ->
                loadedOrders.addAll(response.data)

                totalPages = if (limit > 0) {
                    ((response.total + limit - 1) / limit).coerceAtLeast(1)
                } else {
                    1
                }

                _canLoadMore.value = currentPage < totalPages

                _orderState.value = OrderManagementUiState.Success(
                    orders = loadedOrders.toList(),
                    total = response.total,
                    totalPages = totalPages
                )
            }.onFailure { error ->
                _orderState.value = OrderManagementUiState.Error(
                    error.message ?: "Failed to load orders"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Fetch Next Page (Infinite Scroll)
    // ---------------------------------------------------------

    fun loadMoreOrderManagement(limit: Int = 10) {
        if (_isLoadingMore.value || !_canLoadMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1

            val result = repository.getOrderManagement(
                page = nextPage,
                limit = limit,
                search = currentSearch?.takeIf { it.isNotBlank() },
                status = currentStatus?.takeIf { it.isNotBlank() }
            )

            result.onSuccess { response ->
                val newOrders = response.data

                if (newOrders.isNotEmpty()) {
                    currentPage = nextPage
                    loadedOrders.addAll(newOrders)

                    totalPages = if (limit > 0) {
                        ((response.total + limit - 1) / limit).coerceAtLeast(1)
                    } else {
                        1
                    }

                    _canLoadMore.value = currentPage < totalPages

                    _orderState.value = OrderManagementUiState.Success(
                        orders = loadedOrders.toList(),
                        total = response.total,
                        totalPages = totalPages
                    )
                } else {
                    _canLoadMore.value = false
                }
            }.onFailure {
                _canLoadMore.value = false
            }

            _isLoadingMore.value = false
        }
    }

    fun resetState() {
        _orderState.value = OrderManagementUiState.Idle
    }
}