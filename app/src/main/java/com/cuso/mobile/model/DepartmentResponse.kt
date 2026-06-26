// com/cuso/mobile/model/DepartmentModels.kt
package com.cuso.mobile.model

data class CreatedByDto(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val memberId: String
)

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
    val createdBy: CreatedByDto?,
    val createdAt: String,
    val updatedAt: String,
    val totalEmployees: Int
)

data class DepartmentResponse(
    val success: Boolean,
    val data: List<DepartmentItem>
)