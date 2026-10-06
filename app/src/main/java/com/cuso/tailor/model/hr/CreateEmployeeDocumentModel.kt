package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

// ── Create API Response ──
data class CreateEmployeeDocumentResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CreatedEmployeeDocumentData? = null
)

data class CreatedEmployeeDocumentData(
    @SerializedName("_id") val id: String? = null,
    val organizationId: String? = null,
    val organizationMemberId: String? = null,
    val documentCategoryId: String? = null,
    val title: String? = null,
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val files: List<DocumentFileDto> = emptyList(),
    val downloadEligible: Boolean = true,
    val notes: String? = null,
    val status: String? = null,
    val createdBy: String? = null
)


// ── Generic Single Document Response (Used by view-one & update) ──
data class EmployeeDocumentSingleResponse(
    val success: Boolean,
    val message: String? = null,
    val data: EmployeeDocumentDto? = null
)

// ── Delete API Response (Handles raw unpopulated String IDs) ──
data class DeleteEmployeeDocumentResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: DeletedDocumentData? = null
)

data class DeletedDocumentData(
    @SerializedName("_id") val id: String? = null,
    val organizationId: String? = null,
    val organizationMemberId: String? = null, // String in delete response
    val documentCategoryId: String? = null,   // String in delete response
    val title: String? = null,
    val status: String? = null
)