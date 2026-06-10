package com.example.cusotailor.view.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


@Composable
fun PasswordInputField(
    passwordValue: String,
    onPasswordChange: (String) -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    Column {
        Text("Password", color = Color.Black)



        OutlinedTextField(
            value = passwordValue,                    // use passwordValue
            onValueChange = onPasswordChange,         // use onPasswordChange
            placeholder = { Text("Password", color = Color.Gray) },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            singleLine = true,
            colors = customFieldColors(),
            trailingIcon = {
                val iconImage = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val iconDescription = if (isPasswordVisible) "Hide password" else "Show password"
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = iconImage,
                        contentDescription = iconDescription,
                        tint = Color.Gray
                    )
                }
            }
        )
    }
}