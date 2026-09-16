package com.cuso.mobile.view.home.inventory.bulk_items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.BulkItemDoc
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.InventoryViewModel

@Composable
fun BulkListScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onAddBulkClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val bulkList by viewModel.bulkItems.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchBulkItems()
    }

    val filteredList = remember(bulkList, searchQuery) {
        if (searchQuery.isBlank()) bulkList
        else bulkList.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Primary_background)) {
        FabScaffold(
            fab = FabConfig(
                label = "Add Bulk Item",
                icon = Icons.Default.Add,
                onClick = onAddBulkClick,
                bottomPadding = 40.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                        TitleBar(title = "All Bulk", onClose = onClose)
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search Customers...",
                            onFilterClick = { /* Filter trigger */ }
                        )
                        HorizontalDivider(color = title_border)
                    }
                }
            ) { paddingValues ->
                if (isLoading && bulkList.isEmpty()) {
                    ListSkeleton()
                } else if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching bulk items found" else "No Bulk Items Yet",
                            fontSize = tokens.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        items(filteredList, key = { it.id }) { item ->
                            BulkListItemCard(
                                item = item,
                                onClick = {
                                    if (item.id.isNotBlank()) {
                                        onItemClick(item.id)
                                    }
                                },
                                onEditClick = {
                                    if (item.id.isNotBlank()) {
                                        onEditClick(item.id)
                                    }
                                },
                                onDeleteClick = {
                                    viewModel.deleteBulkItem(item.id) {}
                                }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BulkListItemCard(
    item: BulkItemDoc,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var isChecked by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
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
                    AppCheckbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.name.ifBlank { "—" },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = tokens.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "SKU: ${item.sku.ifBlank { "—" }}",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }

                // 3-Dot Action Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = iconMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        offset = DpOffset(x = (-10).dp, y = 0.dp),
                        modifier = Modifier.background(whiteBg, RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("View", fontSize = tokens.bodyMedium, color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Edit", fontSize = tokens.bodyMedium, color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = TextLog, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete", fontSize = tokens.bodyMedium, color = redText) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = redText, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("STOCK ON HAND", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text("%,.0f".format(item.stockOnHand), fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column {
                    Text("REORDER POINT", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text("%,.0f".format(item.reorderPoint), fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Status", fontSize = tokens.bodySmall, color = TextSecondary)
                Box(
                    modifier = Modifier
                        .background(yellowBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(yellowText, CircleShape)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(item.status.ifBlank { "Active" }, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = yellowText)
                    }
                }
            }
        }
    }
}