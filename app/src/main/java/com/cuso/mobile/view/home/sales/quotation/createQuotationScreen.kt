@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)
package com.cuso.mobile.view.home.sales.quotation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.sales.AddonOption
import com.cuso.mobile.model.sales.CreateQuotationRequest
import com.cuso.mobile.model.sales.CustomerSnapshot
import com.cuso.mobile.model.sales.CustomerSnapshotAddress
import com.cuso.mobile.model.sales.DesignOption
import com.cuso.mobile.model.sales.FabricOption
import com.cuso.mobile.model.sales.QuotationItemInput
import com.cuso.mobile.model.sales.QuotationOptionInput
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.title_font
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.home.reusablecomposables.StepNavigationFab
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.view.home.pdfgenerator.QuotationPdfGenerator
import com.cuso.mobile.viewmodel.CustomerViewModel
import com.cuso.mobile.viewmodel.GarmentPricingUiState
import com.cuso.mobile.viewmodel.GarmentPricingViewModel
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.SalesOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Purple = Color(0xFF3B3BF9)
private val Green = Color(0xFF22C55E)
private val BorderGray = Color(0xFFE5E7EB)
private val TextGray = Color(0xFF6B7280)
private val MutedGray = Color(0xFF9CA3AF)
private val TitleDark = Color(0xFF111827)
private val TintBg = Color(0xFFEEF2FF)
private val TipBg = Color(0xFFEFF6FF)
private val TipBlue = Color(0xFF2563EB)

private const val TAX_RATE = 0.18

// ── Models ──
data class CustomerOption(
    val id: String,
    val name: String,
    val phone: String,
    val addressLine: String = "",
    val city: String = "",
    val pincode: String = ""
)

data class GarmentOption(
    val id: String,
    val name: String,
    val price: Double,
    val fabricOptions: List<FabricOption> = emptyList(),
    val designOptions: List<DesignOption> = emptyList(),
    val addons: List<AddonOption> = emptyList()
)

