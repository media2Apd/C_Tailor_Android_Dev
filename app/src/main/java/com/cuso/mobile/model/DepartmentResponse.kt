// DepartmentModels.kt
package com.cuso.mobile.model

import com.google.gson.annotations.SerializedName

// ─── CreatedBy DTO ───
data class CreatedByDto(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val memberId: String
)

// ─── DepartmentItem ───
data class DepartmentItem(
    val _id: String,
    val name: String,
    val organizationId: String,
    val departmentHeadId: String?,
    val description: String?,
    val status: Boolean,
    val isDefault: Boolean,
    val isDeleted: Boolean,
    val deletedAt: String?,
    @SerializedName("createdBy")
    val createdByRaw: Any? = null,  // Accept both String and Object
    val createdAt: String,
    val updatedAt: String,
    val totalEmployees: Int
) {
    // Computed property to get CreatedByDto from raw data
    val createdBy: CreatedByDto?
        get() = when (createdByRaw) {
            is Map<*, *> -> {
                val map = createdByRaw as Map<*, *>
                CreatedByDto(
                    _id = map["_id"] as? String ?: "",
                    firstName = map["firstName"] as? String ?: "",
                    lastName = map["lastName"] as? String ?: "",
                    memberId = map["memberId"] as? String ?: ""
                )
            }
            is String -> {
                CreatedByDto(
                    _id = createdByRaw,
                    firstName = "",
                    lastName = "",
                    memberId = ""
                )
            }
            else -> null
        }
}

// ─── Department Response ───
data class DepartmentResponse(
    val success: Boolean,
    val data: List<DepartmentItem>
)

// ─── Department Create ───
data class DepartmentCreateRequest(
    val name: String,
    val description: String,
    val departmentHead: String
)

data class DepartmentCreateResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DepartmentItem? = null
)

// ─── Department Update ───
data class DepartmentUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val departmentHead: String? = null,
    val status: Boolean? = null
)

data class DepartmentUpdateResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DepartmentItem? = null
)

// ─── Department Delete ───
data class DepartmentDeleteResponse(
    val success: Boolean,
    val message: String? = null
)