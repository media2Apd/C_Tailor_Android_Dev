package com.example.cusotailor.repository

import android.util.Log
import com.example.cusotailor.model.LoginResponse
import com.example.cusotailor.model.SignupRequest
import com.example.cusotailor.model.LoginRequest
import com.example.cusotailor.model.SignupResponse

class AuthRepository {

    // Create Account
    suspend fun createAccount(request: SignupRequest): Result<SignupResponse> {
        return try {
            val response = RetrofitClient.apiService.signup(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("API_ERROR", "Error: $errorBody")
                Result.failure(Exception("Server error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    // Login
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.apiService.login(
                LoginRequest(email = email, password = password)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}