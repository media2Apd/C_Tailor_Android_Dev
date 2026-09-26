package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

data class PaymentsMadeListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaymentsMadePagination?,
    @SerializedName("data") val data: List<PaymentsMadeItem> = emptyList()
)

data class PaymentsMadePagination(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 1
)

data class PaymentsMadeItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("branchId") val branchId: String?,
    @SerializedName("supplierId") val supplierId: PaymentSupplierRef?,
    @SerializedName("billId") val billId: PaymentBillRef?,
    @SerializedName("paymentNumber") val paymentNumber: String,
    @SerializedName("paymentDate") val paymentDate: String,
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("paymentMode") val paymentMode: String?,
    @SerializedName("referenceNumber") val referenceNumber: String?,
    @SerializedName("paidFromAccountId") val paidFromAccountId: PaymentAccountRef?,
    @SerializedName("status") val status: String = "Completed",
    @SerializedName("journalEntryId") val journalEntryId: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("createdBy") val createdBy: PaymentUserRef?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class PaymentSupplierRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String?
)

data class PaymentBillRef(
    @SerializedName("_id") val id: String,
    @SerializedName("billNumber") val billNumber: String?,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("balanceDue") val balanceDue: Double = 0.0
)

data class PaymentAccountRef(
    @SerializedName("_id") val id: String,
    @SerializedName("accountName") val accountName: String?,
    @SerializedName("accountCode") val accountCode: String?
)

data class PaymentUserRef(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("memberId") val memberId: String?
)