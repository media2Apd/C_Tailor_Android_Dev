@file:Suppress("DEPRECATION")

package com.cuso.tailor.view.home.logistics.order_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.PackageItemData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.viewmodel.LogisticsViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar

@Composable
fun OrderListScreen(
    viewModel: LogisticsViewModel,
    onClose: () -> Unit,
    onViewOrderDetails: (PackageItemData) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val packagesPagingState by viewModel.packagesPagingState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(title = "Order List", onClose = onClose)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Primary_background)
        ) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                placeholder = "Search SO number, customer...",
                height = tokens.fieldHeight,
                showFilterIcon = true,
                filterCount = 0,
                onFilterClick = {}
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    errorMessage != null && packagesPagingState.items.isEmpty() && !packagesPagingState.isInitialLoading -> {
                        Text(
                            text = errorMessage ?: "Failed to load orders",
                            fontSize = tokens.bodyMedium,
                            color = redText,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    !packagesPagingState.isInitialLoading && packagesPagingState.items.isEmpty() -> {
                        Text(
                            text = "No orders found",
                            fontSize = tokens.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        PaginatedDataList(
                            state = packagesPagingState,
                            onLoadMore = { viewModel.loadMorePackages() },
                            modifier = Modifier.fillMaxSize()
                        ) { item ->
                            LogisticsOrderCard(
                                item = item,
                                onViewDetails = { onViewOrderDetails(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogisticsOrderCard(
    item: PackageItemData,
    onViewDetails: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val formattedPrice = remember(item.value) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        formatter.format(item.value)
    }

    val statusText = item.statusBadge ?: item.shipment?.status ?: ""
    val (statusBg, statusTextColor) = when (statusText.lowercase()) {
        "delivered", "shipped" -> Pair(greenBg, greentext)
        "ready", "created" -> Pair(light_blue, blueText)
        else -> Pair(light_grey, TextSecondary)
    }

    val warehouseName = item.shipment?.currentLocation ?: ""
    val paymentTypeText = item.paymentType ?: ""
    val isPrepaid = paymentTypeText.equals("Prepaid", ignoreCase = true)
    val subtitleText = "${item.primaryGarmentName ?: ""} • ${item.location ?: ""}"

    Surface(modifier = Modifier.fillMaxWidth()) {
        DataCard(
            item = item,
            onClick = { onViewDetails() },
            headerContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.orderCode ?: item.packageNumber ?: "",
                        fontSize = tokens.bodySmall,
                        color = headerGrey,
                        fontWeight = FontWeight.Medium
                    )

                    StatusBadge(
                        text = statusText,
                        bgColor = statusBg,
                        textColor = statusTextColor,
                        cornerRadius = 14.dp,
                        showDot = false
                    )
                }
            },
            content = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.customerName ?: "Unknown Customer",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = formattedPrice,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = subtitleText,
                        fontSize = tokens.bodySmall,
                        color = headerGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary_background)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = "PRIORITY", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                                Text(text = "Normal", fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = "WAREHOUSE", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                                Text(text = warehouseName, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = "PAYMENT", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                                Text(
                                    text = paymentTypeText,
                                    fontSize = tokens.bodySmall,
                                    color = if (isPrepaid) complete_button_bg else yellowText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Items: ${String.format(LocalLocale.current.platformLocale, "%02d", item.itemsCount)} Units",
                            fontSize = tokens.bodySmall,
                            color = headerGrey,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.clickable { onViewDetails() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "View Details",
                                fontSize = tokens.bodySmall,
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View Details",
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}
