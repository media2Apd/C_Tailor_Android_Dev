package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Row
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
import kotlinx.coroutines.delay

@Composable
fun ResendLoginOtpSection(
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timer by rememberSaveable { mutableIntStateOf(60) }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }

    Row(modifier = modifier) {
        TextButton(
            onClick = {
                onResendClick()
                timer = 60
            },
            enabled = timer == 0
        ) {
            Text(
                text = if (timer == 0) "Resend OTP" else "Resend in ${timer}s",
                color = if (timer == 0) Color.Blue else Color.DarkGray
            )
        }
    }
}