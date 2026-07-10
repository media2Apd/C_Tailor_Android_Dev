package com.cuso.mobile.model

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