package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.CustomerLastOrder
import com.cuso.mobile.model.MeasurementItem
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

    init {
        loadMeasurements()
    }

    fun loadMeasurements() {
        viewModelScope.launch {
            _uiState.update { MeasurementsUiState.Loading }

            val result = repository.getMeasurements()

            result.fold(
                onSuccess = { response ->
                    val items = response.customersLastOrders.map { order ->
                        order.toMeasurementItem()
                    }
                    _uiState.update {
                        MeasurementsUiState.Success(
                            items = items,
                            total = items.size,
                            totalPages = 1
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

    // ── Map raw API order → UI item ──
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