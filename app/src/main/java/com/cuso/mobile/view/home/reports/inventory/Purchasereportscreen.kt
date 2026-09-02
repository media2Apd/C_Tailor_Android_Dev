@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)

package com.cuso.mobile.view.home.reports.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.FilterDrawer
import com.cuso.mobile.view.composable.FilterOption
import com.cuso.mobile.view.composable.FilterSection
import com.cuso.mobile.view.composable.FilterSectionType
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.view.composable.rememberFilterDrawerState

// ── Design tokens (screen-specific accent colors; sizing/spacing comes from LocalAppTokens) ──
private val ReportPrimary = Primary
private val CriticalRed = redText
private val WarningOrange = Color(0xFFF59E0B)
private val HealthyGreen = greentext
private val TitleColor = title_color
private val MutedColor = mutedText
private val BorderColor = PrimaryBorder

// ── Data Models ──
private data class PoReportStat(
    val label: String,
    val value: String,
    val valueColor: Color = TitleColor
)

private enum class PoBadgeType { HEALTHY, WARNING, CRITICAL }

private data class PurchaseOrder(
    val id: String,
    val poNumber: String,
    val paymentStatus: String,
    val badgeText: String,
    val badgeType: PoBadgeType,
    val supplier: String,
    val items: Int,
    val purchaseDate: String,
    val warehouse: String,
    val totalAmount: String,
    val updatedAt: String
)

private fun badgeColor(type: PoBadgeType): Color = when (type) {
    PoBadgeType.HEALTHY -> HealthyGreen
    PoBadgeType.WARNING -> WarningOrange
    PoBadgeType.CRITICAL -> CriticalRed
}

