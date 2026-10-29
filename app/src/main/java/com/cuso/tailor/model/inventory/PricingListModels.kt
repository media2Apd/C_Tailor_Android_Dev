@file:Suppress("unused")

package com.cuso.tailor.model.inventory

import com.cuso.tailor.model.finance.ReturnPaginationDto
import com.cuso.tailor.utils.currencySymbol
import com.google.gson.annotations.SerializedName

enum class PriceListTransactionType(val label: String) {
    SALES("Sales"),
    PURCHASE("Purchase")
}

enum class PriceListType {
    ALL_ITEMS,
    INDIVIDUAL_ITEMS
}

enum class PricingScheme {
    UNIT_PRICING,
    VOLUME_PRICING
}

enum class AdjustmentType(val label: String) {
    MARKUP_PERCENT("Markup %"),
    MARKUP_FLAT("Markup Flat"),
    DISCOUNT_PERCENT("Discount %"),
    DISCOUNT_FLAT("Discount Flat")
}

enum class AdjustmentBasis(val label: String) {
    SELLING_PRICE("Selling Price"),
    COST_PRICE("Cost Price"),
    MRP("MRP")
}

enum class RoundOffOption(val label: String, val suffix: String) {
    PSYCHOLOGICAL_99("0.99 (Psychological)", ".99"),
    NEAREST_1("Nearest 1", ""),
    NEAREST_10("Nearest 10", ""),
    NO_ROUNDING("No Rounding", "")
}

data class CurrencyOption(
    val code: String,
    val label: String
)

val DefaultCurrencyOptions = listOf(
    CurrencyOption("INR", "INR - Indian Rupee (${currencySymbol()})"),
    CurrencyOption("USD", "USD - US Dollar ($)"),
    CurrencyOption("EUR", "EUR - Euro (€)"),
    CurrencyOption("GBP", "GBP - British Pound (£)")
)

data class PriceListSummary(
    val id: String,
    val name: String,
    val code: String,
    val statusLabel: String = "Active",
    val detailsCount: Int = 0,
    val schemeLabel: String = "Unit Pricing",
    val roundOff: Double = 0.0
)

data class PricingLineItem(
    val id: String,
    val name: String,
    val sku: String,
    val baseCost: Double,
    var markupPercent: Double,
    var isSelected: Boolean = true
) {
    val computedPrice: Double
        get() = baseCost + (baseCost * markupPercent / 100.0)

    val marginPercent: Double
        get() = if (baseCost == 0.0) 0.0 else ((computedPrice - baseCost) / baseCost) * 100.0

    val isBelowCost: Boolean
        get() = computedPrice < baseCost
}

data class PricePreview(
    val sampleItemName: String = "Classic Cotton Shirt",
    val basePrice: Double = 350.0,
    val adjustmentPercentLabel: String = "+15%",
    val roundingSuffix: String = ".99",
    val finalPrice: Double = 402.99,
    val marginPercent: Int = 22,
    val marginHealthy: Boolean = true
)

data class PriceListFormState(
    val name: String = "",
    val transactionType: PriceListTransactionType = PriceListTransactionType.SALES,
    val description: String = "",
    val priceListType: PriceListType = PriceListType.ALL_ITEMS,
    val pricingScheme: PricingScheme = PricingScheme.UNIT_PRICING,
    val adjustmentType: AdjustmentType = AdjustmentType.MARKUP_PERCENT,
    val adjustmentValue: String = "15",
    val adjustmentBasis: AdjustmentBasis = AdjustmentBasis.SELLING_PRICE,
    val currency: CurrencyOption = DefaultCurrencyOptions.first(),
    val roundOff: RoundOffOption = RoundOffOption.PSYCHOLOGICAL_99,
    val automaticConversion: Boolean = true,
    val preview: PricePreview = PricePreview(),
    val items: List<PricingLineItem> = emptyList()
)

// Top level API response wrapper
data class PriceListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: PriceListDataWrapper? = null
)

data class PriceListDataWrapper(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<PriceListItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReturnPaginationDto? = null
)

