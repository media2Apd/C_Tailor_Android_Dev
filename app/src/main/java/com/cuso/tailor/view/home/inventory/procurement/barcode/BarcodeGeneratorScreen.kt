package com.cuso.tailor.view.home.inventory.procurement.barcode

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.GenerateBarcodeRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.delay

@Composable
fun BarcodeGeneratorScreen(
    barcodeId: String? = null,
    onClose: () -> Unit,
    onBarcodeGeneratedSuccessfully: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val isViewMode = !barcodeId.isNullOrBlank()

    // Observe inventory items, warehouses, and single barcode detail
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val warehouseDropdown by viewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val selectedBarcodeDoc by viewModel.selectedBarcode.collectAsStateWithLifecycle()
    val isLoadingBarcodes by viewModel.isLoadingBarcodes.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingBarcode.collectAsStateWithLifecycle()
    val successMessage by viewModel.barcodeSuccessMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.barcodeErrorMessage.collectAsStateWithLifecycle()

    // Load initial item and warehouse dropdowns
    LaunchedEffect(Unit) {
        viewModel.fetchInventoryItems(limit = 100)
        viewModel.loadWarehouseDropdown()
    }

    // Fetch barcode detail if an ID is passed from navigation
    LaunchedEffect(barcodeId) {
        if (!barcodeId.isNullOrBlank()) {
            viewModel.fetchBarcodeViewOne(barcodeId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedBarcode()
        }
    }

    // Product & Warehouse selection states
    var selectedItemId by remember { mutableStateOf("") }
    var selectedWarehouseId by remember { mutableStateOf("") }
    var isProductDropdownExpanded by remember { mutableStateOf(false) }
    var isWarehouseDropdownExpanded by remember { mutableStateOf(false) }

    // Barcode settings states
    var selectedBarcodeType by remember { mutableStateOf("QR") }
    var isBarcodeTypeExpanded by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf("1") }
    var selectedSize by remember { mutableStateOf("Medium (50x25mm)") }
    var isSizeDropdownExpanded by remember { mutableStateOf(false) }

    // Auto-select first item when inventory loads (only in create mode)
    LaunchedEffect(inventoryItems) {
        if (!isViewMode && selectedItemId.isBlank() && inventoryItems.isNotEmpty()) {
            selectedItemId = inventoryItems.first()._id
        }
    }

    // Auto-select first warehouse when dropdown loads (only in create mode)
    LaunchedEffect(warehouseDropdown) {
        if (!isViewMode && selectedWarehouseId.isBlank() && warehouseDropdown.isNotEmpty()) {
            selectedWarehouseId = warehouseDropdown.first().value
        }
    }

    // Pre-populate fields when viewing an existing barcode document
    LaunchedEffect(selectedBarcodeDoc) {
        selectedBarcodeDoc?.let { doc ->
            val associatedItemId = doc.item?.id
            if (!associatedItemId.isNullOrBlank()) {
                selectedItemId = associatedItemId
            }
            val associatedWarehouseId = doc.warehouse?.id
            if (!associatedWarehouseId.isNullOrBlank()) {
                selectedWarehouseId = associatedWarehouseId
            }
            if (doc.barcodeType.isNotBlank()) {
                selectedBarcodeType = doc.barcodeType
            }
            if (doc.labelSize.isNotBlank()) {
                selectedSize = doc.labelSize
            }
        }
    }

    val selectedItem = remember(inventoryItems, selectedItemId) {
        inventoryItems.find { it._id == selectedItemId }
    }

    val selectedWarehouseName = remember(warehouseDropdown, selectedWarehouseId) {
        warehouseDropdown.find { it.value == selectedWarehouseId }?.label ?: "Select Warehouse"
    }

    val displayProductName = selectedItem?.name
        ?: selectedBarcodeDoc?.item?.name
        ?: "Select a Product"
    val displaySku = selectedItem?.sku
        ?: selectedBarcodeDoc?.sku
        ?: selectedBarcodeDoc?.item?.sku
        ?: "—"
    val displayPrice = selectedItem?.sellingPrice ?: 0.0

    val variantLabel = remember(selectedItem, selectedBarcodeDoc) {
        extractProperty(selectedItem, "variantLabel", "variant", "sizeColor").ifBlank {
            selectedBarcodeDoc?.item?.variantLabel.orEmpty()
        }
    }

    // Resolve barcode payload using the fetched barcode document or selected item
    val (rawBarcodePayload, formattedBarcodeDisplay) = remember(
        selectedItem,
        selectedBarcodeDoc,
        selectedBarcodeType
    ) {
        resolveBarcodePayload(
            item = selectedItem,
            barcodeDoc = selectedBarcodeDoc,
            barcodeType = selectedBarcodeType
        )
    }

    // Generate preview bitmap
    val barcodeBitmap = remember(rawBarcodePayload, selectedBarcodeType, selectedSize) {
        generateBarcodeBitmap(
            content = rawBarcodePayload,
            type = selectedBarcodeType,
            labelSize = selectedSize
        )
    }

    // Handle submission completion
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
                    TitleBar(
                        title = if (isViewMode) "View/Reprint Barcode" else "Barcode Generator",
                        onClose = onClose
                    )
                    HorizontalDivider(color = title_border)
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = tokens.screenPadding,
                    top = tokens.screenPadding,
                    end = tokens.screenPadding,
                    bottom = tokens.screenPadding + 110.dp
                ),
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
                            options = productOptions.ifEmpty { listOf(displayProductName) },
                            enabled = !isViewMode,
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
                            enabled = true,
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
                                    options = listOf("QR", "EAN13", "Code128", "UPC"),
                                    enabled = !isViewMode,
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
                                    enabled = true,
                                    onOptionSelected = { selectedSize = it }
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel(if (isViewMode) "Reprint Quantity" else "Print Quantity")
                        FormTextField(
                            value = quantityText,
                            onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                            placeholder = "1",
                            keyboardType = KeyboardType.Number,
                            enabled = true
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
                                    color = grey_border,
                                    style = stroke,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(tokens.cardCornerRadius.toPx())
                                )
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .padding(tokens.screenPadding),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp)
                                ) {
                                    Text(
                                        text = displayProductName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = buildString {
                                            append("SKU: $displaySku")
                                            if (variantLabel.isNotBlank()) append(" | $variantLabel")
                                        },
                                        fontSize = 12.5.sp,
                                        color = TextSecondary
                                    )

                                    Spacer(Modifier.height(14.dp))
                                    HorizontalDivider(color = grey_border, thickness = 1.dp)
                                    Spacer(Modifier.height(14.dp))

                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLoadingBarcodes) {
                                            Box(
                                                modifier = Modifier.height(100.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Primary)
                                            }
                                        } else if (selectedBarcodeType.equals("QR", ignoreCase = true)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(130.dp)
                                                    .background(whiteBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (barcodeBitmap != null) {
                                                    Image(
                                                        bitmap = barcodeBitmap.asImageBitmap(),
                                                        contentDescription = "QR Code",
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                if (barcodeBitmap != null) {
                                                    Image(
                                                        bitmap = barcodeBitmap.asImageBitmap(),
                                                        contentDescription = "Barcode",
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.85f)
                                                            .height(76.dp)
                                                    )
                                                }

                                                Spacer(Modifier.height(8.dp))

                                                Text(
                                                    text = formattedBarcodeDisplay,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = TextPrimary,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))
                                    HorizontalDivider(color = grey_border, thickness = 1.dp)
                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "PRICE",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = mutedText,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "₹${"%.2f".format(displayPrice)}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Scannable barcode preview • Output matches standard scanners",
                            fontSize = tokens.caption,
                            color = mutedText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
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
                                val targetItemId = selectedItemId.ifBlank {
                                    inventoryItems.firstOrNull()?._id.orEmpty()
                                }
                                val targetWarehouseId = selectedWarehouseId.ifBlank {
                                    warehouseDropdown.firstOrNull()?.value.orEmpty()
                                }
                                val targetQuantity = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1

                                if (targetItemId.isNotBlank()) {
                                    val request = GenerateBarcodeRequest(
                                        itemId = targetItemId,
                                        warehouseId = targetWarehouseId.takeIf { it.isNotBlank() },
                                        barcodeType = selectedBarcodeType,
                                        labelSize = selectedSize,
                                        quantity = targetQuantity
                                    )
                                    viewModel.generateBarcode(request)
                                }
                            },
                            enabled = !isSubmitting && (selectedItemId.isNotBlank() || inventoryItems.isNotEmpty()),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                        ) {
                            if (isSubmitting) {
                                CirculerProgressIndicatorSmall()
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.ic_qr),
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                                Text(
                                    text = if (isViewMode) "Reprint Barcode" else "Generate Barcode",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = whiteBg
                                )
                            }
                        }

                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(containerColor = whiteBg),
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
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = successMessage,
            onDismiss = { viewModel.clearBarcodeAlerts() }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMessage?.takeIf { !isLoadingBarcodes },
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
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize * 0.9f)
                    )
                }
                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                Text(text = title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
            }
            Spacer(Modifier.height(tokens.extraPadding * 1.4f))
            content()
        }
    }
}

