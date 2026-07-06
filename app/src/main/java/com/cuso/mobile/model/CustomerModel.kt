package com.cuso.mobile.model

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// Model — matches the API response you shared
// ─────────────────────────────────────────────────────────────

data class GetCustomerAddress(
    val addressLine: String? = null,
    val city: String? = null,
    val pincode: String? = null
)



// ─────────────────────────────────────────────────────────────
// Customer API Response Models
// ─────────────────────────────────────────────────────────────

data class CustomerListResponse(
    val success: Boolean,
    val data: List<CustomerItem>,
    val total: Int,
    val totalPages: Int,
    val page: Int,
    val limit: Int
)
data class CustomerItem(
    @SerializedName("_id")
    val id: String,                 // _id
    val organizationId: String? = null,
    val type: String? = null,       // "individual" / "business"
    val name: String,
    val email: String? = null,
    val mobile: String? = null,
    val gender: String? = null,
    val dateOfBirth: Long? = null,
    val address: CustomerAddress? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    // "Location" column -> derive from address (city, or fallback to addressLine)
    val location: String
        get() = address?.city?.takeIf { it.isNotBlank() }
            ?: address?.addressLine?.takeIf { it.isNotBlank() }
            ?: "—"
}


//view and update
// ── View API (GET) ──────────────────────────────
data class GetCustomerViewResponse(
    val success: Boolean,
    val data: CustomerViewData
)

data class CustomerViewData(
    val _id: String,
    val organizationId: String,
    val type: String,
    val name: String,
    val mobile: String,
    val status: String,
    val address: CustomerViewAddress? = null,
    val customFields: Map<String, @JvmSuppressWildcards Any>? = null,
    val referralCount: Int? = 0,
    val totalSpend: Int? = 0,
    val pendingPayment: Int? = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
)

data class CustomerViewAddress(
    val addressLine: String? = null,
    val city: String? = null,
    val pincode: String? = null
)

// ── Update API (PUT) ────────────────────────────
data class UpdateCustomerRequest(
    val type: String,
    val name: String,
    val mobile: String,
    val status: String,
    val address: CustomerViewAddress,
    val referralCount: Int = 0,
    val totalSpend: Int = 0,
    val pendingPayment: Int = 0,
    val _id: String,
    val organizationId: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
)

data class UpdateCustomerResponse(
    val success: Boolean,
    val data: CustomerViewData
)

data class DeleteCustomerResponse(
    val success: Boolean
)