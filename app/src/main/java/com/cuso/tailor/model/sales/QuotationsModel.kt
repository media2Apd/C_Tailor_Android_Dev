package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName

//// ── Response wrapper ──
//data class QuotationListResponse(
//    val success: Boolean,
//    val data: List<QuotationItemDto> = emptyList(),
//    val pagination: QuotationPaginationDto? = null,
//    val message: String? = null
//)
//
//data class QuotationPaginationDto(
//    val total: Int = 0,
//    val page: Int = 1,
//    val limit: Int = 10,
//    val pages: Int = 1
//)
//
//data class QuotationDetailResponse(
//    val success: Boolean,
//    val data: QuotationItemDto? = null,
//    val message: String? = null
//)
//
//// ── Single quotation record ──
//data class QuotationItemDto(
//    @SerializedName("_id") val id: String,
//    val organizationId: String? = null,
//    val quotationNumber: String,
//    val parentQuotationId: String? = null,
//    val revision: Int = 1,
//    val isLatest: Boolean = true,
//    val revisedFrom: String? = null,
//    val leadId: String? = null,
//    val customerId: String? = null,
//    val orderId: String? = null,
//    val customerSnapshot: QuotationCustomerSnapshotDto? = null,
//    val items: List<QuotationLineItemDto> = emptyList(),
//    val subTotal: Double = 0.0,
//    val taxPercent: Double = 0.0,
//    val taxAmount: Double = 0.0,
//    val discountAmount: Double = 0.0,
//    val grandTotal: Double = 0.0,
//    val status: String = "draft",
//    val notes: String? = null,
//    val createdAt: String? = null,
//    val updatedAt: String? = null
//)
//
//data class QuotationCustomerSnapshotDto(
//    val name: String? = null,
//    val phone: String? = null,
//    val email: String? = null
//)
//
//data class QuotationLineItemDto(
//    val garmentCategoryId: String? = null,
//    val garmentName: String? = null,
//    val quantity: Int = 0,
//    val basePrice: Double = 0.0,
//    val fabric: FabricDetail? = null,
//    val design: DesignDetail? = null,
//    val addons: List<AddonDetail> = emptyList(),
//    val expressCharge: Double = 0.0,
//    val unitPrice: Double = 0.0,
//    val totalPrice: Double = 0.0
//)

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
/**
 * Main Quotation Request payload matching backend schema.
 */
data class CreateQuotationRequest(
    @SerializedName("branchId") val branchId: String,
    @SerializedName("customerId") val customerId: String,
    @SerializedName("salespersonId") val salespersonId: String,
    @SerializedName("quotationDate") val quotationDate: String,
    @SerializedName("expiryDate") val expiryDate: String,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("notes") val notes: String = "",
    @SerializedName("items") val items: List<QuotationPayloadItem>,
    @SerializedName("deliveryCharge") val deliveryCharge: Double = 0.0
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
    val phone: String,              //  "phone" — NOT "mobile"
    val email: String = "",
    val address: CustomerSnapshotAddress = CustomerSnapshotAddress()
)


// ── Response wrapper ──
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

// ── Single quotation record ──
data class QuotationItemDto(
    @SerializedName("_id") val id: String = "",
    val organizationId: String? = null,
    val branchId: String? = null,
    @SerializedName("quotationCode")
    val quotationNumber: String? = null,

    val version: Int = 1,
    val quotationDate: String? = null,
    val expiryDate: String? = null,
    val leadId: String? = null,
    val customerId: CustomerRefDto? = null,

    val customerSnapshot: QuotationCustomerSnapshotDto? = null,
    val items: List<QuotationLineItemDto> = emptyList(),

    // API field: "subtotal"
    @SerializedName("subtotal")
    val subTotal: Double = 0.0,

    // API field: "totalTax"
    @SerializedName("totalTax")
    val taxAmount: Double = 0.0,

    // API field: "totalDiscount"
    @SerializedName("totalDiscount")
    val discountAmount: Double = 0.0,

    val deliveryCharge: Double = 0.0,
    val grandTotal: Double = 0.0,
    val status: String = "Draft",
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
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

//data class QuotationLineItemDto(
//    val lineType: String? = null,
//    val itemDescription: String? = null,
//    val customGarment: CustomGarmentDetailDto? = null,
//    val quantity: Double = 0.0,
//    val unit: String? = null,
//    val unitPrice: Double = 0.0,
//    val discountAmount: Double = 0.0,
//    val taxableAmount: Double = 0.0,
//    val taxAmount: Double = 0.0,
//    @SerializedName("lineTotal") val totalPrice: Double = 0.0
//)
//
//data class CustomGarmentDetailDto(
//    val segmentName: String? = null,
//    val garmentName: String? = null,
//    val categoryDisplayName: String? = null,
//    val designName: String? = null,
//    val stitchingType: String? = null,
//    val fabricNotes: String? = null,
//    val specialInstructions: String? = null
//)

data class QuotationLineItemDto(
    @SerializedName("_id") val id: String? = null,
    val lineType: String? = null,
    val parentLineId: String? = null,
    val itemDescription: String? = null,
    val customGarment: CustomGarmentDetailDto? = null,
    val addonWork: AddonWorkDetailDto? = null,
    val quantity: Double = 1.0,
    val unit: String? = null,
    val unitPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxableAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    @SerializedName("lineTotal") val totalPrice: Double = 0.0,

    // Backward-compatible properties
    val garmentCategoryId: String? = null,
    val fabric: FabricDetail? = null,
    val design: DesignDetail? = null,
    val addons: List<AddonDetail> = emptyList()
)

data class CustomGarmentDetailDto(
    val segmentName: String? = null,
    val garmentName: String? = null,
    val garmentCategoryId: GarmentRefDto? = null,
    val categoryDisplayName: String? = null,
    val designId: GarmentRefDto? = null,
    val designName: String? = null,
    val stitchingType: String? = null,
    val fabricNotes: String? = null,
    val specialInstructions: String? = null
)

data class GarmentRefDto(
    @SerializedName("_id") val id: String? = null,
    val name: String? = null,
    val displayName: String? = null,
    val sku: String? = null
)

data class AddonWorkDetailDto(
    val workType: String? = null,
    val specialInstructions: String? = null
)



data class QuotationPayloadItem(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("parentLineId") val parentLineId: String? = null,
    @SerializedName("lineType") val lineType: String, // "Custom_Garment" | "Garment_Material" | "Garment_Addon"
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

data class ProductPayload(
    @SerializedName("inventoryItemId") val inventoryItemId: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("itemName") val itemName: String
)

data class AddonWorkPayload(
    @SerializedName("workPricingId") val workPricingId: String,
    @SerializedName("workType") val workType: String,
    @SerializedName("specialInstructions") val specialInstructions: String = ""
)