package com.cuso.tailor.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.PurchaseReceiveItem
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.iconMuted
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.ThreeDotLoading
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
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

    val receives by viewModel.allReceives.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingReceives.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreReceives.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreReceives.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

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

    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Primary_background,
            topBar = {
                TitleBar(
                    title = "Purchase Receive",
                    onClose = onClose
                )
            },
            floatingActionButton = {
                Button(
                    onClick = onCreateOrderClick,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                ) {
                    Text(
                        text = "Create Order",
                        color = whiteBg,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Order",
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
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

                when {
                    isLoading && receives.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && receives.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load orders",
                            message = errorMessage ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchAllReceives(search = searchQuery.trim().ifBlank { null }) }
                        )
                    }

                    receives.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No purchase receives found.", color = mutedText, fontSize = tokens.bodyMedium)
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = tokens.buttonHeight * 2f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            items(
                                items = receives,
                                key = { it.id.ifBlank { it.hashCode().toString() } }
                            ) { receiveItem ->
                                val poId = receiveItem.poId?.id.orEmpty()
                                ReceiveOrderCard(
                                    item = receiveItem,
                                    tokens = tokens,
                                    onClick = { onOrderClick(poId, receiveItem.id) }
                                )
                            }

                            if (isLoadingMore) {
                                item(key = "pagination_threedot_loader") {
                                    ThreeDotLoading(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = tokens.screenPadding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMessage?.takeIf { receives.isNotEmpty() },
            onDismiss = { viewModel.clearErrors() }
        )
    }
}

@Composable
fun ReceiveOrderCard(
    item: PurchaseReceiveItem,
    tokens: AppDesignTokens,
    onClick: () -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    val supplierName = item.poId?.supplierId?.name ?: "Unknown Supplier"
    val poNumber = item.poId?.poNumber ?: "No PO"
    val totalQty = item.items.sumOf { it.qtyReceived }
    val formattedDate = item.receiveDate.take(10)
    val isCompleted = item.status.equals("Completed", ignoreCase = true)

    Surface(
        color = whiteBg,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )

                Spacer(Modifier.width(tokens.extraPadding))

                Text(
                    text = item.receiveNumber,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                    color = if (isCompleted) greenBg else yellowBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(tokens.iconSize * 0.35f)
                                .background(if (isCompleted) darkGreenBg else yellowText, CircleShape)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.status,
                            color = if (isCompleted) darkGreenBg else yellowText,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = iconMuted,
                    modifier = Modifier.size(tokens.iconSize * 1.1f)
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            Text(
                text = supplierName,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(Modifier.height(tokens.extraPadding * 0.4f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.25f),
                    color = badgeGrey,
                    border = BorderStroke(1.dp, grey_border)
                ) {
                    Text(
                        text = poNumber,
                        fontSize = tokens.label,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                Text(
                    text = "• Date: $formattedDate",
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Billing Status", fontSize = tokens.caption, color = mutedText)
                    Text(
                        text = item.billingStatus,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (item.billingStatus == "Billed") darkGreenBg else orangeText
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Quantity", fontSize = tokens.caption, color = mutedText)
                    Text(
                        text = "$totalQty",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}