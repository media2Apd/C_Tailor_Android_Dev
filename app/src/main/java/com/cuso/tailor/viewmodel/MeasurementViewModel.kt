package com.cuso.tailor.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.sales.CustomerMeasurementItem
import com.cuso.tailor.model.sales.CustomerMeasurementRecord
import com.cuso.tailor.model.sales.MeasurementDetailData
import com.cuso.tailor.model.sales.MeasurementDetailResponse
import com.cuso.tailor.model.sales.MeasurementItem
import com.cuso.tailor.repository.ApiRepository
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

sealed interface MeasurementDeleteState {
    data object Idle : MeasurementDeleteState
    data object Loading : MeasurementDeleteState
    data class Success(val message: String?) : MeasurementDeleteState
    data class Error(val message: String) : MeasurementDeleteState
}

sealed interface MeasurementDetailState {
    data object Idle : MeasurementDetailState
    data object Loading : MeasurementDetailState
    data class Success(val detail: MeasurementDetailData) : MeasurementDetailState
    data class Error(val message: String) : MeasurementDetailState
}

private const val MEASUREMENT_VIEW_ONE_URL = "/api/sales/customers/measurements/view-one"

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    private val repository: SalesRepository,
    private val apiRepository: ApiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MeasurementsUiState>(MeasurementsUiState.Loading)
    val uiState: StateFlow<MeasurementsUiState> = _uiState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    // Delete operation state
    private val _deleteState = MutableStateFlow<MeasurementDeleteState>(MeasurementDeleteState.Idle)
    val deleteState: StateFlow<MeasurementDeleteState> = _deleteState.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private val loadedItems = mutableListOf<MeasurementItem>()

    // Dynamic customer measurements for Available Measurements Dialog
    private val _availableGarments = MutableStateFlow<List<AvailableGarmentPreset>>(emptyList())
    val availableGarments: StateFlow<List<AvailableGarmentPreset>> = _availableGarments.asStateFlow()

    private val _isLoadingAvailableGarments = MutableStateFlow(false)
    val isLoadingAvailableGarments: StateFlow<Boolean> = _isLoadingAvailableGarments.asStateFlow()

    // Single Measurement Detail (view-one API)
    private val _measurementDetailState =
        MutableStateFlow<MeasurementDetailState>(MeasurementDetailState.Idle)
    val measurementDetailState: StateFlow<MeasurementDetailState> =
        _measurementDetailState.asStateFlow()

    fun fetchMeasurementDetail(measurementId: String) {
        if (measurementId.isBlank()) {
            _measurementDetailState.update {
                MeasurementDetailState.Error("Measurement ID is missing")
            }
            return
        }

        viewModelScope.launch {
            _measurementDetailState.update { MeasurementDetailState.Loading }

            val result = apiRepository.request<MeasurementDetailResponse> {
                get("$MEASUREMENT_VIEW_ONE_URL/$measurementId")
            }

            result.fold(
                onSuccess = { response ->
                    val data = response.data
                    if (response.success && data != null) {
                        _measurementDetailState.update {
                            MeasurementDetailState.Success(data)
                        }
                    } else {
                        _measurementDetailState.update {
                            MeasurementDetailState.Error("Failed to load measurement details")
                        }
                    }
                },
                onFailure = { error ->
                    _measurementDetailState.update {
                        MeasurementDetailState.Error(
                            error.message ?: "Failed to load measurement details"
                        )
                    }
                }
            )
        }
    }

    fun clearMeasurementDetail() {
        _measurementDetailState.update { MeasurementDetailState.Idle }
    }

    fun formatMeasuredDate(isoDate: String?): String = formatIsoDate(isoDate)

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
     * Resets the delete operation state to Idle.
     */
    fun resetDeleteState() {
        _deleteState.update { MeasurementDeleteState.Idle }
    }

    /**
     * Calls repository to delete measurement and updates the local list on success.
     */
    fun deleteMeasurement(id: String) {
        viewModelScope.launch {
            _deleteState.update { MeasurementDeleteState.Loading }
            val result = repository.deleteMeasurement(id)

            result.fold(
                onSuccess = { message ->
                    loadedItems.removeAll { it.id == id }
                    _uiState.update {
                        MeasurementsUiState.Success(
                            items = loadedItems.toList(),
                            total = loadedItems.size,
                            totalPages = totalPages
                        )
                    }
                    _deleteState.update { MeasurementDeleteState.Success(message) }
                },
                onFailure = { error ->
                    _deleteState.update {
                        MeasurementDeleteState.Error(error.message ?: "Failed to delete measurement")
                    }
                }
            )
        }
    }

    /**
     * Fetches dynamic measurements for a specific customer with comprehensive debug logging.
     */
    fun fetchAvailableGarments(customerId: String? = null) {

        if (customerId.isNullOrBlank()) {
            return
        }

        viewModelScope.launch {
            _isLoadingAvailableGarments.update { true }

            val result = repository.fetchCustomerMeasurements(customerId = customerId)

            result.fold(
                onSuccess = { response ->
                    val presets = response.data.mapIndexed { index, record ->
                        record.toPreset(isSelected = index == 0)
                    }
                    _availableGarments.update { presets }
                    _isLoadingAvailableGarments.update { false }
                },
                onFailure = { _ ->
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
            ?: "-"

        return AvailableGarmentPreset(
            id = id.ifBlank { garment?.id.orEmpty() },
            name = displayName,
            imageUrl = garment?.imageUrl ?: garmentCategory?.image,
            isSelected = isSelected
        )
    }

    private fun CustomerMeasurementItem.toUiItem(): MeasurementItem {
        val garment = latestMeasurement?.garment?.displayName?.takeIf { it.isNotBlank() }
            ?: latestMeasurement?.garment?.name?.takeIf { it.isNotBlank() }
            ?: "-"
        val category = latestMeasurement?.garmentCategory?.displayName?.takeIf { it.isNotBlank() }
            ?: latestMeasurement?.garmentCategory?.name?.takeIf { it.isNotBlank() }
            ?: "-"

        return MeasurementItem(
            id = latestMeasurement?.id.orEmpty().ifBlank { customer?.id.orEmpty() },
            customerId = customer?.id.orEmpty(),
            customerName = customer?.fullName?.takeIf { it.isNotBlank() } ?: "-",
            contact = customer?.mobileNumber?.takeIf { it.isNotBlank() } ?: "-",
            customerCode = customer?.customerCode?.takeIf { it.isNotBlank() } ?: "-",
            garmentName = garment,
            categoryName = category,
            measuredDate = formatIsoDate(latestMeasurement?.measuredAt ?: latestMeasurement?.createdAt),
            status = latestMeasurement?.status?.takeIf { it.isNotBlank() } ?: "-",
            profileImageUrl = customer?.profilePicture?.url
        )
    }

    private fun formatIsoDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "-"
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