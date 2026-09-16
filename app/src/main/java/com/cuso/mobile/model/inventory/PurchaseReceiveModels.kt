package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

// ── 1. GET ALL RECEIVES MODELS ──
data class AllReceivesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: ReceivePaginationDto?,
    @SerializedName("data") val data: List<PurchaseReceiveItem> = emptyList()
)

data class ReceivePaginationDto(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class PurchaseReceiveItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("poId") val poId: ReceivePoDto? = null,
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("receiveNumber") val receiveNumber: String,
    @SerializedName("receiveDate") val receiveDate: String,
    @SerializedName("items") val items: List<ReceiveItemDetail> = emptyList(),
    @SerializedName("hasMismatchItems") val hasMismatchItems: Boolean = false,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxTotal") val taxTotal: Double = 0.0,
    @SerializedName("freightCost") val freightCost: Double = 0.0,
    @SerializedName("otherCharges") val otherCharges: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("status") val status: String = "Completed",
    @SerializedName("billingStatus") val billingStatus: String = "Not Billed",
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class ReceivePoDto(
    @SerializedName("_id") val id: String,
    @SerializedName("poNumber") val poNumber: String,
    @SerializedName("supplierId") val supplierId: ReceiveSupplierDto? = null
)

data class ReceiveSupplierDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("contact") val contact: SupplierContactDto? = null
)

data class SupplierContactDto(
    @SerializedName("contactName") val contactName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null
)

data class ReceiveItemDetail(
    @SerializedName("_id") val id: String,
    @SerializedName("itemId") val itemId: ReceiveItemSummaryDto? = null,
    @SerializedName("qtyReceived") val qtyReceived: Int = 0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxAmount") val taxAmount: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("billStatus") val billStatus: String = "Not Billed"
)

data class ReceiveItemSummaryDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String
)

// ── 2. GET RECEIVE HISTORY BY PO MODELS ──
data class ReceiveHistoryByPoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("po") val po: PoHeaderDto? = null,
    @SerializedName("totalReceives") val totalReceives: Int = 0,
    @SerializedName("itemsOverview") val itemsOverview: List<PoItemOverviewDto> = emptyList(),
    @SerializedName("receives") val receives: List<PoReceiveSummaryDto> = emptyList()
)

data class PoHeaderDto(
    @SerializedName("poId") val poId: String,
    @SerializedName("poNumber") val poNumber: String,
    @SerializedName("poDate") val poDate: String,
    @SerializedName("warehouse") val warehouse: String
)

data class PoItemOverviewDto(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("orderedQty") val orderedQty: Int,
    @SerializedName("totalReceivedQty") val totalReceivedQty: Int,
    @SerializedName("rate") val rate: Double,
    @SerializedName("receiveStatus") val receiveStatus: String,
    @SerializedName("percent") val percent: Float
)

data class PoReceiveSummaryDto(
    @SerializedName("_id") val id: String,
    @SerializedName("receiveNumber") val receiveNumber: String,
    @SerializedName("receiveDate") val receiveDate: String,
    @SerializedName("totalQty") val totalQty: Int,
    @SerializedName("grandTotal") val grandTotal: Double,
    @SerializedName("billingStatus") val billingStatus: String,
    @SerializedName("receivedBy") val receivedBy: String? = null
)

// ── 3. SINGLE RECEIVE RESPONSE MODEL ──
data class SingleReceiveResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PurchaseReceiveItem? = null
)

// ── 4. CONVERT TO BILL RESPONSE MODELS ──
data class ConvertToBillResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: BillCreatedData? = null
)

data class BillCreatedData(
    @SerializedName("_id") val id: String,
    @SerializedName("billNumber") val billNumber: String,
    @SerializedName("billDate") val billDate: String,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("supplierId") val supplierId: ReceiveSupplierDto? = null,
    @SerializedName("poId") val poId: ReceivePoDto? = null,
    @SerializedName("items") val items: List<BillItemDetail> = emptyList(),
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxTotal") val taxTotal: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("billStatus") val billStatus: String = "Approved",
    @SerializedName("paymentStatus") val paymentStatus: String = "Unpaid",
    @SerializedName("balanceDue") val balanceDue: Double = 0.0
)

data class BillItemDetail(
    @SerializedName("_id") val id: String,
    @SerializedName("itemId") val itemId: ReceiveItemSummaryDto? = null,
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0
)