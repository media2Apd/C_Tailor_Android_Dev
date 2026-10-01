package com.cuso.tailor.network

import com.cuso.tailor.utils.DynamicIslandManager
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMessageInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        try {
            // peekBody does not consume the response stream, leaving it intact for Retrofit
            val responseBodyString = response.peekBody(1024 * 512).string()

            if (responseBodyString.isNotBlank() && responseBodyString.contains("\"message\"")) {
                val jsonObject = JsonParser.parseString(responseBodyString).asJsonObject
                val message = jsonObject.get("message")?.takeIf { !it.isJsonNull }?.asString.orEmpty()

                if (message.isNotBlank()) {
                    val isMutation = request.method in setOf("POST", "PUT", "PATCH", "DELETE")

                    if (response.isSuccessful && isMutation) {
                        // Display clean success message for write operations
                        DynamicIslandManager.showSuccess(message)
                    } else if (!response.isSuccessful && response.code != 401) {
                        // Display clean error message (401 handled by AuthInterceptor)
                        DynamicIslandManager.showError(message)
                    }
                }
            }
        } catch (_: Exception) {
            // Failsafe to ensure network responses are never blocked
        }

        return response
    }
}