package com.cuso.mobile.model
data class otpVerifyRequest(
    val email:String,
    val otp:String
)

data class otpVerifyResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)