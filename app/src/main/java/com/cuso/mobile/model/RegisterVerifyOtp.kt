package com.cuso.mobile.model

data class RegisterVerifyOtp(
    val email:String,
    val otp:String
)
data class RegisterVerifyOtpResponse(
    val success: Boolean,
    val message:String
)
