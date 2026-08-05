package com.cuso.mobile.view.composable

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.whiteBg


@Composable
fun customFieldColors() = TextFieldDefaults.colors(
    disabledTextColor = Color.Black,
    disabledContainerColor = whiteBg,
    disabledIndicatorColor = Color.Gray,
    disabledLabelColor = Color.Gray,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedIndicatorColor = Primary,
    unfocusedIndicatorColor = PrimaryBorder,
    focusedLabelColor = Color.Black,
    cursorColor = Color.Black,
    focusedContainerColor = whiteBg,
    unfocusedContainerColor = whiteBg,
    errorContainerColor = whiteBg,   // ← add this
    errorIndicatorColor = Color.Red,     // ← add this
    errorCursorColor = Color.Black

)

@Composable
fun customFieldOutlinedColors() = OutlinedTextFieldDefaults.colors(
    disabledTextColor = Color.Black,
    disabledContainerColor = whiteBg,
    disabledLabelColor = Color.Gray,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedLabelColor = Color.Black,
    cursorColor = Color.Black,
    focusedContainerColor = whiteBg,
    unfocusedContainerColor = whiteBg,
    errorContainerColor = whiteBg,   // ← add this
    errorCursorColor = Color.Black,
    focusedBorderColor = Primary,
    unfocusedBorderColor =  PrimaryBorder

)

// ✅ NEW — Outlined Button custom colors (same theme as text fields)
@Composable
fun customOutlinedButtonColors() = ButtonDefaults.outlinedButtonColors(
    contentColor = Color.Black,
    containerColor = whiteBg,
    disabledContentColor = Color.Gray,
    disabledContainerColor = whiteBg
)

// ✅ NEW — Border color for outlined buttons (reuses same Primary theme border)
//val CustomOutlinedButtonBorder: BorderStroke
//    @Composable get() = BorderStroke(1.dp, PrimaryBorder)

