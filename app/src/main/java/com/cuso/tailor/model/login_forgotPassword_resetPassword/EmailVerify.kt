package com.cuso.tailor.model.login_forgotPassword_resetPassword

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EmailVerify(
    @SerializedName("email")
    val email: String
)

@Keep
data class EmailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String
)