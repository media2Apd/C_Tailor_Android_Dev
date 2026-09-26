@file:Suppress("unused")

package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName

/**
 * Top-level response for GET /api/measurements
 */
data class MeasurementsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("data")
    val data: List<CustomerMeasurementItem> = emptyList()
)

/**
 * Encapsulates customer information along with their latest measurement record.
 */
data class CustomerMeasurementItem(
    @SerializedName("customer")
    val customer: MeasurementCustomerInfo? = null,

    @SerializedName("latestMeasurement")
    val latestMeasurement: LatestMeasurementDto? = null
)

/**
 * Basic customer details associated with the measurement record.
 */
data class MeasurementCustomerInfo(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("fullName")
    val fullName: String? = null,

    @SerializedName("mobileNumber")
    val mobileNumber: String? = null,

    @SerializedName("customerCode")
    val customerCode: String? = null,

    @SerializedName("profilePicture")
    val profilePicture: ProfilePictureDto? = null
)

/**
 * Details of the latest recorded measurement.
 */
data class LatestMeasurementDto(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("segmentId")
    val segment: MeasurementSegmentDto? = null,

    @SerializedName("garmentId")
    val garment: MeasurementGarmentDto? = null,

    @SerializedName("garmentCategoryId")
    val garmentCategory: MeasurementGarmentCategoryDto? = null,

    @SerializedName("measuredAt")
    val measuredAt: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null
)

/**
 * Segment classification (e.g., Men, Women, Kids).
 */
data class MeasurementSegmentDto(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("displayName")
    val displayName: String? = null
)

/**
 * Garment definition linked to the measurement.
 */
data class MeasurementGarmentDto(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null
)

/**
 * Garment sub-category (e.g., Bridal Blouse).
 */
data class MeasurementGarmentCategoryDto(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("displayName")
    val displayName: String? = null
)

// ─────────────────────────────────────────────────────────────
// UI Model & Mapping Extension
// ─────────────────────────────────────────────────────────────

data class MeasurementItem(
    val id: String,
    val customerId: String,
    val customerName: String,
    val contact: String,
    val customerCode: String,
    val garmentName: String,
    val categoryName: String,
    val measuredDate: String,
    val status: String,
    val profileImageUrl: String?
)

/**
 * Maps the API item into the presentation model for UI components.
 */
fun CustomerMeasurementItem.toMeasurementItem(): MeasurementItem {
    return MeasurementItem(
        id = latestMeasurement?.id.orEmpty(),
        customerId = customer?.id.orEmpty(),
        customerName = customer?.fullName ?: "—",
        contact = customer?.mobileNumber ?: "—",
        customerCode = customer?.customerCode ?: "—",
        garmentName = latestMeasurement?.garment?.displayName
            ?: latestMeasurement?.garment?.name
            ?: "—",
        categoryName = latestMeasurement?.garmentCategory?.displayName
            ?: latestMeasurement?.garmentCategory?.name
            ?: "—",
        measuredDate = latestMeasurement?.measuredAt ?: "—",
        status = latestMeasurement?.status ?: "—",
        profileImageUrl = customer?.profilePicture?.url
    )
}