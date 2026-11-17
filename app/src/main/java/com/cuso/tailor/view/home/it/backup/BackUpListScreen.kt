@file:Suppress("unused")

package com.cuso.tailor.view.home.it.backup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
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

    val jobsPagingState by viewModel.backupJobsPagingState.collectAsState()
    val metrics by viewModel.backupMetrics.collectAsState()
    val searchQuery by viewModel.backupSearchQuery.collectAsState()
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
                                fontSize = tokens.bodySmall,
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
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onBackupSearchQueryChange,
                    placeholder = "Search backup job, source...",
                    showFilterIcon = true,
                    filterCount = activeFilterCount,
                    onFilterClick = { filterDrawerState.open() },
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = mutedText,
                    height = tokens.fieldHeight
                )

                // Top Summary Metric Cards
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
                            value = "${metrics?.totalJobs ?: jobsPagingState.items.size}",
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
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = mutedText
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = metrics?.nextBackup ?: "Today, 01:00 AM",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                when {
                    errorMessage != null && jobsPagingState.items.isEmpty() && !jobsPagingState.isInitialLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = errorMessage ?: "Failed to load backups", color = redText, fontSize = tokens.bodySmall)
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

                    !jobsPagingState.isInitialLoading && jobsPagingState.items.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No backup jobs found", color = mutedText, fontSize = tokens.bodyMedium)
                        }
                    }

                    else -> {
                        PaginatedDataList(
                            state = jobsPagingState,
                            onLoadMore = { viewModel.loadMoreBackupJobs() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { job ->
                            val isSuccessful = job.status.equals("Successful", ignoreCase = true)
                            val statusBg = if (isSuccessful) greenBg else redBg
                            val statusText = if (isSuccessful) greentext else redText

                            DataCard(
                                item = job,
                                title = job.sourceEntity ?: "CLOUD Database",
                                subtitle = job.jobName.ifBlank { "Database Backup" },
                                topBadgeText = job.status.toTitleCase(),
                                topBadgeBgColor = statusBg,
                                topBadgeTextColor = statusText,
                                topBadgeInline = true,
                                actions = listOf(
                                    MenuAction(label = "View Details", onClick = { onBackupClick(job.effectiveId) })
                                ),
                                onClick = { onBackupClick(job.effectiveId) },
                                content = {
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
                                                Text(text = "TYPE", fontSize = tokens.label, color = mutedText)
                                                Spacer(Modifier.height(3.dp))
                                                Text(
                                                    text = job.backupType ?: "Full",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = title_color
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "LAST BACKUP", fontSize = tokens.label, color = mutedText)
                                                Spacer(Modifier.height(3.dp))
                                                Text(
                                                    text = viewModel.formatTimeAgo(job.lastBackupAt),
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = title_color
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "Backup Size", fontSize = tokens.label, color = mutedText)
                                                Spacer(Modifier.height(3.dp))
                                                Text(
                                                    text = job.lastBackupSize ?: "0.01 MB",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = title_color
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "NEXT RUN : ${job.nextRunAt}",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = mutedText
                                        )
                                        Text(
                                            text = "RETENTION : ${job.retentionDays} Days",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = mutedText
                                        )
                                    }
                                }
                            )
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
 * Metric summary card for header metrics
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