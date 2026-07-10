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
import androidx.navigation.NavController

@Suppress("UNUSED_PARAMETER")

@Composable
fun SignUpText(navController: NavController){
    Row(
        Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center

    ) {
        Text(
            "Dont have an account?",
            Modifier.padding(horizontal = 2.dp),
            color = Color.Gray
        )
        Text("Sign Up",
            color = Color.Blue,
            modifier=Modifier.clickable{
                navController.navigate("signup")
            })
    }
}