private fun resolveBarcodePayload(
    item: Any?,
    barcodeDoc: Any?,
    barcodeType: String
): Pair<String, String> {
    var rawNumber = extractProperty(barcodeDoc, "barcodeNumber", "barcode")

    if (rawNumber.isBlank()) {
        rawNumber = extractProperty(item, "barcodeNumber", "barcode")
    }
    if (rawNumber.isBlank()) {
        val nestedItemId = extractPropertyObject(item, "itemId")
        if (nestedItemId != null) {
            rawNumber = extractProperty(nestedItemId, "barcodeNumber", "barcode")
        }
    }

    if (rawNumber.isBlank()) {
        val id = extractProperty(item, "_id", "id")
        val sku = extractProperty(item, "sku")
        val digitsFromSource = (sku.filter { it.isDigit() } + id.filter { it.isDigit() }).ifBlank { "20" }
        rawNumber = ("890" + digitsFromSource.padStart(8, '0')).take(11)
    }

    return when (barcodeType.uppercase()) {
        "QR" -> {
            Pair(rawNumber, rawNumber)
        }

        "CODE128" -> {
            val clean = rawNumber.filter { it.isLetterOrDigit() || it == '-' }
            Pair(clean, clean)
        }

        "EAN13" -> {
            val digitsOnly = rawNumber.filter { it.isDigit() }
            val base12 = digitsOnly.padEnd(12, '0').take(12)
            val checksum = calculateEan13Checksum(base12)
            val full13 = "$base12$checksum"
            val display = "${full13[0]}   ${full13.substring(1, 7)}   ${full13.substring(7)}"
            Pair(full13, display)
        }

        "UPC" -> {
            val digitsOnly = rawNumber.filter { it.isDigit() }
            val base11 = digitsOnly.padEnd(11, '0').take(11)
            val checksum = calculateUpcAChecksum(base11)
            val full12 = "$base11$checksum"
            val display = "${full12[0]}   ${full12.substring(1, 6)}   ${full12.substring(6, 11)}   ${full12.last()}"
            Pair(full12, display)
        }

        else -> Pair(rawNumber, rawNumber)
    }
}

