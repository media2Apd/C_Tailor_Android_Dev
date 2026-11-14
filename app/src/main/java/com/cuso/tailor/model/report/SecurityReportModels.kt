package com.cuso.tailor.model.report

import com.google.gson.annotations.SerializedName

// =========================================================================
// 1. SECURITY INCIDENTS & ALERTS REPORT MODELS
// =========================================================================

data class SecurityIncidentsReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<SecurityIncidentReportItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: SecurityIncidentSummaryDto? = null
)

data class SecurityIncidentReportItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("alertCode") val alertCode: String = "",
    @SerializedName("eventType") val eventType: String = "",
    @SerializedName("isAlert") val isAlert: String = "No",
    @SerializedName("severity") val severity: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("user") val user: String = "",
    @SerializedName("userRiskLevel") val userRiskLevel: String = "",
    @SerializedName("ipAddress") val ipAddress: String = "",
    @SerializedName("location") val location: String = "",
    @SerializedName("userAgent") val userAgent: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class SecurityIncidentSummaryDto(
    @SerializedName("totalIncidents") val totalIncidents: Int = 0,
    @SerializedName("criticalHighSeverity") val criticalHighSeverity: Int = 0,
    @SerializedName("openUnresolved") val openUnresolved: Int = 0,
    @SerializedName("alertTriggered") val alertTriggered: Int = 0,
    @SerializedName("resolutionRate") val resolutionRate: String = "0%"
)

// =========================================================================
// 2. ACCESS & PERMISSION ACTIVITY REPORT MODELS
// =========================================================================

data class SecurityAccessReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<SecurityAccessReportItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: SecurityAccessSummaryDto? = null
)

data class SecurityAccessReportItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("eventType") val eventType: String = "",
    @SerializedName("user") val user: String = "",
    @SerializedName("ipAddress") val ipAddress: String = "",
    @SerializedName("location") val location: String = "",
    @SerializedName("severity") val severity: String = "",
    @SerializedName("statusBadge") val statusBadge: String = "",
    @SerializedName("isSuspicious") val isSuspicious: Boolean = false,
    @SerializedName("deviceAgent") val deviceAgent: String = "",
    @SerializedName("actionDetail") val actionDetail: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class SecurityAccessSummaryDto(
    @SerializedName("totalAccessEvents") val totalAccessEvents: Int = 0,
    @SerializedName("successfulLogins") val successfulLogins: Int = 0,
    @SerializedName("failedLogins") val failedLogins: Int = 0,
    @SerializedName("unauthorizedBreaches") val unauthorizedBreaches: Int = 0
)

// =========================================================================
// 3. API & INTEGRATION SECURITY REPORT MODELS
// =========================================================================

data class SecurityApiIntegrationsReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<SecurityApiIntegrationReportItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: SecurityApiIntegrationSummaryDto? = null
)

data class SecurityApiIntegrationReportItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("authType") val authType: String = "",
    @SerializedName("baseUrl") val baseUrl: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("rateLimitUsage") val rateLimitUsage: String = "",
    @SerializedName("isRateLimitHigh") val isRateLimitHigh: Boolean = false,
    @SerializedName("totalDeliveries") val totalDeliveries: Int = 0,
    @SerializedName("failedDeliveries") val failedDeliveries: Int = 0,
    @SerializedName("deliverySuccessRate") val deliverySuccessRate: String = "0%",
    @SerializedName("latency") val latency: String = "0 ms",
    @SerializedName("lastSync") val lastSync: String? = null,
    @SerializedName("createdBy") val createdBy: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class SecurityApiIntegrationSummaryDto(
    @SerializedName("totalIntegrations") val totalIntegrations: Int = 0,
    @SerializedName("connectedActiveCount") val connectedActiveCount: Int = 0,
    @SerializedName("failedErrorCount") val failedErrorCount: Int = 0,
    @SerializedName("totalDeliveriesSent") val totalDeliveriesSent: Int = 0,
    @SerializedName("avgDeliverySuccessRate") val avgDeliverySuccessRate: String = "0%",
    @SerializedName("avgLatency") val avgLatency: String = "0ms"
)