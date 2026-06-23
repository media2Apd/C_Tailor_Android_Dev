package com.cuso.mobile.model

import com.google.gson.annotations.SerializedName

data class UpdateLeadResponse(
    val success: Boolean,
    val data: UpdatedLeadData
)

data class UpdatedLeadData(
    @SerializedName("_id")        val id: String,
    val customerType:             String,
    val enquiryType:              String,
    val garmentCategory:          List<String>,
    val estimatedQuantity:        Int,
    val source:                   String,
    val enquiryDate:              String,
    val requiredDate:             String,
    val status:                   String,
    val followUpCount:            Int,
    val followUpDate:             String?,
    val isDeleted:                Boolean,
    val createdAt:                String,
    val updatedAt:                String,
    val person:                   UpdatedPerson,
    val budgetRange:              UpdatedBudgetRange,
    val appointment:              UpdatedAppointment,
    val notes:                    List<UpdatedNote>,
    val customFields:             Map<String, Any>,
    val attachments:              List<String>,
    val convertedCustomerId:      String,
    val convertedOrderId:         String,
    val convertedAt:              String,
    val stage:                    String,
    val updatedBy:                String,
    val organizationId:           String
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
