@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "unused")

package com.cuso.tailor.model.logistics

import com.google.gson.annotations.SerializedName

// =============================================================================
// SHIPMENT LIST MODELS
// =============================================================================

data class ShipmentListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaginationMeta?,
    @SerializedName("data") val data: List<ShipmentListItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class PaginationMeta(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 1
)

data class ShipmentListItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("shipmentId") val shipmentId: String = "",
    @SerializedName("shipmentNumber") val shipmentNumber: String = "",
    @SerializedName("orderId") val orderId: ShipmentOrderSummaryDto? = null,
    @SerializedName("deliveryAddress") val deliveryAddress: DeliveryShipmentAddressDto? = null,
    @SerializedName("courierPartner") val courierPartner: String? = null,
    @SerializedName("serviceType") val serviceType: String? = null,
    @SerializedName("trackingId") val trackingId: String? = null,
    @SerializedName("packageDetails") val packageDetails: PackageDimensionDto? = null,
    @SerializedName("status") val status: String = "Created",
    @SerializedName("currentLocation") val currentLocation: String? = null,
    @SerializedName("trackingTimeline") val trackingTimeline: TrackingTimelineDto? = null,
    @SerializedName("activityLog") val activityLog: List<ShipmentActivityItemDto> = emptyList(),
    @SerializedName("shippingLabel") val shippingLabel: ShippingLabelDto? = null,
    @SerializedName("orderCode") val orderCode: String? = null,
    @SerializedName("orderIdText") val orderIdText: String? = null,
    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class ShipmentOrderSummaryDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("orderCode") val orderCode: String = "",
    @SerializedName("customerSnapshot") val customerSnapshot: CustomerShipmentSnapshotDto? = null
)
//
//data class CustomerShipmentSnapshotDto(
//    @SerializedName("customerCode") val customerCode: String = "",
//    @SerializedName("name") val name: String = "",
//    @SerializedName("email") val email: String = "",
//    @SerializedName("phone") val phone: String = ""
//)
//
//data class DeliveryShipmentAddressDto(
//    @SerializedName("recipientName") val recipientName: String = "",
//    @SerializedName("phoneNumber") val phoneNumber: String = "",
//    @SerializedName("fullAddress") val fullAddress: String = "",
//    @SerializedName("city") val city: String = "",
//    @SerializedName("pincode") val pincode: String = "",
//    @SerializedName("countryCode") val countryCode: String = "IN",
//    @SerializedName("state") val state: String = ""
//)
//
//data class PackageDimensionDto(
//    @SerializedName("weightKg") val weightKg: Double = 0.0,
//    @SerializedName("dimensions") val dimensions: DimensionsDetailDto? = null
//)
//
//data class DimensionsDetailDto(
//    @SerializedName("lengthCm") val lengthCm: Double = 0.0,
//    @SerializedName("widthCm") val widthCm: Double = 0.0,
//    @SerializedName("heightCm") val heightCm: Double = 0.0
//)
//
//data class TrackingTimelineDto(
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("inTransitAt") val inTransitAt: String? = null,
//    @SerializedName("outForDeliveryAt") val outForDeliveryAt: String? = null,
//    @SerializedName("deliveredAt") val deliveredAt: String? = null
//)
//
//data class ShipmentActivityItemDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("action") val action: String = "",
//    @SerializedName("location") val location: String? = null,
//    @SerializedName("note") val note: String? = null,
//    @SerializedName("timestamp") val timestamp: String = ""
//)
//
//data class ShippingLabelDto(
//    @SerializedName("isGenerated") val isGenerated: Boolean = false,
//    @SerializedName("labelUrl") val labelUrl: String? = null,
//    @SerializedName("generatedAt") val generatedAt: String? = null
//)
//
//// =============================================================================
//// SHIPMENT DETAIL RESPONSE
//// =============================================================================
//
//data class ShipmentDetailResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("data") val data: ShipmentDetailData? = null,
//    @SerializedName("message") val message: String? = null
//)
//
//data class ShipmentDetailData(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("shipmentId") val shipmentId: String = "",
//    @SerializedName("shipmentNumber") val shipmentNumber: String = "",
//    @SerializedName("courierPartner") val courierPartner: String? = null,
//    @SerializedName("serviceType") val serviceType: String? = null,
//    @SerializedName("trackingId") val trackingId: String? = null,
//    @SerializedName("status") val status: String = "Created",
//    @SerializedName("currentLocation") val currentLocation: String? = null,
//    @SerializedName("packageDetails") val packageDetails: PackageDimensionDto? = null,
//    @SerializedName("deliveryAddress") val deliveryAddress: DeliveryShipmentAddressDto? = null,
//    @SerializedName("trackingTimeline") val trackingTimeline: TrackingTimelineDto? = null,
//    @SerializedName("activityLog") val activityLog: List<ShipmentActivityItemDto> = emptyList(),
//    @SerializedName("shippingLabel") val shippingLabel: ShippingLabelDto? = null,
//    @SerializedName("orderId") val orderDetail: ShipmentNestedOrderDto? = null,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("updatedAt") val updatedAt: String? = null
//)
//
//data class ShipmentNestedOrderDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("orderCode") val orderCode: String = "",
//    @SerializedName("orderNotes") val orderNotes: String? = null,
//    @SerializedName("customerSnapshot") val customerSnapshot: CustomerShipmentSnapshotDto? = null
//)
//
//// =============================================================================
//// SHIPMENT ACTION REQUESTS & RESPONSES
//// =============================================================================
//
//data class UpdateShipmentStatusRequest(
//    @SerializedName("status") val status: String,
//    @SerializedName("currentLocation") val currentLocation: String?,
//    @SerializedName("notes") val notes: String?
//)
//
//data class GenerateShippingLabelResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("labelUrl") val labelUrl: String?,
//    @SerializedName("message") val message: String?
//)



























































