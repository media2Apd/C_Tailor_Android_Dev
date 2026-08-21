package com.cuso.mobile.view.forgot_password

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.AuthScreenScaffold
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.ForgotOtpSelection

@Suppress("UNUSED_PARAMETER")
@Composable
fun VerifyForgotPassword(
    navController: NavController,
    activity: Activity,
    submittedEmail: String
) {
    // Holds the current error message for the DynamicIslandError pill.
    // Owned here (not inside ForgotOtpSelection) so the pill can anchor
    // itself against the true screen bounds via fillMaxSize, instead of
    // being constrained inside AuthScreenScaffold's card.
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthScreenScaffold(
            title = "Verify OTP",
            subtitle = "Enter the code sent to $submittedEmail"
        ) {
            ForgotOtpSelection(
                navController = navController,
                activity = activity,
                submittedEmail = submittedEmail,
                onError = { message -> errorMessage = message }
            )
        }

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}