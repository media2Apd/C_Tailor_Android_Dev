package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg

/**
 * Reusable scaffold for every auth-related screen (Login, Forgot Password,
 * Reset Password, etc). It provides:
 * - The shared page background
 * - Scroll + keyboard (ime) handling
 * - Adaptive horizontal screen padding (fixes the "no left padding" bug)
 * - App logo
 * - Title + optional subtitle text using the adaptive typography scale
 * - A width-limited, bordered, rounded Card that wraps whatever content
 *   the caller passes in through [content]
 *
 * This guarantees every auth screen looks and behaves identically, and
 * automatically adapts across phone / foldable / tablet sizes because it
 * always reads from [LocalAppTokens].
 *
 * @param title Main heading shown above the card (e.g. "Welcome to CUSO Tailor")
 * @param subtitle Optional helper text shown below the title
 * @param content The screen-specific form content placed inside the card
 */
@Composable
fun AuthScreenScaffold(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    // Read adaptive design tokens provided at the app root
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf5f5f5)) // Shared page background across auth screens
            // Scroll + imePadding so extra content (e.g. OTP fields) never
            // gets pushed past the bottom of the screen or hidden by keyboard
            .verticalScroll(rememberScrollState())
            .imePadding()
            // This horizontal padding is what keeps the card away from the
            // screen edges. Missing this was the root cause of the
            // "no left side padding" bug on ForgotUserPassword.
            .padding(horizontal = tokens.screenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()

        Spacer(modifier = Modifier.height(tokens.screenPadding))

        // Screen title using the adaptive h1 typography token
        Text(
            text = title,
            fontSize = tokens.h1,
            fontWeight = FontWeight.Bold,
            color = blackTitle
        )

        // Optional subtitle using the adaptive bodyMedium typography token
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = tokens.bodyMedium,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding))

        // INDUSTRY GRADE: Limit card width on tablets / desktop so the
        // form does not stretch edge to edge on large screens.
        Box(
            modifier = Modifier
                .widthIn(max = 480.dp) // Professional limit for auth forms
                .fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp, // Thin border for a cleaner look
                        color = Color.LightGray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    ),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        // Dedicated auth-card padding token, distinct from
                        // the outer screenPadding
                        .padding(tokens.cardPadding)
                ) {
                    // Screen-specific content (form fields, buttons, etc.)
                    // is injected here by the caller
                    content()
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding))
    }
}