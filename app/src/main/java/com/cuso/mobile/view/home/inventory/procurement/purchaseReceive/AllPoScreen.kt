package com.cuso.mobile.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.PurchaseReceiveItem
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.AppCheckbox
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.ThreeDotLoading
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AllOrdersScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCreateOrderClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onOrderClick: (poId: String, receiveId: String) -> Unit = { _, _ -> }
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    // Observe receive list and pagination states from ViewModel
    val receives by viewModel.allReceives.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingReceives.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreReceives.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreReceives.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Scroll state tracker for LazyColumn
    val listState = rememberLazyListState()

    // Consolidated initial fetch and debounced search (avoids duplicate call at startup)
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (!isInitialized) {
            isInitialized = true
            viewModel.fetchAllReceives()
        } else {
            delay(400)
            viewModel.fetchAllReceives(search = searchQuery.trim().ifBlank { null })
        }
    }

    // Scroll listener: triggers next page fetch only when crossing the bottom threshold
    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            // Trigger when reaching within 2 items of the list end
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom &&
                    canLoadMore &&
                    !isLoadingMore &&
                    !isLoading &&
                    searchQuery.isBlank()
                ) {
                    viewModel.loadMoreReceives()
                }
            }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            TitleBar(
                title = "All orders",
                onClose = onClose
            )
        },
        floatingActionButton = {
            Button(
                onClick = onCreateOrderClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Create Order",
                    color = whiteBg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Order",
                    tint = whiteBg,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Customers or PO...",
                showFilterIcon = true,
                onFilterClick = onFilterClick,
                height = tokens.fieldHeight
            )

            if (isLoading && receives.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (receives.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No purchase receives found.", color = mutedText, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(
                        items = receives,
                        key = { it.id.ifBlank { it.hashCode().toString() } }
                    ) { receiveItem ->
                        val poId = receiveItem.poId?.id.orEmpty()
                        ReceiveOrderCard(
                            item = receiveItem,
                            onClick = { onOrderClick(poId, receiveItem.id) }
                        )
                    }

                    // Three-dot loader displayed only while a next page request is actively in-flight
                    if (isLoadingMore) {
                        item(key = "pagination_threedot_loader") {
                            ThreeDotLoading(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { viewModel.clearErrors() }
        )
    }
}

@Composable
fun ReceiveOrderCard(
    item: PurchaseReceiveItem,
    onClick: () -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    val supplierName = item.poId?.supplierId?.name ?: "Unknown Supplier"
    val poNumber = item.poId?.poNumber ?: "No PO"
    val totalQty = item.items.sumOf { it.qtyReceived }
    val formattedDate = item.receiveDate.take(10)

    Surface(
        color = whiteBg,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = item.receiveNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (item.status.equals("Completed", ignoreCase = true)) greenBg else Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (item.status.equals("Completed", ignoreCase = true)) darkGreenBg else yellowText, CircleShape)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.status,
                            color = if (item.status.equals("Completed", ignoreCase = true)) darkGreenBg else yellowText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = iconMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = supplierName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeGrey,
                    border = androidx.compose.foundation.BorderStroke(1.dp, grey_border)
                ) {
                    Text(
                        text = poNumber,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "• Date: $formattedDate",
                    fontSize = 11.sp,
                    color = mutedText
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Billing Status", fontSize = 11.sp, color = mutedText)
                    Text(
                        text = item.billingStatus,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.billingStatus == "Billed") darkGreenBg else Color(0xFFE08A2C)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Quantity", fontSize = 11.sp, color = mutedText)
                    Text(
                        text = "$totalQty",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}