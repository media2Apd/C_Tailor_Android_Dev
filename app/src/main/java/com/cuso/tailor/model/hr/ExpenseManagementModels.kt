@file:Suppress("unused", "AssignedValueIsNeverRead")
package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

//data class ExpenseClaimsApiResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: ExpenseClaimsDataContainer?
//)
//
//data class ExpenseClaimsDataContainer(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("data") val data: List<ExpenseClaimDto> = emptyList(),
//    @SerializedName("pagination") val pagination: ExpensePaginationDto?
//)
//
//data class ExpenseClaimDto(
//    @SerializedName("_id") val id: String,
//    @SerializedName("organizationId") val organizationId: String?,
//    @SerializedName("claimNumber") val claimNumber: String? = null,
//    @SerializedName("title") val title: String? = null,
//    @SerializedName("expenseDate") val expenseDate: String?,
//    @SerializedName("category") val category: ExpenseCategoryDto?,
//    @SerializedName("description") val description: String?,
//    @SerializedName("amount") val amount: Double,
//    @SerializedName("taxAmount") val taxAmount: Double?,
//    @SerializedName("totalAmount") val totalAmount: Double,
//    @SerializedName("currency") val currency: String = "INR",
//    @SerializedName("attachments") val attachments: List<ExpenseAttachmentDto> = emptyList(),
//    @SerializedName("employee") val employee: ExpenseEmployeeDto?,
//    @SerializedName("status") val status: String,
//    @SerializedName("submittedAt") val submittedAt: String?,
//    @SerializedName("createdBy") val createdBy: String?,
//    @SerializedName("updatedBy") val updatedBy: String?,
//    @SerializedName("isDeleted") val isDeleted: Boolean = false,
//    @SerializedName("workflow") val workflow: List<ExpenseWorkflowDto> = emptyList(),
//    @SerializedName("currentStage") val currentStage: String?,
//    @SerializedName("createdAt") val createdAt: String?,
//    @SerializedName("updatedAt") val updatedAt: String?
//)

data class ExpenseCategoryDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String
)

data class ExpenseEmployeeDto(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("memberId") val memberId: String?,
    @SerializedName("employeeCode") val employeeCode: String?
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class ExpenseAttachmentDto(
    @SerializedName("_id") val id: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fileUrl") val fileUrl: String,
    @SerializedName("fileType") val fileType: String?,
    @SerializedName("fileSize") val fileSize: Long?,
    @SerializedName("uploadedAt") val uploadedAt: String?,
    @SerializedName("uploadedBy") val uploadedBy: String?
)

data class ExpenseWorkflowDto(
    @SerializedName("stage") val stage: String,
    @SerializedName("status") val status: String
)

data class ExpensePaginationDto(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean
)