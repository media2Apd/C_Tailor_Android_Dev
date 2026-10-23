@file:Suppress("unused", "UNUSED_PARAMETER","DEPRECATION", "AssignedValueIsNeverRead")

package com.cuso.tailor.model.hr

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// Response wrapper for View-All List
data class ExpenseClaimsApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ExpenseClaimsDataContainer?
)

data class ExpenseClaimsDataContainer(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ExpenseClaimDto> = emptyList(),
    @SerializedName("pagination") val pagination: ExpensePaginationDto?
)

// Response wrapper for View-One Detail & Create
data class ExpenseClaimSingleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ExpenseClaimDto?
)

// Main Expense Claim DTO matching both View-All and View-One
data class ExpenseClaimDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("claimNumber") val claimNumber: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("expenseDate") val expenseDate: String? = null,
    @SerializedName("category") val category: ExpenseClaimCategoryDto? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("taxAmount") val taxAmount: Double? = 0.0,
    @SerializedName("totalAmount") val totalAmount: Double = 0.0,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("attachments") val attachments: List<ExpenseClaimAttachmentDto> = emptyList(),
    @SerializedName("employee") val employee: ExpenseClaimEmployeeDto? = null,
    @SerializedName("status") val status: String? = "Pending_Approval",
    @SerializedName("submittedAt") val submittedAt: String? = null,
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("workflow") val workflow: List<ExpenseClaimWorkflowStageDto> = emptyList(),
    @SerializedName("currentStage") val currentStage: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val v: Int? = 0
)

data class ExpenseClaimCategoryDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null
)

data class ExpenseClaimEmployeeDto(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("memberId") val memberId: String? = null,
    @SerializedName("employeeCode") val employeeCode: String? = null
) {
    val fullName: String
        get() = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifBlank { "N/A" }
}

data class ExpenseClaimAttachmentDto(
    @SerializedName("_id") val id: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fileUrl") val fileUrl: String,
    @SerializedName("fileType") val fileType: String? = null,
    @SerializedName("fileSize") val fileSize: Long? = 0L,
    @SerializedName("uploadedAt") val uploadedAt: String? = null,
    @SerializedName("uploadedBy") val uploadedBy: String? = null
)

data class ExpenseClaimWorkflowStageDto(
    @SerializedName("stage") val stage: String,
    @SerializedName("status") val status: String
)
data class ExpenseClaimPaymentDetails(
    val mode: String,
    val referenceNo: String,
    val paymentDate: String,
    val notes: String
)

data class UpdateExpenseClaimStatusRequest(
    val status: String,
    val comment: String? = null,
    val payment: ExpenseClaimPaymentDetails? = null
)

data class UpdateExpenseClaimResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: JsonElement? = null
)

typealias UpdateExpenseClaimStatusResponse = UpdateExpenseClaimResponse

data class DeleteExpenseClaimResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Any? = null
)