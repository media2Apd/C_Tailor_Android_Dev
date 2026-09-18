package com.cuso.tailor.model.sales

import com.cuso.tailor.model.login_forgotPassword_resetPassword.LoginData

data class meResponse(
        val success:String,
        val message:String,
        val data: LoginData
        )