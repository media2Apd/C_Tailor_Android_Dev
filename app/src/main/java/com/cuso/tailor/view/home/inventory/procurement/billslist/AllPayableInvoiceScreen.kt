package com.cuso.tailor.view.home.inventory.procurement.billslist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
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
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.ProcurementBillItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Locale

data class PayableInvoiceItemModel(
    val id: String,
    val billNo: String = "-",
    val vendorName: String = "-",
    val status: String = "Draft",
    val date: String = "-",
    val dueDate: String = "-",
    val balanceDueDate: String = "₹0",
    val totalAmount: String = "₹0",
    val raw: ProcurementBillItem? = null
)

@Composable
fun AllBillListScreen(
    onClose: () -> Unit,
    onCreateOrder: () -> Unit = {},
    onInvoiceClick: (PayableInvoiceItemModel) -> Unit = {},
    onOptionsClick: (PayableInvoiceItemModel) -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val listState = rememberLazyListState()

    val billsList by viewModel.billsList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingBills.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreBills.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreBills.collectAsStateWithLifecycle()
    val error by viewModel.billsError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.fetchAllBills()
    }

    // Infinite scroll detection
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
            .distinctUntilChanged()
            .collect { nearBottom ->
                if (nearBottom && canLoadMore && !isLoadingMore && !isLoading) {
                    viewModel.loadMoreBills()
                }
            }
    }

    val displayItems = remember(billsList) {
        billsList.map { it.toUiModel() }
    }

    val filteredList = remember(displayItems, searchQuery) {
        if (searchQuery.isBlank()) {
            displayItems
        } else {
            displayItems.filter {
                it.billNo.contains(searchQuery, ignoreCase = true) ||
                        it.vendorName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                ) {
                    TitleBar("All Bill List", onClose)
                    HorizontalDivider(color = title_border)
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onCreateOrder,
                    containerColor = Primary,
                    contentColor = whiteBg,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    modifier = Modifier.height(tokens.buttonHeight)
                ) {
                    Text(
                        text = "Create Order",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create",
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search and Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.onBillsSearchQueryChanged(it)
                    },
                    placeholder = "Search Bill No or Vendor...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 2.dp)

                when {
                    isLoading -> {
                        ListSkeleton()
                    }

                    error != null -> {
                        AppErrorState(
                            title = "Failed to load bills",
                            message = error ?: "Unexpected network error",
                            onRetry = { viewModel.fetchAllBills() }
                        )
                    }

                    filteredList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = mutedText,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                                Text(
                                    text = "No bills found",
                                    fontSize = tokens.bodyMedium,
                                    color = mutedText
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            itemsIndexed(filteredList, key = { _, item -> item.id }) { _, item ->
                                val isChecked = selectedItemIds.contains(item.id)
                                PayableInvoiceCardItem(
                                    item = item,
                                    isChecked = isChecked,
                                    tokens = tokens,
                                    onCheckedChange = { checked ->
                                        selectedItemIds = if (checked) {
                                            selectedItemIds + item.id
                                        } else {
                                            selectedItemIds - item.id
                                        }
                                    },
                                    onClick = { onInvoiceClick(item) },
                                    onOptionsClick = { onOptionsClick(item) }
                                )
                            }

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

@Composable
private fun PayableInvoiceCardItem(
    item: PayableInvoiceItemModel,
    isChecked: Boolean,
    tokens: AppDesignTokens,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val (statusBg, statusTextColor) = resolveStatusColors(item.status)

    DataCard(
        item = item,
        onClick = { onClick() },
        headerContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                AppCheckbox(
//                    checked = isChecked,
//                    onCheckedChange = onCheckedChange
//                )
//                Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                        .background(activity_purple_bg)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 3.dp)
                ) {
                    Text(
                        text = "BILL NO",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Bold,
                        color = activity_purple
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                Text(
                    text = item.billNo,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Spacer(Modifier.weight(1f))

                StatusBadge(
                    text = item.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                    bgColor = statusBg,
                    textColor = statusTextColor,
                    dotColor = statusTextColor,
                    cornerRadius = tokens.cardCornerRadius * 2f
                )
            }
        },
        title = item.vendorName,
        titleColor = title_color,
        titleFontWeight = FontWeight.SemiBold,
        actions = listOf(
            MenuAction(
                label = "View Details",
                icon = Icons.Default.Visibility,
                onClick = onClick
            )
        ),
        footerFields = listOf(
            DataCardField(
                label = "Date",
                text = item.date,
                asColumn = true,
                labelColor = close_color,
                textColor = title_color,
                valueFontWeight = FontWeight.Medium
            ),
            DataCardField(
                label = "Due Date",
                text = item.dueDate,
                asColumn = true,
                labelColor = close_color,
                textColor = title_color,
                valueFontWeight = FontWeight.Medium
            ),
            DataCardField(
                label = "Balance Due",
                text = item.balanceDueDate,
                asColumn = true,
                labelColor = close_color,
                textColor = title_color,
                valueFontWeight = FontWeight.Medium
            ),
            DataCardField(
                label = "Total Amount",
                text = item.totalAmount,
                asRow = true,
                labelColor = close_color,
                textColor = Primary,
                valueFontWeight = FontWeight.Bold
            )
        ),
        showDivider = false
    )
    Spacer(Modifier.height(10.dp))
}

private fun ProcurementBillItem.toUiModel(): PayableInvoiceItemModel {
    val vendor = supplierSnapshot?.name?.ifBlank { null }
        ?: supplierId?.name?.ifBlank { null }
        ?: "-"

    return PayableInvoiceItemModel(
        id = id,
        billNo = billNumber.ifBlank { "-" },
        vendorName = vendor,
        status = status.ifBlank { "Draft" },
        date = formatIsoDate(billDate),
        dueDate = formatIsoDate(dueDate),
        balanceDueDate = "₹${formatIndianNumber(balanceDue)}",
        totalAmount = "₹${formatIndianNumber(grandTotal)}",
        raw = this
    )
}

private fun formatIsoDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "-"
    return try {
        val datePart = isoDate.substringBefore("T")
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val month = when (parts[1]) {
                "01" -> "Jan"
                "02" -> "Feb"
                "03" -> "Mar"
                "04" -> "Apr"
                "05" -> "May"
                "06" -> "Jun"
                "07" -> "Jul"
                "08" -> "Aug"
                "09" -> "Sept"
                "10" -> "Oct"
                "11" -> "Nov"
                "12" -> "Dec"
                else -> parts[1]
            }
            "${parts[2]} $month ${parts[0]}"
        } else {
            datePart
        }
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun resolveStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "paid", "completed", "approved" -> greenBg to darkGreenBg
        "sent", "pending", "open" -> activity_purple_bg to Primary
        "void", "cancelled", "rejected" -> redBg to redText
        else -> light_grey to TextSecondary
    }
}