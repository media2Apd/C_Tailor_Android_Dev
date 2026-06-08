package com.example.cusotailor.View.composable

import android.app.Activity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.text.isEmpty

@Composable
fun OtpInput(
    otpLength: Int = 6,
    activity: Activity,
    onOtpComplete: (String) -> Unit
) {
    val otpValues = remember { mutableStateListOf(*Array(otpLength) { "" }) }
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            otpValues.forEachIndexed { index, value ->
                TextField(
                    value = value,
                    onValueChange = { newValue ->
                        if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                            otpValues[index] = newValue

                            if (newValue.isNotEmpty() && index < otpLength - 1) {

                                focusRequesters[index + 1].requestFocus()
                            }

                            if (otpValues.all { it.isNotEmpty() }) {
                                onOtpComplete(otpValues.joinToString(""))
                            }
                        }
                        else if (newValue.isEmpty()){
                            otpValues[index]=""
                            if(index>0){
                                focusRequesters[index-1].requestFocus()
                            }
                        }
                    },
                    colors = customFieldColors(),
                    modifier = Modifier
                        .width(60.dp)
                        .onKeyEvent { event ->
                            if (event.key == Key.Backspace && otpValues[index].isEmpty() && index > 0) {
                                otpValues[index - 1] = ""
                                focusRequesters[index - 1].requestFocus()  // ← move to previous box
                                true
                            } else {
                                false
                            }
                        }
                        .height(60.dp)
                        .border(
                            1.dp,
                            Color.Gray,
                            RoundedCornerShape(8.dp)
                        )
                        .focusRequester(focusRequesters[index]),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
        }
    }
    ResendOtpSection(
        onResendClick = {
            println("Clicked")
        }
    )
}

