@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER","unusedVariable","AssignedValueIsNeverRead",
    "VariableNeverRead"
)

package com.cuso.tailor.view.home.finance.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.TaxRegistrationItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.SalesViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

// ===========================================================
// DATA MODELS FOR GST SETTINGS
// ===========================================================

data class GstRegistrationItem(
    val id: String,
    val title: String,
    val group: String,
    val gstin: String,
    val type: String,
    val state: String,
    val status: String,
    val effectiveDate: String
)

// ===========================================================
// REUSABLE SECTION HEADER WITH 20.DP SPACING
// ===========================================================

@Composable
fun FormSectionHeader(title: String) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = tokens.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = dividerColor, thickness = 2.dp)
    }
}

// ===========================================================
// SCREEN 1: GST SETTINGS OVERVIEW SCREEN
// ===========================================================

@Composable
fun GstSettingsOverviewScreen(
    viewModel: SettingsViewModel,
    onClose: () -> Unit = {},
    onAddGst: () -> Unit = {},
    onEditGst: (TaxRegistrationItemDto) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val listState = rememberLazyListState()

    // ── Observe State from ViewModel ──
    val taxRegistrations by viewModel.taxRegistrations.collectAsState()
    val isLoading by viewModel.isLoadingTaxRegistrations.collectAsState()
    val isLoadingMore by viewModel.isLoadingMoreTaxRegistrations.collectAsState()
    val canLoadMore by viewModel.canLoadMoreTaxRegistrations.collectAsState()
    val errorMessage by viewModel.taxRegistrationsError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // ── Fetch Initial Data on Launch ──
    LaunchedEffect(Unit) {
        viewModel.fetchTaxRegistrations(page = 1)
    }

    // ── Debounced Search Query ──
    LaunchedEffect(searchQuery) {
        delay(400)
        viewModel.fetchTaxRegistrations(page = 1, search = searchQuery.takeIf { it.isNotBlank() })
    }

    // ── Infinite Scroll Detection ──
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && canLoadMore && !isLoading && !isLoadingMore) {
            viewModel.loadMoreTaxRegistrations()
        }
    }

    // ── Dynamic Metric Calculations ──
    val activeCount = remember(taxRegistrations) {
        taxRegistrations.count { it.status.equals("Active", ignoreCase = true) }
    }
    val inactiveCount = remember(taxRegistrations) {
        taxRegistrations.size - activeCount
    }
    val groupCount = remember(taxRegistrations) {
        taxRegistrations.mapNotNull { it.registrationType?.takeIf { type -> type.isNotBlank() } }.distinct().size
    }
    val stateCount = remember(taxRegistrations) {
        taxRegistrations.mapNotNull { it.subdivisionName?.takeIf { state -> state.isNotBlank() } }.distinct().size
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        FabScaffold(
            modifier = Modifier.fillMaxSize(),
            fab = FabConfig(
                label = "Add GST",
                icon = Icons.Default.Add,
                onClick = onAddGst,
                bottomPadding = 50.dp
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(title = "GST Settings", onClose = onClose)

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search Bar
                    item {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search GSTIN, state, or name...",
                            borderColor = sectionBorder,
                            textSecondaryColor = close_color,
                            height = tokens.fieldHeight,
                            onFilterClick = {}
                        )
                    }

                    // Metric Cards
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetricCard(
                                    title = "Active Registrations",
                                    count = activeCount.toString(),
                                    indicatorText = if (inactiveCount == 0) "All Compliant" else "$inactiveCount Inactive",
                                    indicatorColor = greentext,
                                    indicatorIcon = Icons.Default.ArrowDropUp,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricCard(
                                    title = "Inactive Registrations",
                                    count = inactiveCount.toString(),
                                    indicatorText = "Deactivated",
                                    indicatorColor = redText,
                                    indicatorIcon = Icons.Default.ArrowDropDown,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MetricCard(
                                    title = "Primary Settings",
                                    count = groupCount.toString(),
                                    indicatorText = "Categories",
                                    indicatorColor = close_color,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricCard(
                                    title = "Jurisdictions Covered",
                                    count = stateCount.toString(),
                                    indicatorText = "States & UTs",
                                    indicatorColor = close_color,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Initial Loading Indicator
                    if (isLoading && taxRegistrations.isEmpty()) {
                        item {
                            ListSkeleton()
                        }
                    }

                    // Error Message Card
                    if (errorMessage != null && taxRegistrations.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = tokens.screenPadding),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                color = redText.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, redText.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    fontSize = tokens.bodySmall,
                                    color = redText,
                                    modifier = Modifier.padding(14.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Empty State
                    if (!isLoading && taxRegistrations.isEmpty() && errorMessage == null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No GST registrations match your search" else "No GST registrations found",
                                    fontSize = tokens.bodyMedium,
                                    color = close_color
                                )
                            }
                        }
                    }

                    // Dynamic List Items from API
                    items(
                        items = taxRegistrations,
                        key = { it.id }
                    ) { item ->
                        GstRegistrationCard(
                            item = item,
                            onEditClick = { onEditGst(item) }
                        )
                    }

                    // Infinite Scroll Loading Footer
                    if (isLoadingMore) {
                        item {
                            ThreeDotLoading()
                        }
                    }
                }
            }
        }
    }
}


