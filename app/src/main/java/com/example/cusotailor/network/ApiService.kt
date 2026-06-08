package com.example.cusotailor.network

import com.example.cusotailor.model.GoogleLoginRequest
import com.example.cusotailor.model.GoogleLoginResponse
import com.example.cusotailor.model.LoginRequest
import com.example.cusotailor.model.LoginResponse
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

    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    @POST("google-login")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Response<GoogleLoginResponse>
}