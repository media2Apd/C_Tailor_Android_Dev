package com.cuso.tailor.model.it

import com.google.gson.annotations.SerializedName

/**
 * Top-level response for POS Billing List / Terminal Fleet Status.
 */
data class PosTerminalListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("metrics") val metrics: PosTerminalMetricsDto? = null,
    @SerializedName("terminals") val terminals: List<PosTerminalItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: PosTerminalPaginationDto? = null,
    @SerializedName("message") val message: String? = null
)

/**
 * Top-level response for single POS Terminal details.
 */
data class PosTerminalDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: PosTerminalItemDto? = null,
    @SerializedName("message") val message: String? = null
)

data class PosTerminalMetricsDto(
    @SerializedName("totalTerminals") val totalTerminals: Int = 0,
    @SerializedName("online") val online: PosTerminalOnlineMetricDto? = null,
    @SerializedName("offline") val offline: Int = 0,
    @SerializedName("paymentGateways") val paymentGateways: PosGatewayMetricDto? = null,
    @SerializedName("printerIssues") val printerIssues: Int = 0,
    @SerializedName("totalActiveIssues") val totalActiveIssues: Int = 0
)

data class PosTerminalOnlineMetricDto(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("percentage") val percentage: String = "0%"
)

data class PosGatewayMetricDto(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("badge") val badge: String = "Active"
)

data class PosTerminalPaginationDto(
    @SerializedName("currentPage") val currentPage: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("totalRecords") val totalRecords: Int = 0
)

data class PosTerminalItemDto(
    @SerializedName("_id") val mongoId: String = "",
    @SerializedName("id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("posId") val posId: String = "",
    @SerializedName("terminalId") val terminalId: String = "",
    @SerializedName("terminalName") val terminalName: String = "",
    @SerializedName("storeCode") val storeCode: String? = null,
    @SerializedName("branchName") val branchName: String? = null,
    @SerializedName("counterName") val counterName: String? = null,
    @SerializedName("assignedToClerk") val assignedToClerk: String? = null,
    @SerializedName("status") val status: String = "Online",
    @SerializedName("printerStatus") val printerStatus: String? = "READY",
    @SerializedName("activeIssue") val activeIssue: String? = null,
    @SerializedName("lastActivity") val lastActivity: String? = null,
    @SerializedName("lastActivityFormatted") val lastActivityFormatted: String? = null,
    @SerializedName("lastSync") val lastSync: String? = null,
    @SerializedName("lastSyncFormatted") val lastSyncFormatted: String? = null,
    @SerializedName("paymentGatewayStatus") val paymentGatewayStatus: String? = null,
    @SerializedName("hardwareSoftware") val hardwareSoftware: PosHardwareSoftwareDto? = null,
    @SerializedName("ipAddress") val ipAddress: String? = "192.168.1.45",
    @SerializedName("scannerStatus") val scannerStatus: String? = "Ready",
    @SerializedName("cashDrawerStatus") val cashDrawerStatus: String? = "Closed"
) {
    val effectiveId: String
        get() = id.ifBlank { mongoId }
}

data class PosHardwareSoftwareDto(
    @SerializedName("deviceModel") val deviceModel: String? = null,
    @SerializedName("os") val os: String? = null,
    @SerializedName("posVersion") val posVersion: String? = null
)

/**
 * Payload for reporting an issue on a POS terminal.
 * Body: { "issueType": "Paper Jam" }
 */
data class ReportPosIssueRequest(
    @SerializedName("issueType") val issueType: String
)

/**
 * API Response when an issue is reported.
 */
data class ReportPosIssueResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("ticketId") val ticketId: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("terminal") val terminal: PosTerminalItemDto? = null
)

/**
 * API Response when restart/reboot command is dispatched.
 */
data class RestartPosTerminalResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("commandId") val commandId: String? = null,
    @SerializedName("terminal") val terminal: PosTerminalItemDto? = null
)