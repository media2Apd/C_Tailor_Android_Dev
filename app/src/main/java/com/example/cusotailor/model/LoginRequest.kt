package com.example.cusotailor.model

data class LoginRequest(
    val email: String,
    val password: String
)
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,      // JWT token
    val userId: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?
)