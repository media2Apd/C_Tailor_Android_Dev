package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

data class OrderOverviewApiResponse(
    val success: Boolean,
    val data: OrderOverviewData
)

data class OrderOverviewData(
    val order: OrderOverviewOrder,
    val items: List<OrderOverviewItem>,
    val stages: List<OrderOverviewStage>,
    val payments: List<OrderOverviewPayment>,
    val delivery: OrderOverviewDelivery?
)

data class OrderOverviewOrder(
    val _id: String,
    val orderNumber: String,
    val customerId: OrderOverviewCustomer?,
    val branch: OrderOverviewBranch? = null,      // ✅ nullable + default
    val totalPaid: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val paymentStatus: String = "unpaid",
    val source: String? = null,                   // ✅ nullable
    val wearerType: String? = null,                // ✅ nullable
    val orderDate: String? = null,                 // ✅ nullable
    val trialDate: String? = null,
    val deliveryDate: String? = null,
    val status: String = "draft",
    val summaryAdditionalCharges: List<OrderOverviewCharge> = emptyList(),
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val invoiceId: String? = null
)
data class OrderOverviewCustomer(
    val _id: String,
    val name: String,
    val email: String? = null,
    val mobile: String,
    val gender: String?=null,
    val dob: String? = null,
    val address: OrderOverviewAddress? = null
)

data class OrderOverviewAddress(
    val addressLine: String? = null,
    val city: String? = null
)

data class OrderOverviewBranch(
    val _id: String,
    val name: String
)

data class OrderOverviewCharge(
    val amount: Double,
    val _id: String? = null
)

data class OrderOverviewItem(
    val _id: String,
    val categoryName: String,
    val quantity: Int,
    val stitchingCharge: Double,
    val priority: String,
    val trialRequired: Boolean,
    val fabricDetails: OrderOverviewFabricDetails? = null,
    val additionalCharges: List<OrderOverviewCharge> = emptyList()
)

data class OrderOverviewFabricDetails(
    val fabricSource: String? = null,
    val fabricType: String? = null,
    val color: String? = null,
    val pattern: String? = null
)

data class OrderOverviewStage(
    val _id: String,
    val garmentItemId: String,
    val stages: List<OrderOverviewStageStep>,
    val status: String
)

data class OrderOverviewStageStep(
    val stageName: String,
    val status: String,
    val assignedQuantity: Int,
    val completedQuantity: Int,
    val failedQuantity: Int,
    val assignedTo: List<StaffDto> = emptyList()
)
data class OrderOverviewPayment(
    val _id: String? = null,
    val paymentNumber: String? = null,
    val amount: Double? = null,
    val paymentDate: String? = null,
    val method: String? = null,
    val transactionId: String? = null,
    val notes: String? = null
)
data class OrderOverviewDelivery(
    val _id: String? = null,
    val status: String? = null
)



// ── Request ──
data class ConvertToInvoiceRequest(
    @SerializedName("salesOrderId") val salesOrderId: String,
    @SerializedName("dueDate") val dueDate: String? = null   // optional — backend can default if not sent
)

// ── Response ──
data class ConvertToInvoiceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ConvertToInvoiceData?
)

data class ConvertToInvoiceData(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String,
    @SerializedName("branchId") val branchId: String,
    @SerializedName("invoiceNumber") val invoiceNumber: String,
    @SerializedName("salesOrderId") val salesOrderId: String,
    @SerializedName("customerId") val customerId: String,
    @SerializedName("invoiceDate") val invoiceDate: String,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("items") val items: List<ConvertToInvoiceItem>,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("taxAmount") val taxAmount: Double,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("paidAmount") val paidAmount: Double,
    @SerializedName("balanceAmount") val balanceAmount: Double,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class ConvertToInvoiceItem(
    @SerializedName("_id") val id: String,
    @SerializedName("description") val description: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("tax") val tax: Double,
    @SerializedName("total") val total: Double
)


// ── Request body for "Receive Payment" ──
data class ReceivePaymentRequest(
    val amount: Double,
    val method: String,              // "cash", "card", "upi", "bank_transfer" etc.
    val transactionId: String = "",  // Reference No. (Optional)
    val notes: String = "",
    val paymentDate: String? = null, // ISO date string, e.g. "2026-07-27". null => backend defaults to today
    val paymentType: String = "full" // ← ADD THIS: "full" or "partial"
)
// ── Full API response wrapper ──
data class ReceivePaymentResponse(
    val success: Boolean,
    val message: String,
    val data: ReceivePaymentData
)

data class ReceivePaymentData(
    val order: PaymentOrderInfo,
    val payment: PaymentInfo
)

data class PaymentOrderInfo(
    @SerializedName("_id") val id: String,
    val orderNumber: String,
    val totalPaid: Double,
    val balanceAmount: Double,
    val paymentStatus: String,
    val totalAmount: Double,
    val discount: Double
)

data class PaymentInfo(
    @SerializedName("_id") val id: String,
    val paymentNumber: String,
    val amount: Double,
    val paymentDate: String,
    val method: String,
    val transactionId: String,
    val notes: String
)