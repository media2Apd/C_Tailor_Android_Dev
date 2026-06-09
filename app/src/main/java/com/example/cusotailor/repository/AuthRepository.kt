package com.example.cusotailor.repository

import android.util.Log
import com.example.cusotailor.model.EmailResponse
import com.example.cusotailor.model.EmailVerify
import com.example.cusotailor.model.SignupRequest
import com.example.cusotailor.model.OtpRequest
import com.example.cusotailor.model.OtpResponse
import com.example.cusotailor.model.PasswordResponse
import com.example.cusotailor.model.PasswordVerify
import com.example.cusotailor.model.SignupResponse
import javax.inject.Inject

class AuthRepository @Inject constructor() {

    suspend fun login(email: String, password: String): Result<PasswordResponse> {
        return try {
            val response = RetrofitClient.apiService.verifyPassword(
                PasswordVerify(email, password)
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

    suspend fun verifyEmail(email: String): Result<EmailResponse> {
        return try {
            val response = RetrofitClient.apiService.verifyEmail(
                EmailVerify(email)
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Email verification failed")
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    //otpsend
    suspend fun sendOtp(email: String): Result<OtpResponse> {
        return try {

            val response = RetrofitClient.apiService.otpSend(
                OtpRequest(email)
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception("Error ${response.code()} - ${response.message()}")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    // Login
//    suspend fun login(email: String, password: String): Result<LoginResponse> {
//        return try {
//            val response = RetrofitClient.apiService.login(
//                LoginRequest(email)
//            )
//            if (response.isSuccessful && response.body() != null) {
//                Result.success(response.body()!!)
//            } else {
//                Result.failure(Exception("Invalid email or password"))
//            }
//        } catch (e: Exception) {
//            Result.failure(Exception("Network error: ${e.message}"))
//        }
//    }
}