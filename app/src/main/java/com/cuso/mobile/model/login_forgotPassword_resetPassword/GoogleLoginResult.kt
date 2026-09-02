package com.cuso.mobile.model.login_forgotPassword_resetPassword

sealed class GoogleLoginResult {
    data class ExistingUser(val response: GoogleLoginSuccess) : GoogleLoginResult()
    data class NewUser(val response: GoogleLoginNewUser) : GoogleLoginResult()
    data class Failure(val message: String) : GoogleLoginResult()
}