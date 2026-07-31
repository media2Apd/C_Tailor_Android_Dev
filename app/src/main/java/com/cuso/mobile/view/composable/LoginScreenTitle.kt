package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val titletext=Color(0xFF111827)

@Composable
fun LoginScreenTitle(){
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Welcome to CUSO Tailor",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = titletext
        )
        Spacer(Modifier.padding(top=8.dp))

        Text(
            " Please login using the form below ", Modifier,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 13.sp,
            color = Color(0xFF5F6062),
        )
    }
}

