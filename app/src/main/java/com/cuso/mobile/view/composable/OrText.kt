package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.PrimaryBorder

@Composable
fun OrText() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),  // takes equal remaining space
            thickness = DividerDefaults.Thickness,
            color = PrimaryBorder
        )

        Text(
            text = "Or",
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.Gray
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),  // takes equal remaining space
            thickness = DividerDefaults.Thickness,
            color = PrimaryBorder
        )
    }
}