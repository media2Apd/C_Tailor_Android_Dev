package com.cuso.mobile.model.settings

import com.cuso.mobile.model.sales.PaginationInfo
import com.google.gson.annotations.SerializedName

// ── Measurement Field List Response ──
data class MeasurementFieldListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaginationInfo? = null,
    @SerializedName("data") val data: List<MeasurementFieldItem> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class MeasurementResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaginationInfo,
    @SerializedName("data") val data: List<MeasurementFieldItem>
)

// ── Measurement Field Item Model ──
data class MeasurementFieldItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("inputType") val inputType: String = "Number",
    @SerializedName("inputCount") val inputCount: Int = 1,
    @SerializedName("subLabels") val subLabels: List<String> = emptyList(),
    @SerializedName("unit") val unit: String? = "inch",
    @SerializedName("minValue") val minValue: Double? = null,
    @SerializedName("maxValue") val maxValue: Double? = null,
    @SerializedName("options") val options: List<String> = emptyList(),
    @SerializedName("isSystemDefined") val isSystemDefined: Boolean = false,
    @SerializedName("status") val status: String? = null,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class DeactivateMeasurementFieldResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: MeasurementFieldItem?
)

data class UserAuditReference(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("memberId") val memberId: String?
)