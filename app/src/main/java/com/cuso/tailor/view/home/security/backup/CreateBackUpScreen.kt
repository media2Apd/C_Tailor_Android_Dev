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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCardProgressBar
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.MiniSwitch
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SecurityViewModel

// Options constants aligned with React constants
private val SOURCE_TYPE_OPTIONS = listOf("Database")
private val SOURCE_TARGET_OPTIONS = listOf("Production Database (us-east-1)")
private val FREQUENCY_OPTIONS = listOf("Daily", "Weekly", "Monthly")
private val TIMEZONE_OPTIONS = listOf("IST (UTC+5:30)")
private val DESTINATION_TARGET_OPTIONS = listOf("Primary Backup Storage")

@Composable
fun CreateBackupScreen(
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit,
    onSuccessCreated: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isCreating by viewModel.isCreatingBackup.collectAsState()

    // Form field states matching React formData initial values
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var sourceType by remember { mutableStateOf("") }
    var sourceTarget by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var destinationTarget by remember { mutableStateOf("") }
    var encryption by remember { mutableStateOf(true) }
    var compression by remember { mutableStateOf(true) }
    var scheduleEnabled by remember { mutableStateOf(true) }
    var frequency by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("") }
    var dailyDays by remember { mutableStateOf("") }
    var weeklyWeeks by remember { mutableStateOf("") }

    // Dropdown expanded states
    var sourceTypeExpanded by remember { mutableStateOf(false) }
    var sourceTargetExpanded by remember { mutableStateOf(false) }
    var destinationTargetExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var timezoneExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Create Backup",
                subtitle = "Configure the source, schedule, retention, destination, and protection settings for this backup.",
                onClose = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                border = BorderStroke(1.dp, grey_border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (name.trim().isBlank()) {
                                DynamicIslandManager.showError("Backup Name is required")
                                return@Button
                            }

                            // Payload mapping matching React handleSave
                            val request = CreateBackupRequest(
                                name = name.trim(),
                                description = description.trim(),
                                source = sourceType,
                                sourceTarget = sourceTarget,
                                type = type,
                                size = "42 GB",
                                destination = DestinationRequest(
                                    target = destinationTarget
                                ),
                                protection = ProtectionRequest(
                                    encryption = encryption,
                                    compression = compression
                                ),
                                schedule = ScheduleRequest(
                                    enabled = scheduleEnabled,
                                    frequency = frequency,
                                    time = time,
                                    timezone = timezone
                                ),
                                retention = RetentionRequest(
                                    dailyDays = dailyDays.toIntOrNull() ?: 30,
                                    weeklyWeeks = weeklyWeeks.toIntOrNull() ?: 12
                                )
                            )

                            viewModel.createBackup(
                                request = request,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Backup configuration created successfully")
                                    onSuccessCreated()
                                },
                                onError = { errorMsg ->
                                    DynamicIslandManager.showError(errorMsg)
                                }
                            )
                        },
                        enabled = !isCreating && name.isNotBlank(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Create Backup", fontSize = tokens.bodySmall, color = whiteBg)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. BACKUP INFORMATION
            FormSectionCard(icon = Icons.Default.Info, title = "BACKUP INFORMATION") {
                FormLabel(text = "Name", isRequired = true)
                FormTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Enter backup name"
                )

                Spacer(Modifier.height(10.dp))

                FormLabel(text = "Description")
                FormTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Enter backup description"
                )
            }

            // 2. PROTECTION
            FormSectionCard(icon = Icons.Default.Shield, title = "PROTECTION") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Encryption", fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Encrypt data at rest", fontSize = tokens.caption, color = headerGrey)
                    }
                    MiniSwitch(
                        checked = encryption,
                        onCheckedChange = { encryption = it }
                    )
                }

                HorizontalDivider(color = grey_border)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Compression", fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Reduce storage footprint", fontSize = tokens.caption, color = headerGrey)
                    }
                    MiniSwitch(
                        checked = compression,
                        onCheckedChange = { compression = it }
                    )
                }
            }

            // 3. BACKUP SOURCE
            FormSectionCard(icon = Icons.Default.Storage, title = "BACKUP SOURCE") {
                FormDropdown(
                    label = "Source Type",
                    value = sourceType,
                    expanded = sourceTypeExpanded,
                    onExpandChange = { sourceTypeExpanded = it },
                    options = SOURCE_TYPE_OPTIONS,
                    onOptionSelected = {
                        sourceType = it
                        sourceTypeExpanded = false
                    }
                )

                Spacer(Modifier.height(10.dp))

                FormDropdown(
                    label = "Source Target",
                    value = sourceTarget,
                    expanded = sourceTargetExpanded,
                    onExpandChange = { sourceTargetExpanded = it },
                    options = SOURCE_TARGET_OPTIONS,
                    onOptionSelected = {
                        sourceTarget = it
                        sourceTargetExpanded = false
                    }
                )
            }

            // 4. BACKUP TYPE
            FormSectionCard(icon = Icons.Default.Layers, title = "BACKUP TYPE") {
                BackupTypeOption(
                    title = "Full",
                    subtitle = "Captures the entire data source. Slowest, requires most storage.",
                    isSelected = type == "Full",
                    onClick = { type = "Full" }
                )
                Spacer(Modifier.height(8.dp))
                BackupTypeOption(
                    title = "Incremental",
                    subtitle = "Only backs up data changed since the last backup of any type.",
                    isSelected = type == "Incremental",
                    onClick = { type = "Incremental" }
                )
                Spacer(Modifier.height(8.dp))
                BackupTypeOption(
                    title = "Differential",
                    subtitle = "Backs up data changed since the last full backup.",
                    isSelected = type == "Differential",
                    onClick = { type = "Differential" }
                )
            }

            // 5. SCHEDULE (Toggled view matching React scheduleEnabled condition)
            FormSectionCard(
                icon = Icons.Default.Schedule,
                title = "SCHEDULE",
                trailing = {
                    MiniSwitch(
                        checked = scheduleEnabled,
                        onCheckedChange = { scheduleEnabled = it }
                    )
                }
            ) {
                if (scheduleEnabled) {
                    FormDropdown(
                        label = "Frequency",
                        value = frequency,
                        expanded = frequencyExpanded,
                        onExpandChange = { frequencyExpanded = it },
                        options = FREQUENCY_OPTIONS,
                        onOptionSelected = {
                            frequency = it
                            frequencyExpanded = false
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Time")
                            FormTextField(
                                value = time,
                                onValueChange = { time = it },
                                placeholder = "02:00 AM"
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Timezone",
                                value = timezone,
                                expanded = timezoneExpanded,
                                onExpandChange = { timezoneExpanded = it },
                                options = TIMEZONE_OPTIONS,
                                onOptionSelected = {
                                    timezone = it
                                    timezoneExpanded = false
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Automated schedule is disabled. Backup can only be executed manually.",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }
            }

            // 6. RETENTION
            FormSectionCard(icon = Icons.Default.Restore, title = "RETENTION") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Daily Period")
                        FormTextField(
                            value = dailyDays,
                            onValueChange = { dailyDays = it },
                            keyboardType = KeyboardType.Number,
                            trailingText = "Days"
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Weekly Period")
                        FormTextField(
                            value = weeklyWeeks,
                            onValueChange = { weeklyWeeks = it },
                            keyboardType = KeyboardType.Number,
                            trailingText = "Weeks"
                        )
                    }
                }
            }

            // 7. DESTINATION
            FormSectionCard(icon = Icons.Default.Dns, title = "DESTINATION") {
                FormDropdown(
                    label = "Storage Target",
                    value = destinationTarget,
                    expanded = destinationTargetExpanded,
                    onExpandChange = { destinationTargetExpanded = it },
                    options = DESTINATION_TARGET_OPTIONS,
                    onOptionSelected = {
                        destinationTarget = it
                        destinationTargetExpanded = false
                    }
                )

                Spacer(Modifier.height(14.dp))

                // Storage Capacity Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Storage Capacity (10TB)", fontSize = tokens.bodySmall, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(primary_light)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("68% Used", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(8.dp))
                DataCardProgressBar(progress = 0.68f, progressColor = Primary, height = 6.dp)
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Primary))
                        Spacer(Modifier.width(4.dp))
                        Text("6.8TB Used", fontSize = tokens.caption, color = headerGrey)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(light_grey))
                        Spacer(Modifier.width(4.dp))
                        Text("3.2TB Available", fontSize = tokens.caption, color = headerGrey)
                    }
                }
            }

            // 8. CONFIGURATION SUMMARY (Dynamically reflects entered form state)
            FormSectionCard(icon = Icons.AutoMirrored.Filled.Assignment, title = "CONFIGURATION SUMMARY") {
                SummaryRow("Source", sourceTarget)
                HorizontalDivider(color = grey_border)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Type", fontSize = tokens.caption, color = mutedText)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(primary_light)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(type, fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium)
                    }
                }
                HorizontalDivider(color = grey_border)
                SummaryRow(
                    "Schedule",
                    if (scheduleEnabled) "$frequency at $time (IST)" else "Disabled"
                )
                HorizontalDivider(color = grey_border)
                SummaryRow("Retention", "$dailyDays Days / $weeklyWeeks Weeks")
                HorizontalDivider(color = grey_border)
                SummaryRow("Destination", destinationTarget)
                HorizontalDivider(color = grey_border)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Protection", fontSize = tokens.caption, color = mutedText)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (encryption) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .background(greenBg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Encrypted", fontSize = tokens.caption, color = greentext, fontWeight = FontWeight.Medium)
                            }
                        }
                        if (compression) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .background(primary_light)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Compressed", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium)
                            }
                        }
                        if (!encryption && !compression) {
                            Text("None", fontSize = tokens.caption, color = TextSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FormSectionCard(
    icon: ImageVector,
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(primary_light),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = Primary)
                }
                trailing?.invoke()
            }
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun BackupTypeOption(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = tokens.caption, color = headerGrey)
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(1.dp, mutedText, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.caption, color = mutedText)
        Text(value, fontSize = tokens.caption, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}