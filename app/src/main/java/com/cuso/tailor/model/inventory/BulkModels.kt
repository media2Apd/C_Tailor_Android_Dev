@file:Suppress("unused")
package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

data class BulkItem(
    val id: String = "",
    val name: String = "",
    val sku: String = "",
    val stockOnHand: Double = 0.0,
    val reorderPoint: Double = 0.0,
    val status: String = "Low Soon",
    val isSelected: Boolean = false,
    val category: String = "Furniture Sets",
    val warehouse: String = "Factory A",
    val costPrice: Double = 812.00,
    val sellingPrice: Double = 963.00,
    val openingStock: Double = 21.00,
    val committedStock: Double = 0.00,
    val availableForSale: Double = 21.00,
    val physicalStock: Double = 21.00,
    val taxCategory: String = "General (18%)",
    val itemType: String = "Inventory Item",
    val createdSource: String = "Manual Entry"
)

data class BulkComponentItem(
    val id: String,
    val name: String,
    val sku: String,
    val stockQty: Int,
    val requiredQty: Int,
    val unitCost: Double,
    val iconType: String = "bed"
)

data class BulkTransactionItem(
    val id: String,
    val type: String,
    val partyName: String,
    val date: String,
    val qtyChange: String,
    val amount: String,
    val status: String
)

data class AssociatedProductItem(
    val name: String,
    val sku: String,
    val accStock: String,
    val qtyReq: String,
    val totalValue: String,
    val icon: Int
)

data class BulkCategoryDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String
)

data class BulkWarehouseDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null
)

data class PerformedByDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").trim().ifBlank { "—" }
}

data class BaseBulkResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)

data class BuildBulkItemRequest(
    @SerializedName("qty") val qty: Int,
    @SerializedName("warehouseId") val warehouseId: String?,
    @SerializedName("remarks") val remarks: String? = null
)

data class AdjustBulkStockRequest(
    @SerializedName("qty") val qty: Int,
    @SerializedName("warehouseId") val warehouseId: String?,
    @SerializedName("remarks") val remarks: String? = null
)

data class BulkImageDto(
    @SerializedName("fileUrl") val fileUrl: String? = null,
    @SerializedName("publicId") val publicId: String? = null
)

data class BulkComponentItemDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("costPrice") val costPrice: Double? = 0.0,
    @SerializedName("sellingPrice") val sellingPrice: Double? = 0.0,
    @SerializedName("marginPercent") val marginPercent: Double? = 0.0,
    @SerializedName("currentStockValue") val currentStockValue: Double? = 0.0
)

data class BulkComponentEntry(
    @SerializedName("itemId") val rawItemId: Any? = null,
    @SerializedName("qtyRequired") val qtyRequired: Int = 1,
    @SerializedName("costPriceSnapshot") val costPriceSnapshot: Double = 0.0,
    @SerializedName("_id") val id: String? = null
) {
    val itemName: String
        get() = (rawItemId as? Map<*, *>)?.get("name") as? String ?: "—"

    val itemSku: String
        get() = (rawItemId as? Map<*, *>)?.get("sku") as? String ?: "—"
}

data class StockByWarehouseItem(
    @SerializedName("warehouseId") val rawWarehouseId: Any? = null,
    @SerializedName("currentStock") val currentStock: Double = 0.0,
    @SerializedName("committedStock") val committedStock: Double = 0.0
) {
    val warehouseName: String?
        get() = when (val w = rawWarehouseId) {
            is Map<*, *> -> w["name"] as? String
            else -> null
        }
}

data class AssemblyLogItem(
    @SerializedName("type") val type: String = "",
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("warehouseId") val rawWarehouseId: Any? = null,
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("performedBy") val rawPerformedBy: Any? = null,
    @SerializedName("performedAt") val performedAt: String? = null,
    @SerializedName("_id") val id: String? = null
) {
    val warehouseName: String?
        get() = when (val w = rawWarehouseId) {
            is Map<*, *> -> w["name"] as? String
            else -> null
        }

    val performerName: String
        get() = when (val p = rawPerformedBy) {
            is Map<*, *> -> {
                val f = p["firstName"] as? String
                val l = p["lastName"] as? String
                listOfNotNull(f, l).joinToString(" ").trim().ifBlank { "System" }
            }
            is String -> p.take(8)
            else -> "System"
        }
}

data class AssociatedItemDto(
    @SerializedName("itemId") val itemId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String = "",
    @SerializedName("accountingStock") val accountingStock: Double = 0.0,
    @SerializedName("qtyRequired") val qtyRequired: Int = 1,
    @SerializedName("unitCost") val unitCost: Double = 0.0,
    @SerializedName("totalValue") val totalValue: Double = 0.0
)

data class InventorySnapshotDto(
    @SerializedName("openingStock") val openingStock: Double = 0.0,
    @SerializedName("reorderPoint") val reorderPoint: Double = 0.0,
    @SerializedName("stockOnHand") val stockOnHand: Double = 0.0,
    @SerializedName("committedStock") val committedStock: Double = 0.0,
    @SerializedName("availableForSale") val availableForSale: Double = 0.0,
    @SerializedName("actualPhysicalStock") val actualPhysicalStock: Double = 0.0
)

data class BulkItemDoc(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("categoryId") val rawCategory: Any? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("unit") val unit: String = "set",
    @SerializedName("image") val image: BulkImageDto? = null,
    @SerializedName("components") val components: List<BulkComponentEntry> = emptyList(),
    @SerializedName("costPrice") val costPrice: Double = 0.0,
    @SerializedName("sellingPrice") val sellingPrice: Double = 0.0,
    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
    @SerializedName("taxCategory") val taxCategory: String? = null,
    @SerializedName("purchaseAccountId") val purchaseAccountId: String? = null,
    @SerializedName("salesAccountId") val salesAccountId: String? = null,
    @SerializedName("trackInventory") val trackInventory: Boolean = true,
    @SerializedName("assemblyType") val assemblyType: String = "On Order",
    @SerializedName("warehouseRestrictionId") val rawWarehouseRestriction: Any? = null,
    @SerializedName("reorderPoint") val reorderPoint: Double = 0.0,
    @SerializedName("openingStock") val openingStock: Double = 0.0,
    @SerializedName("status") val status: String = "active",
    @SerializedName("stockOnHand") val stockOnHand: Double = 0.0,
    @SerializedName("stockByWarehouse") val stockByWarehouse: List<StockByWarehouseItem> = emptyList(),
    @SerializedName("assemblyLog") val assemblyLog: List<AssemblyLogItem> = emptyList(),
    @SerializedName("associatedItems") val associatedItems: List<AssociatedItemDto> = emptyList(),
    @SerializedName("inventorySnapshot") val inventorySnapshot: InventorySnapshotDto? = null,
    @SerializedName("liveCostPrice") val liveCostPrice: Double = 0.0,
    @SerializedName("costPriceDrifted") val costPriceDrifted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    val warehouseRestrictionName: String?
        get() = when (val w = rawWarehouseRestriction) {
            is Map<*, *> -> w["name"] as? String
            else -> null
        }

    val categoryName: String?
        get() = when (val c = rawCategory) {
            is Map<*, *> -> c["name"] as? String
            is String -> c
            else -> null
        }
}

data class BulkItemListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<BulkItemDoc> = emptyList()
)

data class BulkItemDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: BulkItemDoc? = null,
    @SerializedName("message") val message: String? = null
)