data class GarmentSelectionState(
    val garmentId: String,
    val fabric: FabricOption? = null,
    val design: DesignOption? = null,
    val addons: List<AddonOption> = emptyList(),
    val quantity: Int = 1
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

// ── Formatting helpers ──
private fun formatPrice(amount: Double): String =
    "₹${String.format(Locale.US, "%.2f", amount)}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuotationScreen(
    quotationId: String? = null,
    mode: String = "create",   // "create" | "view" | "edit"
    onClose: () -> Unit = {},
    onSave: () -> Unit = {},
    token: String
) {
    val customerViewModel: CustomerViewModel = hiltViewModel()
    val salesOrderViewModel: SalesOrderViewModel = hiltViewModel()
    val garmentPricingViewModel: GarmentPricingViewModel = hiltViewModel()
    val quotationViewModel: com.cuso.mobile.viewmodel.QuotationViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

    val customerState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val orderState by salesOrderViewModel.orderState.collectAsStateWithLifecycle()
    val garmentPricingState by garmentPricingViewModel.uiState.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

    var currentStep by remember { mutableIntStateOf(if (quotationId != null) 3 else 1) }
    var customerLeadTab by remember { mutableStateOf("Customer") }
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }


    var selectedGarmentId by remember { mutableStateOf<String?>(null) }
    var selectedFabric by remember { mutableStateOf<FabricOption?>(null) }
    var selectedDesign by remember { mutableStateOf<DesignOption?>(null) }

    var selectedGarments by remember { mutableStateOf<List<GarmentSelectionState>>(emptyList()) }

    // Helper to update one garment's selection without touching the others
    fun updateGarment(garmentId: String, update: (GarmentSelectionState) -> GarmentSelectionState) {
        selectedGarments = selectedGarments.map { if (it.garmentId == garmentId) update(it) else it }
    }
    var selectedAddons by remember { mutableStateOf<List<AddonOption>>(emptyList()) }
    var quantity by remember { mutableIntStateOf(1) }
    var previewShown by remember { mutableStateOf(mode == "view") }
    var isPrefilling by remember { mutableStateOf(quotationId != null) }

    val customers = remember(customerState) {
        when (customerState) {
            is com.cuso.mobile.viewmodel.CustomerUiState.Success -> {
                (customerState as com.cuso.mobile.viewmodel.CustomerUiState.Success).customers.map { customer ->
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
            is com.cuso.mobile.viewmodel.OrderUiState.Success -> {
                (orderState as com.cuso.mobile.viewmodel.OrderUiState.Success).orders.map { order ->
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

    val garmentOptions = remember(garmentPricingState) {
        when (garmentPricingState) {
            is GarmentPricingUiState.Success -> {
                (garmentPricingState as GarmentPricingUiState.Success).items.map { item ->
                    GarmentOption(
                        id = item.garmentId,
                        name = item.garmentName,
                        price = item.basePrice,
                        fabricOptions = item.fabricOptions,
                        designOptions = item.designOptions,
                        addons = item.addons
                    )
                }
            }
            else -> emptyList()
        }
    }

    val garmentBreakdowns = remember(selectedGarments, garmentOptions) {
        selectedGarments.mapNotNull { sel ->
            val option = garmentOptions.find { it.id == sel.garmentId } ?: return@mapNotNull null
            val perUnit = option.price + (sel.fabric?.price ?: 0.0) + (sel.design?.price ?: 0.0) +
                    sel.addons.sumOf { it.price }
            GarmentBreakdown(
                garmentId = sel.garmentId,
                garmentName = option.name,
                basePrice = option.price,
                fabricName = sel.fabric?.name ?: "-",
                fabricPrice = sel.fabric?.price ?: 0.0,
                designName = sel.design?.name ?: "-",
                designPrice = sel.design?.price ?: 0.0,
                addonsNames = sel.addons.joinToString(", ") { it.name },
                addonsPrice = sel.addons.sumOf { it.price },
                quantity = sel.quantity,
                itemSubtotal = perUnit * sel.quantity
            )
        }
    }

    val subtotal = garmentBreakdowns.sumOf { it.itemSubtotal }
    val tax = subtotal * TAX_RATE
    val total = subtotal + tax

    val selectedCustomer = remember(currentItems, selectedCustomerId) {
        currentItems.find { it.id == selectedCustomerId }
    }

    val selectedGarment = remember(garmentOptions, selectedGarmentId) {
        garmentOptions.find { it.id == selectedGarmentId }
    }

    val basePrice = selectedGarment?.price ?: 0.0
    val fabricPrice = selectedFabric?.price ?: 0.0
    val designPrice = selectedDesign?.price ?: 0.0
    val addonsPrice = selectedAddons.sumOf { it.price }
//    val subtotal = (basePrice + fabricPrice + designPrice + addonsPrice) * quantity
//    val tax = subtotal * TAX_RATE
//    val total = subtotal + tax


    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers()
        salesOrderViewModel.fetchOrders()
        garmentPricingViewModel.loadGarmentPricing()
        profileViewModel.loadOrganization(token)
        if (quotationId != null) {
            quotationViewModel.fetchQuotationById(quotationId)
        }
    }

    val detailState by quotationViewModel.detailState.collectAsStateWithLifecycle()

    // ── Prefill state once quotation detail + garment options are both ready ──
    LaunchedEffect(detailState, garmentOptions) {
        val state = detailState
        if (quotationId != null && state is com.cuso.mobile.viewmodel.QuotationDetailUiState.Success && garmentOptions.isNotEmpty()) {
            val dto = state.quotation
            selectedCustomerId = dto.customerId

            // ✅ CHANGED — build selection for EVERY item, not just the first
            selectedGarments = dto.items.mapNotNull { item ->
                val matchedGarment = garmentOptions.find { it.id == item.garmentCategoryId } ?: return@mapNotNull null
                GarmentSelectionState(
                    garmentId = item.garmentCategoryId?:"",
                    fabric = matchedGarment.fabricOptions.find { it.name == item.fabric?.label },
                    design = matchedGarment.designOptions.find { it.name == item.design?.label },
                    addons = matchedGarment.addons.filter { addon ->
                        item.addons.any { it.label == addon.name }
                    },
                    quantity = item.quantity
                )
            }
            isPrefilling = false
        } else if (state is com.cuso.mobile.viewmodel.QuotationDetailUiState.Error) {
            isPrefilling = false
        } else if (quotationId == null) {
            isPrefilling = false
        }
    }
    val organizationLogoUrl = remember(profileState) {
        (profileState as? com.cuso.mobile.viewmodel.ProfileUiState.Success)
            ?.data?.organization?.organizationPicture ?: ""
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

    LaunchedEffect(selectedCustomerId) {
        if (selectedCustomerId != null) {
            garmentPricingViewModel.loadGarmentPricing()
        }
    }

    LaunchedEffect(customerLeadTab) {
        if (customerLeadTab == "Customer") {
            customerViewModel.loadCustomers()
        } else {
            salesOrderViewModel.fetchOrders()
        }
    }

    fun goToNextStep() {
        when (currentStep) {
            1 -> if (selectedCustomerId != null) currentStep++
            2 -> if (selectedGarments.isNotEmpty()) currentStep++   // ✅ CHANGED
            3 -> onSave()
        }
    }

    fun goToPreviousStep() {
        if (currentStep > 1) currentStep--
    }
    if (isPrefilling) {
        Box(
            modifier = Modifier.fillMaxSize().background(whiteBg),
            contentAlignment = Alignment.Center
        ) {
            CirculerProgressIndicatorReuse()
        }
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Create Quotation", fontSize = title_font, fontWeight = FontWeight.Bold, color = title_color)
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TitleDark,
                    modifier = Modifier.size(22.dp).clickable { onClose() }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    // ✅ CHANGED — scroll allowed on step 3 too, EXCEPT when PDF preview WebView is showing
                    if (currentStep != 3 || !previewShown) Modifier.verticalScroll(rememberScrollState())
                    else Modifier
                )
        ){
            QuotationStepper(currentStep = currentStep)

            when (currentStep) {
                1 -> Step1CustomerSelection(
                    tab = customerLeadTab,
                    onTabChange = { customerLeadTab = it },
                    items = currentItems,
                    selectedId = selectedCustomerId,
                    onSelect = { selectedCustomerId = it },
                    isLoading = when (customerLeadTab) {
                        "Customer" -> customerState is com.cuso.mobile.viewmodel.CustomerUiState.Loading
                        else -> orderState is com.cuso.mobile.viewmodel.OrderUiState.Loading
                    }
                )

                2 -> Step2GarmentDetails(
                    garmentOptions = garmentOptions,
                    selectedGarments = selectedGarments,                          // ✅ CHANGED
                    onToggleGarment = { garmentId ->                               // ✅ CHANGED
                        selectedGarments = if (selectedGarments.any { it.garmentId == garmentId }) {
                            selectedGarments.filter { it.garmentId != garmentId }
                        } else {
                            selectedGarments + GarmentSelectionState(garmentId = garmentId)
                        }
                    },
                    onSelectFabric = { garmentId, fabric ->                        // ✅ CHANGED
                        updateGarment(garmentId) { it.copy(fabric = fabric) }
                    },
                    onSelectDesign = { garmentId, design ->                        // ✅ CHANGED
                        updateGarment(garmentId) { it.copy(design = design) }
                    },
                    onToggleAddon = { garmentId, addon ->                           // ✅ CHANGED
                        updateGarment(garmentId) { sel ->
                            sel.copy(addons = if (sel.addons.contains(addon)) sel.addons - addon else sel.addons + addon)
                        }
                    },
                    onQuantityChange = { garmentId, qty ->                          // ✅ CHANGED
                        updateGarment(garmentId) { it.copy(quantity = qty) }
                    },
                    isLoading = garmentPricingState is GarmentPricingUiState.Loading,
                    garmentBreakdowns = garmentBreakdowns,                          // ✅ CHANGED
                    subtotal = subtotal,
                    tax = tax,
                    total = total
                )

                3 -> Step3PricingSummary(
                    token = token,
                    previewShown = previewShown,
                    onPreview = { previewShown = true },
                    onComplete = { onSave() },
                    customerName = selectedCustomer?.name ?: "-",
                    logoBase64 = logoBase64,
                    subtotal = subtotal,
                    tax = tax,
                    total = total,
                    quotationNumber = "QUO-${System.currentTimeMillis()}",
                    quotationDate = SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date()),
                    customerAddress = selectedCustomer?.let { "${it.name}\nPhone: ${it.phone}" } ?: "",
                    customerPhone = selectedCustomer?.phone ?: "",
                    customerId = selectedCustomerId,
                    garmentBreakdowns = garmentBreakdowns,   // ✅ CHANGED — single source now
                    quotationViewModel = quotationViewModel,
                    customerSnapshotName = selectedCustomer?.name ?: "",
                    customerSnapshotPhone = selectedCustomer?.phone ?: "",
                    customerSnapshotAddressLine = selectedCustomer?.addressLine ?: "",
                    customerSnapshotCity = selectedCustomer?.city ?: "",
                    customerSnapshotPincode = selectedCustomer?.pincode ?: "",
                    onEdit = {
                        previewShown = false
                        currentStep = 2
                    }
                )
            }

            Spacer(Modifier.height(90.dp))
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
    }
}

@Composable
private fun SendQuotationSection(
    onEmail: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text("Send Quotation", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleDark)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            Button(
                onClick = onEmail,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(16.dp),
                    tint = whiteBg
                )
                Spacer(Modifier.width(6.dp))
                Text("Share", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = whiteBg)
            }
        }
    }
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
    CustomerLeadToggle(selected = tab, onSelect = onTabChange)
    Spacer(Modifier.height(18.dp))

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (tab == "Customer") "Select Customer" else "Select Lead",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TitleDark
        )
        Icon(Icons.Default.Search, contentDescription = null, tint = MutedGray, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.height(10.dp))

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CirculerProgressIndicatorReuse()
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (tab == "Customer") "No customers found" else "No leads found",
                    fontSize = 14.sp,
                    color = MutedGray
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(whiteBg)
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        listOf("Customer", "Lead").forEach { label ->
            val isSelected = selected == label
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Color(0xFFEEF2FF) else Color.Transparent)
                    .clickable { onSelect(label) }
                    .padding(vertical = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF2F27CE) else Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun CustomerSelectionCard(customer: CustomerOption, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) Purple else BorderGray,
                shape = RoundedCornerShape(10.dp)
            )
            .background(if (selected) TintBg else whiteBg)
            .clickable { onSelect() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = Purple, disabledSelectedColor = BorderGray)
        )
        Spacer(Modifier.width(4.dp))
        Column {
            Text(customer.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleDark)
            Text(customer.phone, fontSize = 12.sp, color = MutedGray)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 2 — Garment Details
// ─────────────────────────────────────────────────────────────
@Composable
private fun Step2GarmentDetails(
    garmentOptions: List<GarmentOption>,
    selectedGarments: List<GarmentSelectionState>,
    onToggleGarment: (String) -> Unit,
    onSelectFabric: (String, FabricOption?) -> Unit,
    onSelectDesign: (String, DesignOption?) -> Unit,
    onToggleAddon: (String, AddonOption) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    isLoading: Boolean,
    garmentBreakdowns: List<GarmentBreakdown>,
    subtotal: Double,
    tax: Double,
    total: Double
) {
    Spacer(Modifier.height(4.dp))

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
            CirculerProgressIndicatorReuse()
        }
        return
    }

    // ── Multi-select garment grid ──
    GarmentOptionGrid(
        title = "Select Garment Type (multiple allowed)",
        options = garmentOptions,
        selectedIds = selectedGarments.map { it.garmentId }.toSet(),   // ✅ CHANGED — set of ids
        onToggle = onToggleGarment,                                     // ✅ CHANGED — toggle not replace
        showPrice = true
    )

    // ── One block per SELECTED garment, rendered one-by-one below ──
    selectedGarments.forEachIndexed { index, sel ->
        val garment = garmentOptions.find { it.id == sel.garmentId } ?: return@forEachIndexed
        val breakdown = garmentBreakdowns.find { it.garmentId == sel.garmentId }

        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(TintBg).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${index + 1}. ${garment.name}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TitleDark)
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MutedGray,
                    modifier = Modifier.size(18.dp).clickable { onToggleGarment(garment.id) }
                )
            }

            if (garment.fabricOptions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                OptionSelectionGrid(
                    title = "Select Fabric",
                    options = garment.fabricOptions.map { OptionWithPrice(it.name, it.price) },
                    selectedOption = sel.fabric?.name,
                    onSelect = { name ->
                        onSelectFabric(garment.id, garment.fabricOptions.find { it.name == name })
                    },
                    showPrice = true
                )
            }

            if (garment.designOptions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                OptionSelectionGrid(
                    title = "Select Design",
                    options = garment.designOptions.map { OptionWithPrice(it.name, it.price) },
                    selectedOption = sel.design?.name,
                    onSelect = { name ->
                        onSelectDesign(garment.id, garment.designOptions.find { it.name == name })
                    },
                    showPrice = true
                )
            }

            if (garment.addons.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                AddonSelectionGrid(
                    title = "Addons",
                    addons = garment.addons,
                    selectedAddons = sel.addons,
                    onToggle = { addon -> onToggleAddon(garment.id, addon) }
                )
            }

            Spacer(Modifier.height(8.dp))
            QuantitySelector(
                quantity = sel.quantity,
                onQuantityChange = { qty -> onQuantityChange(garment.id, qty) }
            )

            breakdown?.let {
                PriceBreakdownCard(
                    garment = it.garmentName,
                    garmentPrice = it.basePrice,
                    fabric = it.fabricName,
                    fabricPrice = it.fabricPrice,
                    design = it.designName,
                    designPrice = it.designPrice,
                    addons = it.addonsNames,
                    addonsPrice = it.addonsPrice,
                    subtotal = formatPrice(it.itemSubtotal),
                    tax = formatPrice(it.itemSubtotal * TAX_RATE),
                    total = formatPrice(it.itemSubtotal * (1 + TAX_RATE)),
                    quantity = it.quantity
                )
            }
        }
    }

    // ── Combined total across ALL selected garments ──
    if (selectedGarments.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF9FAFB))
                .padding(14.dp)
        ) {
            Text("Overall Total (${selectedGarments.size} garments)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TitleDark)
            Spacer(Modifier.height(8.dp))
            BreakdownRow("Subtotal", formatPrice(subtotal))
            BreakdownRow("Tax (18%)", formatPrice(tax))
            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TitleDark)
                Text(formatPrice(total), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Purple)
            }
        }
    }

    TipBanner("Tip: You can apply discounts in the next step.")
}

