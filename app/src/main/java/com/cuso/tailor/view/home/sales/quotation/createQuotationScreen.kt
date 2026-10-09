@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.cuso.tailor.view.home.sales.quotation

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.MemberItem
import com.cuso.tailor.model.hr.displayName
import com.cuso.tailor.model.inventory.TaxGroupDto
import com.cuso.tailor.model.sales.*
import com.cuso.tailor.model.settings.DesignItem
import com.cuso.tailor.model.settings.GarmentItem
import com.cuso.tailor.model.settings.GarmentStyleItem
import com.cuso.tailor.model.settings.WorkPricingItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.pdfgenerator.QuotationPdfGenerator
import com.cuso.tailor.viewmodel.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.find

// ─────────────────────────────────────────────────────────────
// DATA MODELS FOR DYNAMIC QUOTATION ITEMS
// ─────────────────────────────────────────────────────────────

data class CustomerOption(
    val id: String,
    val name: String? = "",
    val phone: String,
    val email: String = "",
    val addressLine: String = "",
    val city: String = "",
    val pincode: String = ""
)

enum class QuotationItemType {
    GARMENT,
    FABRIC,
    RETAIL_ITEM
}

data class QuotationScopeItem(
    val id: String = UUID.randomUUID().toString(),
    var itemType: QuotationItemType = QuotationItemType.GARMENT,
    var isExpanded: Boolean = true,

    // Segment info (sent in customGarment payload)
    var segmentName: String = "",

    // Garment Fields
    var garmentType: String = "",
    var garmentId: String = "",
    var garmentCategory: String = "",
    var garmentCategoryId: String = "",
    var segmentId: String = "",
    var quantity: Int = 1,
    var stitchingRate: Double = 0.0,
    var fabricSource: String = "Customer_Provided",
    var customerFabricDescription: String = "",
    var designPresetId: String = "",
    var designPresetName: String = "Select Design",
    var stitchingType: String = "Normal Machine",
    var specialNotes: String = "",
    var selectedWorkPricingIds: List<String> = emptyList(),
    var stitchingSacCode: String = "6543",

    // Fabric & Material Fields
    var selectedItemId: String = "",
    var selectedItemName: String = "",
    var selectedItemSku: String = "",
    var unit: String = "Meters",
    var rate: Double = 0.0,
    var hsnCode: String = "0984",

    // Tax Settings
    var selectedTaxGroupId: String = "",
    var taxRate: Double = 5.0
) {
    fun calculateTotalAddonsPrice(workList: List<WorkPricingItem>): Double {
        return if (itemType == QuotationItemType.GARMENT) {
            workList.filter { selectedWorkPricingIds.contains(it.id) }.sumOf { it.basePrice }
        } else 0.0
    }

    fun calculateItemSubtotal(workList: List<WorkPricingItem>): Double {
        return when (itemType) {
            QuotationItemType.GARMENT -> (stitchingRate + calculateTotalAddonsPrice(workList)) * quantity
            QuotationItemType.FABRIC, QuotationItemType.RETAIL_ITEM -> rate * quantity
        }
    }

    fun calculateItemTax(workList: List<WorkPricingItem>): Double {
        return calculateItemSubtotal(workList) * (taxRate / 100.0)
    }

    fun calculateItemTotal(workList: List<WorkPricingItem>): Double {
        return calculateItemSubtotal(workList) + calculateItemTax(workList)
    }
}

private fun formatPrice(amount: Double): String =
    "₹${String.format(Locale.US, "%,.0f", amount)}"

// ─────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────

private val helperGson = Gson()

private val INVENTORY_ID_KEYS = listOf(
    "inventoryItemId", "itemId", "productId", "variantId", "_id", "id"
)

private val PRICE_KEYS = listOf(
    "sellingPrice", "salePrice", "price", "unitPrice", "mrp"
)

private fun readJsonString(source: Any, keys: List<String>): String {
    val obj = try {
        helperGson.toJsonTree(source).asJsonObject
    } catch (_: Exception) {
        return ""
    }
    for (key in keys) {
        val element = obj.get(key) ?: continue
        if (element.isJsonPrimitive) {
            val value = try { element.asString } catch (_: Exception) { "" }
            if (value.isNotBlank()) return value
        }
    }
    return ""
}

private fun readJsonDouble(source: Any, keys: List<String>): Double? {
    val obj = try {
        helperGson.toJsonTree(source).asJsonObject
    } catch (_: Exception) {
        return null
    }
    for (key in keys) {
        val element = obj.get(key) ?: continue
        if (element.isJsonPrimitive) {
            val value = try { element.asDouble } catch (_: Exception) { null }
            if (value != null) return value
        }
    }
    return null
}

private fun String.orDefaultIfPlaceholder(fallback: String): String =
    if (this.isBlank() || this == "-") fallback else this

