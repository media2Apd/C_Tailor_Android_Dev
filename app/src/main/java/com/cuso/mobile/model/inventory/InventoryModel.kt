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