// ── Metric Cell Helper Component ──
@Composable
private fun MetricSummaryCell(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    indicatorText: String,
    indicatorColor: Color,
    showDot: Boolean = true
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = title, fontSize = tokens.label, color = close_color)
        Text(text = count, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
            Text(text = indicatorText, fontSize = tokens.caption, color = indicatorColor, fontWeight = FontWeight.Medium)
        }
    }
}
// ── Metric Card Component ──
// Standalone card with its own background, border and rounded corners
// ── Metric Card Component ──
// Light bordered card: muted title, large value, and a small indicator line below
@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    indicatorText: String,
    indicatorColor: Color,
    indicatorIcon: ImageVector? = null
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                color = close_color,
                maxLines = 1
            )
            Text(
                text = count,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (indicatorIcon != null) {
                    Icon(
                        imageVector = indicatorIcon,
                        contentDescription = null,
                        tint = indicatorColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = indicatorText,
                    fontSize = tokens.caption,
                    color = indicatorColor,
                    maxLines = 1
                )
            }
        }
    }
}
// ── GST Card Component ──
@Composable
fun GstRegistrationCard(
    item: TaxRegistrationItemDto,
    onEditClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isActive = item.status.equals("Active", ignoreCase = true)

    // Format date string from "2026-10-12T..." or validFrom
    val effectiveDate = remember(item.validFrom, item.createdAt) {
        val rawDate = item.validFrom ?: item.createdAt
        if (!rawDate.isNullOrBlank() && rawDate.length >= 10) {
            val parts = rawDate.take(10).split("-")
            if (parts.size == 3) {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val monthIndex = (parts[1].toIntOrNull() ?: 1) - 1
                val monthName = months.getOrElse(monthIndex) { parts[1] }
                "${parts[2]} $monthName ${parts[0]}"
            } else rawDate.take(10)
        } else {
            "01 Apr 2024"
        }
    }

    DataCard(
        item = item,
        onClick = { onEditClick() },
        title = "${item.subdivisionName ?: item.countryCode ?: "State"} GST Registration",
        subtitle = item.registeredLegalName?.takeIf { it.isNotBlank() } ?: "GST Group",
        titleFontWeight = FontWeight.Medium,
        actions = listOf(
            MenuAction(
                label = "Edit",
                icon = Icons.Default.MoreVert,
                onClick = onEditClick
            )
        ),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // GSTIN Box Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary_background)
                        .border(1.dp, light_blue_border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "GSTIN: ${item.registrationNumber ?: "N/A"}",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                }

                // Type & State Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Type:",
                        fontSize = tokens.caption,
                        color = close_color
                    )
                    Text(
                        text = item.taxScheme ?: "Regular",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                    Text(
                        text = "•",
                        fontSize = tokens.caption,
                        color = close_color
                    )
                    Text(
                        text = "State:",
                        fontSize = tokens.caption,
                        color = close_color
                    )
                    Text(
                        text = item.subdivisionName ?: item.countryCode ?: "N/A",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // Bottom Row: Status Badge on Left & Effective Date on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge with dot
                    StatusBadge(
                        text = item.status,
                        bgColor = if (isActive) greenBg else modelGray,
                        textColor = if (isActive) greentext else close_color,
                        cornerRadius = 20.dp
                    )

                    // Effective Date
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Effective:",
                            fontSize = tokens.bodySmall,
                            color = close_color
                        )
                        Text(
                            text = effectiveDate,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    )
}
// ===========================================================
// SCREEN 2: ADD/EDIT GST SETTINGS FORM
// ===========================================================

@Composable
fun AddGstSettingsScreen(
    settingsViewModel: SettingsViewModel,
    branchViewModel: BranchViewModel,
    inventoryViewModel: InventoryViewModel,
    salesViewModel: SalesViewModel,
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // ── 1. Fetch Dynamic Data on Screen Launch ──
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        inventoryViewModel.fetchTaxGroups()
        salesViewModel.fetchStaff()
    }

    // ── 2. Observe ViewModel States ──
    val selectedTaxItem by settingsViewModel.selectedTaxRegistration.collectAsState()
    val branchUiState by branchViewModel.uiState.collectAsState()
    val taxGroups by inventoryViewModel.taxGroups.collectAsState()
    val staffList by salesViewModel.staffList.collectAsState()

    // ── 3. Derived Dropdown Options from APIs ──
    val branches = remember(branchUiState) {
        if (branchUiState is BranchUiState.Success) {
            (branchUiState as BranchUiState.Success).branches
        } else emptyList()
    }

    val branchOptions = remember(branches) {
        branches.map { "${it.name} (${it.address.city?.ifBlank { "" }})" }
    }

    val taxGroupOptions = remember(taxGroups) {
        taxGroups.map { "${it.name} (${it.totalRate}%)" }
    }

    val authUserOptions = remember(staffList) {
        staffList.map { staff ->
            val name = "${staff.firstName} ${staff.lastName}".trim()
            val memberTag = if (staff.memberId.isNotBlank()) " (${staff.memberId})" else ""
            if (name.isNotBlank()) "$name$memberTag" else staff.memberId.ifBlank { "" }
        }
    }

    // ── Tab Items for SettingsTabs Component ──
    val tabItems = remember {
        listOf(
            TabItem(label = "Tax Registration"),
            TabItem(label = "Tax Defaults & Regime"),
            TabItem(label = "Filing & Invoice Preferences"),
            TabItem(label = "Compliance Reminders")
        )
    }

    // =======================================================
    // FORM FORM STATES
    // =======================================================

    // ── Tab 1: Tax Registration ──
    var registrationNumber by remember { mutableStateOf("") }
    var registrationType by remember { mutableStateOf("") }
    var isRegTypeExpanded by remember { mutableStateOf(false) }
    val registrationTypes = listOf("GSTIN", "VAT", "EIN", "TRN", "ABN", "PAN")

    var status by remember { mutableStateOf("") }
    var isStatusExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Active", "Draft", "Inactive")

    var registeredLegalName by remember { mutableStateOf("") }

    var branchJurisdiction by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var isBranchExpanded by remember { mutableStateOf(false) }

    var countryCode by remember { mutableStateOf("") }
    var subdivisionName by remember { mutableStateOf("") }
    var subdivisionCode by remember { mutableStateOf("") }
    var registrationDate by remember { mutableStateOf("") }
    var registeredAddress by remember { mutableStateOf("") }

    // ── Tab 2: Tax Defaults & Regime ──
    var taxRegime by remember { mutableStateOf("") }
    var isTaxRegimeExpanded by remember { mutableStateOf(false) }
    val taxRegimeList = listOf("GST (Goods & Services Tax)", "VAT (Value Added Tax)", "Sales Tax", "Other")

    var taxScheme by remember { mutableStateOf("") }
    var isTaxSchemeExpanded by remember { mutableStateOf(false) }
    val taxSchemeList = listOf("Regular Scheme", "Composition Scheme", "Flat Rate Scheme", "Exempt", "Other")

    var defaultIntraStateTaxGroup by remember { mutableStateOf("") }
    var selectedIntraStateTaxGroupId by remember { mutableStateOf<String?>(null) }
    var isIntraStateExpanded by remember { mutableStateOf(false) }

    var defaultInterStateTaxGroup by remember { mutableStateOf("") }
    var selectedInterStateTaxGroupId by remember { mutableStateOf<String?>(null) }
    var isInterStateExpanded by remember { mutableStateOf(false) }

    var isPrimaryRegistration by remember { mutableStateOf(false) }
    var isGroupRegistration by remember { mutableStateOf(false) }

    // ── Tab 3: Filing & Invoice Preferences ──
    var filingAccountingMethod by remember { mutableStateOf("") }
    var isFilingMethodExpanded by remember { mutableStateOf(false) }
    val filingMethods = listOf("Accrual Basis", "Cash Basis")

    var filingFrequency by remember { mutableStateOf("") }
    var isFilingFrequencyExpanded by remember { mutableStateOf(false) }
    val filingFrequencies = listOf("Monthly", "Quarterly", "Semi Annually", "Annually")

    var taxRoundingRule by remember { mutableStateOf("") }
    var isTaxRoundingExpanded by remember { mutableStateOf(false) }
    val taxRoundingRules = listOf("Round Half Up (Standard)", "Round Half Down", "Round Down", "Round Up")

    var showTaxBreakdownOnInvoice by remember { mutableStateOf(true) }
    var showHsnSacOnInvoice by remember { mutableStateOf(true) }
    var showPlaceOfSupplyOnInvoice by remember { mutableStateOf(true) }

    // ── Tab 4: Compliance Reminders ──
    var reminderLeadDays by remember { mutableStateOf("") }
    var taxAgentNumber by remember { mutableStateOf("") }

    var authorizedFilingOfficer by remember { mutableStateOf("") }
    var selectedStaffId by remember { mutableStateOf<String?>(null) }
    var isOfficerExpanded by remember { mutableStateOf(false) }

    var sendEmailReminders by remember { mutableStateOf(true) }
    var sendValidationAlerts by remember { mutableStateOf(true) }

    // ── PRE-FILL DATA WHEN EDIT API DATA COMES ──
    LaunchedEffect(selectedTaxItem) {
        selectedTaxItem?.let { item ->
            // Tab 1
            registrationNumber = item.registrationNumber.orEmpty()
            registrationType = item.registrationType.orEmpty()
            status = item.status
            registeredLegalName = item.registeredLegalName.orEmpty()
            countryCode = item.countryCode.orEmpty()
            subdivisionName = item.subdivisionName.orEmpty()
            subdivisionCode = item.subdivisionCode.orEmpty()
            registrationDate = item.validFrom?.take(10).orEmpty()
            registeredAddress = item.registeredAddress.orEmpty()

            selectedBranchId = item.branchId
            branches.find { it.id == item.branchId }?.let { b ->
                branchJurisdiction = "${b.name} (${b.address.city?.ifBlank { "" }})"
            }

            // Tab 2
            taxRegime = item.taxRegime.orEmpty()
            taxScheme = item.taxScheme.orEmpty()
            isPrimaryRegistration = item.isPrimary
            isGroupRegistration = item.isGroupRegistration

            selectedIntraStateTaxGroupId = item.defaultIntraStateTaxGroupId
            taxGroups.find { it.id == item.defaultIntraStateTaxGroupId }?.let { tg ->
                defaultIntraStateTaxGroup = "${tg.name} (${tg.totalRate}%)"
            }

            selectedInterStateTaxGroupId = item.defaultInterStateTaxGroupId
            taxGroups.find { it.id == item.defaultInterStateTaxGroupId }?.let { tg ->
                defaultInterStateTaxGroup = "${tg.name} (${tg.totalRate}%)"
            }

            // Tab 3
            filingAccountingMethod = if (item.filingMethod?.contains("Accrual", ignoreCase = true) == true) "Accrual Basis" else "Cash Basis"
            filingFrequency = item.filingFrequency.orEmpty()
            taxRoundingRule = if (item.taxRoundingRule?.contains("Half_Up", ignoreCase = true) == true) "Round Half Up (Standard)" else item.taxRoundingRule.orEmpty()
            showTaxBreakdownOnInvoice = item.showTaxBreakdownOnInvoice
            showHsnSacOnInvoice = item.showHsnSacOnInvoice
            showPlaceOfSupplyOnInvoice = item.showPlaceOfSupplyOnInvoice

            // Tab 4
            reminderLeadDays = item.reminderDaysBeforeDeadline.toString()
            taxAgentNumber = item.taxAgentNumber.orEmpty()
            sendEmailReminders = item.sendEmailReminder
            sendValidationAlerts = item.notifyOnValidator

            val firstUserId = item.authorizedFilingUsers.firstOrNull()
            if (!firstUserId.isNullOrBlank()) {
                selectedStaffId = firstUserId
                staffList.find { it.id == firstUserId }?.let { staff ->
                    val name = "${staff.firstName} ${staff.lastName}".trim()
                    val tag = if (staff.memberId.isNotBlank()) " (${staff.memberId})" else ""
                    authorizedFilingOfficer = if (name.isNotBlank()) "$name$tag" else staff.memberId
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = if (selectedTaxItem != null) "Edit GST Settings" else "GST Settings",
                onClose = {
                    settingsViewModel.clearSelectedTaxRegistration()
                    onClose()
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(top = 10.dp, bottom = tokens.buttonHeight * 2),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Reusable SettingsTabs Component ──
                item {
                    SettingsTabs(
                        tabs = tabItems,
                        selectedIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ==========================================
                // TAB 0: TAX REGISTRATION
                // ==========================================
                if (selectedTabIndex == 0) {
                    item {
                        FormLabel("Registration Number (GSTIN / VAT ID)", isRequired = true)
                        FormTextField(
                            value = registrationNumber,
                            onValueChange = { registrationNumber = it.uppercase() },
                            placeholder = "E.G. 29ABCDE1234F1Z5"
                        )
                    }

                    item {
                        FormLabel("Registration Type")
                        FormDropdown(
                            value = registrationType,
                            expanded = isRegTypeExpanded,
                            onExpandChange = { isRegTypeExpanded = it },
                            options = registrationTypes,
                            onOptionSelected = { registrationType = it }
                        )
                    }

                    item {
                        FormLabel("Status")
                        FormDropdown(
                            value = status,
                            expanded = isStatusExpanded,
                            onExpandChange = { isStatusExpanded = it },
                            options = statusOptions,
                            onOptionSelected = { status = it }
                        )
                    }

                    item {
                        FormLabel("Registered Legal Name")
                        FormTextField(
                            value = registeredLegalName,
                            onValueChange = { registeredLegalName = it },
                            placeholder = "Legal Trade Entity Name"
                        )
                    }

                    item {
                        FormLabel("Branch Jurisdiction")
                        FormDropdown(
                            value = branchJurisdiction.ifBlank { "Select Branch" },
                            expanded = isBranchExpanded,
                            onExpandChange = { isBranchExpanded = it },
                            options = if (branchOptions.isNotEmpty()) branchOptions else listOf("Loading branches..."),
                            onOptionSelected = { selected ->
                                branchJurisdiction = selected
                                val index = branchOptions.indexOf(selected)
                                if (index != -1 && index < branches.size) {
                                    selectedBranchId = branches[index].id
                                    // Auto-fill state if available
                                    branches[index].address.state?.let { subdivisionName = it }
                                }
                            }
                        )
                    }

                    item {
                        FormLabel("Country Code (ISO 3166-1)")
                        FormTextField(
                            value = countryCode,
                            onValueChange = { countryCode = it.uppercase() },
                            placeholder = "IN"
                        )
                    }

                    item {
                        FormLabel("State / Province (Subdivision Name)")
                        FormTextField(
                            value = subdivisionName,
                            onValueChange = { subdivisionName = it },
                            placeholder = "e.g. Tamil Nadu, Karnataka"
                        )
                    }

                    item {
                        FormLabel("Subdivision Code (ISO 3166-2)")
                        FormTextField(
                            value = subdivisionCode,
                            onValueChange = { subdivisionCode = it.uppercase() },
                            placeholder = "E.G. TN, KA, MH"
                        )
                    }

                    item {
                        FormLabel("Registration Date (Valid From)")
                        DatePickerField(
                            value = registrationDate,
                            onDateSelected = { registrationDate = it }
                        )
                    }

                    item {
                        FormLabel("Registered Statutory Address")
                        FormTextArea(
                            value = registeredAddress,
                            onValueChange = { registeredAddress = it },
                            placeholder = "Full official address registered with tax department...",
                            maxLines = 4,
                            minLines = 3
                        )
                    }
                }

                // ==========================================
                // TAB 1: TAX DEFAULTS & REGIME
                // ==========================================
                if (selectedTabIndex == 1) {
                    item {
                        FormLabel("Tax Regime")
                        FormDropdown(
                            value = taxRegime,
                            expanded = isTaxRegimeExpanded,
                            onExpandChange = { isTaxRegimeExpanded = it },
                            options = taxRegimeList,
                            onOptionSelected = { taxRegime = it }
                        )
                    }

                    item {
                        FormLabel("Tax Scheme")
                        FormDropdown(
                            value = taxScheme,
                            expanded = isTaxSchemeExpanded,
                            onExpandChange = { isTaxSchemeExpanded = it },
                            options = taxSchemeList,
                            onOptionSelected = { taxScheme = it }
                        )
                    }

                    item {
                        FormLabel("Default Intra-State Tax Group")
                        FormDropdown(
                            value = defaultIntraStateTaxGroup.ifBlank { "Select CGST + SGST Group" },
                            expanded = isIntraStateExpanded,
                            onExpandChange = { isIntraStateExpanded = it },
                            options = if (taxGroupOptions.isNotEmpty()) taxGroupOptions else listOf("No tax groups available"),
                            onOptionSelected = { selected ->
                                defaultIntraStateTaxGroup = selected
                                val index = taxGroupOptions.indexOf(selected)
                                if (index != -1 && index < taxGroups.size) {
                                    selectedIntraStateTaxGroupId = taxGroups[index].id
                                }
                            }
                        )
                    }

                    item {
                        FormLabel("Default Inter-State Tax Group")
                        FormDropdown(
                            value = defaultInterStateTaxGroup.ifBlank { "Select IGST Group" },
                            expanded = isInterStateExpanded,
                            onExpandChange = { isInterStateExpanded = it },
                            options = if (taxGroupOptions.isNotEmpty()) taxGroupOptions else listOf("No tax groups available"),
                            onOptionSelected = { selected ->
                                defaultInterStateTaxGroup = selected
                                val index = taxGroupOptions.indexOf(selected)
                                if (index != -1 && index < taxGroups.size) {
                                    selectedInterStateTaxGroupId = taxGroups[index].id
                                }
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPrimaryRegistration = !isPrimaryRegistration }
                        ) {
                            AppCheckbox(
                                checked = isPrimaryRegistration,
                                onCheckedChange = { isPrimaryRegistration = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Set as Primary Registration for this Entity",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isGroupRegistration = !isGroupRegistration }
                        ) {
                            AppCheckbox(
                                checked = isGroupRegistration,
                                onCheckedChange = { isGroupRegistration = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Is Group Tax Registration (Tax Representation Group)",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // ==========================================
                // TAB 2: FILING & INVOICE PREFERENCES
                // ==========================================
                if (selectedTabIndex == 2) {
                    item {
                        FormLabel("Filing Accounting Method")
                        FormDropdown(
                            value = filingAccountingMethod,
                            expanded = isFilingMethodExpanded,
                            onExpandChange = { isFilingMethodExpanded = it },
                            options = filingMethods,
                            onOptionSelected = { filingAccountingMethod = it }
                        )
                    }

                    item {
                        FormLabel("Filing Frequency")
                        FormDropdown(
                            value = filingFrequency,
                            expanded = isFilingFrequencyExpanded,
                            onExpandChange = { isFilingFrequencyExpanded = it },
                            options = filingFrequencies,
                            onOptionSelected = { filingFrequency = it }
                        )
                    }

                    item {
                        FormLabel("Tax Rounding Rule")
                        FormDropdown(
                            value = taxRoundingRule,
                            expanded = isTaxRoundingExpanded,
                            onExpandChange = { isTaxRoundingExpanded = it },
                            options = taxRoundingRules,
                            onOptionSelected = { taxRoundingRule = it }
                        )
                    }

                    item {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "INVOICE DISPLAY PREFERENCES",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTaxBreakdownOnInvoice = !showTaxBreakdownOnInvoice }
                            ) {
                                AppCheckbox(
                                    checked = showTaxBreakdownOnInvoice,
                                    onCheckedChange = { showTaxBreakdownOnInvoice = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Show detailed sub-tax breakdown on customer invoices (CGST/SGST/IGST)",
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showHsnSacOnInvoice = !showHsnSacOnInvoice }
                            ) {
                                AppCheckbox(
                                    checked = showHsnSacOnInvoice,
                                    onCheckedChange = { showHsnSacOnInvoice = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Show HSN/SAC statutory codes on invoice line items",
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPlaceOfSupplyOnInvoice = !showPlaceOfSupplyOnInvoice }
                            ) {
                                AppCheckbox(
                                    checked = showPlaceOfSupplyOnInvoice,
                                    onCheckedChange = { showPlaceOfSupplyOnInvoice = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Show Place of Supply jurisdiction code on invoices",
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // TAB 3: COMPLIANCE REMINDERS
                // ==========================================
                if (selectedTabIndex == 3) {
                    item {
                        FormLabel("Reminder Lead Time (Days Before Deadline)")
                        FormTextField(
                            value = reminderLeadDays,
                            onValueChange = { reminderLeadDays = it },
                            placeholder = "5"
                        )
                    }

                    item {
                        FormLabel("Tax Agent / Preparer ID")
                        FormTextField(
                            value = taxAgentNumber,
                            onValueChange = { taxAgentNumber = it },
                            placeholder = "e.g. AGENT-88402"
                        )
                    }

                    item {
                        FormLabel("Authorized Statutory Filing Officer")
                        FormDropdown(
                            value = authorizedFilingOfficer.ifBlank { "Select Team Member" },
                            expanded = isOfficerExpanded,
                            onExpandChange = { isOfficerExpanded = it },
                            options = if (authUserOptions.isNotEmpty()) authUserOptions else listOf("No team members found"),
                            onOptionSelected = { selected ->
                                authorizedFilingOfficer = selected
                                val index = authUserOptions.indexOf(selected)
                                if (index != -1 && index < staffList.size) {
                                    selectedStaffId = staffList[index].id
                                }
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sendEmailReminders = !sendEmailReminders }
                        ) {
                            AppCheckbox(
                                checked = sendEmailReminders,
                                onCheckedChange = { sendEmailReminders = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Send automated email alerts to authorized filing users before return deadlines",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sendValidationAlerts = !sendValidationAlerts }
                        ) {
                            AppCheckbox(
                                checked = sendValidationAlerts,
                                onCheckedChange = { sendValidationAlerts = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Send real-time alerts on statutory validation failures",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            color = primary_light,
                            border = BorderStroke(1.dp, light_blue_border)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Compliance reminders are dispatched automatically according to statutory deadlines configured for this tax regime. Ensure authorized users have verified email addresses.",
                                    fontSize = tokens.bodySmall,
                                    color = Primary,
                                    lineHeight = tokens.bodySmall.times(1.3f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Fixed Navigation Buttons
        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            onBack = {
                settingsViewModel.clearSelectedTaxRegistration()
                onClose()
            },
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Update(
                label = if (selectedTaxItem != null) "Update Changes" else "Save Changes",
                onClick = onSave
            ),
            backWidthFraction = 0.25f,
            trailingWidthFraction = 0.35f
        )
    }
}

// ── Toggle Switch Row Helper Component ──
@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = tokens.bodySmall,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        MiniSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}