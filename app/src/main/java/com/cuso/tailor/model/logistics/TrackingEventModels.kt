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