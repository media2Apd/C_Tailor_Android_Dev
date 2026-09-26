package com.cuso.tailor.view.home.inventory.bulk_items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.AssociatedItemDto
import com.cuso.tailor.model.inventory.BulkItemDoc
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.pdfgenerator.BulkItemPdfGenerator
import com.cuso.tailor.view.home.subscriptions.WhiteCard
import com.cuso.tailor.viewmodel.InventoryViewModel
import androidx.core.net.toUri

@Composable
fun BulkDetailsScreen(
    itemId: String,
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onAdjustStock: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Transactions")

    val selectedItem by viewModel.selectedBulkItem.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showAdjustStockDialog by remember { mutableStateOf(false) }

    val pdfGenerator = remember(context) { BulkItemPdfGenerator(context) }

    LaunchedEffect(itemId) {
        if (itemId.isNotBlank()) {
            viewModel.fetchBulkItemDetail(itemId)
        }
    }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("Bulk Details", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        if (isLoading && selectedItem == null) {
            Column(
                Modifier.fillMaxSize()
                    .padding(paddingValues)
            ) {
                ListSkeleton()
            }
        } else if (selectedItem == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No bulk item details found",
                    fontSize = tokens.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            val item = selectedItem!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.name.ifBlank { "—" },
                            fontSize = tokens.h2,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(greenBg, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.status.replaceFirstChar { it.uppercase() },
                                fontSize = tokens.caption,
                                color = darkGreenBg,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "SKU: ${item.sku.ifBlank { "—" }} • Unit: ${item.unit}",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit Button
                        OutlinedButton(
                            onClick = { onEdit(item.id) },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = SolidColor(BorderGray)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextLog
                            )

                            Spacer(Modifier.width(4.dp))

                            Text(
                                "Edit",
                                fontSize = tokens.bodySmall,
                                color = TextLog
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                pdfGenerator.downloadBulkItemPdf(item)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = SolidColor(BorderGray)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextLog
                            )

                            Spacer(Modifier.width(4.dp))

                            Text(
                                "PDF",
                                fontSize = tokens.bodySmall,
                                color = TextLog
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        // Adjust Stock Button
                        Button(
                            onClick = {
                                if (item.associatedItems.isNotEmpty()) {
                                    showAdjustStockDialog = true
                                } else {
                                    onAdjustStock(item.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Adjust Stock", fontSize = tokens.bodySmall, color = whiteBg, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Tab Switcher
                AppUnderlineTabRow(
                    tabs = tabTitles,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                // Tab Content LazyColumn
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    if (selectedTab == 0) {
                        item { OverviewTabContent(item) }
                    } else {
                        item { TransactionsTabContent(item) }
                    }
                }
            }

            // ── Select Item to Adjust Stock Dialog ──
            if (showAdjustStockDialog) {
                SelectAdjustStockDialog(
                    associatedItems = item.associatedItems,
                    onDismiss = { showAdjustStockDialog = false },
                    onAdjustItem = { selectedAssociatedItem ->
                        showAdjustStockDialog = false
                        onAdjustStock(selectedAssociatedItem.sku)
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SELECT ITEM TO ADJUST STOCK DIALOG
// ─────────────────────────────────────────────────────────────

@Composable
fun SelectAdjustStockDialog(
    associatedItems: List<AssociatedItemDto>,
    onDismiss: () -> Unit,
    onAdjustItem: (AssociatedItemDto) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Item to Adjust Stock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // List of items
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(associatedItems) { assocItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                                .background(whiteBg, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = assocItem.name.ifBlank { "—" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "SKU: ${assocItem.sku.ifBlank { "—" }}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Button(
                                onClick = { onAdjustItem(assocItem) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F2BE6)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "Adjust",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB CONTENTS & SECTIONS
// ─────────────────────────────────────────────────────────────

@Composable
fun OverviewTabContent(item: BulkItemDoc) {
    val tokens = LocalAppTokens.current
    var selectedImages by remember(item.image?.fileUrl) {
        mutableStateOf(
            listOfNotNull(item.image?.fileUrl?.takeIf { it.isNotBlank() }?.toUri())
        )
    }

    val snapshot = item.inventorySnapshot
    var reportingTagsExpanded by remember { mutableStateOf(false) }
    var priceListsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(tokens.screenPadding)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TagBadge("Assembly: ${item.assemblyType}", light_grey, TextSecondary)
            TagBadge(if (item.trackInventory) "Inventory Tracked" else "Untracked", greenBg, darkGreenBg)
        }
        Spacer(Modifier.height(8.dp))
        Text(item.name, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)

        Spacer(Modifier.height(14.dp))

        ImageUploadSection(
            selectedImages = selectedImages,
            onBrowseClick = {},
            onCameraClick = {},
            onRemoveImage = { uri -> selectedImages = selectedImages.filter { it != uri } }
        )

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetaCol("SKU", item.sku.ifBlank { "—" })
            MetaCol("UNIT", item.unit.ifBlank { "—" })
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetaCol("TAX CATEGORY", item.taxCategory ?: "GST ${item.taxPercent.toInt()}%")
            MetaCol("BRAND", item.brand ?: "—")
        }
        if (item.warehouseRestrictionName != null) {
            Spacer(Modifier.height(12.dp))
            MetaCol("WAREHOUSE RESTRICTION", item.warehouseRestrictionName ?: "—")
        }

        Spacer(Modifier.height(20.dp))
        // Inventory Snapshot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(greentext, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text("INVENTORY SNAPSHOT", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextLog)
            }
            TagBadge("• ${item.status.replaceFirstChar { it.uppercase() }}", greenBg, darkGreenBg)
        }

        Spacer(Modifier.height(12.dp))
        Text("INVENTORY STATUS", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(modelGray, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Opening Stock", fontSize = tokens.caption, color = TextSecondary)
                    Text((snapshot?.openingStock ?: item.openingStock).toInt().toString(), fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(modelGray, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Reorder Point", fontSize = tokens.caption, color = TextSecondary)
                    Text((snapshot?.reorderPoint ?: item.reorderPoint).toInt().toString(), fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("ACCOUNTING STOCK", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.SemiBold)
        StockRow("Stock on Hand", (snapshot?.stockOnHand ?: item.stockOnHand).toInt().toString())
        StockRow("Available for Sale", (snapshot?.availableForSale ?: item.stockOnHand).toInt().toString())

        Spacer(Modifier.height(12.dp))
        Text("PHYSICAL STOCK", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.SemiBold)
        StockRow("Actual Physical Stock", (snapshot?.actualPhysicalStock ?: item.stockOnHand).toInt().toString())

        Spacer(Modifier.height(20.dp))
        SectionHeader(Icons.Default.ShoppingCart, "PURCHASE INFORMATION")
        Text("Cost Price", fontSize = tokens.caption, color = iconMuted)
        Text("₹${item.costPrice}", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Purchase Account", fontSize = tokens.bodySmall, color = TextSecondary)
            Text(item.purchaseAccountId ?: "Cost of Goods Sold (COGS)", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextLog)
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader(Icons.Default.Sell, "SALES INFORMATION")
        Text("Selling Price", fontSize = tokens.caption, color = iconMuted)
        Text("₹${item.sellingPrice}", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Sales Account", fontSize = tokens.bodySmall, color = TextSecondary)
            Text(item.salesAccountId ?: "Sales Revenue", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextLog)
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader(Icons.Default.Inventory2, "ASSOCIATED ITEMS (${item.associatedItems.size})")
        if (item.associatedItems.isEmpty()) {
            Text("No associated items", fontSize = tokens.bodySmall, color = TextSecondary)
        } else {
            item.associatedItems.forEach { assoc ->
                AssociatedItemCard(assoc)
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        AccordionSection(
            icon = Icons.Default.LocalOffer,
            title = "Reporting Tags",
            expanded = reportingTagsExpanded,
            onHeaderClick = { reportingTagsExpanded = !reportingTagsExpanded }
        ) {
            Text("No reporting tags assigned.", fontSize = tokens.bodySmall, color = TextSecondary)
        }

        AccordionSection(
            icon = Icons.Default.PriceCheck,
            title = "Associated Price Lists",
            expanded = priceListsExpanded,
            onHeaderClick = { priceListsExpanded = !priceListsExpanded }
        ) {
            Text("Standard Retail Price List", fontSize = tokens.bodySmall, color = TextPrimary)
        }
    }
}

@Composable
fun TransactionsTabContent(item: BulkItemDoc) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(tokens.screenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetaCol("TOTAL LOGS", item.assemblyLog.size.toString())
            MetaCol("CURRENT WAREHOUSES", item.stockByWarehouse.size.toString())
        }

        Spacer(Modifier.height(20.dp))
        Text("Assembly & Transaction Logs", fontSize = tokens.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        if (item.assemblyLog.isEmpty()) {
            Text("No transactions recorded yet.", fontSize = tokens.bodySmall, color = TextSecondary)
        } else {
            item.assemblyLog.forEach { log ->
                WhiteCard(tokens = tokens, padding = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(log.type, fontWeight = FontWeight.Bold, fontSize = tokens.bodyMedium, color = TextPrimary)
                        Text(
                            text = "${if (log.qty > 0) "+${log.qty}" else log.qty.toString()} Units",
                            fontWeight = FontWeight.Bold,
                            fontSize = tokens.bodyMedium,
                            color = if (log.qty >= 0) darkGreenBg else redText
                        )
                    }

                    if (!log.remarks.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(log.remarks, fontSize = tokens.bodySmall, color = TextSecondary)
                    }

                    if (log.warehouseName != null) {
                        Spacer(Modifier.height(2.dp))
                        Text("Warehouse: ${log.warehouseName}", fontSize = tokens.caption, color = TextLog)
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "By: ${log.performerName}",
                            fontSize = tokens.caption,
                            color = iconMuted
                        )
                        Text(
                            text = log.performedAt?.take(10) ?: "",
                            fontSize = tokens.caption,
                            color = iconMuted
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AssociatedItemCard(item: AssociatedItemDto) {
    val tokens = LocalAppTokens.current

    WhiteCard(tokens = tokens, padding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(yellowBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_desk),
                    contentDescription = null,
                    tint = yellowText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name.ifBlank { "—" }, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("SKU: ${item.sku.ifBlank { "—" }}", fontSize = tokens.caption, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Acc. Stock: ${item.accountingStock.toInt()}", fontSize = tokens.label, color = TextSecondary)
                    Text("Qty Req: ${item.qtyRequired}", fontSize = tokens.label, color = TextSecondary)
                    Text("₹${item.totalValue}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun TagBadge(text: String, bgColor: Color, textColor: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
fun MetaCol(title: String, value: String) {
    val tokens = LocalAppTokens.current
    Column {
        Text(title, fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
fun StockRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = tokens.bodySmall, color = TextSecondary)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(primary_light, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextLog)
    }
}