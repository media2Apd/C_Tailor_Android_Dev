package com.cuso.tailor.model.logistics

data class LogisticsPackagesResponse(
    val success: Boolean = false,
    val pagination: PaginationData? = null,
    val summary: PackageSummaryData? = null,
    val data: List<PackageItemData> = emptyList(),
    val message: String? = null
)

data class PaginationData(
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val totalPages: Int = 1
)

data class PackageSummaryData(
    val total: Int = 0,
    val readyForShipment: Int = 0,
    val shipped: Int = 0,
    val delivered: Int = 0
)

data class PackageDimensions(
    val lengthCm: Double = 0.0,
    val widthCm: Double = 0.0,
    val heightCm: Double = 0.0
)

data class DeliveryAddress(
    val recipientName: String? = null,
    val phoneNumber: String? = null,
    val fullAddress: String? = null,
    val city: String? = null,
    val pincode: String? = null,
    val countryCode: String? = null,
    val state: String? = null
)

data class ActivityLogItem(
    val _id: String? = null,
    val action: String? = null,
    val location: String? = null,
    val note: String? = null,
    val updatedBy: String? = null,
    val timestamp: String? = null
)

data class ShippingLabelData(
    val isGenerated: Boolean = false,
    val labelUrl: String? = null,
    val generatedAt: String? = null
)

data class TrackingTimeline(
    val createdAt: String? = null,
    val inTransitAt: String? = null,
    val outForDeliveryAt: String? = null,
    val deliveredAt: String? = null
)

