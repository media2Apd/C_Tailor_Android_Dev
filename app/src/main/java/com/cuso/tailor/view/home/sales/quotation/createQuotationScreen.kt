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

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.*
import com.cuso.tailor.model.settings.GarmentItem
import com.cuso.tailor.model.settings.WorkPricingItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.pdfgenerator.QuotationPdfGenerator
import com.cuso.tailor.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val TAX_RATE = 0.18

data class CustomerOption(
    val id: String,
    val name: String? = "",
    val phone: String,
    val addressLine: String = "",
    val city: String = "",
    val pincode: String = ""
)

data class GarmentBreakdown(
    val garmentId: String,
    val garmentName: String,
    val basePrice: Double,
    val fabricName: String,
    val fabricPrice: Double,
    val designName: String,
    val designPrice: Double,
    val addonsNames: String,
    val addonsPrice: Double,
    val quantity: Int,
    val itemSubtotal: Double
)

enum class PriceEditType {
    GARMENT,
    FABRIC,
    WORK
}

data class PriceEditDialogState(
    val type: PriceEditType,
    val id: String,
    val title: String,
    val currentPrice: Double,
    val extraData: Any? = null
)

private fun formatPrice(amount: Double): String =
    "₹${String.format(Locale.US, "%.2f", amount)}"

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
    val orderState by salesOrderViewModel.orderState.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val taxGroups by inventoryViewModel.taxGroups.collectAsStateWithLifecycle()

    // ── Staff List for Salesperson Selection ──
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    var selectedSalespersonId by remember { mutableStateOf<String?>(null) }
    var staffDropdownExpanded by remember { mutableStateOf(false) }

    val staffDisplayMap = remember(staffList) {
        staffList.associate { "${it.firstName} ${it.lastName}".trim().ifEmpty { "Staff Member" } to it.id }
    }
    val staffOptions = remember(staffList) {
        staffDisplayMap.keys.toList()
    }

    // ── Master lists for Garment, Fabric, and Work pricing ──
    val garments by settingsViewModel.garments.collectAsStateWithLifecycle()
    val fabricPricingList by settingsViewModel.fabricPricingList.collectAsStateWithLifecycle()
    val workPricingList by settingsViewModel.workPricingList.collectAsStateWithLifecycle()

    // ── Local Price Overrides (In-memory screen edits) ──
    var garmentPriceOverrides by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var fabricPriceOverrides by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var workPriceOverrides by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

    var currentStep by remember { mutableIntStateOf(if (quotationId != null) 3 else 1) }
    var customerLeadTab by remember { mutableStateOf("Customer") }
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }

    // ── Selections for Step 2 ──
    var selectedGarment by remember { mutableStateOf<GarmentItem?>(null) }
    var selectedFabric by remember { mutableStateOf<FabricPricingItem?>(null) }
    var selectedWork by remember { mutableStateOf<WorkPricingItem?>(null) }
    var quantity by remember { mutableIntStateOf(1) }

    var activePriceEdit by remember { mutableStateOf<PriceEditDialogState?>(null) }
    var previewShown by remember { mutableStateOf(mode == "view") }
    var isPrefilling by remember { mutableStateOf(quotationId != null) }

    val customers = remember(customerState) {
        when (customerState) {
            is CustomerUiState.Success -> {
                (customerState as CustomerUiState.Success).customers.map { customer ->
                    CustomerOption(
                        id = customer.id,
                        name = customer.name,
                        phone = customer.mobile ?: "",
                        addressLine = customer.address?.addressLine ?: "",
                        city = customer.address?.city ?: "",
                        pincode = customer.address?.pincode ?: ""
                    )
                }
            }
            else -> emptyList()
        }
    }

    val leads = remember(orderState) {
        when (orderState) {
            is OrderUiState.Success -> {
                (orderState as OrderUiState.Success).orders.map { order ->
                    CustomerOption(
                        id = order.id,
                        name = order.customerId?.name ?: "Lead",
                        phone = order.customerId?.mobile ?: ""
                    )
                }
            }
            else -> emptyList()
        }
    }

    val currentItems = if (customerLeadTab == "Customer") customers else leads
    val selectedCustomer = remember(currentItems, selectedCustomerId) {
        currentItems.find { it.id == selectedCustomerId }
    }

    // ── Calculated price breakdown ──
    val baseGarmentPrice = selectedGarment?.let { garmentPriceOverrides[it.id] ?: it.baseStitchingCharge } ?: 0.0
    val fabricPrice = selectedFabric?.let { fabricPriceOverrides[it.id] ?: it.sellingPrice } ?: 0.0
    val workPrice = selectedWork?.let { workPriceOverrides[it.id] ?: it.basePrice } ?: 0.0

    val unitSubtotal = baseGarmentPrice + fabricPrice + workPrice
    val subtotal = unitSubtotal * quantity
    val tax = subtotal * TAX_RATE
    val total = subtotal + tax

    val dynamicBreakdowns = remember(selectedGarment, selectedFabric, selectedWork, quantity, garmentPriceOverrides, fabricPriceOverrides, workPriceOverrides) {
        if (selectedGarment != null || selectedFabric != null || selectedWork != null) {
            listOf(
                GarmentBreakdown(
                    garmentId = selectedGarment?.id ?: "custom_item",
                    garmentName = selectedGarment?.displayName ?: selectedGarment?.name ?: "Custom Garment",
                    basePrice = baseGarmentPrice,
                    fabricName = selectedFabric?.name ?: "-",
                    fabricPrice = fabricPrice,
                    designName = "-",
                    designPrice = 0.0,
                    addonsNames = selectedWork?.workType ?: "",
                    addonsPrice = workPrice,
                    quantity = quantity,
                    itemSubtotal = subtotal
                )
            )
        } else {
            emptyList()
        }
    }

    // ── Initial API data loading ──
    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers()
        salesOrderViewModel.fetchOrders()
        salesViewModel.fetchStaff()
        settingsViewModel.fetchGarments()
        settingsViewModel.fetchFabricPricing()
        settingsViewModel.fetchWorkPricing()
        branchViewModel.loadBranches()
        inventoryViewModel.fetchTaxGroups()
        profileViewModel.loadOrganization(token)
        if (quotationId != null) {
            quotationViewModel.fetchQuotationById(quotationId)
        }
    }

    val detailState by quotationViewModel.detailState.collectAsStateWithLifecycle()

    LaunchedEffect(detailState) {
        when (val state = detailState) {
            is QuotationDetailUiState.Success -> {
                val dto = state.quotation
                selectedCustomerId = dto.customerId?.id
                isPrefilling = false
            }
            is QuotationDetailUiState.Error -> {
                isPrefilling = false
            }
            else -> {
                if (quotationId == null) {
                    isPrefilling = false
                }
            }
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
                } catch (_: Exception) {
                    ""
                }
            }
        }
    }

    fun goToNextStep() {
        when (currentStep) {
            1 -> if (selectedCustomerId != null) currentStep++
            2 -> if (selectedGarment != null || selectedFabric != null || selectedWork != null) currentStep++
            3 -> onSave()
        }
    }

    fun goToPreviousStep() {
        if (currentStep > 1) currentStep--
    }

    if (isPrefilling) {
        ListSkeleton()
        return
    }

    val quotationDto = (detailState as? QuotationDetailUiState.Success)?.quotation

    val displayBreakdowns = remember(dynamicBreakdowns, quotationDto) {
        if (dynamicBreakdowns.isNotEmpty()) {
            dynamicBreakdowns
        } else if (quotationDto != null) {
            val customGarments = quotationDto.items.filter { it.lineType == "Custom_Garment" || it.customGarment != null }
            if (customGarments.isNotEmpty()) {
                customGarments.map { cg ->
                    val childMaterial = quotationDto.items.find { it.lineType == "Garment_Material" && it.parentLineId == cg.id }
                    val childAddon = quotationDto.items.find { it.lineType == "Garment_Addon" && it.parentLineId == cg.id }

                    GarmentBreakdown(
                        garmentId = cg.id ?: "",
                        garmentName = cg.customGarment?.categoryDisplayName
                            ?: cg.customGarment?.garmentName
                            ?: cg.itemDescription
                            ?: "Garment",
                        basePrice = cg.unitPrice,
                        fabricName = childMaterial?.itemDescription ?: cg.customGarment?.fabricNotes ?: "-",
                        fabricPrice = childMaterial?.totalPrice ?: 0.0,
                        designName = cg.customGarment?.designName ?: "-",
                        designPrice = 0.0,
                        addonsNames = childAddon?.itemDescription ?: cg.addonWork?.workType ?: "",
                        addonsPrice = childAddon?.totalPrice ?: 0.0,
                        quantity = cg.quantity.toInt().coerceAtLeast(1),
                        itemSubtotal = cg.totalPrice + (childMaterial?.totalPrice ?: 0.0) + (childAddon?.totalPrice ?: 0.0)
                    )
                }
            } else {
                quotationDto.items.map { itm ->
                    GarmentBreakdown(
                        garmentId = itm.id ?: "",
                        garmentName = itm.itemDescription ?: "Item",
                        basePrice = itm.unitPrice,
                        fabricName = "-",
                        fabricPrice = 0.0,
                        designName = "-",
                        designPrice = 0.0,
                        addonsNames = "",
                        addonsPrice = 0.0,
                        quantity = itm.quantity.toInt().coerceAtLeast(1),
                        itemSubtotal = itm.totalPrice
                    )
                }
            }
        } else {
            emptyList()
        }
    }

    val displaySubtotal = if (quotationDto != null && dynamicBreakdowns.isEmpty()) quotationDto.subTotal else subtotal
    val displayTax = if (quotationDto != null && dynamicBreakdowns.isEmpty()) quotationDto.taxAmount else tax
    val displayTotal = if (quotationDto != null && dynamicBreakdowns.isEmpty()) quotationDto.grandTotal else total

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
                Text(
                    text = "Create Quotation",
                    fontSize = tokens.h1,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
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
                    if (currentStep != 3 || !previewShown) Modifier.verticalScroll(rememberScrollState())
                    else Modifier
                )
        ) {
            QuotationStepper(currentStep = currentStep)

            when (currentStep) {
                1 -> Step1CustomerSelection(
                    tab = customerLeadTab,
                    onTabChange = { customerLeadTab = it },
                    items = currentItems,
                    selectedId = selectedCustomerId,
                    onSelect = { selectedCustomerId = it },
                    isLoading = when (customerLeadTab) {
                        "Customer" -> customerState is CustomerUiState.Loading
                        else -> orderState is OrderUiState.Loading
                    }
                )

                2 -> Step2PricingSelection(
                    selectedSalespersonId = selectedSalespersonId,
                    staffDisplayMap = staffDisplayMap,
                    staffOptions = staffOptions,
                    staffDropdownExpanded = staffDropdownExpanded,
                    onStaffExpandChange = { staffDropdownExpanded = it },
                    onSelectSalesperson = { selectedSalespersonId = it },

                    garments = garments,
                    fabricPricingList = fabricPricingList,
                    workPricingList = workPricingList,
                    garmentPriceOverrides = garmentPriceOverrides,
                    fabricPriceOverrides = fabricPriceOverrides,
                    workPriceOverrides = workPriceOverrides,
                    selectedGarment = selectedGarment,
                    selectedFabric = selectedFabric,
                    selectedWork = selectedWork,
                    onSelectGarment = { selectedGarment = if (selectedGarment?.id == it.id) null else it },
                    onSelectFabric = { selectedFabric = if (selectedFabric?.id == it.id) null else it },
                    onSelectWork = { selectedWork = if (selectedWork?.id == it.id) null else it },
                    quantity = quantity,
                    onQuantityChange = { quantity = it },

                    onEditGarmentPrice = { item, currentEffectivePrice ->
                        activePriceEdit = PriceEditDialogState(
                            type = PriceEditType.GARMENT,
                            id = item.id,
                            title = item.displayName ?: item.name,
                            currentPrice = currentEffectivePrice
                        )
                    },
                    onEditFabricPrice = { item, currentEffectivePrice ->
                        activePriceEdit = PriceEditDialogState(
                            type = PriceEditType.FABRIC,
                            id = item.id,
                            title = item.name,
                            currentPrice = currentEffectivePrice
                        )
                    },
                    onEditWorkPrice = { item, currentEffectivePrice ->
                        activePriceEdit = PriceEditDialogState(
                            type = PriceEditType.WORK,
                            id = item.id,
                            title = item.workType,
                            currentPrice = currentEffectivePrice,
                            extraData = item
                        )
                    },
                    subtotal = subtotal,
                    tax = tax,
                    total = total
                )

                3 -> Step3PricingSummary(
                    token = token,
                    previewShown = previewShown,
                    onPreview = { previewShown = true },
                    onComplete = { onSave() },
                    customerName = quotationDto?.customerSnapshot?.name
                        ?: quotationDto?.customerId?.fullName
                        ?: selectedCustomer?.name
                        ?: "-",
                    logoBase64 = logoBase64,
                    subtotal = displaySubtotal,
                    tax = displayTax,
                    total = displayTotal,
                    quotationNumber = quotationDto?.quotationNumber ?: "QUO-${System.currentTimeMillis()}",
                    quotationDate = quotationDto?.quotationDate?.take(10)
                        ?: SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date()),
                    customerAddress = selectedCustomer?.let { "${it.name}\nPhone: ${it.phone}" } ?: "",
                    customerPhone = quotationDto?.customerSnapshot?.phone
                        ?: quotationDto?.customerId?.mobileNumber
                        ?: selectedCustomer?.phone
                        ?: "",
                    customerId = selectedCustomerId,
                    garmentBreakdowns = displayBreakdowns,
                    quotationViewModel = quotationViewModel,

                    // ── Dynamic Payload Construction Inputs ──
                    branchState = branchState,
                    taxGroups = taxGroups,
                    selectedGarment = selectedGarment,
                    selectedFabric = selectedFabric,
                    selectedWork = selectedWork,
                    garmentPriceOverrides = garmentPriceOverrides,
                    fabricPriceOverrides = fabricPriceOverrides,
                    workPriceOverrides = workPriceOverrides,
                    quantity = quantity,
                    selectedSalespersonId = selectedSalespersonId,

                    onEdit = {
                        previewShown = false
                        currentStep = 2
                    }
                )
            }

            Spacer(Modifier.height(tokens.buttonHeight * 2))
        }

        if (!(currentStep == 3 && previewShown) && mode != "view") {
            StepNavigationFab(
                showBack = currentStep > 1,
                onBack = { goToPreviousStep() },
                backLabel = if (currentStep == 1) "Cancel" else "Back",
                trailingAction = when (currentStep) {
                    3 -> TrailingFabAction.Next(label = "Preview", onClick = { previewShown = true })
                    else -> TrailingFabAction.Next(label = "Next", onClick = { goToNextStep() })
                },
                backWidthFraction = 0.30f,
                trailingWidthFraction = 0.40f
            )
        }

        activePriceEdit?.let { target ->
            SinglePriceEditDialog(
                title = target.title,
                initialPrice = target.currentPrice,
                onDismiss = { activePriceEdit = null },
                onSave = { newPrice ->
                    when (target.type) {
                        PriceEditType.GARMENT -> {
                            garmentPriceOverrides = garmentPriceOverrides + (target.id to newPrice)
                            if (selectedGarment?.id == target.id) {
                                selectedGarment = selectedGarment?.copy(baseStitchingCharge = newPrice)
                            }
                            activePriceEdit = null
                        }
                        PriceEditType.FABRIC -> {
                            fabricPriceOverrides = fabricPriceOverrides + (target.id to newPrice)
                            if (selectedFabric?.id == target.id) {
                                selectedFabric = selectedFabric?.copy(sellingPrice = newPrice)
                            }
                            activePriceEdit = null
                        }
                        PriceEditType.WORK -> {
                            workPriceOverrides = workPriceOverrides + (target.id to newPrice)
                            if (selectedWork?.id == target.id) {
                                selectedWork = selectedWork?.copy(basePrice = newPrice)
                            }
                            activePriceEdit = null
                        }
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 2 — Salesperson & 3 Dynamic Pricing Sections
// ─────────────────────────────────────────────────────────────
@Composable
private fun Step2PricingSelection(
    selectedSalespersonId: String?,
    staffDisplayMap: Map<String, String>,
    staffOptions: List<String>,
    staffDropdownExpanded: Boolean,
    onStaffExpandChange: (Boolean) -> Unit,
    onSelectSalesperson: (String?) -> Unit,

    garments: List<GarmentItem>,
    fabricPricingList: List<FabricPricingItem>,
    workPricingList: List<WorkPricingItem>,
    garmentPriceOverrides: Map<String, Double>,
    fabricPriceOverrides: Map<String, Double>,
    workPriceOverrides: Map<String, Double>,
    selectedGarment: GarmentItem?,
    selectedFabric: FabricPricingItem?,
    selectedWork: WorkPricingItem?,
    onSelectGarment: (GarmentItem) -> Unit,
    onSelectFabric: (FabricPricingItem) -> Unit,
    onSelectWork: (WorkPricingItem) -> Unit,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onEditGarmentPrice: (GarmentItem, Double) -> Unit,
    onEditFabricPrice: (FabricPricingItem, Double) -> Unit,
    onEditWorkPrice: (WorkPricingItem, Double) -> Unit,
    subtotal: Double,
    tax: Double,
    total: Double
) {
    val tokens = LocalAppTokens.current

    Spacer(Modifier.height(tokens.extraPadding * 0.4f))

    // Salesperson Dropdown
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
        FormDropdown(
            label = "Salesperson / Assigned Staff",
            value = staffDisplayMap.entries.firstOrNull { it.value == selectedSalespersonId }?.key ?: "Select Salesperson",
            expanded = staffDropdownExpanded,
            onExpandChange = onStaffExpandChange,
            options = staffOptions,
            onOptionSelected = { selectedLabel ->
                onSelectSalesperson(staffDisplayMap[selectedLabel])
            }
        )
    }

    Spacer(Modifier.height(tokens.extraPadding * 1.2f))

    // 1. Select Garment Pricing Section
    if (garments.isNotEmpty()) {
        PricingSectionGrid(
            sectionTitle = "Select Garment Pricing",
            items = garments.map {
                val effectivePrice = garmentPriceOverrides[it.id] ?: it.baseStitchingCharge
                PricingCardModel(
                    id = it.id,
                    title = it.displayName ?: it.name,
                    price = effectivePrice
                )
            },
            selectedId = selectedGarment?.id,
            onSelect = { id ->
                garments.find { it.id == id }?.let { onSelectGarment(it) }
            },
            onEdit = { id ->
                garments.find { it.id == id }?.let { item ->
                    val effectivePrice = garmentPriceOverrides[item.id] ?: item.baseStitchingCharge
                    onEditGarmentPrice(item, effectivePrice)
                }
            }
        )
    }

    // 2. Select Fabric Pricing Section
    if (fabricPricingList.isNotEmpty()) {
        Spacer(Modifier.height(tokens.extraPadding * 1.2f))
        PricingSectionGrid(
            sectionTitle = "Select Fabric Pricing",
            items = fabricPricingList.map {
                val effectivePrice = fabricPriceOverrides[it.id] ?: it.sellingPrice
                PricingCardModel(
                    id = it.id,
                    title = it.name,
                    price = effectivePrice
                )
            },
            selectedId = selectedFabric?.id,
            onSelect = { id ->
                fabricPricingList.find { it.id == id }?.let { onSelectFabric(it) }
            },
            onEdit = { id ->
                fabricPricingList.find { it.id == id }?.let { item ->
                    val effectivePrice = fabricPriceOverrides[item.id] ?: item.sellingPrice
                    onEditFabricPrice(item, effectivePrice)
                }
            }
        )
    }

    // 3. Select Work Pricing Section
    if (workPricingList.isNotEmpty()) {
        Spacer(Modifier.height(tokens.extraPadding * 1.2f))
        PricingSectionGrid(
            sectionTitle = "Select Work Pricing",
            items = workPricingList.map {
                val effectivePrice = workPriceOverrides[it.id] ?: it.basePrice
                PricingCardModel(
                    id = it.id,
                    title = it.workType,
                    price = effectivePrice
                )
            },
            selectedId = selectedWork?.id,
            onSelect = { id ->
                workPricingList.find { it.id == id }?.let { onSelectWork(it) }
            },
            onEdit = { id ->
                workPricingList.find { it.id == id }?.let { item ->
                    val effectivePrice = workPriceOverrides[item.id] ?: item.basePrice
                    onEditWorkPrice(item, effectivePrice)
                }
            }
        )
    }

    // 4. Quantity Selection
    Spacer(Modifier.height(tokens.extraPadding * 1.2f))
    QuantitySelector(
        quantity = quantity,
        onQuantityChange = onQuantityChange
    )

    // 5. Consolidated Pricing Breakdown
    if (selectedGarment != null || selectedFabric != null || selectedWork != null) {
        val selectedGarmentPrice = selectedGarment?.let { garmentPriceOverrides[it.id] ?: it.baseStitchingCharge } ?: 0.0
        val selectedFabricPrice = selectedFabric?.let { fabricPriceOverrides[it.id] ?: it.sellingPrice } ?: 0.0
        val selectedWorkPrice = selectedWork?.let { workPriceOverrides[it.id] ?: it.basePrice } ?: 0.0

        Spacer(Modifier.height(tokens.extraPadding * 1.2f))
        PriceBreakdownCard(
            garment = selectedGarment?.displayName ?: selectedGarment?.name ?: "-",
            garmentPrice = selectedGarmentPrice,
            fabric = selectedFabric?.name ?: "-",
            fabricPrice = selectedFabricPrice,
            design = "-",
            designPrice = 0.0,
            addons = selectedWork?.workType ?: "-",
            addonsPrice = selectedWorkPrice,
            subtotal = formatPrice(subtotal),
            tax = formatPrice(tax),
            total = formatPrice(total),
            quantity = quantity
        )
    }

    TipBanner("Tip: You can edit any item's price inline using the pencil icon.")
}

data class PricingCardModel(
    val id: String,
    val title: String,
    val price: Double
)

@Composable
private fun PricingSectionGrid(
    sectionTitle: String,
    items: List<PricingCardModel>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onEdit: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
        Text(
            text = sectionTitle,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
            ) {
                rowItems.forEach { item ->
                    val isSelected = item.id == selectedId
                    PricingSelectableCard(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onSelect(item.id) },
                        onEdit = { onEdit(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                }

                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(tokens.extraPadding * 0.8f))
        }
    }
}

@Composable
private fun PricingSelectableCard(
    item: PricingCardModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Primary else BorderGray,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f)
            )
            .background(if (isSelected) primary_light else whiteBg)
            .clickable { onClick() }
            .padding(tokens.extraPadding * 0.8f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.title,
                    fontSize = tokens.bodySmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Primary else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(tokens.iconSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Price",
                        tint = if (isSelected) Primary else headerGrey,
                        modifier = Modifier.size(tokens.iconSize * 0.75f)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "₹${item.price.toInt()}",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Primary else headerGrey
            )
        }
    }
}

