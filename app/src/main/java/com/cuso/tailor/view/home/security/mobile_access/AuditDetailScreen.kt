package com.cuso.tailor.view.home.security.mobile_access

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.MobileSecurityAuditItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.BlockDeviceDialog
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.toTitleCase
import com.cuso.tailor.view.home.security.mobile_access.components.SeverityBadge
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun AuditDetailScreen(
    audit: MobileSecurityAuditItem,
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var showBlockModal by remember { mutableStateOf(false) }
    val isResolved = audit.status.equals("resolved", ignoreCase = true)

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0) ,
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = audit.eventType.replace('_', ' ').toTitleCase(),
                onClose = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, grey_border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_mobile) ,contentDescription = null, tint = Primary, modifier = Modifier.size(26.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(audit.eventType.replace('_', ' ').toTitleCase(), fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                            Text(audit.timestamp?.take(19)?.replace('T', ' ') ?: "—", fontSize = tokens.caption, color = TextSecondary)
                        }

                        SeverityBadge(audit.severity)
                    }

                    HorizontalDivider(color = grey_border)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showBlockModal = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = redText),
                            border = BorderStroke(1.dp, redText.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                        ) {
                            Text("Block User", fontSize = tokens.bodySmall)
                        }

                        if (!isResolved) {
                            Button(
                                onClick = { viewModel.resolveMobileAudit(audit.id, onBack) },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                            ) {
                                Text("Mark Resolved", color = whiteBg, fontSize = tokens.bodySmall)
                            }
                        }
                    }
                }
            }

            if (isResolved) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(greenBg).padding(12.dp)
                ) {
                    Text("Resolved event.", fontSize = tokens.bodySmall, color = greentext, fontWeight = FontWeight.Medium)
                }
            }

            // User Context
            Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, grey_border)) {
                Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("USER CONTEXT", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Name", fontSize = 10.sp, color = mutedText); Text(audit.userName ?: "—", fontSize = tokens.bodySmall, color = TextPrimary) }
                        Column { Text("Email", fontSize = 10.sp, color = mutedText); Text(audit.userEmail ?: "—", fontSize = tokens.bodySmall, color = TextPrimary) }
                    }
                    Column { Text("Role", fontSize = 10.sp, color = mutedText); Text(audit.userId?.role ?: "Finance Manager", fontSize = tokens.bodySmall, color = TextPrimary) }
                }
            }

            // Device Context
            Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, grey_border)) {
                Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("DEVICE CONTEXT", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Device Model", fontSize = 10.sp, color = mutedText); Text(audit.deviceModel ?: "Unknown Device", fontSize = tokens.bodySmall, color = TextPrimary) }
                        Column { Text("Version", fontSize = 10.sp, color = mutedText); Text(audit.osInfo ?: "Android 14", fontSize = tokens.bodySmall, color = TextPrimary) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("IP Address", fontSize = 10.sp, color = mutedText); Text(audit.ipAddress ?: "—", fontSize = tokens.bodySmall, color = TextPrimary) }
                        Column { Text("MDM Status", fontSize = 10.sp, color = mutedText); Text(audit.mdmStatus ?: "Unmanaged", fontSize = tokens.bodySmall, color = TextPrimary) }
                    }
                }
            }

            // Dark JSON Terminal Box
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = TitleColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("TECHNICAL DETAILS", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = iconMuted)
                    Text(
                        text = """
                        {
                          "event_id": "${audit.id}",
                          "eventType": "${audit.eventType}",
                          "severity": "${audit.severity}",
                          "deviceModel": "${audit.deviceModel ?: "-"}",
                          "ipAddress": "${audit.ipAddress ?: "-"}"
                        }
                        """.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = chartEmerald,
                        lineHeight = 16.sp
                    )

                    audit.detailsSummary?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, fontSize = tokens.caption, color = light_grey)
                    }
                }
            }
        }

        if (showBlockModal) {
            BlockDeviceDialog(
                deviceName = audit.userEmail ?: audit.userName ?: "User's Device",
                onDismiss = { showBlockModal = false },
                onConfirm = {
                    showBlockModal = false
                    viewModel.blockUserFromMobileAudit(audit.id, onBack)
                }
            )
        }
    }
}