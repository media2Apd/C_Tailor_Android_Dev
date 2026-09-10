package com.cuso.mobile.model.settings

import com.google.gson.annotations.SerializedName

// =============================================================================
// 1. API RESPONSE WRAPPERS
// =============================================================================

data class DesignListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("pagination")
    val pagination: PaginationDto? = null,
    @SerializedName("data")
    val data: List<DesignItem> = emptyList(),
    @SerializedName("message")
    val message: String? = null
)

data class DesignDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: DesignItem? = null,
    @SerializedName("message")
    val message: String? = null
)

data class ChangeDesignStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: DesignItem? = null
)

data class DeleteDesignResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null
)

// =============================================================================
// 2. DESIGN CORE ITEM
// =============================================================================

data class DesignItem(
    @SerializedName("_id", alternate = ["id"])
    val id: String,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("name")
    val name: String,
    @SerializedName("designType")
    val designType: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("applicableGarments")
    val applicableGarments: List<ApplicableGarmentItem> = emptyList(),
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("imagePublicId")
    val imagePublicId: String? = null,
    @SerializedName("status")
    val status: String = "Active",
    @SerializedName("createdBy")
    val createdBy: UserMetaDto? = null,
    @SerializedName("updatedBy")
    val updatedBy: UserMetaDto? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("__v")
    val v: Int? = null
)

// =============================================================================
// 3. NESTED SUPPORTING MODELS
// =============================================================================

data class ApplicableGarmentItem(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    @SerializedName("segmentId")
    val segmentId: DesignSegmentDto? = null,
    @SerializedName("garmentId")
    val garmentId: String? = null,
    @SerializedName("garmentCategoryId")
    val garmentCategoryId: String? = null,
    @SerializedName("allCategories")
    val allCategories: Boolean = true
)

data class DesignSegmentDto(
    @SerializedName("_id", alternate = ["id"])
    val id: String,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null
)

// =============================================================================
// 4. REQUEST PAYLOADS
// =============================================================================

data class ChangeDesignStatusRequest(
    @SerializedName("status")
    val status: String
)