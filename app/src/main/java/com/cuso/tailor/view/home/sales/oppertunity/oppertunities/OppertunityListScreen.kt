@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.sales.oppertunity.oppertunities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
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
import com.cuso.tailor.viewmodel.OpportunityActionState
import com.cuso.tailor.viewmodel.OpportunityViewModel
import kotlinx.coroutines.delay

@Composable
fun OpportunitiesListScreen(
    onClose: () -> Unit,
    onAddDeal: () -> Unit,
    onViewOpportunity: (OpportunityListItem) -> Unit,
    onEditOpportunity: (OpportunityListItem) -> Unit = {}, // Callback for Edit
    viewModel: OpportunityViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()

    var opportunityPendingDelete by remember { mutableStateOf<OpportunityListItem?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchQuery) {
        delay(400)
        viewModel.onSearch(searchQuery)
    }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is OpportunityActionState.Success -> {
                actionMessage = (deleteState as OpportunityActionState.Success).message
                viewModel.resetActionStates()
            }
            is OpportunityActionState.Error -> {
                errorMessage = (deleteState as OpportunityActionState.Error).message
                viewModel.resetActionStates()
            }
            else -> Unit
        }
    }

    FabScaffold(
        fab = FabConfig(
            label = "Add Opportunity",
            icon = Icons.Default.Add,
            onClick = onAddDeal,
            bottomPadding = 50.dp
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                    TitleBar(title = "Opportunities", onClose = onClose)
                    HorizontalDivider(color = title_border)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Customers...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = {}
                )

                OpportunityMetricsGrid()

                when (val state = uiState) {
                    is OpportunitiesUiState.Loading -> {
                        ListSkeleton()
                    }
                    is OpportunitiesUiState.Error -> {
                        AppErrorState(
                            title = "Failed to load Opportunities",
                            message = state.message,
                            onRetry = { viewModel.loadOpportunities() }
                        )
                    }
                    is OpportunitiesUiState.Success -> {
                        val opportunities = state.items

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.8f)
                        ) {
                            Text(
                                text = "Showing ${opportunities.size} Deals",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(top = tokens.extraPadding * 0.5f, bottom = 90.dp)
                        ) {
                            items(opportunities, key = { it.id }) { item ->
                                val cardActions = listOf(
                                    MenuAction("View", Icons.Default.Visibility) { onViewOpportunity(item) },
                                    MenuAction("Edit", Icons.Default.Edit) { onEditOpportunity(item) }, // Navigates to Edit
                                    MenuAction("Delete", Icons.Default.Delete, tint = redText, textColor = redText) {
                                        opportunityPendingDelete = item
                                    }
                                )

                                DataCard(
                                    item = item,
                                    onClick = { onViewOpportunity(item) },
                                    title = item.title,
                                    subtitle = item.customerName,
                                    headerContent = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.code,
                                                fontSize = tokens.bodySmall,
                                                color = headerGrey
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val (badgeBg, badgeTextColor) = when (item.status.lowercase()) {
                                                    "closed won", "won" -> greenBg to greentext
                                                    "qualification" -> primary_light to Primary
                                                    "closed lost", "lost" -> redBg to redText
                                                    else -> yellowBg to yellowText
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(badgeBg)
                                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = item.status.toTitleCase(),
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Medium,
                                                        color = badgeTextColor
                                                    )
                                                }

                                                ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = cardActions)
                                            }
                                        }
                                    },
                                    content = {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(light_grey)
                                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = item.category, fontSize = tokens.label, color = TextSecondary)
                                                }

                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(mutedText))
                                                Text(text = "Closing: ${item.closingDate}", fontSize = tokens.caption, color = headerGrey)
                                            }

                                            Spacer(Modifier.height(14.dp))
                                            HorizontalDivider(color = grey_border, thickness = 1.dp)
                                            Spacer(Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column {
                                                    Text(text = "Estimated Value", fontSize = tokens.label, color = mutedText)
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(text = item.estimatedValue, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(background_light_purple)
                                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = "Lead Stage", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Primary)
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }


        }
    }

    opportunityPendingDelete?.let { item ->
        DeleteModel(
            title = "Delete Opportunity",
            message = "Are you sure you want to delete deal \"${item.title}\"? This action cannot be undone.",
            onDismiss = { opportunityPendingDelete = null },
            onDelete = {
                viewModel.deleteOpportunity(item.id)
                opportunityPendingDelete = null
            }
        )
    }
    Box(
        Modifier.fillMaxWidth()
    ) {
        DynamicIslandSuccess(
            message = actionMessage,
            onDismiss = { actionMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { errorMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun OpportunityMetricsGrid() {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(label = "Total Pipeline", value = "₹24.8L", modifier = Modifier.weight(1f))
            MetricCard(label = "Active Deals", value = "42", modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(label = "Win Rate", value = "68%", modifier = Modifier.weight(1f))
            MetricCard(label = "Avg. Cycle", value = "14 Days", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey,
                maxLines = 1
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}