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

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.platform.LocalClipboardManager
    import androidx.compose.ui.unit.DpOffset



    @Composable
    fun LoginOtpInput(
        otpLength: Int = 6,
        activity: Activity,
        onOtpComplete: (String) -> Unit
    ) {
        var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
        val focusRequester = remember { FocusRequester() }

        // ✅ Use Compose's ClipboardManager — works across all API levels
        val clipboardManager = LocalClipboardManager.current

        // ✅ Controls the native-style DropdownMenu (closest to system toolbar)
        var showPasteMenu by remember { mutableStateOf(false) }

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
                    .height(48.dp)
                    .alpha(0.0001f),
                decorationBox = { it() }
            )

            // ✅ Anchor Box — DropdownMenu is attached here so it appears
            // above the OTP row, just like the system toolbar would.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopStart)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showPasteMenu = false
                                    focusRequester.requestFocus()
                                },
                                onLongPress = {
                                    focusRequester.requestFocus()
                                    // Only show paste menu if clipboard has content
                                    if (clipboardManager.getText() != null) {
                                        showPasteMenu = true
                                    }
                                }
                            )
                        },
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(otpLength) { index ->
                        val char = textFieldValue.text.getOrNull(index)?.toString() ?: ""
                        val isFocused = textFieldValue.text.length == index

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .border(
                                    width = if (isFocused) 2.dp else 1.5.dp,
                                    color = when {
                                        isFocused -> Color.Blue
                                        char.isNotEmpty() -> Color(0xFF4CAF50)
                                        else -> Color.Gray
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }

                // ✅ Material3 DropdownMenu — renders as a floating card above
                // the row, visually identical to the system paste toolbar.
                DropdownMenu(
                    expanded = showPasteMenu,
                    onDismissRequest = { showPasteMenu = false },
                    // Negative Y offset pops it ABOVE the row instead of below
                    offset = DpOffset(x = 0.dp, y = (-56).dp),
                    containerColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Paste",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        },
                        onClick = {
                            val clip = clipboardManager.getText()?.text ?: ""
                            val digits = clip.filter { it.isDigit() }.take(otpLength)
                            if (digits.isNotEmpty()) {
                                textFieldValue = TextFieldValue(
                                    text = digits,
                                    selection = TextRange(digits.length)
                                )
                                if (digits.length == otpLength) onOtpComplete(digits)
                            }
                            showPasteMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Select All",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        },
                        onClick = {
                            // Select all = highlight all filled boxes visually
                            textFieldValue = textFieldValue.copy(
                                selection = TextRange(0, textFieldValue.text.length)
                            )
                            showPasteMenu = false
                        }
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }