@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.security.backup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.BackupItemDto
import com.cuso.tailor.model.security.BackupTimelineStepDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.MiniSwitch
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun DataBackupDetailScreen(
    backupId: String,
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val backupDetail by viewModel.selectedBackupDetail.collectAsState()
    val isLoading by viewModel.isLoadingBackupDetail.collectAsState()
    val scrollState = rememberScrollState()

    var showRestoreSheet by remember { mutableStateOf(false) }

    LaunchedEffect(backupId) {
        viewModel.fetchBackupDetail(backupId)
    }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            TitleBar(
                title = "Backup Details",
                subtitle = backupId,
                onClose = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
            }
        } else if (backupDetail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Backup details not found",
                    fontSize = tokens.bodyMedium,
                    color = mutedText
                )
            }
        } else {
            val backup = backupDetail!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Card
                HeaderDetailCard(
                    backup = backup,
                    onDownloadReport = { viewModel.downloadBackupReport(backup.backupId) },
                    onRunAgain = { viewModel.fetchBackupDetail(backup.backupId) }
                )

                // Critical Restore Point Banner
                CriticalRestorePointCard(
                    backup = backup,
                    onUseForRecovery = { showRestoreSheet = true }
                )

                // Backup Summary Card
                BackupSummaryCard(backup = backup)

                // Verification Status Card
                VerificationCard(backup = backup)

                // Activity Timeline Card
                ActivityTimelineCard(timeline = backup.timeline)

                // Configuration Used Card
                ConfigurationUsedCard(backup = backup)

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showRestoreSheet && backupDetail != null) {
        RestoreBackupBottomSheet(
            backup = backupDetail!!,
            onDismiss = { showRestoreSheet = false },
            onConfirmRestore = {
                showRestoreSheet = false
            }
        )
    }
}

@Composable
private fun HeaderDetailCard(
    backup: BackupItemDto,
    onDownloadReport: () -> Unit,
    onRunAgain: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = backup.name,
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "ID: ${backup.backupId}",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }

                StatusBadge(
                    text = backup.status,
                    bgColor = greenBg,
                    textColor = greentext
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDownloadReport,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.dp, sectionBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Download Report",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = onRunAgain,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Run Again",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }
    }
}

@Composable
private fun CriticalRestorePointCard(
    backup: BackupItemDto,
    onUseForRecovery: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = light_blue,
        border = BorderStroke(1.dp, light_blue_border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Critical Restore Point",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                    Text(
                        text = "Available for zero-loss failover",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow("ID", backup.restorePointId ?: "RP-2026-08421")
                    DetailRow("Created", backup.schedule.time.ifEmpty { "Today 02:22 AM" })
                    DetailRow(
                        "Integrity",
                        "Passed",
                        valueColor = greentext,
                        showDot = true
                    )
                    DetailRow("Available for Recovery", "Yes")
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onUseForRecovery,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Use for Recovery",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = whiteBg
                )
            }
        }
    }
}

@Composable
private fun BackupSummaryCard(backup: BackupItemDto) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Backup Summary",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            SummaryItem("NAME", backup.name)
            SummaryItem("SOURCE", backup.sourceTarget)

            HorizontalDivider(color = grey_border, thickness = 1.dp)

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    SummaryItem("TYPE", backup.type)
                }
                Column(modifier = Modifier.weight(1f)) {
                    SummaryItem("SIZE", backup.sizeFormatted.ifEmpty { "42 GB" }, valueColor = Primary)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    SummaryItem("START TIME", "Today 02:18 AM")
                }
                Column(modifier = Modifier.weight(1f)) {
                    SummaryItem("END TIME", "Today 02:22 AM")
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    SummaryItem("DURATION", backup.executionMetrics.durationFormatted.ifEmpty { "4 min" })
                }
                Column(modifier = Modifier.weight(1f)) {
                    SummaryItem("INITIATED BY", backup.executionMetrics.initiatedBy)
                }
            }
        }
    }
}

@Composable
private fun VerificationCard(backup: BackupItemDto) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Verification",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            VerificationRowItem(
                icon = Icons.Default.CheckCircle,
                title = "Integrity Check",
                status = backup.verification.integrityCheck
            )
            VerificationRowItem(
                icon = Icons.Default.CheckCircle,
                title = "Checksum",
                status = backup.verification.checksum
            )
            VerificationRowItem(
                icon = Icons.Default.Shield,
                title = "Backup Readiness",
                status = backup.verification.backupReadiness
            )
        }
    }
}

@Composable
private fun ActivityTimelineCard(timeline: List<BackupTimelineStepDto>) {
    val tokens = LocalAppTokens.current
    val items = timeline.ifEmpty {
        listOf(
        BackupTimelineStepDto(title = "Verified", time = "02:22 AM"),
        BackupTimelineStepDto(title = "Completed", time = "02:22 AM"),
        BackupTimelineStepDto(title = "Transfer", time = "02:20 AM"),
        BackupTimelineStepDto(title = "Snapshot", time = "02:18 AM"),
        BackupTimelineStepDto(title = "Started", time = "02:18 AM")
    )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Activity Timeline",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            Spacer(Modifier.height(14.dp))

            items.forEachIndexed { index, step ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (index == items.lastIndex) headerGrey else complete_button_bg)
                        )
                        if (index < items.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(34.dp)
                                    .background(grey_border)
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = step.time,
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                        Text(
                            text = step.title,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigurationUsedCard(backup: BackupItemDto) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Configuration Used",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            ConfigDetailRow(
                icon = Icons.Default.Schedule,
                label = "SCHEDULE",
                value = "${backup.schedule.frequency} ${backup.schedule.time}"
            )
            ConfigDetailRow(
                icon = Icons.Default.Folder,
                label = "DESTINATION",
                value = backup.destination.target
            )
            ConfigDetailRow(
                icon = Icons.Default.Lock,
                label = "ENCRYPTION / COMPRESSION",
                value = if (backup.protection.encryption) "Enabled" else "Disabled"
            )
            ConfigDetailRow(
                icon = Icons.Default.CalendarMonth,
                label = "RETENTION",
                value = "${backup.retention.dailyDays} Days"
            )
        }
    }
}

