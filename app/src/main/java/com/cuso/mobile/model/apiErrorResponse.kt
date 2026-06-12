package com.cuso.mobile.model

data class ApiErrorResponse(
    val success: Boolean,
    val status: String,
    val message: String
)