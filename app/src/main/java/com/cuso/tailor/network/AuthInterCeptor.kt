package com.cuso.tailor.network

import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.repository.SessionManager
import com.cuso.tailor.utils.AuthEventManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val tokensDao: TokensDao
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestPath = original.url.encodedPath

        val isAuthRoute = requestPath.contains("/auth/login") ||
                requestPath.contains("/auth/check-email") ||
                requestPath.contains("/auth/verify-otp")

        val request = if (isAuthRoute) {
            original
        } else {
            val tokens = runBlocking { tokensDao.getTokens() }
            original.newBuilder().apply {
                tokens?.let {
                    header("Authorization", "Bearer ${it.accessToken}")
                    header("X-CSRF-Token", it.csrfToken)
                }
            }.build()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            val authHeader = request.header("Authorization")
            val hasValidAuthHeader = !authHeader.isNullOrBlank() && authHeader != "Bearer "

            // Normal case: token was sent and rejected, so the session has expired
            if (!isAuthRoute && hasValidAuthHeader) {
                val expiredMessage = "Your session has expired. Please sign in again."
                CoroutineScope(Dispatchers.IO).launch {
                    sessionManager.onSessionExpired(expiredMessage)
                }
                AuthEventManager.emitSessionExpired(expiredMessage)
            }

            // New case: login/auth API returned 401 with "authentication required"
            if (isAuthRoute) {
                // peekBody copies the body, so the original response is not consumed
                val body = runCatching { response.peekBody(1024 * 1024).string() }
                    .getOrDefault("")

                // Read the "message" field from the API response
                val apiMessage = runCatching { JSONObject(body).optString("message") }
                    .getOrDefault("")

                val isAuthRequired = apiMessage.contains("authentication required", ignoreCase = true) ||
                        body.contains("authentication required", ignoreCase = true)

                if (isAuthRequired) {
                    // Show the dialog with the API message (blank falls back to the default in AppNav)
                    AuthEventManager.emitSessionExpired(apiMessage)
                }
            }
        }

        return response
    }
}