@file:Suppress("unused")

package com.cuso.tailor.model.permission

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

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
    @SerializedName("hidden") val hidden: Boolean = false,
    @SerializedName("view") val view: Boolean = false,
    @SerializedName("create") val create: Boolean = false,
    @SerializedName("edit") val edit: Boolean = false,
    @SerializedName("delete") val delete: Boolean = false,
    @SerializedName("export") val export: Boolean = false,
    @SerializedName("print") val print: Boolean = false,
    @SerializedName("share") val share: Boolean = false,
    @SerializedName("approve") val approve: Boolean = false
)

@JsonAdapter(ModulePermissionSetAdapter::class)
data class ModulePermissionSet(
    val hidden: Boolean = false,
    val features: Map<String, ActionPermission> = emptyMap()
)

class ModulePermissionSetAdapter :
    JsonDeserializer<ModulePermissionSet>,
    JsonSerializer<ModulePermissionSet> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ModulePermissionSet {
        if (json == null || !json.isJsonObject) return ModulePermissionSet()

        val obj = json.asJsonObject
        val isHidden = obj.get("hidden")
            ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isBoolean }
            ?.asBoolean ?: false

        val features = mutableMapOf<String, ActionPermission>()
        for ((key, value) in obj.entrySet()) {
            if (key == "hidden") continue
            if (value != null && value.isJsonObject && context != null) {
                val action = context.deserialize<ActionPermission>(
                    value,
                    ActionPermission::class.java
                ) ?: ActionPermission()
                features[key] = action
            }
        }

        return ModulePermissionSet(
            hidden = isHidden,
            features = features
        )
    }

    override fun serialize(
        src: ModulePermissionSet?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val obj = JsonObject()
        if (src == null) return obj

        obj.addProperty("hidden", src.hidden)
        src.features.forEach { (key, value) ->
            if (context != null) {
                obj.add(key, context.serialize(value, ActionPermission::class.java))
            }
        }
        return obj
    }
}

data class MyPermissionsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: MyPermissionsData? = null
)

data class MyPermissionsData(
    @SerializedName("isOwner") val isOwner: Boolean = false,
    @SerializedName("permissions") val permissions: Map<String, ModulePermissionSet>? = null,
    @SerializedName("visible") val visible: List<String> = emptyList()
) {
    private fun getModuleSet(module: String): ModulePermissionSet? {
        val normalized = module.trim()
        return permissions?.entries
            ?.firstOrNull { it.key.equals(normalized, ignoreCase = true) }
            ?.value
    }

    fun isModuleHidden(module: String): Boolean {
        if (isOwner) return false
        return getModuleSet(module)?.hidden == true
    }

    fun isFeatureHidden(module: String, feature: String): Boolean {
        if (isOwner) return false
        val moduleSet = getModuleSet(module) ?: return false
        if (moduleSet.hidden) return true
        val featurePerm = moduleSet.features.entries
            .firstOrNull { it.key.equals(feature.trim(), ignoreCase = true) }
            ?.value ?: return false
        return featurePerm.hidden
    }

    fun getActions(module: String, feature: String): ActionPermission {
        if (isOwner) {
            return ActionPermission(
                hidden = false,
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
        val moduleSet = getModuleSet(module) ?: return ActionPermission()
        if (moduleSet.hidden) return ActionPermission(hidden = true)

        return moduleSet.features.entries
            .firstOrNull { it.key.equals(feature.trim(), ignoreCase = true) }
            ?.value ?: ActionPermission()
    }

    fun canView(module: String, feature: String): Boolean {
        if (isOwner) return true
        val actions = getActions(module, feature)
        return !actions.hidden && actions.view
    }

    fun canCreate(module: String, feature: String): Boolean {
        if (isOwner) return true
        val actions = getActions(module, feature)
        return !actions.hidden && actions.create
    }

    fun canEdit(module: String, feature: String): Boolean {
        if (isOwner) return true
        val actions = getActions(module, feature)
        return !actions.hidden && actions.edit
    }

    fun canDelete(module: String, feature: String): Boolean {
        if (isOwner) return true
        val actions = getActions(module, feature)
        return !actions.hidden && actions.delete
    }

    fun canExport(module: String, feature: String): Boolean {
        if (isOwner) return true
        val actions = getActions(module, feature)
        return !actions.hidden && actions.export
    }

    fun canPrint(module: String, feature: String): Boolean {
        if (isOwner) return true
        val actions = getActions(module, feature)
        return !actions.hidden && actions.print
    }

    fun canShare(module: String, feature: String): Boolean {
        if (isOwner) return true
        val actions = getActions(module, feature)
        return !actions.hidden && actions.share
    }

    fun canApprove(module: String, feature: String): Boolean {
        if (isOwner) return true
        val actions = getActions(module, feature)
        return !actions.hidden && actions.approve
    }
}