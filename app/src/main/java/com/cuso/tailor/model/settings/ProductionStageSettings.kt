package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

// ---------------------------------------------------------------------------
// 1. Production Stage DTO
// ---------------------------------------------------------------------------
data class ServicesProductionStageDto(
    @SerializedName("_id")
    val id: String = "",
    val organizationId: String? = null,
    val name: String = "",
    val displayName: String = "",
    val code: String = "",
    val description: String? = null,
    val workType: String = "",
    val isAllocationRequired: Boolean = true,
    val allowRework: Boolean = true,
    val isSystemDefined: Boolean = false,
    val status: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ---------------------------------------------------------------------------
// 2. API Responses
// ---------------------------------------------------------------------------
data class ProductionStageListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ServicesProductionStageDto> = emptyList(),
    val pagination: StagePagination? = null
)

data class ProductionStageActionResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ServicesProductionStageDto? = null
)

data class StagePagination(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

// ---------------------------------------------------------------------------
// 3. Create / Update Request Payload
// ---------------------------------------------------------------------------
data class SaveProductionStageRequest(
    val name: String,
    val displayName: String,
    val code: String,
    val description: String?,
    val workType: String,
    val isAllocationRequired: Boolean,
    val allowRework: Boolean,
    val status: String
)