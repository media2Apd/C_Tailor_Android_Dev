package com.example.cusotailor.model

data class otpSendRequest(
    val email: String
)

data class otpSendResponse(
    val success: Boolean,
    val status:String,
    val message: String
)