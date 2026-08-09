package com.cuso.mobile.view.forgot_password

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.AuthScreenScaffold
import com.cuso.mobile.view.composable.ForgotOtpSelection

@Suppress("UNUSED_PARAMETER")
@Composable
fun VerifyForgotPassword(
    navController: NavController,
    activity: Activity,
    submittedEmail: String
) {
    // Reusable structure: same background + scroll + adaptive padding +
    // logo + title + subtitle + width-limited bordered card as
    // LoginScreen / ForgotUserPassword / LoginOtpScreen.
    AuthScreenScaffold(
        title = "Verify OTP",
        subtitle = "Enter the code sent to $submittedEmail"
    ) {
        // Screen-specific content goes inside the card
        ForgotOtpSelection(
            navController = navController,
            activity = activity,
            submittedEmail = submittedEmail
        )
    }
}