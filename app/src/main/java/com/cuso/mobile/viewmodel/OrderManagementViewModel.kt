package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.OrderManagementItem
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderManagementUiState {
    object Idle : OrderManagementUiState()
    object Loading : OrderManagementUiState()
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
    private val repository: SalesRepository   // ✅ class directly, same as OrderOverviewViewModel
) : ViewModel() {

    private val _orderState = MutableStateFlow<OrderManagementUiState>(OrderManagementUiState.Idle)
    val orderState: StateFlow<OrderManagementUiState> = _orderState.asStateFlow()

    fun fetchOrderManagement(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _orderState.value = OrderManagementUiState.Loading
            repository.getOrderManagement(page, limit, search, status)
                .onSuccess { response ->
                    val totalPages = if (limit > 0)
                        ((response.total + limit - 1) / limit).coerceAtLeast(1)
                    else 1
                    _orderState.value = OrderManagementUiState.Success(
                        orders = response.data,
                        total = response.total,
                        totalPages = totalPages
                    )
                }
                .onFailure { e ->
                    _orderState.value = OrderManagementUiState.Error(e.message ?: "Failed to load orders")
                }
        }
    }

    fun resetState() {
        _orderState.value = OrderManagementUiState.Idle
    }
}