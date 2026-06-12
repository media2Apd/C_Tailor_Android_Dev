package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun firstNameLastName(
    firstName:String,
    lastName:String,
    onFirstNameChange:(String)->Unit,
    onLastNameChange:(String)->Unit,
) {

    Column() {
        Text("First Name", Modifier, color = Color.Black)
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,

            placeholder = { Text("..",color=Color.Gray) },
            modifier = Modifier
                .width(200.dp)
                .height(60.dp),
            shape=RoundedCornerShape(5.dp),

            singleLine = true,


            colors = customFieldColors()
        )
    }
    Spacer(Modifier.padding(horizontal = 10.dp))

    Column() {
        Text("Last Name", Modifier, color = Color.Black)

        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,

            placeholder = { Text("..",color=Color.Gray) },
            modifier = Modifier
                .width(200.dp)
                .height(60.dp),

            singleLine = true,

            colors = customFieldColors()
        )
    }
}