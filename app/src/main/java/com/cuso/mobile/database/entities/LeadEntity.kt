// database/entities/LeadEntity.kt
package com.cuso.mobile.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cuso.mobile.model.sales.CreateLeadFormRequest
import com.cuso.mobile.model.sales.CreateLeadFormResponse

@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey
    val id: String,
    val customerType: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val gender: String,
    val dob: String,
    val address: String,
    val area: String,
    val city: String,
    val preferredContactMethod: String,
    val enquiryType: String,
    val estimatedQuantity: Int,
    val budgetMin: Int,
    val budgetMax: Int,
    val occasion: String,
    val garments: String,
    val enquiryDate: String?= null,
    val requiredDate: String?= null,
    val source: String,
    val status: String,
    val leadOwner: String = "",          // ✅ NEW — default "" so existing LeadEntity(...) calls don't break
    val appointmentRequired: Boolean,
    val appointmentDate: String,
    val appointmentTime: String?,
    val assignedStaff: String?,
    val priority: String?,
    val followUpDate: String,
    val internalNotes: String,
    val customerNotes: String,
    val createdAt: String
)

// Updated toEntity extension function
fun CreateLeadFormResponse.toEntity(request: CreateLeadFormRequest): LeadEntity {
    val d = this.data ?: throw IllegalStateException("Response data is null")

    // Helper to extract garment ID from either String or Object
    fun extractGarmentId(garment: Any?): String {
        return when (garment) {
            is String -> garment
            is Map<*, *> -> (garment["_id"] as? String) ?: ""
            else -> ""
        }
    }

    // Helper to extract status name from either String or Object
    fun extractStatusName(status: Any?): String {
        return when (status) {
            is String -> status
            is Map<*, *> -> (status["name"] as? String) ?: ""
            else -> ""
        }
    }

    // Get garment ID from response or fallback to request
    val garmentId = d.garmentCategory?.firstOrNull()?.let { extractGarmentId(it) }
        ?: request.garments.firstOrNull()
        ?: ""

    // Get status name from response or fallback to request
    val statusName = if (request.statusName.isNotEmpty()) {
        request.statusName
    } else {
        extractStatusName(d.status)
    }

    // Get occasion from response or fallback to request
    val occasionValue = d.occasion?.takeIf { it.isNotEmpty() } ?: request.occasion

    return LeadEntity(
        id = d._id ?: "",
        customerType = d.customerType ?: "",
        status = statusName,
        createdAt = d.createdAt ?: "",
        fullName = request.person.name,
        phone = request.person.phone,
        email = request.person.email,
        gender = request.person.gender,
        dob = request.person.dob,
        address = request.contact.address,
        area = request.contact.area,
        city = request.contact.city,
        preferredContactMethod = request.contact.preferredContactMethod,
        enquiryType = request.enquiryType,
        estimatedQuantity = request.estimatedQuantity,
        budgetMin = request.budgetRange.min,
        budgetMax = request.budgetRange.max,
        occasion = occasionValue,
        garments = garmentId,
        enquiryDate = request.enquiryDate,
        requiredDate = request.requiredDate,
        source = request.source,
        leadOwner = request.leadOwner,        // ✅ NEW — assuming CreateLeadFormRequest gets a leadOwner field
        appointmentRequired = request.appointment.isRequired,
        appointmentDate = request.appointment.date ?: "",
        appointmentTime = request.appointment.time,
        assignedStaff = request.appointment.assignedStaff,
        priority = request.appointment.priority,
        followUpDate = request.appointment.followUpDate ?: "",
        internalNotes = request.notes.firstOrNull { it.type == "internal" }?.message ?: "",
        customerNotes = request.notes.firstOrNull { it.type == "customer" }?.message ?: ""
    )
}