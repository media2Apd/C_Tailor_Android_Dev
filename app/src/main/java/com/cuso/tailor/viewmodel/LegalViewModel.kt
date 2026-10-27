@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.legal.*
import com.cuso.tailor.repository.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

private const val LEGAL_DASHBOARD_URL = "/api/legal/legal-dashboard/summary"
private const val LEGAL_CONTRACTS_URL = "/api/legal/legal-contract/view-all"
private const val LEGAL_CONTRACTS_OVERVIEW_URL = "/api/legal/legal-contract/view-one"
private const val LEGAL_CONTRACT_CREATE_URL = "/api/legal/legal-contract/create"
private const val LEGAL_CONTRACT_UPDATE_URL = "/api/legal/legal-contract/update"
private const val LEGAL_CONTRACT_DELETE_URL = "/api/legal/legal-contract/delete"
private const val LEGAL_CONTRACT_RENEW_BASE_URL = "/api/legal/legal-contract/renewal"
private const val LEGAL_CONTRACT_RENEWAL_STATUS_URL = "/api/legal/legal-contract/renewal/status"

private const val LEGAL_COMPLIANCE_VIEW_ALL_URL = "/api/legal/legal-compliance/view-all"
private const val LEGAL_COMPLIANCE_VIEW_ONE_URL = "/api/legal/legal-compliance/view-one"
private const val LEGAL_COMPLIANCE_STATUS_URL = "/api/legal/legal-compliance/status"
private const val LEGAL_COMPLIANCE_CREATE_URL = "/api/legal/legal-compliance/create"
private const val LEGAL_COMPLIANCE_UPDATE_URL = "/api/legal/legal-compliance/update"
private const val LEGAL_COMPLIANCE_DELETE_URL = "/api/legal/legal-compliance/delete"

private const val LEGAL_DOCUMENTS_VIEW_ALL_URL = "/api/legal/legal-document/view-all"
private const val LEGAL_DOCUMENTS_VIEW_ONE_URL = "/api/legal/legal-document/view-one"
private const val LEGAL_DOCUMENTS_DELETE_URL = "/api/legal/legal-document/delete"
private const val LEGAL_DOCUMENT_STATUS_URL = "/api/legal/legal-document/status"
private const val LEGAL_DOCUMENTS_CREATE_URL = "/api/legal/legal-document/create"
private const val LEGAL_DOCUMENTS_UPDATE_URL = "/api/legal/legal-document/update"

// ── Legal License Endpoints ──
private const val LEGAL_LICENSE_VIEW_ALL_URL = "/api/legal/legal-license/view-all"
private const val LEGAL_LICENSE_VIEW_ONE_URL = "/api/legal/legal-license/view-one"
private const val LEGAL_LICENSE_CREATE_URL = "/api/legal/legal-license/create"
private const val LEGAL_LICENSE_UPDATE_URL = "/api/legal/legal-license/update"
private const val LEGAL_LICENSE_DELETE_URL = "/api/legal/legal-license/delete"
private const val LEGAL_LICENSE_RENEWAL_URL = "/api/legal/legal-license/renew"

// ── Customer Legal Endpoints ──
private const val LEGAL_CUSTOMER_VIEW_ALL_URL = "/api/legal/legal-customer/view-all"
private const val LEGAL_CUSTOMER_VIEW_ONE_URL = "/api/legal/legal-customer/view-one"
private const val LEGAL_CUSTOMER_CREATE_URL = "/api/legal/legal-customer/create"
private const val LEGAL_CUSTOMER_UPDATE_URL = "/api/legal/legal-customer/update"
private const val LEGAL_CUSTOMER_DELETE_URL = "/api/legal/legal-customer/delete"
private const val LEGAL_CUSTOMER_STATUS_URL = "/api/legal/legal-customer/status"


// ── Legal Employee & Labour Endpoints ──
private const val LEGAL_EMPLOYEE_VIEW_ALL_URL = "/api/legal/legal-employee/view-all"
private const val LEGAL_EMPLOYEE_VIEW_ONE_URL = "/api/legal/legal-employee/view-one"
private const val LEGAL_EMPLOYEE_CREATE_URL = "/api/legal/legal-employee/create"
private const val LEGAL_EMPLOYEE_UPDATE_URL = "/api/legal/legal-employee/update"
private const val LEGAL_EMPLOYEE_DELETE_URL = "/api/legal/legal-employee/delete"
private const val LEGAL_EMPLOYEE_STATUS_URL = "/api/legal/legal-employee/status"

// Page size used when the detail screen has to reload the list to find a contract
private const val DETAIL_LOOKUP_LIMIT = 100

