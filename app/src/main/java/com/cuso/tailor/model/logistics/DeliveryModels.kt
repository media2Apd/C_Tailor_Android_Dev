package com.cuso.tailor.model.logistics

import com.google.gson.annotations.SerializedName

/**
 * API response container for the delivery list.
 */
data class DeliveryListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("pagination")
    val pagination: DeliveryPaginationDto? = null,
    @SerializedName("data")
    val data: List<DeliveryItemDto> = emptyList(),
    @SerializedName("message")
    val message: String? = null
)

/**
 * Pagination details from the API.
 */
data class DeliveryPaginationDto(
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("limit")
    val limit: Int = 10,
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("totalPages")
    val totalPages: Int = 1
)

/**
 * Single delivery item matching your JSON response.
 */
data class DeliveryItemDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("deliveryId")
    val deliveryId: String,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("customer")
    val customer: String,
    @SerializedName("deliveryType")
    val deliveryType: String,
    @SerializedName("deliveryDate")
    val deliveryDate: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("action")
    val action: String = "Assign",
    @SerializedName("courierPartner")
    val courierPartner: String? = null
)

/**
 * Request body for assigning a delivery partner/courier.
 */
data class AssignDeliveryRequest(
    @SerializedName("staffId")
    val staffId: String,

    @SerializedName("staffInstructions")
    val staffInstructions: String,

    @SerializedName("targetCompletionDate")
    val targetCompletionDate: String
)

/**
 * Root API response for GET /api/logistics/delivery/view-one/:id
 */
data class DeliveryDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: DeliveryDetailData? = null,
    @SerializedName("message")
    val message: String? = null
)

/**
 * Main delivery detail entity.
 */
data class DeliveryDetailData(
    @SerializedName("_id")
    val id: String,
    @SerializedName("deliveryNumber")
    val deliveryNumber: String = "",
    @SerializedName("priority")
    val priority: String = "Normal",
    @SerializedName("status")
    val status: String = "Pending",
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("trialCompleted")
    val trialCompleted: Boolean = false,
    @SerializedName("garmentDetails")
    val garmentDetails: GarmentDetailsDto? = null,
    @SerializedName("dispatchInfo")
    val dispatchInfo: DispatchInfoDto? = null,
    @SerializedName("assignedStaff")
    val assignedStaff: AssignedStaffDto? = null,
    @SerializedName("statusHistory")
    val statusHistory: List<DeliveryStatusHistoryDto> = emptyList(),
    @SerializedName("orderId")
    val order: DeliveryOrderDto? = null
)

/**
 * Garment specs associated with this delivery.
 */
data class GarmentDetailsDto(
    @SerializedName("pieceCount")
    val pieceCount: String? = null,
    @SerializedName("areaIssues")
    val areaIssues: List<String> = emptyList()
)

/**
 * Courier dispatch information.
 */
data class DispatchInfoDto(
    @SerializedName("courierName")
    val courierName: String? = null,
    @SerializedName("dispatchedAt")
    val dispatchedAt: String? = null,
    @SerializedName("dispatchedBy")
    val dispatchedBy: String? = null
)

/**
 * Delivery staff assignment information.
 */
data class AssignedStaffDto(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("workMobile")
    val workMobile: String? = null,
    @SerializedName("personalMobile")
    val personalMobile: String? = null,
    @SerializedName("employeeCode")
    val employeeCode: String? = null,
    @SerializedName("memberId")
    val memberId: String? = null,
    @SerializedName("status")
    val status: String? = null
) {
    val fullName: String
        get() = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifBlank { "Unassigned" }
}

/**
 * Audit log entry for delivery status lifecycle.
 */
data class DeliveryStatusHistoryDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("fromStatus")
    val fromStatus: String? = null,
    @SerializedName("toStatus")
    val toStatus: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("changedBy")
    val changedBy: String? = null,
    @SerializedName("changedAt")
    val changedAt: String? = null
)

/**
 * Complete order snapshot inside delivery details.
 */
data class DeliveryOrderDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("orderCode")
    val orderCode: String = "",
    @SerializedName("orderDate")
    val orderDate: String? = null,
    @SerializedName("dueDate")
    val dueDate: String? = null,
    @SerializedName("currency")
    val currency: String = "INR",
    @SerializedName("orderType")
    val orderType: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("subtotal")
    val subtotal: Double = 0.0,
    @SerializedName("totalTax")
    val totalTax: Double = 0.0,
    @SerializedName("grandTotal")
    val grandTotal: Double = 0.0,
    @SerializedName("advanceAmountPaid")
    val advanceAmountPaid: Double = 0.0,
    @SerializedName("balanceAmount")
    val balanceAmount: Double = 0.0,
    @SerializedName("paymentStatus")
    val paymentStatus: String? = null,
    @SerializedName("paymentMode")
    val paymentMode: String? = null,
    @SerializedName("customerSnapshot")
    val customerSnapshot: DeliveryCustomerSnapshotDto? = null,
    @SerializedName("items")
    val items: List<DeliveryOrderItemDto> = emptyList()
)

