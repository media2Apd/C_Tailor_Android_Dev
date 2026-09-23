package com.cuso.tailor.model.inventory

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ProcurementBillListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: ProcurementPagination?,
    @SerializedName("data") val data: List<ProcurementBillItem> = emptyList()
)

data class ProcurementPagination(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 1
)

data class BillAddressDto(
    @SerializedName("flatNo") val flatNo: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("stateCode") val stateCode: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("pincode") val pincode: String? = null
)

data class BillSupplierSnapshot(
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("gstin") val gstin: String? = null,
    @SerializedName("pan") val pan: String? = null,
    @SerializedName("billingAddress") val billingAddress: BillAddressDto? = null,
    @SerializedName("shippingAddress") val shippingAddress: BillAddressDto? = null
)

data class BillSupplierRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String
)

data class BillPurchaseOrderRef(
    @SerializedName("_id") val id: String,
    @SerializedName("poNumber") val poNumber: String?,
    @SerializedName("orderStatus") val orderStatus: String?
)

data class BillWarehouseRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String
)

data class BillCompanySnapshot(
    @SerializedName("name") val name: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("gstin") val gstin: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("email") val email: String?
)

data class BillLineItem(
    @SerializedName("_id") val id: String,
    @SerializedName("lineType") val lineType: String?,
    @SerializedName("hsnCode") val hsnCode: String?,
    @SerializedName("itemDescription") val itemDescription: String?,
    @SerializedName("quantity") val quantity: Double = 0.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxableAmount") val taxableAmount: Double = 0.0,
    @SerializedName("totalTax") val totalTax: Double = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double = 0.0
)

data class ProcurementBillItem(
    @SerializedName("_id") val id: String,
    @SerializedName("billNumber") val billNumber: String,
    @SerializedName("supplierBillReference") val supplierBillReference: String?,
    @SerializedName("billDate") val billDate: String,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("status") val status: String = "Draft",
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("totalTax") val totalTax: Double = 0.0,
    @SerializedName("totalDiscount") val totalDiscount: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("amountPaid") val amountPaid: Double = 0.0,
    @SerializedName("balanceDue") val balanceDue: Double = 0.0,
    @SerializedName("notes") val notes: String?,
    @SerializedName("supplierSnapshot") val supplierSnapshot: BillSupplierSnapshot?,
    @SerializedName("supplierId") val supplierId: BillSupplierRef?,
    @SerializedName("purchaseOrderId") val purchaseOrderId: BillPurchaseOrderRef?,
    @SerializedName("warehouseId") val warehouseId: BillWarehouseRef?,
    @SerializedName("companySnapshot") val companySnapshot: BillCompanySnapshot?,
    @SerializedName("lines") val lines: List<BillLineItem> = emptyList()
)

data class ProcurementBillDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ProcurementBillDetailData?
)

data class ProcurementBillDetailData(
    @SerializedName("_id") val id: String,
    @SerializedName("billNumber") val billNumber: String,
    @SerializedName("supplierBillReference") val supplierBillReference: String?,
    @SerializedName("billDate") val billDate: String,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("exchangeRate") val exchangeRate: Double = 1.0,
    @SerializedName("priceIncludesTax") val priceIncludesTax: Boolean = false,
    @SerializedName("placeOfSupply") val placeOfSupply: String?,
    @SerializedName("taxType") val taxType: String?,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("totalTax") val totalTax: Double = 0.0,
    @SerializedName("totalDiscount") val totalDiscount: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("amountPaid") val amountPaid: Double = 0.0,
    @SerializedName("balanceDue") val balanceDue: Double = 0.0,
    @SerializedName("availableAdvance") val availableAdvance: Double = 0.0,
    @SerializedName("notes") val notes: String?,
    @SerializedName("status") val status: String = "Draft",
    @SerializedName("approvalStatus") val approvalStatus: String?,
    @SerializedName("supplierSnapshot") val supplierSnapshot: BillSupplierSnapshot?,
    @SerializedName("supplierId") val supplierId: BillSupplierRef?,
    @SerializedName("purchaseOrderId") val purchaseOrderId: BillPurchaseOrderRef?,
    @SerializedName("warehouseId") val warehouseId: BillWarehouseRef?,
    @SerializedName("companySnapshot") val companySnapshot: BillCompanySnapshot?,
    @SerializedName("paymentTermId") val paymentTermId: BillPaymentTermRef?,
    @SerializedName("lines") val lines: List<ProcurementBillDetailLine> = emptyList(),
    @SerializedName("createdBy") val createdBy: BillUserRef?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class BillPaymentTermRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("termType") val termType: String?,
    @SerializedName("dueDays") val dueDays: Int?
)

data class BillUserRef(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("memberId") val memberId: String?
)

data class ProcurementBillDetailLine(
    @SerializedName("_id") val id: String,
    @SerializedName("lineType") val lineType: String?,
    @SerializedName("hsnCode") val hsnCode: String?,
    @SerializedName("sacCode") val sacCode: String?,
    @SerializedName("itemDescription") val itemDescription: String?,
    @SerializedName("quantity") val quantity: Double = 0.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("discountPercent") val discountPercent: Double = 0.0,
    @SerializedName("taxableAmount") val taxableAmount: Double = 0.0,
    @SerializedName("totalTax") val totalTax: Double = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double = 0.0,
    @SerializedName("itemId") val item: BillLineItemRef?,
    @SerializedName("expenseAccountId") val expenseAccount: BillExpenseAccountRef?,
    @SerializedName("taxBreakdown") val taxBreakdown: List<BillTaxBreakdownItem> = emptyList()
)

data class BillLineItemRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String?,
    @SerializedName("hsnCode") val hsnCode: String?
)

data class BillExpenseAccountRef(
    @SerializedName("_id") val id: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountCode") val accountCode: String?
)

data class BillTaxBreakdownItem(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("amount") val amount: Double = 0.0
)

//sent and void api

//data class VoidBillRequest(
//    @SerializedName("reason") val reason: String
//)
//
//data class VoidBillResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: ProcurementBillDetailData?
//)
//
//data class SendBillResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: ProcurementBillDetailData?
//)

data class VoidBillRequest(
    @SerializedName("reason") val reason: String
)

data class VoidBillResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: JsonElement? = null
)

data class SendBillResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: JsonElement? = null
)

//record payment
data class RecordPaymentRequest(
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("billId") val billId: String,
    @SerializedName("paymentDate") val paymentDate: String, // Format: yyyy-MM-dd
    @SerializedName("amount") val amount: Double,
    @SerializedName("paymentMode") val paymentMode: String,
    @SerializedName("referenceNumber") val referenceNumber: String = "",
    @SerializedName("paidFromAccountId") val paidFromAccountId: String,
    @SerializedName("notes") val notes: String = ""
)

data class RecordPaymentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: BillPaymentData? = null
)

data class BillPaymentData(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("branchId") val branchId: String?,
    @SerializedName("supplierId") val supplierId: String?,
    @SerializedName("billId") val billId: String?,
    @SerializedName("paymentNumber") val paymentNumber: String?,
    @SerializedName("paymentDate") val paymentDate: String?,
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("paymentMode") val paymentMode: String?,
    @SerializedName("referenceNumber") val referenceNumber: String?,
    @SerializedName("paidFromAccountId") val paidFromAccountId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("journalEntryId") val journalEntryId: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

