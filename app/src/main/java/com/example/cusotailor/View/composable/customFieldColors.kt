package com.example.cusotailor.View.composable

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun customFieldColors() = TextFieldDefaults.colors(
    disabledTextColor = Color.Black,
    disabledContainerColor = Color.White,
    disabledIndicatorColor = Color.Gray,
    disabledLabelColor = Color.Gray,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color.Gray,
    unfocusedIndicatorColor = Color.Gray,
    cursorColor = Color.Black

)

