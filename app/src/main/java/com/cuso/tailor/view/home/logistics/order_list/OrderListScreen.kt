@file:Suppress("DEPRECATION")

package com.cuso.tailor.view.home.logistics.order_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar

@Composable
fun OrderListScreen(
    viewModel: LogisticsViewModel,
    onClose: () -> Unit,
    onViewOrderDetails: (PackageItemData) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val packages by viewModel.packagesList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Filter list based on orderCode or customerName
    val filteredList = remember(packages, searchQuery) {
        if (searchQuery.isBlank()) packages
        else {
            packages.filter { item ->
                (item.orderCode?.contains(searchQuery, ignoreCase = true) == true) ||
                        (item.customerName?.contains(searchQuery, ignoreCase = true) == true) ||
                        (item.packageNumber?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            TitleBar(title = "Order List", onClose = onClose)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent)
        ) {
            // --- Header Section: Title and Search/Filter Bar ---
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                placeholder = "Search SO number, customer...",
                height = tokens.fieldHeight,
                showFilterIcon = true,
                filterCount = 0, // Pass your active filter count here if needed
                onFilterClick = {
                    // Trigger your filter drawer/dialog here
                }
            )

            // --- Screen Content / LazyColumn List ---
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isLoading && packages.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && packages.isEmpty() -> {
                        Text(
                            text = errorMessage ?: "Failed to load orders",
                            fontSize = tokens.bodyMedium,
                            color = redText,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    filteredList.isEmpty() -> {
                        Text(
                            text = "No orders found",
                            fontSize = tokens.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        // LazyColumn without padding as requested
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredList,
                                key = { it._id }
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
}

/**
 * Single Order Card reusing the provided DataCard component
 */
@Composable
fun LogisticsOrderCard(
    item: PackageItemData,
    onViewDetails: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Format currency into Indian Rupees
    val formattedPrice = remember(item.value) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        formatter.format(item.value)
    }

    // Determine status badge color
    val statusText = item.statusBadge ?: item.shipment?.status ?: "Created"
    val (statusBg, statusTextColor) = when (statusText.lowercase()) {
        "delivered", "shipped" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "ready", "created" -> Pair(Color(0xFFE0F2FE), Color(0xFF0284C7))
        else -> Pair(Color(0xFFF3F4F6), Color(0xFF4B5563))
    }

    val warehouseName = item.shipment?.currentLocation ?: "Mumbai-N"
    val paymentTypeText = item.paymentType ?: "COD"
    val isPrepaid = paymentTypeText.equals("Prepaid", ignoreCase = true)
    val subtitleText = "${item.primaryGarmentName ?: "Garment"} • ${item.location ?: "Mumbai"}"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Reusing DataCard with custom header & inner content container
        DataCard(
            item = item,
            onClick = { onViewDetails() },
            headerContent = {
                // Card Top Row: Order Code & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.orderCode ?: item.packageNumber ?: "",
                        fontSize = tokens.bodySmall,
                        color = Color(0xFF6B7280),
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
                    // Customer Name and Formatted Price
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

                    // Garment Name and Location
                    Text(
                        text = subtitleText,
                        fontSize = tokens.bodySmall,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(12.dp))

                    // Inner Rounded Container: Priority, Warehouse, and Payment info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF9FAFB))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: Priority
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "PRIORITY",
                                    fontSize = tokens.label,
                                    color = Color(0xFF9CA3AF),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Normal",
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Column 2: Warehouse
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "WAREHOUSE",
                                    fontSize = tokens.label,
                                    color = Color(0xFF9CA3AF),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = warehouseName,
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Column 3: Payment Type
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "PAYMENT",
                                    fontSize = tokens.label,
                                    color = Color(0xFF9CA3AF),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = paymentTypeText,
                                    fontSize = tokens.bodySmall,
                                    color = if (isPrepaid) Color(0xFF10B981) else Color(0xFFD97706),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Bottom Row: Items Count and View Details Action Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Items: ${String.format(LocalLocale.current.platformLocale, "%02d", item.itemsCount)} Units",
                            fontSize = tokens.bodySmall,
                            color = Color(0xFF6B7280),
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
                                color = Color(0xFF4F46E5),
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View Details",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}