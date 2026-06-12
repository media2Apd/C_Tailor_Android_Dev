package com.cuso.mobile.view.composable


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun city(
    cityValue: String,
    onCityChange: (String) -> Unit
) {
    Column {
        Text(" City", color = Color.Black)
        OutlinedTextField(
            value = cityValue,
            onValueChange = onCityChange,
            placeholder = { Text("..", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            singleLine = true,
            colors = customFieldColors()
        )
    }
}