@HiltViewModel
class LegalViewModel @Inject constructor(
    private val repo: ApiRepository
) : ViewModel() {

    val currentCalendarYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private val _selectedYear = MutableStateFlow(currentCalendarYear)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _dashboardData = MutableStateFlow<LegalDashboardData?>(null)
    val dashboardData: StateFlow<LegalDashboardData?> = _dashboardData.asStateFlow()

    // ── Legal Contracts List State ──
    private val _contracts = MutableStateFlow<List<LegalContractDto>>(emptyList())
    val contracts: StateFlow<List<LegalContractDto>> = _contracts.asStateFlow()

    private val _isLoadingContracts = MutableStateFlow(false)
    val isLoadingContracts: StateFlow<Boolean> = _isLoadingContracts.asStateFlow()

    private val _contractsError = MutableStateFlow<String?>(null)
    val contractsError: StateFlow<String?> = _contractsError.asStateFlow()

    // ── Contract Detail State ──
    private val _contractDetail = MutableStateFlow<LegalContractDto?>(null)
    val contractDetail: StateFlow<LegalContractDto?> = _contractDetail.asStateFlow()

    private val _isLoadingContractDetail = MutableStateFlow(false)
    val isLoadingContractDetail: StateFlow<Boolean> = _isLoadingContractDetail.asStateFlow()

    private val _contractDetailError = MutableStateFlow<String?>(null)
    val contractDetailError: StateFlow<String?> = _contractDetailError.asStateFlow()

    // ── Save / Update Contract Loading State ──
    private val _isSavingContract = MutableStateFlow(false)
    val isSavingContract: StateFlow<Boolean> = _isSavingContract.asStateFlow()

    // ── Renew Contract State ──
    private val _isRenewingContract = MutableStateFlow(false)
    val isRenewingContract: StateFlow<Boolean> = _isRenewingContract.asStateFlow()

    init {
        fetchDashboardSummary(currentCalendarYear)
        fetchContracts()
        fetchCompliances()
        fetchLegalLicenses()
        fetchLegalEmployees()
    }

    fun fetchDashboardSummary(year: Int = _selectedYear.value) {
        _selectedYear.value = year
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repo.request<LegalDashboardResponse> {
                get(LEGAL_DASHBOARD_URL, repo.query("year" to year))
            }

            result.fold(
                onSuccess = { res -> _dashboardData.value = res.data },
                onFailure = { error -> _errorMessage.value = error.message }
            )
            _isLoading.value = false
        }
    }

    fun fetchContracts(search: String? = null, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingContracts.value = true
            _contractsError.value = null

            val result = repo.request<LegalContractsResponse> {
                get(
                    LEGAL_CONTRACTS_URL,
                    repo.query(
                        "search" to search,
                        "page" to page,
                        "limit" to limit
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _contracts.value = res.data.orEmpty()
                },
                onFailure = { error ->
                    _contractsError.value = error.message ?: "Failed to fetch legal contracts"
                }
            )
            _isLoadingContracts.value = false
        }
    }

    fun loadContractDetail(contractId: String) {
        viewModelScope.launch {
            _isLoadingContractDetail.value = true
            _contractDetailError.value = null
            _contractDetail.value = null

            val cached = _contracts.value.firstOrNull { it.id == contractId }
            if (cached != null) {
                _contractDetail.value = cached
                _isLoadingContractDetail.value = false
                return@launch
            }

            val result = repo.request<LegalContractsResponse> {
                get(
                    LEGAL_CONTRACTS_URL,
                    repo.query(
                        "page" to 1,
                        "limit" to DETAIL_LOOKUP_LIMIT
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val list = res.data.orEmpty()
                    _contracts.value = list
                    val found = list.firstOrNull { it.id == contractId }
                    if (found != null) {
                        _contractDetail.value = found
                    } else {
                        _contractDetailError.value = "Contract not found"
                    }
                },
                onFailure = { error ->
                    _contractDetailError.value = error.message ?: "Failed to load contract"
                }
            )
            _isLoadingContractDetail.value = false
        }
    }

    fun clearContractDetail() {
        _contractDetail.value = null
        _contractDetailError.value = null
    }

    private val _isCreatingContract = MutableStateFlow(false)
    val isCreatingContract: StateFlow<Boolean> = _isCreatingContract.asStateFlow()

    private val _createContractError = MutableStateFlow<String?>(null)
    val createContractError: StateFlow<String?> = _createContractError.asStateFlow()

    fun createContract(
        fields: Map<String, RequestBody>,
        mainFiles: List<MultipartBody.Part>,
        supportingFiles: List<MultipartBody.Part>,
        onSuccess: (LegalContractDto?) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingContract.value = true
            _createContractError.value = null

            val allFiles = mainFiles + supportingFiles

            val result = repo.request<LegalContractCreateResponse> {
                postMultipart(LEGAL_CONTRACT_CREATE_URL, fields, allFiles)
            }

            _isCreatingContract.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        onSuccess(res.data)
                    } else {
                        val msg = res.message ?: "Failed to create legal contract"
                        _createContractError.value = msg
                        onError(msg)
                    }
                },
                onFailure = { error ->
                    val msg = error.message ?: "Failed to create legal contract"
                    _createContractError.value = msg
                    onError(msg)
                }
            )
        }
    }

    fun updateContract(
        contractId: String,
        fields: Map<String, RequestBody>,
        mainFiles: List<MultipartBody.Part>,
        supportingFiles: List<MultipartBody.Part>,
        onSuccess: (LegalContractDto?) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingContract.value = true

            val allFiles = mainFiles + supportingFiles

            val result = repo.request<LegalContractUpdateResponse> {
                putMultipart("$LEGAL_CONTRACT_UPDATE_URL/$contractId", fields, allFiles)
            }

            _isSavingContract.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updated ->
                            _contractDetail.value = updated
                            _contracts.value = _contracts.value.map { item ->
                                if (item.id == contractId) updated else item
                            }
                        }
                        fetchContracts()
                        onSuccess(res.data)
                    } else {
                        onError(res.message ?: "Failed to update contract")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update contract")
                }
            )
        }
    }

    fun deleteContract(
        contractId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repo.request<JsonObject> {
                delete("$LEGAL_CONTRACT_DELETE_URL/$contractId")
            }

            result.fold(
                onSuccess = { json ->
                    val success = json.get("success")?.asBoolean ?: false
                    val message = json.get("message")?.asString ?: "Legal contract deleted successfully"

                    if (success) {
                        _contracts.value = _contracts.value.filter { it.id != contractId }
                        fetchContracts(page = 1)
                        onSuccess(message)
                    } else {
                        onError(message)
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete legal contract")
                }
            )
        }
    }

    fun renewContract(
        contractId: String,
        proposedStartDate: String,
        proposedEndDate: String,
        renewalValue: String,
        renewalOwnerId: String,
        renewalTerms: String,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isRenewingContract.value = true

            val request = RenewContractRequest(
                proposedStartDate = formatToApiIso(proposedStartDate),
                proposedEndDate = formatToApiIso(proposedEndDate),
                renewalValue = renewalValue.toDoubleOrNull(),
                renewalOwner = renewalOwnerId,
                renewalTerms = renewalTerms
            )

            val result = repo.request<LegalRenewContractResponse> {
                post("$LEGAL_CONTRACT_RENEW_BASE_URL/$contractId", request)
            }

            _isRenewingContract.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updatedContract ->
                            _contractDetail.value = updatedContract
                            _contracts.value = _contracts.value.map { item ->
                                if (item.id == contractId) updatedContract else item
                            }
                        }
                        onSuccess()
                    } else {
                        onError(res.message ?: "Failed to renew contract")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to renew contract")
                }
            )
        }
    }

    private val _isUpdatingRenewalStatus = MutableStateFlow(false)
    val isUpdatingRenewalStatus: StateFlow<Boolean> = _isUpdatingRenewalStatus.asStateFlow()

    fun updateContractRenewalStatus(
        contractId: String,
        status: String,
        approvalComments: String? = null,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isUpdatingRenewalStatus.value = true

            val request = UpdateContractRenewalStatusRequest(
                status = status,
                approvalComments = approvalComments?.trim()?.ifBlank { null }
            )

            val result = repo.request<LegalRenewStatusResponse> {
                patch("$LEGAL_CONTRACT_RENEWAL_STATUS_URL/$contractId", request)
            }

            _isUpdatingRenewalStatus.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updatedContract ->
                            _contractDetail.value = updatedContract
                            _contracts.value = _contracts.value.map { item ->
                                if (item.id == contractId) updatedContract else item
                            }
                        }
                        onSuccess(res.message ?: "Renewal status updated successfully")
                    } else {
                        onError(res.message ?: "Failed to update renewal status")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update renewal status")
                }
            )
        }
    }

    private val _compliances = MutableStateFlow<List<LegalComplianceDto>>(emptyList())
    val compliances: StateFlow<List<LegalComplianceDto>> = _compliances.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchCompliances(search: String? = null, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repo.request<LegalComplianceResponse> {
                get(
                    LEGAL_COMPLIANCE_VIEW_ALL_URL,
                    repo.query(
                        "search" to search,
                        "page" to page,
                        "limit" to limit
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _compliances.value = res.data.orEmpty()
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to fetch compliances"
                }
            )
            _isLoading.value = false
        }
    }

    private val _complianceDetail = MutableStateFlow<LegalComplianceDetailDto?>(null)
    val complianceDetail: StateFlow<LegalComplianceDetailDto?> = _complianceDetail.asStateFlow()

    private val _isLoadingComplianceDetail = MutableStateFlow(false)
    val isLoadingComplianceDetail: StateFlow<Boolean> = _isLoadingComplianceDetail.asStateFlow()

    private val _complianceDetailError = MutableStateFlow<String?>(null)
    val complianceDetailError: StateFlow<String?> = _complianceDetailError.asStateFlow()

    fun fetchComplianceDetail(complianceId: String) {
        viewModelScope.launch {
            _isLoadingComplianceDetail.value = true
            _complianceDetailError.value = null
            _complianceDetail.value = null

            val result = repo.request<LegalComplianceDetailResponse> {
                get("$LEGAL_COMPLIANCE_VIEW_ONE_URL/$complianceId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _complianceDetail.value = res.data
                    } else {
                        _complianceDetailError.value = res.message ?: "Failed to load compliance details"
                    }
                },
                onFailure = { error ->
                    _complianceDetailError.value = error.message ?: "Failed to load compliance details"
                }
            )
            _isLoadingComplianceDetail.value = false
        }
    }

    private val _isUpdatingComplianceStatus = MutableStateFlow(false)
    val isUpdatingComplianceStatus: StateFlow<Boolean> = _isUpdatingComplianceStatus.asStateFlow()

    fun updateComplianceStatus(
        complianceId: String,
        status: String,
        comments: String? = null,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isUpdatingComplianceStatus.value = true

            val request = UpdateComplianceStatusRequest(
                status = status,
                comments = comments?.trim()?.ifBlank { null }
            )

            val result = repo.request<UpdateComplianceStatusResponse> {
                patch("$LEGAL_COMPLIANCE_STATUS_URL/$complianceId", request)
            }

            _isUpdatingComplianceStatus.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updated ->
                            _complianceDetail.value = updated
                            _compliances.value = _compliances.value.map { item ->
                                if (item.id == complianceId) {
                                    item.copy(status = updated.status)
                                } else {
                                    item
                                }
                            }
                        }
                        onSuccess(res.message ?: "Legal compliance status updated successfully")
                    } else {
                        onError(res.message ?: "Failed to update compliance status")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update compliance status")
                }
            )
        }
    }

    private val _isSavingCompliance = MutableStateFlow(false)
    val isSavingCompliance: StateFlow<Boolean> = _isSavingCompliance.asStateFlow()

    fun createCompliance(
        fields: Map<String, RequestBody>,
        supportingFiles: List<MultipartBody.Part>,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingCompliance.value = true

            val result = repo.request<JsonObject> {
                postMultipart(LEGAL_COMPLIANCE_CREATE_URL, fields, supportingFiles)
            }

            _isSavingCompliance.value = false

            result.fold(
                onSuccess = { res ->
                    val success = res.get("success")?.asBoolean ?: false
                    if (success) {
                        onSuccess()
                    } else {
                        onError(res.get("message")?.asString ?: "Failed to create compliance")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to create compliance")
                }
            )
        }
    }

    fun updateCompliance(
        complianceId: String,
        fields: Map<String, RequestBody>,
        supportingFiles: List<MultipartBody.Part>,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingCompliance.value = true

            val result = repo.request<JsonObject> {
                putMultipart("$LEGAL_COMPLIANCE_UPDATE_URL/$complianceId", fields, supportingFiles)
            }

            _isSavingCompliance.value = false

            result.fold(
                onSuccess = { res ->
                    val success = res.get("success")?.asBoolean ?: false
                    if (success) {
                        onSuccess()
                    } else {
                        onError(res.get("message")?.asString ?: "Failed to update compliance")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update compliance")
                }
            )
        }
    }

    private val _isDeletingCompliance = MutableStateFlow(false)
    val isDeletingCompliance: StateFlow<Boolean> = _isDeletingCompliance.asStateFlow()

    fun deleteCompliance(
        complianceId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isDeletingCompliance.value = true

            val result = repo.request<DeleteComplianceResponse> {
                delete("$LEGAL_COMPLIANCE_DELETE_URL/$complianceId")
            }

            _isDeletingCompliance.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _compliances.value = _compliances.value.filter { it.id != complianceId }
                        fetchCompliances()
                        onSuccess(res.message ?: "Legal compliance deleted successfully")
                    } else {
                        onError(res.message ?: "Failed to delete legal compliance")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete legal compliance")
                }
            )
        }
    }

    // ── Document List State ──
    private val _legalDocuments = MutableStateFlow<List<LegalDocumentItemDto>>(emptyList())
    val legalDocuments: StateFlow<List<LegalDocumentItemDto>> = _legalDocuments.asStateFlow()

    private val _isLoadingLegalDocuments = MutableStateFlow(false)
    val isLoadingLegalDocuments: StateFlow<Boolean> = _isLoadingLegalDocuments.asStateFlow()

    private val _legalDocumentsError = MutableStateFlow<String?>(null)
    val legalDocumentsError: StateFlow<String?> = _legalDocumentsError.asStateFlow()

    // ── Document Detail State ──
    private val _legalDocumentDetail = MutableStateFlow<LegalDocumentDetailDto?>(null)
    val legalDocumentDetail: StateFlow<LegalDocumentDetailDto?> = _legalDocumentDetail.asStateFlow()

    private val _isLoadingLegalDocumentDetail = MutableStateFlow(false)
    val isLoadingLegalDocumentDetail: StateFlow<Boolean> = _isLoadingLegalDocumentDetail.asStateFlow()

    private val _legalDocumentDetailError = MutableStateFlow<String?>(null)
    val legalDocumentDetailError: StateFlow<String?> = _legalDocumentDetailError.asStateFlow()

    fun fetchLegalDocuments(search: String? = null, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingLegalDocuments.value = true
            _legalDocumentsError.value = null

            val result = repo.request<LegalDocumentListResponse> {
                get(
                    LEGAL_DOCUMENTS_VIEW_ALL_URL,
                    repo.query(
                        "search" to search,
                        "page" to page,
                        "limit" to limit
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _legalDocuments.value = res.data.orEmpty()
                },
                onFailure = { error ->
                    _legalDocumentsError.value = error.message ?: "Failed to load legal documents"
                }
            )
            _isLoadingLegalDocuments.value = false
        }
    }

    fun fetchLegalDocumentDetail(documentId: String) {
        viewModelScope.launch {
            _isLoadingLegalDocumentDetail.value = true
            _legalDocumentDetailError.value = null
            _legalDocumentDetail.value = null

            val result = repo.request<LegalDocumentDetailResponse> {
                get("$LEGAL_DOCUMENTS_VIEW_ONE_URL/$documentId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _legalDocumentDetail.value = res.data
                    } else {
                        _legalDocumentDetailError.value = res.message ?: "Failed to load document detail"
                    }
                },
                onFailure = { error ->
                    _legalDocumentDetailError.value = error.message ?: "Failed to load document detail"
                }
            )
            _isLoadingLegalDocumentDetail.value = false
        }
    }

    private val _isUpdatingLegalDocumentStatus = MutableStateFlow(false)
    val isUpdatingLegalDocumentStatus: StateFlow<Boolean> = _isUpdatingLegalDocumentStatus.asStateFlow()

    fun updateLegalDocumentStatus(
        documentId: String,
        status: String,
        comments: String? = null,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isUpdatingLegalDocumentStatus.value = true

            val request = UpdateDocumentStatusRequest(
                status = status,
                comments = comments?.trim()?.ifBlank { null }
            )

            val result = repo.request<UpdateDocumentStatusResponse> {
                patch("$LEGAL_DOCUMENT_STATUS_URL/$documentId", request)
            }

            _isUpdatingLegalDocumentStatus.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updated ->
                            _legalDocumentDetail.value = updated
                            _legalDocuments.value = _legalDocuments.value.map { item ->
                                if (item.id == documentId) {
                                    item.copy(status = updated.status)
                                } else {
                                    item
                                }
                            }
                        }
                        onSuccess(res.message ?: "Legal document status updated successfully")
                    } else {
                        onError(res.message ?: "Failed to update document status")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update document status")
                }
            )
        }
    }

    fun deleteLegalDocument(
        documentId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repo.request<JsonObject> {
                delete("$LEGAL_DOCUMENTS_DELETE_URL/$documentId")
            }

            result.fold(
                onSuccess = { json ->
                    val success = json.get("success")?.asBoolean ?: false
                    val message = json.get("message")?.asString ?: "Legal document deleted successfully"

                    if (success) {
                        _legalDocuments.value = _legalDocuments.value.filter { it.id != documentId }
                        fetchLegalDocuments()
                        onSuccess(message)
                    } else {
                        onError(message)
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete legal document")
                }
            )
        }
    }

    private val _isSavingDocument = MutableStateFlow(false)
    val isSavingDocument: StateFlow<Boolean> = _isSavingDocument.asStateFlow()

    fun createLegalDocument(
        fields: Map<String, RequestBody>,
        files: List<MultipartBody.Part>,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingDocument.value = true
            val result = repo.request<JsonObject> {
                postMultipart(LEGAL_DOCUMENTS_CREATE_URL, fields, files)
            }
            _isSavingDocument.value = false

            result.fold(
                onSuccess = { res ->
                    val success = res.get("success")?.asBoolean ?: false
                    if (success) {
                        fetchLegalDocuments()
                        onSuccess()
                    } else {
                        onError(res.get("message")?.asString ?: "Failed to create document")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to create document")
                }
            )
        }
    }

    fun updateLegalDocument(
        documentId: String,
        fields: Map<String, RequestBody>,
        files: List<MultipartBody.Part>,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingDocument.value = true
            val result = repo.request<JsonObject> {
                putMultipart("$LEGAL_DOCUMENTS_UPDATE_URL/$documentId", fields, files)
            }
            _isSavingDocument.value = false

            result.fold(
                onSuccess = { res ->
                    val success = res.get("success")?.asBoolean ?: false
                    if (success) {
                        fetchLegalDocuments()
                        onSuccess()
                    } else {
                        onError(res.get("message")?.asString ?: "Failed to update document")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update document")
                }
            )
        }
    }

    fun clearLegalDocumentDetail() {
        _legalDocumentDetail.value = null
        _legalDocumentDetailError.value = null
    }

    fun clearComplianceDetail() {
        _complianceDetail.value = null
        _complianceDetailError.value = null
    }

    // =========================================================================
    // ── LEGAL LICENSE STATE & API INTEGRATIONS ──
    // =========================================================================

    // ── Legal Licenses List State ──
    private val _legalLicenses = MutableStateFlow<List<LegalLicenseItemDto>>(emptyList())
    val legalLicenses: StateFlow<List<LegalLicenseItemDto>> = _legalLicenses.asStateFlow()

    private val _isLoadingLicenses = MutableStateFlow(false)
    val isLoadingLicenses: StateFlow<Boolean> = _isLoadingLicenses.asStateFlow()

    private val _licensesError = MutableStateFlow<String?>(null)
    val licensesError: StateFlow<String?> = _licensesError.asStateFlow()

    // ── Legal License Detail State (View One) ──
    private val _licenseDetail = MutableStateFlow<LegalLicenseItemDto?>(null)
    val licenseDetail: StateFlow<LegalLicenseItemDto?> = _licenseDetail.asStateFlow()

    private val _isLoadingLicenseDetail = MutableStateFlow(false)
    val isLoadingLicenseDetail: StateFlow<Boolean> = _isLoadingLicenseDetail.asStateFlow()

    private val _licenseDetailError = MutableStateFlow<String?>(null)
    val licenseDetailError: StateFlow<String?> = _licenseDetailError.asStateFlow()

    // ── License Save / Mutate State ──
    private val _isSavingLicense = MutableStateFlow(false)
    val isSavingLicense: StateFlow<Boolean> = _isSavingLicense.asStateFlow()

    /**
     * Fetches all legal licenses: GET /api/legal/legal-license/view-all
     */
    fun fetchLegalLicenses(search: String? = null, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingLicenses.value = true
            _licensesError.value = null

            val result = repo.request<LegalLicenseListResponse> {
                get(
                    LEGAL_LICENSE_VIEW_ALL_URL,
                    repo.query(
                        "search" to search,
                        "page" to page,
                        "limit" to limit
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _legalLicenses.value = res.data.orEmpty()
                },
                onFailure = { error ->
                    _licensesError.value = error.message ?: "Failed to fetch legal licenses"
                }
            )
            _isLoadingLicenses.value = false
        }
    }

    /**
     * Fetches a single license detail: GET /api/legal/legal-license/view-one/{id}
     */
    fun fetchLicenseDetail(licenseId: String) {
        viewModelScope.launch {
            _isLoadingLicenseDetail.value = true
            _licenseDetailError.value = null
            _licenseDetail.value = null

            val result = repo.request<LegalLicenseDetailResponse> {
                get("$LEGAL_LICENSE_VIEW_ONE_URL/$licenseId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _licenseDetail.value = res.data
                    } else {
                        _licenseDetailError.value = res.message ?: "Failed to load license details"
                    }
                },
                onFailure = { error ->
                    _licenseDetailError.value = error.message ?: "Failed to load license details"
                }
            )
            _isLoadingLicenseDetail.value = false
        }
    }

    fun clearLicenseDetail() {
        _licenseDetail.value = null
        _licenseDetailError.value = null
    }

    /**
     * Creates a new legal license using multipart/form-data: POST /api/legal/legal-license/create
     * Binary files: licenseCertificate, supportingDocuments
     */
    fun createLegalLicense(
        fields: Map<String, RequestBody>,
        certificateFiles: List<MultipartBody.Part>,
        supportingFiles: List<MultipartBody.Part>,
        onSuccess: (LegalLicenseItemDto?) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingLicense.value = true

            val allFiles = certificateFiles + supportingFiles

            val result = repo.request<LegalLicenseMutationResponse> {
                postMultipart(LEGAL_LICENSE_CREATE_URL, fields, allFiles)
            }

            _isSavingLicense.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchLegalLicenses()
                        onSuccess(res.data)
                    } else {
                        val msg = res.message ?: "Failed to create legal license"
                        onError(msg)
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to create legal license")
                }
            )
        }
    }

    /**
     * Updates an existing legal license using multipart/form-data: PUT /api/legal/legal-license/update/{id}
     */
    fun updateLegalLicense(
        licenseId: String,
        fields: Map<String, RequestBody>,
        certificateFiles: List<MultipartBody.Part>,
        supportingFiles: List<MultipartBody.Part>,
        onSuccess: (LegalLicenseItemDto?) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingLicense.value = true

            val allFiles = certificateFiles + supportingFiles

            val result = repo.request<LegalLicenseMutationResponse> {
                putMultipart("$LEGAL_LICENSE_UPDATE_URL/$licenseId", fields, allFiles)
            }

            _isSavingLicense.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updated ->
                            _licenseDetail.value = updated
                            _legalLicenses.value = _legalLicenses.value.map { item ->
                                if (item.id == licenseId) updated else item
                            }
                        }
                        fetchLegalLicenses()
                        onSuccess(res.data)
                    } else {
                        onError(res.message ?: "Failed to update legal license")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update legal license")
                }
            )
        }
    }

    private val _isRenewingLicense = MutableStateFlow(false)
    val isRenewingLicense: StateFlow<Boolean> = _isRenewingLicense.asStateFlow()

    /**
     * Renews an existing legal license: POST /api/legal/legal-license/renewal/{id}
     */
    fun renewLegalLicense(
        licenseId: String,
        effectiveDate: String,
        expiryDate: String,
        notes: String?,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isRenewingLicense.value = true

            fun toApiDate(raw: String): String {
                val clean = raw.trim().replace("/", "-")
                val parts = clean.split("-")
                return if (parts.size == 3 && parts[0].length == 2 && parts[2].length == 4) {
                    "${parts[2]}-${parts[1]}-${parts[0]}"
                } else {
                    clean.take(10)
                }
            }

            val request = RenewLicenseRequest(
                renewalEffectiveDate = toApiDate(effectiveDate),
                renewalExpiryDate = toApiDate(expiryDate),
                renewalNotes = notes?.trim()?.ifBlank { null }
            )

            val result = repo.request<JsonObject> {
                post("$LEGAL_LICENSE_RENEWAL_URL/$licenseId", request)
            }

            _isRenewingLicense.value = false

            result.fold(
                onSuccess = { json ->
                    val success = json.get("success")?.asBoolean ?: false
                    val message = json.get("message")?.asString ?: "License renewed successfully"
                    if (success) {
                        fetchLicenseDetail(licenseId)
                        fetchLegalLicenses()
                        onSuccess()
                    } else {
                        onError(message)
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to renew license")
                }
            )
        }
    }

    /**
     * Deletes a legal license by ID: DELETE /api/legal/legal-license/delete/{id}
     */
    fun deleteLegalLicense(
        licenseId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            // JsonObject used to prevent parsing failures if backend returns string ID in data
            val result = repo.request<JsonObject> {
                delete("$LEGAL_LICENSE_DELETE_URL/$licenseId")
            }

            result.fold(
                onSuccess = { json ->
                    val success = json.get("success")?.asBoolean ?: false
                    val message = json.get("message")?.asString ?: "Legal license deleted successfully"

                    if (success) {
                        _legalLicenses.value = _legalLicenses.value.filter { it.id != licenseId }
                        fetchLegalLicenses()
                        onSuccess(message)
                    } else {
                        onError(message)
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete legal license")
                }
            )
        }
    }

    // ── Customer Legal List State ──
    private val _legalCustomers = MutableStateFlow<List<LegalCustomerItemDto>>(emptyList())
    val legalCustomers: StateFlow<List<LegalCustomerItemDto>> = _legalCustomers.asStateFlow()

    private val _isLoadingCustomers = MutableStateFlow(false)
    val isLoadingCustomers: StateFlow<Boolean> = _isLoadingCustomers.asStateFlow()

    private val _customersError = MutableStateFlow<String?>(null)
    val customersError: StateFlow<String?> = _customersError.asStateFlow()

    // ── Customer Legal Detail State ──
    private val _customerDetail = MutableStateFlow<LegalCustomerItemDto?>(null)
    val customerDetail: StateFlow<LegalCustomerItemDto?> = _customerDetail.asStateFlow()

    private val _isLoadingCustomerDetail = MutableStateFlow(false)
    val isLoadingCustomerDetail: StateFlow<Boolean> = _isLoadingCustomerDetail.asStateFlow()

    private val _customerDetailError = MutableStateFlow<String?>(null)
    val customerDetailError: StateFlow<String?> = _customerDetailError.asStateFlow()

    private val _isSavingCustomer = MutableStateFlow(false)
    val isSavingCustomer: StateFlow<Boolean> = _isSavingCustomer.asStateFlow()

    fun fetchLegalCustomers(search: String? = null, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingCustomers.value = true
            _customersError.value = null

            val result = repo.request<LegalCustomerListResponse> {
                get(
                    LEGAL_CUSTOMER_VIEW_ALL_URL,
                    repo.query("search" to search, "page" to page, "limit" to limit)
                )
            }

            result.fold(
                onSuccess = { res -> _legalCustomers.value = res.data.orEmpty() },
                onFailure = { error -> _customersError.value = error.message ?: "Failed to fetch customer legal records" }
            )
            _isLoadingCustomers.value = false
        }
    }

    fun fetchLegalCustomerDetail(recordId: String) {
        viewModelScope.launch {
            _isLoadingCustomerDetail.value = true
            _customerDetailError.value = null
            _customerDetail.value = null

            val result = repo.request<LegalCustomerDetailResponse> {
                get("$LEGAL_CUSTOMER_VIEW_ONE_URL/$recordId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _customerDetail.value = res.data
                    } else {
                        _customerDetailError.value = res.message ?: "Failed to load record detail"
                    }
                },
                onFailure = { error ->
                    _customerDetailError.value = error.message ?: "Failed to load record detail"
                }
            )
            _isLoadingCustomerDetail.value = false
        }
    }

    fun clearLegalCustomerDetail() {
        _customerDetail.value = null
        _customerDetailError.value = null
    }

    fun createLegalCustomer(
        fields: Map<String, RequestBody>,
        supportingDocuments: List<MultipartBody.Part> = emptyList(),
        consentDocuments: List<MultipartBody.Part> = emptyList(),
        legalCorrespondence: List<MultipartBody.Part> = emptyList(),
        onSuccess: (LegalCustomerItemDto?) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingCustomer.value = true
            // Combine all 3 document categories matching Customer Legal API
            val allFiles = supportingDocuments + consentDocuments + legalCorrespondence

            val result = repo.request<LegalCustomerDetailResponse> {
                postMultipart(LEGAL_CUSTOMER_CREATE_URL, fields, allFiles)
            }
            _isSavingCustomer.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchLegalCustomers()
                        onSuccess(res.data)
                    } else {
                        onError(res.message ?: "Failed to create legal record")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to create legal record")
                }
            )
        }
    }

    fun updateLegalCustomer(
        recordId: String,
        fields: Map<String, RequestBody>,
        supportingDocuments: List<MultipartBody.Part> = emptyList(),
        consentDocuments: List<MultipartBody.Part> = emptyList(),
        legalCorrespondence: List<MultipartBody.Part> = emptyList(),
        onSuccess: (LegalCustomerItemDto?) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingCustomer.value = true
            // Combine all 3 document categories matching Customer Legal API
            val allFiles = supportingDocuments + consentDocuments + legalCorrespondence

            val result = repo.request<LegalCustomerDetailResponse> {
                putMultipart("$LEGAL_CUSTOMER_UPDATE_URL/$recordId", fields, allFiles)
            }
            _isSavingCustomer.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchLegalCustomers()
                        onSuccess(res.data)
                    } else {
                        onError(res.message ?: "Failed to update legal record")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update legal record")
                }
            )
        }
    }

    fun deleteLegalCustomer(
        recordId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repo.request<JsonObject> {
                delete("$LEGAL_CUSTOMER_DELETE_URL/$recordId")
            }

            result.fold(
                onSuccess = { json ->
                    val success = json.get("success")?.asBoolean ?: false
                    val message = json.get("message")?.asString ?: "Legal customer soft deleted"
                    if (success) {
                        _legalCustomers.value = _legalCustomers.value.filter { it.id != recordId }
                        fetchLegalCustomers()
                        onSuccess(message)
                    } else {
                        onError(message)
                    }
                },
                onFailure = { error -> onError(error.message ?: "Failed to delete record") }
            )
        }
    }

    private val _isUpdatingCustomerStatus = MutableStateFlow(false)
    val isUpdatingCustomerStatus: StateFlow<Boolean> = _isUpdatingCustomerStatus.asStateFlow()

    /**
     * Updates legal customer status (e.g., "Active", "Dispute").
     * Endpoint: PATCH /api/legal/legal-customer/status/{recordId}
     */
    fun updateLegalCustomerStatus(
        recordId: String,
        status: String,
        onSuccess: () -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isUpdatingCustomerStatus.value = true

            val request = JsonObject().apply {
                addProperty("status", status)
            }

            val result = repo.request<LegalCustomerDetailResponse> {
                patch("$LEGAL_CUSTOMER_STATUS_URL/$recordId", request)
            }

            _isUpdatingCustomerStatus.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _customerDetail.value = res.data
                        _legalCustomers.value = _legalCustomers.value.map { item ->
                            if (item.id == recordId) item.copy(status = res.data.status) else item
                        }
                        fetchLegalCustomers()
                        onSuccess()
                    } else {
                        onError(res.message ?: "Failed to update status")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update status")
                }
            )
        }
    }

    // ── Legal Employee & Labour List State ──
    private val _legalEmployees = MutableStateFlow<List<LegalEmployeeItem>>(emptyList())
    val legalEmployees: StateFlow<List<LegalEmployeeItem>> = _legalEmployees.asStateFlow()

    private val _isLoadingEmployees = MutableStateFlow(false)
    val isLoadingEmployees: StateFlow<Boolean> = _isLoadingEmployees.asStateFlow()

    private val _employeesError = MutableStateFlow<String?>(null)
    val employeesError: StateFlow<String?> = _employeesError.asStateFlow()

    // ── Legal Employee Detail State (View One) ──
    private val _employeeDetail = MutableStateFlow<LegalEmployeeItem?>(null)
    val employeeDetail: StateFlow<LegalEmployeeItem?> = _employeeDetail.asStateFlow()

    private val _isLoadingEmployeeDetail = MutableStateFlow(false)
    val isLoadingEmployeeDetail: StateFlow<Boolean> = _isLoadingEmployeeDetail.asStateFlow()

    private val _employeeDetailError = MutableStateFlow<String?>(null)
    val employeeDetailError: StateFlow<String?> = _employeeDetailError.asStateFlow()

    // ── Employee Save / Mutate State ──
    private val _isSavingEmployee = MutableStateFlow(false)
    val isSavingEmployee: StateFlow<Boolean> = _isSavingEmployee.asStateFlow()

    private val _isDeletingEmployee = MutableStateFlow(false)
    val isDeletingEmployee: StateFlow<Boolean> = _isDeletingEmployee.asStateFlow()

    private val _isUpdatingEmployeeStatus = MutableStateFlow(false)
    val isUpdatingEmployeeStatus: StateFlow<Boolean> = _isUpdatingEmployeeStatus.asStateFlow()

    // =========================================================================
    // ── LEGAL EMPLOYEE & LABOUR STATE & API INTEGRATIONS ──
    // =========================================================================

    /**
     * Fetches all legal employee records: GET /api/legal/legal-employee/view-all
     */
    fun fetchLegalEmployees(search: String? = null, page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            _isLoadingEmployees.value = true
            _employeesError.value = null

            val result = repo.request<LegalEmployeeListResponse> {
                val queryParams = mutableListOf<Pair<String, Any?>>(
                    "page" to page,
                    "limit" to limit
                )
                if (!search.isNullOrBlank()) {
                    queryParams.add("search" to search)
                }
                get(LEGAL_EMPLOYEE_VIEW_ALL_URL, repo.query(*queryParams.toTypedArray()))
            }

            result.fold(
                onSuccess = { res ->
                    _legalEmployees.value = res.data
                },
                onFailure = { error ->
                    _employeesError.value = error.message ?: "Failed to fetch legal employee records"
                }
            )
            _isLoadingEmployees.value = false
        }
    }

    /**
     * Fetches single legal employee record: GET /api/legal/legal-employee/view-one/{id}
     */
    fun fetchLegalEmployeeDetail(recordId: String) {
        viewModelScope.launch {
            _isLoadingEmployeeDetail.value = true
            _employeeDetailError.value = null
            _employeeDetail.value = null

            val result = repo.request<LegalEmployeeDetailResponse> {
                get("$LEGAL_EMPLOYEE_VIEW_ONE_URL/$recordId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _employeeDetail.value = res.data
                    } else {
                        _employeeDetailError.value = res.message ?: "Failed to load employee legal record"
                    }
                },
                onFailure = { error ->
                    _employeeDetailError.value = error.message ?: "Failed to load employee legal record"
                }
            )
            _isLoadingEmployeeDetail.value = false
        }
    }

    fun clearLegalEmployeeDetail() {
        _employeeDetail.value = null
        _employeeDetailError.value = null
    }

    /**
     * Creates new legal employee record: POST /api/legal/legal-employee/create
     * Handles 3 multipart file categories:
     * - employmentContractDocuments
     * - labourDocuments
     * - supportingDocuments
     */
    fun createLegalEmployee(
        fields: Map<String, RequestBody>,
        contractDocs: List<MultipartBody.Part> = emptyList(),
        labourDocs: List<MultipartBody.Part> = emptyList(),
        supportingDocs: List<MultipartBody.Part> = emptyList(),
        onSuccess: (LegalEmployeeItem?) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingEmployee.value = true

            val allFiles = contractDocs + labourDocs + supportingDocs

            val result = repo.request<LegalEmployeeDetailResponse> {
                postMultipart(LEGAL_EMPLOYEE_CREATE_URL, fields, allFiles)
            }

            _isSavingEmployee.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchLegalEmployees()
                        onSuccess(res.data)
                    } else {
                        onError(res.message ?: "Failed to create legal employee record")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to create legal employee record")
                }
            )
        }
    }

    /**
     * Updates an existing legal employee record: PUT /api/legal/legal-employee/update/{id}
     */
    fun updateLegalEmployee(
        recordId: String,
        fields: Map<String, RequestBody>,
        contractDocs: List<MultipartBody.Part> = emptyList(),
        labourDocs: List<MultipartBody.Part> = emptyList(),
        supportingDocs: List<MultipartBody.Part> = emptyList(),
        onSuccess: (LegalEmployeeItem?) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingEmployee.value = true

            val allFiles = contractDocs + labourDocs + supportingDocs

            val result = repo.request<LegalEmployeeDetailResponse> {
                putMultipart("$LEGAL_EMPLOYEE_UPDATE_URL/$recordId", fields, allFiles)
            }

            _isSavingEmployee.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updated ->
                            _employeeDetail.value = updated
                            _legalEmployees.value = _legalEmployees.value.map { item ->
                                if (item.id == recordId) updated else item
                            }
                        }
                        fetchLegalEmployees()
                        onSuccess(res.data)
                    } else {
                        onError(res.message ?: "Failed to update legal employee record")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update legal employee record")
                }
            )
        }
    }

    /**
     * Deletes a legal employee record: DELETE /api/legal/legal-employee/delete/{id}
     * Uses JsonObject to prevent crashes when backend returns string or object in data.
     */
    fun deleteLegalEmployee(
        recordId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isDeletingEmployee.value = true

            val result = repo.request<JsonObject> {
                delete("$LEGAL_EMPLOYEE_DELETE_URL/$recordId")
            }

            _isDeletingEmployee.value = false

            result.fold(
                onSuccess = { json ->
                    val success = json.get("success")?.asBoolean ?: false
                    val message = json.get("message")?.asString ?: "Legal employee record soft deleted"

                    if (success) {
                        _legalEmployees.value = _legalEmployees.value.filter { it.id != recordId }
                        fetchLegalEmployees()
                        onSuccess(message)
                    } else {
                        onError(message)
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete legal employee record")
                }
            )
        }
    }

    /**
     * Updates legal employee record status.
     * Flow: Pending -> InReview -> Approved -> Active -> Inactive
     * Endpoint: PATCH /api/legal/legal-employee/status/{id}
     */
    fun updateLegalEmployeeStatus(
        recordId: String,
        status: String,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isUpdatingEmployeeStatus.value = true

            val request = UpdateEmployeeStatusRequest(
                status = status
            )

            val result = repo.request<LegalEmployeeDetailResponse> {
                patch("$LEGAL_EMPLOYEE_STATUS_URL/$recordId", request)
            }

            _isUpdatingEmployeeStatus.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updated ->
                            // Keep populated fields of existing detail, only refresh status + activities
                            _employeeDetail.value = _employeeDetail.value?.copy(
                                status = updated.status,
                                activities = updated.activities
                            ) ?: updated
                            _legalEmployees.value = _legalEmployees.value.map { item ->
                                if (item.id == recordId) item.copy(status = updated.status) else item
                            }
                        }
                        onSuccess(res.message ?: "Legal employee record status updated successfully")
                    } else {
                        onError(res.message ?: "Failed to update status")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update status")
                }
            )
        }
    }

    private fun formatToApiIso(raw: String): String {
        if (raw.isBlank()) return ""
        val trimmed = raw.trim()

        if (trimmed.endsWith("Z") && trimmed.contains("T")) return trimmed

        val clean = trimmed.replace("/", "-")
        val formats = listOf(
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "dd-MMM-yyyy",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )

        val outFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        for (pattern in formats) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                    isLenient = false
                }
                val parsedDate = parser.parse(clean)
                if (parsedDate != null) {
                    return outFormat.format(parsedDate)
                }
            } catch (_: Exception) {
                // Try next format
            }
        }

        return raw
    }
}