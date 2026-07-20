package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

data class InventoryItemListResponse(
    val success: Boolean,
    val pagination: InventoryPagination,
    val data: List<InventoryItem>
)

data class InventoryPagination(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class InventoryItem(
    val _id: String,
    val organizationId: String,
    val name: String,
    val sku: String,
    val parentGroupId: String?,
    val attributes: Map<String, @JvmSuppressWildcards Any?>? = emptyMap(),
    val warehouseId: String?,
    val type: String,
    val unit: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val trackInventory: Boolean,
    val isSerialTracked: Boolean,
    val openingStock: Double?,
    val currentStock: Double,
    val reservedStock: Double,
    val incomingStock: Double,
    val wipStock: Double,
    val reorderPoint: Double,
    val status: String? = null,
    val images: List<InventoryImage> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val stockStatus: String
)

// ✅ small helper — UI code everywhere just wants a display URL
fun InventoryItem.firstImageUrl(): String? = images.firstOrNull()?.fileUrl

data class InventoryImage(
    val fileUrl: String,
    val publicId: String? = null,
    val _id: String? = null
)

// single-item response, in case you add a view-one endpoint later
data class InventoryItemDetailResponse(
    val success: Boolean,
    val data: InventoryItem
)


/**
 * If your existing InventoryItem model does NOT yet have every field shown
 * in the API response, make sure it includes these (add any missing ones):
 *
 * data class InventoryItem(
 *     val _id: String,
 *     val organizationId: String,
 *     val name: String,
 *     val sku: String,
 *     val parentGroupId: String?,
 *     val attributes: Map<String, String>?,
 *     val warehouseId: String?,
 *     val type: String,              // "goods"
 *     val unit: String,               // "pcs", "m" etc
 *     val costPrice: Double,
 *     val sellingPrice: Double,
 *     val trackInventory: Boolean,
 *     val isSerialTracked: Boolean,
 *     val openingStock: Double?,
 *     val currentStock: Double,
 *     val reservedStock: Double,
 *     val incomingStock: Double,
 *     val wipStock: Double,
 *     val reorderPoint: Double,
 *     val status: String,             // "active" / "inactive"
 *     val images: List<String>,
 *     val createdAt: String,
 *     val updatedAt: String,
 *     val stockStatus: String         // "Not Tracked" / "In Stock" etc
 * )
 */

/**
 * Small computed helper — used by the UI to derive the values shown on the
 * "Inventory Health" card (Total Stock Value, Available, Low Threshold),
 * since the API doesn't return these directly.
 */
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
        lowThreshold = reorderPoint,
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

//create item
// ── Create Item: form state + item type ──

object ItemType {
    const val IN_HOUSE = "in_house"
    const val CLIENT = "client"
}

// In com.cuso.mobile.model.inventory
data class CreateItemFormState(
    val itemId: String? = null,           // ✅ NEW - tracks if we are editing
    val existingImageUrl: String? = null, // ✅ NEW - shows image from server
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
    val isEditMode: Boolean get() = itemId != null   // NEW

    fun validate(): String? = when {
        name.isBlank() -> "Item name is required"
        !autoGenerateSku && sku.isBlank() -> "SKU is required"
        unit.isBlank() -> "Please select a unit of measure"
        sellingPrice.isBlank() -> "Selling price is required"
        costPrice.isBlank() -> "Cost price is required"
        else -> null
    }
}

//inventory view one
data class InventoryViewOneResponse(
    val success: Boolean,
    val data: InventoryItemviewone
)

data class InventoryItemviewone(
    @SerializedName("_id") val _id: String,
    val organizationId: String,
    val name: String,
    val sku: String,
    val parentGroupId: String?,
    val attributes: Map<String, @JvmSuppressWildcards Any>? = emptyMap(),
    val warehouseId: String?,
    val type: String,                     // "goods" | "service"
    val unit: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val trackInventory: Boolean,
    val isSerialTracked: Boolean,
    val openingStock: Double?,
    val currentStock: Double,
    val reservedStock: Double,
    val incomingStock: Double,
    val wipStock: Double,
    val reorderPoint: Double,
    val status: String,                   // "active" | "inactive" | "draft"
    val images: List<InventoryItemImage> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val stockStatus: String               // "In Stock" | "Out of Stock" | ...
)

data class InventoryItemImage(
    val fileUrl: String,
    val publicId: String,
    @SerializedName("_id") val _id: String
)