@file:Suppress("SpellCheckingInspection", "unused")

package com.cuso.tailor.view.home.inventory.multi_channel_management.allocation_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*

// ── Data Model ──
data class AllocationListItem(
    val id: String,
    val name: String,
    val type: String,
    val channelCount: String,
    val status: String = "Active"
)

// ── Default Filter Configuration for Allocation List ──
fun getAllocationFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Status",
        options = listOf(
            FilterOption("active", "Active", isSelected = false),
            FilterOption("inactive", "Inactive", isSelected = false)
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Allocation Type",
        options = listOf(
            FilterOption("percentage", "% Percentage", isSelected = false),
            FilterOption("equal", "Equal", isSelected = false),
            FilterOption("priority", "Priority", isSelected = false)
        ),
        isMultiSelect = true
    )
)

@Composable
fun AllocationListScreen(
    onClose: () -> Unit = {},
    onAddNew: () -> Unit = {},
    onViewItem: (AllocationListItem) -> Unit = {},
    onEditItem: (AllocationListItem) -> Unit = {},
    onDeleteItem: (AllocationListItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var searchQuery by remember { mutableStateOf("") }
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember { mutableStateOf(getAllocationFilterSections()) }

    // Sample list matching the provided screenshot
    val allocationList = remember {
        mutableStateListOf(
            AllocationListItem(
                id = "1",
                name = "Default Ecommerce",
                type = "% Percentage",
                channelCount = "4 Channels",
                status = "Active"
            ),
            AllocationListItem(
                id = "2",
                name = "Flash Sales",
                type = "Equal",
                channelCount = "4 Channels",
                status = "Active"
            ),
            AllocationListItem(
                id = "3",
                name = "Flash Sales",
                type = "Equal",
                channelCount = "4 Channels",
                status = "Active"
            ),
            AllocationListItem(
                id = "4",
                name = "Flash Sales",
                type = "Equal",
                channelCount = "4 Channels",
                status = "Active"
            )
        )
    }

    val activeFilterCount by remember(filterSections) {
        derivedStateOf {
            filterSections.sumOf { section -> section.options.count { it.isSelected } }
        }
    }

    // Filter Logic
    val filteredList by remember(searchQuery, allocationList, filterSections) {
        derivedStateOf {
            val selectedStatuses = filterSections.find { it.title == "Status" }
                ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()

            val selectedTypes = filterSections.find { it.title == "Allocation Type" }
                ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()

            allocationList.filter { item ->
                val matchesSearch = searchQuery.isBlank() ||
                        item.name.contains(searchQuery, ignoreCase = true) ||
                        item.type.contains(searchQuery, ignoreCase = true)

                val matchesStatus = selectedStatuses.isEmpty() ||
                        selectedStatuses.any { it.equals(item.status, ignoreCase = true) }

                val matchesType = selectedTypes.isEmpty() ||
                        selectedTypes.any { it.equals(item.type, ignoreCase = true) }

                matchesSearch && matchesStatus && matchesType
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top TitleBar ──
            TitleBar(
                title = "Allocation List",
                onClose = onClose
            )

            HorizontalDivider(color = title_border, thickness = 1.dp)

            // ── Search & Filter Bar ──
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search SO number, customer...",
                filterCount = activeFilterCount,
                onFilterClick = { filterDrawerState.open() },
                accentColor = Primary,
                borderColor = grey_border,
                textSecondaryColor = headerGrey
            )


            // ── Main List Content ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No allocations found",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList, key = { it.id }) { item ->
                            DataCard(
                                item = item,
                                title = item.name,
                                titleFontWeight = FontWeight.SemiBold,
                                topBadgeText = "● ${item.status}",
                                topBadgeBgColor = greenBg,
                                topBadgeTextColor = darkGreenBg,
                                topBadgeInline = true,
                                showDateIcon = false,
                                showHeaderDivider = false,
                                actions = listOf(
                                    MenuAction("View", Icons.Default.Visibility) { onViewItem(item) },
                                    MenuAction("Edit", Icons.Default.Edit) { onEditItem(item) },
                                    MenuAction("Delete", Icons.Default.Delete, tint = redText, textColor = redText) { onDeleteItem(item) }
                                ),
                                content = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Type Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Type",
                                                fontSize = tokens.caption,
                                                color = headerGrey
                                            )
                                            Text(
                                                text = item.type,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Normal,
                                                color = TextPrimary
                                            )
                                        }

                                        // Channel Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Channel",
                                                fontSize = tokens.caption,
                                                color = headerGrey
                                            )
                                            Text(
                                                text = item.channelCount,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Normal,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // ── Filter Drawer ──
                FilterDrawer(
                    state = filterDrawerState,
                    title = "Filters",
                    sections = filterSections,
                    onApply = { updatedSections ->
                        filterSections = updatedSections
                    },
                    onClearAll = {
                        filterSections = filterSections.map { section ->
                            section.copy(options = section.options.map { it.copy(isSelected = false) })
                        }
                    }
                )
            }
        }
    }
}