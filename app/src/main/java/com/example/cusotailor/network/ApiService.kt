package com.example.cusotailor.network

import com.example.cusotailor.model.EmailResponse
import com.example.cusotailor.model.EmailVerify
//import com.example.cusotailor.model.GoogleLoginRequest
//import com.example.cusotailor.model.GoogleLoginResponse
import com.example.cusotailor.model.OtpRequest
import com.example.cusotailor.model.OtpResponse
import com.example.cusotailor.model.PasswordResponse
import com.example.cusotailor.model.PasswordVerify
import com.example.cusotailor.model.SignupRequest
import com.example.cusotailor.model.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/api/auth/complete-registration")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

    @POST("/api/auth/check-email")
    suspend fun verifyEmail(
        @Body request: EmailVerify
    ): Response<EmailResponse>
    @POST("/api/auth/login")
    suspend fun verifyPassword(
        @Body request: PasswordVerify
    ): Response<PasswordResponse>

    @POST("/api/auth/login/send-otp")
    suspend fun otpSend(
        @Body request: OtpRequest
    ): Response<OtpResponse>

//    @POST("google-login")
//    suspend fun googleLogin(
//        @Body request: GoogleLoginRequest
//    ): Response<GoogleLoginResponse>
}