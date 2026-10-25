@file:Suppress("unused")
package com.cuso.tailor.model.service

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

data class FeedbackListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<FeedbackItemDto>?,
    @SerializedName("pagination") val pagination: FeedbackPaginationDto?
)

data class FeedbackPaginationDto(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean
)

data class FeedbackItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("feedbackCode") val feedbackCode: String,
    @SerializedName("targetType") val targetType: String? = null,
    @SerializedName("orderCode") val orderCode: String? = null,
    @SerializedName("customer") val customer: FeedbackCustomerDto? = null,
    @SerializedName("overallRating") val overallRating: Int = 5,
    @SerializedName("npsCategory") val npsCategory: String? = null,
    @SerializedName("reviewTitle") val reviewTitle: String? = null,
    @SerializedName("isNegativeReview") val isNegativeReview: Boolean = false,
    @SerializedName("resolutionStatus") val resolutionStatus: String? = null,
    @SerializedName("collectedChannel") val collectedChannel: String? = null,
    @SerializedName("isPublicTestimonial") val isPublicTestimonial: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    val customerDisplayName: String
        get() = customer?.name?.takeIf { it.isNotBlank() } ?: "Customer"

    val displayCode: String
        get() = feedbackCode

    val formattedDate: String
        get() {
            if (createdAt.isNullOrBlank()) return "—"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val parsed = inputFormat.parse(createdAt)
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                parsed?.let { outputFormat.format(it) } ?: createdAt.take(10)
            } catch (_: Exception) {
                createdAt.take(10)
            }
        }

    val channelDisplay: String
        get() = collectedChannel?.replace("_", " ") ?: "In-Store"

    val orderDisplay: String
        get() = orderCode ?: "—"
}

data class FeedbackCustomerDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("code") val code: String?
)

data class SingleFeedbackResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: FeedbackDetailDataDto?
)

data class FeedbackDetailDataDto(
    @SerializedName("_id") val id: String,
    @SerializedName("feedbackCode") val feedbackCode: String?,
    @SerializedName("targetType") val targetType: String?,
    @SerializedName("customerSnapshot") val customerSnapshot: FeedbackCustomerSnapshot?,
    @SerializedName("customerId") val customer: FeedbackCustomerDetail?,
    @SerializedName("salesOrderId") val salesOrder: FeedbackSalesOrderDetail?,
    @SerializedName("overallRating") val overallRating: Double?,
    @SerializedName("fittingRating") val fittingRating: Double?,
    @SerializedName("stitchingQualityRating") val stitchingQualityRating: Double?,
    @SerializedName("deliveryTimelinessRating") val deliveryTimelinessRating: Double?,
    @SerializedName("staffBehaviorRating") val staffBehaviorRating: Double?,
    @SerializedName("npsScore") val npsScore: Int?,
    @SerializedName("npsCategory") val npsCategory: String?,
    @SerializedName("reviewTitle") val reviewTitle: String?,
    @SerializedName("comments") val comments: String?,
    @SerializedName("positiveAspects") val positiveAspects: List<String>?,
    @SerializedName("improvementAreas") val improvementAreas: List<String>?,
    @SerializedName("isPublicTestimonial") val isPublicTestimonial: Boolean?,
    @SerializedName("isFeatured") val isFeatured: Boolean?,
    @SerializedName("status") val status: String?,
    @SerializedName("collectedChannel") val collectedChannel: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class FeedbackCustomerSnapshot(
    @SerializedName("customerCode") val customerCode: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?
)

data class FeedbackCustomerDetail(
    @SerializedName("_id") val id: String?,
    @SerializedName("customerCode") val customerCode: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("mobileNumber") val mobileNumber: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("customerLevel") val customerLevel: String?
)

data class FeedbackSalesOrderDetail(
    @SerializedName("_id") val id: String?,
    @SerializedName("orderCode") val orderCode: String?,
    @SerializedName("orderDate") val orderDate: String?,
    @SerializedName("grandTotal") val grandTotal: Double?,
    @SerializedName("status") val status: String?,
    @SerializedName("items") val items: List<FeedbackOrderItem>?
)

data class FeedbackOrderItem(
    @SerializedName("_id") val id: String?,
    @SerializedName("itemDescription") val itemDescription: String?,
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("unitPrice") val unitPrice: Double?,
    @SerializedName("customGarment") val customGarment: FeedbackCustomGarment?
)

data class FeedbackCustomGarment(
    @SerializedName("garmentName") val garmentName: String?,
    @SerializedName("categoryDisplayName") val categoryDisplayName: String?
)