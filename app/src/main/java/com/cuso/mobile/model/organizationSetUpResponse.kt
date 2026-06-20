package com.cuso.mobile.model

data class organizationSetUpResponse(
    val success: Boolean,
    val message: String,
    val datas: Organization
)