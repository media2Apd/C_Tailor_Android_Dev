@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "VariableNeverRead"
)

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
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg

@Suppress("UNUSED_PARAMETER")
@Composable
fun ForgotOtpInput(
    otpLength: Int = 6,
    activity: Activity,
    onOtpComplete: (String) -> Unit
) {
    // Read adaptive design tokens provided at the app root
    val tokens = LocalAppTokens.current

    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    var showPastePopup by remember { mutableStateOf(false) }
    var longPressOffset by remember { mutableStateOf(Offset.Zero) }
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    // FIX: Removed the extra .padding(horizontal = tokens.screenPadding) that
    // was here before. This composable already sits inside AuthScreenScaffold's
    // Card (which applies the outer screenPadding + the Card's own cardPadding),
    // and ForgotOtpSelection's Column no longer adds its own padding either.
    // Stacking a third layer of padding here was one of the causes of the
    // OTP boxes overflowing/clipping on screen.
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
                .height(tokens.fieldHeight) // Adaptive height instead of fixed 55.dp
                .alpha(0f),
            decorationBox = { it() }
        )

        // FIX: Previously this Row used .fillMaxWidth().widthIn(max = 380.dp)
        // with Arrangement.spacedBy(7.13.dp) — two separate bugs:
        // 1. spacedBy() without an alignment only controls the GAP between
        //    boxes, it does NOT center the group. On a wide Row (fillMaxWidth),
        //    the box group stayed anchored to the left, leaving a large empty
        //    gap on the right on tablets/wide screens.
        // 2. The hard 380.dp width cap was smaller than the actual content
        //    width needed with Expanded/tablet tokens (6 boxes * 58.dp +
        //    5 gaps * 12.dp = 408.dp), so the row content exceeded its own
        //    max width and got clipped — the exact cut-off boxes seen on
        //    the tablet screenshot.
        //
        // The fix: don't set any width/fillMaxWidth on the Row at all — let
        // it size itself naturally to wrap its children (box size + spacing
        // both come from adaptive tokens, so it always requests exactly the
        // width it needs). The parent Box above has contentAlignment = Center,
        // so this Row will always be centered within it and will never
        // overflow or clip on any screen size.
        Row(
            horizontalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
        ) {
            repeat(otpLength) { index ->
                val char = textFieldValue.text.getOrNull(index)?.toString() ?: ""
                val isFocused = textFieldValue.text.length == index

                Box(
                    modifier = Modifier
                        .size(tokens.otpBoxSize) // Adaptive OTP box size instead of fixed 40.dp
                        .border(
                            width = if (isFocused) 2.dp else 1.5.dp,
                            color = if (isFocused) Color(0xFF1D4ED8) else Color(0xFFD1D5DB),
                            shape = RoundedCornerShape(tokens.cardCornerRadius / 2)
                        )
                        .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius / 2))
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
                            fontSize = tokens.h2, // Adaptive font instead of fixed 22.sp
                            fontWeight = FontWeight.Bold,
                            color = blackTitle,
                            textAlign = TextAlign.Center
                        )
                    )

                    // Paste popup shown only on the first box
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
                                            whiteBg,
                                            RoundedCornerShape(tokens.cardCornerRadius / 2.5f)
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
                                        .padding(
                                            horizontal = tokens.cardPadding / 2,
                                            vertical = tokens.cardPadding / 4
                                        )
                                ) {
                                    Text(
                                        text = " Paste | Select All",
                                        color = blackTitle,
                                        fontSize = tokens.bodyMedium, // Adaptive font instead of fixed 14.sp
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