@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "VariableNeverRead"
)

package com.cuso.mobile.view.home.sales.sales_order

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color.parseColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.database.entities.GarmentMeasurement
import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.model.sales.CustomerGarment
import com.cuso.mobile.model.sales.CustomerOrder
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.utils.safeDate
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.home.sales.customer.LabeledField
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.util.UUID

data class GarmentModel(
    val id: String,
    val name: String,
    val isSelected: Boolean = false
)

data class MeasurementField(
    val id: String,
    val label: String,
    val value: String = "",
    val unit: String = "inch"
)

private val orderSectionFieldMap = mapOf(
    "customer" to listOf("mobile", "fullName", "gender", "dressFor", "source"),
    "garment" to listOf("garments"),
    "delivery" to listOf("orderDate", "trialDate", "deliveryDate", "branch")
)

private fun missingGarmentFields(g: SelectedGarment): List<String> {
    return buildList {
        if (g.fabricType.isBlank()) add("Fabric Type")
        if (g.colorTone.isBlank()) add("Color/Tone")
        if (g.pattern.isBlank()) add("Pattern")
        if (g.measurements.isEmpty() || g.measurements.any { it.value.isBlank() }) add("Measurements")
    }
}
// ── Local Order Item State Model for Section 3 ──
data class ConfiguredOrderItem(
    val id: String = UUID.randomUUID().toString(),
    var garmentCategory: String = "Men's Wear",
    var garmentType: String = "Shirt",
    var quantity: Int = 1,
    var fabricSource: String = "Store Fabric",
    var fabricSelection: String = "Premium Giza Cotton - White",
    var designPreset: String = "Slim Fit Classic",
    var colorAccent: String = "White",
    var sizeStandard: String = "40",
    var stitchingType: String = "Normal Machine",
    var assignedTailor: String = "Master Gulam (Team A)",
    var deliveryDate: String = "26 Oct 2026",
    var specialInstructions: String = "",
    var collarStyle: String = "Spread Collar",
    var sleeveStyle: String = "Full Sleeve",
    var cuffPreference: String = "Rounded 2-Button",
    var pocketStyle: String = "No Pocket",
    var fittingPreference: String = "Slim Fit",
    var buttonStyle: String = "Classic Pearl White",
    var stitchingPrice: Double = 1200.0,
    var fabricPrice: Double = 2500.0,
    var addlWorkPrice: Double = 0.0,
    var discountPrice: Double = 300.0,
    var taxPrice: Double = 170.0
) {
    val totalItemPrice: Double
        get() = (stitchingPrice + fabricPrice + addlWorkPrice - discountPrice + taxPrice) * quantity
}

