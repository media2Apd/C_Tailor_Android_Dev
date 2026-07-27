package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.CreateQuotationRequest
import com.cuso.mobile.model.sales.QuotationCreatedData
import com.cuso.mobile.model.sales.QuotationItemDto
import com.cuso.mobile.repository.SalesRepository
import com.cuso.mobile.view.home.sales.customer.toDisplayDate
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

sealed class QuotationSaveUiState {
    object Idle : QuotationSaveUiState()
    object Loading : QuotationSaveUiState()
    data class Success(val quotation: QuotationCreatedData) : QuotationSaveUiState()
    data class Error(val message: String) : QuotationSaveUiState()
}

sealed class QuotationDeleteUiState {
    object Idle : QuotationDeleteUiState()
    object Loading : QuotationDeleteUiState()
    object Success : QuotationDeleteUiState()
    data class Error(val message: String) : QuotationDeleteUiState()
}
sealed class QuotationDetailUiState {
    object Idle : QuotationDetailUiState()
    object Loading : QuotationDetailUiState()
    data class Success(val quotation: QuotationItemDto) : QuotationDetailUiState()
    data class Error(val message: String) : QuotationDetailUiState()
}


@HiltViewModel
class QuotationViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val organizationDao: com.cuso.mobile.database.dao.OrganizationDao   // ✅ ADD — actual package path use pannunga
) : ViewModel() {
    private val _uiState = MutableStateFlow<QuotationUiState>(QuotationUiState.Loading)
    val uiState: StateFlow<QuotationUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<QuotationSaveUiState>(QuotationSaveUiState.Idle)
    val saveState: StateFlow<QuotationSaveUiState> = _saveState.asStateFlow()

    private val _deleteState = MutableStateFlow<QuotationDeleteUiState>(QuotationDeleteUiState.Idle)
    val deleteState: StateFlow<QuotationDeleteUiState> = _deleteState.asStateFlow()

    private val _detailState = MutableStateFlow<QuotationDetailUiState>(QuotationDetailUiState.Idle)
    val detailState: StateFlow<QuotationDetailUiState> = _detailState.asStateFlow()



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



    fun saveDraft(request: CreateQuotationRequest) {
        viewModelScope.launch {
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

    // ✅ Call this from the search bar's onValueChange (debounce if needed)
    fun searchQuotations(query: String) {
        loadQuotations(page = 1, search = query.ifBlank { null })
    }

    fun refresh() {
        loadQuotations(page = currentPage, search = currentSearch)
    }

    fun deleteQuotation(id: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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

    // ─────────────────────────────────────────────
    // PDF DOWNLOAD
    // ─────────────────────────────────────────────

    private val _pdfState = MutableStateFlow<QuotationPdfUiState>(QuotationPdfUiState.Idle)
    val pdfState: StateFlow<QuotationPdfUiState> = _pdfState.asStateFlow()

    fun downloadQuotationPdf(
        quotationId: String,
        pdfGenerator: com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator
    ) {
        viewModelScope.launch {
            _pdfState.value = QuotationPdfUiState.Loading
            val quotationResult = salesRepository.getQuotationById(quotationId)

            quotationResult.fold(
                onSuccess = { quotation ->
                    val organization = organizationDao.getOrganization()
                    val quotationData = com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator.QuotationData(
                        quotationNumber = quotation.quotationNumber ?: "",
                        logoUrl = organization?.organizationPicture,
                        quotationDate = quotation.createdAt.toDisplayDate(),
                        customerName = quotation.customerSnapshot?.name ?: "",
                        customerAddress = "",
                        customerEmail = quotation.customerSnapshot?.email ?: "",
                        customerPhone = quotation.customerSnapshot?.phone ?: "",
                        items = quotation.items.map { item ->
                            com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator.QuotationItem(
                                description = item.garmentName?:"",
                                quantity = item.quantity,
                                rate = item.unitPrice,
                                amount = item.totalPrice
                            )
                        },
                        subtotal = quotation.subTotal,
                        discountAmount = quotation.discountAmount,
                        total = quotation.grandTotal
                    )

                    pdfGenerator.downloadQuotationPdf(quotationData) { saved ->
                        _pdfState.value = if (saved != null && saved.exists() && saved.length() > 0) {
                            QuotationPdfUiState.Success(saved)
                        } else {
                            QuotationPdfUiState.Error("Failed to generate PDF")
                        }
                    }
                },
                onFailure = { e ->
                    _pdfState.value = QuotationPdfUiState.Error(e.message ?: "Failed to load quotation")
                }
            )
        }
    }
    // ─────────────────────────────────────────────
// PDF PREVIEW (same logo/org logic as download)
// ─────────────────────────────────────────────

    private val _previewData = MutableStateFlow<com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator.QuotationData?>(null)
    val previewData: StateFlow<com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator.QuotationData?> = _previewData.asStateFlow()

    fun loadPreviewData(quotationId: String) {
        viewModelScope.launch {
            salesRepository.getQuotationById(quotationId)
                .onSuccess { quotation ->
                    val organization = organizationDao.getOrganization()   // ✅ same fetch as download
                    _previewData.value = com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator.QuotationData(
                        quotationNumber = quotation.quotationNumber ?: "",
                        logoUrl = organization?.organizationPicture,       // ✅ this was missing in preview
                        quotationDate = quotation.createdAt.toDisplayDate(),
                        customerName = quotation.customerSnapshot?.name ?: "",
                        customerAddress = "",
                        customerEmail = quotation.customerSnapshot?.email ?: "",
                        customerPhone = quotation.customerSnapshot?.phone ?: "",
                        items = quotation.items.map { item ->
                            com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator.QuotationItem(
                                description = item.garmentName ?: "",
                                quantity = item.quantity,
                                rate = item.unitPrice,
                                amount = item.totalPrice
                            )
                        },
                        subtotal = quotation.subTotal,
                        discountAmount = quotation.discountAmount,
                        total = quotation.grandTotal
                    )
                }
        }
    }

    fun resetPreviewData() {
        _previewData.value = null
    }

    fun resetPdfState() {
        _pdfState.value = QuotationPdfUiState.Idle
    }

//    fun resetDetailState() {
//        _detailState.value = QuotationDetailUiState.Idle
//    }
}

sealed class QuotationPdfUiState {
    object Idle : QuotationPdfUiState()
    object Loading : QuotationPdfUiState()
    data class Success(val saved: com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator.SavedPdf) : QuotationPdfUiState()
    data class Error(val message: String) : QuotationPdfUiState()
}