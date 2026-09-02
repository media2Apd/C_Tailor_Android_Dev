package com.cuso.mobile.model.settings

import com.google.gson.annotations.SerializedName

// ── Measurement Field List Response ──
data class MeasurementFieldListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaginationInfo? = null,
    @SerializedName("data") val data: List<MeasurementFieldItem> = emptyList(),
    @SerializedName("message") val message: String? = null
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
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)