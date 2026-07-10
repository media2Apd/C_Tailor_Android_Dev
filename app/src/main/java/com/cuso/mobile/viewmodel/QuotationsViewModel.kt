package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.QuotationItemDto
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuotationUiState {
    object Loading : QuotationUiState()
    data class Success(val quotations: List<QuotationItemDto>) : QuotationUiState()
    data class Error(val message: String) : QuotationUiState()
}

@HiltViewModel
class QuotationViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuotationUiState>(QuotationUiState.Loading)
    val uiState: StateFlow<QuotationUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var currentSearch: String? = null

    fun loadQuotations(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        currentPage = page
        currentSearch = search
        viewModelScope.launch {
            _uiState.value = QuotationUiState.Loading
            salesRepository.getQuotations(page, limit, search, status)
                .onSuccess { response ->
                    _uiState.value = QuotationUiState.Success(response.data)
                }
                .onFailure { e ->
                    _uiState.value = QuotationUiState.Error(e.message ?: "Failed to load quotations")
                }
        }
    }

    // ✅ Call this from the search bar's onValueChange (debounce if needed)
    fun searchQuotations(query: String) {
        loadQuotations(page = 1, search = query.ifBlank { null })
    }

    fun refresh() {
        loadQuotations(page = currentPage, search = currentSearch)
    }
}