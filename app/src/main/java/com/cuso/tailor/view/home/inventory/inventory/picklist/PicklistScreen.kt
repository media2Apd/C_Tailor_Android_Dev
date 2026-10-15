package com.cuso.tailor.view.home.inventory.inventory.picklist

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.cuso.tailor.model.inventory.PicklistItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun PicklistListScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCreatePicklistClick: () -> Unit = {},
    onEditPicklistClick: (String) -> Unit = {},
    onPicklistItemClick: (PicklistItemDto) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val picklists by viewModel.picklists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingPicklists.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMorePicklists.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMorePicklists.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchPicklists(page = 1)
    }

    FabScaffold(
        fab = FabConfig(
            label = "Create Picklist",
            icon = Icons.Default.Add,
            onClick = onCreatePicklistClick
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            TitleBar(
                title = "Picklist",
                onClose = onClose
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.fetchPicklists(page = 1, search = it.ifBlank { null })
                },
                placeholder = "Search SO number, customer...",
                onFilterClick = { }
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading && picklists.isEmpty()) {
                    ListSkeleton()
                } else if (picklists.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Picklists found",
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
                        items(picklists, key = { it.id }) { item ->
                            PicklistCardItem(
                                item = item,
                                onClick = { onPicklistItemClick(item) },
                                onEditClick = { onEditPicklistClick(item.id) }
                            )
                        }

                        if (canLoadMore) {
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMorePicklists()
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

@SuppressLint("DefaultLocale")
@Composable
private fun PicklistCardItem(
    item: PicklistItemDto,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val assigneeName = item.assignee?.fullName ?: "Unassigned"
    val picklistNumber = item.picklistNumber ?: "PL-0000"
    val warehouseName = item.warehouse?.name ?: "Main Warehouse"

    val salesOrderCode = item.items.firstOrNull()?.salesOrder?.orderCode ?: "SO-0000"
    val totalItems = item.items.size
    val totalQty = item.items.sumOf { it.qty }

    val formattedDate = remember(item.date) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val parsed = parser.parse(item.date.orEmpty())
            if (parsed != null) {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(parsed)
            } else {
                item.date?.take(10) ?: "—"
            }
        } catch (_: Exception) {
            item.date?.take(10) ?: "—"
        }
    }

    val isPicked = item.status.equals("Picked", ignoreCase = true) || item.status.equals("Completed", ignoreCase = true)
    val statusBg = if (isPicked) greenBg else light_blue
    val statusText = if (isPicked) greentext else Primary

    val menuActions = remember {
        listOf(
            MenuAction("View Details", Icons.Outlined.Visibility, textColor = TextPrimary, tint = TextSecondary, onClick = onClick),
            MenuAction("Edit", Icons.Outlined.Edit, textColor = TextPrimary, tint = TextSecondary, onClick = onEditClick),
            MenuAction("Delete", Icons.Outlined.Delete, textColor = redText, tint = redText, onClick = {})
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
                // Header Row: Name, Picklist/DN Number, Status Badge & Action Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = assigneeName,
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = blackTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = picklistNumber,
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
                            text = item.status ?: "Pending",
                            bgColor = statusBg,
                            textColor = statusText,
                            cornerRadius = tokens.cardCornerRadius * 0.8f
                        )

                        ActionDropdownMenu(
                            actions = menuActions,
                            icon = Icons.Default.MoreVert
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Middle Container: 3 Column Grid with proper alignment & ellipsis
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                        .background(Primary_background)
                        .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.9f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Column: Sales Order (Left Aligned)
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

                        // 2. Column: Location (Centered)
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

                        // 3. Column: Item/Quantity (Right Aligned)
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
                                text = "$totalItems items / $totalQty Qty",
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

                // Footer Date Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Date : ",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                    Text(
                        text = formattedDate,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }
    )
}