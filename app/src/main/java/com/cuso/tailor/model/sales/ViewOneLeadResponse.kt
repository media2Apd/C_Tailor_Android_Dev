package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName
data class ViewOneLeadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ViewOneLeadData
)

data class ViewOneLeadData(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("customerType") val customerType: String = "Individual",
    @SerializedName("enquiryType") val enquiryType: String? = null,
    @SerializedName("requiredDate") val requiredDate: String? = null,
    @SerializedName("enquiryDate") val enquiryDate: String = "",
    @SerializedName("occasion") val occasion: String? = null,

    // Lead Source & Status
    @SerializedName("leadSource") val leadSource: String? = null,
    @SerializedName("source") val legacySource: String? = null,
    @SerializedName("leadStatus") val leadStatus: String? = null,
    @SerializedName("status") val rawStatus: Any? = null,

    // Lead Owner
    @SerializedName("leadOwner") val leadOwner: StaffRef? = null,

    // Customer Identity (Flat fields from JSON)
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("preferredContactMethod") val preferredContactMethod: String? = null,

    // Address
    @SerializedName("address") val rawAddress: Any? = null,

    // Budget
    @SerializedName("budgetMin") val budgetMin: Int? = null,
    @SerializedName("budgetMax") val budgetMax: Int? = null,

    // Garments
    @SerializedName("garmentSpecifications") val garmentSpecifications: List<GarmentSpecDetail>? = null,
    @SerializedName("garmentCategory") val legacyGarmentCategory: List<Any>? = null,
    @SerializedName("estimatedQuantity") val legacyEstimatedQuantity: Int? = null,

    // Appointment
    @SerializedName("isAppointmentRequired") val isAppointmentRequired: Boolean? = null,
    @SerializedName("appointmentDate") val appointmentDate: String? = null,
    @SerializedName("appointmentTime") val appointmentTime: String? = null,
    @SerializedName("appointmentStatus") val appointmentStatus: String? = null,
    @SerializedName("assignedStaffId") val assignedStaffId: StaffRef? = null,
    @SerializedName("followUpDate") val followUpDate: String? = null,
    @SerializedName("priorityLevel") val priorityLevel: String? = null,

    // Notes
    @SerializedName("internalNotes") val internalNotesText: String? = null,
    @SerializedName("customerNotes") val customerNotesText: String? = null,
    @SerializedName("notes") val notesList: List<NoteData>? = null,

    // Meta
    @SerializedName("isFabricProvided") val isFabricProvided: Boolean? = null,
    @SerializedName("fabricSource") val fabricSource: String? = null,
    @SerializedName("fabricNotes") val fabricNotes: String? = null,
    @SerializedName("attachments") val attachments: List<Any>? = null,
    @SerializedName("convertedCustomerId") val convertedCustomerId: String? = null,
    @SerializedName("convertedOrderId") val convertedOrderId: String? = null,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = "",
    @SerializedName("__v") val __v: Int? = null
) {
    val effectiveName: String
        get() = fullName ?: "—"

    val effectivePhone: String
        get() = mobileNumber ?: ""

    val effectiveEmail: String
        get() = email ?: ""

    val effectiveSource: String
        get() = leadSource ?: legacySource ?: "—"

    val effectiveStatus: String
        get() = when {
            !leadStatus.isNullOrBlank() -> leadStatus
            rawStatus is String -> rawStatus
            rawStatus is Map<*, *> -> (rawStatus["name"] as? String) ?: "Active"
            else -> "Active"
        }

    val streetAddress: String
        get() = when (rawAddress) {
            is Map<*, *> -> (rawAddress["street"] as? String) ?: ""
            is String -> rawAddress
            else -> ""
        }

    val areaName: String
        get() = when (rawAddress) {
            is Map<*, *> -> (rawAddress["areaZone"] as? String) ?: ""
            else -> ""
        }

    val cityName: String
        get() = when (rawAddress) {
            is Map<*, *> -> (rawAddress["city"] as? String) ?: ""
            else -> ""
        }

    val minBudgetVal: Int
        get() = budgetMin ?: 0

    val maxBudgetVal: Int
        get() = budgetMax ?: 0

    val totalQuantity: Int
        get() = garmentSpecifications?.sumOf { it.quantity } ?: legacyEstimatedQuantity ?: 1

    val effectiveGarmentIds: String
        get() = garmentSpecifications?.mapNotNull { it.garmentCategoryId?._id ?: it.garmentId?._id }?.joinToString(",") ?: ""
}

data class StaffRef(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)

data class GarmentSpecDetail(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("garmentId") val garmentId: GarmentSubItem? = null,
    @SerializedName("garmentCategoryId") val garmentCategoryId: GarmentCatSubItem? = null
)

data class GarmentSubItem(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("displayName") val displayName: String = "",
    @SerializedName("code") val code: String = ""
)

data class GarmentCatSubItem(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("displayName") val displayName: String = "",
    @SerializedName("sku") val sku: String = ""
)

data class NoteData(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("message") val message: String = "",
    @SerializedName("type") val type: String = "internal",
    @SerializedName("addedAt") val addedAt: String? = null
)