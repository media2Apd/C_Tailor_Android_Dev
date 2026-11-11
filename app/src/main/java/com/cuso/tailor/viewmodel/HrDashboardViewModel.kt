@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.viewmodel

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.hr.*
import com.cuso.tailor.repository.ApiRepository
import com.cuso.tailor.utils.DynamicIslandManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val CHECK_IN_OUT_TODAY_URL = "/api/hr/attendance/check-in-out/today"
private const val ATTENDANCE_BY_EMPLOYEE_URL = "/api/hr/attendance/by-employee"
private const val MONTHLY_ATTENDANCE_VIEW_URL = "/api/hr/attendance/monthly-view"
private const val MY_SPACE_DASHBOARD_URL = "/api/hr/myspace-dashboard"
private const val TEAM_DASHBOARD_URL = "/api/hr/team-dashboard"
private const val ORG_DASHBOARD_URL = "/api/hr/org-dashboard"
private const val MEMBERS_ME_URL = "/api/members/me"
private const val CHECK_IN_URL = "/api/hr/attendance/check-in"
private const val CHECK_OUT_URL = "/api/hr/attendance/check-out"

// Endpoints for Feed, Legal Files, and Designation Career History
private const val ANNOUNCEMENT_FEED_URL = "/api/hr/announcement/my-feed"
private const val ANNOUNCEMENT_CREATE_URL = "/api/hr/announcement/create"

