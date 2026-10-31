@file:Suppress("unused")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.cuso.tailor.model.it.PosTerminalDetailResponse
import com.cuso.tailor.model.it.PosTerminalItemDto
import com.cuso.tailor.model.it.PosTerminalListResponse
import com.cuso.tailor.model.it.PosTerminalMetricsDto
import com.cuso.tailor.model.it.ReportPosIssueRequest
import com.cuso.tailor.model.it.ReportPosIssueResponse
import com.cuso.tailor.model.it.RestartPosTerminalResponse
import com.cuso.tailor.model.it.RestoreAdvancedOptionsDto
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