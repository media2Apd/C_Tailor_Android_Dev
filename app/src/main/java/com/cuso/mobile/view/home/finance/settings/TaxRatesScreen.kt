@file:Suppress("UNUSED_PARAMETER", "SpellCheckingInspection")

package com.cuso.mobile.view.home.finance.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.ActionDropdownMenu
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar

// ---------------------------------------------------------------------------
// Data Models
// ---------------------------------------------------------------------------

data class TaxStatItem(
    val label: String,
    val value: String,
    val statusText: String,
    val statusColor: Color,
    val valueColor: Color = Color(0xFF111827)
)

data class TaxRateItem(
    val id: String,
    val name: String,
    val metaLine: String,
    val ratePercent: String,
    val treatment: String,
    val typeScope: String? = null,
    val isActive: Boolean = true,
    val updatedDate: String
)

// ---------------------------------------------------------------------------
// Main Screen
// ---------------------------------------------------------------------------

@Composable
fun TaxRatesScreen(
    taxRates: List<TaxRateItem> = emptyList(),
    stats: List<TaxStatItem> = emptyList(),
    onClose: () -> Unit,
    onAddTaxRate: () -> Unit,
    onAddTaxGroup: () -> Unit,
    onEditRate: (TaxRateItem) -> Unit = {},
    onDeleteRate: (TaxRateItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current


    var searchQuery by remember { mutableStateOf("") }

    // Fallback default sample statistics matching the design
    val effectiveStats = stats.ifEmpty {
        listOf(
        TaxStatItem("Active Tax Rates", "5", "Compliant", Color(0xFF10B981)),
        TaxStatItem("Inactive Tax Rates", "1", "Deactivated", Color(0xFFEF4444)),
        TaxStatItem("GST Groups", "2", "Active Items", Color(0xFF3B82F6)),
        TaxStatItem("Services Rates", "4", "Active Services", Color(0xFF3B82F6))
    )
    }

    // Fallback default sample tax rates matching the design
    val effectiveRates = taxRates.ifEmpty {
        listOf(
        TaxRateItem("1", "GST 5% — Goods", "GST • Applicable to Goods", "5%", "Taxable", updatedDate = "01 Aug 2026"),
        TaxRateItem("2", "GST 12% — Services", "GST • Applicable to Services", "12%", "Taxable", updatedDate = "01 Apr 2026"),
        TaxRateItem("3", "GST 18% — Goods", "GST • Applicable to Goods", "18%", "Taxable", updatedDate = "15 Mar 2026"),
        TaxRateItem("4", "GST 18% — Services", "GST • Applicable to Services", "18%", "Taxable", typeScope = "Type: GST  •  Scope: Services", updatedDate = "10 Mar 2026"),
        TaxRateItem("5", "GST 28% — Goods", "GST • Applicable to Goods", "28%", "Taxable", updatedDate = "01 Feb 2026"),
        TaxRateItem("6", "GST 0% — Exempt", "GST • Applicable to Goods", "0%", "Exempt", updatedDate = "01 Jan 2026")
    )
    }

    val filteredRates = remember(effectiveRates, searchQuery) {
        if (searchQuery.isBlank()) effectiveRates
        else effectiveRates.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.metaLine.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Row(Modifier.fillMaxWidth()) {
            // Top Title Bar
            TitleBar(title = "Tax Rates & Rules", onClose = onClose)
            HorizontalDivider(color = grey_border, thickness = 2.dp)
        }

        // Search and Filter Bar
        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search Customers...",
            accentColor = Primary,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize() ,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Section with Title, Subtitle and Add Tax Button
            item {
                TaxRatesSectionHeader(
                    onAddTaxRate = onAddTaxRate,
                    onAddTaxGroup = onAddTaxGroup
                )
            }

            // 2x2 Stats Grid Card
            item {
                TaxStatsGrid(
                    stats = effectiveStats,
                )
                Spacer(Modifier.height(16.dp))
            }

            // List of Tax Rate Cards
            items(filteredRates, key = { it.id }) { item ->
                TaxRateCard(
                    item = item,
                    onEdit = { onEditRate(item) },
                    onDelete = { onDeleteRate(item) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section Header Component: Title + Subtitle + "Add Tax" Button
// ---------------------------------------------------------------------------

@Composable
private fun TaxRatesSectionHeader(
    onAddTaxRate: () -> Unit,
    onAddTaxGroup: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val density = LocalDensity.current
    var menuExpanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableStateOf(0.dp) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tax Rates & Rules",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Create and manage tax rates and rules used across quotations, orders and invoices.",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.width(5.dp))

        Box {
            // Add Tax Action Button
            Button(
                onClick = { menuExpanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 25.dp),
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    buttonWidth = with(density) { coordinates.size.width.toDp() }
                }
            ) {
                Text(
                    text = "Add Tax",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Open Add Tax Menu",
                    modifier = Modifier.size(16.dp)
                )
            }

            // Dropdown Menu matching exact button width
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                shape = RoundedCornerShape(8.dp),
                containerColor = Color.White,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .width(if (buttonWidth > 0.dp) buttonWidth else 130.dp)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Add Tax Rate",
                            fontSize = 13.sp,
                            color = Color(0xFF111827)
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    onClick = {
                        menuExpanded = false
                        onAddTaxRate()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Add Tax Group",
                            fontSize = 13.sp,
                            color = Color(0xFF111827)
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    onClick = {
                        menuExpanded = false
                        onAddTaxGroup()
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2x2 Stats Grid Card Component
// ---------------------------------------------------------------------------

@Composable
private fun TaxStatsGrid(stats: List<TaxStatItem>, modifier: Modifier = Modifier) {
    if (stats.size < 4) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TaxStatCell(stats[0], modifier = Modifier.weight(1f))
            VerticalDivider(Modifier.height(150.dp), thickness = 2.dp, color = grey_border)
            TaxStatCell(stats[1], modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = grey_border, thickness = 2.dp)
        Row(modifier = Modifier.fillMaxWidth()) {
            TaxStatCell(stats[2], modifier = Modifier.weight(1f))
            VerticalDivider(Modifier.height(150.dp), thickness = 2.dp, color = grey_border)
            TaxStatCell(stats[3], modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TaxStatCell(stat: TaxStatItem, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(14.dp)) {
        Text(
            text = stat.label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stat.value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = stat.valueColor
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(stat.statusColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stat.statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = stat.statusColor
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Individual Tax Rate Card Component
// ---------------------------------------------------------------------------

@Composable
private fun TaxRateCard(
    item: TaxRateItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()
        .background(whiteBg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(2.dp))

                // Meta line
                Text(
                    text = item.metaLine,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(Modifier.height(10.dp))

                // Pills / Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Rate Pill (Light Blue/Purple Background)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = "Rate: ${item.ratePercent}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Treatment Pill (Light Gray Background with Border)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF9FAFB),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text(
                            text = item.treatment,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Optional Type & Scope Line
                if (!item.typeScope.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.typeScope,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Bottom Info Row: Status Badge & Updated Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Active Status Badge with Dot
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (item.isActive) "Active" else "Inactive",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    // Updated Date Text
                    Text(
                        text = "Updated: ${item.updatedDate}",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // 3-Dot Action Menu (Edit / Delete)
            ActionDropdownMenu(
                actions = listOf(
                    MenuAction(label = "Edit", onClick = onEdit),
                    MenuAction(label = "Delete", textColor = redText, onClick = onDelete)
                )
            )
        }

    }
    Spacer(Modifier.height(15.dp))

}