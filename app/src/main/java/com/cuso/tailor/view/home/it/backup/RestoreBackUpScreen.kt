package com.cuso.tailor.view.home.it.backup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.viewmodel.ItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupScreen(
    snapshotId: String,
    viewModel: ItViewModel,
    onBack: () -> Unit,
    onRestoreTriggered: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val detail by viewModel.backupDetail.collectAsState()
    val isLoadingDetail by viewModel.isLoadingBackupDetail.collectAsState()
    val isExecuting by viewModel.isExecutingRestore.collectAsState()

    // Form states matching Screenshot 2 and JSON payload
    var restoreDestination by remember { mutableStateOf("Production Database (Primary)") }
    var restoreType by remember { mutableStateOf("Full") }
    var verifyIntegrity by remember { mutableStateOf(true) }
    var createSafetySnapshot by remember { mutableStateOf(true) }
    var userConfirmedOverwrite by remember { mutableStateOf(false) }

    var expandedDropdown by remember { mutableStateOf(false) }
    val destinationOptions = listOf("Production Database (Primary)", "Staging Environment")

    LaunchedEffect(snapshotId) {
        if (detail == null || detail?.id != snapshotId) {
            viewModel.fetchBackupSnapshotDetail(snapshotId)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Primary_background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Restore Backup",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                        Text(
                            text = "Restore data from a verified recovery point.",
                            fontSize = 11.sp,
                            color = mutedText
                        )
                    }

                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Cancel", color = title_color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoadingDetail && detail == null) {
            ListSkeleton()
        } else {
            val d = detail
            val backupId = d?.backupId ?: snapshotId
            val source = d?.sourceEntity ?: "customers"
            val formattedSize = d?.formattedSize ?: "0.01 MB"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ─── 1. Selected Recovery Point Card ───
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected Recovery Point",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                            Row(
                                modifier = Modifier.clickable { onBack() },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(text = "Change", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Medium)
                            }
                        }

                        HorizontalDivider(color = sectionBorder, thickness = 0.6.dp)

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Backup ID", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(3.dp))
                                Text(text = backupId, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = title_color)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Source", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(3.dp))
                                Text(text = source, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = title_color)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Date", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = viewModel.formatDeviceDate(d?.createdAt),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Size", fontSize = 11.sp, color = mutedText)
                                Spacer(Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = formattedSize, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = title_color)
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(greenBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "✓ Verified", fontSize = 10.sp, color = greentext, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }

                // ─── 2. Critical Impact Warning Card ───
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = redText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Critical Impact Warning",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = redText
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "You are about to perform a full restore to the Production Database. This action will permanently overwrite all existing data from the backup date to present.",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // ─── 3. Restore Configuration Card ───
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
                        Text(
                            text = "Restore Configuration",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )

                        // Destination Dropdown
                        Column {
                            Text(text = "Restore Destination", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = title_color)
                            Spacer(Modifier.height(6.dp))

                            ExposedDropdownMenuBox(
                                expanded = expandedDropdown,
                                onExpandedChange = { expandedDropdown = !expandedDropdown }
                            ) {
                                OutlinedTextField(
                                    value = restoreDestination,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = BorderGray,
                                        focusedContainerColor = whiteBg,
                                        unfocusedContainerColor = whiteBg
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedDropdown,
                                    onDismissRequest = { expandedDropdown = false }
                                ) {
                                    destinationOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option, fontSize = 13.sp) },
                                            onClick = {
                                                restoreDestination = option
                                                expandedDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = redText, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Targeting Production Environment. Existing data may be overwritten.",
                                    fontSize = 10.sp,
                                    color = redText
                                )
                            }
                        }

                        // Restore Type Radio Selection
                        Column {
                            Text(text = "Restore Type", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = title_color)
                            Spacer(Modifier.height(8.dp))

                            // Full Restore Option
                            RestoreTypeOptionCard(
                                title = "Full Restore",
                                subtitle = "Complete overwrite of destination database.",
                                isSelected = restoreType.equals("Full", ignoreCase = true),
                                onClick = { restoreType = "Full" }
                            )

                            Spacer(Modifier.height(8.dp))

                            // Partial Restore Option
                            RestoreTypeOptionCard(
                                title = "Partial Restore",
                                subtitle = "Select specific tables or schemas to restore.",
                                isSelected = restoreType.equals("Partial", ignoreCase = true),
                                onClick = { restoreType = "Partial" }
                            )
                        }

                        // Advanced Options
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Advanced Options", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = title_color)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { verifyIntegrity = !verifyIntegrity }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = verifyIntegrity,
                                    onCheckedChange = { verifyIntegrity = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                                )
                                Text("Verify data integrity after restore completes", fontSize = 12.sp, color = title_color)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { createSafetySnapshot = !createSafetySnapshot }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = createSafetySnapshot,
                                    onCheckedChange = { createSafetySnapshot = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                                )
                                Text("Create safety snapshot before restore (Recommended)", fontSize = 12.sp, color = title_color)
                            }
                        }
                    }
                }

                // ─── 4. Pre-Restore Checks Card ───
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Pre-Restore Checks",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )

                        CheckItem(title = "Recovery Point Verified", isChecked = true)
                        CheckItem(title = "Sufficient Storage Available", isChecked = true)
                        CheckItem(title = "Target Permissions Confirmed", isChecked = true)
                        CheckItem(
                            title = "Safety Snapshot Scheduled Before Restore",
                            isChecked = false,
                            fallbackDash = true
                        )
                    }
                }

                // ─── 5. Operation Summary Card ───
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Operation Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                        HorizontalDivider(color = sectionBorder, thickness = 0.6.dp)

                        SummaryRow(label = "Action", value = if (restoreType == "Full") "Full Database Restore" else "Partial Schema Restore")
                        SummaryRow(label = "Data Size", value = formattedSize)
                        SummaryRow(label = "Est. Duration", value = "~20 minutes")
                    }
                }

                // ─── 6. Confirmation & Execute Action Bar ───
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
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { userConfirmedOverwrite = !userConfirmedOverwrite }
                        ) {
                            Checkbox(
                                checked = userConfirmedOverwrite,
                                onCheckedChange = { userConfirmedOverwrite = it },
                                colors = CheckboxDefaults.colors(checkedColor = Primary)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "I confirm that this action will overwrite existing production data.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Text("Cancel", color = title_color, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    if (!userConfirmedOverwrite) {
                                        DynamicIslandManager.showError("Please acknowledge the overwrite confirmation.")
                                        return@Button
                                    }
                                    viewModel.executeRestoreBackup(
                                        backupId = backupId,
                                        restoreDestination = restoreDestination,
                                        restoreType = restoreType,
                                        verifyIntegrity = verifyIntegrity,
                                        createSafetySnapshot = createSafetySnapshot,
                                        userAcknowledgedOverwrite = userConfirmedOverwrite,
                                        onSuccess = { restoreId, message ->
                                            DynamicIslandManager.showSuccess("$message ($restoreId)")
                                            onRestoreTriggered()
                                        },
                                        onError = { error ->
                                            DynamicIslandManager.showError(error)
                                        }
                                    )
                                },
                                enabled = userConfirmedOverwrite && !isExecuting,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary,
                                    disabledContainerColor = Primary.copy(alpha = 0.45f)
                                ),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp)
                            ) {
                                if (isExecuting) {
                                    CircularProgressIndicator(
                                        color = whiteBg,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Outlined.Restore, contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Restore Backup", color = whiteBg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RestoreTypeOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) primary_light.copy(alpha = 0.25f) else whiteBg,
        border = BorderStroke(1.2.dp, if (isSelected) Primary else BorderGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Primary),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = title_color)
                Spacer(Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 11.sp, color = mutedText)
            }
        }
    }
}

@Composable
private fun CheckItem(
    title: String,
    isChecked: Boolean,
    fallbackDash: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isChecked) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = greentext, modifier = Modifier.size(16.dp))
        } else if (fallbackDash) {
            Text(text = "—", fontSize = 14.sp, color = mutedText, modifier = Modifier.padding(start = 2.dp, end = 2.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (isChecked) greentext else mutedText,
            fontWeight = if (isChecked) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = mutedText)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = title_color)
    }
}