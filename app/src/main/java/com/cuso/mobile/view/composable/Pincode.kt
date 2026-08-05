package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.blackTitle

@Composable
fun Pincode(
    pincodeValue: String,
    onPincodeChange: (String) -> Unit
) {
    Column {
        Text("Pincode/Zip/Postal", color = blackTitle)
        OutlinedTextField(
            value = pincodeValue,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }
                if (digitsOnly.length <= 10) {
                    onPincodeChange(digitsOnly)
                }
            },
            placeholder = { Text("..", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // ← numbers only
            colors = customFieldColors()
        )
    }
}