package com.cuso.tailor.model.login_forgotPassword_resetPassword

data class organizationSetUpResponse(
    val success: Boolean,
    val message: String,
    val data: Organization
)