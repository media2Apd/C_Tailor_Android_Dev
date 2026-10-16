package com.cuso.tailor.model.finance

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// Wrapper for View-All API
data class CreditNoteListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<CreditNoteItemDto> = emptyList(),
    val pagination: CreditNotePagination? = null
)

// Wrapper for View-One API
data class CreditNoteSingleResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: CreditNoteItemDto? = null
)

data class CreditNotePagination(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 15,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

// Main Credit Note DTO (used in both View-All and View-One)
data class CreditNoteItemDto(
    @SerializedName("_id")
    val id: String = "",
    val organizationId: String? = null,
    val branchId: CreditNoteBranch? = null,
    val warehouseId: String? = null,
    val customerId: CreditNoteCustomer? = null,
    val customerSnapshot: CreditNoteCustomerSnapshot? = null,
    val companySnapshot: CreditNoteCompanySnapshot? = null,
    val invoiceId: String? = null,
    val creditNoteNumber: String = "",
    val creditNoteDate: String = "",
    val currency: String = "INR",
    val exchangeRate: Double = 1.0,
    val priceIncludesTax: Boolean = false,
    val placeOfSupply: String? = null,
    val taxType: String? = null,
    val lines: List<CreditNoteLineDto> = emptyList(),
    val subtotal: Double = 0.0,
    val totalTax: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val amountAllocated: Double = 0.0,
    val amountRefunded: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val status: String = "",
    val approvalStatus: String? = null,
    val journalEntryId: String? = null,
    val notes: String? = null,
    val createdBy: CreditNoteUserSnapshot? = null,
    val updatedBy: CreditNoteUserSnapshot? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CreditNoteBranch(
    @SerializedName("_id") val id: String = "",
    val name: String = ""
)

data class CreditNoteCustomer(
    @SerializedName("_id") val id: String = "",
    val customerCode: String? = null,
    val fullName: String? = null,
    val mobileNumber: String? = null,
    val email: String? = null,
    val billingAddress: BillingAddressDto? = null
)

data class CreditNoteCustomerSnapshot(
    val customerCode: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val taxRegistrationNumber: String? = null,
    val billingAddress: BillingAddressDto? = null
)

data class BillingAddressDto(
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

data class CreditNoteCompanySnapshot(
    val name: String? = null,
    val address: String? = null,
    val countryCode: String? = null,
    val subdivisionCode: String? = null,
    val taxRegistrationNumber: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val logoUrl: String? = null
)

data class CreditNoteLineDto(
    @SerializedName("_id") val id: String = "",
    val lineType: String? = "Product",
    val itemId: LineItemSnapshot? = null,
    val itemName: String = "",
    val itemDescription: String? = null,
    val unit: String? = "Pieces",
    val quantity: Double = 1.0,
    val rate: Double = 0.0,
    val discountPercent: Double = 0.0,
    val taxableAmount: Double = 0.0,
    val totalTax: Double = 0.0,
    val lineTotal: Double = 0.0,
    val reversalAccountId: AccountSnapshot? = null,
    val taxGroupId: TaxGroupSnapshot? = null,
    val taxBreakdown: List<TaxBreakdownDto> = emptyList()
)

data class LineItemSnapshot(
    @SerializedName("_id") val id: String = "",
    val name: String = "",
    val sku: String = "",
    val sellingPrice: Double = 0.0,
    val marginPercent: Double? = null,
    val currentStockValue: Double = 0.0
)

data class AccountSnapshot(
    @SerializedName("_id") val id: String = "",
    val accountName: String = "",
    val accountCode: String? = null
)

data class TaxGroupSnapshot(
    @SerializedName("_id") val id: String = "",
    val name: String = "",
    val totalRate: Double = 0.0,
    val isCompound: Boolean = false
)

data class TaxBreakdownDto(
    @SerializedName("_id") val id: String = "",
    val taxRateId: String? = null,
    val name: String = "",
    val rate: Double = 0.0,
    val taxAccountId: String? = null,
    val amount: Double = 0.0
)

data class CreditNoteUserSnapshot(
    @SerializedName("_id") val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val memberId: String? = null
)


data class CreateCreditNoteRequest(
    val customerId: String,
    val branchId: String?,
    val invoiceId: String? = null,
    val creditNoteDate: String,
    val currency: String = "INR",
    val exchangeRate: Double = 1.0,
    val notes: String = "",
    val priceIncludesTax: Boolean = false,
    val lines: List<CreateCreditNoteLineRequest>
)

data class CreateCreditNoteLineRequest(
    val lineType: String = "", // retail_product
    val itemId: String?,
    val itemName: String,
    val itemDescription: String,
    val discountPercent: Double = 0.0,
    val quantity: Double,
    val rate: Double,
    val reversalAccountId: String?,
    val taxGroupId: String?,
    val unit: String = "Pieces",
    val warehouseId: String? = null,
    val warehouseBinId: String? = null
//    val notes: String = ""
)

// Response wrapper for Credit Note Allocations
data class CreditNoteAllocationsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<CreditNoteAllocationItem> = emptyList()
)

data class CreditNoteAllocationItem(
    @SerializedName("_id") val id: String = "",
    val organizationId: String? = null,
    val branchId: String? = null,
    val creditNoteId: String? = null,
    val invoiceId: AllocationInvoiceDto? = null,
    val amountApplied: Double = 0.0,
    val appliedDate: String = "",
    val journalEntryId: String? = null,
    val notes: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class AllocationInvoiceDto(
    @SerializedName("_id") val id: String = "",
    val invoiceNumber: String = "",
    val grandTotal: Double = 0.0,
    val balanceDue: Double = 0.0,
    val status: String = ""
)

/**
 * Payload sent to apply credit balance to an outstanding sales invoice
 */
data class ApplyCreditToInvoiceRequest(
    @SerializedName("creditNoteId") val creditNoteId: String,
    @SerializedName("invoiceId") val invoiceId: String,
    @SerializedName("amountApplied") val amountApplied: Double,
    @SerializedName("appliedDate") val appliedDate: String
)

/**
 * Response received after applying credit balance to invoice
 */
data class ApplyCreditToInvoiceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AppliedCreditData?
)

data class AppliedCreditData(
    @SerializedName("_id") val id: String?,
    @SerializedName("creditNoteId") val creditNoteId: String?,
    @SerializedName("invoiceId") val invoiceId: String?,
    @SerializedName("amountApplied") val amountApplied: Double?,
    @SerializedName("appliedDate") val appliedDate: String?,
    @SerializedName("journalEntryId") val journalEntryId: String?,
    @SerializedName("notes") val notes: String?
)

///**
// * Outstanding invoice item model
// */
//data class CustomerOutstandingInvoiceItem(
//    @SerializedName("_id") val id: String,
//    @SerializedName("invoiceNumber") val invoiceNumber: String,
//    @SerializedName("dueDate") val dueDate: String?,
//    @SerializedName("currency") val currency: String?,
//    @SerializedName("grandTotal") val grandTotal: Double?,
//    @SerializedName("balanceDue") val balanceDue: Double,
//    @SerializedName("status") val status: String?
//)
//
//data class CustomerOutstandingInvoicesResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String,
//    @SerializedName("data") val data: List<CustomerOutstandingInvoiceItem>
//)


data class VoidCreditNoteResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: JsonElement? = null
)

data class SendCreditNoteResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: JsonElement? = null
)