data class PriceListItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("transactionType") val transactionType: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("priceListType") val priceListType: String = "",
    @SerializedName("pricingScheme") val pricingScheme: String = "",
    @SerializedName("pricingRule") val pricingRule: PricingRuleDto? = null,
    @SerializedName("volumeTiers") val volumeTiers: List<Any> = emptyList(),
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("roundOff") val roundOff: RoundOffDto? = null,
    @SerializedName("automaticConversion") val automaticConversion: Boolean = false,
    @SerializedName("status") val status: String = "ACTIVE",
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
)

data class PricingRuleDto(
    @SerializedName("adjustmentType") val adjustmentType: String = "",
    @SerializedName("value") val value: Double = 0.0,
    @SerializedName("basedOn") val basedOn: String = ""
)

data class RoundOffDto(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("method") val method: String = "",
    @SerializedName("value") val value: Double = 0.0
)

// ── Item Payload for Create (Image 1: itemId is a String) ──
data class CreatePriceListItemPayload(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("adjustmentType") val adjustmentType: String,
    @SerializedName("basedOn") val basedOn: String,
    @SerializedName("value") val value: Double
)

// ── Item Payload for Update (Images 2 & 3: itemId is an object or populated item) ──
data class UpdatePriceListItemPayload(
    @SerializedName("itemId") val itemId: Any, // Supports object payload or String
    @SerializedName("adjustmentType") val adjustmentType: String,
    @SerializedName("basedOn") val basedOn: String,
    @SerializedName("value") val value: Double
)

// ── Generic Price List Request Payload (Create & Update) ──
data class PriceListRequestPayload(
    @SerializedName("name") val name: String,
    @SerializedName("transactionType") val transactionType: String,
    @SerializedName("description") val description: String = "",
    @SerializedName("priceListType") val priceListType: String,
    @SerializedName("pricingScheme") val pricingScheme: String,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("automaticConversion") val automaticConversion: Boolean = false,
    @SerializedName("status") val status: String = "ACTIVE",
    @SerializedName("roundOff") val roundOff: RoundOffDto?,
    @SerializedName("items") val items: List<Any> = emptyList(),
    @SerializedName("pricingRule") val pricingRule: PricingRuleDto? = null
)

// ── View-One Detailed Response Model ──
data class PriceListViewOneResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: PriceListViewOneData? = null
)

data class PriceListViewOneData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("transactionType") val transactionType: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("priceListType") val priceListType: String = "",
    @SerializedName("pricingScheme") val pricingScheme: String = "",
    @SerializedName("pricingRule") val pricingRule: PricingRuleDto? = null,
    @SerializedName("volumeTiers") val volumeTiers: List<Any> = emptyList(),
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("automaticConversion") val automaticConversion: Boolean = false,
    @SerializedName("status") val status: String = "ACTIVE",
    @SerializedName("roundOff") val roundOff: RoundOffDto? = null,
    @SerializedName("items") val items: List<PriceListViewOneItemDto> = emptyList(),
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
)

data class PriceListViewOneItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("itemId") val item: PriceListPopulatedItemDto? = null,
    @SerializedName("adjustmentType") val adjustmentType: String = "MARKUP_PERCENT",
    @SerializedName("value") val value: Double = 0.0,
    @SerializedName("basedOn") val basedOn: String = "SELLING_PRICE",
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("itemName") val itemName: String = "",
    @SerializedName("sku") val sku: String = "",
    @SerializedName("basePrice") val basePrice: Double = 0.0,
    @SerializedName("computedPrice") val computedPrice: Double = 0.0,
    @SerializedName("margin") val margin: Double = 0.0,
    @SerializedName("marginStatus") val marginStatus: String = "HEALTHY"
)

data class PriceListPopulatedItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String = "",
    @SerializedName("unit") val unit: String = "Pieces",
    @SerializedName("costPrice") val costPrice: Double = 0.0,
    @SerializedName("sellingPrice") val sellingPrice: Double = 0.0,
    @SerializedName("marginPercent") val marginPercent: Double = 0.0,
    @SerializedName("currentStockValue") val currentStockValue: Double = 0.0
)

// ── Generic Mutation Response (Create / Update) ──
data class PriceListMutationResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: Any? = null
)

// Delete response data class
data class PriceListDeleteResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: PriceListDeleteData? = null
)

data class PriceListDeleteData(
    @SerializedName("deleted") val deleted: Boolean = false,
    @SerializedName("priceListId") val priceListId: String = ""
)