package com.cuso.mobile.repository

import android.util.Log
import com.cuso.mobile.model.EmailResponse
import com.cuso.mobile.model.EmailVerify
import com.cuso.mobile.model.GoogleLoginNewUser
import com.cuso.mobile.model.GoogleLoginRequest
import com.cuso.mobile.model.GoogleLoginResult
import com.cuso.mobile.model.GoogleLoginSuccess
import com.cuso.mobile.model.PasswordResponse
import com.cuso.mobile.model.PasswordVerify
import com.cuso.mobile.model.RegisterVerifyOtp
import com.cuso.mobile.model.RegisterVerifyOtpResponse
import com.cuso.mobile.model.SignupRequest
import com.cuso.mobile.model.SignupResponse
import com.cuso.mobile.model.forgotPasswordRequest
import com.cuso.mobile.model.forgotPasswordResponse
import com.cuso.mobile.model.forgotPasswordVerifyRequest
import com.cuso.mobile.model.forgotPasswordVerifyResponse
import com.cuso.mobile.model.organizationSetUpRequest
import com.cuso.mobile.model.organizationSetUpResponse
import com.cuso.mobile.model.otpSendRequest
import com.cuso.mobile.model.otpSendResponse
import com.cuso.mobile.model.otpVerifyRequest
import com.cuso.mobile.model.otpVerifyResponse
import com.cuso.mobile.model.resetNewPasswordRequest
import com.cuso.mobile.model.resetNewPasswordResponse
import com.cuso.mobile.model.sales.meResponse
import com.cuso.mobile.model.sales.myLayoutResponse
import com.cuso.mobile.model.sales.myOrganizationResponse
import com.cuso.mobile.network.auth.AuthApiService
import com.cuso.mobile.network.user.UserApiService
import com.google.gson.Gson
import org.json.JSONObject
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApiService,
    private val userApi: UserApiService
) {

    // ---------------------------------------------------------
    // Login
    // ---------------------------------------------------------

    suspend fun login(email: String, password: String): Result<PasswordResponse> {
        return try {
            val response = authApi.verifyPassword(
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

    // ---------------------------------------------------------
    // User Profile & Preferences (UserApiService)
    // ---------------------------------------------------------

    suspend fun getMe(token: String): Result<meResponse> = try {
        val res = userApi.getMe(token)
        if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
        else Result.failure(Exception(res.message()))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getMyOrganization(token: String): Result<myOrganizationResponse> = try {
        val res = userApi.getMyOrganization(token)
        if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
        else Result.failure(Exception(res.message()))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getMyLayout(token: String): Result<myLayoutResponse> = try {
        val res = userApi.getMyLayout(token)
        if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
        else Result.failure(Exception(res.message()))
    } catch (e: Exception) { Result.failure(e) }

    // ---------------------------------------------------------
    // Sign Up & Email Verification (AuthApiService)
    // ---------------------------------------------------------

    suspend fun createAccount(request: SignupRequest): Result<SignupResponse> {
        return try {
            val response = authApi.signup(request)
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
            val response = authApi.verifyEmail(
                EmailVerify(email)
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val status = try {
                    val json = JSONObject(errorBody ?: "")
                    json.getString("status")
                } catch (_: Exception) {
                    "unknown"
                }
                Result.failure(Exception(status))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun organizationSetup(
        token: String,
        csrfToken: String,
        request: organizationSetUpRequest
    ): Result<organizationSetUpResponse> {
        return try {
            val response = authApi.organizationSetUp(token, csrfToken, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    JSONObject(errorBody ?: "").getString("message")
                } catch (_: Exception) { "Organization setup failed" }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    // ---------------------------------------------------------
    // OTP Handling (AuthApiService)
    // ---------------------------------------------------------

    suspend fun sendOtp(email: String): Result<otpSendResponse> {
        return try {
            val response = authApi.otpSend(
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

    suspend fun verifyOtp(email: String, otp: String): Result<otpVerifyResponse> {
        return try {
            val response = authApi.verifyOtp(
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

    suspend fun registerVerifyOtp(email: String, otp: String): Result<RegisterVerifyOtpResponse> {
        return try {
            val response = authApi.signupVerifyOtp(
                RegisterVerifyOtp(email, otp)
            )

            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Result.success(body)
            } else {
                val errorMsg = body?.message ?: "Error ${response.code()} - ${response.message()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------------------------------------------------------
    // Forgot Password Flow (AuthApiService)
    // ---------------------------------------------------------

    suspend fun forgotPassword(email: String): Result<forgotPasswordResponse> {
        return try {
            val response = authApi.forgotPassword(
                forgotPasswordRequest(email)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    JSONObject(errorBody ?: "").getString("message")
                } catch (_: Exception) {
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
            val response = authApi.forgotPasswordVerify(
                forgotPasswordVerifyRequest(email, otp)
            )
            val body = response.body()

            if (response.isSuccessful && body != null && body.success) {
                Result.success(body)
            } else if (body != null) {
                Result.failure(Exception(body.message))
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    JSONObject(errorBody ?: "").getString("message")
                } catch (_: Exception) { "Something went wrong" }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun resetNewPassword(token: String, newPassword: String, confirmPassword: String): Result<resetNewPasswordResponse> {
        return try {
            val response = authApi.resetNewPassword(
                resetNewPasswordRequest(
                    token = token,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    JSONObject(errorBody ?: "").getString("message")
                } catch (_: Exception) { "Something went wrong" }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    // ---------------------------------------------------------
    // Google Login (AuthApiService)
    // ---------------------------------------------------------

    suspend fun googleLogin(idToken: String): GoogleLoginResult {
        return try {
            val response = authApi.googleLogin(GoogleLoginRequest(idToken))
            if (response.isSuccessful && response.body() != null) {
                val json = response.body()!!
                if (json.has("requiresRegistration")) {
                    val newUser = Gson().fromJson(json, GoogleLoginNewUser::class.java)
                    GoogleLoginResult.NewUser(newUser)
                } else {
                    val existingUser = Gson().fromJson(json, GoogleLoginSuccess::class.java)
                    GoogleLoginResult.ExistingUser(existingUser)
                }
            } else {
                val message = try {
                    JSONObject(response.errorBody()?.string() ?: "").getString("message")
                } catch (_: Exception) { "Something went wrong" }
                GoogleLoginResult.Failure(message)
            }
        } catch (e: Exception) {
            GoogleLoginResult.Failure("Network error: ${e.message}")
        }
    }
}