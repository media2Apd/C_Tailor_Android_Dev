package com.example.cusotailor.View.composable

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
import kotlinx.coroutines.delay


@Composable
fun ResendOtpSection(
    onResendClick: () -> Unit
) {
    var timer by rememberSaveable { mutableIntStateOf(20) }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }
    Row() {
        Text("")
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = {
                onResendClick()
                timer = 20
            },
            enabled = timer == 0
        ) {
            Text(
                if (timer == 0)
                    "Resend"
                else
                    "Resend in ${timer}s",Modifier, color = Color.DarkGray
            )
        }
    }

}
