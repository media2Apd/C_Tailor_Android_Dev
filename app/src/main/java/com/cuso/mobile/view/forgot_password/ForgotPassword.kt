package com.cuso.mobile.view.forgot_password

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.AuthScreenScaffold
import com.cuso.mobile.view.composable.CardContentsForgotPassword

@Composable
fun ForgotUserPassword(
    activity: Activity,
    navController: NavController,
    prefilledEmail: String = "" // passed in from the previous screen (e.g. login)
) {
    // Reusable structure: background + scroll + adaptive padding +
    // logo + title + subtitle + width-limited bordered card.
    // This automatically fixes the missing left/right padding issue
    // because AuthScreenScaffold always applies tokens.screenPadding.
    AuthScreenScaffold(
        title = "Forgot Password",
        subtitle = "Enter your email to receive a reset code"
    ) {
        // Only the screen-specific form content goes here
        CardContentsForgotPassword(
            navController = navController,
            activity = activity,
            prefilledEmail = prefilledEmail
        )
    }
}