@Suppress("SameParameterValue")
@Composable
private fun OptionSelectionGrid(
    title: String,
    options: List<OptionWithPrice>,
    selectedOption: String?,
    onSelect: (String) -> Unit,
    showPrice: Boolean = true
) {
    if (options.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleDark)
        Spacer(Modifier.height(8.dp))

        options.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { option ->
                    val selected = option.name == selectedOption
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) Purple else BorderGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(if (selected) TintBg else whiteBg)
                            .clickable { onSelect(option.name) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            option.name,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                            color = TitleDark
                        )
                        if (showPrice && option.price > 0) {
                            Text(
                                formatPrice(option.price),
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Suppress("SameParameterValue")
@Composable
private fun GarmentOptionGrid(
    title: String,
    options: List<GarmentOption>,
    selectedIds: Set<String>,     // ✅ CHANGED — was selectedId: String?
    onToggle: (String) -> Unit,   // ✅ CHANGED — was onSelect
    showPrice: Boolean = true
) {
    if (options.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleDark)
        Spacer(Modifier.height(8.dp))

        options.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { option ->
                    val selected = option.id in selectedIds   // ✅ CHANGED
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) Purple else BorderGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(if (selected) TintBg else whiteBg)
                            .clickable { onToggle(option.id) }   // ✅ CHANGED
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Purple, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                option.name,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                color = TitleDark
                            )
                        }
                        if (showPrice && option.price > 0) {
                            Text(formatPrice(option.price), fontSize = 12.sp, color = TextGray)
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Suppress("SameParameterValue")
@Composable
private fun AddonSelectionGrid(
    title: String,
    addons: List<AddonOption>,
    selectedAddons: List<AddonOption>,
    onToggle: (AddonOption) -> Unit
) {
    if (addons.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleDark)
        Spacer(Modifier.height(8.dp))

        addons.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { addon ->
                    val selected = selectedAddons.contains(addon)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) Purple else BorderGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(if (selected) TintBg else whiteBg)
                            .clickable { onToggle(addon) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            addon.name,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                            color = TitleDark
                        )
                        if (addon.price > 0) {
                            Text(
                                formatPrice(addon.price),
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Quantity", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleDark)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Purple)
            }
            Text(
                "$quantity",
                fontSize = 16.sp,
                color=blackTitle,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(
                onClick = { onQuantityChange(quantity + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Purple)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 3 — Pricing Summary with PDF Preview
// ─────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────
// STEP 3 — Pricing Summary with PDF Preview
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
    // ✅ CHANGED — replaces garmentName/fabricName/designName/quantity/basePrice/fabricOption/designOption/addonOptions/garmentCategoryId
    garmentBreakdowns: List<GarmentBreakdown> = emptyList(),
    quotationViewModel: com.cuso.mobile.viewmodel.QuotationViewModel? = null,
    customerSnapshotName: String = "",
    customerSnapshotPhone: String = "",
    customerSnapshotAddressLine: String = "",
    customerSnapshotCity: String = "",
    customerSnapshotPincode: String = ""
) {
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
            is com.cuso.mobile.viewmodel.QuotationSaveUiState.Success -> {
                isSavingDraft = false
                // Toast-க்கு பதிலாக DynamicIslandSuccess State-ஐ true ஆக்கவும்:
                showDynamicIslandSuccess = true
                dynamicIslandMessage = "Saved as draft successfully"

                quotationViewModel.resetState()
                onComplete()
            }
            is com.cuso.mobile.viewmodel.QuotationSaveUiState.Error -> {
                isSavingDraft = false
                Toast.makeText(context, "Failed to save draft: ${saveState.message}", Toast.LENGTH_SHORT).show()
                quotationViewModel.resetState()
            }
            is com.cuso.mobile.viewmodel.QuotationSaveUiState.Loading -> {
                isSavingDraft = true
            }
            else -> Unit
        }
    }

    // ✅ CHANGED — builds ONE QuotationItemInput per garment in garmentBreakdowns
    fun buildSaveDraftRequest(): CreateQuotationRequest? {
        val custId = customerId ?: return null
        if (garmentBreakdowns.isEmpty()) return null

        val items = garmentBreakdowns.map { b ->
            val perUnitAmount = b.basePrice + b.fabricPrice + b.designPrice + b.addonsPrice
            QuotationItemInput(
                garmentCategoryId = b.garmentId,
                garmentName = b.garmentName,
                quantity = b.quantity,
                basePrice = b.basePrice,
                fabric = if (b.fabricName != "-") QuotationOptionInput(b.fabricName, b.fabricPrice) else null,
                design = if (b.designName != "-") QuotationOptionInput(b.designName, b.designPrice) else null,
                addons = if (b.addonsNames.isNotEmpty())
                    b.addonsNames.split(", ").map { name ->
                        QuotationOptionInput(name, 0.0) // per-addon price not separable from combined addonsPrice
                    } else emptyList(),
                expressCharge = 0.0,
                unitPrice = perUnitAmount,
                totalPrice = b.itemSubtotal
            )
        }

        return CreateQuotationRequest(
            customerId = custId,
            leadId = null,
            customerSnapshot = CustomerSnapshot(
                name = customerSnapshotName,
                phone = customerSnapshotPhone,
                email = "",
                address = CustomerSnapshotAddress(
                    addressLine = customerSnapshotAddressLine,
                    city = customerSnapshotCity,
                    pincode = customerSnapshotPincode
                )
            ),
            items = items,
            subTotal = subtotal,
            taxPercent = TAX_RATE * 100,
            taxAmount = tax,
            discountAmount = 0.0,
            grandTotal = total,
            status = "draft",
            notes = ""
        )
    }

    // ✅ CHANGED — pdf items built from ALL garments, not a single fallback item
    val pdfData = remember(
        customerName, garmentBreakdowns, subtotal, total, logoBase64
    ) {
        QuotationPdfGenerator.QuotationData(
            quotationNumber = quotationNumber,
            quotationDate = quotationDate,
            customerName = customerName,
            logoUrl = logoBase64.ifEmpty { null },
            customerAddress = customerAddress.ifEmpty { "4304 Liberty Avenue\n92680 Tustin, CA" },
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
            Toast.makeText(context, "Please select a customer and garment first", Toast.LENGTH_SHORT).show()
        } else if (!isSavingDraft) {
            quotationViewModel?.saveDraft(request)
        }
    }

    if (!previewShown) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("Quotation Summary", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TitleDark)
                Spacer(Modifier.height(10.dp))
                SummaryRow("Customer", customerName)

                garmentBreakdowns.forEachIndexed { idx, b ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${idx + 1}. ${b.garmentName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleDark
                    )
                    SummaryRow("Fabric", b.fabricName)
                    SummaryRow("Design Style", b.designName)
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

            if (garmentBreakdowns.size > 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF9FAFB))
                        .padding(14.dp)
                ) {
                    Text("Overall Total (${garmentBreakdowns.size} garments)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TitleDark)
                    Spacer(Modifier.height(8.dp))
                    BreakdownRow("Subtotal", formatPrice(subtotal))
                    BreakdownRow("Tax (18%)", formatPrice(tax))
                    HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TitleDark)
                        Text(formatPrice(total), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Purple)
                    }
                }
            }

            TipBanner("Tip: You can apply discounts in the next step.")
            Spacer(Modifier.height(90.dp))   // ✅ CHANGED — extra bottom padding so FAB doesn't overlap last item
        }

        QuickActionsRow(                      // ✅ MOVED outside the scroll column — stays pinned below
            onDiscount = {},
            onEdit = onEdit,
            onSaveDraft = saveDraftAction
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preview",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleDark
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
                                        Toast.makeText(
                                            context,
                                            "Downloaded: ${saved.displayName}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to download PDF",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },
                        enabled = !isDownloading,
                        modifier = Modifier.size(40.dp)
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Primary
                            )
                        } else {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Download",
                                modifier = Modifier.size(24.dp),
                                tint = Primary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            pdfGenerator.printQuotationPdf(pdfData)
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Print",
                            modifier = Modifier.size(24.dp),
                            tint = Primary
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
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

            QuickActionsRow(
                onDiscount = {},
                onEdit = onEdit,
                onSaveDraft = saveDraftAction
            )

            SendQuotationSection(
                onEmail = {
                    pdfGenerator.downloadQuotationPdf(pdfData) { saved ->
                        val uri = saved?.uri
                        if (uri != null) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Quotation $quotationNumber")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Send via Email"))
                        } else {
                            Toast.makeText(context, "Failed to prepare PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, color = MutedGray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleDark)
        }
        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun QuickActionsRow(
    onDiscount: () -> Unit,
    onEdit: () -> Unit,
    onSaveDraft: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text("Quick Actions", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleDark)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton("Discount", Icons.Default.Percent, Modifier.weight(1f), onClick = onDiscount)
            QuickActionButton("Edit", Icons.Default.Edit, Modifier.weight(1f), onClick = onEdit)
        }
        Spacer(Modifier.height(10.dp))
        QuickActionButton("Save as Draft", Icons.Default.Description, Modifier.fillMaxWidth(), onClick = onSaveDraft)
    }
}

@Composable
private fun QuickActionButton(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Purple, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleDark)
    }
}