@Composable
private fun SinglePriceEditDialog(
    title: String,
    initialPrice: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    val tokens = LocalAppTokens.current
    var priceText by remember { mutableStateOf(initialPrice.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        title = {
            Text(
                text = "Edit Price",
                fontSize = tokens.h2,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(tokens.extraPadding))
                FormLabel("Price (₹)", isRequired = true)
                FormTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    placeholder = "Enter new price",
                    keyboardType = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = priceText.toDoubleOrNull() ?: 0.0
                    onSave(parsed)
                },
                enabled = priceText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            ) {
                Text("Save", fontSize = tokens.bodySmall, color = whiteBg)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontSize = tokens.bodySmall, color = headerGrey)
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// STEP 1 — Customer Selection
// ─────────────────────────────────────────────────────────────
@Composable
private fun Step1CustomerSelection(
    tab: String,
    onTabChange: (String) -> Unit,
    items: List<CustomerOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    isLoading: Boolean
) {
    val tokens = LocalAppTokens.current

    CustomerLeadToggle(selected = tab, onSelect = onTabChange)
    Spacer(Modifier.height(tokens.extraPadding * 1.5f))

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (tab == "Customer") "Select Customer" else "Select Lead",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = mutedText,
            modifier = Modifier.size(tokens.iconSize)
        )
    }
    Spacer(Modifier.height(tokens.extraPadding))

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = tokens.extraPadding * 2),
                contentAlignment = Alignment.Center
            ) {
                CirculerProgressIndicatorReuse()
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = tokens.extraPadding * 2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tab == "Customer") "No customers found" else "No leads found",
                    fontSize = tokens.bodyMedium,
                    color = mutedText
                )
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