/**
 * Customer snapshot data.
 */
data class DeliveryCustomerSnapshotDto(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("shippingAddress")
    val shippingAddress: DeliveryAddressDto? = null,
    @SerializedName("billingAddress")
    val billingAddress: DeliveryAddressDto? = null
)

/**
 * Customer delivery and billing address structure.
 */
data class DeliveryAddressDto(
    @SerializedName("flatNo")
    val flatNo: String? = null,
    @SerializedName("street")
    val street: String? = null,
    @SerializedName("areaZone")
    val areaZone: String? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("subdivisionName")
    val subdivisionName: String? = null,
    @SerializedName("pincode")
    val pincode: String? = null
) {
    val formatted: String
        get() = listOfNotNull(flatNo, street, areaZone, city, subdivisionName, pincode)
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { "Address not specified" }
}

/**
 * Line item in the order.
 */
data class DeliveryOrderItemDto(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("itemDescription")
    val itemDescription: String? = null,
    @SerializedName("lineType")
    val lineType: String? = null,
    @SerializedName("quantity")
    val quantity: Int = 1,
    @SerializedName("unitPrice")
    val unitPrice: Double = 0.0,
    @SerializedName("lineTotal")
    val lineTotal: Double = 0.0
)

/**
 * API response model for PUT /api/logistics/delivery/mark-completed/:id
 */
data class MarkDeliveryCompletedResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: MarkDeliveryCompletedData? = null
)

/**
 * Payload data returned after delivery completion.
 */
data class MarkDeliveryCompletedData(
    @SerializedName("_id")
    val id: String,
    @SerializedName("orderId")
    val orderId: String? = null,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("customerId")
    val customerId: String? = null,
    @SerializedName("deliveryNumber")
    val deliveryNumber: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("trialCompleted")
    val trialCompleted: Boolean = false,
    @SerializedName("assignedStaff")
    val assignedStaff: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("garmentDetails")
    val garmentDetails: GarmentDetailsDto? = null,
    @SerializedName("dispatchInfo")
    val dispatchInfo: DispatchInfoDto? = null,
    @SerializedName("statusHistory")
    val statusHistory: List<DeliveryStatusHistoryDto> = emptyList()
)


/**
 * Request payload for confirming in-store pickup matching the exact API requirement.
 */
data class ConfirmInStorePickupRequest(
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("handedOverByStaffName")
    val handedOverByStaffName: String,
    @SerializedName("collectedBy")
    val collectedBy: String,
    @SerializedName("verificationMethod")
    val verificationMethod: String,
    @SerializedName("packageCondition")
    val packageCondition: String,
    @SerializedName("notes")
    val notes: String
)

/**
 * Response received from confirm in-store pickup endpoint.
 */
data class ConfirmInStorePickupResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: InStorePickupData? = null
)

data class InStorePickupData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("orderId")
    val orderId: String? = null,
    @SerializedName("orderCode")
    val orderCode: String? = null,
    @SerializedName("collectedBy")
    val collectedBy: String? = null,
    @SerializedName("collectorName")
    val collectorName: String? = null,
    @SerializedName("verificationMethod")
    val verificationMethod: String? = null,
    @SerializedName("pickupDateTime")
    val pickupDateTime: String? = null,
    @SerializedName("packageCondition")
    val packageCondition: String? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("pickupBranchName")
    val pickupBranchName: String? = null,
    @SerializedName("itemsCount")
    val itemsCount: Int? = null
)

/**
 * Request payload for changing delivery status.
 */
data class UpdateDeliveryStatusRequest(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String? = null
)

/**
 * API response for PUT /api/logistics/delivery/update-status/:id
 */
data class UpdateDeliveryStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: UpdateDeliveryStatusData? = null
)

/**
 * Payload data returned after delivery status change.
 */
data class UpdateDeliveryStatusData(
    @SerializedName("_id")
    val id: String,
    @SerializedName("orderId")
    val orderId: String? = null,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("customerId")
    val customerId: String? = null,
    @SerializedName("deliveryNumber")
    val deliveryNumber: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("trialCompleted")
    val trialCompleted: Boolean = false,
    @SerializedName("assignedStaff")
    val assignedStaff: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("garmentDetails")
    val garmentDetails: GarmentDetailsDto? = null,
    @SerializedName("dispatchInfo")
    val dispatchInfo: DispatchInfoDto? = null,
    @SerializedName("statusHistory")
    val statusHistory: List<DeliveryStatusHistoryDto> = emptyList()
)