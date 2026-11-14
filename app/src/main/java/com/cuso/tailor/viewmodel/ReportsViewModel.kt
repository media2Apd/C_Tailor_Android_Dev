@file:Suppress("unused")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.report.*
import com.cuso.tailor.repository.ApiRepository
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.PagingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.roundToInt

// --- HR Report Endpoints ---
private const val ATTENDANCE_ANALYTICS_URL = "/api/hr/attendance/dashboard/analytics"
private const val ATTENDANCE_RECENT_URL = "/api/hr/attendance/dashboard/recent"

private const val PAYROLL_ANALYTICS_URL = "/api/hr/payroll-runs/dashboard/analytics"
private const val PAYROLL_RECENT_URL = "/api/hr/payroll-runs/dashboard/recent"

private const val PIECE_RATE_ANALYTICS_URL = "/api/hr/piece-rates/dashboard/analytics"
private const val PIECE_RATE_RECENT_URL = "/api/hr/piece-rates/dashboard/recent"

private const val REIMBURSEMENT_ANALYTICS_URL = "/api/hr/reimbursements/dashboard/analytics"
private const val REIMBURSEMENT_RECENT_URL = "/api/hr/reimbursements/dashboard/recent"

// --- IT Report Endpoints ---
private const val IT_ASSETS_REPORT_URL = "/api/it/reports/it-assets"
private const val IT_SERVICE_DESK_REPORT_URL = "/api/it/reports/service-desk"

// --- Security Report Endpoints ---
private const val SECURITY_INCIDENTS_REPORT_URL = "/api/security/reports/incidents-alerts"
private const val SECURITY_ACCESS_REPORT_URL = "/api/security/reports/access-activity"
private const val SECURITY_API_INTEGRATIONS_REPORT_URL = "/api/security/reports/api-integrations"

// --- Sales Report Endpoints ---
private const val SALES_LEAD_MASTER_URL = "/api/sales/reports/leads/master-register"
private const val SALES_LEAD_SOURCE_PERF_URL = "/api/sales/reports/leads/source-performance"
private const val SALES_LEAD_OVERDUE_URL = "/api/sales/reports/leads/overdue-followups"
private const val SALES_LEAD_FOLLOWUP_ACTIVITY_URL = "/api/sales/reports/leads/followup-activity"

private const val SALES_OPP_MASTER_URL = "/api/sales/reports/opportunities/master-register"
private const val SALES_OPP_STAGE_FUNNEL_URL = "/api/sales/reports/opportunities/stage-funnel"
private const val SALES_OPP_CLOSING_ALERTS_URL = "/api/sales/reports/opportunities/closing-alerts"

private const val SALES_CUST_MASTER_URL = "/api/sales/reports/customers/master-register"
private const val SALES_CUST_LTV_URL = "/api/sales/reports/customers/ltv-revenue"
private const val SALES_CUST_CREDIT_URL = "/api/sales/reports/customers/credit-receivables"
private const val SALES_CUST_DORMANT_URL = "/api/sales/reports/customers/dormant-winback"

private const val SALES_MEAS_MASTER_URL = "/api/sales/reports/measurements/master-register"
private const val SALES_MEAS_STALE_URL = "/api/sales/reports/measurements/stale-audit"

private const val SALES_ORDER_MASTER_URL = "/api/sales/reports/orders/master-register"
private const val SALES_ORDER_PRODUCT_STREAM_URL = "/api/sales/reports/orders/product-stream"
private const val SALES_ORDER_DELIVERY_SLA_URL = "/api/sales/reports/orders/delivery-sla"
private const val SALES_BRANCH_WISE_URL = "/api/sales/reports/branch-wise-sales"

private const val SALES_QUOTE_MASTER_URL = "/api/sales/reports/quotations/master-register"
private const val SALES_QUOTE_DISCOUNT_URL = "/api/sales/reports/quotations/discount-leakage"
private const val SALES_QUOTE_EXPIRING_URL = "/api/sales/reports/quotations/expiring-audit"

