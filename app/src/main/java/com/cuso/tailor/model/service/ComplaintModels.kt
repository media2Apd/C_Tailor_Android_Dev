@file:Suppress("unused")
package com.cuso.tailor.model.service

import com.google.gson.annotations.SerializedName

data class ComplaintListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ComplaintItemDto>?,
    @SerializedName("pagination") val pagination: ComplaintPaginationDto?
)

data class ComplaintPaginationDto(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean
)

data class ComplaintItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("complaintCode") val complaintCode: String,
    @SerializedName("salesOrderId") val salesOrder: ComplaintOrderDto? = null,
    @SerializedName("customerId") val customer: ComplaintCustomerDto? = null,
    @SerializedName("customerSnapshot") val customerSnapshot: ComplaintCustomerSnapshotDto? = null,
    @SerializedName("garmentDetails") val garmentDetails: ComplaintGarmentDetailsDto? = null,
    @SerializedName("complaintCategory") val complaintCategory: String? = null,
    @SerializedName("defectSubCategory") val defectSubCategory: String? = null,
    @SerializedName("severity") val severity: String? = null,
    @SerializedName("complaintTitle") val complaintTitle: String? = null,
    @SerializedName("assignedTo") val assignedTo: ComplaintAssignedUserDto? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("targetResolutionDate") val targetResolutionDate: String? = null,
    @SerializedName("slaBreached") val slaBreached: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    // Computed UI helper properties
    val customerDisplayName: String
        get() = customerSnapshot?.name ?: customer?.fullName ?: "Customer"

    val displayCode: String
        get() = complaintCode

    val contactPhone: String
        get() = customerSnapshot?.phone ?: customer?.mobileNumber ?: "—"

    val categoryDisplay: String
        get() = complaintCategory?.replace("_", " ") ?: "General"

    val complaintDisplay: String
        get() = complaintTitle?.take(18)?.let { if (complaintTitle.length > 18) "$it..." else it }
            ?: garmentDetails?.garmentName
            ?: "—"
}

data class ComplaintOrderDto(
    @SerializedName("_id") val id: String,
    @SerializedName("orderCode") val orderCode: String?
)

data class ComplaintCustomerDto(
    @SerializedName("_id") val id: String,
    @SerializedName("customerCode") val customerCode: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("mobileNumber") val mobileNumber: String?
)

data class ComplaintCustomerSnapshotDto(
    @SerializedName("customerCode") val customerCode: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?
)

data class ComplaintGarmentDetailsDto(
    @SerializedName("orderItemId") val orderItemId: String? = null,
    @SerializedName("lineType") val lineType: String? = null,
    @SerializedName("garmentName") val garmentName: String? = null,
    @SerializedName("garmentCategoryName") val garmentCategoryName: String? = null,
    @SerializedName("orderCode") val orderCode: String? = null,
    @SerializedName("deliveredDate") val deliveredDate: String? = null,
    @SerializedName("unitPrice") val unitPrice: Double? = 0.0,
    @SerializedName("wornCount") val wornCount: String? = null
)

data class ComplaintAssignedUserDto(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?
)

data class SingleComplaintResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ComplaintDetailDataDto?
)

data class ComplaintDetailDataDto(
    @SerializedName("_id") val id: String,
    @SerializedName("complaintCode") val complaintCode: String,
    @SerializedName("salesOrderId") val salesOrder: ComplaintOrderDetailDto? = null,
    @SerializedName("customerId") val customer: ComplaintCustomerFullDto? = null,
    @SerializedName("customerSnapshot") val customerSnapshot: ComplaintCustomerSnapshotDto? = null,
    @SerializedName("garmentDetails") val garmentDetails: ComplaintGarmentDetailsDto? = null,
    @SerializedName("complaintCategory") val complaintCategory: String? = null,
    @SerializedName("defectSubCategory") val defectSubCategory: String? = null,
    @SerializedName("severity") val severity: String? = null,
    @SerializedName("complaintTitle") val complaintTitle: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("customerExpectedOutcome") val customerExpectedOutcome: String? = null,
    @SerializedName("attachments") val attachments: List<EvidenceAttachmentDto> = emptyList(),
    @SerializedName("assignedTo") val assignedTo: ComplaintAssignedStaffDto? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("targetResolutionDate") val targetResolutionDate: String? = null,
    @SerializedName("slaBreached") val slaBreached: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("resolution") val resolution: ComplaintResolutionDto? = null
)

data class ComplaintOrderDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("orderCode") val orderCode: String?,
    @SerializedName("orderDate") val orderDate: String?,
    @SerializedName("grandTotal") val grandTotal: Double?,
    @SerializedName("status") val status: String?
)

data class ComplaintCustomerFullDto(
    @SerializedName("_id") val id: String,
    @SerializedName("customerCode") val customerCode: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("mobileNumber") val mobileNumber: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("customerLevel") val customerLevel: String?,
    @SerializedName("billingAddress") val billingAddress: ComplaintAddressDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class ComplaintAddressDto(
    @SerializedName("flatNo") val flatNo: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("areaZone") val areaZone: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("pincode") val pincode: String? = null
) {
    val fullAddressDisplay: String
        get() = listOfNotNull(
            flatNo?.takeIf { it.isNotBlank() },
            street?.takeIf { it.isNotBlank() },
            areaZone?.takeIf { it.isNotBlank() },
            city?.takeIf { it.isNotBlank() },
            pincode?.takeIf { it.isNotBlank() }
        ).joinToString(", ").ifBlank { "Location not provided" }
}

data class ComplaintAssignedStaffDto(
    @SerializedName("_id") val id: String,
    @SerializedName("role") val role: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?
) {
    val fullName: String
        get() = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifEmpty { "Unassigned" }
}

data class ComplaintResolutionDto(
    @SerializedName("action") val action: String?,
    @SerializedName("compensationAmount") val compensationAmount: Double?,
    @SerializedName("approvalStatus") val approvalStatus: String?,
    @SerializedName("isCustomerAgreed") val isCustomerAgreed: Boolean?
)

data class UploadComplaintAttachmentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ComplaintDetailDataDto?
)

data class UpdateComplaintStatusRequest(
    @SerializedName("status") val status: String
)

data class UpdateComplaintStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ComplaintDetailDataDto?
)

data class AssignComplaintStaffRequest(
    @SerializedName("assignedTo") val assignedTo: String
)

data class AssignComplaintStaffResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ComplaintDetailDataDto?
)

data class AddComplaintInternalNoteRequest(
    @SerializedName("investigationNotes") val investigationNotes: String
)

data class AddComplaintInternalNoteResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ComplaintDetailDataDto?
)

data class ResolveComplaintRequest(
    @SerializedName("action") val action: String,
    @SerializedName("compensationAmount") val compensationAmount: Int = 0,
    @SerializedName("isCustomerAgreed") val isCustomerAgreed: Boolean = true,
    @SerializedName("resolutionSummary") val resolutionSummary: String,
    @SerializedName("voucherCode") val voucherCode: String = ""
)

data class ResolveComplaintResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ComplaintDetailDataDto?
)