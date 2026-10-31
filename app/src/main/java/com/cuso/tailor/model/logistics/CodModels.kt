package com.cuso.tailor.model.logistics


import com.google.gson.annotations.SerializedName

/**
 * Top-level response for All Courier / COD List API
 */
data class AllCourierListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: AllCourierPaginationDto? = null,
    @SerializedName("data") val data: List<AllCourierItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class AllCourierPaginationDto(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 1
)

data class AllCourierItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("orderCode") val orderCode: String = "",
    @SerializedName("courierName") val courierName: String = "",
    @SerializedName("codAmount") val codAmount: Double = 0.0,
    @SerializedName("status") val status: String = "Pending",
    @SerializedName("deliveryDate") val deliveryDate: String? = null,
    @SerializedName("trackingId") val trackingId: String? = "--"
)

/**
 * Response for single COD Order Overview API
 * Endpoint: GET /api/logistics/courier-management/overview/:id
 */
data class CodOrderDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: CodOrderDetailData? = null,
    @SerializedName("message") val message: String? = null
)

data class CodOrderDetailData(
    @SerializedName("codNumber") val codNumber: String = "",
    @SerializedName("orderId") val orderId: String = "",
    @SerializedName("orderCode") val orderCode: String = "",
    @SerializedName("codAmount") val codAmount: Double = 0.0,
    @SerializedName("status") val status: String = "Pending",
    @SerializedName("customerInfo") val customerInfo: CodCustomerInfoDto? = null,
    @SerializedName("paymentDetails") val paymentDetails: CodPaymentDetailsDto? = null,
    @SerializedName("courierDetails") val courierDetails: CodCourierDetailsDto? = null,
    @SerializedName("activityTimeline") val activityTimeline: List<CodTimelineItemDto> = emptyList()
)

data class CodCustomerInfoDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("deliveryAddress") val deliveryAddress: String = "",
    @SerializedName("deliveryDate") val deliveryDate: String? = null,
    @SerializedName("coordinates") val coordinates: String? = null
)

data class CodPaymentDetailsDto(
    @SerializedName("paymentType") val paymentType: String = "COD",
    @SerializedName("status") val status: String = "Pending",
    @SerializedName("collectionDate") val collectionDate: String? = null,
    @SerializedName("settlementId") val settlementId: String? = null
)

data class CodCourierDetailsDto(
    @SerializedName("courierName") val courierName: String = "",
    @SerializedName("courierCode") val courierCode: String = "BD",
    @SerializedName("trackingId") val trackingId: String = "--"
)

data class CodTimelineItemDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("isCompleted") val isCompleted: Boolean = true
)

data class MarkCodCollectedRequest(
    @SerializedName("settlementId") val settlementId: String? = null
)