// database/entities/LeadEntity.kt
package com.cuso.tailor.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cuso.tailor.model.sales.CreateLeadFormRequest
import com.cuso.tailor.model.sales.CreateLeadFormResponse

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

    // Resolve garment ID from response or from the request specifications list
    val garmentId = d.garmentCategory?.firstOrNull()?.let { extractGarmentIdFromAny(it) }
        ?.takeIf { it.isNotBlank() }
        ?: request.garmentSpecifications.firstOrNull()?.garmentId
        ?: ""

    val statusName = request.leadStatus.ifEmpty {
        extractStatusName(d.status)
    }

    val occasionValue = d.occasion?.takeIf { it.isNotEmpty() } ?: ""

    // Format address string from structured address
    val formattedAddress = listOfNotNull(
        request.address?.flatNo?.takeIf { it.isNotBlank() },
        request.address?.street?.takeIf { it.isNotBlank() }
    ).joinToString(", ")

    val totalQuantity = request.garmentSpecifications.sumOf { it.quantity }.coerceAtLeast(1)

    return LeadEntity(
        id = d._id ?: "",
        customerType = d.customerType ?: request.customerType,
        status = statusName,
        createdAt = d.createdAt ?: "",
        fullName = request.fullName,
        phone = request.mobileNumber,
        email = request.email.orEmpty(),
        gender = request.gender.orEmpty(),
        dob = request.dateOfBirth.orEmpty(),
        address = formattedAddress,
        area = request.address?.areaZone.orEmpty(),
        city = request.address?.city.orEmpty(),
        preferredContactMethod = request.preferredContactMethod.orEmpty(),
        enquiryType = request.enquiryType,
        estimatedQuantity = totalQuantity,
        budgetMin = request.budgetMin ?: 0,
        budgetMax = request.budgetMax ?: 0,
        occasion = occasionValue,
        garments = garmentId,
        enquiryDate = request.enquiryDate,
        requiredDate = request.requiredDate,
        source = request.leadSource,
        leadOwner = request.leadOwner,
        appointmentRequired = request.isAppointmentRequired,
        appointmentDate = request.appointmentDate.orEmpty(),
        appointmentTime = request.appointmentTime,
        assignedStaff = request.assignedStaffId,
        priority = request.priorityLevel,
        followUpDate = request.followUpDate.orEmpty(),
        internalNotes = request.internalNotes.orEmpty(),
        customerNotes = request.customerNotes.orEmpty()
    )
}