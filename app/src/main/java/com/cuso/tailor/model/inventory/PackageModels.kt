@file:Suppress("unused")
package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

data class PackageResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("pagination") val pagination: Any? = null,
    @SerializedName("summary") val summary: PackageSummaryDto? = null,
    @SerializedName("data") val data: List<PackageItemDto> = emptyList()
)

data class PackageSummaryDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("notShipped") val notShipped: Int = 0,
    @SerializedName("shipped") val shipped: Int = 0,
    @SerializedName("delivered") val delivered: Int = 0
)

data class PackageItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("packageNumber") val packageNumber: String? = null,
    @SerializedName("picklistId") val picklistId: String? = null,
    @SerializedName("salesOrderId") val salesOrderId: String? = null,
    @SerializedName("customerId") val customerId: PackageCustomerDto? = null,
    @SerializedName("warehouseId") val warehouseId: PackageWarehouseDto? = null,
    @SerializedName("shipmentId") val shipmentId: Any? = null,
    @SerializedName("items") val items: List<PackageInnerItemDto> = emptyList(),
    @SerializedName("status") val status: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    // Total quantity of all inner items
    val totalQty: Double
        get() = items.sumOf { it.qty.toDouble() }

    // Customer display name fallback
    val customerDisplay: String
        get() = customerId?.fullName ?: customerId?.name ?: "Customer"
}

data class PackageCustomerDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("fullName") val fullName: String? = null
)

data class PackageWarehouseDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null
)

data class PackageInnerItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("itemId") val itemId: PackageItemDetailDto? = null
)

data class PackageItemDetailDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null
)