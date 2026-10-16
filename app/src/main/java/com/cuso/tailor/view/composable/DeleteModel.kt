package com.cuso.tailor.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.redText

@Composable
fun DeleteModel(
    title: String = "Delete Project",
    message: String = "You're going to delete the \"Demo\"\nproject. Are you sure?",
    checkboxText: String = "I confirm this deletion",
    cancelText: String = "No, Keep It.",
    confirmText: String = "Yes, Delete!",
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var isConfirmed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Warning icon inside light red circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(redText.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_amber),
                            contentDescription = null,
                            tint = redText,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = title,
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2D3748),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = message,
                        fontSize = tokens.bodyMedium,
                        color = Color(0xFF2D3748),
                        textAlign = TextAlign.Center,
                        lineHeight = tokens.bodyMedium.times(1.4f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirmation checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isConfirmed = !isConfirmed }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AppCheckbox(
                            checked = isConfirmed,
                            onCheckedChange = { isConfirmed = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = checkboxText,
                            fontSize = tokens.bodySmall,
                            color = Color(0xFF374151)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F2F4),
                                contentColor = Color(0xFF2D3748)
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = cancelText,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = onDelete,
                            enabled = isConfirmed,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = redText,
                                contentColor = Color.White,
                                disabledContainerColor = redText.copy(alpha = 0.35f),
                                disabledContentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = confirmText,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Close (X) icon - top right
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}