private fun samplePurchaseOrders(): List<PurchaseOrder> = listOf(
    PurchaseOrder("1", "PO-2026-0158", "Paid", "Received", PoBadgeType.HEALTHY, "ABC Industrial Supplies", 28, "22 Jul 2026", "Chennai Central", "\u20B98,450", "Today 10:45 AM"),
    PurchaseOrder("2", "PO-2026-0158", "Paid", "Received", PoBadgeType.HEALTHY, "ABC Industrial Supplies", 28, "22 Jul 2026", "Chennai Central", "\u20B98,450", "Today 10:45 AM"),
    PurchaseOrder("3", "PO-2026-0157", "Partially Paid", "In transit", PoBadgeType.WARNING, "Metro Fabric World", 28, "22 Jul 2026", "Chennai Central", "\u20B98,450", "Today 10:45 AM")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseReportScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var searchQuery by remember { mutableStateOf("") }
    var exportSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    val filterDrawerState = rememberFilterDrawerState()

    var filterBlur by remember { mutableStateOf(0.dp) }
    var exportBlur by remember { mutableStateOf(0.dp) }

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Payment Status",
                    icon = Icons.Filled.Payments,
                    type = FilterSectionType.CHIP_ROW,
                    isMultiSelect = false,
                    options = listOf(
                        FilterOption("all", "All", isSelected = true),
                        FilterOption("paid", "Paid"),
                        FilterOption("partial", "Partially Paid"),
                        FilterOption("unpaid", "Unpaid")
                    )
                ),
                FilterSection(
                    title = "Supplier",
                    icon = Icons.Filled.LocalShipping,
                    type = FilterSectionType.DROPDOWN,
                    options = emptyList(),
                    dropdownValue = "All Suppliers"
                ),
                FilterSection(
                    title = "Warehouse",
                    icon = Icons.Filled.Warehouse,
                    type = FilterSectionType.DROPDOWN,
                    options = emptyList(),
                    dropdownValue = "All Warehouses"
                )
            )
        )
    }

    val purchaseOrders = remember { samplePurchaseOrders() }
    val filteredOrders = remember(searchQuery, purchaseOrders) {
        if (searchQuery.isBlank()) purchaseOrders
        else purchaseOrders.filter {
            it.poNumber.contains(searchQuery, ignoreCase = true) ||
                    it.supplier.contains(searchQuery, ignoreCase = true)
        }
    }

    val stats = listOf(
        PoReportStat("Purchase Orders", "325", HealthyGreen),
        PoReportStat("Suppliers", "48"),
        PoReportStat("Total Purchase", "\u20B91.25M"),
        PoReportStat("Pending Deliveries", "18")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                // TitleBar lives purely in Scaffold's topBar slot so no
                // sheet/drawer scrim or blur can ever sit on top of it.
                Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                    Column {
                        TitleBar("Purchase Report", onClose = onClose)
                    }
                }
                HorizontalDivider(color = BorderColor)
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blurScrim(maxOf(exportBlur, filterBlur))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        contentPadding = PaddingValues(bottom = tokens.screenPadding * 1.5f)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(end = tokens.screenPadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {

                                }
                                Row(
                                    modifier = Modifier
                                        .clickable { exportSheetState = SheetValue.Collapsed }
                                        .padding(horizontal = tokens.screenPadding / 2, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.FileUpload,
                                        contentDescription = "Export",
                                        tint = ReportPrimary,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Text(
                                        "Export",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ReportPrimary
                                    )
                                }
                            }
                        }

                        item {
                            Column(Modifier.fillMaxWidth()) {
                                Row(Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
                                    FormLabel("Search")
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(tokens.fieldHeight)
                                            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                                            .border(
                                                1.dp,
                                                grey_border,
                                                RoundedCornerShape(tokens.cardCornerRadius / 1.5f)
                                            )
                                            .padding(horizontal = tokens.screenPadding * 0.85f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MutedColor,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            textStyle = TextStyle(fontSize = tokens.bodyMedium, color = TitleColor),
                                            decorationBox = { inner ->
                                                if (searchQuery.isEmpty()) {
                                                    Text(
                                                        "Search by PO number or supplier...",
                                                        fontSize = tokens.bodyMedium,
                                                        color = MutedColor
                                                    )
                                                }
                                                inner()
                                            }
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(tokens.fieldHeight)
                                            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                                            .clickable { filterDrawerState.open() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = "Filter",
                                            tint = TitleColor,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                stats.chunked(2).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        row.forEach { stat ->
                                            PurchaseStatCard(
                                                stat,
                                                Modifier.weight(1f)
                                            )
                                        }
                                        if (row.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Purchase Orders",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = TitleColor
                                )
                                Text(
                                    "${filteredOrders.size} orders",
                                    fontSize = tokens.caption,
                                    color = MutedColor
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        items(filteredOrders, key = { it.id }) { order ->
                            DataCard(
                                item = order,
                                smalltitle = order.poNumber,
                                percentage = order.paymentStatus,
                                titleFontWeight = FontWeight.SemiBold,
                                subtitle = "Supplier: ${order.supplier} · Items: ${order.items}",
                                topBadgeText = order.badgeText,
                                topBadgeInline = true,
                                topBadgeTextColor = badgeColor(order.badgeType),
                                topBadgeBgColor = badgeColor(order.badgeType).copy(alpha = 0.12f),
                                trailingText = null,
                                actions = listOf(
                                    MenuAction(
                                        label = "View",
                                        icon = Icons.Default.Visibility,
                                        onClick = { }
                                    ),
                                    MenuAction(
                                        label = "Export",
                                        icon = Icons.Default.FileUpload,
                                        onClick = { }
                                    )
                                ),
                                content = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        PoStatLine("Purchase Date", order.purchaseDate)
                                        Spacer(Modifier.height(6.dp))
                                        PoStatLine("Warehouse", order.warehouse)
                                        Spacer(Modifier.height(6.dp))
                                        PoStatLine("Total Amount", order.totalAmount)
                                        Spacer(Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = MutedColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "Updated ${order.updatedAt}",
                                                fontSize = tokens.caption,
                                                color = MutedColor
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // ── Bottom sheet + filter drawer — nested inside the same padded/bounded Box ──
                SmoothBottomSheet(
                    state = exportSheetState,
                    onStateChange = { exportSheetState = it },
                    peekHeight = 340.dp,
                    topInset = 0.dp,
                    maxBlurRadius = 16.dp,
                    maxScrimAlpha = 0.45f,
                    scrollableContent = false,
                    onBlurScrimChange = { blur, _ -> exportBlur = blur },
                    onDismissRequest = { exportSheetState = SheetValue.Hidden }
                ) {
                    ExportReportSheetContent(onDismiss = { exportSheetState = SheetValue.Hidden })
                }

                FilterDrawer(
                    state = filterDrawerState,
                    title = "Filter Purchase Orders",
                    sections = filterSections,
                    onApply = { updated -> filterSections = updated },
                    onClearAll = { },
                    onBackgroundBlurChange = { blur -> filterBlur = blur }
                )
            }
        }
    }
}

@Composable
private fun PoBadge(text: String, color: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
 fun PoStatLine(label: String, value: String, valueColor: Color = TitleColor) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, color = MutedColor)
        Text(value, fontSize = tokens.bodySmall, color = valueColor)
    }
}

@Composable
private fun PurchaseStatCard(stat: PoReportStat, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .background(whiteBg)
            .padding(tokens.screenPadding * 0.85f)
    ) {
        Text(stat.label, fontSize = tokens.caption, color = MutedColor, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(stat.value, fontSize = tokens.bodySmall, color = stat.valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ExportReportSheetContent(onDismiss: () -> Unit) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
        Text("Export Report", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TitleColor)
        Text("Choose an export option", fontSize = tokens.caption, color = MutedColor)
        Spacer(Modifier.height(12.dp))

        ExportOptionRow(Icons.Default.PictureAsPdf, redText, "Export as PDF") { onDismiss() }
        ExportOptionRow(Icons.Default.GridOn, Color(0xFF16A34A), "Export as Excel") { onDismiss() }
        ExportOptionRow(Icons.Default.Share, Color(0xFF3B82F6), "Share Report") { onDismiss() }
        ExportOptionRow(Icons.Default.Email, Color(0xFF9333EA), "Send by Email") { onDismiss() }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
            shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f)
        ) {
            Text("Cancel", fontSize = tokens.bodyMedium, color = TitleColor, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ExportOptionRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(tokens.fieldHeight * 0.8f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(tokens.iconSize))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TitleColor)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MutedColor, modifier = Modifier.size(tokens.iconSize))
    }
}