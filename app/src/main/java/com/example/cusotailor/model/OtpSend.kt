package com.example.cusotailor.model

data class OtpRequest(
    val email: String
)

data class OtpResponse(
    val success: Boolean,
    val status:String,
    val message: String
)