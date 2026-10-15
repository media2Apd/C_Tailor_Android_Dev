package com.cuso.tailor.view.home.inventory.inventory.packages

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.PackageItemDto
import com.cuso.tailor.model.inventory.PackageSummaryDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun PackagesScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onCreatePackageClick: () -> Unit = {},
    onPackageItemClick: (PackageItemDto) -> Unit = {},
    onEditPackageClick: (PackageItemDto) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    var activeStatus by remember { mutableStateOf<String?>(null) }
    val filterDrawerState = rememberFilterDrawerState()

    val packages by viewModel.packagesList.collectAsStateWithLifecycle()
    val summaryFromVm by viewModel.packageSummary.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingPackages.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMorePackages.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMorePackages.collectAsStateWithLifecycle()
    val errorMessage by viewModel.packagesError.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchPackages()
    }

    val reachedEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(reachedEnd, packages.size) {
        if (reachedEnd && packages.isNotEmpty() && canLoadMore) {
            viewModel.loadMorePackages(limit = 10)
        }
    }

    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("Ready for Shipment", "Ready for Shipment"),
                    FilterOption("Shipped", "Shipped"),
                    FilterOption("Delivered", "Delivered")
                ),
                isMultiSelect = false
            )
        )
    }

    FabScaffold(
        modifier = modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Create Package",
            icon = Icons.Default.Add,
            onClick = onCreatePackageClick
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            TitleBar(
                title = "Packages",
                onClose = onNavigateBack
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.fetchPackages(
                        page = 1,
                        search = it.ifBlank { null },
                        status = activeStatus
                    )
                },
                placeholder = "Search package, customer...",
                showFilterIcon = true,
                onFilterClick = { filterDrawerState.open() }
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading && packages.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && packages.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load packages",
                            message = errorMessage?.ifBlank { null }
                                ?: "Something went wrong while fetching packages.",
                            buttonText = "Retry",
                            onRetry = {
                                viewModel.fetchPackages(
                                    page = 1,
                                    search = searchQuery.ifBlank { null },
                                    status = activeStatus
                                )
                            }
                        )
                    }

                    else -> {
                        val summary = summaryFromVm ?: PackageSummaryDto()

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item(key = "metrics") {
                                PackageMetricsSection(summary = summary)
                            }
                            item {
                                Spacer(Modifier.padding(top = 10.dp))
                            }
                            if (packages.isEmpty()) {
                                item(key = "empty") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = tokens.screenPadding * 2),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No packages found",
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = mutedText
                                        )
                                    }
                                }
                            } else {
                                items(
                                    items = packages,
                                    key = { it.id }
                                ) { pkg ->
                                    PackageCardItem(
                                        item = pkg,
                                        onCardClick = onPackageItemClick,
                                        onEditClick = onEditPackageClick
                                    )
                                }

                                if (isLoadingMore) {
                                    item(key = "loading_more") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = tokens.extraPadding),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(tokens.iconSize),
                                                strokeWidth = 2.dp,
                                                color = Primary
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

        FilterDrawer(
            state = filterDrawerState,
            title = "Filters",
            sections = filterSections,
            onApply = { updatedSections ->
                val chosenStatus = updatedSections
                    .firstOrNull { it.title == "Status" }
                    ?.options
                    ?.firstOrNull { it.isSelected }
                    ?.label
                activeStatus = chosenStatus
                viewModel.fetchPackages(
                    page = 1,
                    search = searchQuery.ifBlank { null },
                    status = chosenStatus
                )
            },
            onClearAll = {
                activeStatus = null
                viewModel.fetchPackages(
                    page = 1,
                    search = searchQuery.ifBlank { null },
                    status = null
                )
            }
        )
    }
}

@Composable
private fun PackageMetricsSection(
    summary: PackageSummaryDto
) {
    val tokens = LocalAppTokens.current
    val readyCount = if (summary.readyForShipment > 0) summary.readyForShipment else summary.notShipped

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            PackageMetricCard(
                title = "Total Packages",
                value = summary.total.toString(),
                modifier = Modifier.weight(1f)
            )
            PackageMetricCard(
                title = "Ready for Shipment",
                value = readyCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            PackageMetricCard(
                title = "Shipped",
                value = summary.shipped.toString(),
                modifier = Modifier.weight(1f)
            )
            PackageMetricCard(
                title = "Delivered",
                value = summary.delivered.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PackageMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding * 0.9f),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)
        ) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Normal,
                color = headerGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = blackTitle
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun PackageCardItem(
    item: PackageItemDto,
    onCardClick: (PackageItemDto) -> Unit,
    onEditClick: (PackageItemDto) -> Unit
) {
    val tokens = LocalAppTokens.current

    val customerName = item.customerDisplay
    val packageCode = item.packageNumber ?: "PCK-00"
    val salesOrderCode = item.salesOrderCode
    val warehouseName = item.warehouseName

    val totalItems = item.items.size
    val totalQty = item.totalQty

    val isDelivered = item.status.equals("Delivered", ignoreCase = true)
    val isShipped = item.status.equals("Shipped", ignoreCase = true)

    val (statusBg, statusTextColor) = when {
        isDelivered -> Pair(greenBg, greentext)
        isShipped -> Pair(light_blue, Primary)
        else -> Pair(orangeBg, orangeText)
    }

    val formattedDate = remember(item.createdAt) {
        formatIsoDate(item.createdAt)
    }

    val menuActions = remember(item.id, item.status) {
        listOf(
            MenuAction(
                "View Details",
                Icons.Outlined.Visibility,
                textColor = TextPrimary,
                tint = TextSecondary,
                onClick = { onCardClick(item) }
            ),
            MenuAction(
                "Edit",
                Icons.Outlined.Edit,
                textColor = TextPrimary,
                tint = TextSecondary,
                onClick = { onEditClick(item) }
            ),
            MenuAction(
                "Delete",
                Icons.Outlined.Delete,
                textColor = redText,
                tint = redText,
                onClick = {}
            )
        )
    }

    DataCard(
        item = item,
        onClick = onCardClick,
        showHeaderDivider = false,
        showActionsInHeader = false,
        actions = emptyList(),
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customerName,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = blackTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = packageCode,
                            fontSize = tokens.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StatusBadge(
                            text = item.status ?: "Ready for Shipment",
                            bgColor = statusBg,
                            textColor = statusTextColor,
                            cornerRadius = tokens.cardCornerRadius * 0.8f
                        )

                        ActionDropdownMenu(
                            actions = menuActions,
                            icon = Icons.Default.MoreVert
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                        .background(Primary_background)
                        .padding(
                            horizontal = tokens.extraPadding,
                            vertical = tokens.extraPadding * 0.9f
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Sales Order",
                                fontSize = tokens.label,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = salesOrderCode,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Location",
                                fontSize = tokens.label,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = warehouseName,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Item/Quantity",
                                fontSize = tokens.label,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalItems items / ${totalQty.toInt()} Qty",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Date : ",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                    Text(
                        text = formattedDate.ifBlank { "-" },
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }
    )
}

private fun formatIsoDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val parsed = parser.parse(isoDate)
        if (parsed != null) formatter.format(parsed) else isoDate
    } catch (_: Exception) {
        isoDate
    }
}