// =============================================================================
// SHIPMENT DETAIL RESPONSE MODEL
// GET /api/logistics/shipment/details/:id
// =============================================================================

data class ShipmentDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ShipmentDetailData? = null,
    @SerializedName("message") val message: String? = null
)

data class ShipmentDetailData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("shipmentId") val shipmentId: String = "",
    @SerializedName("shipmentNumber") val shipmentNumber: String = "",
    @SerializedName("orderId") val orderDetail: ShipmentNestedOrderDto? = null,
    @SerializedName("deliveryAddress") val deliveryAddress: DeliveryShipmentAddressDto? = null,
    @SerializedName("packageDetails") val packageDetails: PackageDimensionDto? = null,
    @SerializedName("courierPartner") val courierPartner: String? = null,
    @SerializedName("serviceType") val serviceType: String? = null,
    @SerializedName("trackingId") val trackingId: String? = null,
    @SerializedName("status") val status: String = "Created",
    @SerializedName("currentLocation") val currentLocation: String? = null,
    @SerializedName("trackingTimeline") val trackingTimeline: TrackingTimelineDto? = null,
    @SerializedName("activityLog") val activityLog: List<ShipmentActivityItemDto> = emptyList(),
    @SerializedName("shippingLabel") val shippingLabel: ShippingLabelDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val version: Int = 0
)

data class ShipmentNestedOrderDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("orderCode") val orderCode: String = "",
    @SerializedName("orderNotes") val orderNotes: String? = null,
    @SerializedName("orderType") val orderType: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("deliveryMethod") val deliveryMethod: String? = null,
    @SerializedName("customerSnapshot") val customerSnapshot: CustomerShipmentSnapshotDto? = null
)

data class CustomerShipmentSnapshotDto(
    @SerializedName("customerCode") val customerCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("taxId") val taxId: String? = null
)

data class DeliveryShipmentAddressDto(
    @SerializedName("recipientName") val recipientName: String = "",
    @SerializedName("phoneNumber") val phoneNumber: String = "",
    @SerializedName("fullAddress") val fullAddress: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("pincode") val pincode: String = "",
    @SerializedName("countryCode") val countryCode: String = "IN",
    @SerializedName("state") val state: String = ""
)

data class PackageDimensionDto(
    @SerializedName("weightKg") val weightKg: Double = 0.0,
    @SerializedName("dimensions") val dimensions: DimensionsDetailDto? = null
)

data class DimensionsDetailDto(
    @SerializedName("lengthCm") val lengthCm: Double = 0.0,
    @SerializedName("widthCm") val widthCm: Double = 0.0,
    @SerializedName("heightCm") val heightCm: Double = 0.0
)

data class TrackingTimelineDto(
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("inTransitAt") val inTransitAt: String? = null,
    @SerializedName("outForDeliveryAt") val outForDeliveryAt: String? = null,
    @SerializedName("deliveredAt") val deliveredAt: String? = null
)

data class ShipmentActivityItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("action") val action: String = "",
    @SerializedName("location") val location: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("timestamp") val timestamp: String = ""
)

data class ShippingLabelDto(
    @SerializedName("isGenerated") val isGenerated: Boolean = false,
    @SerializedName("labelUrl") val labelUrl: String? = null,
    @SerializedName("generatedAt") val generatedAt: String? = null
)

// =============================================================================
// GENERATE SHIPPING LABEL MODELS
// POST /api/logistics/shipment/generate-label/:id
// =============================================================================

data class GenerateShippingLabelResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: GeneratedShippingLabelData? = null
)

data class GeneratedShippingLabelData(
    @SerializedName("shipmentId") val shipmentId: String = "",
    @SerializedName("courierName") val courierName: String = "",
    @SerializedName("trackingId") val trackingId: String = "",
    @SerializedName("packageDetails") val packageDetails: PackageDimensionDto? = null,
    @SerializedName("labelUrl") val labelUrl: String? = null,
    @SerializedName("downloadFileName") val downloadFileName: String? = null
)

