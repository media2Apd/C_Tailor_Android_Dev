package com.cuso.tailor.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import java.util.Locale
import java.util.UUID

/**
 * Unified data model representing an editable procurement/requisition line item.
 */
data class LineItemFormEntry(
    val id: String = UUID.randomUUID().toString(),
    var itemId: String = "",
    var name: String = "",
    var unit: String = "pcs",
    var type: String = "Goods",
    var qty: String = "1",
    var rate: String = "0",
    var taxPercent: String = "0",
    var total: String = "0.00"
)

/**
 * Reusable Line Items section component with:
 * - Dynamic list of line items with item selector dropdown
 * - Auto-fill for unit, rate, and tax
 * - Dynamic line-total calculation
 * - Item removal action
 * - "+ Add Item" action button
 */
@Composable
fun FormLineItemsSection(
    itemsList: SnapshotStateList<LineItemFormEntry>,
    inventoryItemsList: List<Any>,
    modifier: Modifier = Modifier,
    tokens: AppDesignTokens = LocalAppTokens.current,
    title: String = "Items",
    typeOptions: List<String> = listOf("Goods", "Service"),
    horizontalPadding: Boolean = true
) {
    var activeItemDropdownId by remember { mutableStateOf<String?>(null) }
    var activeTypeDropdownId by remember { mutableStateOf<String?>(null) }

    val containerModifier = if (horizontalPadding) {
        modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)
    } else {
        modifier.fillMaxWidth()
    }

    Column(modifier = containerModifier) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = title_color
            )

            Text(
                text = "${itemsList.size} item(s)",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )
        }

        Spacer(Modifier.height(tokens.extraPadding))

        // Cards List
        itemsList.forEachIndexed { index, itemEntry ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = tokens.extraPadding),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(tokens.extraPadding * 1.4f)) {
                    // Item Header Row with Delete Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Item #${index + 1}",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )

                        if (itemsList.size > 1) {
                            IconButton(
                                onClick = { itemsList.removeAt(index) },
                                modifier = Modifier.size(tokens.iconSize * 1.3f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Line Item",
                                    tint = redText,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    // Inventory Item Selector Dropdown
                    FormDropdown(
                        label = "Select Item",
                        value = itemEntry.name.ifBlank { "Select Item" },
                        expanded = activeItemDropdownId == itemEntry.id,
                        onExpandChange = { isExpanded ->
                            activeItemDropdownId = if (isExpanded) itemEntry.id else null
                        },
                        options = inventoryItemsList.map { getItemPropertyString(it, "name", "itemName") }.filter { it.isNotBlank() },
                        onOptionSelected = { selectedName ->
                            val selectedItem = inventoryItemsList.firstOrNull {
                                getItemPropertyString(it, "name", "itemName") == selectedName
                            }
                            if (selectedItem != null) {
                                val itemId = getItemPropertyString(selectedItem, "_id", "id", "itemId")
                                val name = getItemPropertyString(selectedItem, "name", "itemName").ifBlank { "-" }
                                val unit = getItemPropertyString(selectedItem, "unit", "uom").ifBlank { "pcs" }
                                val type = getItemPropertyString(selectedItem, "type", "itemType").ifBlank { "Goods" }
                                val rate = getItemPropertyDouble(selectedItem, "costPrice", "rate", "price")
                                val tax = getItemPropertyDouble(selectedItem, "taxPercentage", "taxPercent", "tax")

                                val currentQty = itemEntry.qty.ifBlank { "1" }
                                val rateStr = if (rate > 0) rate.toString() else "0"
                                val taxStr = if (tax > 0) tax.toString() else "0"

                                itemsList[index] = itemEntry.copy(
                                    itemId = itemId,
                                    name = name,
                                    unit = unit,
                                    type = type,
                                    qty = currentQty,
                                    rate = rateStr,
                                    taxPercent = taxStr,
                                    total = calculateItemTotal(currentQty, rateStr, taxStr)
                                )
                            }
                        },
                        isRequired = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Row 1: Type & Quantity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Type",
                                value = itemEntry.type.ifBlank { "-" },
                                expanded = activeTypeDropdownId == itemEntry.id,
                                onExpandChange = { isExpanded ->
                                    activeTypeDropdownId = if (isExpanded) itemEntry.id else null
                                },
                                options = typeOptions,
                                onOptionSelected = { selectedType ->
                                    itemsList[index] = itemEntry.copy(type = selectedType)
                                }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Qty(Req)")
                            FormTextField(
                                value = itemEntry.qty,
                                onValueChange = { newQty ->
                                    val totalStr = calculateItemTotal(newQty, itemEntry.rate, itemEntry.taxPercent)
                                    itemsList[index] = itemEntry.copy(qty = newQty, total = totalStr)
                                },
                                placeholder = "1",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Row 2: Unit & Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Unit")
                            FormTextField(
                                value = itemEntry.unit,
                                onValueChange = { newUnit ->
                                    itemsList[index] = itemEntry.copy(unit = newUnit)
                                },
                                placeholder = "-"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Rate")
                            FormTextField(
                                value = itemEntry.rate,
                                onValueChange = { newRate ->
                                    val totalStr = calculateItemTotal(itemEntry.qty, newRate, itemEntry.taxPercent)
                                    itemsList[index] = itemEntry.copy(rate = newRate, total = totalStr)
                                },
                                placeholder = "0.00",
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Row 3: Tax % & Calculated Total Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Tax %")
                            FormTextField(
                                value = itemEntry.taxPercent,
                                onValueChange = { newTax ->
                                    val totalStr = calculateItemTotal(itemEntry.qty, itemEntry.rate, newTax)
                                    itemsList[index] = itemEntry.copy(taxPercent = newTax, total = totalStr)
                                },
                                placeholder = "0",
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Total")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tokens.fieldHeight)
                                    .background(disabled.copy(alpha = 0.4f), RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .padding(horizontal = tokens.extraPadding),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = if (itemEntry.total.isNotBlank()) "₹ ${itemEntry.total}" else "-",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Item Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Primary,
                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .clickable { itemsList.add(LineItemFormEntry()) }
                .padding(vertical = tokens.extraPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Add Item",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }
        }
    }
}

private fun calculateItemTotal(qtyStr: String, rateStr: String, taxPercentStr: String): String {
    val qty = qtyStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val tax = taxPercentStr.toDoubleOrNull() ?: 0.0
    val subtotal = qty * rate
    val taxAmount = subtotal * (tax / 100.0)
    val total = subtotal + taxAmount
    return String.format(Locale.US, "%.2f", total)
}

private fun getItemPropertyString(item: Any, vararg candidateNames: String): String {
    for (prop in candidateNames) {
        try {
            val getterName = "get" + prop.replaceFirstChar { it.uppercase() }
            val method = item.javaClass.methods.firstOrNull {
                it.name.equals(getterName, ignoreCase = true) || it.name.equals(prop, ignoreCase = true)
            }
            val result = method?.invoke(item)?.toString()
            if (!result.isNullOrBlank()) return result
        } catch (_: Exception) {}
        try {
            val field = item.javaClass.declaredFields.firstOrNull { it.name.equals(prop, ignoreCase = true) }
            field?.isAccessible = true
            val result = field?.get(item)?.toString()
            if (!result.isNullOrBlank()) return result
        } catch (_: Exception) {}
    }
    return ""
}

private fun getItemPropertyDouble(item: Any, vararg candidateNames: String): Double {
    val str = getItemPropertyString(item, *candidateNames)
    return str.toDoubleOrNull() ?: 0.0
}