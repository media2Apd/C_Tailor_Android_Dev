package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

// =============================================================================
// 1. REASON & BASE RESPONSE MODELS
// =============================================================================

data class AdjustmentReasonsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<String> = emptyList()
)

data class StockAdjustmentListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<StockAdjustmentData> = emptyList(),
    @SerializedName("pagination") val pagination: InventoryPagination? = null
)

data class StockAdjustmentDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: StockAdjustmentData? = null,
    @SerializedName("message") val message: String? = null
)

// =============================================================================
// 2. REQUEST MODELS
// =============================================================================

data class AdjustStockQuantityRequest(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("adjustmentType") val adjustmentType: String, // "increase" | "decrease"
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("reason") val reason: String,
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("binId") val binId: String? = null,
    @SerializedName("referenceNumber") val referenceNumber: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class TransferStockRequest(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("originWarehouseId") val originWarehouseId: String,
    @SerializedName("originBinId") val originBinId: String,
    @SerializedName("destinationWarehouseId") val destinationWarehouseId: String,
    @SerializedName("destinationBinId") val destinationBinId: String,
    @SerializedName("reason") val reason: String = "Stock Rebalancing",
    @SerializedName("referenceNumber") val referenceNumber: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class ReverseAdjustmentRequest(
    @SerializedName("reason") val reason: String? = "Other",
    @SerializedName("notes") val notes: String? = null
)

// =============================================================================
// 3. CORE ADJUSTMENT DTO & NESTED STRUCTURES
// =============================================================================

data class StockAdjustmentData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("adjustmentCode") val adjustmentCode: String = "",
    @SerializedName("itemId") val item: Any? = null, // Can be String ID or ItemRefDto
    @SerializedName("adjustmentType") val adjustmentType: String = "increase", // "increase", "decrease", "transfer"
    @SerializedName("quantity") val quantity: Double = 0.0,
    @SerializedName("unit") val unit: String? = "pcs",
    @SerializedName("origin") val origin: AdjustmentLocationData? = null,
    @SerializedName("destination") val destination: AdjustmentLocationData? = null,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("referenceNumber") val referenceNumber: String? = null,
    @SerializedName("handledBy") val handledBy: Any? = null,
    @SerializedName("status") val status: String = "completed", // "completed", "reversed"
    @SerializedName("reversalOfId") val reversalOfId: String? = null,
    @SerializedName("stockLedgerIds") val stockLedgerIds: List<String> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class AdjustmentLocationData(
    @SerializedName("warehouseId") val warehouse: Any? = null, // Can be String ID or LocationRefDto
    @SerializedName("binId") val bin: Any? = null,             // Can be String ID or LocationRefDto
    @SerializedName("before") val before: Double? = 0.0,
    @SerializedName("after") val after: Double? = 0.0
)

data class LocationRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null
)

data class ItemRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("variantLabel") val variantLabel: String? = null,
    @SerializedName("marginPercent") val marginPercent: Double? = 0.0
)