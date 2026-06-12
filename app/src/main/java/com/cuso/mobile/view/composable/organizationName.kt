package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Orgname(
    organizationValue: String,
    onOrganizationChange: (String) -> Unit
) {
    Column {
        Text("Organization Name", color = Color.Black)
        OutlinedTextField(
            value = organizationValue,
            onValueChange = onOrganizationChange,
            placeholder = { Text("..", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            singleLine = true,
            colors = customFieldColors()
        )
    }

    Spacer(Modifier.padding(horizontal = 10.dp))


}