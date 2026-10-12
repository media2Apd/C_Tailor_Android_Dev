package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// CREATE CUSTOMER REQUEST PAYLOAD
// ─────────────────────────────────────────────────────────────
data class CreateCustomerPayload(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("customerType") val customerType: String = "Individual",
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("preferredContactMethod") val preferredContactMethod: String? = "Whatsapp",
    @SerializedName("preferredLanguage") val preferredLanguage: String? = "English",
    @SerializedName("customerLevel") val customerLevel: String? = "Regular",
    @SerializedName("creditLimit") val creditLimit: Double = 0.0,
    @SerializedName("creditPeriodDays") val creditPeriodDays: Int = 0,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("taxId") val taxId: String? = null,
    @SerializedName("taxIdType") val taxIdType: String? = "GSTIN",
    @SerializedName("sameAsBillingAddress") val sameAsBillingAddress: Boolean = true,
    @SerializedName("billingAddress") val billingAddress: CustomerAddressPayload? = null,
    @SerializedName("shippingAddress") val shippingAddress: CustomerAddressPayload? = null,
    @SerializedName("customFields") val customFields: CustomerCustomFieldsPayload? = null
)

data class CustomerAddressPayload(
    @SerializedName("flatNo") val flatNo: String = "",
    @SerializedName("street") val street: String = "",
    @SerializedName("areaZone") val areaZone: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("subdivisionName") val subdivisionName: String = "",
    @SerializedName("countryName") val countryName: String = "India",
    @SerializedName("pincode") val pincode: String = ""
)

data class CustomerCustomFieldsPayload(
    @SerializedName("notes") val notes: String? = null
)

// ─────────────────────────────────────────────────────────────
// CREATE CUSTOMER API RESPONSE
// ─────────────────────────────────────────────────────────────
data class CreateCustomerApiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: CreatedCustomerData? = null
)

data class CreatedCustomerData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branch: BranchRefDto? = null,
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("mobileNumber") val mobileNumber: String = "",
    @SerializedName("email") val email: String? = null,
    @SerializedName("customerType") val customerType: String = "",
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("preferredLanguage") val preferredLanguage: String? = null,
    @SerializedName("preferredContactMethod") val preferredContactMethod: String? = null,
    @SerializedName("customerLevel") val customerLevel: String? = null,
    @SerializedName("creditLimit") val creditLimit: Double = 0.0,
    @SerializedName("creditPeriodDays") val creditPeriodDays: Int = 0,
    @SerializedName("billingAddress") val billingAddress: CustomerAddressPayload? = null,
    @SerializedName("sameAsBillingAddress") val sameAsBillingAddress: Boolean = true,
    @SerializedName("shippingAddress") val shippingAddress: CustomerAddressPayload? = null,
    @SerializedName("taxId") val taxId: String? = null,
    @SerializedName("taxIdType") val taxIdType: String? = null,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("customFields") val customFields: CustomerCustomFieldsPayload? = null,
    @SerializedName("createdBy") val createdBy: CreatedByRefDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class BranchRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

data class CreatedByRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = ""
)

data class CustomerFinancialProfileResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: CustomerFinancialProfileData? = null
)

data class CustomerFinancialProfileData(
    val customerHeader: CustomerHeaderData? = null,
    val taxAndBilling: CustomerTaxAndBillingData? = null,
    val creditManagement: CustomerCreditManagementData? = null,
    val financialTotals: CustomerFinancialTotalsData? = null
)

data class CustomerHeaderData(
    val _id: String = "",
    val customerCode: String? = null,
    val fullName: String? = null,
    val mobileNumber: String? = null,
    val email: String? = null,
    val customerType: String? = null,
    val customerLevel: String? = null,
    val status: String? = null,
    val branch: CustomerBranchData? = null,
    val createdAt: String? = null
)

data class CustomerBranchData(
    val _id: String = "",
    val name: String? = null,
    val code: String? = null
)

data class CustomerTaxAndBillingData(
    val taxId: String? = null,
    val taxIdType: String? = null,
    val billingAddress: CustomerAddressData? = null,
    val shippingAddress: CustomerAddressData? = null
)

data class CustomerAddressData(
    val flatNo: String? = null,
    val street: String? = null,
    val areaZone: String? = null,
    val city: String? = null,
    val subdivisionCode: String? = null,
    val subdivisionName: String? = null,
    val countryCode: String? = null,
    val countryName: String? = null,
    val pincode: String? = null
)

data class CustomerCreditManagementData(
    val creditLimit: Double? = 0.0,
    val creditPeriodDays: Int? = 0,
    val totalOutstandingReceivables: Double? = 0.0,
    val availableCredit: Double? = 0.0,
    val isCreditExceeded: Boolean? = false
)

data class CustomerFinancialTotalsData(
    val totalOrdersCount: Int? = 0,
    val totalBilledAmount: Double? = 0.0,
    val totalPaidAmount: Double? = 0.0,
    val totalOutstandingReceivables: Double? = 0.0
)