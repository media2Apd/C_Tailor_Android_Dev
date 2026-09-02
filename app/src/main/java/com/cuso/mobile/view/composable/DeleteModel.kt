package com.cuso.mobile.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.whiteBg

@Composable
fun DeleteModel(
    title: String = "You are about to delete a product",
    message: String = "This will delete your product from catalog.\nAre you sure?",
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var isConfirmed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Professional width for dialogs
                .wrapContentHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // 1. Main Dialog Card
            Surface(
                modifier = Modifier
                    .padding(top = 28.dp) // Space for the floating icon
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(tokens.screenPadding)
                        .padding(top = 24.dp), // Extra padding to clear the icon
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close Icon (Top Right)
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier
                                .offset(y = (-10).dp)
                                .size(24.dp)
                                .clickable { onDismiss() }
                        )
                    }

                    // Title
                    Text(
                        text = title,
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtitle / Message
                    Text(
                        text = message,
                        fontSize = tokens.bodyMedium,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        lineHeight = tokens.bodyMedium.times(1.3f)
                    )

                    Spacer(modifier = Modifier.height(tokens.screenPadding))

                    // Checkbox Integration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isConfirmed = !isConfirmed }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Checkbox(
                            checked = isConfirmed,
                            onCheckedChange = { isConfirmed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = redText,
                                checkmarkColor = whiteBg
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "I confirm this deletion",
                            fontSize = tokens.bodySmall,
                            color = Color(0xFF374151)
                        )
                    }

                    Spacer(modifier = Modifier.height(tokens.screenPadding))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                "Cancel",
                                color = Color(0xFF6B7280),
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = onDelete,
                            enabled = isConfirmed, // Dynamic enable logic
                            colors = ButtonDefaults.buttonColors(
                                containerColor = redText,
                                disabledContainerColor = Color(0xFFFEE2E2)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Delete",
                                color = if (isConfirmed) Color.White else redText.copy(alpha = 0.5f),
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Centered Floating Trash Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = redText,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}