package com.example.cusotailor.View.composable

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
fun Address(
    addressValue: String,
    onAddressChange: (String) -> Unit
) {
    Column {
        Text(" Address", color = Color.Black)
        OutlinedTextField(
            value = addressValue,
            onValueChange = onAddressChange,
            placeholder = { Text("Address", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            singleLine = true,
            colors = customFieldColors()
        )
    }
}