@file:Suppress("unused")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.it.AssetDashboardResponse
import com.cuso.tailor.model.it.AssetDetailDto
import com.cuso.tailor.model.it.AssetDetailResponse
import com.cuso.tailor.model.it.AssetItemDto
import com.cuso.tailor.model.it.AssetMetricsDto
import com.cuso.tailor.model.it.AssignAssetRequest
import com.cuso.tailor.model.it.AssignAssetResponse
import com.cuso.tailor.model.it.BackupDashboardResponse
import com.cuso.tailor.model.it.BackupDetailResponse
import com.cuso.tailor.model.it.BackupJobItemDto
import com.cuso.tailor.model.it.BackupMetricsDto
import com.cuso.tailor.model.it.BackupSnapshotDetailDto
import com.cuso.tailor.model.it.DeviceDetailResponse
import com.cuso.tailor.model.it.DeviceItemDto
import com.cuso.tailor.model.it.DeviceListResponse
import com.cuso.tailor.model.it.DeviceMetricsDto
import com.cuso.tailor.model.it.ExecuteRestoreBackupRequest
import com.cuso.tailor.model.it.ExecuteRestoreBackupResponse
import com.cuso.tailor.model.it.ItDashboardData
import com.cuso.tailor.model.it.ItDashboardResponse
import com.cuso.tailor.model.it.MarkDisposalRequest
import com.cuso.tailor.model.it.MarkDisposalResponse
import com.cuso.tailor.model.it.OrgMemberDto
import com.cuso.tailor.model.it.OrgMembersResponse
import com.cuso.tailor.model.it.PosTerminalDetailResponse
import com.cuso.tailor.model.it.PosTerminalItemDto
import com.cuso.tailor.model.it.PosTerminalListResponse
import com.cuso.tailor.model.it.PosTerminalMetricsDto
import com.cuso.tailor.model.it.RegisterAssetRequest
import com.cuso.tailor.model.it.RegisterAssetResponse
import com.cuso.tailor.model.it.ReportPosIssueRequest
import com.cuso.tailor.model.it.ReportPosIssueResponse
import com.cuso.tailor.model.it.RestartPosTerminalResponse
import com.cuso.tailor.model.it.RestoreAdvancedOptionsDto
import com.cuso.tailor.model.it.TicketDashboardResponse
import com.cuso.tailor.model.it.TicketDetailDto
import com.cuso.tailor.model.it.TicketItemDto
import com.cuso.tailor.model.it.*
import com.cuso.tailor.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

private const val IT_DASHBOARD_URL = "/api/it/dashboard/master-overview"

private const val IT_DEVICE_LIST_URL = "/api/it/dashboard"
private const val IT_DEVICE_DETAIL_URL = "/api/it"

private const val IT_POS_TERMINAL_LIST_URL = "/api/it/pos-terminals/fleet"
private const val IT_POS_TERMINAL_DETAIL_URL = "/api/it/pos-terminals"
private const val IT_POS_URL = "/api/it/pos-terminals"

private const val IT_BACKUP_DASHBOARD_URL = "/api/it/backups/dashboard"
private const val IT_BACKUP_SNAPSHOT_DETAIL_URL = "/api/it/backups/snapshots"
private const val IT_BACKUP_RESTORE_URL = "/api/it/backups/restore"

// --- Asset Management Endpoints ---
private const val IT_ASSET_DASHBOARD_URL = "/api/it/assets/dashboard"
private const val IT_ASSET_DETAIL_URL = "/api/it/assets"

private const val ALL_USERS_URL = "/api/users/view-all"

// --- IT Service Desk Endpoints ---
private const val IT_TICKET_DASHBOARD_URL = "/api/it/tickets/dashboard"
private const val IT_TICKETS_URL = "/api/it/tickets"


