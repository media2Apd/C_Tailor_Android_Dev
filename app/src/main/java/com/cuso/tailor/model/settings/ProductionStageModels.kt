package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for Production Stage
 */
data class ProductionStageDto(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("workType") val workType: String? = null,
    @SerializedName("isAllocationRequired") val isAllocationRequired: Boolean = false,
    @SerializedName("allowRework") val allowRework: Boolean = true,
    @SerializedName("isSystemDefined") val isSystemDefined: Boolean = false,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    val id: String get() = _id
    val effectiveTitle: String get() = displayName?.ifBlank { name } ?: name
}

/**
 * Request payload for creating and updating a production stage
 */
data class CreateStageRequest(
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("workType") val workType: String? = null,
    @SerializedName("isAllocationRequired") val isAllocationRequired: Boolean = true,
    @SerializedName("allowRework") val allowRework: Boolean = true,
    @SerializedName("status") val status: String = "Active"
)

/**
 * API response format for stage list endpoint
 */
data class StageListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ProductionStageDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

/**
 * Generic API response format for stage deletion
 */
data class DeleteStageResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)