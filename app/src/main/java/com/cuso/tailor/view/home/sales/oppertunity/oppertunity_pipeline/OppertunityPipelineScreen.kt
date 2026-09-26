package com.cuso.tailor.view.home.sales.oppertunity.oppertunity_pipeline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
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
    var selectedStageIndex by remember { mutableIntStateOf(0) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val apiStages by viewModel.opportunityStages.collectAsStateWithLifecycle()

    // Debounced search
    LaunchedEffect(searchQuery) {
        delay(400)
        viewModel.onSearch(searchQuery)
    }

    val allDeals = (uiState as? OpportunitiesUiState.Success)?.items ?: emptyList()

    // Dynamic stage tabs with actual counts
    val stageTabs = remember(apiStages, allDeals) {
        if (apiStages.isEmpty()) {
            listOf("All Deals (${allDeals.size})")
        } else {
            apiStages.map { stage ->
                val count = allDeals.count { it.status.equals(stage.name, ignoreCase = true) }
                "${stage.name} ($count)"
            }
        }
    }

    // Filtered deals for the currently selected stage tab
    val currentStageName = remember(apiStages, selectedStageIndex) {
        apiStages.getOrNull(selectedStageIndex)?.name
    }

    val filteredDeals = remember(allDeals, currentStageName) {
        if (currentStageName == null) allDeals
        else allDeals.filter { it.status.equals(currentStageName, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(bottom = tokens.buttonHeight + tokens.extraPadding * 2)
        ) {
            // Header Bar
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar(title = "Opportunity Pipeline", onClose = onClose)
                HorizontalDivider(color = title_border)
            }

            // Search Bar & Filter Action
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search pipeline leads...",
                accentColor = BluePrimary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = {}
            )

            // Dynamic Pipeline Stage Category Tabs
            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = tokens.screenPadding,
                    vertical = tokens.extraPadding * 0.6f
                ),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
            ) {
                items(stageTabs.size) { index ->
                    val isSelected = selectedStageIndex == index
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 1.25f),
                        color = if (isSelected) Primary else whiteBg,
                        border = BorderStroke(1.dp, if (isSelected) Primary else BorderGray),
                        modifier = Modifier.clickable { selectedStageIndex = index }
                    ) {
                        Text(
                            text = stageTabs[index],
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) whiteBg else headerGrey,
                            modifier = Modifier.padding(
                                horizontal = tokens.screenPadding * 0.85f,
                                vertical = tokens.extraPadding * 0.7f
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            // Pipeline Deals List
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
                                text = "No deals in this stage",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                color = headerGrey
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = tokens.screenPadding,
                                vertical = tokens.extraPadding * 0.5f
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
                                // Center Floating Add Deal Action
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

        // Bottom Controls: Clear All and Create Lead
        StepNavigationFab(
            showBack = true,
            backLabel = "Clear All",
            showBackArrow = false,
            onBack = onClearAll,
            trailingAction = TrailingFabAction.Next(
                label = "Create Lead",
                onClick = onCreateLead
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
            // Left Indicator Strip that seamlessly curves with card boundary
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
                // Header: Badge and Options
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

                // Customer Name
                Text(
                    text = item.customerName,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                Spacer(Modifier.height(2.dp))

                // Deal Description
                Text(
                    text = item.title,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )

                Spacer(Modifier.height(10.dp))

                // Deal Amount
                Text(
                    text = item.estimatedValue,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = grey_border, thickness = 1.dp)
                Spacer(Modifier.height(10.dp))

                // Footer: Code and Closing Date
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