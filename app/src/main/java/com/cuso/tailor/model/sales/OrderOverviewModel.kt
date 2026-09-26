package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName
//
//data class OrderOverviewApiResponse(
//    val success: Boolean,
//    val data: OrderOverviewData
//)
//
//data class OrderOverviewData(
//    val order: OrderOverviewOrder,
//    val items: List<OrderOverviewItem>,
//    val stages: List<OrderOverviewStage>,
//    val payments: List<OrderOverviewPayment>,
//    val delivery: OrderOverviewDelivery?
//)
//
//data class OrderOverviewOrder(
//    val _id: String,
//    val orderNumber: String,
//    val customerId: OrderOverviewCustomer?,
//    val branch: OrderOverviewBranch? = null,      //   nullable + default
//    val totalPaid: Double = 0.0,
//    val balanceAmount: Double = 0.0,
//    val paymentStatus: String = "unpaid",
//    val source: String? = null,                   //   nullable
//    val wearerType: String? = null,                //   nullable
//    val orderDate: String? = null,                 //   nullable
//    val trialDate: String? = null,
//    val deliveryDate: String? = null,
//    val status: String = "draft",
//    val summaryAdditionalCharges: List<OrderOverviewCharge> = emptyList(),
//    val discount: Double = 0.0,
//    val totalAmount: Double = 0.0,
//    val invoiceId: String? = null
//)
//data class OrderOverviewCustomer(
//    val _id: String,
//    val name: String,
//    val email: String? = null,
//    val mobile: String,
//    val gender: String?=null,
//    val dob: String? = null,
//    val address: OrderOverviewAddress? = null
//)
//
//data class OrderOverviewAddress(
//    val addressLine: String? = null,
//    val city: String? = null
//)
//
//data class OrderOverviewBranch(
//    val _id: String,
//    val name: String
//)
//
//data class OrderOverviewCharge(
//    val amount: Double,
//    val _id: String? = null
//)
//
//data class OrderOverviewItem(
//    val _id: String,
//    val categoryName: String,
//    val quantity: Int,
//    val stitchingCharge: Double,
//    val priority: String,
//    val trialRequired: Boolean,
//    val fabricDetails: OrderOverviewFabricDetails? = null,
//    val additionalCharges: List<OrderOverviewCharge> = emptyList()
//)
//
//data class OrderOverviewFabricDetails(
//    val fabricSource: String? = null,
//    val fabricType: String? = null,
//    val color: String? = null,
//    val pattern: String? = null
//)
//
//data class OrderOverviewStage(
//    val _id: String,
//    val garmentItemId: String,
//    val stages: List<OrderOverviewStageStep>,
//    val status: String
//)
//
//data class OrderOverviewStageStep(
//    val stageName: String,
//    val status: String,
//    val assignedQuantity: Int,
//    val completedQuantity: Int,
//    val failedQuantity: Int,
//    val assignedTo: List<StaffDto> = emptyList()
//)
//data class OrderOverviewPayment(
//    val _id: String? = null,
//    val paymentNumber: String? = null,
//    val amount: Double? = null,
//    val paymentDate: String? = null,
//    val method: String? = null,
//    val transactionId: String? = null,
//    val notes: String? = null
//)
//data class OrderOverviewDelivery(
//    val _id: String? = null,
//    val status: String? = null
//)



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