private const val DEFAULT_PAGE_LIMIT = 10

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repo: ApiRepository
) : ViewModel() {

    // =========================================================================
    // 1. ATTENDANCE REGISTER & ANALYTICS REPORT
    // =========================================================================

    private val _attendanceAnalytics = MutableStateFlow<AttendanceAnalyticsData?>(null)
    val attendanceAnalytics: StateFlow<AttendanceAnalyticsData?> = _attendanceAnalytics.asStateFlow()

    private val _isLoadingAttendanceAnalytics = MutableStateFlow(false)
    val isLoadingAttendanceAnalytics: StateFlow<Boolean> = _isLoadingAttendanceAnalytics.asStateFlow()

    private val _attendancePagingState = MutableStateFlow(PagingUiState<AttendanceRecordDto>())
    val attendancePagingState: StateFlow<PagingUiState<AttendanceRecordDto>> = _attendancePagingState.asStateFlow()

    private val _attendanceSearchQuery = MutableStateFlow("")
    val attendanceSearchQuery: StateFlow<String> = _attendanceSearchQuery.asStateFlow()

    private val _attendanceStatusFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _attendanceModeFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _attendanceLateOnlyFilter = MutableStateFlow(false)

    private var attendanceCurrentPage = 1

    val filteredAttendanceList: StateFlow<List<AttendanceRecordDto>> = combine(
        _attendancePagingState,
        _attendanceSearchQuery,
        _attendanceStatusFilter,
        _attendanceModeFilter,
        _attendanceLateOnlyFilter
    ) { paging, query, statuses, modes, lateOnly ->
        val cleanQuery = query.trim().lowercase()
        paging.items.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.employeeName.lowercase().contains(cleanQuery) ||
                    item.employeeCode.lowercase().contains(cleanQuery) ||
                    item.status.lowercase().contains(cleanQuery) ||
                    item.workMode.lowercase().contains(cleanQuery)

            val matchesStatus = statuses.isEmpty() || statuses.any { it.equals(item.status, ignoreCase = true) }
            val matchesMode = modes.isEmpty() || modes.any { it.equals(item.workMode, ignoreCase = true) }
            val matchesLate = !lateOnly || item.isLate

            matchesQuery && matchesStatus && matchesMode && matchesLate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadAttendanceReport() {
        viewModelScope.launch {
            coroutineScope {
                val analyticsJob = async { fetchAttendanceAnalyticsInternal() }
                val recentJob = async { fetchAttendanceRecentInternal(isLoadMore = false) }
                analyticsJob.await()
                recentJob.await()
            }
        }
    }

    fun loadMoreAttendanceRecords() {
        viewModelScope.launch {
            fetchAttendanceRecentInternal(isLoadMore = true)
        }
    }

    fun onAttendanceSearchQueryChange(query: String) {
        _attendanceSearchQuery.value = query
    }

    fun applyAttendanceFilters(
        statuses: Set<String>,
        workModes: Set<String>,
        lateOnly: Boolean
    ) {
        _attendanceStatusFilter.value = statuses
        _attendanceModeFilter.value = workModes
        _attendanceLateOnlyFilter.value = lateOnly
    }

    fun clearAttendanceFilters() {
        _attendanceSearchQuery.value = ""
        _attendanceStatusFilter.value = emptySet()
        _attendanceModeFilter.value = emptySet()
        _attendanceLateOnlyFilter.value = false
    }

    private suspend fun fetchAttendanceAnalyticsInternal() {
        _isLoadingAttendanceAnalytics.value = true
        val res = repo.request<AttendanceAnalyticsResponse> {
            get(ATTENDANCE_ANALYTICS_URL)
        }
        res.fold(
            onSuccess = { response ->
                if (response.success && response.data != null) {
                    _attendanceAnalytics.value = response.data
                } else {
                    DynamicIslandManager.showError(response.message ?: "Failed to load attendance analytics")
                }
            },
            onFailure = { err ->
                DynamicIslandManager.showError(err.message ?: "Network error loading attendance analytics")
            }
        )
        _isLoadingAttendanceAnalytics.value = false
    }

    private suspend fun fetchAttendanceRecentInternal(isLoadMore: Boolean) {
        val currentState = _attendancePagingState.value
        if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            _attendancePagingState.value = currentState.copy(isLoadingMore = true)
            attendanceCurrentPage++
        } else {
            attendanceCurrentPage = 1
            _attendancePagingState.value = currentState.copy(isInitialLoading = true)
        }

        val res = repo.request<AttendanceRecentResponse> {
            get(
                ATTENDANCE_RECENT_URL,
                repo.query("page" to attendanceCurrentPage, "limit" to DEFAULT_PAGE_LIMIT)
            )
        }

        res.fold(
            onSuccess = { response ->
                if (response.success) {
                    val incoming = response.data
                    val existing = if (isLoadMore) _attendancePagingState.value.items else emptyList()
                    val existingIds = existing.map { it.id }.toSet()
                    val uniqueNew = incoming.filter { it.id !in existingIds }
                    val updatedList = existing + uniqueNew

                    val canLoadMore = response.pagination?.let {
                        attendanceCurrentPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    _attendancePagingState.value = _attendancePagingState.value.copy(
                        items = updatedList,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) attendanceCurrentPage = (attendanceCurrentPage - 1).coerceAtLeast(1)
                    _attendancePagingState.value = _attendancePagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(response.message ?: "Failed to load attendance records")
                }
            },
            onFailure = { err ->
                if (isLoadMore) attendanceCurrentPage = (attendanceCurrentPage - 1).coerceAtLeast(1)
                _attendancePagingState.value = _attendancePagingState.value.copy(
                    isInitialLoading = false,
                    isLoadingMore = false
                )
                DynamicIslandManager.showError(err.message ?: "Network error loading attendance records")
            }
        )
    }

    // =========================================================================
    // 2. PAYROLL RUN AUDIT & ANALYTICS REPORT
    // =========================================================================

    private val _payrollAnalytics = MutableStateFlow<PayrollRunAnalyticsData?>(null)
    val payrollAnalytics: StateFlow<PayrollRunAnalyticsData?> = _payrollAnalytics.asStateFlow()

    private val _isLoadingPayrollAnalytics = MutableStateFlow(false)
    val isLoadingPayrollAnalytics: StateFlow<Boolean> = _isLoadingPayrollAnalytics.asStateFlow()

    private val _payrollPagingState = MutableStateFlow(PagingUiState<PayrollRunRecordDto>())
    val payrollPagingState: StateFlow<PagingUiState<PayrollRunRecordDto>> = _payrollPagingState.asStateFlow()

    private val _payrollSearchQuery = MutableStateFlow("")
    val payrollSearchQuery: StateFlow<String> = _payrollSearchQuery.asStateFlow()

    private val _payrollStatusFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _payrollFrequencyFilter = MutableStateFlow<Set<String>>(emptySet())

    private var payrollCurrentPage = 1

    val filteredPayrollList: StateFlow<List<PayrollRunRecordDto>> = combine(
        _payrollPagingState,
        _payrollSearchQuery,
        _payrollStatusFilter,
        _payrollFrequencyFilter
    ) { paging, query, statuses, frequencies ->
        val cleanQuery = query.trim().lowercase()
        paging.items.filter { item ->
            val periodLabel = "${item.payrollMonth}/${item.payrollYear}"
            val creatorName = item.createdBy?.fullName.orEmpty().lowercase()
            val matchesQuery = cleanQuery.isEmpty() ||
                    periodLabel.contains(cleanQuery) ||
                    item.payFrequency.lowercase().contains(cleanQuery) ||
                    item.status.lowercase().contains(cleanQuery) ||
                    creatorName.contains(cleanQuery)

            val matchesStatus = statuses.isEmpty() || statuses.any { it.equals(item.status, ignoreCase = true) }
            val matchesFreq = frequencies.isEmpty() || frequencies.any { it.equals(item.payFrequency, ignoreCase = true) }

            matchesQuery && matchesStatus && matchesFreq
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPayrollRunReport() {
        viewModelScope.launch {
            coroutineScope {
                val analyticsJob = async { fetchPayrollAnalyticsInternal() }
                val recentJob = async { fetchPayrollRecentInternal(isLoadMore = false) }
                analyticsJob.await()
                recentJob.await()
            }
        }
    }

    fun loadMorePayrollRuns() {
        viewModelScope.launch {
            fetchPayrollRecentInternal(isLoadMore = true)
        }
    }

    fun onPayrollSearchQueryChange(query: String) {
        _payrollSearchQuery.value = query
    }

    fun applyPayrollFilters(statuses: Set<String>, frequencies: Set<String>) {
        _payrollStatusFilter.value = statuses
        _payrollFrequencyFilter.value = frequencies
    }

    fun clearPayrollFilters() {
        _payrollSearchQuery.value = ""
        _payrollStatusFilter.value = emptySet()
        _payrollFrequencyFilter.value = emptySet()
    }

    private suspend fun fetchPayrollAnalyticsInternal() {
        _isLoadingPayrollAnalytics.value = true
        val res = repo.request<PayrollRunAnalyticsResponse> {
            get(PAYROLL_ANALYTICS_URL, repo.query("limit" to DEFAULT_PAGE_LIMIT))
        }
        res.fold(
            onSuccess = { response ->
                if (response.success && response.data != null) {
                    _payrollAnalytics.value = response.data
                } else {
                    DynamicIslandManager.showError(response.message ?: "Failed to load payroll analytics")
                }
            },
            onFailure = { err ->
                DynamicIslandManager.showError(err.message ?: "Network error loading payroll analytics")
            }
        )
        _isLoadingPayrollAnalytics.value = false
    }

    private suspend fun fetchPayrollRecentInternal(isLoadMore: Boolean) {
        val currentState = _payrollPagingState.value
        if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            _payrollPagingState.value = currentState.copy(isLoadingMore = true)
            payrollCurrentPage++
        } else {
            payrollCurrentPage = 1
            _payrollPagingState.value = currentState.copy(isInitialLoading = true)
        }

        val res = repo.request<PayrollRunRecentResponse> {
            get(
                PAYROLL_RECENT_URL,
                repo.query("page" to payrollCurrentPage, "limit" to DEFAULT_PAGE_LIMIT)
            )
        }

        res.fold(
            onSuccess = { response ->
                if (response.success) {
                    val incoming = response.data
                    val existing = if (isLoadMore) _payrollPagingState.value.items else emptyList()
                    val existingIds = existing.map { it.id }.toSet()
                    val uniqueNew = incoming.filter { it.id !in existingIds }
                    val updatedList = existing + uniqueNew

                    val canLoadMore = response.pagination?.let {
                        payrollCurrentPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    _payrollPagingState.value = _payrollPagingState.value.copy(
                        items = updatedList,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) payrollCurrentPage = (payrollCurrentPage - 1).coerceAtLeast(1)
                    _payrollPagingState.value = _payrollPagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(response.message ?: "Failed to load payroll runs")
                }
            },
            onFailure = { err ->
                if (isLoadMore) payrollCurrentPage = (payrollCurrentPage - 1).coerceAtLeast(1)
                _payrollPagingState.value = _payrollPagingState.value.copy(
                    isInitialLoading = false,
                    isLoadingMore = false
                )
                DynamicIslandManager.showError(err.message ?: "Network error loading payroll runs")
            }
        )
    }

    // =========================================================================
    // 3. PIECE RATE MASTER & WAGE REPORT
    // =========================================================================

    private val _pieceRateAnalytics = MutableStateFlow<PieceRateAnalyticsData?>(null)
    val pieceRateAnalytics: StateFlow<PieceRateAnalyticsData?> = _pieceRateAnalytics.asStateFlow()

    private val _isLoadingPieceRateAnalytics = MutableStateFlow(false)
    val isLoadingPieceRateAnalytics: StateFlow<Boolean> = _isLoadingPieceRateAnalytics.asStateFlow()

    private val _pieceRatePagingState = MutableStateFlow(PagingUiState<PieceRateRecordDto>())
    val pieceRatePagingState: StateFlow<PagingUiState<PieceRateRecordDto>> = _pieceRatePagingState.asStateFlow()

    private val _pieceRateSearchQuery = MutableStateFlow("")
    val pieceRateSearchQuery: StateFlow<String> = _pieceRateSearchQuery.asStateFlow()

    private val _pieceRateStatusFilter = MutableStateFlow<Set<String>>(emptySet())

    private var pieceRateCurrentPage = 1

    val filteredPieceRateList: StateFlow<List<PieceRateRecordDto>> = combine(
        _pieceRatePagingState,
        _pieceRateSearchQuery,
        _pieceRateStatusFilter
    ) { paging, query, statuses ->
        val cleanQuery = query.trim().lowercase()
        paging.items.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.workflowName.lowercase().contains(cleanQuery) ||
                    item.garmentName.lowercase().contains(cleanQuery) ||
                    item.garmentCategoryName.lowercase().contains(cleanQuery) ||
                    item.createdBy.lowercase().contains(cleanQuery) ||
                    item.status.lowercase().contains(cleanQuery)

            val matchesStatus = statuses.isEmpty() || statuses.any { it.equals(item.status, ignoreCase = true) }

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPieceRateReport() {
        viewModelScope.launch {
            coroutineScope {
                val analyticsJob = async { fetchPieceRateAnalyticsInternal() }
                val recentJob = async { fetchPieceRateRecentInternal(isLoadMore = false) }
                analyticsJob.await()
                recentJob.await()
            }
        }
    }

    fun loadMorePieceRates() {
        viewModelScope.launch {
            fetchPieceRateRecentInternal(isLoadMore = true)
        }
    }

    fun onPieceRateSearchQueryChange(query: String) {
        _pieceRateSearchQuery.value = query
    }

    fun applyPieceRateFilters(statuses: Set<String>) {
        _pieceRateStatusFilter.value = statuses
    }

    fun clearPieceRateFilters() {
        _pieceRateSearchQuery.value = ""
        _pieceRateStatusFilter.value = emptySet()
    }

    private suspend fun fetchPieceRateAnalyticsInternal() {
        _isLoadingPieceRateAnalytics.value = true
        val res = repo.request<PieceRateAnalyticsResponse> {
            get(PIECE_RATE_ANALYTICS_URL, repo.query("limit" to DEFAULT_PAGE_LIMIT))
        }
        res.fold(
            onSuccess = { response ->
                if (response.success && response.data != null) {
                    _pieceRateAnalytics.value = response.data
                } else {
                    DynamicIslandManager.showError(response.message ?: "Failed to load piece rate analytics")
                }
            },
            onFailure = { err ->
                DynamicIslandManager.showError(err.message ?: "Network error loading piece rate analytics")
            }
        )
        _isLoadingPieceRateAnalytics.value = false
    }

    private suspend fun fetchPieceRateRecentInternal(isLoadMore: Boolean) {
        val currentState = _pieceRatePagingState.value
        if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            _pieceRatePagingState.value = currentState.copy(isLoadingMore = true)
            pieceRateCurrentPage++
        } else {
            pieceRateCurrentPage = 1
            _pieceRatePagingState.value = currentState.copy(isInitialLoading = true)
        }

        val res = repo.request<PieceRateRecentResponse> {
            get(
                PIECE_RATE_RECENT_URL,
                repo.query("page" to pieceRateCurrentPage, "limit" to DEFAULT_PAGE_LIMIT)
            )
        }

        res.fold(
            onSuccess = { response ->
                if (response.success) {
                    val incoming = response.data
                    val existing = if (isLoadMore) _pieceRatePagingState.value.items else emptyList()
                    val existingIds = existing.map { it.id }.toSet()
                    val uniqueNew = incoming.filter { it.id !in existingIds }
                    val updatedList = existing + uniqueNew

                    val canLoadMore = response.pagination?.let {
                        pieceRateCurrentPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    _pieceRatePagingState.value = _pieceRatePagingState.value.copy(
                        items = updatedList,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) pieceRateCurrentPage = (pieceRateCurrentPage - 1).coerceAtLeast(1)
                    _pieceRatePagingState.value = _pieceRatePagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(response.message ?: "Failed to load piece rates")
                }
            },
            onFailure = { err ->
                if (isLoadMore) pieceRateCurrentPage = (pieceRateCurrentPage - 1).coerceAtLeast(1)
                _pieceRatePagingState.value = _pieceRatePagingState.value.copy(
                    isInitialLoading = false,
                    isLoadingMore = false
                )
                DynamicIslandManager.showError(err.message ?: "Network error loading piece rates")
            }
        )
    }

    // =========================================================================
    // 4. REIMBURSEMENT CLAIMS & SETTLEMENT REPORT
    // =========================================================================

    private val _reimbursementAnalytics = MutableStateFlow<ReimbursementAnalyticsData?>(null)
    val reimbursementAnalytics: StateFlow<ReimbursementAnalyticsData?> = _reimbursementAnalytics.asStateFlow()

    private val _isLoadingReimbursementAnalytics = MutableStateFlow(false)
    val isLoadingReimbursementAnalytics: StateFlow<Boolean> = _isLoadingReimbursementAnalytics.asStateFlow()

    private val _reimbursementPagingState = MutableStateFlow(PagingUiState<ReimbursementRecordDto>())
    val reimbursementPagingState: StateFlow<PagingUiState<ReimbursementRecordDto>> = _reimbursementPagingState.asStateFlow()

    private val _reimbursementSearchQuery = MutableStateFlow("")
    val reimbursementSearchQuery: StateFlow<String> = _reimbursementSearchQuery.asStateFlow()

    private val _reimbursementStatusFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _reimbursementPaymentFilter = MutableStateFlow<Set<String>>(emptySet())

    private var reimbursementCurrentPage = 1

    val filteredReimbursementList: StateFlow<List<ReimbursementRecordDto>> = combine(
        _reimbursementPagingState,
        _reimbursementSearchQuery,
        _reimbursementStatusFilter,
        _reimbursementPaymentFilter
    ) { paging, query, statuses, paymentStatuses ->
        val cleanQuery = query.trim().lowercase()
        paging.items.filter { item ->
            val employeeName = item.organizationMemberId?.fullName.orEmpty().lowercase()
            val deptName = item.departmentId?.name.orEmpty().lowercase()
            val matchesQuery = cleanQuery.isEmpty() ||
                    employeeName.contains(cleanQuery) ||
                    deptName.contains(cleanQuery) ||
                    item.claimType.lowercase().contains(cleanQuery) ||
                    item.status.lowercase().contains(cleanQuery) ||
                    item.paymentStatus.lowercase().contains(cleanQuery)

            val matchesStatus = statuses.isEmpty() || statuses.any { it.equals(item.status, ignoreCase = true) }
            val matchesPayment = paymentStatuses.isEmpty() || paymentStatuses.any { it.equals(item.paymentStatus, ignoreCase = true) }

            matchesQuery && matchesStatus && matchesPayment
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadReimbursementReport() {
        viewModelScope.launch {
            coroutineScope {
                val analyticsJob = async { fetchReimbursementAnalyticsInternal() }
                val recentJob = async { fetchReimbursementRecentInternal(isLoadMore = false) }
                analyticsJob.await()
                recentJob.await()
            }
        }
    }

    fun loadMoreReimbursements() {
        viewModelScope.launch {
            fetchReimbursementRecentInternal(isLoadMore = true)
        }
    }

    fun onReimbursementSearchQueryChange(query: String) {
        _reimbursementSearchQuery.value = query
    }

    fun applyReimbursementFilters(statuses: Set<String>, paymentStatuses: Set<String>) {
        _reimbursementStatusFilter.value = statuses
        _reimbursementPaymentFilter.value = paymentStatuses
    }

    fun clearReimbursementFilters() {
        _reimbursementSearchQuery.value = ""
        _reimbursementStatusFilter.value = emptySet()
        _reimbursementPaymentFilter.value = emptySet()
    }

    private suspend fun fetchReimbursementAnalyticsInternal() {
        _isLoadingReimbursementAnalytics.value = true
        val res = repo.request<ReimbursementAnalyticsResponse> {
            get(REIMBURSEMENT_ANALYTICS_URL)
        }
        res.fold(
            onSuccess = { response ->
                if (response.success && response.data != null) {
                    _reimbursementAnalytics.value = response.data
                } else {
                    DynamicIslandManager.showError(response.message ?: "Failed to load reimbursement analytics")
                }
            },
            onFailure = { err ->
                DynamicIslandManager.showError(err.message ?: "Network error loading reimbursement analytics")
            }
        )
        _isLoadingReimbursementAnalytics.value = false
    }

    private suspend fun fetchReimbursementRecentInternal(isLoadMore: Boolean) {
        val currentState = _reimbursementPagingState.value
        if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            _reimbursementPagingState.value = currentState.copy(isLoadingMore = true)
            reimbursementCurrentPage++
        } else {
            reimbursementCurrentPage = 1
            _reimbursementPagingState.value = currentState.copy(isInitialLoading = true)
        }

        val res = repo.request<ReimbursementRecentResponse> {
            get(
                REIMBURSEMENT_RECENT_URL,
                repo.query("page" to reimbursementCurrentPage, "limit" to DEFAULT_PAGE_LIMIT)
            )
        }

        res.fold(
            onSuccess = { response ->
                if (response.success) {
                    val incoming = response.data
                    val existing = if (isLoadMore) _reimbursementPagingState.value.items else emptyList()
                    val existingIds = existing.map { it.id }.toSet()
                    val uniqueNew = incoming.filter { it.id !in existingIds }
                    val updatedList = existing + uniqueNew

                    val canLoadMore = response.pagination?.let {
                        reimbursementCurrentPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    _reimbursementPagingState.value = _reimbursementPagingState.value.copy(
                        items = updatedList,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) reimbursementCurrentPage = (reimbursementCurrentPage - 1).coerceAtLeast(1)
                    _reimbursementPagingState.value = _reimbursementPagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(response.message ?: "Failed to load reimbursements")
                }
            },
            onFailure = { err ->
                if (isLoadMore) reimbursementCurrentPage = (reimbursementCurrentPage - 1).coerceAtLeast(1)
                _reimbursementPagingState.value = _reimbursementPagingState.value.copy(
                    isInitialLoading = false,
                    isLoadingMore = false
                )
                DynamicIslandManager.showError(err.message ?: "Network error loading reimbursements")
            }
        )
    }

    // =========================================================================
    // 5. SECURITY INCIDENTS & ALERTS REPORT
    // =========================================================================

    private val _securityIncidentSummary = MutableStateFlow<SecurityIncidentSummaryDto?>(null)
    val securityIncidentSummary: StateFlow<SecurityIncidentSummaryDto?> = _securityIncidentSummary.asStateFlow()

    private val _securityIncidentsPagingState = MutableStateFlow(PagingUiState<SecurityIncidentReportItemDto>())
    val securityIncidentsPagingState: StateFlow<PagingUiState<SecurityIncidentReportItemDto>> = _securityIncidentsPagingState.asStateFlow()

    private val _securityIncidentSearchQuery = MutableStateFlow("")
    val securityIncidentSearchQuery: StateFlow<String> = _securityIncidentSearchQuery.asStateFlow()

    private val _securityIncidentSeverityFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _securityIncidentStatusFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _securityIncidentAlertOnlyFilter = MutableStateFlow(false)

    private var securityIncidentsCurrentPage = 1

    val filteredSecurityIncidentsList: StateFlow<List<SecurityIncidentReportItemDto>> = combine(
        _securityIncidentsPagingState,
        _securityIncidentSearchQuery,
        _securityIncidentSeverityFilter,
        _securityIncidentStatusFilter,
        _securityIncidentAlertOnlyFilter
    ) { paging, query, severities, statuses, alertsOnly ->
        val cleanQuery = query.trim().lowercase()
        paging.items.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.alertCode.lowercase().contains(cleanQuery) ||
                    item.eventType.lowercase().contains(cleanQuery) ||
                    item.user.lowercase().contains(cleanQuery) ||
                    item.ipAddress.lowercase().contains(cleanQuery) ||
                    item.location.lowercase().contains(cleanQuery)

            val matchesSeverity = severities.isEmpty() || severities.any { it.equals(item.severity, ignoreCase = true) }
            val matchesStatus = statuses.isEmpty() || statuses.any { it.equals(item.status, ignoreCase = true) }
            val matchesAlert = !alertsOnly || item.isAlert.equals("Yes", ignoreCase = true)

            matchesQuery && matchesSeverity && matchesStatus && matchesAlert
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadSecurityIncidentsReport() {
        viewModelScope.launch {
            fetchSecurityIncidentsInternal(isLoadMore = false)
        }
    }

    fun loadMoreSecurityIncidents() {
        viewModelScope.launch {
            fetchSecurityIncidentsInternal(isLoadMore = true)
        }
    }

    fun onSecurityIncidentSearchQueryChange(query: String) {
        _securityIncidentSearchQuery.value = query
    }

    fun applySecurityIncidentFilters(
        severities: Set<String>,
        statuses: Set<String>,
        alertsOnly: Boolean
    ) {
        _securityIncidentSeverityFilter.value = severities
        _securityIncidentStatusFilter.value = statuses
        _securityIncidentAlertOnlyFilter.value = alertsOnly
    }

    fun clearSecurityIncidentFilters() {
        _securityIncidentSearchQuery.value = ""
        _securityIncidentSeverityFilter.value = emptySet()
        _securityIncidentStatusFilter.value = emptySet()
        _securityIncidentAlertOnlyFilter.value = false
    }

    private suspend fun fetchSecurityIncidentsInternal(isLoadMore: Boolean) {
        val currentState = _securityIncidentsPagingState.value
        if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            _securityIncidentsPagingState.value = currentState.copy(isLoadingMore = true)
            securityIncidentsCurrentPage++
        } else {
            securityIncidentsCurrentPage = 1
            _securityIncidentsPagingState.value = currentState.copy(isInitialLoading = true)
        }

        val res = repo.request<SecurityIncidentsReportResponse> {
            get(
                SECURITY_INCIDENTS_REPORT_URL,
                repo.query("page" to securityIncidentsCurrentPage, "limit" to DEFAULT_PAGE_LIMIT)
            )
        }

        res.fold(
            onSuccess = { response ->
                if (response.success) {
                    if (response.summary != null) {
                        _securityIncidentSummary.value = response.summary
                    }
                    val incoming = response.data
                    val existing = if (isLoadMore) _securityIncidentsPagingState.value.items else emptyList()
                    val existingIds = existing.map { it.id }.toSet()
                    val uniqueNew = incoming.filter { it.id !in existingIds }
                    val updatedList = existing + uniqueNew

                    val canLoadMore = response.pagination?.let {
                        securityIncidentsCurrentPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    _securityIncidentsPagingState.value = _securityIncidentsPagingState.value.copy(
                        items = updatedList,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) securityIncidentsCurrentPage = (securityIncidentsCurrentPage - 1).coerceAtLeast(1)
                    _securityIncidentsPagingState.value = _securityIncidentsPagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(response.message ?: "Failed to load security incidents")
                }
            },
            onFailure = { err ->
                if (isLoadMore) securityIncidentsCurrentPage = (securityIncidentsCurrentPage - 1).coerceAtLeast(1)
                _securityIncidentsPagingState.value = _securityIncidentsPagingState.value.copy(
                    isInitialLoading = false,
                    isLoadingMore = false
                )
                DynamicIslandManager.showError(err.message ?: "Network error loading security incidents")
            }
        )
    }

    // =========================================================================
    // 6. ACCESS & PERMISSION ACTIVITY REPORT
    // =========================================================================

    private val _securityAccessSummary = MutableStateFlow<SecurityAccessSummaryDto?>(null)
    val securityAccessSummary: StateFlow<SecurityAccessSummaryDto?> = _securityAccessSummary.asStateFlow()

    private val _securityAccessPagingState = MutableStateFlow(PagingUiState<SecurityAccessReportItemDto>())
    val securityAccessPagingState: StateFlow<PagingUiState<SecurityAccessReportItemDto>> = _securityAccessPagingState.asStateFlow()

    private val _securityAccessSearchQuery = MutableStateFlow("")
    val securityAccessSearchQuery: StateFlow<String> = _securityAccessSearchQuery.asStateFlow()

    private val _securityAccessSeverityFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _securityAccessSuspiciousOnlyFilter = MutableStateFlow(false)

    private var securityAccessCurrentPage = 1

    val filteredSecurityAccessList: StateFlow<List<SecurityAccessReportItemDto>> = combine(
        _securityAccessPagingState,
        _securityAccessSearchQuery,
        _securityAccessSeverityFilter,
        _securityAccessSuspiciousOnlyFilter
    ) { paging, query, severities, suspiciousOnly ->
        val cleanQuery = query.trim().lowercase()
        paging.items.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.user.lowercase().contains(cleanQuery) ||
                    item.eventType.lowercase().contains(cleanQuery) ||
                    item.ipAddress.lowercase().contains(cleanQuery) ||
                    item.statusBadge.lowercase().contains(cleanQuery) ||
                    item.actionDetail.lowercase().contains(cleanQuery)

            val matchesSeverity = severities.isEmpty() || severities.any { it.equals(item.severity, ignoreCase = true) }
            val matchesSuspicious = !suspiciousOnly || item.isSuspicious

            matchesQuery && matchesSeverity && matchesSuspicious
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadSecurityAccessReport() {
        viewModelScope.launch {
            fetchSecurityAccessInternal(isLoadMore = false)
        }
    }

    fun loadMoreSecurityAccess() {
        viewModelScope.launch {
            fetchSecurityAccessInternal(isLoadMore = true)
        }
    }

    fun onSecurityAccessSearchQueryChange(query: String) {
        _securityAccessSearchQuery.value = query
    }

    fun applySecurityAccessFilters(severities: Set<String>, suspiciousOnly: Boolean) {
        _securityAccessSeverityFilter.value = severities
        _securityAccessSuspiciousOnlyFilter.value = suspiciousOnly
    }

    fun clearSecurityAccessFilters() {
        _securityAccessSearchQuery.value = ""
        _securityAccessSeverityFilter.value = emptySet()
        _securityAccessSuspiciousOnlyFilter.value = false
    }

    private suspend fun fetchSecurityAccessInternal(isLoadMore: Boolean) {
        val currentState = _securityAccessPagingState.value
        if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            _securityAccessPagingState.value = currentState.copy(isLoadingMore = true)
            securityAccessCurrentPage++
        } else {
            securityAccessCurrentPage = 1
            _securityAccessPagingState.value = currentState.copy(isInitialLoading = true)
        }

        val res = repo.request<SecurityAccessReportResponse> {
            get(
                SECURITY_ACCESS_REPORT_URL,
                repo.query("page" to securityAccessCurrentPage, "limit" to DEFAULT_PAGE_LIMIT)
            )
        }

        res.fold(
            onSuccess = { response ->
                if (response.success) {
                    if (response.summary != null) {
                        _securityAccessSummary.value = response.summary
                    }
                    val incoming = response.data
                    val existing = if (isLoadMore) _securityAccessPagingState.value.items else emptyList()
                    val existingIds = existing.map { it.id }.toSet()
                    val uniqueNew = incoming.filter { it.id !in existingIds }
                    val updatedList = existing + uniqueNew

                    val canLoadMore = response.pagination?.let {
                        securityAccessCurrentPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    _securityAccessPagingState.value = _securityAccessPagingState.value.copy(
                        items = updatedList,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) securityAccessCurrentPage = (securityAccessCurrentPage - 1).coerceAtLeast(1)
                    _securityAccessPagingState.value = _securityAccessPagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(response.message ?: "Failed to load access activity")
                }
            },
            onFailure = { err ->
                if (isLoadMore) securityAccessCurrentPage = (securityAccessCurrentPage - 1).coerceAtLeast(1)
                _securityAccessPagingState.value = _securityAccessPagingState.value.copy(
                    isInitialLoading = false,
                    isLoadingMore = false
                )
                DynamicIslandManager.showError(err.message ?: "Network error loading access activity")
            }
        )
    }

    // =========================================================================
    // 7. API & INTEGRATION SECURITY REPORT
    // =========================================================================

    private val _securityApiIntegrationSummary = MutableStateFlow<SecurityApiIntegrationSummaryDto?>(null)
    val securityApiIntegrationSummary: StateFlow<SecurityApiIntegrationSummaryDto?> = _securityApiIntegrationSummary.asStateFlow()

    private val _securityApiIntegrationsPagingState = MutableStateFlow(PagingUiState<SecurityApiIntegrationReportItemDto>())
    val securityApiIntegrationsPagingState: StateFlow<PagingUiState<SecurityApiIntegrationReportItemDto>> = _securityApiIntegrationsPagingState.asStateFlow()

    private val _securityApiIntegrationSearchQuery = MutableStateFlow("")
    val securityApiIntegrationSearchQuery: StateFlow<String> = _securityApiIntegrationSearchQuery.asStateFlow()

    private val _securityApiStatusFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _securityApiCategoryFilter = MutableStateFlow<Set<String>>(emptySet())

    private var securityApiCurrentPage = 1

    val filteredSecurityApiIntegrationsList: StateFlow<List<SecurityApiIntegrationReportItemDto>> = combine(
        _securityApiIntegrationsPagingState,
        _securityApiIntegrationSearchQuery,
        _securityApiStatusFilter,
        _securityApiCategoryFilter
    ) { paging, query, statuses, categories ->
        val cleanQuery = query.trim().lowercase()
        paging.items.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.name.lowercase().contains(cleanQuery) ||
                    item.category.lowercase().contains(cleanQuery) ||
                    item.authType.lowercase().contains(cleanQuery) ||
                    item.createdBy.lowercase().contains(cleanQuery) ||
                    item.status.lowercase().contains(cleanQuery)

            val matchesStatus = statuses.isEmpty() || statuses.any { it.equals(item.status, ignoreCase = true) }
            val matchesCategory = categories.isEmpty() || categories.any { it.equals(item.category, ignoreCase = true) }

            matchesQuery && matchesStatus && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadSecurityApiIntegrationsReport() {
        viewModelScope.launch {
            fetchSecurityApiIntegrationsInternal(isLoadMore = false)
        }
    }

    fun loadMoreSecurityApiIntegrations() {
        viewModelScope.launch {
            fetchSecurityApiIntegrationsInternal(isLoadMore = true)
        }
    }

    fun onSecurityApiIntegrationSearchQueryChange(query: String) {
        _securityApiIntegrationSearchQuery.value = query
    }

    fun applySecurityApiIntegrationFilters(statuses: Set<String>, categories: Set<String>) {
        _securityApiStatusFilter.value = statuses
        _securityApiCategoryFilter.value = categories
    }

    fun clearSecurityApiIntegrationFilters() {
        _securityApiIntegrationSearchQuery.value = ""
        _securityApiStatusFilter.value = emptySet()
        _securityApiCategoryFilter.value = emptySet()
    }

    private suspend fun fetchSecurityApiIntegrationsInternal(isLoadMore: Boolean) {
        val currentState = _securityApiIntegrationsPagingState.value
        if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            _securityApiIntegrationsPagingState.value = currentState.copy(isLoadingMore = true)
            securityApiCurrentPage++
        } else {
            securityApiCurrentPage = 1
            _securityApiIntegrationsPagingState.value = currentState.copy(isInitialLoading = true)
        }

        val res = repo.request<SecurityApiIntegrationsReportResponse> {
            get(
                SECURITY_API_INTEGRATIONS_REPORT_URL,
                repo.query("page" to securityApiCurrentPage, "limit" to DEFAULT_PAGE_LIMIT)
            )
        }

        res.fold(
            onSuccess = { response ->
                if (response.success) {
                    if (response.summary != null) {
                        _securityApiIntegrationSummary.value = response.summary
                    }
                    val incoming = response.data
                    val existing = if (isLoadMore) _securityApiIntegrationsPagingState.value.items else emptyList()
                    val existingIds = existing.map { it.id }.toSet()
                    val uniqueNew = incoming.filter { it.id !in existingIds }
                    val updatedList = existing + uniqueNew

                    val canLoadMore = response.pagination?.let {
                        securityApiCurrentPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    _securityApiIntegrationsPagingState.value = _securityApiIntegrationsPagingState.value.copy(
                        items = updatedList,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) securityApiCurrentPage = (securityApiCurrentPage - 1).coerceAtLeast(1)
                    _securityApiIntegrationsPagingState.value = _securityApiIntegrationsPagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(response.message ?: "Failed to load API integration report")
                }
            },
            onFailure = { err ->
                if (isLoadMore) securityApiCurrentPage = (securityApiCurrentPage - 1).coerceAtLeast(1)
                _securityApiIntegrationsPagingState.value = _securityApiIntegrationsPagingState.value.copy(
                    isInitialLoading = false,
                    isLoadingMore = false
                )
                DynamicIslandManager.showError(err.message ?: "Network error loading API integration report")
            }
        )
    }

    // =========================================================================
    // 8. IT ASSET REPORT
    // =========================================================================

    private val _itAssetSummary = MutableStateFlow<ItAssetReportSummaryDto?>(null)
    val itAssetSummary: StateFlow<ItAssetReportSummaryDto?> = _itAssetSummary.asStateFlow()

    private val _itAssetsPagingState = MutableStateFlow(PagingUiState<ItAssetReportItemDto>())
    val itAssetsPagingState: StateFlow<PagingUiState<ItAssetReportItemDto>> = _itAssetsPagingState.asStateFlow()

    private val _itAssetSearchQuery = MutableStateFlow("")
    val itAssetSearchQuery: StateFlow<String> = _itAssetSearchQuery.asStateFlow()

    private val _itAssetStatusFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _itAssetCategoryFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _itAssetExpiredOnlyFilter = MutableStateFlow(false)

    private var itAssetsCurrentPage = 1

    val filteredItAssetsList: StateFlow<List<ItAssetReportItemDto>> = combine(
        _itAssetsPagingState,
        _itAssetSearchQuery,
        _itAssetStatusFilter,
        _itAssetCategoryFilter,
        _itAssetExpiredOnlyFilter
    ) { paging, query, statuses, categories, expiredOnly ->
        val cleanQuery = query.trim().lowercase()
        paging.items.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.assetId.lowercase().contains(cleanQuery) ||
                    item.assetName.lowercase().contains(cleanQuery) ||
                    item.makeModel.lowercase().contains(cleanQuery) ||
                    item.category.lowercase().contains(cleanQuery) ||
                    item.vendor.lowercase().contains(cleanQuery) ||
                    item.location.lowercase().contains(cleanQuery)

            val matchesStatus = statuses.isEmpty() || statuses.any { it.equals(item.statusBadge, ignoreCase = true) }
            val matchesCategory = categories.isEmpty() || categories.any { it.equals(item.category, ignoreCase = true) }
            val matchesWarranty = !expiredOnly || item.isWarrantyExpired

            matchesQuery && matchesStatus && matchesCategory && matchesWarranty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadItAssetReport() {
        viewModelScope.launch {
            fetchItAssetsInternal(isLoadMore = false)
        }
    }

    fun loadMoreItAssets() {
        viewModelScope.launch {
            fetchItAssetsInternal(isLoadMore = true)
        }
    }

    fun onItAssetSearchQueryChange(query: String) {
        _itAssetSearchQuery.value = query
    }

    fun applyItAssetFilters(
        statuses: Set<String>,
        categories: Set<String>,
        expiredOnly: Boolean
    ) {
        _itAssetStatusFilter.value = statuses
        _itAssetCategoryFilter.value = categories
        _itAssetExpiredOnlyFilter.value = expiredOnly
    }

    fun clearItAssetFilters() {
        _itAssetSearchQuery.value = ""
        _itAssetStatusFilter.value = emptySet()
        _itAssetCategoryFilter.value = emptySet()
        _itAssetExpiredOnlyFilter.value = false
    }

    private suspend fun fetchItAssetsInternal(isLoadMore: Boolean) {
        val currentState = _itAssetsPagingState.value
        if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            _itAssetsPagingState.value = currentState.copy(isLoadingMore = true)
            itAssetsCurrentPage++
        } else {
            itAssetsCurrentPage = 1
            _itAssetsPagingState.value = currentState.copy(isInitialLoading = true)
        }

        val res = repo.request<ItAssetReportResponse> {
            get(
                IT_ASSETS_REPORT_URL,
                repo.query("page" to itAssetsCurrentPage, "limit" to DEFAULT_PAGE_LIMIT)
            )
        }

        res.fold(
            onSuccess = { response ->
                if (response.success) {
                    if (response.summary != null) {
                        _itAssetSummary.value = response.summary
                    }
                    val incoming = response.data
                    val existing = if (isLoadMore) _itAssetsPagingState.value.items else emptyList()
                    val existingIds = existing.map { it.id }.toSet()
                    val uniqueNew = incoming.filter { it.id !in existingIds }
                    val updatedList = existing + uniqueNew

                    val canLoadMore = response.pagination?.let {
                        itAssetsCurrentPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    _itAssetsPagingState.value = _itAssetsPagingState.value.copy(
                        items = updatedList,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) itAssetsCurrentPage = (itAssetsCurrentPage - 1).coerceAtLeast(1)
                    _itAssetsPagingState.value = _itAssetsPagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(response.message ?: "Failed to load IT asset report")
                }
            },
            onFailure = { err ->
                if (isLoadMore) itAssetsCurrentPage = (itAssetsCurrentPage - 1).coerceAtLeast(1)
                _itAssetsPagingState.value = _itAssetsPagingState.value.copy(
                    isInitialLoading = false,
                    isLoadingMore = false
                )
                DynamicIslandManager.showError(err.message ?: "Network error loading IT asset report")
            }
        )
    }

    // =========================================================================
    // 9. IT SERVICE DESK REPORT
    // =========================================================================

    private val _itServiceDeskSummary = MutableStateFlow<ItServiceDeskReportSummaryDto?>(null)
    val itServiceDeskSummary: StateFlow<ItServiceDeskReportSummaryDto?> = _itServiceDeskSummary.asStateFlow()

    private val _itServiceDeskPagingState = MutableStateFlow(PagingUiState<ItServiceDeskReportItemDto>())
    val itServiceDeskPagingState: StateFlow<PagingUiState<ItServiceDeskReportItemDto>> = _itServiceDeskPagingState.asStateFlow()

    private val _itServiceDeskSearchQuery = MutableStateFlow("")
    val itServiceDeskSearchQuery: StateFlow<String> = _itServiceDeskSearchQuery.asStateFlow()

    private val _itServiceDeskStatusFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _itServiceDeskPriorityFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _itServiceDeskSlaFilter = MutableStateFlow<Set<String>>(emptySet())

    private var itServiceDeskCurrentPage = 1

    val filteredItServiceDeskList: StateFlow<List<ItServiceDeskReportItemDto>> = combine(
        _itServiceDeskPagingState,
        _itServiceDeskSearchQuery,
        _itServiceDeskStatusFilter,
        _itServiceDeskPriorityFilter,
        _itServiceDeskSlaFilter
    ) { paging, query, statuses, priorities, slaStatuses ->
        val cleanQuery = query.trim().lowercase()
        paging.items.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.ticketId.lowercase().contains(cleanQuery) ||
                    item.subject.lowercase().contains(cleanQuery) ||
                    item.requesterName.lowercase().contains(cleanQuery) ||
                    item.technician.lowercase().contains(cleanQuery) ||
                    item.category.lowercase().contains(cleanQuery) ||
                    item.relatedAsset.lowercase().contains(cleanQuery)

            val matchesStatus = statuses.isEmpty() || statuses.any { it.equals(item.statusBadge, ignoreCase = true) }
            val matchesPriority = priorities.isEmpty() || priorities.any { it.equals(item.priority, ignoreCase = true) }
            val matchesSla = slaStatuses.isEmpty() || slaStatuses.any { it.equals(item.slaStatus, ignoreCase = true) }

            matchesQuery && matchesStatus && matchesPriority && matchesSla
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadItServiceDeskReport() {
        viewModelScope.launch {
            fetchItServiceDeskInternal(isLoadMore = false)
        }
    }

    fun loadMoreItServiceDesk() {
        viewModelScope.launch {
            fetchItServiceDeskInternal(isLoadMore = true)
        }
    }

    fun onItServiceDeskSearchQueryChange(query: String) {
        _itServiceDeskSearchQuery.value = query
    }

    fun applyItServiceDeskFilters(
        statuses: Set<String>,
        priorities: Set<String>,
        slaStatuses: Set<String>
    ) {
        _itServiceDeskStatusFilter.value = statuses
        _itServiceDeskPriorityFilter.value = priorities
        _itServiceDeskSlaFilter.value = slaStatuses
    }

    fun clearItServiceDeskFilters() {
        _itServiceDeskSearchQuery.value = ""
        _itServiceDeskStatusFilter.value = emptySet()
        _itServiceDeskPriorityFilter.value = emptySet()
        _itServiceDeskSlaFilter.value = emptySet()
    }

    private suspend fun fetchItServiceDeskInternal(isLoadMore: Boolean) {
        val currentState = _itServiceDeskPagingState.value
        if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            _itServiceDeskPagingState.value = currentState.copy(isLoadingMore = true)
            itServiceDeskCurrentPage++
        } else {
            itServiceDeskCurrentPage = 1
            _itServiceDeskPagingState.value = currentState.copy(isInitialLoading = true)
        }

        val res = repo.request<ItServiceDeskReportResponse> {
            get(
                IT_SERVICE_DESK_REPORT_URL,
                repo.query("page" to itServiceDeskCurrentPage, "limit" to DEFAULT_PAGE_LIMIT)
            )
        }

        res.fold(
            onSuccess = { response ->
                if (response.success) {
                    if (response.summary != null) {
                        _itServiceDeskSummary.value = response.summary
                    }
                    val incoming = response.data
                    val existing = if (isLoadMore) _itServiceDeskPagingState.value.items else emptyList()
                    val existingIds = existing.map { it.id }.toSet()
                    val uniqueNew = incoming.filter { it.id !in existingIds }
                    val updatedList = existing + uniqueNew

                    val canLoadMore = response.pagination?.let {
                        itServiceDeskCurrentPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    _itServiceDeskPagingState.value = _itServiceDeskPagingState.value.copy(
                        items = updatedList,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) itServiceDeskCurrentPage = (itServiceDeskCurrentPage - 1).coerceAtLeast(1)
                    _itServiceDeskPagingState.value = _itServiceDeskPagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(response.message ?: "Failed to load IT service desk report")
                }
            },
            onFailure = { err ->
                if (isLoadMore) itServiceDeskCurrentPage = (itServiceDeskCurrentPage - 1).coerceAtLeast(1)
                _itServiceDeskPagingState.value = _itServiceDeskPagingState.value.copy(
                    isInitialLoading = false,
                    isLoadingMore = false
                )
                DynamicIslandManager.showError(err.message ?: "Network error loading IT service desk report")
            }
        )
    }

    // =========================================================================
    // 10. SALES REPORT GENERIC HELPERS & STATE HOLDERS (20 ENDPOINTS)
    // =========================================================================

    private var salesPageMap = mutableMapOf<String, Int>()

    private suspend inline fun <reified Resp, Item> executeSalesPagedFetch(
        reportKey: String,
        url: String,
        isLoadMore: Boolean,
        pagingFlow: MutableStateFlow<PagingUiState<Item>>,
        extraQuery: List<Pair<String, Any>> = emptyList(),
        crossinline itemId: (Item) -> String,
        crossinline extractSuccess: (Resp) -> Boolean,
        crossinline extractMessage: (Resp) -> String?,
        crossinline extractData: (Resp) -> List<Item>,
        crossinline extractPagination: (Resp) -> ReportPaginationDto?,
        crossinline onSummaryUpdate: (Resp) -> Unit
    ) {
        val currentState = pagingFlow.value
        val nextPage = if (isLoadMore) {
            if (currentState.isLoadingMore || !currentState.canLoadMore) return
            pagingFlow.value = currentState.copy(isLoadingMore = true)
            (salesPageMap[reportKey] ?: 1) + 1
        } else {
            pagingFlow.value = currentState.copy(isInitialLoading = true)
            1
        }
        salesPageMap[reportKey] = nextPage

        val queryParams = mutableListOf<Pair<String, Any>>(
            "page" to nextPage,
            "limit" to DEFAULT_PAGE_LIMIT
        )
        queryParams.addAll(extraQuery)

        val res = repo.request<Resp> {
            get(url, repo.query(*queryParams.toTypedArray()))
        }

        res.fold(
            onSuccess = { response ->
                if (extractSuccess(response)) {
                    onSummaryUpdate(response)
                    val incoming = extractData(response)
                    val existing = if (isLoadMore) pagingFlow.value.items else emptyList()
                    val existingIds = existing.map(itemId).toSet()
                    val uniqueNew = incoming.filter { itemId(it) !in existingIds }
                    val updated = existing + uniqueNew
                    val pagination = extractPagination(response)
                    val canLoadMore = pagination?.let {
                        nextPage < it.totalPages
                    } ?: (incoming.size >= DEFAULT_PAGE_LIMIT && uniqueNew.isNotEmpty())

                    pagingFlow.value = pagingFlow.value.copy(
                        items = updated,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                } else {
                    if (isLoadMore) salesPageMap[reportKey] = (nextPage - 1).coerceAtLeast(1)
                    pagingFlow.value = pagingFlow.value.copy(isInitialLoading = false, isLoadingMore = false)
                    DynamicIslandManager.showError(extractMessage(response) ?: "Failed to load report")
                }
            },
            onFailure = { err ->
                if (isLoadMore) salesPageMap[reportKey] = (nextPage - 1).coerceAtLeast(1)
                pagingFlow.value = pagingFlow.value.copy(isInitialLoading = false, isLoadingMore = false)
                DynamicIslandManager.showError(err.message ?: "Network error loading report")
            }
        )
    }

    private suspend inline fun <reified Resp, Item> executeSalesNonPagedFetch(
        url: String,
        pagingFlow: MutableStateFlow<PagingUiState<Item>>,
        crossinline extractSuccess: (Resp) -> Boolean,
        crossinline extractMessage: (Resp) -> String?,
        crossinline extractData: (Resp) -> List<Item>,
        crossinline onSummaryUpdate: (Resp) -> Unit
    ) {
        pagingFlow.value = pagingFlow.value.copy(isInitialLoading = true)
        val res = repo.request<Resp> { get(url) }
        res.fold(
            onSuccess = { response ->
                if (extractSuccess(response)) {
                    onSummaryUpdate(response)
                    pagingFlow.value = PagingUiState(
                        items = extractData(response),
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = false
                    )
                } else {
                    pagingFlow.value = pagingFlow.value.copy(isInitialLoading = false)
                    DynamicIslandManager.showError(extractMessage(response) ?: "Failed to load report")
                }
            },
            onFailure = { err ->
                pagingFlow.value = pagingFlow.value.copy(isInitialLoading = false)
                DynamicIslandManager.showError(err.message ?: "Network error loading report")
            }
        )
    }

    // --- 10.1 Lead Master Register ---
    private val _leadMasterSummary = MutableStateFlow<LeadMasterRegisterSummaryDto?>(null)
    val leadMasterSummary: StateFlow<LeadMasterRegisterSummaryDto?> = _leadMasterSummary.asStateFlow()
    private val _leadMasterPaging = MutableStateFlow(PagingUiState<LeadMasterRegisterItemDto>())
    val leadMasterPaging: StateFlow<PagingUiState<LeadMasterRegisterItemDto>> = _leadMasterPaging.asStateFlow()
    private val _leadMasterSearch = MutableStateFlow("")
    val leadMasterSearch: StateFlow<String> = _leadMasterSearch.asStateFlow()
    private val _leadMasterStatusFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _leadMasterPriorityFilter = MutableStateFlow<Set<String>>(emptySet())

    val filteredLeadMasterList: StateFlow<List<LeadMasterRegisterItemDto>> = combine(
        _leadMasterPaging, _leadMasterSearch, _leadMasterStatusFilter, _leadMasterPriorityFilter
    ) { paging, query, statuses, priorities ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.fullName.lowercase().contains(q) || item.leadCode.lowercase().contains(q) ||
                    item.leadSource.lowercase().contains(q) || item.mobileNumber.contains(q)) &&
                    (statuses.isEmpty() || statuses.any { it.equals(item.leadStatus, true) }) &&
                    (priorities.isEmpty() || priorities.any { it.equals(item.priorityLevel, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadLeadMasterReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<LeadMasterRegisterResponse, LeadMasterRegisterItemDto>(
            "lead_master", SALES_LEAD_MASTER_URL, isLoadMore, _leadMasterPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _leadMasterSummary.value = s } }
        )
    }
    fun onLeadMasterSearchChange(q: String) { _leadMasterSearch.value = q }
    fun applyLeadMasterFilters(statuses: Set<String>, priorities: Set<String>) {
        _leadMasterStatusFilter.value = statuses; _leadMasterPriorityFilter.value = priorities
    }
    fun clearLeadMasterFilters() { _leadMasterSearch.value = ""; _leadMasterStatusFilter.value = emptySet(); _leadMasterPriorityFilter.value = emptySet() }

    // --- 10.2 Lead Source Performance ---
    private val _leadSourceSummary = MutableStateFlow<LeadSourcePerformanceSummaryDto?>(null)
    val leadSourceSummary: StateFlow<LeadSourcePerformanceSummaryDto?> = _leadSourceSummary.asStateFlow()
    private val _leadSourcePaging = MutableStateFlow(PagingUiState<LeadSourcePerformanceItemDto>())
    val leadSourcePaging: StateFlow<PagingUiState<LeadSourcePerformanceItemDto>> = _leadSourcePaging.asStateFlow()
    private val _leadSourceSearch = MutableStateFlow("")
    val leadSourceSearch: StateFlow<String> = _leadSourceSearch.asStateFlow()

    val filteredLeadSourceList: StateFlow<List<LeadSourcePerformanceItemDto>> = combine(
        _leadSourcePaging, _leadSourceSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.sourceName.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadLeadSourcePerformanceReport() = viewModelScope.launch {
        executeSalesNonPagedFetch<LeadSourcePerformanceResponse, LeadSourcePerformanceItemDto>(
            SALES_LEAD_SOURCE_PERF_URL, _leadSourcePaging,
            extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, onSummaryUpdate = { it.summary?.let { s -> _leadSourceSummary.value = s } }
        )
    }
    fun onLeadSourceSearchChange(q: String) { _leadSourceSearch.value = q }

    // --- 10.3 Overdue Follow-ups ---
    private val _leadOverdueSummary = MutableStateFlow<LeadOverdueFollowupsSummaryDto?>(null)
    val leadOverdueSummary: StateFlow<LeadOverdueFollowupsSummaryDto?> = _leadOverdueSummary.asStateFlow()
    private val _leadOverduePaging = MutableStateFlow(PagingUiState<LeadOverdueFollowupItemDto>())
    val leadOverduePaging: StateFlow<PagingUiState<LeadOverdueFollowupItemDto>> = _leadOverduePaging.asStateFlow()
    private val _leadOverdueSearch = MutableStateFlow("")
    val leadOverdueSearch: StateFlow<String> = _leadOverdueSearch.asStateFlow()
    private val _leadOverduePriorityFilter = MutableStateFlow<Set<String>>(emptySet())

    val filteredLeadOverdueList: StateFlow<List<LeadOverdueFollowupItemDto>> = combine(
        _leadOverduePaging, _leadOverdueSearch, _leadOverduePriorityFilter
    ) { paging, query, priorities ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.fullName.lowercase().contains(q) || item.leadCode.lowercase().contains(q)) &&
                    (priorities.isEmpty() || priorities.any { it.equals(item.priorityLevel, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadLeadOverdueReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<LeadOverdueFollowupsResponse, LeadOverdueFollowupItemDto>(
            "lead_overdue", SALES_LEAD_OVERDUE_URL, isLoadMore, _leadOverduePaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _leadOverdueSummary.value = s } }
        )
    }
    fun onLeadOverdueSearchChange(q: String) { _leadOverdueSearch.value = q }
    fun applyLeadOverdueFilters(priorities: Set<String>) { _leadOverduePriorityFilter.value = priorities }
    fun clearLeadOverdueFilters() { _leadOverdueSearch.value = ""; _leadOverduePriorityFilter.value = emptySet() }

    // --- 10.4 Follow-up Activity Register ---
    private val _leadActivitySummary = MutableStateFlow<LeadFollowupActivitySummaryDto?>(null)
    val leadActivitySummary: StateFlow<LeadFollowupActivitySummaryDto?> = _leadActivitySummary.asStateFlow()
    private val _leadActivityPaging = MutableStateFlow(PagingUiState<LeadFollowupActivityItemDto>())
    val leadActivityPaging: StateFlow<PagingUiState<LeadFollowupActivityItemDto>> = _leadActivityPaging.asStateFlow()
    private val _leadActivitySearch = MutableStateFlow("")
    val leadActivitySearch: StateFlow<String> = _leadActivitySearch.asStateFlow()
    private val _leadActivityChannelFilter = MutableStateFlow<Set<String>>(emptySet())

    val filteredLeadActivityList: StateFlow<List<LeadFollowupActivityItemDto>> = combine(
        _leadActivityPaging, _leadActivitySearch, _leadActivityChannelFilter
    ) { paging, query, channels ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.fullName.lowercase().contains(q) || item.leadCode.lowercase().contains(q) ||
                    item.channel.lowercase().contains(q)) &&
                    (channels.isEmpty() || channels.any { it.equals(item.channel, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadLeadActivityReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<LeadFollowupActivityResponse, LeadFollowupActivityItemDto>(
            "lead_activity", SALES_LEAD_FOLLOWUP_ACTIVITY_URL, isLoadMore, _leadActivityPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _leadActivitySummary.value = s } }
        )
    }
    fun onLeadActivitySearchChange(q: String) { _leadActivitySearch.value = q }
    fun applyLeadActivityFilters(channels: Set<String>) { _leadActivityChannelFilter.value = channels }
    fun clearLeadActivityFilters() { _leadActivitySearch.value = ""; _leadActivityChannelFilter.value = emptySet() }

    // --- 10.5 Opportunity Master Register ---
    private val _oppMasterSummary = MutableStateFlow<OpportunityMasterRegisterSummaryDto?>(null)
    val oppMasterSummary: StateFlow<OpportunityMasterRegisterSummaryDto?> = _oppMasterSummary.asStateFlow()
    private val _oppMasterPaging = MutableStateFlow(PagingUiState<OpportunityMasterRegisterItemDto>())
    val oppMasterPaging: StateFlow<PagingUiState<OpportunityMasterRegisterItemDto>> = _oppMasterPaging.asStateFlow()
    private val _oppMasterSearch = MutableStateFlow("")
    val oppMasterSearch: StateFlow<String> = _oppMasterSearch.asStateFlow()
    private val _oppMasterStatusFilter = MutableStateFlow<Set<String>>(emptySet())

    val filteredOppMasterList: StateFlow<List<OpportunityMasterRegisterItemDto>> = combine(
        _oppMasterPaging, _oppMasterSearch, _oppMasterStatusFilter
    ) { paging, query, statuses ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.opportunityCode.lowercase().contains(q) || item.name.lowercase().contains(q) ||
                    item.clientName.lowercase().contains(q) || item.stageName.lowercase().contains(q)) &&
                    (statuses.isEmpty() || statuses.any { it.equals(item.status, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadOppMasterReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<OpportunityMasterRegisterResponse, OpportunityMasterRegisterItemDto>(
            "opp_master", SALES_OPP_MASTER_URL, isLoadMore, _oppMasterPaging,
            extraQuery = listOf("dateType" to "createdAt"),
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _oppMasterSummary.value = s } }
        )
    }
    fun onOppMasterSearchChange(q: String) { _oppMasterSearch.value = q }
    fun applyOppMasterFilters(statuses: Set<String>) { _oppMasterStatusFilter.value = statuses }
    fun clearOppMasterFilters() { _oppMasterSearch.value = ""; _oppMasterStatusFilter.value = emptySet() }

    // --- 10.6 Opportunity Stage Funnel ---
    private val _oppFunnelSummary = MutableStateFlow<OpportunityStageFunnelSummaryDto?>(null)
    val oppFunnelSummary: StateFlow<OpportunityStageFunnelSummaryDto?> = _oppFunnelSummary.asStateFlow()
    private val _oppFunnelPaging = MutableStateFlow(PagingUiState<OpportunityStageFunnelItemDto>())
    val oppFunnelPaging: StateFlow<PagingUiState<OpportunityStageFunnelItemDto>> = _oppFunnelPaging.asStateFlow()
    private val _oppFunnelSearch = MutableStateFlow("")
    val oppFunnelSearch: StateFlow<String> = _oppFunnelSearch.asStateFlow()

    val filteredOppFunnelList: StateFlow<List<OpportunityStageFunnelItemDto>> = combine(
        _oppFunnelPaging, _oppFunnelSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.stageName.lowercase().contains(q) || it.stageCode.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadOppStageFunnelReport() = viewModelScope.launch {
        executeSalesNonPagedFetch<OpportunityStageFunnelResponse, OpportunityStageFunnelItemDto>(
            SALES_OPP_STAGE_FUNNEL_URL, _oppFunnelPaging,
            extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, onSummaryUpdate = { it.summary?.let { s -> _oppFunnelSummary.value = s } }
        )
    }
    fun onOppFunnelSearchChange(q: String) { _oppFunnelSearch.value = q }

    // --- 10.7 Closing Soon & Overdue Deals ---
    private val _oppAlertsSummary = MutableStateFlow<OpportunityClosingAlertsSummaryDto?>(null)
    val oppAlertsSummary: StateFlow<OpportunityClosingAlertsSummaryDto?> = _oppAlertsSummary.asStateFlow()
    private val _oppAlertsPaging = MutableStateFlow(PagingUiState<OpportunityClosingAlertItemDto>())
    val oppAlertsPaging: StateFlow<PagingUiState<OpportunityClosingAlertItemDto>> = _oppAlertsPaging.asStateFlow()
    private val _oppAlertsSearch = MutableStateFlow("")
    val oppAlertsSearch: StateFlow<String> = _oppAlertsSearch.asStateFlow()
    private val _oppAlertsOverdueOnly = MutableStateFlow(false)

    val filteredOppAlertsList: StateFlow<List<OpportunityClosingAlertItemDto>> = combine(
        _oppAlertsPaging, _oppAlertsSearch, _oppAlertsOverdueOnly
    ) { paging, query, overdueOnly ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.opportunityCode.lowercase().contains(q) || item.name.lowercase().contains(q) ||
                    item.clientName.lowercase().contains(q)) && (!overdueOnly || item.isOverdue)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadOppClosingAlertsReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<OpportunityClosingAlertsResponse, OpportunityClosingAlertItemDto>(
            "opp_alerts", SALES_OPP_CLOSING_ALERTS_URL, isLoadMore, _oppAlertsPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _oppAlertsSummary.value = s } }
        )
    }
    fun onOppAlertsSearchChange(q: String) { _oppAlertsSearch.value = q }
    fun applyOppAlertsFilters(overdueOnly: Boolean) { _oppAlertsOverdueOnly.value = overdueOnly }
    fun clearOppAlertsFilters() { _oppAlertsSearch.value = ""; _oppAlertsOverdueOnly.value = false }

    // --- 10.8 Customer Master Directory ---
    private val _custMasterSummary = MutableStateFlow<CustomerMasterRegisterSummaryDto?>(null)
    val custMasterSummary: StateFlow<CustomerMasterRegisterSummaryDto?> = _custMasterSummary.asStateFlow()
    private val _custMasterPaging = MutableStateFlow(PagingUiState<CustomerMasterRegisterItemDto>())
    val custMasterPaging: StateFlow<PagingUiState<CustomerMasterRegisterItemDto>> = _custMasterPaging.asStateFlow()
    private val _custMasterSearch = MutableStateFlow("")
    val custMasterSearch: StateFlow<String> = _custMasterSearch.asStateFlow()
    private val _custMasterLevelFilter = MutableStateFlow<Set<String>>(emptySet())

    val filteredCustMasterList: StateFlow<List<CustomerMasterRegisterItemDto>> = combine(
        _custMasterPaging, _custMasterSearch, _custMasterLevelFilter
    ) { paging, query, levels ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.fullName.lowercase().contains(q) || item.customerCode.lowercase().contains(q) ||
                    item.mobileNumber.contains(q) || item.location.lowercase().contains(q)) &&
                    (levels.isEmpty() || levels.any { it.equals(item.customerLevel, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadCustMasterReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<CustomerMasterRegisterResponse, CustomerMasterRegisterItemDto>(
            "cust_master", SALES_CUST_MASTER_URL, isLoadMore, _custMasterPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _custMasterSummary.value = s } }
        )
    }
    fun onCustMasterSearchChange(q: String) { _custMasterSearch.value = q }
    fun applyCustMasterFilters(levels: Set<String>) { _custMasterLevelFilter.value = levels }
    fun clearCustMasterFilters() { _custMasterSearch.value = ""; _custMasterLevelFilter.value = emptySet() }

    // --- 10.9 Customer LTV & Revenue ---
    private val _custLtvSummary = MutableStateFlow<CustomerLtvRevenueSummaryDto?>(null)
    val custLtvSummary: StateFlow<CustomerLtvRevenueSummaryDto?> = _custLtvSummary.asStateFlow()
    private val _custLtvPaging = MutableStateFlow(PagingUiState<CustomerLtvRevenueItemDto>())
    val custLtvPaging: StateFlow<PagingUiState<CustomerLtvRevenueItemDto>> = _custLtvPaging.asStateFlow()
    private val _custLtvSearch = MutableStateFlow("")
    val custLtvSearch: StateFlow<String> = _custLtvSearch.asStateFlow()
    private val _custLtvLevelFilter = MutableStateFlow<Set<String>>(emptySet())

    val filteredCustLtvList: StateFlow<List<CustomerLtvRevenueItemDto>> = combine(
        _custLtvPaging, _custLtvSearch, _custLtvLevelFilter
    ) { paging, query, levels ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.fullName.lowercase().contains(q) || item.customerCode.lowercase().contains(q)) &&
                    (levels.isEmpty() || levels.any { it.equals(item.customerLevel, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadCustLtvReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<CustomerLtvRevenueResponse, CustomerLtvRevenueItemDto>(
            "cust_ltv", SALES_CUST_LTV_URL, isLoadMore, _custLtvPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _custLtvSummary.value = s } }
        )
    }
    fun onCustLtvSearchChange(q: String) { _custLtvSearch.value = q }
    fun applyCustLtvFilters(levels: Set<String>) { _custLtvLevelFilter.value = levels }
    fun clearCustLtvFilters() { _custLtvSearch.value = ""; _custLtvLevelFilter.value = emptySet() }

    // --- 10.10 Customer Credit & Receivables ---
    private val _custCreditSummary = MutableStateFlow<CustomerCreditReceivablesSummaryDto?>(null)
    val custCreditSummary: StateFlow<CustomerCreditReceivablesSummaryDto?> = _custCreditSummary.asStateFlow()
    private val _custCreditPaging = MutableStateFlow(PagingUiState<CustomerCreditReceivableItemDto>())
    val custCreditPaging: StateFlow<PagingUiState<CustomerCreditReceivableItemDto>> = _custCreditPaging.asStateFlow()
    private val _custCreditSearch = MutableStateFlow("")
    val custCreditSearch: StateFlow<String> = _custCreditSearch.asStateFlow()

    val filteredCustCreditList: StateFlow<List<CustomerCreditReceivableItemDto>> = combine(
        _custCreditPaging, _custCreditSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.fullName.lowercase().contains(q) || it.customerCode.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadCustCreditReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<CustomerCreditReceivablesResponse, CustomerCreditReceivableItemDto>(
            "cust_credit", SALES_CUST_CREDIT_URL, isLoadMore, _custCreditPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _custCreditSummary.value = s } }
        )
    }
    fun onCustCreditSearchChange(q: String) { _custCreditSearch.value = q }

    // --- 10.11 Dormant Customer Win-back ---
    private val _custDormantSummary = MutableStateFlow<CustomerDormantWinbackSummaryDto?>(null)
    val custDormantSummary: StateFlow<CustomerDormantWinbackSummaryDto?> = _custDormantSummary.asStateFlow()
    private val _custDormantPaging = MutableStateFlow(PagingUiState<CustomerDormantWinbackItemDto>())
    val custDormantPaging: StateFlow<PagingUiState<CustomerDormantWinbackItemDto>> = _custDormantPaging.asStateFlow()
    private val _custDormantSearch = MutableStateFlow("")
    val custDormantSearch: StateFlow<String> = _custDormantSearch.asStateFlow()

    val filteredCustDormantList: StateFlow<List<CustomerDormantWinbackItemDto>> = combine(
        _custDormantPaging, _custDormantSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.fullName.lowercase().contains(q) || it.customerCode.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadCustDormantReport(isLoadMore: Boolean = false, inactivityDays: Int = 90) = viewModelScope.launch {
        executeSalesPagedFetch<CustomerDormantWinbackResponse, CustomerDormantWinbackItemDto>(
            "cust_dormant", SALES_CUST_DORMANT_URL, isLoadMore, _custDormantPaging,
            extraQuery = listOf("inactivityDays" to inactivityDays),
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _custDormantSummary.value = s } }
        )
    }
    fun onCustDormantSearchChange(q: String) { _custDormantSearch.value = q }

    // --- 10.12 Measurement Master Register ---
    private val _measMasterSummary = MutableStateFlow<MeasurementMasterRegisterSummaryDto?>(null)
    val measMasterSummary: StateFlow<MeasurementMasterRegisterSummaryDto?> = _measMasterSummary.asStateFlow()
    private val _measMasterPaging = MutableStateFlow(PagingUiState<MeasurementMasterRegisterItemDto>())
    val measMasterPaging: StateFlow<PagingUiState<MeasurementMasterRegisterItemDto>> = _measMasterPaging.asStateFlow()
    private val _measMasterSearch = MutableStateFlow("")
    val measMasterSearch: StateFlow<String> = _measMasterSearch.asStateFlow()
    private val _measMasterSegmentFilter = MutableStateFlow<Set<String>>(emptySet())

    val filteredMeasMasterList: StateFlow<List<MeasurementMasterRegisterItemDto>> = combine(
        _measMasterPaging, _measMasterSearch, _measMasterSegmentFilter
    ) { paging, query, segments ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.fullName.lowercase().contains(q) || item.customerCode.lowercase().contains(q) ||
                    item.garmentName.lowercase().contains(q) || item.categoryName.lowercase().contains(q)) &&
                    (segments.isEmpty() || segments.any { it.equals(item.segmentName, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadMeasMasterReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<MeasurementMasterRegisterResponse, MeasurementMasterRegisterItemDto>(
            "meas_master", SALES_MEAS_MASTER_URL, isLoadMore, _measMasterPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _measMasterSummary.value = s } }
        )
    }
    fun onMeasMasterSearchChange(q: String) { _measMasterSearch.value = q }
    fun applyMeasMasterFilters(segments: Set<String>) { _measMasterSegmentFilter.value = segments }
    fun clearMeasMasterFilters() { _measMasterSearch.value = ""; _measMasterSegmentFilter.value = emptySet() }

    // --- 10.13 Stale Measurement Audit ---
    private val _measStaleSummary = MutableStateFlow<MeasurementStaleAuditSummaryDto?>(null)
    val measStaleSummary: StateFlow<MeasurementStaleAuditSummaryDto?> = _measStaleSummary.asStateFlow()
    private val _measStalePaging = MutableStateFlow(PagingUiState<MeasurementStaleAuditItemDto>())
    val measStalePaging: StateFlow<PagingUiState<MeasurementStaleAuditItemDto>> = _measStalePaging.asStateFlow()
    private val _measStaleSearch = MutableStateFlow("")
    val measStaleSearch: StateFlow<String> = _measStaleSearch.asStateFlow()

    val filteredMeasStaleList: StateFlow<List<MeasurementStaleAuditItemDto>> = combine(
        _measStalePaging, _measStaleSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.fullName.lowercase().contains(q) || it.customerCode.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadMeasStaleReport(isLoadMore: Boolean = false, staleMonths: Int = 6) = viewModelScope.launch {
        executeSalesPagedFetch<MeasurementStaleAuditResponse, MeasurementStaleAuditItemDto>(
            "meas_stale", SALES_MEAS_STALE_URL, isLoadMore, _measStalePaging,
            extraQuery = listOf("staleMonths" to staleMonths),
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _measStaleSummary.value = s } }
        )
    }
    fun onMeasStaleSearchChange(q: String) { _measStaleSearch.value = q }

    // --- 10.14 Order Master Register ---
    private val _orderMasterSummary = MutableStateFlow<OrderMasterRegisterSummaryDto?>(null)
    val orderMasterSummary: StateFlow<OrderMasterRegisterSummaryDto?> = _orderMasterSummary.asStateFlow()
    private val _orderMasterPaging = MutableStateFlow(PagingUiState<OrderMasterRegisterItemDto>())
    val orderMasterPaging: StateFlow<PagingUiState<OrderMasterRegisterItemDto>> = _orderMasterPaging.asStateFlow()
    private val _orderMasterSearch = MutableStateFlow("")
    val orderMasterSearch: StateFlow<String> = _orderMasterSearch.asStateFlow()
    private val _orderMasterPaymentFilter = MutableStateFlow<Set<String>>(emptySet())

    val filteredOrderMasterList: StateFlow<List<OrderMasterRegisterItemDto>> = combine(
        _orderMasterPaging, _orderMasterSearch, _orderMasterPaymentFilter
    ) { paging, query, payStatuses ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.orderCode.lowercase().contains(q) || item.clientName.lowercase().contains(q) ||
                    item.itemPreview.lowercase().contains(q) || item.status.lowercase().contains(q)) &&
                    (payStatuses.isEmpty() || payStatuses.any { it.equals(item.paymentStatus, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadOrderMasterReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<OrderMasterRegisterResponse, OrderMasterRegisterItemDto>(
            "order_master", SALES_ORDER_MASTER_URL, isLoadMore, _orderMasterPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _orderMasterSummary.value = s } }
        )
    }
    fun onOrderMasterSearchChange(q: String) { _orderMasterSearch.value = q }
    fun applyOrderMasterFilters(payStatuses: Set<String>) { _orderMasterPaymentFilter.value = payStatuses }
    fun clearOrderMasterFilters() { _orderMasterSearch.value = ""; _orderMasterPaymentFilter.value = emptySet() }

    // --- 10.15 Product Stream ---
    private val _productStreamSummary = MutableStateFlow<OrderProductStreamSummaryDto?>(null)
    val productStreamSummary: StateFlow<OrderProductStreamSummaryDto?> = _productStreamSummary.asStateFlow()
    private val _productStreamPaging = MutableStateFlow(PagingUiState<OrderProductStreamItemDto>())
    val productStreamPaging: StateFlow<PagingUiState<OrderProductStreamItemDto>> = _productStreamPaging.asStateFlow()
    private val _productStreamSearch = MutableStateFlow("")
    val productStreamSearch: StateFlow<String> = _productStreamSearch.asStateFlow()

    val filteredProductStreamList: StateFlow<List<OrderProductStreamItemDto>> = combine(
        _productStreamPaging, _productStreamSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.label.lowercase().contains(q) || it.lineType.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadProductStreamReport() = viewModelScope.launch {
        executeSalesNonPagedFetch<OrderProductStreamResponse, OrderProductStreamItemDto>(
            SALES_ORDER_PRODUCT_STREAM_URL, _productStreamPaging,
            extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, onSummaryUpdate = { it.summary?.let { s -> _productStreamSummary.value = s } }
        )
    }
    fun onProductStreamSearchChange(q: String) { _productStreamSearch.value = q }

    // --- 10.16 Delivery SLA ---
    private val _deliverySlaSummary = MutableStateFlow<OrderDeliverySlaSummaryDto?>(null)
    val deliverySlaSummary: StateFlow<OrderDeliverySlaSummaryDto?> = _deliverySlaSummary.asStateFlow()
    private val _deliverySlaPaging = MutableStateFlow(PagingUiState<OrderDeliverySlaItemDto>())
    val deliverySlaPaging: StateFlow<PagingUiState<OrderDeliverySlaItemDto>> = _deliverySlaPaging.asStateFlow()
    private val _deliverySlaSearch = MutableStateFlow("")
    val deliverySlaSearch: StateFlow<String> = _deliverySlaSearch.asStateFlow()

    val filteredDeliverySlaList: StateFlow<List<OrderDeliverySlaItemDto>> = combine(
        _deliverySlaPaging, _deliverySlaSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.orderCode.lowercase().contains(q) || it.clientName.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadDeliverySlaReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<OrderDeliverySlaResponse, OrderDeliverySlaItemDto>(
            "delivery_sla", SALES_ORDER_DELIVERY_SLA_URL, isLoadMore, _deliverySlaPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _deliverySlaSummary.value = s } }
        )
    }
    fun onDeliverySlaSearchChange(q: String) { _deliverySlaSearch.value = q }

    // --- 10.17 Branch-Wise Sales ---
    private val _branchSalesSummary = MutableStateFlow<BranchWiseSalesSummaryDto?>(null)
    val branchSalesSummary: StateFlow<BranchWiseSalesSummaryDto?> = _branchSalesSummary.asStateFlow()
    private val _branchSalesPaging = MutableStateFlow(PagingUiState<BranchWiseSalesItemDto>())
    val branchSalesPaging: StateFlow<PagingUiState<BranchWiseSalesItemDto>> = _branchSalesPaging.asStateFlow()
    private val _branchSalesSearch = MutableStateFlow("")
    val branchSalesSearch: StateFlow<String> = _branchSalesSearch.asStateFlow()

    val filteredBranchSalesList: StateFlow<List<BranchWiseSalesItemDto>> = combine(
        _branchSalesPaging, _branchSalesSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.branchName.lowercase().contains(q) || it.branchCode.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadBranchWiseSalesReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<BranchWiseSalesResponse, BranchWiseSalesItemDto>(
            "branch_sales", SALES_BRANCH_WISE_URL, isLoadMore, _branchSalesPaging,
            itemId = { it.branchId }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _branchSalesSummary.value = s } }
        )
    }
    fun onBranchSalesSearchChange(q: String) { _branchSalesSearch.value = q }

    // --- 10.18 Quotation Master Register ---
    private val _quoteMasterSummary = MutableStateFlow<QuotationMasterRegisterSummaryDto?>(null)
    val quoteMasterSummary: StateFlow<QuotationMasterRegisterSummaryDto?> = _quoteMasterSummary.asStateFlow()
    private val _quoteMasterPaging = MutableStateFlow(PagingUiState<QuotationMasterRegisterItemDto>())
    val quoteMasterPaging: StateFlow<PagingUiState<QuotationMasterRegisterItemDto>> = _quoteMasterPaging.asStateFlow()
    private val _quoteMasterSearch = MutableStateFlow("")
    val quoteMasterSearch: StateFlow<String> = _quoteMasterSearch.asStateFlow()
    private val _quoteMasterStatusFilter = MutableStateFlow<Set<String>>(emptySet())

    val filteredQuoteMasterList: StateFlow<List<QuotationMasterRegisterItemDto>> = combine(
        _quoteMasterPaging, _quoteMasterSearch, _quoteMasterStatusFilter
    ) { paging, query, statuses ->
        val q = query.trim().lowercase()
        paging.items.filter { item ->
            (q.isEmpty() || item.quotationCode.lowercase().contains(q) || item.clientName.lowercase().contains(q) ||
                    item.itemPreview.lowercase().contains(q)) &&
                    (statuses.isEmpty() || statuses.any { it.equals(item.status, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadQuoteMasterReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<QuotationMasterRegisterResponse, QuotationMasterRegisterItemDto>(
            "quote_master", SALES_QUOTE_MASTER_URL, isLoadMore, _quoteMasterPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _quoteMasterSummary.value = s } }
        )
    }
    fun onQuoteMasterSearchChange(q: String) { _quoteMasterSearch.value = q }
    fun applyQuoteMasterFilters(statuses: Set<String>) { _quoteMasterStatusFilter.value = statuses }
    fun clearQuoteMasterFilters() { _quoteMasterSearch.value = ""; _quoteMasterStatusFilter.value = emptySet() }

    // --- 10.19 Discount Leakage ---
    private val _quoteDiscountSummary = MutableStateFlow<QuotationDiscountLeakageSummaryDto?>(null)
    val quoteDiscountSummary: StateFlow<QuotationDiscountLeakageSummaryDto?> = _quoteDiscountSummary.asStateFlow()
    private val _quoteDiscountPaging = MutableStateFlow(PagingUiState<QuotationDiscountLeakageItemDto>())
    val quoteDiscountPaging: StateFlow<PagingUiState<QuotationDiscountLeakageItemDto>> = _quoteDiscountPaging.asStateFlow()
    private val _quoteDiscountSearch = MutableStateFlow("")
    val quoteDiscountSearch: StateFlow<String> = _quoteDiscountSearch.asStateFlow()

    val filteredQuoteDiscountList: StateFlow<List<QuotationDiscountLeakageItemDto>> = combine(
        _quoteDiscountPaging, _quoteDiscountSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.quotationCode.lowercase().contains(q) || it.clientName.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadQuoteDiscountReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<QuotationDiscountLeakageResponse, QuotationDiscountLeakageItemDto>(
            "quote_discount", SALES_QUOTE_DISCOUNT_URL, isLoadMore, _quoteDiscountPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _quoteDiscountSummary.value = s } }
        )
    }
    fun onQuoteDiscountSearchChange(q: String) { _quoteDiscountSearch.value = q }

    // --- 10.20 Expiring Quotations ---
    private val _quoteExpiringSummary = MutableStateFlow<QuotationExpiringAuditSummaryDto?>(null)
    val quoteExpiringSummary: StateFlow<QuotationExpiringAuditSummaryDto?> = _quoteExpiringSummary.asStateFlow()
    private val _quoteExpiringPaging = MutableStateFlow(PagingUiState<QuotationExpiringAuditItemDto>())
    val quoteExpiringPaging: StateFlow<PagingUiState<QuotationExpiringAuditItemDto>> = _quoteExpiringPaging.asStateFlow()
    private val _quoteExpiringSearch = MutableStateFlow("")
    val quoteExpiringSearch: StateFlow<String> = _quoteExpiringSearch.asStateFlow()

    val filteredQuoteExpiringList: StateFlow<List<QuotationExpiringAuditItemDto>> = combine(
        _quoteExpiringPaging, _quoteExpiringSearch
    ) { paging, query ->
        val q = query.trim().lowercase()
        paging.items.filter { q.isEmpty() || it.quotationCode.lowercase().contains(q) || it.clientName.lowercase().contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadQuoteExpiringReport(isLoadMore: Boolean = false) = viewModelScope.launch {
        executeSalesPagedFetch<QuotationExpiringAuditResponse, QuotationExpiringAuditItemDto>(
            "quote_expiring", SALES_QUOTE_EXPIRING_URL, isLoadMore, _quoteExpiringPaging,
            itemId = { it.id }, extractSuccess = { it.success }, extractMessage = { it.message },
            extractData = { it.data }, extractPagination = { it.pagination },
            onSummaryUpdate = { it.summary?.let { s -> _quoteExpiringSummary.value = s } }
        )
    }
    fun onQuoteExpiringSearchChange(q: String) { _quoteExpiringSearch.value = q }

    // =========================================================================
    // 5. FORMATTING HELPERS
    // =========================================================================
    fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed = parser.parse(isoString) ?: return "--"
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(parsed)
        } catch (_: Exception) {
            "--"
        }
    }

    fun formatTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed = parser.parse(isoString) ?: return "--"
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(parsed)
        } catch (_: Exception) {
            "--"
        }
    }

    fun formatMinutesToHours(minutes: Int): String {
        if (minutes <= 0) return "--"
        val hours = minutes / 60.0
        return String.format(Locale.US, "%.1f hrs", hours)
    }

    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        }
        return "\u20B9${formatter.format(amount)}"
    }

    fun formatStatusLabel(raw: String): String {
        if (raw.isBlank()) return "--"
        return raw.replace("_", " ").trim().split("\\s+".toRegex()).joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
            }
        }
    }

    fun formatHoursFromMinutesZeroAllowed(minutes: Int): String {
        val hours = (minutes / 60.0 * 10.0).roundToInt() / 10.0
        return "$hours hrs"
    }
}