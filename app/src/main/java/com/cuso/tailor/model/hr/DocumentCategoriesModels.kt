package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

// ── Document Category DTO ──
data class DocumentCategoryDtoCat(
    @SerializedName("_id") val id: String = "",
    val organizationId: String? = null,
    val categoryName: String = "",
    val description: String? = null,
    val validityRequired: Boolean = false,
    val autoExpiryAlerts: Boolean = false,
    val expiryAlertDays: Int? = 30,
    val status: String = "Active",
    val createdBy: Any? = null,
    val updatedBy: Any? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ── List Response ──
data class DocumentCategoryListResponseCat(
    val success: Boolean,
    val data: List<DocumentCategoryDtoCat> = emptyList(),
    val pagination: DocumentPagination? = null
)

// ── Single Response (View One, Create, Update) ──
data class DocumentCategorySingleResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DocumentCategoryDtoCat? = null
)

// ── Request Body for Create / Update ──
data class SaveDocumentCategoryRequest(
    val categoryName: String,
    val description: String?,
    val validityRequired: Boolean,
    val autoExpiryAlerts: Boolean,
    val expiryAlertDays: Int = 30,
    val status: String = "Active"
)

// ── Delete Document Category Response ──
data class DeleteDocumentCategoryResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: DeletedDocumentCategoryData? = null
)

data class DeletedDocumentCategoryData(
    @SerializedName("_id") val id: String? = null,
    val organizationId: String? = null,
    val categoryName: String? = null,
    val description: String? = null,
    val status: String? = null
)