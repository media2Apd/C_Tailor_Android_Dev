package com.cuso.mobile.model

import com.google.gson.annotations.SerializedName

data class UpdateStageRequest(
    val status: String   // "pending" | "in_progress" | "completed"
)

data class UpdateStageResponse(
    val success: Boolean,
    val message: String?,
    val data: GarmentStageDoc?
)

data class GarmentStageDoc(
    @SerializedName("_id") val id: String,
    val organizationId: String?,
    val salesOrderId: String?,
    val garmentItemId: String,
    val stages: List<GarmentStageItem>,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?
)

data class GarmentStageItem(
    @SerializedName("_id") val id: String,
    val stageName: String,
    val assignedTo: List<String>,
    val assignedQuantity: Int,
    val completedQuantity: Int,
    val failedQuantity: Int,
    val status: String,
    val completedAt: String? = null
)