// =============================================================================
// UPDATE STATUS MODELS
// PUT /api/logistics/shipment/update-status/:id
// =============================================================================

data class UpdateShipmentStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("currentLocation") val currentLocation: String?,
    @SerializedName("notes") val notes: String?
)

data class UpdateShipmentStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ShipmentDetailData? = null
)



// =============================================================================
// COURIER PARTNERS LIST RESPONSE
// GET /api/logistics/courier/list
// =============================================================================

data class CourierPartnersResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("metrics") val metrics: CourierMetricsDto?,
    @SerializedName("pagination") val pagination: PaginationMeta?,
    @SerializedName("data") val data: List<CourierPartnerItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class CourierMetricsDto(
    @SerializedName("totalFound") val totalFound: Int = 0,
    @SerializedName("activeCouriers") val activeCouriers: Int = 0,
    @SerializedName("inactiveCouriers") val inactiveCouriers: Int = 0,
    @SerializedName("deliveryPincodesActive") val deliveryPincodesActive: Int = 0,
    @SerializedName("pickupPincodesActive") val pickupPincodesActive: Int = 0,
    @SerializedName("rtoPincodes") val rtoPincodes: Int = 0,
    @SerializedName("outOfDeliveryPincodes") val outOfDeliveryPincodes: Int = 0
)

data class CourierPartnerItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("shiprocketCourierId") val shiprocketCourierId: Int? = null,
    @SerializedName("courierName") val courierName: String = "",
    @SerializedName("status") val status: String = "Active",
    @SerializedName("transportMode") val transportMode: String = "",
    @SerializedName("regionType") val regionType: String? = "Domestic",
    @SerializedName("serviceability") val serviceability: CourierServiceabilityDto? = null,
    @SerializedName("serviceTypes") val serviceTypes: List<String> = emptyList(),
    @SerializedName("reliabilityScore") val reliabilityScore: Double = 0.0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class CourierServiceabilityDto(
    @SerializedName("coverageType") val coverageType: String? = "DOMESTIC",
    @SerializedName("coveredCountries") val coveredCountries: List<String> = emptyList()
)

// =============================================================================
// CREATE SHIPMENT REQUEST & RESPONSE (Matches exact payload & response)
// POST /api/logistics/shipment/create
// =============================================================================

data class CreateShipmentApiRequest(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("courierPartner") val courierPartner: String,
    @SerializedName("serviceType") val serviceType: String,
    @SerializedName("deliveryAddress") val deliveryAddress: DeliveryShipmentAddressDto,
    @SerializedName("packageDetails") val packageDetails: PackageDimensionDto,
    @SerializedName("allocation") val allocation: ShipmentAllocationDto? = null
)

data class ShipmentAllocationDto(
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("warehouseName") val warehouseName: String? = null
)

data class CreateShipmentApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CreatedShipmentDataDto? = null
)

data class CreatedShipmentDataDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("shipmentId") val shipmentId: String = "",
    @SerializedName("shipmentNumber") val shipmentNumber: String = "",
    @SerializedName("orderId") val orderId: String = "",
    @SerializedName("deliveryAddress") val deliveryAddress: DeliveryShipmentAddressDto? = null,
    @SerializedName("courierPartner") val courierPartner: String? = null,
    @SerializedName("serviceType") val serviceType: String? = null,
    @SerializedName("trackingId") val trackingId: String? = null,
    @SerializedName("packageDetails") val packageDetails: PackageDimensionDto? = null,
    @SerializedName("status") val status: String = "Created",
    @SerializedName("currentLocation") val currentLocation: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// =============================================================================
// COURIER DELIVERY ITEM DTO & LIST RESPONSE
// GET /api/logistics/delivery/view-all
// =============================================================================

data class CourierDeliveryListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaginationMeta?,
    @SerializedName("data") val data: List<CourierDeliveryItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class CourierDeliveryItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("deliveryId") val deliveryId: String = "",
    @SerializedName("orderId") val orderId: String = "",
    @SerializedName("customer") val customer: String = "",
    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("deliveryType") val deliveryType: String = "",
    @SerializedName("deliveryDate") val deliveryDate: String = "",
    @SerializedName("status") val status: String = "Ready",
    @SerializedName("courierPartner") val courierPartner: String? = null
)

// =============================================================================
// ASSIGN COURIER REQUEST & RESPONSE
// POST /api/logistics/delivery/assign-courier/:deliveryId
// =============================================================================

data class AssignCourierRequest(
    @SerializedName("courierPartner") val courierPartner: String,
    @SerializedName("deliveryCharge") val deliveryCharge: Double,
    @SerializedName("instructions") val instructions: String?
)

data class AssignCourierResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)