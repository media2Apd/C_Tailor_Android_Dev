package com.cuso.mobile.view.composable

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
@Composable
fun forgotOtpInput(
    otpLength: Int = 6,
    activity: Activity,
    onOtpComplete: (String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    var showPastePopup by remember { mutableStateOf(false) }
    var longPressOffset by remember { mutableStateOf(Offset.Zero) }
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val digits = newValue.text.filter { it.isDigit() }.take(otpLength)
                textFieldValue = TextFieldValue(
                    text = digits,
                    selection = TextRange(digits.length)
                )
                if (digits.length == otpLength) onOtpComplete(digits)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .height(55.dp)
                .alpha(0f),
            decorationBox = { it() }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(otpLength) { index ->
                val char = textFieldValue.text.getOrNull(index)?.toString() ?: ""
                val isFocused = textFieldValue.text.length == index

                Box(
                    modifier = Modifier
                        .width(55.dp)
                        .height(55.dp)
                        .border(
                            width = if (isFocused) 2.dp else 1.5.dp,
                            color = when {
                                isFocused -> Color.Blue
                                char.isNotEmpty() -> Color(0xFF4CAF50)
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showPastePopup = false
                                    focusRequester.requestFocus()
                                },
                                onLongPress = { offset ->
                                    longPressOffset = offset
                                    showPastePopup = true
                                    focusRequester.requestFocus()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    )

                    // paste popup on first box only
                    if (index == 0 && showPastePopup) {
                        Box(modifier = Modifier.align(Alignment.TopCenter)) {
                            Popup(
                                alignment = Alignment.TopCenter,
                                offset = IntOffset(0, -110),
                                onDismissRequest = { showPastePopup = false }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            val clip = clipboardManager.primaryClip
                                                ?.getItemAt(0)?.text?.toString() ?: ""
                                            val digits = clip
                                                .filter { it.isDigit() }
                                                .take(otpLength)
                                            if (digits.isNotEmpty()) {
                                                textFieldValue = TextFieldValue(
                                                    text = digits,
                                                    selection = TextRange(digits.length)
                                                )
                                                if (digits.length == otpLength) {
                                                    onOtpComplete(digits)
                                                }
                                            }
                                            showPastePopup = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = " Paste | Select All",
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }


}