package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName
data class UpdateLeadResponse(
    val success: Boolean,
    val data: UpdatedLeadData
)

data class UpdatedLeadData(
    val _id: String,
    val organizationId: String,
    val customerType: String,
    val person: PersonData?,
    val enquiryType: String,
    val garmentCategory: List<String>?,
    val estimatedQuantity: Int?,
    val source: String,
    val occasion: String?,
    val enquiryDate: String,
    val requiredDate: String?,
    val status: String,               // ⚠️ plain string id here (not object)
    val leadOwner: String?,           // ⚠️ plain string id here
    val budgetRange: BudgetRangeData?,
    val appointment: UpdatedAppointmentData?,
    val followUpCount: Int?,
    val notes: List<NoteData>?,
    val customFields: Map<String, Any>?,
    val attachments: List<Any>?,
    val convertedCustomerId: String?,
    val convertedOrderId: String?,
    val isDeleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val updatedBy: String?,
    val convertedAt: String?,
    val followUpDate: String?,
    val stage: String?,
    val __v: Int
)

data class UpdatedAppointmentData(
    val isRequired: Boolean,
    val date: String?,
    val time: String?,
    val assignedStaff: String?,       // ⚠️ plain string id here
    val priority: String?,
    val followUpDate: String?
)

data class UpdatedPerson(val name: String, val phone: String, val email: String)
data class UpdatedBudgetRange(val min: Int, val max: Int)
data class UpdatedAppointment(val isRequired: Boolean)
data class UpdatedNote(
    @SerializedName("_id") val id: String,
    val message: String,
    val type: String,
    val addedAt: String
)


data class UpdateLeadRequest(
    val customerType: String,
    val enquiryType: String,
    val estimatedQuantity: Int,
    val budgetRange: BudgetRangeRequest,
    val enquiryDate: String,
    val requiredDate: String?,
    val status: String,
    val source: String,
    val person: PersonRequest,
    val appointment: AppointmentRequest,
    val notes: List<NoteRequest>,
    val contact: ContactRequest,
    val garmentCategory: List<String>
)

data class BudgetRangeRequest(val min: Int, val max: Int)

data class PersonRequest(
    val name: String,
    val phone: String,
    val email: String,
    val gender: String,
    val dob: String
)

data class AppointmentRequest(
    val isRequired: Boolean,
    val date: String? = null,
    val time: String? = null,
    val assignedStaff: String? = null,
    val priority: String? = null,
    val followUpDate: String? = null
)

data class NoteRequest(
    val message: String,
    val type: String
)

data class ContactRequest(
    val address: String,
    val area: String,
    val city: String,
    val preferredContactMethod: String
)
