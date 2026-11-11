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
                        fetchAttendanceHistoryMonthToDate(res.data._id)
                    }
                },
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
}