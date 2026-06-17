package com.cuso.mobile.model

data class SignupRequest(
    val country: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val mobile: String,
    val organizationName: String,
    val password: String,
    val state: String,
    val termsAccepted:Boolean
)
data class SignupResponse(
    val success: Boolean,
    val message: String
)


