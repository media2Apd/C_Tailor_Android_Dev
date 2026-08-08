package com.cuso.mobile.view.composable

import android.app.Activity import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController
import com.cuso.mobile.viewmodel.Authenticate
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.viewmodel.UiState




@Composable
fun ForgotOtpSelection(
    navController: NavController,
    activity: Activity,
    submittedEmail: String
) {
    val tokens = LocalAppTokens.current
    val authViewModel: Authenticate = hiltViewModel()
    val accountState by authViewModel.accountState.collectAsState()
    var savedEmail by rememberSaveable { mutableStateOf(submittedEmail) }


    var otp by remember { mutableStateOf("") }
    var isOtpComplete by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {}

    Column(
        modifier = Modifier.padding(tokens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter your OTP",
            color = blackTitle,
            fontSize = tokens.h2, // Adaptive Header
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(tokens.screenPadding))

        // OTP Input Component
        ForgotOtpInput(
            activity = activity,
            onOtpComplete = { enteredOtp ->
                otp = enteredOtp
                isOtpComplete = true
            }
        )

        Spacer(Modifier.height(tokens.screenPadding))

        // Resend section using adaptive body text
        ResendForgotOtpSection(onResendClick = { savedEmail }, email = savedEmail, authViewModel,savedEmail=savedEmail,otp)

        Spacer(Modifier.height(tokens.screenPadding))

        // Standardized Button: 40dp height, 5dp radius
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Button(
                onClick = {
                    if (isOtpComplete) {
                        authViewModel.verifyForgotPasswordOtp(submittedEmail, otp)
                    }
                },
                enabled = isOtpComplete && accountState !is UiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = whiteBg
                ),
                shape = RoundedCornerShape(5.dp)
            ) {
                if (accountState is UiState.Loading) {
                    CirculerProgressIndicatorSmall()
                } else {
                    Text(
                        text = "Verify and continue",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.screenPadding))
        BackToSignIn(navController)
    }

    LaunchedEffect(accountState) {
        if (accountState is UiState.ForgotPasswordVerified) {
            val resetToken = (accountState as UiState.ForgotPasswordVerified).resetToken
            navController.navigate("reset-pass/$resetToken")
        }
    }
}