package com.cuso.mobile.model

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