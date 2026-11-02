package com.cuso.tailor.model.permission

import com.google.gson.annotations.SerializedName

data class ModulePermissionsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: ModulePermissionsData? = null,
    @SerializedName("message") val message: String? = null
)

data class ModulePermissionsData(
    @SerializedName("home") val home: List<String> = emptyList(),
    @SerializedName("sales") val sales: List<String> = emptyList(),
    @SerializedName("marketing") val marketing: List<String> = emptyList(),
    @SerializedName("finance") val finance: List<String> = emptyList(),
    @SerializedName("inventory") val inventory: List<String> = emptyList(),
    @SerializedName("logistics") val logistics: List<String> = emptyList(),
    @SerializedName("services") val services: List<String> = emptyList(),
    @SerializedName("hr") val hr: List<String> = emptyList(),
    @SerializedName("it") val itModule: List<String> = emptyList(),
    @SerializedName("legal") val legal: List<String> = emptyList(),
    @SerializedName("security") val security: List<String> = emptyList(),
    @SerializedName("reports") val reports: List<String> = emptyList()
) {
    /**
     * Checks if a given module and feature are enabled
     */
    fun isFeatureAllowed(moduleName: String, featureKey: String): Boolean {
        val normalizedModule = moduleName.lowercase().trim()
        val allowedList = when (normalizedModule) {
            "home" -> home
            "sales" -> sales
            "marketing" -> marketing
            "finance" -> finance
            "inventory" -> inventory
            "logistics" -> logistics
            "services" -> services
            "hr" -> hr
            "it" -> itModule
            "legal" -> legal
            "security" -> security
            "reports" -> reports
            else -> emptyList()
        }
        return allowedList.any { it.equals(featureKey, ignoreCase = true) }
    }
}

/**
 * Paywall metadata to display in the Upgrade Screen
 */
data class PaywallModuleConfig(
    val moduleKey: String,
    val moduleTitle: String,
    val headline: String,
    val description: String,
    val efficiencyBadge: String = "+30% Efficiency",
    val currentPlan: String = "Enterprise",
    val featuresIncluded: List<String>
)