@file:Suppress("unused")
package com.cuso.tailor.model.service

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

data class AlterationListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<AlterationItemDto>?,
    @SerializedName("pagination") val pagination: AlterationPaginationDto?
)

data class AlterationPaginationDto(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean
)

data class AlterationItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("serviceRequestCode") val serviceRequestCode: String,
    @SerializedName("primaryCategory") val primaryCategory: String? = null,
    @SerializedName("garmentSource") val garmentSource: String? = null,
    @SerializedName("originalSalesOrderId") val originalSalesOrderId: String? = null,
    @SerializedName("customerId") val customer: CustomerDetailDto? = null,
    @SerializedName("customerSnapshot") val customerSnapshot: CustomerSnapshotDto? = null,
    @SerializedName("items") val items: List<AlterationGarmentItemDto> = emptyList(),
    @SerializedName("status") val status: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("paymentStatus") val paymentStatus: String? = null,
    @SerializedName("deliveryDate") val deliveryDate: String? = null,
    @SerializedName("receivedDate") val receivedDate: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    // Computed UI helper properties
    val displayTicketCode: String get() = serviceRequestCode

    val customerDisplayName: String
        get() = customerSnapshot?.name
            ?: customer?.fullName
            ?: "Unknown Customer"

    val isFreeCharge: Boolean
        get() = items.firstOrNull()?.isChargeable != true

    val formattedDate: String
        get() {
            val sourceDate = createdAt ?: receivedDate
            if (sourceDate.isNullOrBlank()) return "N/A"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val parsed = inputFormat.parse(sourceDate)
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                parsed?.let { outputFormat.format(it) } ?: sourceDate
            } catch (_: Exception) {
                sourceDate.take(10)
            }
        }

    val orderIdDisplay: String
        get() = originalSalesOrderId ?: "ORD-4401"

    val garmentDisplay: String
        get() = items.firstOrNull()?.garmentName?.takeIf { it.isNotBlank() }
            ?: items.firstOrNull()?.garmentDescription?.takeIf { it.isNotBlank() }
            ?: items.firstOrNull()?.garmentCategoryName?.takeIf { it.isNotBlank() }
            ?: "Silk Saree"

    val tailorDisplay: String
        get() = "Unassigned"
}

data class CustomerDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("customerCode") val customerCode: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("mobileNumber") val mobileNumber: String?,
    @SerializedName("email") val email: String?
)

data class CustomerSnapshotDto(
    @SerializedName("customerCode") val customerCode: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("email") val email: String?
)

data class AlterationGarmentItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("garmentName") val garmentName: String? = null,
    @SerializedName("garmentCategoryName") val garmentCategoryName: String? = null,
    @SerializedName("garmentDescription") val garmentDescription: String? = null,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("isChargeable") val isChargeable: Boolean = false,
    @SerializedName("serviceCharge") val serviceCharge: Double = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double = 0.0,
    @SerializedName("itemStatus") val itemStatus: String? = null,
    @SerializedName("productionState") val productionState: String? = null
)

data class SingleAlterationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: AlterationViewData?
)

data class AlterationViewData(
    @SerializedName("serviceRequest") val serviceRequest: AlterationDetailDto?,
    @SerializedName("jobCard") val jobCard: JobCardInfoDto?,
    @SerializedName("assignedTailor") val assignedTailor: AssignedTailorDto?
)

data class JobCardInfoDto(
    @SerializedName("_id") val id: String,
    @SerializedName("jobCardCode") val jobCardCode: String? = null,
    @SerializedName("totalQuantity") val totalQuantity: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("currentStageName") val currentStageName: String? = null
)

data class AssignedTailorDto(
    @SerializedName("_id") val id: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String?
)
data class ServiceRequestUserDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)

data class AlterationDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("serviceRequestCode") val serviceRequestCode: String,
    @SerializedName("primaryCategory") val primaryCategory: String? = null,
    @SerializedName("garmentSource") val garmentSource: String? = null,
    @SerializedName("originalSalesOrderId") val originalSalesOrderId: String? = null,
    @SerializedName("customerId") val customer: CustomerDetailDto? = null,
    @SerializedName("customerSnapshot") val customerSnapshot: CustomerSnapshotDto? = null,
    @SerializedName("items") val items: List<AlterationDetailItemDto> = emptyList(),
    @SerializedName("attachments") val attachments: List<EvidenceAttachmentDto> = emptyList(),
    @SerializedName("status") val status: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("preferredDate") val preferredDate: String? = null,
    @SerializedName("deliveryDate") val deliveryDate: String? = null,
    @SerializedName("receivedDate") val receivedDate: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double? = 0.0,
    @SerializedName("balanceAmount") val balanceAmount: Double? = 0.0,
    @SerializedName("paymentMode") val paymentMode: String? = null,
    @SerializedName("paymentStatus") val paymentStatus: String? = null,
    @SerializedName("createdBy") val createdBy: ServiceRequestUserDto? = null
)

data class AlterationDetailItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("garmentName") val garmentName: String? = null,
    @SerializedName("garmentCategoryName") val garmentCategoryName: String? = null,
    @SerializedName("segmentName") val segmentName: String? = null,
    @SerializedName("garmentDescription") val garmentDescription: String? = null,
    @SerializedName("issueSummary") val issueSummary: String? = null,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("isChargeable") val isChargeable: Boolean = false,
    @SerializedName("serviceCharge") val serviceCharge: Double? = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double? = 0.0,
    @SerializedName("productionState") val productionState: String? = null,
    @SerializedName("itemStatus") val itemStatus: String? = null,
    @SerializedName("alterations") val alterations: List<AlterationDeltaEntryDto> = emptyList()
)

data class AlterationDeltaEntryDto(
    @SerializedName("targetArea") val targetArea: String,
    @SerializedName("action") val action: String? = null,
    @SerializedName("currentValue") val currentValue: Double? = null,
    @SerializedName("targetValue") val targetValue: Double? = null,
    @SerializedName("value") val value: Double? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class EvidenceAttachmentDto(
    @SerializedName("url") val url: String,
    @SerializedName("originalName") val originalName: String? = null
)

data class DeliverAlterationRequest(
    @SerializedName("status") val status: String = "Delivered",
    @SerializedName("allowWithoutAdvance") val allowWithoutAdvance: Boolean = true
)

data class DeliverAlterationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: AlterationDetailDto?
)

data class AddAlterationCommentRequest(
    @SerializedName("commentText") val commentText: String
)

data class AlterationCommentItem(
    @SerializedName("title") val title: String? = null,
    @SerializedName("desc") val desc: String? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("time") val time: String? = null
)

data class AddAlterationCommentData(
    @SerializedName("success") val success: Boolean,
    @SerializedName("comment") val comment: AlterationCommentItem?
)

data class AddAlterationCommentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: AddAlterationCommentData?
)