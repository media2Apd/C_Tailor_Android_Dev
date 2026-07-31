package com.cuso.mobile.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Suppress("UNUSED_PARAMETER")

@Composable
fun SignUpText(){
    Row(
        Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center

    ) {
        Text(
            "Don't have an account?",
            Modifier.padding(horizontal = 2.dp),
            color = Color.Gray,
            fontSize = 10.sp
        )
        Text("Contact Admin",
            color = Color.Blue,
            fontSize = 10.sp

        )
    }
}