@Composable
private fun VerificationRowItem(icon: ImageVector, title: String, status: String) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = cardBgLight,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = greentext,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary
                )
            }
            Text(
                text = status,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = greentext
            )
        }
    }
}

@Composable
private fun ConfigDetailRow(icon: ImageVector, label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                .background(primary_light),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    showDot: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = headerGrey)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(valueColor)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, valueColor: Color = TextPrimary) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(text = value, fontSize = tokens.bodySmall, color = valueColor, fontWeight = FontWeight.Normal)
    }
}

// -------------------------------------------------------------------------
// RESTORE BACKUP BOTTOM SHEET (MODAL)
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestoreBackupBottomSheet(
    backup: BackupItemDto,
    onDismiss: () -> Unit,
    onConfirmRestore: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTarget by remember { mutableStateOf("original") }
    var overwriteExisting by remember { mutableStateOf(false) }
    var verifyDataAfterRestore by remember { mutableStateOf(true) }
    var restartServices by remember { mutableStateOf(false) }
    var isConfirmedCheckbox by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = whiteBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(mutedText.copy(alpha = 0.5f))
            )
        },
        shape = RoundedCornerShape(
            topStart = tokens.cardCornerRadius,
            topEnd = tokens.cardCornerRadius
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Restore Backup",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Text(
                        text = "Review the selected backup and choose where data should be restored.",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = close_color)
                }
            }

            HorizontalDivider(color = grey_border)

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Backup summary box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = cardBgLight,
                    border = BorderStroke(1.dp, grey_border)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Backup summary",
                            fontSize = tokens.label,
                            color = headerGrey,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Name", fontSize = tokens.label, color = mutedText)
                                Text(backup.name, fontSize = tokens.caption, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ID", fontSize = tokens.label, color = mutedText)
                                Text(backup.backupId, fontSize = tokens.caption, color = TextPrimary)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Source", fontSize = tokens.label, color = mutedText)
                                Text(backup.sourceTarget, fontSize = tokens.caption, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Created", fontSize = tokens.label, color = mutedText)
                                Text("Today, 02:22 AM", fontSize = tokens.caption, color = TextPrimary)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Size", fontSize = tokens.label, color = mutedText)
                                Text(backup.sizeFormatted.ifEmpty { "227.16 KB" }, fontSize = tokens.caption, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Integrity", fontSize = tokens.label, color = mutedText)
                                Text("Passed", fontSize = tokens.caption, color = greentext, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Restore Destination
                Text(
                    text = "Restore Destination",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                DestinationOptionItem(
                    title = "Original Source",
                    subtitle = "Restore to original production source",
                    isSelected = selectedTarget == "original",
                    onClick = { selectedTarget = "original" }
                )

                DestinationOptionItem(
                    title = "Alternate Environment",
                    subtitle = "Restore to another isolated environment",
                    isSelected = selectedTarget == "alternate",
                    onClick = { selectedTarget = "alternate" }
                )

                MiniSwitchRow("Overwrite Existing Data", "Clear target before restore", overwriteExisting) { overwriteExisting = it }
                MiniSwitchRow("Verify Data After Restore", "Run integrity check post-recovery", verifyDataAfterRestore) { verifyDataAfterRestore = it }
                MiniSwitchRow("Restart Services", "Automatically reboot attached services", restartServices) { restartServices = it }

                // Metrics Strip
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = cardBgLight,
                    border = BorderStroke(1.dp, grey_border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Data Size", fontSize = tokens.label, color = mutedText)
                            Text("42 GB", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Est. Duration", fontSize = tokens.label, color = mutedText)
                            Text("18-25m", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Downtime", fontSize = tokens.label, color = mutedText)
                            Text("~10m", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = redText)
                        }
                    }
                }

                // Warning Alert Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(redBg)
                        .border(1.dp, redBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = redText, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Restoring to original source may affect existing data. Ensure active connections are drained before proceeding.",
                            fontSize = tokens.caption,
                            color = redText
                        )
                    }
                }

                // Confirmation Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isConfirmedCheckbox = !isConfirmedCheckbox }
                ) {
                    AppCheckbox(
                        checked = isConfirmedCheckbox,
                        onCheckedChange = { isConfirmedCheckbox = it }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "I understand that restoring this backup may affect existing data.",
                        fontSize = tokens.caption,
                        color = TextPrimary
                    )
                }
            }

            HorizontalDivider(color = grey_border)

            // Sheet Bottom Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.dp, sectionBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Text("Cancel", fontSize = tokens.bodySmall, color = TextSecondary)
                }

                Button(
                    onClick = onConfirmRestore,
                    enabled = isConfirmedCheckbox,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = disabled
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                    Spacer(Modifier.width(6.dp))
                    Text("Start Recovery", fontSize = tokens.bodySmall, color = whiteBg)
                }
            }
        }
    }
}

@Composable
private fun DestinationOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = whiteBg,
        border = BorderStroke(1.dp, if (isSelected) Primary else sectionBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Primary)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(text = title, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(text = subtitle, fontSize = tokens.caption, color = headerGrey)
            }
        }
    }
}

@Composable
private fun MiniSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = tokens.bodySmall, color = TextPrimary)
            Text(text = subtitle, fontSize = tokens.caption, color = headerGrey)
        }
        MiniSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}