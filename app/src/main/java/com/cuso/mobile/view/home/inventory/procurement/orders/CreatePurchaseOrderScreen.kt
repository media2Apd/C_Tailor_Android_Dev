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

package com.cuso.mobile.view.home.inventory.procurement.orders

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.inventory.items.all_items.StatusBadge
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

private val AccentColor = Color(0xFF3D3DFF)
private val BorderColor = Color(0xFFE3E4E8)
private val TitleColor = Color(0xFF111827)
private val LabelColor = Color(0xFF8A8A99)

enum class PoPriority { NORMAL, URGENT, CRITICAL }

@Composable
fun PurchaseOrderHeaderCard(
    code: String,
    name: String,
    stockQty: String,
    variant: String,
    category: String,
    reorderLevel: String,
    suggestedQty: String,
    utilizationPercent: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F2F4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Code, Name, Stock Quantity and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = code,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E2238)
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        color = Color(0xFF8B8FA3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = stockQty,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFE53935)
                    )
                }

                Spacer(Modifier.width(8.dp))

                StatusBadge(
                    text = "Low",
                    bgColor = Color(0xFFFDE7E7),
                    textColor = Color(0xFFE53935),
                    showDot = false,
                    cornerRadius = 20.dp
                )
            }

            // Metrics with Vertical Dividers
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderStatColumn(
                    label = "Variant",
                    value = variant,
                    valueColor = Color(0xFF1E2238),
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(horizontal = 4.dp),
                    color = grey_border,
                    thickness = 1.dp
                )

                HeaderStatColumn(
                    label = "Category",
                    value = category,
                    valueColor = Color(0xFF1E2238),
                    modifier = Modifier.weight(1.3f)
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(horizontal = 4.dp),
                    color = grey_border,
                    thickness = 1.dp
                )

                HeaderStatColumn(
                    label = "Reorder Level",
                    value = reorderLevel,
                    valueColor = Color(0xFF3B38D6),
                    modifier = Modifier.weight(1.2f)
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(horizontal = 4.dp),
                    color = grey_border,
                    thickness = 1.dp
                )

                HeaderStatColumn(
                    label = "Suggested Qty",
                    value = suggestedQty,
                    valueColor = Color(0xFF1E2238),
                    modifier = Modifier.weight(1.2f)
                )
            }

            // Stock Utilization Progress Gauge
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock Utilization Gauge",
                        fontSize = 13.sp,
                        color = Color(0xFF8B8FA3)
                    )
                    Text(
                        text = "$utilizationPercent% CAPACITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE53935)
                    )
                }

                DataCardProgressBar(
                    progress = utilizationPercent / 100f,
                    progressColor = Color(0xFFE53935),
                    trackColor = Color(0xFFF1F3F5),
                    height = 6.dp
                )
            }
        }
    }
}

@Composable
private fun HeaderStatColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF8B8FA3),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CreatePurchaseOrderScreen(
    onClose: () -> Unit,
    onCancel: () -> Unit,
    onCreateOrder: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var expandedSection by remember { mutableStateOf("supplier") }

    // Supplier & Warehouse
    var supplier by remember { mutableStateOf("Global Textile Corp") }
    var supplierExpanded by remember { mutableStateOf(false) }
    val supplierOptions = listOf("Global Textile Corp", "Sunrise Fabrics", "Premium Weavers Co.")

    var warehouse by remember { mutableStateOf("Factory Warehouse (Primary)") }
    var warehouseExpanded by remember { mutableStateOf(false) }
    val warehouseOptions = listOf("Factory Warehouse (Primary)", "Retail Warehouse", "Cold Storage Unit")

    // Purchase Details
    var reorderQty by remember { mutableStateOf("200") }
    var unitPrice by remember { mutableStateOf("200") }

    // Delivery & Notes
    var expectedDelivery by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(PoPriority.URGENT) }
    var notes by remember { mutableStateOf("") }

    // Specifications & Attachments State
    var selectedSpecSheets by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Launcher to select all document & image file formats
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedSpecSheets = selectedSpecSheets + uris
        }
    }

    // Camera picture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            capturedImageUri?.let { uri ->
                selectedSpecSheets = selectedSpecSheets + uri
                capturedImageUri = null
            }
        }
    }

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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TitleBar("Create Purchase Order", onClose = onClose)
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // Header Information Card
                item {
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
                }

                // Supplier & Warehouse Section
                item {
                    AccordionSection(
                        title = "Supplier & Warehouse",
                        expanded = expandedSection == "supplier",
                        onHeaderClick = {
                            expandedSection = if (expandedSection == "supplier") "" else "supplier"
                        }
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
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = LabelColor,
                                modifier = Modifier.size(tokens.iconSize * 0.9f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Mumbai   ★ 4.8   Avg 5 days",
                                fontSize = tokens.caption,
                                color = LabelColor
                            )
                        }
                    }
                    Spacer(Modifier.height(tokens.screenPadding * 0.6f))
                }

                // Purchase Details Section
                item {
                    AccordionSection(
                        title = "Purchase Details",
                        expanded = expandedSection == "purchase",
                        onHeaderClick = {
                            expandedSection = if (expandedSection == "purchase") "" else "purchase"
                        }
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
                }

                // Delivery Section
                item {
                    AccordionSection(
                        title = "Delivery",
                        expanded = expandedSection == "delivery",
                        onHeaderClick = {
                            expandedSection = if (expandedSection == "delivery") "" else "delivery"
                        }
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
                }

                // Additional Notes Section
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding)
                    ) {
                        FormLabel("Additional Notes")
                        FormTextArea(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = "Specific packaging or handling requirements..."
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // Reusable Documentation & Specifications Upload Section
                item {
                    ImageUploadSection(
                        isImage = false,
                        selectedImages = selectedSpecSheets,
                        onBrowseClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        onCameraClick = {
                            if (cameraPermissionState.status.isGranted) {
                                val tempFile = File.createTempFile("spec_sheet_", ".jpg", context.cacheDir)
                                capturedImageUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    tempFile
                                )
                                capturedImageUri?.let { cameraLauncher.launch(it) }
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        onRemoveImage = { item ->
                            selectedSpecSheets = selectedSpecSheets.filter { it != item }
                        },
                        modifier = Modifier.padding(horizontal = tokens.screenPadding),
                        browseText = "Browse Files",
                        cameraText = "Camera",
                        previewHeaderTitle = "ATTACHED SPECIFICATIONS"
                    )
                }

                // Bottom Space for Floating Action Bar
                item {
                    Spacer(Modifier.height(tokens.buttonHeight * 2f))
                }
            }

            StepNavigationFab(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(tokens.buttonHeight * 1.65f),
                showBack = true,
                showBackArrow = false,
                showTrailingArrow = false,
                onBack = onCancel,
                backLabel = "Cancel",
                backWidthFraction = 0.25f,
                trailingAction = TrailingFabAction.Next(
                    label = "Create",
                    onClick = onCreateOrder
                ),
                trailingWidthFraction = 0.30f
            )
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
        textStyle = TextStyle(fontSize = tokens.bodyMedium, color = textColor),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = borderColor,
            focusedBorderColor = focusedBorderColor,
            focusedContainerColor = whiteBg,
            unfocusedContainerColor = whiteBg
        ),
        modifier = modifier.fillMaxWidth()
    )
}