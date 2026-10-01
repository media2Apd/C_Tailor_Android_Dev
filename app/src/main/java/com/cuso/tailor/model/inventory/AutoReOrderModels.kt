package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

// Response wrapper for Auto Re-Order rules
data class AutoReorderResponse(
    val success: Boolean,
    val pagination: AutoReorderPagination? = null,
    val data: List<AutoReorderRuleItemDto> = emptyList()
)

data class AutoReorderPagination(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1
)

// Individual Auto Re-Order rule entity
data class AutoReorderRuleItemDto(
    @SerializedName("_id")
    val id: String,
    val organizationId: String?,
    val ruleName: String,
    val itemId: AutoReorderItemDetailDto?,
    val warehouseId: AutoReorderWarehouseDto?,
    val triggerCondition: AutoReorderTriggerConditionDto?,
    val reorderQty: Double?,
    val supplierId: AutoReorderSupplierDto?,
    val isActive: Boolean = true,
    val lastRunStatus: String? = null,
    val linkedPOIds: List<String> = emptyList(),
    val isDeleted: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class AutoReorderTriggerConditionDto(
    val operator: String = "<=",
    val thresholdQty: Double = 0.0
)

data class AutoReorderItemDetailDto(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val sku: String? = null,
    val unit: String? = null,
    val marginPercent: Double? = 0.0,
    val currentStockValue: Double? = 0.0
)

data class AutoReorderWarehouseDto(
    @SerializedName("_id")
    val id: String,
    val name: String
)

data class AutoReorderSupplierDto(
    @SerializedName("_id")
    val id: String,
    val name: String
)

data class DeleteAutoReorderResponse(
    val success: Boolean,
    val message: String
)

data class CreateAutoReorderRuleRequest(
    @SerializedName("ruleName") val ruleName: String,
    @SerializedName("itemId") val itemId: String,
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("triggerCondition") val triggerCondition: TriggerConditionPayload,
    @SerializedName("reorderQty") val reorderQty: Int,
    @SerializedName("supplierId") val supplierId: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true
)

data class TriggerConditionPayload(
    @SerializedName("operator") val operator: String,
    @SerializedName("thresholdQty") val thresholdQty: Int
)

data class TriggerConditionDto(
    @SerializedName("operator") val operator: String,
    @SerializedName("thresholdQty") val thresholdQty: Double
)

data class CreateAutoReorderRuleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AutoReorderRuleData? = null
)

data class AutoReorderRuleData(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("ruleName") val ruleName: String,
    @SerializedName("itemId") val itemId: String,
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("triggerCondition") val triggerCondition: TriggerConditionDto?,
    @SerializedName("reorderQty") val reorderQty: Double,
    @SerializedName("supplierId") val supplierId: String?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("lastRunStatus") val lastRunStatus: String?,
    @SerializedName("linkedPOIds") val linkedPOIds: List<String> = emptyList(),
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

/**
 * Response model for toggling an auto-reorder rule's active status.
 */
data class ToggleAutoReorderStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: AutoReorderRuleData? = null,
    @SerializedName("message")
    val message: String? = null
)