package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cuso.mobile.adaptive_screen.LocalAppTokens

@Composable
fun SignUpText() {
    // Read adaptive design tokens provided at the app root
    // so this text scales consistently with the rest of the app
    // (phone / foldable / tablet)
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Don't have an account?",
            //  screenPadding is a Dp token (screenPadding is meant for
            // outer margins, but dividing it down gives a small adaptive
            // gap here) — tokens.label is a TextUnit (font size) and cannot
            // be used inside padding(), which caused the type mismatch.
            modifier = Modifier.padding(horizontal = tokens.screenPadding / 8),
            color = Color.Gray,
            // Micro-text size, adapts with screen width (was hardcoded 10.sp)
            fontSize = tokens.caption
        )
        Text(
            text = "Contact Admin",
            color = Color.Blue,
            // Same adaptive micro-text size for visual consistency
            fontSize = tokens.caption
        )
    }
}