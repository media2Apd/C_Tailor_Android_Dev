@file:Suppress("unused")

package com.cuso.tailor.model.inventory

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

data class PurchaseOrderDetailResponse(
    @field:SerializedName("success") val success: Boolean? = null,
    @field:SerializedName("data") val data: PurchaseOrderDetailData? = null
)

data class PurchaseOrderDetailData(
    @field:SerializedName("_id") val id: String? = null,
    @field:SerializedName("organizationId") val organizationId: String? = null,
    @field:SerializedName("poNumber") val poNumber: String? = null,
    @field:SerializedName("requisitionId") val requisitionId: String? = null,
    @field:SerializedName("supplierId") val supplier: PODetailSupplier? = null,
    @field:SerializedName("poDate") val poDate: String? = null,
    @field:SerializedName("eta") val eta: String? = null,
    @field:SerializedName("currency") val currency: String? = null,
    @field:SerializedName("warehouseId") val warehouse: PODetailWarehouse? = null,
    @field:SerializedName("poType") val poType: String? = null,
    @field:SerializedName("items") val items: List<PODetailItem>? = null,
    @field:SerializedName("subtotal") val subtotal: Double? = null,
    @field:SerializedName("taxTotal") val taxTotal: Double? = null,
    @field:SerializedName("discount") val discount: Double? = null,
    @field:SerializedName("shippingCost") val shippingCost: Double? = null,
    @field:SerializedName("grandTotal") val grandTotal: Double? = null,
    @field:SerializedName("orderStatus") val orderStatus: String? = null,
    @field:SerializedName("advancePaid") val advancePaid: Double? = null,
    @field:SerializedName("availableAdvance") val availableAdvance: Double? = null,
    @field:SerializedName("receiveStatus") val receiveStatus: String? = null,
    @field:SerializedName("billStatus") val billStatus: String? = null,
    @field:SerializedName("paymentStatus") val paymentStatus: String? = null,
    @field:SerializedName("lifecycleStatus") val lifecycleStatus: String? = null,
    @field:SerializedName("shippingMethod") val shippingMethod: String? = null,
    @field:SerializedName("transportName") val transportName: String? = null,
    @field:SerializedName("vehicleNumber") val vehicleNumber: String? = null,
    @field:SerializedName("trackingNumber") val trackingNumber: String? = null,
    @field:SerializedName("freightTerms") val freightTerms: String? = null,
    @field:SerializedName("internalNotes") val internalNotes: String? = null,
    @field:SerializedName("attachments") val attachments: List<String>? = null,
    @field:SerializedName("remarks") val remarks: String? = null,
    @field:SerializedName("createdBy") val createdBy: String? = null,
    @field:SerializedName("isDeleted") val isDeleted: Boolean? = null,
    @field:SerializedName("createdAt") val createdAt: String? = null,
    @field:SerializedName("updatedAt") val updatedAt: String? = null
)

data class PODetailSupplier(
    @field:SerializedName("_id") val id: String? = null,
    @field:SerializedName("name") val name: String? = null,
    @field:SerializedName("supplierCode") val supplierCode: String? = null,
    @field:SerializedName("complianceStatus") val complianceStatus: String? = null,
    @field:SerializedName("contact") val contact: PODetailSupplierContact? = null,
    @field:SerializedName("address") val address: PODetailSupplierAddressContainer? = null,
    @field:SerializedName("tax") val tax: PODetailSupplierTax? = null
)

data class PODetailSupplierContact(
    @field:SerializedName("contactName") val contactName: String? = null,
    @field:SerializedName("email") val email: String? = null,
    @field:SerializedName("phone") val phone: String? = null,
    @field:SerializedName("alternatePhone") val alternatePhone: String? = null,
    @field:SerializedName("website") val website: String? = null
)

data class PODetailSupplierAddressContainer(
    @field:SerializedName("billing") val billing: PODetailAddress? = null,
    @field:SerializedName("shipping") val shipping: PODetailAddress? = null,
    @field:SerializedName("sameAsBilling") val sameAsBilling: Boolean? = null
)

data class PODetailAddress(
    @field:SerializedName("flatNo") val flatNo: String? = null,
    @field:SerializedName("street") val street: String? = null,
    @field:SerializedName("city") val city: String? = null,
    @field:SerializedName("state") val state: String? = null,
    @field:SerializedName("stateCode") val stateCode: String? = null,
    @field:SerializedName("country") val country: String? = null,
    @field:SerializedName("pincode") val pincode: String? = null
)

data class PODetailSupplierTax(
    @field:SerializedName("gstNumber") val gstNumber: String? = null,
    @field:SerializedName("gstType") val gstType: String? = null,
    @field:SerializedName("pan") val pan: String? = null,
    @field:SerializedName("defaultPurchaseTax") val defaultPurchaseTax: String? = null,
    @field:SerializedName("reverseCharge") val reverseCharge: Boolean? = null,
    @field:SerializedName("tdsApplicable") val tdsApplicable: Boolean? = null
)

data class PODetailWarehouse(
    @field:SerializedName("_id") val id: String? = null,
    @field:SerializedName("name") val name: String? = null,
    @field:SerializedName("code") val code: String? = null,
    @field:SerializedName("contactPerson") val contactPerson: String? = null,
    @field:SerializedName("contactPhone") val contactPhone: String? = null,
    @field:SerializedName("address") val address: PODetailWarehouseAddress? = null
)

data class PODetailWarehouseAddress(
    @field:SerializedName("address") val address: String? = null,
    @field:SerializedName("city") val city: String? = null,
    @field:SerializedName("state") val state: String? = null,
    @field:SerializedName("country") val country: String? = null,
    @field:SerializedName("pincode") val pincode: String? = null
)

data class PODetailItem(
    @field:SerializedName("_id") val id: String? = null,
    @field:SerializedName("itemId") val itemRef: PODetailItemRef? = null,
    @field:SerializedName("qty") val qty: Double? = null,
    @field:SerializedName("receivedQty") val receivedQty: Double? = null,
    @field:SerializedName("billedQty") val billedQty: Double? = null,
    @field:SerializedName("receiveStatus") val receiveStatus: String? = null,
    @field:SerializedName("billStatus") val billStatus: String? = null,
    @field:SerializedName("rate") val rate: Double? = null,
    @field:SerializedName("taxPercent") val taxPercent: Double? = null,
    @field:SerializedName("subtotal") val subtotal: Double? = null,
    @field:SerializedName("taxAmount") val taxAmount: Double? = null,
    @field:SerializedName("total") val total: Double? = null
)

data class PODetailItemRef(
    @field:SerializedName("_id") val id: String? = null,
    @field:SerializedName("name") val name: String? = null,
    @field:SerializedName("sku") val sku: String? = null,
    @field:SerializedName("type") val type: String? = null,
    @field:SerializedName("unit") val unit: String? = null,
    @field:SerializedName("isSerialTracked") val isSerialTracked: Boolean? = null,
    @field:SerializedName("marginPercent") val marginPercent: Double? = null,
    @field:SerializedName("currentStockValue") val currentStockValue: Double? = null
)