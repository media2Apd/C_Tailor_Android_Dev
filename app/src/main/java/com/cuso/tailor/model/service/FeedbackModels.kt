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