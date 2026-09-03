package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

data class InventoryItemListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("pagination") val pagination: InventoryPagination? = null,
    @SerializedName("items") private val _items: List<InventoryItem>? = null,
    @SerializedName("data") private val _data: List<InventoryItem>? = null
) {
    // Provides .data to prevent compiler breakage across ViewModels/Repos
    val data: List<InventoryItem>
        get() = _items ?: _data ?: emptyList()
}

data class InventoryPagination(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1
)

data class InventoryItem(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String = "",
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("parentGroupId") val parentGroupId: String? = null,
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("attributes") val attributes: Map<String, @JvmSuppressWildcards Any?>? = emptyMap(),
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("type") val type: String = "goods",
    @SerializedName("unit") val unit: String = "",
    @SerializedName("costPrice") val costPrice: Double = 0.0,
    @SerializedName("sellingPrice") val sellingPrice: Double = 0.0,
    @SerializedName("trackInventory") val trackInventory: Boolean = true,
    @SerializedName("isSerialTracked") val isSerialTracked: Boolean = false,
    @SerializedName("openingStock") val openingStock: Double? = 0.0,
    @SerializedName("currentStock") val currentStock: Double = 0.0,
    @SerializedName("reservedStock") val reservedStock: Double = 0.0,
    @SerializedName("incomingStock") val incomingStock: Double = 0.0,
    @SerializedName("wipStock") val wipStock: Double = 0.0,
    @SerializedName("reorderPoint") val reorderPoint: Double = 0.0,
    @SerializedName("reorderLevel") val reorderLevel: Double = 0.0,
    @SerializedName("safetyStock") val safetyStock: Double = 0.0,
    @SerializedName("status") val status: String = "active",
    @SerializedName("images") val images: List<InventoryImage> = emptyList(),
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = "",
    @SerializedName("stockStatus") private val _stockStatus: String? = null
) {
    val stockStatus: String
        get() = _stockStatus ?: when {
            !trackInventory -> "Not Tracked"
            currentStock <= 0 -> "Out of Stock"
            currentStock <= (if (reorderPoint > 0) reorderPoint else reorderLevel) -> "Low Stock"
            else -> "In Stock"
        }
}

data class InventoryImage(
    @SerializedName("fileUrl") val fileUrl: String = "",
    @SerializedName("publicId") val publicId: String? = null,
    @SerializedName("_id") val _id: String? = null
)

data class InventoryItemDetailResponse(
    val success: Boolean = false,
    val data: InventoryItem
)

data class InventoryHealthDisplay(
    val totalStockValue: Double,
    val available: Double,
    val reserved: Double,
    val wip: Double,
    val incoming: Double,
    val lowThreshold: Double,
    val isTracked: Boolean
)

fun InventoryItem.toHealthDisplay(): InventoryHealthDisplay {
    return InventoryHealthDisplay(
        totalStockValue = costPrice * currentStock,
        available = (currentStock - reservedStock).coerceAtLeast(0.0),
        reserved = reservedStock,
        wip = wipStock,
        incoming = incomingStock,
        lowThreshold = if (reorderPoint > 0) reorderPoint else reorderLevel,
        isTracked = trackInventory
    )
}

data class AdjustStockRequest(
    val itemId: String,
    val adjustmentType: String,
    val quantity: Double,
    val reason: String,
    val notes: String
)

object ItemType {
    const val IN_HOUSE = "goods"
    const val CLIENT = "service"
}

data class CreateItemFormState(
    val itemId: String? = null,
    val existingImageUrl: String? = null,
    val itemType: String = ItemType.IN_HOUSE,
    val name: String = "",
    val sku: String = "",
    val category: String = "",
    val status: String = "active",
    val unit: String = "",
    val autoGenerateSku: Boolean = true,
    val returnable: Boolean = false,
    val hsnCode: String = "",
    val taxPercentage: String = "",
    val taxInclusive: Boolean = false,
    val length: String = "",
    val width: String = "",
    val height: String = "",
    val weight: String = "",
    val manufacturer: String = "",
    val brand: String = "",
    val barcode: String = "",
    val sellingPrice: String = "",
    val salesAccount: String = "",
    val salesDescription: String = "",
    val costPrice: String = "",
    val purchaseAccount: String = "",
    val preferredVendor: String = "",
    val purchaseDescription: String = "",
    val trackInventory: Boolean = false,
    val isSerialTracked: Boolean = false,
    val inventoryAccount: String = "",
    val openingStock: String = "",
    val imageUri: android.net.Uri? = null
) {
    fun validate(): String? = when {
        name.isBlank() -> "Item name is required"
        !autoGenerateSku && sku.isBlank() -> "SKU is required"
        unit.isBlank() -> "Please select a unit of measure"
        sellingPrice.isBlank() -> "Selling price is required"
        costPrice.isBlank() -> "Cost price is required"
        else -> null
    }
}

