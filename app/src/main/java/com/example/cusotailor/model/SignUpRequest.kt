package com.example.cusotailor.model

data class SignupRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val country: String,
    val state: String,
    val organization: String,
    val password: String
)
data class SignupResponse(
    val success: Boolean,
    val message: String,
    val userId: String?
)


