package com.cuso.mobile.view.login

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.AuthScreenScaffold
import com.cuso.mobile.view.composable.LoginOtpSelection

@Composable
fun LoginOtpScreen(
    navController: NavController,
    activity: Activity,
    submittedEmail: String
) {
    // Reusable structure: background + scroll + adaptive padding +
    // logo + title + subtitle + width-limited bordered card.
    // Replace the title/subtitle strings below with whatever
    // OtpTitle() previously displayed if it differs.
    AuthScreenScaffold(
        title = "Verify OTP",
        subtitle = "Enter the code sent to your email"
    ) {
        // Screen-specific content goes inside the card
        LoginOtpSelection(
            navController = navController,
            activity = activity,
            submittedEmail = submittedEmail
        )
    }
}