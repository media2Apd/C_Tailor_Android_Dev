package com.example.cusotailor.repository

import android.util.Log
import com.example.cusotailor.model.EmailResponse
import com.example.cusotailor.model.EmailVerify
import com.example.cusotailor.model.SignupRequest
import com.example.cusotailor.model.otpSendRequest
import com.example.cusotailor.model.otpSendResponse
import com.example.cusotailor.model.PasswordResponse
import com.example.cusotailor.model.PasswordVerify
import com.example.cusotailor.model.SignupResponse
import com.example.cusotailor.model.forgotPasswordRequest
import com.example.cusotailor.model.forgotPasswordResponse
import com.example.cusotailor.model.otpVerifyRequest
import com.example.cusotailor.model.otpVerifyResponse
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
                val errorBody = response.errorBody()?.string()
                val status = try {
                    val json = org.json.JSONObject(errorBody ?: "")
                    json.getString("status")
                } catch (e: Exception) {
                    "unknown"
                }
                Result.failure(Exception(status))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    //otpSend
    suspend fun sendOtp(email: String): Result<otpSendResponse> {
        return try {

            val response = RetrofitClient.apiService.otpSend(
                otpSendRequest(email)
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

    //otpVerify
    suspend fun verifyOtp(email: String,otp:String): Result<otpVerifyResponse> {
        return try {

            val response = RetrofitClient.apiService.verifyOtp(
                otpVerifyRequest(email, otp)
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

    suspend fun forgotPassword(email: String): Result<forgotPasswordResponse> {
        return try {
            val response = RetrofitClient.apiService.forgotPassword(
                forgotPasswordRequest(email)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    org.json.JSONObject(errorBody ?: "").getString("message")
                } catch (e: Exception) {
                    "Something went wrong"
                }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
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