package com.cuso.mobile.model

import com.google.gson.annotations.SerializedName

// ── Garment Pricing API Response ──
data class GarmentPricingResponse(
    val success: Boolean = true,
    val data: List<GarmentPricingItem> = emptyList(),
    val message: String? = null
)

data class GarmentPricingItem(
    @SerializedName("garmentId") val garmentId: String,
    @SerializedName("garmentName") val garmentName: String,
    @SerializedName("basePrice") val basePrice: Double = 0.0,
    @SerializedName("fabricOptions") val fabricOptions: List<FabricOption> = emptyList(),
    @SerializedName("designOptions") val designOptions: List<DesignOption> = emptyList(),
    @SerializedName("addons") val addons: List<AddonOption> = emptyList(),
    @SerializedName("expressCharge") val expressCharge: Double = 0.0,
    @SerializedName("bulkRules") val bulkRules: List<BulkRule> = emptyList()
)

data class FabricOption(
    val name: String = "",
    val price: Double = 0.0
)

data class DesignOption(
    val name: String = "",
    val price: Double = 0.0
)

data class AddonOption(
    val name: String = "",
    val price: Double = 0.0
)

data class BulkRule(
    val minQuantity: Int = 0,
    val discountPercent: Double = 0.0
)

// ── Selected Garment Pricing for UI ──
data class SelectedGarmentPricing(
    val garmentId: String = "",
    val garmentName: String = "",
    val basePrice: Double = 0.0,
    val selectedFabric: FabricOption? = null,
    val selectedDesign: DesignOption? = null,
    val selectedAddons: List<AddonOption> = emptyList(),
    val quantity: Int = 1,
    val expressCharge: Double = 0.0,
    val totalPrice: Double = 0.0
) {
    fun calculateTotal(): Double {
        val fabricPrice = selectedFabric?.price ?: 0.0
        val designPrice = selectedDesign?.price ?: 0.0
        val addonsPrice = selectedAddons.sumOf { it.price }
        return (basePrice + fabricPrice + designPrice + addonsPrice + expressCharge) * quantity
    }
}