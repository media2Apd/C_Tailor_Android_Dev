package com.cuso.mobile.model.settings

import com.cuso.mobile.model.inventory.LowStockItemDto
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class BinItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("warehouseId") val warehouseElement: JsonElement? = null,
    @SerializedName("floorId") val floorId: String? = null,
    @SerializedName("sectionId") val sectionId: String? = null,
    @SerializedName("rackId") val rackElement: JsonElement? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("sequenceOrder") val sequenceOrder: Int = 1,
    @SerializedName("description") val description: String? = null,
    @SerializedName("binType") val binType: String? = "",
    @SerializedName("maxQuantity") val maxQuantity: Int = 0,
    @SerializedName("maxWeightKg") val maxWeightKg: Double = 0.0,
    @SerializedName("defaultUOM") val defaultUOM: String? = "",
    @SerializedName("fullPathCode") val fullPathCode: String? = null,
    @SerializedName("status") val status: String = "",
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("deletedAt") val deletedAt: String? = null,
    @SerializedName("deletedBy") val deletedBy: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val v: Int? = 0
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

    val rackIdValue: String
        get() {
            if (rackElement == null) return ""
            return try {
                if (rackElement.isJsonObject) {
                    rackElement.asJsonObject.get("_id")?.asString ?: ""
                } else if (rackElement.isJsonPrimitive) {
                    rackElement.asString
                } else ""
            } catch (_: Exception) { "" }
        }

    val rackName: String
        get() {
            if (rackElement == null) return ""
            return try {
                if (rackElement.isJsonObject) {
                    rackElement.asJsonObject.get("name")?.asString ?: ""
                } else ""
            } catch (_: Exception) { "" }
        }

    val rackCode: String
        get() {
            if (rackElement == null) return ""
            return try {
                if (rackElement.isJsonObject) {
                    rackElement.asJsonObject.get("code")?.asString ?: ""
                } else ""
            } catch (_: Exception) { "" }
        }

    val rackDisplayName: String
        get() = rackName.ifBlank { rackCode.ifBlank { rackIdValue } }

    // ── Single Low Stock Item Response ──
    data class LowStockDetailResponse(
        @SerializedName("success") val success: Boolean = false,
        @SerializedName("data") val data: LowStockItemDto? = null,
        @SerializedName("message") val message: String? = null
    )
}
data class BaseInventoryResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null
)

data class GetSectionsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("sections") val sections: List<SectionItem>? = emptyList(),
    @SerializedName("data") val data: List<SectionItem>? = null,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("pageSize") val pageSize: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("message") val message: String? = null
)

data class GetRacksResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("racks") val racks: List<RackItem>? = emptyList(),
    @SerializedName("data") val data: List<RackItem>? = null,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("pageSize") val pageSize: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("message") val message: String? = null
)

data class GetBinsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("bins") val bins: List<BinItem>? = emptyList(),
    @SerializedName("data") val data: List<BinItem>? = null,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("pageSize") val pageSize: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("message") val message: String? = null
)

data class AllowedSections(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = ""
)

data class CreateFloorRequest(
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("sequenceOrder") val sequenceOrder: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("temperatureZone") val temperatureZone: String,
    @SerializedName("floorAreaSqft") val floorAreaSqft: Double,
    @SerializedName("maxWeightCapacityKg") val maxWeightCapacityKg: Double,
    @SerializedName("status") val status: String
)

data class FloorItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("warehouseId") val warehouseElement: JsonElement? = null,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
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
)

data class CreateSectionRequest(
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("floorId") val floorId: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("sequenceOrder") val sequenceOrder: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("allowedProductCategories") val allowedProductCategories: List<String>,
    @SerializedName("storageType") val storageType: String,
    @SerializedName("climateControl") val climateControl: String,
    @SerializedName("status") val status: String
)

