package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

data class StaffResponse(
    val success: Boolean,
    val data: List<StaffDto>
)

data class StaffDto(
    @SerializedName("_id")
    val id: String,

    val role: String,
    val branchId: String,
    val firstName: String,
    val lastName: String,
    val memberId: String
)