package com.cuso.tailor.view.composable

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.blackTitle
import com.cuso.tailor.ui.theme.whiteBg
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
@Composable
fun LoginOtpInput(
    otpLength: Int = 6,
    activity: Activity,
    onOtpComplete: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(""))
    }

    val focusRequester = remember {
        FocusRequester()
    }

    var showPasteMenu by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val digits = newValue.text
                    .filter { it.isDigit() }
                    .take(otpLength)

                textFieldValue = TextFieldValue(
                    text = digits,
                    selection = TextRange(digits.length)
                )

                if (digits.length == otpLength) {
                    onOtpComplete(digits)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .height(tokens.fieldHeight)
                .alpha(0.0001f),
            decorationBox = {
                it()
            }
        )

        Box(
            modifier = Modifier.fillMaxWidth()
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

                                coroutineScope.launch {
                                    val clipEntry = clipboard.getClipEntry()

                                    if (clipEntry != null) {
                                        showPasteMenu = true
                                    }
                                }
                            }
                        )
                    },
                horizontalArrangement = Arrangement.spacedBy(
                    space = tokens.otpBoxSpacing,
                    alignment = Alignment.CenterHorizontally
                )
            ) {
                repeat(otpLength) { index ->

                    val char = textFieldValue.text
                        .getOrNull(index)
                        ?.toString()
                        ?: ""

                    val isFocused =
                        textFieldValue.text.length == index

                    Box(
                        modifier = Modifier
                            .size(tokens.otpBoxSize)
                            .border(
                                width = if (isFocused) {
                                    2.dp
                                } else {
                                    1.5.dp
                                },
                                color = when {
                                    isFocused -> Color.Blue
                                    char.isNotEmpty() ->
                                        Color(0xFF4CAF50)

                                    else -> Color.Gray
                                },
                                shape = RoundedCornerShape(
                                    tokens.cardCornerRadius / 2
                                )
                            )
                            .background(
                                whiteBg,
                                RoundedCornerShape(
                                    tokens.cardCornerRadius / 2
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = TextStyle(
                                fontSize = tokens.h2,
                                fontWeight = FontWeight.Bold,
                                color = blackTitle,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = showPasteMenu,
                onDismissRequest = {
                    showPasteMenu = false
                },
                offset = DpOffset(
                    x = 0.dp,
                    y = -(tokens.otpBoxSize + tokens.otpBoxSpacing)
                ),
                containerColor = whiteBg,
                shape = RoundedCornerShape(
                    tokens.cardCornerRadius / 2
                )
            ) {

                // PASTE
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Paste",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = blackTitle
                        )
                    },
                    onClick = {
                        coroutineScope.launch {

                            val clipEntry =
                                clipboard.getClipEntry()

                            val clipText = clipEntry
                                ?.clipData
                                ?.getItemAt(0)
                                ?.coerceToText(context)
                                ?.toString()
                                ?: ""

                            val digits = clipText
                                .filter { it.isDigit() }
                                .take(otpLength)

                            if (digits.isNotEmpty()) {

                                textFieldValue =
                                    TextFieldValue(
                                        text = digits,
                                        selection = TextRange(
                                            digits.length
                                        )
                                    )

                                if (digits.length == otpLength) {
                                    onOtpComplete(digits)
                                }
                            }

                            showPasteMenu = false
                        }
                    }
                )

                // SELECT ALL
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Select All",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = blackTitle
                        )
                    },
                    onClick = {
                        textFieldValue =
                            textFieldValue.copy(
                                selection = TextRange(
                                    0,
                                    textFieldValue.text.length
                                )
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