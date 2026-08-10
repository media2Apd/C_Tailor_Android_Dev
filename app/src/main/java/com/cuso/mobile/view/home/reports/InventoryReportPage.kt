@file:Suppress(
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "unusedvariable",
    "unused",
    "NAME_SHADOWING",
    "GrazieInspection",
    "SpellCheckingInspection", "VariableNeverRead"
)
package com.cuso.mobile.view.home.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.R

@Composable
fun InventoryReportPage(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit,
    onReportClick: (String) -> Unit = {}   // NEW
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()
    var expandedSection by remember { mutableStateOf("Stock Reports") }

    Scaffold(
        topBar = {
            Column(
                Modifier.fillMaxWidth()
            ) {
                TitleBar("Inventory", onClose = onClose)
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            ScreenBreadcrumb(listOf("Reports", "Inventory"), onClick = onBreadCrumbClick)
            Spacer(modifier = Modifier.height(16.dp))

            AccordionSection(
                title = "Stock Reports",
                iconPainter = painterResource(R.drawable.box),
                expanded = expandedSection == "Stock Reports",
                onHeaderClick = { expandedSection = if (expandedSection == "Stock Reports") "" else "Stock Reports" },
                trailing = { ReportCountBadge(5) }
            ) {
                ReportListItem("Stock Summary", "View current stock levels across all warehouses.",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_inventory_stock_summary") })
                ReportListItem("Stock Movement", "Track incoming, outgoing, and transferred inventory.",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_inventory_stock_movement") })
                ReportListItem("Inventory Valuation", "Calculate the total value of current inventory.",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_inventory_valuation") })
                ReportListItem("Fast Moving Items", "Identify products with the highest sales turnover.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_inventory_fast_moving") })
                ReportListItem("Slow Moving Items", "Find products with low sales activity.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_inventory_slow_moving") })
            }

            AccordionSection(
                title = "Inventory Alerts",
                icon = Icons.Default.WarningAmber,
                expanded = expandedSection == "Inventory Alerts",
                onHeaderClick = { expandedSection = if (expandedSection == "Inventory Alerts") "" else "Inventory Alerts" },
                trailing = { ReportCountBadge(2) }
            ) {
                ReportListItem("Low Stock", "Monitor items below the minimum stock level.",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_inventory_low_stock") })
                ReportListItem("Reorder Report", "View products that require immediate replenishment.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_inventory_reorder") })
            }

            AccordionSection(
                title = "Warehouse Reports",
                iconPainter = painterResource(R.drawable.ic_warehouse),
                expanded = expandedSection == "Warehouse Reports",
                onHeaderClick = { expandedSection = if (expandedSection == "Warehouse Reports") "" else "Warehouse Reports" },
                trailing = { ReportCountBadge(1) }
            ) {
                ReportListItem("Warehouse Report", "Analyze stock availability by warehouse location.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_inventory_warehouse_report") })
            }

            AccordionSection(
                title = "Procurement Reports",
                iconPainter = painterResource(R.drawable.cart),
                expanded = expandedSection == "Procurement Reports",
                onHeaderClick = { expandedSection = if (expandedSection == "Procurement Reports") "" else "Procurement Reports" },
                trailing = { ReportCountBadge(1) }
            ) {
                ReportListItem("Purchase Report", "Review purchase orders, suppliers, and procurement history.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_inventory_purchase_report") })
            }

            AccordionSection(
                title = "Inventory Analysis",
                iconPainter = painterResource(R.drawable.ic_inventory),
                expanded = expandedSection == "Inventory Analysis",
                onHeaderClick = { expandedSection = if (expandedSection == "Inventory Analysis") "" else "Inventory Analysis" },
                trailing = { ReportCountBadge(1) }
            ) {
                ReportListItem("Dead Stock Report", "Identify inventory with no movement for a long period",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_inventory_dead_stock") })
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
@Composable
fun ReportListItem(
    title: String,
    description: String,
    initialFavorite: Boolean,
    onClick: () -> Unit = {}
) {
    var isSelected by remember { mutableStateOf(false) } // Track if text is clicked
    var isFavorite by remember { mutableStateOf(initialFavorite) } // Independent Favorite toggle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { isSelected = !isSelected
                onClick()  }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                // Color changes ONLY if selected
                color = if (isSelected) Color(0xFF5A57D6) else Color(0xFF6B7280)
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }

        // Independent Star Click Logic
        IconButton(onClick = { isFavorite = !isFavorite }) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color(0xFFFFA500) else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF0F0F0))
}

@Composable
fun ReportCountBadge(count: Int) {
    Surface(
        color = Color(0xFFEEEEFF),
        shape = CircleShape,
        modifier = Modifier.size(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = count.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A57D6)
            )
        }
    }
}