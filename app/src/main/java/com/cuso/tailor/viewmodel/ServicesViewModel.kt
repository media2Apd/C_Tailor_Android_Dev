@file:Suppress("unused")
package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.service.*
import com.cuso.tailor.model.settings.CreateStageRequest
import com.cuso.tailor.model.settings.CreateTemplateRequest
import com.cuso.tailor.model.settings.ProductionStageDto
import com.cuso.tailor.model.settings.ProductionTemplateDto
import com.cuso.tailor.repository.ApiRepository
import com.cuso.tailor.repository.ServicesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import com.cuso.tailor.model.hr.GenericApiResponse

data class ServiceRequestDraft(
    val modeIndex: Int = 0, // 0 = Pre Order, 1 = External
    val customerId: String = "",
    val customerName: String = "",
    val customerCode: String = "",
    val customerMobile: String = "",
    val customerEmail: String = "",
    val selectedOrderId: String? = null,
    val selectedOrderCode: String? = null,
    val orderDeliveryDate: String = "",
    val selectedCustomGarmentId: String? = null,
    val serviceCategory: String = "",
    val priorityLevel: String = "",
    val preferredServiceDate: String = "",
    val expectedDeliveryDate: String = "",
    val garmentPiecesCount: Int = 1,
    val garmentDescription: String = "",
    val garmentCategoryName: String = "",
    val specificIssueDescription: String = "",
    val activeAlterationDeltas: List<AlterationDeltaDraft> = emptyList(),
    val selectedRequirements: List<String> = emptyList(),
    val isChargeableService: Boolean = false,
    val ratePerPiece: String = "0",
    val isTaxable: Boolean = false,
    val selectedTaxGroupId: String? = null,
    val selectedTaxGroupName: String? = null,
    val selectedAttachments: List<Uri> = emptyList(),
    val internalStaffNotes: String = ""
)

data class AlterationDeltaDraft(
    val area: String,
    val action: String = "",
    val currentVal: String = "",
    val requiredVal: String = "",
    val unit: String = "",
    val specificNotes: String = ""
)

data class ProductionTemplateUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 1,
    val templates: List<ProductionTemplateDto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = ""
)

sealed class ServiceRequestUiState {
    data object Loading : ServiceRequestUiState()
    data class Success(val items: List<ServiceRequestData>, val total: Int) : ServiceRequestUiState()
    data class Error(val message: String) : ServiceRequestUiState()
}

sealed class ServiceDetailUiState {
    data object Idle : ServiceDetailUiState()
    data object Loading : ServiceDetailUiState()
    data class Success(val request: ServiceRequestData) : ServiceDetailUiState()
    data class Error(val message: String) : ServiceDetailUiState()
}

sealed class ServiceActionUiState {
    data object Idle : ServiceActionUiState()
    data object Loading : ServiceActionUiState()
    data class Success(val message: String) : ServiceActionUiState()
    data class Error(val message: String) : ServiceActionUiState()
}
private const val SERVICE_ORDERS_VIEW_ALL_URL = "/api/services/production/job-cards/view-all"
private const val SALES_ORDERS_DROPDOWN_URL = "/api/sales/orders/dropdown-list"
private const val SERVICE_ALTERATION_VIEW_ONE_URL = "/api/services/alterations/tickets/view-one"
private const val SERVICES_ANALYTICS_URL = "/api/services/service-requests/dashboard/analytics"
private const val SERVICES_CHARTS_URL = "/api/services/service-requests/dashboard/charts"
private const val SERVICES_RECENT_REQUESTS_URL = "/api/services/service-requests/dashboard/recent"
private const val SERVICES_RECENT_COMPLAINTS_URL = "/api/services/complaints/dashboard/recent"
private const val SERVICES_RECENT_JOBCARDS_URL = "/api/services/production/dashboard/recent-cards"
private const val SERVICES_RECENT_FEEDBACKS_URL = "/api/services/feedbacks/dashboard/recent"
private const val FEEDBACK_VIEW_ONE_URL = "/api/services/feedbacks/view-one"
private const val SERVICE_REQUEST_VIEW_ONE_URL = "/api/services/service-requests/view-one"
private const val SERVICE_REQUESTS_VIEW_ALL_URL = "/api/services/service-requests/view-all"
private const val SERVICE_ALTERATION_VIEW_ALL_URL = "/api/services/alterations/tickets/view-all"
private const val SERVICE_ALTERATION_UPDATE_STATUS_URL = "/api/services/service-requests/change-status"
private const val SERVICE_ALTERATION_COMMENT_URL = "/api/services/alterations/tickets/add-comment"
private const val COMPLAINTS_VIEW_ALL_URL = "/api/services/complaints/view-all"
private const val COMPLAINTS_VIEW_ONE_URL = "/api/services/complaints/view-one"
private const val FEEDBACKS_VIEW_ALL_URL = "/api/services/feedbacks/view-all"
private const val COMPLAINT_ATTACHMENT_UPLOAD_URL = "/api/services/complaints/upload-attachments"
private const val COMPLAINT_UPDATE_URL = "/api/services/complaints/update-one"
private const val COMPLAINT_RESOLVE_URL = "/api/services/complaints/resolve"
private const val SERVICE_ORDER_DETAIL_URL = "/api/services/production/job-cards/order-view"
private const val WORKSHOP_SPECIALISTS_WORKLOAD_URL = "/api/services/production/workers/workload/view-all"
private const val JOB_CARD_VIEW_ONE_URL = "/api/services/production/job-cards/view-one"



private const val UPDATE_STAGE_PROGRESS_URL = "/api/services/production/job-cards/update-stage"
private const val ALLOCATE_WORKERS_URL = "/api/services/production/job-cards/bulk-assign"
private const val LOG_ISSUE_TO_JOB_CARD_URL = "/api/services/production/job-cards/log-issue"
private const val LOG_FABRIC_WASTAGE_URL = "/api/services/production/job-cards/fabric-wastage"
private const val MARK_READY_FOR_DELIVERY_URL = "/api/services/production/job-cards/update-progress"




