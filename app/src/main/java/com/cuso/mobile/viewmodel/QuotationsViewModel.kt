package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.CreateQuotationRequest
import com.cuso.mobile.model.sales.QuotationCreatedData
import com.cuso.mobile.model.sales.QuotationItemDto
import com.cuso.mobile.repository.SalesRepository
import com.cuso.mobile.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuotationUiState {
    data object Loading : QuotationUiState()
    data class Success(
        val quotations: List<QuotationItemDto>,
        val total: Int = 0,
        val totalPages: Int = 1
    ) : QuotationUiState()
    data class Error(val message: String) : QuotationUiState()
}

sealed class QuotationSaveUiState {
    data object Idle : QuotationSaveUiState()
    data object Loading : QuotationSaveUiState()
    data class Success(val quotation: QuotationCreatedData) : QuotationSaveUiState()
    data class Error(val message: String) : QuotationSaveUiState()
}

sealed class QuotationDeleteUiState {
    data object Idle : QuotationDeleteUiState()
    data object Loading : QuotationDeleteUiState()
    data object Success : QuotationDeleteUiState()
    data class Error(val message: String) : QuotationDeleteUiState()
}

sealed class QuotationDetailUiState {
    data object Idle : QuotationDetailUiState()
    data object Loading : QuotationDetailUiState()
    data class Success(val quotation: QuotationItemDto) : QuotationDetailUiState()
    data class Error(val message: String) : QuotationDetailUiState()
}

@HiltViewModel
class QuotationViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuotationUiState>(QuotationUiState.Loading)
    val uiState: StateFlow<QuotationUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<QuotationSaveUiState>(QuotationSaveUiState.Idle)
    val saveState: StateFlow<QuotationSaveUiState> = _saveState.asStateFlow()

    private val _deleteState = MutableStateFlow<QuotationDeleteUiState>(QuotationDeleteUiState.Idle)
    val deleteState: StateFlow<QuotationDeleteUiState> = _deleteState.asStateFlow()

    private val _detailState = MutableStateFlow<QuotationDetailUiState>(QuotationDetailUiState.Idle)
    val detailState: StateFlow<QuotationDetailUiState> = _detailState.asStateFlow()

    // Pagination states
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private var currentSearch: String? = null
    private var currentStatus: String? = null
    private val loadedQuotations = mutableListOf<QuotationItemDto>()

    private var fetchJob: Job? = null

    // ---------------------------------------------------------
    // Fetch Quotations (Initial load / Refresh)
    // ---------------------------------------------------------

    fun loadQuotations(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = QuotationUiState.Loading
            currentPage = page
            currentSearch = search
            currentStatus = status
            loadedQuotations.clear()

            salesRepository.getQuotations(page, limit, search, status)
                .onSuccess { response ->
                    loadedQuotations.addAll(response.data)

                    // Read totalPages and total from response.pagination
                    val totalCount = response.pagination?.total ?: loadedQuotations.size
                    totalPages = response.pagination?.pages
                        ?: if (limit > 0) ((totalCount + limit - 1) / limit).coerceAtLeast(1) else 1

                    _canLoadMore.value = currentPage < totalPages

                    _uiState.value = QuotationUiState.Success(
                        quotations = loadedQuotations.toList(),
                        total = totalCount,
                        totalPages = totalPages
                    )
                }
                .onFailure { e ->
                    _uiState.value = QuotationUiState.Error(e.message ?: "Failed to load quotations")
                }
        }
    }

    // ---------------------------------------------------------
    // Fetch Next Page (Infinite Scroll)
    // ---------------------------------------------------------

    fun loadMoreQuotations(limit: Int = 10) {
        if (_isLoadingMore.value || !_canLoadMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1

            salesRepository.getQuotations(nextPage, limit, currentSearch, currentStatus)
                .onSuccess { response ->
                    val newItems = response.data

                    if (newItems.isNotEmpty()) {
                        currentPage = nextPage
                        loadedQuotations.addAll(newItems)

                        val totalCount = response.pagination?.total ?: loadedQuotations.size
                        totalPages = response.pagination?.pages
                            ?: if (limit > 0) ((totalCount + limit - 1) / limit).coerceAtLeast(1) else 1

                        _canLoadMore.value = currentPage < totalPages

                        _uiState.value = QuotationUiState.Success(
                            quotations = loadedQuotations.toList(),
                            total = totalCount,
                            totalPages = totalPages
                        )
                    } else {
                        _canLoadMore.value = false
                    }
                }
                .onFailure {
                    _canLoadMore.value = false
                }

            _isLoadingMore.value = false
        }
    }

    fun saveDraft(request: CreateQuotationRequest) {
        launchBusy {
            _saveState.value = QuotationSaveUiState.Loading
            salesRepository.createQuotation(request)
                .onSuccess { response ->
                    val data = response.data
                    if (response.success && data != null) {
                        _saveState.value = QuotationSaveUiState.Success(data)
                    } else {
                        _saveState.value = QuotationSaveUiState.Error("Failed to save quotation")
                    }
                }
                .onFailure { e ->
                    _saveState.value = QuotationSaveUiState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun resetState() {
        _saveState.value = QuotationSaveUiState.Idle
    }

    fun searchQuotations(query: String) {
        loadQuotations(page = 1, search = query.ifBlank { null })
    }

    fun refresh() {
        loadQuotations(page = 1, search = currentSearch)
    }

    fun deleteQuotation(id: String) {
        launchBusy {
            _deleteState.value = QuotationDeleteUiState.Loading
            salesRepository.deleteQuotation(id)
                .onSuccess {
                    _deleteState.value = QuotationDeleteUiState.Success
                    refresh()
                }
                .onFailure { e ->
                    _deleteState.value = QuotationDeleteUiState.Error(e.message ?: "Failed to delete quotation")
                }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = QuotationDeleteUiState.Idle
    }

    fun fetchQuotationById(id: String) {
        launchBusy {
            _detailState.value = QuotationDetailUiState.Loading
            salesRepository.getQuotationById(id)
                .onSuccess { quotation ->
                    _detailState.value = QuotationDetailUiState.Success(quotation)
                }
                .onFailure { e ->
                    _detailState.value = QuotationDetailUiState.Error(e.message ?: "Failed to load quotation")
                }
        }
    }
}

sealed class QuotationPdfUiState {
    data object Idle : QuotationPdfUiState()
    data object Loading : QuotationPdfUiState()
    data class Success(val saved: com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator.SavedPdf) : QuotationPdfUiState()
    data class Error(val message: String) : QuotationPdfUiState()
}