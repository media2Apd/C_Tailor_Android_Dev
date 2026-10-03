@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "unused_variable",
    "unused_parameter"
)

package com.cuso.tailor.view.home.hr.payroll_management.salary_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.SalaryComponentItem
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.UiState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.view.composable.toTitleCase
import com.cuso.tailor.viewmodel.HrViewModel

/**
 * Generates default filter configurations for Salary Components.
 */
fun getSalaryComponentFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Type",
        options = listOf(
            FilterOption("earning", "Earning"),
            FilterOption("deduction", "Deduction")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Category",
        options = listOf(
            FilterOption("custom", "Custom"),
            FilterOption("voluntary", "Voluntary"),
            FilterOption("statutory", "Statutory")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Status",
        options = listOf(
            FilterOption("active", "Active"),
            FilterOption("inactive", "Inactive")
        ),
        isMultiSelect = true
    )
)

/**
 * Screen displaying the list of all salary components with dynamic filters.
 */
@Composable
fun SalaryComponentListScreen(
    onClose: () -> Unit,
    onCreateComponentClick: () -> Unit,
    onViewComponentClick: (String) -> Unit,
    onEditComponentClick: (String) -> Unit,
    viewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val salaryComponents by viewModel.components.collectAsStateWithLifecycle()
    val listState by viewModel.list.collectAsStateWithLifecycle()
    val isLoading = listState is UiState.Loading

    var searchQuery by remember { mutableStateOf("") }

    // Drawer state and applied filter selections
    val filterDrawerState = rememberFilterDrawerState()
    var appliedSections by remember { mutableStateOf(getSalaryComponentFilterSections()) }

    // Count of actively selected filter criteria
    val selectedFilterCount = remember(appliedSections) {
        appliedSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.loadList()
    }

    // Filter components according to search input and active filter sections
    val filteredComponents = remember(salaryComponents, searchQuery, appliedSections) {
        val selectedTypes = appliedSections
            .find { it.title.equals("Type", ignoreCase = true) }
            ?.options?.filter { it.isSelected }?.map { it.id.lowercase() }.orEmpty()

        val selectedCategories = appliedSections
            .find { it.title.equals("Category", ignoreCase = true) }
            ?.options?.filter { it.isSelected }?.map { it.id.lowercase() }.orEmpty()

        val selectedStatuses = appliedSections
            .find { it.title.equals("Status", ignoreCase = true) }
            ?.options?.filter { it.isSelected }?.map { it.id.lowercase() }.orEmpty()

        salaryComponents.filter { item ->
            // Search query matching
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    (item.nameInPayslip?.contains(searchQuery, ignoreCase = true) == true) ||
                    item.type.contains(searchQuery, ignoreCase = true)

            // Type filter matching
            val matchesType = selectedTypes.isEmpty() ||
                    selectedTypes.contains(item.type.lowercase())

            // Category filter matching
            val matchesCategory = selectedCategories.isEmpty() ||
                    (item.category != null && selectedCategories.contains(item.category.lowercase()))

            // Status filter matching
            val matchesStatus = selectedStatuses.isEmpty() ||
                    (selectedStatuses.contains("active") && item.isActive) ||
                    (selectedStatuses.contains("inactive") && !item.isActive)

            matchesSearch && matchesType && matchesCategory && matchesStatus
        }
    }

    // Wrap screen with reusable FabScaffold for floating Create button
    FabScaffold(
        fab = FabConfig(
            label = "Create Component",
            icon = Icons.Default.Add,
            onClick = onCreateComponentClick
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {

                // Header Bar
                TitleBar(
                    title = "Salary Comp List",
                    onClose = onClose
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Search and Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search components...",
                    showFilterIcon = true,
                    filterCount = selectedFilterCount,
                    onFilterClick = { filterDrawerState.open() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Body Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (isLoading && salaryComponents.isEmpty()) {
                        ListSkeleton()
                    } else if (filteredComponents.isEmpty()) {
                        Text(
                            text = "No components found",
                            fontSize = tokens.bodySmall,
                            color = mutedText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredComponents,
                                key = { it.id }
                            ) { component ->
                                SalaryComponentListItem(
                                    component = component,
                                    onViewClick = { onViewComponentClick(component.id) },
                                    onEditClick = { onEditComponentClick(component.id) },
                                    onToggleStatus = {
                                        viewModel.toggleStatus(
                                            id = component.id,
                                            isActive = !component.isActive,
                                            onError = { DynamicIslandManager.showError(it) }
                                        )
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteComponent(
                                            id = component.id,
                                            onError = { DynamicIslandManager.showError(it) }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Filter Drawer
            FilterDrawer(
                state = filterDrawerState,
                title = "Filter Components",
                sections = appliedSections,
                onApply = { updatedSections ->
                    appliedSections = updatedSections
                },
                onClearAll = {
                    appliedSections = getSalaryComponentFilterSections()
                }
            )
        }
    }
}

/**
 * Individual Salary Component Card item.
 */
@Composable
private fun SalaryComponentListItem(
    component: SalaryComponentItem,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Action items for the three-dot dropdown menu
    val menuActions = remember(component.isActive) {
        listOf(
            MenuAction(
                label = "View",
                icon = Icons.Outlined.RemoveRedEye,
                textColor = TextPrimary,
                tint = mutedText,
                onClick = onViewClick
            ),
            MenuAction(
                label = "Edit",
                icon = Icons.Default.Edit,
                textColor = TextPrimary,
                tint = mutedText,
                onClick = onEditClick
            ),
            MenuAction(
                label = if (component.isActive) "Deactivate" else "Activate",
                textColor = if (component.isActive) redText else greentext,
                tint = if (component.isActive) redText else greentext,
                onClick = onToggleStatus
            ),
            MenuAction(
                label = "Delete",
                icon = Icons.Outlined.Delete,
                textColor = redText,
                tint = redText,
                onClick = onDeleteClick
            )
        )
    }

    // Calculation format logic for secondary tag
    val calculationLabel = remember(component.calculationType, component.value) {
        when (component.calculationType) {
            "percentage_of_basic" -> "${component.value?.toInt() ?: 0}% of Basic"
            "fixed" -> "Fixed"
            else -> component.calculationType?.replace("_", " ")?.toTitleCase() ?: "Fixed"
        }
    }

    // Card boundary with clean outline
    DataCard(
        item = component,
        title = component.name,
        titleFontWeight = FontWeight.Medium,
        titleColor = title_color,
        topBadgeText = if (component.isActive) "Active" else "Inactive",
        topBadgeTextColor = if (component.isActive) greentext else redText,
        topBadgeBgColor = if (component.isActive) greenBg else redBg,
        topBadgeDotColor = if (component.isActive) greentext else redText,
        topBadgeInline = true,
        actions = menuActions,
        onClick = { onViewClick() },
        content = {
            // Earning/Deduction & Calculation pill tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                // Type Badge (Earning or Deduction)
                StatusBadge(
                    text = component.type.replaceFirstChar { it.uppercase() },
                    bgColor = if (component.type.equals("earning", ignoreCase = true)) primary_light else redBg,
                    textColor = if (component.type.equals("earning", ignoreCase = true)) Primary else redText,
                    showDot = false
                )

                // Calculation Type Badge (Fixed, % of Basic, etc.)
                StatusBadge(
                    text = calculationLabel,
                    bgColor = light_grey,
                    textColor = TextSecondary,
                    showDot = false
                )
            }
        }
    )
}