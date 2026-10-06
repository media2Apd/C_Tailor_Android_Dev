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
import com.cuso.tailor.model.hr.AssignedTrainingListResponse
import com.cuso.tailor.model.hr.AssignedTrainingProgramDto
import com.cuso.tailor.model.hr.AssignedTrainingSingleResponse
import com.cuso.tailor.model.hr.AttendanceRecord
import com.cuso.tailor.model.hr.CreateEmployeeDocumentResponse
import com.cuso.tailor.model.hr.CreateManualAttendanceRequest
import com.cuso.tailor.model.hr.CreateMemberRequest
import com.cuso.tailor.model.hr.CreateShiftRequest
import com.cuso.tailor.model.hr.CreatedMemberFullData
import com.cuso.tailor.model.hr.DeleteDocumentCategoryResponse
import com.cuso.tailor.model.hr.DeleteEmployeeDocumentResponse
import com.cuso.tailor.model.hr.DocumentCategoryDto
import com.cuso.tailor.model.hr.DocumentCategoryDtoCat
import com.cuso.tailor.model.hr.DocumentCategoryListResponse
import com.cuso.tailor.model.hr.DocumentCategoryListResponseCat
import com.cuso.tailor.model.hr.DocumentCategorySingleResponse
import com.cuso.tailor.model.hr.EmployeeDocumentDto
import com.cuso.tailor.model.hr.EmployeeDocumentListResponse
import com.cuso.tailor.model.hr.EmployeeDocumentSingleResponse
import com.cuso.tailor.model.hr.LeaveRequestItemDto
import com.cuso.tailor.model.hr.LeaveTypeItemDto
import com.cuso.tailor.model.hr.MemberDetail
import com.cuso.tailor.model.hr.MemberItem
import com.cuso.tailor.model.hr.MonthlyAttendanceItem
import com.cuso.tailor.model.hr.RoleItem
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
import com.cuso.tailor.repository.ApiRepository
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

