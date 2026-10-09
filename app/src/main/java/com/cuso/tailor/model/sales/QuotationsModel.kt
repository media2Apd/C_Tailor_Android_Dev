package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// 1. API RESPONSE WRAPPERS
// ─────────────────────────────────────────────────────────────

data class QuotationListResponse(
    val success: Boolean = false,
    val data: List<QuotationItemDto> = emptyList(),
    val pagination: QuotationPaginationDto? = null,
    val message: String? = null
)

data class QuotationPaginationDto(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    @SerializedName("totalPages") val pages: Int = 1
)

data class QuotationDetailResponse(
    val success: Boolean = false,
    val data: QuotationItemDto? = null,
    val message: String? = null
)

data class QuotationDeleteResponse(
    val success: Boolean = false,
    val message: String? = null
)

data class CreateQuotationResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: QuotationCreatedData? = null
)

data class QuotationCreatedData(
    @SerializedName("_id") val id: String = "",
    val organizationId: String? = null,
    val branchId: String? = null,
    @SerializedName("quotationCode") val quotationNumber: String? = null,
    val customerId: com.google.gson.JsonElement? = null,
    val leadId: String? = null,
    val items: List<QuotationLineItemDto> = emptyList(),
    @SerializedName("subtotal") val subTotal: Double = 0.0,
    @SerializedName("totalTax") val taxAmount: Double = 0.0,
    @SerializedName("totalDiscount") val discountAmount: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val grandTotal: Double = 0.0,
    val status: String = "Draft",
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ─────────────────────────────────────────────────────────────
// 2. GET API RESPONSE DTO MODELS (VIEW-ONE & VIEW-ALL)
// ─────────────────────────────────────────────────────────────

data class QuotationItemDto(
    @SerializedName("_id") val id: String = "",
    val organizationId: String? = null,
    val branchId: String? = null,
    @SerializedName("quotationCode")
    val quotationNumber: String? = null,

    val version: Int = 1,
    val quotationDate: String? = null,
    val expiryDate: String? = null,
    val leadId: QuotationLeadDto? = null,
    val customerId: CustomerRefDto? = null,
    val salespersonId: QuotationSalespersonRefDto? = null,

    val customerSnapshot: QuotationCustomerSnapshotDto? = null,
    val items: List<QuotationLineItemDto> = emptyList(),

    @SerializedName("subtotal")
    val subTotal: Double = 0.0,

    @SerializedName("totalTax")
    val taxAmount: Double = 0.0,

    @SerializedName("totalDiscount")
    val discountAmount: Double = 0.0,

    val deliveryCharge: Double = 0.0,
    val grandTotal: Double = 0.0,
    val status: String = "Draft",
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val totalDiscount: Double get() = discountAmount
}

data class QuotationSalespersonRefDto(
    @SerializedName("_id") val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)

data class QuotationLeadDto(
    @SerializedName("_id") val id: String? = null,
    val leadCode: String? = null,
    val fullName: String? = null,
    val mobileNumber: String? = null,
    val email: String? = null
)

data class CustomerRefDto(
    @SerializedName("_id") val id: String? = null,
    val customerCode: String? = null,
    val fullName: String? = null,
    val mobileNumber: String? = null,
    val email: String? = null
)

data class QuotationCustomerSnapshotDto(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null
)

data class TaxGroupRefDto(
    @SerializedName("_id") val id: String? = null,
    val name: String? = null,
    val totalRate: Double? = 0.0,
    val isCompound: Boolean = false
)

data class TaxBreakdownDto(
    val taxRateId: String? = null,
    val name: String? = null,
    val rate: Double? = 0.0,
    val taxAccountId: String? = null,
    val amount: Double? = 0.0,
    @SerializedName("_id") val id: String? = null
)

data class QuotationLineItemDto(
    @SerializedName("_id") val id: String? = null,
    val lineType: String? = null,
    val parentLineId: String? = null,
    val itemDescription: String? = null,
    val sacCode: String? = null,
    val hsnCode: String? = null,
    val customGarment: CustomGarmentDetailDto? = null,
    val product: ProductDetailDto? = null,
    val addonWork: AddonWorkDetailDto? = null,
    val quantity: Double = 1.0,
    val unit: String? = null,
    val unitPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val isTaxable: Boolean = true,
    val taxGroupId: TaxGroupRefDto? = null,
    val taxableAmount: Double = 0.0,
    val taxBreakdown: List<TaxBreakdownDto> = emptyList(),
    val taxAmount: Double = 0.0,
    @SerializedName("lineTotal") val totalPrice: Double = 0.0,

    val garmentCategoryId: String? = null,
    val fabric: FabricDetail? = null,
    val design: DesignDetail? = null,
    val addons: List<AddonDetail> = emptyList()
)

data class CustomGarmentDetailDto(
    val segmentId: GarmentRefDto? = null,
    val segmentName: String? = null,
    val garmentId: GarmentRefDto? = null,
    val garmentName: String? = null,
    val garmentCategoryId: GarmentRefDto? = null,
    val categoryDisplayName: String? = null,
    val designId: GarmentRefDto? = null,
    val designName: String? = null,
    val stitchingType: String? = null,
    val fabricSource: String? = null,
    val fabricNotes: String? = null,
    val specialInstructions: String? = null
)

data class GarmentRefDto(
    @SerializedName("_id") val id: String? = null,
    val name: String? = null,
    val displayName: String? = null,
    val sku: String? = null
)

data class WorkPricingRefDto(
    @SerializedName("_id") val id: String? = null,
    val workType: String? = null,
    val unit: String? = null,
    val basePrice: Double? = 0.0
)

data class AddonWorkDetailDto(
    val workPricingId: WorkPricingRefDto? = null,
    val workType: String? = null,
    val specialInstructions: String? = null
)

data class ProductDetailDto(
    val inventoryItemId: GarmentRefDto? = null,
    val sku: String? = null,
    val itemName: String? = null
)

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

// ─────────────────────────────────────────────────────────────
// 3. POST / CREATE QUOTATION REQUEST PAYLOAD MODELS
// ─────────────────────────────────────────────────────────────

data class CreateQuotationRequest(
    @SerializedName("branchId") val branchId: String,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("leadId") val leadId: String? = null,
    @SerializedName("customerSnapshot") val customerSnapshot: QuotationCustomerSnapshotPayload,
    @SerializedName("salespersonId") val salespersonId: String,
    @SerializedName("quotationDate") val quotationDate: String,
    @SerializedName("expiryDate") val expiryDate: String,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("exchangeRate") val exchangeRate: Int = 1,
    @SerializedName("status") val status: String = "Draft",
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("totalTax") val totalTax: Double,
    @SerializedName("totalDiscount") val totalDiscount: Double = 0.0,
    @SerializedName("deliveryCharge") val deliveryCharge: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double,
    @SerializedName("notes") val notes: String = "",
    @SerializedName("items") val items: List<QuotationPayloadItem>
)

data class QuotationCustomerSnapshotPayload(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String = ""
)

data class QuotationPayloadItem(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("parentLineId") val parentLineId: String? = null,
    @SerializedName("lineType") val lineType: String,
    @SerializedName("itemDescription") val itemDescription: String,
    @SerializedName("isTaxable") val isTaxable: Boolean = true,
    @SerializedName("taxGroupId") val taxGroupId: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unit") val unit: String,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("discountAmount") val discountAmount: Double = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double,
    @SerializedName("hsnCode") val hsnCode: String? = null,
    @SerializedName("sacCode") val sacCode: String? = null,
    @SerializedName("customGarment") val customGarment: CustomGarmentPayload? = null,
    @SerializedName("product") val product: ProductPayload? = null,
    @SerializedName("addonWork") val addonWork: AddonWorkPayload? = null
)

data class CustomGarmentPayload(
    @SerializedName("segmentId") val segmentId: String,
    @SerializedName("segmentName") val segmentName: String,
    @SerializedName("garmentId") val garmentId: String,
    @SerializedName("garmentName") val garmentName: String,
    @SerializedName("garmentCategoryId") val garmentCategoryId: String,
    @SerializedName("categoryDisplayName") val categoryDisplayName: String,
    @SerializedName("designId") val designId: String? = null,
    @SerializedName("designName") val designName: String? = null,
    @SerializedName("sizeStandard") val sizeStandard: String = "36",
    @SerializedName("stitchingType") val stitchingType: String = "Normal Machine",
    @SerializedName("fabricSource") val fabricSource: String = "Store_Fabric",
    @SerializedName("fabricNotes") val fabricNotes: String = "",
    @SerializedName("specialInstructions") val specialInstructions: String = ""
)

data class AddonWorkPayload(
    @SerializedName("workPricingId") val workPricingId: String,
    @SerializedName("workType") val workType: String,
    @SerializedName("specialInstructions") val specialInstructions: String = ""
)

data class ProductPayload(
    @SerializedName("inventoryItemId") val inventoryItemId: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("itemName") val itemName: String
)

data class CustomerSnapshotAddress(
    val addressLine: String = "",
    val city: String = "",
    val pincode: String = ""
)

data class CustomerSnapshot(
    val name: String,
    val phone: String,
    val email: String = "",
    val address: CustomerSnapshotAddress = CustomerSnapshotAddress()
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

data class UpdateQuotationResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: QuotationItemDto? = null
)