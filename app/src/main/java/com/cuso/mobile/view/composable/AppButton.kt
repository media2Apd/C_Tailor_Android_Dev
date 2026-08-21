package com.cuso.mobile.view.composable

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.disabled
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.whiteBg
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = Primary,
    contentColor: Color = Color.White,
    disabledContainerColor: Color = disabled,
    disabledContentColor: Color = whiteBg
) {
    val tokens = LocalAppTokens.current

    Button(
        onClick = onClick,
        modifier = modifier.height(tokens.buttonHeight),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        )
    ) {
        if (isLoading) {
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center) {
                CirculerProgressIndicatorSmall()
                Spacer(Modifier.width(10.dp))
                Text(
                    text = text,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
            }

        } else {
            Text(
                text = text,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}