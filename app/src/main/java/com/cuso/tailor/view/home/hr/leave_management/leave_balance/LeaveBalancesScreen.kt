@file:Suppress("unused", "AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.hr.leave_management.leave_balance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.LeaveBalanceItemDto
import com.cuso.tailor.model.hr.extractAllocatedDays
import com.cuso.tailor.model.hr.extractEmployeeName
import com.cuso.tailor.model.hr.extractLeaveTypeName
import com.cuso.tailor.model.hr.extractRemainingDays
import com.cuso.tailor.model.hr.extractUsedDays
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.view.composable.ActionDropdownMenu
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardStat
import com.cuso.tailor.view.composable.DataCardStatsRow
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.HrViewModel
import kotlin.math.roundToInt

@Composable
fun LeaveBalancesScreen(
    hrViewModel: HrViewModel,
    onBack: () -> Unit,
    onApplyLeave: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val pagingState by hrViewModel.leaveBalancesPagingState.collectAsStateWithLifecycle()
    val leaveTypes by hrViewModel.leaveTypes.collectAsStateWithLifecycle()
    val error by hrViewModel.leaveBalancesError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLeaveTypeFilter by remember { mutableStateOf("all") }
    var selectedBudgetFilter by remember { mutableStateOf("all") }
    val selectedIds = remember { mutableStateListOf<String>() }
    val filterDrawerState = rememberFilterDrawerState()

    val leaveTypeMap = remember(leaveTypes) {
        leaveTypes.associate { it.id to it.name }
    }

    val activeFilterCount = remember(selectedLeaveTypeFilter, selectedBudgetFilter) {
        (if (selectedLeaveTypeFilter != "all") 1 else 0) +
                (if (selectedBudgetFilter != "all") 1 else 0)
    }

    val filterSections = remember(leaveTypes, selectedLeaveTypeFilter, selectedBudgetFilter) {
        buildList {
            add(
                FilterSection(
                    title = "Budget Status",
                    options = listOf(
                        FilterOption(
                            id = "in_budget",
                            label = "In Budget",
                            isSelected = selectedBudgetFilter == "in_budget"
                        ),
                        FilterOption(
                            id = "exhausted",
                            label = "Exhausted",
                            isSelected = selectedBudgetFilter == "exhausted"
                        )
                    ),
                    isMultiSelect = false
                )
            )
            if (leaveTypes.isNotEmpty()) {
                add(
                    FilterSection(
                        title = "Leave Type",
                        options = leaveTypes.map { type ->
                            FilterOption(
                                id = type.id,
                                label = type.name,
                                isSelected = selectedLeaveTypeFilter == type.id
                            )
                        },
                        isMultiSelect = false
                    )
                )
            }
        }
    }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(Unit) {
        hrViewModel.fetchLeaveBalances(limit = 100)
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && pagingState.canLoadMore && !pagingState.isLoadingMore) {
            hrViewModel.loadMoreLeaveBalances()
        }
    }

    val allSelected = pagingState.items.isNotEmpty() &&
            pagingState.items.all { it.resolvedId in selectedIds }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Reusable Page Header
            TitleBar(
                title = "Leave Management",
                subtitle = "HR > Leave Management",
                onClose = onBack
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.6f))

            // Reusable Search and Filter Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    hrViewModel.onLeaveBalanceSearchChanged(query)
                },
                placeholder = "Search employee...",
                filterCount = activeFilterCount,
                onFilterClick = { filterDrawerState.open() },
                accentColor = Primary,
                borderColor = sectionBorder,
                textSecondaryColor = mutedText,
                height = tokens.fieldHeight
            )

            // Select All Employees Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
                    .clickable {
                        selectedIds.clear()
                        if (!allSelected) {
                            selectedIds.addAll(pagingState.items.map { it.resolvedId })
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { checked ->
                        selectedIds.clear()
                        if (checked) {
                            selectedIds.addAll(pagingState.items.map { it.resolvedId })
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primary,
                        uncheckedColor = mutedText
                    )
                )
                Text(
                    text = "Select all employees",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
            }

            // Balance List Content
            when {
                pagingState.isInitialLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                !error.isNullOrBlank() && pagingState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error.orEmpty(),
                            color = redText,
                            fontSize = tokens.bodySmall
                        )
                    }
                }

                pagingState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No leave balance allocations found.",
                            color = mutedText,
                            fontSize = tokens.bodySmall
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = tokens.buttonHeight * 1.8f)
                    ) {
                        items(
                            items = pagingState.items,
                            key = { it.resolvedId.ifBlank { "${it.extractEmployeeName()}_${it.hashCode()}" } }
                        ) { item ->
                            val itemId = item.resolvedId
                            LeaveBalanceDataCard(
                                item = item,
                                leaveTypeName = item.extractLeaveTypeName(leaveTypeMap),
                                isChecked = itemId in selectedIds,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (itemId !in selectedIds) selectedIds.add(itemId)
                                    } else {
                                        selectedIds.remove(itemId)
                                    }
                                },
                                onApplyLeaveClick = onApplyLeave
                            )
                        }

                        if (pagingState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(tokens.extraPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Primary,
                                        modifier = Modifier.size(tokens.iconSize * 1.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reusable Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Leave Balances",
            sections = filterSections,
            onApply = { updatedSections ->
                val budgetOption = updatedSections
                    .find { it.title == "Budget Status" }
                    ?.options
                    ?.firstOrNull { it.isSelected }
                    ?.id ?: "all"

                val leaveTypeOption = updatedSections
                    .find { it.title == "Leave Type" }
                    ?.options
                    ?.firstOrNull { it.isSelected }
                    ?.id ?: "all"

                selectedBudgetFilter = budgetOption
                selectedLeaveTypeFilter = leaveTypeOption
                hrViewModel.applyLeaveBalanceFilters(leaveTypeOption, budgetOption)
            },
            onClearAll = {
                selectedBudgetFilter = "all"
                selectedLeaveTypeFilter = "all"
                hrViewModel.resetLeaveBalanceFilters()
            }
        )
    }
}

