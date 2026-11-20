@file:Suppress("unused")
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

data class DocumentPagination(
    val total: Int = 0,
    val totalDocuments: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1
)


data class EmployeeDocumentSingleResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: EmployeeDocumentDto? = null
)

data class EmployeeDocumentDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("organizationMemberId") val organizationMemberId: EmployeeDocumentMemberDto? = null,
    @SerializedName("documentCategoryId") val documentCategoryId: EmployeeDocumentCategoryRefDto? = null,
    @SerializedName("title") val title: String = "",
    @SerializedName("issueDate") val issueDate: String? = null,
    @SerializedName("expiryDate") val expiryDate: String? = null,
    @SerializedName("files") val files: List<DocumentFileDto> = emptyList(),
    @SerializedName("downloadEligible") val downloadEligible: Boolean = true,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("status") val status: String = "",
    @SerializedName("createdBy") val createdBy: EmployeeDocumentMemberDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class EmployeeDocumentMemberDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("memberId") val memberId: String = "",
    @SerializedName("employeeCode") val employeeCodeField: String = "",
    @SerializedName("department") val department: String? = null
) {
    val fullName: String
        get() = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")

    val employeeCode: String
        get() = employeeCodeField.ifBlank { memberId }
}

data class EmployeeDocumentCategoryRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("categoryName") val categoryName: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("validityRequired") val validityRequired: Boolean = false,
    @SerializedName("autoExpiryAlerts") val autoExpiryAlerts: Boolean = false,
    @SerializedName("expiryAlertDays") val expiryAlertDays: Int = 0
)

data class DocumentFileDto(
    @SerializedName("url") val url: String = "",
    @SerializedName("publicId") val publicId: String = "",
    @SerializedName("resourceType") val resourceType: String = ""
)