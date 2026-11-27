package com.cuso.tailor.view.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*

@Composable
fun PlanLimitDialog(
    title: String,
    message: String,
    resourceLabel: String = "",
    usedCount: Int = 0,
    maxLimit: Int = 0,
    onDismiss: () -> Unit,
    onUpgradePlan: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val safeLimit = maxLimit.coerceAtLeast(1)
    val progress = (usedCount.toFloat() / safeLimit.toFloat()).coerceIn(0f, 1f)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(tokens.cardCornerRadius * 1.4f),
            color = whiteBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.cardPadding, vertical = tokens.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top-right close icon
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = close_color,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                // Dialog title
                Text(
                    text = title,
                    fontSize = tokens.h2,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Subtitle explanation
                Text(
                    text = message,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                // Usage progress box (only shown if resourceLabel is provided)
                if (resourceLabel.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = Primary_background,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = resourceLabel,
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$usedCount",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = redText
                                    )
                                    Text(
                                        text = " / $maxLimit",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                color = redText,
                                trackColor = redBg
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                }

                // Upgrade Plan pill button
                Button(
                    onClick = onUpgradePlan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "Upgrade Plan",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }
    }
}