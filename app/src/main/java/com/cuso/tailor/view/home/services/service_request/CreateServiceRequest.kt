@file:Suppress("UNUSED_VALUE", "unused", "unusedVariable")

package com.cuso.tailor.view.home.services.service_request

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.CustomerDropdownItem
import com.cuso.tailor.model.inventory.TaxGroupDto
import com.cuso.tailor.model.sales.OrderOverviewItem
import com.cuso.tailor.model.service.CreateServiceRequestItemPayload
import com.cuso.tailor.model.service.CreateServiceRequestPayload
import com.cuso.tailor.model.service.SalesOrderDropdownItem
import com.cuso.tailor.model.service.ServiceAlterationAction
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.currencySymbol
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.CustomerViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.OrderOverviewState
import com.cuso.tailor.viewmodel.OrderOverviewViewModel
import com.cuso.tailor.viewmodel.ServiceActionUiState
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import com.cuso.tailor.viewmodel.AlterationDeltaDraft
import com.cuso.tailor.viewmodel.ServiceRequestDraft

// State holder for granular alteration deltas spawned per chip
data class AlterationDeltaState(
    val area: String,
    var action: String = "Shorten",
    var currentVal: String = "",
    var requiredVal: String = "",
    var unit: String = "in",
    var specificNotes: String = ""
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateServiceRequest(
    onClose: () -> Unit = {},
    onCancel: () -> Unit = {},
    onNavigateToReview: () -> Unit = {},
    serviceViewModel: ServicesViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    orderOverviewViewModel: OrderOverviewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current

    // Collect ViewModels state flows
    val actionState by serviceViewModel.actionState.collectAsStateWithLifecycle()
    val customerDropdownList by customerViewModel.customerDropdownList.collectAsStateWithLifecycle()
    val salesOrdersList by serviceViewModel.salesOrdersDropdown.collectAsStateWithLifecycle()
    val isLoadingOrders by serviceViewModel.isLoadingSalesOrders.collectAsStateWithLifecycle()
    val taxGroups by inventoryViewModel.taxGroups.collectAsStateWithLifecycle()
    val overviewState by orderOverviewViewModel.overviewState.collectAsStateWithLifecycle()

    // Collects the draft state from ServicesViewModel
    val savedDraft by serviceViewModel.serviceDraft.collectAsStateWithLifecycle()

    // Mode Switch: 0 = Pre Order, 1 = External
    var selectedModeIndex by remember { mutableIntStateOf(savedDraft?.modeIndex ?: 0) }
    val modeTabs = remember {
        listOf(
            TabItem(label = "Pre Order", iconPainter = R.drawable.ic_clippad_lines),
            TabItem(label = "External", iconPainter = R.drawable.ic_shopping_bag)
        )
    }

    // Accordion Expansion States
    var customerInfoExpanded by remember { mutableStateOf(true) }
    var orderRefExpanded by remember { mutableStateOf(true) }
    var serviceDetailsExpanded by remember { mutableStateOf(true) }
    var garmentSpecsExpanded by remember { mutableStateOf(true) }
    var uploadEvidenceExpanded by remember { mutableStateOf(true) }
    var internalNotesExpanded by remember { mutableStateOf(true) }

    // 1. Customer Selection State
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<CustomerDropdownItem?>(null) }
    var customerId by remember { mutableStateOf(savedDraft?.customerId.orEmpty()) }
    var customerMobile by remember { mutableStateOf(savedDraft?.customerMobile.orEmpty()) }
    var customerEmail by remember { mutableStateOf(savedDraft?.customerEmail.orEmpty()) }

    // 2. Sales Order Reference State
    var orderDropdownExpanded by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<SalesOrderDropdownItem?>(null) }
    var orderDeliveryDate by remember { mutableStateOf(savedDraft?.orderDeliveryDate.orEmpty()) }
    var orderStatusText by remember { mutableStateOf(if (savedDraft?.selectedOrderId != null) "Delivered" else "No Order Linked") }
    var selectedCustomGarmentId by remember { mutableStateOf(savedDraft?.selectedCustomGarmentId) }

    // 3. Service Request Details
    var serviceCategory by remember { mutableStateOf(savedDraft?.serviceCategory ?: "Alteration") }
    var serviceCategoryExpanded by remember { mutableStateOf(false) }
    var priorityLevel by remember { mutableStateOf(savedDraft?.priorityLevel ?: "Medium") }
    var priorityExpanded by remember { mutableStateOf(false) }
    var preferredServiceDate by remember { mutableStateOf(savedDraft?.preferredServiceDate ?: "2026-10-22") }
    var expectedDeliveryDate by remember { mutableStateOf(savedDraft?.expectedDeliveryDate.orEmpty()) }

    // 4. Garment Specifications
    var garmentPiecesCount by remember { mutableIntStateOf(savedDraft?.garmentPiecesCount ?: 1) }
    var garmentDescription by remember { mutableStateOf(savedDraft?.garmentDescription.orEmpty()) }
    var garmentCategoryName by remember { mutableStateOf(savedDraft?.garmentCategoryName.orEmpty()) }
    var specificIssueDescription by remember { mutableStateOf(savedDraft?.specificIssueDescription.orEmpty()) }

    val allAlterationAreas = remember {
        listOf("Sleeve", "Waist", "Length", "Neck", "Shoulder", "Chest", "Fit", "Armhole", "Hip", "Cuff", "Collar", "Pocket", "Hem", "Side Seam", "Trouser / Bottom", "Other")
    }

    val activeAlterationDeltas = remember { mutableStateListOf<AlterationDeltaState>() }
    val allRequirements = remember {
        listOf("Maintain Original Design", "Preserve Existing Fabric", "Preserve Original Stitching", "Match Existing Finish")
    }
    val selectedRequirements = remember { mutableStateListOf<String>() }

    // Chargeable & Tax fields
    var isChargeableService by remember { mutableStateOf(savedDraft?.isChargeableService ?: false) }
    var ratePerPiece by remember { mutableStateOf(savedDraft?.ratePerPiece ?: "0") }
    var isTaxable by remember { mutableStateOf(savedDraft?.isTaxable ?: false) }
    var taxGroupDropdownExpanded by remember { mutableStateOf(false) }
    var selectedTaxGroup by remember { mutableStateOf<TaxGroupDto?>(null) }

    // Calculated line total
    val parsedRate = ratePerPiece.toDoubleOrNull() ?: 0.0
    val rawLineTotal = if (isChargeableService) parsedRate * garmentPiecesCount else 0.0

    // 5. Attachments
    var selectedAttachments by remember { mutableStateOf(savedDraft?.selectedAttachments.orEmpty()) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedAttachments = (selectedAttachments + uris).distinct()
        }
    }

    // 6. Internal Staff Notes
    var internalStaffNotes by remember { mutableStateOf(savedDraft?.internalStaffNotes.orEmpty()) }

    // Fetch dropdown APIs on launch
    LaunchedEffect(Unit) {
        customerViewModel.fetchCustomerDropdown()
        inventoryViewModel.fetchTaxGroups()
    }

    // Synchronize all saved draft fields back when returning from Review Screen
    LaunchedEffect(savedDraft) {
        savedDraft?.let { draft ->
            selectedModeIndex = draft.modeIndex
            customerId = draft.customerId
            customerMobile = draft.customerMobile
            customerEmail = draft.customerEmail
            orderDeliveryDate = draft.orderDeliveryDate
            selectedCustomGarmentId = draft.selectedCustomGarmentId
            serviceCategory = draft.serviceCategory
            priorityLevel = draft.priorityLevel
            preferredServiceDate = draft.preferredServiceDate
            expectedDeliveryDate = draft.expectedDeliveryDate
            garmentPiecesCount = draft.garmentPiecesCount
            garmentDescription = draft.garmentDescription
            garmentCategoryName = draft.garmentCategoryName
            specificIssueDescription = draft.specificIssueDescription
            isChargeableService = draft.isChargeableService
            ratePerPiece = draft.ratePerPiece
            isTaxable = draft.isTaxable
            selectedAttachments = draft.selectedAttachments
            internalStaffNotes = draft.internalStaffNotes

            activeAlterationDeltas.clear()
            draft.activeAlterationDeltas.forEach {
                activeAlterationDeltas.add(
                    AlterationDeltaState(it.area, it.action, it.currentVal, it.requiredVal, it.unit, it.specificNotes)
                )
            }

            selectedRequirements.clear()
            selectedRequirements.addAll(draft.selectedRequirements)

            if (draft.customerId.isNotBlank()) {
                serviceViewModel.fetchSalesOrdersByCustomer(draft.customerId, status = "Delivered")
            }
            if (!draft.selectedOrderId.isNullOrBlank()) {
                orderOverviewViewModel.fetchSalesOverview(draft.selectedOrderId)
            }
        }
    }

    // Match customer object once list is loaded
    LaunchedEffect(customerDropdownList, savedDraft) {
        if (savedDraft != null && selectedCustomer == null) {
            selectedCustomer = customerDropdownList.firstOrNull { it.id == savedDraft?.customerId }
        }
    }

    // Match order object once list is loaded
    LaunchedEffect(salesOrdersList, savedDraft) {
        if (savedDraft != null && selectedOrder == null) {
            selectedOrder = salesOrdersList.firstOrNull { it.id == savedDraft?.selectedOrderId }
        }
    }

    // Monitor API action state
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is ServiceActionUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                serviceViewModel.resetActionState()
                serviceViewModel.clearServiceDraft()
                onClose()
            }
            is ServiceActionUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                serviceViewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = "New Service / Alteration Request",
                onClose = {
                    serviceViewModel.clearServiceDraft()
                    serviceViewModel.clearSalesOrders()
                    onClose()
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = tokens.extraPadding)
        ) {
            Spacer(Modifier.height(tokens.extraPadding * 0.5f))

            // Mode Switcher Tab (Pre-Order vs External)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                SettingsTabs(
                    tabs = modeTabs,
                    selectedIndex = selectedModeIndex,
                    onTabSelected = { index ->
                        selectedModeIndex = index
                        if (index == 1) {
                            selectedOrder = null
                            selectedCustomGarmentId = null
                            orderDeliveryDate = ""
                            orderStatusText = "No Order Linked"
                        }
                    },
                    height = 42.dp,
                    cornerRadius = tokens.cardCornerRadius,
                    selectedCornerRadius = tokens.cardCornerRadius * 0.8f
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // 1. CUSTOMER INFORMATION
            FormAccordionSection(
                title = "Customer Information",
                subtitle = "Search customer or create new profile",
                iconPainter = painterResource(R.drawable.ic_person),
                isExpanded = customerInfoExpanded,
                onToggle = { customerInfoExpanded = !customerInfoExpanded }
            ) {
                FormDropdown(
                    label = "Customer Name",
                    isRequired = true,
                    value = selectedCustomer?.fullName?.ifBlank { savedDraft?.customerName ?: "Select Customer" } ?: "Select Customer",
                    expanded = customerDropdownExpanded,
                    onExpandChange = { customerDropdownExpanded = it },
                    options = customerDropdownList.map { it.fullName.orEmpty() },
                    onOptionSelected = { chosenName ->
                        val foundCustomer = customerDropdownList.firstOrNull { it.fullName == chosenName }
                        if (foundCustomer != null) {
                            selectedCustomer = foundCustomer
                            customerId = foundCustomer.customerCode ?: foundCustomer.id
                            customerMobile = foundCustomer.mobileNumber.orEmpty()
                            customerEmail = foundCustomer.email.orEmpty()

                            selectedOrder = null
                            selectedCustomGarmentId = null
                            orderDeliveryDate = ""
                            orderStatusText = "No Order Linked"

                            serviceViewModel.fetchSalesOrdersByCustomer(
                                customerId = foundCustomer.id,
                                status = "Delivered"
                            )
                        }
                    }
                )

                Spacer(Modifier.height(tokens.extraPadding))
                FormLabel(text = "Customer ID")
                FormTextField(
                    value = customerId,
                    onValueChange = {},
                    placeholder = "Auto ID",
                    enabled = false
                )

                Spacer(Modifier.height(tokens.extraPadding))
                FormLabel(text = "Phone Number")
                FormTextField(
                    value = customerMobile,
                    onValueChange = {},
                    placeholder = "+91 ...",
                    enabled = false,
                    keyboardType = KeyboardType.Phone
                )

                Spacer(Modifier.height(tokens.extraPadding))
                FormLabel(text = "Email Address")
                FormTextField(
                    value = customerEmail,
                    onValueChange = {},
                    placeholder = "customer@example.com",
                    enabled = false,
                    keyboardType = KeyboardType.Email
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // 2. SALES ORDER REFERENCE (Pre Order Flow)
            if (selectedModeIndex == 0) {
                FormAccordionSection(
                    title = "Sales Order Reference",
                    subtitle = "Select delivered order & review order details",
                    iconPainter = painterResource(R.drawable.ic_clippad_lines),
                    isExpanded = orderRefExpanded,
                    onToggle = { orderRefExpanded = !orderRefExpanded }
                ) {
                    val isCustomerSelected = selectedCustomer != null || customerId.isNotBlank()
                    val orderCodes = salesOrdersList.mapNotNull { it.orderCode }

                    val dropdownDisplayText = when {
                        !isCustomerSelected -> "Select Customer First"
                        isLoadingOrders -> "Loading Delivered Orders..."
                        selectedOrder != null -> selectedOrder?.orderCode ?: ""
                        !savedDraft?.selectedOrderCode.isNullOrBlank() -> savedDraft?.selectedOrderCode ?: ""
                        else -> "Select Delivered Order"
                    }

                    FormDropdown(
                        label = "Delivered Order ID",
                        isRequired = true,
                        value = dropdownDisplayText,
                        expanded = orderDropdownExpanded,
                        onExpandChange = {
                            if (isCustomerSelected && !isLoadingOrders) {
                                orderDropdownExpanded = it
                            }
                        },
                        enabled = isCustomerSelected && !isLoadingOrders,
                        options = orderCodes,
                        onOptionSelected = { chosenCode ->
                            val foundOrder = salesOrdersList.firstOrNull { it.orderCode == chosenCode }
                            if (foundOrder != null) {
                                selectedOrder = foundOrder
                                orderDeliveryDate = formatDisplayDate(foundOrder.dueDate ?: foundOrder.orderDate)
                                orderStatusText = foundOrder.status ?: "Delivered"
                                selectedCustomGarmentId = null

                                orderOverviewViewModel.fetchSalesOverview(orderId = foundOrder.id)

                                foundOrder.customer?.let { custRef ->
                                    if (customerMobile.isBlank()) customerMobile = custRef.phone.orEmpty()
                                    if (customerEmail.isBlank()) customerEmail = custRef.email.orEmpty()
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Order Delivery Date")
                    FormTextField(
                        value = orderDeliveryDate,
                        onValueChange = {},
                        placeholder = "YYYY-MM-DD",
                        enabled = false
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Status")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(if (selectedOrder != null || !savedDraft?.selectedOrderId.isNullOrBlank()) greenBg else light_grey)
                            .border(1.dp, if (selectedOrder != null || !savedDraft?.selectedOrderId.isNullOrBlank()) greentext.copy(alpha = 0.5f) else grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedOrder != null || !savedDraft?.selectedOrderId.isNullOrBlank()) greentext else mutedText)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = orderStatusText,
                                fontSize = tokens.bodySmall,
                                color = if (selectedOrder != null || !savedDraft?.selectedOrderId.isNullOrBlank()) greentext else TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                FormAccordionSection(
                    title = "Walk-in Garment Flow",
                    subtitle = "Customer brought external garment (No order attached)",
                    iconPainter = painterResource(R.drawable.ic_shopping_bag),
                    isExpanded = orderRefExpanded,
                    onToggle = { orderRefExpanded = !orderRefExpanded }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = yellowBg.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, yellowText.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = yellowText,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "External Customer Garment",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = tokens.bodySmall,
                                    color = TitleColor
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Customer brought an outside garment (e.g. Raymond Coat, Silk Saree). Enter garment title, category, and issue description directly in the section below.",
                                    fontSize = tokens.caption,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // CUSTOM GARMENTS FROM ORDER
            if (selectedModeIndex == 0 && (selectedOrder != null || !savedDraft?.selectedOrderId.isNullOrBlank())) {
                val overviewData = (overviewState as? OrderOverviewState.Success)?.data
                val customGarments: List<OrderOverviewItem> = overviewData?.items.orEmpty()

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding),
                    color = whiteBg,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_dotted_pencil),
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Select Custom Garments from Order",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = tokens.bodySmall,
                                        color = TitleColor
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Pick stitched garments requiring alteration or repair (Materials & Retail items excluded)",
                                        fontSize = tokens.caption,
                                        color = mutedText,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .background(primary_light)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${customGarments.size} custom garments",
                                    fontSize = 11.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        if (overviewState is OrderOverviewState.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
                            }
                        } else if (customGarments.isEmpty()) {
                            Text(
                                text = "No custom stitched garments found in this order",
                                fontSize = tokens.bodySmall,
                                color = mutedText,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            customGarments.forEach { garmentItem ->
                                val isSelected = selectedCustomGarmentId == garmentItem._id
                                val garmentTitle = garmentItem.customGarment?.garmentName?.takeIf { it.isNotBlank() }
                                    ?: garmentItem.categoryName
                                val subDesc = garmentItem.itemDescription.orEmpty()

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCustomGarmentId = if (isSelected) null else garmentItem._id
                                            if (!isSelected) {
                                                garmentDescription = subDesc.ifBlank { garmentTitle }
                                                garmentCategoryName = garmentTitle
                                                garmentPiecesCount = garmentItem.quantity.coerceAtLeast(1)
                                            }
                                        },
                                    color = if (isSelected) primary_light.copy(alpha = 0.3f) else whiteBg,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    border = BorderStroke(1.dp, if (isSelected) Primary else BorderGray)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = garmentTitle,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = tokens.bodySmall,
                                                color = TitleColor
                                            )
                                            if (subDesc.isNotBlank()) {
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = subDesc,
                                                    fontSize = tokens.caption,
                                                    color = TextSecondary
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = "Original Qty: ${garmentItem.quantity}",
                                                fontSize = tokens.caption,
                                                color = mutedText
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .border(
                                                    1.5.dp,
                                                    if (isSelected) Primary else BorderGray,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .background(if (isSelected) Primary else Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = whiteBg,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
            }

            // 3. SERVICE REQUEST DETAILS
            FormAccordionSection(
                title = "Service Request Details",
                subtitle = "Configure category classification, priority level, and promised dates",
                iconPainter = painterResource(R.drawable.ic_clippad_lines),
                isExpanded = serviceDetailsExpanded,
                onToggle = { serviceDetailsExpanded = !serviceDetailsExpanded }
            ) {
                FormDropdown(
                    label = "Service Category",
                    isRequired = true,
                    value = serviceCategory,
                    expanded = serviceCategoryExpanded,
                    onExpandChange = { serviceCategoryExpanded = it },
                    options = listOf("Alteration", "Damage Repair", "Remake"),
                    onOptionSelected = { serviceCategory = it }
                )

                Spacer(Modifier.height(tokens.extraPadding))
                FormDropdown(
                    label = "Priority Level",
                    value = priorityLevel,
                    expanded = priorityExpanded,
                    onExpandChange = { priorityExpanded = it },
                    options = listOf("Low", "Medium", "High", "Urgent"),
                    onOptionSelected = { priorityLevel = it }
                )

                Spacer(Modifier.height(tokens.extraPadding))
                FormLabel(text = "Preferred Service Date")
                DatePickerField(
                    value = preferredServiceDate,
                    onDateSelected = { preferredServiceDate = it }
                )

                Spacer(Modifier.height(tokens.extraPadding))
                FormLabel(text = "Expected Delivery Date", isRequired = true)
                DatePickerField(
                    value = expectedDeliveryDate,
                    onDateSelected = { expectedDeliveryDate = it }
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // 4. GARMENT SPECIFICATIONS & MEASUREMENT DELTAS
            FormAccordionSection(
                title = "Garment Specifications & Measurement Deltas",
                subtitle = "Select alteration area chips to spawn granular measurement deltas",
                iconPainter = painterResource(R.drawable.ic_dotted_pencil),
                isExpanded = garmentSpecsExpanded,
                onToggle = { garmentSpecsExpanded = !garmentSpecsExpanded }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                        .background(primary_light.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GARMENT #1",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Bold,
                        color = TitleColor
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Number of Pieces (pcs):",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(whiteBg)
                                .border(1.dp, BorderGray, RoundedCornerShape(4.dp))
                                .clickable { if (garmentPiecesCount > 1) garmentPiecesCount-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = garmentPiecesCount.toString(),
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(whiteBg)
                                .border(1.dp, BorderGray, RoundedCornerShape(4.dp))
                                .clickable { garmentPiecesCount++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))
                FormLabel(text = "Garment Description / Title", isRequired = true)
                FormTextField(
                    value = garmentDescription,
                    onValueChange = { garmentDescription = it },
                    placeholder = "e.g. Midnight Navy Tuxedo"
                )

                Spacer(Modifier.height(tokens.extraPadding))
                FormLabel(text = "Garment Category Name")
                FormTextField(
                    value = garmentCategoryName,
                    onValueChange = { garmentCategoryName = it },
                    placeholder = "e.g. Bespoke Suit, Formal Trouser, Saree Blouse"
                )

                Spacer(Modifier.height(tokens.extraPadding))
                FormLabel(text = "Specific Issue Description", isRequired = true)
                FormTextArea(
                    value = specificIssueDescription,
                    onValueChange = { specificIssueDescription = it },
                    placeholder = "e.g. Sleeve length needs shortening by 2 inches and waist tightening by 2 inches..."
                )

                Spacer(Modifier.height(tokens.extraPadding))

                Text(
                    text = "Alteration Area",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TitleColor
                )
                Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                // Alteration Area Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allAlterationAreas.forEach { area ->
                        val isSelected = activeAlterationDeltas.any { it.area == area }
                        FilterChipItem(
                            text = area,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    activeAlterationDeltas.removeAll { it.area == area }
                                } else {
                                    activeAlterationDeltas.add(AlterationDeltaState(area = area))
                                }
                            }
                        )
                    }
                }

                // Active Alteration Specifications & Measurement Deltas Cards
                if (activeAlterationDeltas.isNotEmpty()) {
                    Spacer(Modifier.height(tokens.extraPadding))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = whiteBg,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(14.dp)
                                            .background(Primary, RoundedCornerShape(2.dp))
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Active Alteration Specifications (${activeAlterationDeltas.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = tokens.bodySmall,
                                        color = TitleColor
                                    )
                                }
                                Text(
                                    text = "${activeAlterationDeltas.size} areas defined",
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                            }

                            HorizontalDivider(
                                color = BorderGray,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )

                            activeAlterationDeltas.forEachIndexed { index, delta ->
                                AlterationDeltaItemCard(
                                    delta = delta,
                                    onDelete = { activeAlterationDeltas.removeAt(index) },
                                    tokens = tokens
                                )
                                if (index < activeAlterationDeltas.lastIndex) {
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                Text(
                    text = "Special Requirements / Workmanship",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TitleColor
                )
                Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allRequirements.forEach { req ->
                        val isSelected = selectedRequirements.contains(req)
                        FilterChipItem(
                            text = req,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) selectedRequirements.remove(req)
                                else selectedRequirements.add(req)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                // Is Chargeable Service Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Is Chargeable Service?",
                        fontSize = tokens.bodySmall,
                        color = TextPrimary
                    )
                    MiniSwitch(
                        checked = isChargeableService,
                        onCheckedChange = { isChargeableService = it }
                    )
                }

                // Rate per piece, Is Taxable, Tax Group & Line Total
                if (isChargeableService) {
                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    FormLabel(text = "Rate per piece (${currencySymbol()})")
                    FormTextField(
                        value = ratePerPiece,
                        onValueChange = { ratePerPiece = it },
                        placeholder = "0",
                        keyboardType = KeyboardType.Decimal
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Is Taxable?",
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        )
                        MiniSwitch(
                            checked = isTaxable,
                            onCheckedChange = { isTaxable = it }
                        )
                    }

                    if (isTaxable) {
                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                        val taxGroupOptions = taxGroups.map { "${it.name} (${it.totalRate}%)" }
                        val selectedTaxGroupName = selectedTaxGroup?.let { "${it.name} (${it.totalRate}%)" } ?: "Select an option"

                        FormDropdown(
                            label = "Tax Group",
                            value = selectedTaxGroupName,
                            expanded = taxGroupDropdownExpanded,
                            onExpandChange = { taxGroupDropdownExpanded = it },
                            options = taxGroupOptions,
                            onOptionSelected = { chosenOption ->
                                selectedTaxGroup = taxGroups.firstOrNull { "${it.name} (${it.totalRate}%)" == chosenOption }
                            }
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    // Line Total display
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Line Total: ${currencySymbol()} ${rawLineTotal.toInt()} (${currencySymbol()}$ratePerPiece × $garmentPiecesCount pcs)",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // 5. UPLOAD EVIDENCE
            FormAccordionSection(
                title = "Upload Evidence",
                subtitle = "Attach defect photos or fitting trial slips",
                iconPainter = painterResource(R.drawable.ic_upload_cloud),
                isExpanded = uploadEvidenceExpanded,
                onToggle = { uploadEvidenceExpanded = !uploadEvidenceExpanded }
            ) {
                ImageUploadSection(
                    isImage = false,
                    selectedImages = selectedAttachments,
                    onBrowseClick = { filePickerLauncher.launch("image/*") },
                    onCameraClick = null,
                    onRemoveImage = { uri ->
                        selectedAttachments = selectedAttachments - uri
                    },
                    browseText = "Drop defect photos or fitting slips here",
                    previewHeaderTitle = "ATTACHED EVIDENCE"
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // 6. INTERNAL STAFF NOTES
            FormAccordionSection(
                title = "Internal Staff Notes",
                subtitle = "Private instructions for cutting master and alteration tailor",
                iconPainter = painterResource(R.drawable.ic_lock_2),
                isExpanded = internalNotesExpanded,
                onToggle = { internalNotesExpanded = !internalNotesExpanded }
            ) {
                FormTextArea(
                    value = internalStaffNotes,
                    onValueChange = { internalStaffNotes = it },
                    placeholder = "Document any internal observations, priority flags, or workshop directives..."
                )
            }

            Spacer(Modifier.height(tokens.extraPadding))

            // 7. INLINE FOOTER SUMMARY & SUBMIT ACTIONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        serviceViewModel.clearServiceDraft()
                        serviceViewModel.clearSalesOrders()
                        onCancel()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Cancel", color = TextPrimary, fontSize = tokens.bodySmall)
                }

                AppButton(
                    text = "Review Request",
                    modifier = Modifier.weight(1.5f),
                    onClick = {
                        if (selectedCustomer == null && customerId.isBlank()) {
                            Toast.makeText(context, "Please select a customer first", Toast.LENGTH_SHORT).show()
                            return@AppButton
                        }
                        if (selectedModeIndex == 0 && selectedOrder == null && savedDraft?.selectedOrderId.isNullOrBlank()) {
                            Toast.makeText(context, "Please select a delivered order", Toast.LENGTH_SHORT).show()
                            return@AppButton
                        }

                        // Save current form draft into ViewModel
                        serviceViewModel.saveServiceDraft(
                            ServiceRequestDraft(
                                modeIndex = selectedModeIndex,
                                customerId = selectedCustomer?.id ?: customerId,
                                customerName = selectedCustomer?.fullName ?: customerId,
                                customerCode = selectedCustomer?.customerCode.orEmpty(),
                                customerMobile = customerMobile,
                                customerEmail = customerEmail,
                                selectedOrderId = selectedOrder?.id ?: savedDraft?.selectedOrderId,
                                selectedOrderCode = selectedOrder?.orderCode ?: savedDraft?.selectedOrderCode,
                                orderDeliveryDate = orderDeliveryDate,
                                selectedCustomGarmentId = selectedCustomGarmentId,
                                serviceCategory = serviceCategory,
                                priorityLevel = priorityLevel,
                                preferredServiceDate = preferredServiceDate,
                                expectedDeliveryDate = expectedDeliveryDate,
                                garmentPiecesCount = garmentPiecesCount,
                                garmentDescription = garmentDescription,
                                garmentCategoryName = garmentCategoryName,
                                specificIssueDescription = specificIssueDescription,
                                activeAlterationDeltas = activeAlterationDeltas.map {
                                    AlterationDeltaDraft(
                                        it.area,
                                        it.action,
                                        it.currentVal,
                                        it.requiredVal,
                                        it.unit,
                                        it.specificNotes
                                    )
                                },
                                selectedRequirements = selectedRequirements.toList(),
                                isChargeableService = isChargeableService,
                                ratePerPiece = ratePerPiece,
                                isTaxable = isTaxable,
                                selectedTaxGroupId = selectedTaxGroup?.id,
                                selectedTaxGroupName = selectedTaxGroup?.name,
                                selectedAttachments = selectedAttachments,
                                internalStaffNotes = internalStaffNotes
                            )
                        )

                        // Navigate to Review Screen
                        onNavigateToReview()
                    }
                )
            }

            Spacer(Modifier.height(tokens.extraPadding))
        }
    }
}

// ─────────────────────────────────────────────
// Alteration Delta Card Component
// ─────────────────────────────────────────────

@Composable
private fun AlterationDeltaItemCard(
    delta: AlterationDeltaState,
    onDelete: () -> Unit,
    tokens: AppDesignTokens
) {
    var actionExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    var actionState by remember(delta.action) { mutableStateOf(delta.action) }
    var currentValState by remember(delta.currentVal) { mutableStateOf(delta.currentVal) }
    var requiredValState by remember(delta.requiredVal) { mutableStateOf(delta.requiredVal) }
    var unitState by remember(delta.unit) { mutableStateOf(delta.unit) }
    var notesState by remember(delta.specificNotes) { mutableStateOf(delta.specificNotes) }

    val cNum = currentValState.toDoubleOrNull() ?: 0.0
    val rNum = requiredValState.toDoubleOrNull() ?: 0.0
    val diff = rNum - cNum
    val diffLabel = if (cNum != 0.0 && rNum != 0.0) {
        val sign = if (diff > 0) "+$diff" else "$diff"
        val fullUnit = if (unitState == "in") "inch" else unitState
        " ($sign $fullUnit)"
    } else ""

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${delta.area} Alteration",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FormDropdown(
                        value = actionState,
                        expanded = actionExpanded,
                        onExpandChange = { actionExpanded = it },
                        options = listOf("Shorten", "Lengthen", "Tighten", "Loosen", "Resize"),
                        onOptionSelected = {
                            actionState = it
                            delta.action = it
                        },
                        modifier = Modifier.width(115.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "Remove",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Current", fontSize = tokens.caption, color = mutedText)
                    Spacer(Modifier.height(4.dp))
                    FormTextField(
                        value = currentValState,
                        onValueChange = {
                            currentValState = it
                            delta.currentVal = it
                        },
                        placeholder = "24",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Required", fontSize = tokens.caption, color = mutedText)
                    Spacer(Modifier.height(4.dp))
                    FormTextField(
                        value = requiredValState,
                        onValueChange = {
                            requiredValState = it
                            delta.requiredVal = it
                        },
                        placeholder = "22",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                Column(modifier = Modifier.weight(0.9f)) {
                    Text("Unit", fontSize = tokens.caption, color = mutedText)
                    Spacer(Modifier.height(4.dp))
                    FormDropdown(
                        value = unitState,
                        expanded = unitExpanded,
                        onExpandChange = { unitExpanded = it },
                        options = listOf("in", "cm"),
                        onOptionSelected = {
                            unitState = it
                            delta.unit = it
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            FormTextField(
                value = notesState,
                onValueChange = {
                    notesState = it
                    delta.specificNotes = it
                },
                placeholder = "Specific notes (e.g. from cuffs)"
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Action:",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "$actionState$diffLabel",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Reusable Layout Components
// ─────────────────────────────────────────────

@Composable
private fun FormAccordionSection(
    title: String,
    subtitle: String? = null,
    iconPainter: Painter?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = whiteBg,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (iconPainter != null) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                    }
                    Column {
                        Text(
                            text = title,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TitleColor
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(tokens.iconSize * 1.1f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = tokens.extraPadding),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(if (isSelected) primary_light else whiteBg)
            .border(
                1.dp,
                if (isSelected) Primary else BorderGray,
                RoundedCornerShape(tokens.cardCornerRadius)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            fontSize = tokens.caption,
            color = if (isSelected) Primary else TextPrimary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun formatDisplayDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputFormat.parse(rawDate)
        if (date != null) outputFormat.format(date) else rawDate.take(10)
    } catch (_: Exception) {
        rawDate.take(10)
    }
}

private fun String?.orDash(): String = if (this.isNullOrBlank()) "-" else this

@Composable
fun ReviewServiceRequestScreen(
    onBackToEdit: () -> Unit,
    onSuccessSubmitted: () -> Unit,
    serviceViewModel: ServicesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current
    val draft by serviceViewModel.serviceDraft.collectAsStateWithLifecycle()
    val actionState by serviceViewModel.actionState.collectAsStateWithLifecycle()

    if (draft == null) {
        onBackToEdit()
        return
    }

    val currentDraft = draft!!

    Scaffold(
        topBar = {
            TitleBar(title = "Review Service Request", onClose = onBackToEdit)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding)
        ) {
            // ─────────────────────────────────────────────
            // 1. CUSTOMER PROFILE CARD
            // ─────────────────────────────────────────────
            ReviewSectionCard {
                Text(
                    text = "CUSTOMER PROFILE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = mutedText,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(12.dp))

                ReviewKeyValueRow("Name:", currentDraft.customerName.ifBlank { "—" })
                ReviewKeyValueRow("Code:", currentDraft.customerCode.ifBlank { "—" })
                ReviewKeyValueRow("Mobile:", currentDraft.customerMobile.ifBlank { "—" })
                ReviewKeyValueRow("Email:", currentDraft.customerEmail.ifBlank { "—" })
            }

            Spacer(Modifier.height(14.dp))

            // ─────────────────────────────────────────────
            // 2. ORDER & TIMELINE CARD
            // ─────────────────────────────────────────────
            ReviewSectionCard {
                Text(
                    text = "ORDER & TIMELINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = mutedText,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(12.dp))

                val sourceText = if (currentDraft.modeIndex == 0 && !currentDraft.selectedOrderCode.isNullOrBlank()) {
                    "In-House Order (${currentDraft.selectedOrderCode})"
                } else {
                    "External Customer Garment"
                }

                ReviewKeyValueRow("Garment Source:", sourceText)
                ReviewKeyValueRow("Service Category:", currentDraft.serviceCategory)
                ReviewKeyValueRow("Priority:", currentDraft.priorityLevel)
                ReviewKeyValueRow(
                    label = "Expected Delivery:",
                    value = currentDraft.expectedDeliveryDate.ifBlank { currentDraft.preferredServiceDate },
                    valueColor = Primary,
                    isBold = true
                )
            }

            Spacer(Modifier.height(14.dp))

            // ─────────────────────────────────────────────
            // 3. GARMENT ITEMS TO PROCESS CARD
            // ─────────────────────────────────────────────
            ReviewSectionCard {
                Text(
                    text = "Garment Items to Process (1)",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TitleColor
                )
                Spacer(Modifier.height(12.dp))

                // Header pill for garment item & warranty status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(light_grey.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val garmentTitle = "${currentDraft.garmentDescription} (${currentDraft.garmentCategoryName}) - Qty: ${currentDraft.garmentPiecesCount}"
                    Text(
                        text = garmentTitle,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TitleColor
                    )
                    Text(
                        text = if (currentDraft.isChargeableService) "Chargeable Service" else "Free Warranty Service",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentDraft.isChargeableService) TitleColor else greentext
                    )
                }

                if (currentDraft.specificIssueDescription.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\"${currentDraft.specificIssueDescription}\"",
                        fontSize = tokens.caption,
                        color = mutedText,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Render each alteration delta card
                currentDraft.activeAlterationDeltas.forEach { delta ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = whiteBg,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = delta.area,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TitleColor
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Current: ${delta.currentVal} ${delta.unit} → Target: ${delta.requiredVal} ${delta.unit}",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                            Spacer(Modifier.height(4.dp))
                            val diff = (delta.requiredVal.toDoubleOrNull() ?: 0.0) - (delta.currentVal.toDoubleOrNull() ?: 0.0)
                            val diffText = if (diff != 0.0) " (${if (diff > 0) "+$diff" else "$diff"} ${delta.unit})" else ""
                            Text(
                                text = "Action: ${delta.action}$diffText",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ─────────────────────────────────────────────
            // 4. PHOTO EVIDENCE / ATTACHED SLIPS CARD
            // ─────────────────────────────────────────────
            ReviewSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_upload_cloud),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Photo Evidence / Attached Slips (${currentDraft.selectedAttachments.size})",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TitleColor
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (currentDraft.selectedAttachments.isEmpty()) {
                    Text(
                        text = "No attachments uploaded",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentDraft.selectedAttachments.forEach { uri ->
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(uri).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ─────────────────────────────────────────────
            // 5. WARRANTY / CHARGES BANNER
            // ─────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (!currentDraft.isChargeableService) greenBg else primary_light.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (!currentDraft.isChargeableService) greentext.copy(alpha = 0.4f) else Primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (!currentDraft.isChargeableService) greentext else Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (!currentDraft.isChargeableService) {
                            "Free Warranty Service (No billing or invoice generated)"
                        } else {
                            "Chargeable Service (${currencySymbol()}${currentDraft.ratePerPiece} per pc | Total: ${currencySymbol()}${(currentDraft.ratePerPiece.toDoubleOrNull() ?: 0.0) * currentDraft.garmentPiecesCount})"
                        },
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = if (!currentDraft.isChargeableService) greentext else Primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─────────────────────────────────────────────
            // 6. ACTION BUTTONS: Back to Edit & Create
            // ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onBackToEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Back to Edit", color = TextPrimary, fontSize = tokens.bodySmall)
                }

                // ✅ AFTER: Converts the saved draft to the API payload and calls serviceViewModel.createServiceRequest
                Button(
                    onClick = {
                        val parsedCharge = if (currentDraft.isChargeableService) {
                            currentDraft.ratePerPiece.toDoubleOrNull() ?: 0.0
                        } else 0.0

                        // ✅ AFTER (Fixed):
                        val mappedAlterations = currentDraft.activeAlterationDeltas.map { d ->
                            val currentD = d.currentVal.toDoubleOrNull() ?: 0.0
                            val requiredD = d.requiredVal.toDoubleOrNull() ?: 0.0
                            val deltaVal = kotlin.math.abs(requiredD - currentD)

                            ServiceAlterationAction(
                                targetArea = d.area,
                                action = d.action,
                                currentValue = currentD,
                                targetValue = requiredD,
                                value = if (deltaVal > 0) deltaVal else requiredD,
                                unit = d.unit,
                                notes = d.specificNotes.ifBlank { currentDraft.specificIssueDescription }
                            )
                        }

                        val payload = CreateServiceRequestPayload(
                            customerId = currentDraft.customerId,
                            primaryCategory = currentDraft.serviceCategory,
                            originalSalesOrderId = if (currentDraft.modeIndex == 0) currentDraft.selectedOrderId else null,
                            priority = currentDraft.priorityLevel,
                            deliveryDate = currentDraft.expectedDeliveryDate.ifBlank { currentDraft.preferredServiceDate },
                            advanceAmountPaid = 0.0,
                            attachments = currentDraft.selectedAttachments.map { it.toString() },
                            items = listOf(
                                CreateServiceRequestItemPayload(
                                    category = currentDraft.serviceCategory,
                                    garmentDescription = currentDraft.garmentDescription,
                                    quantity = currentDraft.garmentPiecesCount,
                                    serviceCharge = parsedCharge,
                                    issueSummary = currentDraft.specificIssueDescription,
                                    internalNotes = currentDraft.internalStaffNotes,
                                    alterations = mappedAlterations
                                )
                            )
                        )

                        // Execute API call through ViewModel
                        serviceViewModel.createServiceRequest(payload) {
                            serviceViewModel.clearServiceDraft()
                            onSuccessSubmitted()
                        }
                    },
                    modifier = Modifier.weight(1.4f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = actionState !is ServiceActionUiState.Loading
                ) {
                    if (actionState is ServiceActionUiState.Loading) {
                        CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Create Service Request", color = whiteBg, fontSize = tokens.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))
        }
    }
}

// ─────────────────────────────────────────────
// Reusable Review Layout Components
// ─────────────────────────────────────────────

@Composable
private fun ReviewSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = whiteBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            content = content
        )
    }
}

@Composable
private fun ReviewKeyValueRow(
    label: String,
    value: String,
    valueColor: Color = TitleColor,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = mutedText)
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}