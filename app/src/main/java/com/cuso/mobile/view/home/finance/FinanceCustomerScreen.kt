@file:Suppress("UNUSED_PARAMETER", "UNUSED", "RedundantSuppression", "unused")

package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.sales.CustomerItemV2
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

private val CustPrimary = Color(0xFF3B3BF9)
private val CustPrimarySoft = Color(0xFFEEEEFE)
private val CustTextDark = Color(0xFF111827)
private val CustmutedText = Color(0xFF9CA3AF)
private val CustGreen = Color(0xFF22C55E)
private val CustRed = Color(0xFFEF4444)
private val CustYellow = Color(0xFFF59E0B)
private val CustBgLight = Color(0xFFF5F5F7)

// ─────────────────────────────────────────────────────────────
// FinanceCustomerScreen — Customer list with Infinite Scroll
// ─────────────────────────────────────────────────────────────
@Composable
fun FinanceCustomerScreen(
    onClose: () -> Unit,
    onCustomerClick: (String) -> Unit,
    onCustomerEdit: (String) -> Unit = onCustomerClick,
    onBreadCrumbClick: () -> Unit = {}
) {
    val viewModel: FinanceViewModel = hiltViewModel()

    val customerListResponse by viewModel.financeCustomerList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingFinanceCustomers.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreFinanceCustomers.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreFinanceCustomers.collectAsStateWithLifecycle()
    val error by viewModel.financeCustomerError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val listState = rememberLazyListState()

    // Fetch initial list
    LaunchedEffect(Unit) {
        viewModel.fetchCustomerForFinance()
    }

    // Infinite scroll detection: triggers loadMore when 3 items away from the bottom
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 3
        }
            .distinctUntilChanged()
            .collect { nearBottom ->
                if (nearBottom && canLoadMore && !isLoadingMore && !isLoading) {
                    viewModel.loadMoreCustomerForFinance()
                }
            }
    }

    val allCustomers = customerListResponse?.data ?: emptyList()

    val filteredCustomers = allCustomers.filter { customer ->
        val matchesSearch = searchQuery.isBlank() ||
                customer.name.contains(searchQuery, ignoreCase = true) ||
                customer.mobile.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Active" -> customer.status.equals("Active", ignoreCase = true)
            "Inactive" -> customer.status.equals("Inactive", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // --- PERSISTENT HEADER SECTION ---
        Column(modifier = Modifier.fillMaxWidth()) {
            TitleBar("All Customers", onClose = onClose)

            ScreenBreadcrumb(
                segments = listOf("Finance", "Customer"),
                onClick = { onBreadCrumbClick() }
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Customers...",
                accentColor = BluePrimary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = { /* Handle Filter Click */ }
            )

            HorizontalDivider(color = Color(0xFFF0F0F0))
        }

        // --- DYNAMIC CONTENT SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                isLoading -> {
                    ListSkeleton()
                }

                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error ?: "Something went wrong", color = CustRed, fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.fetchCustomerForFinance() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                filteredCustomers.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PersonOff,
                                contentDescription = null,
                                tint = CustmutedText,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "No matching customers found"
                                else "No customers yet",
                                fontSize = 14.sp,
                                color = CustmutedText
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredCustomers, key = { it._id }) { customer ->
                            CustomerCardItem(
                                customer = customer,
                                onClick = { onCustomerClick(customer._id) },
                                onEdit = { onCustomerEdit(customer._id) }
                            )
                        }

                        // Bottom loading indicator for pagination
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

@Composable
private fun CustomerCardItem(
    customer: CustomerItemV2,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    val (badgeText, badgeColor) = statusColorsOfCustomer(customer.status)

    val typeAndAddress = "${customer.type.replaceFirstChar { it.uppercase() }} • " +
            (customer.address?.addressLine?.takeIf { it.isNotBlank() } ?: "N/A")

    DataCard(
        item = customer,
        topBadgeText = badgeText,
        topBadgeTextColor = badgeColor,
        topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
        title = customer.name,
        footerFields = listOf(
            DataCardField(
                icon = Icons.Default.Phone,
                text = customer.mobile
            ),
            DataCardField(
                text = typeAndAddress
            ),
            DataCardField(
                text = "Outstanding: ₹${formatIndianNumber(customer.outstanding ?: 0.0)}"
            )
        ),
        actions = listOf(
            MenuAction("View", Icons.Default.Visibility) { onClick() },
            MenuAction("Edit", Icons.Default.Edit) { onEdit() }
        ),
        onClick = { onClick() }
    )
}

private fun statusColorsOfCustomer(status: String?): Pair<String, Color> = when (status?.lowercase()) {
    "active" -> "Active" to greentext
    "blocked" -> "Blocked" to redText
    "inactive" -> "Inactive" to Color(0xFF6B7280)
    else -> "N/A" to Color(0xFF9CA3AF)
}

fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("T")[0].split("-")
        if (parts.size == 3) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            dateString
        }
    } catch (_: Exception) {
        dateString
    }
}