package com.cuso.mobile.model.login_forgotPassword_resetPassword

data class EmailVerify(
    val email: String
)

data class EmailResponse(
    val success: Boolean,
    val status:String,
    val message: String
)
