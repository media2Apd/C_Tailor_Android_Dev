package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

// ── Document Category Models ──
data class DocumentCategoryListResponse(
    val success: Boolean,
    val data: List<DocumentCategoryDto> = emptyList(),
    val pagination: DocumentPagination? = null
)

data class DocumentCategoryDto(
    @SerializedName("_id") val id: String,
    val categoryName: String,
    val description: String? = null,
    val validityRequired: Boolean = false,
    val autoExpiryAlerts: Boolean = false,
    val expiryAlertDays: Int? = null,
    val status: String? = null
)

// ── Employee Document Models ──
data class EmployeeDocumentListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: EmployeeDocumentDataContainer? = null
)

data class EmployeeDocumentDataContainer(
    val documents: List<EmployeeDocumentDto> = emptyList(),
    val pagination: DocumentPagination? = null
)

data class EmployeeDocumentDto(
    @SerializedName("_id") val id: String,
    val organizationId: String? = null,
    val organizationMemberId: DocumentMemberDto? = null,
    val documentCategoryId: DocumentCategoryDto? = null,
    val title: String = "",
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val files: List<DocumentFileDto> = emptyList(),
    val downloadEligible: Boolean = true,
    val notes: String? = null,
    val status: String = "Active"
)

data class DocumentMemberDto(
    @SerializedName("_id") val id: String,
    val firstName: String = "",
    val lastName: String = "",
    val employeeCode: String = "",
    val department: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class DocumentFileDto(
    val url: String = "",
    val publicId: String = "",
    val resourceType: String = ""
)

data class DocumentPagination(
    val total: Int = 0,
    val totalDocuments: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1
)