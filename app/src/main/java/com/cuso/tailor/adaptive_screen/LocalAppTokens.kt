package com.cuso.tailor.adaptive_screen

// LocalAppTokens.kt
import androidx.compose.runtime.compositionLocalOf

val LocalAppTokens = compositionLocalOf<AppDesignTokens> {
    error("No AppDesignTokens provided")
}