package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName

// Response wrapper for GET /api/finance/settings/payment-terms/dropdown
data class PaymentTermsDropdownResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<PaymentTermDropdownDto> = emptyList()
)

// Individual payment term item returned by the dropdown API
data class PaymentTermDropdownDto(
    @SerializedName("_id")
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val termType: String = "",
    val dueDays: Int = 0,
    val eomBufferDays: Int = 0,
    val isDefault: Boolean = false
)