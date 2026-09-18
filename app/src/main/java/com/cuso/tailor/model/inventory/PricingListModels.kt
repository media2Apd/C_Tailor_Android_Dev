@file:Suppress("unused")

package com.cuso.tailor.model.inventory

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
    CurrencyOption("INR", "INR - Indian Rupee (₹)"),
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