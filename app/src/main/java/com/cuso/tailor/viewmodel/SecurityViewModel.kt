@file:Suppress("unused")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.sales.ApiResponse
import com.cuso.tailor.model.security.*
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
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

// --- Security Data Backup Endpoints ---
private const val SECURITY_BACKUP_VIEW_ALL_URL = "/api/security/backup/view-all"
private const val SECURITY_BACKUP_VIEW_ONE_URL = "/api/security/backup/view-one"
private const val SECURITY_BACKUP_DOWNLOAD_REPORT_URL = "/api/security/backup/download-report"
private const val SECURITY_BACKUP_CREATE_URL = "/api/security/backup/create"
private const val SECURITY_BACKUP_RUN_NOW_URL = "/api/security/backup/run-now"

// --- Security Data Recovery Endpoints ---
private const val SECURITY_RECOVERY_INITIATE_URL = "/api/security/recovery/initiate"
private const val SECURITY_RECOVERY_VIEW_ONE_URL = "/api/security/recovery/view-one"

// --- Security API Integration Endpoints ---
private const val SECURITY_API_INTEGRATIONS_VIEW_ALL_URL = "/api/security/api-integrations/view-all"
private const val SECURITY_API_INTEGRATIONS_VIEW_ONE_URL = "/api/security/api-integrations/view-one"
private const val SECURITY_API_INTEGRATIONS_CREATE_URL = "/api/security/api-integrations/create"
private const val SECURITY_API_INTEGRATIONS_TOGGLE_STATUS_URL = "/api/security/api-integrations/toggle-status"
private const val SECURITY_API_INTEGRATIONS_UPDATE_ONE_URL = "/api/security/api-integrations/update-one"
private const val SECURITY_API_INTEGRATIONS_REVOKE_ONE_URL = "/api/security/api-integrations/revoke-one"
private const val SECURITY_API_INTEGRATIONS_REVEAL_SECRET_URL = "/api/security/api-integrations/reveal-secret"

// --- Security API Logs Endpoints ---
private const val SECURITY_API_LOGS_VIEW_ALL_URL = "/api/security/api-logs/view-all"
private const val SECURITY_API_LOGS_VIEW_ONE_URL = "/api/security/api-logs/view-one"

// --- Security Audit Logs Endpoints ---
private const val SECURITY_AUDIT_LOGS_VIEW_ALL_URL = "/api/inventory/audit-logs/view-all"
private const val SECURITY_AUDIT_LOGS_VIEW_ONE_URL = "/api/inventory/audit-logs/view-one"

// =========================================================================
// 16. MOBILE ACCESS MANAGEMENT (DEVICE MGMT, AUTH, DATA SECURITY & AUDIT)
// =========================================================================

// --- MOBILE ACCESS ENDPOINTS ---
private const val MOBILE_DEVICES_VIEW_ALL_URL = "/api/security/mobile-device/view-all"
private const val MOBILE_DEVICE_APPROVE_URL = "/api/security/mobile-device/approve"
private const val MOBILE_DEVICE_BLOCK_URL = "/api/security/mobile-device/block"

private const val MOBILE_AUTH_POLICY_URL = "/api/security/mobile-auth-policy"
private const val MOBILE_AUTH_POLICY_PASSWORD_URL = "/api/security/mobile-auth-policy/password"
private const val MOBILE_AUTH_POLICY_OTP_URL = "/api/security/mobile-auth-policy/otp"
private const val MOBILE_AUTH_POLICY_2FA_URL = "/api/security/mobile-auth-policy/2fa"
private const val MOBILE_AUTH_POLICY_BIOMETRIC_URL = "/api/security/mobile-auth-policy/biometric"

private const val MOBILE_DATA_SECURITY_POLICY_URL = "/api/security/mobile-data-security-policy"

private const val MOBILE_SECURITY_AUDIT_VIEW_ALL_URL = "/api/security/mobile-security-audit/view-all"
private const val MOBILE_SECURITY_AUDIT_RESOLVE_URL = "/api/security/mobile-security-audit/resolve"
private const val MOBILE_SECURITY_AUDIT_BLOCK_USER_URL = "/api/security/mobile-security-audit/block-user"

// --- HELP CENTER ENDPOINTS ---
private const val HELP_ARTICLES_VIEW_ALL_URL = "/api/security/help-article/view-all"
private const val HELP_ARTICLE_BY_SLUG_URL = "/api/security/help-article/view-one"
private const val HELP_ARTICLE_VOTE_URL = "/api/security/help-article"

