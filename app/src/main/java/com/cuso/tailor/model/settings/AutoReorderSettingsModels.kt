package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

data class AutoReorderSettingsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val data: AutoReorderSettingsData? = null,
    @SerializedName("message")
    val message: String? = null
)

data class AutoReorderSettingsData(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("organizationId")
    val organizationId: String = "",
    @SerializedName("enabled")
    val enabled: Boolean = true,
    @SerializedName("checkFrequency")
    val checkFrequency: String = "Every 1 Minute",
    @SerializedName("lastRunAt")
    val lastRunAt: String? = null,

    // Consolidation
    @SerializedName("poConsolidationMode")
    val poConsolidationMode: String = "CONSOLIDATED", // "CONSOLIDATED" or "INDIVIDUAL"
    @SerializedName("appendToExistingDraftPO")
    val appendToExistingDraftPO: Boolean = false,
    @SerializedName("enableLookAheadWindow")
    val enableLookAheadWindow: Boolean = false,
    @SerializedName("lookAheadThresholdPercent")
    val lookAheadThresholdPercent: Int = 15,

    // Sales Velocity
    @SerializedName("enableSalesVelocity")
    val enableSalesVelocity: Boolean = false,
    @SerializedName("velocityLookAheadDays")
    val velocityLookAheadDays: Int = 7,
    @SerializedName("salesHistoryDays")
    val salesHistoryDays: Int = 30,

    // Defaults
    @SerializedName("defaultSupplierId")
    val defaultSupplierId: NamedRef? = null,
    @SerializedName("defaultWarehouseId")
    val defaultWarehouseId: NamedRef? = null,
    @SerializedName("minOrderQty")
    val minOrderQty: Int = 20,
    @SerializedName("maxOrderQty")
    val maxOrderQty: Int = 200,

    // Lifecycle
    @SerializedName("autoCreatePO")
    val autoCreatePO: Boolean = true,
    @SerializedName("autoApprovePO")
    val autoApprovePO: Boolean = false,
    @SerializedName("notifyPurchasingTeam")
    val notifyPurchasingTeam: Boolean = true,

    // Notifications
    @SerializedName("notificationChannels")
    val notificationChannels: NotificationChannelsConfig? = null,
    @SerializedName("recipientRoleIds")
    val recipientRoleIds: List<String> = emptyList(),

    // Priority
    @SerializedName("reorderPriorityRule")
    val reorderPriorityRule: String = "Product Priority",

    // Safety Buffer
    @SerializedName("safetyBuffer")
    val safetyBuffer: SafetyBufferConfig? = null
)

data class NamedRef(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("name")
    val name: String = ""
)

data class NotificationChannelsConfig(
    @SerializedName("email")
    val email: Boolean = true,
    @SerializedName("inApp")
    val inApp: Boolean = true,
    @SerializedName("sms")
    val sms: Boolean = false
)

data class SafetyBufferConfig(
    @SerializedName("enabled")
    val enabled: Boolean = false,
    @SerializedName("quantity")
    val quantity: Int = 0
)

/**
 * Request payload for saving Auto Reorder Settings
 */
data class SaveAutoReorderSettingsRequest(
    @SerializedName("enabled")
    val enabled: Boolean,
    @SerializedName("checkFrequency")
    val checkFrequency: String,
    @SerializedName("lastRunAt")
    val lastRunAt: String? = null,
    @SerializedName("poConsolidationMode")
    val poConsolidationMode: String,
    @SerializedName("appendToExistingDraftPO")
    val appendToExistingDraftPO: Boolean,
    @SerializedName("enableLookAheadWindow")
    val enableLookAheadWindow: Boolean,
    @SerializedName("lookAheadThresholdPercent")
    val lookAheadThresholdPercent: Int = 15,
    @SerializedName("enableSalesVelocity")
    val enableSalesVelocity: Boolean,
    @SerializedName("velocityLookAheadDays")
    val velocityLookAheadDays: Int,
    @SerializedName("salesHistoryDays")
    val salesHistoryDays: Int,
    @SerializedName("defaultSupplierId")
    val defaultSupplierId: String,
    @SerializedName("defaultWarehouseId")
    val defaultWarehouseId: String,
    @SerializedName("minOrderQty")
    val minOrderQty: Int,
    @SerializedName("maxOrderQty")
    val maxOrderQty: Int,
    @SerializedName("autoCreatePO")
    val autoCreatePO: Boolean,
    @SerializedName("autoApprovePO")
    val autoApprovePO: Boolean,
    @SerializedName("notifyPurchasingTeam")
    val notifyPurchasingTeam: Boolean,
    @SerializedName("notificationChannels")
    val notificationChannels: NotificationChannelsConfig,
    @SerializedName("recipientRoleIds")
    val recipientRoleIds: List<String>,
    @SerializedName("reorderPriorityRule")
    val reorderPriorityRule: String,
    @SerializedName("safetyBuffer")
    val safetyBuffer: SafetyBufferConfig
)

/**
 * Response when saving Auto Reorder Settings
 */
data class SaveAutoReorderSettingsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val data: AutoReorderSettingsData? = null,
    @SerializedName("message")
    val message: String? = null
)