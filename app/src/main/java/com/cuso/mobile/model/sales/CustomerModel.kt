@file:Suppress("UNUSED_PARAMETER", "UNUSED", "RedundantSuppression", "unused")

package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// Model — matches the API response you shared
// ─────────────────────────────────────────────────────────────

data class GetCustomerAddress(
    val addressLine: String? = null,
    val city: String? = null,
    val area: String? = null,        //   ADDED — payload has "area"
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
    val type: String? = null,       // "individual" / "business" / "regular"
    val name: String,
    val email: String? = null,
    val mobile: String? = null,
    val gender: String? = null,
    @SerializedName("dob")
    val dateOfBirth: String? = null,
    val address: CustomerAddress? = null,
    val preferences: CustomerPreferences? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    // "Location" column -> derive from address (city, then area, then addressLine)
    val location: String
        get() = address?.city?.takeIf { it.isNotBlank() }
            ?: address?.area?.takeIf { it.isNotBlank() }
            ?: address?.addressLine?.takeIf { it.isNotBlank() }
            ?: "—"

    val displayType: String
        get() = when (type?.lowercase()) {
            "business" -> "Business"
            "individual" -> "Individual"
            "regular" -> "Regular"
            else -> "—"
        }
}

data class CustomerPreferences(
    val language: String? = null,
    val contactMethod: String? = null
)


//view and update
// ── View API (GET) ──────────────────────────────
data class GetCustomerViewResponse(
    val success: Boolean,
    val data: CustomerViewData
)

data class CustomerViewData(
    @SerializedName("_id")
    val id: String,
    val organizationId: String,
    val type: String,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val status: String,
    val address: CustomerViewAddress? = null,
    val preferences: CustomerPreferences? = null,   //   ADDED — was missing, payload has this
    val customFields: Map<String, @JvmSuppressWildcards Any>? = null,
    val referralCount: Int? = 0,
    val totalSpend: Int? = 0,
    val pendingPayment: Int? = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("__v")                          //   FIXED — was missing, "v" never matched "__v"
    val v: Int? = null
)

data class CustomerViewAddress(
    val addressLine: String? = null,
    val city: String? = null,
    val area: String? = null,        //   ADDED — payload has "area"
    val pincode: String? = null
)

// ── Update API (PUT) ────────────────────────────
data class UpdateCustomerRequest(
    val type: String,
    val name: String,
    val mobile: String,
    val email: String? = null,          //   ADD
    val gender: String? = null,         //   ADD
    val dob: String? = null,            //   ADD
    val status: String,
    val address: CustomerViewAddress,
    val preferences: CustomerPreferences? = null,
    val referralCount: Int = 0,
    val totalSpend: Int = 0,
    val pendingPayment: Int = 0,
    @SerializedName("_id")              //   ADD — backend expects "_id" not "id"
    val id: String,
    val organizationId: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("__v")
    val v: Int? = null
)
data class UpdateCustomerResponse(
    val success: Boolean,
    val data: CustomerViewData
)

data class DeleteCustomerResponse(
    val success: Boolean
)

// ─────────────────────────────────────────────────────────────
// NEW Customer List API Response Model (v2)
// ─────────────────────────────────────────────────────────────

data class CustomerListResponseV2(
    val success: Boolean,
    val pagination: PaginationInfo? = null,
    val data: List<CustomerItemV2>
)

data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

data class CustomerItemV2(
    @SerializedName("_id")
    val _id: String,
    val organizationId: String,
    val type: String,                    // "individual" or "business"
    val name: String,
    val mobile: String,
    val address: CustomerAddressV2? = null,
    val status: String,                  // "Active", "Inactive", etc.
    val customFields: Map<String, Any>? = null,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v")
    val version: Int? = null,
    val creditLimit: Int? = 0,
    val outstanding: Int? = 0,
    val lastInvoice: String? = null,
    val totalPaid: Int? = 0
) {
    val location: String
        get() = address?.city?.takeIf { it.isNotBlank() }
            ?: address?.area?.takeIf { it.isNotBlank() }     //   ADDED for consistency
            ?: address?.addressLine?.takeIf { it.isNotBlank() }
            ?: "—"

    val displayName: String
        get() = name.ifEmpty { "—" }

    val displayMobile: String
        get() = mobile.ifEmpty { "—" }

    val displayStatus: String
        get() = status.ifEmpty { "Active" }

    val displayType: String
        get() = type.replaceFirstChar { it.uppercase() }
}

data class CustomerAddressV2(
    val addressLine: String? = null,
    val city: String? = null,
    val area: String? = null,        //   ADDED — payload has "area"
    val pincode: String? = null
)

