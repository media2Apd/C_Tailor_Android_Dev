package com.cuso.mobile.view.composable

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder


@Composable
fun CustomFieldColors() = TextFieldDefaults.colors(
    disabledTextColor = Color.Black,
    disabledContainerColor = Color.White,
    disabledIndicatorColor = Color.Gray,
    disabledLabelColor = Color.Gray,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedIndicatorColor = Color.Blue,
    unfocusedIndicatorColor = Color.Gray,
    focusedLabelColor = Color.Black,
    cursorColor = Color.Black,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = Color.White,   // ← add this
    errorIndicatorColor = Color.Red,     // ← add this
    errorCursorColor = Color.Black

)

@Composable
fun CustomFieldOutlinedColors() = OutlinedTextFieldDefaults.colors(
    disabledTextColor = Color.Black,
    disabledContainerColor = Color.White,
    disabledLabelColor = Color.Gray,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedLabelColor = Color.Black,
    cursorColor = Color.Black,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = Color.White,   // ← add this
    errorCursorColor = Color.Black,
    focusedBorderColor = Primary,
    unfocusedBorderColor =  PrimaryBorder

)

