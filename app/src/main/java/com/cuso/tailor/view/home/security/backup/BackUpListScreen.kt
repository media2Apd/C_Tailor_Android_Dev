package com.cuso.tailor.view.home.security.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
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
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun DataBackupListScreen(
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val pagingState by viewModel.filteredBackupsPagingState.collectAsState()
    val errorMessage by viewModel.backupsError.collectAsState()
    val isRunningBackup by viewModel.isRunningBackup.collectAsState()
    val searchQuery by viewModel.backupSearchQuery.collectAsState()
    val filterDrawerState = rememberFilterDrawerState()

    LaunchedEffect(Unit) {
        viewModel.fetchBackups()
    }

    val backupFilterSections = remember {
        listOf(
            FilterSection(
                title = "Backup Status",
                options = listOf(
                    FilterOption("Successful", "Successful"),
                    FilterOption("In Progress", "In Progress"),
                    FilterOption("Failed", "Failed")
                ),
                isMultiSelect = false
            )
        )
    }

    val selectedFilterCount = remember(backupFilterSections) {
        backupFilterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier.fillMaxSize(),
            fab = FabConfig(
                label = "Create Backup",
                icon = Icons.Default.Add,
                onClick = onNavigateToCreate,
                alignment = Alignment.BottomEnd,
                bottomPadding = tokens.buttonHeight * 2f,
                endPadding = tokens.screenPadding
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Primary_background)
            ) {
                // Page Header
                TitleBar(
                    title = "Data Backup",
                    onClose = onNavigateBack,
                    trailingContent = {
                        // Run Backup Button
                        OutlinedButton(
                            onClick = {
                                val targetBackupConfigId = pagingState.items.firstOrNull()?.id.orEmpty()
                                if (targetBackupConfigId.isBlank()) {
                                    DynamicIslandManager.showError("No backup configuration available to run")
                                } else {
                                    viewModel.runBackupNow(
                                        backupConfigId = targetBackupConfigId,
                                        onSuccess = { message -> DynamicIslandManager.showSuccess(message) },
                                        onError = { error -> DynamicIslandManager.showError(error) }
                                    )
                                }
                            },
                            enabled = !isRunningBackup && pagingState.items.isNotEmpty(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Primary)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            contentPadding = PaddingValues(
                                horizontal = tokens.extraPadding * 1.2f,
                                vertical = tokens.extraPadding * 0.6f
                            ),
                            modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                        ) {
                            if (isRunningBackup) {
                                CircularProgressIndicator(
                                    color = Primary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            } else {
                                Text(
                                    text = "Run Backup",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                        }

                        // Export Button
                        OutlinedButton(
                            onClick = { /* Export report action */ },
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(sectionBorder)),
                            contentPadding = PaddingValues(
                                horizontal = tokens.extraPadding * 1.2f,
                                vertical = tokens.extraPadding * 0.6f
                            ),
                            modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Export",
                                tint = TextSecondary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                            Text(
                                text = "Export",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                )

                // Search & Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onBackupSearchQueryChange(it) },
                    placeholder = "Search backup ID, name, source...",
                    showFilterIcon = true,
                    filterCount = selectedFilterCount,
                    onFilterClick = { filterDrawerState.open() }
                )

                when {
                    errorMessage != null && pagingState.items.isEmpty() && !pagingState.isInitialLoading -> {
                        AppErrorState(
                            title = "Failed to load backups",
                            message = errorMessage?.ifBlank { "Unable to fetch backup records." }
                                ?: "Unable to fetch backup records.",
                            onRetry = { viewModel.fetchBackups() }
                        )
                    }

                    !pagingState.isInitialLoading && pagingState.items.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No backup records found",
                                fontSize = tokens.bodyMedium,
                                color = mutedText
                            )
                        }
                    }

                    else -> {
                        PaginatedDataList(
                            state = pagingState,
                            onLoadMore = { viewModel.loadMoreBackups() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { backup ->
                            BackupListItemCard(
                                backup = backup,
                                onClick = { onNavigateToDetail(backup.backupId) }
                            )
                        }
                    }
                }
            }
        }

        // Animated Slide-in Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Backups",
            sections = backupFilterSections,
            onApply = { sections ->
                val selectedOption = sections.firstOrNull()?.options?.firstOrNull { it.isSelected }
                viewModel.onBackupStatusFilterChange(selectedOption?.id)
            },
            onClearAll = {
                viewModel.clearBackupFilters()
            }
        )
    }
}

@Composable
private fun BackupListItemCard(
    backup: BackupItemDto,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val actions = remember(backup) {
        listOf(
            MenuAction(
                label = "View Details",
                onClick = onClick
            )
        )
    }

    DataCard(
        item = backup,
        title = backup.name,
        topBadgeText = backup.status,
        topBadgeTextColor = if (backup.status.equals("Successful", true)) greentext else redText,
        topBadgeBgColor = if (backup.status.equals("Successful", true)) greenBg else redBg,
        topBadgeInline = true,
        actions = actions,
        onClick = { onClick() },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
            ) {
                // Source Subtitle Row
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

                // Middle Container Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(Primary_background)
                        .padding(
                            horizontal = tokens.cardPadding * 0.7f,
                            vertical = tokens.extraPadding
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "TYPE",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Text(
                                text = backup.type,
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCHEDULE",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Text(
                                text = "${backup.schedule.frequency}, ${backup.schedule.time}",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Size",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Text(
                                text = backup.sizeFormatted.ifEmpty { "42 GB" },
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // Last Run Caption Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val lastRunTime = backup.executionMetrics.completedAt?.let {
                        backup.schedule.time
                    } ?: "Today, 02:18 AM"

                    Text(
                        text = "LAST RUN : $lastRunTime",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
        }
    )
}