@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter"
)
package com.cuso.tailor.viewmodel

import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.hr.ActiveExpenseTypeDto
import com.cuso.tailor.model.hr.ActiveExpenseTypeListResponse
import com.cuso.tailor.model.hr.AssignedTrainingListResponse
import com.cuso.tailor.model.hr.AssignedTrainingProgramDto
import com.cuso.tailor.model.hr.AssignedTrainingSingleResponse
import com.cuso.tailor.model.hr.AttendanceRecord
import com.cuso.tailor.model.hr.CreateEmployeeDocumentResponse
import com.cuso.tailor.model.hr.CreateManualAttendanceRequest
import com.cuso.tailor.model.hr.CreateMemberRequest
import com.cuso.tailor.model.hr.CreateRoleRequest
import com.cuso.tailor.model.hr.CreateShiftRequest
import com.cuso.tailor.model.hr.CreatedMemberFullData
import com.cuso.tailor.model.hr.DeleteDocumentCategoryResponse
import com.cuso.tailor.model.hr.DeleteEmployeeDocumentResponse
import com.cuso.tailor.model.hr.DeleteExpenseClaimResponse
import com.cuso.tailor.model.hr.DocumentCategoryDto
import com.cuso.tailor.model.hr.DocumentCategoryDtoCat
import com.cuso.tailor.model.hr.DocumentCategoryListResponse
import com.cuso.tailor.model.hr.DocumentCategoryListResponseCat
import com.cuso.tailor.model.hr.DocumentCategorySingleResponse
import com.cuso.tailor.model.hr.EmployeeDocumentDto
import com.cuso.tailor.model.hr.EmployeeDocumentListResponse
import com.cuso.tailor.model.hr.EmployeeDocumentSingleResponse
import com.cuso.tailor.model.hr.CreateExitSettlementRequest
import com.cuso.tailor.model.hr.ExitApprovalDecisionRequest
import com.cuso.tailor.model.hr.ExitAssetAssignmentRequest
import com.cuso.tailor.model.hr.ExitSettlementCalculationDto
import com.cuso.tailor.model.hr.ExitSettlementCalculationResponse
import com.cuso.tailor.model.hr.ExitSettlementDto
import com.cuso.tailor.model.hr.ExitSettlementResponse
import com.cuso.tailor.model.hr.ExitAssignedAssetDto
import com.cuso.tailor.model.hr.ExitAssignedAssetListResponse
import com.cuso.tailor.model.hr.ExitClearanceFinalizeRequest
import com.cuso.tailor.model.hr.ExitClearanceItemStatusRequest
import com.cuso.tailor.model.hr.ExitRequestDto
import com.cuso.tailor.model.hr.ExitRequestListResponse
import com.cuso.tailor.model.hr.ExitRequestSingleResponse
import com.cuso.tailor.model.hr.ExitStatusUpdateRequest
import com.cuso.tailor.model.hr.ExpenseClaimDto
import com.cuso.tailor.model.hr.ExpenseClaimPaymentDetails
import com.cuso.tailor.model.hr.ExpenseClaimSingleResponse
import com.cuso.tailor.model.hr.ExpenseClaimsApiResponse
import com.cuso.tailor.model.hr.ExpenseTypeDto
import com.cuso.tailor.model.hr.ExpenseTypeListApiResponse
import com.cuso.tailor.model.hr.LeaveRequestItemDto
import com.cuso.tailor.model.hr.LeaveRequestsResponse
import com.cuso.tailor.model.hr.LeaveTypeItemDto
import com.cuso.tailor.model.hr.MemberDetail
import com.cuso.tailor.model.hr.MemberDetailResponse
import com.cuso.tailor.model.hr.MemberItem
import com.cuso.tailor.model.hr.MonthlyAttendanceItem
import com.cuso.tailor.model.hr.OrgMemberDto
import com.cuso.tailor.model.hr.OrgStructureResponse
import com.cuso.tailor.model.hr.RoleData
import com.cuso.tailor.model.hr.RoleItem
import com.cuso.tailor.model.hr.RoleResponse
import com.cuso.tailor.model.hr.SalaryComponentItem
import com.cuso.tailor.model.hr.SalaryComponentRequest
import com.cuso.tailor.model.hr.ShiftDetailData
import com.cuso.tailor.model.hr.ShiftItem
import com.cuso.tailor.model.hr.UpdateMemberRequest
import com.cuso.tailor.model.hr.UpdateShiftRequest
import com.cuso.tailor.repository.HrRepository
import com.cuso.tailor.utils.convert12HrTo24Hr
import com.cuso.tailor.model.hr.SalaryComponentListResponse
import com.cuso.tailor.model.hr.SalaryComponentSingleResponse
import com.cuso.tailor.model.hr.SalaryTemplateDto
import com.cuso.tailor.model.hr.SalaryTemplateListResponse
import com.cuso.tailor.model.hr.SalaryTemplateSingleResponse
import com.cuso.tailor.model.hr.SaveAssignedTrainingRequest
import com.cuso.tailor.model.hr.SaveDocumentCategoryRequest
import com.cuso.tailor.model.hr.SaveSalaryTemplateRequest
import com.cuso.tailor.model.hr.SaveTrainingProgramRequest
import com.cuso.tailor.model.hr.SimpleActionResponse
import com.cuso.tailor.model.hr.ToggleSalaryComponentStatusRequest
import com.cuso.tailor.model.hr.TrainingProgramDto
import com.cuso.tailor.model.hr.TrainingProgramListResponse
import com.cuso.tailor.model.hr.TrainingProgramSingleResponse
import com.cuso.tailor.model.hr.CreateUpdateProductionTeamRequest
import com.cuso.tailor.model.hr.ProductionTeamAssignedWorkData
import com.cuso.tailor.model.hr.ProductionTeamAssignedWorkResponse
import com.cuso.tailor.model.hr.ProductionTeamDto
import com.cuso.tailor.model.hr.ProductionTeamKpiSummary
import com.cuso.tailor.model.hr.ProductionTeamListResponse
import com.cuso.tailor.model.hr.ProductionTeamSingleResponse
import com.cuso.tailor.model.hr.UpdateExpenseClaimResponse
import com.cuso.tailor.model.hr.UpdateExpenseClaimStatusRequest
import com.cuso.tailor.model.hr.UpdateProductionTeamStatusRequest
import com.cuso.tailor.model.hr.activeMembers
import com.cuso.tailor.model.hr.availabilityStatus
import com.cuso.tailor.model.hr.displayStatus
import com.cuso.tailor.model.hr.extractMemberName
import com.cuso.tailor.model.hr.resolveAssetId
import com.cuso.tailor.repository.ApiRepository
import com.cuso.tailor.utils.PagingUiState
import com.cuso.tailor.utils.UiState // explicit import wins over same-package UiState
import com.cuso.tailor.utils.launchState
import com.cuso.tailor.utils.convertUiDateToApiDate
import com.cuso.tailor.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject
import kotlin.collections.filter
import com.cuso.tailor.model.hr.LeaveBalanceItemDto
import com.cuso.tailor.model.hr.LeaveBalanceListResponse
import com.cuso.tailor.model.hr.LeaveDashboardMetrics
import com.cuso.tailor.model.hr.LeaveUtilizationUiItem
import com.cuso.tailor.model.hr.MonthlyUsageTrendUiItem
import com.cuso.tailor.model.hr.extractAllocatedDays
import com.cuso.tailor.model.hr.extractEmployeeCode
import com.cuso.tailor.model.hr.extractEmployeeName
import com.cuso.tailor.model.hr.extractLeaveTypeId
import com.cuso.tailor.model.hr.extractLeaveTypeName
import com.cuso.tailor.model.hr.extractRemainingDays
import com.cuso.tailor.model.hr.extractUsedDays
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
// Base path for all salary component endpoints
private const val SALARY_BASE = "/api/hr/salary-component"
// Add constant at top of HrViewModel.kt
private const val EXPENSE_TYPE_BASE = "/api/hr/expense-type"
private const val LEAVE_BALANCE_BASE = "/api/hr/leave-balance"
private const val LEAVE_REQUEST_BASE = "/api/hr/leave-request"


private const val CREATE_ROLE_URL = "/api/roles/create"
private const val EXIT_REQUEST_BASE = "/api/hr/exit-request"
private const val EXIT_SETTLEMENT_BASE = "/api/hr/exit-settlement"
private const val PRODUCTION_TEAM_BASE = "/api/hr/production-team"

sealed interface AttendanceUiState {
    object Loading : AttendanceUiState
    data class Success(val records: List<AttendanceRecord>, val canLoadMore: Boolean) : AttendanceUiState
    data class Error(val message: String) : AttendanceUiState
}

/**
 * HrViewModel - Handles Roles, Shifts, Members (with infinite scroll pagination),
 * and Create/Update operations for the HR module.
 */
