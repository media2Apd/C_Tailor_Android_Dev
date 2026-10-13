@file:Suppress("unused")

package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

data class PicklistResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("pagination") val pagination: Any? = null,
    @SerializedName("data") val data: List<PicklistItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class PicklistItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("picklistNumber") val picklistNumber: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("assigneeId") val assigneeId: PicklistAssigneeDto? = null,
    @SerializedName("warehouseId") val warehouseId: PicklistWarehouseDto? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("items") val items: List<PicklistSubItemDto> = emptyList(),
    @SerializedName("status") val status: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    val totalQty: Int
        get() = items.sumOf { it.qty }

    val totalItemsCount: Int
        get() = items.size

    val primarySalesOrderDisplay: String
        get() = items.firstOrNull()?.salesOrderId?.takeLast(6)?.let { "SO-$it" } ?: "N/A"
}

data class PicklistAssigneeDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Unassigned" }
}

data class PicklistWarehouseDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null
)

data class PicklistSubItemDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("itemId") val itemId: PicklistProductDto? = null,
    @SerializedName("salesOrderId") val salesOrderId: String? = null,
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("qtyPicked") val qtyPicked: Int = 0,
    @SerializedName("status") val status: String? = null
)

data class PicklistProductDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("marginPercent") val marginPercent: Double = 0.0,
    @SerializedName("currentStockValue") val currentStockValue: Double = 0.0
)