data class SectionItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("warehouseId") val warehouseElement: JsonElement? = null,
    @SerializedName("floorId") val floorElement: JsonElement? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("sequenceOrder") val sequenceOrder: Int = 1,
    @SerializedName("description") val description: String? = null,
    @SerializedName("allowedProductCategories") val allowedCategoriesElement: JsonElement? = null,
    @SerializedName("storageType") val storageType: String? = "",
    @SerializedName("climateControl") val climateControl: String? = "",
    @SerializedName("racksCount") val racksCount: Int = 0,
    @SerializedName("binsCount") val binsCount: Int = 0,
    @SerializedName("status") val status: String = "",
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    val allowedProductCategories: List<AllowedSections>
        get() {
            if (allowedCategoriesElement == null || !allowedCategoriesElement.isJsonArray) return emptyList()
            val list = mutableListOf<AllowedSections>()
            allowedCategoriesElement.asJsonArray.forEach { elem ->
                if (elem.isJsonObject) {
                    val obj = elem.asJsonObject
                    list.add(
                        AllowedSections(
                            id = obj.get("_id")?.asString ?: "",
                            name = obj.get("name")?.asString ?: "",
                            code = obj.get("code")?.asString ?: ""
                        )
                    )
                } else if (elem.isJsonPrimitive) {
                    list.add(AllowedSections(id = elem.asString, name = "", code = ""))
                }
            }
            return list
        }

    val floorName: String
        get() {
            if (floorElement == null) return ""
            return try {
                if (floorElement.isJsonObject) {
                    floorElement.asJsonObject.get("name")?.asString ?: ""
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

data class CreateRackRequest(
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("floorId") val floorId: String,
    @SerializedName("sectionId") val sectionId: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("sequenceOrder") val sequenceOrder: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("rackType") val rackType: String,
    @SerializedName("maxQuantityCapacity") val maxQuantityCapacity: Int,
    @SerializedName("maxWeightCapacityKg") val maxWeightCapacityKg: Double,
    @SerializedName("status") val status: String
)

data class RackItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("warehouseId") val warehouseElement: JsonElement? = null,
    @SerializedName("floorId") val floorElement: JsonElement? = null,
    @SerializedName("sectionId") val sectionElement: JsonElement? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("sequenceOrder") val sequenceOrder: Int = 1,
    @SerializedName("description") val description: String? = null,
    @SerializedName("rackType") val rackType: String? = "",
    @SerializedName("maxQuantityCapacity") val maxQuantityCapacity: Int = 0,
    @SerializedName("maxWeightCapacityKg") val maxWeightCapacityKg: Double = 0.0,
    @SerializedName("binsCount") val binsCount: Int = 0,
    @SerializedName("status") val status: String = "",
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    val warehouseName: String
        get() {
            if (warehouseElement == null) return ""
            return try {
                if (warehouseElement.isJsonObject) {
                    warehouseElement.asJsonObject.get("name")?.asString ?: ""
                } else ""
            } catch (_: Exception) { "" }
        }

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

    val floorName: String
        get() {
            if (floorElement == null) return ""
            return try {
                if (floorElement.isJsonObject) {
                    floorElement.asJsonObject.get("name")?.asString ?: ""
                } else ""
            } catch (_: Exception) { "" }
        }

    val floorIdValue: String
        get() {
            if (floorElement == null) return ""
            return try {
                if (floorElement.isJsonObject) {
                    floorElement.asJsonObject.get("_id")?.asString ?: ""
                } else if (floorElement.isJsonPrimitive) {
                    floorElement.asString
                } else ""
            } catch (_: Exception) { "" }
        }

    val sectionName: String
        get() {
            if (sectionElement == null) return ""
            return try {
                if (sectionElement.isJsonObject) {
                    sectionElement.asJsonObject.get("name")?.asString ?: ""
                } else ""
            } catch (_: Exception) { "" }
        }

    val sectionCode: String
        get() {
            if (sectionElement == null) return ""
            return try {
                if (sectionElement.isJsonObject) {
                    sectionElement.asJsonObject.get("code")?.asString ?: ""
                } else ""
            } catch (_: Exception) { "" }
        }

    val sectionIdValue: String
        get() {
            if (sectionElement == null) return ""
            return try {
                if (sectionElement.isJsonObject) {
                    sectionElement.asJsonObject.get("_id")?.asString ?: ""
                } else if (sectionElement.isJsonPrimitive) {
                    sectionElement.asString
                } else ""
            } catch (_: Exception) { "" }
        }

    val sectionDisplayName: String
        get() = sectionName.ifBlank { sectionCode.ifBlank { "N/A" } }
}

data class CreateBinRequest(
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("floorId") val floorId: String,
    @SerializedName("sectionId") val sectionId: String,
    @SerializedName("rackId") val rackId: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("sequenceOrder") val sequenceOrder: Int,
    @SerializedName("binType") val binType: String,
    @SerializedName("maxQuantity") val maxQuantity: Int,
    @SerializedName("maxWeightKg") val maxWeightKg: Double,
    @SerializedName("defaultUOM") val defaultUOM: String,
    @SerializedName("status") val status: String
)
