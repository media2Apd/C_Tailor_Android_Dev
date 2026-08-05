package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.whiteBg

@Composable
fun CirculerProgressIndicatorReuse() {
    CircularProgressIndicator(color = Primary)
}
@Composable
fun CirculerProgressIndicatorForButton() {
    CircularProgressIndicator(Modifier.size(24.dp),color = whiteBg)
}
@Composable
fun CirculerProgressIndicatorSmall() {
    CircularProgressIndicator(Modifier.size(20.dp),color = whiteBg)
}
