package com.cuso.tailor.view.home.security.mobile_access

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.cuso.tailor.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.MobileDeviceItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.BlockDeviceDialog
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.security.mobile_access.components.DeviceStatusBadge
import com.cuso.tailor.view.home.security.mobile_access.components.PlatformChip
import com.cuso.tailor.view.home.security.mobile_access.components.TrustBadge
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun DeviceDetailScreen(
    device: MobileDeviceItem,
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var showBlockModal by remember { mutableStateOf(false) }

    val isBlocked = device.approvalStatus == "blocked"
    val isApproved = device.approvalStatus == "approved"

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0) ,
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = device.deviceName.ifBlank { "Mobile Device" },
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
            // Header Card
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
                            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_mobile) ,contentDescription = null, tint = Primary, modifier = Modifier.size(26.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(device.deviceName, fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("${device.userId?.fullName ?: "—"} • ${device.userId?.role ?: "User"}", fontSize = tokens.caption, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                DeviceStatusBadge(device.approvalStatus)
                                TrustBadge(device.trustStatus)
                            }
                        }
                    }

                    HorizontalDivider(color = grey_border)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!isBlocked) {
                            OutlinedButton(
                                onClick = { showBlockModal = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = redText),
                                border = BorderStroke(1.dp, redText.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                            ) {
                                Text("Block Device", fontSize = tokens.bodySmall)
                            }
                        }

                        if (!isApproved && !device.isJailbrokenOrRooted) {
                            Button(
                                onClick = { viewModel.approveMobileDevice(device.id, onBack) },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                            ) {
                                Text("Approve", color = whiteBg, fontSize = tokens.bodySmall)
                            }
                        }
                    }
                }
            }

            // Jailbreak Banner
            if (device.isJailbrokenOrRooted) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(redBg).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = redText, modifier = Modifier.size(18.dp))
                    Text("Jailbreak/root was detected on this device. It cannot be approved.", fontSize = tokens.caption, color = redText)
                }
            }

            // Left & Right Info Cards
            Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, grey_border)) {
                Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SYSTEM INFORMATION", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Platform", fontSize = 10.sp, color = mutedText); PlatformChip(device.platform) }
                        Column { Text("OS Version", fontSize = 10.sp, color = mutedText); Text(device.osVersion.ifBlank { "—" }, fontSize = tokens.bodySmall, color = TextPrimary) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("App Version", fontSize = 10.sp, color = mutedText); Text("${device.appVersion} (Up to date)", fontSize = tokens.bodySmall, color = TextPrimary) }
                        Column { Text("Device ID", fontSize = 10.sp, color = mutedText); Text(device.maskedDeviceId ?: "XXXX-XXXX-93A2", fontSize = tokens.bodySmall, color = TextPrimary) }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, grey_border)) {
                Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ACTIVITY", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Last Active", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text(device.lastActiveAt?.take(16)?.replace('T', ' ') ?: "Just now", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = greentext)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Last Login", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text(device.lastLoginAt?.take(16)?.replace('T', ' ') ?: "—", fontSize = tokens.bodySmall, color = TextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Registered Date", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text(device.registeredAt?.take(10) ?: "—", fontSize = tokens.bodySmall, color = TextPrimary)
                    }
                }
            }
        }

        if (showBlockModal) {
            BlockDeviceDialog(
                deviceName = device.deviceName.ifBlank { "Mobile Device" },
                onDismiss = { showBlockModal = false },
                onConfirm = {
                    showBlockModal = false
                    viewModel.blockMobileDevice(device.id, onBack)
                }
            )
        }
    }
}