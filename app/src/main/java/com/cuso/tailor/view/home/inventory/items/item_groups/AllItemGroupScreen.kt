@file:Suppress("unused")

package com.cuso.tailor.view.home.inventory.items.item_groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.ItemGroupDto
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.background_light_purple
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.ThreeDotLoading
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AllItemGroupScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onAddItemGroup: () -> Unit = {},
    onView: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val uiState by viewModel.uiState.collectAsState()
    val deleteSuccessMessage by viewModel.deleteItemGroupSuccess.collectAsState()

    var displayedErrorMessage by remember { mutableStateOf<String?>(null) }
    var itemGroupToDelete by remember { mutableStateOf<ItemGroupDto?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // ── Filter Drawer State & Sections ──
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Category",
                    type = FilterSectionType.CHECKBOX_LIST,
                    options = listOf(
                        FilterOption("shirt", "Shirt"),
                        FilterOption("pant", "Pant"),
                        FilterOption("blazer", "Blazer"),
                        FilterOption("fabric", "Fabric")
                    )
                ),
                FilterSection(
                    title = "Fabric",
                    type = FilterSectionType.CHECKBOX_LIST,
                    options = listOf(
                        FilterOption("cotton", "Cotton"),
                        FilterOption("linen", "Linen"),
                        FilterOption("terry_wool", "Terry Wool")
                    )
                )
            )
        )
    }

    // Dynamic populate options from Item Groups response
    LaunchedEffect(uiState.filteredList) {
        val dynamicFabrics = uiState.filteredList.mapNotNull { it.productType ?: it.type }.filter { it.isNotBlank() }.distinct()
        if (dynamicFabrics.isNotEmpty()) {
            filterSections = filterSections.map { sec ->
                if (sec.title == "Fabric") {
                    val allFabrics = (sec.options.map { it.label } + dynamicFabrics).distinct()
                    sec.copy(options = allFabrics.map { FilterOption(id = it.lowercase(), label = it) })
                } else sec
            }
        }
    }

    val activeFilterCount by remember(filterSections) {
        derivedStateOf { filterSections.sumOf { sec -> sec.options.count { it.isSelected } } }
    }

    LaunchedEffect(Unit) { viewModel.refreshItemGroups() }

    // Client-side Filter
    val displayedList by remember(uiState.filteredList, filterSections) {
        derivedStateOf {
            val selectedCategories = filterSections.find { it.title == "Category" }
                ?.options?.filter { it.isSelected }?.map { it.label.lowercase() } ?: emptyList()
            val selectedFabrics = filterSections.find { it.title == "Fabric" }
                ?.options?.filter { it.isSelected }?.map { it.label.lowercase() } ?: emptyList()

            uiState.filteredList.filter { group ->
                val matchesCategory = selectedCategories.isEmpty() ||
                        selectedCategories.any { group.name.contains(it, ignoreCase = true) }
                val matchesFabric = selectedFabrics.isEmpty() ||
                        selectedFabrics.any { (group.productType ?: group.type)?.contains(it, ignoreCase = true) == true }

                matchesCategory && matchesFabric
            }
        }
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize().background(Primary_background),
        fab = FabConfig(
            label = "Create item group",
            icon = Icons.Default.Add,
            onClick = {
                viewModel.clearSelectedItemGroupDetail()
                onAddItemGroup()
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(title = "Item Group", onClose = onDismiss)
                }

                SearchFilterBar(
                    query = uiState.searchQuery,
                    onQueryChange = { query -> viewModel.onSearchQueryChanged(query) },
                    placeholder = "Search Item Group...",
                    filterCount = activeFilterCount,
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = mutedText,
                    onFilterClick = { filterDrawerState.open() }
                )

                HorizontalDivider(color = title_border, thickness = 2.dp)

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when {
                        uiState.isLoading && uiState.filteredList.isEmpty() -> ListSkeleton()
                        uiState.errorMessage != null && uiState.filteredList.isEmpty() -> {
                            AppErrorState(
                                title = "Failed to load item groups",
                                message = uiState.errorMessage ?: "Something went wrong.",
                                onRetry = { viewModel.refreshItemGroups() }
                            )
                        }
                        displayedList.isEmpty() -> EmptyStateView(tokens = tokens)
                        else -> {
                            ItemGroupListView(
                                itemGroups = displayedList,
                                listState = listState,
                                isLoadingMore = uiState.isLoadingMore,
                                tokens = tokens,
                                onView = onView,
                                onEdit = { id ->
                                    viewModel.fetchItemGroupViewOne(id) { onEdit(id) }
                                },
                                onDelete = { group ->
                                    itemGroupToDelete = group
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // ── Filter Item Groups Drawer ──
            FilterDrawer(
                state = filterDrawerState,
                title = "Filter Item Groups",
                sections = filterSections,
                onApply = { updated -> filterSections = updated },
                onClearAll = {
                    filterSections = filterSections.map { sec ->
                        sec.copy(options = sec.options.map { it.copy(isSelected = false) })
                    }
                }
            )

            if (showDeleteDialog && itemGroupToDelete != null) {
                DeleteModel(
                    title = "Delete Item Group",
                    message = "Are you sure you want to delete \"${itemGroupToDelete?.name}\"?",
                    onDismiss = { showDeleteDialog = false; itemGroupToDelete = null },
                    onDelete = {
                        itemGroupToDelete?.let { group -> viewModel.deleteItemGroup(id = group.id) }
                        showDeleteDialog = false
                        itemGroupToDelete = null
                    }
                )
            }

            DynamicIslandSuccess(message = deleteSuccessMessage, onDismiss = { viewModel.clearDeleteSuccessMessage() })
            DynamicIslandError(message = displayedErrorMessage, onDismiss = { displayedErrorMessage = null })
        }
    }
}

@Composable
private fun ItemGroupListView(
    itemGroups: List<ItemGroupDto>,
    listState: LazyListState,
    isLoadingMore: Boolean,
    tokens: AppDesignTokens,
    onView: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (group: ItemGroupDto) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = tokens.extraPadding)
    ) {
        items(
            items = itemGroups,
            key = { it.id.ifBlank { it.hashCode().toString() } }
        ) { group ->
            val attributesSummary = group.variantAttributes
                .joinToString(", ") { it.name }
                .ifBlank { "No attributes" }

            val formattedDate = formatIsoDate(group.createdAt)

            DataCard(
                item = group,
                title = group.name,
                topBadgeText = "${group.variantCount} Items",
                topBadgeTextColor = Primary,
                topBadgeBgColor = background_light_purple,
                topBadgeInline = true,
                footerAsRows = false,
                footerFields = listOf(
                    DataCardField(
                        label = "",
                        text = attributesSummary,
                        labelColor = mutedText,
                        textColor = mutedText
                    ),
                    DataCardField(
                        label = "",
                        text = formattedDate,
                        labelColor = mutedText,
                        textColor = mutedText
                    )
                ),
                actions = listOf(
                    MenuAction(
                        label = "View",
                        icon = Icons.Filled.Visibility,
                        onClick = { onView(group.id) }
                    ),
                    MenuAction(
                        label = "Edit",
                        icon = Icons.Filled.Edit,
                        onClick = { onEdit(group.id) }
                    ),
                    MenuAction(
                        label = "Delete",
                        icon = Icons.Filled.Delete,
                        tint = redText,
                        textColor = redText,
                        onClick = { onDelete(group) }
                    )
                ),
                onClick = { onView(group.id) }
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding / 2))
        }

        // Three-dot loading indicator: shown only while a next page request is in-flight
        if (isLoadingMore) {
            item(key = "pagination_threedot_loader") {
                ThreeDotLoading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun EmptyStateView(tokens: AppDesignTokens) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No item groups found",
            fontSize = tokens.bodyMedium,
            color = TextSecondary
        )
    }
}

private fun formatIsoDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        date?.let { outputFormat.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}