@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repository: ServicesRepository,
    private val repo: ApiRepository
) : ViewModel() {

    // In-memory draft for Create <-> Review navigation
    private val _serviceDraft = MutableStateFlow<ServiceRequestDraft?>(null)
    val serviceDraft: StateFlow<ServiceRequestDraft?> = _serviceDraft.asStateFlow()

    fun saveServiceDraft(draft: ServiceRequestDraft) {
        _serviceDraft.value = draft
    }

    fun clearServiceDraft() {
        _serviceDraft.value = null
    }

    // Template States
    private val _uiState = MutableStateFlow(ProductionTemplateUiState())
    val uiState: StateFlow<ProductionTemplateUiState> = _uiState.asStateFlow()

    private val _selectedTemplate = MutableStateFlow<ProductionTemplateDto?>(null)
    val selectedTemplate: StateFlow<ProductionTemplateDto?> = _selectedTemplate.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // Production Stages States
    private val _stages = MutableStateFlow<List<ProductionStageDto>>(emptyList())
    val stages: StateFlow<List<ProductionStageDto>> = _stages.asStateFlow()

    private val _isLoadingStages = MutableStateFlow(false)
    val isLoadingStages: StateFlow<Boolean> = _isLoadingStages.asStateFlow()

    private val _stagesError = MutableStateFlow<String?>(null)
    val stagesError: StateFlow<String?> = _stagesError.asStateFlow()

    // Dashboard States
    private val _analytics = MutableStateFlow<ServicesAnalyticsData?>(null)
    val analytics: StateFlow<ServicesAnalyticsData?> = _analytics.asStateFlow()

    private val _charts = MutableStateFlow<ServicesChartsData?>(null)
    val charts: StateFlow<ServicesChartsData?> = _charts.asStateFlow()

    private val _recentRequests = MutableStateFlow<List<RecentServiceRequestItem>>(emptyList())
    val recentRequests: StateFlow<List<RecentServiceRequestItem>> = _recentRequests.asStateFlow()

    private val _recentJobCards = MutableStateFlow<List<RecentJobCardItem>>(emptyList())
    val recentJobCards: StateFlow<List<RecentJobCardItem>> = _recentJobCards.asStateFlow()

    private val _recentComplaints = MutableStateFlow<List<RecentComplaintItem>>(emptyList())
    val recentComplaints: StateFlow<List<RecentComplaintItem>> = _recentComplaints.asStateFlow()

    private val _recentFeedbacks = MutableStateFlow<List<RecentFeedbackItem>>(emptyList())
    val recentFeedbacks: StateFlow<List<RecentFeedbackItem>> = _recentFeedbacks.asStateFlow()

    private val _isDashboardLoading = MutableStateFlow(false)
    val isDashboardLoading: StateFlow<Boolean> = _isDashboardLoading.asStateFlow()

    // ── Full Service Requests List State (View-All) ──
    private val _serviceRequestsList = MutableStateFlow<List<ServiceRequestListItem>>(emptyList())
    val serviceRequestsList: StateFlow<List<ServiceRequestListItem>> = _serviceRequestsList.asStateFlow()

    private val _serviceRequestsPagination = MutableStateFlow<ServiceRequestPagination?>(null)
    val serviceRequestsPagination: StateFlow<ServiceRequestPagination?> = _serviceRequestsPagination.asStateFlow()

    private val _isLoadingServiceRequestsList = MutableStateFlow(false)
    val isLoadingServiceRequestsList: StateFlow<Boolean> = _isLoadingServiceRequestsList.asStateFlow()

    private val _serviceRequestsListError = MutableStateFlow<String?>(null)
    val serviceRequestsListError: StateFlow<String?> = _serviceRequestsListError.asStateFlow()

    init {
        loadTemplates()
        loadStages()
        fetchDashboardData()
        fetchServiceRequestsList()
    }

    // ── Service Orders List State ──
    private val _serviceOrdersList = MutableStateFlow<List<ServiceOrderItemDto>>(emptyList())
    val serviceOrdersList: StateFlow<List<ServiceOrderItemDto>> = _serviceOrdersList.asStateFlow()

    private val _serviceOrdersPagination = MutableStateFlow<ServiceOrderPagination?>(null)
    val serviceOrdersPagination: StateFlow<ServiceOrderPagination?> = _serviceOrdersPagination.asStateFlow()

    private val _isLoadingServiceOrders = MutableStateFlow<Boolean>(false)
    val isLoadingServiceOrders: StateFlow<Boolean> = _isLoadingServiceOrders.asStateFlow()

    private val _serviceOrdersError = MutableStateFlow<String?>(null)
    val serviceOrdersError: StateFlow<String?> = _serviceOrdersError.asStateFlow()

    private var serviceOrderSearchJob: Job? = null

    /**
     * Fetches paginated service orders / production orders with search and status filters
     */
    fun fetchServiceOrders(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingServiceOrders.value = true
            _serviceOrdersError.value = null

            val result = repo.request<ServiceOrderListResponse> {
                get(
                    SERVICE_ORDERS_VIEW_ALL_URL,
                    repo.query(
                        "page" to page,
                        "limit" to limit,
                        "search" to search,
                        "status" to status
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _serviceOrdersList.value = res.data.orEmpty()
                    _serviceOrdersPagination.value = res.pagination
                },
                onFailure = { error ->
                    _serviceOrdersError.value = error.message ?: "Failed to load service orders"
                    _serviceOrdersList.value = emptyList()
                }
            )
            _isLoadingServiceOrders.value = false
        }
    }

    // ── Workshop Specialists Workload State ──
    private val _specialistWorkloadList = MutableStateFlow<List<SpecialistWorkloadDto>>(emptyList())
    val specialistWorkloadList: StateFlow<List<SpecialistWorkloadDto>> = _specialistWorkloadList.asStateFlow()

    private val _isLoadingSpecialists = MutableStateFlow(false)
    val isLoadingSpecialists: StateFlow<Boolean> = _isLoadingSpecialists.asStateFlow()

    private val _specialistsError = MutableStateFlow<String?>(null)
    val specialistsError: StateFlow<String?> = _specialistsError.asStateFlow()

    /**
     * Fetches workshop specialists workload and active tasks via ApiRepository
     */
    fun fetchSpecialistWorkload() {
        viewModelScope.launch {
            _isLoadingSpecialists.value = true
            _specialistsError.value = null

            val result = repo.request<SpecialistWorkloadResponse> {
                get(WORKSHOP_SPECIALISTS_WORKLOAD_URL)
            }

            result.fold(
                onSuccess = { res ->
                    _specialistWorkloadList.value = res.data.orEmpty()
                },
                onFailure = { err ->
                    _specialistsError.value = err.message ?: "Failed to load specialists workload"
                }
            )
            _isLoadingSpecialists.value = false
        }
    }

    // Add state flows inside ServicesViewModel
    private val _serviceOrderDetail = MutableStateFlow<ServiceOrderDetailData?>(null)
    val serviceOrderDetail: StateFlow<ServiceOrderDetailData?> = _serviceOrderDetail.asStateFlow()

    private val _isLoadingOrderDetail = MutableStateFlow(false)
    val isLoadingOrderDetail: StateFlow<Boolean> = _isLoadingOrderDetail.asStateFlow()

    private val _orderDetailError = MutableStateFlow<String?>(null)
    val orderDetailError: StateFlow<String?> = _orderDetailError.asStateFlow()

    /**
     * Fetches the full service order details with job cards and workflow stages
     */
    fun fetchServiceOrderDetail(orderId: String) {
        if (orderId.isBlank()) return
        viewModelScope.launch {
            _isLoadingOrderDetail.value = true
            _orderDetailError.value = null

            val result = repo.request<ServiceOrderDetailResponse> {
                get("$SERVICE_ORDER_DETAIL_URL/$orderId")
            }

            result.fold(
                onSuccess = { res ->
                    _serviceOrderDetail.value = res.data
                    _isLoadingOrderDetail.value = false
                },
                onFailure = { err ->
                    _orderDetailError.value = err.message ?: "Failed to load order details"
                    _isLoadingOrderDetail.value = false
                }
            )
        }
    }

    fun onServiceOrderSearch(query: String, status: String? = null) {
        serviceOrderSearchJob?.cancel()
        serviceOrderSearchJob = viewModelScope.launch {
            delay(350)
            fetchServiceOrders(
                page = 1,
                limit = 20,
                search = query.takeIf { it.isNotBlank() },
                status = status
            )
        }
    }

    // 2. Add these StateFlow variables inside ServicesViewModel class:
    private val _jobCardDetail = MutableStateFlow<JobCardViewOneData?>(null)
    val jobCardDetail: StateFlow<JobCardViewOneData?> = _jobCardDetail.asStateFlow()

    private val _isLoadingJobCardDetail = MutableStateFlow(false)
    val isLoadingJobCardDetail: StateFlow<Boolean> = _isLoadingJobCardDetail.asStateFlow()

    private val _jobCardDetailError = MutableStateFlow<String?>(null)
    val jobCardDetailError: StateFlow<String?> = _jobCardDetailError.asStateFlow()

    /**
     * Fetches single job card tracking, workbench details, and activity logs
     */
    fun fetchJobCardDetail(jobCardId: String) {
        if (jobCardId.isBlank()) return
        viewModelScope.launch {
            _isLoadingJobCardDetail.value = true
            _jobCardDetailError.value = null

            val result = repo.request<JobCardViewOneResponse> {
                get("$JOB_CARD_VIEW_ONE_URL/$jobCardId")
            }

            result.fold(
                onSuccess = { res ->
                    _jobCardDetail.value = res.data
                    _isLoadingJobCardDetail.value = false
                },
                onFailure = { err ->
                    _jobCardDetailError.value = err.message ?: "Failed to load job card details"
                    _isLoadingJobCardDetail.value = false
                }
            )
        }
    }

    fun clearJobCardDetail() {
        _jobCardDetail.value = null
        _jobCardDetailError.value = null
    }

    // ── Update Stage Progress States ──
    // ── Update Stage Progress States ──
    private val _isUpdatingStageProgress = MutableStateFlow<Boolean>(false)
    val isUpdatingStageProgress: StateFlow<Boolean> = _isUpdatingStageProgress.asStateFlow()

    private val _updateStageProgressError = MutableStateFlow<String?>(null)
    val updateStageProgressError: StateFlow<String?> = _updateStageProgressError.asStateFlow()

    // ── Allocate Workers Across Workshop Stages State ──
    private val _isAllocatingWorkers = MutableStateFlow<Boolean>(false)
    val isAllocatingWorkers: StateFlow<Boolean> = _isAllocatingWorkers.asStateFlow()

    private val _allocateWorkersError = MutableStateFlow<String?>(null)
    val allocateWorkersError: StateFlow<String?> = _allocateWorkersError.asStateFlow()

    /**
     * Allocates assigned workers to job card stages and routes to workshop
     * Endpoint: PUT /api/services/production/job-cards/allocate-workers/{jobCardId}
     */
    fun allocateWorkersToWorkshop(
        jobCardId: String,
        assignments: List<AllocateWorkersStageItem>,
        forceReassign: Boolean = false,
        replaceExisting: Boolean = true,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (jobCardId.isBlank()) {
            onError("Invalid Job Card ID")
            return
        }

        if (assignments.isEmpty()) {
            onError("No worker assignments provided")
            return
        }

        viewModelScope.launch {
            _isAllocatingWorkers.value = true
            _allocateWorkersError.value = null

            val payload = AllocateWorkersRequest(
                assignments = assignments,
                forceReassign = forceReassign,
                replaceExisting = replaceExisting
            )

            val result = repo.request<AllocateWorkersResponse> {
                patch("$ALLOCATE_WORKERS_URL/$jobCardId", payload)
            }

            result.fold(
                onSuccess = { res ->
                    _isAllocatingWorkers.value = false
                    if (res.data != null) {
                        _jobCardDetail.value = res.data
                    }
                    onSuccess(res.message ?: "Workers successfully pre-allocated across stages.")
                },
                onFailure = { error ->
                    _isAllocatingWorkers.value = false
                    val errorMsg = error.message ?: "Failed to allocate workers to workshop"
                    _allocateWorkersError.value = errorMsg
                    onError(errorMsg)
                }
            )
        }
    }

    /**
     * Updates Job Card stage status (e.g. Trial -> Rework)
     * Payload: { "notes": "...", "stageId": "...", "status": "Rework" }
     */
    fun updateStageProgress(
        jobCardId: String,
        stageId: String,
        status: String,
        notes: String = "",
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (jobCardId.isBlank() || stageId.isBlank()) {
            onError("Invalid Job Card or Stage ID")
            return
        }

        viewModelScope.launch {
            _isUpdatingStageProgress.value = true
            _updateStageProgressError.value = null

            val payload = UpdateStageProgressRequest(
                stageId = stageId,
                status = status,
                notes = notes.trim()
            )

            val result = repo.request<UpdateStageProgressResponse> {
                put("$UPDATE_STAGE_PROGRESS_URL/$jobCardId", payload)
            }

            result.fold(
                onSuccess = { res ->
                    _isUpdatingStageProgress.value = false
                    if (res.data != null) {
                        _jobCardDetail.value = res.data
                    }
                    onSuccess(res.message ?: "Stage progress successfully updated.")
                },
                onFailure = { error ->
                    _isUpdatingStageProgress.value = false
                    val errorMsg = error.message ?: "Failed to update stage progress"
                    _updateStageProgressError.value = errorMsg
                    onError(errorMsg)
                }
            )
        }
    }

    /**
     * Fetches the full list of service requests with pagination and filters
     */
    fun fetchServiceRequestsList(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingServiceRequestsList.value = true
            _serviceRequestsListError.value = null

            val result = repo.request<ServiceRequestListResponse> {
                get(
                    SERVICE_REQUESTS_VIEW_ALL_URL,
                    repo.query(
                        "page" to page,
                        "limit" to limit,
                        "search" to search,
                        "status" to status
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _serviceRequestsList.value = res.data.orEmpty()
                    _serviceRequestsPagination.value = res.pagination
                },
                onFailure = { error ->
                    _serviceRequestsListError.value = error.message ?: "Failed to load service requests"
                }
            )
            _isLoadingServiceRequestsList.value = false
        }
    }
    private val _serviceRequestDetail = MutableStateFlow<ServiceRequestDetailData?>(null)
    val serviceRequestDetail: StateFlow<ServiceRequestDetailData?> = _serviceRequestDetail.asStateFlow()

    private val _isLoadingServiceRequestDetail = MutableStateFlow(false)
    val isLoadingServiceRequestDetail: StateFlow<Boolean> = _isLoadingServiceRequestDetail.asStateFlow()

    private val _serviceRequestDetailError = MutableStateFlow<String?>(null)
    val serviceRequestDetailError: StateFlow<String?> = _serviceRequestDetailError.asStateFlow()

// 3. Add these functions inside ServicesViewModel class:

    /**
     * Fetches single service request details by ID using ApiRepository
     */
    fun fetchServiceRequestDetail(id: String) {
        viewModelScope.launch {
            _isLoadingServiceRequestDetail.value = true
            _serviceRequestDetailError.value = null

            val result = repo.request<SingleServiceRequestResponse> {
                get("$SERVICE_REQUEST_VIEW_ONE_URL/$id")
            }

            result.fold(
                onSuccess = { res ->
                    _serviceRequestDetail.value = res.data
                },
                onFailure = { error ->
                    _serviceRequestDetailError.value = error.message ?: "Failed to load service request details"
                }
            )

            _isLoadingServiceRequestDetail.value = false
        }
    }

    // Inside ServicesViewModel class:
    private val _salesOrdersDropdown = MutableStateFlow<List<SalesOrderDropdownItem>>(emptyList())
    val salesOrdersDropdown: StateFlow<List<SalesOrderDropdownItem>> = _salesOrdersDropdown.asStateFlow()

    private val _isLoadingSalesOrders = MutableStateFlow(false)
    val isLoadingSalesOrders: StateFlow<Boolean> = _isLoadingSalesOrders.asStateFlow()

    /**
     * Fetches sales orders for a specific customer ID
     */
    fun fetchSalesOrdersByCustomer(customerId: String, status: String) {
        viewModelScope.launch {
            _isLoadingSalesOrders.value = true
            val result = repo.request<SalesOrderDropdownResponse> {
                get(
                    SALES_ORDERS_DROPDOWN_URL,
                    repo.query("customerId" to customerId, "status" to status)
                )
            }
            result.fold(
                onSuccess = { res ->
                    _salesOrdersDropdown.value = res.data.orEmpty()
                },
                onFailure = {
                    _salesOrdersDropdown.value = emptyList()
                }
            )
            _isLoadingSalesOrders.value = false
        }
    }

    /**
     * Clears the sales orders list when customer is reset
     */
    fun clearSalesOrders() {
        _salesOrdersDropdown.value = emptyList()
    }

    /**
     * Clears the currently selected service request detail state
     */
    fun clearServiceRequestDetail() {
        _serviceRequestDetail.value = null
        _serviceRequestDetailError.value = null
    }

    /**
     * Loads all services dashboard widgets concurrently via ApiRepository
     */
    fun fetchDashboardData() {
        viewModelScope.launch {
            _isDashboardLoading.value = true

            launch {
                repo.request<ServicesAnalyticsResponse> { get(SERVICES_ANALYTICS_URL) }
                    .onSuccess { _analytics.value = it.data }
            }
            launch {
                repo.request<ServicesChartsResponse> { get(SERVICES_CHARTS_URL) }
                    .onSuccess { _charts.value = it.data }
            }
            launch {
                repo.request<RecentServiceRequestsResponse> { get(SERVICES_RECENT_REQUESTS_URL) }
                    .onSuccess { _recentRequests.value = it.data.orEmpty() }
            }
            launch {
                repo.request<RecentJobCardsResponse> { get(SERVICES_RECENT_JOBCARDS_URL) }
                    .onSuccess { _recentJobCards.value = it.data.orEmpty() }
            }
            launch {
                repo.request<RecentComplaintsResponse> { get(SERVICES_RECENT_COMPLAINTS_URL) }
                    .onSuccess { _recentComplaints.value = it.data.orEmpty() }
            }
            launch {
                repo.request<RecentFeedbacksResponse> { get(SERVICES_RECENT_FEEDBACKS_URL) }
                    .onSuccess { _recentFeedbacks.value = it.data.orEmpty() }
            }

            _isDashboardLoading.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Template Actions
    // ─────────────────────────────────────────────────────────────

    fun loadTemplates(search: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, currentPage = 1) }
            repository.getTemplates(page = 1, limit = 10, search = search)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            templates = response.data,
                            canLoadMore = response.pagination?.hasNextPage ?: false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun loadMoreTemplates() {
        val state = _uiState.value
        if (state.isLoadingMore || state.isLoading || !state.canLoadMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = state.currentPage + 1

            repository.getTemplates(
                page = nextPage,
                limit = 10,
                search = state.searchQuery.takeIf { it.isNotBlank() }
            ).onSuccess { response ->
                _uiState.update { s ->
                    s.copy(
                        isLoadingMore = false,
                        currentPage = nextPage,
                        templates = (s.templates + response.data).distinctBy { it.id },
                        canLoadMore = response.pagination?.hasNextPage ?: false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadTemplates(search = query.takeIf { it.isNotBlank() })
        }
    }

    fun fetchTemplateDetail(id: String) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            repository.getTemplateById(id)
                .onSuccess { template -> _selectedTemplate.value = template }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
            _isLoadingDetail.value = false
        }
    }

    fun setSelectedTemplateDirect(template: ProductionTemplateDto?) {
        _selectedTemplate.value = template
    }

    fun createTemplate(request: CreateTemplateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.createTemplate(request)
                .onSuccess {
                    _uiState.update { s -> s.copy(successMessage = "Template created successfully!") }
                    loadTemplates()
                    onSuccess()
                }
                .onFailure { error -> _uiState.update { s -> s.copy(errorMessage = error.message) } }
            _isSubmitting.value = false
        }
    }

    fun updateTemplate(id: String, request: CreateTemplateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateTemplate(id, request)
                .onSuccess {
                    _uiState.update { s -> s.copy(successMessage = "Template updated successfully!") }
                    loadTemplates()
                    onSuccess()
                }
                .onFailure { error -> _uiState.update { s -> s.copy(errorMessage = error.message) } }
            _isSubmitting.value = false
        }
    }

    fun deleteTemplate(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteTemplate(id)
                .onSuccess {
                    _uiState.update { s -> s.copy(successMessage = "Template deleted successfully") }
                    loadTemplates()
                    onSuccess()
                }
                .onFailure { error -> _uiState.update { s -> s.copy(errorMessage = error.message) } }
            _isSubmitting.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Production Stage Actions
    // ─────────────────────────────────────────────────────────────

    fun loadStages() {
        viewModelScope.launch {
            _isLoadingStages.value = true
            _stagesError.value = null
            repository.getProductionStages(page = 1, limit = 50)
                .onSuccess { response ->
                    _stages.value = response.data
                    _isLoadingStages.value = false
                }
                .onFailure { error ->
                    _stagesError.value = error.message
                    _isLoadingStages.value = false
                }
        }
    }

    fun createStage(request: CreateStageRequest, onSuccess: (ProductionStageDto) -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.createProductionStage(request)
                .onSuccess { createdStage ->
                    loadStages()
                    onSuccess(createdStage)
                }
                .onFailure { error ->
                    _uiState.update { s -> s.copy(errorMessage = error.message) }
                }
            _isSubmitting.value = false
        }
    }

    fun deleteStage(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteProductionStage(id)
                .onSuccess {
                    loadStages()
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { s -> s.copy(errorMessage = error.message) }
                }
            _isSubmitting.value = false
        }
    }

    fun clearAlerts() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private val _listState = MutableStateFlow<ServiceRequestUiState>(ServiceRequestUiState.Loading)
    val listState: StateFlow<ServiceRequestUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<ServiceDetailUiState>(ServiceDetailUiState.Idle)
    val detailState: StateFlow<ServiceDetailUiState> = _detailState.asStateFlow()

    private val _actionState = MutableStateFlow<ServiceActionUiState>(ServiceActionUiState.Idle)
    val actionState: StateFlow<ServiceActionUiState> = _actionState.asStateFlow()

    private var searchJob: Job? = null
    private var currentPage = 1
    private var currentSearch: String? = null
    private var currentStatus: String? = null

    fun loadServiceRequests(
        page: Int = 1,
        search: String? = null,
        status: String? = null
    ) {
        currentPage = page
        currentSearch = search
        currentStatus = status

        viewModelScope.launch {
            _listState.value = ServiceRequestUiState.Loading
            repository.getServiceRequests(page = page, search = search, status = status)
                .onSuccess { response ->
                    val items = response.data?.map { it.toServiceRequestData() } ?: emptyList()
                    _listState.value = ServiceRequestUiState.Success(
                        items = items,
                        total = response.pagination?.total ?: items.size
                    )
                }
                .onFailure { error ->
                    _listState.value = ServiceRequestUiState.Error(error.localizedMessage ?: "Failed to load requests")
                }
        }
    }

    fun searchRequests(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            loadServiceRequests(page = 1, search = query.ifBlank { null }, status = currentStatus)
        }
    }

    fun loadServiceRequestById(id: String) {
        viewModelScope.launch {
            _detailState.value = ServiceDetailUiState.Loading
            repository.getServiceRequestById(id)
                .onSuccess { data ->
                    _detailState.value = ServiceDetailUiState.Success(data)
                }
                .onFailure { error ->
                    _detailState.value = ServiceDetailUiState.Error(error.localizedMessage ?: "Unable to fetch details")
                }
        }
    }

    fun createServiceRequest(payload: CreateServiceRequestPayload, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _actionState.value = ServiceActionUiState.Loading
            repository.createServiceRequest(payload)
                .onSuccess {
                    _actionState.value = ServiceActionUiState.Success("Service request created successfully.")
                    loadServiceRequests(page = 1)
                    onSuccess()
                }
                .onFailure { error ->
                    _actionState.value = ServiceActionUiState.Error(error.localizedMessage ?: "Creation failed")
                }
        }
    }

    fun updateServiceStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = ServiceActionUiState.Loading
            repository.updateServiceRequestStatus(id, status)
                .onSuccess {
                    _actionState.value = ServiceActionUiState.Success("Status updated to $status.")
                    loadServiceRequestById(id)
                    loadServiceRequests(page = currentPage)
                }
                .onFailure { error ->
                    _actionState.value = ServiceActionUiState.Error(error.localizedMessage ?: "Status update failed")
                }
        }
    }
    private val _alterationList = MutableStateFlow<List<AlterationItemDto>>(emptyList())
    val alterationList: StateFlow<List<AlterationItemDto>> = _alterationList.asStateFlow()

    private val _alterationQuery = MutableStateFlow("")
    val alterationQuery: StateFlow<String> = _alterationQuery.asStateFlow()

    private val _isAlterationsLoading = MutableStateFlow(false)
    val isAlterationsLoading: StateFlow<Boolean> = _isAlterationsLoading.asStateFlow()

    fun fetchAlterations(search: String? = null) {
        viewModelScope.launch {
            _isAlterationsLoading.value = true
            val result = repo.request<AlterationListResponse> {
                get(
                    SERVICE_ALTERATION_VIEW_ALL_URL,
                    repo.query(
                        "page" to 1,
                        "limit" to 50,
                        "search" to search,
                        "primaryCategory" to "Alteration"
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _alterationList.value = res.data.orEmpty()
                },
                onFailure = {
                    _alterationList.value = emptyList()
                }
            )
            _isAlterationsLoading.value = false
        }
    }

    fun onAlterationSearch(query: String) {
        _alterationQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            fetchAlterations(search = query.ifBlank { null })
        }
    }

    // Add inside ServicesViewModel:
    private val _alterationDetail = MutableStateFlow<AlterationViewData?>(null)
    val alterationDetail: StateFlow<AlterationViewData?> = _alterationDetail.asStateFlow()

    private val _isLoadingAlterationDetail = MutableStateFlow(false)
    val isLoadingAlterationDetail: StateFlow<Boolean> = _isLoadingAlterationDetail.asStateFlow()

    private val _alterationDetailError = MutableStateFlow<String?>(null)
    val alterationDetailError: StateFlow<String?> = _alterationDetailError.asStateFlow()

    /**
     * Fetches single alteration ticket detail by ticket/service request ID
     */
    fun fetchAlterationDetail(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isLoadingAlterationDetail.value = true
            _alterationDetailError.value = null

            val result = repo.request<SingleAlterationResponse> {
                get("$SERVICE_ALTERATION_VIEW_ONE_URL/$id")
            }

            result.fold(
                onSuccess = { res ->
                    _alterationDetail.value = res.data
                },
                onFailure = { error ->
                    _alterationDetailError.value = error.message ?: "Failed to load alteration details"
                }
            )
            _isLoadingAlterationDetail.value = false
        }
    }
    fun clearAlterationDetail() {
        _alterationDetail.value = null
        _alterationDetailError.value = null
    }

    private val _isDelivering = MutableStateFlow(false)
    val isDelivering: StateFlow<Boolean> = _isDelivering.asStateFlow()

    /**
     * Updates service request status to 'Delivered'
     */
    fun markAlterationAsDelivered(
        id: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isDelivering.value = true
            val payload = DeliverAlterationRequest(
                status = "Delivered",
                allowWithoutAdvance = true
            )

            val result = repo.request<DeliverAlterationResponse> {
                patch("$SERVICE_ALTERATION_UPDATE_STATUS_URL/$id", payload)
            }

            result.fold(
                onSuccess = { res ->
                    _isDelivering.value = false
                    // Refresh current view details
                    fetchAlterationDetail(id)
                    onSuccess(res.message ?: "Service request status updated to Delivered.")
                },
                onFailure = { err ->
                    _isDelivering.value = false
                    onError(err.message ?: "Failed to update status to Delivered")
                }
            )
        }
    }

    private val _isSendingComment = MutableStateFlow(false)
    val isSendingComment: StateFlow<Boolean> = _isSendingComment.asStateFlow()

    /**
     * Submits a staff observation or update comment to the service request
     */
    fun addAlterationComment(
        id: String,
        commentText: String,
        onSuccess: (AlterationCommentItem?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (id.isBlank() || commentText.isBlank()) return
        viewModelScope.launch {
            _isSendingComment.value = true
            val payload = AddAlterationCommentRequest(commentText = commentText.trim())

            val result = repo.request<AddAlterationCommentResponse> {
                post("$SERVICE_ALTERATION_COMMENT_URL/$id", payload)
            }

            result.fold(
                onSuccess = { res ->
                    _isSendingComment.value = false
                    // Refresh alteration ticket data to reflect logs
                    fetchAlterationDetail(id)
                    onSuccess(res.data?.comment)
                },
                onFailure = { err ->
                    _isSendingComment.value = false
                    onError(err.message ?: "Failed to post comment")
                }
            )
        }
    }


    private val _complaintsList = MutableStateFlow<List<ComplaintItemDto>>(emptyList())
    val complaintsList: StateFlow<List<ComplaintItemDto>> = _complaintsList.asStateFlow()

    private val _complaintSearchQuery = MutableStateFlow("")
    val complaintSearchQuery: StateFlow<String> = _complaintSearchQuery.asStateFlow()

    private val _isLoadingComplaints = MutableStateFlow(false)
    val isLoadingComplaints: StateFlow<Boolean> = _isLoadingComplaints.asStateFlow()

    private val _complaintsError = MutableStateFlow<String?>(null)
    val complaintsError: StateFlow<String?> = _complaintsError.asStateFlow()

    private var complaintSearchJob: Job? = null

    /**
     * Fetches complaints list with optional search query
     */
    fun fetchComplaints(search: String? = null) {
        viewModelScope.launch {
            _isLoadingComplaints.value = true
            _complaintsError.value = null

            val result = repo.request<ComplaintListResponse> {
                get(
                    COMPLAINTS_VIEW_ALL_URL,
                    repo.query(
                        "page" to 1,
                        "limit" to 50,
                        "search" to search
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _complaintsList.value = res.data.orEmpty()
                },
                onFailure = { err ->
                    _complaintsError.value = err.message ?: "Failed to load complaints"
                    _complaintsList.value = emptyList()
                }
            )
            _isLoadingComplaints.value = false
        }
    }

    fun onComplaintSearch(query: String) {
        _complaintSearchQuery.value = query
        complaintSearchJob?.cancel()
        complaintSearchJob = viewModelScope.launch {
            delay(350)
            fetchComplaints(search = query.ifBlank { null })
        }
    }

    // ── Complaint Detail View State ──
    private val _complaintDetail = MutableStateFlow<ComplaintDetailDataDto?>(null)
    val complaintDetail: StateFlow<ComplaintDetailDataDto?> = _complaintDetail.asStateFlow()

    private val _isLoadingComplaintDetail = MutableStateFlow(false)
    val isLoadingComplaintDetail: StateFlow<Boolean> = _isLoadingComplaintDetail.asStateFlow()

    private val _complaintDetailError = MutableStateFlow<String?>(null)
    val complaintDetailError: StateFlow<String?> = _complaintDetailError.asStateFlow()

    /**
     * Fetches complaint details by ID from the API
     */
    fun fetchComplaintDetail(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isLoadingComplaintDetail.value = true
            _complaintDetailError.value = null

            val result = repo.request<SingleComplaintResponse> {
                get("/api/services/complaints/view-one/$id")
            }

            result.fold(
                onSuccess = { res ->
                    _complaintDetail.value = res.data
                },
                onFailure = { err ->
                    _complaintDetailError.value = err.message ?: "Failed to load complaint details"
                }
            )
            _isLoadingComplaintDetail.value = false
        }
    }

    private val _feedbacksList = MutableStateFlow<List<FeedbackItemDto>>(emptyList())
    val feedbacksList: StateFlow<List<FeedbackItemDto>> = _feedbacksList.asStateFlow()

    private val _feedbackSearchQuery = MutableStateFlow("")
    val feedbackSearchQuery: StateFlow<String> = _feedbackSearchQuery.asStateFlow()

    private val _isLoadingFeedbacks = MutableStateFlow(false)
    val isLoadingFeedbacks: StateFlow<Boolean> = _isLoadingFeedbacks.asStateFlow()

    private val _feedbacksError = MutableStateFlow<String?>(null)
    val feedbacksError: StateFlow<String?> = _feedbacksError.asStateFlow()

    private var feedbackSearchJob: Job? = null

    /**
     * Fetches feedback list with search query support
     */
    fun fetchFeedbacks(search: String? = null) {
        viewModelScope.launch {
            _isLoadingFeedbacks.value = true
            _feedbacksError.value = null

            val result = repo.request<FeedbackListResponse> {
                get(
                    FEEDBACKS_VIEW_ALL_URL,
                    repo.query(
                        "page" to 1,
                        "limit" to 50,
                        "search" to search
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _feedbacksList.value = res.data.orEmpty()
                },
                onFailure = { err ->
                    _feedbacksError.value = err.message ?: "Failed to load feedbacks"
                    _feedbacksList.value = emptyList()
                }
            )
            _isLoadingFeedbacks.value = false
        }
    }

    // ── Upload Attachment States ──
    private val _isUploadingAttachment = MutableStateFlow(false)
    val isUploadingAttachment: StateFlow<Boolean> = _isUploadingAttachment.asStateFlow()

    private val _uploadAttachmentError = MutableStateFlow<String?>(null)
    val uploadAttachmentError: StateFlow<String?> = _uploadAttachmentError.asStateFlow()

    /**
     * Uploads evidence attachment files to the complaint ticket and automatically refreshes view-one
     */
    fun uploadComplaintAttachment(
        complaintId: String,
        imagePart: okhttp3.MultipartBody.Part,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (complaintId.isBlank()) return
        viewModelScope.launch {
            _isUploadingAttachment.value = true
            _uploadAttachmentError.value = null

            val result = repo.request<UploadComplaintAttachmentResponse> {
                postMultipart(
                    url = "$COMPLAINT_ATTACHMENT_UPLOAD_URL/$complaintId",
                    files = listOf(imagePart)
                )
            }

            result.fold(
                onSuccess = { res ->
                    _isUploadingAttachment.value = false
                    // Update current detail directly or re-fetch to refresh view-one
                    if (res.data != null) {
                        _complaintDetail.value = res.data
                    } else {
                        fetchComplaintDetail(complaintId)
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _isUploadingAttachment.value = false
                    val errorMsg = error.message ?: "Failed to upload attachment"
                    _uploadAttachmentError.value = errorMsg
                    onError(errorMsg)
                }
            )
        }
    }

    // Inside ServicesViewModel class:
    private val _feedbackDetail = MutableStateFlow<FeedbackDetailDataDto?>(null)
    val feedbackDetail: StateFlow<FeedbackDetailDataDto?> = _feedbackDetail.asStateFlow()

    private val _isLoadingFeedbackDetail = MutableStateFlow(false)
    val isLoadingFeedbackDetail: StateFlow<Boolean> = _isLoadingFeedbackDetail.asStateFlow()

    private val _feedbackDetailError = MutableStateFlow<String?>(null)
    val feedbackDetailError: StateFlow<String?> = _feedbackDetailError.asStateFlow()

    /**
     * Fetches single feedback record by ID
     */
    fun fetchFeedbackDetail(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isLoadingFeedbackDetail.value = true
            _feedbackDetailError.value = null

            val result = repo.request<SingleFeedbackResponse> {
                get("$FEEDBACK_VIEW_ONE_URL/$id")
            }

            result.fold(
                onSuccess = { res ->
                    _feedbackDetail.value = res.data
                },
                onFailure = { error ->
                    _feedbackDetailError.value = error.message ?: "Failed to load feedback details"
                }
            )
            _isLoadingFeedbackDetail.value = false
        }
    }

    private val _isUpdatingComplaintStatus = MutableStateFlow<Boolean>(false)
    val isUpdatingComplaintStatus: StateFlow<Boolean> = _isUpdatingComplaintStatus.asStateFlow()

    private val _updateComplaintStatusError = MutableStateFlow<String?>(null)
    val updateComplaintStatusError: StateFlow<String?> = _updateComplaintStatusError.asStateFlow()

    /**
     * Updates complaint status using PATCH /api/services/complaints/update/{id}
     */
    fun updateComplaintStatus(
        complaintId: String,
        status: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        android.util.Log.d("UPDATE_COMPLAINT", "updateComplaintStatus called with ID: '$complaintId', Status: '$status'")

        if (complaintId.isBlank()) {
            android.util.Log.e("UPDATE_COMPLAINT", "Error: complaintId is blank!")
            onError("Invalid complaint ticket ID")
            return
        }

        viewModelScope.launch {
            _isUpdatingComplaintStatus.value = true
            _updateComplaintStatusError.value = null

            // Exact backend enum mapping
            val backendStatus = when (status.trim().lowercase()) {
                "open" -> "Open"
                "in investigation / in progress", "in investigation", "in progress" -> "In_Investigation"
                "pending customer response" -> "Pending_Customer_Response"
                "resolution approved" -> "Resolution_Approved"
                "resolved" -> "Resolved"
                "closed" -> "Closed"
                "rejected" -> "Rejected"
                else -> status.trim().replace(" ", "_")
            }

            val payload = UpdateComplaintStatusRequest(status = backendStatus)
            android.util.Log.d("UPDATE_COMPLAINT", "Sending PATCH to $COMPLAINT_UPDATE_URL/$complaintId with payload: $payload")

            val result = repo.request<UpdateComplaintStatusResponse> {
                put("$COMPLAINT_UPDATE_URL/$complaintId", payload)
            }

            result.fold(
                onSuccess = { res ->
                    _isUpdatingComplaintStatus.value = false
                    android.util.Log.d("UPDATE_COMPLAINT", "Update SUCCESS: ${res.message}, new status: ${res.data?.status}")

                    // Immediately update UI with returned response data or fetch
                    if (res.data != null) {
                        _complaintDetail.value = res.data
                    } else {
                        fetchComplaintDetail(complaintId)
                    }
                    onSuccess(res.message ?: "Complaint updated successfully.")
                },
                onFailure = { error ->
                    _isUpdatingComplaintStatus.value = false
                    val errorMsg = error.message ?: "Failed to update complaint status"
                    android.util.Log.e("UPDATE_COMPLAINT", "Update FAILED: $errorMsg", error)
                    _updateComplaintStatusError.value = errorMsg
                    onError(errorMsg)
                }
            )
        }
    }

    // ── Assign Staff States ──
    private val _isAssigningStaff = MutableStateFlow<Boolean>(false)
    val isAssigningStaff: StateFlow<Boolean> = _isAssigningStaff.asStateFlow()

    private val _assignStaffError = MutableStateFlow<String?>(null)
    val assignStaffError: StateFlow<String?> = _assignStaffError.asStateFlow()

    /**
     * Assigns operations staff to a complaint ticket
     * Endpoint: PATCH /api/services/complaints/update-one/{complaintId}
     * Payload: { "assignedTo": "<memberId>" }
     */
    fun assignComplaintStaff(
        complaintId: String,
        memberId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (complaintId.isBlank()) {
            onError("Invalid Complaint ID")
            return
        }
        if (memberId.isBlank()) {
            onError("Please select a staff member")
            return
        }

        viewModelScope.launch {
            _isAssigningStaff.value = true
            _assignStaffError.value = null

            val payload = AssignComplaintStaffRequest(assignedTo = memberId)

            val result = repo.request<AssignComplaintStaffResponse> {
                put("$COMPLAINT_UPDATE_URL/$complaintId", payload)
            }

            result.fold(
                onSuccess = { res ->
                    _isAssigningStaff.value = false
                    // Update current detail in state or refresh view-one
                    if (res.data != null) {
                        _complaintDetail.value = res.data
                    } else {
                        fetchComplaintDetail(complaintId)
                    }
                    onSuccess(res.message ?: "Staff assigned successfully.")
                },
                onFailure = { error ->
                    _isAssigningStaff.value = false
                    val errorMsg = error.message ?: "Failed to assign staff"
                    _assignStaffError.value = errorMsg
                    onError(errorMsg)
                }
            )
        }
    }

    // ── Add Internal Note States ──
    private val _isSavingInternalNote = MutableStateFlow<Boolean>(false)
    val isSavingInternalNote: StateFlow<Boolean> = _isSavingInternalNote.asStateFlow()

    private val _saveInternalNoteError = MutableStateFlow<String?>(null)
    val saveInternalNoteError: StateFlow<String?> = _saveInternalNoteError.asStateFlow()

    /**
     * Adds/Updates internal investigation notes for a complaint ticket
     * Endpoint: PATCH /api/services/complaints/update-one/{complaintId}
     * Payload: { "investigationNotes": "..." }
     */
    fun addComplaintInternalNote(
        complaintId: String,
        note: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (complaintId.isBlank()) {
            onError("Invalid Complaint ID")
            return
        }
        if (note.trim().isBlank()) {
            onError("Please enter a note")
            return
        }

        viewModelScope.launch {
            _isSavingInternalNote.value = true
            _saveInternalNoteError.value = null

            val payload = AddComplaintInternalNoteRequest(investigationNotes = note.trim())

            val result = repo.request<AddComplaintInternalNoteResponse> {
                put("$COMPLAINT_UPDATE_URL/$complaintId", payload)
            }

            result.fold(
                onSuccess = { res ->
                    _isSavingInternalNote.value = false
                    // Update state directly or refresh view-one
                    if (res.data != null) {
                        _complaintDetail.value = res.data
                    } else {
                        fetchComplaintDetail(complaintId)
                    }
                    onSuccess(res.message ?: "Internal note saved successfully.")
                },
                onFailure = { error ->
                    _isSavingInternalNote.value = false
                    val errorMsg = error.message ?: "Failed to save internal note"
                    _saveInternalNoteError.value = errorMsg
                    onError(errorMsg)
                }
            )
        }
    }

    private val _isResolvingComplaint = MutableStateFlow<Boolean>(false)
    val isResolvingComplaint: StateFlow<Boolean> = _isResolvingComplaint.asStateFlow()

    private val _resolveComplaintError = MutableStateFlow<String?>(null)
    val resolveComplaintError: StateFlow<String?> = _resolveComplaintError.asStateFlow()

    /**
     * Resolves a complaint ticket with the exact payload structure:
     * {
     *   "action": "Garment_Remake",
     *   "compensationAmount": 0,
     *   "isCustomerAgreed": true,
     *   "resolutionSummary": "qwdqwd",
     *   "voucherCode": ""
     * }
     */
    fun resolveComplaint(
        complaintId: String,
        actionId: String,
        summary: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (complaintId.isBlank()) {
            onError("Invalid Complaint ID")
            return
        }
        if (summary.trim().isBlank()) {
            onError("Please enter a resolution summary")
            return
        }

        viewModelScope.launch {
            _isResolvingComplaint.value = true
            _resolveComplaintError.value = null

            // Exact JSON payload construction
            val payload = ResolveComplaintRequest(
                action = actionId,
                compensationAmount = 0,
                isCustomerAgreed = true,
                resolutionSummary = summary.trim(),
                voucherCode = ""
            )

            android.util.Log.d("RESOLVE_COMPLAINT", "Calling PUT to $COMPLAINT_RESOLVE_URL/$complaintId with payload: $payload")

            val result = repo.request<ResolveComplaintResponse> {
                patch("$COMPLAINT_RESOLVE_URL/$complaintId", payload)
            }

            result.fold(
                onSuccess = { res ->
                    _isResolvingComplaint.value = false
                    android.util.Log.d("RESOLVE_COMPLAINT", "Success: ${res.message}, status=${res.data?.status}")

                    // Immediately update UI state with returned data or re-fetch
                    if (res.data != null) {
                        _complaintDetail.value = res.data
                    } else {
                        fetchComplaintDetail(complaintId)
                    }
                    onSuccess(res.message ?: "Complaint resolved successfully.")
                },
                onFailure = { error ->
                    _isResolvingComplaint.value = false
                    val errorMsg = error.message ?: "Failed to resolve complaint"
                    android.util.Log.e("RESOLVE_COMPLAINT", "Error: $errorMsg", error)
                    _resolveComplaintError.value = errorMsg
                    onError(errorMsg)
                }
            )
        }
    }

    /**
     * Updates Job Card stage progress (e.g., Mark Complete, Start Work, Put on Hold)
     * Endpoint: PATCH /api/services/production/job-cards/update-stage/{jobCardId}
     */
    fun updateStageProgress(
        jobCardId: String,
        stageId: String,
        status: String,
        completedQuantity: Int? = null,
        defectiveQuantity: Int? = null,
        completedAt: String? = null,
        startedAt: String? = null,
        holdReason: String? = null,
        notes: String? = null,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (jobCardId.isBlank() || stageId.isBlank()) {
            onError("Invalid Job Card or Stage ID")
            return
        }

        viewModelScope.launch {
            _isUpdatingStageProgress.value = true
            _updateStageProgressError.value = null

            // Construct payload matching Screenshot 1
            val payload = UpdateStageProgressRequestComplete(
                stageId = stageId,
                status = status,
                completedQuantity = completedQuantity,
                defectiveQuantity = defectiveQuantity,
                completedAt = completedAt,
                startedAt = startedAt,
                holdReason = holdReason,
                notes = notes?.trim()
            )

            val result = repo.request<UpdateStageProgressResponse> {
                patch("$UPDATE_STAGE_PROGRESS_URL/$jobCardId", payload)
            }

            result.fold(
                onSuccess = { res ->
                    _isUpdatingStageProgress.value = false
                    if (res.data != null) {
                        _jobCardDetail.value = res.data
                    }
                    onSuccess(res.message ?: "Stage progress successfully updated.")
                },
                onFailure = { error ->
                    _isUpdatingStageProgress.value = false
                    val errorMsg = error.message ?: "Failed to update stage progress"
                    _updateStageProgressError.value = errorMsg
                    onError(errorMsg)
                }
            )
        }
    }

    /**
     * Logs an issue against a job card
     * Endpoint: POST /api/services/production/job-cards/log-issue/{jobCardId}
     */
    fun logIssueToJobCard(
        jobCardId: String,
        issueType: String,
        description: String,
        affectedQuantity: Int = 1,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (jobCardId.isBlank() || description.isBlank()) {
            onError("Description cannot be empty")
            return
        }

        viewModelScope.launch {
            // Construct payload matching Screenshot 2
            val payload = LogIssueRequest(
                issueType = issueType,
                description = description.trim(),
                affectedQuantity = affectedQuantity
            )

            val result = repo.request<LogIssueResponse> {
                post("$LOG_ISSUE_TO_JOB_CARD_URL/$jobCardId", payload)
            }

            result.fold(
                onSuccess = { res ->
                    onSuccess(res.message ?: "Issue successfully logged against job card.")
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed to log floor issue")
                }
            )
        }
    }

    fun logFabricWastage(
        jobCardId: String,
        totalIssuedMeters: Double,
        actualGarmentMeters: Double,
        scrapType: String,
        notes: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (jobCardId.isBlank()) return
        viewModelScope.launch {
            val payload = mapOf(
                "totalIssuedMeters" to totalIssuedMeters,
                "actualGarmentMeters" to actualGarmentMeters,
                "scrapType" to scrapType,
                "notes" to notes.trim()
            )
            val result = repo.request<GenericApiResponse> {
                post("$LOG_FABRIC_WASTAGE_URL/$jobCardId", payload)
            }
            result.fold(
                onSuccess = { res ->
                    onSuccess(res.message ?: "Fabric scrap logged successfully!")
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed to log fabric scrap")
                }
            )
        }
    }
    fun clearFeedbackDetail() {
        _feedbackDetail.value = null
        _feedbackDetailError.value = null
    }

    fun onFeedbackSearch(query: String) {
        _feedbackSearchQuery.value = query
        feedbackSearchJob?.cancel()
        feedbackSearchJob = viewModelScope.launch {
            delay(350)
            fetchFeedbacks(search = query.ifBlank { null })
        }
    }

    fun clearComplaintDetail() {
        _complaintDetail.value = null
        _complaintDetailError.value = null
    }

    fun resetActionState() {
        _actionState.value = ServiceActionUiState.Idle
    }
}