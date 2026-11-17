package com.cuso.tailor.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardStat
import com.cuso.tailor.view.composable.DataCardStatsRow
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.paywall.PermissionAction
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay

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

    val receivesPagingState by viewModel.receivesPagingState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        FabScaffold(
            modifier = Modifier.fillMaxSize(),
            fab = FabConfig(
                label = "Create Order",
                icon = Icons.Default.Add,
                requiredAction = PermissionAction.CREATE,
                alignment = Alignment.BottomEnd,
                endPadding = tokens.screenPadding,
                bottomPadding = tokens.screenPadding,
                draggable = false,
                onClick = onCreateOrderClick
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(
                    title = "Purchase Receive",
                    onClose = onClose
                )

                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Customers or PO...",
                    showFilterIcon = true,
                    onFilterClick = onFilterClick,
                    height = tokens.fieldHeight
                )

                HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 2.dp)

                when {
                    errorMessage != null && receivesPagingState.items.isEmpty() && !receivesPagingState.isInitialLoading -> {
                        AppErrorState(
                            title = "Failed to load orders",
                            message = errorMessage ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchAllReceives(search = searchQuery.trim().ifBlank { null }) }
                        )
                    }

                    !receivesPagingState.isInitialLoading && receivesPagingState.items.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No purchase receives found.", color = mutedText, fontSize = tokens.bodyMedium)
                        }
                    }

                    else -> {
                        PaginatedDataList(
                            state = receivesPagingState,
                            onLoadMore = { viewModel.loadMoreReceives() },
                            modifier = Modifier.fillMaxSize()
                        ) { receiveItem ->
                            val poId = receiveItem.poId?.id.orEmpty()
                            ReceiveOrderCard(
                                item = receiveItem,
                                tokens = tokens,
                                onClick = { onOrderClick(poId, receiveItem.id) }
                            )
                        }
                    }
                }
            }
        }
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

    val menuActions = remember(item.id) {
        listOf(
            MenuAction(
                label = "View Details",
                icon = Icons.Default.Visibility,
                tint = Primary,
                onClick = onClick
            )
        )
    }

    DataCard(
        item = item,
        onClick = { onClick() },
        headerContent = {
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

                StatusBadge(
                    text = item.status,
                    bgColor = if (isCompleted) greenBg else yellowBg,
                    textColor = if (isCompleted) darkGreenBg else yellowText
                )
            }
        },
        title = supplierName,
        actions = menuActions,
        showHeaderDivider = true,
        content = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = tokens.extraPadding * 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.25f))
                            .background(badgeGrey)
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = poNumber,
                            fontSize = tokens.label,
                            color = TextSecondary
                        )
                    }

                    Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                    Text(
                        text = "• Date: $formattedDate",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                DataCardStatsRow(
                    stats = listOf(
                        DataCardStat(
                            label = "Billing Status",
                            value = item.billingStatus,
                            valueColor = if (item.billingStatus == "Billed") darkGreenBg else orangeText
                        ),
                        DataCardStat(
                            label = "Total Quantity",
                            value = "$totalQty",
                            valueColor = TextPrimary
                        )
                    )
                )
            }
        }
    )
}