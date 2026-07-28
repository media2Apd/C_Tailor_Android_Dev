package com.cuso.mobile.view.home.reusablecomposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.view.composable.ScreenBreadcrumb

/**
 * ONE reusable header for any list screen:
 *  - breadcrumb row (click → opens Modules panel via onBreadcrumbClick)
 *  - search + filter bar (filter icon click → opens FilterDrawer automatically)
 *  - the FilterDrawer itself (state managed internally via rememberFilterDrawerState)
 *
 * Usage:
 * FilterableScreenHeader(
 *     breadcrumbSegments = listOf("Finance", "Sales Invoices"),
 *     onBreadcrumbClick = { modulesPanelInitialExpanded = "Finance"; showModulesPanel = true },
 *     searchQuery = searchQuery,
 *     onSearchQueryChange = { searchQuery = it },
 *     searchPlaceholder = "Search Invoices...",
 *     filterSections = filterSections,
 *     onApplyFilters = { updated -> filterSections = updated /* + refetch */ },
 *     onClearFilters = { /* reset + refetch */ }
 * )
 */
@Composable
fun FilterableScreenHeader(
    modifier: Modifier = Modifier,
    breadcrumbSegments: List<String>,
    onBreadcrumbClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchPlaceholder: String = "Search...",
    filterSections: List<FilterSection>,
    onApplyFilters: (List<FilterSection>) -> Unit,
    onClearFilters: () -> Unit,
    filterDrawerTitle: String = "Filters"
) {

    val filterDrawerState = rememberFilterDrawerState()

    Column(modifier = modifier.fillMaxWidth()) {
        ScreenBreadcrumb(
            segments = breadcrumbSegments,
            onClick = onBreadcrumbClick
        )

        SearchFilterBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            placeholder = searchPlaceholder,
            accentColor = BluePrimary,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { filterDrawerState.open() }   // ✅ THIS is the key wiring —
            // filter icon tap now opens the FilterDrawer directly, no boilerplate per screen
        )
    }

    // ✅ Drawer lives here too — one call site owns the whole feature
    FilterDrawer(
        state = filterDrawerState,
        title = filterDrawerTitle,
        sections = filterSections,
        onApply = onApplyFilters,
        onClearAll = onClearFilters
    )
}