private fun calculateEan13Checksum(first12: String): Int {
    var sum = 0
    for (i in 0 until 12) {
        val digit = first12[i].digitToInt()
        sum += if (i % 2 == 0) digit else digit * 3
    }
    val remainder = sum % 10
    return if (remainder == 0) 0 else 10 - remainder
}

private fun calculateUpcAChecksum(first11: String): Int {
    var sum = 0
    for (i in 0 until 11) {
        val digit = first11[i].digitToInt()
        sum += if (i % 2 == 0) digit * 3 else digit
    }
    val remainder = sum % 10
    return if (remainder == 0) 0 else 10 - remainder
}

private fun generateBarcodeBitmap(
    content: String,
    type: String,
    @Suppress("UNUSED_PARAMETER") labelSize: String = ""
): Bitmap? {
    if (content.isBlank()) return null

    val (format, width, height) = when (type.uppercase()) {
        "QR" -> Triple(BarcodeFormat.QR_CODE, 512, 512)
        "EAN13" -> Triple(BarcodeFormat.EAN_13, 600, 220)
        "UPC" -> Triple(BarcodeFormat.UPC_A, 600, 220)
        "CODE128" -> Triple(BarcodeFormat.CODE_128, 600, 200)
        else -> Triple(BarcodeFormat.CODE_128, 600, 200)
    }

    val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.CHARACTER_SET to "UTF-8"
    )

    return try {
        encodeToBitmap(content, format, width, height, hints)
    } catch (_: Exception) {
        try {
            encodeToBitmap(content, BarcodeFormat.CODE_128, width, height, hints)
        } catch (_: Exception) {
            null
        }
    }
}

private fun encodeToBitmap(
    content: String,
    format: BarcodeFormat,
    width: Int,
    height: Int,
    hints: Map<EncodeHintType, Any>
): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, format, width, height, hints)
    val matrixWidth = bitMatrix.width
    val matrixHeight = bitMatrix.height
    val pixels = IntArray(matrixWidth * matrixHeight)

    for (y in 0 until matrixHeight) {
        val offset = y * matrixWidth
        for (x in 0 until matrixWidth) {
            pixels[offset + x] = if (bitMatrix.get(x, y)) {
                android.graphics.Color.BLACK
            } else {
                android.graphics.Color.WHITE
            }
        }
    }

    val bitmap = createBitmap(matrixWidth, matrixHeight)
    bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
    return bitmap
}

private fun extractProperty(item: Any?, vararg candidateNames: String): String {
    if (item == null) return ""
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

private fun extractPropertyObject(item: Any?, propName: String): Any? {
    if (item == null) return null
    try {
        val getterName = "get" + propName.replaceFirstChar { it.uppercase() }
        val method = item.javaClass.methods.firstOrNull {
            it.name.equals(getterName, ignoreCase = true) || it.name.equals(propName, ignoreCase = true)
        }
        val result = method?.invoke(item)
        if (result != null) return result
    } catch (_: Exception) {}
    try {
        val field = item.javaClass.declaredFields.firstOrNull { it.name.equals(propName, ignoreCase = true) }
        field?.isAccessible = true
        return field?.get(item)
    } catch (_: Exception) {}
    return null
}