package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.OrderViewData
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderViewUiState {
    object Idle : OrderViewUiState()
    object Loading : OrderViewUiState()
    data class Success(val data: OrderViewData) : OrderViewUiState()
    data class Error(val message: String) : OrderViewUiState()
}
@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class OrderViewViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _orderViewState = MutableStateFlow<OrderViewUiState>(OrderViewUiState.Idle)
    val orderViewState: StateFlow<OrderViewUiState> = _orderViewState.asStateFlow()

    fun getOrdersView(orderId: String) {
        viewModelScope.launch {
            _orderViewState.value = OrderViewUiState.Loading
            repository.getOrdersView(orderId)
                .onSuccess { data ->
                    _orderViewState.value = OrderViewUiState.Success(data)
                }
                .onFailure { error ->
                    _orderViewState.value = OrderViewUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun resetState() {
        _orderViewState.value = OrderViewUiState.Idle
    }
}