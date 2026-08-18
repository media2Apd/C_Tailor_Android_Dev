    package com.cuso.mobile.viewmodel

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.cuso.mobile.model.sales.BulkRuleDto
    import com.cuso.mobile.model.sales.GarmentPricingDetailDto
    import com.cuso.mobile.model.sales.GarmentPricingListItemDto
    import com.cuso.mobile.model.sales.PriceAdjustmentDto
    import com.cuso.mobile.model.sales.PricingCategoryItem
    import com.cuso.mobile.model.sales.PricingQuotationData
    import com.cuso.mobile.model.sales.PricingQuotationSaveRequest
    import com.cuso.mobile.model.sales.PricingQuotationSaveResponse
    import com.cuso.mobile.model.sales.PricingStatValue
    import com.cuso.mobile.model.sales.PricingStats
    import com.cuso.mobile.repository.SalesRepository
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch
    import javax.inject.Inject
    import kotlin.time.Duration.Companion.milliseconds

    // ── UI State for the listing screen ──
    sealed interface PricingQuotationUiState {
        data object Loading : PricingQuotationUiState
        data class Success(val data: PricingQuotationData) : PricingQuotationUiState
        data class Error(val message: String) : PricingQuotationUiState
    }
    sealed interface GarmentPricingListUiState {
        data object Loading : GarmentPricingListUiState
        data class Success(val items: List<GarmentPricingListItemDto>) : GarmentPricingListUiState
        data class Error(val message: String) : GarmentPricingListUiState
    }

    // ── UI State: single record detail (for edit prefill) ──
    sealed interface GarmentPricingDetailUiState {
        data object Idle : GarmentPricingDetailUiState
        data object Loading : GarmentPricingDetailUiState
        data class Success(val detail: GarmentPricingDetailDto) : GarmentPricingDetailUiState
        data class Error(val message: String) : GarmentPricingDetailUiState
    }

    // ── UI State for the save operation ──
    data class SaveUiState(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val errorMessage: String? = null,
        val response: PricingQuotationSaveResponse? = null
    )
    @Suppress("UNUSED_PARAMETER")
    @HiltViewModel
    class PricingQuotationViewModel @Inject constructor(
        private val repository: SalesRepository
    ) : ViewModel() {

        // ── State for listing screen ──
        private val _uiState = MutableStateFlow<PricingQuotationUiState>(PricingQuotationUiState.Loading)
        val uiState: StateFlow<PricingQuotationUiState> = _uiState

        // ── State for save operation ──
        private val _saveState = MutableStateFlow(SaveUiState())
        val saveState: StateFlow<SaveUiState> = _saveState

        init {
            loadPricingQuotation()
        }

        private val _garmentPricingListState =
            MutableStateFlow<GarmentPricingListUiState>(GarmentPricingListUiState.Loading)
        val garmentPricingListState: StateFlow<GarmentPricingListUiState> = _garmentPricingListState

        private val _garmentPricingDetailState =
            MutableStateFlow<GarmentPricingDetailUiState>(GarmentPricingDetailUiState.Idle)
        val garmentPricingDetailState: StateFlow<GarmentPricingDetailUiState> = _garmentPricingDetailState


        // ── Load Pricing Quotation Data ──
        fun loadPricingQuotation() {
            viewModelScope.launch {
                _uiState.value = PricingQuotationUiState.Loading
                try {
                    delay(300.milliseconds) // simulate network
                    _uiState.value = PricingQuotationUiState.Success(loadDummyData())
                } catch (e: Exception) {
                    _uiState.value = PricingQuotationUiState.Error(e.message ?: "Something went wrong")
                }
            }
        }

        // ── Save Pricing Quotation ──
        fun savePricingQuotation(
            garmentCategoryId: String,
            basePrice: Double,
            fabricAdjustments: List<PriceAdjustmentDto>,
            designAdjustments: List<PriceAdjustmentDto>,
            additionalCharges: List<PriceAdjustmentDto>,
            expressCharge: Double,
            bulkRules: List<BulkRuleDto>
        ) {
            viewModelScope.launch {
                // Reset and show loading
                _saveState.value = SaveUiState(isLoading = true)

                try {
                    val request = PricingQuotationSaveRequest(
                        garmentCategory = garmentCategoryId,
                        basePrice = basePrice,
                        fabricAdjustments = fabricAdjustments,
                        designAdjustments = designAdjustments,
                        additionalCharges = additionalCharges,
                        expressCharge = expressCharge,
                        bulkRules = bulkRules
                    )

                    val result = repository.savePricingQuotation(request)

                    result.fold(
                        onSuccess = { response ->
                            _saveState.value = SaveUiState(
                                isLoading = false,
                                isSuccess = true,
                                response = response
                            )
                        },
                        onFailure = { error ->
                            _saveState.value = SaveUiState(
                                isLoading = false,
                                isSuccess = false,
                                errorMessage = error.message ?: "Failed to save pricing"
                            )
                        }
                    )
                } catch (e: Exception) {
                    _saveState.value = SaveUiState(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }

        // ── Reset Save State ──
        fun resetSaveState() {
            _saveState.value = SaveUiState()
        }

        // ── Reset UI State ──
        fun resetUiState() {
            _uiState.value = PricingQuotationUiState.Loading
        }

        // ── Dummy Data for UI Testing ──
        private fun loadDummyData(): PricingQuotationData {
            return PricingQuotationData(
                stats = PricingStats(
                    activeQuotations = PricingStatValue(value = "24", changePercent = 12.0),
                    avgQuoteValue = PricingStatValue(value = "₹2,450", changePercent = 8.0),
                    approvalRate = PricingStatValue(value = "78%", changePercent = 5.0),
                    thisMonth = PricingStatValue(value = "₹58.8k", changePercent = 15.0)
                ),
                categories = listOf(
                    PricingCategoryItem(
                        id = "1",
                        title = "Garment wise pricing",
                        subtitle = "Pricing based on garment types",
                        basePriceMin = 500.0,
                        basePriceMax = 5000.0,
                        categoryType = "garment"
                    ),
                    PricingCategoryItem(
                        id = "2",
                        title = "Fabric wise pricing",
                        subtitle = "Custom design charges",
                        basePriceMin = 500.0,
                        basePriceMax = 5000.0,
                        categoryType = "fabric"
                    ),
                    PricingCategoryItem(
                        id = "3",
                        title = "Design/Style pricing",
                        subtitle = "Standard stitching rates",
                        basePriceMin = 500.0,
                        basePriceMax = 5000.0,
                        categoryType = "design"
                    ),
                    PricingCategoryItem(
                        id = "4",
                        title = "Stitching charges",
                        subtitle = "Volume discounts",
                        basePriceMin = 500.0,
                        basePriceMax = 5000.0,
                        categoryType = "stitching"
                    ),
                    PricingCategoryItem(
                        id = "5",
                        title = "Bulk pricing",
                        subtitle = "Luxury customization",
                        basePriceMin = 500.0,
                        basePriceMax = 5000.0,
                        categoryType = "bulk"
                    )
                )
            )
        }

        fun updatePricingQuotation(
            id: String,
            garmentCategoryId: String,
            basePrice: Double,
            fabricAdjustments: List<PriceAdjustmentDto>,
            designAdjustments: List<PriceAdjustmentDto>,
            additionalCharges: List<PriceAdjustmentDto>,
            expressCharge: Double,
            bulkRules: List<BulkRuleDto>
        ) {
            viewModelScope.launch {
                _saveState.value = SaveUiState(isLoading = true)

                try {
                    val request = PricingQuotationSaveRequest(
                        garmentCategory = garmentCategoryId,
                        basePrice = basePrice,
                        fabricAdjustments = fabricAdjustments,
                        designAdjustments = designAdjustments,
                        additionalCharges = additionalCharges,
                        expressCharge = expressCharge,
                        bulkRules = bulkRules
                    )

                    val result = repository.updatePricingQuotation(id, request)

                    result.fold(
                        onSuccess = { response ->
                            _saveState.value = SaveUiState(
                                isLoading = false,
                                isSuccess = true,
                                response = response
                            )
                        },
                        onFailure = { error ->
                            _saveState.value = SaveUiState(
                                isLoading = false,
                                isSuccess = false,
                                errorMessage = error.message ?: "Failed to update pricing"
                            )
                        }
                    )
                } catch (e: Exception) {
                    _saveState.value = SaveUiState(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }

        fun fetchGarmentPricingList() {
            viewModelScope.launch {
                _garmentPricingListState.value = GarmentPricingListUiState.Loading
                val result = repository.getGarmentPricingList()   //   no token param
                result.fold(
                    onSuccess = { items -> _garmentPricingListState.value = GarmentPricingListUiState.Success(items) },
                    onFailure = { e -> _garmentPricingListState.value = GarmentPricingListUiState.Error(e.message ?: "Failed to load pricing list") }
                )
            }
        }

        fun fetchGarmentPricingDetail(id: String) {
            viewModelScope.launch {
                _garmentPricingDetailState.value = GarmentPricingDetailUiState.Loading
                val result = repository.getGarmentPricingDetail(id)   //   no token param
                result.fold(
                    onSuccess = { detail -> _garmentPricingDetailState.value = GarmentPricingDetailUiState.Success(detail) },
                    onFailure = { e -> _garmentPricingDetailState.value = GarmentPricingDetailUiState.Error(e.message ?: "Failed to load pricing detail") }
                )
            }
        }

        fun resetGarmentPricingDetailState() {
            _garmentPricingDetailState.value = GarmentPricingDetailUiState.Idle
        }
    }