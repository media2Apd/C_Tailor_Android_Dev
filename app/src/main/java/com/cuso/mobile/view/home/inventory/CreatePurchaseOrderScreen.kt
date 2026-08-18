package com.cuso.mobile.view.home.inventory

// ─────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.modelGray
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AccordionSection

// ── Reused from HomeScreen.kt (com.cuso.mobile.view.home) ──
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField

// ── Reused date picker (same one HR uses) ──
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.TitleBar

// ── Your existing DataCard system ──
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.SegmentedSelector
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TrailingFabAction

// ── Design tokens (colors only — sizing now comes from AppDesignTokens) ──
private val AccentColor = Color(0xFF3D3DFF)
private val BorderColor = Color(0xFFE3E4E8)
private val TitleColor = Color(0xFF111827)
private val LabelColor = Color(0xFF8A8A99)

// ─────────────────────────────────────────────
// Priority enum
// ─────────────────────────────────────────────
enum class PoPriority { NORMAL, URGENT, CRITICAL }


// ─────────────────────────────────────────────
// PurchaseOrderHeaderCard — DataCard for read-only info
// + gauge attached below (gauge isn't a DataCard slot)
// ─────────────────────────────────────────────
@Composable
fun PurchaseOrderHeaderCard(
    code: String,
    name: String,
    stockQty: String,
    variant: String,
    category: String,
    reorderLevel: String,
    suggestedQty: String,
    utilizationPercent: Int
) {
    val tokens = LocalAppTokens.current
    Column {
        DataCard(
            item = code,
            title = "$code  ·  $name  ·  $stockQty",
            topBadgeText = "Low",
            topBadgeTextColor = Color(0xFFE53935),
            topBadgeBgColor = Color(0xFFFDE7E7),
            topBadgeInline = true,
            footerAsRows = true,
            footerFields = listOf(
                DataCardField(label = "Variant", text = variant),
                DataCardField(label = "Category", text = category),
                DataCardField(label = "Reorder Level", text = reorderLevel, textColor = AccentColor),
                DataCardField(label = "Suggested Qty", text = suggestedQty)
            )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.cardPadding * 0.5f, vertical = tokens.cardPadding * 0.35f)
                .background(Color.Transparent)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Stock Utilization Gauge", fontSize = tokens.caption, color = LabelColor)
                Text("$utilizationPercent% CAPACITY", fontSize = tokens.caption, color = Color(0xFFE53935))
            }
            Spacer(Modifier.height(6.dp))
            StockUtilizationGauge(percentage = utilizationPercent / 100f)        }
    }
}

