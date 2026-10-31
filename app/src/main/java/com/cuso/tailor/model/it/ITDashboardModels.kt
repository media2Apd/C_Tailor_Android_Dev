package com.cuso.tailor.model.it

import com.google.gson.annotations.SerializedName

/**
 * Root API response for the IT Dashboard.
 */
data class ItDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: ItDashboardData? = null,
    @SerializedName("message") val message: String? = null
)

data class ItDashboardData(
    @SerializedName("lastUpdated") val lastUpdated: String? = null,
    @SerializedName("kpis") val kpis: ItKpisDto? = null,
    @SerializedName("overallHealth") val overallHealth: ItOverallHealthDto? = null,
    @SerializedName("coreHealth") val coreHealth: List<ItCoreHealthDto> = emptyList(),
    @SerializedName("moduleOverviews") val moduleOverviews: List<ItModuleOverviewDto> = emptyList(),
    @SerializedName("integrationStats") val integrationStats: ItIntegrationStatsDto? = null,
    @SerializedName("integrationCategories") val integrationCategories: List<ItIntegrationCategoryDto> = emptyList(),
    @SerializedName("integrationIssues") val integrationIssues: List<ItIntegrationIssueDto> = emptyList(),
    @SerializedName("integrationIssueCount") val integrationIssueCount: Int = 0,
    @SerializedName("recentActivity") val recentActivity: List<ItRecentActivityDto> = emptyList(),
    @SerializedName("supportTriage") val supportTriage: List<ItSupportTriageDto> = emptyList(),
    @SerializedName("metrics") val metrics: ItMetricsDto? = null
)

data class ItKpisDto(
    @SerializedName("systems") val systems: ItKpiItemDto? = null,
    @SerializedName("devices") val devices: ItKpiItemDto? = null,
    @SerializedName("applications") val applications: ItKpiItemDto? = null,
    @SerializedName("network") val network: ItKpiItemDto? = null,
    @SerializedName("alerts") val alerts: ItKpiItemDto? = null,
    @SerializedName("tickets") val tickets: ItKpiItemDto? = null
)

data class ItKpiItemDto(
    @SerializedName("value") val value: Any? = null,
    @SerializedName("sub") val sub: String? = null
) {
    fun displayValue(): String {
        return when (value) {
            null -> "--"
            is Double -> if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
            else -> value.toString()
        }
    }
}

data class ItOverallHealthDto(
    @SerializedName("percent") val percent: Int = 0,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("healthy") val healthy: Int = 0,
    @SerializedName("warning") val warning: Int = 0,
    @SerializedName("critical") val critical: Int? = null
)

data class ItCoreHealthDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("icon") val icon: String = "",
    @SerializedName("badge") val badge: String = "",
    @SerializedName("badgeColor") val badgeColor: String = "",
    @SerializedName("healthPercent") val healthPercent: String = "",
    @SerializedName("stats") val stats: List<ItHealthStatDto> = emptyList(),
    @SerializedName("link") val link: String? = null
)

data class ItHealthStatDto(
    @SerializedName("label") val label: String = "",
    @SerializedName("isWarn") val isWarn: Boolean = false
)

data class ItModuleOverviewDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("icon") val icon: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("btnText") val btnText: String = "",
    @SerializedName("link") val link: String? = null,
    @SerializedName("details") val details: List<ItModuleDetailItemDto> = emptyList()
)

data class ItModuleDetailItemDto(
    @SerializedName("text") val text: String = ""
)

data class ItIntegrationStatsDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("connected") val connected: Int = 0,
    @SerializedName("attention") val attention: Int = 0,
    @SerializedName("disconnected") val disconnected: Int = 0,
    @SerializedName("failed") val failed: Int = 0,
    @SerializedName("lastSync") val lastSync: String = ""
)

data class ItIntegrationCategoryDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("healthy") val healthy: Int = 0,
    @SerializedName("attention") val attention: Int = 0
)

data class ItIntegrationIssueDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("reason") val reason: String = "",
    @SerializedName("sub") val sub: String = ""
)

data class ItRecentActivityDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("location") val location: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("time") val time: String = "",
    @SerializedName("type") val type: String = "info" // error, success, warn, info
)

data class ItSupportTriageDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("sub") val sub: String = "",
    @SerializedName("time") val time: String = "",
    @SerializedName("location") val location: String = ""
)

data class ItMetricsDto(
    @SerializedName("devices") val devices: Any? = null,
    @SerializedName("pos") val pos: Any? = null,
    @SerializedName("assets") val assets: Any? = null,
    @SerializedName("backups") val backups: Any? = null,
    @SerializedName("tickets") val tickets: Any? = null
)