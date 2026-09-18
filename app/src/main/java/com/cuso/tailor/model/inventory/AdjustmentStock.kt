@file:Suppress("unused", "AssignedValueIsNeverUsed")

package com.cuso.tailor.model.inventory

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// =============================================================================
// 1. REASON & BASE RESPONSE MODELS
// =============================================================================

data class StockAdjustmentListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<StockAdjustmentData> = emptyList(),
    @SerializedName("pagination") val pagination: AdjustmentPaginationDto? = null,
    // Fallbacks if backend sends them directly at root
    @SerializedName("total") val totalDirect: Int? = null,
    @SerializedName("page") val pageDirect: Int? = null,
    @SerializedName("limit") val limitDirect: Int? = null
) {
    val totalCount: Int
        get() = pagination?.total ?: totalDirect ?: data.size

    val currentPage: Int
        get() = pagination?.page ?: pageDirect ?: 1

    val pageSize: Int
        get() = pagination?.limit ?: limitDirect ?: 20

    val totalPagesCount: Int
        get() = pagination?.totalPages ?: 1
}

data class AdjustmentPaginationDto(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 1
)

data class StockAdjustmentDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: StockAdjustmentData? = null,
    @SerializedName("message") val message: String? = null
)

data class AdjustmentReasonsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<String> = emptyList()
)

/**
 * Top-level response for stock summary list.
 */
data class StockSummaryListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") private val _data: List<StockSummaryItemDto>? = null,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("pageSize") val pageSize: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1
) {
    val data: List<StockSummaryItemDto>
        get() = _data ?: emptyList()
}

/**
 * Individual item stock summary DTO matching backend JSON.
 */
data class StockSummaryItemDto(
    @SerializedName("itemId") private val rawItemId: JsonElement? = null,
    @SerializedName("product") val product: String = "",
    @SerializedName("sku") val sku: String = "",
    @SerializedName("variant") val variant: String? = null,
    @SerializedName("warehouseId") private val rawWarehouseId: JsonElement? = null,
    @SerializedName("warehouse") val warehouse: String = "",
    @SerializedName("reserved") val reserved: Double = 0.0,
    @SerializedName("available") val available: Double = 0.0,
    @SerializedName("reorderLevel") val reorderLevel: Double = 0.0,
    @SerializedName("lastUpdated") val lastUpdated: String? = null,
    @SerializedName("unit") val unit: String? = "pcs"
) {
    val itemId: String
        get() = extractJsonId(rawItemId)

    val warehouseId: String
        get() = extractJsonId(rawWarehouseId)
}

// =============================================================================
// 2. CRASH-PROOF STOCK ADJUSTMENT DATA MODEL
// =============================================================================

data class StockAdjustmentData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("adjustmentCode") val adjustmentCode: String? = null,

    // Handles String ID or Populated Object { _id, name, sku, variantLabel, ... }
    @SerializedName("itemId") val rawItem: JsonElement? = null,

    @SerializedName("adjustmentType") val adjustmentType: String? = "increase",
    @SerializedName("quantity") val quantity: Double = 0.0,
    @SerializedName("unit") val unit: String? = "pcs",
    @SerializedName("origin") val origin: AdjustmentLocationData? = null,
    @SerializedName("destination") val destination: AdjustmentLocationData? = null,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("referenceNumber") val referenceNumber: String? = null,

    // Handles String or Populated User Objects
    @SerializedName("handledBy") val rawHandledBy: JsonElement? = null,
    @SerializedName("createdBy") val rawCreatedBy: JsonElement? = null,

    @SerializedName("status") val status: String? = "completed",

    // Handles String or Populated Reversal Object
    @SerializedName("reversalOfId") val rawReversalOfId: JsonElement? = null,

    // Handles Array of Strings or Array of Objects
    @SerializedName("stockLedgerIds") val rawStockLedgerIds: JsonElement? = null,

    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    val _id: String get() = id
    val type: String get() = adjustmentType ?: "increase"
    val handledByName: String
        get() {
            if (rawHandledBy == null || rawHandledBy.isJsonNull) return "—"
            if (rawHandledBy.isJsonObject) {
                val obj = rawHandledBy.asJsonObject
                val first = obj.get("firstName")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
                val last = obj.get("lastName")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
                val full = "$first $last".trim()
                return if (full.isNotBlank()) full else obj.get("name")?.takeIf { !it.isJsonNull }?.asString ?: "—"
            }
            if (rawHandledBy.isJsonPrimitive) return rawHandledBy.asString
            return "—"
        }
    // Safe getters for UI
    val itemId: String get() = extractJsonId(rawItem)
    val itemName: String get() = extractJsonName(rawItem)
    val itemSku: String get() = extractJsonSku(rawItem)
    val itemVariant: String get() = extractJsonVariant(rawItem)
    val reversalOfId: String? get() = extractJsonId(rawReversalOfId).takeIf { it.isNotBlank() }
    val isReversed: Boolean get() = status.equals("reversed", ignoreCase = true)

    // Origin/Destination Helpers
    val originWarehouseName: String get() = origin?.warehouseName ?: "—"
    val originWarehouseCode: String get() = origin?.warehouseCode ?: "—"
    val destinationWarehouseName: String get() = destination?.warehouseName ?: "—"
    val destinationWarehouseCode: String get() = destination?.warehouseCode ?: "—"
}

