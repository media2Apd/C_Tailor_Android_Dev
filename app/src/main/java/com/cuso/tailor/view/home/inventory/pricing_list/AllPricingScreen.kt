@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.pricing_list

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.PriceListItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel

@Composable
fun AllPricingScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onAddNew: () -> Unit = {},
    onItemClick: (PriceListItemDto) -> Unit = {},
    onEditItem: (PriceListItemDto) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val priceListsPagingState by viewModel.priceListsPagingState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // State for delete confirmation dialog
    var itemToDelete by remember { mutableStateOf<PriceListItemDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchPriceLists()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        FabScaffold(
            fab = FabConfig(
                label = "Add Pricing",
                icon = Icons.Default.Add,
                onClick = onAddNew,
                bottomPadding = 40.dp
            ),
            fabVisible = !priceListsPagingState.isInitialLoading,
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                    ) {
                        TitleBar(title = "All Pricing", onClose = onClose)
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { query ->
                                searchQuery = query
                                viewModel.fetchPriceLists(search = query)
                            },
                            placeholder = "Search price lists by name...",
                            onFilterClick = {}
                        )
                        HorizontalDivider(color = title_border)
                    }
                }
            ) { paddingValues ->
                Column(modifier = Modifier.fillMaxSize()) {
                    if (!priceListsPagingState.isInitialLoading && priceListsPagingState.items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching price lists found" else "No Pricing Schemes Found",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    } else {
                        PaginatedDataList(
                            state = priceListsPagingState,
                            onLoadMore = { viewModel.loadMorePriceLists() },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) { item ->
                            PriceListItemCard(
                                item = item,
                                onClick = { onItemClick(item) },
                                onEditClick = { onEditItem(item) },
                                onDeleteClick = { itemToDelete = item }
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        itemToDelete?.let { item ->
            DeleteModel(
                title = "Delete Price List",
                message = "You're going to delete the \"${item.name}\"\nprice list. Are you sure?",
                checkboxText = "I confirm this deletion",
                cancelText = "No, Keep It.",
                confirmText = "Yes, Delete!",
                onDismiss = { itemToDelete = null },
                onDelete = {
                    viewModel.deletePriceList(
                        id = item.id,
                        onSuccess = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            itemToDelete = null
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            itemToDelete = null
                        }
                    )
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ── REUSABLE DATACARD COMPONENT FOR PRICING LIST ITEM ──
// ─────────────────────────────────────────────────────────────
@Composable
private fun PriceListItemCard(
    item: PriceListItemDto,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isActive = item.status.equals("ACTIVE", ignoreCase = true)

    val menuActions = listOf(
        MenuAction(label = "Edit", onClick = onEditClick),
        MenuAction(label = "Delete", onClick = onDeleteClick)
    )

    val pricingSchemeDisplay = item.pricingScheme.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }

    val roundOffDisplay = if (item.roundOff?.enabled == true) {
        "${currencySymbol()}${item.roundOff.value}"
    } else {
        "—"
    }

    val detailsDisplay = if (item.description.isNotBlank()) {
        item.description
    } else {
        item.transactionType
    }

    DataCard(
        item = item,
        title = item.name.ifBlank { "Untitled Pricing" },
        code = item.transactionType,
        titleFontWeight = FontWeight.Medium,
        titleColor = TextPrimary,
        topBadgeText = if (isActive) "Active" else item.status,
        topBadgeInline = true,
        topBadgeBgColor = if (isActive) greenBg else grey_border,
        topBadgeTextColor = if (isActive) darkGreenBg else TextSecondary,
        topBadgeShowDot = false,
        actions = menuActions,
        onClick = { onClick() },
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary_background,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Details Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Details",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = detailsDisplay,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 2. Pricing Scheme Column
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Pricing Scheme",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = pricingSchemeDisplay,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 3. Round-Off Column
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Round-Off",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = roundOffDisplay,
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    )
}