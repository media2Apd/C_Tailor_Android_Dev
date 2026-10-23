package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

data class ActiveExpenseTypeListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<ActiveExpenseTypeDto> = emptyList()
)

data class ActiveExpenseTypeDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("defaultLimit") val defaultLimit: Double = 0.0,
    @SerializedName("status") val status: String,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("updatedBy") val updatedBy: String?,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("__v") val v: Int? = 0
)