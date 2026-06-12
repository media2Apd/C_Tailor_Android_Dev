package com.cuso.mobile.model

data class resetNewPasswordRequest(
    val confirmPassword:String,
    val newPassword:String,
    val token:String
)

data class resetNewPasswordResponse(
    val success: Boolean,
    val message:String
)
