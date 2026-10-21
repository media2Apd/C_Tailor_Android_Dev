@file:Suppress("unused")
package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

data class GetFloorsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<FloorItemSettings>? = null,     // 👈 API sends "data"
    @SerializedName("floors") val floors: List<FloorItemSettings>? = null,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("message") val message: String? = null
)

data class WarehouseReference(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String
)

data class FloorItemSettings(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("warehouseId") val warehouseElement: com.google.gson.JsonElement? = null, // 👈 Handles Object or String
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("sequenceOrder") val sequenceOrder: Int = 1,
    @SerializedName("description") val description: String? = null,
    @SerializedName("temperatureZone") val temperatureZone: String? = "",
    @SerializedName("floorAreaSqft") val floorAreaSqft: Double = 0.0,
    @SerializedName("maxWeightCapacityKg") val maxWeightCapacityKg: Double = 0.0,
    @SerializedName("sectionsCount") val sectionsCount: Int = 0,
    @SerializedName("racksCount") val racksCount: Int = 0,
    @SerializedName("binsCount") val binsCount: Int = 0,
    @SerializedName("status") val status: String = "",
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    val warehouseIdValue: String
        get() {
            if (warehouseElement == null) return ""
            return try {
                if (warehouseElement.isJsonObject) {
                    warehouseElement.asJsonObject.get("_id")?.asString ?: ""
                } else if (warehouseElement.isJsonPrimitive) {
                    warehouseElement.asString
                } else ""
            } catch (_: Exception) { "" }
        }

    val warehouseName: String
        get() {
            if (warehouseElement == null) return ""
            return try {
                if (warehouseElement.isJsonObject) {
                    warehouseElement.asJsonObject.get("name")?.asString ?: ""
                } else ""
            } catch (_: Exception) { "" }
        }
}