package com.cuso.mobile.view.composable



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun GstBoxValue(
    GstValue: String,
    onGstChange: (String) -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    Row {
        Text("Tax Information", color = Color.Black)
        Spacer(Modifier.padding(top=10.dp))
        Row {
            Text("Is this bussiness registered for GST/vat/TRN/Local Tax?", color = Color.Black)
            Spacer(Modifier.weight(1f))

            Switch(
                checked = checked,
                onCheckedChange = { checked = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.Green
                )
            )
        }

    }
    Column {
        Text(" GST/vat/TRN/Local Tax ID", color = Color.Black)
        OutlinedTextField(
            value = GstValue,
            onValueChange = onGstChange,
            placeholder = { Text("..", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            singleLine = true,
            colors = CustomFieldColors()
        )
    }
}