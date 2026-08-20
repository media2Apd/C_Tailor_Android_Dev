// model/LeadTableItem.kt
package com.cuso.mobile.model.sales

import com.cuso.mobile.database.entities.LeadEntity
import com.google.gson.annotations.SerializedName
import kotlin.collections.get

data class LeadsTableResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<LeadTableItem>,
    @SerializedName("total") val total: Int
)

data class LeadTableItem(
    @SerializedName("_id") val id: String,
    @SerializedName("customerType") val customerType: String,
    @SerializedName("person") val person: PersonTableItem,
    @SerializedName("enquiryType") val enquiryType: String,
    @SerializedName("garmentCategory") val garmentCategory: List<Any>?,
    @SerializedName("estimatedQuantity") val estimatedQuantity: Int,
    @SerializedName("source") val source: String,
    @SerializedName("budgetRange") val budgetRange: BudgetRangeTableItem,
    @SerializedName("occasion") val occasion: String?,
    @SerializedName("enquiryDate") val enquiryDate: String,
    @SerializedName("requiredDate") val requiredDate: String?,
    @SerializedName("status") val status: Any,
    @SerializedName("appointment") val appointment: AppointmentTableItem?,
    @SerializedName("notes") val notes: List<NoteTableItem>?,
    @SerializedName("convertedCustomerId") val convertedCustomerId: String?,   //   NEW
    @SerializedName("convertedOrderId") val convertedOrderId: String?,        //   NEW
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)
data class GarmentCategoryItem(
    val id: String = "",
    val categoryName: String = ""
)

data class StatusItem(
    val id: String = "",
    val name: String = ""
)
data class PersonTableItem(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String
)

data class BudgetRangeTableItem(
    @SerializedName("min") val min: Int,
    @SerializedName("max") val max: Int
)

data class AppointmentTableItem(
    @SerializedName("isRequired") val isRequired: Boolean,
    @SerializedName("date") val date: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("assignedStaff") val assignedStaff: String?,
    @SerializedName("priority") val priority: String?,
    @SerializedName("followUpDate") val followUpDate: String?
)

data class NoteTableItem(
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("_id") val id: String,
    @SerializedName("addedAt") val addedAt: String
)

//   Extension to convert LeadTableItem to LeadEntity (for edit/view only)
fun LeadTableItem.toLeadEntity(): LeadEntity {
    // Extract status name
    val statusName = when (this.status) {
        is String -> this.status
        is Map<*, *> -> (this.status["name"] as? String) ?: ""
        else -> ""
    }

    // Extract garment ID
    val garmentId = this.garmentCategory?.firstOrNull()?.let { garment ->
        when (garment) {
            is String -> garment
            is Map<*, *> -> (garment["_id"] as? String) ?: ""
            else -> ""
        }
    } ?: ""

    // Get appointment values
    val appointment = this.appointment ?: AppointmentTableItem(false, null, null, null, null, null)
    val notes = this.notes ?: emptyList()

    return LeadEntity(
        id = this.id,
        customerType = this.customerType,
        fullName = this.person.name,
        phone = this.person.phone,
        email = this.person.email,
        gender = "",
        dob = "",
        address = "",
        area = "",
        city = "",
        preferredContactMethod = "",
        enquiryType = this.enquiryType,
        estimatedQuantity = this.estimatedQuantity,
        budgetMin = this.budgetRange.min,
        budgetMax = this.budgetRange.max,
        occasion = this.occasion ?: "",
        garments = garmentId,
        enquiryDate = this.enquiryDate,
        requiredDate = this.requiredDate ?: "",
        source = this.source,
        status = statusName,
        appointmentRequired = appointment.isRequired,
        appointmentDate = appointment.date ?: "",
        appointmentTime = appointment.time ?: "",
        assignedStaff = appointment.assignedStaff ?: "",
        priority = appointment.priority ?: "",
        followUpDate = appointment.followUpDate ?: "",
        internalNotes = notes.firstOrNull { it.type == "internal" }?.message ?: "",
        customerNotes = notes.firstOrNull { it.type == "customer" }?.message ?: "",
        createdAt = this.createdAt
    )
}

//convert to order
data class ConvertLeadToOrderResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data:ConvertToOrderData
)
data class ConvertToOrderData(
    @SerializedName("message")
    val message:String,
    @SerializedName("customerId")
    val customerId:String,
    @SerializedName("orderId")
    val orderId:String
)