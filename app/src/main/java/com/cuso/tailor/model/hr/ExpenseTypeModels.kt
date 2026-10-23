package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

data class ExpenseTypeListApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ExpenseTypeContainerDto?
)

data class ExpenseTypeContainerDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ExpenseTypeDto> = emptyList(),
    @SerializedName("pagination") val pagination: ExpensePaginationDto?
)

data class ExpenseTypeDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("defaultLimit") val defaultLimit: Double = 0.0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("organizationId") val organizationId: ExpenseTypeOrgDto? = null,
    @SerializedName("createdBy") val createdBy: ExpenseTypeUserDto? = null,
    @SerializedName("updatedBy") val updatedBy: ExpenseTypeUserDto? = null
)

data class ExpenseTypeOrgDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null
)

data class ExpenseTypeUserDto(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("employeeCode") val employeeCode: String? = null
)

data class SaveExpenseTypeRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("defaultLimit") val defaultLimit: Double,
    @SerializedName("status") val status: String
)

data class ExpenseTypeActionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ExpenseTypeActionDataDto?
)

data class ExpenseTypeActionDataDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("defaultLimit") val defaultLimit: Double = 0.0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)