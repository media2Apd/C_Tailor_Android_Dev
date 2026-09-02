package com.cuso.mobile.model.login_forgotPassword_resetPassword

data class RegisterVerifyOtp(
    val email:String,
    val otp:String
)
data class RegisterVerifyOtpResponse(
    val success: Boolean,
    val message:String
)