private fun formatIsoToDate(rawIso: String?): String {
    if (rawIso.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(rawIso.take(10)) ?: return rawIso
        SimpleDateFormat("dd-MM-yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        rawIso
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuotationScreen(
    quotationId: String? = null,
    mode: String = "create",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {},
    token: String
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current

    val customerViewModel: CustomerViewModel = hiltViewModel()
    val salesOrderViewModel: SalesOrderViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val quotationViewModel: QuotationViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val salesViewModel: SalesViewModel = hiltViewModel()
    val branchViewModel: BranchViewModel = hiltViewModel()
    val inventoryViewModel: InventoryViewModel = hiltViewModel()

    val customerState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val taxGroups by inventoryViewModel.taxGroups.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()

    val garments by settingsViewModel.garments.collectAsStateWithLifecycle()
    val garmentStyles by settingsViewModel.garmentStyles.collectAsStateWithLifecycle()
    val workPricingList by settingsViewModel.workPricingList.collectAsStateWithLifecycle()
    val designsList by settingsViewModel.designs.collectAsStateWithLifecycle()
    val fabricItemList by inventoryViewModel.fabricItemList.collectAsStateWithLifecycle()
    val accessoryItemList by inventoryViewModel.accessoryItemList.collectAsStateWithLifecycle()

    val leadList by salesViewModel.tableLeads.collectAsStateWithLifecycle()
    val isLoadingTableLeads by salesViewModel.isLoadingTableLeads.collectAsStateWithLifecycle()

    val hrViewModel: HrViewModel = hiltViewModel()
    val memberList by hrViewModel.members.collectAsStateWithLifecycle(initialValue = emptyList())

    // Multi-step Navigation State
    var currentStep by remember {
        mutableIntStateOf(if (mode == "view") 4 else 1)
    }

    var customerLeadTab by remember { mutableStateOf("Customer") }
    var customerSearchQuery by remember { mutableStateOf("") }
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    var selectedSalespersonId by remember { mutableStateOf<String?>(null) }
    var selectedBranchId by remember { mutableStateOf("") }

    // Scope & Pricing Items
    var quotationItems by remember {
        mutableStateOf(listOf(QuotationScopeItem(itemType = QuotationItemType.GARMENT)))
    }

    var overallDiscount by remember { mutableStateOf("0") }
    var deliveryCharge by remember { mutableStateOf("0") }
    val defaultExpiry = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 21) }
        SimpleDateFormat("dd-MM-yyyy", Locale.US).format(cal.time)
    }
    var validityExpiryDate by remember { mutableStateOf(defaultExpiry) }
    var termsAndConditionsText by remember {
        mutableStateOf("50% advance required to confirm order. First fitting provided within 7 days.")
    }

    var previewShown by remember { mutableStateOf(mode == "view") }
    var isPrefilling by remember { mutableStateOf(quotationId != null) }

    var showDesignDialog by remember { mutableStateOf(false) }
    var activeDesignTargetItemId by remember { mutableStateOf("") }

    val defaultTaxGroup = remember(taxGroups) {
        taxGroups.find { it.totalRate == 5.0 } ?: taxGroups.firstOrNull()
    }

    fun withDefaultTax(list: List<QuotationScopeItem>): List<QuotationScopeItem> =
        list.map {
            if (it.selectedTaxGroupId.isBlank() && defaultTaxGroup != null) {
                it.copy(selectedTaxGroupId = defaultTaxGroup.id, taxRate = defaultTaxGroup.totalRate)
            } else it
        }

    // Initial Master APIs Loading
    LaunchedEffect(Unit) {
        salesViewModel.fetchStaff()
        settingsViewModel.fetchGarments()
        settingsViewModel.fetchWorkPricing(status = "Active")
        settingsViewModel.fetchDesigns()
        branchViewModel.loadBranches()
        hrViewModel.fetchMembers()
        inventoryViewModel.fetchTaxGroups()
        inventoryViewModel.fetchFabricList()
        inventoryViewModel.fetchAccessoryList()
        profileViewModel.loadOrganization(token)
        if (quotationId != null) {
            quotationViewModel.fetchQuotationById(quotationId)
        }
    }

    LaunchedEffect(customerLeadTab) {
        if (customerLeadTab == "Customer") {
            customerViewModel.loadCustomers()
        } else {
            salesViewModel.fetchTableLeads()
        }
    }

    LaunchedEffect(taxGroups, branchState, staffList) {
        if (taxGroups.isNotEmpty()) {
            val defaultGroup = taxGroups.find { it.totalRate == 5.0 } ?: taxGroups.first()
            quotationItems = quotationItems.map {
                if (it.selectedTaxGroupId.isBlank()) {
                    it.copy(selectedTaxGroupId = defaultGroup.id, taxRate = defaultGroup.totalRate)
                } else it
            }
        }
        if (selectedBranchId.isBlank()) {
            val branches = (branchState as? BranchUiState.Success)?.branches ?: emptyList()
            if (branches.isNotEmpty()) {
                selectedBranchId = branches.first().id
            }
        }
        if (selectedSalespersonId.isNullOrBlank() && staffList.isNotEmpty()) {
            selectedSalespersonId = staffList.first().id
        }
    }

    val customers = remember(customerState) {
        when (customerState) {
            is CustomerUiState.Success -> {
                (customerState as CustomerUiState.Success).customers.map { customer ->
                    CustomerOption(
                        id = customer.id,
                        name = customer.name,
                        phone = customer.mobile ?: "",
                        email = customer.email.orEmpty(),
                        addressLine = customer.address?.addressLine ?: "",
                        city = customer.address?.city ?: "",
                        pincode = customer.address?.pincode ?: ""
                    )
                }
            }
            else -> emptyList()
        }
    }

    val leads = remember(leadList) {
        leadList.map { lead ->
            val resolvedName = lead.name.takeIf { it.isNotBlank() && it != "—" }
                ?: lead.fullName.orEmpty().ifBlank { "Lead" }
            CustomerOption(
                id = lead.id,
                name = resolvedName,
                phone = lead.phone,
                addressLine = "",
                city = "",
                pincode = ""
            )
        }
    }

    val currentRawItems = if (customerLeadTab == "Customer") customers else leads
    val filteredItems = remember(currentRawItems, customerSearchQuery) {
        if (customerSearchQuery.isBlank()) currentRawItems
        else currentRawItems.filter {
            (it.name ?: "").contains(customerSearchQuery, ignoreCase = true) ||
                    it.phone.contains(customerSearchQuery, ignoreCase = true)
        }
    }

    val selectedCustomer = remember(currentRawItems, selectedCustomerId) {
        currentRawItems.find { it.id == selectedCustomerId }
    }

    // Totals Calculation
    val totalConfiguredSubtotal = quotationItems.sumOf { it.calculateItemSubtotal(workPricingList) }
    val totalConfiguredTax = quotationItems.sumOf { it.calculateItemTax(workPricingList) }
    val parsedDiscount = overallDiscount.toDoubleOrNull() ?: 0.0
    val parsedDelivery = deliveryCharge.toDoubleOrNull() ?: 0.0
    val totalConfiguredValue = (totalConfiguredSubtotal - parsedDiscount + totalConfiguredTax + parsedDelivery).coerceAtLeast(0.0)

    val detailState by quotationViewModel.detailState.collectAsStateWithLifecycle()

    // ── Prefill Logic in Edit Mode ──
    LaunchedEffect(detailState) {
        when (val state = detailState) {
            is QuotationDetailUiState.Success -> {
                val dto = state.quotation

                // 1. Customer or Lead
                if (dto.leadId != null && !dto.leadId.id.isNullOrBlank()) {
                    customerLeadTab = "Lead"
                    selectedCustomerId = dto.leadId.id
                } else if (dto.customerId != null && !dto.customerId.id.isNullOrBlank()) {
                    customerLeadTab = "Customer"
                    selectedCustomerId = dto.customerId.id
                }

                // 2. Salesperson & Branch
                dto.salespersonId?.id?.takeIf { it.isNotBlank() }?.let {
                    selectedSalespersonId = it
                }
                dto.branchId?.takeIf { it.isNotBlank() }?.let {
                    selectedBranchId = it
                }

                // 3. Discount, Delivery, Validity & Notes
                overallDiscount = if (dto.discountAmount > 0) dto.discountAmount.toInt().toString() else "0"
                deliveryCharge = if (dto.deliveryCharge > 0) dto.deliveryCharge.toInt().toString() else "0"
                if (!dto.expiryDate.isNullOrBlank()) {
                    validityExpiryDate = formatIsoToDate(dto.expiryDate)
                }
                if (!dto.notes.isNullOrBlank()) {
                    termsAndConditionsText = dto.notes
                }

                // 4. Quotation Items Mapping (Preserves _id for update)
                val rawItems = dto.items
                val mainLines = rawItems.filter { it.lineType != "Garment_Addon" }
                val addonLines = rawItems.filter { it.lineType == "Garment_Addon" }

                val mappedItems = mainLines.map { line ->
                    val lineId = line.id.orEmpty()
                    val lineTaxGroupId = line.taxGroupId?.id.orEmpty()
                    val taxRateVal = line.taxGroupId?.totalRate ?: 5.0

                    when (line.lineType) {
                        "Custom_Garment" -> {
                            val cg = line.customGarment
                            val matchedAddons = addonLines.filter { it.parentLineId == lineId }
                            val addonWorkIds = matchedAddons.mapNotNull { it.addonWork?.workPricingId?.id }

                            QuotationScopeItem(
                                id = if (lineId.length == 24) lineId else UUID.randomUUID().toString(),
                                itemType = QuotationItemType.GARMENT,
                                isExpanded = false,
                                segmentId = cg?.segmentId?.id.orEmpty(),
                                segmentName = cg?.segmentName.orEmpty(),
                                garmentId = cg?.garmentId?.id.orEmpty(),
                                garmentType = cg?.garmentName.orEmpty(),
                                garmentCategoryId = cg?.garmentCategoryId?.id.orEmpty(),
                                garmentCategory = cg?.categoryDisplayName ?: cg?.garmentName.orEmpty(),
                                quantity = line.quantity.toInt().coerceAtLeast(1),
                                stitchingRate = line.unitPrice,
                                fabricSource = cg?.fabricSource ?: "Customer_Provided",
                                customerFabricDescription = cg?.fabricNotes.orEmpty(),
                                designPresetId = cg?.designId?.id.orEmpty(),
                                designPresetName = cg?.designName?.takeIf { it.isNotBlank() } ?: "Select Design",
                                stitchingType = cg?.stitchingType ?: "Normal Machine",
                                specialNotes = cg?.specialInstructions.orEmpty(),
                                selectedWorkPricingIds = addonWorkIds,
                                stitchingSacCode = line.sacCode ?: "6543",
                                selectedTaxGroupId = lineTaxGroupId,
                                taxRate = taxRateVal
                            )
                        }

                        "Garment_Material" -> {
                            QuotationScopeItem(
                                id = if (lineId.length == 24) lineId else UUID.randomUUID().toString(),
                                itemType = QuotationItemType.FABRIC,
                                isExpanded = false,
                                selectedItemId = line.product?.inventoryItemId?.id.orEmpty(),
                                selectedItemName = line.product?.itemName ?: line.itemDescription.orEmpty(),
                                selectedItemSku = line.product?.sku.orEmpty(),
                                unit = line.unit ?: "Meters",
                                rate = line.unitPrice,
                                quantity = line.quantity.toInt().coerceAtLeast(1),
                                hsnCode = line.hsnCode ?: "0984",
                                selectedTaxGroupId = lineTaxGroupId,
                                taxRate = taxRateVal
                            )
                        }

                        else -> { // Retail_Product
                            QuotationScopeItem(
                                id = if (lineId.length == 24) lineId else UUID.randomUUID().toString(),
                                itemType = QuotationItemType.RETAIL_ITEM,
                                isExpanded = false,
                                selectedItemId = line.product?.inventoryItemId?.id.orEmpty(),
                                selectedItemName = line.product?.itemName ?: line.itemDescription.orEmpty(),
                                selectedItemSku = line.product?.sku.orEmpty(),
                                unit = line.unit ?: "Pieces",
                                rate = line.unitPrice,
                                quantity = line.quantity.toInt().coerceAtLeast(1),
                                hsnCode = line.hsnCode ?: "1234",
                                selectedTaxGroupId = lineTaxGroupId,
                                taxRate = taxRateVal
                            )
                        }
                    }
                }

                if (mappedItems.isNotEmpty()) {
                    quotationItems = mappedItems
                }

                isPrefilling = false
            }
            is QuotationDetailUiState.Error -> isPrefilling = false
            else -> if (quotationId == null) isPrefilling = false
        }
    }

    val organizationLogoUrl = remember(profileState) {
        (profileState as? ProfileUiState.Success)?.data?.organization?.organizationPicture ?: ""
    }
    var logoBase64 by remember { mutableStateOf("") }
    LaunchedEffect(organizationLogoUrl) {
        if (organizationLogoUrl.isNotEmpty()) {
            logoBase64 = withContext(Dispatchers.IO) {
                try {
                    val bytes = java.net.URL(organizationLogoUrl).readBytes()
                    "data:image/png;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                } catch (_: Exception) { "" }
            }
        }
    }

    fun goToNextStep() {
        when (currentStep) {
            1 -> {
                if (selectedCustomerId == null) {
                    DynamicIslandManager.showError("Please select a customer or lead first")
                } else {
                    currentStep = 2
                }
            }
            2 -> {
                val hasInvalidGarment = quotationItems.any {
                    it.itemType == QuotationItemType.GARMENT && (it.garmentType.isBlank() || it.garmentCategory.isBlank())
                }
                val hasInvalidFabric = quotationItems.any {
                    it.itemType == QuotationItemType.FABRIC &&
                            (it.selectedItemName.isBlank() || it.selectedItemId.isBlank())
                }
                val hasInvalidItem = quotationItems.any {
                    it.itemType == QuotationItemType.RETAIL_ITEM &&
                            (it.selectedItemName.isBlank() || it.selectedItemId.isBlank())
                }

                if (hasInvalidGarment || hasInvalidFabric || hasInvalidItem) {
                    DynamicIslandManager.showError("Please complete configuration for all items")
                } else {
                    currentStep = 3
                }
            }
            3 -> {
                previewShown = true
                currentStep = 4
            }
            4 -> onSave()
        }
    }

    fun goToPreviousStep() {
        if (currentStep > 1) currentStep--
    }

    if (isPrefilling) {
        ListSkeleton()
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (currentStep > 1) goToPreviousStep() else onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = title_color)
                    }
                    Text(
                        text = "Sales",
                        fontSize = tokens.h1,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = close_color,
                    modifier = Modifier.size(tokens.iconSize * 1.2f).clickable { onClose() }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    if (currentStep < 4) Modifier.verticalScroll(rememberScrollState())
                    else Modifier
                )
        ) {
            QuotationFourStepper(currentStep = currentStep)

            when (currentStep) {
                1 -> Step1CustomerSelectionScreen(
                    tab = customerLeadTab,
                    onTabChange = {
                        customerLeadTab = it
                        selectedCustomerId = null
                    },
                    searchQuery = customerSearchQuery,
                    onSearchQueryChange = { customerSearchQuery = it },
                    items = filteredItems,
                    selectedId = selectedCustomerId,
                    onSelect = { selectedCustomerId = it },
                    isLoading = when (customerLeadTab) {
                        "Customer" -> customerState is CustomerUiState.Loading
                        else -> isLoadingTableLeads
                    },
                    memberList = memberList,
                    selectedSalespersonId = selectedSalespersonId,
                    onSelectSalesperson = { selectedSalespersonId = it },
                    branchState = branchState,
                    selectedBranchId = selectedBranchId,
                    onSelectBranch = { selectedBranchId = it }
                )

                2 -> Step2ConfigureQuotationScope(
                    items = quotationItems,
                    garments = garments,
                    garmentStyles = garmentStyles,
                    fabricOptions = fabricItemList.map { it.name },
                    accessoryOptions = accessoryItemList.map { it.name },
                    onSelectFabricItem = { name, item ->
                        val selected = fabricItemList.find { it.name == name }
                        if (selected != null) {
                            val inventoryId = readJsonString(selected, INVENTORY_ID_KEYS)
                            if (inventoryId.isBlank()) {
                                Toast.makeText(context, "Selected fabric has no inventory id", Toast.LENGTH_SHORT).show()
                            } else {
                                quotationItems = quotationItems.map {
                                    if (it.id == item.id) it.copy(
                                        selectedItemName = selected.name,
                                        selectedItemId = inventoryId,
                                        selectedItemSku = selected.sku,
                                        rate = selected.sellingPrice
                                    ) else it
                                }
                            }
                        }
                    },
                    onSelectAccessoryItem = { name, item ->
                        val selected = accessoryItemList.find { it.name == name }
                        if (selected != null) {
                            val inventoryId = readJsonString(selected, INVENTORY_ID_KEYS)
                            if (inventoryId.isBlank()) {
                                Toast.makeText(context, "Selected item has no inventory id", Toast.LENGTH_SHORT).show()
                            } else {
                                quotationItems = quotationItems.map {
                                    if (it.id == item.id) it.copy(
                                        selectedItemName = selected.name,
                                        selectedItemId = inventoryId,
                                        selectedItemSku = selected.sku,
                                        rate = readJsonDouble(selected, PRICE_KEYS) ?: 0.0
                                    ) else it
                                }
                            }
                        }
                    },
                    workPricingList = workPricingList,
                    onItemsChange = { quotationItems = withDefaultTax(it) },
                    onOpenDesignDialog = { itemId ->
                        activeDesignTargetItemId = itemId
                        showDesignDialog = true
                    },
                    onFetchGarmentStyles = { garmentId ->
                        settingsViewModel.fetchGarmentStyles(segmentId = null, garmentId = garmentId)
                    },
                    subtotal = totalConfiguredSubtotal,
                    tax = totalConfiguredTax,
                    totalValue = totalConfiguredValue
                )

                3 -> Step3PricingAndEstimationCharges(
                    items = quotationItems,
                    taxGroups = taxGroups,
                    workPricingList = workPricingList,
                    onItemsChange = { quotationItems = withDefaultTax(it) },
                    overallDiscount = overallDiscount,
                    onDiscountChange = { overallDiscount = it },
                    deliveryCharge = deliveryCharge,
                    onDeliveryChange = { deliveryCharge = it },
                    validityExpiryDate = validityExpiryDate,
                    onExpiryDateChange = { validityExpiryDate = it },
                    termsNotes = termsAndConditionsText,
                    onTermsNotesChange = { termsAndConditionsText = it },
                    subtotal = totalConfiguredSubtotal,
                    tax = totalConfiguredTax,
                    totalValue = totalConfiguredValue
                )

                4 -> Step4SummaryAndPreview(
                    token = token,
                    previewShown = previewShown,
                    onPreview = { previewShown = true },
                    onComplete = { onSave() },
                    customerName = selectedCustomer?.name ?: "Valued Customer",
                    customerPhone = selectedCustomer?.phone ?: "",
                    customerEmail = selectedCustomer?.email.orEmpty(),
                    isLead = customerLeadTab == "Lead",
                    logoBase64 = logoBase64,
                    subtotal = totalConfiguredSubtotal,
                    tax = totalConfiguredTax,
                    total = totalConfiguredValue,
                    discount = parsedDiscount,
                    deliveryCharge = parsedDelivery,
                    expiryDate = validityExpiryDate,
                    termsNotes = termsAndConditionsText,
                    items = quotationItems,
                    workPricingList = workPricingList,
                    customerId = selectedCustomerId,
                    salespersonId = selectedSalespersonId ?: staffList.firstOrNull()?.id.orEmpty(),
                    branchId = selectedBranchId.ifBlank { (branchState as? BranchUiState.Success)?.branches?.firstOrNull()?.id.orEmpty() },
                    quotationViewModel = quotationViewModel,
                    quotationId = quotationId,
                    mode = mode,
                    onEdit = {
                        previewShown = false
                        currentStep = 2
                    }
                )
            }

            Spacer(Modifier.height(tokens.buttonHeight * 2))
        }

        // Stepper Navigation FAB
        if (!(currentStep == 4 && previewShown) && mode != "view") {
            StepNavigationFab(
                showBack = currentStep > 1,
                onBack = { goToPreviousStep() },
                backLabel = if (currentStep == 1) "Cancel" else "Previous",
                trailingAction = when (currentStep) {
                    1 -> TrailingFabAction.Next(label = "Next", onClick = { goToNextStep() })
                    2 -> TrailingFabAction.Next(label = "Next to Pricing", onClick = { goToNextStep() })
                    3 -> TrailingFabAction.Next(label = "Preview Quotation", onClick = { goToNextStep() })
                    4 -> TrailingFabAction.Next(label = if (mode == "edit") "Update Quotation" else "Save as Draft", onClick = { onSave() })
                    else -> TrailingFabAction.Next(label = "Next", onClick = { goToNextStep() })
                },
                backWidthFraction = 0.35f,
                trailingWidthFraction = 0.50f
            )
        }

        // Design Selection Dialog
        if (showDesignDialog) {
            SelectQuotationDesignDialog(
                designs = designsList,
                onDismiss = { showDesignDialog = false },
                onDesignSelected = { design ->
                    quotationItems = quotationItems.map { itm ->
                        if (itm.id == activeDesignTargetItemId) {
                            itm.copy(designPresetId = design.id, designPresetName = design.name)
                        } else itm
                    }
                    showDesignDialog = false
                }
            )
        }
    }
}


