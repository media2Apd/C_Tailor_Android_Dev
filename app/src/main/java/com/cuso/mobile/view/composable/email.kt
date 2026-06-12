package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun email(
    emailValue: String,
    onEmailChange: (String) -> Unit,

    ) {
    Column {
        Text("Email", color = Color.Black)
        OutlinedTextField(
            value = emailValue,
            onValueChange = onEmailChange,
            placeholder = { Text("..", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(9),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }

    Spacer(Modifier.padding(horizontal = 10.dp))

}