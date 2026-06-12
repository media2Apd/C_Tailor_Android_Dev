package com.cuso.mobile.model

data class forgotPasswordVerifyRequest(
    val email:String,
    val otp:String
)
data class forgotPasswordVerifyResponse(
    val success:Boolean,
    val message:String,
    val resetToken:String,
    val expiryHours: Int
)