// Base path for all salary component endpoints
private const val SALARY_BASE = "/api/hr/salary-component"

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

            val result = hrRepository.getMembers(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    val newMembers = response.data
                    _members.value = newMembers
                    _membersTotal.value = response.total

                    // Check if more items exist based on total count
                    _canLoadMoreMembers.value = newMembers.size < response.total && newMembers.isNotEmpty()
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _membersError.value = e.message ?: "Failed to fetch employees"
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
                        val updatedList = _members.value + newMembers
                        _members.value = updatedList
                        _currentMemberPage.value = nextPage
                        _membersTotal.value = response.total

                        _canLoadMoreMembers.value = updatedList.size < response.total
                    } else {
                        _canLoadMoreMembers.value = false
                    }
                },
                onFailure = {
                    // Do not permanently lock pagination so user can retry on scroll
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
    private val _memberDetail = MutableStateFlow<MemberDetail?>(null)
    val memberDetail: StateFlow<MemberDetail?> = _memberDetail.asStateFlow()

    private val _isLoadingMemberDetail = MutableStateFlow(false)
    val isLoadingMemberDetail: StateFlow<Boolean> = _isLoadingMemberDetail.asStateFlow()

    private val _memberDetailError = MutableStateFlow<String?>(null)
    val memberDetailError: StateFlow<String?> = _memberDetailError.asStateFlow()

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

    fun fetchMemberDetail(memberId: String) {
        launchBusy {
            _isLoadingMemberDetail.value = true
            _memberDetailError.value = null
            val result = hrRepository.getMemberDetail(memberId)
            result.fold(
                onSuccess = { _memberDetail.value = it },
                onFailure = { e -> _memberDetailError.value = e.message ?: "Failed to fetch employee detail" }
            )
            _isLoadingMemberDetail.value = false
        }
    }

    fun clearMemberDetail() {
        _memberDetail.value = null
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

    fun fetchShiftsViewAll() {
        launchBusy {
            _isLoadingShifts.value = true
            _shiftsError.value = null
            val result = hrRepository.getShiftsViewAll()
            result.fold(
                onSuccess = { _shifts.value = it },
                onFailure = { e -> _shiftsError.value = e.message ?: "Failed to fetch shifts" }
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

    fun fetchLeaveRequests(status: String? = null) {
        launchBusy {
            _isLoadingLeaveRequests.value = true
            _leaveRequestsError.value = null
            val result = hrRepository.getLeaveRequests(page = 1, limit = 50, status = status)
            result.fold(
                onSuccess = { _leaveRequests.value = it },
                onFailure = { e -> _leaveRequestsError.value = e.message ?: "Failed to load leave requests" }
            )
            _isLoadingLeaveRequests.value = false
        }
    }

    // Unified leave status updater
    fun updateLeaveStatus(
        id: String,
        status: String, // "approved" or "rejected"
        note: String? = null,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = hrRepository.updateLeaveStatus(id, status, note)
            result.fold(
                onSuccess = { msg ->
                    // Refresh list on status update
                    fetchLeaveRequests()
                    onSuccess(msg)
                },
                onFailure = { e ->
                    onError(e.message ?: "Failed to update leave status")
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

    fun fetchTrainingPrograms(search: String? = null) {
        launchState(
            state = _trainingListState,
            onSuccess = { res ->
                _trainingPrograms.value = res.data
            }
        ) {
            repo.request<TrainingProgramListResponse> {
                get(
                    "$TRAINING_PROGRAM_BASE/view-all",
                    repo.query("search" to search)
                )
            }
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
                _trainingPrograms.value = _trainingPrograms.value.filter { it.id != id }
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

    fun fetchAssignedTrainingPrograms(search: String? = null) {
        launchState(
            state = _assignedTrainingListState,
            onSuccess = { res ->
                _assignedTrainingPrograms.value = res.data
            }
        ) {
            repo.request<AssignedTrainingListResponse> {
                get(
                    "$ASSIGNED_TRAINING_BASE/view-all",
                    repo.query("search" to search)
                )
            }
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

    // Employee Documents State
    private val _employeeDocuments = MutableStateFlow<List<EmployeeDocumentDto>>(emptyList())
    val employeeDocuments: StateFlow<List<EmployeeDocumentDto>> = _employeeDocuments.asStateFlow()

    private val _isLoadingDocuments = MutableStateFlow(false)
    val isLoadingDocuments: StateFlow<Boolean> = _isLoadingDocuments.asStateFlow()

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

    // 2. Fetch Employee Documents with search and pagination
    fun fetchEmployeeDocuments(search: String? = null) {
        launchBusy {
            _isLoadingDocuments.value = true
            val result = repo.request<EmployeeDocumentListResponse> {
                get(
                    "$EMP_DOCUMENT_BASE/view-all",
                    repo.query(
                        "search" to search,
                        "page" to 1,
                        "limit" to 50
                    )
                )
            }
            result.fold(
                onSuccess = { res ->
                    _employeeDocuments.value = res.data?.documents.orEmpty()
                },
                onFailure = {
                    _employeeDocuments.value = emptyList()
                }
            )
            _isLoadingDocuments.value = false
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
        viewModelScope.launch {
            _isLoadingDocumentDetail.value = true
            val result = repo.request<EmployeeDocumentSingleResponse> {
                get("$EMP_DOCUMENT_BASE/view-one/$id")
            }
            result.fold(
                onSuccess = { res ->
                    _documentDetail.value = res.data
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
                        // Remove deleted item from local list without reloading entire screen
                        _employeeDocuments.value = _employeeDocuments.value.filter { it.id != id }
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

    // 1. View All Document Categories
    fun fetchDocumentCategoriesCat() {
        launchBusy {
            _isLoadingCategoriesCat.value = true
            val result = repo.request<DocumentCategoryListResponseCat> {
                get("$DOC_CATEGORY_BASE/view-all")
            }
            result.fold(
                onSuccess = { res ->
                    _documentCategoriesCat.value = res.data
                },
                onFailure = {
                    _documentCategoriesCat.value = emptyList()
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

    sealed class CreateMemberState {
        object Idle : CreateMemberState()
        object Loading : CreateMemberState()
        data class Success(val member: CreatedMemberFullData) : CreateMemberState()
        data class Error(val message: String) : CreateMemberState()
    }
}