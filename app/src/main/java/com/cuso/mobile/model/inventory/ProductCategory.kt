package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

data class ProductCategoryItem(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("parentCategoryId") val parentCategoryId: String? = null
)

data class ProductCategoriesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<ProductCategoryItem>? = emptyList(),
    @SerializedName("message") val message: String? = null
)