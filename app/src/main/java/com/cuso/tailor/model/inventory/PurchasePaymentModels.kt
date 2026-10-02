package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

// =============================================================================
// DOCUMENT TEMPLATE MODELS
// =============================================================================

data class DocumentTemplateListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<DocumentTemplateDto> = emptyList()
)

data class DocumentTemplateDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("templateKey") val templateKey: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("applicableTo") val applicableTo: List<String> = emptyList(),
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("designConfig") val designConfig: DocumentTemplateDesignConfig? = null,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class DocumentTemplateDesignConfig(
    @SerializedName("primaryColor") val primaryColor: String? = null,
    @SerializedName("accentColor") val accentColor: String? = null,
    @SerializedName("fontFamily") val fontFamily: String? = null,
    @SerializedName("showCompanyLogo") val showCompanyLogo: Boolean = true,
    @SerializedName("showSignature") val showSignature: Boolean = true
)

// =============================================================================
// PURCHASE PAYMENT VIEW ONE & VIEW ALL MODELS
// =============================================================================

data class PurchasePaymentViewOneResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PurchasePaymentRecord? = null
)

data class PurchasePaymentViewAllResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaymentPagination? = null,
    @SerializedName("data") val data: List<PurchasePaymentRecord> = emptyList()
)

data class PaymentPagination(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 1
)

data class PurchasePaymentRecord(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("supplierId") val supplierId: PaymentSupplierDto? = null,
    @SerializedName("billId") val billId: PaymentBillDto? = null,
    @SerializedName("paymentNumber") val paymentNumber: String = "",
    @SerializedName("paymentDate") val paymentDate: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("paymentMode") val paymentMode: String = "",
    @SerializedName("referenceNumber") val referenceNumber: String? = null,
    @SerializedName("paidFromAccountId") val paidFromAccountId: PaymentAccountDto? = null,
    @SerializedName("status") val status: String = "",
    @SerializedName("journalEntryId") val journalEntryId: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("templateTheme") val templateTheme: String? = null,
    @SerializedName("createdBy") val createdBy: PaymentUserDto? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class PaymentSupplierDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String = "",
    @SerializedName("contact") val contact: PaymentSupplierContact? = null,
    @SerializedName("billingAddress") val billingAddress: PaymentSupplierAddress? = null,
    @SerializedName("shippingAddress") val shippingAddress: PaymentSupplierAddress? = null,
    @SerializedName("sameAsBillingAddress") val sameAsBillingAddress: Boolean = false
)

data class PaymentSupplierContact(
    @SerializedName("contactName") val contactName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("alternatePhone") val alternatePhone: String? = null,
    @SerializedName("website") val website: String? = null
)

data class PaymentSupplierAddress(
    @SerializedName("flatNo") val flatNo: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("areaZone") val areaZone: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("subdivisionCode") val subdivisionCode: String? = null,
    @SerializedName("subdivisionName") val subdivisionName: String? = null,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("pincode") val pincode: String? = null
) {
    fun formatAddress(): String {
        return listOfNotNull(flatNo, street, areaZone, city, subdivisionName, pincode, countryName)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}

data class PaymentBillDto(
    @SerializedName("_id") val id: String,
    @SerializedName("billNumber") val billNumber: String = "",
    @SerializedName("billDate") val billDate: String = "",
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("amountPaid") val amountPaid: Double = 0.0,
    @SerializedName("balanceDue") val balanceDue: Double = 0.0,
    @SerializedName("status") val status: String = ""
)

data class PaymentAccountDto(
    @SerializedName("_id") val id: String,
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("accountCode") val accountCode: String = ""
)

data class PaymentUserDto(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("memberId") val memberId: String? = null
) {
    fun getFullName(): String {
        return listOfNotNull(firstName, lastName).joinToString(" ").trim()
    }
}

/**
 * Request payload for voiding a payment.
 */
data class VoidPaymentRequest(
    @SerializedName("reason")
    val reason: String
)

/**
 * Response payload received after voiding a payment.
 */
data class VoidPaymentResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: VoidPaymentData?
)

data class VoidPaymentData(
    @SerializedName("_id")
    val id: String,
    @SerializedName("organizationId")
    val organizationId: String?,
    @SerializedName("branchId")
    val branchId: String?,
    @SerializedName("supplierId")
    val supplierId: String?,
    @SerializedName("billId")
    val billId: String?,
    @SerializedName("paymentNumber")
    val paymentNumber: String,
    @SerializedName("paymentDate")
    val paymentDate: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("paymentMode")
    val paymentMode: String?,
    @SerializedName("referenceNumber")
    val referenceNumber: String?,
    @SerializedName("paidFromAccountId")
    val paidFromAccountId: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("journalEntryId")
    val journalEntryId: String?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("templateTheme")
    val templateTheme: String?,
    @SerializedName("createdBy")
    val createdBy: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("updatedBy")
    val updatedBy: String?
)