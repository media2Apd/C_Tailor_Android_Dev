@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.mobile.view.home.inventory.items.all_items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.light_grey
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet

private val PurplePrimary = Primary
private val PurpleLight = Color(0xFFF3F1FE)
private val BorderGray = grey_border
private val TextGray = Color(0xFF8A93A6)
private val TextDark = Color(0xFF111827)

enum class AdjustmentType(val label: String) {
    INCREASE("Increase Stock"),
    DECREASE("Decrease Stock"),
    SET_EXACT("Set Exact Quantity")
}

@Composable
fun AdjustStockSheet(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onBlurScrimChange: (Dp, Float) -> Unit = { _, _ -> },
    onSubmit: (type: AdjustmentType, quantity: Double, reason: String, notes: String) -> Unit
) {
    val tokens = LocalAppTokens.current

    var sheetState by remember { mutableStateOf(SheetValue.Collapsed) }

    var adjustmentType by remember { mutableStateOf(AdjustmentType.INCREASE) }
    var quantityText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reasonExpanded by remember { mutableStateOf(false) }

    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val signedAdjustment = when (adjustmentType) {
        AdjustmentType.INCREASE -> quantity
        AdjustmentType.DECREASE -> -quantity
        AdjustmentType.SET_EXACT -> quantity - item.currentStock
    }
    val newBalance = when (adjustmentType) {
        AdjustmentType.SET_EXACT -> quantity
        else -> (item.currentStock + signedAdjustment).coerceAtLeast(0.0)
    }

    val reasonOptions = listOf("Damaged Goods", "Inventory Correction", "Production Loss")

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = { newState ->
            sheetState = newState
            if (newState == SheetValue.Hidden) {
                onDismiss()
            }
        },
        collapsedFraction = 0.55f, // Starts at half-page (~55%)
        topInset = 5.dp,
        onDismissRequest = onDismiss,
        onBlurScrimChange = onBlurScrimChange,
        sheetBackgroundColor = Primary_background,
        scrollableContent = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
                .padding(bottom = tokens.screenPadding)
        ) {
            Spacer(Modifier.padding(top = 10.dp))
            Text(
                text = "ADJUST STOCK",
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(tokens.extraPadding * 2))

            Text("Adjustment Type", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(Modifier.height(tokens.extraPadding))

            AdjustmentType.entries.forEach { type ->
                AdjustmentTypeOption(
                    label = type.label,
                    selected = adjustmentType == type,
                    onClick = { adjustmentType = type }
                )
                Spacer(Modifier.height(tokens.extraPadding))
            }

            Spacer(Modifier.height(tokens.extraPadding / 2))
            Text("Quantity", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(Modifier.height(tokens.extraPadding - 2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.fieldHeight)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = tokens.extraPadding),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (quantityText.isEmpty()) {
                        Text("0", color = Color(0xFF9CA3AF), fontSize = tokens.bodyMedium)
                    }
                    BasicTextField(
                        value = quantityText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) quantityText = input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(
                            fontSize = tokens.bodyMedium,
                            color = Color(0xFF111827)
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(light_grey),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item.unit,
                        modifier = Modifier.padding(horizontal = tokens.extraPadding),
                        color = blackTitle,
                        fontSize = tokens.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding + 6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(PurpleLight)
                    .padding(vertical = tokens.extraPadding + 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric("Current", formatQty(item.currentStock))
                SummaryMetric("Adjustment", (if (signedAdjustment >= 0) "+" else "") + formatQty(signedAdjustment))
                SummaryMetric("New Balance", "${formatQty(newBalance)} ${item.unit}", valueColor = PurplePrimary)
            }

            Spacer(Modifier.height(tokens.extraPadding * 2))
            FormDropdown(
                label = "Adjustment Reason",
                value = reason.ifEmpty { "Select an option" },
                expanded = reasonExpanded,
                onExpandChange = { reasonExpanded = it },
                options = reasonOptions,
                onOptionSelected = { reason = it }
            )

            Spacer(Modifier.height(tokens.extraPadding * 2))
            Text("Internal Notes", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(Modifier.height(tokens.extraPadding - 2.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = {
                    Text("Enter optional notes for audit trail...", color = Color(0xFF9CA3AF), fontSize = tokens.bodySmall)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp), // multiline field, kept fixed since no dedicated textarea token
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderGray,
                    focusedBorderColor = PurplePrimary
                )
            )

            Spacer(Modifier.height(tokens.extraPadding * 2 + 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding + 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    border = BorderStroke(1.dp, TextSecondary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = whiteBg)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium, color = TextSecondary)
                }

                Button(
                    onClick = { onSubmit(adjustmentType, quantity, reason, notes) },
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Adjust Stock", fontWeight = FontWeight.Medium, color = whiteBg)
                }
            }
        }
    }
}

@Composable
private fun AdjustmentTypeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .border(1.dp, if (selected) PurplePrimary else BorderGray, RoundedCornerShape(tokens.cardCornerRadius))
            .clickable { onClick() }
            .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(tokens.iconSize)
                .clip(CircleShape)
                .border(1.5.dp, if (selected) PurplePrimary else Color(0xFFD1D5DB), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize / 2)
                        .clip(CircleShape)
                        .background(PurplePrimary)
                )
            }
        }
        Spacer(Modifier.width(tokens.extraPadding))
        Text(label, fontSize = tokens.bodyMedium, color = TextDark)
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, valueColor: Color = TextDark) {
    val tokens = LocalAppTokens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = tokens.caption, color = TextGray)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

private fun formatQty(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}