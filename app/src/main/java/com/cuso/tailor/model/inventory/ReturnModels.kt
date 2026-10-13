package com.cuso.tailor.model.inventory

import com.cuso.tailor.model.sales.PaginationInfo
import com.google.gson.annotations.SerializedName

data class InventoryReturnsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("pagination") val pagination: PaginationInfo? = null,
    @SerializedName("data") val data: List<InventoryReturnItem> = emptyList()
)

data class InventoryReturnItem(
    @SerializedName("_id", alternate = ["id"]) val _id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("supplierId") val supplier: ReturnSupplier? = null,
    @SerializedName("poId") val po: ReturnPo? = null,
    @SerializedName("receiveId") val receive: ReturnReceive? = null,
    @SerializedName("rmaNumber") val rmaNumber: String = "",
    @SerializedName("returnDate") val returnDate: String = "",
    @SerializedName("reason") val reason: String = "",
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxTotal") val taxTotal: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("status") val status: String = "",
    @SerializedName("receiveStatus") val receiveStatus: String = "",
    @SerializedName("returned") val returned: Boolean = false,
    @SerializedName("refundStatus") val refundStatus: String = "",
    @SerializedName("remarks") val remarks: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
) {
    val id: String get() = _id
}

data class ReturnSupplier(
    @SerializedName("_id", alternate = ["id"]) val _id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("supplierCode") val supplierCode: String = ""
) {
    val id: String get() = _id
}

data class ReturnPo(
    @SerializedName("_id", alternate = ["id"]) val _id: String = "",
    @SerializedName("poNumber") val poNumber: String = ""
) {
    val id: String get() = _id
}

data class ReturnReceive(
    @SerializedName("_id", alternate = ["id"]) val _id: String = "",
    @SerializedName("receiveNumber") val receiveNumber: String = ""
) {
    val id: String get() = _id
}