data class InventoryViewOneResponse(
    val success: Boolean = false,
    val data: InventoryItemviewone
)

data class InventoryItemviewone(
    @SerializedName("_id") val _id: String = "",
    val organizationId: String = "",
    val name: String = "",
    val sku: String = "",
    val parentGroupId: String? = null,
    val attributes: Map<String, @JvmSuppressWildcards Any?>? = emptyMap(),
    val warehouseId: String? = null,
    val type: String = "goods",
    val unit: String = "",
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val trackInventory: Boolean = false,
    val isSerialTracked: Boolean = false,
    val openingStock: Double? = 0.0,
    val currentStock: Double = 0.0,
    val reservedStock: Double = 0.0,
    val incomingStock: Double = 0.0,
    val wipStock: Double = 0.0,
    val reorderPoint: Double = 0.0,
    val status: String = "active",
    val images: List<InventoryItemImage> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = "",
    val stockStatus: String = "In Stock"
)

data class InventoryItemImage(
    val fileUrl: String = "",
    val publicId: String = "",
    @SerializedName("_id") val _id: String = ""
)
//=================================================================================================
//low stock alert

// ── Generic Wrapper for Low Stock API ──
data class LowStockResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("pagination") val pagination: Any? = null,
    @SerializedName("data") val data: List<LowStockItemDto>? = emptyList(),
    @SerializedName("message") val message: String? = null
)

// ── Low Stock Item DTO matching Backend JSON ──
data class LowStockItemDto(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("variantLabel") val variantLabel: String? = null,
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("warehouseName") val warehouseName: String? = null,
    @SerializedName("available") val available: Double = 0.0,
    @SerializedName("reserved") val reserved: Double = 0.0,
    @SerializedName("reorderLevel") val reorderLevel: Double = 0.0,
    @SerializedName("safetyStock") val safetyStock: Double = 0.0,
    @SerializedName("preferredVendorId") val preferredVendorId: String? = null,
    @SerializedName("unit") val unit: String? = "pcs",
    @SerializedName("costPrice") val costPrice: Double = 0.0,
    @SerializedName("severity") val severity: String = "Critical",
    @SerializedName("suggestedQty") val suggestedQty: Double = 0.0,
    @SerializedName("stockUtilizationPercent") val stockUtilizationPercent: Double = 0.0
)

//==================================================================================================

// ── PO Item Request Entry ──
data class CreatePoItemRequest(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("qty") val qty: Double,
    @SerializedName("rate") val rate: Double,
    @SerializedName("taxPercent") val taxPercent: Double = 18.0
)

// ── Create Purchase Order Request Payload ──
data class CreatePurchaseOrderRequest(
    @SerializedName("supplierId") val supplierId: String,
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("eta") val eta: String?,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("poType") val poType: String = "Standard",
    @SerializedName("items") val items: List<CreatePoItemRequest>,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("shippingCost") val shippingCost: Double = 0.0,
    @SerializedName("internalNotes") val internalNotes: String? = null
)

// ── Response Models ──
data class PoAttachment(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("fileUrl") val fileUrl: String? = null,
    @SerializedName("publicId") val publicId: String? = null
)

data class PoItemResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("itemId") val itemId: String,
    @SerializedName("qty") val qty: Double = 0.0,
    @SerializedName("receivedQty") val receivedQty: Double = 0.0,
    @SerializedName("billedQty") val billedQty: Double = 0.0,
    @SerializedName("receiveStatus") val receiveStatus: String = "Not Received",
    @SerializedName("billStatus") val billStatus: String = "Not Billed",
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxAmount") val taxAmount: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0
)

data class PurchaseOrderData(
    @SerializedName("_id") val id: String,
    @SerializedName("poNumber") val poNumber: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("supplierId") val supplierId: String?,
    @SerializedName("branchId") val branchId: String?,
    @SerializedName("warehouseId") val warehouseId: String?,
    @SerializedName("poDate") val poDate: String?,
    @SerializedName("eta") val eta: String?,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("poType") val poType: String = "Standard",
    @SerializedName("items") val items: List<PoItemResponse> = emptyList(),
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxTotal") val taxTotal: Double = 0.0,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("shippingCost") val shippingCost: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("orderStatus") val orderStatus: String = "Draft",
    @SerializedName("receiveStatus") val receiveStatus: String = "Not Received",
    @SerializedName("billStatus") val billStatus: String = "Not Billed",
    @SerializedName("paymentStatus") val paymentStatus: String = "Unpaid",
    @SerializedName("lifecycleStatus") val lifecycleStatus: String = "Open",
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("attachments") val attachments: List<PoAttachment> = emptyList(),
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class CreatePurchaseOrderResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: PurchaseOrderData? = null,
    @SerializedName("message") val message: String? = null
)