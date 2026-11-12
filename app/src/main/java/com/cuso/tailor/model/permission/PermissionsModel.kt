@file:Suppress("unused")
package com.cuso.tailor.model.permission


import com.google.gson.annotations.SerializedName

enum class PermissionAction {
    VIEW, CREATE, EDIT, DELETE, EXPORT, PRINT, SHARE, APPROVE
}

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
    @SerializedName("delete") val delete: Boolean = false,
    @SerializedName("export") val export: Boolean = false,
    @SerializedName("print") val print: Boolean = false,
    @SerializedName("share") val share: Boolean = false,
    @SerializedName("approve") val approve: Boolean = false
)

data class MyPermissionsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: MyPermissionsData? = null
)

data class MyPermissionsData(
    @SerializedName("isOwner") val isOwner: Boolean = false,
    @SerializedName("permissions") val permissions: Map<String, Map<String, ActionPermission>>? = null,
    @SerializedName("visible") val visible: List<String> = emptyList()
) {
    fun getActions(module: String, feature: String): ActionPermission {
        if (isOwner) {
            return ActionPermission(
                view = true,
                create = true,
                edit = true,
                delete = true,
                export = true,
                print = true,
                share = true,
                approve = true
            )
        }
        val moduleMap = permissions?.get(module.lowercase().trim()) ?: return ActionPermission()
        return moduleMap[feature.trim()] ?: ActionPermission()
    }

    fun canView(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).view

    fun canCreate(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).create

    fun canEdit(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).edit

    fun canDelete(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).delete

    fun canExport(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).export

    fun canPrint(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).print

    fun canShare(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).share

    fun canApprove(module: String, feature: String): Boolean =
        isOwner || getActions(module, feature).approve
}