package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName

data class ProductionTemplateResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<ProductionTemplateItem> = emptyList()
)

data class ProductionTemplateItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("status") val status: String = "Active"
)