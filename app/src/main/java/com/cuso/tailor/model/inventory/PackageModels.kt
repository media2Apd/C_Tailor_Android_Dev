@file:Suppress("unused")

package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

data class PackageResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("pagination") val pagination: PackagePaginationDto? = null,
    @SerializedName("summary") val summary: PackageSummaryDto? = null,
    @SerializedName("data") val data: List<PackageItemDto> = emptyList()
)

data class PackagePaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

data class PackageSummaryDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("readyForShipment") val readyForShipment: Int = 0,
    @SerializedName("notShipped") val notShipped: Int = 0,
    @SerializedName("shipped") val shipped: Int = 0,
    @SerializedName("delivered") val delivered: Int = 0
)

data class PackageItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("packageNumber") val packageNumber: String? = null,
    @SerializedName("picklistIds") val picklistIds: List<PackagePicklistRefDto> = emptyList(),
    @SerializedName("salesOrderId") val salesOrderId: PackageSalesOrderDto? = null, // Object in JSON!
    @SerializedName("customerId") val customerId: PackageCustomerDto? = null,
    @SerializedName("warehouseId") val warehouseId: PackageWarehouseDto? = null,
    @SerializedName("shipmentId") val shipmentId: Any? = null,
    @SerializedName("items") val items: List<PackageInnerItemDto> = emptyList(),
    @SerializedName("status") val status: String? = null,
    @SerializedName("statusHistory") val statusHistory: List<PackageStatusHistoryDto> = emptyList(),
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    // Total quantity of all items in this package
    val totalQty: Double
        get() = items.sumOf { it.qty.toDouble() }

    // Customer display name fallback
    val customerDisplay: String
        get() = customerId?.fullName ?: customerId?.name ?: "Customer"

    // Helper for Sales Order Code
    val salesOrderCode: String
        get() = salesOrderId?.orderCode ?: "-"

    // Helper for Warehouse Name
    val warehouseName: String
        get() = warehouseId?.name ?: "-"
}

data class PackageSalesOrderDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("orderCode") val orderCode: String? = null
)

data class PackagePicklistRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("picklistNumber") val picklistNumber: String? = null
)

data class PackageCustomerDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("sameAsBillingAddress") val sameAsBillingAddress: Boolean = true
)

data class PackageWarehouseDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null
)

data class PackageInnerItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("picklistId") val picklistId: String? = null,
    @SerializedName("picklistItemId") val picklistItemId: String? = null,
    @SerializedName("itemId") val itemId: Any? = null,
    @SerializedName("soLineId") val soLineId: String? = null,
    @SerializedName("lineType") val lineType: String? = null,
    @SerializedName("itemName") val itemName: String? = null,
    @SerializedName("requiresStock") val requiresStock: Boolean = false,
    @SerializedName("qty") val qty: Int = 0
)

data class PackageStatusHistoryDto(
    @SerializedName("fromStatus") val fromStatus: String? = null,
    @SerializedName("toStatus") val toStatus: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("changedBy") val changedBy: Any? = null,   // object or string
    @SerializedName("changedAt") val changedAt: String? = null
) {
    val changedByName: String
        get() = when (val c = changedBy) {
            is String -> c
            is Map<*, *> -> (c["fullName"] ?: c["name"] ?: c["_id"])?.toString() ?: "-"
            else -> "-"
        }
}

data class CreatePackageRequest(
    @SerializedName("picklistIds") val picklistId: List<String> = emptyList(),
    @SerializedName("estimatedDelivery") val estimatedDelivery: String? = null,
    @SerializedName("items") val items: List<CreatePackageItemRequest> = emptyList()
)

data class CreatePackageItemRequest(
    @SerializedName("picklistItemId") val picklistItemId: String,
    @SerializedName("qty") val qty: Int
)

data class CreatePackageResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    // Keep raw JSON, we do not need to map it for create/update
    @SerializedName("data") val data: com.google.gson.JsonElement? = null
)
/**
 * Response for View One Package API (/api/inventory/package/view-one/{id})
 */
data class PackageViewOneResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: PackageItemDto? = null,
    @SerializedName("message") val message: String? = null
)

/**
 * Payload for updating an existing package (/api/inventory/package/update-one/{id})
 */
data class UpdatePackageRequest(
    @SerializedName("estimatedDelivery") val estimatedDelivery: String? = null
)