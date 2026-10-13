@file:Suppress("UNUSED_PARAMETER")

package com.cuso.tailor.view.home.inventory.procurement.returns

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Visibility
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
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.InventoryReturnItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun AllReturnsScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCreateReturnClick: () -> Unit = {},
    onReturnItemClick: (InventoryReturnItem) -> Unit = {},
    onEditRma: (InventoryReturnItem) -> Unit = {},
    onPdfExport: (InventoryReturnItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val returns by viewModel.returnsList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMore.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchReturns(page = 1)
    }

    FabScaffold(
        fab = FabConfig(
            label = "Create Return",
            icon = Icons.Default.Add,
            onClick = onCreateReturnClick
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Header Bar
            TitleBar(
                title = "Purchase Returns",
                onClose = onClose
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            // Search Filter
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.fetchReturns(page = 1, search = it.ifBlank { null })
                },
                placeholder = "Search RMA number, supplier..."
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            // Content Section
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading && returns.isEmpty()) {
                    ListSkeleton()
                } else if (returns.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No purchase returns found",
                            fontSize = tokens.bodyMedium,
                            color = mutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = tokens.screenPadding * 4)
                    ) {
                        items(returns, key = { it.id }) { item ->
                            ReturnCardItem(
                                item = item,
                                onClick = { onReturnItemClick(item) }
                            )
                        }

                        // Infinite scroll loader
                        if (canLoadMore) {
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMoreReturns()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(tokens.screenPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoadingMore) {
                                        ThreeDotLoading()
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

/**
 * Return Card Item matching the screenshot layout.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun ReturnCardItem(
    item: InventoryReturnItem,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Extract supplier initials for avatar
    val supplierName = item.supplier?.name?.trim()?.ifBlank { "Supplier" } ?: "Supplier"
    val initials = remember(supplierName) {
        val parts = supplierName.split("\\s+".toRegex()).filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
            parts.isNotEmpty() -> parts[0].take(1).uppercase()
            else -> "S"
        }
    }

    // Format Date (DD-MM-YYYY)
    val formattedDate = remember(item.returnDate) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val parsedDate = parser.parse(item.returnDate)
            if (parsedDate != null) {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(parsedDate)
            } else {
                item.returnDate.take(10)
            }
        } catch (_: Exception) {
            item.returnDate.take(10)
        }
    }

    // Order Code
    val orderCode = item.po?.poNumber?.ifBlank { "N/A" } ?: "N/A"

    // Status colors
    val isApproved = item.status.equals("Approved", ignoreCase = true)
    val statusBg = if (isApproved) greenBg else light_grey
    val statusText = if (isApproved) greentext else headerGrey

    val menuActions = remember {
        listOf(
            MenuAction(
                label = "View Details",
                icon = Icons.Outlined.Visibility,
                textColor = TextPrimary,
                tint = TextSecondary,
                onClick = onClick
            ),
            MenuAction(
                label = "Delete",
                icon = Icons.Outlined.Delete,
                textColor = redText,
                tint = redText,
                onClick = {}
            )
        )
    }

    DataCard(
        item = item,
        onClick = { onClick() },
        showHeaderDivider = false,
        showActionsInHeader = false,
        actions = emptyList(),
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── ROW 1: CHECKBOX • AVATAR • NAME & ORDER • STATUS BADGE • MENU ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Title & Order/Date Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = supplierName,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = blackTitle,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#$orderCode",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                            Text(
                                text = "  •  $formattedDate",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }
                    }

                    // StatusBadge with dot indicator & 3-dot dropdown menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.3f)
                    ) {
                        StatusBadge(
                            text = item.status.ifBlank { "" },
                            bgColor = statusBg,
                            textColor = statusText,
                            dotColor = statusText,
                            cornerRadius = tokens.cardCornerRadius
                        )

//                        ActionDropdownMenu(
//                            actions = menuActions,
//                            icon = Icons.Default.MoreVert
//                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

                // ── ROW 2: RMA NUMBER CHIP & RETURNED STATUS ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        // RMA Chip pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(light_blue)
                                .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.rmaNumber.ifBlank { "" },
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }

                    // Returned: Yes/No
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Returned: ",
                            fontSize = tokens.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = if (item.returned) "Yes" else "No",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

                // ── ROW 3: BOTTOM STATUS CONTAINER (RECEIVE STATUS & REFUND STATUS) ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                        .background(cardBgLight)
                        .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                        .padding(tokens.extraPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Column 1: Receive Status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RECEIVE STATUS",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = mutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.receiveStatus.ifBlank { "" },
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Column 2: Refund Status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "REFUND STATUS",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = mutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.refundStatus.ifBlank { "" },
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}