// ─────────────────────────────────────────────────────────────
// SHARED — Stepper / Price breakdown / Tip banner
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuotationStepper(currentStep: Int) {
    val steps = listOf("Customer Selection", "Garment Details", "Pricing Summary")
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, _ ->
                val stepNum = index + 1
                StepCircle(stepNum = stepNum, currentStep = currentStep)
                if (index != steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (stepNum < currentStep) Green else BorderGray)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            steps[currentStep - 1],
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = Purple,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StepCircle(stepNum: Int, currentStep: Int) {
    val isDone = stepNum < currentStep
    val isCurrent = stepNum == currentStep
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                when {
                    isDone -> Green
                    isCurrent -> Purple
                    else -> whiteBg
                }
            )
            .then(
                if (!isDone && !isCurrent) Modifier.border(1.5.dp, Color(0xFFD1D5DB), CircleShape) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isDone) {
            Icon(Icons.Default.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(14.dp))
        } else {
            Text(
                "$stepNum",
                color = if (isCurrent) whiteBg else MutedGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Suppress("unused_parameter")
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MutedGray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Price breakdown", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleDark)
        }
        Spacer(Modifier.height(10.dp))

        BreakdownRowWithPrice("Garment", garment, garmentPrice)
        if (fabric != "-") {
            BreakdownRowWithPrice("Fabric", fabric, fabricPrice)
        }
        if (design != "-") {
            BreakdownRowWithPrice("Design", design, designPrice)
        }
        if (addons.isNotEmpty() && addons != "-") {
            BreakdownRowWithPrice("Addons", addons, addonsPrice)
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
            Text(
                "Total Amount",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TitleDark
            )
            Text(
                total,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TitleDark
            )
        }
    }
}

@Composable
private fun BreakdownRowWithPrice(label: String, name: String, price: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "$label: $name",
            fontSize = 13.sp,
            color = TextGray
        )
        Text(
            formatPrice(price),
            fontSize = 13.sp,
            color = TitleDark
        )
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextGray)
        Text(value, fontSize = 13.sp, color = TitleDark)
    }
}

@Suppress("SameParameterValue")
@Composable
private fun TipBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TipBg)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = TipBlue)
    }
}

data class OptionWithPrice(
    val name: String,
    val price: Double
)