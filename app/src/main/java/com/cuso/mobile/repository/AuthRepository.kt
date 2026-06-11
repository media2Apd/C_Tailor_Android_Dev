package com.cuso.mobile.repository

import android.util.Log
import com.cuso.mobile.model.EmailResponse
import com.cuso.mobile.model.EmailVerify
import com.cuso.mobile.model.GoogleLoginNewUser
import com.cuso.mobile.model.GoogleLoginRequest
import com.cuso.mobile.model.SignupRequest
import com.cuso.mobile.model.otpSendRequest
import com.cuso.mobile.model.otpSendResponse
import com.cuso.mobile.model.PasswordResponse
import com.cuso.mobile.model.PasswordVerify
import com.cuso.mobile.model.SignupResponse
import com.cuso.mobile.model.forgotPasswordRequest
import com.cuso.mobile.model.forgotPasswordResponse
import com.cuso.mobile.model.otpVerifyRequest
import com.cuso.mobile.model.otpVerifyResponse
import com.cuso.mobile.model.GoogleLoginResult
import com.cuso.mobile.model.GoogleLoginSuccess
import com.cuso.mobile.model.forgotPasswordVerifyRequest
import com.cuso.mobile.model.forgotPasswordVerifyResponse
import com.google.gson.Gson
import org.json.JSONObject
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

    suspend fun verifyForgotPasswordOtp(email: String, otp: String): Result<forgotPasswordVerifyResponse> {
        return try {
            val response = RetrofitClient.apiService.forgotPasswordVerify(
                forgotPasswordVerifyRequest(email, otp)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    org.json.JSONObject(errorBody ?: "").getString("message")
                } catch (e: Exception) { "Something went wrong" }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun googleLogin(idToken: String): GoogleLoginResult {
        return try {
            val response = RetrofitClient.apiService.googleLogin(GoogleLoginRequest(idToken))
            if (response.isSuccessful && response.body() != null) {
                val json = response.body()!!
                if (json.has("requiresRegistration")) {
                    // ← new user
                    val newUser = Gson().fromJson(json, GoogleLoginNewUser::class.java)
                    GoogleLoginResult.NewUser(newUser)
                } else {
                    // ← existing user
                    val existingUser = Gson().fromJson(json, GoogleLoginSuccess::class.java)
                    GoogleLoginResult.ExistingUser(existingUser)
                }
            } else {
                val message = try {
                    JSONObject(response.errorBody()?.string() ?: "").getString("message")
                } catch (e: Exception) { "Something went wrong" }
                GoogleLoginResult.Failure(message)
            }
        } catch (e: Exception) {
            GoogleLoginResult.Failure("Network error: ${e.message}")
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