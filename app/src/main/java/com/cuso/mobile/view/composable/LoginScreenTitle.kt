package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.cuso.mobile.R

@Composable
fun LoginScreenTitle(){
    Column() {
        Text(
            "Welcome to CUSO Tailor",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            " Please login usibg the form below ", Modifier,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 13.sp,
            color = Color(0xFF5F6062),
        )
    }
}

