package com.cuso.mobile.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cuso.mobile.ui.theme.whiteBg

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TermsCheckbox(
    navController: NavController,
    onCheckedChange: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange(it)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Blue,
                uncheckedColor = Color.Gray,
                checkmarkColor = whiteBg,
                disabledCheckedColor = Color.LightGray,
                disabledUncheckedColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        // FlowRow wraps its children onto the next line when they don't fit,
        // so "Privacy Policy" won't get clipped on narrow screens.
        FlowRow(modifier = Modifier.weight(1f)) {
            Text(text = "I agree to ", color = Color.Black)
            Text(
                text = "Terms of Service",
                color = Color.Blue,
                modifier = Modifier.clickable { navController.navigate("terms") }
            )
            Text(text = " and ", color = Color.Black)
            Text(
                text = "Privacy Policy",
                color = Color.Blue,
                modifier = Modifier.clickable { navController.navigate("privacy") }
            )
        }
    }
}