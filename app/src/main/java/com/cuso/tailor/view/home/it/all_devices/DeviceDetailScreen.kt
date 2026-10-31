package com.cuso.tailor.view.home.it.all_devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.toTitleCase
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun DeviceDetailScreen(
    deviceId: String,
    viewModel: ItViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val device by viewModel.deviceDetail.collectAsState()
    val isLoading by viewModel.isLoadingDeviceDetail.collectAsState()
    val errorMessage by viewModel.deviceDetailError.collectAsState()

    LaunchedEffect(deviceId) {
        viewModel.fetchDeviceDetail(deviceId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearDeviceDetail()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(title = "Device Detail", onClose = onBack)
        }
    ) { paddingValues ->
        if (isLoading && device == null) {
            ListSkeleton()
        } else if (device == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Device details not found",
                    color = mutedText,
                    fontSize = 14.sp
                )
            }
        } else {
            val d = device!!
            val isOnline = d.status.equals("Online", ignoreCase = true)
            val statusBg = if (isOnline) greenBg else redBg
            val statusText = if (isOnline) greentext else redText

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ─── 1. Identity & Actions Card ───
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = d.deviceName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "• ${d.status.toTitleCase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = statusText
                                )
                            }
                        }

                        Text(
                            text = "${d.deviceType} • ${d.deviceNo}",
                            fontSize = 13.sp,
                            color = mutedText
                        )

                        HorizontalDivider(color = sectionBorder, thickness = 0.8.dp)

                        // Action Buttons: Edit | Assign
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {},
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, null, tint = title_color, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Edit", color = title_color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }

                            OutlinedButton(
                                onClick = {},
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Icon(Icons.Outlined.PersonAdd, null, tint = title_color, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Assign", color = title_color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Full width: Adjust Stock Button
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(Icons.Outlined.Inventory2, null, tint = whiteBg, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Adjust Stock", color = whiteBg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // ─── 2. Device Overview Card ───
                DeviceSectionCard(
                    icon = Icons.Outlined.Info,
                    title = "Device Overview"
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SpecColumn(label = "Device Name", value = d.deviceName, modifier = Modifier.weight(1f))
                        SpecColumn(label = "Device Type", value = d.deviceType, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SpecColumn(label = "Manufacturer", value = d.manufacturer ?: "--", modifier = Modifier.weight(1f))
                        SpecColumn(label = "Model", value = d.model ?: "--", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SpecColumn(label = "Serial Number", value = d.serialNumber, modifier = Modifier.weight(1f))
                        SpecColumn(label = "Assigned To", value = d.assignedTo?.name ?: "Unassigned", modifier = Modifier.weight(1f))
                    }
                }

                // ─── 3. Operational Status Card ───
                DeviceSectionCard(
                    icon = Icons.Outlined.Bolt,
                    title = "Operational Status"
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Status", fontSize = 11.sp, color = mutedText)
                            Spacer(Modifier.height(3.dp))
                            Text("• ${d.status.toTitleCase()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = statusText)
                        }
                        SpecColumn(label = "Last Seen", value = d.lastSeenFormatted ?: "2 mins ago", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SpecColumn(label = "Maintenance", value = d.maintenanceStatus, modifier = Modifier.weight(1f))
                        SpecColumn(label = "IP Address", value = d.ipAddress ?: d.plainIp ?: "--", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SpecColumn(label = "Purchase Date", value = viewModel.formatDeviceDate(d.purchaseDate), modifier = Modifier.weight(1f))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Warranty", fontSize = 11.sp, color = mutedText)
                            Spacer(Modifier.height(3.dp))
                            Text("Active until ${viewModel.formatDeviceDate(d.warrantyExpiry)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = greentext)
                        }
                    }
                }

                // ─── 4. Hardware Specifications Card ───
                DeviceSectionCard(
                    icon = Icons.Outlined.Computer,
                    title = "Hardware Specifications"
                ) {
                    val specs = d.hardwareSpecs
                    HardwareSpecRow("Processor", specs?.processor ?: "--")
                    HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))
                    HardwareSpecRow("Memory (RAM)", specs?.ram ?: "--")
                    HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))
                    HardwareSpecRow("Storage", specs?.storage ?: "--")
                    HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))
                    HardwareSpecRow("Operating System", specs?.os ?: "--")
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ─── Sub-Components ───

@Composable
private fun DeviceSectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
            }
            content()
        }
    }
}

@Composable
private fun SpecColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 11.sp, color = mutedText)
        Spacer(Modifier.height(3.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = title_color)
    }
}

@Composable
private fun HardwareSpecRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = mutedText)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = title_color)
    }
}