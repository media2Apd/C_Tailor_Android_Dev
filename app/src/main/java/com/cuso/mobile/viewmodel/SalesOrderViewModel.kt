package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.CreateOrderRequest
import com.cuso.mobile.model.OrderItem
import com.cuso.mobile.model.toOrderItem
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────

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

    private var fetchJob: Job? = null

    // ─────────────────────────────────────────────────────────
    // Fetch Orders (paginated, filtered)
    // ─────────────────────────────────────────────────────────

    fun fetchOrders(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null,
    ) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _orderState.value = OrderUiState.Loading

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
                    val totalPages = if (response.totalPages > 0) {
                        response.totalPages
                    } else {
                        maxOf(1, (response.total + limit - 1) / limit)
                    }
                    _orderState.value = OrderUiState.Success(
                        orders = orders,
                        total = response.total,
                        totalPages = totalPages,
                        currentPage = page,
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

    // ─────────────────────────────────────────────────────────
    // Fetch Single Order by ID
    // ─────────────────────────────────────────────────────────

    fun fetchOrderById(orderId: String) {
        viewModelScope.launch {
            _actionState.value = OrderActionState.Loading

            val result = repository.getOrderById(orderId)

            if (result.isSuccess) {
                _selectedOrder.value = result.getOrNull()  // already OrderItem
                _actionState.value = OrderActionState.Idle
            } else {
                _actionState.value = OrderActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load order"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Update Order Status
    // ─────────────────────────────────────────────────────────

    fun updateOrderStatus(
        orderId: String,
        status: String,
        currentPage: Int = 1,
        limit: Int = 10,
        search: String? = null,
        statusFilter: String? = null,
        onSuccess: (() -> Unit)? = null,
    ) {
        viewModelScope.launch {
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

    // ─────────────────────────────────────────────────────────
    // Create order
    // ─────────────────────────────────────────────────────────

    // NEW
    fun createOrder(
        request: CreateOrderRequest,
        imageParts: List<okhttp3.MultipartBody.Part> = emptyList(),
        voiceNotePart: okhttp3.MultipartBody.Part? = null,
        onSuccess: (OrderItem) -> Unit
    ) {
        viewModelScope.launch {
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

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }

    fun resetActionState() {
        _actionState.value = OrderActionState.Idle
    }
}