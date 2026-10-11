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