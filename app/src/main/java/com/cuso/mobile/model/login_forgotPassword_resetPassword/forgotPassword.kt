package com.cuso.mobile.model.login_forgotPassword_resetPassword

data class forgotPasswordRequest(
    val email:String
)

data class forgotPasswordResponse(
    val success:Boolean,
    val message:String,
    val expiryMinutes:Int
)
