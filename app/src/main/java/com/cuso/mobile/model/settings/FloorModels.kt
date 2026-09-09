package com.cuso.mobile.model.settings

import com.google.gson.annotations.SerializedName

data class GetFloorsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("floors") val floors: List<FloorItemSettings>?,
    @SerializedName("total") val total: Int? = 0,
    @SerializedName("page") val page: Int? = 1,
    @SerializedName("pageSize") val pageSize: Int? = 20,
    @SerializedName("totalPages") val totalPages: Int? = 1,
    @SerializedName("message") val message: String? = null
)

data class WarehouseReference(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String
)

data class FloorItemSettings(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("warehouseId") val warehouse: WarehouseReference? = null,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("sequenceOrder") val sequenceOrder: Int = 1,
    @SerializedName("description") val description: String? = null,
    @SerializedName("temperatureZone") val temperatureZone: String? = "normal",
    @SerializedName("floorAreaSqft") val floorAreaSqft: Double = 0.0,
    @SerializedName("maxWeightCapacityKg") val maxWeightCapacityKg: Double = 0.0,
    @SerializedName("sectionsCount") val sectionsCount: Int = 0,
    @SerializedName("racksCount") val racksCount: Int = 0,
    @SerializedName("binsCount") val binsCount: Int = 0,
    @SerializedName("status") val status: String = "active",
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)