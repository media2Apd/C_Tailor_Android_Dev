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

data class ActionPermission(
    @SerializedName("view") val view: Boolean = false,
    @SerializedName("create") val create: Boolean = false,
    @SerializedName("edit") val edit: Boolean = false,
    @SerializedName("delete") val delete: Boolean = false
)

data class MyPermissionsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: MyPermissionsData? = null
)

data class MyPermissionsData(
    @SerializedName("isOwner") val isOwner: Boolean = false,
    // Nested structure: Map<Module, Map<Feature, ActionPermission>>
    @SerializedName("permissions") val permissions: Map<String, Map<String, ActionPermission>>? = null,
    @SerializedName("visible") val visible: List<String> = emptyList()
) {
    /**
     * Resolves permissions for a specific module and feature.
     * If user is owner, all actions return true.
     */
    fun getActions(module: String, feature: String): ActionPermission {
        if (isOwner) return ActionPermission(view = true, create = true, edit = true, delete = true)

        val moduleMap = permissions?.get(module.lowercase()) ?: return ActionPermission()
        return moduleMap[feature] ?: ActionPermission()
    }

    fun canView(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).view

    fun canCreate(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).create

    fun canEdit(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).edit

    fun canDelete(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).delete
}