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

package com.cuso.tailor.view.home.sales.sales_order

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.database.entities.SelectedGarment
import com.cuso.tailor.model.sales.CustomerGarment
import com.cuso.tailor.model.sales.CustomerOrder
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.safeDate
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.SalesViewModel
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ConfiguredOrderItem(
    val id: String = UUID.randomUUID().toString(),
    var garmentCategory: String = "",
    var garmentType: String = "",
    var quantity: Int = 1,
    var fabricSource: String = "",
    var fabricSelection: String = "",
    var designPreset: String = "",
    var colorAccent: String = "",
    var sizeStandard: String = "",
    var stitchingType: String = "",
    var assignedTailor: String = "",
    var deliveryDate: String = "",
    var specialInstructions: String = "",
    var collarStyle: String = "",
    var sleeveStyle: String = "",
    var cuffPreference: String = "",
    var pocketStyle: String = "",
    var fittingPreference: String = "",
    var buttonStyle: String = "",
    var stitchingPrice: Double = 0.0,
    var fabricPrice: Double = 0.0,
    var addlWorkPrice: Double = 0.0,
    var discountPrice: Double = 0.0,
    var taxPrice: Double = 0.0,
    var trialRequired: Boolean = false
) {
    val totalItemPrice: Double
        get() = ((stitchingPrice + fabricPrice + addlWorkPrice - discountPrice + taxPrice) * quantity).coerceAtLeast(0.0)
}

