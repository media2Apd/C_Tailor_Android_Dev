@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"

)
package com.cuso.mobile.view.home.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.toHealthDisplay
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.ListSkeleton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ── Design tokens (match the screenshot) ──
private val PurplePrimary = Color(0xFF5A4FE0)
private val PurpleLight = Color(0xFFEFEDFC)
private val GreenActive = Color(0xFF1FAA59)
private val GreenActiveBg = Color(0xFFE4F7EC)
private val DarkCardBg = Color(0xFF1B2437)
private val DarkCardBg2 = Color(0xFF212B40)
private val AmberText = Color(0xFFE8A33D)
private val RedText = Color(0xFFE5484D)
private val TextGray = Color(0xFF8A93A6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryViewOne(
    item: InventoryItem?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onAdjustStock: (InventoryItem) -> Unit = {},
    onAdjustStockSubmit: (AdjustmentType, Double, String, String) -> Unit = { _, _, _, _ -> },   //   NEW
    onWarehouseTransfer: (InventoryItem) -> Unit = {},
    onReorderStock: (InventoryItem) -> Unit = {},
    onMarkInactive: (InventoryItem) -> Unit = {},
    onEdit: (InventoryItem) -> Unit = {},
    onShare: (InventoryItem) -> Unit = {}
) {

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Overview, 1 = Transactions
    var showAdjustStockSheet by remember { mutableStateOf(false) }   //   NEW

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleBar("Item Details", onClose = onDismiss)

        }

            when {
                isLoading -> {
                    ListSkeleton()
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(errorMessage, color = RedText, textAlign = TextAlign.Center)
                    }
                }
                item != null -> {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        // ── Name row: name + Active badge + edit/share ──
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(item.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            StatusBadge(active = item.status.equals("active", ignoreCase = true))
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { onEdit(item) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = TextGray)
                            }
                            IconButton(onClick = { onShare(item) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = TextGray)
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            "SKU: ${item.sku} | Created on ${formatDate(item.createdAt)}",
                            fontSize = 12.sp,
                            color = TextGray
                        )

                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showAdjustStockSheet = true },   //   CHANGED
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Adjust Stock", fontWeight = FontWeight.Medium, color = whiteBg)
                        }

                        Spacer(Modifier.height(16.dp))
                        SegmentedTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

                        Spacer(Modifier.height(20.dp))

                        if (selectedTab == 0) {
                            OverviewContent(
                                item = item,
                                onAdjustStockClick = { showAdjustStockSheet = true },   //   CHANGED
                                onWarehouseTransfer = onWarehouseTransfer,
                                onReorderStock = onReorderStock,
                                onMarkInactive = onMarkInactive
                            )
                        } else {
                            TransactionsPlaceholder()
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
    }

    //   NEW — Adjust Stock bottom sheet (still inside InventoryViewOne, before its closing brace)
    if (showAdjustStockSheet && item != null) {
        AdjustStockSheet(
            item = item,
            onDismiss = { showAdjustStockSheet = false },
            onSubmit = { type, quantity, reason, notes ->
                showAdjustStockSheet = false
                onAdjustStockSubmit(type, quantity, reason, notes)   //   CHANGED
            }
        )
    }
}   //   this closes InventoryViewOne

@Composable
private fun StatusBadge(active: Boolean) {
    val bg = if (active) GreenActiveBg else Color(0xFFFBE9E9)
    val fg = if (active) GreenActive else RedText
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(if (active) "Active" else "Inactive", color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SegmentedTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF3F4F7))
            .padding(4.dp)
    ) {
        TabChip(
            label = "Overview",
            icon = Icons.Outlined.Description,
            selected = selectedTab == 0,
            modifier = Modifier.weight(1f)
        ) { onTabSelected(0) }
        TabChip(
            label = "Transactions",
            icon = Icons.Outlined.SwapHoriz,
            selected = selectedTab == 1,
            modifier = Modifier.weight(1f)
        ) { onTabSelected(1) }
    }
}

