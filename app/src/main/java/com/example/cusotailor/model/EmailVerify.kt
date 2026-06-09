package com.example.cusotailor.model

data class EmailVerify(
    val email: String
)

data class EmailResponse(
    val success: Boolean,
    val status:String,
    val message: String
)
