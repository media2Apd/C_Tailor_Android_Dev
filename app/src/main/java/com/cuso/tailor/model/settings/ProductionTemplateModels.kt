package com.cuso.tailor.model.settings

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// ── Generic Wrapper ──
data class BaseApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

data class DeleteTemplateResponse(
    @SerializedName("success") val success: Any?,
    @SerializedName("message") val message: String? = null
)

data class TemplatePagination(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

data class TemplateListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ProductionTemplateDto> = emptyList(),
    @SerializedName("pagination") val pagination: TemplatePagination? = null
)

// ── Production Template DTO ──
data class ProductionTemplateDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("segmentId") val segment: TemplateSegmentRef? = null,
    @SerializedName("garmentId") val garment: TemplateGarmentRef? = null,

    // Polymorphic JsonElement: Handles both List<String> and List<Object> safely
    @SerializedName("garmentIds") val rawGarmentIds: List<JsonElement>? = null,
    @SerializedName("garmentCategoryIds") val garmentCategoryIds: List<TemplateGarmentCategoryRef>? = null,
    @SerializedName("stages") val stages: List<TemplateStageItemDto>? = null,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("createdBy") val createdBy: TemplateUserRef? = null,
    @SerializedName("updatedBy") val updatedBy: TemplateUserRef? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    /**
     * Safely computes garment count regardless of whether backend returned strings, objects, or empty array.
     */
    val safeGarmentCount: Int
        get() {
            val fromCategories = garmentCategoryIds?.size ?: 0
            val fromGarmentIds = rawGarmentIds?.size ?: 0
            val singleGarment = if (garment != null) 1 else 0
            return maxOf(fromCategories, fromGarmentIds, singleGarment)
        }

    val safeStepCount: Int
        get() = stages?.size ?: 0
}

// ── Referenced Entity Models ──
data class TemplateSegmentRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("code") val code: String? = null
)

data class TemplateGarmentRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("code") val code: String? = null
)

data class TemplateGarmentCategoryRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("image") val image: String? = null
)

data class TemplateUserRef(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)

// ── Stage Models ──
data class TemplateStageItemDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("stageId") val stageDetail: TemplateStageDetail? = null,
    @SerializedName("displayOrder") val displayOrder: Int = 1,
    @SerializedName("isMandatory") val isMandatory: Boolean = true,
    @SerializedName("isAllocationRequired") val isAllocationRequired: Boolean = true,
    @SerializedName("allowRework") val allowRework: Boolean = true,
    @SerializedName("instructions") val instructions: String? = null
)

data class TemplateStageDetail(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("workType") val workType: String? = null,
    @SerializedName("isAllocationRequired") val isAllocationRequired: Boolean = true,
    @SerializedName("allowRework") val allowRework: Boolean = true
)

// ── Create & Update Request Models ──
data class CreateTemplateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("segmentId") val segmentId: String? = null,
    @SerializedName("garmentId") val garmentId: String? = null,
//    @SerializedName("garmentCategoryIds") val garmentCategoryIds: List<String> = emptyList(),
    @SerializedName("stages") val stages: List<CreateTemplateStageItem> = emptyList(),
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("status") val status: String = "Active"
)

data class CreateTemplateStageItem(
    @SerializedName("stageId") val stageId: String,
    @SerializedName("displayOrder") val displayOrder: Int,
    @SerializedName("isMandatory") val isMandatory: Boolean = true,
    @SerializedName("isAllocationRequired") val isAllocationRequired: Boolean = true,
    @SerializedName("allowRework") val allowRework: Boolean = true,
    @SerializedName("instructions") val instructions: String? = null
)