@SuppressLint("UseKtx")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    initialData: OrderReviewData? = null,
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onNextStep: (OrderReviewData) -> Unit = {},
    salesViewModel: SalesViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val isEditMode = initialData?.orderId != null

    // ── 1. Customer Details State ──
    var phone by rememberSaveable { mutableStateOf(initialData?.phone ?: "") }
    var fullName by rememberSaveable { mutableStateOf(initialData?.fullName ?: "") }
    var address by rememberSaveable { mutableStateOf(initialData?.address ?: "") }
    var gender by rememberSaveable { mutableStateOf(initialData?.gender ?: "Male") }
    var dressFor by rememberSaveable { mutableStateOf(initialData?.dressFor ?: "") }
    var source by rememberSaveable { mutableStateOf(initialData?.source ?: "Walk-in") }
    var countryCode by rememberSaveable { mutableStateOf(initialData?.countryCode ?: "+91") }

    // ── 2. Order Information State ──
    val generatedOrderId = rememberSaveable { "ORD-${(1000..9999).random()}" }
    var orderIdText by rememberSaveable { mutableStateOf(initialData?.orderId ?: generatedOrderId) }
    var orderDate by rememberSaveable { mutableStateOf(initialData?.orderDate.orEmpty().ifBlank { "12 Oct 2026" }) }
    var salesExecutive by rememberSaveable { mutableStateOf("Anil Kumar") }
    var orderType by rememberSaveable { mutableStateOf("New Stitching") }
    var priority by rememberSaveable { mutableStateOf("Normal") }
    var expectedDeliveryDate by rememberSaveable { mutableStateOf(initialData?.deliveryDate.orEmpty().ifBlank { "26 Oct 2026" }) }
    var deliveryMethod by rememberSaveable { mutableStateOf("Store Pickup") }
    var orderNotes by rememberSaveable { mutableStateOf("") }

    // ── 3. Order Items State ──
    var orderItemsList by remember {
        mutableStateOf(listOf(ConfiguredOrderItem()))
    }

    // ── 4. Pricing & Charges Calculation ──
    val subtotalAmount = orderItemsList.sumOf { (it.stitchingPrice + it.fabricPrice + it.addlWorkPrice) * it.quantity }
    val totalDiscount = orderItemsList.sumOf { it.discountPrice * it.quantity }
    val totalTax = orderItemsList.sumOf { it.taxPrice * it.quantity }
    val deliveryCharges = 0.0
    val grandTotalAmount = subtotalAmount - totalDiscount + totalTax + deliveryCharges

    // ── 5. Payment & Billing Preference State ──
    var paymentType by rememberSaveable { mutableStateOf("Advance") }
    var advanceAmount by rememberSaveable { mutableStateOf("1500") }
    var paymentMode by rememberSaveable { mutableStateOf("UPI / GPay") }
    var billingNotes by rememberSaveable { mutableStateOf("") }
    var financeClearanceRequired by rememberSaveable { mutableStateOf(true) }

    // ── 6. Attachments & Voice Notes State ──
    var selectedDesignImages by rememberSaveable { mutableStateOf(initialData?.designImages ?: emptyList()) }
    var recordedVoiceNoteUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isRecording by rememberSaveable { mutableStateOf(false) }

    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // ── Accordion Collapse/Expand State ──
    var expandedSection by rememberSaveable { mutableStateOf("order_info") }

    // ── Dropdown Expansion States ──
    var orderTypeExpanded by remember { mutableStateOf(false) }
    var salesExecExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var deliveryMethodExpanded by remember { mutableStateOf(false) }
    var paymentTypeExpanded by remember { mutableStateOf(false) }
    var paymentModeExpanded by remember { mutableStateOf(false) }

    var errorField by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // All-Files Picker Launcher
    val allFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedDesignImages = selectedDesignImages + uris
        }
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Order" else "Create Order",
                onClose = onCancel
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(bottom = 90.dp)
            ) {
                // ─────────────────────────────────────────────────────────────
                // 1. CUSTOMER DETAILS
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "1. Customer Details",
                    expanded = expandedSection == "customer",
                    onHeaderClick = { expandedSection = if (expandedSection == "customer") "" else "customer" }
                ) {
                    // Previous Measurements Hint Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEF2FF))
                            .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(8.dp))
                            .clickable { /* Handle measurement import */ }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Previous measurements available",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Mobile Number", isRequired = true)
                    PhoneInputField(
                        phoneValue = phone,
                        onPhoneChange = { phone = it },
                        onCountryChange = { countryCode = it.code }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Customer Name", isRequired = true)
                    FormTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Enter customer full name"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Delivery / Billing Address")
                    FormTextArea(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = "Enter full delivery address..."
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 2. ORDER INFORMATION
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "2. Order Information",
                    expanded = expandedSection == "order_info",
                    onHeaderClick = { expandedSection = if (expandedSection == "order_info") "" else "order_info" }
                ) {
                    FormLabel("Order ID", isRequired = true)
                    FormTextField(
                        value = orderIdText,
                        onValueChange = { orderIdText = it },
                        placeholder = "ORD-3012"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Order Date", isRequired = true)
                    DatePickerField(
                        value = orderDate,
                        onDateSelected = { orderDate = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Sales Executive",
                        value = salesExecutive,
                        expanded = salesExecExpanded,
                        onExpandChange = { salesExecExpanded = it },
                        options = listOf("Anil Kumar", "Suresh Raina", "Pooja Sharma"),
                        onOptionSelected = { salesExecutive = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Order Type",
                        value = orderType,
                        expanded = orderTypeExpanded,
                        onExpandChange = { orderTypeExpanded = it },
                        options = listOf("New Stitching", "Alteration Only", "Fabric + Stitching"),
                        onOptionSelected = { orderType = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Priority",
                        value = priority,
                        expanded = priorityExpanded,
                        onExpandChange = { priorityExpanded = it },
                        options = listOf("Normal", "High", "Urgent"),
                        onOptionSelected = { priority = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Expected Delivery Date", isRequired = true)
                    DatePickerField(
                        value = expectedDeliveryDate,
                        onDateSelected = { expectedDeliveryDate = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Delivery Method",
                        value = deliveryMethod,
                        expanded = deliveryMethodExpanded,
                        onExpandChange = { deliveryMethodExpanded = it },
                        options = listOf("Store Pickup", "Home Delivery", "Courier Express"),
                        onOptionSelected = { deliveryMethod = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Order Notes")
                    FormTextArea(
                        value = orderNotes,
                        onValueChange = { orderNotes = it },
                        placeholder = "Add any order-specific notes or special instructions here..."
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 3. ORDER ITEMS
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "3. Order Items",
                    expanded = expandedSection == "order_items",
                    onHeaderClick = { expandedSection = if (expandedSection == "order_items") "" else "order_items" }
                ) {
                    orderItemsList.forEachIndexed { itemIndex, item ->
                        var catExpanded by remember { mutableStateOf(false) }
                        var typeExpanded by remember { mutableStateOf(false) }
                        var sourceExpanded by remember { mutableStateOf(false) }
                        var fabricExpanded by remember { mutableStateOf(false) }
                        var presetExpanded by remember { mutableStateOf(false) }
                        var colorExpanded by remember { mutableStateOf(false) }
                        var sizeExpanded by remember { mutableStateOf(false) }
                        var stitchTypeExpanded by remember { mutableStateOf(false) }
                        var tailorExpanded by remember { mutableStateOf(false) }

                        // Customization dropdowns
                        var collarExpanded by remember { mutableStateOf(false) }
                        var sleeveExpanded by remember { mutableStateOf(false) }
                        var cuffExpanded by remember { mutableStateOf(false) }
                        var pocketExpanded by remember { mutableStateOf(false) }
                        var fitExpanded by remember { mutableStateOf(false) }
                        var buttonExpanded by remember { mutableStateOf(false) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "Item #${itemIndex + 1}",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Garment Configuration",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                        }

                        // Row: Category | Type | Qty
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                FormDropdown(
                                    label = "Garment Category",
                                    value = item.garmentCategory,
                                    expanded = catExpanded,
                                    onExpandChange = { catExpanded = it },
                                    options = listOf("Men's Wear", "Women's Wear", "Kids Wear"),
                                    onOptionSelected = { item.garmentCategory = it },
                                    isRequired = true
                                )
                            }
                            Column(modifier = Modifier.weight(1.2f)) {
                                FormDropdown(
                                    label = "Garment Type",
                                    value = item.garmentType,
                                    expanded = typeExpanded,
                                    onExpandChange = { typeExpanded = it },
                                    options = listOf("Shirt", "Pant", "Suit", "Kurta"),
                                    onOptionSelected = { item.garmentType = it },
                                    isRequired = true
                                )
                            }
                            Column(modifier = Modifier.weight(0.7f)) {
                                FormLabel("Qty", isRequired = true)
                                FormTextField(
                                    value = item.quantity.toString(),
                                    onValueChange = { item.quantity = it.toIntOrNull() ?: 1 },
                                    keyboardType = KeyboardType.Number,
                                    placeholder = "1"
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        FormDropdown(
                            label = "Fabric Source",
                            value = item.fabricSource,
                            expanded = sourceExpanded,
                            onExpandChange = { sourceExpanded = it },
                            options = listOf("Store Fabric", "Client Supplied", "In-House Tailoring"),
                            onOptionSelected = { item.fabricSource = it },
                            isRequired = true
                        )

                        Spacer(Modifier.height(14.dp))

                        FormDropdown(
                            label = "Fabric / Material Selection",
                            value = item.fabricSelection,
                            expanded = fabricExpanded,
                            onExpandChange = { fabricExpanded = it },
                            options = listOf("Premium Giza Cotton - White", "Linen Classic Blue", "Italian Silk Blend"),
                            onOptionSelected = { item.fabricSelection = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormDropdown(
                            label = "Design / Style Preset",
                            value = item.designPreset,
                            expanded = presetExpanded,
                            onExpandChange = { presetExpanded = it },
                            options = listOf("Slim Fit Classic", "Relaxed Comfort", "Tuxedo Cut"),
                            onOptionSelected = { item.designPreset = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        // Reference Image Upload Box
                        FormLabel("Reference Image")
                        ImageUploadSection(
                            isImage = true,
                            selectedImages = selectedDesignImages,
                            documentUploadText = "Browse Images",
                            onBrowseClick = { allFilesLauncher.launch("*/*") },
                            onRemoveImage = { removedUri ->
                                selectedDesignImages = selectedDesignImages.filter { it != removedUri }
                            },
                            previewHeaderTitle = "SELECTED IMAGES"
                        )

                        Spacer(Modifier.height(14.dp))

                        // Row: Color Accent | Size Standard
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                var showColorPicker by remember { mutableStateOf(false) }

                                FormLabel("Color Accent")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(tokens.fieldHeight)
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                        .background(whiteBg)
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(tokens.cardCornerRadius))
                                        .clickable { showColorPicker = true }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Color Preview Box
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (item.colorAccent.lowercase()) {
                                                    "white" -> Color.White
                                                    "navy blue" -> Color(0xFF1E3A8A)
                                                    "charcoal black" -> Color(0xFF1F2937)
                                                    else -> runCatching {
                                                        Color(parseColor(if (item.colorAccent.startsWith("#")) item.colorAccent else "#${item.colorAccent}"))
                                                    }.getOrDefault(Color.White)
                                                }
                                            )
                                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                    )

                                    Spacer(Modifier.width(10.dp))

                                    // Color Name / Hex Text
                                    Text(
                                        text = item.colorAccent.ifBlank { "White" },
                                        fontSize = tokens.bodyMedium,
                                        color = title_color,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Dropdown Indicator Icon
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (showColorPicker) {
                                    ColorPickerDialog(
                                        initialHex = if (item.colorAccent.startsWith("#")) item.colorAccent else "#FFFFFF",
                                        onDismiss = { showColorPicker = false },
                                        onConfirm = { hexColor ->
                                            item.colorAccent = hexColor
                                            showColorPicker = false
                                        }
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "Size Standard",
                                    value = item.sizeStandard,
                                    expanded = sizeExpanded,
                                    onExpandChange = { sizeExpanded = it },
                                    options = listOf("38", "40", "42", "44", "Custom"),
                                    onOptionSelected = { item.sizeStandard = it }
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        FormDropdown(
                            label = "Stitching Type",
                            value = item.stitchingType,
                            expanded = stitchTypeExpanded,
                            onExpandChange = { stitchTypeExpanded = it },
                            options = listOf("Normal Machine", "Hand-stitched Premium", "Double Seam"),
                            onOptionSelected = { item.stitchingType = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormDropdown(
                            label = "Tailor / Production Assignment",
                            value = item.assignedTailor,
                            expanded = tailorExpanded,
                            onExpandChange = { tailorExpanded = it },
                            options = listOf("Master Gulam (Team A)", "Rashid Master (Team B)", "Unassigned"),
                            onOptionSelected = { item.assignedTailor = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Item Delivery Date")
                        DatePickerField(
                            value = item.deliveryDate,
                            onDateSelected = { item.deliveryDate = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Garment Special Instructions")
                        FormTextArea(
                            value = item.specialInstructions,
                            onValueChange = { item.specialInstructions = it },
                            placeholder = "Add any garment-specific stitching or design instructions for this order..."
                        )

                        Spacer(Modifier.height(16.dp))

                        // ── Nested Customization & Design Details Card ──
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFF)),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Customization & Design Details",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )

                                Spacer(Modifier.height(12.dp))

                                // Row 1: Collar Style | Sleeve Style
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Collar Style",
                                            value = item.collarStyle,
                                            expanded = collarExpanded,
                                            onExpandChange = { collarExpanded = it },
                                            options = listOf("Spread Collar", "Mandarin Collar", "Button Down"),
                                            onOptionSelected = { item.collarStyle = it }
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Sleeve Style",
                                            value = item.sleeveStyle,
                                            expanded = sleeveExpanded,
                                            onExpandChange = { sleeveExpanded = it },
                                            options = listOf("Full Sleeve", "Half Sleeve", "Roll-up Sleeve"),
                                            onOptionSelected = { item.sleeveStyle = it }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // Row 2: Cuff Preference | Pocket Style
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Cuff Preference",
                                            value = item.cuffPreference,
                                            expanded = cuffExpanded,
                                            onExpandChange = { cuffExpanded = it },
                                            options = listOf("Rounded 2-Button", "French Cuff", "Single Button"),
                                            onOptionSelected = { item.cuffPreference = it }
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Pocket Style",
                                            value = item.pocketStyle,
                                            expanded = pocketExpanded,
                                            onExpandChange = { pocketExpanded = it },
                                            options = listOf("No Pocket", "Single V-Pocket", "Double Flap Pocket"),
                                            onOptionSelected = { item.pocketStyle = it }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // Row 3: Fitting Preference | Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Fitting Preference",
                                            value = item.fittingPreference,
                                            expanded = fitExpanded,
                                            onExpandChange = { fitExpanded = it },
                                            options = listOf("Slim Fit", "Regular Fit", "Tailored Fit"),
                                            onOptionSelected = { item.fittingPreference = it }
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Buttons",
                                            value = item.buttonStyle,
                                            expanded = buttonExpanded,
                                            onExpandChange = { buttonExpanded = it },
                                            options = listOf("Classic Pearl White", "Matte Horn", "Metallic Silver"),
                                            onOptionSelected = { item.buttonStyle = it }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                    }

                    // + Add Another Item Action
                    Row(
                        modifier = Modifier
                            .clickable {
                                orderItemsList = orderItemsList + ConfiguredOrderItem()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Add Another Item",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 4. PRICING & CHARGES
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "4. Pricing & Charges",
                    expanded = expandedSection == "pricing",
                    onHeaderClick = { expandedSection = if (expandedSection == "pricing") "" else "pricing" }
                ) {
                    orderItemsList.forEachIndexed { itemIdx, item ->
                        // Item Pricing Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Item #${itemIdx + 1} – ${item.garmentType}",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )

                                Spacer(Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Stitching", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text("₹${formatIndianNumber(item.stitchingPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = title_color)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Fabric", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text("₹${formatIndianNumber(item.fabricPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = title_color)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Addl. Work", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text("₹${formatIndianNumber(item.addlWorkPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = title_color)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Discount", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text("-₹${formatIndianNumber(item.discountPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = redText)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Tax/GST", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text("₹${formatIndianNumber(item.taxPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = title_color)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Total", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text("₹${formatIndianNumber(item.totalItemPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Currency: INR (₹) • GST: Standard 5% applied",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(Modifier.height(10.dp))

                    // Summary Breakdown Table
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = tokens.bodySmall, color = Color(0xFF64748B))
                        Text("₹${formatIndianNumber(subtotalAmount)}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Discount", fontSize = tokens.bodySmall, color = Color(0xFF64748B))
                        Text("-₹${formatIndianNumber(totalDiscount)}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = redText)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Tax (GST)", fontSize = tokens.bodySmall, color = Color(0xFF64748B))
                        Text("+₹${formatIndianNumber(totalTax)}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Charges", fontSize = tokens.bodySmall, color = Color(0xFF64748B))
                        Text("₹0", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(Modifier.height(10.dp))

                    // Grand Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GRAND TOTAL",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                        Text(
                            text = "₹${formatIndianNumber(grandTotalAmount)}",
                            fontSize = tokens.h2,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 5. PAYMENT & BILLING PREFERENCE
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "5. Payment & Billing Preference",
                    expanded = expandedSection == "payment",
                    onHeaderClick = { expandedSection = if (expandedSection == "payment") "" else "payment" }
                ) {
                    FormDropdown(
                        label = "Payment Type",
                        value = paymentType,
                        expanded = paymentTypeExpanded,
                        onExpandChange = { paymentTypeExpanded = it },
                        options = listOf("Advance", "Full Payment", "Pay on Delivery"),
                        onOptionSelected = { paymentType = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Advance Amount Required", isRequired = true)
                    FormTextField(
                        value = advanceAmount,
                        onValueChange = { advanceAmount = it },
                        placeholder = "₹1,500",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Payment Mode",
                        value = paymentMode,
                        expanded = paymentModeExpanded,
                        onExpandChange = { paymentModeExpanded = it },
                        options = listOf("UPI / GPay", "Cash", "Card", "Bank Transfer"),
                        onOptionSelected = { paymentMode = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Billing / Credit Notes")
                    FormTextArea(
                        value = billingNotes,
                        onValueChange = { billingNotes = it },
                        placeholder = "Add any billing or credit notes..."
                    )

                    Spacer(Modifier.height(14.dp))

                    // Finance Clearance Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Finance Clearance Required Before Service",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            modifier = Modifier.weight(1f)
                        )
                        MiniSwitch(
                            checked = financeClearanceRequired,
                            onCheckedChange = { financeClearanceRequired = it }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Yellow Warning Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Text(
                            text = "Order will be held for finance approval before being assigned to production. Customer will be notified of payment confirmation.",
                            fontSize = 12.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 6. ATTACHMENTS & VOICE NOTES
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "6. Attachments & Voice Notes",
                    expanded = expandedSection == "attachments",
                    onHeaderClick = { expandedSection = if (expandedSection == "attachments") "" else "attachments" }
                ) {
                    Text(
                        text = "Attachments & References",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Upload reference images, fabric swatches, or measurement sheets",
                        fontSize = tokens.caption,
                        color = Color(0xFF64748B)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Cloud Document Upload Box
                    ImageUploadSection(
                        isImage = false,
                        selectedImages = selectedDesignImages,
                        documentUploadText = "Tap to upload files",
                        onBrowseClick = { allFilesLauncher.launch("*/*") },
                        onRemoveImage = { removedUri ->
                            selectedDesignImages = selectedDesignImages.filter { it != removedUri }
                        },
                        previewHeaderTitle = "ATTACHED FILES"
                    )

                    Spacer(Modifier.height(16.dp))

                }
            }

            // ─────────────────────────────────────────────────────────────
            // FLOATING ACTION BUTTON (TRAILING FAB)
            // ─────────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp)
            ) {
                TrailingFabButton(
                    action = TrailingFabAction.Next(
                        label = "Next",
                        onClick = {
                            val reviewData = OrderReviewData(
                                leadId = initialData?.leadId,
                                orderId = orderIdText,
                                customerId = initialData?.customerId ?: "",
                                branchId = initialData?.branchId ?: "",
                                fullName = fullName,
                                countryCode = countryCode.ifBlank { "+91" },
                                phone = phone,
                                gender = gender,
                                dressFor = dressFor,
                                address = address,
                                source = source,
                                orderDate = orderDate,
                                trialDate = initialData?.trialDate.orEmpty().ifBlank { orderDate },
                                deliveryDate = expectedDeliveryDate,
                                discount = initialData?.discount ?: 0.0,
                                paidSoFar = advanceAmount.toDoubleOrNull() ?: 0.0,
                                designImages = selectedDesignImages,
                                existingImageUrls = initialData?.existingImageUrls ?: emptyList(),
                                voiceNoteUri = recordedVoiceNoteUris.firstOrNull(),
                                garments = orderItemsList.map { item ->
                                    SelectedGarment(
                                        category = item.garmentType,
                                        categoryName = item.garmentType,
                                        categoryId = item.garmentType,
                                        quantity = item.quantity,
                                        price = item.totalItemPrice,
                                        priority = priority,
                                        trialRequired = true,
                                        fabricSource = item.fabricSource,
                                        fabricType = item.fabricSelection,
                                        colorTone = item.colorAccent,
                                        pattern = item.designPreset,
                                        models = emptyList()
                                    )
                                }
                            )
                            onNextStep(reviewData)
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun QuickAddCategoryRow(
    categories: List<Pair<String, String>>,
    selectedCategoryId: String?,
    onCategoryClick: (String, String) -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        categories.forEach { (name, categoryId) ->
            val isSelected = categoryId == selectedCategoryId
            CategoryPillButton(
                name = name,
                isSelected = isSelected,
                onClick = { onCategoryClick(name, categoryId) }
            )
        }
    }
}

@Composable
fun CategoryPillButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val borderColor = if (isSelected) Primary else grey_border
    val contentColor = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF9CA3AF)

    Row(
        modifier = Modifier
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(tokens.cardCornerRadius)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Icon(
            imageVector = Icons.Default.Checkroom,
            contentDescription = name,
            tint = contentColor,
            modifier = Modifier.size(tokens.iconSize)
        )
        Text(
            text = name,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
fun CustomerOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val tokens = LocalAppTokens.current
    val interactionSource = remember { MutableInteractionSource() }
    val colors = customFieldOutlinedColors()

    Column {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth().height(tokens.fieldHeight),
            enabled = enabled,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) Color(0xFF111827) else Color(0xFF9CA3AF)
            ),
            cursorBrush = SolidColor(Color(0xFF3B3BF9)),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    isError = isError,
                    placeholder = { Text(placeholder, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium) },
                    contentPadding = PaddingValues(horizontal = tokens.extraPadding, vertical = 0.dp),
                    colors = colors,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                        )
                    }
                )
            }
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = redText,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun PreviousMeasurementsDialog(
    orders: List<CustomerOrder>,
    onImport: (List<CustomerGarment>) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val expandedOrders = remember { mutableStateOf(setOf<String>()) }
    val selectedGarments = remember { mutableStateOf(mapOf<String, Set<String>>()) }
    val totalSelected = selectedGarments.value.values.sumOf { it.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = tokens.screenPadding),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Previous Measurements", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text("Select garments to copy", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF9CA3AF))
                    }
                }

                HorizontalDivider(color = grey_border)

                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    orders.forEach { order ->
                        val isExpanded = expandedOrders.value.contains(order.id)
                        val orderSelectedGarments = selectedGarments.value[order.id] ?: emptySet()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = BorderStroke(1.dp, grey_border)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedOrders.value =
                                                if (isExpanded) expandedOrders.value - order.id
                                                else expandedOrders.value + order.id
                                        }
                                        .padding(tokens.extraPadding),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                        ) {
                                            Text(
                                                "Order #${order.orderNumber}",
                                                fontSize = tokens.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF111827)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        when (order.status.lowercase()) {
                                                            "confirmed" -> Color(0xFFDCFCE7)
                                                            "completed" -> Color(0xFFDCFCE7)
                                                            "pending" -> Color(0xFFFEF3C7)
                                                            "cancelled" -> Color(0xFFFFEBEE)
                                                            else -> Color(0xFFE0E7FF)
                                                        },
                                                        RoundedCornerShape(20.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    order.status,
                                                    fontSize = tokens.caption,
                                                    color = when (order.status.lowercase()) {
                                                        "confirmed" -> Color(0xFF16A34A)
                                                        "completed" -> Color(0xFF16A34A)
                                                        "pending" -> Color(0xFFD97706)
                                                        "cancelled" -> Color(0xFFDC2626)
                                                        else -> Color(0xFF4338CA)
                                                    },
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Text(
                                            "${order.orderDate.safeDate()} • ${order.garments.size} Garment${if (order.garments.size != 1) "s" else ""}",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                    Icon(
                                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        null,
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(whiteBg)
                                            .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding),
                                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                    ) {
                                        order.garments.forEach { garment ->
                                            val isSelected = orderSelectedGarments.contains(garment.id)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val current = selectedGarments.value.toMutableMap()
                                                        val currentSet = (current[order.id] ?: emptySet()).toMutableSet()
                                                        if (isSelected) currentSet.remove(garment.id) else currentSet.add(garment.id)
                                                        current[order.id] = currentSet
                                                        selectedGarments.value = current
                                                    }
                                                    .padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .background(
                                                            if (isSelected) Color(0xFF3B3BF9) else whiteBg,
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) Color(0xFF3B3BF9) else Color(0xFFD1D5DB),
                                                            RoundedCornerShape(4.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            null,
                                                            tint = whiteBg,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        garment.categoryName,
                                                        fontSize = tokens.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF111827)
                                                    )
                                                    val measurementText = garment.measurementSnapshot
                                                        ?.entries
                                                        ?.take(3)
                                                        ?.joinToString(", ") { it.key }
                                                        ?: ""
                                                    if (measurementText.isNotBlank()) {
                                                        Text("$measurementText...", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
                                                    }
                                                }

                                                Text(
                                                    if (isSelected) "Selected" else "Select",
                                                    fontSize = tokens.bodyMedium,
                                                    color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF9CA3AF),
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = grey_border)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, grey_border)
                    ) {
                        Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.padding(10.dp))
                    Button(
                        onClick = {
                            val garmentsToImport = orders.flatMap { order ->
                                val selectedIds = selectedGarments.value[order.id] ?: emptySet()
                                order.garments.filter { it.id in selectedIds }
                            }
                            onImport(garmentsToImport)
                        },
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        enabled = totalSelected > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B3BF9),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        ),
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    ) {
                        Icon(Icons.Default.Download, null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                        Spacer(Modifier.width(6.dp))
                        Text("Import ", color = whiteBg, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
private fun InlineGarmentPanel(
    garment: SelectedGarment,
    categories: List<Pair<String, String>>,
    isEditing: Boolean = false,
    allowCategorySelection: Boolean = false,
    onGarmentChange: (SelectedGarment) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val priorityOptions = listOf("Low", "Medium", "High", "Urgent")
    val fabricSourceOptions = listOf("In-House", "Client")
    val fabricTypeOptions = listOf("Cotton", "Polyester", "Silk", "Wool", "Linen", "Denim", "Satin", "Velvet", "Jersey", "Chiffon")
    val patternOptions = listOf("Solid", "Striped", "Checked", "Printed", "Plain", "Plaid", "Floral")
    val availableModels = listOf(
        GarmentModel("1", "Ankle Fit"),
        GarmentModel("2", "Mom Fit")
    )

    var garmentTypeExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var fabricTypeExpanded by remember { mutableStateOf(false) }
    var patternExpanded by remember { mutableStateOf(false) }

    var selectedModels by remember(garment.id, garment.categoryId) {
        mutableStateOf(garment.models.toMutableList())
    }
    var measurements by remember(garment.id) {
        mutableStateOf(
            if (garment.measurements.isNotEmpty())
                garment.measurements.map { m -> MeasurementField(id = m.id.ifBlank { m.label }, label = m.label, value = m.value, unit = m.unit) }
            else defaultMeasurementsFor(selectedModels)
        )
    }

    LaunchedEffect(garment.id, garment.categoryId) {
        selectedModels = garment.models.toMutableList()
        measurements = if (garment.measurements.isNotEmpty()) {
            garment.measurements.map { m -> MeasurementField(id = m.id.ifBlank { m.label }, label = m.label, value = m.value, unit = m.unit) }
        } else {
            defaultMeasurementsFor(garment.models)
        }
    }

    var subSection by remember { mutableStateOf("basic") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding).background(Color.Transparent),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                if (isEditing) "EDIT GARMENT" else "ADD NEW GARMENT",
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(tokens.screenPadding))

            GarmentSubSection(
                iconPainter = painterResource(R.drawable.ic_info),
                label = "Basic Information",
                expanded = subSection == "basic",
                onToggle = { subSection = if (subSection == "basic") "" else "basic" }
            ) {
                FormDropdown(
                    label = "Garment Type",
                    value = garment.categoryName.ifEmpty { "Select Garment Type" },
                    expanded = garmentTypeExpanded && allowCategorySelection,
                    onExpandChange = { if (allowCategorySelection) garmentTypeExpanded = it },
                    options = categories.map { it.first },
                    onOptionSelected = { selectedName ->
                        val selectedCategory = categories.find { it.first == selectedName }
                        if (selectedCategory != null) {
                            onGarmentChange(
                                garment.copy(
                                    categoryName = selectedCategory.first,
                                    category = selectedCategory.second,
                                    categoryId = selectedCategory.second
                                )
                            )
                        }
                    },
                    isRequired = true,
                    enabled = allowCategorySelection
                )

                Spacer(Modifier.height(tokens.screenPadding))

                LabeledField("Quantity *") {
                    CustomerOutlinedField(
                        value = garment.quantity.toString(),
                        onValueChange = { selected ->
                            selected.toIntOrNull()?.let {
                                onGarmentChange(garment.copy(quantity = it))
                            }
                        },
                        placeholder = "Enter quantity",
                        enabled = true
                    )
                }
                Spacer(Modifier.height(tokens.screenPadding))

                FormDropdown(
                    label = "Priority",
                    value = garment.priority.ifEmpty { "Select Priority" },
                    expanded = priorityExpanded,
                    onExpandChange = { priorityExpanded = it },
                    options = priorityOptions,
                    onOptionSelected = { selected -> onGarmentChange(garment.copy(priority = selected)) },
                    isRequired = true
                )

                Spacer(Modifier.height(tokens.screenPadding))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(tokens.cardCornerRadius))
                        .padding(tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Trial Required", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                        Text("Schedule fitting?", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
                    }
                    MiniSwitch(
                        checked = garment.trialRequired,
                        onCheckedChange = { onGarmentChange(garment.copy(trialRequired = it)) },
                    )
                }
            }

            GarmentSubSection(
                iconPainter = painterResource(R.drawable.ic_message),
                label = "Fabric Details",
                expanded = subSection == "fabric",
                onToggle = { subSection = if (subSection == "fabric") "" else "fabric" }
            ) {
                FormLabel("Fabric Source")
                SegmentedSelector(
                    options = fabricSourceOptions,
                    selected = garment.fabricSource.ifEmpty { "In-House" },
                    onSelect = { selected -> onGarmentChange(garment.copy(fabricSource = selected)) },
                    label = { it }
                )
                Spacer(Modifier.height(tokens.screenPadding))

                FormTextField(
                    value = garment.fabricType,
                    onValueChange = { newValue ->
                        onGarmentChange(garment.copy(fabricType = newValue))
                    },
                    placeholder = "e.g Cotton",
                    keyboardType = KeyboardType.Text,
                    isError = false,
                    errorMessage = null
                )

                Spacer(Modifier.height(tokens.screenPadding))

                FormLabel("Color / Tone")
                ColorPickerField(
                    value = garment.colorTone,
                    onColorSelected = { onGarmentChange(garment.copy(colorTone = it)) }
                )

                Spacer(Modifier.height(tokens.screenPadding))

                FormDropdown(
                    label = "Pattern",
                    value = garment.pattern.ifEmpty { "Select Pattern" },
                    expanded = patternExpanded,
                    onExpandChange = { patternExpanded = it },
                    options = patternOptions,
                    onOptionSelected = { selected -> onGarmentChange(garment.copy(pattern = selected)) },
                    isRequired = true
                )
            }

            GarmentSubSection(
                iconPainter = painterResource(R.drawable.ic_circle),
                label = "Models",
                expanded = subSection == "models",
                onToggle = { subSection = if (subSection == "models") "" else "models" }
            ) {
                ModelGridSelector(
                    models = availableModels,
                    selectedModels = selectedModels,
                    onModelToggle = { modelName ->
                        val wasEmpty = selectedModels.isEmpty()
                        val updatedModels = if (selectedModels.contains(modelName)) {
                            mutableListOf()
                        } else {
                            mutableListOf(modelName)
                        }
                        selectedModels = updatedModels
                        measurements = if (updatedModels.isEmpty()) {
                            emptyList()
                        } else if (wasEmpty || measurements.isEmpty()) {
                            defaultMeasurementsFor(updatedModels)
                        } else {
                            measurements
                        }
                        onGarmentChange(garment.copy(models = updatedModels.toList()))
                    }
                )

                if (selectedModels.isNotEmpty() || measurements.isNotEmpty()) {
                    Spacer(Modifier.height(tokens.screenPadding))
                    MeasurementsSection(
                        measurements = measurements,
                        onMeasurementsChange = { updated ->
                            measurements = updated
                            onGarmentChange(
                                garment.copy(
                                    measurements = updated.map { m ->
                                        GarmentMeasurement(id = m.id, label = m.label, value = m.value, unit = m.unit)
                                    }
                                )
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(tokens.cardPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                BackFabButton(
                    onClick = onCancel,
                    label = "Cancel",
                    modifier = Modifier.weight(1f)
                )

                TrailingFabButton(
                    action = TrailingFabAction.Next(
                        label = "Apply",
                        onClick = onSave
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GarmentSubSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    iconTint: Color = Primary,
    label: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    val chevronRotation by androidx.compose.animation.core.animateFloatAsState(
        if (expanded) 180f else 0f, label = "sub_chevron"
    )

    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = tokens.extraPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    iconPainter != null -> Icon(
                        painter = iconPainter,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    icon != null -> Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
                Spacer(Modifier.width(tokens.extraPadding))
                Text(label, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = iconTint)
            }
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp).rotate(chevronRotation)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = tokens.extraPadding)) {
                content()
            }
        }

        HorizontalDivider(color = grey_border)
    }
}

@Composable
fun ModelGridSelector(
    models: List<GarmentModel>,
    selectedModels: List<String>,
    onModelToggle: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        models.forEach { model ->
            val isSelected = selectedModels.contains(model.name)
            CategoryPillButton(
                name = model.name,
                isSelected = isSelected,
                onClick = { onModelToggle(model.name) }
            )
        }
    }
}

@Composable
fun MeasurementsSection(
    measurements: List<MeasurementField>,
    onMeasurementsChange: (List<MeasurementField>) -> Unit
) {
    val tokens = LocalAppTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        measurements.forEachIndexed { index, field ->
            MeasurementInputField(
                label = "${field.label} (Number)",
                value = field.value,
                onValueChange = { newValue ->
                    val updated = measurements.toMutableList()
                    updated[index] = field.copy(value = newValue)
                    onMeasurementsChange(updated)
                },
                unit = field.unit,
                onUnitChange = { newUnit ->
                    val updated = measurements.toMutableList()
                    updated[index] = field.copy(unit = newUnit)
                    onMeasurementsChange(updated)
                }
            )
        }

        Spacer(Modifier.height(4.dp))

        AddCustomFieldLink(
            onClick = {
                val updated = measurements.toMutableList()
                updated.add(
                    MeasurementField(
                        id = "custom_${System.currentTimeMillis()}",
                        label = "",
                        value = "",
                        unit = "inch"
                    )
                )
                onMeasurementsChange(updated)
            }
        )

        measurements.filter { it.id.startsWith("custom_") }.forEach { field ->
            val index = measurements.indexOfFirst { it.id == field.id }
            if (index >= 0) {
                Spacer(Modifier.height(tokens.extraPadding))
                CustomFieldRow(
                    labelValue = field.label,
                    onLabelChange = { newLabel ->
                        val updated = measurements.toMutableList()
                        updated[index] = field.copy(label = newLabel)
                        onMeasurementsChange(updated)
                    },
                    fieldValue = field.value,
                    onFieldValueChange = { newValue ->
                        val updated = measurements.toMutableList()
                        updated[index] = field.copy(value = newValue)
                        onMeasurementsChange(updated)
                    },
                    onRemove = {
                        val updated = measurements.toMutableList()
                        updated.removeAt(index)
                        onMeasurementsChange(updated)
                    }
                )
            }
        }
    }
}

@Composable
fun MeasurementInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    unitOptions: List<String> = listOf("inch", "cm")
) {
    val tokens = LocalAppTokens.current
    var unitExpanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF111827)),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.fieldHeight)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .padding(horizontal = tokens.extraPadding),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text("0.0", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
                        }
                        inner()
                    }
                }
            )

            Box {
                Row(
                    modifier = Modifier
                        .width(72.dp)
                        .height(tokens.fieldHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                        .background(whiteBg)
                        .clickable { unitExpanded = true }
                        .padding(horizontal = tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(unit, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    unitOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium) },
                            onClick = { onUnitChange(it); unitExpanded = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomFieldLink(onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    Text(
        "+ Add Custom Field",
        fontSize = tokens.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF3B3BF9),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun CustomFieldRow(
    labelValue: String,
    onLabelChange: (String) -> Unit,
    fieldValue: String,
    onFieldValueChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Label", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = labelValue,
                onValueChange = onLabelChange,
                textStyle = TextStyle(fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF111827)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.fieldHeight)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .padding(horizontal = tokens.extraPadding),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        if (labelValue.isEmpty()) Text("Label Name", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
                        inner()
                    }
                }
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text("Value", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = fieldValue,
                onValueChange = onFieldValueChange,
                textStyle = TextStyle(fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF111827)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.fieldHeight)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .padding(horizontal = tokens.extraPadding),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        if (fieldValue.isEmpty()) Text("Value", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
                        inner()
                    }
                }
            )
        }

        IconButton(onClick = onRemove, modifier = Modifier.padding(top = tokens.screenPadding)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFF9CA3AF))
        }
    }
}

private fun defaultMeasurementsFor(modelNames: List<String>): List<MeasurementField> {
    if (modelNames.isEmpty()) return emptyList()
    return listOf(
        MeasurementField(id = "chest", label = "Chest"),
        MeasurementField(id = "sleeve_length", label = "Sleeve Length")
    )
}

@Composable
fun ColorPickerField(
    value: String,
    onColorSelected: (String) -> Unit,
    placeholder: String = "Color name"
) {
    val tokens = LocalAppTokens.current
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight)
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
            .clickable { showDialog = true }
            .padding(horizontal = tokens.extraPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Icon(
            Icons.Filled.Colorize, contentDescription = "Color picker", tint = PrimaryBorder
        )
        Text(
            text = value.ifBlank { placeholder },
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (value.isBlank()) Color(0xFF9CA3AF) else Color(0xFF111827)
        )
    }

    if (showDialog) {
        ColorPickerDialog(
            initialHex = value,
            onDismiss = { showDialog = false },
            onConfirm = { hex ->
                onColorSelected(hex)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val controller = rememberColorPickerController()
    var selectedHex by remember { mutableStateOf(initialHex.ifBlank { "#3B82F6" }) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = tokens.screenPadding),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                Text("Choose Color", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                HsvColorPicker(
                    modifier = Modifier.fillMaxWidth().height(260.dp).padding(tokens.extraPadding),
                    controller = controller,
                    initialColor = parseHexColorOrNull(selectedHex) ?: Color(0xFF3B82F6),
                    onColorChanged = { envelope ->
                        val argb = envelope.color.toArgb()
                        val rgbHex = String.format("#%06X", 0xFFFFFF and argb)
                        selectedHex = rgbHex
                    }
                )

                BrightnessSlider(
                    modifier = Modifier.fillMaxWidth().height(35.dp),
                    controller = controller
                )

                AlphaSlider(
                    modifier = Modifier.fillMaxWidth().height(35.dp),
                    controller = controller
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(parseHexColorOrNull(selectedHex) ?: grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                    )
                    Text(selectedHex.uppercase(), fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    ) {
                        Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onConfirm(selectedHex.uppercase()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    ) {
                        Text("Select", color = whiteBg, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun parseHexColorOrNull(hex: String): Color? {
    return try {
        val cleaned = hex.trim().removePrefix("#")
        if (cleaned.length != 6 && cleaned.length != 8) return null
        val colorLong = cleaned.toLong(16)
        if (cleaned.length == 6) Color(0xFF000000 or colorLong) else Color(colorLong)
    } catch (_: Exception) {
        null
    }
}