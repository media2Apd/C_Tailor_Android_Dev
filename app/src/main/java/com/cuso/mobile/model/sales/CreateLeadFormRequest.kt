// LeadModels.kt
package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName


// In CreateLeadFormRequest.kt - Add occasion field

data class CreateLeadFormRequest(
    val customerType: String,
    val enquiryType: String,
    val estimatedQuantity: Int,
    val budgetRange: BudgetRange,
    @SerializedName("garmentCategory")   //   backend expects "garmentCategory", not "garments"
    val garments: List<String>,
    val enquiryDate: String,
    val requiredDate: String,
    val source: String,
    val leadOwner: String = "",   //   NEW — staff ID, same convention as appointment.assignedStaff
    val person: LeadPerson,
    val contact: LeadContact,
    val appointment: LeadAppointment,
    val status: String,
    val statusName: String,
    val notes: List<LeadNote>,
    val occasion: String = ""
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
    val garmentCategory: List<Any>? = null, // Can be String or Object
    val occasion: String? = null  //   ADD THIS
)