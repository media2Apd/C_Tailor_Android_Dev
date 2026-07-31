package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.viewmodel.Authenticate
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
private val resendtext=Color(0xFF6B7280)

@Suppress("UNUSED_PARAMETER")
@Composable
fun ResendForgotOtpSection(
    onResendClick: () -> Unit,
    email:String,
    authViewModel: Authenticate,
    savedEmail:String,
    otp:String
) {
    var timer by rememberSaveable { mutableIntStateOf(60) }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000.milliseconds)
            timer--
        }
    }
    Row {
        Text("")
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = {
                onResendClick()
                timer = 60
                authViewModel.forgotPasswordOtp(savedEmail)
            },
            enabled = timer == 0
        ) {
            Text(
                text = if (timer == 0) "Resend OTP" else "Resend in ${timer}s",
                color = if (timer == 0) Primary else resendtext, fontSize = 12.47.sp
            )
        }
    }

}
