package com.cuso.mobile.model

data class forgotPasswordRequest(
    val email:String
)

data class forgotPasswordResponse(
    val success:Boolean,
    val message:String,
    val expiryMinutes:Int
)