private const val LEGAL_EMPLOYEE_BY_EMPLOYEE_URL = "/api/legal/legal-employee/by-employee"
private const val DESIGNATION_VIEW_ONE_URL = "/api/home/designations/view-one"

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HrDashboardViewModel @Inject constructor(
    private val repo: ApiRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _todayAttendance = MutableStateFlow<CheckInOutTodayData?>(null)
    val todayAttendance: StateFlow<CheckInOutTodayData?> = _todayAttendance.asStateFlow()

    private val _myProfile = MutableStateFlow<MyProfileData?>(null)
    val myProfile: StateFlow<MyProfileData?> = _myProfile.asStateFlow()

    private val _mySpaceData = MutableStateFlow<MySpaceDashboardData?>(null)
    val mySpaceData: StateFlow<MySpaceDashboardData?> = _mySpaceData.asStateFlow()

    private val _teamData = MutableStateFlow<TeamDashboardData?>(null)
    val teamData: StateFlow<TeamDashboardData?> = _teamData.asStateFlow()

    private val _orgData = MutableStateFlow<OrgDashboardData?>(null)
    val orgData: StateFlow<OrgDashboardData?> = _orgData.asStateFlow()

    private val _attendanceHistoryMonthToDate = MutableStateFlow<List<CheckInOutTodayData>>(emptyList())
    val attendanceHistoryMonthToDate: StateFlow<List<CheckInOutTodayData>> = _attendanceHistoryMonthToDate.asStateFlow()

    // Dynamic StateFlows for Feed, Files, and Career History
    private val _feedList = MutableStateFlow<List<AnnouncementItemData>>(emptyList())
    val feedList: StateFlow<List<AnnouncementItemData>> = _feedList.asStateFlow()

    private val _legalFiles = MutableStateFlow<List<LegalEmployeeData>>(emptyList())
    val legalFiles: StateFlow<List<LegalEmployeeData>> = _legalFiles.asStateFlow()

    private val _designationHistory = MutableStateFlow<DesignationDetailData?>(null)
    val designationHistory: StateFlow<DesignationDetailData?> = _designationHistory.asStateFlow()

    init {
        loadInitialData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadInitialData() {
        fetchMyProfile()
        fetchCheckInOutToday()
        fetchMySpaceDashboard()
        fetchTeamDashboard()
        fetchOrgDashboard()
        fetchEmployeeFeed() // Fetch real feed announcements
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchMyProfile() {
        viewModelScope.launch {
            val result = repo.request<MyProfileResponse> {
                get(MEMBERS_ME_URL)
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _myProfile.value = res.data
                        // Fetch Day 1 to Current Date Attendance
                        fetchAttendanceHistoryMonthToDate(res.data._id)
                        // Fetch Employee Legal Files using Member ID
                        fetchLegalFiles(res.data._id)
                        // Fetch Career History using Designation ID
                        res.data.designationId?._id?.let { fetchCareerDesignationHistory(it) }
                    }
                },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    // 1. Fetch Feed Announcements (/api/hr/announcement/my-feed)
    fun fetchEmployeeFeed() {
        viewModelScope.launch {
            val result = repo.request<AnnouncementFeedResponse> {
                get(ANNOUNCEMENT_FEED_URL)
            }
            result.fold(
                onSuccess = { if (it.success) _feedList.value = it.data },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    // 2. Fetch Employee Legal Files (/api/legal/legal-employee/by-employee/{id})
    fun fetchLegalFiles(employeeId: String) {
        if (employeeId.isBlank()) return
        viewModelScope.launch {
            val result = repo.request<LegalEmployeeResponse> {
                get("$LEGAL_EMPLOYEE_BY_EMPLOYEE_URL/$employeeId")
            }
            result.fold(
                onSuccess = { if (it.success) _legalFiles.value = it.data },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    // 3. Fetch Designation Career History (/api/home/designations/view-one/{id})
    fun fetchCareerDesignationHistory(designationId: String) {
        if (designationId.isBlank()) return
        viewModelScope.launch {
            val result = repo.request<DesignationDetailResponse> {
                get("$DESIGNATION_VIEW_ONE_URL/$designationId")
            }
            result.fold(
                onSuccess = { if (it.success) _designationHistory.value = it.data },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    fun fetchCheckInOutToday() {
        viewModelScope.launch {
            val result = repo.request<CheckInOutTodayResponse> {
                get(CHECK_IN_OUT_TODAY_URL)
            }
            result.fold(
                onSuccess = { if (it.success) _todayAttendance.value = it.data },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchAttendanceHistoryMonthToDate(memberId: String) {
        if (memberId.isBlank()) return
        viewModelScope.launch {
            val today = LocalDate.now()
            val records = mutableListOf<CheckInOutTodayData>()

            for (d in 1..today.dayOfMonth) {
                val dateStr = String.format("%04d-%02d-%02d", today.year, today.monthValue, d)
                val res = repo.request<CheckInOutTodayResponse> {
                    get(
                        ATTENDANCE_BY_EMPLOYEE_URL,
                        repo.query(
                            "organizationMemberId" to memberId,
                            "employeeId" to memberId,
                            "date" to dateStr
                        )
                    )
                }
                res.fold(
                    onSuccess = { if (it.success && it.data != null) records.add(it.data) },
                    onFailure = { /* Day without punch */ }
                )
            }
            _attendanceHistoryMonthToDate.value = records.reversed()
        }
    }

    fun fetchMySpaceDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repo.request<MySpaceDashboardResponse> {
                get(MY_SPACE_DASHBOARD_URL)
            }
            result.fold(
                onSuccess = { if (it.success) _mySpaceData.value = it.data },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun fetchTeamDashboard() {
        viewModelScope.launch {
            val result = repo.request<TeamDashboardResponse> {
                get(TEAM_DASHBOARD_URL)
            }
            result.fold(
                onSuccess = { if (it.success) _teamData.value = it.data },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    fun fetchOrgDashboard() {
        viewModelScope.launch {
            val result = repo.request<OrgDashboardResponse> {
                get(ORG_DASHBOARD_URL)
            }
            result.fold(
                onSuccess = { if (it.success) _orgData.value = it.data },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    fun performPunchAction() {
        viewModelScope.launch {
            val isCheckOut = _todayAttendance.value?.checkedIn == true
            val targetUrl = if (isCheckOut) CHECK_OUT_URL else CHECK_IN_URL

            val result = repo.request<CheckInOutTodayResponse> {
                post(targetUrl, emptyMap<String, Any>())
            }
            result.fold(
                onSuccess = {
                    val msg = if (isCheckOut) "Checked out successfully" else "Checked in successfully"
                    DynamicIslandManager.showSuccess(msg)
                    fetchCheckInOutToday()
                    _myProfile.value?._id?.let { fetchAttendanceHistoryMonthToDate(it) }
                },
                onFailure = {
                    DynamicIslandManager.showError(it.message ?: "Failed to record punch")
                }
            )
        }
    }

    // Create Announcement (Save as Draft or Publish)
    fun createAnnouncement(
        postCategory: String,
        title: String,
        description: String,
        status: String,
        branchIds: List<String>,
        departmentIds: List<String>,
        employmentTypes: List<String>,
        pollQuestion: String?,
        pollOptions: List<String>,
        allowMultipleAnswers: Boolean,
        closingDate: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val audience = AnnouncementAudiencePayload(
                type = "Filtered_Employees",
                organizationMemberIds = emptyList(),
                employmentTypes = employmentTypes,
                departmentIds = departmentIds,
                branchIds = branchIds
            )

            val poll = if (!pollQuestion.isNullOrBlank() && pollOptions.size >= 2) {
                AnnouncementPollPayload(
                    question = pollQuestion.trim(),
                    options = pollOptions.filter { it.isNotBlank() }.map { AnnouncementPollOptionPayload(it.trim()) },
                    allowMultipleSelections = allowMultipleAnswers,
                    closingDate = closingDate
                )
            } else null

            val payload = CreateAnnouncementRequest(
                postCategory = postCategory,
                title = title.trim(),
                description = description.trim(),
                status = status,
                audience = audience,
                poll = poll
            )

            val result = repo.request<AnnouncementCreateResponse> {
                post(ANNOUNCEMENT_CREATE_URL, payload)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        val message = if (status == "Draft") "Feed saved as draft" else "Feed published successfully"
                        DynamicIslandManager.showSuccess(message)
                        fetchEmployeeFeed() // Refresh feed list
                        onSuccess()
                    } else {
                        DynamicIslandManager.showError(res.message ?: "Failed to create announcement")
                    }
                },
                onFailure = {
                    DynamicIslandManager.showError(it.message ?: "Network error creating announcement")
                }
            )
            _isLoading.value = false
        }
    }
}