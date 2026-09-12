package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

// =============================================================================
// 1. MAIN RESPONSE WRAPPERS
// =============================================================================

data class SupplierListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<SupplierDto> = emptyList(),
    @SerializedName("pagination") val pagination: Any? = null
)

data class SupplierViewOneResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: SupplierDto? = null
)

data class SupplierActionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SupplierDto? = null
)

data class SupplierDropdownResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<SupplierDropdownItem> = emptyList()
)

data class SupplierDropdownItem(
    @SerializedName("value") val value: String = "",
    @SerializedName("label") val label: String = ""
)

data class SupplierLedgerResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: SupplierLedgerContainer? = null
)

// =============================================================================
// 2. SUPPLIER DTO & NESTED OBJECTS
// =============================================================================

data class SupplierDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("supplierCode") val supplierCode: String = "",
    @SerializedName("complianceStatus") val complianceStatus: String = "Pending",
    @SerializedName("type") val type: String = "Manufacturer",
    @SerializedName("category") val category: String = "Raw Materials",
    @SerializedName("status") val status: String = "active",
    @SerializedName("contact") val contact: SupplierContact? = null,
    @SerializedName("address") val address: SupplierAddressContainer? = null,
    @SerializedName("tax") val tax: SupplierTax? = null,
    @SerializedName("payment") val payment: SupplierPayment? = null,
    @SerializedName("banks") val banks: List<SupplierBank> = emptyList(),
    @SerializedName("documents") val documents: SupplierDocuments? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,

    // Additional fields in View One
    @SerializedName("financialSummary") val financialSummary: SupplierFinancialSummary? = null,
    @SerializedName("purchaseActivity") val purchaseActivity: SupplierPurchaseActivity? = null,
    @SerializedName("pendingItems") val pendingItems: List<SupplierPendingItem> = emptyList()
)

data class SupplierContact(
    @SerializedName("contactName") val contactName: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("alternatePhone") val alternatePhone: String? = null,
    @SerializedName("website") val website: String? = null
)

data class SupplierAddressContainer(
    @SerializedName("billing") val billing: SupplierAddressInfo? = null,
    @SerializedName("shipping") val shipping: SupplierAddressInfo? = null,
    @SerializedName("sameAsBilling") val sameAsBilling: Boolean = true
)

data class SupplierAddressInfo(
    @SerializedName("flatNo") val flatNo: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("address") val address: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("state") val state: String = "",
    @SerializedName("stateCode") val stateCode: String? = null,
    @SerializedName("country") val country: String = "India",
    @SerializedName("pincode") val pincode: String = ""
) {
    val fullAddressText: String
        get() = listOfNotNull(flatNo, street, address.takeIf { it.isNotBlank() }, city, state, pincode)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

data class SupplierTax(
    @SerializedName("gstNumber") val gstNumber: String = "",
    @SerializedName("gstType") val gstType: String = "Regular",
    @SerializedName("pan") val pan: String = "",
    @SerializedName("defaultPurchaseTax") val defaultPurchaseTax: String = "GST18",
    @SerializedName("reverseCharge") val reverseCharge: Boolean = false,
    @SerializedName("tdsApplicable") val tdsApplicable: Boolean = true
)

data class SupplierPayment(
    @SerializedName("paymentTerm") val paymentTerm: Int = 30,
    @SerializedName("creditLimit") val creditLimit: Double = 0.0,
    @SerializedName("openingBalance") val openingBalance: Double = 0.0,
    @SerializedName("preferredPaymentMethod") val preferredPaymentMethod: String = "Bank"
)

data class SupplierBank(
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("accountNumber") val accountNumber: String = "",
    @SerializedName("ifsc") val ifsc: String = "",
    @SerializedName("bankName") val bankName: String = "",
    @SerializedName("branch") val branch: String = "",
    @SerializedName("isPrimary") val isPrimary: Boolean = true
)

data class SupplierDocuments(
    @SerializedName("gstCertificate") val gstCertificate: DocumentItem? = null,
    @SerializedName("companyRegistration") val companyRegistration: DocumentItem? = null,
    @SerializedName("bankProof") val bankProof: DocumentItem? = null,
    @SerializedName("agreements") val agreements: DocumentItem? = null
)

data class DocumentItem(
    @SerializedName("fileUrl") val fileUrl: String = "",
    @SerializedName("publicId") val publicId: String = ""
)

data class SupplierFinancialSummary(
    @SerializedName("outstandingPayable") val outstandingPayable: Double = 0.0,
    @SerializedName("advanceGiven") val advanceGiven: Double = 0.0,
    @SerializedName("totalPurchaseValue") val totalPurchaseValue: Double = 0.0,
    @SerializedName("lastPaymentDate") val lastPaymentDate: String? = null
)

data class SupplierPurchaseActivity(
    @SerializedName("totalPO") val totalPO: Int = 0,
    @SerializedName("openPO") val openPO: Int = 0,
    @SerializedName("partial") val partial: Int = 0,
    @SerializedName("completed") val completed: Int = 0,
    @SerializedName("lastPO") val lastPO: SupplierLastPO? = null
)

data class SupplierLastPO(
    @SerializedName("poNumber") val poNumber: String = "",
    @SerializedName("poDate") val poDate: String = "",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0
)

data class SupplierPendingItem(
    @SerializedName("poNo") val poNo: String = "",
    @SerializedName("itemName") val itemName: String = "",
    @SerializedName("qtyPending") val qtyPending: Double = 0.0
)

// =============================================================================
// 3. LEDGER DTOs
// =============================================================================

data class SupplierLedgerContainer(
    @SerializedName("ledger") val ledger: List<SupplierLedgerEntry> = emptyList(),
    @SerializedName("lastTransaction") val lastTransaction: SupplierLedgerEntry? = null
)

data class SupplierLedgerEntry(
    @SerializedName("date") val date: String = "",
    @SerializedName("refNo") val refNo: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("debit") val debit: Double = 0.0,
    @SerializedName("credit") val credit: Double = 0.0,
    @SerializedName("balance") val balance: Double = 0.0
)

// =============================================================================
// 4. REQUEST MODELS
// =============================================================================

data class CreateSupplierRequest(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String = "Manufacturer",
    @SerializedName("category") val category: String = "Raw Materials",
    @SerializedName("contact") val contact: SupplierContact,
    @SerializedName("address") val address: SupplierAddressContainer,
    @SerializedName("tax") val tax: SupplierTax,
    @SerializedName("payment") val payment: SupplierPayment,
    @SerializedName("banks") val banks: List<SupplierBank> = emptyList(),
    @SerializedName("notes") val notes: String? = null
)