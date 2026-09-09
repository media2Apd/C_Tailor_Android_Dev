@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.mobile.view.home.finance.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.sales.lead.MiniSwitch

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
    onClose: () -> Unit = {},
    onAddGst: () -> Unit = {},
    onEditGst: (GstRegistrationItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val mockGstList = remember {
        listOf(
            GstRegistrationItem(
                id = "1",
                title = "Tamil Nadu GST Registration",
                group = "South India GST Group",
                gstin = "33AABCU9603R1ZM",
                type = "Regular",
                state = "Tamil Nadu",
                status = "Active",
                effectiveDate = "01 Apr 2024"
            ),
            GstRegistrationItem(
                id = "2",
                title = "Karnataka GST Registration",
                group = "South India GST Group",
                gstin = "29AABCU9603R1ZP",
                type = "Regular",
                state = "Karnataka",
                status = "Active",
                effectiveDate = "01 Apr 2024"
            ),
            GstRegistrationItem(
                id = "3",
                title = "Kerala GST Registration",
                group = "South India GST Group",
                gstin = "32AABCU9603R1ZN",
                type = "Composition",
                state = "Kerala",
                status = "Active",
                effectiveDate = "15 Jun 2024"
            ),
            GstRegistrationItem(
                id = "4",
                title = "Maharashtra GST Registration",
                group = "West India GST Group",
                gstin = "27AABCU9603R1ZQ",
                type = "Regular",
                state = "Maharashtra",
                status = "Inactive",
                effectiveDate = "01 Apr 2023"
            ),
            GstRegistrationItem(
                id = "5",
                title = "Delhi GST Registration",
                group = "North India GST Group",
                gstin = "07AABCU9603R1ZP",
                type = "Regular",
                state = "Delhi",
                status = "Active",
                effectiveDate = "01 Jul 2024"
            )
        )
    }

    val filteredList = remember(searchQuery, mockGstList) {
        if (searchQuery.isBlank()) mockGstList
        else mockGstList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.gstin.contains(searchQuery, ignoreCase = true) ||
                    it.state.contains(searchQuery, ignoreCase = true)
        }
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
                HorizontalDivider(color = dividerColor, thickness = 2.dp)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = tokens.extraPadding * 0.5f,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)
                ) {
                    // Search & Filter Bar
                    item {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search Customers...",
                            borderColor = sectionBorder,
                            textSecondaryColor = close_color,
                            height = tokens.fieldHeight,
                            onFilterClick = {}
                        )
                    }

                    // Header Title & Description
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding)
                        ) {
                            Text(
                                text = "GST Settings",
                                fontSize = tokens.h2,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Manage your GST registrations and GST groups across states and union territories.",
                                fontSize = tokens.bodySmall,
                                color = close_color,
                                lineHeight = tokens.bodySmall.times(1.3f)
                            )
                        }
                    }

                    // 2x2 Metric Summary Box
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(whiteBg)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                MetricSummaryCell(
                                    title = "Active Registrations",
                                    count = "4",
                                    indicatorText = "All Compliant",
                                    indicatorColor = greentext,
                                    modifier = Modifier.weight(1f)
                                )
                                VerticalDivider(color = dividerColor, thickness = 2.dp, modifier = Modifier.height(100.dp))
                                MetricSummaryCell(
                                    title = "Inactive Registrations",
                                    count = "1",
                                    indicatorText = "Deactivated",
                                    indicatorColor = redText,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider(color = dividerColor, thickness = 2.dp)
                            Row(modifier = Modifier.fillMaxWidth()) {
                                MetricSummaryCell(
                                    title = "GST Groups",
                                    count = "2",
                                    indicatorText = "North / South / West",
                                    indicatorColor = close_color,
                                    showDot = false,
                                    modifier = Modifier.weight(1f)
                                )
                                VerticalDivider(color = dividerColor, thickness = 2.dp, modifier = Modifier.height(100.dp))

                                MetricSummaryCell(
                                    title = "States Covered",
                                    count = "4",
                                    indicatorText = "States & UTs",
                                    indicatorColor = close_color,
                                    showDot = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // List of GST Registration Cards
                    items(filteredList, key = { it.id }) { item ->
                        GstRegistrationCard(
                            item = item,
                            onEditClick = { onEditGst(item) }
                        )
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

// ── GST Card Component ──
@Composable
fun GstRegistrationCard(
    item: GstRegistrationItem,
    onEditClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isActive = item.status.equals("Active", ignoreCase = true)
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding * 0.8f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Title & 3-Dot Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = item.group,
                        fontSize = tokens.caption,
                        color = close_color
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(whiteBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = TextPrimary, fontSize = tokens.bodySmall) },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            }
                        )
                    }
                }
            }

            // GSTIN Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(cardBgLight)
                    .border(1.dp, light_blue_border, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "GSTIN: ${item.gstin}",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Bold,
                    color = textSubdued
                )
            }

            // Details Line
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Type:", fontSize = tokens.caption, color = close_color)
                Text(text = item.type, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = "•", fontSize = tokens.caption, color = close_color)
                Text(text = "State:", fontSize = tokens.caption, color = close_color)
                Text(text = item.state, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            HorizontalDivider(color = dividerColor, thickness = 2.dp)
            // Bottom Badges Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) greenBg else modelGray)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isActive) greentext else close_color)
                    )
                    Text(
                        text = item.status,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) greentext else close_color
                    )
                }
                // Effective Date
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Effective:", fontSize = tokens.label, color = close_color)
                    Text(text = item.effectiveDate, fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
        }
    }
}

