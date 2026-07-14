package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

data class StageAssignRequest(
    @SerializedName("workerIds")
    val workerIds: List<String>,
    @SerializedName("assignedQuantity")
    val assignedQuantity: Int
)

data class StageDetail(
    val stageName: String,
    val assignedTo: List<String>,
    val assignedQuantity: Int,
    val completedQuantity: Int,
    val failedQuantity: Int,
    val status: String,
    val _id: String
)

data class GarmentStageData(
    val _id: String,
    val organizationId: String,
    val salesOrderId: String,
    val garmentItemId: String,
    val stages: List<StageDetail>,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class AssignStageResponse(
    val success: Boolean,
    val message: String,
    val data: GarmentStageData
)