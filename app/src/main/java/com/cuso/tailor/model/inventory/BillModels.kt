package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

/**
 * Request payload for creating a new purchase bill.
 * Contains both the new dynamic payload fields and legacy fields with defaults.
 */
data class CreateBillRequest(
    @SerializedName("billDate") val billDate: String? = null,
    @SerializedName("defaultExpenseAccountId") val defaultExpenseAccountId: String? = null,
    @SerializedName("defaultTaxGroupId") val defaultTaxGroupId: String? = null,
    @SerializedName("extraLines") val extraLines: List<ExtraBillLineRequest> = emptyList(),
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("paymentTermId") val paymentTermId: String? = null,
    @SerializedName("priceIncludesTax") val priceIncludesTax: Boolean = false,
    @SerializedName("receiveIds") val receiveIds: List<String> = emptyList(),
    @SerializedName("selectedReceiveItems") val selectedReceiveItems: List<SelectedReceiveItemRequest> = emptyList(),
    @SerializedName("supplierBillReference") val supplierBillReference: String? = null,
    @SerializedName("supplierId") val supplierId: String? = null,
    @SerializedName("warehouseId") val warehouseId: String? = null,

    // Legacy fields with defaults to ensure full backward compatibility
    @SerializedName("purchaseOrderId") val purchaseOrderId: String? = null,
    @SerializedName("billNumber") val billNumber: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("exchangeRate") val exchangeRate: Double = 1.0,
    @SerializedName("lines") val lines: List<BillLineRequest> = emptyList(),
    @SerializedName("totalDiscount") val totalDiscount: Double = 0.0
)

data class SelectedReceiveItemRequest(
    @SerializedName("receiveId") val receiveId: String,
    @SerializedName("receiveItemId") val receiveItemId: String
)

data class ExtraBillLineRequest(
    @SerializedName("lineType") val lineType: String = "Service",
    @SerializedName("itemDescription") val itemDescription: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("rate") val rate: Double,
    @SerializedName("discountPercent") val discountPercent: Double = 0.0,
    @SerializedName("expenseAccountId") val expenseAccountId: String,
    @SerializedName("taxGroupId") val taxGroupId: String
)

data class BillLineRequest(
    @SerializedName("lineType") val lineType: String = "Product",
    @SerializedName("itemId") val itemId: String? = null,
    @SerializedName("purchaseReceiveId") val purchaseReceiveId: String? = null,
    @SerializedName("purchaseReceiveItemId") val purchaseReceiveItemId: String? = null,
    @SerializedName("itemDescription") val itemDescription: String? = null,
    @SerializedName("quantity") val quantity: Double = 0.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("discountPercent") val discountPercent: Double = 0.0,
    @SerializedName("taxGroupId") val taxGroupId: String? = null,
    @SerializedName("expenseAccountId") val expenseAccountId: String? = null
)

/**
 * Response received after creating a bill.
 */
data class CreateBillResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: BillResponseData,
    @SerializedName("message") val message: String? = null
)

data class BillResponseData(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("warehouseId") val warehouseId: String? = null,
    @SerializedName("supplierId") val supplierId: String? = null,
    @SerializedName("purchaseOrderId") val purchaseOrderId: String? = null,
    @SerializedName("billNumber") val billNumber: String,
    @SerializedName("supplierBillReference") val supplierBillReference: String? = null,
    @SerializedName("billDate") val billDate: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("currency") val currency: String? = "INR",
    @SerializedName("exchangeRate") val exchangeRate: Double? = 1.0,
    @SerializedName("priceIncludesTax") val priceIncludesTax: Boolean = false,
    @SerializedName("lines") val lines: List<BillLineResponse> = emptyList(),
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("totalTax") val totalTax: Double = 0.0,
    @SerializedName("totalDiscount") val totalDiscount: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("amountPaid") val amountPaid: Double = 0.0,
    @SerializedName("balanceDue") val balanceDue: Double = 0.0,
    @SerializedName("status") val status: String = "Draft",
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class BillLineResponse(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("lineType") val lineType: String? = "Product",
    @SerializedName("itemId") val itemId: String? = null,
    @SerializedName("purchaseReceiveId") val purchaseReceiveId: String? = null,
    @SerializedName("purchaseReceiveItemId") val purchaseReceiveItemId: String? = null,
    @SerializedName("itemDescription") val itemDescription: String? = null,
    @SerializedName("quantity") val quantity: Double = 0.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("discountPercent") val discountPercent: Double = 0.0,
    @SerializedName("taxableAmount") val taxableAmount: Double = 0.0,
    @SerializedName("totalTax") val totalTax: Double = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double = 0.0
)

/**
 * Payment Terms API response and DTOs.
 */
data class PaymentTermsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<PaymentTermDto> = emptyList(),
    @SerializedName("pagination") val pagination: PaymentTermPagination? = null
)

data class PaymentTermDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("termType") val termType: String? = null,
    @SerializedName("dueDays") val dueDays: Int = 0,
    @SerializedName("eomBufferDays") val eomBufferDays: Int = 0,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("isActive") val isActive: Boolean = true
)

data class PaymentTermPagination(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1
)

/**
 * Tax Groups API response and DTOs.
 */
data class TaxGroupsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<TaxGroupDto> = emptyList(),
    @SerializedName("pagination") val pagination: TaxGroupPagination? = null
)

data class TaxGroupDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("totalRate") val totalRate: Double = 0.0,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("taxRates") val taxRates: List<TaxRateDto> = emptyList()
)

data class TaxRateDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxType") val taxType: String? = null,
    @SerializedName("taxSubType") val taxSubType: String? = null
)

data class TaxGroupPagination(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1
)