@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead"
)
package com.cuso.tailor.view.home.sales.measurements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.tailor.R
import com.cuso.tailor.model.sales.MeasurementItem
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.blackTitle
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.DataCardImage
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.MeasurementsUiState
import com.cuso.tailor.viewmodel.MeasurementsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Suppress("UNUSED_PARAMETER")
@Composable
fun MeasurementsScreen(
    navController: NavController,
    viewModel: MeasurementsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCreateMeasurement: () -> Unit = {},
    onViewMeasurement: (MeasurementItem) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMore.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val filterDrawerState = rememberFilterDrawerState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(title = "Garments", options = emptyList(), isMultiSelect = true),
                FilterSection(title = "Garment Categories", options = emptyList(), isMultiSelect = true),
                FilterSection(title = "Status", options = emptyList(), isMultiSelect = true)
            )
        )
    }

    val allItems = (uiState as? MeasurementsUiState.Success)?.items ?: emptyList()
    LaunchedEffect(allItems) {
        if (allItems.isNotEmpty()) {
            val dynamicGarments = allItems.map { it.garmentName }.filter { it.isNotBlank() && it != "—" }.distinct()
            val dynamicCategories = allItems.map { it.categoryName }.filter { it.isNotBlank() && it != "—" }.distinct()
            val dynamicStatuses = allItems.map { it.status }.filter { it.isNotBlank() && it != "—" }.distinct()

            filterSections = filterSections.map { section ->
                when (section.title) {
                    "Garments" -> {
                        val selected = section.options.filter { it.isSelected }.map { it.label }.toSet()
                        section.copy(options = dynamicGarments.map { FilterOption(it.lowercase().replace(" ", "_"), it, it in selected) })
                    }
                    "Garment Categories" -> {
                        val selected = section.options.filter { it.isSelected }.map { it.label }.toSet()
                        section.copy(options = dynamicCategories.map { FilterOption(it.lowercase().replace(" ", "_"), it, it in selected) })
                    }
                    "Status" -> {
                        val selected = section.options.filter { it.isSelected }.map { it.label }.toSet()
                        section.copy(options = dynamicStatuses.map { FilterOption(it.lowercase().replace(" ", "_"), it, it in selected) })
                    }
                    else -> section
                }
            }
        }
    }

    val activeFilterCount by remember(filterSections) {
        derivedStateOf { filterSections.sumOf { s -> s.options.count { it.isSelected } } }
    }

    val filteredList by remember(allItems, searchQuery, filterSections) {
        derivedStateOf {
            val selectedGarments = filterSections.find { it.title == "Garments" }?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
            val selectedCategories = filterSections.find { it.title == "Garment Categories" }?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
            val selectedStatuses = filterSections.find { it.title == "Status" }?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()

            allItems.filter { item ->
                val matchesSearch = searchQuery.isBlank() ||
                        item.customerName.contains(searchQuery, ignoreCase = true) ||
                        item.contact.contains(searchQuery, ignoreCase = true) ||
                        item.customerCode.contains(searchQuery, ignoreCase = true) ||
                        item.garmentName.contains(searchQuery, ignoreCase = true) ||
                        item.categoryName.contains(searchQuery, ignoreCase = true)

                val matchesGarment = selectedGarments.isEmpty() || selectedGarments.any { it.equals(item.garmentName, ignoreCase = true) }
                val matchesCategory = selectedCategories.isEmpty() || selectedCategories.any { it.equals(item.categoryName, ignoreCase = true) }
                val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.any { it.equals(item.status, ignoreCase = true) }

                matchesSearch && matchesGarment && matchesCategory && matchesStatus
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && canLoadMore && !isLoadingMore) {
                    viewModel.loadMoreMeasurements()
                }
            }
    }

    FabScaffold(
        fab = FabConfig(
            label = "New Measurement", // <-- Updated label
            icon = Icons.Default.Add,
            onClick = onCreateMeasurement, // <-- Triggers navigation to measurement entry screen
            bottomPadding = 50.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TitleBar("Measurements", onClose = onBack)
            }

            HorizontalDivider(color = title_border)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Measurements...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        filterCount = activeFilterCount,
                        onFilterClick = { filterDrawerState.open() }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (val state = uiState) {
                            is MeasurementsUiState.Loading -> {
                                ListSkeleton()
                            }

                            is MeasurementsUiState.Error -> {
                                AppErrorState(
                                    title = "Failed to load Measurements",
                                    message = state.message,
                                    onRetry = { viewModel.loadMeasurements() }
                                )
                            }

                            is MeasurementsUiState.Success -> {
                                if (filteredList.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(whiteBg, RoundedCornerShape(12.dp))
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .background(light_grey, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.List,
                                                    contentDescription = null,
                                                    tint = Color(0xFF9CA3AF),
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }

                                            Text(
                                                text = "No Measurements Found",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF111827)
                                            )

                                            Text(
                                                text = if (searchQuery.isNotBlank() || activeFilterCount > 0) {
                                                    "No measurement records match your search or filter criteria."
                                                } else {
                                                    "No measurement records available."
                                                },
                                                fontSize = 14.sp,
                                                color = Color(0xFF6B7280),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 32.dp)
                                            )

                                            if (searchQuery.isNotBlank() || activeFilterCount > 0) {
                                                OutlinedButton(
                                                    onClick = {
                                                        searchQuery = ""
                                                        filterSections = filterSections.map { section ->
                                                            section.copy(options = section.options.map { it.copy(isSelected = false) })
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFF3B3BF9)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3B3BF9)),
                                                    modifier = Modifier.padding(top = 8.dp)
                                                ) {
                                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Clear Filters", fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 90.dp)
                                    ) {
                                        items(filteredList, key = { it.id.ifBlank { it.customerId } }) { item ->
                                            val isActive = item.status.equals("active", ignoreCase = true)
                                            val badgeTextColor = if (isActive) darkGreenBg else yellowText
                                            val badgeBgColor = if (isActive) greenBg else yellowBg

                                            val garmentDisplay = if (item.categoryName.isNotBlank() && item.categoryName != "—") {
                                                "${item.garmentName} • ${item.categoryName}"
                                            } else {
                                                item.garmentName
                                            }

                                            val hasProfilePic = !item.profileImageUrl.isNullOrBlank()

                                            DataCard(
                                                item = item,
                                                onClick = { onViewMeasurement(item) },
                                                eyebrowText = "Code: ${item.customerCode}",
                                                topBadgeText = item.status,
                                                topBadgeTextColor = badgeTextColor,
                                                topBadgeBgColor = badgeBgColor,
                                                topBadgeShowDot = false,
                                                showActionsInHeader = true,
                                                image = if (hasProfilePic) {
                                                    DataCardImage(
                                                        url = item.profileImageUrl,
                                                        size = 36.dp,
                                                        shape = CircleShape,
                                                        backgroundColor = Color.Transparent
                                                    )
                                                } else {
                                                    DataCardImage(
                                                        painter = painterResource(R.drawable.ic_person),
                                                        size = 36.dp,
                                                        shape = CircleShape,
                                                        tint = blackTitle,
                                                        backgroundColor = light_grey
                                                    )
                                                },
                                                title = item.customerName,
                                                footerFields = listOf(
                                                    DataCardField(text = "Phone: ${item.contact}", textColor = mutedText),
                                                    DataCardField(text = "Garment: $garmentDisplay", textColor = mutedText),
                                                    DataCardField(text = "Measured: ${item.measuredDate}", textColor = mutedText)
                                                ),
                                                actions = listOf(
                                                    MenuAction("View", Icons.Default.Visibility) {
                                                        onViewMeasurement(item)
                                                    }
                                                )
                                            )
                                        }

                                        if (isLoadingMore) {
                                            item {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CirculerProgressIndicatorSmall()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                FilterDrawer(
                    state = filterDrawerState,
                    title = "Filter Measurements",
                    sections = filterSections,
                    onApply = { updatedSections -> filterSections = updatedSections },
                    onClearAll = {
                        filterSections = filterSections.map { section ->
                            section.copy(options = section.options.map { option -> option.copy(isSelected = false) })
                        }
                    }
                )
            }
        }
    }
}