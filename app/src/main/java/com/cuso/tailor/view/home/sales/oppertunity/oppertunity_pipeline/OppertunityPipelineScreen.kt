@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "VariableNeverRead"
)

package com.cuso.tailor.view.home.sales.oppertunity.oppertunity_pipeline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.OpportunityListItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.OpportunitiesUiState
import com.cuso.tailor.viewmodel.OpportunityViewModel
import kotlinx.coroutines.delay

@Composable
fun OpportunityPipelineScreen(
    onClose: () -> Unit,
    onCreateLead: () -> Unit,
    onAddDeal: () -> Unit,
    onViewOpportunity: (String) -> Unit = {},
    onClearAll: () -> Unit = {},
    viewModel: OpportunityViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val filterDrawerState = rememberFilterDrawerState()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val apiStages by viewModel.opportunityStages.collectAsStateWithLifecycle()

    val allDeals = (uiState as? OpportunitiesUiState.Success)?.items ?: emptyList()

    // Retrieve stage options from API or fallback
    val stageNames = remember(apiStages) {
        if (apiStages.isNotEmpty()) apiStages.map { it.name }
        else listOf("New Opportunity", "Qualification", "Proposal/Quotation", "Negotiation", "Closed Won", "Closed Lost")
    }

    // Extract unique product categories from deal items
    val categoryNames = remember(allDeals) {
        val distinct = allDeals.map { it.category }.filter { it.isNotBlank() }.distinct()
        if (distinct.isNotEmpty()) distinct else listOf("Bridal Blouse")
    }

    // Initialize filter sections
    var filterSections by remember(stageNames, categoryNames) {
        mutableStateOf(getDefaultOpportunityFilterSections(stageNames, categoryNames))
    }

    // Total selected filters count badge
    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    // Debounced search query
    LaunchedEffect(searchQuery) {
        delay(400)
        viewModel.onSearch(searchQuery)
    }

    // Apply active filter selections to deals
    val filteredDeals = remember(allDeals, filterSections) {
        val selectedStages = filterSections.find { it.title == "Deal Stage" }?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val selectedCategories = filterSections.find { it.title == "Product Category" }?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()

        allDeals.filter { item ->
            val matchesStage = selectedStages.isEmpty() || selectedStages.any { it.equals(item.status, ignoreCase = true) }
            val matchesCategory = selectedCategories.isEmpty() || selectedCategories.any { it.contains(item.category, ignoreCase = true) || item.category.contains(it, ignoreCase = true) }

            matchesStage && matchesCategory
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Reusable FabScaffold hosting the Create Lead FAB action
        FabScaffold(
            fab = FabConfig(
                label = "Create Lead",
                icon = Icons.Default.Add,
                onClick = onCreateLead,
                bottomPadding = 50.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Section 1: Top TitleBar (remains fixed on top and is not covered by FilterDrawer)
                Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                    TitleBar(title = "Opportunity Pipeline", onClose = onClose)
                }
                HorizontalDivider(color = title_border)

                // Section 2: Body container (FilterDrawer slides in below TitleBar within this Box)
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search pipeline leads...",
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            filterCount = activeFilterCount,
                            onFilterClick = { filterDrawerState.open() }
                        )

                        Spacer(Modifier.height(tokens.extraPadding * 0.6f))

                        when (uiState) {
                            is OpportunitiesUiState.Loading -> {
                                ListSkeleton()
                            }

                            is OpportunitiesUiState.Error -> {
                                AppErrorState(
                                    title = "Failed to load pipeline",
                                    message = (uiState as OpportunitiesUiState.Error).message,
                                    onRetry = { viewModel.loadOpportunities() }
                                )
                            }

                            is OpportunitiesUiState.Success -> {
                                if (filteredDeals.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No deals found",
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.Normal,
                                            color = headerGrey
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(
                                            start = tokens.screenPadding,
                                            end = tokens.screenPadding,
                                            top = tokens.extraPadding * 0.5f,
                                            bottom = 90.dp
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        items(filteredDeals, key = { it.id }) { item ->
                                            PipelineCard(
                                                item = item,
                                                onClick = { onViewOpportunity(item.id) }
                                            )
                                        }

                                        item {
                                            // Centered Add Deal icon button
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = tokens.extraPadding),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                IconButton(
                                                    onClick = onAddDeal,
                                                    modifier = Modifier
                                                        .size(tokens.fieldHeight)
                                                        .clip(CircleShape)
                                                        .background(light_blue)
                                                        .border(1.dp, light_blue_border, CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Add Deal",
                                                        tint = Primary,
                                                        modifier = Modifier.size(tokens.iconSize)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FilterDrawer overlay positioned strictly below TitleBar
                    FilterDrawer(
                        state = filterDrawerState,
                        title = "Filters",
                        sections = filterSections,
                        onApply = { updatedSections ->
                            filterSections = updatedSections
                        },
                        onClearAll = {
                            filterSections = filterSections.map { sec ->
                                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PipelineCard(
    item: OpportunityListItem,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val cardRadius = 16.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(cardRadius),
        border = BorderStroke(1.dp, BorderGray),
        color = whiteBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.5.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = cardRadius,
                            bottomStart = cardRadius,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(Primary)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(primary_light)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.status.toTitleCase(),
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = headerGrey,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = item.customerName,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = item.title,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = item.estimatedValue,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = grey_border, thickness = 1.dp)
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.code,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.closingDate,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                    }
                }
            }
        }
    }
}