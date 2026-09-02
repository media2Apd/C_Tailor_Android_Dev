package com.cuso.mobile.model.login_forgotPassword_resetPassword
data class PasswordVerify(
    val email:String,
    val password:String
)

data class PasswordResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)