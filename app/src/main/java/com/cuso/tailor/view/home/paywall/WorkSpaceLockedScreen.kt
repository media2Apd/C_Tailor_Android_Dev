package com.cuso.tailor.view.home.paywall

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*

@Composable
fun WorkspaceLockedScreen(
    onUnlockWorkspace: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Block hardware back button completely - cannot be dismissed
    BackHandler(enabled = true) {
        // Intentionally do nothing to prevent bypassing the lock screen
    }

    // Full screen dimmed backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8F95A3).copy(alpha = 0.85f))
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Centered Lock Card
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(26.dp),
            color = whiteBg,
            shadowElevation = 16.dp,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lock Icon Container with Sparkle Badge
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Light blue background container
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(primary_light),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Top-right Sparkle Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Title
                Text(
                    text = "Workspace Access Locked",
                    fontSize = tokens.h1,
                    fontWeight = FontWeight.Bold,
                    color = title_color,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                // Subtitle
                Text(
                    text = "Your workspace requires an active subscription to access and manage core ERP modules (Tailoring Operations, Sales, Measurements, and Inventory).",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = tokens.bodySmall.times(1.35f)
                )

                Spacer(Modifier.height(24.dp))

                // Feature Checklist Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary_background)
                        .border(1.dp, grey_border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        WorkspaceBenefitRow(text = "Full Stitching & Tailoring Operations Workflow")
                        WorkspaceBenefitRow(text = "Customer Measurements & Order Tracking")
                        WorkspaceBenefitRow(text = "GST Invoices, Billing, and Financial Analytics")
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Primary CTA Button
                Button(
                    onClick = onUnlockWorkspace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight * 1.25f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Choose a Plan & Unlock Workspace",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = whiteBg
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Secondary Back to Login Button
                OutlinedButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight * 1.25f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, sectionBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text(
                        text = "Back to Login",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkspaceBenefitRow(text: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = complete_button_bg,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
    }
}