@HiltViewModel
class HrViewModel @Inject constructor(
    private val hrRepository: HrRepository,
    private val repo: ApiRepository // generic repository for new APIs
) : ViewModel() {

    // ═══════════════════════════════════════════════
    // ── Roles ──
    // ═══════════════════════════════════════════════
    private val _roles = MutableStateFlow<List<RoleItem>>(emptyList())
    val roles: StateFlow<List<RoleItem>> = _roles.asStateFlow()

    private val _isLoadingRoles = MutableStateFlow(false)
    val isLoadingRoles: StateFlow<Boolean> = _isLoadingRoles.asStateFlow()

    private val _rolesError = MutableStateFlow<String?>(null)
    val rolesError: StateFlow<String?> = _rolesError.asStateFlow()

    fun fetchRoles() {
        launchBusy {
            _isLoadingRoles.value = true
            _rolesError.value = null
            val result = hrRepository.getRoles()
            result.fold(
                onSuccess = { _roles.value = it },
                onFailure = { e -> _rolesError.value = e.message ?: "Failed to fetch roles" }
            )
            _isLoadingRoles.value = false
        }
    }

    // =========================================================================
    // ROLE MANAGEMENT (CREATE, VIEW-ONE, UPDATE-ONE)
    // =========================================================================

    private val _roleDetails = MutableStateFlow<RoleData?>(null)
    val roleDetails: StateFlow<RoleData?> = _roleDetails.asStateFlow()

    /**
     * Fetch single role by ID: GET /api/roles/view-one/{roleId}
     */
    fun fetchRoleById(roleId: String) {
        if (roleId.isBlank()) return
        viewModelScope.launch {
            _isRoleLoading.value = true
            _roleErrorMessage.value = null

            val result = repo.request<RoleResponse> {
                get("/api/roles/view-one/$roleId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _roleDetails.value = res.data
                    } else {
                        _roleErrorMessage.value = res.message ?: "Failed to fetch role details"
                    }
                },
                onFailure = { err ->
                    _roleErrorMessage.value = err.message ?: "Network error fetching role"
                }
            )
            _isRoleLoading.value = false
        }
    }

    /**
     * Create new role: POST /api/roles/create
     * OR Update existing role: PUT /api/roles/update-one/{roleId}
     */
    fun saveRole(
        roleIdToEdit: String?,
        name: String,
        roleCode: String,
        description: String,
        status: Boolean,
        isDefault: Boolean,
        accessScope: String,
        sessionTimeoutMinutes: Int,
        matrixPermissions: Map<String, Boolean>,
        hiddenSubmodules: Set<String>, // Added hiddenSubmodules parameter
        fieldPermissions: Map<String, Boolean>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isRoleLoading.value = true
            _roleErrorMessage.value = null

            // 1. Build nested permissions map holding both submodule actions and "hidden": true
            val nestedPermissions = mutableMapOf<String, MutableMap<String, Any>>()

            // Populate granted actions
            matrixPermissions.forEach { (key, isGranted) ->
                if (isGranted) {
                    val segments = key.split(".")
                    if (segments.size == 3) {
                        val (moduleKey, submoduleKey, actionKey) = segments
                        val moduleMap = nestedPermissions.getOrPut(moduleKey) { mutableMapOf() }

                        @Suppress("UNCHECKED_CAST")
                        val subMap = moduleMap.getOrPut(submoduleKey) { mutableMapOf<String, Boolean>() } as MutableMap<String, Boolean>
                        subMap[actionKey] = true
                    }
                }
            }

            // 2. Insert "hidden": true for modules/submodules that were toggled with eye icon
            hiddenSubmodules.forEach { hiddenKey ->
                val segments = hiddenKey.split(".")
                if (segments.size == 2) {
                    val (moduleKey, submoduleKey) = segments
                    val moduleMap = nestedPermissions.getOrPut(moduleKey) { mutableMapOf() }

                    // Check if the whole module is hidden or a specific submodule
                    if (submoduleKey == "module_root") {
                        moduleMap["hidden"] = true
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val subMap = moduleMap.getOrPut(submoduleKey) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
                        subMap["hidden"] = true
                    }
                }
            }

            // 3. Convert field permissions
            val nestedFieldPermissions = mutableMapOf<String, MutableMap<String, MutableMap<String, Boolean>>>()
            fieldPermissions.forEach { (key, isGranted) ->
                val segments = key.split("_")
                if (segments.size == 3) {
                    val (groupKey, fieldKey, actionKey) = segments
                    nestedFieldPermissions
                        .getOrPut(groupKey) { mutableMapOf() }
                        .getOrPut(fieldKey) { mutableMapOf() }[actionKey] = isGranted
                }
            }

            val payload = CreateRoleRequest(
                name = name.trim(),
                roleCode = roleCode.trim(),
                description = description.trim(),
                status = status,
                isDefault = isDefault,
                accessScope = accessScope,
                sessionTimeoutMinutes = sessionTimeoutMinutes,
                fieldPermissions = nestedFieldPermissions,
                permissions = nestedPermissions
            )

            val isEditMode = !roleIdToEdit.isNullOrBlank()
            val url = if (isEditMode) "/api/roles/update-one/$roleIdToEdit" else "/api/roles/create"

            val result = repo.request<RoleResponse> {
                if (isEditMode) put(url, payload) else post(url, payload)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) onSuccess() else _roleErrorMessage.value = res.message ?: "Failed to save role"
                },
                onFailure = { err ->
                    _roleErrorMessage.value = err.message ?: "Network error saving role"
                }
            )
            _isRoleLoading.value = false
        }
    }

    fun clearRoleDetails() {
        _roleDetails.value = null
    }

    // =========================================================================
    // ROLE MANAGEMENT STATES & API CALLS
    // =========================================================================

    private val _isRoleLoading = MutableStateFlow(false)
    val isRoleLoading: StateFlow<Boolean> = _isRoleLoading.asStateFlow()

    private val _roleErrorMessage = MutableStateFlow<String?>(null)
    val roleErrorMessage: StateFlow<String?> = _roleErrorMessage.asStateFlow()

    private val _createdRoleData = MutableStateFlow<RoleData?>(null)
    val createdRoleData: StateFlow<RoleData?> = _createdRoleData.asStateFlow()

    /**
     * Creates a new role using repository pattern matching the exact payload format.
     */
    fun createRole(
        name: String,
        roleCode: String,
        description: String,
        status: Boolean,
        isDefault: Boolean,
        accessScope: String,
        sessionTimeoutMinutes: Int,
        matrixPermissions: Map<String, Boolean>,
        fieldPermissions: Map<String, Boolean>,
        onSuccess: (RoleData) -> Unit
    ) {
        viewModelScope.launch {
            _isRoleLoading.value = true
            _roleErrorMessage.value = null

            // 1. Transform flat matrixPermissions ("home.branches.create" -> Boolean)
            // into nested structure: Map<Module, Map<Submodule, Map<Action, Boolean>>>
            val nestedPermissions = mutableMapOf<String, MutableMap<String, MutableMap<String, Boolean>>>()
            matrixPermissions.forEach { (key, isGranted) ->
                if (isGranted) {
                    val segments = key.split(".")
                    if (segments.size == 3) {
                        val moduleKey = segments[0]
                        val submoduleKey = segments[1]
                        val actionKey = segments[2]

                        nestedPermissions
                            .getOrPut(moduleKey) { mutableMapOf() }
                            .getOrPut(submoduleKey) { mutableMapOf() }[actionKey] = true
                    }
                }
            }

            // 2. Transform flat fieldPermissions ("sales_customerName_view" -> Boolean)
            // into nested structure: Map<Group, Map<Field, Map<Action, Boolean>>>
            val nestedFieldPermissions = mutableMapOf<String, MutableMap<String, MutableMap<String, Boolean>>>()
            fieldPermissions.forEach { (key, isGranted) ->
                val segments = key.split("_")
                if (segments.size == 3) {
                    val groupKey = segments[0]
                    val fieldKey = segments[1]
                    val actionKey = segments[2]

                    nestedFieldPermissions
                        .getOrPut(groupKey) { mutableMapOf() }
                        .getOrPut(fieldKey) { mutableMapOf() }[actionKey] = isGranted
                }
            }

            // 3. Prepare payload
            val payload = CreateRoleRequest(
                name = name.trim(),
                roleCode = roleCode.trim(),
                description = description.trim(),
                status = status,
                isDefault = isDefault,
                accessScope = accessScope,
                sessionTimeoutMinutes = sessionTimeoutMinutes,
                fieldPermissions = nestedFieldPermissions,
                permissions = nestedPermissions
            )

            // 4. API Request through repository
            val result = repo.request<RoleResponse> {
                post(CREATE_ROLE_URL, payload)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _createdRoleData.value = res.data
                        onSuccess(res.data)
                    } else {
                        _roleErrorMessage.value = res.message ?: "Failed to create role"
                    }
                },
                onFailure = { err ->
                    _roleErrorMessage.value = err.message ?: "Network error creating role"
                }
            )
            _isRoleLoading.value = false
        }
    }

    fun clearRoleError() {
        _roleErrorMessage.value = null
    }

    // ═══════════════════════════════════════════════
    // ── Shifts ──
    // ═══════════════════════════════════════════════
    private val _shifts = MutableStateFlow<List<ShiftItem>>(emptyList())
    val shifts: StateFlow<List<ShiftItem>> = _shifts.asStateFlow()

    private val _isLoadingShifts = MutableStateFlow(false)
    val isLoadingShifts: StateFlow<Boolean> = _isLoadingShifts.asStateFlow()

    private val _shiftsError = MutableStateFlow<String?>(null)
    val shiftsError: StateFlow<String?> = _shiftsError.asStateFlow()

    fun fetchShifts() {
        launchBusy {
            _isLoadingShifts.value = true
            _shiftsError.value = null
            val result = hrRepository.getShifts()
            result.fold(
                onSuccess = { _shifts.value = it },
                onFailure = { e -> _shiftsError.value = e.message ?: "Failed to fetch shifts" }
            )
            _isLoadingShifts.value = false
        }
    }
    // =========================================================================
    // EXPENSE CLAIMS
    // =========================================================================
    private val _expenseClaims = MutableStateFlow<List<ExpenseClaimDto>>(emptyList())
    val expenseClaims: StateFlow<List<ExpenseClaimDto>> = _expenseClaims.asStateFlow()

    private val _isLoadingExpenseClaims = MutableStateFlow(false)
    val isLoadingExpenseClaims: StateFlow<Boolean> = _isLoadingExpenseClaims.asStateFlow()

    private val _expenseClaimsError = MutableStateFlow<String?>(null)
    val expenseClaimsError: StateFlow<String?> = _expenseClaimsError.asStateFlow()

    private val _expenseClaimsPagingState = MutableStateFlow(PagingUiState<ExpenseClaimDto>())
    val expenseClaimsPagingState: StateFlow<PagingUiState<ExpenseClaimDto>> =
        _expenseClaimsPagingState.asStateFlow()

    private var expenseClaimCurrentPage = 1
    private val expenseClaimPageSize = 15
    private var activeExpenseClaimStatus: String? = "Pending_Approval"
    private var activeExpenseClaimSearch: String? = null

    /**
     * Fetches expense claims list with optional status or search filter
     */
    fun fetchExpenseClaims(
        page: Int = 1,
        limit: Int = expenseClaimPageSize,
        status: String? = "Pending_Approval",
        search: String? = null
    ) {
        expenseClaimCurrentPage = page
        activeExpenseClaimStatus = status
        activeExpenseClaimSearch = search

        viewModelScope.launch {
            _isLoadingExpenseClaims.value = true
            _expenseClaimsError.value = null
            _expenseClaimsPagingState.value = PagingUiState(isInitialLoading = true)

            val result = repo.request<ExpenseClaimsApiResponse> {
                get(
                    "/api/hr/expense-claim/view-all",
                    repo.query(
                        "page" to page,
                        "limit" to limit,
                        "status" to status,
                        "search" to search
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val list = res.data?.data ?: emptyList()
                    _expenseClaims.value = list
                    _expenseClaimsPagingState.value = PagingUiState(
                        items = list,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = list.size >= limit
                    )
                },
                onFailure = { error ->
                    _expenseClaimsError.value = error.message ?: "Failed to fetch expense claims"
                    _expenseClaimsPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
            _isLoadingExpenseClaims.value = false
        }
    }

    fun loadMoreExpenseClaims(limit: Int = expenseClaimPageSize) {
        val current = _expenseClaimsPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return

        viewModelScope.launch {
            _expenseClaimsPagingState.value = current.copy(isLoadingMore = true)
            val nextPage = expenseClaimCurrentPage + 1

            val result = repo.request<ExpenseClaimsApiResponse> {
                get(
                    "/api/hr/expense-claim/view-all",
                    repo.query(
                        "page" to nextPage,
                        "limit" to limit,
                        "status" to activeExpenseClaimStatus,
                        "search" to activeExpenseClaimSearch
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val newItems = res.data?.data ?: emptyList()
                    val merged = (current.items + newItems).distinctBy { it.id }
                    val hasNewItems = merged.size > current.items.size
                    if (hasNewItems) {
                        expenseClaimCurrentPage = nextPage
                        _expenseClaims.value = merged
                        _expenseClaimsPagingState.value = current.copy(
                            items = merged,
                            isLoadingMore = false,
                            canLoadMore = newItems.size >= limit
                        )
                    } else {
                        _expenseClaimsPagingState.value = current.copy(
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                },
                onFailure = {
                    _expenseClaimsPagingState.value = current.copy(isLoadingMore = false)
                }
            )
        }
    }
    /**
     * Fetches expense claims list
     * Endpoint: GET /api/hr/expense-claim/view-all?page=1&limit=10
     */
    fun fetchNewClaims(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null,
        search: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingExpenseClaims.value = true
            _expenseClaimsError.value = null

            val result = repo.request<ExpenseClaimsApiResponse> {
                get(
                    "/api/hr/expense-claim/view-all",
                    repo.query(
                        "page" to page,
                        "limit" to limit,
                        "status" to status,
                        "search" to search
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _expenseClaims.value = res.data?.data ?: emptyList()
                },
                onFailure = { error ->
                    _expenseClaimsError.value = error.message ?: "Failed to fetch expense claims"
                }
            )
            _isLoadingExpenseClaims.value = false
        }
    }

    // =========================================================================
    // ACTIVE EXPENSE TYPES (FOR CATEGORY DROPDOWN)
    // =========================================================================
    private val _activeExpenseTypes = MutableStateFlow<List<ActiveExpenseTypeDto>>(emptyList())
    val activeExpenseTypes: StateFlow<List<ActiveExpenseTypeDto>> = _activeExpenseTypes.asStateFlow()

    private val _isLoadingActiveExpenseTypes = MutableStateFlow(false)
    val isLoadingActiveExpenseTypes: StateFlow<Boolean> = _isLoadingActiveExpenseTypes.asStateFlow()

    private val _activeExpenseTypesError = MutableStateFlow<String?>(null)
    val activeExpenseTypesError: StateFlow<String?> = _activeExpenseTypesError.asStateFlow()

    /**
     * Fetches active expense types for dropdown
     * Endpoint: GET /api/hr/expense-type/active
     */
    fun fetchActiveExpenseTypes() {
        viewModelScope.launch {
            _isLoadingActiveExpenseTypes.value = true
            _activeExpenseTypesError.value = null

            val result = repo.request<ActiveExpenseTypeListResponse> {
                get("/api/hr/expense-type/active")
            }

            result.fold(
                onSuccess = { res ->
                    _activeExpenseTypes.value = res.data
                },
                onFailure = { error ->
                    _activeExpenseTypesError.value = error.message ?: "Failed to fetch active expense types"
                }
            )
            _isLoadingActiveExpenseTypes.value = false
        }
    }
    // ═══════════════════════════════════════════════════════════════════
    // ── Leave Dashboard & Leave Balances ──
    // ═══════════════════════════════════════════════════════════════════

    private val _isLoadingLeaveDashboard = MutableStateFlow(false)
    val isLoadingLeaveDashboard: StateFlow<Boolean> = _isLoadingLeaveDashboard.asStateFlow()

    private val _leaveDashboardError = MutableStateFlow<String?>(null)
    val leaveDashboardError: StateFlow<String?> = _leaveDashboardError.asStateFlow()

    private val _leaveDashboardMetrics = MutableStateFlow(LeaveDashboardMetrics())
    val leaveDashboardMetrics: StateFlow<LeaveDashboardMetrics> = _leaveDashboardMetrics.asStateFlow()

    private val _leaveUtilizationList = MutableStateFlow<List<LeaveUtilizationUiItem>>(emptyList())
    val leaveUtilizationList: StateFlow<List<LeaveUtilizationUiItem>> = _leaveUtilizationList.asStateFlow()

    private val _monthlyLeaveTrends = MutableStateFlow<List<MonthlyUsageTrendUiItem>>(emptyList())
    val monthlyLeaveTrends: StateFlow<List<MonthlyUsageTrendUiItem>> = _monthlyLeaveTrends.asStateFlow()

    private val _allLeaveBalances = MutableStateFlow<List<LeaveBalanceItemDto>>(emptyList())
    val allLeaveBalances: StateFlow<List<LeaveBalanceItemDto>> = _allLeaveBalances.asStateFlow()

    private val _leaveBalancesPagingState = MutableStateFlow(PagingUiState<LeaveBalanceItemDto>())
    val leaveBalancesPagingState: StateFlow<PagingUiState<LeaveBalanceItemDto>> =
        _leaveBalancesPagingState.asStateFlow()

    private val _leaveBalancesError = MutableStateFlow<String?>(null)
    val leaveBalancesError: StateFlow<String?> = _leaveBalancesError.asStateFlow()

    private var balanceCurrentPage = 1
    private val balancePageSize = 100
    private var balanceHasNextPage = false
    private var balanceSearchQuery = ""
    private var balanceSelectedLeaveTypeId = "all"
    private var balanceSelectedBudgetStatus = "all" // "all", "in_budget", "exhausted"

    /**
     * Fetches Leave Requests, Leave Balances, and Leave Types in parallel for the Leave Dashboard.
     */
    fun fetchLeaveDashboardData() {
        viewModelScope.launch {
            _isLoadingLeaveDashboard.value = true
            _leaveDashboardError.value = null

            try {
                coroutineScope {
                    val requestsDeferred = async {
                        repo.request<LeaveRequestsResponse> {
                            get("$LEAVE_REQUEST_BASE/view-all", repo.query("page" to 1, "limit" to 100))
                        }
                    }
                    val balancesDeferred = async {
                        repo.request<LeaveBalanceListResponse> {
                            get("$LEAVE_BALANCE_BASE/view-all", repo.query("page" to 1, "limit" to 100))
                        }
                    }
                    val typesDeferred = async {
                        hrRepository.getLeaveTypes()
                    }

                    val requestsResult = requestsDeferred.await()
                    val balancesResult = balancesDeferred.await()
                    val typesResult = typesDeferred.await()

                    val leaves = requestsResult.getOrNull()?.data?.data.orEmpty()
                    val balances = balancesResult.getOrNull()?.data.orEmpty()
                    val types = typesResult.getOrDefault(emptyList())

                    _leaveRequests.value = leaves
                    _allLeaveBalances.value = balances
                    _leaveTypes.value = types

                    // Build leave type lookup map (id -> name)
                    val typeNameMap = mutableMapOf<String, String>()
                    types.forEach { t ->
                        val id = t.id
                        if (id.isNotBlank() && t.name.isNotBlank()) {
                            typeNameMap[id] = t.name
                        }
                    }

                    // 1. Compute KPI counts
                    val approved = leaves.count { it.status.equals("Approved", ignoreCase = true) }
                    val pending = leaves.count { it.status.equals("Pending", ignoreCase = true) }
                    val rejected = leaves.count { it.status.equals("Rejected", ignoreCase = true) }

                    _leaveDashboardMetrics.value = LeaveDashboardMetrics(
                        totalLeaves = leaves.size,
                        approvedCount = approved,
                        pendingCount = pending,
                        rejectedCount = rejected
                    )

                    // 2. Compute Leave Balance Overview grouped by leave type
                    val grouped = linkedMapOf<String, Pair<Double, Double>>() // name -> (used, total)
                    balances.forEach { balance ->
                        val name = balance.extractLeaveTypeName(typeNameMap)
                        val allocated = balance.extractAllocatedDays()
                        val used = balance.extractUsedDays()
                        val current = grouped[name] ?: (0.0 to 0.0)
                        grouped[name] = (current.first + used) to (current.second + allocated)
                    }

                    _leaveUtilizationList.value = grouped.map { (name, totals) ->
                        val usedInt = totals.first.toInt()
                        val totalInt = totals.second.toInt()
                        val remainingInt = (totalInt - usedInt).coerceAtLeast(0)
                        val fraction = if (totalInt > 0) (usedInt.toFloat() / totalInt.toFloat()).coerceIn(0f, 1f) else 0f
                        LeaveUtilizationUiItem(
                            name = name,
                            used = usedInt,
                            total = totalInt,
                            remaining = remainingInt,
                            progress = fraction
                        )
                    }

                    // 3. Compute 6-Month Usage Trend
                    _monthlyLeaveTrends.value = buildSixMonthUsageTrends(leaves)

                    if (requestsResult.isFailure && balancesResult.isFailure) {
                        _leaveDashboardError.value = "Failed to load leave dashboard data"
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _leaveDashboardError.value = e.message ?: "Failed to load leave dashboard data"
                }
            } finally {
                _isLoadingLeaveDashboard.value = false
            }
        }
    }

    /**
     * Fetches paginated Leave Balances from GET /api/hr/leave-balance/view-all?limit=100
     */
    fun fetchLeaveBalances(limit: Int = balancePageSize) {
        balanceCurrentPage = 1
        viewModelScope.launch {
            _leaveBalancesError.value = null
            _leaveBalancesPagingState.value = PagingUiState(isInitialLoading = true)

            // Ensure leave types are loaded so leaveTypeId references resolve to display names
            if (_leaveTypes.value.isEmpty()) {
                hrRepository.getLeaveTypes().onSuccess { _leaveTypes.value = it }
            }

            val result = repo.request<LeaveBalanceListResponse> {
                get(
                    "$LEAVE_BALANCE_BASE/view-all",
                    repo.query("page" to balanceCurrentPage, "limit" to limit)
                )
            }

            result.fold(
                onSuccess = { res ->
                    _allLeaveBalances.value = res.data
                    balanceHasNextPage = res.pagination?.hasNextPage ?: (res.data.size >= limit)
                    rebuildLeaveBalancesPagingState(isLoadingMore = false)
                },
                onFailure = { error ->
                    _leaveBalancesError.value = error.message ?: "Failed to fetch leave balances"
                    _leaveBalancesPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
        }
    }

    /**
     * Loads the next page of Leave Balances when scrolling to the bottom
     */
    fun loadMoreLeaveBalances(limit: Int = balancePageSize) {
        val current = _leaveBalancesPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !balanceHasNextPage) return

        viewModelScope.launch {
            _leaveBalancesPagingState.value = current.copy(isLoadingMore = true)
            val nextPage = balanceCurrentPage + 1

            val result = repo.request<LeaveBalanceListResponse> {
                get(
                    "$LEAVE_BALANCE_BASE/view-all",
                    repo.query("page" to nextPage, "limit" to limit)
                )
            }

            result.fold(
                onSuccess = { res ->
                    val merged = (_allLeaveBalances.value + res.data).distinctBy {
                        it.resolvedId.ifBlank { "${it.extractEmployeeName()}_${it.extractLeaveTypeId()}" }
                    }
                    if (merged.size > _allLeaveBalances.value.size) {
                        balanceCurrentPage = nextPage
                    }
                    _allLeaveBalances.value = merged
                    balanceHasNextPage = res.pagination?.hasNextPage ?: (res.data.size >= limit)
                    rebuildLeaveBalancesPagingState(isLoadingMore = false)
                },
                onFailure = {
                    _leaveBalancesPagingState.value = current.copy(isLoadingMore = false)
                }
            )
        }
    }

    fun onLeaveBalanceSearchChanged(query: String) {
        balanceSearchQuery = query
        rebuildLeaveBalancesPagingState(isLoadingMore = false)
    }

    fun applyLeaveBalanceFilters(leaveTypeId: String, budgetStatus: String) {
        balanceSelectedLeaveTypeId = leaveTypeId
        balanceSelectedBudgetStatus = budgetStatus
        rebuildLeaveBalancesPagingState(isLoadingMore = false)
    }

    fun resetLeaveBalanceFilters() {
        balanceSearchQuery = ""
        balanceSelectedLeaveTypeId = "all"
        balanceSelectedBudgetStatus = "all"
        rebuildLeaveBalancesPagingState(isLoadingMore = false)
    }

    private fun rebuildLeaveBalancesPagingState(isLoadingMore: Boolean) {
        val typeMap = _leaveTypes.value.associate { it.id to it.name }
        val query = balanceSearchQuery.trim().lowercase()

        val filtered = _allLeaveBalances.value.filter { item ->
            val empName = item.extractEmployeeName().lowercase()
            val empCode = item.extractEmployeeCode().lowercase()
            val typeName = item.extractLeaveTypeName(typeMap).lowercase()
            val typeId = item.extractLeaveTypeId()
            val remaining = item.extractRemainingDays()

            val matchesSearch = query.isBlank() ||
                    empName.contains(query) ||
                    empCode.contains(query) ||
                    typeName.contains(query)

            val matchesType = balanceSelectedLeaveTypeId == "all" ||
                    typeId == balanceSelectedLeaveTypeId ||
                    typeName.equals(balanceSelectedLeaveTypeId, ignoreCase = true)

            val matchesBudget = when (balanceSelectedBudgetStatus) {
                "in_budget" -> remaining > 0.0
                "exhausted" -> remaining <= 0.0
                else -> true
            }

            matchesSearch && matchesType && matchesBudget
        }

        _leaveBalancesPagingState.value = PagingUiState(
            items = filtered,
            isInitialLoading = false,
            isLoadingMore = isLoadingMore,
            canLoadMore = balanceHasNextPage
        )
    }

    private fun buildSixMonthUsageTrends(leaves: List<LeaveRequestItemDto>): List<MonthlyUsageTrendUiItem> {
        val calendar = Calendar.getInstance()
        val monthLabelFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
        val isoParsers = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        )

        fun parseMonthYear(rawDate: String?): Pair<Int, Int>? {
            if (rawDate.isNullOrBlank()) return null
            for (parser in isoParsers) {
                try {
                    val parsed = parser.parse(rawDate) ?: continue
                    val cal = Calendar.getInstance().apply { time = parsed }
                    return cal.get(Calendar.MONTH) to cal.get(Calendar.YEAR)
                } catch (_: Exception) {
                }
            }
            return null
        }

        val rawBuckets = (5 downTo 0).map { offset ->
            val cal = (calendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -offset)
            }
            val targetMonth = cal.get(Calendar.MONTH)
            val targetYear = cal.get(Calendar.YEAR)
            val label = monthLabelFormat.format(cal.time)

            val count = leaves.count { req ->
                val dateTuple = parseMonthYear(req.createdAt ?: req.startDate)
                dateTuple != null && dateTuple.first == targetMonth && dateTuple.second == targetYear
            }
            Triple(label, count, offset == 0)
        }

        val maxCount = rawBuckets.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

        return rawBuckets.map { (label, count, isCurrent) ->
            MonthlyUsageTrendUiItem(
                monthLabel = label,
                count = count,
                heightFraction = if (count == 0) 0f else (count.toFloat() / maxCount.toFloat()).coerceIn(0.12f, 1f),
                isCurrentMonth = isCurrent
            )
        }
    }
    // =========================================================================
    // EXPENSE CLAIMS (VIEW-ALL, VIEW-ONE, SUBMIT/DRAFT)
    // =========================================================================
    private val _expenseClaimDetail = MutableStateFlow<ExpenseClaimDto?>(null)
    val expenseClaimDetail: StateFlow<ExpenseClaimDto?> = _expenseClaimDetail.asStateFlow()

    private val _isLoadingExpenseClaimDetail = MutableStateFlow(false)
    val isLoadingExpenseClaimDetail: StateFlow<Boolean> = _isLoadingExpenseClaimDetail.asStateFlow()

    private val _isSubmittingExpenseClaim = MutableStateFlow(false)
    val isSubmittingExpenseClaim: StateFlow<Boolean> = _isSubmittingExpenseClaim.asStateFlow()

    fun fetchExpenseClaimDetail(claimId: String) {
        viewModelScope.launch {
            _isLoadingExpenseClaimDetail.value = true
            val result = repo.request<ExpenseClaimSingleResponse> {
                get("/api/hr/expense-claim/view-one/$claimId")
            }
            result.fold(
                onSuccess = { res ->
                    _expenseClaimDetail.value = res.data
                },
                onFailure = {
                    _expenseClaimDetail.value = null
                }
            )
            _isLoadingExpenseClaimDetail.value = false
        }
    }

    fun clearExpenseClaimDetail() {
        _expenseClaimDetail.value = null
    }

    fun submitExpenseClaim(
        claimId: String? = null,
        title: String,
        description: String,
        amount: String,
        taxAmount: String,
        currency: String = "INR",
        status: String,
        internalComments: String = "",
        expenseDate: String,
        category: String,
        files: List<File>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) {
            onError("Please enter claim title")
            return
        }
        if (category.isBlank()) {
            onError("Please select a category")
            return
        }
        if (amount.isBlank()) {
            onError("Please enter amount")
            return
        }

        viewModelScope.launch {
            _isSubmittingExpenseClaim.value = true

            val parts = mutableMapOf(
                "title" to repo.text(title),
                "description" to repo.text(description),
                "amount" to repo.text(amount),
                "taxAmount" to repo.text(taxAmount.ifBlank { "0" }),
                "currency" to repo.text(currency),
                "status" to repo.text(status),
                "internalComments" to repo.text(internalComments),
                "expenseDate" to repo.text(formatToApiDate(expenseDate)),
                "category" to repo.text(category)
            )

            val fileParts = files.map { file ->
                val extension = file.extension.lowercase()
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: when (extension) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "pdf" -> "application/pdf"
                        else -> "application/octet-stream"
                    }
                repo.filePart("attachments", file, mimeType)
            }

            if (claimId.isNullOrBlank()) {
                // CREATE CALL
                val result = repo.request<UpdateExpenseClaimResponse> {
                    postMultipart(
                        url = "/api/hr/expense-claim/create",
                        parts = parts,
                        files = fileParts
                    )
                }

                _isSubmittingExpenseClaim.value = false
                result.fold(
                    onSuccess = { res ->
                        onSuccess(res.message ?: "Expense claim created successfully")
                    },
                    onFailure = { error ->
                        onError(error.message ?: "Failed to process expense claim")
                    }
                )
            } else {
                // UPDATE CALL (Using UpdateExpenseClaimResponse prevents String-to-Object JsonSyntaxException)
                val result = repo.request<UpdateExpenseClaimResponse> {
                    putMultipart(
                        url = "/api/hr/expense-claim/update/$claimId",
                        parts = parts,
                        files = fileParts
                    )
                }

                _isSubmittingExpenseClaim.value = false
                result.fold(
                    onSuccess = { res ->
                        onSuccess(res.message ?: "Expense claim updated successfully")
                    },
                    onFailure = { error ->
                        onError(error.message ?: "Failed to update expense claim")
                    }
                )
            }
        }
    }

    private fun formatToApiDate(rawDate: String): String {
        return try {
            if (rawDate.contains("-") && rawDate.length == 10) {
                val parts = rawDate.split("-")
                // If it is in dd-MM-yyyy format, convert to yyyy-MM-dd
                if (parts[0].length == 2 && parts[2].length == 4) {
                    "${parts[2]}-${parts[1]}-${parts[0]}"
                } else {
                    rawDate
                }
            } else {
                rawDate
            }
        } catch (_: Exception) {
            rawDate
        }
    }
    // =========================================================================
    // UPDATE EXPENSE CLAIM STATUS (APPROVE, REJECT, CLARIFY)
    // =========================================================================
    private val _isUpdatingClaimStatus = MutableStateFlow(false)
    val isUpdatingClaimStatus: StateFlow<Boolean> = _isUpdatingClaimStatus.asStateFlow()

    fun updateExpenseClaimStatus(
        claimId: String,
        status: String,
        comment: String = "",
        payment: ExpenseClaimPaymentDetails? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isUpdatingClaimStatus.value = true
            val request = UpdateExpenseClaimStatusRequest(
                status = status,
                comment = comment.trim().ifEmpty { null },
                payment = payment
            )

            val result = repo.request<UpdateExpenseClaimResponse> {
                patch("/api/hr/expense-claim/status/$claimId", request)
            }

            result.fold(
                onSuccess = { res ->
                    _isUpdatingClaimStatus.value = false
                    fetchExpenseClaimDetail(claimId)
                    fetchExpenseClaims(
                        status = activeExpenseClaimStatus,
                        search = activeExpenseClaimSearch
                    )
                    onSuccess(res.message ?: "Expense claim status updated successfully")
                },
                onFailure = { error ->
                    _isUpdatingClaimStatus.value = false
                    onError(error.message ?: "Failed to update claim status")
                }
            )
        }
    }

    // =========================================================================
    // DELETE EXPENSE CLAIM
    // =========================================================================
    fun deleteExpenseClaim(
        claimId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repo.request<DeleteExpenseClaimResponse> {
                delete("/api/hr/expense-claim/delete/$claimId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        // Immediately remove the deleted claim from local state
                        _expenseClaims.value = _expenseClaims.value.filter { it.id != claimId }
                        val current = _expenseClaimsPagingState.value
                        _expenseClaimsPagingState.value = current.copy(
                            items = current.items.filter { it.id != claimId }
                        )
                        onSuccess(res.message ?: "Expense claim deleted successfully")
                    }else {
                        onError(res.message ?: "Failed to delete expense claim")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete expense claim")
                }
            )
        }
    }

    // ═══════════════════════════════════════════════
    // ── Finance Reimbursement Claims ──
    // ═══════════════════════════════════════════════
    fun fetchReimbursementClaims(page: Int = 1, limit: Int = 10, search: String? = null) {
        fetchExpenseClaims(
            page = page,
            limit = limit,
            status = "Approved,Payment_Pending,Paid,Rejected",
            search = search
        )
    }

    // =========================================================================
    // EXPENSE TYPES
    // =========================================================================
    private val _expenseTypes = MutableStateFlow<List<ExpenseTypeDto>>(emptyList())
    val expenseTypes: StateFlow<List<ExpenseTypeDto>> = _expenseTypes.asStateFlow()

    private val _isLoadingExpenseTypes = MutableStateFlow(false)
    val isLoadingExpenseTypes: StateFlow<Boolean> = _isLoadingExpenseTypes.asStateFlow()

    private val _expenseTypesError = MutableStateFlow<String?>(null)
    val expenseTypesError: StateFlow<String?> = _expenseTypesError.asStateFlow()

    private val _expenseTypesPagingState = MutableStateFlow(PagingUiState<ExpenseTypeDto>())
    val expenseTypesPagingState: StateFlow<PagingUiState<ExpenseTypeDto>> =
        _expenseTypesPagingState.asStateFlow()

    private var expenseTypeCurrentPage = 1
    private val expenseTypePageSize = 20
    private var activeExpenseTypeSearch: String? = null

    /**
     * Fetches expense types list
     * Endpoint: GET /api/hr/expense-type/view-all?page=1&limit=20
     */
    fun fetchExpenseTypes(page: Int = 1, limit: Int = expenseTypePageSize, search: String? = null) {
        expenseTypeCurrentPage = page
        activeExpenseTypeSearch = search

        viewModelScope.launch {
            _isLoadingExpenseTypes.value = true
            _expenseTypesError.value = null
            _expenseTypesPagingState.value = PagingUiState(isInitialLoading = true)

            val result = repo.request<ExpenseTypeListApiResponse> {
                get(
                    "/api/hr/expense-type/view-all",
                    repo.query(
                        "page" to page,
                        "limit" to limit,
                        "search" to search
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val list = res.data?.data ?: emptyList()
                    _expenseTypes.value = list
                    _expenseTypesPagingState.value = PagingUiState(
                        items = list,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = list.size >= limit
                    )
                },
                onFailure = { error ->
                    _expenseTypesError.value = error.message ?: "Failed to fetch expense types"
                    _expenseTypesPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
            _isLoadingExpenseTypes.value = false
        }
    }

    fun loadMoreExpenseTypes(limit: Int = expenseTypePageSize) {
        val current = _expenseTypesPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return

        viewModelScope.launch {
            _expenseTypesPagingState.value = current.copy(isLoadingMore = true)
            val nextPage = expenseTypeCurrentPage + 1

            val result = repo.request<ExpenseTypeListApiResponse> {
                get(
                    "/api/hr/expense-type/view-all",
                    repo.query(
                        "page" to nextPage,
                        "limit" to limit,
                        "search" to activeExpenseTypeSearch
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val newItems = res.data?.data ?: emptyList()
                    val merged = (current.items + newItems).distinctBy { it.id }
                    val hasNewItems = merged.size > current.items.size
                    if (hasNewItems) {
                        expenseTypeCurrentPage = nextPage
                        _expenseTypes.value = merged
                        _expenseTypesPagingState.value = current.copy(
                            items = merged,
                            isLoadingMore = false,
                            canLoadMore = newItems.size >= limit
                        )
                    } else {
                        _expenseTypesPagingState.value = current.copy(
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                },
                onFailure = {
                    _expenseTypesPagingState.value = current.copy(isLoadingMore = false)
                }
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE / UPDATE / DELETE EXPENSE TYPE
    // ═══════════════════════════════════════════════════════════════
    private val _isSubmittingExpenseType = MutableStateFlow(false)
    val isSubmittingExpenseType: StateFlow<Boolean> = _isSubmittingExpenseType.asStateFlow()

    fun createOrUpdateExpenseType(
        id: String? = null,
        name: String,
        description: String,
        defaultLimit: Double,
        status: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingExpenseType.value = true

            val request = com.cuso.tailor.model.hr.SaveExpenseTypeRequest(
                name = name,
                description = description,
                defaultLimit = defaultLimit,
                status = status
            )

            val result = if (id.isNullOrBlank()) {
                // CREATE (POST /api/hr/expense-type/create)
                repo.request<com.cuso.tailor.model.hr.ExpenseTypeActionResponse> {
                    post("$EXPENSE_TYPE_BASE/create", request)
                }
            } else {
                // UPDATE (PUT /api/hr/expense-type/update/{id})
                repo.request<com.cuso.tailor.model.hr.ExpenseTypeActionResponse> {
                    put("$EXPENSE_TYPE_BASE/update/$id", request)
                }
            }

            _isSubmittingExpenseType.value = false

            result.fold(
                onSuccess = { res ->
                    fetchExpenseTypes() // Refresh list automatically
                    onSuccess(res.message ?: "Expense type saved successfully")
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to save expense type")
                }
            )
        }
    }

    fun deleteExpenseType(
        id: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repo.request<SimpleActionResponse> {
                delete("$EXPENSE_TYPE_BASE/delete/$id")
            }
            result.fold(
                onSuccess = {
                    _expenseTypes.value = _expenseTypes.value.filter { it.id != id }
                    val current = _expenseTypesPagingState.value
                    _expenseTypesPagingState.value = current.copy(
                        items = current.items.filter { it.id != id }
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete expense type")
                }
            )
        }
    }
    // ═══════════════════════════════════════════════
    // ── Members (List & Infinite Scroll) ──
    // ═══════════════════════════════════════════════
    private val _members = MutableStateFlow<List<MemberItem>>(emptyList())
    val members: StateFlow<List<MemberItem>> = _members.asStateFlow()

    private val _membersTotal = MutableStateFlow(0)
    val membersTotal: StateFlow<Int> = _membersTotal.asStateFlow()

    private val _isLoadingMembers = MutableStateFlow(false)
    val isLoadingMembers: StateFlow<Boolean> = _isLoadingMembers.asStateFlow()

    private val _isLoadingMoreMembers = MutableStateFlow(false)
    val isLoadingMoreMembers: StateFlow<Boolean> = _isLoadingMoreMembers.asStateFlow()

    private val _canLoadMoreMembers = MutableStateFlow(true)
    val canLoadMoreMembers: StateFlow<Boolean> = _canLoadMoreMembers.asStateFlow()

    private val _currentMemberPage = MutableStateFlow(1)
    val currentMemberPage: StateFlow<Int> = _currentMemberPage.asStateFlow()

    private val _membersError = MutableStateFlow<String?>(null)
    val membersError: StateFlow<String?> = _membersError.asStateFlow()

    private val _membersPagingState = MutableStateFlow(PagingUiState<MemberItem>())
    val membersPagingState: StateFlow<PagingUiState<MemberItem>> = _membersPagingState.asStateFlow()

    private var activeMemberSearch: String? = null
    private var activeMemberStatus: String? = null
    private var fetchMembersJob: Job? = null

    // ── Monthly Attendance Calendar State ──
    private val _monthlyAttendance = MutableStateFlow<List<MonthlyAttendanceItem>>(emptyList())
    val monthlyAttendance: StateFlow<List<MonthlyAttendanceItem>> = _monthlyAttendance.asStateFlow()

    private val _isLoadingMonthlyAttendance = MutableStateFlow(false)
    val isLoadingMonthlyAttendance: StateFlow<Boolean> = _isLoadingMonthlyAttendance.asStateFlow()

    private val _monthlyAttendanceError = MutableStateFlow<String?>(null)
    val monthlyAttendanceError: StateFlow<String?> = _monthlyAttendanceError.asStateFlow()
    fun fetchMembers(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchMembersJob?.cancel()
        fetchMembersJob = launchBusy {
            _isLoadingMembers.value = true
            _membersError.value = null
            _currentMemberPage.value = page
            activeMemberSearch = search
            activeMemberStatus = status

            _membersPagingState.value = PagingUiState(
                items = emptyList(),
                isInitialLoading = true,
                isLoadingMore = false,
                canLoadMore = false
            )

            val result = hrRepository.getMembers(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    val newMembers = response.data
                    _members.value = newMembers
                    _membersTotal.value = response.total

                    val canLoadMore = newMembers.size < response.total && newMembers.isNotEmpty()
                    _canLoadMoreMembers.value = canLoadMore
                    _membersPagingState.value = PagingUiState(
                        items = newMembers,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _membersError.value = e.message ?: "Failed to fetch employees"
                        _membersPagingState.value = PagingUiState(isInitialLoading = false)
                    }
                }
            )
            _isLoadingMembers.value = false
        }
    }

    fun loadMoreMembers(limit: Int = 10) {
        if (_isLoadingMoreMembers.value || _isLoadingMembers.value || !_canLoadMoreMembers.value) {
            return
        }

        launchBusy {
            _isLoadingMoreMembers.value = true
            _membersPagingState.value = _membersPagingState.value.copy(isLoadingMore = true)
            val nextPage = _currentMemberPage.value + 1

            val result = hrRepository.getMembers(
                page = nextPage,
                limit = limit,
                search = activeMemberSearch,
                status = activeMemberStatus
            )

            result.fold(
                onSuccess = { response ->
                    val newMembers = response.data
                    if (newMembers.isNotEmpty()) {
                        val updatedList = (_members.value + newMembers).distinctBy { it._id }
                        _members.value = updatedList
                        _currentMemberPage.value = nextPage
                        _membersTotal.value = response.total

                        val canLoadMore = updatedList.size < response.total
                        _canLoadMoreMembers.value = canLoadMore
                        _membersPagingState.value = PagingUiState(
                            items = updatedList,
                            isInitialLoading = false,
                            isLoadingMore = false,
                            canLoadMore = canLoadMore
                        )
                    } else {
                        _canLoadMoreMembers.value = false
                        _membersPagingState.value = _membersPagingState.value.copy(
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                },
                onFailure = {
                    _membersPagingState.value = _membersPagingState.value.copy(isLoadingMore = false)
                }
            )
            _isLoadingMoreMembers.value = false
        }
    }

    fun refreshMembers() {
        fetchMembers(page = 1, search = activeMemberSearch, status = activeMemberStatus)
    }

    fun clearMembersError() {
        _membersError.value = null
    }

    // ═══════════════════════════════════════════════
    // ── Member Detail (VIEW / EDIT prefill) ──
    // ═══════════════════════════════════════════════
//    private val _memberDetail = MutableStateFlow<MemberDetail?>(null)
//    val memberDetail: StateFlow<MemberDetail?> = _memberDetail.asStateFlow()
//
//    private val _isLoadingMemberDetail = MutableStateFlow(false)
//    val isLoadingMemberDetail: StateFlow<Boolean> = _isLoadingMemberDetail.asStateFlow()
//
//    private val _memberDetailError = MutableStateFlow<String?>(null)
//    val memberDetailError: StateFlow<String?> = _memberDetailError.asStateFlow()

    private val _uploadPictureState = MutableStateFlow<UploadPictureState>(UploadPictureState.Idle)
    val uploadPictureState: StateFlow<UploadPictureState> = _uploadPictureState.asStateFlow()

    private val _deletePictureState = MutableStateFlow<DeletePictureState>(DeletePictureState.Idle)
    val deletePictureState: StateFlow<DeletePictureState> = _deletePictureState.asStateFlow()

    fun uploadProfilePicture(memberId: String, file: File) {
        launchBusy {
            _uploadPictureState.value = UploadPictureState.Loading
            val result = hrRepository.uploadProfilePicture(memberId, file)
            result.onSuccess { response ->
                val url = response.member.profilePicture.orEmpty()
                _uploadPictureState.value = UploadPictureState.Success(url)
            }.onFailure { e ->
                _uploadPictureState.value = UploadPictureState.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun resetUploadPictureState() {
        _uploadPictureState.value = UploadPictureState.Idle
    }

    fun deleteProfilePicture(memberId: String) {
        launchBusy {
            _deletePictureState.value = DeletePictureState.Loading
            val result = hrRepository.deleteProfilePicture(memberId)
            result.onSuccess {
                _deletePictureState.value = DeletePictureState.Success
            }.onFailure { e ->
                _deletePictureState.value = DeletePictureState.Error(e.message ?: "Delete failed")
            }
        }
    }

    fun resetDeletePictureState() {
        _deletePictureState.value = DeletePictureState.Idle
    }

    // ═══════════════════════════════════════════════
    // ── Member Detail (VIEW ONE) ──
    // ═══════════════════════════════════════════════
    private val _memberDetail = MutableStateFlow<MemberDetail?>(null)
    val memberDetail: StateFlow<MemberDetail?> = _memberDetail.asStateFlow()

    private val _isLoadingMemberDetail = MutableStateFlow(false)
    val isLoadingMemberDetail: StateFlow<Boolean> = _isLoadingMemberDetail.asStateFlow()

    private val _memberDetailError = MutableStateFlow<String?>(null)
    val memberDetailError: StateFlow<String?> = _memberDetailError.asStateFlow()

    fun fetchMemberDetail(memberId: String) {
        viewModelScope.launch {
            _isLoadingMemberDetail.value = true
            _memberDetailError.value = null

            // Direct generic API call to GET /api/members/{memberId}
            val result = repo.request<MemberDetailResponse> {
                get("/api/members/view-one/$memberId")
            }

            result.fold(
                onSuccess = { response ->
                    _memberDetail.value = response.data ?: response.member
                },
                onFailure = { error ->
                    // Fallback to hrRepository if available
                    val fallback = hrRepository.getMemberDetail(memberId)
                    fallback.fold(
                        onSuccess = { detail -> _memberDetail.value = detail },
                        onFailure = { _memberDetailError.value = error.message ?: "Failed to fetch employee detail" }
                    )
                }
            )
            _isLoadingMemberDetail.value = false
        }
    }

    fun clearMemberDetail() {
        _memberDetail.value = null
        _memberDetailError.value = null
    }

//    fun clearMemberDetail() {
//        _memberDetail.value = null
//    }

    // =========================================================================
    // ORGANIZATION STRUCTURE (HIERARCHY)
    // =========================================================================
    private val _orgMembers = MutableStateFlow<List<OrgMemberDto>>(emptyList())
    val orgMembers: StateFlow<List<OrgMemberDto>> = _orgMembers.asStateFlow()

    private val _isLoadingOrgStructure = MutableStateFlow(false)
    val isLoadingOrgStructure: StateFlow<Boolean> = _isLoadingOrgStructure.asStateFlow()

    private val _orgStructureError = MutableStateFlow<String?>(null)
    val orgStructureError: StateFlow<String?> = _orgStructureError.asStateFlow()

    /**
     * Fetches organization structure members
     * Endpoint: GET /api/members/org-structure (or pass your endpoint URL)
     */
    fun fetchOrgStructure(departmentId: String? = null) {
        viewModelScope.launch {
            _isLoadingOrgStructure.value = true
            _orgStructureError.value = null

            val result = repo.request<OrgStructureResponse> {
                get(
                    "/api/members/dropdown-filter",
                    repo.query("departmentId" to departmentId)
                )
            }

            result.fold(
                onSuccess = { res ->
                    _orgMembers.value = res.data
                },
                onFailure = { error ->
                    _orgStructureError.value = error.message ?: "Failed to fetch organization structure"
                }
            )
            _isLoadingOrgStructure.value = false
        }
    }

    // ═══════════════════════════════════════════════
    // ── Create / Update Member ──
    // ═══════════════════════════════════════════════
    private val _createMemberState = MutableStateFlow<CreateMemberState>(CreateMemberState.Idle)
    val createMemberState: StateFlow<CreateMemberState> = _createMemberState.asStateFlow()

    fun createMember(request: CreateMemberRequest) {
        launchBusy {
            _createMemberState.value = CreateMemberState.Loading
            val result = hrRepository.createMember(request)
            result.fold(
                onSuccess = {
                    _createMemberState.value = CreateMemberState.Success(it)
                    refreshMembers()
                },
                onFailure = { e -> _createMemberState.value = CreateMemberState.Error(e.message ?: "Failed to create employee") }
            )
        }
    }

    fun updateMember(memberId: String, request: UpdateMemberRequest) {
        launchBusy {
            _createMemberState.value = CreateMemberState.Loading
            val result = hrRepository.updateMember(memberId, request)
            result.fold(
                onSuccess = {
                    _createMemberState.value = CreateMemberState.Success(
                        CreatedMemberFullData(_id = memberId)
                    )
                    refreshMembers()
                },
                onFailure = { e ->
                    val msg = e.message ?: "Failed to update employee"
                    if (msg.contains("updated successfully", ignoreCase = true)) {
                        _createMemberState.value = CreateMemberState.Success(
                            CreatedMemberFullData(_id = memberId)
                        )
                        refreshMembers()
                    } else {
                        _createMemberState.value = CreateMemberState.Error(msg)
                    }
                }
            )
        }
    }

    fun resetCreateMemberState() {
        _createMemberState.value = CreateMemberState.Idle
    }

    //MONTHLY ATTENDANCE


    fun fetchMonthlyAttendance(organizationMemberId: String, month: Int, year: Int) {
        launchBusy {
            _isLoadingMonthlyAttendance.value = true
            _monthlyAttendanceError.value = null
            val result = hrRepository.getMonthlyAttendance(organizationMemberId, month, year)
            result.fold(
                onSuccess = { _monthlyAttendance.value = it },
                onFailure = { e ->
                    _monthlyAttendance.value = emptyList()
                    _monthlyAttendanceError.value = e.message ?: "Failed to fetch monthly attendance"
                }
            )
            _isLoadingMonthlyAttendance.value = false
        }
    }

    //daily attendance
    private val _uiState = MutableStateFlow<AttendanceUiState>(AttendanceUiState.Loading)
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow("All")
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    private var currentPage = 1
    private val pageSize = 10
    private var isEndReached = false
    private var isLoadingMore = false
    private val allRecords = mutableListOf<AttendanceRecord>()
    private var searchJob: Job? = null

//    init {
//        loadAttendance(reset = true)
//    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // Debounce search
            loadAttendance(reset = true)
        }
    }

    fun onFilterTypeChanged(filter: String) {
        _filterType.value = filter
        loadAttendance(reset = true)
    }

    fun loadAttendance(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            isEndReached = false
            allRecords.clear()
            _uiState.value = AttendanceUiState.Loading
        }

        if (isEndReached || isLoadingMore) return
        isLoadingMore = true

        viewModelScope.launch {
            val result = hrRepository.getDailyAttendanceList(
                page = currentPage,
                limit = pageSize,
                search = _searchQuery.value,
                status = _filterType.value
            )

            result.onSuccess { data ->
                if (data.size < pageSize) {
                    isEndReached = true
                }
                allRecords.addAll(data)
                currentPage++
                _uiState.value = AttendanceUiState.Success(
                    records = allRecords.toList(),
                    canLoadMore = !isEndReached
                )
            }.onFailure { error ->
                if (allRecords.isEmpty()) {
                    _uiState.value = AttendanceUiState.Error(error.localizedMessage ?: "Failed to load attendance")
                }
            }

            isLoadingMore = false
        }
    }

    //approval
    fun approveAttendance(attendanceId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val result = hrRepository.approveAttendance(attendanceId)
            result.onSuccess {
                loadAttendance(reset = true)
                onSuccess()
            }.onFailure { error ->
                onError(error.localizedMessage ?: "Failed to approve")
            }
        }
    }

    fun loadNextPage() {
        if (!isEndReached && !isLoadingMore) {
            loadAttendance(reset = false)
        }
    }

    //create manual

    private val _isSubmittingManualAttendance = MutableStateFlow(false)
    val isSubmittingManualAttendance: StateFlow<Boolean> = _isSubmittingManualAttendance.asStateFlow()

    fun submitManualAttendance(
        organizationMemberId: String,
        date: String,
        checkIn: String,
        checkOut: String,
        adminNote: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (organizationMemberId.isBlank()) {
            onError("Please select an employee")
            return
        }
        if (date.isBlank()) {
            onError("Please select a date")
            return
        }
        if (checkIn.isBlank() || checkOut.isBlank()) {
            onError("Please select both check-in and check-out time")
            return
        }

        val formattedDate = convertUiDateToApiDate(date)
        val formattedCheckIn = convert12HrTo24Hr(checkIn)
        val formattedCheckOut = convert12HrTo24Hr(checkOut)

        val request = CreateManualAttendanceRequest(
            organizationMemberId = organizationMemberId,
            date = formattedDate,
            checkIn = formattedCheckIn,
            checkOut = formattedCheckOut,
            adminNote = adminNote.ifBlank { "Manual Entry" },
            isApproved = true
        )

        viewModelScope.launch {
            _isSubmittingManualAttendance.value = true
            val result = hrRepository.createManualAttendance(request)
            result.onSuccess {
                _isSubmittingManualAttendance.value = false
                loadAttendance(reset = true) // Refresh list
                onSuccess()
            }.onFailure { error ->
                _isSubmittingManualAttendance.value = false
                onError(error.localizedMessage ?: "Failed to submit request")
            }
        }
    }

    //shifts
    // ═══════════════════════════════════════════════
    // ── Shifts ──
    // ═══════════════════════════════════════════════

    private val _shiftsPagingState = MutableStateFlow(PagingUiState<ShiftItem>())
    val shiftsPagingState: StateFlow<PagingUiState<ShiftItem>> = _shiftsPagingState.asStateFlow()

    private var shiftSearchQuery: String = ""
    private var shiftCurrentPage: Int = 1
    private val shiftPageSize: Int = 10

    fun onShiftSearchChanged(query: String) {
        shiftSearchQuery = query
        rebuildShiftsPagingState(resetPage = true)
    }

    fun loadMoreShifts() {
        val current = _shiftsPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return
        shiftCurrentPage += 1
        rebuildShiftsPagingState(resetPage = false)
    }

    private fun rebuildShiftsPagingState(resetPage: Boolean) {
        if (resetPage) shiftCurrentPage = 1
        val allShifts = _shifts.value
        val filtered = if (shiftSearchQuery.isBlank()) {
            allShifts
        } else {
            allShifts.filter {
                it.title.contains(shiftSearchQuery, ignoreCase = true) ||
                        it.department.contains(shiftSearchQuery, ignoreCase = true) ||
                        it.shiftType.contains(shiftSearchQuery, ignoreCase = true)
            }
        }
        val paged = filtered.take(shiftCurrentPage * shiftPageSize)
        _shiftsPagingState.value = PagingUiState(
            items = paged,
            isInitialLoading = false,
            isLoadingMore = false,
            canLoadMore = paged.size < filtered.size
        )
    }

    fun fetchShiftsViewAll() {
        launchBusy {
            _isLoadingShifts.value = true
            _shiftsError.value = null
            _shiftsPagingState.value = PagingUiState(isInitialLoading = true)
            val result = hrRepository.getShiftsViewAll()
            result.fold(
                onSuccess = {
                    _shifts.value = it
                    rebuildShiftsPagingState(resetPage = true)
                },
                onFailure = { e ->
                    _shiftsError.value = e.message ?: "Failed to fetch shifts"
                    _shiftsPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
            _isLoadingShifts.value = false
        }
    }

    private val _isCreatingShift = MutableStateFlow(false)
    val isCreatingShift: StateFlow<Boolean> = _isCreatingShift.asStateFlow()

    fun createShift(
        name: String,
        shiftId: String,
        startTime12: String,
        endTime12: String,
        breakDurationStr: String,
        shiftType: String,
        customWorkingDays: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("Please enter Shift Name")
            return
        }
        if (shiftId.isBlank()) {
            onError("Please enter HR Code / Shift ID")
            return
        }
        if (startTime12.isBlank() || endTime12.isBlank()) {
            onError("Please select both start and end time")
            return
        }

        val startTime24 = convert12HrTo24Hr(startTime12)
        val endTime24 = convert12HrTo24Hr(endTime12)
        val breakMins = breakDurationStr.filter { it.isDigit() }.toIntOrNull() ?: 0

        val request = CreateShiftRequest(
            name = name,
            shiftId = shiftId,
            startTime = startTime24,
            endTime = endTime24,
            breakDuration = breakMins,
            shiftType = shiftType,
            customWorkingDays = customWorkingDays,
            status = true
        )

        viewModelScope.launch {
            _isCreatingShift.value = true
            val result = hrRepository.createShift(request)
            result.onSuccess {
                _isCreatingShift.value = false
                fetchShiftsViewAll() // Refresh shifts list
                onSuccess()
            }.onFailure { e ->
                _isCreatingShift.value = false
                onError(e.localizedMessage ?: "Failed to create shift")
            }
        }
    }

    private val _shiftDetail = MutableStateFlow<ShiftDetailData?>(null)
    val shiftDetail: StateFlow<ShiftDetailData?> = _shiftDetail.asStateFlow()

    private val _isLoadingShiftDetail = MutableStateFlow(false)
    val isLoadingShiftDetail: StateFlow<Boolean> = _isLoadingShiftDetail.asStateFlow()

    private val _isUpdatingShift = MutableStateFlow(false)
    val isUpdatingShift: StateFlow<Boolean> = _isUpdatingShift.asStateFlow()

    fun fetchShiftDetail(shiftId: String) {
        viewModelScope.launch {
            _isLoadingShiftDetail.value = true
            val result = hrRepository.getShiftDetail(shiftId)
            result.onSuccess { data ->
                _shiftDetail.value = data
                _isLoadingShiftDetail.value = false
            }.onFailure {
                _isLoadingShiftDetail.value = false
            }
        }
    }

    fun clearShiftDetail() {
        _shiftDetail.value = null
    }

    fun updateShift(
        shiftId: String,
        name: String,
        shiftCode: String,
        startTime12: String,
        endTime12: String,
        breakDurationStr: String,
        shiftType: String,
        customWorkingDays: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("Please enter Shift Name")
            return
        }
        if (shiftCode.isBlank()) {
            onError("Please enter HR Code / Shift ID")
            return
        }

        val startTime24 = convert12HrTo24Hr(startTime12)
        val endTime24 = convert12HrTo24Hr(endTime12)
        val breakMins = breakDurationStr.filter { it.isDigit() }.toIntOrNull() ?: 0

        val request = UpdateShiftRequest(
            name = name,
            shiftId = shiftCode,
            startTime = startTime24,
            endTime = endTime24,
            breakDuration = breakMins,
            shiftType = shiftType,
            customWorkingDays = customWorkingDays,
            status = true
        )

        viewModelScope.launch {
            _isUpdatingShift.value = true
            val result = hrRepository.updateShift(shiftId, request)
            result.onSuccess {
                _isUpdatingShift.value = false
                fetchShiftsViewAll()
                onSuccess()
            }.onFailure { e ->
                _isUpdatingShift.value = false
                onError(e.localizedMessage ?: "Failed to update shift")
            }
        }
    }

    fun deleteShift(shiftId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val result = hrRepository.deleteShift(shiftId)
            result.onSuccess {
                fetchShiftsViewAll()
                onSuccess()
            }.onFailure { e ->
                onError(e.localizedMessage ?: "Failed to delete shift")
            }
        }
    }

    //LEAVE MANAGEMENT

    // ═══════════════════════════════════════════════
// ── Leave Approval State ──
// ═══════════════════════════════════════════════
    private val _leaveRequests = MutableStateFlow<List<LeaveRequestItemDto>>(emptyList())
    val leaveRequests: StateFlow<List<LeaveRequestItemDto>> = _leaveRequests.asStateFlow()

    private val _isLoadingLeaveRequests = MutableStateFlow(false)
    val isLoadingLeaveRequests: StateFlow<Boolean> = _isLoadingLeaveRequests.asStateFlow()

    private val _leaveRequestsError = MutableStateFlow<String?>(null)
    val leaveRequestsError: StateFlow<String?> = _leaveRequestsError.asStateFlow()

    private val _leaveRequestsPagingState = MutableStateFlow(PagingUiState<LeaveRequestItemDto>())
    val leaveRequestsPagingState: StateFlow<PagingUiState<LeaveRequestItemDto>> =
        _leaveRequestsPagingState.asStateFlow()

    private var leaveCurrentPage = 1
    private val leavePageSize = 15
    private var activeLeaveStatus: String? = null

    fun fetchLeaveRequests(status: String? = null) {
        leaveCurrentPage = 1
        activeLeaveStatus = status

        viewModelScope.launch {
            _isLoadingLeaveRequests.value = true
            _leaveRequestsError.value = null
            _leaveRequestsPagingState.value = PagingUiState(isInitialLoading = true)

            val result = repo.request<LeaveRequestsResponse> {
                get(
                    "/api/hr/leave-request/view-all",
                    repo.query(
                        "page" to leaveCurrentPage,
                        "limit" to leavePageSize,
                        "status" to status
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val list = res.data?.data ?: emptyList()
                    _leaveRequests.value = list
                    _leaveRequestsPagingState.value = PagingUiState(
                        items = list,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = list.size >= leavePageSize
                    )
                },
                onFailure = { error ->
                    _leaveRequestsError.value = error.message ?: "Failed to load leave requests"
                    _leaveRequestsPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
            _isLoadingLeaveRequests.value = false
        }
    }

    fun loadMoreLeaveRequests() {
        val current = _leaveRequestsPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return

        viewModelScope.launch {
            _leaveRequestsPagingState.value = current.copy(isLoadingMore = true)
            val nextPage = leaveCurrentPage + 1

            val result = repo.request<LeaveRequestsResponse> {
                get(
                    "/api/hr/leave-request/view-all",
                    repo.query(
                        "page" to nextPage,
                        "limit" to leavePageSize,
                        "status" to activeLeaveStatus
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val newItems = res.data?.data ?: emptyList()
                    val merged = (current.items + newItems).distinctBy { it.id }
                    val hasNewItems = merged.size > current.items.size

                    if (hasNewItems) {
                        leaveCurrentPage = nextPage
                        _leaveRequests.value = merged
                        _leaveRequestsPagingState.value = current.copy(
                            items = merged,
                            isLoadingMore = false,
                            canLoadMore = newItems.size >= leavePageSize
                        )
                    } else {
                        _leaveRequestsPagingState.value = current.copy(
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                },
                onFailure = {
                    _leaveRequestsPagingState.value = current.copy(isLoadingMore = false)
                }
            )
        }
    }

    // Unified leave status updater
    // Updated to support exact uppercase/capitalized status format and approvalRemarks
    fun updateLeaveStatus(
        id: String,
        status: String, // "Approved" or "Rejected"
        approvalRemarks: String? = null,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val request = com.cuso.tailor.model.hr.UpdateLeaveStatusRequest(
                status = status,
                approvalRemarks = approvalRemarks?.trim()?.ifBlank { null }
            )

            val result = repo.request<com.cuso.tailor.model.hr.UpdateLeaveStatusResponse> {
                patch("/api/hr/leave-request/status/$id", request)
            }

            result.fold(
                onSuccess = { res ->
                    // Refresh pending list
                    fetchLeaveRequests(status = "Pending")
                    onSuccess(res.message ?: "Leave request status updated successfully")
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update leave status")
                }
            )
        }
    }

    // ═══════════════════════════════════════════════
    // ── Leave Types & Application State ──
    // ═══════════════════════════════════════════════
    private val _leaveTypes = MutableStateFlow<List<LeaveTypeItemDto>>(emptyList())
    val leaveTypes: StateFlow<List<LeaveTypeItemDto>> = _leaveTypes.asStateFlow()

    private val _isLoadingLeaveTypes = MutableStateFlow(false)
    val isLoadingLeaveTypes: StateFlow<Boolean> = _isLoadingLeaveTypes.asStateFlow()

    private val _isSubmittingLeave = MutableStateFlow(false)
    val isSubmittingLeave: StateFlow<Boolean> = _isSubmittingLeave.asStateFlow()

    fun fetchLeaveTypes() {
        viewModelScope.launch {
            _isLoadingLeaveTypes.value = true
            val result = hrRepository.getLeaveTypes()
            result.fold(
                onSuccess = { _leaveTypes.value = it },
                onFailure = { _leaveTypes.value = emptyList() }
            )
            _isLoadingLeaveTypes.value = false
        }
    }

    private val _isCreatingLeave = MutableStateFlow(false)
    val isCreatingLeave: StateFlow<Boolean> = _isCreatingLeave.asStateFlow()

    fun createLeaveRequest(
        organizationMemberId: String,
        leaveTypeId: String,
        startDate: String,
        endDate: String,
        totalDays: Int,
        isHalfDay: Boolean,
        reason: String,
        attachmentParts: List<MultipartBody.Part>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (organizationMemberId.isBlank()) {
            onError("Please select an employee")
            return
        }
        if (leaveTypeId.isBlank()) {
            onError("Please select a leave type")
            return
        }
        if (startDate.isBlank() || endDate.isBlank()) {
            onError("Please select start and end dates")
            return
        }
        if (reason.isBlank()) {
            onError("Please provide a reason for leave")
            return
        }

        viewModelScope.launch {
            _isCreatingLeave.value = true
            val result = hrRepository.createLeaveRequest(
                organizationMemberId = organizationMemberId,
                leaveTypeId = leaveTypeId,
                startDate = startDate,
                endDate = endDate,
                totalDays = totalDays,
                isHalfDay = isHalfDay,
                reason = reason,
                attachmentParts = attachmentParts
            )
            result.fold(
                onSuccess = { message ->
                    _isCreatingLeave.value = false
                    fetchLeaveRequests()
                    onSuccess(message)
                },
                onFailure = { error ->
                    _isCreatingLeave.value = false
                    onError(error.message ?: "Failed to create leave request")
                }
            )
        }
    }

    // ═══════════════════════════════════════════════
    // ── Salary Components State & Handlers ──
    // ═══════════════════════════════════════════════

    private val _salaryComponents = MutableStateFlow<List<SalaryComponentItem>>(emptyList())
    val salaryComponents: StateFlow<List<SalaryComponentItem>> = _salaryComponents.asStateFlow()

    private val _salaryComponentTotal = MutableStateFlow(0)
    val salaryComponentTotal: StateFlow<Int> = _salaryComponentTotal.asStateFlow()

    private val _isLoadingSalaryComponents = MutableStateFlow(false)
    val isLoadingSalaryComponents: StateFlow<Boolean> = _isLoadingSalaryComponents.asStateFlow()

    private val _salaryComponentError = MutableStateFlow<String?>(null)
    val salaryComponentError: StateFlow<String?> = _salaryComponentError.asStateFlow()

    private val _salaryComponentDetail = MutableStateFlow<SalaryComponentItem?>(null)
    val salaryComponentDetail: StateFlow<SalaryComponentItem?> = _salaryComponentDetail.asStateFlow()

    private val _isLoadingSalaryComponentDetail = MutableStateFlow(false)
    val isLoadingSalaryComponentDetail: StateFlow<Boolean> = _isLoadingSalaryComponentDetail.asStateFlow()

    private val _isSubmittingSalaryComponent = MutableStateFlow(false)
    val isSubmittingSalaryComponent: StateFlow<Boolean> = _isSubmittingSalaryComponent.asStateFlow()

    // ── Fetch All ──
    fun fetchSalaryComponents(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
        type: String? = null,
        status: String? = null
    ) {
        launchBusy {
            _isLoadingSalaryComponents.value = true
            _salaryComponentError.value = null

            val result = hrRepository.getSalaryComponents(
                page = page,
                limit = limit,
                search = search,
                type = type,
                status = status
            )

            result.fold(
                onSuccess = { listData ->
                    _salaryComponents.value = listData.data
                    _salaryComponentTotal.value = listData.pagination?.total ?: listData.data.size
                },
                onFailure = { e ->
                    _salaryComponentError.value = e.message ?: "Failed to fetch salary components"
                }
            )
            _isLoadingSalaryComponents.value = false
        }
    }

    // ── Fetch One ──
    fun fetchSalaryComponentDetail(id: String) {
        viewModelScope.launch {
            _isLoadingSalaryComponentDetail.value = true
            val result = hrRepository.getSalaryComponentDetail(id)
            result.onSuccess { data ->
                _salaryComponentDetail.value = data
                _isLoadingSalaryComponentDetail.value = false
            }.onFailure {
                _isLoadingSalaryComponentDetail.value = false
            }
        }
    }

    fun clearSalaryComponentDetail() {
        _salaryComponentDetail.value = null
    }

    // ── Create ──
    fun createSalaryComponent(
        request: SalaryComponentRequest,
        onSuccess: (SalaryComponentItem) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingSalaryComponent.value = true
            val result = hrRepository.createSalaryComponent(request)
            result.onSuccess { createdItem ->
                _isSubmittingSalaryComponent.value = false
                fetchSalaryComponents() // Refresh list
                onSuccess(createdItem)
            }.onFailure { e ->
                _isSubmittingSalaryComponent.value = false
                onError(e.localizedMessage ?: "Failed to create salary component")
            }
        }
    }

    // ── Update ──
    fun updateSalaryComponent(
        id: String,
        request: SalaryComponentRequest,
        onSuccess: (SalaryComponentItem) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingSalaryComponent.value = true
            val result = hrRepository.updateSalaryComponent(id, request)
            result.onSuccess { updatedItem ->
                _isSubmittingSalaryComponent.value = false
                fetchSalaryComponents() // Refresh list
                onSuccess(updatedItem)
            }.onFailure { e ->
                _isSubmittingSalaryComponent.value = false
                onError(e.localizedMessage ?: "Failed to update salary component")
            }
        }
    }

    // ── Toggle Active / Inactive ──
    fun toggleSalaryComponentStatus(
        id: String,
        isActive: Boolean,
        onSuccess: (SalaryComponentItem) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = hrRepository.toggleSalaryComponentStatus(id, isActive)
            result.onSuccess { updatedItem ->
                // Update item in local list directly to avoid full reload flicker
                _salaryComponents.value = _salaryComponents.value.map { item ->
                    if (item.id == id) updatedItem else item
                }
                onSuccess(updatedItem)
            }.onFailure { e ->
                onError(e.localizedMessage ?: "Failed to change status")
            }
        }
    }

    // ── Delete ──
    fun deleteSalaryComponent(
        id: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = hrRepository.deleteSalaryComponent(id)
            result.onSuccess {
                _salaryComponents.value = _salaryComponents.value.filter { it.id != id }
                onSuccess()
            }.onFailure { e ->
                onError(e.localizedMessage ?: "Failed to delete salary component")
            }
        }
    }

    //NEW GENERIC API

    // ─────────────────────────────────────────────
    // LIST  (GET with query params)
    // ─────────────────────────────────────────────

    // Loading / Error state of the list call
    private val _list = MutableStateFlow<UiState<SalaryComponentListResponse>>(UiState.Idle)
    val list: StateFlow<UiState<SalaryComponentListResponse>> = _list.asStateFlow()

    // Plain list used by the UI (also edited locally after toggle / delete)
    private val _components = MutableStateFlow<List<SalaryComponentItem>>(emptyList())
    val components: StateFlow<List<SalaryComponentItem>> = _components.asStateFlow()

    private val _total = MutableStateFlow(0)
    val total: StateFlow<Int> = _total.asStateFlow()

    fun loadList(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
        type: String? = null,
        status: String? = null
    ) {
        launchState(
            state = _list,
            onSuccess = { res ->
                // res.data = list data, res.data.data = items
                _components.value = res.data.data
                _total.value = res.data.pagination?.total ?: res.data.data.size
            }
        ) {
            repo.request<SalaryComponentListResponse> {
                get(
                    "$SALARY_BASE/view-all",
                    repo.query(
                        "page" to page,
                        "limit" to limit,
                        "search" to search,
                        "type" to type,
                        "status" to status
                    )
                )
            }
        }
    }

    // ─────────────────────────────────────────────
    // DETAIL  (GET one)
    // ─────────────────────────────────────────────

    private val _detail = MutableStateFlow<UiState<SalaryComponentSingleResponse>>(UiState.Idle)
    val detail: StateFlow<UiState<SalaryComponentSingleResponse>> = _detail.asStateFlow()

    fun loadDetail(id: String) {
        launchState(_detail) {
            repo.request<SalaryComponentSingleResponse> { get("$SALARY_BASE/view-one/$id") }
        }
    }
    // Reset detail when the form opens in "create" mode
    fun clearDetail() {
        _detail.value = UiState.Idle
    }

    // ─────────────────────────────────────────────
    // CREATE / UPDATE  (POST / PUT with JSON body)
    // ─────────────────────────────────────────────

    // Shared by create and update (Loading = button spinner)
    private val _save = MutableStateFlow<UiState<SalaryComponentSingleResponse>>(UiState.Idle)
    val save: StateFlow<UiState<SalaryComponentSingleResponse>> = _save.asStateFlow()

    fun create(
        request: SalaryComponentRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _save,
            onSuccess = {
                loadList() // refresh list after create
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<SalaryComponentSingleResponse> { post("$SALARY_BASE/create", request) }
        }
    }

    fun update(
        id: String,
        request: SalaryComponentRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _save,
            onSuccess = {
                loadList() // refresh list after update
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<SalaryComponentSingleResponse> { put("$SALARY_BASE/update/$id", request) }
        }
    }

    // ─────────────────────────────────────────────
    // TOGGLE ACTIVE / INACTIVE  (PATCH with JSON body)
    // ─────────────────────────────────────────────

    private val _toggle = MutableStateFlow<UiState<SalaryComponentSingleResponse>>(UiState.Idle)
    val toggle: StateFlow<UiState<SalaryComponentSingleResponse>> = _toggle.asStateFlow()

    fun toggleStatus(
        id: String,
        isActive: Boolean,
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _toggle,
            onSuccess = { res ->
                // Replace only the changed item locally
                res.data?.let { updated ->
                    _components.value = _components.value.map { if (it.id == id) updated else it }
                }
            },
            onError = onError
        ) {
            repo.request<SalaryComponentSingleResponse> {
                patch("$SALARY_BASE/delete/$id", ToggleSalaryComponentStatusRequest(isActive))
            }
        }
    }

    // ─────────────────────────────────────────────
    // DELETE  (DELETE without body)
    // ─────────────────────────────────────────────

    private val _delete = MutableStateFlow<UiState<SalaryComponentSingleResponse>>(UiState.Idle)
    val delete: StateFlow<UiState<SalaryComponentSingleResponse>> = _delete.asStateFlow()

    fun deleteComponent(
        id: String,
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _delete,
            onSuccess = {
                // Remove the item from the local list
                _components.value = _components.value.filter { it.id != id }
            },
            onError = onError
        ) {
            repo.request<SalaryComponentSingleResponse> { delete("$SALARY_BASE/delete/$id") }
        }
    }


    //SALARY TEMPLATE

    // HrViewModel.kt - Add this constant at the top
    private val TEMPLATE_BASE = "/api/hr/salary-template"

// Add these inside HrViewModel class:

    // ─────────────────────────────────────────────
    // SALARY TEMPLATES (LIST / VIEW ALL)
    // ─────────────────────────────────────────────
    private val _templateListState = MutableStateFlow<UiState<SalaryTemplateListResponse>>(UiState.Idle)
    val templateListState: StateFlow<UiState<SalaryTemplateListResponse>> = _templateListState.asStateFlow()

    private val _salaryTemplates = MutableStateFlow<List<SalaryTemplateDto>>(emptyList())
    val salaryTemplates: StateFlow<List<SalaryTemplateDto>> = _salaryTemplates.asStateFlow()

    private val _templateTotal = MutableStateFlow(0)
    val templateTotal: StateFlow<Int> = _templateTotal.asStateFlow()

    fun fetchSalaryTemplates(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null
    ) {
        launchState(
            state = _templateListState,
            onSuccess = { res ->
                _salaryTemplates.value = res.data.data
                _templateTotal.value = res.data.pagination?.total ?: res.data.data.size
            }
        ) {
            repo.request<SalaryTemplateListResponse> {
                get(
                    "$TEMPLATE_BASE/view-all",
                    repo.query(
                        "page" to page,
                        "limit" to limit,
                        "search" to search
                    )
                )
            }
        }
    }

    // ─────────────────────────────────────────────
    // SALARY TEMPLATE DETAIL (VIEW ONE)
    // ─────────────────────────────────────────────
    private val _templateDetailState = MutableStateFlow<UiState<SalaryTemplateSingleResponse>>(UiState.Idle)
    val templateDetailState: StateFlow<UiState<SalaryTemplateSingleResponse>> = _templateDetailState.asStateFlow()

    fun fetchSalaryTemplateDetail(templateId: String) {
        launchState(_templateDetailState) {
            repo.request<SalaryTemplateSingleResponse> {
                get("$TEMPLATE_BASE/view-one/$templateId")
            }
        }
    }

    fun clearTemplateDetail() {
        _templateDetailState.value = UiState.Idle
    }

    // ─────────────────────────────────────────────
    // CREATE / UPDATE / DELETE SALARY TEMPLATE
    // ─────────────────────────────────────────────
    private val _saveTemplateState = MutableStateFlow<UiState<SalaryTemplateSingleResponse>>(UiState.Idle)
    val saveTemplateState: StateFlow<UiState<SalaryTemplateSingleResponse>> = _saveTemplateState.asStateFlow()

    fun createSalaryTemplate(
        request: SaveSalaryTemplateRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _saveTemplateState,
            onSuccess = {
                fetchSalaryTemplates()
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<SalaryTemplateSingleResponse> {
                post("$TEMPLATE_BASE/create", request)
            }
        }
    }

    fun updateSalaryTemplate(
        templateId: String,
        request: SaveSalaryTemplateRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _saveTemplateState,
            onSuccess = {
                fetchSalaryTemplates()
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<SalaryTemplateSingleResponse> {
                put("$TEMPLATE_BASE/update/$templateId", request)
            }
        }
    }

    fun deleteSalaryTemplate(
        templateId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _saveTemplateState,
            onSuccess = {
                _salaryTemplates.value = _salaryTemplates.value.filter { it.id != templateId }
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<SalaryTemplateSingleResponse> {
                delete("$TEMPLATE_BASE/delete/$templateId")
            }
        }
    }

    //TRAINING MANAGEMENT

    // HrViewModel.kt - Add constant at top
    private val TRAINING_PROGRAM_BASE = "/api/hr/training-program"

// Add inside HrViewModel class:

    // ─────────────────────────────────────────────
    // TRAINING PROGRAM (VIEW ALL / VIEW ONE / CREATE / UPDATE / DELETE)
    // ─────────────────────────────────────────────

    private val _trainingListState = MutableStateFlow<UiState<TrainingProgramListResponse>>(UiState.Idle)
    val trainingListState: StateFlow<UiState<TrainingProgramListResponse>> = _trainingListState.asStateFlow()

    private val _trainingPrograms = MutableStateFlow<List<TrainingProgramDto>>(emptyList())
    val trainingPrograms: StateFlow<List<TrainingProgramDto>> = _trainingPrograms.asStateFlow()

    private val _trainingProgramsPagingState =
        MutableStateFlow(PagingUiState<TrainingProgramDto>())
    val trainingProgramsPagingState: StateFlow<PagingUiState<TrainingProgramDto>> =
        _trainingProgramsPagingState.asStateFlow()

    private var trainingCurrentPage = 1
    private val trainingPageSize = 15
    private var activeTrainingSearch: String? = null
    private var fetchTrainingJob: Job? = null
    private var trainingSearchDebounceJob: Job? = null

    fun searchTrainingPrograms(query: String) {
        trainingSearchDebounceJob?.cancel()
        trainingSearchDebounceJob = viewModelScope.launch {
            delay(350)
            fetchTrainingPrograms(search = query.trim().ifBlank { null })
        }
    }

    fun fetchTrainingPrograms(search: String? = activeTrainingSearch) {
        fetchTrainingJob?.cancel()
        trainingCurrentPage = 1
        activeTrainingSearch = search?.trim()?.ifBlank { null }

        fetchTrainingJob = launchBusy {
            _trainingProgramsPagingState.value = PagingUiState(
                items = emptyList(),
                isInitialLoading = true,
                isLoadingMore = false,
                canLoadMore = false
            )

            val result = repo.request<TrainingProgramListResponse> {
                get(
                    "$TRAINING_PROGRAM_BASE/view-all",
                    repo.query(
                        "search" to activeTrainingSearch,
                        "page" to trainingCurrentPage,
                        "limit" to trainingPageSize
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val programs = res.data
                    _trainingPrograms.value = programs
                    _trainingProgramsPagingState.value = PagingUiState(
                        items = programs,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = programs.size >= trainingPageSize
                    )
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _trainingPrograms.value = emptyList()
                        _trainingProgramsPagingState.value = PagingUiState(
                            items = emptyList(),
                            isInitialLoading = false,
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                }
            )
        }
    }

    fun loadMoreTrainingPrograms() {
        val current = _trainingProgramsPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) {
            return
        }

        launchBusy {
            _trainingProgramsPagingState.value = current.copy(isLoadingMore = true)
            val nextPage = trainingCurrentPage + 1

            val result = repo.request<TrainingProgramListResponse> {
                get(
                    "$TRAINING_PROGRAM_BASE/view-all",
                    repo.query(
                        "search" to activeTrainingSearch,
                        "page" to nextPage,
                        "limit" to trainingPageSize
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val newPrograms = res.data
                    val merged = (current.items + newPrograms).distinctBy { it.id }
                    val hasNewItems = merged.size > current.items.size

                    if (hasNewItems) {
                        trainingCurrentPage = nextPage
                        _trainingPrograms.value = merged
                        _trainingProgramsPagingState.value = current.copy(
                            items = merged,
                            isLoadingMore = false,
                            canLoadMore = newPrograms.size >= trainingPageSize
                        )
                    } else {
                        _trainingProgramsPagingState.value = current.copy(
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                },
                onFailure = {
                    _trainingProgramsPagingState.value = current.copy(isLoadingMore = false)
                }
            )
        }
    }

    private val _trainingDetailState = MutableStateFlow<UiState<TrainingProgramSingleResponse>>(UiState.Idle)
    val trainingDetailState: StateFlow<UiState<TrainingProgramSingleResponse>> = _trainingDetailState.asStateFlow()

    fun fetchTrainingProgramDetail(id: String) {
        launchState(_trainingDetailState) {
            repo.request<TrainingProgramSingleResponse> {
                get("$TRAINING_PROGRAM_BASE/view-one/$id")
            }
        }
    }

    fun clearTrainingProgramDetail() {
        _trainingDetailState.value = UiState.Idle
    }

    private val _saveTrainingState = MutableStateFlow<UiState<TrainingProgramSingleResponse>>(UiState.Idle)
    val saveTrainingState: StateFlow<UiState<TrainingProgramSingleResponse>> = _saveTrainingState.asStateFlow()

    fun createTrainingProgram(
        request: SaveTrainingProgramRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _saveTrainingState,
            onSuccess = {
                fetchTrainingPrograms()
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<TrainingProgramSingleResponse> {
                post("$TRAINING_PROGRAM_BASE/create", request)
            }
        }
    }

    fun updateTrainingProgram(
        id: String,
        request: SaveTrainingProgramRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _saveTrainingState,
            onSuccess = {
                fetchTrainingPrograms()
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<TrainingProgramSingleResponse> {
                put("$TRAINING_PROGRAM_BASE/update/$id", request)
            }
        }
    }

    private val _deleteTrainingState = MutableStateFlow<UiState<SimpleActionResponse>>(UiState.Idle)
    val deleteTrainingState: StateFlow<UiState<SimpleActionResponse>> = _deleteTrainingState.asStateFlow()

    fun deleteTrainingProgram(
        id: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _deleteTrainingState,
            onSuccess = {
                val updatedList = _trainingPrograms.value.filter { it.id != id }
                _trainingPrograms.value = updatedList

                val current = _trainingProgramsPagingState.value
                _trainingProgramsPagingState.value = current.copy(
                    items = current.items.filter { it.id != id }
                )
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<SimpleActionResponse> {
                delete("$TRAINING_PROGRAM_BASE/delete/$id")
            }
        }
    }

    //ASSIGNED TRAINING

    // HrViewModel.kt - Base path for assigned training endpoints
    private val ASSIGNED_TRAINING_BASE = "/api/hr/assign-trainingprogram"

// Add inside HrViewModel class:

    // ─────────────────────────────────────────────
    // ASSIGNED TRAINING PROGRAM (LIST, DETAIL, CREATE, UPDATE, DELETE)
    // ─────────────────────────────────────────────

    private val _assignedTrainingListState = MutableStateFlow<UiState<AssignedTrainingListResponse>>(UiState.Idle)
    val assignedTrainingListState: StateFlow<UiState<AssignedTrainingListResponse>> = _assignedTrainingListState.asStateFlow()

    private val _assignedTrainingPrograms = MutableStateFlow<List<AssignedTrainingProgramDto>>(emptyList())
    val assignedTrainingPrograms: StateFlow<List<AssignedTrainingProgramDto>> = _assignedTrainingPrograms.asStateFlow()

    private val _assignedTrainingPagingState =
        MutableStateFlow(PagingUiState<AssignedTrainingProgramDto>())
    val assignedTrainingPagingState: StateFlow<PagingUiState<AssignedTrainingProgramDto>> =
        _assignedTrainingPagingState.asStateFlow()

    private var assignedCurrentPage = 1
    private val assignedPageSize = 15
    private var activeAssignedSearch: String? = null
    private var fetchAssignedJob: Job? = null
    private var assignedSearchDebounceJob: Job? = null

    fun searchAssignedTrainingPrograms(query: String) {
        assignedSearchDebounceJob?.cancel()
        assignedSearchDebounceJob = viewModelScope.launch {
            delay(350)
            fetchAssignedTrainingPrograms(search = query.trim().ifBlank { null })
        }
    }

    fun fetchAssignedTrainingPrograms(search: String? = activeAssignedSearch) {
        fetchAssignedJob?.cancel()
        assignedCurrentPage = 1
        activeAssignedSearch = search?.trim()?.ifBlank { null }

        fetchAssignedJob = launchBusy {
            _assignedTrainingPagingState.value = PagingUiState(isInitialLoading = true)
            val result = repo.request<AssignedTrainingListResponse> {
                get(
                    "$ASSIGNED_TRAINING_BASE/view-all",
                    repo.query(
                        "search" to activeAssignedSearch,
                        "page" to assignedCurrentPage,
                        "limit" to assignedPageSize
                    )
                )
            }
            result.fold(
                onSuccess = { res ->
                    _assignedTrainingPrograms.value = res.data
                    _assignedTrainingPagingState.value = PagingUiState(
                        items = res.data,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = res.data.size >= assignedPageSize
                    )
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _assignedTrainingPrograms.value = emptyList()
                        _assignedTrainingPagingState.value = PagingUiState(isInitialLoading = false)
                    }
                }
            )
        }
    }

    fun loadMoreAssignedTrainingPrograms() {
        val current = _assignedTrainingPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return

        launchBusy {
            _assignedTrainingPagingState.value = current.copy(isLoadingMore = true)
            val nextPage = assignedCurrentPage + 1

            val result = repo.request<AssignedTrainingListResponse> {
                get(
                    "$ASSIGNED_TRAINING_BASE/view-all",
                    repo.query(
                        "search" to activeAssignedSearch,
                        "page" to nextPage,
                        "limit" to assignedPageSize
                    )
                )
            }
            result.fold(
                onSuccess = { res ->
                    val newItems = res.data
                    val merged = (current.items + newItems).distinctBy { it.id }
                    val hasNewItems = merged.size > current.items.size
                    if (hasNewItems) {
                        assignedCurrentPage = nextPage
                        _assignedTrainingPrograms.value = merged
                        _assignedTrainingPagingState.value = current.copy(
                            items = merged,
                            isLoadingMore = false,
                            canLoadMore = newItems.size >= assignedPageSize
                        )
                    } else {
                        _assignedTrainingPagingState.value = current.copy(
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                },
                onFailure = {
                    _assignedTrainingPagingState.value = current.copy(isLoadingMore = false)
                }
            )
        }
    }

    private val _assignedTrainingDetailState = MutableStateFlow<UiState<AssignedTrainingSingleResponse>>(UiState.Idle)
    val assignedTrainingDetailState: StateFlow<UiState<AssignedTrainingSingleResponse>> = _assignedTrainingDetailState.asStateFlow()

    fun fetchAssignedTrainingDetail(id: String) {
        launchState(_assignedTrainingDetailState) {
            repo.request<AssignedTrainingSingleResponse> {
                get("$ASSIGNED_TRAINING_BASE/view-one/$id")
            }
        }
    }

    fun clearAssignedTrainingDetail() {
        _assignedTrainingDetailState.value = UiState.Idle
    }

    private val _saveAssignedTrainingState = MutableStateFlow<UiState<AssignedTrainingSingleResponse>>(UiState.Idle)
    val saveAssignedTrainingState: StateFlow<UiState<AssignedTrainingSingleResponse>> = _saveAssignedTrainingState.asStateFlow()

    fun createAssignedTraining(
        request: SaveAssignedTrainingRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _saveAssignedTrainingState,
            onSuccess = {
                fetchAssignedTrainingPrograms()
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<AssignedTrainingSingleResponse> {
                post("$ASSIGNED_TRAINING_BASE/create", request)
            }
        }
    }

    fun updateAssignedTraining(
        id: String,
        request: SaveAssignedTrainingRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _saveAssignedTrainingState,
            onSuccess = {
                fetchAssignedTrainingPrograms()
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<AssignedTrainingSingleResponse> {
                put("$ASSIGNED_TRAINING_BASE/update/$id", request)
            }
        }
    }

    private val _deleteAssignedTrainingState = MutableStateFlow<UiState<SimpleActionResponse>>(UiState.Idle)
    val deleteAssignedTrainingState: StateFlow<UiState<SimpleActionResponse>> = _deleteAssignedTrainingState.asStateFlow()

    fun deleteAssignedTraining(
        id: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchState(
            state = _deleteAssignedTrainingState,
            onSuccess = {
                _assignedTrainingPrograms.value = _assignedTrainingPrograms.value.filter { it.id != id }
                val current = _assignedTrainingPagingState.value
                _assignedTrainingPagingState.value = current.copy(
                    items = current.items.filter { it.id != id }
                )
                onSuccess()
            },
            onError = onError
        ) {
            repo.request<SimpleActionResponse> {
                delete("$ASSIGNED_TRAINING_BASE/delete/$id")
            }
        }
    }

    //DOCUMENT CATEGORY & EMPLOYEE DOCUMENT

    // ── Add these constants at the top of HrViewModel.kt ──
    private val DOC_CATEGORY_BASE = "/api/hr/document-category"
    private val EMP_DOCUMENT_BASE = "/api/hr/employee-document"

    // ── Add inside the HrViewModel class ──

    // Document Categories State
    private val _documentCategories = MutableStateFlow<List<DocumentCategoryDto>>(emptyList())
    val documentCategories: StateFlow<List<DocumentCategoryDto>> = _documentCategories.asStateFlow()

    private val _isLoadingCategories = MutableStateFlow(false)
    val isLoadingCategories: StateFlow<Boolean> = _isLoadingCategories.asStateFlow()

    // Employee Documents Paginated State
    private val _employeeDocumentsPagingState =
        MutableStateFlow(PagingUiState<EmployeeDocumentDto>())
    val employeeDocumentsPagingState: StateFlow<PagingUiState<EmployeeDocumentDto>> =
        _employeeDocumentsPagingState.asStateFlow()

    private var docCurrentPage = 1
    private val docPageSize = 15
    private var activeDocSearch: String? = null
    private var fetchDocumentsJob: Job? = null
    private var docSearchDebounceJob: Job? = null

    private val _isSubmittingUpload = MutableStateFlow(false)
    val isSubmittingUpload: StateFlow<Boolean> = _isSubmittingUpload.asStateFlow()

    // 1. Fetch Document Categories for dropdown
    fun fetchDocumentCategories() {
        launchBusy {
            _isLoadingCategories.value = true
            val result = repo.request<DocumentCategoryListResponse> {
                get("$DOC_CATEGORY_BASE/view-all")
            }
            result.fold(
                onSuccess = { res ->
                    _documentCategories.value = res.data
                },
                onFailure = {
                    _documentCategories.value = emptyList()
                }
            )
            _isLoadingCategories.value = false
        }
    }

    // Debounced search handler for Employee Documents
    fun searchEmployeeDocuments(query: String) {
        docSearchDebounceJob?.cancel()
        docSearchDebounceJob = viewModelScope.launch {
            delay(350)
            fetchEmployeeDocuments(search = query.trim().ifBlank { null })
        }
    }

    // 2. Fetch Initial Page of Employee Documents
    fun fetchEmployeeDocuments(search: String? = activeDocSearch) {
        fetchDocumentsJob?.cancel()
        docCurrentPage = 1
        activeDocSearch = search?.trim()?.ifBlank { null }

        fetchDocumentsJob = launchBusy {
            _employeeDocumentsPagingState.value = PagingUiState(
                items = emptyList(),
                isInitialLoading = true,
                isLoadingMore = false,
                canLoadMore = false
            )

            val result = repo.request<EmployeeDocumentListResponse> {
                get(
                    "$EMP_DOCUMENT_BASE/view-all",
                    repo.query(
                        "search" to activeDocSearch,
                        "page" to docCurrentPage,
                        "limit" to docPageSize
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val docs = res.data?.documents.orEmpty()
                    _employeeDocumentsPagingState.value = PagingUiState(
                        items = docs,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = docs.size >= docPageSize
                    )
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _employeeDocumentsPagingState.value = PagingUiState(
                            items = emptyList(),
                            isInitialLoading = false,
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                }
            )
        }
    }

    // Load Next Page of Employee Documents for PaginatedDataList
    fun loadMoreEmployeeDocuments() {
        val current = _employeeDocumentsPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) {
            return
        }

        launchBusy {
            _employeeDocumentsPagingState.value = current.copy(isLoadingMore = true)
            val nextPage = docCurrentPage + 1

            val result = repo.request<EmployeeDocumentListResponse> {
                get(
                    "$EMP_DOCUMENT_BASE/view-all",
                    repo.query(
                        "search" to activeDocSearch,
                        "page" to nextPage,
                        "limit" to docPageSize
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val newDocs = res.data?.documents.orEmpty()
                    if (newDocs.isNotEmpty()) {
                        docCurrentPage = nextPage
                        val merged = (current.items + newDocs).distinctBy { it.id }
                        _employeeDocumentsPagingState.value = current.copy(
                            items = merged,
                            isLoadingMore = false,
                            canLoadMore = newDocs.size >= docPageSize
                        )
                    } else {
                        _employeeDocumentsPagingState.value = current.copy(
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                },
                onFailure = {
                    _employeeDocumentsPagingState.value = current.copy(isLoadingMore = false)
                }
            )
        }
    }

    // 3. Upload new employee document
    fun uploadEmployeeDocument(
        memberId: String,
        categoryId: String,
        title: String,
        issueDate: String,
        expiryDate: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (memberId.isBlank()) {
            onError("Please select an employee")
            return
        }
        if (categoryId.isBlank()) {
            onError("Please select a document category")
            return
        }
        if (title.isBlank()) {
            onError("Please enter a document title")
            return
        }

        viewModelScope.launch {
            _isSubmittingUpload.value = true
            // Make the API call using your repository
            // Replace with actual multipart/json upload endpoint
            _isSubmittingUpload.value = false
            fetchEmployeeDocuments()
            onSuccess()
        }
    }

    // ── Inside HrViewModel.kt ──
    fun uploadEmployeeDocument(
        memberId: String,
        categoryId: String,
        title: String,
        issueDate: String,
        expiryDate: String,
        notes: String,
        files: List<File>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (memberId.isBlank()) {
            onError("Please select an employee")
            return
        }
        if (categoryId.isBlank()) {
            onError("Please select a document category")
            return
        }
        if (title.isBlank()) {
            onError("Please enter a document title")
            return
        }
        if (issueDate.isBlank()) {
            onError("Please select an issue date")
            return
        }
        if (expiryDate.isBlank()) {
            onError("Please select an expiry date")
            return
        }
        if (files.isEmpty()) {
            onError("Please attach at least one document file")
            return
        }

        viewModelScope.launch {
            _isSubmittingUpload.value = true

            // 1. Form data fields matching the Web payload
            val parts = mapOf(
                "organizationMemberId" to repo.text(memberId),
                "documentCategoryId" to repo.text(categoryId),
                "title" to repo.text(title),
                "issueDate" to repo.text(convertUiDateToApiDate(issueDate)),
                "expiryDate" to repo.text(convertUiDateToApiDate(expiryDate)),
                "downloadEligible" to repo.text("true"),
                "status" to repo.text("Active"),
                "notes" to repo.text(notes)
            )

            // 2. Prepare binary files with exact MIME type (e.g., image/jpeg)
            val fileParts = files.map { file ->
                val extension = file.extension.lowercase()
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: when (extension) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "pdf" -> "application/pdf"
                        "webp" -> "image/webp"
                        else -> "application/octet-stream"
                    }

                repo.filePart("files", file, mimeType)
            }

            // 3. Post Multipart
            val result = repo.request<CreateEmployeeDocumentResponse> {
                postMultipart(
                    url = "$EMP_DOCUMENT_BASE/create",
                    parts = parts,
                    files = fileParts
                )
            }

            result.fold(
                onSuccess = {
                    _isSubmittingUpload.value = false
                    fetchEmployeeDocuments() // Refresh list
                    onSuccess()
                },
                onFailure = { error ->
                    _isSubmittingUpload.value = false
                    onError(error.message ?: "Failed to upload document")
                }
            )
        }
    }

    // ── Single Employee Document State (View-One & Edit) ──
    private val _documentDetail = MutableStateFlow<EmployeeDocumentDto?>(null)
    val documentDetail: StateFlow<EmployeeDocumentDto?> = _documentDetail.asStateFlow()

    private val _isLoadingDocumentDetail = MutableStateFlow(false)
    val isLoadingDocumentDetail: StateFlow<Boolean> = _isLoadingDocumentDetail.asStateFlow()

    fun clearDocumentDetail() {
        _documentDetail.value = null
    }

    // 4. Fetch Single Employee Document by ID (View-One)
    fun fetchEmployeeDocumentById(id: String, onError: (String) -> Unit = {}) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isLoadingDocumentDetail.value = true
            _documentDetail.value = null
            val result = repo.request<EmployeeDocumentSingleResponse> {
                get("$EMP_DOCUMENT_BASE/view-one/$id")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _documentDetail.value = res.data
                    } else {
                        onError(res.message ?: "Failed to fetch document details")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to fetch document details")
                }
            )
            _isLoadingDocumentDetail.value = false
        }
    }

    // 5. Update Employee Document (Multipart PUT via GenericApi)
    fun updateEmployeeDocument(
        documentId: String,
        memberId: String,
        categoryId: String,
        title: String,
        issueDate: String,
        expiryDate: String,
        notes: String,
        files: List<File>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (memberId.isBlank()) {
            onError("Please select an employee")
            return
        }
        if (categoryId.isBlank()) {
            onError("Please select a document category")
            return
        }
        if (title.isBlank()) {
            onError("Please enter a document title")
            return
        }
        if (issueDate.isBlank()) {
            onError("Please select an issue date")
            return
        }
        if (expiryDate.isBlank()) {
            onError("Please select an expiry date")
            return
        }

        viewModelScope.launch {
            _isSubmittingUpload.value = true

            // Form data fields
            val parts = mutableMapOf(
                "organizationMemberId" to repo.text(memberId),
                "documentCategoryId" to repo.text(categoryId),
                "title" to repo.text(title),
                "issueDate" to repo.text(convertUiDateToApiDate(issueDate)),
                "expiryDate" to repo.text(convertUiDateToApiDate(expiryDate)),
                "downloadEligible" to repo.text("true"),
                "status" to repo.text("Active"),
                "notes" to repo.text(notes)
            )

            // Convert files to MultipartBody.Part if new files were selected
            val fileParts = files.map { file ->
                val extension = file.extension.lowercase()
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: when (extension) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "pdf" -> "application/pdf"
                        "webp" -> "image/webp"
                        else -> "application/octet-stream"
                    }

                repo.filePart("files", file, mimeType)
            }.takeIf { it.isNotEmpty() }

            val result = repo.request<EmployeeDocumentSingleResponse> {
                putMultipart(
                    url = "$EMP_DOCUMENT_BASE/update/$documentId",
                    parts = parts,
                    files = fileParts
                )
            }

            result.fold(
                onSuccess = {
                    _isSubmittingUpload.value = false
                    fetchEmployeeDocuments() // Refresh document list
                    onSuccess()
                },
                onFailure = { error ->
                    _isSubmittingUpload.value = false
                    onError(error.message ?: "Failed to update document")
                }
            )
        }
    }

    // ── Delete Employee Document ──
    // ── Delete Employee Document ──
    fun deleteEmployeeDocument(
        id: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            // FIXED: Use DeleteEmployeeDocumentResponse instead of EmployeeDocumentSingleResponse
            // This prevents Gson Expected BEGIN_OBJECT but was STRING parsing crash
            val result = repo.request<DeleteEmployeeDocumentResponse> {
                delete("$EMP_DOCUMENT_BASE/delete/$id")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        // Remove deleted item from local paging list without reloading entire screen
                        val current = _employeeDocumentsPagingState.value
                        _employeeDocumentsPagingState.value = current.copy(
                            items = current.items.filter { it.id != id }
                        )
                        onSuccess()
                    } else {
                        onError(res.message ?: "Failed to delete employee document")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete employee document")
                }
            )
        }
    }

    // ═══════════════════════════════════════════════
    // ── Document Category Operations (Using Cat DTOs) ──
    // ═══════════════════════════════════════════════

    // Document Categories List State
    private val _documentCategoriesCat = MutableStateFlow<List<DocumentCategoryDtoCat>>(emptyList())
    val documentCategoriesCat: StateFlow<List<DocumentCategoryDtoCat>> = _documentCategoriesCat.asStateFlow()

    private val _isLoadingCategoriesCat = MutableStateFlow(false)
    val isLoadingCategoriesCat: StateFlow<Boolean> = _isLoadingCategoriesCat.asStateFlow()

    // View-One Category Detail State
    private val _categoryDetailCat = MutableStateFlow<DocumentCategoryDtoCat?>(null)
    val categoryDetailCat: StateFlow<DocumentCategoryDtoCat?> = _categoryDetailCat.asStateFlow()

    private val _isLoadingCategoryDetailCat = MutableStateFlow(false)
    val isLoadingCategoryDetailCat: StateFlow<Boolean> = _isLoadingCategoryDetailCat.asStateFlow()

    private val _isSubmittingCategoryCat = MutableStateFlow(false)
    val isSubmittingCategoryCat: StateFlow<Boolean> = _isSubmittingCategoryCat.asStateFlow()

    // Clear single category detail on screen dismiss
    fun clearCategoryDetailCat() {
        _categoryDetailCat.value = null
    }

    private val _documentCategoriesCatPagingState =
        MutableStateFlow(PagingUiState<DocumentCategoryDtoCat>())
    val documentCategoriesCatPagingState: StateFlow<PagingUiState<DocumentCategoryDtoCat>> =
        _documentCategoriesCatPagingState.asStateFlow()

    private var catSearchQuery: String = ""
    private var catCurrentPage: Int = 1
    private val catPageSize: Int = 10

    fun onDocumentCategorySearchChanged(query: String) {
        catSearchQuery = query
        rebuildDocCategoryPagingState(resetPage = true)
    }

    fun loadMoreDocumentCategoriesCat() {
        val current = _documentCategoriesCatPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return
        catCurrentPage += 1
        rebuildDocCategoryPagingState(resetPage = false)
    }

    private fun rebuildDocCategoryPagingState(resetPage: Boolean) {
        if (resetPage) catCurrentPage = 1
        val allCategories = _documentCategoriesCat.value
        val filtered = if (catSearchQuery.isBlank()) {
            allCategories
        } else {
            allCategories.filter {
                it.categoryName.contains(catSearchQuery, ignoreCase = true) ||
                        (it.description ?: "").contains(catSearchQuery, ignoreCase = true)
            }
        }
        val paged = filtered.take(catCurrentPage * catPageSize)
        _documentCategoriesCatPagingState.value = PagingUiState(
            items = paged,
            isInitialLoading = false,
            isLoadingMore = false,
            canLoadMore = paged.size < filtered.size
        )
    }

    // 1. View All Document Categories
    fun fetchDocumentCategoriesCat() {
        launchBusy {
            _isLoadingCategoriesCat.value = true
            _documentCategoriesCatPagingState.value = PagingUiState(isInitialLoading = true)
            val result = repo.request<DocumentCategoryListResponseCat> {
                get("$DOC_CATEGORY_BASE/view-all")
            }
            result.fold(
                onSuccess = { res ->
                    _documentCategoriesCat.value = res.data
                    rebuildDocCategoryPagingState(resetPage = true)
                },
                onFailure = {
                    _documentCategoriesCat.value = emptyList()
                    _documentCategoriesCatPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
            _isLoadingCategoriesCat.value = false
        }
    }

    // 2. View One Document Category by ID
    fun fetchCategoryByIdCat(id: String, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoadingCategoryDetailCat.value = true
            val result = repo.request<DocumentCategorySingleResponse> {
                get("$DOC_CATEGORY_BASE/view-one/$id")
            }
            result.fold(
                onSuccess = { res ->
                    _categoryDetailCat.value = res.data
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to load category details")
                }
            )
            _isLoadingCategoryDetailCat.value = false
        }
    }

    // 3. Create Document Category
    fun createCategoryCat(
        request: SaveDocumentCategoryRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingCategoryCat.value = true
            val result = repo.request<DocumentCategorySingleResponse> {
                post("$DOC_CATEGORY_BASE/create", request)
            }
            result.fold(
                onSuccess = {
                    _isSubmittingCategoryCat.value = false
                    fetchDocumentCategoriesCat() // Refresh list
                    onSuccess()
                },
                onFailure = { error ->
                    _isSubmittingCategoryCat.value = false
                    onError(error.message ?: "Failed to create document category")
                }
            )
        }
    }

    // 4. Update Document Category
    fun updateCategoryCat(
        id: String,
        request: SaveDocumentCategoryRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingCategoryCat.value = true
            val result = repo.request<DocumentCategorySingleResponse> {
                put("$DOC_CATEGORY_BASE/update/$id", request)
            }
            result.fold(
                onSuccess = {
                    _isSubmittingCategoryCat.value = false
                    fetchDocumentCategoriesCat() // Refresh list
                    onSuccess()
                },
                onFailure = { error ->
                    _isSubmittingCategoryCat.value = false
                    onError(error.message ?: "Failed to update document category")
                }
            )
        }
    }

    // 5. Delete Document Category
    fun deleteCategoryCat(
        id: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repo.request<DeleteDocumentCategoryResponse> {
                delete("$DOC_CATEGORY_BASE/delete/$id")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        // Remove deleted item from local list to avoid screen reload flicker
                        _documentCategoriesCat.value = _documentCategoriesCat.value.filter { it.id != id }
                        rebuildDocCategoryPagingState(resetPage = false)
                        onSuccess()
                    } else {
                        onError(res.message ?: "Failed to delete document category")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete document category")
                }
            )
        }
    }

    // =========================================================================
// EXIT MANAGEMENT (VIEW-ALL, VIEW-ONE, CREATE, DELETE, APPROVAL)
// =========================================================================

    private val _exitRequests = MutableStateFlow<List<ExitRequestDto>>(emptyList())
    val exitRequests: StateFlow<List<ExitRequestDto>> = _exitRequests.asStateFlow()

    private val _isLoadingExitRequests = MutableStateFlow(false)
    val isLoadingExitRequests: StateFlow<Boolean> = _isLoadingExitRequests.asStateFlow()

    private val _exitRequestsError = MutableStateFlow<String?>(null)
    val exitRequestsError: StateFlow<String?> = _exitRequestsError.asStateFlow()

    private val _exitRequestDetail = MutableStateFlow<ExitRequestDto?>(null)
    val exitRequestDetail: StateFlow<ExitRequestDto?> = _exitRequestDetail.asStateFlow()

    private val _isLoadingExitRequestDetail = MutableStateFlow(false)
    val isLoadingExitRequestDetail: StateFlow<Boolean> = _isLoadingExitRequestDetail.asStateFlow()

    private val _isSubmittingExitRequest = MutableStateFlow(false)
    val isSubmittingExitRequest: StateFlow<Boolean> = _isSubmittingExitRequest.asStateFlow()

    private val _isSubmittingExitDecision = MutableStateFlow(false)
    val isSubmittingExitDecision: StateFlow<Boolean> = _isSubmittingExitDecision.asStateFlow()

    private val _updatingClearanceItemId = MutableStateFlow<String?>(null)
    val updatingClearanceItemId: StateFlow<String?> = _updatingClearanceItemId.asStateFlow()

    private val _isFinalizingClearance = MutableStateFlow(false)
    val isFinalizingClearance: StateFlow<Boolean> = _isFinalizingClearance.asStateFlow()

    private val _exitAssignedAssets = MutableStateFlow<List<ExitAssignedAssetDto>>(emptyList())
    val exitAssignedAssets: StateFlow<List<ExitAssignedAssetDto>> = _exitAssignedAssets.asStateFlow()

    private val _isLoadingExitAssets = MutableStateFlow(false)
    val isLoadingExitAssets: StateFlow<Boolean> = _isLoadingExitAssets.asStateFlow()

    private val _exitAssetsError = MutableStateFlow<String?>(null)
    val exitAssetsError: StateFlow<String?> = _exitAssetsError.asStateFlow()

    private val _exitSettlement = MutableStateFlow<ExitSettlementDto?>(null)
    val exitSettlement: StateFlow<ExitSettlementDto?> = _exitSettlement.asStateFlow()

    private val _exitSettlementCalculation = MutableStateFlow<ExitSettlementCalculationDto?>(null)
    val exitSettlementCalculation: StateFlow<ExitSettlementCalculationDto?> = _exitSettlementCalculation.asStateFlow()

    private val _isLoadingExitSettlement = MutableStateFlow(false)
    val isLoadingExitSettlement: StateFlow<Boolean> = _isLoadingExitSettlement.asStateFlow()

    private val _isSavingExitSettlement = MutableStateFlow(false)
    val isSavingExitSettlement: StateFlow<Boolean> = _isSavingExitSettlement.asStateFlow()

    private val _exitSettlementError = MutableStateFlow<String?>(null)
    val exitSettlementError: StateFlow<String?> = _exitSettlementError.asStateFlow()

    private val _exitRequestsPagingState = MutableStateFlow(PagingUiState<ExitRequestDto>())
    val exitRequestsPagingState: StateFlow<PagingUiState<ExitRequestDto>> =
        _exitRequestsPagingState.asStateFlow()

    private var exitCurrentPage = 1
    private val exitPageSize = 15
    private var activeExitSearch: String? = null
    private var activeExitStatus: String? = null
    private var activeExitType: String? = null

    /**
     * Fetch all exit requests: GET /api/hr/exit-request/view-all
     */
    fun fetchExitRequests(
        page: Int = 1,
        limit: Int = exitPageSize,
        search: String? = null,
        status: String? = null,
        exitType: String? = null
    ) {
        exitCurrentPage = page
        activeExitSearch = search
        activeExitStatus = status
        activeExitType = exitType

        launchBusy {
            _isLoadingExitRequests.value = true
            _exitRequestsError.value = null
            _exitRequestsPagingState.value = PagingUiState(isInitialLoading = true)

            val result = repo.request<ExitRequestListResponse> {
                get(
                    "$EXIT_REQUEST_BASE/view-all",
                    repo.query(
                        "page" to page,
                        "limit" to limit,
                        "search" to search,
                        "status" to status,
                        "exitType" to exitType
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _exitRequests.value = res.data
                    _exitRequestsPagingState.value = PagingUiState(
                        items = res.data,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = res.data.size >= limit
                    )
                },
                onFailure = { error ->
                    _exitRequestsError.value = error.message ?: "Failed to fetch exit requests"
                    _exitRequestsPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
            _isLoadingExitRequests.value = false
        }
    }

    fun loadMoreExitRequests() {
        val current = _exitRequestsPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return

        launchBusy {
            _exitRequestsPagingState.value = current.copy(isLoadingMore = true)
            val nextPage = exitCurrentPage + 1

            val result = repo.request<ExitRequestListResponse> {
                get(
                    "$EXIT_REQUEST_BASE/view-all",
                    repo.query(
                        "page" to nextPage,
                        "limit" to exitPageSize,
                        "search" to activeExitSearch,
                        "status" to activeExitStatus,
                        "exitType" to activeExitType
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    val newItems = res.data
                    val merged = (current.items + newItems).distinctBy { it.id }
                    val hasNewItems = merged.size > current.items.size
                    if (hasNewItems) {
                        exitCurrentPage = nextPage
                        _exitRequests.value = merged
                        _exitRequestsPagingState.value = current.copy(
                            items = merged,
                            isLoadingMore = false,
                            canLoadMore = newItems.size >= exitPageSize
                        )
                    } else {
                        _exitRequestsPagingState.value = current.copy(
                            isLoadingMore = false,
                            canLoadMore = false
                        )
                    }
                },
                onFailure = {
                    _exitRequestsPagingState.value = current.copy(isLoadingMore = false)
                }
            )
        }
    }

    /**
     * Fetch single exit request: GET /api/hr/exit-request/view-one/{id}
     */
    fun fetchExitRequestById(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isLoadingExitRequestDetail.value = true
            val result = repo.request<ExitRequestSingleResponse> {
                get("$EXIT_REQUEST_BASE/view-one/$id")
            }
            result.fold(
                onSuccess = { res ->
                    _exitRequestDetail.value = res.data
                },
                onFailure = {
                    // Fallback to cached list item if view-one fails
                    _exitRequestDetail.value = _exitRequests.value.find { it.id == id }
                }
            )
            _isLoadingExitRequestDetail.value = false
        }
    }

    fun clearExitRequestDetail() {
        _exitRequestDetail.value = null
    }

    /**
     * Reuse exit-request view-all endpoint for Exit Clearance list screen
     */
    fun fetchExitClearanceList(
        page: Int = 1,
        limit: Int = 50,
        search: String? = null
    ) {
        fetchExitRequests(
            page = page,
            limit = limit,
            search = search
        )
    }

    /**
     * Reuse exit-request view-one endpoint for Exit Clearance checklist screen
     */
    fun fetchExitClearanceDetail(exitRequestId: String) {
        fetchExitRequestById(exitRequestId)
    }

    /**
     * Toggle a single clearance item status between "Cleared" and "Pending":
     * PATCH /api/hr/exit-request/{exitRequestId}/clearance-items/{itemId}
     */
    fun toggleClearanceItemStatus(
        exitRequestId: String,
        itemId: String,
        currentStatus: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (exitRequestId.isBlank() || itemId.isBlank() || _updatingClearanceItemId.value != null) {
            return
        }

        val previousDetail = _exitRequestDetail.value
        val nextStatus = if (currentStatus.equals("Cleared", ignoreCase = true)) "Pending" else "Cleared"

        viewModelScope.launch {
            _updatingClearanceItemId.value = itemId

            // Optimistically update the clicked item in local detail state
            previousDetail?.let { current ->
                _exitRequestDetail.value = current.copy(
                    clearanceItems = current.clearanceItems.map { item ->
                        if (item.id == itemId) item.copy(status = nextStatus) else item
                    }
                )
            }

            val payload = ExitClearanceItemStatusRequest(status = nextStatus)
            val endpoint = "$EXIT_REQUEST_BASE/$exitRequestId/clearance-items/$itemId"

            var result = repo.request<ExitRequestSingleResponse> {
                patch(endpoint, payload)
            }

            // Fallback to PUT if the backend route expects PUT instead of PATCH
            if (result.isFailure) {
                result = repo.request<ExitRequestSingleResponse> {
                    put(endpoint, payload)
                }
            }

            _updatingClearanceItemId.value = null

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updated ->
                            _exitRequestDetail.value = updated
                            _exitRequests.value = _exitRequests.value.map { listItem ->
                                if (listItem.id == exitRequestId) updated else listItem
                            }
                        }
                        onSuccess(res.message ?: "Task marked as $nextStatus")
                    } else {
                        _exitRequestDetail.value = previousDetail
                        onError(res.message ?: "Failed to update clearance item")
                    }
                },
                onFailure = { error ->
                    _exitRequestDetail.value = previousDetail
                    onError(error.message ?: "Failed to save clearance status")
                }
            )
        }
    }

    /**
     * Finalize department clearance once all checklist items are cleared
     */
    fun finalizeExitClearance(
        exitRequestId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (exitRequestId.isBlank() || _isFinalizingClearance.value) return

        viewModelScope.launch {
            _isFinalizingClearance.value = true
            val payload = ExitClearanceFinalizeRequest(
                status = "Clearance_Completed",
                clearanceStatus = "Completed"
            )
            val endpoint = "$EXIT_REQUEST_BASE/status/$exitRequestId"

            val result = repo.request<ExitRequestSingleResponse> {
                patch(endpoint, payload)
            }

            _isFinalizingClearance.value = false
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.data?.let { updated ->
                            _exitRequestDetail.value = updated
                            _exitRequests.value = _exitRequests.value.map { item ->
                                if (item.id == exitRequestId) updated else item
                            }
                        }
                        fetchExitRequestById(exitRequestId)
                        fetchExitRequests()
                        onSuccess(res.message ?: "Department clearance finalized successfully!")
                    } else {
                        onError(res.message ?: "Failed to complete clearance")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to complete clearance")
                }
            )
        }
    }

    /**
     * Fetch hardware assets assigned to the departing employee for IT Clearance
     */
    fun fetchAssignedAssetsForExit(memberId: String) {
        if (memberId.isBlank()) {
            _exitAssignedAssets.value = emptyList()
            _exitAssetsError.value = null
            return
        }
        viewModelScope.launch {
            _isLoadingExitAssets.value = true
            _exitAssetsError.value = null

            val result = repo.request<ExitAssignedAssetListResponse> {
                get("/api/it/asset/my-assigned/$memberId")
            }

            result.fold(
                onSuccess = { res ->
                    _exitAssignedAssets.value = res.data
                },
                onFailure = {
                    // Fallback to alternative asset route if needed
                    val fallback = repo.request<ExitAssignedAssetListResponse> {
                        get("/api/asset/my-assigned/$memberId")
                    }
                    fallback.fold(
                        onSuccess = { res -> _exitAssignedAssets.value = res.data },
                        onFailure = {
                            _exitAssignedAssets.value = emptyList()
                            _exitAssetsError.value = null
                        }
                    )
                }
            )
            _isLoadingExitAssets.value = false
        }
    }

    fun clearExitAssignedAssets() {
        _exitAssignedAssets.value = emptyList()
        _exitAssetsError.value = null
    }

    /**
     * Load exit request detail, existing settlement, and auto-calculate if no settlement exists yet
     */
    fun fetchExitSettlementData(exitRequestId: String) {
        if (exitRequestId.isBlank()) return
        fetchExitRequestById(exitRequestId)

        viewModelScope.launch {
            _isLoadingExitSettlement.value = true
            _exitSettlementError.value = null
            _exitSettlement.value = null
            _exitSettlementCalculation.value = null

            val existingRes = repo.request<ExitSettlementResponse> {
                get("$EXIT_SETTLEMENT_BASE/exit-request/$exitRequestId")
            }

            var hasExisting = false
            existingRes.fold(
                onSuccess = { res ->
                    if (res.data != null) {
                        _exitSettlement.value = res.data
                        hasExisting = true
                    }
                },
                onFailure = { /* Proceed to calculation if fetch fails or returns empty */ }
            )

            // Calculate draft settlement automatically when no saved settlement exists
            if (!hasExisting) {
                var calcRes = repo.request<ExitSettlementCalculationResponse> {
                    get("$EXIT_SETTLEMENT_BASE/calculate/$exitRequestId")
                }
                if (calcRes.isFailure) {
                    calcRes = repo.request<ExitSettlementCalculationResponse> {
                        post("$EXIT_SETTLEMENT_BASE/calculate/$exitRequestId", emptyMap<String, Any>())
                    }
                }
                calcRes.fold(
                    onSuccess = { res -> _exitSettlementCalculation.value = res.data },
                    onFailure = { err -> _exitSettlementError.value = err.message ?: "Failed to calculate settlement" }
                )
            }

            _isLoadingExitSettlement.value = false
        }
    }

    /**
     * Create / save final settlement: POST /api/hr/exit-settlement/create
     */
    fun createExitSettlement(
        request: CreateExitSettlementRequest,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (_isSavingExitSettlement.value) return
        viewModelScope.launch {
            _isSavingExitSettlement.value = true
            val result = repo.request<ExitSettlementResponse> {
                post("$EXIT_SETTLEMENT_BASE/create", request)
            }
            _isSavingExitSettlement.value = false

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _exitSettlement.value = res.data
                        fetchExitRequestById(request.exitRequestId)
                        fetchExitRequests()
                        onSuccess(res.message ?: "Exit settlement created successfully")
                    } else {
                        onError(res.message ?: "Failed to create exit settlement")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to create exit settlement")
                }
            )
        }
    }

    fun clearExitSettlementData() {
        _exitSettlement.value = null
        _exitSettlementCalculation.value = null
        _exitSettlementError.value = null
    }

    /**
     * Toggle hardware asset status between Returned ("Available") and Assigned
     */
    fun toggleExitAssetStatus(
        asset: ExitAssignedAssetDto,
        exitRequest: ExitRequestDto,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val assetId = asset.resolveAssetId()
        if (assetId.isBlank() || _updatingClearanceItemId.value != null) {
            if (assetId.isBlank()) onError("Asset ID is missing")
            return
        }

        val returnedStatuses = listOf("Available", "Returned", "Recovered")
        val isCurrentlyReturned = returnedStatuses.any { it.equals(asset.status, ignoreCase = true) }
        val previousStatus = asset.status
        val nextStatus = if (isCurrentlyReturned) "Assigned" else "Available"
        val uid = "asset-$assetId"

        viewModelScope.launch {
            _updatingClearanceItemId.value = uid

            // Optimistically update the asset status locally
            _exitAssignedAssets.value = _exitAssignedAssets.value.map {
                if (it.resolveAssetId() == assetId) it.copy(status = nextStatus) else it
            }

            val payload = if (nextStatus == "Available") {
                ExitAssetAssignmentRequest(assigneeName = "Unassigned")
            } else {
                val orig = asset.currentAssignee
                ExitAssetAssignmentRequest(
                    assigneeName = orig?.name?.takeIf { it.isNotBlank() }
                        ?: exitRequest.organizationMemberId.extractMemberName(),
                    empId = orig?.empId,
                    department = orig?.department,
                    userId = orig?.userId
                )
            }

            val result = repo.request<SimpleActionResponse> {
                put("/api/it/asset/$assetId/assignment", payload)
            }

            _updatingClearanceItemId.value = null
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        onSuccess("Asset marked as ${if (nextStatus == "Available") "Returned" else "Pending"}")
                    } else {
                        _exitAssignedAssets.value = _exitAssignedAssets.value.map {
                            if (it.resolveAssetId() == assetId) it.copy(status = previousStatus) else it
                        }
                        onError(res.message ?: "Failed to update asset status")
                    }
                },
                onFailure = { error ->
                    _exitAssignedAssets.value = _exitAssignedAssets.value.map {
                        if (it.resolveAssetId() == assetId) it.copy(status = previousStatus) else it
                    }
                    onError(error.message ?: "Failed to update asset status")
                }
            )
        }
    }

    /**
     * Create exit request (multipart/form-data): POST /api/hr/exit-request/create
     */
    fun createExitRequest(
        organizationMemberId: String,
        branchId: String,
        departmentId: String,
        designationId: String,
        exitType: String,
        reason: String,
        requestedLastWorkingDate: String,
        noticePeriodDays: Int,
        status: String = "Submitted",
        files: List<File>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (organizationMemberId.isBlank()) {
            onError("Please select an employee")
            return
        }
        if (exitType.isBlank()) {
            onError("Please select an exit type")
            return
        }
        if (reason.isBlank()) {
            onError("Please enter a reason for exit")
            return
        }

        viewModelScope.launch {
            _isSubmittingExitRequest.value = true

            val parts = mutableMapOf(
                "organizationMemberId" to repo.text(organizationMemberId.trim()),
                "exitType" to repo.text(exitType.trim()),
                "reason" to repo.text(reason.trim()),
                "noticePeriodDays" to repo.text(noticePeriodDays.toString()),
                "status" to repo.text(status)
            )

            if (departmentId.isNotBlank()) {
                parts["departmentId"] = repo.text(departmentId.trim())
            }
            if (branchId.isNotBlank()) {
                parts["branchId"] = repo.text(branchId.trim())
            }
            if (designationId.isNotBlank()) {
                parts["designationId"] = repo.text(designationId.trim())
            }
            if (requestedLastWorkingDate.isNotBlank()) {
                parts["requestedLastWorkingDate"] = repo.text(convertUiDateToApiDate(requestedLastWorkingDate))
            }

            val fileParts = files.map { file ->
                val extension = file.extension.lowercase()
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: when (extension) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "pdf" -> "application/pdf"
                        else -> "application/octet-stream"
                    }
                repo.filePart("attachments", file, mimeType)
            }

            val result = repo.request<ExitRequestSingleResponse> {
                postMultipart(
                    url = "$EXIT_REQUEST_BASE/create",
                    parts = parts,
                    files = fileParts
                )
            }

            _isSubmittingExitRequest.value = false
            result.fold(
                onSuccess = { res ->
                    fetchExitRequests()
                    onSuccess(res.message ?: "Exit request submitted successfully")
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to submit exit request")
                }
            )
        }
    }

    /**
     * Delete exit request: DELETE /api/hr/exit-request/delete/{id}
     */
    fun deleteExitRequest(
        id: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repo.request<ExitRequestSingleResponse> {
                delete("$EXIT_REQUEST_BASE/delete/$id")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _exitRequests.value = _exitRequests.value.filter { it.id != id }
                        val current = _exitRequestsPagingState.value
                        _exitRequestsPagingState.value = current.copy(
                            items = current.items.filter { it.id != id }
                        )
                        onSuccess(res.message ?: "Exit request deleted successfully")
                    } else {
                        onError(res.message ?: "Failed to delete exit request")
                    }
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to delete exit request")
                }
            )
        }
    }

    /**
     * Submit Manager Approval / Reconsideration decision
     */
    fun submitExitApprovalDecision(
        exitRequestId: String,
        approvalId: String?,
        approverId: String?,
        decisionStatus: String,
        comments: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (decisionStatus == "Rejected" && comments.isBlank()) {
            onError("Please enter comments explaining the reconsideration request")
            return
        }

        viewModelScope.launch {
            _isSubmittingExitDecision.value = true
            val trimmedComments = comments.trim().ifBlank { null }

            var succeeded = false
            var updatedDto: ExitRequestDto? = null
            var successMessage = "Exit request ${if (decisionStatus == "Approved") "accepted" else "sent for reconsideration"}"
            var lastError: String? = null

            // 1. Update existing approval record: PUT /api/hr/exit-request/{exitId}/approvals/{approvalId}
            if (!approvalId.isNullOrBlank()) {
                val res = repo.request<ExitRequestSingleResponse> {
                    put(
                        "$EXIT_REQUEST_BASE/$exitRequestId/approvals/$approvalId",
                        ExitApprovalDecisionRequest(
                            stage = "Manager_Review",
                            approverId = approverId?.takeIf { it.isNotBlank() },
                            status = decisionStatus,
                            comments = trimmedComments
                        )
                    )
                }
                res.fold(
                    onSuccess = {
                        if (it.success) {
                            succeeded = true
                            updatedDto = it.data
                            if (!it.message.isNullOrBlank()) successMessage = it.message
                        }
                    },
                    onFailure = { lastError = it.message }
                )
            }

            // 2. Add new approval record if no approval ID exists: POST /api/hr/exit-request/{exitId}/approvals
            if (!succeeded) {
                val res = repo.request<ExitRequestSingleResponse> {
                    post(
                        "$EXIT_REQUEST_BASE/$exitRequestId/approvals",
                        ExitApprovalDecisionRequest(
                            stage = "Manager_Review",
                            approverId = approverId?.takeIf { it.isNotBlank() },
                            status = decisionStatus,
                            comments = trimmedComments
                        )
                    )
                }
                res.fold(
                    onSuccess = {
                        if (it.success) {
                            succeeded = true
                            updatedDto = it.data
                            if (!it.message.isNullOrBlank()) successMessage = it.message
                        }
                    },
                    onFailure = { lastError = it.message }
                )
            }

            // 3. Fallback to direct status update
            if (!succeeded) {
                val res = repo.request<ExitRequestSingleResponse> {
                    patch(
                        "$EXIT_REQUEST_BASE/status/$exitRequestId",
                        ExitStatusUpdateRequest(
                            status = decisionStatus,
                            comments = trimmedComments
                        )
                    )
                }
                res.fold(
                    onSuccess = {
                        if (it.success) {
                            succeeded = true
                            updatedDto = it.data
                            if (!it.message.isNullOrBlank()) successMessage = it.message
                        }
                    },
                    onFailure = { lastError = it.message }
                )
            }

            _isSubmittingExitDecision.value = false
            if (succeeded) {
                // Immediately sync detail and list states from the API response
                updatedDto?.let { dto ->
                    _exitRequestDetail.value = dto
                    _exitRequests.value = _exitRequests.value.map { item ->
                        if (item.id == exitRequestId) dto else item
                    }
                }
                fetchExitRequestById(exitRequestId)
                fetchExitRequests()
                onSuccess(successMessage)
            } else {
                onError(lastError ?: "Failed to submit approval decision")
            }
        }
    }
    sealed class UploadPictureState {
        object Idle : UploadPictureState()
        object Loading : UploadPictureState()
        data class Success(val pictureUrl: String) : UploadPictureState()
        data class Error(val message: String) : UploadPictureState()
    }

    sealed class DeletePictureState {
        object Idle : DeletePictureState()
        object Loading : DeletePictureState()
        object Success : DeletePictureState()
        data class Error(val message: String) : DeletePictureState()
    }

    // =========================================================================
    // PRODUCTION TEAMS (VIEW-ALL, VIEW-ONE, CREATE, UPDATE, STATUS, DELETE)
    // =========================================================================

    private val _allProductionTeams = MutableStateFlow<List<ProductionTeamDto>>(emptyList())
    val allProductionTeams: StateFlow<List<ProductionTeamDto>> = _allProductionTeams.asStateFlow()

    private val _productionTeamsPagingState =
        MutableStateFlow(PagingUiState<ProductionTeamDto>())
    val productionTeamsPagingState: StateFlow<PagingUiState<ProductionTeamDto>> =
        _productionTeamsPagingState.asStateFlow()

    private val _productionTeamKpi = MutableStateFlow(ProductionTeamKpiSummary())
    val productionTeamKpi: StateFlow<ProductionTeamKpiSummary> = _productionTeamKpi.asStateFlow()

    private val _productionTeamsError = MutableStateFlow<String?>(null)
    val productionTeamsError: StateFlow<String?> = _productionTeamsError.asStateFlow()

    private val _selectedProductionTeam = MutableStateFlow<ProductionTeamDto?>(null)
    val selectedProductionTeam: StateFlow<ProductionTeamDto?> = _selectedProductionTeam.asStateFlow()

    private val _isLoadingProductionTeamDetail = MutableStateFlow(false)
    val isLoadingProductionTeamDetail: StateFlow<Boolean> = _isLoadingProductionTeamDetail.asStateFlow()

    private val _isSubmittingProductionTeam = MutableStateFlow(false)
    val isSubmittingProductionTeam: StateFlow<Boolean> = _isSubmittingProductionTeam.asStateFlow()

    private val _productionTeamAssignedWork = MutableStateFlow<ProductionTeamAssignedWorkData?>(null)
    val productionTeamAssignedWork: StateFlow<ProductionTeamAssignedWorkData?> =
        _productionTeamAssignedWork.asStateFlow()

    private var teamSearchQuery: String = ""
    private var teamFilterSpecs: Set<String> = emptySet()
    private var teamFilterLocations: Set<String> = emptySet()
    private var teamFilterStatuses: Set<String> = emptySet()
    private var teamFilterAvailabilities: Set<String> = emptySet()
    private var teamCurrentPage: Int = 1
    private val teamPageSize: Int = 10
    private var teamSearchDebounceJob: Job? = null

    fun fetchProductionTeams() {
        viewModelScope.launch {
            _productionTeamsError.value = null
            _productionTeamsPagingState.value = PagingUiState(isInitialLoading = true)

            val result = repo.request<ProductionTeamListResponse> {
                get("$PRODUCTION_TEAM_BASE/view-all", repo.query("limit" to 500))
            }

            result.fold(
                onSuccess = { res ->
                    val visibleTeams = res.data.filter { !it.isTemplate && !it.isDeleted }
                    _allProductionTeams.value = visibleTeams
                    recomputeProductionTeamKpis(visibleTeams)
                    rebuildProductionTeamsPagingState(resetPage = true)
                },
                onFailure = { err ->
                    _productionTeamsError.value = err.message ?: "Failed to load production teams"
                    _productionTeamsPagingState.value = PagingUiState(isInitialLoading = false)
                }
            )
        }
    }

    fun onProductionTeamSearchChanged(query: String) {
        teamSearchQuery = query
        teamSearchDebounceJob?.cancel()
        teamSearchDebounceJob = viewModelScope.launch {
            delay(250)
            rebuildProductionTeamsPagingState(resetPage = true)
        }
    }

    fun applyProductionTeamFilters(
        specializations: Set<String>,
        locations: Set<String>,
        statuses: Set<String>,
        availabilities: Set<String>
    ) {
        teamFilterSpecs = specializations
        teamFilterLocations = locations
        teamFilterStatuses = statuses
        teamFilterAvailabilities = availabilities
        rebuildProductionTeamsPagingState(resetPage = true)
    }

    fun clearProductionTeamFilters() {
        teamFilterSpecs = emptySet()
        teamFilterLocations = emptySet()
        teamFilterStatuses = emptySet()
        teamFilterAvailabilities = emptySet()
        rebuildProductionTeamsPagingState(resetPage = true)
    }

    fun loadMoreProductionTeams() {
        val current = _productionTeamsPagingState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.canLoadMore) return
        teamCurrentPage += 1
        rebuildProductionTeamsPagingState(resetPage = false)
    }

    private fun recomputeProductionTeamKpis(teams: List<ProductionTeamDto>) {
        val total = teams.size
        val active = teams.count { it.displayStatus().equals("Active", ignoreCase = true) }
        val membersCount = teams.sumOf { it.activeMembers().size }
        val available = teams.count { it.availabilityStatus().equals("Available", ignoreCase = true) }
        val facilities = teams.map { it.location.trim() }.filter { it.isNotBlank() }.toSet().size
        val activePct = if (total > 0) String.format(Locale.US, "%.1f", (active * 100.0) / total) else "0.0"

        _productionTeamKpi.value = ProductionTeamKpiSummary(
            totalTeams = total,
            activeTeams = active,
            activePercentage = activePct,
            totalMembers = membersCount,
            availableTeams = available,
            facilitiesCount = facilities
        )
    }

    private fun getFilteredProductionTeams(): List<ProductionTeamDto> {
        val q = teamSearchQuery.trim().lowercase()
        return _allProductionTeams.value.filter { team ->
            val matchesQuery = q.isBlank() ||
                    team.name.lowercase().contains(q) ||
                    team.teamCode.lowercase().contains(q) ||
                    (team.teamLead?.name ?: "").lowercase().contains(q)

            val matchesSpec = teamFilterSpecs.isEmpty() ||
                    team.specializations.any { it.code in teamFilterSpecs }

            val matchesLoc = teamFilterLocations.isEmpty() ||
                    team.location.trim() in teamFilterLocations

            val matchesStatus = teamFilterStatuses.isEmpty() ||
                    team.displayStatus() in teamFilterStatuses

            val matchesAvail = teamFilterAvailabilities.isEmpty() ||
                    team.availabilityStatus() in teamFilterAvailabilities

            matchesQuery && matchesSpec && matchesLoc && matchesStatus && matchesAvail
        }
    }

    private fun rebuildProductionTeamsPagingState(resetPage: Boolean) {
        if (resetPage) teamCurrentPage = 1
        val filtered = getFilteredProductionTeams()
        val paged = filtered.take(teamCurrentPage * teamPageSize)
        _productionTeamsPagingState.value = PagingUiState(
            items = paged,
            isInitialLoading = false,
            isLoadingMore = false,
            canLoadMore = paged.size < filtered.size
        )
    }

    fun fetchProductionTeamById(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isLoadingProductionTeamDetail.value = true
            val result = repo.request<ProductionTeamSingleResponse> {
                get("$PRODUCTION_TEAM_BASE/view-one/$id")
            }
            result.fold(
                onSuccess = { res -> _selectedProductionTeam.value = res.data },
                onFailure = {
                    _selectedProductionTeam.value = _allProductionTeams.value.find { it.id == id }
                }
            )
            _isLoadingProductionTeamDetail.value = false
        }
    }

    fun fetchProductionTeamForDuplicate(
        id: String,
        onLoaded: (ProductionTeamDto) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repo.request<ProductionTeamSingleResponse> {
                get("$PRODUCTION_TEAM_BASE/view-one/$id")
            }
            result.fold(
                onSuccess = { res ->
                    res.data?.let(onLoaded) ?: onError("Unable to load selected team")
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed to load selected team")
                }
            )
        }
    }

    fun fetchProductionTeamAssignedWork(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            val result = repo.request<ProductionTeamAssignedWorkResponse> {
                get("$PRODUCTION_TEAM_BASE/$id/assigned-work")
            }
            result.fold(
                onSuccess = { res -> _productionTeamAssignedWork.value = res.data },
                onFailure = { _productionTeamAssignedWork.value = null }
            )
        }
    }

    fun clearSelectedProductionTeam() {
        _selectedProductionTeam.value = null
        _productionTeamAssignedWork.value = null
    }

    fun createProductionTeam(
        request: CreateUpdateProductionTeamRequest,
        onSuccess: (ProductionTeamDto) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isSubmittingProductionTeam.value) return
        launchBusy {
            _isSubmittingProductionTeam.value = true
            val result = repo.request<ProductionTeamSingleResponse> {
                post("$PRODUCTION_TEAM_BASE/create", request)
            }
            _isSubmittingProductionTeam.value = false
            result.fold(
                onSuccess = { res ->
                    val created = res.data
                    if (res.success && created != null) {
                        fetchProductionTeams()
                        onSuccess(created)
                    } else {
                        onError(res.message ?: "Failed to create production team")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed to create production team")
                }
            )
        }
    }

    fun updateProductionTeam(
        id: String,
        request: CreateUpdateProductionTeamRequest,
        onSuccess: (ProductionTeamDto) -> Unit,
        onError: (String) -> Unit
    ) {
        if (id.isBlank() || _isSubmittingProductionTeam.value) return
        launchBusy {
            _isSubmittingProductionTeam.value = true
            val result = repo.request<ProductionTeamSingleResponse> {
                put("$PRODUCTION_TEAM_BASE/update/$id", request)
            }
            _isSubmittingProductionTeam.value = false
            result.fold(
                onSuccess = { res ->
                    val updated = res.data
                    if (res.success && updated != null) {
                        _selectedProductionTeam.value = updated
                        fetchProductionTeams()
                        onSuccess(updated)
                    } else {
                        onError(res.message ?: "Failed to update production team")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed to update production team")
                }
            )
        }
    }

    fun updateProductionTeamStatus(
        id: String,
        newStatus: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (id.isBlank()) return
        launchBusy {
            val payload = UpdateProductionTeamStatusRequest(operationalStatus = newStatus)
            var result = repo.request<ProductionTeamSingleResponse> {
                put("$PRODUCTION_TEAM_BASE/update/$id", payload)
            }
            if (result.isFailure) {
                result = repo.request<ProductionTeamSingleResponse> {
                    patch("$PRODUCTION_TEAM_BASE/update/$id", payload)
                }
            }
            result.fold(
                onSuccess = { res ->
                    val updated = res.data
                    if (res.success && updated != null) {
                        _selectedProductionTeam.value = updated
                        _allProductionTeams.value = _allProductionTeams.value.map {
                            if (it.id == id) updated else it
                        }
                        recomputeProductionTeamKpis(_allProductionTeams.value)
                        rebuildProductionTeamsPagingState(resetPage = false)
                        onSuccess(res.message ?: "Production team status updated")
                    } else {
                        onError(res.message ?: "Failed to update status")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed to update status")
                }
            )
        }
    }

    fun deleteProductionTeam(
        id: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (id.isBlank()) return
        launchBusy {
            val result = repo.request<ProductionTeamSingleResponse> {
                delete("$PRODUCTION_TEAM_BASE/delete/$id")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _allProductionTeams.value = _allProductionTeams.value.filterNot { it.id == id }
                        recomputeProductionTeamKpis(_allProductionTeams.value)
                        rebuildProductionTeamsPagingState(resetPage = false)
                        onSuccess(res.message ?: "Production team deleted successfully")
                    } else {
                        onError(res.message ?: "Failed to delete production team")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed to delete production team")
                }
            )
        }
    }

    sealed class CreateMemberState {
        object Idle : CreateMemberState()
        object Loading : CreateMemberState()
        data class Success(val member: CreatedMemberFullData) : CreateMemberState()
        data class Error(val message: String) : CreateMemberState()
    }
}