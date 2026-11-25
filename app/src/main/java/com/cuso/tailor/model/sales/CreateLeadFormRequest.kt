@file:Suppress("unused", "AssignedValueIsNeverUsed")

// LeadModels.kt
package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName

data class GarmentCategoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<GarmentCategoryDto> = emptyList()
)

data class GarmentCategoryDto(
    @SerializedName("_id") val id: String,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("displayName") val displayNameRaw: String? = null,
    @SerializedName("garmentId") val garmentIdObj: GarmentCategoryGarmentRef? = null,
    @SerializedName("measurementFields") val measurementFields: List<GarmentCategoryMeasurementFieldDto>? = null
) {
    val displayName: String
        get() = displayNameRaw?.ifBlank { null }
            ?: categoryName?.ifBlank { null }
            ?: name
            ?: ""
}

data class GarmentCategoryGarmentRef(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("measurementFields") val measurementFields: List<GarmentCategoryMeasurementFieldDto>? = null
)

data class GarmentCategoryMeasurementFieldDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("fieldId") val fieldId: GarmentCategoryFieldDefDto? = null,
    @SerializedName("isRequired") val isRequired: Boolean = false,
    @SerializedName("displayOrder") val displayOrder: Int = 0
)

data class GarmentCategoryFieldDefDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("groupName") val groupName: String? = null,
    @SerializedName("inputType") val inputType: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("minValue") val minValue: Double? = null,
    @SerializedName("maxValue") val maxValue: Double? = null
)
// In CreateLeadFormRequest.kt - Add occasion field

data class CreateLeadFormRequest(
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("leadSource") val leadSource: String,
    @SerializedName("enquiryDate") val enquiryDate: String,
    @SerializedName("leadOwner") val leadOwner: String? = null,
    @SerializedName("leadStatus") val leadStatus: String,
    @SerializedName("customerType") val customerType: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("preferredContactMethod") val preferredContactMethod: String? = null,
    @SerializedName("preferredLanguage") val preferredLanguage: String? = "English",
    @SerializedName("enquiryType") val enquiryType: String,
    @SerializedName("requiredDate") val requiredDate: String,
    @SerializedName("address") val address: LeadAddressDto? = null,
    @SerializedName("garmentSpecifications") val garmentSpecifications: List<LeadGarmentSpecificationPayload> = emptyList(),
    @SerializedName("budgetMin") val budgetMin: Int? = null,
    @SerializedName("budgetMax") val budgetMax: Int? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("isFabricProvided") val isFabricProvided: Boolean = false,
    @SerializedName("fabricSource") val fabricSource: String? = null,
    @SerializedName("fabricNotes") val fabricNotes: String? = null,
    @SerializedName("isAppointmentRequired") val isAppointmentRequired: Boolean = false,
    @SerializedName("appointmentDate") val appointmentDate: String? = null,
    @SerializedName("appointmentTime") val appointmentTime: String? = null,
    @SerializedName("appointmentStatus") val appointmentStatus: String? = null,
    @SerializedName("assignedStaffId") val assignedStaffId: String? = null,
    @SerializedName("followUpDate") val followUpDate: String? = null,
    @SerializedName("followUpTime") val followUpTime: String? = null,
    @SerializedName("priorityLevel") val priorityLevel: String? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("customerNotes") val customerNotes: String? = null,
    @SerializedName("status") val status: String = "Active"
)

data class LeadAddressDto(
    @SerializedName("flatNo") val flatNo: String = "",
    @SerializedName("street") val street: String = "",
    @SerializedName("areaZone") val areaZone: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("subdivisionName") val subdivisionName: String = "",
    @SerializedName("countryName") val countryName: String = "India",
    @SerializedName("pincode") val pincode: String = ""
)

data class LeadGarmentSpecificationPayload(
    @SerializedName("garmentId") val garmentId: String,
    @SerializedName("garmentCategoryId") val garmentCategoryId: String? = null,
    @SerializedName("quantity") val quantity: Int
)
// Add these data classes (e.g. in the same file as CreateLeadFormResponse, or a new GarmentCategory.kt)
// New Data class for Garment item in Lead Request
data class LeadGarmentRequestItem(
    @SerializedName("garmentId")
    val garmentId: String,
    @SerializedName("garmentCategoryId")
    val garmentCategoryId: String,
    @SerializedName("quantity")
    val quantity: Int = 1
)


data class CategoryId(
    val _id: String? = null,
    val categoryName: String? = null,
    val models: List<GarmentModel>? = null
)

data class GarmentCategory(
    val _id: String? = null,
    val organizationId: String? = null,
    val categoryId: CategoryId? = null,
    val isActive: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
data class BudgetRange(
    val min: Int,
    val max: Int
)
data class LeadPerson(
    val name: String,
    val phone: String,
    val email: String,
    val gender: String,      //   Make sure this is included
    val dob: String          //   Make sure this is included
)


data class LeadContact(
    val address: String,     //   Make sure this is included
    val area: String,        //   Make sure this is included
    val city: String,        //   Make sure this is included
    val preferredContactMethod: String  //   Make sure this is included
)

data class LeadAppointment(
    val isRequired: Boolean,
    val date: String? = null,
    val time: String? = null,
    val assignedStaff: String? = null,
    val priority: String? = null,
    val followUpDate: String? = null
)
data class LeadNote(
    val message: String,
    val type: String   // "internal" or "customer"
)

// Update CreateLeadFormResponse to handle complex garmentCategory
data class CreateLeadFormResponse(
    val success: Boolean,
    val data: LeadData?
)

// Update LeadData to handle garmentCategory as List<Any> (can be String or Object)
data class LeadData(
    val _id: String?,
    val customerType: String?,
    val status: Any?, // Can be String or StatusObject
    val createdAt: String?,
    val garmentCategory: List<Any>?,
    val occasion: String? = null  //   ADD THIS
)