@file:Suppress("unused")

package com.cuso.tailor.model.finance

import com.cuso.tailor.model.sales.PaginationInfo
import com.google.gson.annotations.SerializedName

// ── CUSTOMER PAYMENT LIST RESPONSE ──
data class CustomerPaymentResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: List<CustomerPaymentItem> = emptyList(),
    val pagination: PaginationInfo? = null
)

data class CustomerPaymentItem(
    @SerializedName("_id", alternate = ["id"])
    val _id: String = "",
    val organizationId: String = "",
    val branchId: String = "",
    @SerializedName("customerId", alternate = ["customer"])
    val customerId: CustomerPaymentCustomer? = null,
    @SerializedName("invoiceId", alternate = ["invoice"])
    val invoiceId: CustomerPaymentInvoice? = null,
    @SerializedName("salesOrderId", alternate = ["salesOrder"])
    val salesOrderId: CustomerPaymentSalesOrder? = null,
    val paymentNumber: String = "",
    val paymentDate: String = "",
    val currency: String = "INR",
    val exchangeRate: Double = 1.0,
    val amount: Double = 0.0,
    val isAdvancePayment: Boolean = false,
    val unallocatedAmount: Double = 0.0,
    val paymentMode: String = "",
    val referenceNumber: String? = "",
    val bankAccountId: String? = null,
    @SerializedName("depositToAccountId", alternate = ["depositToAccount"])
    val depositToAccountId: CustomerPaymentDepositAccount? = null,
    val status: String = "",
    val approvalStatus: String = "",
    val journalEntryId: String? = null,
    val notes: String? = "",
    val attachments: List<CustomerPaymentAttachment> = emptyList(),
    val createdBy: CustomerPaymentCreatedBy? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    val id: String get() = _id
    val customer: CustomerPaymentCustomer? get() = customerId
    val invoice: CustomerPaymentInvoice? get() = invoiceId
    val salesOrder: CustomerPaymentSalesOrder? get() = salesOrderId
    val depositToAccount: CustomerPaymentDepositAccount? get() = depositToAccountId
}

data class CustomerPaymentCustomer(
    @SerializedName("_id", alternate = ["id"])
    val _id: String = "",
    val customerCode: String = "",
    val fullName: String = "",
    val mobileNumber: String = "",
    val email: String = ""
) {
    val id: String get() = _id
}

data class CustomerPaymentInvoice(
    @SerializedName("_id", alternate = ["id"])
    val _id: String = "",
    val invoiceNumber: String = "",
    val grandTotal: Double = 0.0,
    val balanceDue: Double = 0.0,
    val status: String = ""
) {
    val id: String get() = _id
}

data class CustomerPaymentSalesOrder(
    @SerializedName("_id", alternate = ["id"])
    val _id: String = "",
    val orderCode: String = "",
    val grandTotal: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val status: String = ""
) {
    val id: String get() = _id
}

data class CustomerPaymentDepositAccount(
    @SerializedName("_id", alternate = ["id"])
    val _id: String = "",
    val accountName: String = "",
    val accountCode: String = "",
    val normalBalance: String = "",
    val currency: String = "INR"
) {
    val id: String get() = _id
}

data class CustomerPaymentAttachment(
    @SerializedName("_id", alternate = ["id"])
    val _id: String = "",
    val url: String = "",
    val publicId: String? = null,
    val name: String = "",
    val uploadedAt: String = ""
) {
    val id: String get() = _id
}

data class CustomerPaymentCreatedBy(
    @SerializedName("_id", alternate = ["id"])
    val _id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val memberId: String? = null
) {
    val id: String get() = _id
}

// ── CUSTOMER OUTSTANDING INVOICES ──
data class CustomerOutstandingInvoicesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: List<CustomerOutstandingInvoiceItem> = emptyList()
)

data class CustomerOutstandingInvoiceItem(
    @SerializedName("_id", alternate = ["id"]) val _id: String = "",
    @SerializedName("invoiceNumber") val invoiceNumber: String = "",
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("balanceDue") val balanceDue: Double = 0.0,
    @SerializedName("status") val status: String = ""
) {
    val id: String get() = _id
}

// ── CUSTOMER PAYMENT DETAIL (VIEW ONE) ──
data class CustomerPaymentDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: CustomerPaymentDetailData? = null
)

data class CustomerPaymentDetailData(
    @SerializedName("_id", alternate = ["id"])
    val _id: String = "",
    val organizationId: String? = null,
    val branchId: String? = null,
    @SerializedName("customerId", alternate = ["customer"])
    val customerId: CustomerPaymentCustomer? = null,
    @SerializedName("invoiceId", alternate = ["invoice"])
    val invoiceId: CustomerPaymentInvoice? = null,
    @SerializedName("salesOrderId", alternate = ["salesOrder"])
    val salesOrderId: CustomerPaymentSalesOrder? = null,
    val paymentNumber: String = "",
    val paymentDate: String = "",
    val currency: String = "INR",
    val exchangeRate: Double = 1.0,
    val amount: Double = 0.0,
    val isAdvancePayment: Boolean = false,
    val unallocatedAmount: Double = 0.0,
    val paymentMode: String = "",
    val referenceNumber: String? = null,
    @SerializedName("depositToAccountId", alternate = ["depositToAccount"])
    val depositToAccountId: CustomerPaymentDepositAccount? = null,
    val status: String = "",
    val approvalStatus: String? = null,
    val journalEntryId: String? = null,
    val notes: String? = null,
    val attachments: List<CustomerPaymentAttachment> = emptyList(),
    val createdBy: CustomerPaymentCreatedBy? = null,
    val createdAt: String? = null
) {
    val id: String get() = _id
    val customer: CustomerPaymentCustomer? get() = customerId
    val invoice: CustomerPaymentInvoice? get() = invoiceId
    val salesOrder: CustomerPaymentSalesOrder? get() = salesOrderId
    val depositToAccount: CustomerPaymentDepositAccount? get() = depositToAccountId
}

// Type aliases for backwards compatibility
typealias ReceiptAttachment = CustomerPaymentAttachment
typealias ReceiptCreatedBy = CustomerPaymentCreatedBy