// --- TICKET ENDPOINTS ---
private const val SUPPORT_TICKETS_VIEW_ALL_URL = "/api/security/ticket/view-all"
private const val SUPPORT_TICKET_VIEW_ONE_URL = "/api/security/ticket/view-one"
private const val SUPPORT_TICKET_CREATE_URL = "/api/security/ticket/create"
private const val SUPPORT_TICKET_BASE_URL = "/api/security/ticket"

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

            matchesQuery && matchesStatus
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
    // 8. DATA BACKUP STATES & API OPERATIONS
    // =========================================================================

    private val _backupsList = MutableStateFlow<List<BackupItemDto>>(emptyList())
    val backupsList: StateFlow<List<BackupItemDto>> = _backupsList.asStateFlow()

    private val _backupSearchQuery = MutableStateFlow("")
    val backupSearchQuery: StateFlow<String> = _backupSearchQuery.asStateFlow()

    private val _selectedBackupStatus = MutableStateFlow<String?>(null)
    val selectedBackupStatus: StateFlow<String?> = _selectedBackupStatus.asStateFlow()

    private val _isLoadingBackups = MutableStateFlow(false)
    val isLoadingBackups: StateFlow<Boolean> = _isLoadingBackups.asStateFlow()

    private val _backupsError = MutableStateFlow<String?>(null)
    val backupsError: StateFlow<String?> = _backupsError.asStateFlow()

    private val _selectedBackupDetail = MutableStateFlow<BackupItemDto?>(null)
    val selectedBackupDetail: StateFlow<BackupItemDto?> = _selectedBackupDetail.asStateFlow()

    private val _isLoadingBackupDetail = MutableStateFlow(false)
    val isLoadingBackupDetail: StateFlow<Boolean> = _isLoadingBackupDetail.asStateFlow()

    private val _backupDetailError = MutableStateFlow<String?>(null)
    val backupDetailError: StateFlow<String?> = _backupDetailError.asStateFlow()

    private val _isCreatingBackup = MutableStateFlow(false)
    val isCreatingBackup: StateFlow<Boolean> = _isCreatingBackup.asStateFlow()

    private val _createBackupError = MutableStateFlow<String?>(null)
    val createBackupError: StateFlow<String?> = _createBackupError.asStateFlow()

    private val _auditReport = MutableStateFlow<BackupAuditReportResponse?>(null)
    val auditReport: StateFlow<BackupAuditReportResponse?> = _auditReport.asStateFlow()

    private val _isDownloadingReport = MutableStateFlow(false)
    val isDownloadingReport: StateFlow<Boolean> = _isDownloadingReport.asStateFlow()

    private val _downloadReportError = MutableStateFlow<String?>(null)
    val downloadReportError: StateFlow<String?> = _downloadReportError.asStateFlow()

    private val _isRunningBackup = MutableStateFlow(false)
    val isRunningBackup: StateFlow<Boolean> = _isRunningBackup.asStateFlow()

    private val _runBackupError = MutableStateFlow<String?>(null)
    val runBackupError: StateFlow<String?> = _runBackupError.asStateFlow()

    val filteredBackups: StateFlow<List<BackupItemDto>> = combine(
        _backupsList,
        _backupSearchQuery,
        _selectedBackupStatus
    ) { list, query, status ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.backupId.lowercase().contains(cleanQuery) ||
                    item.name.lowercase().contains(cleanQuery) ||
                    (item.restorePointId?.lowercase()?.contains(cleanQuery) == true) ||
                    item.sourceTarget.lowercase().contains(cleanQuery) ||
                    item.source.lowercase().contains(cleanQuery)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBackupSearchQueryChange(query: String) {
        _backupSearchQuery.value = query
    }

    fun onBackupStatusFilterChange(status: String?) {
        _selectedBackupStatus.value = status
    }

    fun clearBackupFilters() {
        _backupSearchQuery.value = ""
        _selectedBackupStatus.value = null
    }

    fun fetchBackups() {
        viewModelScope.launch {
            _isLoadingBackups.value = true
            _backupsError.value = null

            val result = repo.request<BackupListApiResponse> {
                get(SECURITY_BACKUP_VIEW_ALL_URL)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _backupsList.value = res.data
                    } else {
                        _backupsError.value = res.message ?: "Failed to fetch backups"
                    }
                },
                onFailure = { err ->
                    _backupsError.value = err.message ?: "Network error fetching backups"
                }
            )
            _isLoadingBackups.value = false
        }
    }

    fun fetchBackupDetail(backupId: String) {
        viewModelScope.launch {
            _isLoadingBackupDetail.value = true
            _backupDetailError.value = null

            val result = repo.request<BackupDetailApiResponse> {
                get("$SECURITY_BACKUP_VIEW_ONE_URL/$backupId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _selectedBackupDetail.value = res.data
                    } else {
                        _backupDetailError.value = res.message ?: "Backup not found"
                    }
                },
                onFailure = { err ->
                    _backupDetailError.value = err.message ?: "Network error loading backup detail"
                }
            )
            _isLoadingBackupDetail.value = false
        }
    }

    fun createBackup(
        request: CreateBackupRequest,
        onSuccess: (BackupItemDto?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingBackup.value = true
            _createBackupError.value = null

            val result = repo.request<CreateBackupApiResponse> {
                post(SECURITY_BACKUP_CREATE_URL, request)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchBackups()
                        onSuccess(res.data)
                    } else {
                        val msg = res.message ?: "Failed to create backup configuration"
                        _createBackupError.value = msg
                        onError(msg)
                    }
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error creating backup"
                    _createBackupError.value = msg
                    onError(msg)
                }
            )
            _isCreatingBackup.value = false
        }
    }

    fun downloadBackupReport(
        backupId: String,
        onSuccess: (BackupAuditReportResponse) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isDownloadingReport.value = true
            _downloadReportError.value = null

            val result = repo.request<BackupAuditReportResponse> {
                get("$SECURITY_BACKUP_DOWNLOAD_REPORT_URL/$backupId")
            }

            result.fold(
                onSuccess = { report ->
                    _auditReport.value = report
                    onSuccess(report)
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error downloading backup report"
                    _downloadReportError.value = msg
                    onError(msg)
                }
            )
            _isDownloadingReport.value = false
        }
    }

    fun runBackupNow(
        backupConfigId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (backupConfigId.isBlank()) {
            onError("Invalid backup configuration ID")
            return
        }

        viewModelScope.launch {
            _isRunningBackup.value = true
            _runBackupError.value = null

            val result = repo.request<RunBackupApiResponse> {
                post("$SECURITY_BACKUP_RUN_NOW_URL/$backupConfigId", emptyMap<String, Any>())
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        val msg = res.message ?: "Backup execution triggered"
                        fetchBackups()
                        onSuccess(msg)
                    } else {
                        val msg = res.message ?: "Failed to trigger backup execution"
                        _runBackupError.value = msg
                        onError(msg)
                    }
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error triggering backup execution"
                    _runBackupError.value = msg
                    onError(msg)
                }
            )
            _isRunningBackup.value = false
        }
    }

    // =========================================================================
    // 9. DATA RECOVERY STATES & API OPERATIONS
    // =========================================================================

    private val _recoverySearchQuery = MutableStateFlow("")
    val recoverySearchQuery: StateFlow<String> = _recoverySearchQuery.asStateFlow()

    private val _isInitiatingRecovery = MutableStateFlow(false)
    val isInitiatingRecovery: StateFlow<Boolean> = _isInitiatingRecovery.asStateFlow()

    private val _initiateRecoveryError = MutableStateFlow<String?>(null)
    val initiateRecoveryError: StateFlow<String?> = _initiateRecoveryError.asStateFlow()

    private val _selectedRecoveryDetail = MutableStateFlow<RecoveryItemDto?>(null)
    val selectedRecoveryDetail: StateFlow<RecoveryItemDto?> = _selectedRecoveryDetail.asStateFlow()

    private val _isLoadingRecoveryDetail = MutableStateFlow(false)
    val isLoadingRecoveryDetail: StateFlow<Boolean> = _isLoadingRecoveryDetail.asStateFlow()

    private val _recoveryDetailError = MutableStateFlow<String?>(null)
    val recoveryDetailError: StateFlow<String?> = _recoveryDetailError.asStateFlow()

    val filteredRecoveryBackups: StateFlow<List<BackupItemDto>> = combine(
        _backupsList,
        _recoverySearchQuery
    ) { list, query ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            cleanQuery.isEmpty() ||
                    item.backupId.lowercase().contains(cleanQuery) ||
                    item.name.lowercase().contains(cleanQuery) ||
                    (item.restorePointId?.lowercase()?.contains(cleanQuery) == true) ||
                    item.sourceTarget.lowercase().contains(cleanQuery) ||
                    item.source.lowercase().contains(cleanQuery)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onRecoverySearchQueryChange(query: String) {
        _recoverySearchQuery.value = query
    }

    fun initiateRecovery(
        request: InitiateRecoveryRequest,
        onSuccess: (RecoveryItemDto) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isInitiatingRecovery.value = true
            _initiateRecoveryError.value = null

            val result = repo.request<InitiateRecoveryApiResponse> {
                post(SECURITY_RECOVERY_INITIATE_URL, request)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        onSuccess(res.data)
                    } else {
                        val msg = res.message ?: "Failed to initiate recovery"
                        _initiateRecoveryError.value = msg
                        onError(msg)
                    }
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error initiating recovery"
                    _initiateRecoveryError.value = msg
                    onError(msg)
                }
            )
            _isInitiatingRecovery.value = false
        }
    }

    fun fetchRecoveryDetail(recoveryId: String) {
        viewModelScope.launch {
            _isLoadingRecoveryDetail.value = true
            _recoveryDetailError.value = null

            val result = repo.request<RecoveryDetailApiResponse> {
                get("$SECURITY_RECOVERY_VIEW_ONE_URL/$recoveryId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _selectedRecoveryDetail.value = res.data
                    } else {
                        _recoveryDetailError.value = res.message ?: "Recovery record not found"
                    }
                },
                onFailure = { err ->
                    _recoveryDetailError.value = err.message ?: "Network error loading recovery details"
                }
            )
            _isLoadingRecoveryDetail.value = false
        }
    }

    // =========================================================================
    // 10. API INTEGRATION STATES & OPERATIONS
    // =========================================================================

    private val _integrationsList = MutableStateFlow<List<ApiIntegrationItemDto>>(emptyList())
    val integrationsList: StateFlow<List<ApiIntegrationItemDto>> = _integrationsList.asStateFlow()

    private val _integrationSearchQuery = MutableStateFlow("")
    val integrationSearchQuery: StateFlow<String> = _integrationSearchQuery.asStateFlow()

    private val _isLoadingIntegrations = MutableStateFlow(false)
    val isLoadingIntegrations: StateFlow<Boolean> = _isLoadingIntegrations.asStateFlow()

    private val _integrationsError = MutableStateFlow<String?>(null)
    val integrationsError: StateFlow<String?> = _integrationsError.asStateFlow()

    private val _selectedIntegrationDetail = MutableStateFlow<ApiIntegrationItemDto?>(null)
    val selectedIntegrationDetail: StateFlow<ApiIntegrationItemDto?> = _selectedIntegrationDetail.asStateFlow()

    private val _isLoadingIntegrationDetail = MutableStateFlow(false)
    val isLoadingIntegrationDetail: StateFlow<Boolean> = _isLoadingIntegrationDetail.asStateFlow()

    private val _integrationDetailError = MutableStateFlow<String?>(null)
    val integrationDetailError: StateFlow<String?> = _integrationDetailError.asStateFlow()

    private val _isCreatingIntegration = MutableStateFlow(false)
    val isCreatingIntegration: StateFlow<Boolean> = _isCreatingIntegration.asStateFlow()

    private val _createIntegrationError = MutableStateFlow<String?>(null)
    val createIntegrationError: StateFlow<String?> = _createIntegrationError.asStateFlow()

    private val _isUpdatingIntegration = MutableStateFlow(false)
    val isUpdatingIntegration: StateFlow<Boolean> = _isUpdatingIntegration.asStateFlow()

    private val _updateIntegrationError = MutableStateFlow<String?>(null)
    val updateIntegrationError: StateFlow<String?> = _updateIntegrationError.asStateFlow()

    private val _isRevokingIntegration = MutableStateFlow(false)
    val isRevokingIntegration: StateFlow<Boolean> = _isRevokingIntegration.asStateFlow()

    private val _revealedSecrets = MutableStateFlow<RevealedSecretDataDto?>(null)
    val revealedSecrets: StateFlow<RevealedSecretDataDto?> = _revealedSecrets.asStateFlow()

    private val _isRevealingSecret = MutableStateFlow(false)
    val isRevealingSecret: StateFlow<Boolean> = _isRevealingSecret.asStateFlow()

    val filteredIntegrations: StateFlow<List<ApiIntegrationItemDto>> = combine(
        _integrationsList,
        _integrationSearchQuery
    ) { list, query ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            cleanQuery.isEmpty() ||
                    item.name.lowercase().contains(cleanQuery) ||
                    item.category.lowercase().contains(cleanQuery) ||
                    item.baseUrl.lowercase().contains(cleanQuery) ||
                    item.authType.lowercase().contains(cleanQuery)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onIntegrationSearchQueryChange(query: String) {
        _integrationSearchQuery.value = query
    }

    fun clearSelectedIntegrationDetail() {
        _selectedIntegrationDetail.value = null
    }

    fun clearRevealedSecrets() {
        _revealedSecrets.value = null
    }

    fun fetchApiIntegrations(page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            _isLoadingIntegrations.value = true
            _integrationsError.value = null

            val result = repo.request<ApiIntegrationListResponse> {
                get(
                    SECURITY_API_INTEGRATIONS_VIEW_ALL_URL,
                    repo.query("page" to page, "limit" to limit)
                )
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _integrationsList.value = res.data
                    } else {
                        _integrationsError.value = res.message ?: "Failed to load integrations"
                    }
                },
                onFailure = { err ->
                    _integrationsError.value = err.message ?: "Network error fetching integrations"
                }
            )
            _isLoadingIntegrations.value = false
        }
    }

    fun fetchApiIntegrationDetail(integrationId: String) {
        viewModelScope.launch {
            _isLoadingIntegrationDetail.value = true
            _integrationDetailError.value = null

            val result = repo.request<ApiIntegrationDetailResponse> {
                get("$SECURITY_API_INTEGRATIONS_VIEW_ONE_URL/$integrationId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _selectedIntegrationDetail.value = res.data
                    } else {
                        _integrationDetailError.value = res.message ?: "Integration details not found"
                    }
                },
                onFailure = { err ->
                    _integrationDetailError.value = err.message ?: "Network error loading integration"
                }
            )
            _isLoadingIntegrationDetail.value = false
        }
    }

    fun createApiIntegration(
        request: CreateApiIntegrationRequest,
        onSuccess: (ApiIntegrationItemDto) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingIntegration.value = true
            _createIntegrationError.value = null

            val result = repo.request<CreateApiIntegrationResponse> {
                post(SECURITY_API_INTEGRATIONS_CREATE_URL, request)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        fetchApiIntegrations()
                        onSuccess(res.data)
                    } else {
                        val msg = res.message ?: "Failed to create integration"
                        _createIntegrationError.value = msg
                        onError(msg)
                    }
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error creating integration"
                    _createIntegrationError.value = msg
                    onError(msg)
                }
            )
            _isCreatingIntegration.value = false
        }
    }

    fun updateApiIntegration(
        integrationId: String,
        request: CreateApiIntegrationRequest,
        onSuccess: (ApiIntegrationItemDto) -> Unit,
        onError: (String) -> Unit
    ) {
        if (integrationId.isBlank()) {
            onError("Invalid integration ID")
            return
        }

        viewModelScope.launch {
            _isUpdatingIntegration.value = true
            _updateIntegrationError.value = null

            val result = repo.request<UpdateApiIntegrationResponse> {
                put("$SECURITY_API_INTEGRATIONS_UPDATE_ONE_URL/$integrationId", request)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        fetchApiIntegrations()
                        onSuccess(res.data)
                    } else {
                        val msg = res.message ?: "Failed to update integration"
                        _updateIntegrationError.value = msg
                        onError(msg)
                    }
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error updating integration"
                    _updateIntegrationError.value = msg
                    onError(msg)
                }
            )
            _isUpdatingIntegration.value = false
        }
    }

    fun revokeApiIntegration(
        integrationId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (integrationId.isBlank()) {
            onError("Invalid integration ID")
            return
        }

        viewModelScope.launch {
            _isRevokingIntegration.value = true

            val result = repo.request<RevokeApiIntegrationResponse> {
                delete("$SECURITY_API_INTEGRATIONS_REVOKE_ONE_URL/$integrationId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _integrationsList.value = _integrationsList.value.filter { it.id != integrationId }
                        val msg = res.message ?: "API Integration revoked successfully"
                        onSuccess(msg)
                    } else {
                        val msg = res.message ?: "Failed to revoke integration"
                        onError(msg)
                    }
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error revoking integration"
                    onError(msg)
                }
            )
            _isRevokingIntegration.value = false
        }
    }

    fun revealApiIntegrationSecret(
        integrationId: String,
        onSuccess: (RevealedSecretDataDto) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val current = _revealedSecrets.value
        if (current != null && current.id == integrationId) {
            onSuccess(current)
            return
        }

        viewModelScope.launch {
            _isRevealingSecret.value = true
            val result = repo.request<RevealSecretApiResponse> {
                post("$SECURITY_API_INTEGRATIONS_REVEAL_SECRET_URL/$integrationId", emptyMap<String, Any>())
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _revealedSecrets.value = res.data
                        onSuccess(res.data)
                    } else {
                        val msg = res.message ?: "Failed to reveal secrets"
                        onError(msg)
                    }
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error revealing secrets"
                    onError(msg)
                }
            )
            _isRevealingSecret.value = false
        }
    }

    fun toggleApiIntegrationStatus(
        integrationId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (integrationId.isBlank()) {
            onError("Invalid integration ID")
            return
        }

        viewModelScope.launch {
            val result = repo.request<ToggleApiIntegrationStatusResponse> {
                put("$SECURITY_API_INTEGRATIONS_TOGGLE_STATUS_URL/$integrationId", emptyMap<String, Any>())
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        val updatedItem = res.data
                        _integrationsList.value = _integrationsList.value.map { item ->
                            if (item.id == integrationId) updatedItem else item
                        }
                        val msg = res.message ?: "Status updated successfully"
                        onSuccess(msg)
                    } else {
                        val msg = res.message ?: "Failed to update status"
                        onError(msg)
                    }
                },
                onFailure = { err ->
                    val msg = err.message ?: "Network error toggling status"
                    onError(msg)
                }
            )
        }
    }

    // =========================================================================
    // 11. SECURITY API LOGS STATES & API OPERATIONS (Clean Separation)
    // =========================================================================

    private val _apiLogsList = MutableStateFlow<List<ApiLogItemDto>>(emptyList())
    val apiLogsList: StateFlow<List<ApiLogItemDto>> = _apiLogsList.asStateFlow()

    private val _apiLogSearchQuery = MutableStateFlow("")
    val apiLogSearchQuery: StateFlow<String> = _apiLogSearchQuery.asStateFlow()

    private val _isLoadingApiLogs = MutableStateFlow(false)
    val isLoadingApiLogs: StateFlow<Boolean> = _isLoadingApiLogs.asStateFlow()

    private val _apiLogsError = MutableStateFlow<String?>(null)
    val apiLogsError: StateFlow<String?> = _apiLogsError.asStateFlow()

    private val _selectedApiLogDetail = MutableStateFlow<ApiLogItemDto?>(null)
    val selectedApiLogDetail: StateFlow<ApiLogItemDto?> = _selectedApiLogDetail.asStateFlow()

    private val _isLoadingApiLogDetail = MutableStateFlow(false)
    val isLoadingApiLogDetail: StateFlow<Boolean> = _isLoadingApiLogDetail.asStateFlow()

    private val _apiLogDetailError = MutableStateFlow<String?>(null)
    val apiLogDetailError: StateFlow<String?> = _apiLogDetailError.asStateFlow()

    val filteredApiLogs: StateFlow<List<ApiLogItemDto>> = combine(
        _apiLogsList,
        _apiLogSearchQuery
    ) { list, query ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            cleanQuery.isEmpty() ||
                    (item.integration?.name?.lowercase()?.contains(cleanQuery) == true) ||
                    item.logId.lowercase().contains(cleanQuery) ||
                    item.method.lowercase().contains(cleanQuery) ||
                    item.endpoint.lowercase().contains(cleanQuery) ||
                    item.statusCode.toString().contains(cleanQuery)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onApiLogSearchQueryChange(query: String) {
        _apiLogSearchQuery.value = query
    }

    /**
     * Fetches all API logs from /api/security/api-logs/view-all
     */
    fun fetchApiLogs(page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            _isLoadingApiLogs.value = true
            _apiLogsError.value = null

            val result = repo.request<ApiLogListResponse> {
                get(
                    SECURITY_API_LOGS_VIEW_ALL_URL,
                    repo.query("page" to page, "limit" to limit)
                )
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _apiLogsList.value = res.data
                    } else {
                        _apiLogsError.value = res.message ?: "Failed to load API logs"
                    }
                },
                onFailure = { err ->
                    _apiLogsError.value = err.message ?: "Network error fetching API logs"
                }
            )
            _isLoadingApiLogs.value = false
        }
    }

    /**
     * Fetches single API log details from /api/security/api-logs/view-one/{logId}
     */
    fun fetchApiLogDetail(logId: String) {
        viewModelScope.launch {
            _isLoadingApiLogDetail.value = true
            _apiLogDetailError.value = null

            val result = repo.request<ApiLogDetailResponse> {
                get("$SECURITY_API_LOGS_VIEW_ONE_URL/$logId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _selectedApiLogDetail.value = res.data
                    } else {
                        _apiLogDetailError.value = res.message ?: "Log detail not found"
                    }
                },
                onFailure = { err ->
                    _apiLogDetailError.value = err.message ?: "Network error loading log detail"
                }
            )
            _isLoadingApiLogDetail.value = false
        }
    }

    // =========================================================================
