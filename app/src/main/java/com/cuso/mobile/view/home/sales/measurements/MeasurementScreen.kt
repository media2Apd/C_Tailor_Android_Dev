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
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.ui.theme.yellowBg
import com.cuso.mobile.ui.theme.yellowtext
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
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
                .background(Color.Transparent)
        ) {
            // ── Top Bar ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TitleBar("Measurements", onClose = onBack)

            }
            Column(
               Modifier.fillMaxWidth()

            ) {
                ScreenBreadcrumb(segments = listOf("Sales", "Measurements"), onClick = {onBreadCrumbClick()})

                // ── Filter Row ──────────────────────────────────────
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Measurements...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = {  }
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
                                Text("Retry", color = whiteBg)
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
                                        // item.pending is a String, so parse it to Double first (strip commas if any)
                                        val pendingAmount = item.pending.replace(",", "").toDoubleOrNull() ?: 0.0

                                        val isPaid = pendingAmount <= 0.0

                                        // Build badge text and colors dynamically based on the pending amount
                                        val badgeText = if (isPaid) {
                                            "₹${formatAmount(pendingAmount)} Paid"
                                        } else {
                                            "₹${formatAmount(pendingAmount)} Pending"
                                        }

                                        val badgeTextColor = if (isPaid) greentext else yellowtext
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
                                                DataCardField(text = "Updated • ${item.lastUpdated}", textColor = mutedText),
                                            ),
                                            actions = listOf(
                                                MenuAction("View", Icons.Default.Visibility) {},
                                            )
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

// Formats a number with comma separators, no decimal places (e.g. 2801.0 -> "2,801")
fun formatAmount(amount: Double): String {
    return "%,.0f".format(amount)
}