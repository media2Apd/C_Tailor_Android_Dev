package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignUpTitle(){
    Spacer(Modifier.height(10.dp))
    Text(
        "Create Account",
        style = MaterialTheme.typography.headlineLarge,
        fontSize = 30.sp,
        color = Color.Black
    )
}