//// ─────────────────────────────────────────────────────────────
//// API Response Models for Order Overview / Details
//// ─────────────────────────────────────────────────────────────
//
//data class OrderOverviewApiResponse(
//    @SerializedName("success") val success: Boolean = false,
//    @SerializedName("message") val message: String? = null,
//    @SerializedName("data") val data: OrderOverviewData
//)
//
//data class OrderOverviewData(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("orderCode") val orderCode: String? = null,
//    @SerializedName("orderNumber") val orderNumberLegacy: String? = null,
//    @SerializedName("organizationId") val organizationId: String? = null,
//    @SerializedName("branchId") val branchId: String? = null,
//    @SerializedName("orderDate") val orderDate: String? = null,
//    @SerializedName("dueDate") val dueDate: String? = null,
//    @SerializedName("customerSnapshot") val customerSnapshot: OrderCustomerSnapshot? = null,
//    @SerializedName("customerId") val customerId: OrderCustomerDetails? = null,
//    @SerializedName("salespersonId") val salespersonId: OrderSalesperson? = null,
//    @SerializedName("paymentTermId") val paymentTermId: OrderPaymentTerm? = null,
//    @SerializedName("orderType") val orderType: String? = null,
//    @SerializedName("priority") val priority: String? = null,
//    @SerializedName("orderNotes") val orderNotes: String? = null,
//    @SerializedName("items") val items: List<OrderOverviewItem> = emptyList(),
//    @SerializedName("subtotal") val subtotal: Double? = 0.0,
//    @SerializedName("totalTax") val totalTax: Double? = 0.0,
//    @SerializedName("totalDiscount") val totalDiscount: Double? = 0.0,
//    @SerializedName("deliveryCharge") val deliveryCharge: Double? = 0.0,
//    @SerializedName("grandTotal") val grandTotal: Double? = 0.0,
//    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double? = 0.0,
//    @SerializedName("advanceAmountRequired") val advanceAmountRequired: Double? = 0.0,
//    @SerializedName("balanceAmount") val balanceAmount: Double? = 0.0,
//    @SerializedName("deliveryMethod") val deliveryMethod: String? = null,
//    @SerializedName("paymentType") val paymentType: String? = null,
//    @SerializedName("paymentMode") val paymentMode: String? = null,
//    @SerializedName("status") val status: String? = null,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("updatedAt") val updatedAt: String? = null,
//
//    // Legacy fallback mapping
//    @SerializedName("order") private val _legacyOrder: OrderOverviewOrder? = null
//) {
//    /**
//     * Bridges legacy screen code that expects `orderData.order`
//     */
//    val order: OrderOverviewOrder
//        get() = _legacyOrder ?: OrderOverviewOrder(
//            _id = id,
//            orderNumber = orderCode?.takeIf { it.isNotBlank() }
//                ?: orderNumberLegacy?.takeIf { it.isNotBlank() }
//                ?: id.takeLast(6).uppercase(),
//            customerId = OrderOverviewCustomer(
//                _id = customerId?._id ?: "",
//                name = customerSnapshot?.name ?: customerId?.fullName ?: "—",
//                email = customerSnapshot?.email ?: customerId?.email,
//                mobile = customerSnapshot?.phone ?: customerId?.mobileNumber ?: "—",
//                address = OrderOverviewAddress(
//                    addressLine = listOfNotNull(
//                        customerSnapshot?.billingAddress?.flatNo,
//                        customerSnapshot?.billingAddress?.street
//                    ).filter { it.isNotBlank() }.joinToString(", ").ifBlank {
//                        customerId?.billingAddress?.street
//                    },
//                    city = customerSnapshot?.billingAddress?.city ?: customerId?.billingAddress?.city
//                )
//            ),
//            totalPaid = advanceAmountPaid ?: 0.0,
//            balanceAmount = balanceAmount ?: 0.0,
//            paymentStatus = when {
//                (balanceAmount ?: 0.0) <= 0.0 -> "paid"
//                (advanceAmountPaid ?: 0.0) > 0.0 -> "partial"
//                else -> "unpaid"
//            },
//            status = status ?: "In_Production",
//            totalAmount = grandTotal ?: subtotal ?: 0.0,
//            deliveryDate = dueDate
//        )
//}
//
//data class OrderCustomerSnapshot(
//    @SerializedName("customerCode") val customerCode: String? = null,
//    @SerializedName("name") val name: String? = null,
//    @SerializedName("email") val email: String? = null,
//    @SerializedName("phone") val phone: String? = null,
//    @SerializedName("billingAddress") val billingAddress: OrderAddressDto? = null,
//    @SerializedName("shippingAddress") val shippingAddress: OrderAddressDto? = null,
//    @SerializedName("taxId") val taxId: String? = null
//)
//
//data class OrderCustomerDetails(
//    @SerializedName("_id") val _id: String? = null,
//    @SerializedName("customerCode") val customerCode: String? = null,
//    @SerializedName("fullName") val fullName: String? = null,
//    @SerializedName("mobileNumber") val mobileNumber: String? = null,
//    @SerializedName("email") val email: String? = null,
//    @SerializedName("billingAddress") val billingAddress: OrderAddressDto? = null
//)
//
//data class OrderAddressDto(
//    @SerializedName("flatNo") val flatNo: String? = null,
//    @SerializedName("street") val street: String? = null,
//    @SerializedName("areaZone") val areaZone: String? = null,
//    @SerializedName("city") val city: String? = null,
//    @SerializedName("subdivisionName") val subdivisionName: String? = null,
//    @SerializedName("countryName") val countryName: String? = null,
//    @SerializedName("pincode") val pincode: String? = null
//)
//
//data class OrderSalesperson(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("firstName") val firstName: String? = null,
//    @SerializedName("lastName") val lastName: String? = null
//) {
//    val fullName: String
//        get() = listOfNotNull(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
//}
//
//data class OrderPaymentTerm(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("name") val name: String? = null,
//    @SerializedName("code") val code: String? = null,
//    @SerializedName("dueDays") val dueDays: Int? = null
//)
//
//data class OrderOverviewItem(
//    @SerializedName("_id") val _id: String = "",
//    @SerializedName("itemDescription") val itemDescription: String? = null,
//    @SerializedName("lineType") val lineType: String? = null,
//    @SerializedName("quantity") val quantityNumber: Double? = 1.0,
//    @SerializedName("unit") val unit: String? = null,
//    @SerializedName("unitPrice") val unitPrice: Double? = 0.0,
//    @SerializedName("discountAmount") val discountAmount: Double? = 0.0,
//    @SerializedName("taxableAmount") val taxableAmount: Double? = 0.0,
//    @SerializedName("taxAmount") val taxAmount: Double? = 0.0,
//    @SerializedName("lineTotal") val lineTotal: Double? = 0.0,
//    @SerializedName("productionState") val productionState: String? = null,
//    @SerializedName("currentProductionStageName") val currentProductionStageName: String? = null,
//    @SerializedName("customGarment") val customGarment: OrderOverviewCustomGarment? = null,
//    @SerializedName("categoryName") private val _categoryName: String? = null,
//    @SerializedName("priority") val priority: String = "Medium",
//    @SerializedName("trialRequired") val trialRequired: Boolean = false,
//    @SerializedName("additionalCharges") val additionalCharges: List<OrderOverviewCharge> = emptyList()
//) {
//    val quantity: Int
//        get() = quantityNumber?.toInt() ?: 1
//
//    val categoryName: String
//        get() = _categoryName?.takeIf { it.isNotBlank() }
//            ?: customGarment?.categoryDisplayName?.takeIf { it.isNotBlank() }
//            ?: customGarment?.garmentName?.takeIf { it.isNotBlank() }
//            ?: itemDescription?.takeIf { it.isNotBlank() }
//            ?: "Custom Garment"
//
//    val stitchingCharge: Double
//        get() = unitPrice ?: 0.0
//}
//
//data class OrderOverviewCustomGarment(
//    @SerializedName("segmentName") val segmentName: String? = null,
//    @SerializedName("garmentName") val garmentName: String? = null,
//    @SerializedName("categoryDisplayName") val categoryDisplayName: String? = null,
//    @SerializedName("designName") val designName: String? = null,
//    @SerializedName("stitchingType") val stitchingType: String? = null,
//    @SerializedName("fabricSource") val fabricSource: String? = null,
//    @SerializedName("fabricNotes") val fabricNotes: String? = null
//)
//
//// Legacy wrappers to prevent compilation errors
//data class OrderOverviewOrder(
//    val _id: String,
//    val orderNumber: String,
//    val customerId: OrderOverviewCustomer?,
//    val branch: OrderOverviewBranch? = null,
//    val totalPaid: Double = 0.0,
//    val balanceAmount: Double = 0.0,
//    val paymentStatus: String = "unpaid",
//    val source: String? = null,
//    val wearerType: String? = null,
//    val orderDate: String? = null,
//    val trialDate: String? = null,
//    val deliveryDate: String? = null,
//    val status: String = "In_Production",
//    val summaryAdditionalCharges: List<OrderOverviewCharge> = emptyList(),
//    val discount: Double = 0.0,
//    val totalAmount: Double = 0.0,
//    val invoiceId: String? = null
//)
//
//data class OrderOverviewCustomer(
//    val _id: String,
//    val name: String,
//    val email: String? = null,
//    val mobile: String,
//    val gender: String? = null,
//    val dob: String? = null,
//    val address: OrderOverviewAddress? = null
//)
//
//data class OrderOverviewAddress(
//    val addressLine: String? = null,
//    val city: String? = null
//)
//
//data class OrderOverviewBranch(
//    val _id: String,
//    val name: String
//)
//
//data class OrderOverviewCharge(
//    val amount: Double = 0.0,
//    val _id: String? = null
//)
















