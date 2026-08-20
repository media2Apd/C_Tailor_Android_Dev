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
    val enquiryType: String?,
    val estimatedQuantity: Int,
    val budgetMin: Int,
    val budgetMax: Int,
    val occasion: String,
    val garments: String,
    val enquiryDate: String? = null,
    val requiredDate: String? = null,
    val source: String,
    val status: String,
    val leadOwner: String = "",
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

//   Helper — works for both List<Any>? (garmentCategory / garments) items
private fun extractGarmentIdFromAny(garment: Any?): String {
    return when (garment) {
        is String -> garment
        is Map<*, *> -> {
            (garment["_id"] as? String)
                ?: ((garment["categoryId"] as? Map<*, *>)?.get("_id") as? String)
                ?: ""
        }
        else -> ""
    }
}

fun CreateLeadFormResponse.toEntity(request: CreateLeadFormRequest): LeadEntity {
    val d = this.data ?: throw IllegalStateException("Response data is null")

    fun extractStatusName(status: Any?): String {
        return when (status) {
            is String -> status
            is Map<*, *> -> (status["name"] as? String) ?: ""
            else -> ""
        }
    }

    //   response.data.garmentCategory is List<GarmentCategory>? (typed) — use _id directly
    //   fallback to request.garments which is List<Any>? now
    val garmentId = d.garmentCategory?.firstOrNull()?.let { extractGarmentIdFromAny(it) }
        ?.takeIf { it.isNotBlank() }
        ?: request.garments?.firstOrNull()?.let { extractGarmentIdFromAny(it) }
        ?: ""

    val statusName = request.statusName.ifEmpty {
        extractStatusName(d.status)
    }

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
        enquiryType = request.enquiryType ?: "unknown",
        estimatedQuantity = request.estimatedQuantity,
        budgetMin = request.budgetRange.min,
        budgetMax = request.budgetRange.max,
        occasion = occasionValue,
        garments = garmentId,
        enquiryDate = request.enquiryDate,
        requiredDate = request.requiredDate,
        source = request.source,
        leadOwner = request.leadOwner,
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