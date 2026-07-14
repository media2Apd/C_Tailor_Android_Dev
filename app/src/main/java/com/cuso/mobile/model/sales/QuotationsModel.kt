package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

// ── Response wrapper ──
data class QuotationListResponse(
    val success: Boolean,
    val data: List<QuotationItemDto> = emptyList(),
    val pagination: QuotationPaginationDto? = null,
    val message: String? = null
)

data class QuotationPaginationDto(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val pages: Int = 1
)

data class QuotationDetailResponse(
    val success: Boolean,
    val data: QuotationItemDto? = null,
    val message: String? = null
)

// ── Single quotation record ──
data class QuotationItemDto(
    @SerializedName("_id") val id: String,
    val organizationId: String? = null,
    val quotationNumber: String,
    val parentQuotationId: String? = null,
    val revision: Int = 1,
    val isLatest: Boolean = true,
    val revisedFrom: String? = null,
    val leadId: String? = null,
    val customerId: String? = null,
    val orderId: String? = null,
    val customerSnapshot: QuotationCustomerSnapshotDto? = null,
    val items: List<QuotationLineItemDto> = emptyList(),
    val subTotal: Double = 0.0,
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val status: String = "draft",
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class QuotationCustomerSnapshotDto(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null
)

data class QuotationLineItemDto(
    val garmentCategoryId: String? = null,
    val garmentName: String? = null,
    val quantity: Int = 0,
    val basePrice: Double = 0.0,
    val fabric: FabricDetail? = null,
    val design: DesignDetail? = null,
    val addons: List<AddonDetail> = emptyList(),
    val expressCharge: Double = 0.0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
)

// Add these to your model file

data class FabricDetail(
    val label: String = "",
    val price: Double = 0.0
)

data class DesignDetail(
    val label: String = "",
    val price: Double = 0.0
)

data class AddonDetail(
    val label: String = "",
    val price: Double = 0.0
)


data class QuotationDeleteResponse(
    val success: Boolean,
    val message: String? = null
)


// ── Request ──
data class CreateQuotationRequest(
    val customerId: String,
    val leadId: String? = null,
    val customerSnapshot: CustomerSnapshot,   // ➕ ADD THIS LINE
    val items: List<QuotationItemInput>,
    val subTotal: Double,
    val taxPercent: Double,
    val taxAmount: Double,
    val discountAmount: Double = 0.0,
    val grandTotal: Double,
    val status: String = "draft",
    val notes: String = ""
)

data class QuotationItemInput(
    val garmentCategoryId: String,
    val garmentName: String,
    val quantity: Int,
    val basePrice: Double,
    val fabric: QuotationOptionInput? = null,
    val design: QuotationOptionInput? = null,
    val addons: List<QuotationOptionInput> = emptyList(),
    val expressCharge: Double = 0.0,
    val unitPrice: Double,
    val totalPrice: Double
)

data class QuotationOptionInput(
    val label: String,
    val price: Double
)

// ── Response ──
data class CreateQuotationResponse(
    val success: Boolean,
    val data: QuotationCreatedData?
)

data class QuotationCreatedData(
    val organizationId: String,
    val quotationNumber: String,
    val _id: String,
    val customerId: String,
    val items: List<QuotationItemInput>,
    val subTotal: Double,
    val taxPercent: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val grandTotal: Double,
    val status: String,
    val notes: String,
    val createdAt: String,
    val updatedAt: String
)

data class CustomerSnapshotAddress(
    val addressLine: String = "",
    val city: String = "",
    val pincode: String = ""
)

data class CustomerSnapshot(
    val name: String,
    val phone: String,              // ⚠️ "phone" — NOT "mobile"
    val email: String = "",
    val address: CustomerSnapshotAddress = CustomerSnapshotAddress()
)