data class AdjustmentLocationData(
    // Handles String warehouseId OR Object warehouseId { _id, name, code }
    @SerializedName("warehouseId") val rawWarehouseId: JsonElement? = null,

    // Handles String binId OR Object binId { _id, name, code }
    @SerializedName("binId") val rawBinId: JsonElement? = null,

    @SerializedName("before") val before: Double? = 0.0,
    @SerializedName("after") val after: Double? = 0.0
) {
    val warehouseId: String? get() = extractJsonId(rawWarehouseId).takeIf { it.isNotBlank() }
    val warehouseName: String? get() = extractJsonName(rawWarehouseId).takeIf { it.isNotBlank() }
    val warehouseCode: String? get() = extractJsonCode(rawWarehouseId).takeIf { it.isNotBlank() }
    val binId: String? get() = extractJsonId(rawBinId).takeIf { it.isNotBlank() }
}

// =============================================================================
// 3. SAFE PARSING EXTENSION HELPERS
// =============================================================================

private fun extractJsonId(element: JsonElement?): String {
    if (element == null || element.isJsonNull) return ""
    if (element.isJsonPrimitive) return element.asString
    if (element.isJsonObject) {
        val obj = element.asJsonObject
        return obj.get("_id")?.takeIf { !it.isJsonNull }?.asString
            ?: obj.get("id")?.takeIf { !it.isJsonNull }?.asString
            ?: ""
    }
    return ""
}

private fun extractJsonName(element: JsonElement?): String {
    if (element == null || element.isJsonNull) return ""
    if (element.isJsonObject) {
        val obj = element.asJsonObject
        return obj.get("name")?.takeIf { !it.isJsonNull }?.asString
            ?: obj.get("firstName")?.takeIf { !it.isJsonNull }?.asString
            ?: ""
    }
    return ""
}

private fun extractJsonSku(element: JsonElement?): String {
    if (element == null || element.isJsonNull) return ""
    if (element.isJsonObject) {
        val obj = element.asJsonObject
        return obj.get("sku")?.takeIf { !it.isJsonNull }?.asString ?: ""
    }
    return ""
}

private fun extractJsonVariant(element: JsonElement?): String {
    if (element == null || element.isJsonNull) return ""
    if (element.isJsonObject) {
        val obj = element.asJsonObject
        return obj.get("variantLabel")?.takeIf { !it.isJsonNull }?.asString
            ?: obj.get("variant")?.takeIf { !it.isJsonNull }?.asString
            ?: ""
    }
    return ""
}

private fun extractJsonCode(element: JsonElement?): String {
    if (element == null || element.isJsonNull) return ""
    if (element.isJsonObject) {
        val obj = element.asJsonObject
        return obj.get("code")?.takeIf { !it.isJsonNull }?.asString ?: ""
    }
    return ""
}

// =============================================================================
// 4. REQUEST MODELS
// =============================================================================

data class AdjustStockQuantityRequest(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("adjustmentType") val adjustmentType: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("reason") val reason: String,
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("binId") val binId: String? = null,
    @SerializedName("referenceNumber") val referenceNumber: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class ReverseAdjustmentRequest(
    @SerializedName("reason") val reason: String? = "Other",
    @SerializedName("notes") val notes: String? = null
)

data class IncreaseStockRequest(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("reason") val reason: String,
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("binId") val binId: String? = null,
    @SerializedName("referenceNumber") val referenceNumber: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class DecreaseStockRequest(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("reason") val reason: String,
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("binId") val binId: String? = null,
    @SerializedName("referenceNumber") val referenceNumber: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class TransferStockRequest(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("fromWarehouseId") val fromWarehouseId: String,
    @SerializedName("fromBinId") val fromBinId: String? = null,
    @SerializedName("toWarehouseId") val toWarehouseId: String,
    @SerializedName("toBinId") val toBinId: String? = null,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("reason") val reason: String? = null
)