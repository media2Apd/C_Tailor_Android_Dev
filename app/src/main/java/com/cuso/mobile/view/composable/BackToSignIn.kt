package com.cuso.mobile.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary

@Composable
fun BackToSignIn(navController: NavController) {
    // Read adaptive design tokens provided at the app root
    val tokens = LocalAppTokens.current

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(tokens.iconSize) // Adaptive icon size instead of fixed 20.dp
        )

        Spacer(modifier = Modifier.width(tokens.screenPadding / 8)) // Small adaptive gap between icon and text

        Text(
            text = "Back to Sign In",
            modifier = Modifier
                .clickable {
                    navController.navigate("login")
                },
            fontSize = tokens.bodySmall, // Adaptive font instead of fixed 14.sp
            color = Primary
        )
    }
}