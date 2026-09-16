package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

// =============================================================================
// COMMON REUSABLE MODELS
// =============================================================================

data class AttachmentDoc(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("fileUrl") val fileUrl: String,
    @SerializedName("publicId") val publicId: String
)

data class WarehouseRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String? = null
)

data class ItemRefDetail(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("marginPercent") val marginPercent: Double = 0.0
)

// =============================================================================
// 1 & 2. PURCHASE ORDER (CREATE / UPDATE / LIST)
// =============================================================================

data class PurchaseOrderItem(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("itemId") val itemId: Any, // String ID in Create/Update, ItemRefDetail in GetAll
    @SerializedName("qty") val qty: Double,
    @SerializedName("receivedQty") val receivedQty: Double = 0.0,
    @SerializedName("billedQty") val billedQty: Double = 0.0,
    @SerializedName("receiveStatus") val receiveStatus: String = "Not Received",
    @SerializedName("billStatus") val billStatus: String = "Not Billed",
    @SerializedName("rate") val rate: Double,
    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxAmount") val taxAmount: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0
)

data class PurchaseOrder(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("poNumber") val poNumber: String? = null,
    @SerializedName("requisitionId") val requisitionId: String? = null,
    @SerializedName("supplierId") val supplierId: Any? = null,
    @SerializedName("eta") val eta: String? = null,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("warehouseId") val warehouseId: Any? = null, // String or WarehouseRef
    @SerializedName("poType") val poType: String = "Standard",
    @SerializedName("items") val items: List<PurchaseOrderItem> = emptyList(),
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxTotal") val taxTotal: Double = 0.0,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("shippingCost") val shippingCost: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("orderStatus") val orderStatus: String = "Draft",
    @SerializedName("receiveStatus") val receiveStatus: String = "Not Received",
    @SerializedName("billStatus") val billStatus: String = "Not Billed",
    @SerializedName("paymentStatus") val paymentStatus: String = "Unpaid",
    @SerializedName("lifecycleStatus") val lifecycleStatus: String = "Open",
    @SerializedName("attachments") val attachments: List<AttachmentDoc> = emptyList(),
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("poDate") val poDate: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class PurchaseOrderListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: Any? = null,
    @SerializedName("data") val data: List<PurchaseOrder> = emptyList()
)

data class PurchaseOrderSingleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: PurchaseOrder? = null
)

// =============================================================================
// 3. RECEIVE PURCHASE ORDER
// =============================================================================

data class ReceivePOItemRequest(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("itemId") val itemId: String,
    @SerializedName("poItemId") val poItemId: String,
    @SerializedName("isExtraItem") val isExtraItem: Boolean = false,
    @SerializedName("mismatchRemarks") val mismatchRemarks: String? = null,
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("binId") val binId: String,
    @SerializedName("qtyReceived") val qtyReceived: Double,
    @SerializedName("billedQty") val billedQty: Double = 0.0,
    @SerializedName("billStatus") val billStatus: String = "Not Billed",
    @SerializedName("rate") val rate: Double,
    @SerializedName("taxPercent") val taxPercent: Double,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("taxAmount") val taxAmount: Double,
    @SerializedName("total") val total: Double
)

data class ReceivePurchaseOrderRequest(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("poId") val poId: String,
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("receiveNumber") val receiveNumber: String? = null,
    @SerializedName("receiveDate") val receiveDate: String? = null,
    @SerializedName("items") val items: List<ReceivePOItemRequest> = emptyList(),
    @SerializedName("hasMismatchItems") val hasMismatchItems: Boolean = false,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxTotal") val taxTotal: Double = 0.0,
    @SerializedName("freightCost") val freightCost: Double = 0.0,
    @SerializedName("otherCharges") val otherCharges: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("status") val status: String = "Completed",
    @SerializedName("billingStatus") val billingStatus: String = "Not Billed",
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("attachments") val attachments: List<AttachmentDoc> = emptyList()
)

data class PurchaseReceiveResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ReceivePurchaseOrderRequest? = null
)

// =============================================================================
// 4. GET PO FOR BILL CONVERT
// =============================================================================

data class BillConvertItem(
    @SerializedName("poItemId") val poItemId: String,
    @SerializedName("itemId") val itemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("orderedQty") val orderedQty: Double,
    @SerializedName("receivedQty") val receivedQty: Double,
    @SerializedName("billedQty") val billedQty: Double,
    @SerializedName("balanceQty") val balanceQty: Double,
    @SerializedName("rate") val rate: Double,
    @SerializedName("taxPercent") val taxPercent: Double
)

data class SupplierBillInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("contact") val contact: Any? = null,
    @SerializedName("address") val address: Any? = null
)

data class POBillConvertData(
    @SerializedName("poId") val poId: String,
    @SerializedName("poNumber") val poNumber: String,
    @SerializedName("poDate") val poDate: String,
    @SerializedName("supplier") val supplier: SupplierBillInfo? = null,
    @SerializedName("currency") val currency: String,
    @SerializedName("warehouse") val warehouse: WarehouseRef? = null,
    @SerializedName("items") val items: List<BillConvertItem> = emptyList(),
    @SerializedName("branchName") val branchName: String? = null
)

data class POBillConvertResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: POBillConvertData? = null
)