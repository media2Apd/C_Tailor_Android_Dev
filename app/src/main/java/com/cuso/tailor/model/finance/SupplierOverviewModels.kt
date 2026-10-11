package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName

data class SupplierOverviewApiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: SupplierOverviewData? = null
)

data class SupplierOverviewData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("supplierCode") val supplierCode: String = "",
    @SerializedName("complianceStatus") val complianceStatus: String = "Pending",
    @SerializedName("type") val type: String = "Corporate",
    @SerializedName("status") val status: String = "active",
    @SerializedName("contact") val contact: SupplierContactDto? = null,
    @SerializedName("tax") val tax: SupplierTaxDto? = null,
    @SerializedName("payment") val payment: SupplierPaymentDto? = null,
    @SerializedName("billingAddress") val billingAddress: SupplierAddressDto? = null,
    @SerializedName("shippingAddress") val shippingAddress: SupplierAddressDto? = null,
    @SerializedName("sameAsBillingAddress") val sameAsBillingAddress: Boolean = true,
    @SerializedName("financialSummary") val financialSummary: SupplierFinancialSummaryDto? = null,
    @SerializedName("purchaseActivity") val purchaseActivity: SupplierPurchaseActivityDto? = null,
    @SerializedName("documents") val documents: SupplierDocumentsDto? = null, // <-- ADD THIS
    @SerializedName("isMSME") val isMSME: Boolean = false,
    @SerializedName("notes") val notes: String = "",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// <-- ADD THESE TWO DATA CLASSES
data class SupplierDocumentsDto(
    @SerializedName("gstCertificate") val gstCertificate: DocumentItemDto? = null,
    @SerializedName("companyRegistration") val companyRegistration: DocumentItemDto? = null,
    @SerializedName("bankProof") val bankProof: DocumentItemDto? = null,
    @SerializedName("agreements") val agreements: DocumentItemDto? = null
)

data class DocumentItemDto(
    @SerializedName("fileUrl") val fileUrl: String = "",
    @SerializedName("publicId") val publicId: String = ""
)

data class SupplierContactDto(
    @SerializedName("contactName") val contactName: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("website") val website: String = ""
)

data class SupplierTaxDto(
    @SerializedName("gstNumber") val gstNumber: String = "",
    @SerializedName("pan") val pan: String = "",
    @SerializedName("reverseCharge") val reverseCharge: Boolean = false,
    @SerializedName("tdsApplicable") val tdsApplicable: Boolean = false
)

data class SupplierPaymentDto(
    @SerializedName("paymentTerm") val paymentTerm: Int = 0,
    @SerializedName("creditLimit") val creditLimit: Double = 0.0,
    @SerializedName("openingBalance") val openingBalance: Double = 0.0,
    @SerializedName("preferredPaymentMethod") val preferredPaymentMethod: String = "Bank Transfer",
    @SerializedName("advanceBalance") val advanceBalance: Double = 0.0
)

data class SupplierAddressDto(
    @SerializedName("flatNo") val flatNo: String = "",
    @SerializedName("street") val street: String = "",
    @SerializedName("areaZone") val areaZone: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("subdivisionCode") val subdivisionCode: String = "",
    @SerializedName("subdivisionName") val subdivisionName: String = "",
    @SerializedName("countryCode") val countryCode: String = "",
    @SerializedName("countryName") val countryName: String = "",
    @SerializedName("pincode") val pincode: String = ""
) {
    val displayAddress: String
        get() = listOfNotNull(
            street.takeIf { it.isNotBlank() },
            city.takeIf { it.isNotBlank() },
            subdivisionName.takeIf { it.isNotBlank() },
            pincode.takeIf { it.isNotBlank() }
        ).joinToString(", ").ifBlank { "N/A" }
}

data class SupplierFinancialSummaryDto(
    @SerializedName("outstandingPayable") val outstandingPayable: Double = 0.0,
    @SerializedName("advanceGiven") val advanceGiven: Double = 0.0,
    @SerializedName("totalPurchaseValue") val totalPurchaseValue: Double = 0.0,
    @SerializedName("lastPaymentDate") val lastPaymentDate: String? = null
)

data class SupplierPurchaseActivityDto(
    @SerializedName("totalPO") val totalPO: Int = 0,
    @SerializedName("openPO") val openPO: Int = 0,
    @SerializedName("partial") val partial: Int = 0,
    @SerializedName("completed") val completed: Int = 0,
    @SerializedName("lastPO") val lastPO: SupplierLastPoDto? = null
)

data class SupplierLastPoDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("poNumber") val poNumber: String = ""
)
