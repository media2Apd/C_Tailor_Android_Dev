package com.cuso.mobile.view.composable

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.viewmodel.Authenticate

@Composable
fun LoginOtpSelection(
    navController: NavController,
    activity: Activity,
    submittedEmail: String
) {
    // Disable system back button on this screen
    BackHandler(enabled = true) {
    }

    val tokens = LocalAppTokens.current
    val authViewModel: Authenticate = hiltViewModel()

    var otp by remember { mutableStateOf("") }
    val verifyResult by authViewModel.otpVerifyResult.observeAsState()
    var isOtpComplete by remember { mutableStateOf(false) }
    var savedEmail by rememberSaveable { mutableStateOf(submittedEmail) }

    // Feedback message states
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendSuccessMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Recipient Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 2.5f)
                        .clip(CircleShape)
                        .background(color = Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonOutline,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Spacer(modifier = Modifier.width(tokens.cardPadding / 2))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OTP sent to",
                        fontSize = tokens.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = savedEmail,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = blackTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "Change",
                    modifier = Modifier.clickable {
                        navController.navigate("login")
                    },
                    color = Color.Blue,
                    fontSize = tokens.bodyMedium
                )
            }

            Spacer(Modifier.height(tokens.cardPadding))

            Text(
                text = "Enter your OTP",
                color = blackTitle,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(tokens.cardPadding / 2))

            // OTP 6-digit input component
            ForgotOtpInput(
                activity = activity,
                onOtpComplete = { enteredOtp ->
                    otp = enteredOtp
                    isOtpComplete = true
                    errorMessage = null
                }
            )

            Spacer(Modifier.height(tokens.cardPadding / 2))

            // Options Row: Sign In with Password & Resend OTP
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sign in using password",
                    modifier = Modifier.clickable {
                        val formattedEmail = savedEmail.trim()
                        navController.navigate("login-with-email/${Uri.encode(formattedEmail)}") {
                            popUpTo(0) { }
                        }
                    },
                    color = Color.Blue,
                    fontSize = tokens.bodySmall
                )

                Spacer(Modifier.weight(1f))

                ResendLoginOtpSection(
                    onResendClick = {
                        val formattedEmail = savedEmail.trim()
                        if (formattedEmail.isNotBlank()) {
                            authViewModel.sendOtp(
                                email = formattedEmail,
                                onSuccess = {
                                    resendSuccessMessage = "OTP resent successfully"
                                    errorMessage = null
                                },
                                onError = { error ->
                                    errorMessage = error
                                }
                            )
                        }
                    }
                )
            }

            Spacer(Modifier.height(tokens.cardPadding))

            // Submit Button
            Button(
                onClick = {
                    if (isOtpComplete) {
                        authViewModel.verifyOtp(savedEmail.trim(), otp.trim())
                    }
                },
                enabled = isOtpComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(tokens.cardCornerRadius / 2)
            ) {
                Text(
                    text = "Verify and continue",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(tokens.cardPadding / 2))

            BackToSignIn(navController)
        }

        // Dynamic Island Overlay Feedback
        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = resendSuccessMessage,
            onDismiss = { resendSuccessMessage = null }
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }

    // Handle OTP verification result
    LaunchedEffect(verifyResult) {
        verifyResult?.let { result ->
            result.onSuccess { response ->
                val org = response.data.user.organizationId

                if (org.orgSetupComplete) {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    navController.navigate("org") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }.onFailure { error ->
                Log.e("OTP_API", "Login OTP verification failed", error)
                errorMessage = error.message ?: "Invalid OTP. Please try again."
            }
        }
    }
}