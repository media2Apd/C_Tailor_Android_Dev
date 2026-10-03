package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

/**
 * Component line item inside a salary template
 */
data class SalaryTemplateComponentItem(
    @SerializedName("salaryComponentId")
    val salaryComponentId: String? = null,
    @SerializedName("defaultAmount")
    val defaultAmount: Double? = null
)

/**
 * Salary Template Data Item
 */
data class SalaryTemplateDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("countryCode")
    val countryCode: String? = null,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("annualCTC")
    val annualCTC: Double? = 0.0,
    @SerializedName("components")
    val components: List<SalaryTemplateComponentItem> = emptyList(),
    @SerializedName("isActive")
    val isActive: Boolean = true,
    @SerializedName("createdBy")
    val createdBy: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("updatedBy")
    val updatedBy: String? = null
)

data class SalaryTemplatePagination(
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("limit")
    val limit: Int = 10,
    @SerializedName("totalPages")
    val totalPages: Int = 1,
    @SerializedName("hasNextPage")
    val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage")
    val hasPreviousPage: Boolean = false
)

data class SalaryTemplateListData(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("data")
    val data: List<SalaryTemplateDto> = emptyList(),
    @SerializedName("pagination")
    val pagination: SalaryTemplatePagination? = null
)

// Response for View All
data class SalaryTemplateListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: SalaryTemplateListData
)

// Response for View One
data class SalaryTemplateSingleResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: SalaryTemplateDto
)

// Request body for Create / Update
data class SaveSalaryTemplateRequest(
    val name: String,
    val description: String? = null,
    val annualCTC: Double? = null,
    val components: List<SalaryTemplateComponentItem> = emptyList(),
    val countryCode: String = "IN",
    val isActive: Boolean = true
)