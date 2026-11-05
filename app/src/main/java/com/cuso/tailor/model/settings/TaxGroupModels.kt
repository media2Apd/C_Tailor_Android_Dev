package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

// ── 1. Create Tax Rate Request (Matches real payload) ──
data class CreateTaxRateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("rate") val rate: Any, // Accepts numeric or string rate (e.g. 12 or "12")
    @SerializedName("taxType") val taxType: String = "GST",
    @SerializedName("effectiveFrom") val effectiveFrom: String? = null
)

// ── 2. Update Tax Rate Request (Matches real payload: only name is updated) ──
data class UpdateTaxRateRequest(
    @SerializedName("name") val name: String
)

// ── 3. Create Tax Group Request (Matches real payload) ──
data class CreateTaxGroupRequest(
    @SerializedName("name") val name: String,
    @SerializedName("taxRateIds") val taxRateIds: List<String>
)

// ── 4. Tax Rate Action Response ──
data class TaxRateActionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TaxRateItemDto? = null
)

// ── 5. Tax Group Action Response ──
data class TaxGroupActionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TaxGroupItemDto? = null
)

// ── 6. Created Tax Group Data Model ──
data class TaxGroupItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("taxRates") val taxRates: List<TaxRateItemDto> = emptyList(),
    @SerializedName("totalRate") val totalRate: Double = 0.0,
    @SerializedName("isCompound") val isCompound: Boolean = false,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("createdBy") val createdBy: TaxUserDto? = null,
    @SerializedName("updatedBy") val updatedBy: TaxUserDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)