@Composable
private fun CustomerLeadToggle(selected: String, onSelect: (String) -> Unit) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
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
            .border(
                width = 1.dp,
                color = if (selected) Primary else BorderGray,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f)
            )
            .background(whiteBg)
            .clickable { onSelect() }
            .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.8f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomRadioDot(selected = selected)
        Spacer(Modifier.width(tokens.extraPadding))
        Column {
            Text(formattedName, fontSize = tokens.bodyMedium, color = TextPrimary)
            Text(customer.phone, fontSize = tokens.caption, color = mutedText)
        }
    }
}

@Composable
private fun CustomRadioDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(if (selected) Primary else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Primary else BorderGray,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(whiteBg)
            )
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Quantity", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                modifier = Modifier.size(tokens.fieldHeight * 0.8f)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Primary)
            }
            Text(
                text = "$quantity",
                fontSize = tokens.bodyLarge,
                color = blackTitle,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = tokens.extraPadding)
            )
            IconButton(
                onClick = { onQuantityChange(quantity + 1) },
                modifier = Modifier.size(tokens.fieldHeight * 0.8f)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Primary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 3 — Pricing Summary with PDF Preview & Dynamic Request Building
// ─────────────────────────────────────────────────────────────
@Suppress("unused_parameter")
@Composable
private fun Step3PricingSummary(
    token: String,
    previewShown: Boolean,
    onPreview: () -> Unit,
    onComplete: () -> Unit = {},
    customerName: String,
    logoBase64: String = "",
    subtotal: Double,
    tax: Double,
    total: Double,
    quotationNumber: String = "QUO-${System.currentTimeMillis()}",
    quotationDate: String = SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date()),
    customerAddress: String = "",
    customerVat: String = "",
    customerEmail: String = "",
    customerPhone: String = "",
    termsAndConditions: List<String> = listOf(
        "50% advance payment required to start work",
        "Final measurements will be taken before starting the work",
        "First fitting will be provided after 7 days",
        "One free alteration included within 30 days",
        "Express delivery subject to fabric availability"
    ),
    onEdit: () -> Unit = {},
    customerId: String? = null,
    garmentBreakdowns: List<GarmentBreakdown> = emptyList(),
    quotationViewModel: QuotationViewModel? = null,

    // Dynamic Payload Dependencies
    branchState: BranchUiState,
    taxGroups: List<com.cuso.tailor.model.inventory.TaxGroupDto>,
    selectedGarment: GarmentItem?,
    selectedFabric: FabricPricingItem?,
    selectedWork: WorkPricingItem?,
    garmentPriceOverrides: Map<String, Double>,
    fabricPriceOverrides: Map<String, Double>,
    workPriceOverrides: Map<String, Double>,
    quantity: Int,
    selectedSalespersonId: String? = null
) {
    val tokens = LocalAppTokens.current
    var isDownloading by remember { mutableStateOf(false) }
    var isSavingDraft by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val pdfGenerator = remember { QuotationPdfGenerator(context) }

    val saveState = quotationViewModel?.saveState?.collectAsStateWithLifecycle()?.value
    var showDynamicIslandSuccess by remember { mutableStateOf(false) }
    var dynamicIslandMessage by remember { mutableStateOf("") }

    if (showDynamicIslandSuccess) {
        DynamicIslandSuccess(
            message = dynamicIslandMessage,
            onDismiss = { showDynamicIslandSuccess = false },
            modifier = Modifier
        )
    }

    LaunchedEffect(saveState) {
        when (saveState) {
            is QuotationSaveUiState.Success -> {
                isSavingDraft = false
                showDynamicIslandSuccess = true
                dynamicIslandMessage = "Saved as draft successfully"
                quotationViewModel.resetState()
                onComplete()
            }
            is QuotationSaveUiState.Error -> {
                isSavingDraft = false
                Toast.makeText(context, "Failed to save draft: ${saveState.message}", Toast.LENGTH_SHORT).show()
                quotationViewModel.resetState()
            }
            is QuotationSaveUiState.Loading -> {
                isSavingDraft = true
            }
            else -> Unit
        }
    }

    /**
     * Builds the quotation request payload dynamically using selected models.
     */
    fun buildSaveDraftRequest(): CreateQuotationRequest? {
        val custId = customerId ?: return null
        val staffId = selectedSalespersonId ?: return null

        // 1. Resolve dynamic branch ID
        val branches = (branchState as? BranchUiState.Success)?.branches ?: emptyList()
        val resolvedBranchId = branches.firstOrNull()?.id ?: return null

        // 2. Resolve default tax group ID
        val defaultTaxGroupId = taxGroups.firstOrNull()?.id ?: ""

        // 3. Generate ISO 8601 timestamps
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val currentDate = Date()
        val calendar = Calendar.getInstance().apply {
            time = currentDate
            add(Calendar.DAY_OF_YEAR, 21) // 3-week expiry
        }
        val currentIso = isoFormat.format(currentDate)
        val expiryIso = isoFormat.format(calendar.time)

        val tempParentId = "temp_quote_${System.currentTimeMillis()}"
        val lineItems = mutableListOf<QuotationPayloadItem>()

        // ── Line 1: Custom Garment ──
        if (selectedGarment != null) {
            val segment = selectedGarment.applicableSegments.firstOrNull()
            val garmentPrice = garmentPriceOverrides[selectedGarment.id] ?: selectedGarment.baseStitchingCharge
            val lineTotal = garmentPrice * quantity

            lineItems.add(
                QuotationPayloadItem(
                    id = tempParentId,
                    lineType = "Custom_Garment",
                    itemDescription = "${selectedGarment.displayName ?: selectedGarment.name} Stitching",
                    isTaxable = true,
                    taxGroupId = defaultTaxGroupId,
                    quantity = quantity.toDouble(),
                    unit = "Piece",
                    unitPrice = garmentPrice,
                    discountAmount = 0.0,
                    lineTotal = lineTotal,
                    customGarment = CustomGarmentPayload(
                        segmentId = segment?.id ?: "",
                        segmentName = segment?.displayName ?: segment?.name ?: "",
                        garmentId = selectedGarment.id,
                        garmentName = selectedGarment.name,
                        garmentCategoryId = selectedGarment.id,
                        categoryDisplayName = selectedGarment.displayName ?: selectedGarment.name,
                        designId = null,
                        designName = null,
                        sizeStandard = "36",
                        stitchingType = "Normal Machine",
                        fabricSource = if (selectedFabric != null) "Store_Fabric" else "Customer_Fabric",
                        fabricNotes = selectedFabric?.name ?: "",
                        specialInstructions = ""
                    )
                )
            )
        }

        // ── Line 2: Garment Material (Fabric) ──
        if (selectedFabric != null) {
            val fabricUnitPrice = fabricPriceOverrides[selectedFabric.id] ?: selectedFabric.sellingPrice
            val fabricQty = 1.5 // Standard meter requirement per garment
            val lineTotal = fabricUnitPrice * fabricQty

            lineItems.add(
                QuotationPayloadItem(
                    parentLineId = tempParentId,
                    lineType = "Garment_Material",
                    itemDescription = "${selectedFabric.name} (${fabricQty}M)",
                    isTaxable = true,
                    taxGroupId = defaultTaxGroupId,
                    hsnCode = "5007",
                    quantity = fabricQty,
                    unit = selectedFabric.unit.ifBlank { "Meter" },
                    unitPrice = fabricUnitPrice,
                    discountAmount = 0.0,
                    lineTotal = lineTotal,
                    product = ProductPayload(
                        inventoryItemId = selectedFabric.id,
                        sku = selectedFabric.sku,
                        itemName = selectedFabric.name
                    )
                )
            )
        }

        // ── Line 3: Garment Addon (Work / Craftsmanship) ──
        if (selectedWork != null) {
            val workUnitPrice = workPriceOverrides[selectedWork.id] ?: selectedWork.basePrice
            val lineTotal = workUnitPrice * 1.0

            lineItems.add(
                QuotationPayloadItem(
                    parentLineId = tempParentId,
                    lineType = "Garment_Addon",
                    itemDescription = selectedWork.workType,
                    isTaxable = selectedWork.isTaxable,
                    taxGroupId = selectedWork.taxGroup?.id ?: defaultTaxGroupId,
                    sacCode = "998812",
                    quantity = 1.0,
                    unit = "Piece",
                    unitPrice = workUnitPrice,
                    discountAmount = 0.0,
                    lineTotal = lineTotal,
                    addonWork = AddonWorkPayload(
                        workPricingId = selectedWork.id,
                        workType = selectedWork.workType,
                        specialInstructions = ""
                    )
                )
            )
        }

        if (lineItems.isEmpty()) return null

        return CreateQuotationRequest(
            branchId = resolvedBranchId,
            customerId = custId,
            salespersonId = staffId,
            quotationDate = currentIso,
            expiryDate = expiryIso,
            currency = "INR",
            notes = "Direct quotation generated via app",
            items = lineItems,
            deliveryCharge = 0.0
        )
    }

    val pdfData = remember(
        customerName, garmentBreakdowns, subtotal, total, logoBase64
    ) {
        QuotationPdfGenerator.QuotationData(
            quotationNumber = quotationNumber,
            quotationDate = quotationDate,
            customerName = customerName,
            logoUrl = logoBase64.ifEmpty { null },
            customerAddress = customerAddress.ifEmpty { "Customer Delivery Address" },
            customerVat = customerVat,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            items = garmentBreakdowns.map { b ->
                QuotationPdfGenerator.QuotationItem(
                    description = b.garmentName,
                    quantity = b.quantity,
                    rate = if (b.quantity > 0) b.itemSubtotal / b.quantity else 0.0,
                    amount = b.itemSubtotal
                )
            },
            subtotal = subtotal,
            discountPercent = 0.0,
            discountAmount = 0.0,
            total = total,
            termsAndConditions = termsAndConditions,
            thankYouMessage = "Thank you for your business!",
            poweredBy = "This is a computer-generated quotation and does not require a signature."
        )
    }

    val saveDraftAction: () -> Unit = {
        val request = buildSaveDraftRequest()
        if (request == null) {
            Toast.makeText(context, "Please select customer, salesperson, and pricing first", Toast.LENGTH_SHORT).show()
        } else if (!isSavingDraft) {
            quotationViewModel?.saveDraft(request)
        }
    }

    val shareQuotationPdf: (targetPackage: String?) -> Unit = { targetPackage ->
        pdfGenerator.downloadQuotationPdf(pdfData) { saved ->
            val uri = saved?.uri
            if (uri != null) {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Quotation $quotationNumber")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (targetPackage != null) setPackage(targetPackage)
                }
                try {
                    context.startActivity(intent)
                } catch (_: android.content.ActivityNotFoundException) {
                    val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Quotation $quotationNumber")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(fallbackIntent, "Send via"))
                }
            } else {
                Toast.makeText(context, "Failed to prepare PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (!previewShown) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.8f)) {
                Text("Quotation Summary", fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(tokens.extraPadding))
                SummaryRow("Customer", customerName)

                garmentBreakdowns.forEachIndexed { idx, b ->
                    Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                    Text(
                        text = "${idx + 1}. ${b.garmentName}",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    SummaryRow("Fabric", b.fabricName)
                    SummaryRow("Workmanship", b.addonsNames.ifBlank { "-" })
                    SummaryRow("Quantity", b.quantity.toString())
                }
            }

            garmentBreakdowns.forEach { b ->
                PriceBreakdownCard(
                    garment = b.garmentName,
                    garmentPrice = b.basePrice,
                    fabric = b.fabricName,
                    fabricPrice = b.fabricPrice,
                    design = b.designName,
                    designPrice = b.designPrice,
                    addons = b.addonsNames,
                    addonsPrice = b.addonsPrice,
                    subtotal = formatPrice(b.itemSubtotal),
                    tax = formatPrice(b.itemSubtotal * TAX_RATE),
                    total = formatPrice(b.itemSubtotal * (1 + TAX_RATE)),
                    quantity = b.quantity,
                    showAllItems = false
                )
            }

            TipBanner("Tip: You can apply discounts in the next step.")

            SendQuotationSection(
                onWhatsApp = { shareQuotationPdf("com.whatsapp") },
                onEmail = { shareQuotationPdf(null) }
            )

            QuickActionsRow(
                onDiscount = {},
                onEdit = onEdit,
                onSaveDraft = saveDraftAction,
                isSavingDraft = isSavingDraft
            )

            Spacer(Modifier.height(tokens.extraPadding * 2))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (tokens.isTablet) Modifier.verticalScroll(rememberScrollState())
                    else Modifier.fillMaxHeight()
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (tokens.isTablet) tokens.screenPadding else 16.dp,
                        vertical = tokens.extraPadding * 0.8f
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preview",
                    fontSize = tokens.h2,
                    color = TextPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (!isDownloading) {
                                isDownloading = true
                                pdfGenerator.downloadQuotationPdf(pdfData) { saved ->
                                    isDownloading = false
                                    if (saved != null) {
                                        Toast.makeText(context, "Downloaded: ${saved.displayName}", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Failed to download PDF", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = !isDownloading,
                        modifier = Modifier.size(tokens.buttonHeight * 0.9f)
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(tokens.iconSize),
                                strokeWidth = 2.dp,
                                color = Primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                modifier = Modifier.size(tokens.iconSize * 1.2f),
                                tint = Primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { pdfGenerator.printQuotationPdf(pdfData) },
                        modifier = Modifier.size(tokens.buttonHeight * 0.9f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print",
                            modifier = Modifier.size(tokens.iconSize * 1.2f),
                            tint = Primary
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (tokens.isTablet) Modifier.height(540.dp)
                        else Modifier.weight(1f)
                    )
                    .padding(horizontal = if (tokens.isTablet) tokens.screenPadding else 8.dp)
                    .background(whiteBg)
            ) {
                AndroidView(
                    factory = { ctx ->
                        android.webkit.WebView(ctx).apply {
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            webViewClient = android.webkit.WebViewClient()
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(
                            null,
                            pdfGenerator.buildQuotationHtml(pdfData),
                            "text/html",
                            "UTF-8",
                            null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            SendQuotationSection(
                onWhatsApp = { shareQuotationPdf("com.whatsapp") },
                onEmail = { shareQuotationPdf(null) }
            )

            QuickActionsRow(
                onDiscount = {},
                onEdit = onEdit,
                onSaveDraft = saveDraftAction,
                isSavingDraft = isSavingDraft
            )

            Spacer(Modifier.height(if (tokens.isTablet) 80.dp else 16.dp))
        }
    }
}

@Composable
private fun SendQuotationSection(
    onWhatsApp: () -> Unit,
    onEmail: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.8f)
    ) {
        Text("Send Quotation", fontSize = tokens.bodyLarge, color = TextPrimary)
        Spacer(Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Button(
                onClick = onWhatsApp,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight * 1.15f),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                colors = ButtonDefaults.buttonColors(containerColor = darkGreenBg, contentColor = whiteBg),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_whatsapp),
                    contentDescription = "WhatsApp",
                    modifier = Modifier.size(tokens.iconSize),
                    tint = whiteBg
                )
                Spacer(Modifier.width(8.dp))
                Text("WhatsApp", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = whiteBg)
            }

            Button(
                onClick = onEmail,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight * 1.15f),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = whiteBg),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mail),
                    contentDescription = "Email",
                    modifier = Modifier.size(tokens.iconSize),
                    tint = whiteBg
                )
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
    isSavingDraft: Boolean = false
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.8f)
    ) {
        Text("Quick Actions", fontSize = tokens.bodyLarge, color = TextPrimary)
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
            label = if (isSavingDraft) "Saving..." else "Save as Draft",
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
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = whiteBg,
            contentColor = TextPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(tokens.iconSize), tint = TextPrimary)
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = tokens.bodySmall, color = mutedText)
            Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        HorizontalDivider(color = light_grey, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun QuotationStepper(currentStep: Int) {
    val tokens = LocalAppTokens.current
    val steps = listOf("Customer Selection", "Garment Details", "Pricing Summary")

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = tokens.extraPadding * 1.5f)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding * 2.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, _ ->
                val stepNum = index + 1
                StepCircle(stepNum = stepNum, currentStep = currentStep)

                if (index != steps.lastIndex) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(if (stepNum < currentStep) darkGreenBg else BorderGray)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding * 2.5f),
            verticalAlignment = Alignment.Top
        ) {
            steps.forEachIndexed { index, label ->
                val stepNum = index + 1

                Box(
                    modifier = Modifier.size(width = 30.dp, height = 34.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (stepNum == currentStep) {
                        Text(
                            text = label,
                            fontSize = tokens.caption,
                            color = Primary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = true,
                            modifier = Modifier.wrapContentWidth(unbounded = true)
                        )
                    }
                }

                if (index != steps.lastIndex) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StepCircle(stepNum: Int, currentStep: Int) {
    val tokens = LocalAppTokens.current
    val isDone = stepNum < currentStep
    val isCurrent = stepNum == currentStep

    Box(
        modifier = Modifier
            .size(tokens.iconSize * 1.5f)
            .clip(CircleShape)
            .background(
                when {
                    isDone -> darkGreenBg
                    isCurrent -> Primary
                    else -> whiteBg
                }
            )
            .then(
                if (!isDone && !isCurrent) Modifier.border(1.5.dp, sectionBorder, CircleShape) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isDone) {
            Icon(Icons.Default.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize * 0.8f))
        } else {
            Text(
                text = "$stepNum",
                color = if (isCurrent) whiteBg else mutedText,
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PriceBreakdownCard(
    garment: String = "-",
    garmentPrice: Double = 0.0,
    fabric: String = "-",
    fabricPrice: Double = 0.0,
    design: String = "-",
    designPrice: Double = 0.0,
    addons: String = "",
    addonsPrice: Double = 0.0,
    subtotal: String = formatPrice(0.0),
    tax: String = formatPrice(0.0),
    total: String = formatPrice(0.0),
    quantity: Int = 1,
    showAllItems: Boolean = true
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.9f))
            Spacer(Modifier.width(6.dp))
            Text("Price breakdown", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Spacer(Modifier.height(tokens.extraPadding))

        BreakdownRowWithPrice("Garment", garment, garmentPrice)
        if (fabric != "-") {
            BreakdownRowWithPrice("Fabric", fabric, fabricPrice)
        }
        if (design != "-") {
            BreakdownRowWithPrice("Design", design, designPrice)
        }
        if (addons.isNotEmpty() && addons != "-") {
            BreakdownRowWithPrice("Workmanship", addons, addonsPrice)
        }
        BreakdownRow("Quantity", quantity.toString())

        HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
        BreakdownRow("Subtotal", subtotal)
        BreakdownRow("Tax (18%)", tax)
        HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Amount", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(total, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
private fun BreakdownRowWithPrice(label: String, name: String, price: Double) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label: $name", fontSize = tokens.bodySmall, color = TextSecondary)
        Text(formatPrice(price), fontSize = tokens.bodySmall, color = TextPrimary)
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = tokens.bodySmall, color = TextSecondary)
        Text(value, fontSize = tokens.bodySmall, color = TextPrimary)
    }
}

@Composable
private fun TipBanner(text: String) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f)
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
            .background(activity_purple_bg)
            .padding(tokens.extraPadding),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(tokens.iconSize)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = tokens.caption, color = Primary)
    }
}