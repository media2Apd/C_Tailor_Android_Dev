@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName

data class PurchaseReturnListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("pagination") val pagination: ReturnPaginationDto? = null,
    @SerializedName("data") val data: List<PurchaseReturnDto> = emptyList()
)

data class ReturnPaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

data class PurchaseReturnDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("supplierId") val supplier: ReturnSupplierDto? = null,
    @SerializedName("poId") val po: ReturnPoDto? = null,
    @SerializedName("receiveId") val receive: ReturnReceiveDto? = null,
    @SerializedName("rmaNumber") val rmaNumber: String = "",
    @SerializedName("returnDate") val returnDate: String = "",
    @SerializedName("status") val status: String = "Draft",
    @SerializedName("receiveStatus") val receiveStatus: String = "Not Sent",
    @SerializedName("refundStatus") val refundStatus: String = "Not Refunded",
    @SerializedName("returned") val returned: Boolean = false,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("reason") val reason: String = "",
    @SerializedName("items") val items: List<PurchaseReturnItemDto> = emptyList()
)

data class ReturnSupplierDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("supplierCode") val supplierCode: String = ""
)

data class ReturnPoDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("poNumber") val poNumber: String = ""
)

data class ReturnReceiveDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("receiveNumber") val receiveNumber: String = ""
)

data class PurchaseReturnItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("itemId") val itemId: String = "",
    @SerializedName("qty") val qty: Double = 0.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("reason") val reason: String = ""
)

//
//data class PurchaseReturnDetailResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("data") val data: PurchaseReturnDetailDto?
//)

data class PurchaseReturnDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("supplierId") val supplier: SupplierInfo?,
    @SerializedName("poId") val po: PoInfo?,
    @SerializedName("receiveId") val receive: ReceiveInfo?,
    @SerializedName("rmaNumber") val rmaNumber: String = "",
    @SerializedName("returnDate") val returnDate: String = "",
    @SerializedName("items") val items: List<ReturnItemDto> = emptyList(),
    @SerializedName("reason") val reason: String = "",
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxTotal") val taxTotal: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("status") val status: String = "",
    @SerializedName("receiveStatus") val receiveStatus: String = "",
    @SerializedName("returned") val returned: Boolean = false,
    @SerializedName("refundStatus") val refundStatus: String = "",
    @SerializedName("attachments") val attachments: List<AttachmentDto> = emptyList(),
    @SerializedName("remarks") val remarks: String = "",
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("approvedAt") val approvedAt: String? = null,
    @SerializedName("shippedAt") val shippedAt: String? = null,
    @SerializedName("supplierReceivedAt") val supplierReceivedAt: String? = null
)

data class SupplierInfo(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String = "",
    @SerializedName("supplierCode") val supplierCode: String = "",
    @SerializedName("contact") val contact: ContactInfo?
)

data class ContactInfo(
    @SerializedName("contactName") val contactName: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("alternatePhone") val alternatePhone: String = "",
    @SerializedName("website") val website: String = ""
)

data class PoInfo(
    @SerializedName("_id") val id: String?,
    @SerializedName("poNumber") val poNumber: String = "",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0
)

data class ReceiveInfo(
    @SerializedName("_id") val id: String?,
    @SerializedName("receiveNumber") val receiveNumber: String = "",
    @SerializedName("receiveDate") val receiveDate: String = ""
)

data class ReturnItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("itemId") val item: ItemDetail?,
    @SerializedName("warehouseId") val warehouse: NamedEntity?,
    @SerializedName("binId") val bin: NamedEntity?,
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxAmount") val taxAmount: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("isSerialTracked") val isSerialTracked: Boolean = false,
    @SerializedName("serialNumbers") val serialNumbers: List<String> = emptyList(),
    @SerializedName("reason") val reason: String = ""
)

data class ItemDetail(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String = ""
)

data class NamedEntity(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String = ""
)

data class AttachmentDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("fileUrl") val fileUrl: String = "",
    @SerializedName("publicId") val publicId: String = ""
)


data class PurchaseReturnDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: PurchaseReturnDetailData? = null,
    @SerializedName("message") val message: String? = null
)

