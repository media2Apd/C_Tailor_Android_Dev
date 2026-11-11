package com.cuso.tailor.view.home.security.recovery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.RecoveryItemDto
import com.cuso.tailor.model.security.RecoveryTimelineStepDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun DataRecoveryDetailScreen(
    recoveryId: String,
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val recoveryDetail by viewModel.selectedRecoveryDetail.collectAsState()
    val isLoading by viewModel.isLoadingRecoveryDetail.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(recoveryId) {
        viewModel.fetchRecoveryDetail(recoveryId)
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Recovery Details",
                subtitle = recoveryId,
                onClose = onNavigateBack,
                trailingContent = {
                    StatusBadge(
                        text = recoveryDetail?.status ?: "Running",
                        textColor = Primary,
                        bgColor = primary_light
                    )
                }
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
        } else if (recoveryDetail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Recovery record not found",
                    fontSize = tokens.bodyMedium,
                    color = mutedText
                )
            }
        } else {
            val recovery = recoveryDetail!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Download Report Action Button
                Button(
                    onClick = {
                        viewModel.downloadBackupReport(recovery.backupId)
                    },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Download Report",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }

                // 1. RECOVERY PROGRESS GAUGE CARD
                RecoveryProgressGaugeCard(recovery = recovery)

                // 2. RECOVERY SUMMARY CARD
                RecoverySummaryCard(recovery = recovery)

                // 3. RECOVERY VERIFICATION CARD
                RecoveryVerificationCard(recovery = recovery)

                // 4. ACTIVITY TIMELINE CARD
                RecoveryTimelineCard(timeline = recovery.timeline)

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun RecoveryProgressGaugeCard(recovery: RecoveryItemDto) {
    val tokens = LocalAppTokens.current
    val progressPercent = recovery.progress.percentage.coerceIn(0, 100)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECOVERY PROGRESS",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                StatusBadge(
                    text = "Live Status",
                    textColor = Primary,
                    bgColor = primary_light
                )
            }

            Spacer(Modifier.height(16.dp))

            // Circular Progress Gauge
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = light_grey,
                    strokeWidth = 10.dp,
                    trackColor = light_grey,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progressPercent / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = Primary,
                    strokeWidth = 10.dp,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$progressPercent %",
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "RESTORED",
                        fontSize = tokens.label,
                        color = mutedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = recovery.progress.currentStepText.ifEmpty { "Restoring database files..." },
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = recovery.progress.estimatedTimeRemaining.ifEmpty { "10 minutes remaining" },
                    fontSize = tokens.caption,
                    color = headerGrey
                )
            }
        }
    }
}

@Composable
private fun RecoverySummaryCard(recovery: RecoveryItemDto) {
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
                text = "RECOVERY SUMMARY",
                fontSize = tokens.label,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            Column {
                Text("BACKUP ID", fontSize = tokens.label, color = mutedText)
                Text(recovery.backupId, fontSize = tokens.bodySmall, color = TextPrimary)
            }

            Column {
                Text("BACKUP SOURCE", fontSize = tokens.label, color = mutedText)
                Text(recovery.backupSource.ifEmpty { "Database Backup" }, fontSize = tokens.bodySmall, color = TextPrimary)
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SOURCE TARGET", fontSize = tokens.label, color = mutedText)
                    Text(recovery.sourceTarget.ifEmpty { "Production Database" }, fontSize = tokens.caption, color = TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("DESTINATION", fontSize = tokens.label, color = mutedText)
                    Text(recovery.destination.targetEnvironment.ifEmpty { "Original Source" }, fontSize = tokens.caption, color = TextPrimary)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("BACKUP TYPE", fontSize = tokens.label, color = mutedText)
                    Text(recovery.backupType, fontSize = tokens.caption, color = TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("BACKUP SIZE", fontSize = tokens.label, color = mutedText)
                    Text(recovery.backupSizeFormatted.ifEmpty { "42 GB" }, fontSize = tokens.caption, color = TextPrimary)
                }
            }

            Column {
                Text("STARTED", fontSize = tokens.label, color = mutedText)
                Text("Today, 10:42 AM", fontSize = tokens.caption, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun RecoveryVerificationCard(recovery: RecoveryItemDto) {
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
                text = "RECOVERY VERIFICATION",
                fontSize = tokens.label,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            VerificationCheckRow(
                icon = Icons.Default.Refresh,
                title = "Data Integrity",
                status = recovery.verification.dataIntegrity
            )
            VerificationCheckRow(
                icon = Icons.Default.Storage,
                title = "Post-Restore",
                status = recovery.verification.postRestore
            )
            VerificationCheckRow(
                icon = Icons.Default.Bolt,
                title = "Service Health",
                status = recovery.verification.serviceHealth
            )
        }
    }
}

@Composable
private fun VerificationCheckRow(icon: ImageVector, title: String, status: String) {
    val tokens = LocalAppTokens.current
    val isRunning = status.equals("Running", ignoreCase = true)
    val isPassed = status.equals("Passed", ignoreCase = true) || status.equals("Completed", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                    .background(cardBgLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
            }
            Spacer(Modifier.width(10.dp))
            Text(title, fontSize = tokens.bodySmall, color = TextPrimary)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                .background(if (isRunning) primary_light else if (isPassed) greenBg else light_grey)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = status,
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = if (isRunning) Primary else if (isPassed) greentext else headerGrey
            )
        }
    }
}

@Composable
private fun RecoveryTimelineCard(timeline: List<RecoveryTimelineStepDto>) {
    val tokens = LocalAppTokens.current
    val steps = timeline.ifEmpty {
        listOf(
        RecoveryTimelineStepDto(title = "Recovery started", time = "Today, 10:42 AM", status = "Completed"),
        RecoveryTimelineStepDto(title = "Restore point mounted", time = "Today, 10:43 AM", status = "Completed"),
        RecoveryTimelineStepDto(title = "Pre-restore validation completed", time = "Today, 10:44 AM", status = "Completed"),
        RecoveryTimelineStepDto(title = "Data restoration in progress", time = "Today, 10:46 AM", status = "Active"),
        RecoveryTimelineStepDto(title = "Post-restore verification", time = "", status = "Pending"),
        RecoveryTimelineStepDto(title = "Service health check", time = "", status = "Pending"),
        RecoveryTimelineStepDto(title = "Recovery completed", time = "", status = "Pending")
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
                text = "ACTIVITY TIMELINE",
                fontSize = tokens.label,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            Spacer(Modifier.height(14.dp))

            steps.forEachIndexed { index, step ->
                val isCompleted = step.status.equals("Completed", ignoreCase = true)
                val isActive = step.status.equals("Active", ignoreCase = true)

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(24.dp)
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Primary)
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(whiteBg)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(mutedText.copy(alpha = 0.5f))
                            )
                        }

                        if (index < steps.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(34.dp)
                                    .background(if (isCompleted) Primary.copy(alpha = 0.3f) else grey_border)
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = step.title,
                            fontSize = tokens.bodySmall,
                            fontWeight = if (isActive || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isActive) Primary else if (isCompleted) TextPrimary else headerGrey,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = step.time.ifBlank { "" },
                            fontSize = tokens.caption,
                            color = if (isActive) Primary else headerGrey
                        )
                    }
                }
            }
        }
    }
}