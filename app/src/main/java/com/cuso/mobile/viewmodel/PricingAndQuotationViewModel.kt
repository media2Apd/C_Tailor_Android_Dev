package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.BulkRuleDto
import com.cuso.mobile.model.PriceAdjustmentDto
import com.cuso.mobile.model.PricingCategoryItem
import com.cuso.mobile.model.PricingQuotationData
import com.cuso.mobile.model.PricingQuotationSaveRequest
import com.cuso.mobile.model.PricingQuotationSaveResponse
import com.cuso.mobile.model.PricingStatValue
import com.cuso.mobile.model.PricingStats
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

// ── UI State for the save operation ──
data class SaveUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val response: PricingQuotationSaveResponse? = null
)

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

    // ── Load Pricing Quotation Data ──
    fun loadPricingQuotation() {
        viewModelScope.launch {
            _uiState.value = PricingQuotationUiState.Loading
            try {
                // TODO (API HOOKUP): replace loadDummyData() with the real call:
                //
                // val response = repository.getPricingQuotation()
                // if (response.isSuccess) {
                //     val data = response.getOrNull()
                //     if (data != null && data.success) {
                //         _uiState.value = PricingQuotationUiState.Success(data.data)
                //     } else {
                //         _uiState.value = PricingQuotationUiState.Error("Failed to load pricing data")
                //     }
                // } else {
                //     _uiState.value = PricingQuotationUiState.Error(
                //         response.exceptionOrNull()?.message ?: "Failed to load pricing data"
                //     )
                // }
                //
                // Until then, screen renders from dummy data below so UI stays unblocked.
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
}