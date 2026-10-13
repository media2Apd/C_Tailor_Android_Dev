package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName

data class RecordPaymentApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: RecordPaymentData?
)

data class RecordPaymentData(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("branchId") val branchId: String?,
    @SerializedName("customerId") val customer: PaymentCustomerInfo?,
    @SerializedName("invoiceId") val invoice: PaymentInvoiceInfo?,
    @SerializedName("paymentNumber") val paymentNumber: String?,
    @SerializedName("paymentDate") val paymentDate: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("paymentMode") val paymentMode: String?,
    @SerializedName("referenceNumber") val referenceNumber: String?,
    @SerializedName("depositToAccountId") val depositAccount: PaymentAccountInfo?,
    @SerializedName("status") val status: String?,
    @SerializedName("journalEntryId") val journalEntryId: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("attachments") val attachments: List<PaymentAttachmentItem>?
)

data class PaymentCustomerInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("customerCode") val customerCode: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("mobileNumber") val mobileNumber: String?,
    @SerializedName("email") val email: String?
)

data class PaymentInvoiceInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("invoiceNumber") val invoiceNumber: String?,
    @SerializedName("grandTotal") val grandTotal: Double?,
    @SerializedName("balanceDue") val balanceDue: Double?,
    @SerializedName("status") val status: String?
)

data class PaymentAccountInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("accountName") val accountName: String?,
    @SerializedName("accountCode") val accountCode: String?,
    @SerializedName("normalBalance") val normalBalance: String?,
    @SerializedName("currency") val currency: String?
)

data class PaymentAttachmentItem(
    @SerializedName("url") val url: String?,
    @SerializedName("publicId") val publicId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("_id") val id: String?,
    @SerializedName("uploadedAt") val uploadedAt: String?
)