data class ShipmentData(
    val _id: String? = null,
    val organizationId: String? = null,
    val shipmentId: String? = null,
    val shipmentNumber: String? = null,
    val orderId: String? = null,
    val deliveryAddress: DeliveryAddress? = null,
    val courierPartner: String? = null,
    val serviceType: String? = null,
    val trackingId: String? = null,
    val status: String? = null,
    val currentLocation: String? = null,
    val trackingTimeline: TrackingTimeline? = null,
    val activityLog: List<ActivityLogItem> = emptyList(),
    val shippingLabel: ShippingLabelData? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class PackageItemData(
    val _id: String,
    val packageId: String? = null,
    val packageNumber: String? = null,
    val orderId: String? = null,
    val orderCode: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val location: String? = null,
    val primaryGarmentName: String? = null,
    val itemsCount: Int = 1,
    val packageWeightKg: Double = 0.0,
    val dimensions: PackageDimensions? = null,
    val value: Double = 0.0,
    val paymentType: String? = null,
    val statusBadge: String? = null,
    val shipment: ShipmentData? = null
)

data class LogisticsOrderDetailResponse(
    val success: Boolean = false,
    val data: LogisticsOrderDetailData? = null,
    val message: String? = null
)

data class LogisticsOrderDetailData(
    val `package`: PackageDetailDto? = null,
    val order: SalesOrderDetailDto? = null,
    val items: List<PackageItemDetailDto> = emptyList(),
    val logistics: LogisticsTimelineDto? = null,
    val shipment: ShipmentDetailDto? = null,
    val allocationDetails: AllocationDetailsDto? = null
)

data class PackageDetailDto(
    val _id: String,
    val packageNumber: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val estimatedDelivery: String? = null
)

data class SalesOrderDetailDto(
    val _id: String,
    val orderCode: String? = null,
    val dueDate: String? = null,
    val customerSnapshot: CustomerSnapshotDto? = null,
    val items: List<SalesOrderItemDto> = emptyList(),
    val grandTotal: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val deliveryMethod: String? = null,
    val paymentMode: String? = null,
    val status: String? = null,
    val formattedAddress: String? = null
)

data class CustomerSnapshotDto(
    val customerCode: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val shippingAddress: SnapshotAddressDto? = null
)

data class SnapshotAddressDto(
    val flatNo: String? = null,
    val street: String? = null,
    val areaZone: String? = null,
    val city: String? = null,
    val subdivisionName: String? = null,
    val countryName: String? = null,
    val pincode: String? = null
) {
    fun fullDisplayAddress(): String {
        return listOfNotNull(flatNo, street, areaZone, city, subdivisionName, pincode, countryName)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}

data class SalesOrderItemDto(
    val _id: String,
    val itemDescription: String? = null,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val lineTotal: Double = 0.0,
    val sacCode: String? = null,
    val customGarment: CustomGarmentSnapshotDto? = null
)

data class CustomGarmentSnapshotDto(
    val garmentName: String? = null,
    val categoryDisplayName: String? = null,
    val stitchingType: String? = null
)

data class PackageItemDetailDto(
    val _id: String,
    val itemName: String? = null,
    val qty: Int = 1,
    val lineType: String? = null
)

data class LogisticsTimelineDto(
    val _id: String? = null,
    val status: String? = null,
    val timeline: TimelineTimestampsDto? = null,
    val activityLog: List<LogisticsActivityLogDto> = emptyList()
)

data class TimelineTimestampsDto(
    val createdAt: String? = null,
    val pickedAt: String? = null,
    val packedAt: String? = null,
    val inTransitAt: String? = null,
    val deliveredAt: String? = null
)

data class LogisticsActivityLogDto(
    val _id: String? = null,
    val action: String? = null,
    val note: String? = null,
    val timestamp: String? = null
)

data class ShipmentDetailDto(
    val _id: String? = null,
    val shipmentNumber: String? = null,
    val courierPartner: String? = null,
    val trackingId: String? = null,
    val status: String? = null,
    val currentLocation: String? = null
)

data class AllocationDetailsDto(
    val warehouse: String? = null,
    val warehouseName: String? = null,
    val warehouseCode: String? = null,
    val allocatedTime: String? = null
)


data class WarehouseAllocationResponse(
    val success: Boolean = false,
    val data: WarehouseAllocationData? = null,
    val message: String? = null
)

data class WarehouseAllocationData(
    val orderId: String? = null,
    val deliveryPincode: String? = null,
    val warehouses: List<WarehouseItemDto> = emptyList()
)

data class WarehouseItemDto(
    val _id: String,
    val name: String? = null,
    val code: String? = null,
    val location: String? = null,
    val stock: String? = null,
    val zoneDistanceKm: String? = null,
    val isRecommended: Boolean = false,
    val isOutOfStock: Boolean = false
)

// --- Request / Mutation Body Models ---

data class ConfirmAllocationRequest(
    val warehouseId: String,
    val isAutoAllocate: Boolean = true
)

data class UpdateOrderStatusRequest(
    val status: String,
    val notes: String? = null
)

data class SimpleLogisticsActionResponse(
    val success: Boolean = false,
    val message: String? = null
)

data class UpdateOrderStatusResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: UpdatedLogisticsStatusData? = null
)

data class UpdatedLogisticsStatusData(
    val _id: String,
    val organizationId: String? = null,
    val salesOrderId: String? = null,
    val status: String? = null,
    val timeline: UpdatedTimelineDto? = null,
    val activityLog: List<LogisticsActivityLogDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val allocatedAt: String? = null,
    val allocatedWarehouseId: String? = null
)

data class UpdatedTimelineDto(
    val createdAt: String? = null,
    val pickedAt: String? = null,
    val packedAt: String? = null,
    val allocatedAt: String? = null,
    val deliveredAt: String? = null,
    val shippedAt: String? = null
)

data class LogisticsShipmentsResponse(
    val success: Boolean = false,
    val pagination: PaginationData? = null,
    val data: List<ShipmentItemDto> = emptyList(),
    val message: String? = null
)

data class ShipmentItemDto(
    val _id: String,
    val orderId: String? = null,
    val shipmentId: String? = null,
    val courier: String? = null,
    val status: String? = null,
    val action: String? = null
)

data class MarkDispatchedRequest(
    val shipmentId: String
)