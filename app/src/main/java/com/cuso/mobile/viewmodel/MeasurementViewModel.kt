package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.CustomerLastOrder
import com.cuso.mobile.model.sales.MeasurementItem
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

sealed class MeasurementsUiState {
    data object Loading : MeasurementsUiState()
    data class Success(
        val items: List<MeasurementItem>,
        val total: Int,
        val totalPages: Int
    ) : MeasurementsUiState()
    data class Error(val message: String) : MeasurementsUiState()
}

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MeasurementsUiState>(MeasurementsUiState.Loading)
    val uiState: StateFlow<MeasurementsUiState> = _uiState.asStateFlow()

    // Pagination loading and availability states
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private val loadedItems = mutableListOf<MeasurementItem>()

    init {
        loadMeasurements()
    }

    /**
     * Initial data load or refresh (Page 1)
     */
    fun loadMeasurements() {
        viewModelScope.launch {
            _uiState.update { MeasurementsUiState.Loading }
            currentPage = 1
            loadedItems.clear()

            // Pass page parameter if repository supports pagination; otherwise calls default
            val result = repository.getMeasurements()

            result.fold(
                onSuccess = { response ->
                    val newItems = response.customersLastOrders.map { order ->
                        order.toMeasurementItem()
                    }
                    loadedItems.addAll(newItems)

                    // Update total pages based on response if available, or determine by item count
                    totalPages = 1
                    _canLoadMore.update { currentPage < totalPages }

                    _uiState.update {
                        MeasurementsUiState.Success(
                            items = loadedItems.toList(),
                            total = loadedItems.size,
                            totalPages = totalPages
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        MeasurementsUiState.Error(error.message ?: "Failed to load measurements")
                    }
                }
            )
        }
    }

    /**
     * Fetches the next page of measurements and appends them to the current list
     */
    fun loadMoreMeasurements() {
        if (_isLoadingMore.value || !_canLoadMore.value) return

        viewModelScope.launch {
            _isLoadingMore.update { true }
            val nextPage = currentPage + 1

            val result = repository.getMeasurements()

            result.fold(
                onSuccess = { response ->
                    val newItems = response.customersLastOrders.map { order ->
                        order.toMeasurementItem()
                    }

                    if (newItems.isNotEmpty()) {
                        currentPage = nextPage
                        loadedItems.addAll(newItems)
                        _canLoadMore.update { currentPage < totalPages }

                        _uiState.update {
                            MeasurementsUiState.Success(
                                items = loadedItems.toList(),
                                total = loadedItems.size,
                                totalPages = totalPages
                            )
                        }
                    } else {
                        _canLoadMore.update { false }
                    }
                    _isLoadingMore.update { false }
                },
                onFailure = {
                    _isLoadingMore.update { false }
                }
            )
        }
    }

    // Map raw API order to UI item
    private fun CustomerLastOrder.toMeasurementItem(): MeasurementItem {
        return MeasurementItem(
            id = id,
            customerName = customerName,
            contact = contact,
            type = if (type.equals("corporate", ignoreCase = true)) "Corporate" else "Individual",
            garments = garments.joinToString(" / "),
            pending = "₹$pendingPayment",
            totalSpend = "₹$totalSpend",
            lastUpdated = formatDate(lastUpdated)
        )
    }

    private fun formatDate(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoDate)
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: isoDate
        } catch (_: Exception) {
            isoDate
        }
    }
}