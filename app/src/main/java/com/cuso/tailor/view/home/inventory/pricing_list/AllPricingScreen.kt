package com.cuso.tailor.view.home.inventory.pricing_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.PriceListSummary
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.inventory.bulk_items.MetaCol
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AllPricingScreen(
    priceLists: List<PriceListSummary> = samplePriceLists(),
    isLoading: Boolean = false,
    isLoadingMore: Boolean = false,
    canLoadMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onClose: () -> Unit = {},
    onAddNew: () -> Unit = {},
    onItemClick: (PriceListSummary) -> Unit = {},
    onEditItem: (PriceListSummary) -> Unit = {},
    onDeleteItem: (PriceListSummary) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    val checkedIds = remember { mutableStateMapOf<String, Boolean>() }

    // Scroll state tracker for LazyColumn
    val listState = rememberLazyListState()

    // Trigger next page fetch only when crossing the bottom scroll threshold
    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            // Trigger when reaching 2 items before the end of the list
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom && canLoadMore && !isLoadingMore && !isLoading && searchQuery.isBlank()) {
                    onLoadMore()
                }
            }
    }

    val filteredList = remember(priceLists, searchQuery) {
        if (searchQuery.isBlank()) priceLists
        else priceLists.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Primary_background)) {
        FabScaffold(
            fab = FabConfig(
                label = "Add Price List",
                icon = Icons.Default.Add,
                onClick = onAddNew,
                bottomPadding = 40.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent)) {
                        TitleBar(title = "Price Lists", onClose = onClose)
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search price lists...",
                            onFilterClick = { }
                        )
                        HorizontalDivider(color = title_border)
                    }
                }
            ) { paddingValues ->
                if (isLoading && priceLists.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CirculerProgressIndicatorSmall()
                    }
                } else if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching price lists found" else "No Price Lists Yet",
                            fontSize = tokens.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = 10.dp)
                    ) {
                        items(
                            items = filteredList,
                            key = { it.id.ifBlank { it.hashCode().toString() } }
                        ) { item ->
                            PriceListCard(
                                item = item,
                                isChecked = checkedIds[item.id] == true,
                                onCheckedChange = { checkedIds[item.id] = it },
                                onClick = { onItemClick(item) },
                                onEditClick = { onEditItem(item) },
                                onDeleteClick = { onDeleteItem(item) }
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        // Bottom indicator: shown only while a next page request is actively in-flight
                        if (isLoadingMore) {
                            item(key = "pagination_threedot_loader") {
                                ThreeDotLoading(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                )
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceListCard(
    item: PriceListSummary,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AppCheckbox(checked = isChecked, onCheckedChange = onCheckedChange)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.name.ifBlank { "—" },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = tokens.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Code: ${item.code.ifBlank { "—" }}",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(greenBg, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(darkGreenBg, CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = item.statusLabel,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = darkGreenBg
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = iconMuted, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            offset = DpOffset(x = (-10).dp, y = 0.dp),
                            modifier = Modifier.background(whiteBg, RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", fontSize = tokens.bodyMedium, color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextLog, modifier = Modifier.size(18.dp)) },
                                onClick = { menuExpanded = false; onEditClick() }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", fontSize = tokens.bodyMedium, color = redText) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = redText, modifier = Modifier.size(18.dp)) },
                                onClick = { menuExpanded = false; onDeleteClick() }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = title_border)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetaCol("DETAILS", "${item.detailsCount} Items")
                MetaCol("SCHEME", item.schemeLabel)
                MetaCol("ROUND-OFF", "₹${item.roundOff.toInt()}")
            }
        }
    }
}

private fun samplePriceLists() = listOf(
    PriceListSummary("1", "Retail Standard 2026", "PL-RET-01", "Active", 24, "All Items", 0.0),
    PriceListSummary("2", "Wholesale Bulk Rates", "PL-WHOLE-02", "Active", 120, "Individual", 0.0),
    PriceListSummary("3", "Festival Special Pricing", "PL-FEST-03", "Active", 45, "Unit Pricing", 0.0)
)