package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

// ── Item Model ──
data class SalaryComponentItem(
    @SerializedName("_id")
    val id: String,
    val organizationId: String? = null,
    val countryCode: String? = "IN",
    val region: String? = null,
    val name: String,
    val nameInPayslip: String? = null,
    val type: String, // "earning" or "deduction"
    val category: String? = null, // "statutory", "custom", "voluntary"
    val calculationType: String? = null, // "fixed", "percentage_of_basic", etc.
    val value: Double? = 0.0,
    val formula: String? = null,
    val slabs: List<Any>? = emptyList(),
    val wageCeiling: Double? = null,
    val eligibilityWaitingPeriodMonths: Int? = 0,
    val isTaxable: Boolean = false,
    val isProRata: Boolean = false,
    val isFlexibleBenefit: Boolean = false,
    val maxFlexibleAmount: Double? = null,
    val isSystemDefined: Boolean = false,
    val isEditable: Boolean = true,
    val isActive: Boolean = true,
    val displayOrder: Int = 0,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ── Pagination ──
data class SalaryComponentPagination(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

data class SalaryComponentListData(
    val success: Boolean,
    val data: List<SalaryComponentItem> = emptyList(),
    val pagination: SalaryComponentPagination? = null
)

// ── API Responses ──
data class SalaryComponentListResponse(
    val success: Boolean,
    val message: String,
    val data: SalaryComponentListData
)

data class SalaryComponentSingleResponse(
    val success: Boolean,
    val message: String,
    val data: SalaryComponentItem?
)

// ── Create & Update Request Payload ──
data class SalaryComponentRequest(
    val name: String,
    val nameInPayslip: String,
    val type: String,
    val category: String,
    val calculationType: String,
    val value: Double,
    val formula: String? = null,
    val slabs: List<Any> = emptyList(),
    val wageCeiling: Double? = null,
    val eligibilityWaitingPeriodMonths: Int = 0,
    val isTaxable: Boolean = false,
    val isProRata: Boolean = false,
    val isFlexibleBenefit: Boolean = false,
    val maxFlexibleAmount: Double? = null,
    val countryCode: String = "IN",
    val region: String = "Tamil Nadu",
    val displayOrder: Int = 1,
    val isActive: Boolean = true
)

// ── Toggle Active/Deactivate Status Payload ──
data class ToggleSalaryComponentStatusRequest(
    val isActive: Boolean
)