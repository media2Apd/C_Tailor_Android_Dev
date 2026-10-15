@file:Suppress("unused","unusedVariable","AssignedValueIsNeverRead")

package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

data class PicklistListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<PicklistItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: PicklistPaginationDto? = null
)

data class PicklistPaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

data class PicklistItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("picklistNumber") val picklistNumber: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("assigneeId") val assignee: PicklistAssigneeDto? = null,
    @SerializedName("warehouseId") val warehouse: PicklistWarehouseDto? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("items") val items: List<PicklistLineItemDto> = emptyList(),
    @SerializedName("status") val status: String? = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class PicklistAssigneeDto(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
) {
    val fullName: String
        get() = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifBlank { "Unassigned" }
}

data class PicklistWarehouseDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null
)

data class PicklistLineItemDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("itemName") val itemName: String? = null,
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("qtyPicked") val qtyPicked: Int = 0,
    @SerializedName("qtyPacked") val qtyPacked: Int = 0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("salesOrderId") val salesOrder: PicklistSalesOrderRef? = null
)

data class PicklistSalesOrderRef(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("orderCode") val orderCode: String? = null
)
data class CreatePicklistResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)


/**
 * Wrapper response for Picklist View One API.
 */
data class PicklistViewOneResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PicklistDetailDto? = null
)

/**
 * Detail DTO for a single Picklist.
 */
data class PicklistDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("picklistNumber") val picklistNumber: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("assigneeId") val assignee: PicklistAssigneeDto? = null,
    @SerializedName("warehouseId") val warehouse: PicklistWarehouseDto? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("items") val items: List<PicklistDetailLineItemDto> = emptyList(),
    @SerializedName("status") val status: String? = "Pending",
    @SerializedName("convertedPackageIds") val convertedPackages: List<PicklistConvertedPackageDto> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null
)

data class PicklistConvertedPackageDto(
    @SerializedName("_id") val id: String,
    @SerializedName("packageNumber") val packageNumber: String? = null,
    @SerializedName("status") val status: String? = null
)

data class PicklistDetailLineItemDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("itemName") val itemName: String? = null,
    @SerializedName("lineType") val lineType: String? = null,
    @SerializedName("requiresStock") val requiresStock: Boolean = false,
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("qtyPicked") val qtyPicked: Int = 0,
    @SerializedName("qtyPacked") val qtyPacked: Int = 0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("salesOrderId") val salesOrder: PicklistSalesOrderRef? = null
)

data class PicklistDetailResponse(
    val success: Boolean = false,
    val data: PicklistDetailData? = null
)

data class PicklistDetailData(
    val _id: String = "",
    val picklistNumber: String = "",
    val date: String = "",
    val status: String = "",
    val items: List<PicklistItemDetail> = emptyList()
)

data class PicklistItemDetail(
    val _id: String = "",
    val itemName: String = "",
    val qty: Double = 0.0,
    val status: String = "",
    val salesOrderId: SalesOrderRef? = null
)

data class SalesOrderRef(
    val _id: String = "",
    val orderCode: String = ""
)


data class ReadyForDeliveryResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ReadyForDeliveryOrderDto> = emptyList(),
    @SerializedName("pagination") val pagination: PicklistPaginationDto? = null
)

data class ReadyForDeliveryOrderDto(
    @SerializedName("_id") val id: String,
    @SerializedName("orderCode") val orderCode: String? = null,
    @SerializedName("orderDate") val orderDate: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("customer") val customer: ReadyDeliveryCustomerDto? = null,
    @SerializedName("branch") val branch: ReadyDeliveryBranchDto? = null,
    @SerializedName("payment") val payment: ReadyDeliveryPaymentDto? = null,
    @SerializedName("deliveryMethod") val deliveryMethod: String? = null,
    @SerializedName("dispatchItemsCount") val dispatchItemsCount: Int = 0,
    @SerializedName("dispatchItems") val dispatchItems: List<ReadyDeliveryDispatchItemDto> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null
)

data class ReadyDeliveryCustomerDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("code") val code: String? = null
)

data class ReadyDeliveryBranchDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null
)

data class ReadyDeliveryPaymentDto(
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("advancePaid") val advancePaid: Double = 0.0,
    @SerializedName("balanceDue") val balanceDue: Double = 0.0,
    @SerializedName("paymentStatus") val paymentStatus: String? = null,
    @SerializedName("paymentMode") val paymentMode: String? = null
)

data class ReadyDeliveryDispatchItemDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("lineType") val lineType: String? = null,
    @SerializedName("itemName") val itemName: String? = null,
    @SerializedName("itemDescription") val itemDescription: String? = null,
    @SerializedName("quantity") val quantity: Int = 0,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("lineTotal") val lineTotal: Double = 0.0
)
data class CreatePicklistRequest(
    @SerializedName("date") val date: String,
    @SerializedName("assigneeId") val assigneeId: String? = null,
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("salesOrderId") val salesOrderId: String? = null,
    @SerializedName("items") val items: List<PicklistLineItemPayload> = emptyList()
)

data class PicklistLineItemPayload(
    @SerializedName("soLineId") val soLineId: String? = null,
    @SerializedName("salesOrderId") val salesOrderId: String? = null,
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("qtyPicked") val qtyPicked: Int = 0,
    @SerializedName("itemId") val itemId: String? = null,
    @SerializedName("lineType") val lineType: String? = null,
    @SerializedName("itemName") val itemName: String? = null
)
/**
 * Exact payload sent on Edit/Update matching the web request.
 */
data class UpdatePicklistRequest(
    @SerializedName("assigneeId") val assigneeId: String? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("items") val items: List<UpdatePicklistItemPayload> = emptyList()
)

data class UpdatePicklistItemPayload(
    @SerializedName("_id") val id: String,
    @SerializedName("qtyPicked") val qtyPicked: Int
)