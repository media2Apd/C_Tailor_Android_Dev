package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName

// ── 1. Summary Metrics ──
data class MetricValueDto(
    @SerializedName("value") val value: Long = 0,
    @SerializedName("changePercent") val changePercent: Double? = null,
    @SerializedName("trend") val trend: String? = null,
    @SerializedName("sparkline") val sparkline: List<Long> = emptyList()
)

data class SecurityDashboardSummaryData(
    @SerializedName("totalUsers") val totalUsers: MetricValueDto = MetricValueDto(),
    @SerializedName("activeUsers") val activeUsers: MetricValueDto = MetricValueDto(),
    @SerializedName("loginAttempts") val loginAttempts: MetricValueDto = MetricValueDto(),
    @SerializedName("failedLogins") val failedLogins: MetricValueDto = MetricValueDto(),
    @SerializedName("securityAlerts") val securityAlerts: MetricValueDto = MetricValueDto()
)

data class SecuritySummaryResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SecurityDashboardSummaryData? = null
)

// ── 2. Login Attempts Trend ──
data class LoginTrendPointDto(
    @SerializedName("successful") val successful: Long = 0,
    @SerializedName("failed") val failed: Long = 0,
    @SerializedName("period") val period: String = ""
)

data class LoginTrendData(
    @SerializedName("granularity") val granularity: String = "monthly",
    @SerializedName("points") val points: List<LoginTrendPointDto> = emptyList()
)

data class LoginTrendResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: LoginTrendData? = null
)

// ── 3. Security Health ──
data class SecurityHealthItemDto(
    @SerializedName("key") val key: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("percent") val percent: Int = 0,
    @SerializedName("status") val status: String = "low"
)

data class SecurityHealthData(
    @SerializedName("overall") val overall: Double = 0.0,
    @SerializedName("status") val status: String = "low",
    @SerializedName("items") val items: List<SecurityHealthItemDto> = emptyList()
)

data class SecurityHealthResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SecurityHealthData? = null
)

// ── 4. Users by Role ──
data class RoleDistributionItemDto(
    @SerializedName("count") val count: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("percent") val percent: Double = 0.0
)

data class UsersByRoleData(
    @SerializedName("total") val total: Long = 0,
    @SerializedName("roles") val roles: List<RoleDistributionItemDto> = emptyList()
)

data class UsersByRoleResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: UsersByRoleData? = null
)

// ── 5. Top Attack Types ──
data class AttackTypeItemDto(
    @SerializedName("type") val type: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("count") val count: Long = 0,
    @SerializedName("percent") val percent: Double = 0.0
)

data class TopAttackTypesData(
    @SerializedName("period") val period: String = "this_month",
    @SerializedName("total") val total: Long = 0,
    @SerializedName("items") val items: List<AttackTypeItemDto> = emptyList()
)

data class TopAttackTypesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TopAttackTypesData? = null
)

// ── 6. Recent Alerts ──
data class SecurityAlertItemDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("alertId") val alertId: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("severity") val severity: String = "low",
    @SerializedName("status") val status: String = "open",
    @SerializedName("location") val location: String = "",
    @SerializedName("ip") val ip: String = "",
    @SerializedName("createdAt") val createdAt: String = ""
)

data class RecentAlertsData(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 5,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("alerts") val alerts: List<SecurityAlertItemDto> = emptyList()
)

data class RecentAlertsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: RecentAlertsData? = null
)

// ── 7. Active Sessions ──
data class ActiveSessionItemDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("memberId") val memberId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("profilePicture") val profilePicture: String? = null,
    @SerializedName("role") val role: String = "",
    @SerializedName("ipAddress") val ipAddress: String = "",
    @SerializedName("location") val location: String = "",
    @SerializedName("device") val device: String = "",
    @SerializedName("lastActivity") val lastActivity: String = "",
    @SerializedName("status") val status: String = "active"
)

data class ActiveSessionsData(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 5,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("sessions") val sessions: List<ActiveSessionItemDto> = emptyList()
)

data class ActiveSessionsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ActiveSessionsData? = null
)