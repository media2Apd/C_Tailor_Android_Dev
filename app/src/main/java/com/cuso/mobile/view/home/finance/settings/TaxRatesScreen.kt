package com.cuso.mobile.view.home.finance.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.ActionDropdownMenu
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar

// ---------------------------------------------------------------------------
// Data models
// ---------------------------------------------------------------------------

data class TaxStatItem(
    val label: String,
    val value: String,
    val statusText: String,
    val statusColor: Color,
    val valueColor: Color = TextPrimary
)

data class TaxRateItem(
    val id: String,
    val name: String,
    val metaLine: String,
    val ratePercent: String,
    val treatment: String,
    val typeScope: String? = null, // e.g. "Type: GST  •  Scope: Services"
    val isActive: Boolean,
    val updatedDate: String
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun TaxRatesScreen(
    taxRates: List<TaxRateItem>,
    stats: List<TaxStatItem>,
    onClose: () -> Unit,
    onAddTaxRate: () -> Unit,
    onAddTaxGroup: () -> Unit,
    onEditRate: (TaxRateItem) -> Unit = {},
    onDeleteRate: (TaxRateItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(whiteBg)
    ) {
        TitleBar(title = "Tax Rates & Rules", onClose = onClose)
        HorizontalDivider(color = title_border)

        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search Customers...",
            accentColor = Primary,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { }
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                TaxRatesSectionHeader(onAddTaxRate = onAddTaxRate, onAddTaxGroup = onAddTaxGroup)
            }
            item {
                TaxStatsGrid(
                    stats = stats,
                    modifier = Modifier.padding(horizontal = tokens.screenPadding)
                )
                Spacer(Modifier.height(tokens.screenPadding))
            }
            items(taxRates, key = { it.id }) { item ->
                TaxRateCard(
                    item = item,
                    onEdit = { onEditRate(item) },
                    onDelete = { onDeleteRate(item) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ---------------------------------------------------------------------------
// Section header: title + description + "Add Tax" dropdown button
// ---------------------------------------------------------------------------

@Composable
private fun TaxRatesSectionHeader(
    onAddTaxRate: () -> Unit,
    onAddTaxGroup: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Tax Rates & Rules",
                fontSize = tokens.h2,
                fontWeight = FontWeight.Bold,
                color = title_color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Create and manage tax rates and rules used across quotations, orders and invoices.",
                fontSize = tokens.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(Modifier.width(12.dp))

        Box {
            Button(
                onClick = { menuExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = whiteBg),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("Add Tax", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                    .background(whiteBg)
                    .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Add Tax Rate", fontSize = tokens.bodySmall, color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp)) },
                    onClick = { menuExpanded = false; onAddTaxRate() }
                )
                DropdownMenuItem(
                    text = { Text("Add Tax Group", fontSize = tokens.bodySmall, color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp)) },
                    onClick = { menuExpanded = false; onAddTaxGroup() }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2x2 bordered stats grid
// ---------------------------------------------------------------------------

@Composable
private fun TaxStatsGrid(stats: List<TaxStatItem>, modifier: Modifier = Modifier) {
    if (stats.size < 4) return
    val tokens = LocalAppTokens.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius))
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TaxStatCell(stats[0], modifier = Modifier.weight(1f))
            VerticalDivider()
            TaxStatCell(stats[1], modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = BorderGray)
        Row(modifier = Modifier.fillMaxWidth()) {
            TaxStatCell(stats[2], modifier = Modifier.weight(1f))
            VerticalDivider()
            TaxStatCell(stats[3], modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(BorderGray)
    )
}

@Composable
private fun TaxStatCell(stat: TaxStatItem, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier.padding(14.dp)) {
        Text(stat.label, fontSize = tokens.caption, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        Text(stat.value, fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = stat.valueColor)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(stat.statusColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(stat.statusText, fontSize = tokens.caption, color = stat.statusColor)
        }
    }
}

// ---------------------------------------------------------------------------
// Individual tax rate card
// ---------------------------------------------------------------------------

@Composable
private fun TaxRateCard(
    item: TaxRateItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(item.metaLine, fontSize = tokens.caption, color = TextSecondary)

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaxPill(text = "Rate: ${item.ratePercent}", bg = primary_light, textColor = Primary)
                    TaxPill(text = item.treatment, bg = light_grey, textColor = TextSecondary)
                }

                if (item.typeScope != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(item.typeScope, fontSize = tokens.caption, color = mutedText)
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val dotColor = if (item.isActive) greentext else redText
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(dotColor)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (item.isActive) "Active" else "Inactive",
                            fontSize = tokens.caption,
                            color = dotColor
                        )
                    }
                    Text(
                        "Updated: ${item.updatedDate}",
                        fontSize = tokens.caption,
                        color = mutedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            ActionDropdownMenu(
                actions = listOf(
                    MenuAction(label = "Edit", onClick = onEdit),
                    MenuAction(label = "Delete", textColor = redText, onClick = onDelete)
                )
            )
        }
        HorizontalDivider(color = BorderGray)
    }
}

@Composable
private fun TaxPill(text: String, bg: Color, textColor: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}