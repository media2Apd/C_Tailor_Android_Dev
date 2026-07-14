package com.cuso.mobile.model.sales

import com.cuso.mobile.database.entities.SalesStatusEntity
import com.google.gson.annotations.SerializedName

// --- Status response ---
data class SalesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<SalesStatusDto>
)

data class SalesStatusDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("color") val color: String,
    @SerializedName("order") val order: Int,
    @SerializedName("conversionStatus") val conversionStatus: Boolean,
    @SerializedName("isDefault") val isDefault: Boolean,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("__v") val version: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

fun SalesStatusDto.toEntity() = SalesStatusEntity(
    id               = id,
    name             = name,
    code             = code,
    color            = color,
    order            = order,
    conversionStatus = conversionStatus,
    isDefault        = isDefault,
    organizationId   = organizationId,
    version          = version,
    createdAt        = createdAt,
    updatedAt        = updatedAt
)

// --- Summary response ---
data class SalesSummaryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SalesSummaryDto
)

data class SalesSummaryDto(
    @SerializedName("totalAssigned")  val totalAssigned:  Int,
    @SerializedName("active")         val active:         Int,
    @SerializedName("inactive")       val inactive:       Int,
    @SerializedName("availableSlots") val availableSlots: Int?,
    @SerializedName("categories")     val categories:     List<CategoryItem> = emptyList()  // ✅ added
)

data class CategoryItem(
    @SerializedName("_id")        val id:         String,
    @SerializedName("categoryId") val categoryId: CategoryDetail,
    @SerializedName("isActive")   val isActive:   Boolean
)

data class CategoryDetail(
    @SerializedName("_id")          val id:           String,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("models")       val models: List<GarmentModel> = emptyList()
)

data class GarmentModel(
    @SerializedName("modelName")  val modelName:  String,
    @SerializedName("pieceRate")  val pieceRate:  Int,
    @SerializedName("modelIcon")  val modelIcon:  String? = null,
    @SerializedName("_id")        val id:         String
)