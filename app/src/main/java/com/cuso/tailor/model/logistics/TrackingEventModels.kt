package com.cuso.tailor.model.logistics

import com.google.gson.annotations.SerializedName

/**
 * API response container for Tracking Events / Order Tracking list.
 */
data class TrackingEventsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("pagination")
    val pagination: TrackingPaginationDto? = null,
    @SerializedName("data")
    val data: List<TrackingEventItemDto> = emptyList(),
    @SerializedName("message")
    val message: String? = null
)

data class TrackingPaginationDto(
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
 * Single tracking event matching the desktop table columns:
 * Shipment ID, Date, Location, Updated By, Status, and Tracking ID.
 */
data class TrackingEventItemDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("shipmentId")
    val shipmentId: String,
    @SerializedName("orderId")
    val orderId: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("location")
    val location: String = "",
    @SerializedName("updatedBy")
    val updatedBy: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("trackingId")
    val trackingId: String? = null
)

data class TrackingOverviewResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: TrackingOverviewData?,
    @SerializedName("message") val message: String? = null
)

data class TrackingOverviewData(
    @SerializedName("orderCode") val orderCode: String? = null,
    @SerializedName("orderId") val orderId: String? = null,
    @SerializedName("trackingId") val trackingId: String? = null,
    @SerializedName("shipmentId") val shipmentId: String? = null,
    @SerializedName("courierPartner") val courierPartner: String? = null,
    @SerializedName("serviceType") val serviceType: String? = null,
    @SerializedName("currentStatus") val currentStatus: String? = null,
    @SerializedName("estDeliveryDate") val estDeliveryDate: String? = null,
    @SerializedName("shipmentInfo") val shipmentInfo: TrackingShipmentInfo? = null,
    @SerializedName("trackingTimeline") val trackingTimeline: List<TrackingTimelineItemDto> = emptyList(),
    @SerializedName("activityLog") val activityLog: List<TrackingActivityLogDto> = emptyList()
)

data class TrackingShipmentInfo(
    @SerializedName("origin") val origin: String? = null,
    @SerializedName("destination") val destination: String? = null,
    @SerializedName("packageWeightKg") val packageWeightKg: String? = null
)

data class TrackingTimelineItemDto(
    @SerializedName("stage") val stage: String,
    @SerializedName("location") val location: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("isCompleted") val isCompleted: Boolean = false,
    @SerializedName("note") val note: String? = null
)

data class TrackingActivityLogDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class UpdateTrackingStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("location") val location: String? = null,
    @SerializedName("note") val note: String? = null
)