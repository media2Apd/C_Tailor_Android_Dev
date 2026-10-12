package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName

// Generic API response model for any DELETE operation
data class GenericDeleteResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null
)