// ─────────────────────────────────────────────────────────────
// Extension function to convert V2 to V1 (if needed)
// ─────────────────────────────────────────────────────────────

fun CustomerItemV2.toCustomerItem(): CustomerItem {
    return CustomerItem(
        id = this._id,
        organizationId = this.organizationId,
        type = this.type,
        name = this.name,
        email = null,
        mobile = this.mobile,
        gender = null,
        dateOfBirth = null,
        address = this.address?.let {
            CustomerAddress(
                addressLine = it.addressLine ?: "",   //   FIXED — was hardcoded to "", now maps actual value
                city = it.city ?: "",
                area = it.area ?: "",                  //   ADDED
                pincode = it.pincode ?: ""
            )
        },
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

data class GetCustomerDetailResponseV2(
    val success: Boolean,
    val data: CustomerDetailV2
)

// ─────────────────────────────────────────────────────────────
// Customer Detail API Response (for View/Edit)
// ─────────────────────────────────────────────────────────────

data class CustomerDetailResponseV2(
    val success: Boolean,
    val data: CustomerDetailV2
)

data class CustomerDetailV2(
    @SerializedName("_id")
    val _id: String,
    val organizationId: String,
    val type: String,
    val name: String,
    val mobile: String,
    val address: CustomerAddressV2? = null,
    val status: String,
    val customFields: Map<String, Any>? = null,
    val referralCount: Int? = 0,
    val totalSpend: Int? = 0,
    val pendingPayment: Int? = 0,
    val creditLimit: Int? = 0,
    val outstanding: Int? = 0,
    val lastInvoice: String? = null,
    val totalPaid: Int? = 0,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v")
    val version: Int? = null
)

// ─────────────────────────────────────────────────────────────
// Create Customer Request/Response
// ─────────────────────────────────────────────────────────────

data class CreateCustomerRequestV2(
    val type: String,
    val name: String,
    val mobile: String,
    val address: CustomerAddressV2,
    val status: String = "Active"
)

data class CreateCustomerResponseV2(
    val success: Boolean,
    val data: CustomerDetailV2
)

// ─────────────────────────────────────────────────────────────
// Update Customer Request/Response
// ─────────────────────────────────────────────────────────────

data class UpdateCustomerRequestV2(
    val type: String,
    val name: String,
    val mobile: String,
    val address: CustomerAddressV2,
    val status: String
)

data class UpdateCustomerResponseV2(
    val success: Boolean,
    val data: CustomerDetailV2
)

// ─────────────────────────────────────────────────────────────
// Delete Customer Response
// ─────────────────────────────────────────────────────────────

data class DeleteCustomerResponseV2(
    val success: Boolean,
    val message: String? = null
)

// ─────────────────────────────────────────────────────────────
// Customer State for UI
// ─────────────────────────────────────────────────────────────

sealed class CustomerUiStateV2 {
    object Loading : CustomerUiStateV2()
    data class Success(val customers: List<CustomerItemV2>, val pagination: PaginationInfo?) : CustomerUiStateV2()
    data class Error(val message: String) : CustomerUiStateV2()
}

sealed class CustomerDetailUiStateV2 {
    object Loading : CustomerDetailUiStateV2()
    data class Success(val customer: CustomerDetailV2) : CustomerDetailUiStateV2()
    data class Error(val message: String) : CustomerDetailUiStateV2()
}

sealed class CustomerDeleteStateV2 {
    object Idle : CustomerDeleteStateV2()
    object Loading : CustomerDeleteStateV2()
    object Success : CustomerDeleteStateV2()
    data class Error(val message: String) : CustomerDeleteStateV2()
}

// ─────────────────────────────────────────────────────────────
// Finance - Customer View One (GET /api/customers/{id} - Finance specific)
// ─────────────────────────────────────────────────────────────

data class GetFinanceCustomerViewOneResponse(
    val success: Boolean,
    val data: FinanceCustomerViewOneData
)

data class FinanceCustomerViewOneData(
    val customerInformation: FinanceCustomerInfo,
    val financialSummary: FinanceFinancialSummary,
    val billingAddress: FinanceAddress,
    val shippingAddress: FinanceAddress
)

data class FinanceCustomerInfo(
    val name: String,
    val phone: String,
    val type: String,
    val createdAt: String
)

data class FinanceFinancialSummary(
    val outstandingReceivables: Double,
    val unusedCredits: Double
)

data class FinanceAddress(
    val addressLine: String? = null,
    val city: String? = null,
    val area: String? = null,        //   ADDED — payload has "area"
    val pincode: String? = null
)