@HiltViewModel
class ItViewModel @Inject constructor(
    private val repo: ApiRepository
) : ViewModel() {

    private val _dashboardData = MutableStateFlow<ItDashboardData?>(null)
    val dashboardData: StateFlow<ItDashboardData?> = _dashboardData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchDashboard()
    }

    private val _orgMembers = MutableStateFlow<List<OrgMemberDto>>(emptyList())
    val orgMembers: StateFlow<List<OrgMemberDto>> = _orgMembers.asStateFlow()

    private val _isLoadingMembers = MutableStateFlow(false)
    val isLoadingMembers: StateFlow<Boolean> = _isLoadingMembers.asStateFlow()

    /**
     * Fetches all organization members for assignment dropdown.
     */
    fun fetchUsersForAssignment() {
        viewModelScope.launch {
            _isLoadingMembers.value = true
            val result = repo.request<OrgMembersResponse> {
                get(ALL_USERS_URL)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _orgMembers.value = res.data
                    }
                },
                onFailure = {
                    // Ignore or log error
                }
            )
            _isLoadingMembers.value = false
        }
    }

    /**
     * Fetches IT Dashboard data from backend endpoint.
     */
    fun fetchDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repo.request<ItDashboardResponse> {
                get(IT_DASHBOARD_URL)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _dashboardData.value = res.data
                    } else {
                        _errorMessage.value = res.message ?: "Failed to load IT Dashboard data"
                    }
                },
                onFailure = { err ->
                    _errorMessage.value = err.message ?: "Network error fetching IT Dashboard"
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * Calculates time elapsed for the header badge.
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

    private val _devicesList = MutableStateFlow<List<DeviceItemDto>>(emptyList())
    val devicesList: StateFlow<List<DeviceItemDto>> = _devicesList.asStateFlow()

    private val _deviceMetrics = MutableStateFlow<DeviceMetricsDto?>(null)
    val deviceMetrics: StateFlow<DeviceMetricsDto?> = _deviceMetrics.asStateFlow()

    private val _deviceSearchQuery = MutableStateFlow("")
    val deviceSearchQuery: StateFlow<String> = _deviceSearchQuery.asStateFlow()

    private val _selectedDeviceStatus = MutableStateFlow<String?>(null)
    val selectedDeviceStatus: StateFlow<String?> = _selectedDeviceStatus.asStateFlow()

    private val _isLoadingDevices = MutableStateFlow(false)
    val isLoadingDevices: StateFlow<Boolean> = _isLoadingDevices.asStateFlow()

    private val _devicesError = MutableStateFlow<String?>(null)
    val devicesError: StateFlow<String?> = _devicesError.asStateFlow()

    // --- States for Single Device Detail ---
    private val _deviceDetail = MutableStateFlow<DeviceItemDto?>(null)
    val deviceDetail: StateFlow<DeviceItemDto?> = _deviceDetail.asStateFlow()

    private val _isLoadingDeviceDetail = MutableStateFlow(false)
    val isLoadingDeviceDetail: StateFlow<Boolean> = _isLoadingDeviceDetail.asStateFlow()

    private val _deviceDetailError = MutableStateFlow<String?>(null)
    val deviceDetailError: StateFlow<String?> = _deviceDetailError.asStateFlow()

    /**
     * Filtered reactive list for All Devices based on search and status filter.
     */
    val filteredDevices: StateFlow<List<DeviceItemDto>> = combine(
        _devicesList,
        _deviceSearchQuery,
        _selectedDeviceStatus
    ) { list, query, status ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.deviceName.lowercase().contains(cleanQuery) ||
                    item.deviceNo.lowercase().contains(cleanQuery) ||
                    item.serialNumber.lowercase().contains(cleanQuery) ||
                    item.deviceType.lowercase().contains(cleanQuery) ||
                    (item.branchName?.lowercase()?.contains(cleanQuery) == true) ||
                    (item.assignedTo?.name?.lowercase()?.contains(cleanQuery) == true)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDeviceSearchQueryChange(query: String) {
        _deviceSearchQuery.value = query
    }

    fun onDeviceStatusFilterChange(status: String?) {
        _selectedDeviceStatus.value = status
    }

    fun clearDeviceFilters() {
        _deviceSearchQuery.value = ""
        _selectedDeviceStatus.value = null
    }

    /**
     * Fetches all devices and top summary metrics.
     */
    fun fetchDevicesList(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingDevices.value = true
            _devicesError.value = null

            val result = repo.request<DeviceListResponse> {
                get(IT_DEVICE_LIST_URL, repo.query("page" to page, "limit" to limit))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _devicesList.value = res.devices
                        _deviceMetrics.value = res.metrics
                    } else {
                        _devicesError.value = res.message ?: "Failed to load devices"
                    }
                },
                onFailure = { err ->
                    _devicesError.value = err.message ?: "Network error fetching devices"
                }
            )
            _isLoadingDevices.value = false
        }
    }

    /**
     * Fetches single device details by ID.
     */
    fun fetchDeviceDetail(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isLoadingDeviceDetail.value = true
            _deviceDetailError.value = null

            val result = repo.request<DeviceDetailResponse> {
                get("$IT_DEVICE_DETAIL_URL/$id")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _deviceDetail.value = res.data
                    } else {
                        _deviceDetailError.value = res.message ?: "Failed to load device details"
                    }
                },
                onFailure = { err ->
                    _deviceDetailError.value = err.message ?: "Network error fetching device details"
                }
            )
            _isLoadingDeviceDetail.value = false
        }
    }

    fun clearDeviceDetail() {
        _deviceDetail.value = null
        _deviceDetailError.value = null
    }

    private val _posTerminalsList = MutableStateFlow<List<PosTerminalItemDto>>(emptyList())
    val posTerminalsList: StateFlow<List<PosTerminalItemDto>> = _posTerminalsList.asStateFlow()

    private val _posTerminalMetrics = MutableStateFlow<PosTerminalMetricsDto?>(null)
    val posTerminalMetrics: StateFlow<PosTerminalMetricsDto?> = _posTerminalMetrics.asStateFlow()

    private val _posSearchQuery = MutableStateFlow("")
    val posSearchQuery: StateFlow<String> = _posSearchQuery.asStateFlow()

    private val _selectedPosStatus = MutableStateFlow<String?>(null)
    val selectedPosStatus: StateFlow<String?> = _selectedPosStatus.asStateFlow()

    private val _isLoadingPosTerminals = MutableStateFlow(false)
    val isLoadingPosTerminals: StateFlow<Boolean> = _isLoadingPosTerminals.asStateFlow()

    private val _posTerminalsError = MutableStateFlow<String?>(null)
    val posTerminalsError: StateFlow<String?> = _posTerminalsError.asStateFlow()

    // --- Single POS Detail State ---
    private val _posTerminalDetail = MutableStateFlow<PosTerminalItemDto?>(null)
    val posTerminalDetail: StateFlow<PosTerminalItemDto?> = _posTerminalDetail.asStateFlow()

    private val _isLoadingPosDetail = MutableStateFlow(false)
    val isLoadingPosDetail: StateFlow<Boolean> = _isLoadingPosDetail.asStateFlow()

    private val _posDetailError = MutableStateFlow<String?>(null)
    val posDetailError: StateFlow<String?> = _posDetailError.asStateFlow()

    /**
     * Filtered reactive list for POS Terminals.
     */
    val filteredPosTerminals: StateFlow<List<PosTerminalItemDto>> = combine(
        _posTerminalsList,
        _posSearchQuery,
        _selectedPosStatus
    ) { list, query, status ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.posId.lowercase().contains(cleanQuery) ||
                    item.terminalName.lowercase().contains(cleanQuery) ||
                    (item.branchName?.lowercase()?.contains(cleanQuery) == true) ||
                    (item.counterName?.lowercase()?.contains(cleanQuery) == true) ||
                    (item.assignedToClerk?.lowercase()?.contains(cleanQuery) == true)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPosSearchQueryChange(query: String) {
        _posSearchQuery.value = query
    }

    fun onPosStatusFilterChange(status: String?) {
        _selectedPosStatus.value = status
    }

    fun clearPosFilters() {
        _posSearchQuery.value = ""
        _selectedPosStatus.value = null
    }

    /**
     * Fetches all POS Terminals and summary metrics.
     */
    fun fetchPosTerminalsList(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingPosTerminals.value = true
            _posTerminalsError.value = null

            val result = repo.request<PosTerminalListResponse> {
                get(IT_POS_TERMINAL_LIST_URL, repo.query("page" to page, "limit" to limit))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _posTerminalsList.value = res.terminals
                        _posTerminalMetrics.value = res.metrics
                    } else {
                        _posTerminalsError.value = res.message ?: "Failed to load POS terminals"
                    }
                },
                onFailure = { err ->
                    _posTerminalsError.value = err.message ?: "Network error fetching POS terminals"
                }
            )
            _isLoadingPosTerminals.value = false
        }
    }

    /**
     * Fetches single POS Terminal details by ID.
     */
    fun fetchPosTerminalDetail(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isLoadingPosDetail.value = true
            _posDetailError.value = null

            val result = repo.request<PosTerminalDetailResponse> {
                get("$IT_POS_TERMINAL_DETAIL_URL/$id")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _posTerminalDetail.value = res.data
                    } else {
                        _posDetailError.value = res.message ?: "Failed to load terminal details"
                    }
                },
                onFailure = { err ->
                    _posDetailError.value = err.message ?: "Network error fetching terminal details"
                }
            )
            _isLoadingPosDetail.value = false
        }
    }

    private val _isReportingIssue = MutableStateFlow(false)
    val isReportingIssue: StateFlow<Boolean> = _isReportingIssue.asStateFlow()

    private val _isRestartingTerminal = MutableStateFlow(false)
    val isRestartingTerminal: StateFlow<Boolean> = _isRestartingTerminal.asStateFlow()

    /**
     * Reports an issue for the selected POS Terminal.
     * Body: { "issueType": "..." }
     */
    fun reportPosTerminalIssue(
        terminalId: String,
        issueType: String,
        onSuccess: (message: String) -> Unit = {},
        onError: (message: String) -> Unit = {}
    ) {
        if (terminalId.isBlank() || issueType.isBlank()) return
        viewModelScope.launch {
            _isReportingIssue.value = true

            val requestBody = ReportPosIssueRequest(issueType = issueType.trim())
            val result = repo.request<ReportPosIssueResponse> {
                post("$IT_POS_URL/$terminalId/report-issue", requestBody)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.terminal?.let { _posTerminalDetail.value = it } ?: fetchPosTerminalDetail(terminalId)
                        fetchPosTerminalsList()
                        onSuccess(res.message ?: "Incident Ticket created successfully!")
                    } else {
                        onError(res.message ?: "Failed to report issue")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Network error reporting issue")
                }
            )
            _isReportingIssue.value = false
        }
    }

    /**
     * Dispatches remote restart/reboot command to the POS Terminal.
     */
    fun restartPosTerminal(
        terminalId: String,
        onSuccess: (message: String) -> Unit = {},
        onError: (message: String) -> Unit = {}
    ) {
        if (terminalId.isBlank()) return
        viewModelScope.launch {
            _isRestartingTerminal.value = true

            val result = repo.request<RestartPosTerminalResponse> {
                post("$IT_POS_URL/$terminalId/restart", emptyMap<String, String>())
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.terminal?.let { _posTerminalDetail.value = it } ?: fetchPosTerminalDetail(terminalId)
                        fetchPosTerminalsList()
                        onSuccess(res.message ?: "Remote reboot command dispatched successfully!")
                    } else {
                        onError(res.message ?: "Failed to restart terminal")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Network error restarting terminal")
                }
            )
            _isRestartingTerminal.value = false
        }
    }

    fun clearPosTerminalDetail() {
        _posTerminalDetail.value = null
        _posDetailError.value = null
    }

    // Backup List & Dashboard States
    private val _backupJobsList = MutableStateFlow<List<BackupJobItemDto>>(emptyList())
    val backupJobsList: StateFlow<List<BackupJobItemDto>> = _backupJobsList.asStateFlow()

    private val _backupMetrics = MutableStateFlow<BackupMetricsDto?>(null)
    val backupMetrics: StateFlow<BackupMetricsDto?> = _backupMetrics.asStateFlow()

    private val _backupSearchQuery = MutableStateFlow("")
    val backupSearchQuery: StateFlow<String> = _backupSearchQuery.asStateFlow()

    private val _selectedBackupStatus = MutableStateFlow<String?>(null)
    val selectedBackupStatus: StateFlow<String?> = _selectedBackupStatus.asStateFlow()

    private val _isLoadingBackups = MutableStateFlow(false)
    val isLoadingBackups: StateFlow<Boolean> = _isLoadingBackups.asStateFlow()

    private val _backupsError = MutableStateFlow<String?>(null)
    val backupsError: StateFlow<String?> = _backupsError.asStateFlow()

    // Backup Snapshot Detail States
    private val _backupDetail = MutableStateFlow<BackupSnapshotDetailDto?>(null)
    val backupDetail: StateFlow<BackupSnapshotDetailDto?> = _backupDetail.asStateFlow()

    private val _isLoadingBackupDetail = MutableStateFlow(false)
    val isLoadingBackupDetail: StateFlow<Boolean> = _isLoadingBackupDetail.asStateFlow()

    private val _backupDetailError = MutableStateFlow<String?>(null)
    val backupDetailError: StateFlow<String?> = _backupDetailError.asStateFlow()

    /**
     * Reactive filtered backup jobs based on search query and status filter.
     */
    val filteredBackupJobs: StateFlow<List<BackupJobItemDto>> = combine(
        _backupJobsList,
        _backupSearchQuery,
        _selectedBackupStatus
    ) { list, query, status ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.jobName.lowercase().contains(cleanQuery) ||
                    (item.sourceEntity?.lowercase()?.contains(cleanQuery) == true) ||
                    (item.backupType?.lowercase()?.contains(cleanQuery) == true) ||
                    (item.environment?.lowercase()?.contains(cleanQuery) == true)

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

    /**
     * Fetches backup dashboard metrics and job list from backend.
     */
    fun fetchBackupDashboard(page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            _isLoadingBackups.value = true
            _backupsError.value = null

            val result = repo.request<BackupDashboardResponse> {
                get(IT_BACKUP_DASHBOARD_URL, repo.query("page" to page, "limit" to limit))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _backupJobsList.value = res.jobs
                        _backupMetrics.value = res.metrics
                    } else {
                        _backupsError.value = res.message ?: "Failed to load backup dashboard data"
                    }
                },
                onFailure = { err ->
                    _backupsError.value = err.message ?: "Network error fetching backup data"
                }
            )
            _isLoadingBackups.value = false
        }
    }

    /**
     * Fetches single backup snapshot detail by snapshot ID or Job ID.
     */
    fun fetchBackupSnapshotDetail(snapshotId: String) {
        if (snapshotId.isBlank()) return
        viewModelScope.launch {
            _isLoadingBackupDetail.value = true
            _backupDetailError.value = null

            val result = repo.request<BackupDetailResponse> {
                get("$IT_BACKUP_SNAPSHOT_DETAIL_URL/$snapshotId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _backupDetail.value = res.data
                    } else {
                        _backupDetailError.value = res.message ?: "Failed to load backup snapshot detail"
                    }
                },
                onFailure = { err ->
                    _backupDetailError.value = err.message ?: "Network error fetching backup detail"
                }
            )
            _isLoadingBackupDetail.value = false
        }
    }

    private val _isExecutingRestore = MutableStateFlow(false)
    val isExecutingRestore: StateFlow<Boolean> = _isExecutingRestore.asStateFlow()

    private val _restoreExecutionError = MutableStateFlow<String?>(null)
    val restoreExecutionError: StateFlow<String?> = _restoreExecutionError.asStateFlow()

    /**
     * Executes the restore pipeline by dispatching the restore request payload.
     */
    fun executeRestoreBackup(
        backupId: String,
        restoreDestination: String,
        restoreType: String,
        verifyIntegrity: Boolean,
        createSafetySnapshot: Boolean,
        userAcknowledgedOverwrite: Boolean,
        onSuccess: (restoreId: String, message: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        if (backupId.isBlank()) {
            onError("Missing backup snapshot identifier")
            return
        }

        viewModelScope.launch {
            _isExecutingRestore.value = true
            _restoreExecutionError.value = null

            val requestBody = ExecuteRestoreBackupRequest(
                backupId = backupId,
                restoreDestination = restoreDestination,
                restoreType = restoreType,
                selectedSchemas = emptyList(),
                advancedOptions = RestoreAdvancedOptionsDto(
                    verifyIntegrityAfterRestore = verifyIntegrity,
                    createSafetySnapshot = createSafetySnapshot
                ),
                userAcknowledgedOverwrite = userAcknowledgedOverwrite
            )

            val result = repo.request<ExecuteRestoreBackupResponse> {
                post(IT_BACKUP_RESTORE_URL, requestBody)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        onSuccess(
                            res.restoreId ?: "RST-QUEUED",
                            res.message ?: "Restore pipeline queued successfully."
                        )
                    } else {
                        val errMsg = res.message ?: "Failed to queue restore process"
                        _restoreExecutionError.value = errMsg
                        onError(errMsg)
                    }
                },
                onFailure = { err ->
                    val errMsg = err.message ?: "Network error triggering backup restore"
                    _restoreExecutionError.value = errMsg
                    onError(errMsg)
                }
            )
            _isExecutingRestore.value = false
        }
    }

    fun clearBackupSnapshotDetail() {
        _backupDetail.value = null
        _backupDetailError.value = null
    }

    /**
     * Formats ISO timestamp to short human-readable time (e.g. "12:42 PM" or "Today, 12:42 PM").
     */
    fun formatBackupTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString) ?: return isoString
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            "Today, ${formatter.format(date)}"
        } catch (_: Exception) {
            isoString
        }
    }

    // --- States for Asset Dashboard & List ---
    private val _assetList = MutableStateFlow<List<AssetItemDto>>(emptyList())
    val assetList: StateFlow<List<AssetItemDto>> = _assetList.asStateFlow()

    private val _assetMetrics = MutableStateFlow<AssetMetricsDto?>(null)
    val assetMetrics: StateFlow<AssetMetricsDto?> = _assetMetrics.asStateFlow()

    private val _assetSearchQuery = MutableStateFlow("")
    val assetSearchQuery: StateFlow<String> = _assetSearchQuery.asStateFlow()

    private val _selectedAssetStatus = MutableStateFlow<String?>(null)
    val selectedAssetStatus: StateFlow<String?> = _selectedAssetStatus.asStateFlow()

    private val _selectedAssetType = MutableStateFlow<String?>(null)
    val selectedAssetType: StateFlow<String?> = _selectedAssetType.asStateFlow()

    private val _isLoadingAssets = MutableStateFlow(false)
    val isLoadingAssets: StateFlow<Boolean> = _isLoadingAssets.asStateFlow()

    private val _assetError = MutableStateFlow<String?>(null)
    val assetError: StateFlow<String?> = _assetError.asStateFlow()

    // --- Single Asset Detail State ---
    private val _assetDetail = MutableStateFlow<AssetDetailDto?>(null)
    val assetDetail: StateFlow<AssetDetailDto?> = _assetDetail.asStateFlow()

    private val _isLoadingAssetDetail = MutableStateFlow(false)
    val isLoadingAssetDetail: StateFlow<Boolean> = _isLoadingAssetDetail.asStateFlow()

    private val _assetDetailError = MutableStateFlow<String?>(null)
    val assetDetailError: StateFlow<String?> = _assetDetailError.asStateFlow()

    // --- Filtered Assets reactive flow ---
    val filteredAssets: StateFlow<List<AssetItemDto>> = combine(
        _assetList,
        _assetSearchQuery,
        _selectedAssetStatus,
        _selectedAssetType
    ) { list, query, status, type ->
        val clean = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = clean.isEmpty() ||
                    item.assetName.lowercase().contains(clean) ||
                    item.assetId.lowercase().contains(clean) ||
                    (item.serialOrLicense?.lowercase()?.contains(clean) == true) ||
                    (item.location?.lowercase()?.contains(clean) == true) ||
                    (item.assignedTo?.lowercase()?.contains(clean) == true)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)
            val matchesType = type.isNullOrBlank() || item.type.equals(type, ignoreCase = true)

            matchesQuery && matchesStatus && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onAssetSearchQueryChange(query: String) {
        _assetSearchQuery.value = query
    }

    fun onAssetStatusFilterChange(status: String?) {
        _selectedAssetStatus.value = status
    }

    fun onAssetTypeFilterChange(type: String?) {
        _selectedAssetType.value = type
    }

    fun clearAssetFilters() {
        _assetSearchQuery.value = ""
        _selectedAssetStatus.value = null
        _selectedAssetType.value = null
    }

    fun fetchAssetDashboard(page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            _isLoadingAssets.value = true
            _assetError.value = null

            val result = repo.request<AssetDashboardResponse> {
                get(IT_ASSET_DASHBOARD_URL, repo.query("page" to page, "limit" to limit))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _assetList.value = res.assets
                        _assetMetrics.value = res.metrics
                    } else {
                        _assetError.value = res.message ?: "Failed to load IT assets"
                    }
                },
                onFailure = { err ->
                    _assetError.value = err.message ?: "Network error loading IT assets"
                }
            )
            _isLoadingAssets.value = false
        }
    }

    fun fetchAssetDetail(assetId: String) {
        if (assetId.isBlank()) return
        viewModelScope.launch {
            _isLoadingAssetDetail.value = true
            _assetDetailError.value = null

            val result = repo.request<AssetDetailResponse> {
                get("$IT_ASSET_DETAIL_URL/$assetId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _assetDetail.value = res.data
                    } else {
                        _assetDetailError.value = res.message ?: "Failed to load asset details"
                    }
                },
                onFailure = { err ->
                    _assetDetailError.value = err.message ?: "Network error loading asset details"
                }
            )
            _isLoadingAssetDetail.value = false
        }
    }

    fun registerAsset(
        request: RegisterAssetRequest,
        onSuccess: (message: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoadingAssets.value = true
            val result = repo.request<RegisterAssetResponse> {
                post("$IT_ASSET_DETAIL_URL/register", request)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchAssetDashboard()
                        onSuccess(res.message ?: "Asset registered successfully")
                    } else {
                        onError(res.message ?: "Failed to register asset")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Network error registering asset")
                }
            )
            _isLoadingAssets.value = false
        }
    }

    fun markAssetForDisposal(
        assetId: String,
        reason: String,
        onSuccess: (message: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (assetId.isBlank()) return
        viewModelScope.launch {
            val result = repo.request<MarkDisposalResponse> {
                patch("$IT_ASSET_DETAIL_URL/$assetId/disposal", MarkDisposalRequest(reason))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.asset?.let { _assetDetail.value = it } ?: fetchAssetDetail(assetId)
                        fetchAssetDashboard()
                        onSuccess(res.message ?: "Asset marked for disposal")
                    } else {
                        onError(res.message ?: "Failed to mark asset for disposal")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Network error marking for disposal")
                }
            )
        }
    }

    fun assignAsset(
        assetId: String,
        request: AssignAssetRequest,
        onSuccess: (message: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (assetId.isBlank()) return
        viewModelScope.launch {
            val result = repo.request<AssignAssetResponse> {
                put("$IT_ASSET_DETAIL_URL/$assetId/assignment", request)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.asset?.let { _assetDetail.value = it } ?: fetchAssetDetail(assetId)
                        fetchAssetDashboard()
                        onSuccess(res.message ?: "Asset assignment updated")
                    } else {
                        onError(res.message ?: "Failed to update assignment")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Network error updating assignment")
                }
            )
        }
    }

    // --- Service Desk States ---
    private val _ticketList = MutableStateFlow<List<TicketItemDto>>(emptyList())
    val ticketList: StateFlow<List<TicketItemDto>> = _ticketList.asStateFlow()

    private val _ticketMetrics = MutableStateFlow<TicketMetricsDto?>(null)
    val ticketMetrics: StateFlow<TicketMetricsDto?> = _ticketMetrics.asStateFlow()

    private val _ticketSearchQuery = MutableStateFlow("")
    val ticketSearchQuery: StateFlow<String> = _ticketSearchQuery.asStateFlow()

    private val _selectedTicketStatus = MutableStateFlow<String?>(null)
    val selectedTicketStatus: StateFlow<String?> = _selectedTicketStatus.asStateFlow()

    private val _selectedTicketPriority = MutableStateFlow<String?>(null)
    val selectedTicketPriority: StateFlow<String?> = _selectedTicketPriority.asStateFlow()

    private val _selectedTicketCategory = MutableStateFlow<String?>(null)
    val selectedTicketCategory: StateFlow<String?> = _selectedTicketCategory.asStateFlow()

    private val _isLoadingTickets = MutableStateFlow(false)
    val isLoadingTickets: StateFlow<Boolean> = _isLoadingTickets.asStateFlow()

    private val _ticketDetail = MutableStateFlow<TicketDetailDto?>(null)
    val ticketDetail: StateFlow<TicketDetailDto?> = _ticketDetail.asStateFlow()

    private val _isLoadingTicketDetail = MutableStateFlow(false)
    val isLoadingTicketDetail: StateFlow<Boolean> = _isLoadingTicketDetail.asStateFlow()

    val filteredTickets: StateFlow<List<TicketItemDto>> = combine(
        _ticketList,
        _ticketSearchQuery,
        _selectedTicketStatus,
        _selectedTicketPriority,
        _selectedTicketCategory
    ) { list, query, status, priority, category ->
        val clean = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = clean.isEmpty() ||
                    item.ticketId.lowercase().contains(clean) ||
                    item.subject.lowercase().contains(clean) ||
                    item.requester.lowercase().contains(clean) ||
                    item.branch.lowercase().contains(clean) ||
                    item.assignee.lowercase().contains(clean)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)
            val matchesPriority = priority.isNullOrBlank() || item.priority.equals(priority, ignoreCase = true)
            val matchesCategory = category.isNullOrBlank() || item.category.equals(category, ignoreCase = true)

            matchesQuery && matchesStatus && matchesPriority && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTicketSearchQueryChange(query: String) { _ticketSearchQuery.value = query }
    fun onTicketStatusFilterChange(status: String?) { _selectedTicketStatus.value = status }
    fun onTicketPriorityFilterChange(priority: String?) { _selectedTicketPriority.value = priority }
    fun onTicketCategoryFilterChange(category: String?) { _selectedTicketCategory.value = category }

    fun clearTicketFilters() {
        _ticketSearchQuery.value = ""
        _selectedTicketStatus.value = null
        _selectedTicketPriority.value = null
        _selectedTicketCategory.value = null
    }

    fun fetchTicketDashboard(page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            _isLoadingTickets.value = true
            val result = repo.request<TicketDashboardResponse> {
                get(IT_TICKET_DASHBOARD_URL, repo.query("page" to page, "limit" to limit))
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _ticketList.value = res.tickets
                        _ticketMetrics.value = res.metrics
                    }
                },
                onFailure = { /* Handle error */ }
            )
            _isLoadingTickets.value = false
        }
    }

    fun fetchTicketDetail(ticketId: String) {
        if (ticketId.isBlank()) return
        viewModelScope.launch {
            _isLoadingTicketDetail.value = true
            val result = repo.request<TicketDetailResponse> {
                get("$IT_TICKETS_URL/$ticketId")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _ticketDetail.value = res.data ?: res.ticket
                    }
                },
                onFailure = { /* Handle error */ }
            )
            _isLoadingTicketDetail.value = false
        }
    }

    fun createTicket(
        request: CreateTicketRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoadingTickets.value = true
            val result = repo.request<TicketDetailResponse> {
                post(IT_TICKETS_URL, request)
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchTicketDashboard()
                        onSuccess(res.message ?: "Ticket created successfully")
                    } else {
                        onError(res.message ?: "Failed to create ticket")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Network error creating ticket")
                }
            )
            _isLoadingTickets.value = false
        }
    }

    fun updateTicketState(
        ticketId: String,
        request: UpdateTicketStateRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (ticketId.isBlank()) return
        viewModelScope.launch {
            val result = repo.request<TicketDetailResponse> {
                patch("$IT_TICKETS_URL/$ticketId/state", request)
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.ticket?.let { _ticketDetail.value = it } ?: fetchTicketDetail(ticketId)
                        fetchTicketDashboard()
                        onSuccess(res.message ?: "Ticket state updated successfully.")
                    } else {
                        onError(res.message ?: "Failed to update ticket state")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Network error updating ticket state")
                }
            )
        }
    }

    fun postTicketConversation(
        ticketId: String,
        message: String,
        isInternalNote: Boolean,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (ticketId.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            val result = repo.request<TicketDetailResponse> {
                post(
                    "$IT_TICKETS_URL/$ticketId/conversation",
                    PostTicketMessageRequest(message = message.trim(), isInternalNote = isInternalNote)
                )
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        res.ticket?.let { _ticketDetail.value = it } ?: fetchTicketDetail(ticketId)
                        onSuccess(res.message ?: "Message posted successfully.")
                    } else {
                        onError(res.message ?: "Failed to post message")
                    }
                },
                onFailure = { err ->
                    onError(err.message ?: "Network error posting message")
                }
            )
        }
    }

    /**
     * Formats next scheduled time string.
     */
    fun formatNextScheduled(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Tomorrow, 01:00 AM"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString) ?: return isoString
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            "Tomorrow, ${formatter.format(date)}"
        } catch (_: Exception) {
            isoString
        }
    }

    /**
     * Helper to format ISO date strings into readable text.
     */
    fun formatDeviceDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString)
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: isoString
        } catch (_: Exception) {
            isoString
        }
    }
}