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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.viewmodel.MeasurementsUiState
import com.cuso.mobile.viewmodel.MeasurementsViewModel

// ─────────────────────────────────────────────────────────────
// Measurements Screen
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")

@Composable
fun MeasurementsScreen(
    navController: NavController,
    viewModel: MeasurementsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCreateOrder: () -> Unit = {},
    onBreadCrumbClick: () -> Unit ={}

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("all") }
    var page by remember { mutableIntStateOf(1) }
    var itemsPerPage by remember { mutableIntStateOf(10) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showItemsPerPageDropdown by remember { mutableStateOf(false) }

    val typeOptions = listOf(
        "all" to "All Customers",
        "individual" to "Individual",
        "corporate" to "Corporate"
    )

    FabScaffold(
        fab = FabConfig(
            label = "New Order",
            icon = Icons.Default.Add,
            onClick = onCreateOrder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            // ── Top Bar ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        "Measurements",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onBack() },
                        tint = Color(0xFF111827)
                    )
                }
            }
            Column(
               Modifier.fillMaxWidth()
                   .background(Primary_background)

            ) {
                ScreenBreadcrumb(segments = listOf("Sales", "Measurements"), onClick = {onBreadCrumbClick()})

                // ── Filter Row ──────────────────────────────────────
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    placeholder = "Search Measurements...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { /* TODO: open filter drawer */ }
                )
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // ── Content ──────────────────────────────────────────
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
                                state.message,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.loadMeasurements() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9))
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }

                is MeasurementsUiState.Success -> {
                    val items = state.items

                    val filteredList = if (searchQuery.isNotBlank()) {
                        items.filter {
                            it.customerName.contains(searchQuery, ignoreCase = true) ||
                                    it.contact.contains(searchQuery, ignoreCase = true)
                        }
                    } else {
                        items
                    }

                    val startIdx = (page - 1) * itemsPerPage
                    val endIdx = minOf(startIdx + itemsPerPage, filteredList.size)
                    val pagedList = if (filteredList.isNotEmpty()) {
                        filteredList.subList(startIdx, endIdx)
                    } else {
                        emptyList()
                    }
                    val totalFilteredCount = filteredList.size
                    val totalPagesFilteredCount = if (totalFilteredCount > 0) (totalFilteredCount + itemsPerPage - 1) / itemsPerPage else 1

                    if (pagedList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
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
                                        onClick = {
                                            searchQuery = ""
                                            page = 1
                                        },
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
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    pagedList.forEach { item ->
                                        val (badgeText, badgeColor) = if (item.type == "Corporate")
                                            "Corporate" to Color(0xFFD97706)
                                        else
                                            "Individual" to Color(0xFF3B3BF9)

                                        DataCard(
                                            item = item,
                                            eyebrowText = "Order ID : Order id not found",
                                            topBadgeText = item.pending,
                                            topBadgeTextColor = badgeColor,
                                            topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                            title = item.customerName,
                                            footerFields = listOf(
                                                DataCardField(text = item.contact),
                                                DataCardField(text = item.garments),
                                                DataCardField(text = "Updated • ${item.lastUpdated}"),

                                            ),
                                            actions = listOf(
                                                MenuAction("View", Icons.Default.Visibility) {},
                                                MenuAction("Edit", Icons.Default.Edit) {},
                                                MenuAction("Delete", Icons.Default.Delete, tint = Color(0xFFF44336), textColor = Color(0xFFF44336)) {}
                                            )
                                        )
                                    }
                                }
                            }

                            // ── Pagination ──
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val from = (page - 1) * itemsPerPage + 1
                                    val to = minOf(page * itemsPerPage, totalFilteredCount)
                                    Text("Showing $from - $to of $totalFilteredCount", fontSize = 13.sp, color = Color(0xFF6B7280))

                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                                                .clickable { showItemsPerPageDropdown = true }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Settings, null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                                            Text("$itemsPerPage per page", fontSize = 13.sp, color = Color(0xFF374151))
                                        }
                                        DropdownMenu(
                                            expanded = showItemsPerPageDropdown,
                                            onDismissRequest = { showItemsPerPageDropdown = false },
                                            containerColor = Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            listOf(10, 25, 50, 100).forEach { count ->
                                                DropdownMenuItem(
                                                    text = { Text("$count per page", color = Color(0xFF111827)) },
                                                    onClick = {
                                                        itemsPerPage = count
                                                        page = 1
                                                        showItemsPerPageDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (page > 1) page-- },
                                        enabled = page > 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronLeft,
                                            contentDescription = "Previous",
                                            tint = if (page > 1) Color(0xFF374151) else Color(0xFFD1D5DB)
                                        )
                                    }
                                    Text("$page - $totalPagesFilteredCount", fontSize = 13.sp, color = Color(0xFF6B7280))
                                    IconButton(
                                        onClick = { if (page < totalPagesFilteredCount) page++ },
                                        enabled = page < totalPagesFilteredCount,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Next",
                                            tint = if (page < totalPagesFilteredCount) Color(0xFF374151) else Color(0xFFD1D5DB)
                                        )
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