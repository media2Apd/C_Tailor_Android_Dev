package com.example.cusotailor.model
data class otpVerify(
    val email:String,
    val otp:String
)

data class otpResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)