package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.GarmentPricingItem
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GarmentPricingUiState {
    object Loading : GarmentPricingUiState()
    data class Success(val items: List<GarmentPricingItem>) : GarmentPricingUiState()
    data class Error(val message: String) : GarmentPricingUiState()
}
@Suppress("unused_parameter")

@HiltViewModel
class GarmentPricingViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GarmentPricingUiState>(GarmentPricingUiState.Loading)
    val uiState: StateFlow<GarmentPricingUiState> = _uiState.asStateFlow()

    private var cachedItems: List<GarmentPricingItem> = emptyList()

    fun loadGarmentPricing() {
        viewModelScope.launch {
            _uiState.value = GarmentPricingUiState.Loading
            salesRepository.getGarmentPricing()
                .onSuccess { items ->
                    cachedItems = items
                    _uiState.value = GarmentPricingUiState.Success(items)
                }
                .onFailure { e ->
                    _uiState.value = GarmentPricingUiState.Error(e.message ?: "Failed to load garment pricing")
                }
        }
    }

    fun getGarmentById(garmentId: String): GarmentPricingItem? {
        return cachedItems.find { it.garmentId == garmentId }
    }

    fun getGarmentsByIds(garmentIds: List<String>): List<GarmentPricingItem> {
        return cachedItems.filter { it.garmentId in garmentIds }
    }
}