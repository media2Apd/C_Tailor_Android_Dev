package com.cuso.tailor.network

import com.cuso.tailor.repository.SessionManager
import com.cuso.tailor.utils.AuthEventManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            val authHeader = request.header("Authorization")
            val requestPath = request.url.encodedPath

            // Ignore 401 for login or auth routes where 401 indicates invalid credentials
            val isAuthRoute = requestPath.contains("/auth/login") ||
                    requestPath.contains("/auth/check-email") ||
                    requestPath.contains("/auth/verify-otp")

            // Process session expiration only for authenticated requests with a valid token attached
            val hasValidAuthHeader = !authHeader.isNullOrBlank() && authHeader != "Bearer "

            if (!isAuthRoute && hasValidAuthHeader) {
                val expiredMessage = "Your session has expired. Please sign in again."
                CoroutineScope(Dispatchers.IO).launch {
                    sessionManager.onSessionExpired(expiredMessage)
                }
                AuthEventManager.emitSessionExpired(expiredMessage)
            }
        }

        return response
    }
}