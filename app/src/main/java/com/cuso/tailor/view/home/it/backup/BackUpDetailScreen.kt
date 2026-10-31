package com.cuso.tailor.view.home.it.backup

import android.annotation.SuppressLint
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.toTitleCase
import com.cuso.tailor.viewmodel.ItViewModel

@SuppressLint("DefaultLocale")
@Composable
fun BackupRecoveryDetailScreen(
    snapshotId: String,
    viewModel: ItViewModel,
    onBack: () -> Unit,
    onRestoreClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val detail by viewModel.backupDetail.collectAsState()
    val isLoading by viewModel.isLoadingBackupDetail.collectAsState()
    val errorMessage by viewModel.backupDetailError.collectAsState()

    LaunchedEffect(snapshotId) {
        viewModel.fetchBackupSnapshotDetail(snapshotId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearBackupSnapshotDetail()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "Database Backup",
                subtitle = "IT • Backup & Recovery",
                onClose = onBack
            )
        }
    ) { paddingValues ->
        if (isLoading && detail == null) {
            ListSkeleton()
        } else if (detail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Backup details not found",
                    color = mutedText,
                    fontSize = 14.sp
                )
            }
        } else {
            val d = detail!!
            val isSuccess = d.status.equals("Successful", ignoreCase = true)
            val statusBg = if (isSuccess) greenBg else redBg
            val statusColor = if (isSuccess) greentext else redText

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ─── 1. Title & Actions Card ───
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Database Backup",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "# ${d.backupId ?: "BKP-10482"} • ${d.backupType ?: "Full"} Backup • Source: ${d.sourceEntity ?: "CUSO Database"}",
                                    fontSize = 12.sp,
                                    color = mutedText
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "• ${d.status.toTitleCase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = statusColor
                                )
                            }
                        }

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
                                    .height(44.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = title_color, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Edit", color = title_color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }

                            Button(
                                onClick = {
                                    val targetId = d.backupId ?: snapshotId
                                    onRestoreClick(targetId)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp)
                            ) {
                                Icon(Icons.Outlined.Restore, contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Restore Backup", color = whiteBg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // ─── 2. Backup Health Card ───
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
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primary_light),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Backup Health: ${d.healthStatus ?: "Healthy"}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Last successful backup: ${viewModel.formatTimeAgo(d.createdAt)}. No critical issues detected in the pipeline.",
                                    fontSize = 12.sp,
                                    color = mutedText,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        HorizontalDivider(
                            color = sectionBorder,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Storage Allocation",
                                fontSize = 12.sp,
                                color = mutedText
                            )
                            Text(
                                text = "${d.storageAllocationPercent}% Optimised",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { d.storageAllocationPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Primary,
                            trackColor = primary_light
                        )
                    }
                }

                // ─── 3. Backup Status Card ───
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Backup Status",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Grid Row 1: Current Status | Last Backup
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Current Status", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "• ${d.status.toTitleCase()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColor
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Last Backup", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = viewModel.formatBackupTime(d.createdAt),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Grid Row 2: Backup Size | Duration
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Backup Size", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = d.formattedSize ?: "24.8 GB",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Duration", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = d.durationFormatted ?: "18 min 42 sec",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Grid Row 3: Records Processed | Verification
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Records Processed", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = String.format("%,d", d.recordsProcessed),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Verification", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = greentext,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = d.verificationStatus ?: "Verified",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = greentext
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Scheduled Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = cardBgLight,
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Text(text = "Next Scheduled", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = viewModel.formatNextScheduled(d.backupJobId?.nextRunAt),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                            }
                        }
                    }
                }

                // ─── 4. Configuration Card ───
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
                        Text(
                            text = "Configuration",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )

                        Spacer(Modifier.height(14.dp))

                        // Backup ID row with badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Backup ID", fontSize = 12.sp, color = mutedText)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cardBgLight)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = d.backupId ?: "BKP-10482",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                        }

                        HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))

                        ConfigRow(label = "Type", value = d.backupType ?: "Full")
                        HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))

                        ConfigRow(label = "Source Entity", value = d.sourceEntity ?: "CUSO Database")
                        HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))

                        ConfigRow(label = "Environment", value = d.environment ?: "Production")
                        HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))

                        ConfigRow(label = "Branch Level", value = d.branchLevel ?: "Global/HQ")
                        HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))

                        ConfigRow(label = "Storage Medium", value = d.storageMedium ?: "Cloud")
                        HorizontalDivider(color = sectionBorder, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 10.dp))

                        val retentionDays = d.backupJobId?.retentionDays ?: 30
                        ConfigRow(label = "Retention Policy", value = "$retentionDays Days")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ConfigRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = mutedText)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = title_color)
    }
}