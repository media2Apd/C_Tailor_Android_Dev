package com.cuso.mobile.view.home.inventory.procurement.barcode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.GenerateBarcodeRequest
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay

@Composable
fun BarcodeGeneratorScreen(
    onClose: () -> Unit,
    onBarcodeGeneratedSuccessfully: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    // Observe inventory items and warehouses
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val warehouseDropdown by viewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingBarcode.collectAsStateWithLifecycle()
    val successMessage by viewModel.barcodeSuccessMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.barcodeErrorMessage.collectAsStateWithLifecycle()

    // Fetch initial datasets
    LaunchedEffect(Unit) {
        viewModel.fetchInventoryItems(limit = 100)
        viewModel.loadWarehouseDropdown()
    }

    // Product Selection State
    var selectedItemId by remember { mutableStateOf("") }
    var selectedWarehouseId by remember { mutableStateOf("") }
    var isProductDropdownExpanded by remember { mutableStateOf(false) }
    var isWarehouseDropdownExpanded by remember { mutableStateOf(false) }

    // Auto-select first product if available
    LaunchedEffect(inventoryItems) {
        if (selectedItemId.isBlank() && inventoryItems.isNotEmpty()) {
            selectedItemId = inventoryItems.first()._id
        }
    }

    // Auto-select first warehouse if available
    LaunchedEffect(warehouseDropdown) {
        if (selectedWarehouseId.isBlank() && warehouseDropdown.isNotEmpty()) {
            selectedWarehouseId = warehouseDropdown.first().value
        }
    }

    val selectedItem = remember(inventoryItems, selectedItemId) {
        inventoryItems.find { it._id == selectedItemId }
    }

    val selectedWarehouseName = remember(warehouseDropdown, selectedWarehouseId) {
        warehouseDropdown.find { it.value == selectedWarehouseId }?.label ?: "Select Warehouse"
    }

    // Barcode Configuration State
    var selectedBarcodeType by remember { mutableStateOf("EAN13") }
    var isBarcodeTypeExpanded by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf("1") }
    var selectedSize by remember { mutableStateOf("Medium (50x25mm)") }
    var isSizeDropdownExpanded by remember { mutableStateOf(false) }

    val displayProductName = selectedItem?.name ?: "Select a Product"
    val displaySku = selectedItem?.sku ?: "—"
    val displayPrice = selectedItem?.sellingPrice ?: 0.0

    LaunchedEffect(successMessage) {
        if (!successMessage.isNullOrBlank()) {
            delay(1200)
            viewModel.clearBarcodeAlerts()
            onBarcodeGeneratedSuccessfully()
            onClose()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Primary_background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                ) {
                    TitleBar("Barcode Generator", onClose)
                    HorizontalDivider(color = title_border)
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)
            ) {
                // ── 1. Product Selection ──
                item {
                    SectionCard(tokens = tokens, icon = R.drawable.box, title = "Product Selection") {
                        val productOptions = inventoryItems.map { it.name }

                        FormDropdown(
                            label = "Product Name",
                            value = displayProductName,
                            expanded = isProductDropdownExpanded,
                            onExpandChange = { isProductDropdownExpanded = it },
                            options = productOptions.ifEmpty { listOf("No Products Found") },
                            onOptionSelected = { chosenName ->
                                selectedItemId = inventoryItems.find { it.name == chosenName }?._id.orEmpty()
                            }
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        val warehouseOptions = warehouseDropdown.map { it.label }
                        FormDropdown(
                            label = "Warehouse",
                            value = selectedWarehouseName,
                            expanded = isWarehouseDropdownExpanded,
                            onExpandChange = { isWarehouseDropdownExpanded = it },
                            options = warehouseOptions.ifEmpty { listOf("No Warehouses Found") },
                            onOptionSelected = { chosenLabel ->
                                selectedWarehouseId = warehouseDropdown.find { it.label == chosenLabel }?.value.orEmpty()
                            }
                        )
                    }
                }

                // ── 2. Barcode Settings ──
                item {
                    SectionCard(tokens = tokens, icon = R.drawable.services, title = "Barcode Settings") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "Barcode Type",
                                    value = selectedBarcodeType,
                                    expanded = isBarcodeTypeExpanded,
                                    onExpandChange = { isBarcodeTypeExpanded = it },
                                    options = listOf("EAN13", "Code128", "UPC", "QR"),
                                    onOptionSelected = { selectedBarcodeType = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "Label Size",
                                    value = selectedSize,
                                    expanded = isSizeDropdownExpanded,
                                    onExpandChange = { isSizeDropdownExpanded = it },
                                    options = listOf(
                                        "Small (30x20mm)",
                                        "Medium (50x25mm)",
                                        "Large (75x50mm)"
                                    ),
                                    onOptionSelected = { selectedSize = it }
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("Print Quantity")
                        FormTextField(
                            value = quantityText,
                            onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                            placeholder = "1",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                // ── 3. Live Preview ──
                item {
                    SectionCard(tokens = tokens, icon = R.drawable.ic_eye, title = "Live Preview") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Primary_background, shape = RoundedCornerShape(tokens.cardCornerRadius)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val stroke = Stroke(
                                    width = 1.2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                                drawRoundRect(
                                    color = Color(0xFFCBD5E1),
                                    style = stroke,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(tokens.cardCornerRadius.toPx())
                                )
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .padding(tokens.screenPadding),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(tokens.screenPadding),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = displayProductName,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = title_color
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "SKU: $displaySku",
                                        fontSize = tokens.label,
                                        color = mutedText
                                    )

                                    Spacer(Modifier.height(tokens.extraPadding))
                                    HorizontalDivider(color = grey_border)
                                    Spacer(Modifier.height(tokens.extraPadding))

                                    BarcodeGraphic(
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(75.dp)
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        text = "AUTO-GENERATED ON SAVE",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp,
                                        color = mutedText
                                    )

                                    Spacer(Modifier.height(tokens.extraPadding * 1.2f))
                                    HorizontalDivider(color = grey_border)
                                    Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("PRICE", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.SemiBold)
                                        Text("₹${"%.2f".format(displayPrice)}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Actual print may vary based on label size",
                            fontSize = tokens.caption,
                            color = mutedText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // ── 4. Bottom Action Buttons ──
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Button(
                            onClick = {
                                if (selectedItemId.isNotBlank()) {
                                    val req = GenerateBarcodeRequest(
                                        itemId = selectedItemId,
                                        warehouseId = selectedWarehouseId.takeIf { it.isNotBlank() },
                                        barcodeType = selectedBarcodeType,
                                        labelSize = selectedSize,
                                        quantity = quantityText.toIntOrNull() ?: 1
                                    )
                                    viewModel.generateBarcode(req)
                                }
                            },
                            enabled = !isSubmitting && selectedItemId.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(tokens.iconSize))
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.ic_qr),
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                                Text("Generate Barcode", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = whiteBg)
                            }
                        }

                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(containerColor = modelGray),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                        ) {
                            Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                        }
                    }
                }
            }
        }

        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { viewModel.clearBarcodeAlerts() }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { viewModel.clearBarcodeAlerts() }
        )
    }
}