@Composable
private fun Step1CustomerSelectionScreen(
    tab: String,
    onTabChange: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    items: List<CustomerOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    isLoading: Boolean,
    memberList: List<MemberItem>,
    selectedSalespersonId: String?,
    onSelectSalesperson: (String) -> Unit,
    branchState: BranchUiState,
    selectedBranchId: String,
    onSelectBranch: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val branches = (branchState as? BranchUiState.Success)?.branches ?: emptyList()
    var executiveDropdownExpanded by remember { mutableStateOf(false) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }

    val currentExecutiveDisplay = memberList
        .find { it._id == selectedSalespersonId }
        ?.displayName()
        ?.takeIf { it.isNotBlank() && it != "—" }
        ?: "Select Executive"

    val currentBranchDisplay = branches
        .find { it.id == selectedBranchId }
        ?.name
        ?.takeIf { it.isNotBlank() }
        ?: "Select Branch"

    val executiveOptions = remember(memberList) {
        memberList
            .map { it.displayName() }
            .filter { it.isNotBlank() && it != "—" }
            .ifEmpty { listOf("Loading...") }
    }

    val branchOptions = remember(branches) {
        branches
            .mapNotNull { it.name?.takeIf { n -> n.isNotBlank() } }
            .ifEmpty { listOf("Loading...") }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FormDropdown(
                    label = "Sales Executive *",
                    isRequired = true,
                    value = currentExecutiveDisplay,
                    expanded = executiveDropdownExpanded,
                    onExpandChange = { executiveDropdownExpanded = it },
                    options = executiveOptions,
                    onOptionSelected = { chosenName ->
                        memberList.find { it.displayName() == chosenName }?.let {
                            onSelectSalesperson(it._id)
                        }
                    }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                FormDropdown(
                    label = "Branch *",
                    isRequired = true,
                    value = currentBranchDisplay,
                    expanded = branchDropdownExpanded,
                    onExpandChange = { branchDropdownExpanded = it },
                    options = branchOptions,
                    onOptionSelected = { name ->
                        branches.find { it.name == name }?.let {
                            onSelectBranch(it.id)
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search customers by name or phone...", fontSize = tokens.bodySmall, color = mutedText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize)) },
            modifier = Modifier.fillMaxWidth().height(tokens.fieldHeight),
            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = BorderGray,
                focusedContainerColor = whiteBg,
                unfocusedContainerColor = whiteBg
            )
        )

        Spacer(Modifier.height(14.dp))

        CustomerLeadToggle(selected = tab, onSelect = onTabChange)

        Spacer(Modifier.height(14.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = tokens.extraPadding * 2), contentAlignment = Alignment.Center) {
                CirculerProgressIndicatorReuse()
            }
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = tokens.extraPadding * 2), contentAlignment = Alignment.Center) {
                Text(text = "No records found", fontSize = tokens.bodyMedium, color = mutedText)
            }
        } else {
            items.forEach { item ->
                CustomerSelectionCard(
                    customer = item,
                    selected = item.id == selectedId,
                    onSelect = { onSelect(item.id) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 2 — Scope & Items Configuration
// ─────────────────────────────────────────────────────────────
@Composable
private fun Step2ConfigureQuotationScope(
    items: List<QuotationScopeItem>,
    garments: List<GarmentItem>,
    garmentStyles: List<GarmentStyleItem>,
    fabricOptions: List<String>,
    accessoryOptions: List<String>,
    onSelectFabricItem: (String, QuotationScopeItem) -> Unit,
    onSelectAccessoryItem: (String, QuotationScopeItem) -> Unit,
    workPricingList: List<WorkPricingItem>,
    onItemsChange: (List<QuotationScopeItem>) -> Unit,
    onOpenDesignDialog: (String) -> Unit,
    onFetchGarmentStyles: (String) -> Unit,
    subtotal: Double,
    tax: Double,
    totalValue: Double
) {
    val tokens = LocalAppTokens.current
    var showAddItemMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = tokens.extraPadding * 0.8f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Configure Quotation Scope",
                fontSize = tokens.h2,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${items.size} Item(s) Configured",
                fontSize = tokens.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(8.dp))

        items.forEachIndexed { index, item ->
            val itemNum = index + 1
            when (item.itemType) {
                QuotationItemType.GARMENT -> {
                    ScopeGarmentCard(
                        itemNumber = itemNum,
                        item = item,
                        garments = garments,
                        garmentStyles = garmentStyles,
                        workPricingList = workPricingList,
                        onUpdate = { updatedItem ->
                            onItemsChange(items.map { if (it.id == item.id) updatedItem else it })
                        },
                        onDelete = {
                            if (items.size > 1) onItemsChange(items.filterNot { it.id == item.id })
                        },
                        onOpenDesignDialog = { onOpenDesignDialog(item.id) },
                        onFetchGarmentStyles = onFetchGarmentStyles
                    )
                }
                QuotationItemType.FABRIC -> {
                    ScopeFabricCard(
                        itemNumber = itemNum,
                        item = item,
                        fabricOptions = fabricOptions,
                        onSelectFabric = { onSelectFabricItem(it, item) },
                        onUpdate = { updatedItem ->
                            onItemsChange(items.map { if (it.id == item.id) updatedItem else it })
                        },
                        onDelete = {
                            if (items.size > 1) onItemsChange(items.filterNot { it.id == item.id })
                        }
                    )
                }
                QuotationItemType.RETAIL_ITEM -> {
                    ScopeAccessoryCard(
                        itemNumber = itemNum,
                        item = item,
                        accessoryOptions = accessoryOptions,
                        onSelectAccessory = { onSelectAccessoryItem(it, item) },
                        onUpdate = { updatedItem ->
                            onItemsChange(items.map { if (it.id == item.id) updatedItem else it })
                        },
                        onDelete = {
                            if (items.size > 1) onItemsChange(items.filterNot { it.id == item.id })
                        }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { showAddItemMenu = true },
                color = whiteBg
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                    Spacer(Modifier.width(6.dp))
                    Text("Add New Item", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                }
            }

            DropdownMenu(
                expanded = showAddItemMenu,
                onDismissRequest = { showAddItemMenu = false },
                modifier = Modifier.background(whiteBg)
            ) {
                DropdownMenuItem(
                    text = { Text("Add Garment") },
                    leadingIcon = { Icon(Icons.Default.Checkroom, contentDescription = null, tint = Primary) },
                    onClick = {
                        showAddItemMenu = false
                        onItemsChange(items + QuotationScopeItem(itemType = QuotationItemType.GARMENT))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add Fabric") },
                    leadingIcon = { Icon(Icons.Default.Texture, contentDescription = null, tint = Color(0xFF0D9488)) },
                    onClick = {
                        showAddItemMenu = false
                        onItemsChange(items + QuotationScopeItem(itemType = QuotationItemType.FABRIC, unit = "Meters", hsnCode = "0984"))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add Item") },
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = darkGreenBg) },
                    onClick = {
                        showAddItemMenu = false
                        onItemsChange(items + QuotationScopeItem(itemType = QuotationItemType.RETAIL_ITEM, unit = "Pieces", hsnCode = "1234"))
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        EstimationBreakdownCard(
            totalItems = items.size,
            subtotal = subtotal,
            tax = tax,
            totalValue = totalValue
        )
    }
}

@Composable
private fun ScopeGarmentCard(
    itemNumber: Int,
    item: QuotationScopeItem,
    garments: List<GarmentItem>,
    garmentStyles: List<GarmentStyleItem>,
    workPricingList: List<WorkPricingItem>,
    onUpdate: (QuotationScopeItem) -> Unit,
    onDelete: () -> Unit,
    onOpenDesignDialog: () -> Unit,
    onFetchGarmentStyles: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Primary) {
                        Text(
                            text = "Item #$itemNumber",
                            color = whiteBg,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Column {
                        Text(
                            text = item.garmentCategory.ifBlank { item.garmentType.ifBlank { "New Garment" } },
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(text = "Qty: ${item.quantity}", fontSize = tokens.caption, color = TextSecondary)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = redText)
                    }
                    IconButton(onClick = { onUpdate(item.copy(isExpanded = !item.isExpanded)) }, modifier = Modifier.size(32.dp)) {
                        Icon(if (item.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                    }
                }
            }

            AnimatedVisibility(visible = item.isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    var typeExpanded by remember { mutableStateOf(false) }
                    val garmentTypeOptions = remember(garments) { garments.map { it.displayName ?: it.name } }

                    FormDropdown(
                        label = "Garment Type",
                        isRequired = true,
                        value = item.garmentType.ifBlank { "Select Garment" },
                        expanded = typeExpanded,
                        onExpandChange = { typeExpanded = it },
                        options = garmentTypeOptions.ifEmpty { listOf("Loading...") },
                        onOptionSelected = { chosenName ->
                            val chosenGarment = garments.find { (it.displayName ?: it.name) == chosenName }
                            if (chosenGarment != null) {
                                val segment = chosenGarment.applicableSegments.firstOrNull()
                                onFetchGarmentStyles(chosenGarment.id)
                                onUpdate(
                                    item.copy(
                                        garmentType = chosenName,
                                        garmentId = chosenGarment.id,
                                        garmentCategory = "",
                                        garmentCategoryId = "",
                                        segmentId = segment?.id.orEmpty(),
                                        segmentName = segment?.name.orEmpty(),
                                        stitchingRate = chosenGarment.baseStitchingCharge
                                    )
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    var catExpanded by remember { mutableStateOf(false) }
                    val relevantStyles = remember(garmentStyles, item.garmentId) { garmentStyles.filter { it.garment?.id == item.garmentId } }
                    val categoryOptions = remember(relevantStyles) { relevantStyles.map { it.displayName ?: it.name } }

                    FormDropdown(
                        label = "Garment Category",
                        isRequired = true,
                        value = item.garmentCategory.ifBlank { "Select Category" },
                        expanded = catExpanded,
                        onExpandChange = { catExpanded = it },
                        options = if (item.garmentId.isBlank()) listOf("Select Garment Type first") else categoryOptions.ifEmpty { listOf("No categories") },
                        onOptionSelected = { catName ->
                            val chosenCat = relevantStyles.find { (it.displayName ?: it.name) == catName }
                            val effectiveRate = if ((chosenCat?.stitchingCharge ?: 0.0) > 0.0) chosenCat!!.stitchingCharge else item.stitchingRate

                            onUpdate(
                                item.copy(
                                    garmentCategory = catName,
                                    garmentCategoryId = chosenCat?.id.orEmpty(),
                                    segmentId = chosenCat?.segment?.id ?: item.segmentId,
                                    segmentName = chosenCat?.segment?.name?.takeIf { it.isNotBlank() } ?: item.segmentName,
                                    stitchingRate = effectiveRate
                                )
                            )
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    FormLabel("Quantity", isRequired = true)
                    FormTextField(
                        value = if (item.quantity == 0) "" else item.quantity.toString(),
                        onValueChange = { str ->
                            val parsed = str.filter { it.isDigit() }.toIntOrNull() ?: 1
                            onUpdate(item.copy(quantity = parsed))
                        },
                        placeholder = "1",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(12.dp))

                    FormLabel("Stitching Rate", isRequired = true)
                    FormTextField(
                        value = if (item.stitchingRate == 0.0) "" else item.stitchingRate.toInt().toString(),
                        onValueChange = { str ->
                            val parsed = str.toDoubleOrNull() ?: 0.0
                            onUpdate(item.copy(stitchingRate = parsed))
                        },
                        placeholder = "2000",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(12.dp))

                    var fabricSourceExpanded by remember { mutableStateOf(false) }
                    FormDropdown(
                        label = "Fabric Source",
                        isRequired = true,
                        value = item.fabricSource,
                        expanded = fabricSourceExpanded,
                        onExpandChange = { fabricSourceExpanded = it },
                        options = listOf("Customer_Provided" , "Store_Provided"),
                        onOptionSelected = { onUpdate(item.copy(fabricSource = it)) }
                    )

                    Spacer(Modifier.height(12.dp))

                    FormLabel("Customer Fabric Description")
                    FormTextField(
                        value = item.customerFabricDescription,
                        onValueChange = { onUpdate(item.copy(customerFabricDescription = it)) },
                        placeholder = "e.g. Customer provided silk cloth"
                    )

                    Spacer(Modifier.height(12.dp))

                    FormLabel("Design / Style Preset")
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .clickable { onOpenDesignDialog() },
                        color = whiteBg
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = item.designPresetName, fontSize = tokens.bodySmall, color = if (item.designPresetName == "Select Design") TextSecondary else TextPrimary)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    var stitchTypeExpanded by remember { mutableStateOf(false) }
                    FormDropdown(
                        label = "Stitching Type",
                        value = item.stitchingType,
                        expanded = stitchTypeExpanded,
                        onExpandChange = { stitchTypeExpanded = it },
                        options = listOf("Normal Machine", "Hand Finished", "Double Seam"),
                        onOptionSelected = { onUpdate(item.copy(stitchingType = it)) }
                    )

                    Spacer(Modifier.height(12.dp))

                    FormLabel("Special Notes (Optional)")
                    FormTextArea(
                        value = item.specialNotes,
                        onValueChange = { onUpdate(item.copy(specialNotes = it)) },
                        placeholder = "Specific cut guidelines, pattern matching..."
                    )

                    Spacer(Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = primary_light.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "CUSTOMIZATION (MULTI-SELECT)", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))

                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                workPricingList.forEach { workItem ->
                                    val isSelected = item.selectedWorkPricingIds.contains(workItem.id)

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) primary_light else whiteBg,
                                        border = BorderStroke(1.dp, if (isSelected) Primary else BorderGray),
                                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable {
                                            val updatedIds = if (isSelected) item.selectedWorkPricingIds - workItem.id else item.selectedWorkPricingIds + workItem.id
                                            onUpdate(item.copy(selectedWorkPricingIds = updatedIds))
                                        }
                                    ) {
                                        Text(
                                            text = if (isSelected) "✓ ${workItem.workType} (₹${workItem.basePrice.toInt()})" else "+ ${workItem.workType} (₹${workItem.basePrice.toInt()})",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) Primary else TextPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopeFabricCard(
    itemNumber: Int,
    item: QuotationScopeItem,
    fabricOptions: List<String>,
    onSelectFabric: (String) -> Unit,
    onUpdate: (QuotationScopeItem) -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var fabDropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0D9488)) {
                        Text(
                            text = "Item #$itemNumber",
                            color = whiteBg,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Column {
                        Text(
                            text = item.selectedItemName.ifBlank { "Store Fabric" },
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(text = "Qty: ${item.quantity} Meters", fontSize = tokens.caption, color = TextSecondary)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = redText)
                    }
                    IconButton(onClick = { onUpdate(item.copy(isExpanded = !item.isExpanded)) }, modifier = Modifier.size(32.dp)) {
                        Icon(if (item.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                    }
                }
            }

            AnimatedVisibility(visible = item.isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    FormDropdown(
                        label = "Fabric Selection",
                        isRequired = true,
                        value = item.selectedItemName.ifBlank { "Select Fabric" },
                        expanded = fabDropdownExpanded,
                        onExpandChange = { fabDropdownExpanded = it },
                        options = fabricOptions.ifEmpty { listOf("Loading fabrics...") },
                        onOptionSelected = { chosenName ->
                            onSelectFabric(chosenName)
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Quantity (Meters)", isRequired = true)
                            FormTextField(
                                value = item.quantity.toString(),
                                onValueChange = { onUpdate(item.copy(quantity = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 1)) },
                                keyboardType = KeyboardType.Number
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Estimated Rate / Meter", isRequired = true)
                            FormTextField(
                                value = if (item.rate == 0.0) "" else item.rate.toInt().toString(),
                                onValueChange = { onUpdate(item.copy(rate = it.toDoubleOrNull() ?: 0.0)) },
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopeAccessoryCard(
    itemNumber: Int,
    item: QuotationScopeItem,
    accessoryOptions: List<String>,
    onSelectAccessory: (String) -> Unit,
    onUpdate: (QuotationScopeItem) -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var itemDropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = darkGreenBg) {
                        Text(
                            text = "Item #$itemNumber",
                            color = whiteBg,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Column {
                        Text(
                            text = item.selectedItemName.ifBlank { "Accessory Item" },
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(text = "Qty: ${item.quantity} Pieces", fontSize = tokens.caption, color = TextSecondary)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = redText)
                    }
                    IconButton(onClick = { onUpdate(item.copy(isExpanded = !item.isExpanded)) }, modifier = Modifier.size(32.dp)) {
                        Icon(if (item.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                    }
                }
            }

            AnimatedVisibility(visible = item.isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    FormDropdown(
                        label = "Item Selection",
                        isRequired = true,
                        value = item.selectedItemName.ifBlank { "Select Item" },
                        expanded = itemDropdownExpanded,
                        onExpandChange = { itemDropdownExpanded = it },
                        options = accessoryOptions.ifEmpty { listOf("Loading accessories...") },
                        onOptionSelected = { chosenName ->
                            onSelectAccessory(chosenName)
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Quantity (Pieces)", isRequired = true)
                            FormTextField(
                                value = item.quantity.toString(),
                                onValueChange = { onUpdate(item.copy(quantity = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 1)) },
                                keyboardType = KeyboardType.Number
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Estimated Price / Unit", isRequired = true)
                            FormTextField(
                                value = if (item.rate == 0.0) "" else item.rate.toInt().toString(),
                                onValueChange = { onUpdate(item.copy(rate = it.toDoubleOrNull() ?: 0.0)) },
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 3 — Pricing & Estimation Charges
// ─────────────────────────────────────────────────────────────
@Composable
private fun Step3PricingAndEstimationCharges(
    items: List<QuotationScopeItem>,
    taxGroups: List<TaxGroupDto>,
    workPricingList: List<WorkPricingItem>,
    onItemsChange: (List<QuotationScopeItem>) -> Unit,
    overallDiscount: String,
    onDiscountChange: (String) -> Unit,
    deliveryCharge: String,
    onDeliveryChange: (String) -> Unit,
    validityExpiryDate: String,
    onExpiryDateChange: (String) -> Unit,
    termsNotes: String,
    onTermsNotesChange: (String) -> Unit,
    subtotal: Double,
    tax: Double,
    totalValue: Double
) {
    val tokens = LocalAppTokens.current

    val garmentItems = items.filter { it.itemType == QuotationItemType.GARMENT }
    val materialItems = items.filter { it.itemType != QuotationItemType.GARMENT }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
    ) {
        Text(
            text = "3. Pricing & Estimation Charges",
            fontSize = tokens.h2,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = tokens.extraPadding * 0.8f)
        )

        if (garmentItems.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                color = whiteBg,
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Tailored Garments (${garmentItems.size})",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(12.dp))

                    garmentItems.forEachIndexed { idx, itm ->
                        val itmTax = itm.calculateItemTax(workPricingList)
                        val itmTotal = itm.calculateItemTotal(workPricingList)
                        val addonsCost = itm.calculateTotalAddonsPrice(workPricingList)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Primary) {
                                    Text(
                                        text = "#${idx + 1}",
                                        color = whiteBg,
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = itm.garmentCategory.ifBlank { itm.garmentType.ifBlank { "Garment" } },
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(text = itm.fabricSource, fontSize = tokens.caption, color = TextSecondary)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "-",
                                    fontSize = tokens.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier
                                        .clickable {
                                            if (itm.quantity > 1) {
                                                onItemsChange(items.map { if (it.id == itm.id) it.copy(quantity = itm.quantity - 1) else it })
                                            }
                                        }
                                        .padding(horizontal = 6.dp)
                                )
                                Text(
                                    text = "${itm.quantity}",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                Text(
                                    text = "+",
                                    fontSize = tokens.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier
                                        .clickable {
                                            onItemsChange(items.map { if (it.id == itm.id) it.copy(quantity = itm.quantity + 1) else it })
                                        }
                                        .padding(horizontal = 6.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        SummaryRow("Stitching", formatPrice(itm.stitchingRate * itm.quantity))
                        SummaryRow("Addl. Work", formatPrice(addonsCost * itm.quantity))
                        SummaryRow("Tax", "+${formatPrice(itmTax)}")
                        SummaryRow("Total", formatPrice(itmTotal))

                        Spacer(Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = primary_light.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Item Breakdown — ${itm.garmentCategory.ifBlank { "Garment" }} (#${idx + 1})",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(8.dp))
                                SummaryRow("CHARGE TYPE", "Stitching", valueColor = Primary)
                                SummaryRow("CODE", itm.stitchingSacCode)
                                SummaryRow("QTY / UNIT", "${itm.quantity} Pieces")

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("RATE", fontSize = tokens.bodySmall, color = mutedText)
                                    Box(modifier = Modifier.width(90.dp)) {
                                        BasicTextField(
                                            value = if (itm.stitchingRate == 0.0) "" else itm.stitchingRate.toInt().toString(),
                                            onValueChange = { str ->
                                                val parsed = str.toDoubleOrNull() ?: 0.0
                                                onItemsChange(items.map { if (it.id == itm.id) it.copy(stitchingRate = parsed) else it })
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, BorderGray, RoundedCornerShape(4.dp))
                                                .background(whiteBg)
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            textStyle = LocalTextStyle.current.copy(
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.End,
                                                color = TextPrimary
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }
                                }

                                var taxDropdownOpen by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("TAX", fontSize = tokens.bodySmall, color = mutedText)
                                    Box {
                                        Row(
                                            modifier = Modifier.clickable { taxDropdownOpen = true }.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "GST ${itm.taxRate.toInt()}% (CGST ${itm.taxRate / 2}% + SGST ${itm.taxRate / 2}%)",
                                                fontSize = tokens.caption,
                                                color = TextPrimary
                                            )
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                                        }

                                        DropdownMenu(
                                            expanded = taxDropdownOpen,
                                            onDismissRequest = { taxDropdownOpen = false }
                                        ) {
                                            taxGroups.forEach { tg ->
                                                DropdownMenuItem(
                                                    text = { Text("${tg.name} (${tg.totalRate.toInt()}%)", fontSize = tokens.bodySmall) },
                                                    onClick = {
                                                        onItemsChange(
                                                            items.map {
                                                                if (it.id == itm.id) it.copy(selectedTaxGroupId = tg.id, taxRate = tg.totalRate)
                                                                else it
                                                            }
                                                        )
                                                        taxDropdownOpen = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                SummaryRow("TAX AMT", "+${formatPrice(itmTax)}", valueColor = greentext)
                                SummaryRow("Amount", formatPrice(itmTotal), valueColor = TextPrimary)
                            }
                        }

                        if (idx != garmentItems.lastIndex) {
                            HorizontalDivider(color = light_grey, modifier = Modifier.padding(vertical = 10.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        if (materialItems.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                color = whiteBg,
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Store Fabrics & Inventory Items (${materialItems.size})",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(12.dp))

                    materialItems.forEachIndexed { idx, itm ->
                        val itmTax = itm.calculateItemTax(workPricingList)
                        val itmTotal = itm.calculateItemTotal(workPricingList)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (itm.itemType == QuotationItemType.FABRIC) Color(0xFF0D9488) else darkGreenBg
                                ) {
                                    Text(
                                        text = "#${idx + 1}",
                                        color = whiteBg,
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Column {
                                    Text(text = itm.selectedItemName, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(text = "HSN: ${itm.hsnCode}", fontSize = tokens.caption, color = TextSecondary)
                                }
                            }

                            Text(text = formatPrice(itmTotal), fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Spacer(Modifier.height(8.dp))
                        SummaryRow("Rate", "${formatPrice(itm.rate)} / ${itm.unit}")
                        SummaryRow("Qty", "${itm.quantity} ${itm.unit}")
                        SummaryRow("Tax", "+${formatPrice(itmTax)}")

                        if (idx != materialItems.lastIndex) {
                            HorizontalDivider(color = light_grey, modifier = Modifier.padding(vertical = 10.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
            color = whiteBg,
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                FormLabel("Overall Discount Amount")
                FormTextField(
                    value = overallDiscount,
                    onValueChange = onDiscountChange,
                    placeholder = "0",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(12.dp))

                FormLabel("Delivery Charge")
                FormTextField(
                    value = deliveryCharge,
                    onValueChange = onDeliveryChange,
                    placeholder = "0",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(12.dp))

                FormLabel("Validity Expiry Date")
                DatePickerField(
                    value = validityExpiryDate,
                    onDateSelected = onExpiryDateChange
                )

                Spacer(Modifier.height(12.dp))

                FormLabel("Terms & Conditions / Notes")
                FormTextArea(
                    value = termsNotes,
                    onValueChange = onTermsNotesChange,
                    placeholder = "50% advance required to confirm order. First fitting provided within 7 days."
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        EstimationBreakdownCard(
            totalItems = items.size,
            subtotal = subtotal,
            tax = tax,
            totalValue = totalValue
        )
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 4 — Summary & PDF Preview Wire-up
// ─────────────────────────────────────────────────────────────
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun Step4SummaryAndPreview(
    token: String,
    previewShown: Boolean,
    onPreview: () -> Unit,
    onComplete: () -> Unit = {},
    customerName: String,
    customerPhone: String,
    customerEmail: String,
    isLead: Boolean,
    logoBase64: String = "",
    subtotal: Double,
    tax: Double,
    total: Double,
    discount: Double,
    deliveryCharge: Double,
    expiryDate: String,
    termsNotes: String,
    items: List<QuotationScopeItem>,
    workPricingList: List<WorkPricingItem>,
    customerId: String?,
    salespersonId: String,
    branchId: String,
    quotationViewModel: QuotationViewModel?,
    quotationId: String? = null,
    mode: String = "create",
    onEdit: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val pdfGenerator = remember { QuotationPdfGenerator(context) }
    var isSavingDraft by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    val previewScrollState = rememberScrollState()

    // ── Create State Listener ──
    val saveState = quotationViewModel?.saveState?.collectAsStateWithLifecycle()?.value

    LaunchedEffect(saveState) {
        when (saveState) {
            is QuotationSaveUiState.Success -> {
                isSavingDraft = false
                DynamicIslandManager.showSuccess("Quotation Draft Saved Successfully!")
                quotationViewModel.resetState()
                onComplete()
            }
            is QuotationSaveUiState.Error -> {
                isSavingDraft = false
                Toast.makeText(context, "Failed: ${saveState.message}", Toast.LENGTH_SHORT).show()
                quotationViewModel.resetState()
            }
            is QuotationSaveUiState.Loading -> isSavingDraft = true
            else -> Unit
        }
    }

    // ── Update State Listener ──
    val isUpdatingQuotation by quotationViewModel?.isUpdatingQuotation?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val updateQuotationSuccess by quotationViewModel?.updateQuotationSuccess?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    val updateQuotationError by quotationViewModel?.updateQuotationError?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }

    LaunchedEffect(updateQuotationSuccess) {
        updateQuotationSuccess?.let { msg ->
            DynamicIslandManager.showSuccess(msg)
            quotationViewModel?.clearUpdateQuotationAlerts()
            onComplete()
        }
    }

    LaunchedEffect(updateQuotationError) {
        updateQuotationError?.let { err ->
            DynamicIslandManager.showError(err)
            quotationViewModel?.clearUpdateQuotationAlerts()
        }
    }

    val quotationNumber = remember { "QUO-${System.currentTimeMillis()}" }
    val quotationDate = remember { SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date()) }

    val pdfData = remember(customerName, items, subtotal, total, logoBase64) {
        QuotationPdfGenerator.QuotationData(
            quotationNumber = quotationNumber,
            quotationDate = quotationDate,
            customerName = customerName,
            logoUrl = logoBase64.ifEmpty { null },
            customerAddress = "Customer Phone: $customerPhone",
            customerVat = "",
            customerEmail = "",
            customerPhone = customerPhone,
            items = items.map { itm ->
                val lineTotal = itm.calculateItemTotal(workPricingList)
                QuotationPdfGenerator.QuotationItem(
                    description = when (itm.itemType) {
                        QuotationItemType.GARMENT -> itm.garmentCategory.ifBlank { itm.garmentType.ifBlank { "Garment" } }
                        else -> itm.selectedItemName.ifBlank { "Material" }
                    },
                    quantity = itm.quantity,
                    rate = if (itm.quantity > 0) lineTotal / itm.quantity else 0.0,
                    amount = lineTotal
                )
            },
            subtotal = subtotal,
            discountPercent = 0.0,
            discountAmount = discount,
            total = total,
            termsAndConditions = listOf(termsNotes),
            thankYouMessage = "Thank you for your business!",
            poweredBy = "When accepted, this quotation converts into a live Sales Order."
        )
    }

    val sharePdf: (String?) -> Unit = { pkg ->
        pdfGenerator.downloadQuotationPdf(pdfData) { saved ->
            val uri = saved?.uri
            if (uri != null) {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Quotation $quotationNumber")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (pkg != null) setPackage(pkg)
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                    context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                }
            } else {
                Toast.makeText(context, "Could not generate PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun expiryToIso(isoFormat: SimpleDateFormat, fallbackFrom: Date): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            isoFormat.format(inputFormat.parse(expiryDate)!!)
        } catch (_: Exception) {
            val cal = Calendar.getInstance().apply {
                time = fallbackFrom
                add(Calendar.DAY_OF_YEAR, 21)
            }
            isoFormat.format(cal.time)
        }
    }

    // ── Build Request Payload matching Backend Update Schema ──
    fun buildCreateQuotationRequest(): CreateQuotationRequest? {
        val selectedId = customerId ?: return null
        if (branchId.isBlank() || salespersonId.isBlank()) return null

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val now = Date()
        val quotationIso = isoFormat.format(now)
        val expiryIso = expiryToIso(isoFormat, now)

        val lineItems = mutableListOf<QuotationPayloadItem>()
        val currentTime = System.currentTimeMillis()

        items.forEachIndexed { idx, itm ->
            // Preserves 24-char MongoDB ID for existing items from DB, otherwise generates new
            val effectiveLineId = if (itm.id.length == 24) itm.id else "temp_${currentTime}_$idx"

            when (itm.itemType) {
                QuotationItemType.GARMENT -> {
                    lineItems.add(
                        QuotationPayloadItem(
                            id = effectiveLineId,
                            lineType = "Custom_Garment",
                            itemDescription = itm.garmentCategory.ifBlank { itm.garmentType },
                            isTaxable = true,
                            taxGroupId = itm.selectedTaxGroupId,
                            sacCode = itm.stitchingSacCode.orDefaultIfPlaceholder("6543"),
                            quantity = itm.quantity.toDouble(),
                            unit = "Pieces",
                            unitPrice = itm.stitchingRate,
                            discountAmount = 0.0,
                            lineTotal = itm.stitchingRate * itm.quantity,
                            customGarment = CustomGarmentPayload(
                                segmentId = itm.segmentId,
                                segmentName = itm.segmentName,
                                garmentId = itm.garmentId,
                                garmentName = itm.garmentType,
                                garmentCategoryId = itm.garmentCategoryId,
                                categoryDisplayName = itm.garmentCategory,
                                designId = itm.designPresetId.takeIf { it.isNotBlank() },
                                designName = itm.designPresetName.takeIf { it != "Select Design" },
                                stitchingType = itm.stitchingType.orDefaultIfPlaceholder("Normal Machine"),
                                fabricSource = itm.fabricSource,
                                fabricNotes = itm.customerFabricDescription,
                                specialInstructions = itm.specialNotes
                            )
                        )
                    )

                    // Add-on works linked with parent line ID
                    itm.selectedWorkPricingIds.forEach { wpId ->
                        val wp = workPricingList.find { it.id == wpId } ?: return@forEach
                        lineItems.add(
                            QuotationPayloadItem(
                                id = null,
                                parentLineId = effectiveLineId,
                                lineType = "Garment_Addon",
                                itemDescription = wp.workType,
                                isTaxable = wp.isTaxable,
                                taxGroupId = itm.selectedTaxGroupId,
                                sacCode = wp.sacCode ?: "8977",
                                quantity = 1.0,
                                unit = "Piece",
                                unitPrice = wp.basePrice,
                                discountAmount = 0.0,
                                lineTotal = wp.basePrice,
                                addonWork = AddonWorkPayload(
                                    workPricingId = wp.id,
                                    workType = wp.workType,
                                    specialInstructions = ""
                                )
                            )
                        )
                    }
                }

                QuotationItemType.FABRIC -> {
                    lineItems.add(
                        QuotationPayloadItem(
                            id = effectiveLineId,
                            lineType = "Garment_Material",
                            itemDescription = itm.selectedItemName,
                            isTaxable = true,
                            taxGroupId = itm.selectedTaxGroupId,
                            hsnCode = itm.hsnCode.orDefaultIfPlaceholder("0984"),
                            quantity = itm.quantity.toDouble(),
                            unit = itm.unit,
                            unitPrice = itm.rate,
                            discountAmount = 0.0,
                            lineTotal = itm.rate * itm.quantity,
                            product = ProductPayload(
                                inventoryItemId = itm.selectedItemId,
                                sku = itm.selectedItemSku,
                                itemName = itm.selectedItemName
                            )
                        )
                    )
                }

                QuotationItemType.RETAIL_ITEM -> {
                    lineItems.add(
                        QuotationPayloadItem(
                            id = effectiveLineId,
                            lineType = "Retail_Product",
                            itemDescription = itm.selectedItemName,
                            isTaxable = true,
                            taxGroupId = itm.selectedTaxGroupId,
                            hsnCode = itm.hsnCode.orDefaultIfPlaceholder("1234"),
                            quantity = itm.quantity.toDouble(),
                            unit = itm.unit,
                            unitPrice = itm.rate,
                            discountAmount = 0.0,
                            lineTotal = itm.rate * itm.quantity,
                            product = ProductPayload(
                                inventoryItemId = itm.selectedItemId,
                                sku = itm.selectedItemSku,
                                itemName = itm.selectedItemName
                            )
                        )
                    )
                }
            }
        }

        return CreateQuotationRequest(
            branchId = branchId,
            customerId = if (isLead) null else selectedId,
            leadId = if (isLead) selectedId else null,
            customerSnapshot = QuotationCustomerSnapshotPayload(
                name = customerName,
                phone = customerPhone,
                email = customerEmail
            ),
            salespersonId = salespersonId,
            quotationDate = quotationIso,
            expiryDate = expiryIso,
            currency = "INR",
            exchangeRate = 1,
            status = "Draft",
            subtotal = subtotal,
            totalTax = tax,
            totalDiscount = discount,
            deliveryCharge = deliveryCharge,
            grandTotal = total,
            notes = termsNotes,
            items = lineItems
        )
    }

    // ── Save or Update Action ──
    val saveDraftAction: () -> Unit = {
        val hasMissingProduct = items.any {
            (it.itemType == QuotationItemType.FABRIC || it.itemType == QuotationItemType.RETAIL_ITEM) &&
                    it.selectedItemId.isBlank()
        }
        val hasMissingTax = items.any { it.selectedTaxGroupId.isBlank() }

        when {
            hasMissingProduct ->
                Toast.makeText(context, "Please select a valid fabric/item for all lines", Toast.LENGTH_SHORT).show()
            hasMissingTax ->
                Toast.makeText(context, "Please select a tax group for all lines", Toast.LENGTH_SHORT).show()
            else -> {
                val req = buildCreateQuotationRequest()
                if (req != null) {
                    if (!quotationId.isNullOrBlank() && mode == "edit") {
                        // EDIT MODE: Call Update API
                        quotationViewModel?.updateQuotation(quotationId, req)
                    } else {
                        // CREATE MODE: Call Create API
                        quotationViewModel?.saveDraft(req)
                    }
                } else {
                    Toast.makeText(context, "Please select customer & salesperson", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(previewScrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Preview", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (!isDownloading) {
                            isDownloading = true
                            pdfGenerator.downloadQuotationPdf(pdfData) {
                                isDownloading = false
                                Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download", tint = Primary)
                }

                IconButton(onClick = { pdfGenerator.printQuotationPdf(pdfData) }) {
                    Icon(Icons.Default.Print, contentDescription = "Print", tint = Primary)
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(490.dp)
                .padding(horizontal = tokens.screenPadding)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderGray, RoundedCornerShape(12.dp)),
            color = whiteBg
        ) {
            AndroidView(
                factory = { ctx ->
                    android.webkit.WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        webViewClient = android.webkit.WebViewClient()
                    }
                },
                update = { webView ->
                    val htmlContent = pdfGenerator.buildQuotationHtml(pdfData)
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(16.dp))

        SendQuotationSection(
            onWhatsApp = { sharePdf("com.whatsapp") },
            onEmail = { sharePdf(null) }
        )

        Spacer(Modifier.height(10.dp))

        val isBusySaving = isSavingDraft || isUpdatingQuotation
        QuickActionsRow(
            onDiscount = onEdit,
            onEdit = onEdit,
            onSaveDraft = saveDraftAction,
            isSavingDraft = isBusySaving,
            saveButtonLabel = if (isBusySaving) "Saving..." else if (mode == "edit") "Update Quotation" else "Save as Draft"
        )

        Spacer(Modifier.height(110.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// QUICK ACTIONS COMPONENTS
// ─────────────────────────────────────────────────────────────
@Composable
private fun SendQuotationSection(
    onWhatsApp: () -> Unit,
    onEmail: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
    ) {
        Text("Send Quotation", fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Button(
                onClick = onWhatsApp,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight * 1.15f),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                colors = ButtonDefaults.buttonColors(containerColor = darkGreenBg, contentColor = whiteBg)
            ) {
                Icon(painterResource(R.drawable.ic_whatsapp), contentDescription = "WhatsApp", modifier = Modifier.size(tokens.iconSize), tint = whiteBg)
                Spacer(Modifier.width(8.dp))
                Text("WhatsApp", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = whiteBg)
            }

            Button(
                onClick = onEmail,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight * 1.15f),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = whiteBg)
            ) {
                Icon(painterResource(R.drawable.ic_mail), contentDescription = "Email", modifier = Modifier.size(tokens.iconSize), tint = whiteBg)
                Spacer(Modifier.width(8.dp))
                Text("Email", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = whiteBg)
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onDiscount: () -> Unit,
    onEdit: () -> Unit,
    onSaveDraft: () -> Unit,
    isSavingDraft: Boolean = false,
    saveButtonLabel: String = "Save as Draft"
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.8f)
    ) {
        Text("Quick Actions", fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Percent,
                label = "Discount",
                onClick = onDiscount
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Edit,
                label = "Edit",
                onClick = onEdit
            )
        }

        Spacer(Modifier.height(tokens.extraPadding))

        QuickActionCard(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Description,
            label = saveButtonLabel,
            onClick = onSaveDraft,
            enabled = !isSavingDraft
        )
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val tokens = LocalAppTokens.current

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(tokens.buttonHeight * 1.15f),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg, contentColor = TextPrimary),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(tokens.iconSize), tint = TextPrimary)
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE COMPONENTS
// ─────────────────────────────────────────────────────────────

@Composable
private fun EstimationBreakdownCard(
    totalItems: Int,
    subtotal: Double,
    tax: Double,
    totalValue: Double
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
                Text(
                    text = "ESTIMATION BREAKDOWN",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Spacer(Modifier.height(10.dp))

            SummaryRow("Total Items", "$totalItems Configured")
            SummaryRow("Subtotal", formatPrice(subtotal))
            SummaryRow("Tax", formatPrice(tax), valueColor = greentext)

            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Value",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = formatPrice(totalValue),
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = primary_light.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "When accepted, this quotation converts into a live Sales Order with all specifications preserved.",
                    fontSize = tokens.caption,
                    color = Primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun QuotationFourStepper(currentStep: Int) {
    val tokens = LocalAppTokens.current
    val steps = listOf("Customer", "Scope", "Pricing", "Summary")

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = tokens.extraPadding * 1.2f)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding * 2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, _ ->
                val stepNum = index + 1
                val isDone = stepNum < currentStep
                val isCurrent = stepNum == currentStep

                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 1.4f)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone -> darkGreenBg
                                isCurrent -> Primary
                                else -> whiteBg
                            }
                        )
                        .then(if (!isDone && !isCurrent) Modifier.border(1.5.dp, sectionBorder, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize * 0.75f))
                    } else {
                        Text(
                            text = "$stepNum",
                            color = if (isCurrent) whiteBg else mutedText,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (index != steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (stepNum < currentStep) darkGreenBg else BorderGray)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectQuotationDesignDialog(
    designs: List<DesignItem>,
    onDismiss: () -> Unit,
    onDesignSelected: (DesignItem) -> Unit
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(designs, searchQuery) {
        if (searchQuery.isBlank()) designs
        else designs.filter { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(tokens.cardCornerRadius)),
            color = whiteBg
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Design Preset", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search designs...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { design ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                                .clickable { onDesignSelected(design) },
                            shape = RoundedCornerShape(8.dp),
                            color = whiteBg
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(grey_border),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!design.imageUrl.isNullOrBlank()) {
                                        AsyncImage(model = design.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Icon(Icons.Default.Checkroom, contentDescription = null, tint = TextSecondary)
                                    }
                                }
                                Column {
                                    Text(design.name, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("${design.designType} • ${design.code}", fontSize = tokens.caption, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerLeadToggle(selected: String, onSelect: (String) -> Unit) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight * 1.1f)
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(whiteBg)
            .border(1.dp, light_grey, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .padding(4.dp)
    ) {
        listOf("Customer", "Lead").forEach { label ->
            val isSelected = selected == label
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                    .background(if (isSelected) primary_light else Color.Transparent)
                    .clickable { onSelect(label) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = tokens.bodySmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) Primary else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun CustomerSelectionCard(customer: CustomerOption, selected: Boolean, onSelect: () -> Unit) {
    val tokens = LocalAppTokens.current
    val formattedName = remember(customer.name) { customer.name?.toTitleCase() ?: "Unknown Customer" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
            .border(width = 1.dp, color = if (selected) Primary else BorderGray, shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
            .background(whiteBg)
            .clickable { onSelect() }
            .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.8f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (selected) Primary else Color.Transparent)
                .border(1.dp, if (selected) Primary else BorderGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(whiteBg))
            }
        }
        Spacer(Modifier.width(tokens.extraPadding))
        Column {
            Text(formattedName, fontSize = tokens.bodyMedium, color = TextPrimary)
            Text(customer.phone, fontSize = tokens.caption, color = mutedText)
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, color = mutedText)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = valueColor)
    }
}