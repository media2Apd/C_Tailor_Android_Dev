package com.cuso.mobile.model.login_forgotPassword_resetPassword

data class resetNewPasswordRequest(
    val confirmPassword:String,
    val newPassword:String,
    val token:String
)

data class resetNewPasswordResponse(
    val success: Boolean,
    val message:String
)
