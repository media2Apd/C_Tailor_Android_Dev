package com.cuso.mobile.view.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*

/**
 * Enterprise-grade, clean reusable Error State component.
 *
 * @param title Short title explaining what failed (default: "Something went wrong")
 * @param message User-friendly error message description
 * @param buttonText Text for the action button (default: "Retry")
 * @param icon The leading error icon (default: CloudOff)
 * @param onRetry Action to execute when user clicks the retry button
 */
@Composable
fun AppErrorState(
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    message: String = "We encountered a problem loading this data. Please check your connection and try again.",
    buttonText: String = "Retry",
    icon: ImageVector = Icons.Default.CloudOff,
    onRetry: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(tokens.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(whiteBg)
                .border(BorderStroke(1.dp, sectionBorder), RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Soft Error Icon Container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(redBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = redText,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    fontSize = tokens.bodySmall,
                    color = close_color,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(4.dp))

            // Primary Retry Button
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.fieldHeight),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}