// ─────────────────────────────────────────────────────────────
// API Response Models for Order Overview / Details
// ─────────────────────────────────────────────────────────────

data class OrderOverviewApiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: OrderOverviewData
)

data class OrderOverviewData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("orderCode") val orderCode: String? = null,
    @SerializedName("orderNumber") val orderNumberLegacy: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("orderDate") val orderDate: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("trialDate") val trialDate: String? = null,
    @SerializedName("customerSnapshot") val customerSnapshot: OrderCustomerSnapshot? = null,
    @SerializedName("customerId") val customerId: OrderCustomerDetails? = null,
    @SerializedName("salespersonId") val salespersonId: OrderSalesperson? = null,
    @SerializedName("paymentTermId") val paymentTermId: OrderPaymentTerm? = null,
    @SerializedName("orderType") val orderType: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("orderNotes") val orderNotes: String? = null,
    @SerializedName("billingNotes") val billingNotes: String? = null,
    @SerializedName("items") val items: List<OrderOverviewItem> = emptyList(),
    @SerializedName("subtotal") val subtotal: Double? = 0.0,
    @SerializedName("totalTax") val totalTax: Double? = 0.0,
    @SerializedName("totalDiscount") val totalDiscount: Double? = 0.0,
    @SerializedName("deliveryCharge") val deliveryCharge: Double? = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double? = 0.0,
    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double? = 0.0,
    @SerializedName("advanceAmountRequired") val advanceAmountRequired: Double? = 0.0,
    @SerializedName("balanceAmount") val balanceAmount: Double? = 0.0,
    @SerializedName("deliveryMethod") val deliveryMethod: String? = null,
    @SerializedName("paymentType") val paymentType: String? = null,
    @SerializedName("paymentMode") val paymentMode: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,

    // Workflow Stages, Payments, and Delivery
    @SerializedName("stages") val stages: List<OrderOverviewStage> = emptyList(),
    @SerializedName("delivery") val delivery: OrderOverviewDelivery? = null,
    @SerializedName("payments") private val _payments: List<OrderOverviewPayment>? = null,

    // Legacy fallback mapping
    @SerializedName("order") private val _legacyOrder: OrderOverviewOrder? = null
) {
    /**
     * Synthesizes payments from advance/balance fields if explicit payment array is omitted.
     */
    val payments: List<OrderOverviewPayment>
        get() = _payments ?: if ((advanceAmountPaid ?: 0.0) > 0.0) {
            listOf(
                OrderOverviewPayment(
                    _id = id,
                    paymentNumber = "ADV-1",
                    amount = advanceAmountPaid,
                    paymentDate = createdAt ?: orderDate,
                    method = paymentMode ?: "Advance",
                    transactionId = "",
                    notes = billingNotes ?: orderNotes
                )
            )
        } else emptyList()

    /**
     * Bridges legacy callers expecting `orderData.order`
     */
    val order: OrderOverviewOrder
        get() = _legacyOrder ?: OrderOverviewOrder(
            _id = id,
            orderNumber = orderCode?.takeIf { it.isNotBlank() }
                ?: orderNumberLegacy?.takeIf { it.isNotBlank() }
                ?: id.takeLast(6).uppercase(),
            customerId = OrderOverviewCustomer(
                _id = customerId?._id ?: "",
                name = customerSnapshot?.name ?: customerId?.fullName ?: "—",
                email = customerSnapshot?.email ?: customerId?.email,
                mobile = customerSnapshot?.phone ?: customerId?.mobileNumber ?: "—",
                address = OrderOverviewAddress(
                    addressLine = listOfNotNull(
                        customerSnapshot?.billingAddress?.flatNo,
                        customerSnapshot?.billingAddress?.street
                    ).filter { it.isNotBlank() }.joinToString(", ").ifBlank {
                        customerId?.billingAddress?.street
                    },
                    city = customerSnapshot?.billingAddress?.city ?: customerId?.billingAddress?.city
                )
            ),
            totalPaid = advanceAmountPaid ?: 0.0,
            balanceAmount = balanceAmount ?: 0.0,
            paymentStatus = when {
                (balanceAmount ?: 0.0) <= 0.0 -> "paid"
                (advanceAmountPaid ?: 0.0) > 0.0 -> "partial"
                else -> "unpaid"
            },
            status = status ?: "In_Production",
            totalAmount = grandTotal ?: subtotal ?: 0.0,
            deliveryDate = dueDate,
            trialDate = trialDate,
            orderDate = orderDate,
            source = orderType ?: priority,
            discount = totalDiscount ?: 0.0
        )
}

