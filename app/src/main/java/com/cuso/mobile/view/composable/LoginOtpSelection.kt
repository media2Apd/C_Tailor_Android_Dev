package com.cuso.mobile.view.composable

import android.app.Activity
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.viewmodel.Authenticate
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextOverflow
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle

@Composable
fun LoginOtpSelection(
    navController: NavController,
    activity: Activity,
    submittedEmail: String
) {
    // Disable back button on this screen
    BackHandler(enabled = true) {
    }

    // Read adaptive design tokens provided at the app root
    val tokens = LocalAppTokens.current

    val authViewModel: Authenticate = hiltViewModel()

    var otp by remember { mutableStateOf("") }
    val verifyResult by authViewModel.otpVerifyResult.observeAsState()
    var isOtpComplete by remember { mutableStateOf(false) }
    var savedEmail by rememberSaveable { mutableStateOf(submittedEmail) }

    // NOTE: No border/background here anymore. This content already sits
    // inside AuthScreenScaffold's Card, so adding another bordered Box
    // created a "card inside card" look. Now it's just a plain row.
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(tokens.iconSize * 2.5f) // Avatar circle scaled from base icon size
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

    // FIX: Single, moderate gap instead of stacking multiple full
    // screenPadding-sized spacers, which was making the card look
    // stretched/tall on larger (Medium/Expanded) window sizes.
    Spacer(Modifier.height(tokens.cardPadding))

    Text(
        text = "Enter your OTP",
        color = blackTitle,
        fontSize = tokens.bodyLarge,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(Modifier.height(tokens.cardPadding / 2))

    LoginOtpInput(
        activity = activity,
        onOtpComplete = { enteredOtp ->
            otp = enteredOtp
            isOtpComplete = true
        }
    )

    Spacer(Modifier.height(tokens.cardPadding / 2))

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sign in using password",
            modifier = Modifier.clickable {
                navController.navigate("login-with-email/${savedEmail}") {
                    popUpTo(0) { }
                }
            },
            color = Color.Blue,
            fontSize = tokens.bodySmall
        )
        Spacer(Modifier.weight(1f))
        ResendLoginOtpSection(onResendClick = { savedEmail }, email = savedEmail, authViewModel)
    }

    Spacer(Modifier.height(tokens.cardPadding))

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
            }
        }
    }

    Button(
        onClick = {
            if (isOtpComplete) {
                authViewModel.verifyOtp(savedEmail, otp)
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