data class PurchaseReturnDetailData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("supplierId") val supplier: ReturnSupplierDetail? = null,
    @SerializedName("poId") val po: ReturnPoDetail? = null,
    @SerializedName("receiveId") val receive: ReturnReceiveDetail? = null,
    @SerializedName("rmaNumber") val rmaNumber: String = "",
    @SerializedName("returnDate") val returnDate: String = "",
    @SerializedName("items") val items: List<ReturnItemDetail> = emptyList(),
    @SerializedName("reason") val reason: String = "",
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxTotal") val taxTotal: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("status") val status: String = "",
    @SerializedName("receiveStatus") val receiveStatus: String = "",
    @SerializedName("returned") val returned: Boolean = false,
    @SerializedName("refundStatus") val refundStatus: String = "",
    @SerializedName("linkedVendorCreditId") val linkedVendorCreditId: com.google.gson.JsonElement? = null,
    @SerializedName("attachments") val attachments: List<ReturnAttachment> = emptyList(),
    @SerializedName("remarks") val remarks: String = "",
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = "",
    @SerializedName("approvedAt") val approvedAt: String? = null,
    @SerializedName("shippedAt") val shippedAt: String? = null,
    @SerializedName("supplierReceivedAt") val supplierReceivedAt: String? = null
)

data class ReturnSupplierDetail(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("supplierCode") val supplierCode: String = "",
    @SerializedName("contact") val contact: ReturnSupplierContact? = null
)

data class ReturnSupplierContact(
    @SerializedName("contactName") val contactName: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("alternatePhone") val alternatePhone: String? = null,
    @SerializedName("website") val website: String? = null
)

data class ReturnPoDetail(
    @SerializedName("_id") val id: String = "",
    @SerializedName("poNumber") val poNumber: String = "",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0
)

data class ReturnReceiveDetail(
    @SerializedName("_id") val id: String = "",
    @SerializedName("receiveNumber") val receiveNumber: String = "",
    @SerializedName("receiveDate") val receiveDate: String = ""
)

data class ReturnItemDetail(
    @SerializedName("_id") val id: String = "",
    @SerializedName("itemId") val item: ReturnItemInfo? = null,
    @SerializedName("warehouseId") val warehouse: ReturnNamedEntity? = null,
    @SerializedName("binId") val bin: ReturnNamedEntity? = null,
    @SerializedName("qty") val qty: Double = 0.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxAmount") val taxAmount: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("isSerialTracked") val isSerialTracked: Boolean = false,
    @SerializedName("serialNumbers") val serialNumbers: List<String> = emptyList(),
    @SerializedName("reason") val reason: String = ""
)

data class ReturnItemInfo(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String = ""
)

data class ReturnNamedEntity(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

data class ReturnAttachment(
    @SerializedName("_id") val id: String = "",
    @SerializedName("fileUrl") val fileUrl: String = "",
    @SerializedName("publicId") val publicId: String = ""
)

data class RejectPurchaseReturnRequest(
    @SerializedName("remarks") val rejectionReason: String
)

sealed class PurchaseReturnActionState {
    object Idle : PurchaseReturnActionState()
    object Loading : PurchaseReturnActionState()
    data class Success(val message: String) : PurchaseReturnActionState()
    data class Error(val message: String) : PurchaseReturnActionState()
}


data class GenericReturnActionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null
)

data class PurchaseReturnReceiveItemsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: PurchaseReturnReceiveData? = null,
    @SerializedName("message") val message: String? = null
)

data class PurchaseReturnReceiveData(
    @SerializedName("receiveId") val receiveId: String = "",
    @SerializedName("receiveNumber") val receiveNumber: String = "",
    @SerializedName("poId") val poId: String = "",
    @SerializedName("items") val items: List<ReturnableGrnItem> = emptyList()
)

data class ReturnableGrnItem(
    @SerializedName("receiveItemId") val receiveItemId: String = "",
    @SerializedName("itemId") val itemId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String = "",
    @SerializedName("unit") val unit: String = "Pieces",
    @SerializedName("qtyReceived") val qtyReceived: Double = 0.0,
    @SerializedName("alreadyReturned") val alreadyReturned: Double = 0.0,
    @SerializedName("availableQty") val availableQty: Double = 0.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
    @SerializedName("isSerialTracked") val isSerialTracked: Boolean = false,
    @SerializedName("availableSerials") val availableSerials: List<String> = emptyList()
)

// Payload item model for purchase return line items
data class PurchaseReturnItemPayload(
    @SerializedName("receiveItemId")
    val receiveItemId: String,
    @SerializedName("qty")
    val qty: Double,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("serialNumbers")
    val serialNumbers: List<String> = emptyList()
)

// Response model for purchase return creation
data class CreatePurchaseReturnApiResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: Any? = null
)