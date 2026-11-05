@file:Suppress("unused", "AssignedValueIsNeverRead", "UnusedVariable")

package com.cuso.tailor.view.home.hr.settings.leave_type

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.LeaveTypeItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveTypesListScreen(
    viewModel: SettingsViewModel,
    onClose: () -> Unit,
    onCreateNew: () -> Unit,
    onViewLeaveType: (String) -> Unit,
    onEditLeaveType: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val leaveTypes by viewModel.leaveTypes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLeaveTypes.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val filterDrawerState = rememberFilterDrawerState()
    var leaveToDelete by remember { mutableStateOf<LeaveTypeItemDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchLeaveTypes(page = 1)
    }

    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("active", "Published"),
                    FilterOption("draft", "Draft")
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Category",
                options = listOf(
                    FilterOption("Regular Leave", "Regular Leave"),
                    FilterOption("Medical Leave", "Medical Leave"),
                    FilterOption("Special Leave", "Special Leave")
                ),
                isMultiSelect = false
            )
        )
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = FabConfig(
            label = "Create Leave Type",
            icon = Icons.Default.Add,
            onClick = onCreateNew
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            // Header Bar
            TitleBar(
                title = "Leave Types",
                onClose = onClose,
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { /* Export action */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Export",
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Text(
                            text = "Export",
                            color = Primary,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )

            Spacer(Modifier.height(6.dp))

            // Search and Filter Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    viewModel.onLeaveTypeSearchQueryChanged(query)
                },
                placeholder = "Search leave types...",
                showFilterIcon = true,
                onFilterClick = { filterDrawerState.open() }
            )

            HorizontalDivider(color = sectionBorder, thickness = 1.dp)

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                when {
                    isLoading && leaveTypes.isEmpty() -> {
                        ListSkeleton()
                    }

                    leaveTypes.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No leave types found.",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(leaveTypes, key = { it.id }) { item ->
                                LeaveTypeCardItem(
                                    item = item,
                                    onCardClick = { onViewLeaveType(item.id) },
                                    onEdit = { onEditLeaveType(item.id) },
                                    onDelete = { leaveToDelete = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter Drawer
    FilterDrawer(
        state = filterDrawerState,
        title = "Filter Leave Types",
        sections = filterSections,
        onApply = { appliedSections ->
            val statusOpt = appliedSections.find { it.title == "Status" }?.options?.firstOrNull { it.isSelected }?.id
            val catOpt = appliedSections.find { it.title == "Category" }?.options?.firstOrNull { it.isSelected }?.id
            viewModel.fetchLeaveTypes(status = statusOpt, category = catOpt)
        },
        onClearAll = {
            viewModel.fetchLeaveTypes()
        }
    )

    // Delete Confirmation Dialog
    leaveToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { leaveToDelete = null },
            title = {
                Text(
                    text = "Delete Leave Type",
                    fontSize = tokens.h2,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${target.leaveTypeName}\"? This action cannot be undone.",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLeaveType(target.id) {
                            leaveToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = redText)
                ) {
                    Text("Delete", color = whiteBg, fontSize = tokens.bodySmall)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { leaveToDelete = null }) {
                    Text("Cancel", color = TextSecondary, fontSize = tokens.bodySmall)
                }
            },
            containerColor = whiteBg
        )
    }
}

@Composable
private fun LeaveTypeCardItem(
    item: LeaveTypeItemDto,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isPublished = item.status.equals("active", ignoreCase = true) || item.status.equals("published", ignoreCase = true)
    val parsedColor = remember(item.colorCode) {
        try {
            Color(item.colorCode.toColorInt())
        } catch (_: Exception) {
            Color(0xFF6366F1)
        }
    }

    var isMenuExpanded by remember { mutableStateOf(false) }

    val allowanceText = when (item.allocationMethod) {
        "no_quota" -> "No Quota"
        "earned_coff" -> "Earned / COFF"
        else -> if (item.daysPerYear > 0) "${item.daysPerYear}" else "As per policy"
    }

    val footerFields = listOf(
        DataCardField(
            icon = Icons.Default.CheckCircle,
            text = if (item.compensationType.equals("paid", ignoreCase = true)) "Paid" else "Unpaid",
            textColor = if (item.compensationType.equals("paid", ignoreCase = true)) greentext else TextSecondary
        ),
        DataCardField(
            icon = Icons.Default.Shield,
            text = if (item.approval.requiresApproval) "Approval: Req." else "Approval: Auto",
            textColor = TextSecondary
        ),
        DataCardField(
            icon = Icons.AutoMirrored.Filled.Redo,
            text = if (item.carryForward.enabled) "Carry Forward: Yes" else "Carry Forward: No",
            textColor = TextSecondary
        )
    )

    DataCard(
        item = item,
        onClick = { onCardClick() },
        actions = emptyList(),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Identity Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(parsedColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.shortCode.ifBlank { item.leaveTypeName.take(2).uppercase() },
                            color = parsedColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = tokens.bodySmall
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.leaveTypeName,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.description.isNotBlank()) {
                            Text(
                                text = item.description,
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 3-Dot Overflow Menu
                    Box {
                        IconButton(
                            onClick = { isMenuExpanded = true },
                            modifier = Modifier.size(tokens.iconSize * 1.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = close_color
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier.background(whiteBg)
                        ) {
                            DropdownMenuItem(
                                text = { Text("View", color = TextPrimary, fontSize = tokens.bodySmall) },
                                onClick = {
                                    isMenuExpanded = false
                                    onCardClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit", color = TextPrimary, fontSize = tokens.bodySmall) },
                                onClick = {
                                    isMenuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = redText, fontSize = tokens.bodySmall) },
                                onClick = {
                                    isMenuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                // Allowance Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(Primary_background)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LEAVE ALLOWANCE",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = headerGrey
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = allowanceText,
                                    fontSize = tokens.h1,
                                    fontWeight = FontWeight.Medium,
                                    color = parsedColor
                                )
                                if (item.allocationMethod !in listOf("no_quota", "earned_coff") && item.daysPerYear > 0) {
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (item.daysPerYear == 1) "day / year" else "days / year",
                                        fontSize = tokens.caption,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        StatusBadge(
                            text = if (isPublished) "Published" else "Draft",
                            bgColor = if (isPublished) greenBg else yellowBg,
                            textColor = if (isPublished) greentext else yellowText
                        )
                    }
                }

                HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                // Footer Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    footerFields.forEach { field ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            field.icon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = field.textColor,
                                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                text = field.text,
                                fontSize = tokens.caption,
                                color = field.textColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    )
}