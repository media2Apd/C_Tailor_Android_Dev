package com.cuso.mobile.view.composable

import android.app.Activity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController
import com.cuso.mobile.viewmodel.Authenticate
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.viewmodel.UiState

@Composable
fun ForgotOtpSelection(
    navController: NavController,
    activity: Activity,
    submittedEmail: String,
    // Bubbles error messages up to the parent screen, which hosts the
    // DynamicIslandError at true full-screen scope (see VerifyForgotPassword).
    // Rendering the pill from inside this composable doesn't work correctly
    // because this composable only occupies the AuthScreenScaffold card's
    // bounds, not the actual screen bounds.
    onError: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val authViewModel: Authenticate = hiltViewModel()
    val accountState by authViewModel.accountState.collectAsState()
    var savedEmail by rememberSaveable { mutableStateOf(submittedEmail) }

    var otp by remember { mutableStateOf("") }
    var isOtpComplete by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {}

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Text(
            text = "Enter your OTP",
            color = blackTitle,
            fontSize = tokens.h2,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(tokens.screenPadding))

        ForgotOtpInput(
            activity = activity,
            onOtpComplete = { enteredOtp ->
                otp = enteredOtp
                isOtpComplete = true
            }
        )

        Spacer(Modifier.height(tokens.screenPadding))

        ResendForgotOtpSection(
            onResendClick = { savedEmail },
            email = savedEmail,
            authViewModel,
            savedEmail = savedEmail,
            otp
        )

        Spacer(Modifier.height(tokens.screenPadding))

        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            AppButton(
                text = "Verify and continue",
                onClick = {
                    if (isOtpComplete) {
                        authViewModel.verifyForgotPasswordOtp(submittedEmail, otp)
                    }
                },
                enabled = isOtpComplete && accountState !is UiState.Loading,
                isLoading = accountState is UiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(tokens.screenPadding))
        BackToSignIn(navController)
    }

    LaunchedEffect(accountState) {
        when (val state = accountState) {
            is UiState.ForgotPasswordVerified -> {
                navController.navigate("reset-pass/${state.resetToken}")
            }
            is UiState.Error -> {
                onError(ErrorMapper.map(state.message))
            }
            else -> Unit
        }
    }
}