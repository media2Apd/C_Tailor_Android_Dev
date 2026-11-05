package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

// ── Main View-All API Response ──
data class TaxRateListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<TaxRateItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: TaxRatePaginationDto? = null
)

// ── Tax Rate Item DTO ──
data class TaxRateItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("subdivisionCode") val subdivisionCode: String? = null,
    @SerializedName("applicableFor") val applicableFor: String? = "Both",
    @SerializedName("jurisdictionLevel") val jurisdictionLevel: String? = null,
    @SerializedName("jurisdictionName") val jurisdictionName: String? = null,
    @SerializedName("salesTaxAccountId") val salesTaxAccountId: TaxAccountRefDto? = null,
    @SerializedName("purchaseTaxAccountId") val purchaseTaxAccountId: TaxAccountRefDto? = null,
    @SerializedName("taxType") val taxType: String? = "GST",
    @SerializedName("taxTag") val taxTag: String? = null,
    @SerializedName("effectiveTo") val effectiveTo: String? = null,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("approvalStatus") val approvalStatus: String? = null,
    @SerializedName("isSystemDefined") val isSystemDefined: Boolean = false,
    @SerializedName("effectiveFrom") val effectiveFrom: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// ── Linked Financial Ledger Account Reference ──
data class TaxAccountRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("accountCode") val accountCode: String = "",
    @SerializedName("accountType") val accountType: String = ""
)

// ── Pagination Metadata DTO ──
data class TaxRatePaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)