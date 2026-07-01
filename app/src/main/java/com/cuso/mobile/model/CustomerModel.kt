package com.cuso.mobile.model

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