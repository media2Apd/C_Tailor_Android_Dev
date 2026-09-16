package com.cuso.mobile.model.inventory

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// =============================================================================
// REQUEST MODELS
// =============================================================================

data class GenerateBarcodeRequest(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("warehouseId") val warehouseId: String?,
    @SerializedName("barcodeType") val barcodeType: String = "EAN13",
    @SerializedName("labelSize") val labelSize: String = "Medium (50x25mm)",
    @SerializedName("quantity") val quantity: Int = 1
)

// =============================================================================
// RESPONSE MODELS
// =============================================================================

data class BarcodeListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("pagination") val pagination: Any? = null,
    @SerializedName("data") val data: List<BarcodeItemDoc> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class BarcodeDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: BarcodeItemDoc? = null,
    @SerializedName("message") val message: String? = null
)

data class DeleteBarcodeResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null
)

// =============================================================================
// CORE BARCODE ENTITY (Handles String ID vs Populated Object)
// =============================================================================

data class BarcodeItemDoc(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("sku") val sku: String = "",
    @SerializedName("barcodeType") val barcodeType: String = "EAN13",
    @SerializedName("barcodeNumber") val barcodeNumber: String = "",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("labelSize") val labelSize: String = "",
    @SerializedName("totalPrinted") val totalPrinted: Int = 0,
    @SerializedName("printLogs") val printLogs: List<BarcodePrintLog> = emptyList(),
    @SerializedName("status") val status: String = "Active",
    @SerializedName("createdBy") val createdBy: String = "",
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = "",

    // Raw Json Elements to support both String ID & Populated Object
    @SerializedName("itemId") private val _rawItem: JsonElement? = null,
    @SerializedName("warehouseId") private val _rawWarehouse: JsonElement? = null
) {
    val item: BarcodeItemRef?
        get() = try {
            if (_rawItem != null && _rawItem.isJsonObject) {
                Gson().fromJson(_rawItem, BarcodeItemRef::class.java)
            } else if (_rawItem != null && _rawItem.isJsonPrimitive) {
                BarcodeItemRef(id = _rawItem.asString)
            } else null
        } catch (_: Exception) {
            null
        }

    val warehouse: BarcodeWarehouseRef?
        get() = try {
            if (_rawWarehouse != null && _rawWarehouse.isJsonObject) {
                Gson().fromJson(_rawWarehouse, BarcodeWarehouseRef::class.java)
            } else if (_rawWarehouse != null && _rawWarehouse.isJsonPrimitive) {
                BarcodeWarehouseRef(id = _rawWarehouse.asString)
            } else null
        } catch (_: Exception) {
            null
        }
}

data class BarcodeItemRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String = "",
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("variantLabel") val variantLabel: String? = null,
    @SerializedName("marginPercent") val marginPercent: Double = 0.0,
    @SerializedName("currentStockValue") val currentStockValue: Double = 0.0
)

data class BarcodeWarehouseRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

data class BarcodePrintLog(
    @SerializedName("_id") val id: String = "",
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("labelSize") val labelSize: String = "",
    @SerializedName("printedBy") val printedBy: Any? = null,
    @SerializedName("printedAt") val printedAt: String = ""
)