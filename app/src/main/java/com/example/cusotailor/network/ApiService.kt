package com.example.cusotailor.network

import com.example.cusotailor.model.EmailResponse
import com.example.cusotailor.model.EmailVerify
//import com.example.cusotailor.model.GoogleLoginRequest
//import com.example.cusotailor.model.GoogleLoginResponse

import com.example.cusotailor.model.PasswordResponse
import com.example.cusotailor.model.PasswordVerify
import com.example.cusotailor.model.SignupRequest
import com.example.cusotailor.model.SignupResponse
import com.example.cusotailor.model.forgotPasswordRequest
import com.example.cusotailor.model.forgotPasswordResponse
import com.example.cusotailor.model.otpSendRequest
import com.example.cusotailor.model.otpSendResponse
import com.example.cusotailor.model.otpVerifyRequest
import com.example.cusotailor.model.otpVerifyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
//  Sign up

    @POST("/api/auth/complete-registration")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

//  Check email id
    @POST("/api/auth/check-email")
    suspend fun verifyEmail(
        @Body request: EmailVerify
    ): Response<EmailResponse>

//  Password verification
    @POST("/api/auth/login")
    suspend fun verifyPassword(
        @Body request: PasswordVerify
    ): Response<PasswordResponse>

//  Otp sending
    @POST("/api/auth/login/send-otp")
    suspend fun otpSend(
        @Body request: otpSendRequest
    ): Response<otpSendResponse>


//  Otp verification
    @POST("api/auth/login/verify-otp")
    suspend fun verifyOtp(
        @Body request: otpVerifyRequest
    ): Response<otpVerifyResponse>

    @POST("/api/forgot-password/send-otp")
    suspend fun forgotPassword(
        @Body request: forgotPasswordRequest
    ): Response<forgotPasswordResponse>

//    @POST("google-login")
//    suspend fun googleLogin(
//        @Body request: GoogleLoginRequest
//    ): Response<GoogleLoginResponse>
}