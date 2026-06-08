package com.example.cusotailor.View.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cusotailor.R

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
