package com.cuso.mobile.network.auth

import com.cuso.mobile.model.*
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/register/send-otp")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>

    @POST("/api/auth/register/verify-otp")
    suspend fun signupVerifyOtp(@Body request: RegisterVerifyOtp): Response<RegisterVerifyOtpResponse>

    @POST("/api/auth/check-email")
    suspend fun verifyEmail(@Body request: EmailVerify): Response<EmailResponse>

    @POST("/api/auth/login")
    suspend fun verifyPassword(@Body request: PasswordVerify): Response<PasswordResponse>

    @POST("/api/auth/login/send-otp")
    suspend fun otpSend(@Body request: otpSendRequest): Response<otpSendResponse>

    @POST("/api/auth/login/verify-otp")
    suspend fun verifyOtp(@Body request: otpVerifyRequest): Response<otpVerifyResponse>

    @POST("/api/forgot-password/send-otp")
    suspend fun forgotPassword(@Body request: forgotPasswordRequest): Response<forgotPasswordResponse>

    @POST("/api/forgot-password/verify-otp")
    suspend fun forgotPasswordVerify(@Body request: forgotPasswordVerifyRequest): Response<forgotPasswordVerifyResponse>

    @POST("/api/forgot-password/reset-password")
    suspend fun resetNewPassword(@Body request: resetNewPasswordRequest): Response<resetNewPasswordResponse>

    @POST("/api/auth/google/login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<JsonObject>

    @POST("/api/auth/complete-registration")
    suspend fun organizationSetUp(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: organizationSetUpRequest
    ): Response<organizationSetUpResponse>
}