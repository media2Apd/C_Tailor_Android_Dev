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
    val customerId: OrderOverviewCustomer,
    val branch: OrderOverviewBranch,
    val totalPaid: Double,
    val balanceAmount: Double,
    val paymentStatus: String,
    val source: String,
    val wearerType: String,
    val orderDate: String,
    val trialDate: String?,
    val deliveryDate: String?,
    val status: String,
    val summaryAdditionalCharges: List<OrderOverviewCharge> = emptyList(),
    val discount: Double = 0.0,
    val totalAmount: Double,
    val invoiceId: String? = null   // ← add this
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