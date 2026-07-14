package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

// ---------- Response wrapper (future API shape) ----------

//data class PricingQuotationResponse(
//    @SerializedName("success")
//    val success: Boolean,
//    @SerializedName("data")
//    val data: PricingQuotationData?
//)

data class PricingQuotationData(
    @SerializedName("stats")
    val stats: PricingStats,
    @SerializedName("categories")
    val categories: List<PricingCategoryItem>
)

// ---------- Top stat cards (Active Quotations, Avg Quote Value, Approval Rate, This Month) ----------

data class PricingStats(
    @SerializedName("activeQuotations")
    val activeQuotations: PricingStatValue,
    @SerializedName("avgQuoteValue")
    val avgQuoteValue: PricingStatValue,
    @SerializedName("approvalRate")
    val approvalRate: PricingStatValue,
    @SerializedName("thisMonth")
    val thisMonth: PricingStatValue
)

data class PricingStatValue(
    @SerializedName("value")
    val value: String,              // e.g. "24", "₹2,450", "78%", "₹58.8k"
    @SerializedName("changePercent")
    val changePercent: Double = 0.0, // e.g. 12.0, 8.0, 5.0, 15.0
    @SerializedName("changeLabel")
    val changeLabel: String = "from last month"
)

// ---------- Pricing category list items ----------

data class PricingCategoryItem(
    @SerializedName("_id")
    val id: String,
    @SerializedName("title")
    val title: String,               // "Garment wise pricing"
    @SerializedName("subtitle")
    val subtitle: String,            // "Pricing based on garment types"
    @SerializedName("basePriceMin")
    val basePriceMin: Double = 0.0,
    @SerializedName("basePriceMax")
    val basePriceMax: Double = 0.0,
    @SerializedName("categoryType")
    val categoryType: String = ""    // "garment" | "fabric" | "design" | "stitching" | "bulk" -> drives icon/route
)




// ── Request ──
data class PricingQuotationSaveRequest(
    @SerializedName("garmentCategory")     val garmentCategory: String,
    @SerializedName("basePrice")           val basePrice: Double,
    @SerializedName("fabricAdjustments")   val fabricAdjustments: List<PriceAdjustmentDto>,
    @SerializedName("designAdjustments")   val designAdjustments: List<PriceAdjustmentDto>,
    @SerializedName("additionalCharges")   val additionalCharges: List<PriceAdjustmentDto>,
    @SerializedName("expressCharge")       val expressCharge: Double,
    @SerializedName("bulkRules")           val bulkRules: List<BulkRuleDto>
)

// ⚠️ ASSUMED field names — fabricAdjustments/designAdjustments/additionalCharges empty-a
// vandhadhaala confirm pannala. "name"+"price" nu assume panniruken.
data class PriceAdjustmentDto(
    @SerializedName("name")  val name: String,
    @SerializedName("price") val price: Double
)

// ⚠️ ASSUMED — bulkRules-um empty-a vandhadhu. "minQuantity"+"discountPercent" nu assume.
data class BulkRuleDto(
    @SerializedName("minQuantity")     val minQuantity: Int,
    @SerializedName("discountPercent") val discountPercent: Double
)

// ── Response ──
data class PricingQuotationSaveResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data")    val data: PricingQuotationDataForSave
)

data class PricingQuotationDataForSave(
    @SerializedName("_id")               val id: String,
    @SerializedName("organizationId")    val organizationId: String,
    @SerializedName("garmentCategory")   val garmentCategory: String,
    @SerializedName("basePrice")         val basePrice: Double,
    @SerializedName("fabricAdjustments") val fabricAdjustments: List<PriceAdjustmentDto>,
    @SerializedName("designAdjustments") val designAdjustments: List<PriceAdjustmentDto>,
    @SerializedName("additionalCharges") val additionalCharges: List<PriceAdjustmentDto>,
    @SerializedName("expressCharge")     val expressCharge: Double,
    @SerializedName("bulkRules")         val bulkRules: List<BulkRuleDto>,
    @SerializedName("isActive")          val isActive: Boolean,
    @SerializedName("createdAt")         val createdAt: String,
    @SerializedName("updatedAt")         val updatedAt: String
)


//PRICING DASHBOARD VIEW ALL


// ── Request body for CREATE (POST) ──
data class GarmentPricingRequest(
    val garmentCategoryId: String,
    val basePrice: Double,
    val fabricAdjustments: List<PriceAdjustmentDto>,
    val designAdjustments: List<PriceAdjustmentDto>,
    val additionalCharges: List<PriceAdjustmentDto>,
    val expressCharge: Double,
    val bulkRules: List<BulkRuleDto>
)

// ── Request body for UPDATE (PUT/PATCH) ──
data class GarmentPricingUpdateRequest(
    val garmentCategoryId: String,
    val basePrice: Double,
    val fabricAdjustments: List<PriceAdjustmentDto>,
    val designAdjustments: List<PriceAdjustmentDto>,
    val additionalCharges: List<PriceAdjustmentDto>,
    val expressCharge: Double,
    val bulkRules: List<BulkRuleDto>
)

//// ── Response after create/update ──
//data class GarmentPricingResponse(
//    val id: String,
//    val itemName: String,
//    val basePrice: Double,
//    val fabricCost: Double,
//    val designCost: Double,
//    val additionalCost: Double,
//    val expressCharge: Double,
//    val totalPrice: Double,
//    val applicableGarment: Int,
//    val status: Boolean
//)

// ── Wrapper matching your API's { success, data } shape ──
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)

// ── Dashboard list item (matches the JSON you shared) ──
data class GarmentPricingListItemDto(
    val id: String,
    val itemName: String,
    val basePrice: Double,
    val fabricCost: Double,
    val designCost: Double,
    val additionalCost: Double,
    val expressCharge: Double,
    val totalPrice: Double,
    val applicableGarment: Int,
    val status: Boolean
)


data class GarmentPricingDetailDto(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("garmentCategory")
    val applicableGarmentId: String = "",

    val basePrice: Double = 0.0,

    val fabricAdjustments: List<PriceAdjustmentDto> = emptyList(),
    val designAdjustments: List<PriceAdjustmentDto> = emptyList(),
    val additionalCharges: List<PriceAdjustmentDto> = emptyList(),
    val expressCharge: Double = 0.0,
    val bulkRules: List<BulkRuleDto> = emptyList(),

    @SerializedName("isActive")
    val status: Boolean = true,

    // ✅ Backend never sends these — keep nullable/defaulted, don't rely on them
    val itemName: String? = null,
    val totalPrice: Double? = null
)