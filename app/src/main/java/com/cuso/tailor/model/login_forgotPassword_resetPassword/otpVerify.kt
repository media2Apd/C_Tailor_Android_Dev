package com.cuso.tailor.model.login_forgotPassword_resetPassword
data class otpVerifyRequest(
    val email:String,
    val otp:String
)

data class otpVerifyResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)