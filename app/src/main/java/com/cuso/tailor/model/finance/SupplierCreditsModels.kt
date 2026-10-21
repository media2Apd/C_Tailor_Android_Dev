package com.cuso.tailor.model.finance

import com.cuso.tailor.model.sales.PaginationInfo
import com.google.gson.annotations.SerializedName

data class VendorCreditListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: List<VendorCreditItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: PaginationInfo? = null
)

data class VendorCreditItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: Any? = null,
    @SerializedName("supplierSnapshot") val supplierSnapshot: VendorCreditSupplierSnapshot? = null,
    @SerializedName("supplierId") val supplierId: Any? = null,
    @SerializedName("billId") val billId: Any? = null,
    @SerializedName("vendorCreditNumber") val vendorCreditNumber: String = "",
    @SerializedName("supplierCreditReference") val supplierCreditReference: String = "",
    @SerializedName("vendorCreditDate") val vendorCreditDate: String = "",
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("remainingBalance") val remainingBalance: Double = 0.0,
    @SerializedName("status") val status: String = "Open",
    @SerializedName("lines") val lines: List<VendorCreditLineItemDto> = emptyList(),
    @SerializedName("notes") val notes: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class VendorCreditSupplierSnapshot(
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = ""
)

data class VendorCreditLineItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("itemDescription") val itemDescription: String? = null,
    @SerializedName("quantity") val quantity: Double = 0.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double = 0.0
)