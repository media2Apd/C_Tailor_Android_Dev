package com.cuso.mobile.model.sales

import com.cuso.mobile.database.entities.LeadEntity
import com.google.gson.annotations.SerializedName

data class LeadsTableResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LeadTableItem> = emptyList(),
    @SerializedName("total") val totalDirect: Int? = null,
    @SerializedName("pagination") val pagination: PaginationDto? = null
) {
    val total: Int
        get() = pagination?.total ?: totalDirect ?: data.size
}

data class PaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1
)

data class LeadTableItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("customerType") val customerType: String? = null,

    // Flat fields from JSON
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("leadSource") val leadSource: String? = null,
    @SerializedName("source") val legacySource: String? = null,
    @SerializedName("leadStatus") val leadStatus: String? = null,
    @SerializedName("status") val rawStatus: Any? = null,
    @SerializedName("enquiryType") val enquiryType: String? = null,
    @SerializedName("requiredDate") val requiredDate: String? = null,
    @SerializedName("enquiryDate") val enquiryDate: String? = null,
    @SerializedName("budgetMin") val budgetMin: Int? = null,
    @SerializedName("budgetMax") val budgetMax: Int? = null,
    @SerializedName("isAppointmentRequired") val isAppointmentRequired: Boolean? = null,
    @SerializedName("appointmentDate") val appointmentDate: String? = null,
    @SerializedName("appointmentTime") val appointmentTime: String? = null,
    @SerializedName("followUpDate") val followUpDate: String? = null,
    @SerializedName("priorityLevel") val priorityLevel: String? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("customerNotes") val customerNotes: String? = null,
    @SerializedName("garmentSpecifications") val garmentSpecifications: List<GarmentSpecificationItem>? = null,
    @SerializedName("assignedStaffId") val assignedStaffId: Any? = null,

    // Nested fallback support
    @SerializedName("person") val nestedPerson: PersonTableItem? = null,
    @SerializedName("budgetRange") val nestedBudgetRange: BudgetRangeTableItem? = null,
    @SerializedName("garmentCategory") val garmentCategory: List<Any>? = null,
    @SerializedName("estimatedQuantity") val legacyQuantity: Int? = null,
    @SerializedName("appointment") val nestedAppointment: AppointmentTableItem? = null,
    @SerializedName("notes") val notes: List<NoteTableItem>? = null,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
) {
    val name: String
        get() = fullName ?: nestedPerson?.name ?: "—"

    val phone: String
        get() = mobileNumber ?: nestedPerson?.phone ?: ""

    val emailAddress: String
        get() = email ?: nestedPerson?.email ?: ""

    val effectiveSource: String
        get() = leadSource ?: legacySource ?: "—"

    val effectiveStatus: String
        get() = when {
            !leadStatus.isNullOrBlank() -> leadStatus
            rawStatus is String -> rawStatus
            rawStatus is Map<*, *> -> (rawStatus["name"] as? String) ?: ""
            else -> "—"
        }

    val minBudget: Int
        get() = budgetMin ?: nestedBudgetRange?.min ?: 0

    val maxBudget: Int
        get() = budgetMax ?: nestedBudgetRange?.max ?: 0

    val effectiveQuantity: Int
        get() = garmentSpecifications?.sumOf { it.quantity } ?: legacyQuantity ?: 1

    val garmentName: String
        get() {
            val specName = garmentSpecifications?.firstOrNull()?.garmentCategoryId?.name
                ?: garmentSpecifications?.firstOrNull()?.garmentId?.name
            if (!specName.isNullOrBlank()) return specName

            val legacyGarment = garmentCategory?.firstOrNull()
            return when (legacyGarment) {
                is Map<*, *> -> {
                    val category = legacyGarment["categoryId"] as? Map<*, *>
                    (category?.get("categoryName") as? String) ?: "—"
                }
                is String -> legacyGarment
                else -> "—"
            }
        }
}

data class GarmentSpecificationItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("garmentId") val garmentId: GarmentRef? = null,
    @SerializedName("garmentCategoryId") val garmentCategoryId: GarmentCategoryRef? = null
)

data class GarmentRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("displayName") val displayName: String = ""
)

data class GarmentCategoryRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("displayName") val displayName: String = ""
)

data class PersonTableItem(
    @SerializedName("name") val name: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("email") val email: String = ""
)

data class BudgetRangeTableItem(
    @SerializedName("min") val min: Int = 0,
    @SerializedName("max") val max: Int = 0
)

data class AppointmentTableItem(
    @SerializedName("isRequired") val isRequired: Boolean = false,
    @SerializedName("date") val date: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("assignedStaff") val assignedStaff: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("followUpDate") val followUpDate: String? = null
)

data class NoteTableItem(
    @SerializedName("message") val message: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("_id") val id: String = ""
)

fun LeadTableItem.toLeadEntity(): LeadEntity {
    return LeadEntity(
        id = this.id,
        customerType = this.customerType ?: "Individual",
        fullName = this.name,
        phone = this.phone,
        email = this.emailAddress,
        gender = this.gender ?: "",
        dob = this.dateOfBirth ?: "",
        address = "",
        area = "",
        city = "",
        preferredContactMethod = "",
        enquiryType = this.enquiryType ?: "",
        estimatedQuantity = this.effectiveQuantity,
        budgetMin = this.minBudget,
        budgetMax = this.maxBudget,
        occasion = "",
        garments = "",
        enquiryDate = this.enquiryDate ?: "",
        requiredDate = this.requiredDate ?: "",
        source = this.effectiveSource,
        status = this.effectiveStatus,
        appointmentRequired = this.isAppointmentRequired ?: false,
        appointmentDate = this.appointmentDate ?: "",
        appointmentTime = this.appointmentTime ?: "",
        assignedStaff = when (val staff = this.assignedStaffId) {
            is Map<*, *> -> (staff["_id"] as? String) ?: ""
            is String -> staff
            else -> ""
        },
        priority = this.priorityLevel ?: "",
        followUpDate = this.followUpDate ?: "",
        internalNotes = this.internalNotes ?: "",
        customerNotes = this.customerNotes ?: "",
        createdAt = this.createdAt
    )
}

data class ConvertLeadToOrderResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ConvertToOrderData
)

data class ConvertToOrderData(
    @SerializedName("message") val message: String,
    @SerializedName("customerId") val customerId: String,
    @SerializedName("orderId") val orderId: String
)