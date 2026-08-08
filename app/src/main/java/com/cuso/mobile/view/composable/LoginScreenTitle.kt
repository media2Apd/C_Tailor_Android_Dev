package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cuso.mobile.adaptive_screen.LocalAppTokens

// Industry standard color naming
private val TitleTextColor = Color(0xFF111827)
private val SubtitleTextColor = Color(0xFF5F6062)

/**
 * Adaptive Title component for the Login Screen.
 * Uses Design Tokens for responsive typography and spacing.
 */
@Composable
fun LoginScreenTitle() {
    // Access design tokens for current screen size (Compact/Medium/Expanded)
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to CUSO Tailor",
            // Uses tokens.h1 (e.g., 22sp for mobile, scaling up for tablets)
            fontSize = tokens.h1,
            fontWeight = FontWeight.ExtraBold,
            color = TitleTextColor,
            textAlign = TextAlign.Center
        )

        // Adaptive spacing: half of the screen padding for consistent vertical rhythm
        Spacer(modifier = Modifier.height(tokens.screenPadding / 2))

        Text(
            text = "Please login using the form below",
            // Uses tokens.bodyMedium (e.g., 14sp for mobile)
            fontSize = tokens.bodyMedium,
            color = SubtitleTextColor,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )
    }
}