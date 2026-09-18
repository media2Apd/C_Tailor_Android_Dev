package com.cuso.tailor.model.login_forgotPassword_resetPassword

data class otpSendRequest(
    val email: String
)

data class otpSendResponse(
    val success: Boolean,
    val status:String,
    val message: String
)