package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.Primary
@Composable
fun CirculerProgressIndicatorReuse() {
    CircularProgressIndicator(color = Primary)
}
@Composable
fun CirculerProgressIndicatorForButton() {
    CircularProgressIndicator(Modifier.size(24.dp),color = Color.White)
}
@Composable
fun CirculerProgressIndicatorSmall() {
    CircularProgressIndicator(Modifier.size(20.dp),color = Color.White)
}
