package com.cuso.tailor.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.sales.CustomerMeasurementItem
import com.cuso.tailor.model.sales.CustomerMeasurementRecord
import com.cuso.tailor.model.sales.MeasurementItem
import com.cuso.tailor.repository.SalesRepository
import com.cuso.tailor.view.home.sales.measurements.AvailableGarmentPreset
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

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private val loadedItems = mutableListOf<MeasurementItem>()

    // Dynamic customer measurements for Available Measurements Dialog
    private val _availableGarments = MutableStateFlow<List<AvailableGarmentPreset>>(emptyList())
    val availableGarments: StateFlow<List<AvailableGarmentPreset>> = _availableGarments.asStateFlow()

    private val _isLoadingAvailableGarments = MutableStateFlow(false)
    val isLoadingAvailableGarments: StateFlow<Boolean> = _isLoadingAvailableGarments.asStateFlow()

    init {
        loadMeasurements()
    }

    fun loadMeasurements() {
        viewModelScope.launch {
            _uiState.update { MeasurementsUiState.Loading }
            currentPage = 1
            loadedItems.clear()

            val result = repository.getMeasurements(page = 1, limit = 10)

            result.fold(
                onSuccess = { response ->
                    val newItems = response.data.map { it.toUiItem() }
                    loadedItems.addAll(newItems)

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

    fun loadMoreMeasurements(page: Int? = null) {
        if (_isLoadingMore.value || !_canLoadMore.value) return

        viewModelScope.launch {
            _isLoadingMore.update { true }
            val nextPage = page ?: (currentPage + 1)

            val result = repository.getMeasurements(page = nextPage, limit = 10)

            result.fold(
                onSuccess = { response ->
                    val newItems = response.data.map { it.toUiItem() }

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

    /**
     * Fetches dynamic measurements for a specific customer with comprehensive debug logging.
     */
    fun fetchAvailableGarments(customerId: String? = null) {
        Log.d("MEASUREMENT_DEBUG", "STEP 4: MeasurementsViewModel.fetchAvailableGarments called with customerId: '$customerId'")

        if (customerId.isNullOrBlank()) {
            Log.e("MEASUREMENT_DEBUG", "❌ STEP 4 FAILED: customerId is null or blank! Aborting API call.")
            return
        }

        viewModelScope.launch {
            _isLoadingAvailableGarments.update { true }
            Log.d("MEASUREMENT_DEBUG", "STEP 5: Triggering repository.fetchCustomerMeasurements('$customerId')...")

            val result = repository.fetchCustomerMeasurements(customerId = customerId)

            result.fold(
                onSuccess = { response ->
                    Log.d("MEASUREMENT_DEBUG", "✅ STEP 6: API Success! Received ${response.data.size} items. Count = ${response.count}")
                    val presets = response.data.mapIndexed { index, record ->
                        record.toPreset(isSelected = index == 0)
                    }
                    _availableGarments.update { presets }
                    _isLoadingAvailableGarments.update { false }
                },
                onFailure = { error ->
                    Log.e("MEASUREMENT_DEBUG", "❌ STEP 6 FAILED: API call failed -> ${error.message}", error)
                    _isLoadingAvailableGarments.update { false }
                }
            )
        }
    }

    private fun CustomerMeasurementRecord.toPreset(isSelected: Boolean): AvailableGarmentPreset {
        val displayName = garment?.displayName?.takeIf { it.isNotBlank() }
            ?: garment?.name?.takeIf { it.isNotBlank() }
            ?: garmentCategory?.displayName?.takeIf { it.isNotBlank() }
            ?: garmentCategory?.name?.takeIf { it.isNotBlank() }
            ?: "Garment"

        return AvailableGarmentPreset(
            id = id.ifBlank { garment?.id.orEmpty() },
            name = displayName,
            imageUrl = garment?.imageUrl ?: garmentCategory?.image,
            isSelected = isSelected
        )
    }

    private fun CustomerMeasurementItem.toUiItem(): MeasurementItem {
        val garment = latestMeasurement?.garment?.displayName
            ?: latestMeasurement?.garment?.name
            ?: "—"
        val category = latestMeasurement?.garmentCategory?.displayName
            ?: latestMeasurement?.garmentCategory?.name
            ?: "—"

        return MeasurementItem(
            id = latestMeasurement?.id.orEmpty().ifBlank { customer?.id.orEmpty() },
            customerId = customer?.id.orEmpty(),
            customerName = customer?.fullName?.takeIf { it.isNotBlank() } ?: "Unknown Customer",
            contact = customer?.mobileNumber?.takeIf { it.isNotBlank() } ?: "—",
            customerCode = customer?.customerCode?.takeIf { it.isNotBlank() } ?: "—",
            garmentName = garment,
            categoryName = category,
            measuredDate = formatIsoDate(latestMeasurement?.measuredAt ?: latestMeasurement?.createdAt),
            status = latestMeasurement?.status?.takeIf { it.isNotBlank() } ?: "Active",
            profileImageUrl = customer?.profilePicture?.url
        )
    }

    private fun formatIsoDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "—"
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
            )
            var parsedDate: java.util.Date? = null
            for (pattern in inputFormats) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    parsedDate = sdf.parse(isoDate)
                    if (parsedDate != null) break
                } catch (_: Exception) {}
            }
            parsedDate?.let {
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                outputFormat.format(it)
            } ?: isoDate
        } catch (_: Exception) {
            isoDate
        }
    }
}