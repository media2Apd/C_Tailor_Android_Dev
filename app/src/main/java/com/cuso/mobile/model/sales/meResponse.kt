package com.cuso.mobile.model.sales

import com.cuso.mobile.model.login_forgotPassword_resetPassword.LoginData

data class meResponse(
        val success:String,
        val message:String,
        val data: LoginData
        )