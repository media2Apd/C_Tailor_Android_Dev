package com.cuso.tailor.view.home.it.pos_billing

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
import androidx.compose.ui.window.Dialog
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.toTitleCase
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun PosDetailScreen(
    posTerminalId: String,
    viewModel: ItViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val terminal by viewModel.posTerminalDetail.collectAsState()
    val isLoading by viewModel.isLoadingPosDetail.collectAsState()
    val errorMessage by viewModel.posDetailError.collectAsState()

    val isReporting by viewModel.isReportingIssue.collectAsState()
    val isRestarting by viewModel.isRestartingTerminal.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var issueTypeInput by remember { mutableStateOf("") }

    LaunchedEffect(posTerminalId) {
        viewModel.fetchPosTerminalDetail(posTerminalId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearPosTerminalDetail()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "POS Details",
                subtitle = "IT • POS & Billing Technology",
                onClose = onBack
            )
        }
    ) { paddingValues ->
        if (isLoading && terminal == null) {
            ListSkeleton()
        } else if (terminal == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "POS Terminal details not found",
                    color = mutedText,
                    fontSize = 14.sp
                )
            }
        } else {
            val t = terminal!!
            val isOnline = t.status.equals("Online", ignoreCase = true)
            val isWarning = t.status.equals("Warning", ignoreCase = true)
            val statusBg = when {
                isOnline -> greenBg
                isWarning -> orangeBg
                else -> redBg
            }
            val statusText = when {
                isOnline -> greentext
                isWarning -> orangeText
                else -> redText
            }

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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = t.posId,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Text(
                                    text = "Terminal${t.terminalId}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = t.branchName ?: "Store",
                                        fontSize = 12.sp,
                                        color = mutedText
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "• ${t.status.toTitleCase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = statusText
                                )
                            }
                        }

                        HorizontalDivider(color = sectionBorder, thickness = 0.8.dp)

                        // 3 Action Buttons: Edit | Report | Restart
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {},
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, null, tint = title_color, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit", color = title_color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            // Report Button
                            OutlinedButton(
                                onClick = {
                                    issueTypeInput = t.activeIssue?.takeIf { it != "None" } ?: "Paper Jam"
                                    showReportDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Icon(Icons.Outlined.ReportProblem, null, tint = title_color, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Report", color = title_color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            // Restart Button
                            Button(
                                onClick = { showRestartDialog = true },
                                enabled = !isRestarting,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(42.dp)
                            ) {
                                if (isRestarting) {
                                    CircularProgressIndicator(
                                        color = whiteBg,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Outlined.RestartAlt, null, tint = whiteBg, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Restart", color = whiteBg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // ─── 2. Current Status Card ───
                PosSectionCard(
                    icon = null,
                    title = "Current Status"
                ) {
                    PosStatusRow(
                        label = "Status",
                        value = "• ${t.status.toTitleCase()}",
                        valueColor = statusText
                    )
                    Spacer(Modifier.height(10.dp))
                    PosStatusRow(label = "Assigned to", value = t.assignedToClerk ?: "Unassigned")
                    Spacer(Modifier.height(10.dp))
                    PosStatusRow(label = "Last activity", value = t.lastActivityFormatted ?: "2 mins ago")
                    Spacer(Modifier.height(10.dp))
                    PosStatusRow(label = "Last sync", value = t.lastSyncFormatted ?: "12:46 PM")
                }

                // ─── 3. Identity & Location Card ───
                PosSectionCard(
                    icon = Icons.Outlined.Info,
                    title = "Identity & Location"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PosInfoBox(label = "POS ID", value = t.posId, modifier = Modifier.weight(1f))
                        PosInfoBox(label = "Terminal ID", value = t.terminalId, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PosInfoBox(label = "Store", value = t.storeCode ?: "#04", modifier = Modifier.weight(1f))
                        PosInfoBox(label = "Branch", value = t.branchName ?: "Chennai", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = cardBgLight,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Counter", fontSize = 10.sp, color = mutedText)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = t.counterName ?: "Billing Counter",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(primary_light)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Primary Lane",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                // ─── 4. Hardware & Software Card ───
                PosSectionCard(
                    icon = Icons.Outlined.Memory,
                    title = "Hardware & Software"
                ) {
                    val hs = t.hardwareSoftware
                    PosSpecRow(label = "Device Model", value = hs?.deviceModel ?: "Dell POS Terminal")
                    HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))
                    PosSpecRow(label = "OS", value = hs?.os ?: "Windows 10 IoT")
                    HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))
                    PosSpecRow(
                        label = "POS Version",
                        value = hs?.posVersion ?: "v4.8.2-stable",
                        isValueBadge = true
                    )
                }

                // ─── 5. Peripherals & Network Card ───
                PosSectionCard(
                    icon = Icons.Outlined.Wifi,
                    title = "Peripherals & Network",
                    trailingBadge = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(greenBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "All Normal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = greentext
                            )
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = cardBgLight,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "IP Address", fontSize = 12.sp, color = mutedText)
                            Text(
                                text = t.ipAddress ?: "192.168.1.45",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PeripheralCell(
                            label = "Printer",
                            status = if (t.printerStatus.equals("JAMMED", ignoreCase = true)) "Jammed" else "Online",
                            color = if (t.printerStatus.equals("JAMMED", ignoreCase = true)) redText else greentext,
                            modifier = Modifier.weight(1f)
                        )
                        PeripheralCell(
                            label = "Scanner",
                            status = t.scannerStatus ?: "Ready",
                            color = greentext,
                            modifier = Modifier.weight(1f)
                        )
                        PeripheralCell(
                            label = "Cash Drawer",
                            status = t.cashDrawerStatus ?: "Closed",
                            color = greentext,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            // ─── Modal Dialog: Report Issue ───
            if (showReportDialog) {
                Dialog(onDismissRequest = { showReportDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = whiteBg,
                        modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            Text(
                                text = "Report Terminal Issue",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Submit an incident ticket under IT Service Desk for ${t.terminalName.ifBlank { t.posId }}.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(Modifier.height(14.dp))

                            Text(
                                text = "Issue Type",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            Spacer(Modifier.height(4.dp))

                            FormTextField(
                                value = issueTypeInput,
                                onValueChange = { issueTypeInput = it },
                                placeholder = "e.g. Paper Jam, Printer Offline, Screen Freeze"
                            )

                            Spacer(Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showReportDialog = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BorderGray)
                                ) {
                                    Text("Cancel", color = title_color, fontSize = tokens.bodySmall)
                                }

                                Button(
                                    onClick = {
                                        if (issueTypeInput.isBlank()) {
                                            DynamicIslandManager.showError("Please specify the issue type")
                                            return@Button
                                        }
                                        showReportDialog = false
                                        val targetId = t.effectiveId
                                        viewModel.reportPosTerminalIssue(
                                            terminalId = targetId,
                                            issueType = issueTypeInput,
                                            onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                                            onError = { err -> DynamicIslandManager.showError(err) }
                                        )
                                    },
                                    enabled = !isReporting,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Text(
                                        text = if (isReporting) "Submitting..." else "Submit Ticket",
                                        color = whiteBg,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── Modal Dialog: Remote Restart Confirmation ───
            if (showRestartDialog) {
                Dialog(onDismissRequest = { showRestartDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = whiteBg,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            Text(
                                text = "Restart Terminal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Are you sure you want to dispatch a remote reboot command to '${t.terminalName.ifBlank { t.posId }}'? This will briefly interrupt billing operations on this lane.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showRestartDialog = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BorderGray)
                                ) {
                                    Text("Cancel", color = title_color, fontSize = tokens.bodySmall)
                                }

                                Button(
                                    onClick = {
                                        showRestartDialog = false
                                        val targetId = t.effectiveId
                                        viewModel.restartPosTerminal(
                                            terminalId = targetId,
                                            onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                                            onError = { err -> DynamicIslandManager.showError(err) }
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Text("Restart Now", color = whiteBg, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Reusable Sub-Components ───

@Composable
private fun PosSectionCard(
    icon: ImageVector?,
    title: String,
    trailingBadge: (@Composable () -> Unit)? = null,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
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
                    }
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                }

                trailingBadge?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun PosStatusRow(
    label: String,
    value: String,
    valueColor: Color = title_color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = mutedText)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

@Composable
private fun PosInfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = cardBgLight,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 10.sp, color = mutedText)
            Spacer(Modifier.height(3.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = title_color)
        }
    }
}

@Composable
private fun PosSpecRow(
    label: String,
    value: String,
    isValueBadge: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = mutedText)
        if (isValueBadge) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(cardBgLight)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = title_color)
            }
        } else {
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = title_color)
        }
    }
}

@Composable
private fun PeripheralCell(
    label: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = cardBgLight,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = mutedText)
            Spacer(Modifier.height(3.dp))
            Text(text = status, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}