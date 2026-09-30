@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter"
)
package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.hr.AttendanceRecord
import com.cuso.tailor.model.hr.CreateManualAttendanceRequest
import com.cuso.tailor.model.hr.CreateMemberRequest
import com.cuso.tailor.model.hr.CreateShiftRequest
import com.cuso.tailor.model.hr.CreatedMemberFullData
import com.cuso.tailor.model.hr.MemberDetail
import com.cuso.tailor.model.hr.MemberItem
import com.cuso.tailor.model.hr.MonthlyAttendanceItem
import com.cuso.tailor.model.hr.RoleItem
import com.cuso.tailor.model.hr.ShiftDetailData
import com.cuso.tailor.model.hr.ShiftItem
import com.cuso.tailor.model.hr.UpdateMemberRequest
import com.cuso.tailor.model.hr.UpdateShiftRequest
import com.cuso.tailor.repository.HrRepository
import com.cuso.tailor.utils.convert12HrTo24Hr
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
import java.io.File
import javax.inject.Inject

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
    private val hrRepository: HrRepository
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

    init {
        loadAttendance(reset = true)
    }

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