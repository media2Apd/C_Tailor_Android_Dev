package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

data class StockLocationItemListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<StockLocationItemDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class StockLocationItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("parentGroupId") val parentGroupId: String? = null,
    @SerializedName("status") val status: String = "active",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("variantLabel") val variantLabel: String? = null,
    @SerializedName("totalStock") val totalStock: Double = 0.0,
    @SerializedName("locationCount") val locationCount: Int = 0,
    @SerializedName("category") val category: String? = null,
    @SerializedName("brand") val brand: String? = null
)

// ── Root View-One Response ──
data class StockLocationViewOneResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: StockLocationViewOneData
)

// ── Main Data Container ──
data class StockLocationViewOneData(
    @SerializedName("sourceType") val sourceType: String? = null,
    @SerializedName("overview") val overview: StockLocationOverviewDto? = null,
    @SerializedName("locationStock") val locationStock: List<LocationStockDto> = emptyList(),
    @SerializedName("variants") val variants: List<StockLocationVariantDto> = emptyList()
)

// ── Overview Section ──
data class StockLocationOverviewDto(
    @SerializedName("itemId") val itemId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("variantLabel") val variantLabel: String? = null,
    @SerializedName("itemType") val itemType: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("reorderLevel") val reorderLevel: Double = 0.0,
    @SerializedName("gstRate") val gstRate: String? = null, // Changed from Double? to String?
    @SerializedName("hsnCode") val hsnCode: String? = null,
    @SerializedName("sacCode") val sacCode: String? = null
)

// ── Location Breakdown Section ──
data class LocationStockDto(
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("warehouseName") val warehouseName: String? = null,
    @SerializedName("totalQuantity") val totalQuantity: Double = 0.0,
    @SerializedName("totalReserved") val totalReserved: Double = 0.0,
    @SerializedName("available") val available: Double = 0.0,
    @SerializedName("stockCondition") val stockCondition: String? = null,
    @SerializedName("rotationMethod") val rotationMethod: String? = null,
    @SerializedName("variantBreakdown") val variantBreakdown: List<VariantBreakdownDto> = emptyList()
)

// ── Variant Breakdown inside each Location ──
data class VariantBreakdownDto(
    @SerializedName("label") val label: String? = null,
    @SerializedName("qty") val qty: Double = 0.0
)

// ── Variant Items Section ──
data class StockLocationVariantDto(
    @SerializedName("itemId") val itemId: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("variantLabel") val variantLabel: String? = null,
    @SerializedName("totalStock") val totalStock: Double = 0.0,
    @SerializedName("status") val status: String = "active",
    @SerializedName("isCurrentItem") val isCurrentItem: Boolean = false
)

//dropdowns

// ── Shared Item Model for Floor, Section, Rack, and Bin ──
data class HierarchyDropdownItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("sequenceOrder") val sequenceOrder: Int = 0
)

// ── Floor Dropdown Response ──
data class FloorDropdownResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<HierarchyDropdownItem> = emptyList()
)

// ── Section Dropdown Response ──
data class SectionDropdownResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<HierarchyDropdownItem> = emptyList()
)

// ── Rack Dropdown Response ──
data class RackDropdownResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<HierarchyDropdownItem> = emptyList()
)

// ── Bin Dropdown Response ──
data class BinDropdownResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<HierarchyDropdownItem> = emptyList()
)

//create location

/**
 * Request payload for assigning inventory item to a warehouse bin.
 */
data class AssignStockLocationRequest(
    @SerializedName("itemId")
    val itemId: String,
    @SerializedName("binId")
    val binId: String,
    @SerializedName("quantity")
    val quantity: Double,
    @SerializedName("reservedQuantity")
    val reservedQuantity: Double = 0.0,
    @SerializedName("stockCondition")
    val stockCondition: String = "good",
    @SerializedName("rotationMethod")
    val rotationMethod: String = "fifo",
    @SerializedName("asOfDate")
    val asOfDate: String
)

/**
 * Response payload returned after stock location assignment.
 */
data class AssignStockLocationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: StockLocationAssignmentData
)

/**
 * Resulting location assignment record details.
 */
data class StockLocationAssignmentData(
    @SerializedName("_id")
    val mongoId: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("itemId")
    val itemId: String? = null,
    @SerializedName("warehouseId")
    val warehouseId: String? = null,
    @SerializedName("binId")
    val binId: String? = null,
    @SerializedName("quantity")
    val quantity: Double? = null,
    @SerializedName("reservedQuantity")
    val reservedQuantity: Double? = null,
    @SerializedName("availableQuantity")
    val availableQuantity: Double? = null,
    @SerializedName("stockCondition")
    val stockCondition: String? = null,
    @SerializedName("rotationMethod")
    val rotationMethod: String? = null,
    @SerializedName("asOfDate")
    val asOfDate: String? = null,
    @SerializedName("createdBy")
    val createdBy: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)