package com.cuso.tailor.model.logistics

import com.google.gson.annotations.SerializedName

// =============================================================================
// COURIER PARTNER DETAIL RESPONSE
// GET /api/logistics/courier-partner/details/:id
// =============================================================================

data class CourierPartnerDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CourierPartnerDetailData? = null
)

data class CourierPartnerDetailData(
    @SerializedName("_id") val id: String,
    @SerializedName("courierName") val courierName: String,
    @SerializedName("status") val status: String,
    @SerializedName("integrationType") val integrationType: String? = "MANUAL",
    @SerializedName("shiprocketCourierId") val shiprocketCourierId: Int? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("transportMode") val transportMode: String = "",
    @SerializedName("regionType") val regionType: String? = "Domestic",
    @SerializedName("reliabilityScore") val reliabilityScore: Double = 98.4,
    @SerializedName("serviceTypes") val serviceTypes: List<String> = emptyList(),
    @SerializedName("serviceability") val serviceability: CourierDetailServiceabilityDto? = null,
    @SerializedName("activityTimeline") val activityTimeline: List<CourierActivityTimelineDto> = emptyList(),
    @SerializedName("notes") val notes: String = "",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class CourierDetailServiceabilityDto(
    @SerializedName("coverageType") val coverageType: String? = "DOMESTIC",
    @SerializedName("primaryHubs") val primaryHubs: List<String> = emptyList(),
    @SerializedName("coveredPostalCodes") val coveredPostalCodes: List<String> = emptyList(),
    @SerializedName("coveredCountries") val coveredCountries: List<String> = emptyList()
)

data class CourierActivityTimelineDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("timestamp") val timestamp: String = ""
)

/**
 * Payload sent for both creating and updating a courier partner.
 * Matches the exact API request structure.
 */
data class CreateUpdateCourierPartnerRequest(
    @SerializedName("courierName")
    val courierName: String,
    @SerializedName("status")
    val status: String = "Active",
    @SerializedName("integrationType")
    val integrationType: String = "MANUAL",
    @SerializedName("shiprocketCourierId")
    val shiprocketCourierId: Int? = null,
    @SerializedName("notes")
    val notes: String = "",
    @SerializedName("reliabilityScore")
    val reliabilityScore: Double = 98.4,
    @SerializedName("serviceTypes")
    val serviceTypes: List<String> = emptyList(),
    @SerializedName("serviceability")
    val serviceability: ServiceabilityRequestDto
)

data class ServiceabilityRequestDto(
    @SerializedName("coverageType")
    val coverageType: String, // "DOMESTIC" for All India or "SELECTED_POSTAL_CODES"
    @SerializedName("coveredCountries")
    val coveredCountries: List<String> = listOf("IN"),
    @SerializedName("coveredPostalCodes")
    val coveredPostalCodes: List<String> = emptyList(),
    @SerializedName("primaryHubs")
    val primaryHubs: List<String> = emptyList()
)

/**
 * Response received after creating or updating courier partner.
 */
data class CreateCourierPartnerResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: CourierPartnerDetailData? = null
)