// ===========================================================
// SCREEN 2: ADD/EDIT GST SETTINGS FORM
// ===========================================================

@Composable
fun AddGstSettingsScreen(
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "GST Registration",
        "GST Behaviour",
        "Tax Preferences",
        "Compliance Settings"
    )

    // ── Tab 1 States (GST Registration) ──
    var isGstRegistered by remember { mutableStateOf("Yes") }
    var gstinNumber by remember { mutableStateOf("") }
    var legalBusinessName by remember { mutableStateOf("Threads & Style Pvt. Ltd.") }
    var tradeName by remember { mutableStateOf("CUSO Tailor") }
    var panNumber by remember { mutableStateOf("ABCDE1234F") }
    var registrationDate by remember { mutableStateOf("") }
    var businessState by remember { mutableStateOf("") }
    var isBusinessStateExpanded by remember { mutableStateOf(false) }
    var taxScheme by remember { mutableStateOf("") }
    var isTaxSchemeExpanded by remember { mutableStateOf(false) }
    var registrationStatus by remember { mutableStateOf("Active") }
    var isRegStatusExpanded by remember { mutableStateOf(false) }

    // ── Tab 2 States (GST Behaviour) ──
    var defaultIntraStateTax by remember { mutableStateOf("") }
    var isIntraStateExpanded by remember { mutableStateOf(false) }
    var defaultInterStateTax by remember { mutableStateOf("") }
    var isInterStateExpanded by remember { mutableStateOf(false) }
    var rcmEnabled by remember { mutableStateOf(false) }
    var compositionSchemeEnabled by remember { mutableStateOf(false) }
    var exportSezEnabled by remember { mutableStateOf(false) }

    // ── Tab 3 States (Tax Preferences) ──
    var defaultTaxTreatment by remember { mutableStateOf("") }
    var isTaxTreatmentExpanded by remember { mutableStateOf(false) }
    var defaultTaxRate by remember { mutableStateOf("") }
    var isTaxRateExpanded by remember { mutableStateOf(false) }
    var taxCalculationMethod by remember { mutableStateOf("") }
    var isTaxCalcMethodExpanded by remember { mutableStateOf(false) }
    var placeOfSupplyBehaviour by remember { mutableStateOf("") }
    var isPlaceOfSupplyExpanded by remember { mutableStateOf(false) }
    var showTaxBreakdown by remember { mutableStateOf(true) }
    var showHsnSac by remember { mutableStateOf(true) }
    var showPlaceOfSupply by remember { mutableStateOf(true) }

    // ── Tab 4 States (Compliance Settings) ──
    var filingFrequency by remember { mutableStateOf("") }
    var isFilingFrequencyExpanded by remember { mutableStateOf(false) }
    var returnPeriodStart by remember { mutableStateOf("") }
    var isReturnPeriodStartExpanded by remember { mutableStateOf(false) }
    var requireApprovalBeforeFiling by remember { mutableStateOf(true) }
    var sendEmailReminder by remember { mutableStateOf(true) }
    var notifyValidationErrors by remember { mutableStateOf(true) }
    var authorizedFilingUser by remember { mutableStateOf("") }
    var isAuthUserExpanded by remember { mutableStateOf(false) }
    var complianceReminder by remember { mutableStateOf("") }
    var isComplianceReminderExpanded by remember { mutableStateOf(false) }

    val stateList = listOf("Karnataka", "Tamil Nadu", "Kerala", "Maharashtra", "Delhi")
    val taxSchemeList = listOf("Regular", "Composition", "Casual Taxable", "Non-Resident")
    val statusList = listOf("Active", "Inactive", "Suspended")
    val intraStateTaxList = listOf("CGST + SGST", "UTGST", "Exempt")
    val interStateTaxList = listOf("IGST", "Exempt", "Zero Rated")
    val taxTreatmentList = listOf("Taxable", "Exempt", "Nil Rated", "Non-GST")
    val taxRateList = listOf("GST 0%", "GST 5%", "GST 12%", "GST 18% — Services", "GST 28%")
    val taxCalcMethodList = listOf("Tax Exclusive (tax added on top)", "Tax Inclusive (tax included in price)")
    val placeOfSupplyList = listOf("Use Customer Billing State", "Use Customer Shipping State", "Use Place of Delivery")
    val filingFrequencyList = listOf("Monthly", "Quarterly", "Annually")
    val returnPeriodList = listOf("April (Financial Year)", "January (Calendar Year)")
    val authUserList = listOf("Finance Admin (admin@cusotailor.in)", "Accounts Manager (accounts@cusotailor.in)")
    val reminderDaysList = listOf("3 days before deadline", "7 days before deadline", "15 days before deadline")

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "GST Settings", onClose = onClose)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(bottom = tokens.buttonHeight * 2),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // Section Title & Subtitle based on selected tab
                item {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = tabs[selectedTabIndex],
                            fontSize = tokens.h2,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = when (selectedTabIndex) {
                                0 -> "Manage your GST registration details across states and union territories."
                                1 -> "Configure how GST is applied to your sales and purchases."
                                2 -> "Configure tax rates, tax rules, and preferences for your business."
                                else -> "Configure how GST is applied to your sales and purchases."
                            },
                            fontSize = tokens.bodySmall,
                            color = close_color
                        )
                    }
                }

                // ── Reusable Underline Tab Row Component ──
                item {
                    AppUnderlineTabRow(
                        tabs = tabs,
                        selectedIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                        isScrollable = true
                    )
                }

                // ==========================================
                // TAB 1: GST REGISTRATION
                // ==========================================
                if (selectedTabIndex == 0) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        FormLabel("GST Registered", isRequired = true)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { isGstRegistered = "Yes" }
                            ) {
                                RadioButton(
                                    selected = isGstRegistered == "Yes",
                                    onClick = { isGstRegistered = "Yes" },
                                    colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                )
                                Text("Yes", fontSize = tokens.bodyMedium, color = TextPrimary)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { isGstRegistered = "No" }
                            ) {
                                RadioButton(
                                    selected = isGstRegistered == "No",
                                    onClick = { isGstRegistered = "No" },
                                    colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                )
                                Text("No", fontSize = tokens.bodyMedium, color = TextPrimary)
                            }
                        }
                    }

                    item {
                        FormLabel("GSTIN", isRequired = true)
                        FormTextField(
                            value = gstinNumber,
                            onValueChange = { gstinNumber = it },
                            placeholder = "29ABCDE1234F1Z5"
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "15-character GST Identification Number",
                            fontSize = tokens.caption,
                            color = close_color
                        )
                    }

                    item {
                        FormLabel("Legal Business Name (from Business Profile)")
                        FormTextField(
                            value = legalBusinessName,
                            onValueChange = { legalBusinessName = it },
                            enabled = false,
                            containerColor = light_blue
                        )
                    }

                    item {
                        FormLabel("Trade Name (from Business Profile)")
                        FormTextField(
                            value = tradeName,
                            onValueChange = { tradeName = it },
                            enabled = false,
                            containerColor = light_blue
                        )
                    }

                    item {
                        FormLabel("PAN (from Business Profile)")
                        FormTextField(
                            value = panNumber,
                            onValueChange = { panNumber = it },
                            enabled = false,
                            containerColor = light_blue
                        )
                    }

                    item {
                        FormLabel("Registration Date")
                        DatePickerField(
                            value = registrationDate,
                            onDateSelected = { registrationDate = it }
                        )
                    }

                    item {
                        FormLabel("Business State")
                        FormDropdown(
                            value = businessState,
                            expanded = isBusinessStateExpanded,
                            onExpandChange = { isBusinessStateExpanded = it },
                            options = stateList,
                            onOptionSelected = { businessState = it }
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
                        FormLabel("Registration Status")
                        FormDropdown(
                            value = registrationStatus,
                            expanded = isRegStatusExpanded,
                            onExpandChange = { isRegStatusExpanded = it },
                            options = statusList,
                            onOptionSelected = { registrationStatus = it }
                        )
                    }
                }

                // ==========================================
                // TAB 2: GST BEHAVIOUR 
                // ==========================================
                if (selectedTabIndex == 1) {
                    item {
                        FormSectionHeader("Tax Type Defaults")

                        FormLabel("Default Intra-State Tax", isRequired = true)
                        FormDropdown(
                            value = defaultIntraStateTax,
                            expanded = isIntraStateExpanded,
                            onExpandChange = { isIntraStateExpanded = it },
                            options = intraStateTaxList,
                            onOptionSelected = { defaultIntraStateTax = it }
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Applied when customer and business are in the same state", fontSize = tokens.caption, color = close_color)

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Default Inter-State Tax", isRequired = true)
                        FormDropdown(
                            value = defaultInterStateTax,
                            expanded = isInterStateExpanded,
                            onExpandChange = { isInterStateExpanded = it },
                            options = interStateTaxList,
                            onOptionSelected = { defaultInterStateTax = it }
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Applied when customer and business are in different states", fontSize = tokens.caption, color = close_color)
                    }

                    item {
                        FormSectionHeader("GST Features")

                        ToggleRow("Reverse Charge Mechanism (RCM)", rcmEnabled) { rcmEnabled = it }
                        HorizontalDivider(color = dividerColor, thickness = 2.dp)
                        ToggleRow("Composition Scheme", compositionSchemeEnabled) { compositionSchemeEnabled = it }
                        HorizontalDivider(color = dividerColor, thickness = 2.dp)
                        ToggleRow("Export / SEZ Transactions", exportSezEnabled) { exportSezEnabled = it }
                    }

                    item {
                        FormSectionHeader("Business State")

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            color = cardBgLight,
                            border = BorderStroke(1.dp, light_blue_border)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Karnataka — sourced from Business Profile.",
                                    fontSize = tokens.bodySmall,
                                    color = close_color
                                )
                                Text(
                                    text = "Edit in Business Profile →",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary,
                                    modifier = Modifier.clickable { }
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // TAB 3: TAX PREFERENCES
                // ==========================================
                if (selectedTabIndex == 2) {
                    item {
                        FormSectionHeader("Tax Configuration")

                        FormLabel("Default Tax Treatment", isRequired = true)
                        FormDropdown(
                            value = defaultTaxTreatment,
                            expanded = isTaxTreatmentExpanded,
                            onExpandChange = { isTaxTreatmentExpanded = it },
                            options = taxTreatmentList,
                            onOptionSelected = { defaultTaxTreatment = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Default Tax Rate", isRequired = true)
                        FormDropdown(
                            value = defaultTaxRate,
                            expanded = isTaxRateExpanded,
                            onExpandChange = { isTaxRateExpanded = it },
                            options = taxRateList,
                            onOptionSelected = { defaultTaxRate = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Tax Calculation Method")
                        FormDropdown(
                            value = taxCalculationMethod,
                            expanded = isTaxCalcMethodExpanded,
                            onExpandChange = { isTaxCalcMethodExpanded = it },
                            options = taxCalcMethodList,
                            onOptionSelected = { taxCalculationMethod = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Place of Supply Behaviour")
                        FormDropdown(
                            value = placeOfSupplyBehaviour,
                            expanded = isPlaceOfSupplyExpanded,
                            onExpandChange = { isPlaceOfSupplyExpanded = it },
                            options = placeOfSupplyList,
                            onOptionSelected = { placeOfSupplyBehaviour = it }
                        )
                    }

                    item {
                        FormSectionHeader("Tax Display")

                        ToggleRow("Show tax breakdown on invoices", showTaxBreakdown) { showTaxBreakdown = it }
                        HorizontalDivider(color = dividerColor, thickness = 2.dp)
                        ToggleRow("Show HSN/SAC on invoices", showHsnSac) { showHsnSac = it }
                        HorizontalDivider(color = dividerColor, thickness = 2.dp)
                        ToggleRow("Show Place of Supply on invoices", showPlaceOfSupply) { showPlaceOfSupply = it }
                    }
                }

                // ==========================================
                // TAB 4: COMPLIANCE SETTINGS
                // ==========================================
                if (selectedTabIndex == 3) {
                    item {
                        FormSectionHeader("Filing Configuration")

                        FormLabel("Filing Frequency", isRequired = true)
                        FormDropdown(
                            value = filingFrequency,
                            expanded = isFilingFrequencyExpanded,
                            onExpandChange = { isFilingFrequencyExpanded = it },
                            options = filingFrequencyList,
                            onOptionSelected = { filingFrequency = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Return Period Start", isRequired = true)
                        FormDropdown(
                            value = returnPeriodStart,
                            expanded = isReturnPeriodStartExpanded,
                            onExpandChange = { isReturnPeriodStartExpanded = it },
                            options = returnPeriodList,
                            onOptionSelected = { returnPeriodStart = it }
                        )
                    }

                    item {
                        FormSectionHeader("Approval Workflow")

                        ToggleRow("Require approval before filing GST returns", requireApprovalBeforeFiling) { requireApprovalBeforeFiling = it }
                        HorizontalDivider(color = dividerColor, thickness = 2.dp)
                        ToggleRow("Send email reminder before filing deadline", sendEmailReminder) { sendEmailReminder = it }
                        HorizontalDivider(color = dividerColor, thickness = 2.dp)
                        ToggleRow("Notify on validation errors", notifyValidationErrors) { notifyValidationErrors = it }
                    }

                    item {
                        FormSectionHeader("Filing & Reminders")

                        FormLabel("Authorized Filing User")
                        FormDropdown(
                            value = authorizedFilingUser,
                            expanded = isAuthUserExpanded,
                            onExpandChange = { isAuthUserExpanded = it },
                            options = authUserList,
                            onOptionSelected = { authorizedFilingUser = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Compliance Reminder")
                        FormDropdown(
                            value = complianceReminder,
                            expanded = isComplianceReminderExpanded,
                            onExpandChange = { isComplianceReminderExpanded = it },
                            options = reminderDaysList,
                            onOptionSelected = { complianceReminder = it }
                        )

                        Spacer(Modifier.height(16.dp))

                        // Info Box
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
                                    text = "Compliance reminders are sent to the authorized filing user via email. Ensure the user has an active account in CUSO.",
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

        // Bottom Fixed Navigation Buttons (Cancel / Save Changes)
        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            onBack = onClose,
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Update(
                label = "Save Changes",
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