package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.CreateOrderRequest
import com.cuso.mobile.model.sales.OrderItem
import com.cuso.mobile.model.sales.toOrderItem
import com.cuso.mobile.repository.SalesRepository
import com.cuso.mobile.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// -------------------------------------------------------------
// UI State
// -------------------------------------------------------------

sealed class OrderUiState {
    data object Loading : OrderUiState()
    data class Success(
        val orders: List<OrderItem>,
        val total: Int,
        val totalPages: Int,
        val currentPage: Int,
    ) : OrderUiState()
    data class Error(val message: String) : OrderUiState()
}

sealed class OrderActionState {
    data object Idle : OrderActionState()
    data object Loading : OrderActionState()
    data class Success(val message: String) : OrderActionState()
    data class Error(val message: String) : OrderActionState()
}

// -------------------------------------------------------------
// ViewModel
// -------------------------------------------------------------
@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class SalesOrderViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<OrderUiState>(OrderUiState.Loading)
    val orderState: StateFlow<OrderUiState> = _orderState.asStateFlow()

    private val _actionState = MutableStateFlow<OrderActionState>(OrderActionState.Idle)
    val actionState: StateFlow<OrderActionState> = _actionState.asStateFlow()

    private val _selectedOrder = MutableStateFlow<OrderItem?>(null)
    val selectedOrder: StateFlow<OrderItem?> = _selectedOrder.asStateFlow()

    // Pagination states
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private var currentSearch: String? = null
    private var currentStatus: String? = null
    private val loadedOrders = mutableListOf<OrderItem>()

    private var fetchJob: Job? = null

    // ---------------------------------------------------------
    // Fetch Orders (Initial load / Refresh)
    // ---------------------------------------------------------

    fun fetchOrders(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _orderState.value = OrderUiState.Loading
            currentPage = page
            currentSearch = search
            currentStatus = status
            loadedOrders.clear()

            val result = repository.getOrders(
                page = page,
                limit = limit,
                search = search?.takeIf { it.isNotBlank() },
                status = status?.takeIf { it.isNotBlank() }
            )

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                try {
                    val orders = response.data.map { it.toOrderItem() }
                    loadedOrders.addAll(orders)

                    totalPages = if (response.totalPages > 0) {
                        response.totalPages
                    } else {
                        maxOf(1, (response.total + limit - 1) / limit)
                    }

                    _canLoadMore.value = currentPage < totalPages

                    _orderState.value = OrderUiState.Success(
                        orders = loadedOrders.toList(),
                        total = response.total,
                        totalPages = totalPages,
                        currentPage = currentPage
                    )
                } catch (e: Exception) {
                    _orderState.value = OrderUiState.Error("Error parsing data: ${e.message}")
                }
            } else {
                _orderState.value = OrderUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load orders"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Fetch Next Page (Infinite Scroll)
    // ---------------------------------------------------------

    fun loadMoreOrders(limit: Int = 10) {
        if (_isLoadingMore.value || !_canLoadMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1

            val result = repository.getOrders(
                page = nextPage,
                limit = limit,
                search = currentSearch?.takeIf { it.isNotBlank() },
                status = currentStatus?.takeIf { it.isNotBlank() }
            )

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                val newOrders = response.data.map { it.toOrderItem() }

                if (newOrders.isNotEmpty()) {
                    currentPage = nextPage
                    loadedOrders.addAll(newOrders)

                    totalPages = if (response.totalPages > 0) {
                        response.totalPages
                    } else {
                        maxOf(1, (response.total + limit - 1) / limit)
                    }

                    _canLoadMore.value = currentPage < totalPages

                    _orderState.value = OrderUiState.Success(
                        orders = loadedOrders.toList(),
                        total = response.total,
                        totalPages = totalPages,
                        currentPage = currentPage
                    )
                } else {
                    _canLoadMore.value = false
                }
            } else {
                _canLoadMore.value = false
            }
            _isLoadingMore.value = false
        }
    }

    // ---------------------------------------------------------
    // Fetch Single Order by ID
    // ---------------------------------------------------------

    fun fetchOrderById(orderId: String) {
        viewModelScope.launch {
            _actionState.value = OrderActionState.Loading

            val result = repository.getOrderById(orderId)

            if (result.isSuccess) {
                _selectedOrder.value = result.getOrNull()
                _actionState.value = OrderActionState.Idle
            } else {
                _actionState.value = OrderActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load order"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Update Order Status
    // ---------------------------------------------------------

    fun updateOrderStatus(
        orderId: String,
        status: String,
        currentPage: Int = 1,
        limit: Int = 10,
        search: String? = null,
        statusFilter: String? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        launchBusy {
            _actionState.value = OrderActionState.Loading

            val result = repository.updateOrderStatus(orderId, status)

            if (result.isSuccess) {
                _actionState.value = OrderActionState.Success("Status updated successfully")
                onSuccess?.invoke()
                fetchOrders(page = currentPage, limit = limit, search = search, status = statusFilter)
            } else {
                _actionState.value = OrderActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to update status"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Create Order
    // ---------------------------------------------------------

    fun createOrder(
        request: CreateOrderRequest,
        imageParts: List<okhttp3.MultipartBody.Part> = emptyList(),
        voiceNotePart: okhttp3.MultipartBody.Part? = null,
        onSuccess: (OrderItem) -> Unit
    ) {
        launchBusy {
            _actionState.value = OrderActionState.Loading
            val result = repository.createOrder(request, imageParts, voiceNotePart)
            if (result.isSuccess) {
                _actionState.value = OrderActionState.Success("Order created successfully")
                result.getOrNull()?.let { onSuccess(it) }
            } else {
                _actionState.value = OrderActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to create order"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Update Order
    // ---------------------------------------------------------

    fun updateOrder(
        orderId: String,
        request: CreateOrderRequest,
        existingImages: List<String> = emptyList(),
        imageParts: List<okhttp3.MultipartBody.Part> = emptyList(),
        voiceNotePart: okhttp3.MultipartBody.Part? = null,
        onSuccess: (OrderItem) -> Unit
    ) {
        launchBusy {
            _actionState.value = OrderActionState.Loading
            val result = repository.updateOrder(orderId, request, existingImages, imageParts, voiceNotePart)
            if (result.isSuccess) {
                _actionState.value = OrderActionState.Success("Order updated successfully")
                result.getOrNull()?.let { onSuccess(it) }
            } else {
                _actionState.value = OrderActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to update order"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }

    fun resetActionState() {
        _actionState.value = OrderActionState.Idle
    }
}