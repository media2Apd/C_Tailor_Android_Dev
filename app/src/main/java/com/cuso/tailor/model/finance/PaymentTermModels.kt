package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName

data class PaymentTermsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<PaymentTermItem> = emptyList()
)

data class PaymentTermItem(
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("termType")
    val termType: String? = null,
    @SerializedName("dueDays")
    val dueDays: Int = 0,
    @SerializedName("eomBufferDays")
    val eomBufferDays: Int = 0,
    @SerializedName("isDefault")
    val isDefault: Boolean = false
)