// ─────────────────────────────────────────────────────────────
// Customer & Location Entities
// ─────────────────────────────────────────────────────────────

data class OrderCustomerSnapshot(
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("billingAddress") val billingAddress: OrderAddressDto? = null,
    @SerializedName("shippingAddress") val shippingAddress: OrderAddressDto? = null,
    @SerializedName("taxId") val taxId: String? = null
)

data class OrderCustomerDetails(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("billingAddress") val billingAddress: OrderAddressDto? = null
)

data class OrderAddressDto(
    @SerializedName("flatNo") val flatNo: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("areaZone") val areaZone: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("subdivisionName") val subdivisionName: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("pincode") val pincode: String? = null
)

data class OrderSalesperson(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)

data class OrderPaymentTerm(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("dueDays") val dueDays: Int? = null
)

// ─────────────────────────────────────────────────────────────
// Item & Fabric Details
// ─────────────────────────────────────────────────────────────

data class OrderOverviewItem(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("itemDescription") val itemDescription: String? = null,
    @SerializedName("lineType") val lineType: String? = null,
    @SerializedName("quantity") val quantityNumber: Double? = 1.0,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("unitPrice") val unitPrice: Double? = 0.0,
    @SerializedName("discountAmount") val discountAmount: Double? = 0.0,
    @SerializedName("taxableAmount") val taxableAmount: Double? = 0.0,
    @SerializedName("taxAmount") val taxAmount: Double? = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double? = 0.0,
    @SerializedName("productionState") val productionState: String? = null,
    @SerializedName("currentProductionStageName") val currentProductionStageName: String? = null,
    @SerializedName("customGarment") val customGarment: OrderOverviewCustomGarment? = null,
    @SerializedName("categoryName") private val _categoryName: String? = null,
    @SerializedName("stitchingCharge") private val _stitchingCharge: Double? = null,
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("trialRequired") val trialRequired: Boolean = false,
    @SerializedName("additionalCharges") val additionalCharges: List<OrderOverviewCharge> = emptyList(),
    @SerializedName("fabricDetails") private val _fabricDetails: OrderOverviewFabricDetails? = null
) {
    val quantity: Int
        get() = quantityNumber?.toInt() ?: 1

    val categoryName: String
        get() = _categoryName?.takeIf { it.isNotBlank() }
            ?: customGarment?.categoryDisplayName?.takeIf { it.isNotBlank() }
            ?: customGarment?.garmentName?.takeIf { it.isNotBlank() }
            ?: itemDescription?.takeIf { it.isNotBlank() }
            ?: "Custom Garment"

    val stitchingCharge: Double
        get() = _stitchingCharge ?: unitPrice ?: 0.0

    /**
     * Resolves fabric details from direct field or nested customGarment object
     */
    val fabricDetails: OrderOverviewFabricDetails?
        get() = _fabricDetails ?: customGarment?.let {
            OrderOverviewFabricDetails(
                fabricSource = it.fabricSource,
                fabricType = it.fabricNotes,
                color = it.colorAccent,
                pattern = null
            )
        }
}

