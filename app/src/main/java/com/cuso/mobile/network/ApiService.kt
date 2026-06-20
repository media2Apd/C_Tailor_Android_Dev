package com.cuso.mobile.network

import com.cuso.mobile.model.EmailResponse
import com.cuso.mobile.model.EmailVerify
import com.cuso.mobile.model.GoogleLoginRequest
//import com.cuso.mobile.model.GoogleLoginRequest
//import com.cuso.mobile.model.GoogleLoginResponse

import com.cuso.mobile.model.PasswordResponse
import com.cuso.mobile.model.PasswordVerify
import com.cuso.mobile.model.RegisterVerifyOtp
import com.cuso.mobile.model.RegisterVerifyOtpResponse
import com.cuso.mobile.model.SignupRequest
import com.cuso.mobile.model.SignupResponse
import com.cuso.mobile.model.forgotPasswordRequest
import com.cuso.mobile.model.forgotPasswordResponse
import com.cuso.mobile.model.otpSendRequest
import com.cuso.mobile.model.otpSendResponse
import com.cuso.mobile.model.otpVerifyRequest
import com.cuso.mobile.model.otpVerifyResponse
import com.cuso.mobile.model.forgotPasswordVerifyRequest
import com.cuso.mobile.model.forgotPasswordVerifyResponse
import com.cuso.mobile.model.meResponse
import com.cuso.mobile.model.myLayoutResponse
import com.cuso.mobile.model.myOrganizationResponse
import com.cuso.mobile.model.organizationSetUpRequest
import com.cuso.mobile.model.organizationSetUpResponse
import com.cuso.mobile.model.resetNewPasswordRequest
import com.cuso.mobile.model.resetNewPasswordResponse
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
//  Sign up

    @POST("/api/auth/register/send-otp")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

     @POST("/api/auth/register/verify-otp")
    suspend fun signupVerifyOtp(
        @Body request: RegisterVerifyOtp
    ): Response<RegisterVerifyOtpResponse>



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

    @POST("/api/forgot-password/verify-otp")
    suspend fun forgotPasswordVerify(
        @Body request: forgotPasswordVerifyRequest
    ): Response<forgotPasswordVerifyResponse>

    @POST("api/forgot-password/reset-password")
    suspend fun resetNewPassword(
        @Body request: resetNewPasswordRequest
    ): Response<resetNewPasswordResponse>
    @POST("/api/auth/google/login")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Response<JsonObject>

    @POST("/api/auth/complete-registration")
    suspend fun organizationSetUp(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: organizationSetUpRequest
    ): Response<organizationSetUpResponse>


    @GET("/api/members/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<meResponse>


    @GET("/api/organizations/my-organization")
    suspend fun getMyOrganization(
        @Header("Authorization") token: String
    ): Response<myOrganizationResponse>

    @GET("/api/dashboard-preference/my-layout")
    suspend fun getMyLayout(
        @Header("Authorization") token: String
    ): Response<myLayoutResponse>
}