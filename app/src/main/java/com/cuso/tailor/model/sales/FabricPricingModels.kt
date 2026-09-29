package com.cuso.tailor.model.sales
import com.google.gson.annotations.SerializedName

data class FabricPricingListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("items") val items: List<FabricPricingItem>?,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class FabricPricingItem(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("sellingPrice") val sellingPrice: Double,
    @SerializedName("fabricType") val fabricType: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("size") val size: String?,
    @SerializedName("status") val status: String,
    @SerializedName("stockStatus") val stockStatus: String?
)

data class UpdateFabricPriceRequest(
    @SerializedName("sellingPrice") val sellingPrice: Double
)

data class UpdateFabricPriceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: FabricPricingItem?,
    @SerializedName("message") val message: String?
)