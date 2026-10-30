package com.cuso.tailor.model.logistics

import com.google.gson.annotations.SerializedName

// =============================================================================
// COURIER MANAGEMENT LIST RESPONSE
// GET /api/logistics/courier-management/list
// =============================================================================

data class CourierManagementListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaginationMeta?,
    @SerializedName("data") val data: List<CourierManagementItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class CourierManagementItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("deliveryId") val deliveryId: String = "",
    @SerializedName("orderId") val orderId: String = "",
    @SerializedName("customer") val customer: String = "",
    @SerializedName("deliveryType") val deliveryType: String = "",
    @SerializedName("deliveryDate") val deliveryDate: String = "",
    @SerializedName("status") val status: String = "Ready",
    @SerializedName("courierPartner") val courierPartner: String? = null
)

// =============================================================================
// COURIER DELIVERY OVERVIEW RESPONSE
// GET /api/logistics/delivery/courier-overview/:id
// =============================================================================

data class CourierDeliveryOverviewResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CourierDeliveryOverviewData? = null
)

data class CourierDeliveryOverviewData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("deliveryId") val deliveryId: String = "",
    @SerializedName("internalRef") val internalRef: String = "",
    @SerializedName("status") val status: String = "Ready",
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("deliveryProgress") val deliveryProgress: List<DeliveryProgressStepDto> = emptyList(),
    @SerializedName("customerInfo") val customerInfo: CourierCustomerInfoDto? = null,
    @SerializedName("salesOrderReference") val salesOrderReference: CourierSalesOrderReferenceDto? = null,
    @SerializedName("productDetails") val productDetails: CourierProductDetailsDto? = null,
    @SerializedName("preferredResolution") val preferredResolution: CourierPreferredResolutionDto? = null,
    @SerializedName("deliveryCharge") val deliveryCharge: Double = 0.0,
    @SerializedName("assignedCourier") val assignedCourier: AssignedCourierInfoDto? = null
)

data class DeliveryProgressStepDto(
    @SerializedName("stage") val stage: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("isCompleted") val isCompleted: Boolean = false,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class CourierCustomerInfoDto(
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("customerName") val customerName: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("address") val address: String = ""
)

data class CourierSalesOrderReferenceDto(
    @SerializedName("orderId") val orderId: String = "",
    @SerializedName("garmentName") val garmentName: String = "",
    @SerializedName("garmentType") val garmentType: String = "",
    @SerializedName("deliveryDate") val deliveryDate: String? = null,
    @SerializedName("trialCompleted") val trialCompleted: Boolean = false
)

data class CourierProductDetailsDto(
    @SerializedName("pieceCount") val pieceCount: String = "Single Piece",
    @SerializedName("areaIssues") val areaIssues: List<String> = emptyList()
)

data class CourierPreferredResolutionDto(
    @SerializedName("preferredServiceDate") val preferredServiceDate: String? = null,
    @SerializedName("resolutionNotes") val resolutionNotes: String = ""
)

data class AssignedCourierInfoDto(
    @SerializedName("courierName") val courierName: String = "",
    @SerializedName("trackingId") val trackingId: String = "--",
    @SerializedName("instructions") val instructions: String = ""
)

/**
 * Payload sent to toggle courier partner status.
 * Matches backend schema: { status: "Active" | "Inactive" }
 */
data class ToggleCourierStatusRequest(
    @SerializedName("status")
    val status: String
)