data class OrderOverviewCustomGarment(
    @SerializedName("segmentName") val segmentName: String? = null,
    @SerializedName("garmentName") val garmentName: String? = null,
    @SerializedName("categoryDisplayName") val categoryDisplayName: String? = null,
    @SerializedName("designName") val designName: String? = null,
    @SerializedName("stitchingType") val stitchingType: String? = null,
    @SerializedName("fabricSource") val fabricSource: String? = null,
    @SerializedName("fabricNotes") val fabricNotes: String? = null,
    @SerializedName("colorAccent") val colorAccent: String? = null
)

data class OrderOverviewFabricDetails(
    val fabricSource: String? = null,
    val fabricType: String? = null,
    val color: String? = null,
    val pattern: String? = null
)

// ─────────────────────────────────────────────────────────────
// Production Stages & Tracking
// ─────────────────────────────────────────────────────────────

data class OrderOverviewStage(
    val _id: String = "",
    val garmentItemId: String = "",
    val stages: List<OrderOverviewStageStep> = emptyList(),
    val status: String = ""
)

data class OrderOverviewStageStep(
    val stageName: String = "",
    val status: String = "",
    val assignedQuantity: Int = 0,
    val completedQuantity: Int = 0,
    val failedQuantity: Int = 0,
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

// ─────────────────────────────────────────────────────────────
// Legacy Compatibility Models
// ─────────────────────────────────────────────────────────────

data class OrderOverviewOrder(
    val _id: String,
    val orderNumber: String,
    val customerId: OrderOverviewCustomer?,
    val branch: OrderOverviewBranch? = null,
    val totalPaid: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val paymentStatus: String = "unpaid",
    val source: String? = null,
    val wearerType: String? = null,
    val orderDate: String? = null,
    val trialDate: String? = null,
    val deliveryDate: String? = null,
    val status: String = "In_Production",
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
    val gender: String? = null,
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
    val amount: Double = 0.0,
    val _id: String? = null
)