@SuppressLint("UseKtx")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    initialData: OrderReviewData? = null,
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onAddNewCustomer: () -> Unit = {},
    onNextStep: (OrderReviewData) -> Unit = {},
    salesViewModel: SalesViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    val isEditMode = initialData?.orderId != null

    // Load initial staff and branch configurations from API
    LaunchedEffect(Unit) {
        salesViewModel.fetchStaff()
        branchViewModel.loadBranches()
    }

    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val staffOptions = remember(staffList) {
        staffList.map { "${it.firstName} ${it.lastName}".trim() }.filter { it.isNotBlank() }
    }

    // Branch state from BranchViewModel
    val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branches = (branchUiState as? BranchUiState.Success)?.branches ?: emptyList()

    val branchNames = remember(branches) {
        branches.map { branch ->
            branch.name?.takeIf { it.isNotBlank() } ?: branch.branchId ?: "Branch"
        }
    }

    val branchNameToIdMap = remember(branches) {
        branches.associate { branch ->
            val displayName = branch.name?.takeIf { it.isNotBlank() } ?: branch.branchId ?: "Branch"
            displayName to branch.id
        }
    }

    val todayFormatted = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
    }

    // ── 1. Customer Details State ──
    var customerId by rememberSaveable { mutableStateOf(initialData?.customerId ?: "") }
    var phone by rememberSaveable { mutableStateOf(initialData?.phone ?: "") }
    var fullName by rememberSaveable { mutableStateOf(initialData?.fullName ?: "") }
    var emailAddress by rememberSaveable { mutableStateOf("") }
    var customerType by rememberSaveable { mutableStateOf("Individual") }
    var selectedBranchName by rememberSaveable { mutableStateOf("") }
    var selectedBranchId by rememberSaveable { mutableStateOf(initialData?.branchId ?: "") }
    var address by rememberSaveable { mutableStateOf(initialData?.address ?: "") }
    var gender by rememberSaveable { mutableStateOf(initialData?.gender ?: "") }
    var dressFor by rememberSaveable { mutableStateOf(initialData?.dressFor ?: "") }
    var source by rememberSaveable { mutableStateOf(initialData?.source ?: "") }
    var countryCode by rememberSaveable { mutableStateOf(initialData?.countryCode ?: "+91") }

    // Pre-select default branch if not selected
    LaunchedEffect(branches) {
        if (selectedBranchId.isNotBlank() && selectedBranchName.isBlank()) {
            val matching = branches.firstOrNull { it.id == selectedBranchId }
            if (matching != null) {
                selectedBranchName = matching.name?.takeIf { it.isNotBlank() } ?: matching.branchId.orEmpty()
            }
        } else if (selectedBranchId.isBlank() && branches.isNotEmpty()) {
            val first = branches.first()
            selectedBranchId = first.id
            selectedBranchName = first.name?.takeIf { it.isNotBlank() } ?: first.branchId.orEmpty()
        }
    }

    // ── Customer Search & Measurement Import States ──
    val isSearchingCustomer by salesViewModel.isSearchingCustomer.collectAsStateWithLifecycle()
    val searchResult by salesViewModel.customerSearchResult.collectAsStateWithLifecycle()
    val foundCustomer = searchResult?.customer
    val previousOrders = searchResult?.orders ?: emptyList()
    var showPreviousMeasurementsDialog by remember { mutableStateOf(false) }

    // Trigger mobile search when 10 digits are entered
    LaunchedEffect(phone, countryCode) {
        val cleanPhone = phone.filter { it.isDigit() }
        if (cleanPhone.length >= 10) {
            salesViewModel.searchCustomerByMobile(cleanPhone, countryCode)
        } else {
            salesViewModel.clearCustomerSearch()
        }
    }

    // Auto-populate customer fields when search returns a verified profile
    LaunchedEffect(foundCustomer) {
        if (foundCustomer != null) {
            customerId = foundCustomer.id
            if (fullName.isBlank()) {
                fullName = foundCustomer.name
            }
            if (foundCustomer.type.isNotBlank()) {
                customerType = foundCustomer.type.replaceFirstChar { it.uppercase() }
            }
            if (address.isBlank() && foundCustomer.address != null) {
                val addr = foundCustomer.address
                val resolvedAddress = listOfNotNull(
                    addr.addressLine.takeIf { it.isNotBlank() },
                    addr.area?.takeIf { it.isNotBlank() },
                    addr.city.takeIf { it.isNotBlank() },
                    addr.pincode.takeIf { it.isNotBlank() }
                ).joinToString(", ")
                if (resolvedAddress.isNotBlank()) {
                    address = resolvedAddress
                }
            }
        }
    }

    // ── 2. Order Information State ──
    var orderIdText by rememberSaveable { mutableStateOf(initialData?.orderId ?: "") }
    var orderDate by rememberSaveable { mutableStateOf(initialData?.orderDate.orEmpty().ifBlank { todayFormatted }) }
    var salesExecutive by rememberSaveable { mutableStateOf("") }
    var orderType by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf("") }
    var expectedDeliveryDate by rememberSaveable { mutableStateOf(initialData?.deliveryDate.orEmpty()) }
    var deliveryMethod by rememberSaveable { mutableStateOf("") }
    var orderNotes by rememberSaveable { mutableStateOf("") }

    // ── 3. Order Items State ──
    var orderItemsList by remember {
        mutableStateOf(
            if (!initialData?.garments.isNullOrEmpty()) {
                initialData.garments.map { g ->
                    ConfiguredOrderItem(
                        garmentType = g.categoryName,
                        quantity = g.quantity,
                        stitchingPrice = g.price,
                        fabricSource = g.fabricSource,
                        fabricSelection = g.fabricType,
                        colorAccent = g.colorTone,
                        designPreset = g.pattern,
                        trialRequired = g.trialRequired
                    )
                }
            } else {
                listOf(ConfiguredOrderItem())
            }
        )
    }

    // ── 4. Pricing & Charges Calculation ──
    val subtotalAmount = orderItemsList.sumOf { (it.stitchingPrice + it.fabricPrice + it.addlWorkPrice) * it.quantity }
    val totalDiscount = orderItemsList.sumOf { it.discountPrice * it.quantity }
    val totalTax = orderItemsList.sumOf { it.taxPrice * it.quantity }
    val deliveryCharges = 0.0
    val grandTotalAmount = (subtotalAmount - totalDiscount + totalTax + deliveryCharges).coerceAtLeast(0.0)

    // ── 5. Payment & Billing Preference State ──
    var paymentType by rememberSaveable { mutableStateOf("") }
    var advanceAmount by rememberSaveable {
        mutableStateOf(
            initialData?.paidSoFar?.takeIf { it > 0 }?.let { formatIndianNumber(it) } ?: ""
        )
    }
    var paymentMode by rememberSaveable { mutableStateOf("") }
    var billingNotes by rememberSaveable { mutableStateOf("") }

    // ── 6. Attachments & References State ──
    var selectedDesignImages by rememberSaveable { mutableStateOf(initialData?.designImages ?: emptyList()) }
    var recordedVoiceNoteUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // ── Accordion Collapse/Expand State ──
    var expandedSection by rememberSaveable { mutableStateOf("customer") }

    // ── Dropdown Expansion States ──
    var customerTypeExpanded by remember { mutableStateOf(false) }
    var branchExpanded by remember { mutableStateOf(false) }
    var orderTypeExpanded by remember { mutableStateOf(false) }
    var salesExecExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var deliveryMethodExpanded by remember { mutableStateOf(false) }
    var paymentTypeExpanded by remember { mutableStateOf(false) }
    var paymentModeExpanded by remember { mutableStateOf(false) }

    val allFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) selectedDesignImages = selectedDesignImages + uris
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
                    FormLabel("Mobile Number", isRequired = true)
                    PhoneInputField(
                        phoneValue = phone,
                        onPhoneChange = { phone = it },
                        onCountryChange = { countryCode = it.code },
                        isLoading = isSearchingCustomer
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Customer Name", isRequired = true)
                    FormTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Enter customer name"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Email Address")
                    FormTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        placeholder = "Enter email address"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Customer Type",
                        value = customerType.ifEmpty { "Individual" },
                        expanded = customerTypeExpanded,
                        onExpandChange = { customerTypeExpanded = it },
                        options = listOf("Individual", "Corporate"),
                        onOptionSelected = { customerType = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    // Branch Dropdown displaying names, mapping to IDs
                    FormDropdown(
                        label = "Branch",
                        value = selectedBranchName.ifEmpty { "Select Branch" },
                        expanded = branchExpanded,
                        onExpandChange = { branchExpanded = it },
                        options = branchNames,
                        onOptionSelected = { name ->
                            selectedBranchName = name
                            selectedBranchId = branchNameToIdMap[name] ?: ""
                        },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Delivery / Billing Address")
                    FormTextArea(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = "Enter full delivery address..."
                    )

                    Spacer(Modifier.height(14.dp))

                    // + Add New Customer Action Button
                    Row(
                        modifier = Modifier
                            .clickable {
                                onAddNewCustomer()
                                salesViewModel.clearCustomerSearch()
                                fullName = ""
                                phone = ""
                                address = ""
                                emailAddress = ""
                                customerId = ""
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Customer",
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Add New Customer",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }

                    // Customer Profile Loaded Info Banner
                    AnimatedVisibility(visible = foundCustomer != null, enter = fadeIn(), exit = fadeOut()) {
                        foundCustomer?.let { cust ->
                            Spacer(Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                colors = CardDefaults.cardColors(containerColor = primary_light),
                                border = BorderStroke(1.dp, light_blue_border)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Customer Profile Loaded: ${cust.name} > ${customerType} > ${selectedBranchName.ifBlank { "Branch" }}",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Primary,
                                        lineHeight = tokens.caption * 1.3f
                                    )

                                    if (previousOrders.isNotEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier
                                                .clickable { showPreviousMeasurementsDialog = true }
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Previous measurements available",
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Bold,
                                                color = Primary
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(tokens.iconSize * 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 2. ORDER INFORMATION
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "2. Order Information",
                    expanded = expandedSection == "order_info",
                    onHeaderClick = { expandedSection = if (expandedSection == "order_info") "" else "order_info" }
                ) {
                    FormLabel("Order ID")
                    FormTextField(
                        value = orderIdText,
                        onValueChange = { orderIdText = it },
                        placeholder = "e.g. ORD-2026-0001 (auto-generated if empty)"
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
                        value = salesExecutive.ifEmpty { "Select Sales Executive" },
                        expanded = salesExecExpanded,
                        onExpandChange = { salesExecExpanded = it },
                        options = staffOptions,
                        onOptionSelected = { salesExecutive = it },
                        isRequired = false
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Order Type",
                        value = orderType.ifEmpty { "Select Order Type" },
                        expanded = orderTypeExpanded,
                        onExpandChange = { orderTypeExpanded = it },
                        options = listOf("New Stitching", "Alteration Only", "Fabric + Stitching"),
                        onOptionSelected = { orderType = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Priority",
                        value = priority.ifEmpty { "Select Priority" },
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
                        value = deliveryMethod.ifEmpty { "Select Delivery Method" },
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
                        placeholder = "Add any order-specific notes or special instructions..."
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
                        var stitchTypeExpanded by remember { mutableStateOf(false) }
                        var tailorExpanded by remember { mutableStateOf(false) }

                        // Customization dropdown states
                        var collarExpanded by remember { mutableStateOf(false) }
                        var sleeveExpanded by remember { mutableStateOf(false) }
                        var cuffExpanded by remember { mutableStateOf(false) }
                        var pocketExpanded by remember { mutableStateOf(false) }
                        var fitExpanded by remember { mutableStateOf(false) }
                        var buttonExpanded by remember { mutableStateOf(false) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "Item #${itemIndex + 1} Configuration",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            if (orderItemsList.size > 1) {
                                IconButton(
                                    onClick = {
                                        orderItemsList = orderItemsList.filterIndexed { i, _ -> i != itemIndex }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove Item", tint = redText)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                FormDropdown(
                                    label = "Garment Category",
                                    value = item.garmentCategory.ifEmpty { "Select Category" },
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
                                    value = item.garmentType.ifEmpty { "Select Type" },
                                    expanded = typeExpanded,
                                    onExpandChange = { typeExpanded = it },
                                    options = listOf("Shirt", "Pant", "Suit", "Kurta", "Blouse", "Sherwani"),
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
                            value = item.fabricSource.ifEmpty { "Select Fabric Source" },
                            expanded = sourceExpanded,
                            onExpandChange = { sourceExpanded = it },
                            options = listOf("Store Fabric", "Client Supplied", "In-House Tailoring"),
                            onOptionSelected = { item.fabricSource = it },
                            isRequired = true
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Fabric / Material Selection")
                        FormTextField(
                            value = item.fabricSelection,
                            onValueChange = { item.fabricSelection = it },
                            placeholder = "Enter fabric name / material code..."
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Design / Style Preset")
                        FormTextField(
                            value = item.designPreset,
                            onValueChange = { item.designPreset = it },
                            placeholder = "Enter design style preset..."
                        )

                        Spacer(Modifier.height(14.dp))

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
                                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                                        .clickable { showColorPicker = true }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                runCatching {
                                                    Color(parseColor(if (item.colorAccent.startsWith("#")) item.colorAccent else "#${item.colorAccent}"))
                                                }.getOrDefault(Color.Transparent)
                                            )
                                            .border(1.dp, sectionBorder, RoundedCornerShape(4.dp))
                                    )

                                    Spacer(Modifier.width(10.dp))

                                    Text(
                                        text = item.colorAccent.ifBlank { "Select Color" },
                                        fontSize = tokens.bodyMedium,
                                        color = if (item.colorAccent.isBlank()) mutedText else title_color,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = headerGrey,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (showColorPicker) {
                                    ColorPickerDialog(
                                        initialHex = if (item.colorAccent.startsWith("#")) item.colorAccent else "#3B82F6",
                                        onDismiss = { showColorPicker = false },
                                        onConfirm = { hexColor ->
                                            item.colorAccent = hexColor
                                            showColorPicker = false
                                        }
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Size Standard")
                                FormTextField(
                                    value = item.sizeStandard,
                                    onValueChange = { item.sizeStandard = it },
                                    placeholder = "e.g. 38, 40, Custom"
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        FormDropdown(
                            label = "Stitching Type",
                            value = item.stitchingType.ifEmpty { "Select Stitching Type" },
                            expanded = stitchTypeExpanded,
                            onExpandChange = { stitchTypeExpanded = it },
                            options = listOf("Normal Machine", "Hand-stitched Premium", "Double Seam"),
                            onOptionSelected = { item.stitchingType = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormDropdown(
                            label = "Tailor / Production Assignment",
                            value = item.assignedTailor.ifEmpty { "Select Tailor / Staff" },
                            expanded = tailorExpanded,
                            onExpandChange = { tailorExpanded = it },
                            options = staffOptions,
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
                            placeholder = "Add any garment-specific stitching or design instructions..."
                        )

                        Spacer(Modifier.height(16.dp))

                        // ── Customization & Design Details Card ──
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            colors = CardDefaults.cardColors(containerColor = cardBgLight),
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(modifier = Modifier.padding(tokens.screenPadding * 0.8f)) {
                                Text(
                                    text = "Customization & Design Details",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Collar Style",
                                            value = item.collarStyle.ifEmpty { "Select Collar" },
                                            expanded = collarExpanded,
                                            onExpandChange = { collarExpanded = it },
                                            options = listOf("Spread Collar", "Mandarin Collar", "Button Down"),
                                            onOptionSelected = { item.collarStyle = it }
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Sleeve Style",
                                            value = item.sleeveStyle.ifEmpty { "Select Sleeve" },
                                            expanded = sleeveExpanded,
                                            onExpandChange = { sleeveExpanded = it },
                                            options = listOf("Full Sleeve", "Half Sleeve", "Roll-up Sleeve"),
                                            onOptionSelected = { item.sleeveStyle = it }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Cuff Preference",
                                            value = item.cuffPreference.ifEmpty { "Select Cuff" },
                                            expanded = cuffExpanded,
                                            onExpandChange = { cuffExpanded = it },
                                            options = listOf("Rounded 2-Button", "French Cuff", "Single Button"),
                                            onOptionSelected = { item.cuffPreference = it }
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Pocket Style",
                                            value = item.pocketStyle.ifEmpty { "Select Pocket" },
                                            expanded = pocketExpanded,
                                            onExpandChange = { pocketExpanded = it },
                                            options = listOf("No Pocket", "Single V-Pocket", "Double Flap Pocket"),
                                            onOptionSelected = { item.pocketStyle = it }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Fitting Preference",
                                            value = item.fittingPreference.ifEmpty { "Select Fitting" },
                                            expanded = fitExpanded,
                                            onExpandChange = { fitExpanded = it },
                                            options = listOf("Slim Fit", "Regular Fit", "Tailored Fit"),
                                            onOptionSelected = { item.fittingPreference = it }
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Buttons",
                                            value = item.buttonStyle.ifEmpty { "Select Button" },
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

                    Row(
                        modifier = Modifier
                            .clickable { orderItemsList = orderItemsList + ConfiguredOrderItem() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
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
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            colors = CardDefaults.cardColors(containerColor = Primary_background),
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Item #${itemIdx + 1} Pricing (${item.garmentType.ifBlank { "Garment" }})",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        FormLabel("Stitching (₹)")
                                        FormTextField(
                                            value = if (item.stitchingPrice > 0) item.stitchingPrice.toString() else "",
                                            onValueChange = { item.stitchingPrice = it.toDoubleOrNull() ?: 0.0 },
                                            keyboardType = KeyboardType.Number,
                                            placeholder = "0"
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        FormLabel("Fabric (₹)")
                                        FormTextField(
                                            value = if (item.fabricPrice > 0) item.fabricPrice.toString() else "",
                                            onValueChange = { item.fabricPrice = it.toDoubleOrNull() ?: 0.0 },
                                            keyboardType = KeyboardType.Number,
                                            placeholder = "0"
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        FormLabel("Discount (₹)")
                                        FormTextField(
                                            value = if (item.discountPrice > 0) item.discountPrice.toString() else "",
                                            onValueChange = { item.discountPrice = it.toDoubleOrNull() ?: 0.0 },
                                            keyboardType = KeyboardType.Number,
                                            placeholder = "0"
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        FormLabel("Tax (₹)")
                                        FormTextField(
                                            value = if (item.taxPrice > 0) item.taxPrice.toString() else "",
                                            onValueChange = { item.taxPrice = it.toDoubleOrNull() ?: 0.0 },
                                            keyboardType = KeyboardType.Number,
                                            placeholder = "0"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = grey_border)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = tokens.bodySmall, color = headerGrey)
                        Text("₹${formatIndianNumber(subtotalAmount)}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Discount", fontSize = tokens.bodySmall, color = headerGrey)
                        Text("-₹${formatIndianNumber(totalDiscount)}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = redText)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Tax", fontSize = tokens.bodySmall, color = headerGrey)
                        Text("+₹${formatIndianNumber(totalTax)}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = sectionBorder)
                    Spacer(Modifier.height(10.dp))

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
                        value = paymentType.ifEmpty { "Select Payment Type" },
                        expanded = paymentTypeExpanded,
                        onExpandChange = { paymentTypeExpanded = it },
                        options = listOf("Advance", "Full Payment", "Pay on Delivery"),
                        onOptionSelected = { paymentType = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Advance Amount Received")
                    FormTextField(
                        value = advanceAmount,
                        onValueChange = { advanceAmount = it },
                        placeholder = "₹0.00",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Payment Mode",
                        value = paymentMode.ifEmpty { "Select Payment Mode" },
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
                }

                // ─────────────────────────────────────────────────────────────
                // 6. ATTACHMENTS & REFERENCES
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "6. Attachments & References",
                    expanded = expandedSection == "attachments",
                    onHeaderClick = { expandedSection = if (expandedSection == "attachments") "" else "attachments" }
                ) {
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
                                orderId = orderIdText.trim(),
                                customerId = customerId.ifBlank { initialData?.customerId ?: "" },
                                branchId = selectedBranchId.ifBlank { initialData?.branchId ?: "" },
                                fullName = fullName.trim(),
                                countryCode = countryCode.ifBlank { "+91" },
                                phone = phone.trim(),
                                gender = gender.trim(),
                                dressFor = dressFor.trim(),
                                address = address.trim(),
                                source = source.trim(),
                                orderDate = orderDate.trim(),
                                trialDate = initialData?.trialDate.orEmpty(),
                                deliveryDate = expectedDeliveryDate.trim(),
                                discount = totalDiscount,
                                paidSoFar = advanceAmount.trim().toDoubleOrNull() ?: 0.0,
                                designImages = selectedDesignImages,
                                existingImageUrls = initialData?.existingImageUrls ?: emptyList(),
                                voiceNoteUri = recordedVoiceNoteUris.firstOrNull(),
                                garments = orderItemsList.map { item ->
                                    SelectedGarment(
                                        category = item.garmentType.trim(),
                                        categoryName = item.garmentType.trim(),
                                        categoryId = item.garmentType.trim(),
                                        quantity = item.quantity,
                                        price = item.totalItemPrice,
                                        priority = priority.trim(),
                                        trialRequired = item.trialRequired,
                                        fabricSource = item.fabricSource.trim(),
                                        fabricType = item.fabricSelection.trim(),
                                        colorTone = item.colorAccent.trim(),
                                        pattern = item.designPreset.trim(),
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

    // Previous Measurements Selection Dialog
    if (showPreviousMeasurementsDialog && previousOrders.isNotEmpty()) {
        PreviousMeasurementsDialog(
            orders = previousOrders,
            onImport = { importedGarments ->
                orderItemsList = importedGarments.map { g ->
                    ConfiguredOrderItem(
                        garmentType = g.categoryName.ifBlank { g.category },
                        quantity = g.quantity,
                        stitchingPrice = 0.0,
                        fabricSource = g.fabricDetails?.fabricSource.orEmpty(),
                        fabricSelection = g.fabricDetails?.fabricType.orEmpty(),
                        colorAccent = g.fabricDetails?.color.orEmpty(),
                        designPreset = g.fabricDetails?.pattern.orEmpty(),
                        trialRequired = g.trialRequired
                    )
                }
                showPreviousMeasurementsDialog = false
            },
            onDismiss = { showPreviousMeasurementsDialog = false }
        )
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
                        Text("Previous Measurements", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Select garments to copy", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = headerGrey)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = mutedText)
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
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
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
                                                color = TextPrimary
                                            )

                                            val (statusBg, statusTextColor) = when (order.status.lowercase()) {
                                                "confirmed", "completed" -> greenBg to darkGreenBg
                                                "pending" -> yellowBg to yellowText
                                                "cancelled" -> redBg to redText
                                                else -> primary_light to Primary
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .background(statusBg, RoundedCornerShape(20.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    order.status,
                                                    fontSize = tokens.caption,
                                                    color = statusTextColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Text(
                                            "${order.orderDate.safeDate()} • ${order.garments.size} Garment${if (order.garments.size != 1) "s" else ""}",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = headerGrey
                                        )
                                    }
                                    Icon(
                                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        null,
                                        tint = headerGrey,
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
                                                            if (isSelected) Primary else whiteBg,
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) Primary else BorderGray,
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
                                                        color = TextPrimary
                                                    )
                                                    val measurementText = garment.measurementSnapshot
                                                        ?.entries
                                                        ?.take(3)
                                                        ?.joinToString(", ") { it.key }
                                                        ?: ""
                                                    if (measurementText.isNotBlank()) {
                                                        Text("$measurementText...", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = headerGrey)
                                                    }
                                                }

                                                Text(
                                                    if (isSelected) "Selected" else "Select",
                                                    fontSize = tokens.bodyMedium,
                                                    color = if (isSelected) Primary else mutedText,
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
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, grey_border)
                    ) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
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
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                    ) {
                        Icon(Icons.Default.Download, null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                        Spacer(Modifier.width(6.dp))
                        Text("Import", color = whiteBg, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
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
                Text("Choose Color", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)

                HsvColorPicker(
                    modifier = Modifier.fillMaxWidth().height(260.dp).padding(tokens.extraPadding),
                    controller = controller,
                    initialColor = parseHexColorOrNull(selectedHex) ?: Primary,
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
                            .background(parseHexColorOrNull(selectedHex) ?: grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    )
                    Text(selectedHex.uppercase(), fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, grey_border)
                    ) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onConfirm(selectedHex.uppercase()) },
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
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