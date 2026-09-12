package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

data class UpdateLeadResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: UpdatedLeadData? = null
)

data class UpdatedLeadData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("customerType") val customerType: String = "Individual",
    @SerializedName("enquiryType") val enquiryType: String? = null,
    @SerializedName("enquiryDate") val enquiryDate: String = "",
    @SerializedName("requiredDate") val requiredDate: String? = null,
    @SerializedName("leadSource") val leadSource: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("leadStatus") val leadStatus: String? = null,
    @SerializedName("status") val status: Any? = null,
    @SerializedName("leadOwner") val leadOwner: StaffRef? = null,

    // Customer Details
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("address") val address: Any? = null,
    @SerializedName("preferredContactMethod") val preferredContactMethod: String? = null,

    // Budget
    @SerializedName("budgetMin") val budgetMin: Int? = null,
    @SerializedName("budgetMax") val budgetMax: Int? = null,

    // Garment & Specs
    @SerializedName("garmentSpecifications") val garmentSpecifications: List<GarmentSpecDetail>? = null,
    @SerializedName("isFabricProvided") val isFabricProvided: Boolean? = null,
    @SerializedName("fabricSource") val fabricSource: String? = null,
    @SerializedName("fabricNotes") val fabricNotes: String? = null,

    // Appointment
    @SerializedName("isAppointmentRequired") val isAppointmentRequired: Boolean? = null,
    @SerializedName("appointmentDate") val appointmentDate: String? = null,
    @SerializedName("appointmentTime") val appointmentTime: String? = null,
    @SerializedName("assignedStaffId") val assignedStaffId: StaffRef? = null,
    @SerializedName("appointmentStatus") val appointmentStatus: String? = null,
    @SerializedName("followUpDate") val followUpDate: String? = null,
    @SerializedName("priorityLevel") val priorityLevel: String? = null,

    // Notes
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("customerNotes") val customerNotes: String? = null,

    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
)

data class UpdateLeadRequest(
    @SerializedName("customerType") val customerType: String,
    @SerializedName("enquiryType") val enquiryType: String,
    @SerializedName("estimatedQuantity") val estimatedQuantity: Int,
    @SerializedName("budgetRange") val budgetRange: BudgetRangeRequest,
    @SerializedName("enquiryDate") val enquiryDate: String,
    @SerializedName("requiredDate") val requiredDate: String?,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("leadStatus") val leadStatus: String? = null,
    @SerializedName("source") val source: String,
    @SerializedName("person") val person: PersonRequest,
    @SerializedName("appointment") val appointment: AppointmentRequest,
    @SerializedName("notes") val notes: List<NoteRequest>,
    @SerializedName("contact") val contact: ContactRequest,
    @SerializedName("garmentCategory") val garmentCategory: List<String>
)

data class BudgetRangeRequest(
    @SerializedName("min") val min: Int,
    @SerializedName("max") val max: Int
)

data class PersonRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("dob") val dob: String
)

data class AppointmentRequest(
    @SerializedName("isRequired") val isRequired: Boolean,
    @SerializedName("date") val date: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("assignedStaff") val assignedStaff: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("followUpDate") val followUpDate: String? = null
)

data class NoteRequest(
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String
)

data class ContactRequest(
    @SerializedName("address") val address: String,
    @SerializedName("area") val area: String,
    @SerializedName("city") val city: String,
    @SerializedName("preferredContactMethod") val preferredContactMethod: String
)