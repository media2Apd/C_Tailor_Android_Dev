// com/cuso/mobile/model/ViewOneLeadResponse.kt

package com.cuso.mobile.model

data class ViewOneLeadResponse(
    val success: Boolean,
    val data: ViewOneLeadData
)

data class ViewOneLeadData(
    val _id: String,
    val organizationId: String,
    val customerType: String,
    val person: PersonData?,
    val enquiryType: String,
    val garmentCategory: List<String>?,  // ✅ List of garment IDs
    val estimatedQuantity: Int?,
    val source: String,
    val budgetRange: BudgetRangeData?,
    val occasion: String?,
    val enquiryDate: String,
    val requiredDate: String?,
    val status: StatusData,  // ✅ Now an object
    val appointment: AppointmentData?,
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
    val __v: Int
)

data class StatusData(
    val _id: String,
    val name: String,
    val color: String
)

data class PersonData(
    val name: String,
    val phone: String,
    val email: String,
    val gender: String,
    val dob: String
)

//data class ContactData(
//    val address: String,
//    val area: String,
//    val city: String,
//    val preferredContactMethod: String
//)

data class BudgetRangeData(
    val min: Int,
    val max: Int
)

data class AppointmentData(
    val isRequired: Boolean,
    val date: String,
    val time: String,
    val assignedStaff: AssignedStaffData?,
    val priority: String,
    val followUpDate: String
)

data class AssignedStaffData(
    val _id: String
)

data class NoteData(
    val content: String,
    val type: String
)