package com.cuso.tailor.utils

import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DynamicIslandManager {
    private val _successMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val successMessage = _successMessage.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorMessage = _errorMessage.asSharedFlow()

    // Trigger success notification
    fun showSuccess(raw: String) {
        _successMessage.tryEmit(extractCleanMessage(raw))
    }

    // Trigger error notification
    fun showError(raw: String) {
        _errorMessage.tryEmit(extractCleanMessage(raw))
    }

    // Extracts only the string value of "message" from raw JSON or error string
    fun extractCleanMessage(raw: String?): String {
        if (raw.isNullOrBlank()) return "Something went wrong. Please try again."

        // 1. If response is a JSON string containing "message"
        if (raw.contains("\"message\"")) {
            try {
                val jsonObject = JsonParser.parseString(raw).asJsonObject
                if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                    val msg = jsonObject.get("message").asString
                    if (msg.isNotBlank()) return msg
                }
            } catch (_: Exception) { }
        }

        // 2. Prevent technical Java exception traces from showing
        if (raw.startsWith("java.") || raw.contains("Exception:") || raw.contains("BEGIN_OBJECT")) {
            return "An unexpected server error occurred. Please try again."
        }

        return raw
    }
}