// ─────────────────────────────────────────────
// CreatePurchaseOrderScreen — full screen, now using
// FormLabel / FormTextField / FormDropdown from HomeScreen.kt
// ─────────────────────────────────────────────
@Composable
fun CreatePurchaseOrderScreen(
    onClose: () -> Unit,
    onCancel: () -> Unit,
    onCreateOrder: () -> Unit
) {
    val tokens = LocalAppTokens.current

    var expandedSection by remember { mutableStateOf("supplier") }

    // ── Supplier & Warehouse ──
    var supplier by remember { mutableStateOf("Global Textile Corp") }
    var supplierExpanded by remember { mutableStateOf(false) }
    val supplierOptions = listOf("Global Textile Corp", "Sunrise Fabrics", "Premium Weavers Co.")

    var warehouse by remember { mutableStateOf("Factory Warehouse (Primary)") }
    var warehouseExpanded by remember { mutableStateOf(false) }
    val warehouseOptions = listOf("Factory Warehouse (Primary)", "Retail Warehouse", "Cold Storage Unit")

    // ── Purchase Details ──
    var reorderQty by remember { mutableStateOf("200") }
    var unitPrice by remember { mutableStateOf("200") }

    // ── Delivery ──
    var expectedDelivery by remember { mutableStateOf(" ") }
    var priority by remember { mutableStateOf(PoPriority.URGENT) }
    var notes by remember { mutableStateOf("") }

    val totalOrderValue = (reorderQty.toIntOrNull() ?: 0) * (unitPrice.toIntOrNull() ?: 0)

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    TitleBar("Create Purchase Order", onClose = onClose)

                }
            }
        },
        bottomBar = {
            Row(
                Modifier.background(modelGray)
                    .fillMaxWidth()
            ) {
                StepNavigationFab(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight * 1.65f),
                    showBack = true,
                    onBack = onCancel,
                    backLabel = "Cancel",
                    backWidthFraction = 0.46f,
                    trailingAction = TrailingFabAction.Next(
                        label = "Create Order",
                        onClick = onCreateOrder
                    ),
                    trailingWidthFraction = 0.46f
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(bottom = 10.dp)
                .verticalScroll(rememberScrollState())
                .background(Color.Transparent)
        ) {
            PurchaseOrderHeaderCard(
                code = "FAB-ITL-220",
                name = "Linen Shirt Fabric",
                stockQty = "40M",
                variant = "Blue",
                category = "Premium Fabric",
                reorderLevel = "100M",
                suggestedQty = "200M",
                utilizationPercent = 40
            )
            Spacer(Modifier.height(tokens.screenPadding * 0.8f))

            // ── Supplier & Warehouse ──
            AccordionSection(
                icon = Icons.Filled.Business,
                title = "Supplier & Warehouse",
                expanded = expandedSection == "supplier",
                onHeaderClick = { expandedSection = if (expandedSection == "supplier") "" else "supplier" }
            ) {
                FormDropdown(
                    label = "Supplier",
                    value = supplier,
                    expanded = supplierExpanded,
                    onExpandChange = { supplierExpanded = it },
                    options = supplierOptions,
                    onOptionSelected = { supplier = it }
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                FormDropdown(
                    label = "Warehouse",
                    value = warehouse,
                    expanded = warehouseExpanded,
                    onExpandChange = { warehouseExpanded = it },
                    options = warehouseOptions,
                    onOptionSelected = { warehouse = it }
                )

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = LabelColor, modifier = Modifier.size(tokens.iconSize * 0.9f))
                    Spacer(Modifier.width(4.dp))
                    Text("Mumbai   ★ 4.8   Avg 5 days", fontSize = tokens.caption, color = LabelColor)
                }
            }
            Spacer(Modifier.height(tokens.screenPadding * 0.6f))

            // ── Purchase Details ──
            AccordionSection(
                icon = Icons.Filled.ShoppingCart,
                title = "Purchase Details",
                expanded = expandedSection == "purchase",
                onHeaderClick = { expandedSection = if (expandedSection == "purchase") "" else "purchase" }
            ) {
                FormLabel("Reorder Quantity (Metres)")
                FormTextField(
                    value = reorderQty,
                    onValueChange = { reorderQty = it.filter { c -> c.isDigit() } },
                    placeholder = "Enter reorder quantity",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                FormLabel("Unit Price (₹)")
                FormTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it.filter { c -> c.isDigit() } },
                    placeholder = "Enter unit price",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.45f))
                        .background(Color(0xFFEDEDFB))
                        .padding(tokens.cardPadding * 0.4f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                "Total Order Value",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentColor
                            )
                            Text(
                                "Excluding Taxes & Shipping",
                                fontSize = tokens.label,
                                color = LabelColor
                            )
                        }
                        Text(
                            "₹${"%,d".format(totalOrderValue)}",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = AccentColor
                        )
                    }
                }
            }
            Spacer(Modifier.height(tokens.screenPadding * 0.6f))

            // ── Delivery ──
            AccordionSection(
                icon = Icons.Filled.LocalShipping,
                title = "Delivery",
                expanded = expandedSection == "delivery",
                onHeaderClick = { expandedSection = if (expandedSection == "delivery") "" else "delivery" }
            ) {
                FormLabel("Expected Delivery")
                DatePickerField(
                    value = expectedDelivery,
                    onDateSelected = { expectedDelivery = it }
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                FormLabel("Priority")
                SegmentedSelector(
                    options = PoPriority.entries,
                    selected = priority,
                    onSelect = { priority = it },
                    label = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    accentColor = AccentColor,
                    borderColor = BorderColor,
                    unselectedTextColor = TitleColor
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.8f))

            }
            Spacer(Modifier.height(tokens.screenPadding * 0.6f))
            Column(
                Modifier.fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ){
                FormLabel("Additional Notes")
                FormTextArea(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Specific packaging or handling requirements..."
                )
                Spacer(Modifier.height(12.dp))


            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding) //   NEW — match AccordionSection's horizontal inset
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.45f))
                    .border(1.dp, AccentColor, RoundedCornerShape(tokens.cardCornerRadius * 0.45f))
                    .padding(tokens.cardPadding * 0.85f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = LabelColor, modifier = Modifier.size(tokens.iconSize * 1.2f))
                    Text("Drag & Drop specification sheets", fontSize = tokens.bodySmall, color = LabelColor)
                    Text("Max 5MB · PDF, JPG, PNG", fontSize = tokens.label, color = Color(0xFFB0B0B0))
                }
            }
        }
    }
}

@Composable
fun FormTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    minLines: Int = 4,
    maxLines: Int = 6,
    borderColor: Color = BorderColor,
    focusedBorderColor: Color = AccentColor,
    textColor: Color = TitleColor,
    placeholderColor: Color = LabelColor
) {
    val tokens = LocalAppTokens.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = placeholderColor, fontSize = tokens.bodySmall) },
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = tokens.bodyMedium, color = textColor),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = borderColor,
            focusedBorderColor = focusedBorderColor,
            focusedContainerColor = whiteBg,
            unfocusedContainerColor = whiteBg
        ),
        modifier = modifier.fillMaxWidth()
    )
}