package com.cuso.mobile.view.home.inventory

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.view.home.FormDropdown

private val PurplePrimary = Color(0xFF5A4FE0)
private val PurpleLight = Color(0xFFF3F1FE)
private val BorderGray = Color(0xFFE5E7EB)
private val TextGray = Color(0xFF8A93A6)
private val TextDark = Color(0xFF111827)

enum class AdjustmentType(val label: String) {
    INCREASE("Increase Stock"),
    DECREASE("Decrease Stock"),
    SET_EXACT("Set Exact Quantity")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustStockSheet(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onSubmit: (type: AdjustmentType, quantity: Double, reason: String, notes: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFD1D5DB))
        ) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            Text(
                "ADJUST STOCK",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            Text("Adjustment Type", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(Modifier.height(10.dp))

            AdjustmentType.entries.forEach { type ->
                AdjustmentTypeOption(
                    label = type.label,
                    selected = adjustmentType == type,
                    onClick = { adjustmentType = type }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(6.dp))
            Text("Quantity", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, BorderGray, RoundedCornerShape(10.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (quantityText.isEmpty()) {
                        Text("0", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = quantityText,
                        onValueChange = { input -> if (input.all { it.isDigit() || it == '.' }) quantityText = input },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF111827)
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.unit, modifier = Modifier.padding(horizontal = 14.dp), color = Color.Black, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PurpleLight)
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric("Current", formatQty(item.currentStock))
                SummaryMetric("Adjustment", (if (signedAdjustment >= 0) "+" else "") + formatQty(signedAdjustment))
                SummaryMetric("New Balance", "${formatQty(newBalance)} ${item.unit}", valueColor = PurplePrimary)
            }

            Spacer(Modifier.height(20.dp))
            FormDropdown(
                label = "Adjustment Reason",
                value = reason.ifEmpty { "Select an option" },
                expanded = reasonExpanded,
                onExpandChange = { reasonExpanded = it },
                options = reasonOptions,
                onOptionSelected = { reason = it }
            )

            Spacer(Modifier.height(20.dp))
            Text("Internal Notes", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Enter optional notes for audit trail...", color = Color(0xFF9CA3AF), fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderGray,
                    focusedBorderColor = PurplePrimary
                )
            )

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { onSubmit(adjustmentType, quantity, reason, notes) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Adjust Stock", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun AdjustmentTypeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, if (selected) PurplePrimary else BorderGray, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(1.5.dp, if (selected) PurplePrimary else Color(0xFFD1D5DB), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 14.sp, color = TextDark)
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, valueColor: Color = TextDark) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = TextGray)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

private fun formatQty(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}