@Composable
private fun SectionCard(
    tokens: AppDesignTokens,
    icon: Int,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 1.5f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(icon), contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize * 0.9f))
                }
                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                Text(text = title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
            }
            Spacer(Modifier.height(tokens.extraPadding * 1.4f))
            content()
        }
    }
}

@Composable
private fun BarcodeGraphic(
    modifier: Modifier = Modifier,
    barColor: Color = Color.Black
) {
    val barPattern = remember {
        listOf(
            2, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 3, 1, 1, 1, 2, 1, 2, 1, 1, 1, 3, 1, 1, 2,
            1, 1, 2, 1, 1, 2, 2, 1, 1, 1, 2, 1, 1, 3, 1, 1, 2, 1, 1, 1, 2, 1, 3, 1, 1, 1,
            2, 2, 1, 1, 2, 1, 1, 3, 1, 1, 2, 1, 1, 1, 3, 1, 2, 1, 1, 1, 2, 2, 1, 1, 3, 3
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalUnits = barPattern.sum().toFloat()
            val unitWidth = size.width / totalUnits
            var currentX = 0f

            barPattern.forEachIndexed { index, widthUnits ->
                val isBar = index % 2 == 0
                val barWidth = widthUnits * unitWidth

                if (isBar) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(currentX, 0f),
                        size = androidx.compose.ui.geometry.Size(barWidth, size.height)
                    )
                }
                currentX += barWidth
            }
        }
    }
}