@Composable
private fun TabChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) whiteBg else Color.Transparent
    val fg = if (selected) PurplePrimary else TextGray
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(vertical = 5.dp)
            .then(Modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
@Composable
private fun OverviewContent(
    item: InventoryItem,
    onAdjustStockClick: () -> Unit,          //   CHANGED — was onAdjustStock: (InventoryItem) -> Unit
    onWarehouseTransfer: (InventoryItem) -> Unit,
    onReorderStock: (InventoryItem) -> Unit,
    onMarkInactive: (InventoryItem) -> Unit
) {
    val health = item.toHealthDisplay()

    SectionHeader(icon = Icons.Outlined.Inventory2, title = "Item Details")
    Spacer(Modifier.height(8.dp))
    InfoRow("Item Type", item.type.replaceFirstChar { it.uppercase() })
    DividerLine()
    InfoRow("Unit", item.unit)
    DividerLine()
    InfoRow("Current Stock", formatQty(item.currentStock))

    Spacer(Modifier.height(20.dp))
    SectionHeader(icon = Icons.Outlined.Sell, title = "Sales Information")
    Spacer(Modifier.height(8.dp))
    InfoRow("Selling Price", formatCurrency(item.sellingPrice))

    Spacer(Modifier.height(16.dp))
    InventoryHealthCard(
        isTracked = health.isTracked,
        statusLabel = item.stockStatus,
        totalStockValue = health.totalStockValue,
        available = health.available,
        reserved = health.reserved,
        wip = health.wip,
        incoming = health.incoming,
        lowThreshold = health.lowThreshold
    )

    Spacer(Modifier.height(20.dp))
    SectionHeader(icon = Icons.Outlined.ShoppingCart, title = "Inventory Actions")
    Spacer(Modifier.height(10.dp))

    Button(
        onClick = onAdjustStockClick,        //   CHANGED
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Adjust Stock", fontWeight = FontWeight.Medium, color = whiteBg)
    }
    Spacer(Modifier.height(10.dp))
    OutlinedActionButton(
        label = "Warehouse Transfer",
        icon = Icons.Outlined.SwapHoriz,
        onClick = { onWarehouseTransfer(item) }
    )
    Spacer(Modifier.height(10.dp))
    OutlinedActionButton(
        label = "Reorder Stock",
        icon = Icons.Outlined.Refresh,
        onClick = { onReorderStock(item) }
    )
    Spacer(Modifier.height(10.dp))
    OutlinedActionButton(
        label = "Mark Inactive",
        icon = Icons.Outlined.Block,
        onClick = { onMarkInactive(item) },
        contentColor = RedText,
        borderColor = Color(0xFFF6D2D2)
    )

    Spacer(Modifier.height(20.dp))
    QuickInsightCard(item = item)
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = blackTitle)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGray, fontSize = 13.sp)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextGray)
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)
}

@Composable
private fun InventoryHealthCard(
    isTracked: Boolean,
    statusLabel: String,
    totalStockValue: Double,
    available: Double,
    reserved: Double,
    wip: Double,
    incoming: Double,
    lowThreshold: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCardBg)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    "INVENTORY HEALTH",
                    color = TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        statusLabel.uppercase(),
                        color = whiteBg,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GreenActive)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(GreenActive),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthMetric("Total Stock Value", formatCurrency(totalStockValue), Modifier.weight(1f))
            HealthMetric("Available", formatQty(available), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthMetric("Reserved", formatQty(reserved), Modifier.weight(1f))
            HealthMetric("WIP", formatQty(wip), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthMetric("Incoming", formatQty(incoming), Modifier.weight(1f), valueColor = AmberText)
            HealthMetric("Low Threshold", formatQty(lowThreshold), Modifier.weight(1f), valueColor = AmberText)
        }
    }
}

@Composable
private fun HealthMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = whiteBg
) {
    Column(modifier = modifier) {
        Text(label, color = TextGray, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OutlinedActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    contentColor: Color = Color(0xFF2B2F38),
    borderColor: Color = Color(0xFFE2E4E9)
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun QuickInsightCard(item: InventoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PurpleLight)
            .padding(16.dp)
    ) {
        Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Quick Insight", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,color=whiteBg)
            Spacer(Modifier.height(4.dp))
            Text(
                "Demand for ${item.name} has been changing recently. Review reorder point and current stock to avoid stockouts.",
                fontSize = 12.sp,
                color = Color(0xFF4A4A5A)
            )
        }
    }
}

@Composable
private fun TransactionsPlaceholder() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No transactions to show yet.", color = TextGray)
    }
}

// ── Formatting helpers ──
private fun formatCurrency(value: Double): String {
    val format = NumberFormat.getNumberInstance(
        Locale.Builder().setLanguage("en").setRegion("IN").build()
    )
    format.maximumFractionDigits = 0
    return "\u20B9${format.format(value)}"
}

private fun formatQty(value: Double): String {
    val format = NumberFormat.getNumberInstance(
        Locale.Builder().setLanguage("en").setRegion("IN").build()
    )
    format.maximumFractionDigits = if (value % 1.0 == 0.0) 0 else 1
    return format.format(value)
}

private fun formatDate(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso)
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        formatter.format(date!!)
    } catch (e: Exception) {
        iso
    }
}