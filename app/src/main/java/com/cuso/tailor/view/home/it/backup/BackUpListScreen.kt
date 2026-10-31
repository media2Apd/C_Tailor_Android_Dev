@file:Suppress("unused")

package com.cuso.tailor.view.home.it.backup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
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
import com.cuso.tailor.model.it.BackupJobItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun BackupRecoveryListScreen(
    viewModel: ItViewModel,
    onBack: () -> Unit,
    onBackupClick: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val jobs by viewModel.filteredBackupJobs.collectAsState()
    val metrics by viewModel.backupMetrics.collectAsState()
    val searchQuery by viewModel.backupSearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingBackups.collectAsState()
    val errorMessage by viewModel.backupsError.collectAsState()
    val selectedStatus by viewModel.selectedBackupStatus.collectAsState()

    val filterDrawerState = rememberFilterDrawerState()

    LaunchedEffect(Unit) {
        viewModel.fetchBackupDashboard()
    }

    val activeFilterCount = remember(selectedStatus) {
        if (!selectedStatus.isNullOrBlank()) 1 else 0
    }

    val filterSections = remember(selectedStatus) {
        listOf(
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("Successful", "Successful", selectedStatus.equals("Successful", ignoreCase = true)),
                    FilterOption("Failed", "Failed", selectedStatus.equals("Failed", ignoreCase = true)),
                    FilterOption("Running", "Running", selectedStatus.equals("Running", ignoreCase = true))
                ),
                isMultiSelect = false
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "Backup &\nRecovery",
                    onClose = onBack,
                    trailingContent = {
                        OutlinedButton(
                            onClick = { viewModel.fetchBackupDashboard() },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderGray),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh",
                                tint = title_color,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Refresh",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search and Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onBackupSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search SO number, customer...",
                                color = mutedText,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderGray,
                            focusedContainerColor = whiteBg,
                            unfocusedContainerColor = whiteBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.fieldHeight)
                    )

                    Surface(
                        modifier = Modifier
                            .size(tokens.fieldHeight)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { filterDrawerState.open() },
                        color = whiteBg,
                        border = BorderStroke(1.dp, BorderGray),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = "Filter",
                                tint = title_color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Top Summary Metric Cards (2x2 Grid)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BackupMetricCard(
                                    label = "TOTAL JOBS",
                                    value = "${metrics?.totalJobs ?: jobs.size}",
                                    valueColor = Primary,
                                    modifier = Modifier.weight(1f)
                                )
                                BackupMetricCard(
                                    label = "SUCCESSFUL",
                                    value = "${metrics?.successful ?: 0}",
                                    valueColor = greentext,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BackupMetricCard(
                                    label = "FAILED",
                                    value = "${metrics?.failed ?: 0}",
                                    valueColor = title_color,
                                    modifier = Modifier.weight(1f)
                                )
                                BackupMetricCard(
                                    label = "RUNNING",
                                    value = "${metrics?.running ?: 0}",
                                    valueColor = Primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Next Backup Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = whiteBg,
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Text(
                                        text = "NEXT BACKUP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = mutedText
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = metrics?.nextBackup ?: "Today, 01:00 AM",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = title_color
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(10.dp)) }

                    // Backup Job Items List
                    when {
                        isLoading && jobs.isEmpty() -> {
                            item { ListSkeleton() }
                        }

                        errorMessage != null && jobs.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = errorMessage ?: "Failed to load backups", color = redText, fontSize = 13.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.fetchBackupDashboard() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                        ) {
                                            Text("Retry", color = whiteBg)
                                        }
                                    }
                                }
                            }
                        }

                        jobs.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No backup jobs found", color = mutedText, fontSize = 14.sp)
                                }
                            }
                        }

                        else -> {
                            items(
                                items = jobs,
                                key = { it.effectiveId }
                            ) { job ->
                                BackupJobCard(
                                    job = job,
                                    timeAgoText = viewModel.formatTimeAgo(job.lastBackupAt),
                                    onClick = { onBackupClick(job.effectiveId) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Status Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            sections = filterSections,
            onApply = { updatedSections ->
                val status = updatedSections.find { it.title == "Status" }
                    ?.options?.find { it.isSelected }?.id
                viewModel.onBackupStatusFilterChange(status)
            },
            onClearAll = {
                viewModel.clearBackupFilters()
            }
        )
    }
}

/**
 * Metric Card for Total Jobs, Successful, Failed, Running.
 */
@Composable
private fun BackupMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

/**
 * Backup Job item card exactly matching Screenshot 1 bottom layout.
 */
@Composable
private fun BackupJobCard(
    job: BackupJobItemDto,
    timeAgoText: String,
    onClick: () -> Unit
) {
    val isSuccessful = job.status.equals("Successful", ignoreCase = true)
    val statusBg = if (isSuccessful) greenBg else redBg
    val statusText = if (isSuccessful) greentext else redText

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: CLOUD Database | Badge | Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.sourceEntity ?: "CLOUD Database",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = job.status.toTitleCase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusText
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Options",
                        tint = mutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Subheader: BACKUP JOB | Database Backup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BACKUP JOB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = mutedText
                )
                Text(
                    text = job.jobName.ifBlank { "Database Backup" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }

            Spacer(Modifier.height(12.dp))

            // Inner light container: TYPE | LAST BACKUP | Backup Size
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TYPE", fontSize = 10.sp, color = mutedText)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = job.backupType ?: "Full",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "LAST BACKUP", fontSize = 10.sp, color = mutedText)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = timeAgoText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Backup Size", fontSize = 10.sp, color = mutedText)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = job.lastBackupSize ?: "0.01 MB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Footer: NEXT RUN : 1:00 AM | RETENTION : 20 Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEXT RUN : 1:00 AM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = mutedText
                )
                Text(
                    text = "RETENTION : ${job.retentionDays} Days",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = mutedText
                )
            }
        }
    }
}