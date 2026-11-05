@file:Suppress("unused")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.security.*
import com.cuso.tailor.repository.ApiRepository
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

// --- Security Dashboard Endpoints ---
private const val SECURITY_SUMMARY_URL = "/api/security-dashboard/summary"
private const val SECURITY_LOGIN_TREND_URL = "/api/security-dashboard/login-trend"
private const val SECURITY_HEALTH_URL = "/api/security-dashboard/security-health"
private const val SECURITY_USERS_BY_ROLE_URL = "/api/security-dashboard/users-by-role"
private const val SECURITY_TOP_ATTACKS_URL = "/api/security-dashboard/top-attack-types"
private const val SECURITY_RECENT_ALERTS_URL = "/api/security-dashboard/recent-alerts"
private const val SECURITY_ACTIVE_SESSIONS_URL = "/api/security-dashboard/active-sessions"

private const val SECURITY_USERS_LIST_URL = "/api/users/view-all"
private const val SECURITY_CREATE_USER_URL = "/api/users/create"



@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val repo: ApiRepository
) : ViewModel() {

    // =========================================================================
    // 1. GLOBAL & SUMMARY STATES
    // =========================================================================

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow("")
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val _summary = MutableStateFlow<SecurityDashboardSummaryData?>(null)
    val summary: StateFlow<SecurityDashboardSummaryData?> = _summary.asStateFlow()

    // =========================================================================
    // 2. LOGIN ATTEMPTS TREND STATES
    // =========================================================================

    private val _loginTrend = MutableStateFlow<LoginTrendData?>(null)
    val loginTrend: StateFlow<LoginTrendData?> = _loginTrend.asStateFlow()

    private val _trendGranularity = MutableStateFlow("monthly")
    val trendGranularity: StateFlow<String> = _trendGranularity.asStateFlow()

    private val _isLoadingTrend = MutableStateFlow(false)
    val isLoadingTrend: StateFlow<Boolean> = _isLoadingTrend.asStateFlow()

    private val _trendError = MutableStateFlow<String?>(null)
    val trendError: StateFlow<String?> = _trendError.asStateFlow()

    // =========================================================================
    // 3. SECURITY HEALTH STATES
    // =========================================================================

    private val _securityHealth = MutableStateFlow<SecurityHealthData?>(null)
    val securityHealth: StateFlow<SecurityHealthData?> = _securityHealth.asStateFlow()

    private val _isLoadingHealth = MutableStateFlow(false)
    val isLoadingHealth: StateFlow<Boolean> = _isLoadingHealth.asStateFlow()

    private val _healthError = MutableStateFlow<String?>(null)
    val healthError: StateFlow<String?> = _healthError.asStateFlow()

    // =========================================================================
    // 4. USERS BY ROLE STATES
    // =========================================================================

    private val _usersByRole = MutableStateFlow<UsersByRoleData?>(null)
    val usersByRole: StateFlow<UsersByRoleData?> = _usersByRole.asStateFlow()

    private val _isLoadingUsersByRole = MutableStateFlow(false)
    val isLoadingUsersByRole: StateFlow<Boolean> = _isLoadingUsersByRole.asStateFlow()

    private val _usersByRoleError = MutableStateFlow<String?>(null)
    val usersByRoleError: StateFlow<String?> = _usersByRoleError.asStateFlow()

    // =========================================================================
    // 5. TOP ATTACK TYPES STATES
    // =========================================================================

    private val _attackTypes = MutableStateFlow<TopAttackTypesData?>(null)
    val attackTypes: StateFlow<TopAttackTypesData?> = _attackTypes.asStateFlow()

    private val _attackPeriod = MutableStateFlow("this_month")
    val attackPeriod: StateFlow<String> = _attackPeriod.asStateFlow()

    private val _isLoadingAttacks = MutableStateFlow(false)
    val isLoadingAttacks: StateFlow<Boolean> = _isLoadingAttacks.asStateFlow()

    private val _attacksError = MutableStateFlow<String?>(null)
    val attacksError: StateFlow<String?> = _attacksError.asStateFlow()

    // =========================================================================
    // 6. RECENT ALERTS STATES
    // =========================================================================

    private val _recentAlertsList = MutableStateFlow<List<SecurityAlertItemDto>>(emptyList())
    val recentAlertsList: StateFlow<List<SecurityAlertItemDto>> = _recentAlertsList.asStateFlow()
    val recentAlerts: StateFlow<List<SecurityAlertItemDto>> get() = recentAlertsList

    private val _alertsSearchQuery = MutableStateFlow("")
    val alertsSearchQuery: StateFlow<String> = _alertsSearchQuery.asStateFlow()

    private val _selectedAlertSeverity = MutableStateFlow<String?>(null)
    val selectedAlertSeverity: StateFlow<String?> = _selectedAlertSeverity.asStateFlow()

    private val _selectedAlertStatus = MutableStateFlow<String?>(null)
    val selectedAlertStatus: StateFlow<String?> = _selectedAlertStatus.asStateFlow()

    private val _isLoadingAlerts = MutableStateFlow(false)
    val isLoadingAlerts: StateFlow<Boolean> = _isLoadingAlerts.asStateFlow()

    private val _alertsError = MutableStateFlow<String?>(null)
    val alertsError: StateFlow<String?> = _alertsError.asStateFlow()

    /**
     * Filtered security alerts by search query, severity, and status.
     */
    val filteredAlerts: StateFlow<List<SecurityAlertItemDto>> = combine(
        _recentAlertsList,
        _alertsSearchQuery,
        _selectedAlertSeverity,
        _selectedAlertStatus
    ) { list, query, severity, status ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.alertId.lowercase().contains(cleanQuery) ||
                    item.type.lowercase().contains(cleanQuery) ||
                    item.location.lowercase().contains(cleanQuery) ||
                    item.ip.lowercase().contains(cleanQuery)

            val matchesSeverity = severity.isNullOrBlank() || item.severity.equals(severity, ignoreCase = true)
            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)

            matchesQuery && matchesSeverity && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // 7. ACTIVE SESSIONS STATES
    // =========================================================================

    private val _activeSessionsList = MutableStateFlow<List<ActiveSessionItemDto>>(emptyList())
    val activeSessionsList: StateFlow<List<ActiveSessionItemDto>> = _activeSessionsList.asStateFlow()
    val activeSessions: StateFlow<List<ActiveSessionItemDto>> get() = activeSessionsList

    private val _sessionsSearchQuery = MutableStateFlow("")
    val sessionsSearchQuery: StateFlow<String> = _sessionsSearchQuery.asStateFlow()

    private val _isLoadingSessions = MutableStateFlow(false)
    val isLoadingSessions: StateFlow<Boolean> = _isLoadingSessions.asStateFlow()

    private val _sessionsError = MutableStateFlow<String?>(null)
    val sessionsError: StateFlow<String?> = _sessionsError.asStateFlow()

    /**
     * Filtered active sessions by name, role, IP, and location.
     */
    val filteredSessions: StateFlow<List<ActiveSessionItemDto>> = combine(
        _activeSessionsList,
        _sessionsSearchQuery
    ) { list, query ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            cleanQuery.isEmpty() ||
                    item.name.lowercase().contains(cleanQuery) ||
                    item.role.lowercase().contains(cleanQuery) ||
                    item.ipAddress.lowercase().contains(cleanQuery) ||
                    item.location.lowercase().contains(cleanQuery) ||
                    item.device.lowercase().contains(cleanQuery)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // INITIALIZATION
    // =========================================================================

    init {
        setDefaultDateRange()
        fetchAllDashboardData()
    }

    private fun setDefaultDateRange() {
        val cal = Calendar.getInstance()
        val dtf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        _endDate.value = dtf.format(cal.time)
        cal.add(Calendar.DAY_OF_MONTH, -30)
        _startDate.value = dtf.format(cal.time)
    }

    /**
     * Fetches all security metrics concurrently for the selected range and parameters.
     */
    fun fetchAllDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                coroutineScope {
                    val summaryDeferred = async { fetchSummaryInternal() }
                    val trendDeferred = async { fetchLoginTrendInternal() }
                    val healthDeferred = async { fetchSecurityHealthInternal() }
                    val rolesDeferred = async { fetchUsersByRoleInternal() }
                    val attacksDeferred = async { fetchTopAttackTypesInternal() }
                    val alertsDeferred = async { fetchRecentAlertsInternal() }
                    val sessionsDeferred = async { fetchActiveSessionsInternal() }

                    summaryDeferred.await()
                    trendDeferred.await()
                    healthDeferred.await()
                    rolesDeferred.await()
                    attacksDeferred.await()
                    alertsDeferred.await()
                    sessionsDeferred.await()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to refresh dashboard data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================================
    // 9. USER MANAGEMENT STATES & API CALLS
    // =========================================================================

    private val _usersList = MutableStateFlow<List<UserManagementItemDto>>(emptyList())
    val usersList: StateFlow<List<UserManagementItemDto>> = _usersList.asStateFlow()

    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    private val _selectedUserStatus = MutableStateFlow<String?>(null)
    val selectedUserStatus: StateFlow<String?> = _selectedUserStatus.asStateFlow()

    private val _isLoadingUsers = MutableStateFlow(false)
    val isLoadingUsers: StateFlow<Boolean> = _isLoadingUsers.asStateFlow()

    private val _usersError = MutableStateFlow<String?>(null)
    val usersError: StateFlow<String?> = _usersError.asStateFlow()

    private val _totalUsersCount = MutableStateFlow(0)
    val totalUsersCount: StateFlow<Int> = _totalUsersCount.asStateFlow()

    /**
     * Filtered reactive user list based on search query and status filter.
     */
    val filteredUsers: StateFlow<List<UserManagementItemDto>> = combine(
        _usersList,
        _userSearchQuery,
        _selectedUserStatus
    ) { list, query, status ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.fullName.lowercase().contains(cleanQuery) ||
                    item.email.lowercase().contains(cleanQuery) ||
                    (item.memberId?.lowercase()?.contains(cleanQuery) == true) ||
                    item.role.lowercase().contains(cleanQuery) ||
                    item.branchName.lowercase().contains(cleanQuery)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onUserSearchQueryChange(query: String) {
        _userSearchQuery.value = query
    }

    fun onUserStatusFilterChange(status: String?) {
        _selectedUserStatus.value = status
    }

    fun clearUserFilters() {
        _userSearchQuery.value = ""
        _selectedUserStatus.value = null
    }

    /**
     * Fetches all organization users from /api/users/view-all.
     */
    fun fetchUsers(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingUsers.value = true
            _usersError.value = null

            val result = repo.request<UserListResponse> {
                get(
                    SECURITY_USERS_LIST_URL,
                    repo.query("page" to page, "limit" to limit)
                )
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _usersList.value = res.data
                        _totalUsersCount.value = res.total
                    } else {
                        _usersError.value = res.message ?: "Failed to load users"
                    }
                },
                onFailure = { err ->
                    _usersError.value = err.message ?: "Network error fetching users"
                }
            )
            _isLoadingUsers.value = false
        }
    }

    private val _isCreatingUser = MutableStateFlow(false)
    val isCreatingUser: StateFlow<Boolean> = _isCreatingUser.asStateFlow()

    private val _createUserError = MutableStateFlow<String?>(null)
    val createUserError: StateFlow<String?> = _createUserError.asStateFlow()

    /**
     * Submits the typed payload matching the exact backend schema for /api/users/create
     */
    fun createUser(
        request: CreateUserRequest,
        onSuccess: (CreateUserData?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingUser.value = true
            _createUserError.value = null

            val result = repo.request<CreateUserApiResponse> {
                post(SECURITY_CREATE_USER_URL, request)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchUsers() // Refresh user directory
                        onSuccess(res.data)
                    } else {
                        val msg = res.message ?: "Failed to create user"
                        _createUserError.value = msg
                        onError(msg)
                    }
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error creating user"
                    _createUserError.value = msg
                    onError(msg)
                }
            )
            _isCreatingUser.value = false
        }
    }

    // =========================================================================
    // 1. SUMMARY API CALLS
    // =========================================================================

    fun onDateRangeSelected(start: String, end: String) {
        _startDate.value = start
        _endDate.value = end
        fetchSummary()
        fetchLoginTrend()
    }

    fun fetchSummary() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchSummaryInternal()
            _isLoading.value = false
        }
    }

    private suspend fun fetchSummaryInternal() {
        val fromIso = convertToIsoDate(_startDate.value)
        val toIso = convertToIsoDate(_endDate.value)

        val result = repo.request<SecuritySummaryResponse> {
            get(
                SECURITY_SUMMARY_URL,
                repo.query("from" to fromIso, "to" to toIso)
            )
        }

        result.fold(
            onSuccess = { res ->
                if (res.success && res.data != null) {
                    _summary.value = res.data
                } else {
                    _errorMessage.value = res.message ?: "Failed to load security summary"
                }
            },
            onFailure = { err ->
                _errorMessage.value = err.message ?: "Network error fetching summary"
            }
        )
    }

    // =========================================================================
    // 2. LOGIN TREND API CALLS
    // =========================================================================

    fun onTrendGranularityChanged(granularity: String) {
        _trendGranularity.value = granularity
        fetchLoginTrend()
    }

    fun fetchLoginTrend() {
        viewModelScope.launch {
            _isLoadingTrend.value = true
            _trendError.value = null
            fetchLoginTrendInternal()
            _isLoadingTrend.value = false
        }
    }

    private suspend fun fetchLoginTrendInternal() {
        val fromIso = convertToIsoDate(_startDate.value)
        val toIso = convertToIsoDate(_endDate.value)

        val result = repo.request<LoginTrendResponse> {
            get(
                SECURITY_LOGIN_TREND_URL,
                repo.query(
                    "from" to fromIso,
                    "to" to toIso,
                    "granularity" to _trendGranularity.value
                )
            )
        }

        result.fold(
            onSuccess = { res ->
                if (res.success && res.data != null) {
                    _loginTrend.value = res.data
                } else {
                    _trendError.value = res.message ?: "Failed to load login trend"
                }
            },
            onFailure = { err ->
                _trendError.value = err.message ?: "Network error fetching login trend"
            }
        )
    }

    // =========================================================================
    // 3. SECURITY HEALTH API CALLS
    // =========================================================================

    fun fetchSecurityHealth() {
        viewModelScope.launch {
            _isLoadingHealth.value = true
            _healthError.value = null
            fetchSecurityHealthInternal()
            _isLoadingHealth.value = false
        }
    }

    private suspend fun fetchSecurityHealthInternal() {
        val result = repo.request<SecurityHealthResponse> {
            get(SECURITY_HEALTH_URL)
        }

        result.fold(
            onSuccess = { res ->
                if (res.success && res.data != null) {
                    _securityHealth.value = res.data
                } else {
                    _healthError.value = res.message ?: "Failed to load security health metrics"
                }
            },
            onFailure = { err ->
                _healthError.value = err.message ?: "Network error fetching security health"
            }
        )
    }

    // =========================================================================
    // 4. USERS BY ROLE API CALLS
    // =========================================================================

    fun fetchUsersByRole() {
        viewModelScope.launch {
            _isLoadingUsersByRole.value = true
            _usersByRoleError.value = null
            fetchUsersByRoleInternal()
            _isLoadingUsersByRole.value = false
        }
    }

    private suspend fun fetchUsersByRoleInternal() {
        val result = repo.request<UsersByRoleResponse> {
            get(SECURITY_USERS_BY_ROLE_URL)
        }

        result.fold(
            onSuccess = { res ->
                if (res.success && res.data != null) {
                    _usersByRole.value = res.data
                } else {
                    _usersByRoleError.value = res.message ?: "Failed to load user role distribution"
                }
            },
            onFailure = { err ->
                _usersByRoleError.value = err.message ?: "Network error fetching users by role"
            }
        )
    }

    // =========================================================================
    // 5. TOP ATTACK TYPES API CALLS
    // =========================================================================

    fun onAttackPeriodChanged(period: String) {
        _attackPeriod.value = period
        fetchTopAttackTypes()
    }

    fun fetchTopAttackTypes() {
        viewModelScope.launch {
            _isLoadingAttacks.value = true
            _attacksError.value = null
            fetchTopAttackTypesInternal()
            _isLoadingAttacks.value = false
        }
    }

    private suspend fun fetchTopAttackTypesInternal() {
        val result = repo.request<TopAttackTypesResponse> {
            get(
                SECURITY_TOP_ATTACKS_URL,
                repo.query("period" to _attackPeriod.value)
            )
        }

        result.fold(
            onSuccess = { res ->
                if (res.success && res.data != null) {
                    _attackTypes.value = res.data
                } else {
                    _attacksError.value = res.message ?: "Failed to load attack types"
                }
            },
            onFailure = { err ->
                _attacksError.value = err.message ?: "Network error fetching attack types"
            }
        )
    }

    // =========================================================================
    // 6. RECENT ALERTS API CALLS & FILTERS
    // =========================================================================

    fun onAlertSearchQueryChange(query: String) {
        _alertsSearchQuery.value = query
    }

    fun onAlertSeverityFilterChange(severity: String?) {
        _selectedAlertSeverity.value = severity
    }

    fun onAlertStatusFilterChange(status: String?) {
        _selectedAlertStatus.value = status
    }

    fun clearAlertFilters() {
        _alertsSearchQuery.value = ""
        _selectedAlertSeverity.value = null
        _selectedAlertStatus.value = null
    }

    fun fetchRecentAlerts() {
        viewModelScope.launch {
            _isLoadingAlerts.value = true
            _alertsError.value = null
            fetchRecentAlertsInternal()
            _isLoadingAlerts.value = false
        }
    }

    private suspend fun fetchRecentAlertsInternal() {
        val result = repo.request<RecentAlertsResponse> {
            get(SECURITY_RECENT_ALERTS_URL)
        }

        result.fold(
            onSuccess = { res ->
                if (res.success && res.data != null) {
                    _recentAlertsList.value = res.data.alerts
                } else {
                    _alertsError.value = res.message ?: "Failed to load security alerts"
                }
            },
            onFailure = { err ->
                _alertsError.value = err.message ?: "Network error fetching security alerts"
            }
        )
    }

    // =========================================================================
    // 7. ACTIVE SESSIONS API CALLS
    // =========================================================================

    fun onSessionSearchQueryChange(query: String) {
        _sessionsSearchQuery.value = query
    }

    fun fetchActiveSessions() {
        viewModelScope.launch {
            _isLoadingSessions.value = true
            _sessionsError.value = null
            fetchActiveSessionsInternal()
            _isLoadingSessions.value = false
        }
    }

    private suspend fun fetchActiveSessionsInternal() {
        val result = repo.request<ActiveSessionsResponse> {
            get(SECURITY_ACTIVE_SESSIONS_URL)
        }

        result.fold(
            onSuccess = { res ->
                if (res.success && res.data != null) {
                    _activeSessionsList.value = res.data.sessions
                } else {
                    _sessionsError.value = res.message ?: "Failed to load active sessions"
                }
            },
            onFailure = { err ->
                _sessionsError.value = err.message ?: "Network error fetching active sessions"
            }
        )
    }

    // =========================================================================
    // 8. STRING & DATE FORMATTING UTILITIES
    // =========================================================================

    /**
     * Converts a DD-MM-YYYY string to YYYY-MM-DD for backend API consumption.
     */
    fun convertToIsoDate(dmy: String): String {
        if (dmy.isBlank()) return ""
        return try {
            val parser = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(dmy.trim())
            if (date != null) formatter.format(date) else ""
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Formats an ISO-8601 UTC timestamp to time-elapsed string ("Just now", "X min ago", etc.).
     */
    fun formatTimeAgo(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Just now"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString) ?: return "Just now"
            val diffMins = (Date().time - date.time) / (1000 * 60)
            when {
                diffMins < 1 -> "Just now"
                diffMins < 60 -> "$diffMins min ago"
                diffMins < 1440 -> "${diffMins / 60} hr ago"
                else -> "${diffMins / 1440} days ago"
            }
        } catch (_: Exception) {
            "Just now"
        }
    }

    /**
     * Formats an ISO-8601 UTC timestamp to readable display date (e.g. "02 Nov 2026, 11:23 AM").
     */
    fun formatDisplayDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString) ?: return "--"
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
        } catch (_: Exception) {
            "--"
        }
    }
}