package com.example.cusotailor.view.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cusotailor.R
@Composable
fun AppLogo(){
    Column {
        Image(
            painter = painterResource(id = R.drawable.cuso_logo),
            contentDescription = "App logo",
            Modifier.size(100.dp)
        )

    }
}