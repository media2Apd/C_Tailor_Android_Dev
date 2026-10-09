package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName

// ── View All Models ──
data class BillingPaymentListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<BillingPaymentItemDto> = emptyList(),
    val pagination: PaginationInfo? = null
)

data class BillingPaymentItemDto(
    @SerializedName("_id") val id: String = "",
    val orderCode: String = "",
    val orderDate: String? = null,
    val dueDate: String? = null,
    val orderType: String = "",
    val orderStatus: String = "",
    val customer: BillingCustomerDto? = null,
    val branch: BillingBranchDto? = null,
    val billing: BillingSummaryDto? = null,
    val itemsCount: Int = 0,
    val createdAt: String? = null
)

data class BillingCustomerDto(
    @SerializedName("_id") val id: String = "",
    val code: String? = null,
    val name: String = "",
    val phone: String = "",
    val email: String? = null,
    val level: String? = null,
    val billingAddress: CustomerAddressV2? = null
)

data class BillingBranchDto(
    @SerializedName("_id") val id: String = "",
    val name: String = ""
)

data class BillingSummaryDto(
    val currency: String = "INR",
    val subtotal: Double = 0.0,
    val totalTax: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val grandTotal: Double = 0.0,
    @SerializedName("paidAmount", alternate = ["totalPaid"]) val paidAmount: Double = 0.0,
    val balanceDue: Double = 0.0,
    val paymentMode: String = "Cash",
    val paymentStatus: String = "Pending",
    val paymentType: String = "Advance"
)

// ── View One Models ──
data class BillingPaymentDetailResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: BillingPaymentDetailData? = null
)

data class BillingPaymentDetailData(
    val orderHeader: BillingOrderHeaderDto? = null,
    val customer: BillingCustomerDto? = null,
    val branch: BillingBranchDto? = null,
    val billingSummary: BillingSummaryDto? = null,
    val itemsCount: Int = 0,
    val items: List<BillingLineItemDto> = emptyList(),
    val paymentsCount: Int = 0,
    val paymentHistory: List<BillingHistoryItemDto> = emptyList()
)

data class BillingOrderHeaderDto(
    @SerializedName("_id") val id: String = "",
    val orderCode: String = "",
    val orderDate: String? = null,
    val dueDate: String? = null,
    val orderType: String = "",
    val priority: String = "Medium",
    val status: String = "",
    val invoiceId: String? = null,
    val createdAt: String? = null
)

data class BillingLineItemDto(
    @SerializedName("_id") val id: String = "",
    val lineType: String = "",
    val itemName: String = "",
    val quantity: Int = 1,
    val unit: String = "Pieces",
    val unitPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val lineTotal: Double = 0.0
)

data class BillingHistoryItemDto(
    @SerializedName("_id") val id: String = "",
    val paymentNumber: String = "",
    val paymentDate: String? = null,
    val amount: Double = 0.0,
    val currency: String = "INR",
    val paymentMode: String = "Cash",
    val referenceNumber: String? = null,
    val depositAccount: String? = null,
    val notes: String? = null,
    val status: String = "Completed",
    val receivedBy: String? = null
)

// ── Record Payment Models ──
data class RecordOrderPaymentRequest(
    val amount: Double,
    val notes: String,
    val paymentDate: String,
    val paymentMode: String
)

data class RecordOrderPaymentResponse(
    val success: Boolean = false,
    val message: String? = null
)