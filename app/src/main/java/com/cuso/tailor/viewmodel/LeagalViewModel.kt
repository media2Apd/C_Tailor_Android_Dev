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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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

    /**
     * Fetches legal contracts list with optional search query and pagination
     */
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

    /**
     * Loads one contract for the overview screen.
     * Uses the already loaded list first. If the contract is not there,
     * the list endpoint is called again and the contract is looked up by id.
     */
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

    // ── Contract Creation State ──
    private val _isCreatingContract = MutableStateFlow(false)
    val isCreatingContract: StateFlow<Boolean> = _isCreatingContract.asStateFlow()

    private val _createContractError = MutableStateFlow<String?>(null)
    val createContractError: StateFlow<String?> = _createContractError.asStateFlow()

    /**
     * Creates a new legal contract using multipart form data matching backend requirements.
     * Binary files: mainDocuments, supportingDocuments
     */
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

    /**
     * Updates an existing legal contract using multipart form data.
     * Endpoint: PUT /api/legal/legal-contract/update/{contractId}
     */
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
                        // Update cache and refresh list
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

    /**
     * Deletes a legal contract by ID.
     * Endpoint: DELETE /api/legal/legal-contract/delete/{id}
     */
    fun deleteContract(
        contractId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            // Use JsonObject to prevent Gson from crashing on unpopulated string IDs in "data"
            val result = repo.request<JsonObject> {
                delete("$LEGAL_CONTRACT_DELETE_URL/$contractId")
            }

            result.fold(
                onSuccess = { json ->
                    val success = json.get("success")?.asBoolean ?: false
                    val message = json.get("message")?.asString ?: "Legal contract deleted successfully"

                    if (success) {
                        // Immediately remove from local list to update UI
                        _contracts.value = _contracts.value.filter { it.id != contractId }
                        // Fetch fresh list from server
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

    /**
     * Initiates contract renewal (POST /api/legal/legal-contract/renew/{id})
     */
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
                        // Immediately update single detail and local list
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

    // 2. Add loading state inside LegalViewModel class
    private val _isUpdatingRenewalStatus = MutableStateFlow(false)
    val isUpdatingRenewalStatus: StateFlow<Boolean> = _isUpdatingRenewalStatus.asStateFlow()

    /**
     * Updates the contract renewal status matching the exact transitions:
     * "Renewal_Notice" -> "In_Review" -> "Pending_Approval" -> "Approved" / "Rejected" -> "Renewed"
     */
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
                        // Update current detail state and local list immediately
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

    /**
     * Converts UI dates (dd-MM-yyyy or yyyy-MM-dd) to ISO 8601 UTC timestamp
     */
    /**
     * Converts any user selected date format (dd-MM-yyyy, dd/MM/yyyy, yyyy-MM-dd)
     * to exact ISO 8601 UTC timestamp (e.g., "2026-09-30T00:00:00.000Z")
     */
    private fun formatToApiIso(raw: String): String {
        if (raw.isBlank()) return ""
        val trimmed = raw.trim()

        // If already in ISO format, return directly
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