@Composable
private fun LeaveBalanceDataCard(
    item: LeaveBalanceItemDto,
    leaveTypeName: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onApplyLeaveClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val allocated = item.extractAllocatedDays().roundToInt()
    val used = item.extractUsedDays().roundToInt()
    val remaining = item.extractRemainingDays().roundToInt()
    val isInBudget = remaining > 0

    val cardActions = remember(onApplyLeaveClick) {
        listOf(
            MenuAction(
                label = "Apply Leave",
                icon = Icons.Default.Add,
                tint = Primary,
                textColor = TextPrimary,
                onClick = onApplyLeaveClick
            )
        )
    }

    DataCard(
        item = item,
        showHeaderDivider = true,
        headerContent = {
            // Top Row: Checkbox + Employee Name + Budget Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = onCheckedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Primary,
                            uncheckedColor = mutedText
                        )
                    )
                    Text(
                        text = item.extractEmployeeName(),
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(tokens.extraPadding * 0.5f))

                StatusBadge(
                    text = if (isInBudget) "In Budget" else "Exhausted",
                    bgColor = if (isInBudget) greenBg else redBg,
                    textColor = if (isInBudget) greentext else redText,
                    cornerRadius = tokens.cardCornerRadius * 0.6f,
                    showDot = false
                )
            }
        },
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Middle Row: Allocated | Used | Remaining
                DataCardStatsRow(
                    stats = listOf(
                        DataCardStat(
                            label = "Allocated",
                            value = "$allocated days",
                            valueColor = TextPrimary
                        ),
                        DataCardStat(
                            label = "Used",
                            value = "$used days",
                            valueColor = TextPrimary
                        ),
                        DataCardStat(
                            label = "Remaining",
                            value = "$remaining days",
                            valueColor = if (isInBudget) Primary else redText
                        )
                    )
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                // Bottom Row: Leave Type Name + Overflow Action Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = leaveTypeName,
                        fontSize = tokens.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    ActionDropdownMenu(
                        actions = cardActions,
                        icon = Icons.Default.MoreHoriz
                    )
                }
            }
        }
    )
}