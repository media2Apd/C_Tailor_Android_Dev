package com.cuso.tailor.model.settings

import com.cuso.tailor.model.logistics.PaginationMeta
import com.google.gson.annotations.SerializedName

data class PieceRateListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<PieceRateItemDto> = emptyList(),
    val pagination: PaginationMeta? = null
)

data class PieceRateViewOneResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: PieceRateItemDto? = null
)

data class PieceRateActionResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: PieceRateItemDto? = null
)

data class PieceRateDeleteResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: Any? = null
)

data class PieceRateItemDto(
    @SerializedName("_id")
    val id: String = "",
    val organizationId: String? = null,
    val workflowId: PieceRateWorkflowDto? = null,
    val garmentId: PieceRateGarmentDto? = null,
    val garmentCategoryId: Any? = null,
    val regularRatePerPiece: Double = 0.0,
    val otRatePerPiece: Double = 0.0,
    val effectiveFrom: String? = null,
    val effectiveUntil: String? = null,
    val status: String = "Active",
    val notes: String? = null,
    val createdBy: Any? = null,
    val updatedBy: Any? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class PieceRateWorkflowDto(
    @SerializedName("_id")
    val id: String = "",
    val name: String = "",
    val displayName: String? = null,
    val code: String = "",
    val workType: String? = null,
    val status: String = "Active"
)

data class PieceRateGarmentDto(
    @SerializedName("_id")
    val id: String = "",
    val name: String = "",
    val displayName: String? = null,
    val code: String = "",
    val status: String = "Active",
    val imageUrl: String? = null
)

data class SavePieceRateRequest(
    val workflowId: String,
    val garmentId: String?,
    val garmentCategoryId: String? = null,
    val regularRatePerPiece: Double,
    val otRatePerPiece: Double,
    val effectiveFrom: String,
    val effectiveUntil: String?,
    val status: String = "Active",
    val notes: String? = null
)