// AUDIT LOGS STATES & OPERATIONS
// =========================================================================

    private val _auditLogsList = MutableStateFlow<List<AuditLogItemDto>>(emptyList())
    val auditLogsList: StateFlow<List<AuditLogItemDto>> = _auditLogsList.asStateFlow()

    private val _auditLogSearchQuery = MutableStateFlow("")
    val auditLogSearchQuery: StateFlow<String> = _auditLogSearchQuery.asStateFlow()

    private val _isLoadingAuditLogs = MutableStateFlow(false)
    val isLoadingAuditLogs: StateFlow<Boolean> = _isLoadingAuditLogs.asStateFlow()

    private val _auditLogsError = MutableStateFlow<String?>(null)
    val auditLogsError: StateFlow<String?> = _auditLogsError.asStateFlow()

    private val _selectedAuditLogDetail = MutableStateFlow<AuditLogItemDto?>(null)
    val selectedAuditLogDetail: StateFlow<AuditLogItemDto?> = _selectedAuditLogDetail.asStateFlow()

    private val _isLoadingAuditLogDetail = MutableStateFlow(false)
    val isLoadingAuditLogDetail: StateFlow<Boolean> = _isLoadingAuditLogDetail.asStateFlow()

    private val _auditLogDetailError = MutableStateFlow<String?>(null)
    val auditLogDetailError: StateFlow<String?> = _auditLogDetailError.asStateFlow()

    val filteredAuditLogs: StateFlow<List<AuditLogItemDto>> = combine(
        _auditLogsList,
        _auditLogSearchQuery
    ) { list, query ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            cleanQuery.isEmpty() ||
                    item.recordName.lowercase().contains(cleanQuery) ||
                    item.action.lowercase().contains(cleanQuery) ||
                    item.module.lowercase().contains(cleanQuery) ||
                    item.description.lowercase().contains(cleanQuery) ||
                    (item.member?.name?.lowercase()?.contains(cleanQuery) == true) ||
                    item.ipAddress.lowercase().contains(cleanQuery)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onAuditLogSearchQueryChange(query: String) {
        _auditLogSearchQuery.value = query
    }

    /**
     * Fetches all audit logs from /api/inventory/audit-logs/view-all
     */
    fun fetchAuditLogs(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingAuditLogs.value = true
            _auditLogsError.value = null

            val result = repo.request<AuditLogListResponse> {
                get(
                    SECURITY_AUDIT_LOGS_VIEW_ALL_URL,
                    repo.query("page" to page, "limit" to limit)
                )
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _auditLogsList.value = res.data.data
                    } else {
                        _auditLogsError.value = res.message ?: "Failed to load audit logs"
                    }
                },
                onFailure = { err ->
                    _auditLogsError.value = err.message ?: "Network error fetching audit logs"
                }
            )
            _isLoadingAuditLogs.value = false
        }
    }

    /**
     * Fetches single audit log details by ID from /api/inventory/audit-logs/view-one/{logId}
     */
    fun fetchAuditLogDetail(logId: String) {
        viewModelScope.launch {
            _isLoadingAuditLogDetail.value = true
            _auditLogDetailError.value = null

            val result = repo.request<AuditLogDetailResponse> {
                get("$SECURITY_AUDIT_LOGS_VIEW_ONE_URL/$logId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _selectedAuditLogDetail.value = res.data
                    } else {
                        _auditLogDetailError.value = res.message ?: "Audit log detail not found"
                    }
                },
                onFailure = { err ->
                    _auditLogDetailError.value = err.message ?: "Network error loading audit log detail"
                }
            )
            _isLoadingAuditLogDetail.value = false
        }
    }

    // =========================================================================
    // 12. INITIALIZATION & OTHER MODULE APIS
    // =========================================================================

    init {
        setDefaultDateRange()
        fetchAllDashboardData()
        fetchBackups()
    }

    private fun setDefaultDateRange() {
        val cal = Calendar.getInstance()
        val dtf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        _endDate.value = dtf.format(cal.time)
        cal.add(Calendar.DAY_OF_MONTH, -30)
        _startDate.value = dtf.format(cal.time)
    }

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
    // 13. USER MANAGEMENT STATES & API CALLS
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
                        fetchUsers()
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
    // 14. SUMMARY & TREND API CALLS
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
    // 16. MOBILE ACCESS MANAGEMENT (DEVICE MGMT, AUTH, DATA SECURITY & AUDIT)
    // =========================================================================
    // --- DEVICE STATE & PAGINATION ---
    private val _mobileDevicesPagingState = MutableStateFlow(PagingUiState<MobileDeviceItem>())
    val mobileDevicesPagingState: StateFlow<PagingUiState<MobileDeviceItem>> = _mobileDevicesPagingState.asStateFlow()

    private var deviceCurrentPage = 1
    private val devicePageLimit = 10

    private val _mobileDeviceSearch = MutableStateFlow("")
    val mobileDeviceSearch: StateFlow<String> = _mobileDeviceSearch.asStateFlow()

    private val _devicePlatformFilter = MutableStateFlow<String?>(null)
    private val _deviceApprovalStatusFilter = MutableStateFlow<String?>(null)
    private val _deviceTrustStatusFilter = MutableStateFlow<String?>(null)

    // --- AUDIT STATE & PAGINATION ---
    private val _mobileAuditsPagingState = MutableStateFlow(PagingUiState<MobileSecurityAuditItem>())
    val mobileAuditsPagingState: StateFlow<PagingUiState<MobileSecurityAuditItem>> = _mobileAuditsPagingState.asStateFlow()

    private var auditCurrentPage = 1
    private val auditPageLimit = 10

    private val _mobileAuditSearch = MutableStateFlow("")
    val mobileAuditSearch: StateFlow<String> = _mobileAuditSearch.asStateFlow()

    private val _auditEventTypeFilter = MutableStateFlow<String?>(null)
    private val _auditSeverityFilter = MutableStateFlow<String?>(null)
    private val _auditStatusFilter = MutableStateFlow<String?>(null)

    // --- AUTH & DATA POLICY STATE ---
    private val _mobileAuthPolicy = MutableStateFlow(MobileAuthPolicyData())
    val mobileAuthPolicy: StateFlow<MobileAuthPolicyData> = _mobileAuthPolicy.asStateFlow()

    private val _mobileDataSecurityPolicy = MutableStateFlow(MobileDataSecurityPolicyData())
    val mobileDataSecurityPolicy: StateFlow<MobileDataSecurityPolicyData> = _mobileDataSecurityPolicy.asStateFlow()

    // --- DEVICE API CALLS ---
    fun fetchMobileDevices(isLoadMore: Boolean = false) {
        viewModelScope.launch {
            if (isLoadMore) {
                if (_mobileDevicesPagingState.value.isLoadingMore || !_mobileDevicesPagingState.value.canLoadMore) return@launch
                _mobileDevicesPagingState.value = _mobileDevicesPagingState.value.copy(isLoadingMore = true)
                deviceCurrentPage++
            } else {
                deviceCurrentPage = 1
                _mobileDevicesPagingState.value = _mobileDevicesPagingState.value.copy(isInitialLoading = true)
            }

            val queryParams = mutableListOf<Pair<String, Any>>(
                "page" to deviceCurrentPage,
                "limit" to devicePageLimit
            )
            if (_mobileDeviceSearch.value.isNotBlank()) queryParams.add("search" to _mobileDeviceSearch.value.trim())
            _devicePlatformFilter.value?.let { queryParams.add("platform" to it) }
            _deviceApprovalStatusFilter.value?.let { queryParams.add("approvalStatus" to it) }
            _deviceTrustStatusFilter.value?.let { queryParams.add("trustStatus" to it) }

            val res = repo.request<MobileDeviceListResponse> {
                get(MOBILE_DEVICES_VIEW_ALL_URL, repo.query(*queryParams.toTypedArray()))
            }

            res.fold(
                onSuccess = { response ->
                    if (response.success) {
                        val newItems = response.data
                        val totalPages = response.pagination?.totalPages ?: 1
                        val updatedList = if (isLoadMore) _mobileDevicesPagingState.value.items + newItems else newItems

                        _mobileDevicesPagingState.value = _mobileDevicesPagingState.value.copy(
                            items = updatedList,
                            isInitialLoading = false,
                            isLoadingMore = false,
                            canLoadMore = deviceCurrentPage < totalPages
                        )
                    } else {
                        _mobileDevicesPagingState.value = _mobileDevicesPagingState.value.copy(isInitialLoading = false, isLoadingMore = false)
                    }
                },
                onFailure = {
                    _mobileDevicesPagingState.value = _mobileDevicesPagingState.value.copy(isInitialLoading = false, isLoadingMore = false)
                }
            )
        }
    }

    fun loadMoreMobileDevices() {
        fetchMobileDevices(isLoadMore = true)
    }

    fun fetchMobileSecurityAudit(isLoadMore: Boolean = false) {
        viewModelScope.launch {
            if (isLoadMore) {
                if (_mobileAuditsPagingState.value.isLoadingMore || !_mobileAuditsPagingState.value.canLoadMore) return@launch
                _mobileAuditsPagingState.value = _mobileAuditsPagingState.value.copy(isLoadingMore = true)
                auditCurrentPage++
            } else {
                auditCurrentPage = 1
                _mobileAuditsPagingState.value = _mobileAuditsPagingState.value.copy(isInitialLoading = true)
            }

            val queryParams = mutableListOf<Pair<String, Any>>(
                "page" to auditCurrentPage,
                "limit" to auditPageLimit
            )
            if (_mobileAuditSearch.value.isNotBlank()) queryParams.add("search" to _mobileAuditSearch.value.trim())
            _auditEventTypeFilter.value?.let { queryParams.add("eventType" to it) }
            _auditSeverityFilter.value?.let { queryParams.add("severity" to it) }
            _auditStatusFilter.value?.let { queryParams.add("status" to it) }

            val res = repo.request<MobileSecurityAuditListResponse> {
                get(MOBILE_SECURITY_AUDIT_VIEW_ALL_URL, repo.query(*queryParams.toTypedArray()))
            }

            res.fold(
                onSuccess = { response ->
                    if (response.success) {
                        val newItems = response.data
                        val totalPages = response.pagination?.totalPages ?: 1
                        val updatedList = if (isLoadMore) _mobileAuditsPagingState.value.items + newItems else newItems

                        _mobileAuditsPagingState.value = _mobileAuditsPagingState.value.copy(
                            items = updatedList,
                            isInitialLoading = false,
                            isLoadingMore = false,
                            canLoadMore = auditCurrentPage < totalPages
                        )
                    } else {
                        _mobileAuditsPagingState.value = _mobileAuditsPagingState.value.copy(isInitialLoading = false, isLoadingMore = false)
                    }
                },
                onFailure = {
                    _mobileAuditsPagingState.value = _mobileAuditsPagingState.value.copy(isInitialLoading = false, isLoadingMore = false)
                }
            )
        }
    }

    fun loadMoreMobileSecurityAudit() {
        fetchMobileSecurityAudit(isLoadMore = true)
    }

    fun fetchMobileAuthPolicy() {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repo.request<MobileAuthPolicyResponse> {
                get(MOBILE_AUTH_POLICY_URL)
            }
            res.fold(
                onSuccess = { if (it.success && it.data != null) _mobileAuthPolicy.value = it.data },
                onFailure = { _errorMessage.value = it.message ?: "Failed to load authentication policy" }
            )
            _isLoading.value = false
        }
    }

    fun fetchMobileDataSecurityPolicy() {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repo.request<MobileDataSecurityPolicyResponse> {
                get(MOBILE_DATA_SECURITY_POLICY_URL)
            }
            res.fold(
                onSuccess = { if (it.success && it.data != null) _mobileDataSecurityPolicy.value = it.data },
                onFailure = { _errorMessage.value = it.message ?: "Failed to load data security policy" }
            )
            _isLoading.value = false
        }
    }

    fun approveMobileDevice(deviceId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repo.request<ApiResponse<Any>> {
                post("$MOBILE_DEVICE_APPROVE_URL/$deviceId", emptyMap<String, Any>())
            }
            res.fold(
                onSuccess = {
                    DynamicIslandManager.showSuccess("Device approved")
                    fetchMobileDevices()
                    onSuccess()
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to approve device") }
            )
            _isLoading.value = false
        }
    }

    fun blockMobileDevice(deviceId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val payload = mapOf("reason" to "Blocked by administrator")
            val res = repo.request<ApiResponse<Any>> {
                post("$MOBILE_DEVICE_BLOCK_URL/$deviceId", payload)
            }
            res.fold(
                onSuccess = {
                    DynamicIslandManager.showSuccess("Device blocked")
                    fetchMobileDevices()
                    onSuccess()
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to block device") }
            )
            _isLoading.value = false
        }
    }

    // --- AUDIT API CALLS ---
    fun resolveMobileAudit(eventId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repo.request<ApiResponse<Any>> {
                put("$MOBILE_SECURITY_AUDIT_RESOLVE_URL/$eventId", emptyMap<String, Any>())
            }
            res.fold(
                onSuccess = {
                    DynamicIslandManager.showSuccess("Marked as resolved")
                    fetchMobileSecurityAudit()
                    onSuccess()
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to resolve event") }
            )
            _isLoading.value = false
        }
    }

    fun blockUserFromMobileAudit(eventId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repo.request<ApiResponse<Any>> {
                post("$MOBILE_SECURITY_AUDIT_BLOCK_USER_URL/$eventId", emptyMap<String, Any>())
            }
            res.fold(
                onSuccess = {
                    DynamicIslandManager.showSuccess("User blocked")
                    fetchMobileSecurityAudit()
                    onSuccess()
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to block user") }
            )
            _isLoading.value = false
        }
    }

    // --- POLICY MUTATORS WITH EXACT PICKED PAYLOADS ---
    fun savePasswordPolicy(p: PasswordPolicy, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val payload = mapOf(
                "enabled" to p.enabled,
                "minStrength" to p.minStrength,
                "minLength" to p.minLength,
                "expiryDays" to p.expiryDays,
                "failedAttemptLimit" to p.failedAttemptLimit,
                "lockDurationMinutes" to p.lockDurationMinutes
            )
            val res = repo.request<MobileAuthPolicyResponse> {
                put(MOBILE_AUTH_POLICY_PASSWORD_URL, payload)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _mobileAuthPolicy.value = it.data
                        DynamicIslandManager.showSuccess("Password settings saved")
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to save password settings") }
            )
            _isLoading.value = false
        }
    }

    fun saveOtpPolicy(p: OtpPolicy, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val payload = mapOf(
                "enabled" to p.enabled,
                "channel" to p.channel,
                "expirySeconds" to p.expirySeconds,
                "maxAttempts" to p.maxAttempts,
                "resendLimit" to p.resendLimit,
                "resendCooldownSeconds" to p.resendCooldownSeconds
            )
            val res = repo.request<MobileAuthPolicyResponse> {
                put(MOBILE_AUTH_POLICY_OTP_URL, payload)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _mobileAuthPolicy.value = it.data
                        DynamicIslandManager.showSuccess("OTP settings saved")
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to save OTP settings") }
            )
            _isLoading.value = false
        }
    }

    fun saveTwoFAPolicy(p: TwoFAPolicy, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val payload = mapOf(
                "enabled" to p.enabled,
                "methods" to p.methods,
                "enforcement" to p.enforcement,
                "requireForSensitiveOps" to p.requireForSensitiveOps
            )
            val res = repo.request<MobileAuthPolicyResponse> {
                put(MOBILE_AUTH_POLICY_2FA_URL, payload)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _mobileAuthPolicy.value = it.data
                        DynamicIslandManager.showSuccess("2FA settings saved")
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to save 2FA settings") }
            )
            _isLoading.value = false
        }
    }

    fun saveBiometricPolicy(p: BiometricPolicy, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val payload = mapOf(
                "enabled" to p.enabled,
                "requirePasscodeBackup" to p.requirePasscodeBackup
            )
            val res = repo.request<MobileAuthPolicyResponse> {
                put(MOBILE_AUTH_POLICY_BIOMETRIC_URL, payload)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _mobileAuthPolicy.value = it.data
                        DynamicIslandManager.showSuccess("Biometrics settings saved")
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to save biometrics settings") }
            )
            _isLoading.value = false
        }
    }

    fun patchDataSecurityPolicy(patchPayload: Map<String, Any>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repo.request<MobileDataSecurityPolicyResponse> {
                put(MOBILE_DATA_SECURITY_POLICY_URL, patchPayload)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _mobileDataSecurityPolicy.value = it.data
                        DynamicIslandManager.showSuccess("Data security policy updated")
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to update data security") }
            )
            _isLoading.value = false
        }
    }

    // Filter setters
    fun setDeviceSearch(q: String) { _mobileDeviceSearch.value = q; fetchMobileDevices() }
    fun applyDeviceFilters(platform: String?, approval: String?, trust: String?) {
        _devicePlatformFilter.value = platform
        _deviceApprovalStatusFilter.value = approval
        _deviceTrustStatusFilter.value = trust
        fetchMobileDevices()
    }

    fun setAuditSearch(q: String) { _mobileAuditSearch.value = q; fetchMobileSecurityAudit() }
    fun applyAuditFilters(eventType: String?, severity: String?, status: String?) {
        _auditEventTypeFilter.value = eventType
        _auditSeverityFilter.value = severity
        _auditStatusFilter.value = status
        fetchMobileSecurityAudit()
    }

    // --- HELP CENTER STATES ---
    private val _helpArticles = MutableStateFlow<List<HelpArticleItem>>(emptyList())
    val helpArticles: StateFlow<List<HelpArticleItem>> = _helpArticles.asStateFlow()

    private val _selectedHelpArticle = MutableStateFlow<HelpArticleItem?>(null)
    val selectedHelpArticle: StateFlow<HelpArticleItem?> = _selectedHelpArticle.asStateFlow()

    private val _isHelpCenterLoading = MutableStateFlow(false)
    val isHelpCenterLoading: StateFlow<Boolean> = _isHelpCenterLoading.asStateFlow()

    private val _helpSearchQuery = MutableStateFlow("")
    val helpSearchQuery: StateFlow<String> = _helpSearchQuery.asStateFlow()

    private val _selectedHelpCategory = MutableStateFlow("")
    val selectedHelpCategory: StateFlow<String> = _selectedHelpCategory.asStateFlow()

    private val _showAllArticles = MutableStateFlow(false)
    val showAllArticles: StateFlow<Boolean> = _showAllArticles.asStateFlow()

    fun onHelpSearchChange(query: String) {
        _helpSearchQuery.value = query
    }

    fun onHelpCategorySelect(category: String) {
        _selectedHelpCategory.value = if (_selectedHelpCategory.value == category) "" else category
        fetchHelpArticles()
    }

    fun setShowAllArticles(showAll: Boolean) {
        _showAllArticles.value = showAll
        fetchHelpArticles()
    }

    fun clearHelpFilters() {
        _helpSearchQuery.value = ""
        _selectedHelpCategory.value = ""
        _showAllArticles.value = false
        fetchHelpArticles()
    }

    fun fetchHelpArticles() {
        viewModelScope.launch {
            _isHelpCenterLoading.value = true
            val limit = if (_helpSearchQuery.value.isNotBlank() || _selectedHelpCategory.value.isNotBlank() || _showAllArticles.value) 24 else 4

            val queryParams = mutableListOf<Pair<String, Any>>(
                "page" to 1,
                "limit" to limit
            )
            if (_selectedHelpCategory.value.isNotBlank()) queryParams.add("category" to _selectedHelpCategory.value)
            if (_helpSearchQuery.value.isNotBlank()) queryParams.add("search" to _helpSearchQuery.value.trim())

            val res = repo.request<HelpArticleListResponse> {
                get(HELP_ARTICLES_VIEW_ALL_URL, repo.query(*queryParams.toTypedArray()))
            }
            res.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _helpArticles.value = response.data?.articles.orEmpty()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to load help articles") }
            )
            _isHelpCenterLoading.value = false
        }
    }

    fun fetchHelpArticleBySlug(slug: String) {
        viewModelScope.launch {
            _isHelpCenterLoading.value = true
            _selectedHelpArticle.value = null // Clears stale article before fetching
            val res = repo.request<HelpArticleSingleResponse> {
                get("$HELP_ARTICLE_BY_SLUG_URL/$slug")
            }
            res.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _selectedHelpArticle.value = response.data
                    } else {
                        DynamicIslandManager.showError(response.message ?: "Article not found")
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to load article") }
            )
            _isHelpCenterLoading.value = false
        }
    }

    fun voteHelpArticle(articleId: String, isHelpful: Boolean, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val payload = mapOf("isHelpful" to isHelpful)
            val res = repo.request<ApiResponse<Any>> {
                post("$HELP_ARTICLE_VOTE_URL/$articleId/vote", payload)
            }
            res.fold(
                onSuccess = {
                    DynamicIslandManager.showSuccess("Thank you for your feedback")
                    onSuccess()
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Could not record feedback") }
            )
        }
    }

    // --- TICKET STATES WITH PAGINATION ---
    private val _supportTicketsPagingState = MutableStateFlow(PagingUiState<SupportTicketItem>())
    val supportTicketsPagingState: StateFlow<PagingUiState<SupportTicketItem>> = _supportTicketsPagingState.asStateFlow()

    private var ticketCurrentPage = 1
    private val ticketPageLimit = 15

    private val _selectedSupportTicket = MutableStateFlow<SupportTicketItem?>(null)
    val selectedSupportTicket: StateFlow<SupportTicketItem?> = _selectedSupportTicket.asStateFlow()

    private val _isTicketLoading = MutableStateFlow(false)
    val isTicketLoading: StateFlow<Boolean> = _isTicketLoading.asStateFlow()

    private val _ticketSearchQuery = MutableStateFlow("")
    val ticketSearchQuery: StateFlow<String> = _ticketSearchQuery.asStateFlow()

    private val _ticketStatusFilter = MutableStateFlow<String?>(null)
    private val _ticketPriorityFilter = MutableStateFlow<String?>(null)
    fun onTicketSearchChange(query: String) {
        _ticketSearchQuery.value = query
        fetchSupportTickets()
    }

    fun applyTicketFilters(status: String?, priority: String?) {
        _ticketStatusFilter.value = status
        _ticketPriorityFilter.value = priority
        fetchSupportTickets()
    }

    // 1. Fetch All Tickets with Paginated Infinite Scroll
    fun fetchSupportTickets(isLoadMore: Boolean = false) {
        viewModelScope.launch {
            if (isLoadMore) {
                if (_supportTicketsPagingState.value.isLoadingMore || !_supportTicketsPagingState.value.canLoadMore) return@launch
                _supportTicketsPagingState.value = _supportTicketsPagingState.value.copy(isLoadingMore = true)
                ticketCurrentPage++
            } else {
                ticketCurrentPage = 1
                _supportTicketsPagingState.value = _supportTicketsPagingState.value.copy(isInitialLoading = true)
            }

            val queryParams = mutableListOf<Pair<String, Any>>(
                "page" to ticketCurrentPage,
                "limit" to ticketPageLimit
            )
            if (_ticketSearchQuery.value.isNotBlank()) queryParams.add("search" to _ticketSearchQuery.value.trim())
            _ticketStatusFilter.value?.let { queryParams.add("status" to it) }
            _ticketPriorityFilter.value?.let { queryParams.add("priority" to it) }

            val res = repo.request<SupportTicketListResponse> {
                get(SUPPORT_TICKETS_VIEW_ALL_URL, repo.query(*queryParams.toTypedArray()))
            }
            res.fold(
                onSuccess = { response ->
                    if (response.success) {
                        val newItems = response.data?.tickets.orEmpty()
                        val totalPages = response.data?.meta?.totalPages ?: 1
                        val updatedList = if (isLoadMore) _supportTicketsPagingState.value.items + newItems else newItems

                        _supportTicketsPagingState.value = _supportTicketsPagingState.value.copy(
                            items = updatedList,
                            isInitialLoading = false,
                            isLoadingMore = false,
                            canLoadMore = ticketCurrentPage < totalPages
                        )
                    } else {
                        _supportTicketsPagingState.value = _supportTicketsPagingState.value.copy(
                            isInitialLoading = false,
                            isLoadingMore = false
                        )
                    }
                },
                onFailure = {
                    _supportTicketsPagingState.value = _supportTicketsPagingState.value.copy(
                        isInitialLoading = false,
                        isLoadingMore = false
                    )
                    DynamicIslandManager.showError(it.message ?: "Failed to load tickets")
                }
            )
        }
    }

    fun loadMoreSupportTickets() {
        fetchSupportTickets(isLoadMore = true)
    }

    // 2. Fetch Single Ticket Details
    fun fetchSupportTicketById(ticketId: String) {
        viewModelScope.launch {
            _isTicketLoading.value = true
            _selectedSupportTicket.value = null
            val res = repo.request<SupportTicketSingleResponse> {
                get("$SUPPORT_TICKET_VIEW_ONE_URL/$ticketId")
            }
            res.fold(
                onSuccess = { if (it.success) _selectedSupportTicket.value = it.data },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to load ticket details") }
            )
            _isTicketLoading.value = false
        }
    }

    // 3. Create Support Ticket (Multipart)
    fun createSupportTicket(
        type: String = "external",
        issueType: String,
        subject: String,
        description: String,
        priority: String,
        affectedArea: String,
        contactPreference: String,
        affectedDevice: String?,
        affectedUserId: String?,
        branchId: String?,
        contactEmail: String?,
        attachmentParts: List<MultipartBody.Part>,
        onSuccess: (SupportTicketItem) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val textMap = mutableMapOf<String, RequestBody>(
                    "type" to repo.text(type),
                    "issueType" to repo.text(issueType),
                    "subject" to repo.text(subject),
                    "description" to repo.text(description),
                    "priority" to repo.text(priority),
                    "affectedArea" to repo.text(affectedArea),
                    "contactPreference" to repo.text(contactPreference)
                )
                if (!affectedDevice.isNullOrBlank()) textMap["affectedDevice"] = repo.text(affectedDevice)
                if (!affectedUserId.isNullOrBlank()) textMap["affectedUserId"] = repo.text(affectedUserId)
                if (!branchId.isNullOrBlank()) textMap["branchId"] = repo.text(branchId)
                if (!contactEmail.isNullOrBlank()) textMap["contactEmail"] = repo.text(contactEmail)

                val res = repo.request<SupportTicketSingleResponse> {
                    postMultipart(SUPPORT_TICKET_CREATE_URL, textMap, attachmentParts)
                }
                res.fold(
                    onSuccess = { response ->
                        if (response.success && response.data != null) {
                            DynamicIslandManager.showSuccess("Ticket created successfully")
                            fetchSupportTickets()
                            onSuccess(response.data)
                        }
                    },
                    onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to create ticket") }
                )
            } catch (e: Exception) {
                DynamicIslandManager.showError(e.message ?: "Unexpected error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 4. Assign Ticket (PATCH)
    fun assignSupportTicket(ticketId: String, team: String, assignedTo: String, note: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val payload = mapOf(
                "team" to team,
                "assignedTo" to assignedTo,
                "note" to note
            )
            val res = repo.request<SupportTicketSingleResponse> {
                patch("$SUPPORT_TICKET_BASE_URL/$ticketId/assign", payload)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _selectedSupportTicket.value = it.data
                        DynamicIslandManager.showSuccess("Ticket assigned")
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to assign ticket") }
            )
            _isLoading.value = false
        }
    }

    // 5. Change Status (PATCH)
    fun changeSupportTicketStatus(ticketId: String, newStatus: String, reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val payload = mapOf(
                "newStatus" to newStatus,
                "reason" to reason
            )
            val res = repo.request<SupportTicketSingleResponse> {
                patch("$SUPPORT_TICKET_BASE_URL/$ticketId/status", payload)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _selectedSupportTicket.value = it.data
                        DynamicIslandManager.showSuccess("Status updated")
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to update status") }
            )
            _isLoading.value = false
        }
    }

    // 6. Change Priority (PATCH)
    fun changeSupportTicketPriority(ticketId: String, newPriority: String, reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val payload = mapOf(
                "newPriority" to newPriority,
                "reason" to reason
            )
            val res = repo.request<SupportTicketSingleResponse> {
                patch("$SUPPORT_TICKET_BASE_URL/$ticketId/priority", payload)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _selectedSupportTicket.value = it.data
                        DynamicIslandManager.showSuccess("Priority updated")
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to update priority") }
            )
            _isLoading.value = false
        }
    }

    // 7. Send Conversation Reply (POST Multipart)
    fun replySupportTicket(ticketId: String, message: String, attachmentParts: List<MultipartBody.Part>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val textMap = mapOf("message" to repo.text(message))
            val res = repo.request<SupportTicketSingleResponse> {
                postMultipart("$SUPPORT_TICKET_BASE_URL/$ticketId/reply", textMap, attachmentParts)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _selectedSupportTicket.value = it.data
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to send reply") }
            )
        }
    }

    // 8. Resolve Ticket (POST)
    fun resolveSupportTicket(ticketId: String, resolutionNote: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val payload = mapOf("resolutionNote" to resolutionNote)
            val res = repo.request<SupportTicketSingleResponse> {
                patch("$SUPPORT_TICKET_BASE_URL/$ticketId/resolve", payload)
            }
            res.fold(
                onSuccess = {
                    if (it.success && it.data != null) {
                        _selectedSupportTicket.value = it.data
                        DynamicIslandManager.showSuccess("Ticket resolved")
                        onSuccess()
                    }
                },
                onFailure = { DynamicIslandManager.showError(it.message ?: "Failed to resolve ticket") }
            )
            _isLoading.value = false
        }
    }
    // =========================================================================
    // 15. STRING & DATE FORMATTING UTILITIES
    // =========================================================================

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