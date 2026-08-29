@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead"
)
package com.cuso.mobile.view.home.sales.measurements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.ui.theme.yellowBg
import com.cuso.mobile.ui.theme.yellowText
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.MeasurementsUiState
import com.cuso.mobile.viewmodel.MeasurementsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

// -------------------------------------------------------------
// Measurements Screen (Infinite Scrolling Implementation)
// -------------------------------------------------------------
@Suppress("UNUSED_PARAMETER")
@Composable
fun MeasurementsScreen(
    navController: NavController,
    viewModel: MeasurementsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCreateOrder: () -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMore.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Trigger pagination when scrolling near the end of the list
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
            label = "New Order",
            icon = Icons.Default.Add,
            onClick = onCreateOrder,
            bottomPadding = 50.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TitleBar("Measurements", onClose = onBack)
            }

            Column(Modifier.fillMaxWidth()) {

                // Search Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Measurements...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
                )
            }

            HorizontalDivider(color = title_border)

            // Main Content Area
            when (val state = uiState) {
                is MeasurementsUiState.Loading -> {
                    ListSkeleton()
                }

                is MeasurementsUiState.Error -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.loadMeasurements() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9))
                            ) {
                                Text("Retry", color = whiteBg)
                            }
                        }
                    }
                }

                is MeasurementsUiState.Success -> {
                    val items = state.items

                    val filteredList = remember(items, searchQuery) {
                        if (searchQuery.isNotBlank()) {
                            items.filter {
                                it.customerName.contains(searchQuery, ignoreCase = true) ||
                                        it.contact.contains(searchQuery, ignoreCase = true)
                            }
                        } else {
                            items
                        }
                    }

                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
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
                                        .background(Color(0xFFF3F4F6), CircleShape),
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
                                    text = if (searchQuery.isNotBlank()) {
                                        "No customers match your search criteria"
                                    } else {
                                        "No measurement records available"
                                    },
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )

                                if (searchQuery.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = { searchQuery = "" },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF3B3BF9)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF3B3BF9)
                                        ),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Clear Search", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // LazyColumn for Infinite Scroll
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(filteredList, key = { it.id }) { item ->
                                val pendingAmount = item.pending.replace(",", "").toDoubleOrNull() ?: 0.0
                                val isPaid = pendingAmount <= 0.0

                                val badgeText = if (isPaid) {
                                    "₹${formatAmount(pendingAmount)} Paid"
                                } else {
                                    "₹${formatAmount(pendingAmount)} Pending"
                                }

                                val badgeTextColor = if (isPaid) greentext else yellowText
                                val badgeBgColor = if (isPaid) greenBg else yellowBg

                                DataCard(
                                    item = item,
                                    eyebrowText = "Order ID : Order id not found",
                                    topBadgeText = badgeText,
                                    topBadgeTextColor = badgeTextColor,
                                    topBadgeBgColor = badgeBgColor,
                                    topBadgeShowDot = false,
                                    showActionsInHeader = true,
                                    title = item.customerName,
                                    footerFields = listOf(
                                        DataCardField(text = item.contact, textColor = mutedText),
                                        DataCardField(text = item.garments, textColor = mutedText),
                                        DataCardField(text = "Updated • ${item.lastUpdated}", textColor = mutedText)
                                    ),
                                    actions = listOf(
                                        MenuAction("View", Icons.Default.Visibility) {}
                                    )
                                )
                            }

                            // Bottom loading indicator when fetching more items
                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
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
}

// Formats amount with comma separators and no decimal places
fun formatAmount(amount: Double): String {
    return "%,.0f".format(amount)
}