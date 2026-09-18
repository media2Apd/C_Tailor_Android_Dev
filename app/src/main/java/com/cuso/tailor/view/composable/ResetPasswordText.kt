@file:Suppress("unused")

package com.cuso.tailor.view.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.cuso.tailor.ui.theme.blackTitle

@Composable
fun ResetPasswordText(){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            " Reset your password ",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 30.sp,
            color = blackTitle
        )

    }
}