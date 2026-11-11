@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.security.recovery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.BackupItemDto
import com.cuso.tailor.model.security.InitiateRecoveryRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun DataRecoveryListScreen(
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRecoveryDetail: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val backups by viewModel.filteredRecoveryBackups.collectAsState()
    val isLoading by viewModel.isLoadingBackups.collectAsState()
    val searchQuery by viewModel.recoverySearchQuery.collectAsState()
    val isInitiating by viewModel.isInitiatingRecovery.collectAsState()

    var selectedBackupForRestore by remember { mutableStateOf<BackupItemDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchBackups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Title Bar
        TitleBar(
            title = "Data Recovery",
            onClose = onNavigateBack,
            trailingContent = {
                OutlinedButton(
                    onClick = { /* Export action */ },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(sectionBorder)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Export",
                        tint = TextSecondary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Export",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        )

        // Search Bar
        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { viewModel.onRecoverySearchQueryChange(it) },
            placeholder = "Search SO number, customer...",
            showFilterIcon = true,
            onFilterClick = {}
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
            }
        } else if (backups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recovery restore points found",
                    fontSize = tokens.bodyMedium,
                    color = mutedText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(backups, key = { it.id ?: it.backupId }) { backup ->
                    RecoveryListItemCard(
                        backup = backup,
                        onRestoreClick = {
                            selectedBackupForRestore = backup
                        }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for initiating Recovery
    if (selectedBackupForRestore != null) {
        InitiateRecoveryBottomSheet(
            backup = selectedBackupForRestore!!,
            isInitiating = isInitiating,
            onDismiss = { selectedBackupForRestore = null },
            onConfirmRestore = { destinationType, destinationLocation, overwriteAck, overwriteExisting, restartServices, verifyData ->
                val request = InitiateRecoveryRequest(
                    backupId = selectedBackupForRestore!!.backupId,
                    destinationLocation = destinationLocation,
                    destinationType = destinationType,
                    overwriteAcknowledged = overwriteAck,
                    overwriteExisting = overwriteExisting,
                    restartServices = restartServices,
                    verifyDataAfterRestore = verifyData
                )
                viewModel.initiateRecovery(
                    request = request,
                    onSuccess = { recoveryDto ->
                        selectedBackupForRestore = null
                        DynamicIslandManager.showSuccess("Data recovery initiated")
                        onNavigateToRecoveryDetail(recoveryDto.recoveryId)
                    },
                    onError = { errorMsg ->
                        DynamicIslandManager.showError(errorMsg)
                    }
                )
            }
        )
    }
}

@Composable
private fun RecoveryListItemCard(
    backup: BackupItemDto,
    onRestoreClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val actions = remember(backup) {
        listOf(
            MenuAction(
                label = "Restore Backup",
                onClick = onRestoreClick
            )
        )
    }

    DataCard(
        item = backup,
        title = backup.name,
        topBadgeText = "Ready",
        topBadgeTextColor = greentext,
        topBadgeBgColor = greenBg,
        topBadgeInline = true,
        actions = actions,
        onClick = { onRestoreClick() },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SOURCE",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = backup.sourceTarget.ifEmpty { "Production Database" },
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(cardBgLight)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("ID", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
                            Text(backup.backupId, fontSize = tokens.bodySmall, color = TextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TYPE / SIZE", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
                            Text("${backup.type} / ${backup.sizeFormatted.ifEmpty { "42 GB" }}", fontSize = tokens.bodySmall, color = TextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("INTEGRITY", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
                            Text(backup.verification.integrityCheck, fontSize = tokens.bodySmall, color = TextPrimary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CREATED : Today, 02:18 AM",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InitiateRecoveryBottomSheet(
    backup: BackupItemDto,
    isInitiating: Boolean,
    onDismiss: () -> Unit,
    onConfirmRestore: (
        destinationType: String,
        destinationLocation: String,
        overwriteAcknowledged: Boolean,
        overwriteExisting: Boolean,
        restartServices: Boolean,
        verifyDataAfterRestore: Boolean
    ) -> Unit
) {
    val tokens = LocalAppTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var destinationType by remember { mutableStateOf("Original Source") }
    var destinationLocation by remember { mutableStateOf("Original Source") }
    var overwriteExisting by remember { mutableStateOf(false) }
    var verifyDataAfterRestore by remember { mutableStateOf(true) }
    var restartServices by remember { mutableStateOf(false) }
    var overwriteAcknowledged by remember { mutableStateOf(false) }

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
                        text = "Review backup details & choose destination",
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
                // BACKUP SUMMARY
                Text(
                    text = "BACKUP SUMMARY",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("NAME", fontSize = tokens.label, color = mutedText)
                                Text(backup.name, fontSize = tokens.bodySmall, color = TextPrimary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .background(light_grey)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(backup.backupId, fontSize = tokens.label, color = headerGrey)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SOURCE", fontSize = tokens.label, color = mutedText)
                                Text(backup.sourceTarget, fontSize = tokens.caption, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CREATED", fontSize = tokens.label, color = mutedText)
                                Text("Today, 02:22 AM", fontSize = tokens.caption, color = TextPrimary)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SIZE", fontSize = tokens.label, color = mutedText)
                                Text(backup.sizeFormatted.ifEmpty { "42 GB" }, fontSize = tokens.caption, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("INTEGRITY", fontSize = tokens.label, color = mutedText)
                                StatusBadge(text = backup.verification.integrityCheck, textColor = greentext, bgColor = greenBg)
                            }
                        }
                    }
                }

                // RESTORE DESTINATION
                Text(
                    text = "RESTORE DESTINATION",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )

                DestinationChoiceCard(
                    title = "Original Source",
                    subtitle = "Restore directly to original production source",
                    isSelected = destinationType == "Original Source",
                    onClick = {
                        destinationType = "Original Source"
                        destinationLocation = "Original Source"
                    }
                )

                DestinationChoiceCard(
                    title = "Alternate Environment",
                    subtitle = "Restore data to a sandbox or staging environment",
                    isSelected = destinationType == "Alternate Environment",
                    onClick = {
                        destinationType = "Alternate Environment"
                        destinationLocation = "Alternate Environment"
                    }
                )

                // RECOVERY SETTINGS
                Text(
                    text = "RECOVERY SETTINGS",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RecoveryToggleRow(
                            title = "Overwrite Existing Data",
                            subtitle = "Clear target container before restore",
                            checked = overwriteExisting,
                            onCheckedChange = { overwriteExisting = it }
                        )
                        HorizontalDivider(color = grey_border)
                        RecoveryToggleRow(
                            title = "Verify Data After Restore",
                            subtitle = "Run integrity checksum post-recovery",
                            checked = verifyDataAfterRestore,
                            onCheckedChange = { verifyDataAfterRestore = it }
                        )
                        HorizontalDivider(color = grey_border)
                        RecoveryToggleRow(
                            title = "Restart Services",
                            subtitle = "Automatically reboot attached services",
                            checked = restartServices,
                            onCheckedChange = { restartServices = it }
                        )
                    }
                }

                // ESTIMATED IMPACT
                Text(
                    text = "ESTIMATED IMPACT",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ImpactStatCard(label = "DATA SIZE", value = "42 GB", modifier = Modifier.weight(1f))
                    ImpactStatCard(label = "EST. DURATION", value = "18-25m", modifier = Modifier.weight(1f))
                    ImpactStatCard(label = "DOWNTIME", value = "~10m", valueColor = redText, modifier = Modifier.weight(1f))
                }

                // Warning Message Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(yellowBg)
                        .border(1.dp, yellowText.copy(alpha = 0.3f), RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = yellowText, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Restoring to original source may overwrite existing tables. Ensure active database connections are drained before proceeding.",
                            fontSize = tokens.caption,
                            color = yellowText
                        )
                    }
                }

                // Overwrite Acknowledged Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { overwriteAcknowledged = !overwriteAcknowledged }
                ) {
                    AppCheckbox(
                        checked = overwriteAcknowledged,
                        onCheckedChange = { overwriteAcknowledged = it }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "I understand that restoring this backup will modify production records and cannot be undone.",
                        fontSize = tokens.caption,
                        color = TextPrimary
                    )
                }
            }

            HorizontalDivider(color = grey_border)

            // Bottom Actions
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
                    onClick = {
                        onConfirmRestore(
                            destinationType,
                            destinationLocation,
                            overwriteAcknowledged,
                            overwriteExisting,
                            restartServices,
                            verifyDataAfterRestore
                        )
                    },
                    enabled = overwriteAcknowledged && !isInitiating,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = disabled
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    if (isInitiating) {
                        CircularProgressIndicator(color = whiteBg, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                        Spacer(Modifier.width(6.dp))
                        Text("Start Recovery", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationChoiceCard(
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
                Text(text = title, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = subtitle, fontSize = tokens.caption, color = headerGrey)
            }
        }
    }
}

@Composable
private fun RecoveryToggleRow(
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
            Text(text = title, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(text = subtitle, fontSize = tokens.caption, color = headerGrey)
        }
        MiniSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ImpactStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = cardBgLight,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = tokens.label, color = mutedText)
            Spacer(Modifier.height(4.dp))
            Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}