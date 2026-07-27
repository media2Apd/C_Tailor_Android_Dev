package com.cuso.mobile.utils


// ✅ PERMANENT FIX — null-safe date/string truncation used across the app.
// Prevents "Parameter specified as non-null is null" crashes when API
// responses omit a field that the Kotlin model marked non-nullable.

fun String?.safeDate(length: Int = 10, fallback: String = "—"): String {
    return this?.takeIf { it.isNotBlank() }?.take(length) ?: fallback
}

fun String?.safeTake(length: Int, fallback: String = "—"): String {
    return this?.takeIf { it.isNotBlank() }?.take(length) ?: fallback
}

fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: "—"