@file:Suppress("UNUSED_PARAMETER", "UNUSED", "RedundantSuppression", "unused", "SpellCheckingInspection")

package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// Common Address Models
// ─────────────────────────────────────────────────────────────

data class GetCustomerAddress(
    val addressLine: String? = null,
    val city: String? = null,
    val area: String? = null,
    val pincode: String? = null
)

data class CustomerAddress1(
    val addressLine: String? = null,
    val city: String? = null,
    val area: String? = null,
    val pincode: String? = null
)

data class CustomerPreferences(
    val language: String? = null,
    val contactMethod: String? = null
)

// ─────────────────────────────────────────────────────────────
// Customer API Response Models (v1)
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
    val id: String,
    val organizationId: String? = null,
    val type: String? = null,
    val name: String,
    val email: String? = null,
    val mobile: String? = null,
    val gender: String? = null,
    @SerializedName("dob")
    val dateOfBirth: String? = null,
    val address: CustomerAddress1? = null,
    val preferences: CustomerPreferences? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
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

// ─────────────────────────────────────────────────────────────
// View & Update API Models (v1)
// ─────────────────────────────────────────────────────────────

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
    val preferences: CustomerPreferences? = null,
    val customFields: Map<String, @JvmSuppressWildcards Any>? = null,
    val referralCount: Int? = 0,
    val totalSpend: Int? = 0,
    val pendingPayment: Int? = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("__v")
    val v: Int? = null
)

data class CustomerViewAddress(
    val addressLine: String? = null,
    val city: String? = null,
    val area: String? = null,
    val pincode: String? = null
)

data class UpdateCustomerRequest(
    val type: String,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val status: String,
    val address: CustomerViewAddress,
    val preferences: CustomerPreferences? = null,
    val referralCount: Int = 0,
    val totalSpend: Int = 0,
    val pendingPayment: Int = 0,
    @SerializedName("_id")
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
    val success: Boolean,
    val message: String? = null
)

// ─────────────────────────────────────────────────────────────
// Customer List API Response Model (v2)
// ─────────────────────────────────────────────────────────────

data class CustomerListResponseV2(
    val success: Boolean,
    val pagination: PaginationInfo? = null,
    val data: List<CustomerItemV2> = emptyList()
)

data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

data class CustomerAddressV2(
    @SerializedName("addressLine") val addressLine: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("pincode") val pincode: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("street") val street: String? = null
)

data class CustomerItemV2(
    @SerializedName("_id")
    val _id: String = "",

    @SerializedName("organizationId")
    val organizationId: String? = null,

    @SerializedName("name")
    private val _name: String? = null,

    @SerializedName("customerName")
    private val _customerName: String? = null,

    @SerializedName("mobile")
    private val _mobile: String? = null,

    @SerializedName("mobileNumber")
    private val _mobileNumber: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("type")
    private val _type: String? = null,

    @SerializedName("customerType")
    private val _customerType: String? = null,

    @SerializedName("status")
    val status: String? = "Active",

    @SerializedName("outstanding")
    val outstanding: Double? = 0.0,

    @SerializedName("address")
    val address: CustomerAddressV2? = null,

    @SerializedName("billingAddress")
    val billingAddress: CustomerAddressV2? = null,

    @SerializedName("createdAt")
    val createdAt: String? = "",

    @SerializedName("updatedAt")
    val updatedAt: String? = ""
) {
    val name: String
        get() = _name?.ifBlank { null } ?: _customerName?.ifBlank { null } ?: "Walk-in Customer"

    val mobile: String
        get() = _mobile?.ifBlank { null } ?: _mobileNumber?.ifBlank { null } ?: "N/A"

    val type: String
        get() = _type?.ifBlank { null } ?: _customerType?.ifBlank { null } ?: "Individual"

    val displayAddress: String
        get() = address?.addressLine?.takeIf { it.isNotBlank() }
            ?: billingAddress?.addressLine?.takeIf { it.isNotBlank() }
            ?: address?.city?.takeIf { it.isNotBlank() }
            ?: billingAddress?.city?.takeIf { it.isNotBlank() }
            ?: "N/A"
}

// ─────────────────────────────────────────────────────────────
// Extension function to convert V2 to V1
// ─────────────────────────────────────────────────────────────

fun CustomerItemV2.toCustomerItem(): CustomerItem {
    return CustomerItem(
        id = this._id,
        organizationId = this.organizationId,
        type = this.type,
        name = this.name,
        email = this.email,
        mobile = this.mobile,
        gender = null,
        dateOfBirth = null,
        address = this.address?.let {
            CustomerAddress1(
                addressLine = it.addressLine ?: "",
                city = it.city ?: "",
                area = it.area ?: "",
                pincode = it.pincode ?: ""
            )
        },
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

// ─────────────────────────────────────────────────────────────
// Customer Detail API Response (v2)
// ─────────────────────────────────────────────────────────────

data class GetCustomerDetailResponseV2(
    val success: Boolean,
    val data: CustomerDetailV2
)

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
// Create / Update / Delete Customer (v2)
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
// Finance - Customer View One (GET /api/customers/{id})
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
    val area: String? = null,
    val pincode: String? = null
)