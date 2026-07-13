package com.cuso.mobile.view.home.sales.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.customFieldOutlinedColors
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.PhoneInputField
import com.cuso.mobile.view.home.FormDropdown   // ✅ NEW — reuse the shared dropdown from Lead screens
import com.cuso.mobile.view.home.reusablecomposables.StepNavigationFab
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.view.home.reusablecomposables.dashedBorder
import com.cuso.mobile.view.organization.OrgOptions
import com.cuso.mobile.view.organization.OrganizationDropdown
import com.cuso.mobile.viewmodel.CustomerDetailUiState
import com.cuso.mobile.viewmodel.CustomerUpdateState
import com.cuso.mobile.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch

private val stepLabels = listOf(
    "Personal\nInformation",
    "Measurements",
    "Order\n& Payment",
    "Preferences",
    "Notes\n& Tags"
)
@Suppress("UNUSED_PARAMETER")

@Composable
fun CustomerDetailScreen(
    navController: NavController,
    customerId: String,
    viewModel: CustomerViewModel = hiltViewModel(),
    startInEditMode: Boolean = false,
    onClose: () -> Unit = { navController.popBackStack() },
    onUpdateSuccess: () -> Unit = onClose,
    onRequestEdit: () -> Unit = {}

) {
    val detailState by viewModel.detailState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var currentStep by remember { mutableIntStateOf(0) }


    var isEditMode by remember(startInEditMode) { mutableStateOf(startInEditMode) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(customerId) { viewModel.loadCustomerDetail(customerId) }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is CustomerUpdateState.Success -> {
                coroutineScope.launch { snackbarHostState.showSnackbar("Customer updated successfully") }
                isEditMode = false
                onUpdateSuccess()
                viewModel.resetUpdateState()
            }
            is CustomerUpdateState.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Header ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Customer Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF111827),
                        modifier = Modifier.clickable { onClose() }
                    )
                }

                // ── Stepper ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    stepLabels.forEachIndexed { index, label ->
                        val done = index < currentStep
                        val active = index == currentStep
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(
                                        color = when {
                                            done -> Color(0xFF22C55E)
                                            active -> Color(0xFF3B3BF9)
                                            else -> Color(0xFFE5E7EB)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (done) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                } else {
                                    Text("${index + 1}", color = if (active) Color.White else Color(0xFF9CA3AF), fontSize = 12.sp)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                label,
                                fontSize = 9.sp,
                                lineHeight = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (active) Color(0xFF3B3BF9) else Color(0xFF9CA3AF),
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(top = 10.dp))

                // ── Body ── (fills the rest of the Column, buttons float ON TOP via outer Box)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 90.dp)     // reserve scroll space so content isn't hidden behind FABs
                ) {
                    when (currentStep) {
                        0 -> PersonalInformationStep(detailState, formState, viewModel, isEditMode)
                        1 -> MeasurementsStep(isEditMode)
                        2 -> OrderPaymentStep()
                        3 -> PreferencesStep()
                        4 -> NotesTagsStep(isEditMode)
                    }
                }
            }



            StepNavigationFab(
                showBack = currentStep > 0,
                onBack = { currentStep-- },
                trailingAction = when {
                    currentStep < stepLabels.lastIndex -> TrailingFabAction.Next { currentStep++ }
                    !isEditMode -> TrailingFabAction.Edit { onRequestEdit() }
                    else -> TrailingFabAction.Update(
                        onClick = { viewModel.updateCustomer(customerId) },
                        isLoading = updateState is CustomerUpdateState.Loading
                    )
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 1 — Personal Information (dynamic, from getCustomerView API)
// ─────────────────────────────────────────────────────────────

@Composable
private fun PersonalInformationStep(
    detailState: CustomerDetailUiState,
    formState: com.cuso.mobile.viewmodel.CustomerFormState,
    viewModel: CustomerViewModel,
    isEditMode: Boolean   // controls whether fields are editable
) {
    when (detailState) {
        is CustomerDetailUiState.Loading -> {
            Box(Modifier
                .fillMaxWidth()
                .padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                CirculerProgressIndicatorReuse()
            }
        }
        is CustomerDetailUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text(detailState.message, color = Color.Red, fontSize = 14.sp)
            }
        }
        is CustomerDetailUiState.Success -> {
            // ── Accordion open/close state — only one section open at a time ──
            var expandedSection by remember { mutableStateOf("identity") }

            // ── UI-only fields (NOT sent to update API — backend doesn't support them yet) ──
            var gender by remember { mutableStateOf("") }
            var dob by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var preferredContact by remember { mutableStateOf("") }
            var areaZone by remember { mutableStateOf("") }

            // ✅ expanded state for each FormDropdown (it's a hoisted/controlled dropdown)
            var typeExpanded by remember { mutableStateOf(false) }
            var genderExpanded by remember { mutableStateOf(false) }
            var statusExpanded by remember { mutableStateOf(false) }
            var contactExpanded by remember { mutableStateOf(false) }
            // state (rename from preferredLanguage -> language, remove languageExpanded, not needed anymore)
            var language by remember { mutableStateOf("") }

            Column {
                // Info banner — shows current mode so it's obvious to the user
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isEditMode)
                            "Edit mode — update the details below."
                        else
                            "Viewing customer details. Tap Edit to make changes.",
                        fontSize = 12.sp,
                        color = Color(0xFF1E40AF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── SECTION 1 — Customer Identity ──
                AccordionSectionCard(
                    icon = Icons.Default.Person,
                    title = "Customer Identity",
                    subtitle = "Basic customer information",
                    expanded = expandedSection == "identity",
                    onToggle = { expandedSection = if (expandedSection == "identity") "" else "identity" }
                ) {
                    FormDropdown(
                        label = "Customer Type",
                        value = formState.type.replaceFirstChar { it.uppercase() }.ifEmpty { "Select an option" },
                        expanded = typeExpanded,
                        onExpandChange = { typeExpanded = it },
                        options = listOf("Individual", "Business"),
                        onOptionSelected = { label ->
                            viewModel.onTypeChange(label.lowercase())   // maps back to "individual"/"business" for the API
                        },
                        isRequired = true,
                        enabled = isEditMode
                    )
                    Spacer(Modifier.height(14.dp))
                    LabeledField("Full Name *") {
                        CustomerOutlinedField(
                            value = formState.name,
                            onValueChange = viewModel::onNameChange,
                            placeholder = "Enter your name",
                            enabled = isEditMode
                        )
                    }
                    FormDropdown(
                        label = "Gender",
                        value = gender.ifEmpty { "Select an option" },
                        expanded = genderExpanded,
                        onExpandChange = { genderExpanded = it },
                        options = listOf("Male", "Female", "Other"),
                        onOptionSelected = { gender = it },
                        isRequired = true,
                        enabled = isEditMode
                    )
                    Spacer(Modifier.height(14.dp))
                    LabeledField("Date of Birth *") {
                        DatePickerField(
                            value = dob.ifEmpty { "Select date" },
                            onDateSelected = { dob = it },
                            enabled = isEditMode   // DatePickerField exposes an `enabled` param
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── SECTION 2 — Customer Details ──
                AccordionSectionCard(
                    icon = Icons.Default.Badge,
                    title = "Customer Details",
                    subtitle = "Communication Preferences",
                    expanded = expandedSection == "details",
                    onToggle = { expandedSection = if (expandedSection == "details") "" else "details" }
                ) {
                    PhoneInputField(
                        phoneValue = formState.mobile,
                        onPhoneChange = viewModel::onMobileChange,
                        onCountryChange = {  },
                        enabled = isEditMode
                    )
                    LabeledField("Email address *") {
                        CustomerOutlinedField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Enter your email",
                            enabled = isEditMode
                        )
                    }
                    FormDropdown(
                        label = "Status",
                        value = formState.status.ifEmpty { "Select an option" },
                        expanded = statusExpanded,
                        onExpandChange = { statusExpanded = it },
                        options = listOf("Active", "Inactive"),
                        onOptionSelected = viewModel::onStatusChange,
                        isRequired = true,
                        enabled = isEditMode
                    )
                    Spacer(Modifier.height(14.dp))
                    Text("Preferred Language",color=Color(0xFF9CA3AF), fontSize = 13.sp)
                    OrganizationDropdown(
                        items = OrgOptions.languages,
                        selected = language,
                        enabled = isEditMode
                    ) { language = it }
                    Spacer(Modifier.height(14.dp))
                    FormDropdown(
                        label = "Preferred Contact",
                        value = preferredContact.ifEmpty { "Select an option" },
                        expanded = contactExpanded,
                        onExpandChange = { contactExpanded = it },
                        options = listOf("Call", "WhatsApp", "Email"),
                        onOptionSelected = { preferredContact = it },
                        isRequired = true,
                        enabled = isEditMode
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── SECTION 3 — Location & Communication ──
                AccordionSectionCard(
                    icon = Icons.Default.LocationOn,
                    title = "Location & Communication",
                    subtitle = "Contact details",
                    expanded = expandedSection == "location",
                    onToggle = { expandedSection = if (expandedSection == "location") "" else "location" }
                ) {
                    LabeledField("Address *") {
                        CustomerOutlinedField(
                            value = formState.addressLine,
                            onValueChange = viewModel::onAddressLineChange,
                            placeholder = "Enter your address",
                            enabled = isEditMode
                        )
                    }
                    LabeledField("Area/Zone *") {
                        CustomerOutlinedField(
                            value = areaZone,
                            onValueChange = { areaZone = it },
                            placeholder = "Select Status",
                            enabled = isEditMode
                        )
                    }
                    LabeledField("City *") {
                        CustomerOutlinedField(
                            value = formState.city,
                            onValueChange = viewModel::onCityChange,
                            placeholder = "Enter your City",
                            enabled = isEditMode
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Accordion section — expand/collapse with rotating chevron
// ─────────────────────────────────────────────────────────────
@Composable
private fun AccordionSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val chevronRotation by androidx.compose.animation.core.animateFloatAsState(
        if (expanded) 180f else 0f, label = "accordion_chevron"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
                Text(subtitle, fontSize = 11.sp, color = Color(0xFF9CA3AF))
            }
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = Color(0xFF6B7280),
                modifier = Modifier
                    .size(22.dp)
                    .rotate(chevronRotation)
            )
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Column(modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .padding(bottom = 14.dp)) {
                content()
            }
        }
    }
}

@Composable
 fun CustomerOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = customFieldOutlinedColors()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),                      // exact fixed height
        enabled = enabled,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 14.sp,
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
                placeholder = { Text(placeholder, fontSize = 14.sp) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),  // centers text in 40dp
                colors = colors,
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            )
        }
    )
}

// ─────────────────────────────────────────────────────────────
// STEP 2 — Measurements (static, matches screenshot)
// ─────────────────────────────────────────────────────────────
@Composable
private fun MeasurementsStep(isEditMode: Boolean) {
    var expandedSection by remember { mutableStateOf("profile") }

    Column {
        // ── SECTION 1 — Measurement Profile (stats + chips + buttons) ──
        AccordionSectionCard(
            icon = Icons.Default.Straighten,
            title = "Measurement Profile",
            subtitle = "Linked measurement records (read-only)",
            expanded = expandedSection == "profile",
            onToggle = { expandedSection = if (expandedSection == "profile") "" else "profile" }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox(modifier = Modifier.weight(1f), value = "3", label = "ACTIVE ORDERS", highlight = true)
                StatBox(modifier = Modifier.weight(1f), value = "4 Types", label = "GARMENT TYPES")
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("LAST UPDATED", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                Text("15/12/2026", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            }
            Spacer(Modifier.height(14.dp))
            Text("GARMENT TYPES COVERED", fontSize = 11.sp, color = Color(0xFF9CA3AF))
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Shirt", "Pant", "Suit", "Kurta").forEach { Chip(it) }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = {},
                enabled = isEditMode,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B3BF9))
            ) {
                Icon(Icons.Default.Add, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add New Measurement", color = Color(0xFF3B3BF9))
            }

            Spacer(Modifier.height(8.dp))

            OutlinedIconActionButton(
                text = "View Measurements",
                icon = Icons.Default.Visibility,
                onClick = {},
                enabled = true   // always viewable, regardless of edit mode
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── SECTION 2 — Measurement Insights ──
        AccordionSectionCard(
            icon = Icons.Default.Insights,
            title = "Measurement Insights",
            subtitle = "",
            expanded = expandedSection == "insights",
            onToggle = { expandedSection = if (expandedSection == "insights") "" else "insights" }
        ) {
            InsightRow(label = "Total Alterations", value = "12")
            Spacer(Modifier.height(12.dp))
            InsightRow(label = "Frequency") {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("MEDIUM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                }
            }
            Spacer(Modifier.height(12.dp))
            InsightRow(label = "Rework Flag") {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("NO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── SECTION 3 — Measurement Profile (notes) ──
        AccordionSectionCard(
            icon = Icons.Default.Description,
            title = "Measurement Profile",
            subtitle = "",
            expanded = expandedSection == "notes",
            onToggle = { expandedSection = if (expandedSection == "notes") "" else "notes" }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF9C3), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("Prefers Slightly Loose Fitting", fontSize = 13.sp, color = Color(0xFF713F12))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Small row helper for label + value/badge pairs (Insights section)
// ─────────────────────────────────────────────────────────────
@Suppress("SameParameterValue")

@Composable
private fun InsightRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF374151))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable Outlined Icon Button (View Measurements / Add Internal Notes / Add Customer Notes)
// ─────────────────────────────────────────────────────────────
@Composable
private fun OutlinedIconActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true

) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .dashedBorder(
                color = com.cuso.mobile.ui.theme.PrimaryBorder,
                strokeWidth = 1.dp,
                cornerRadius = 8.dp
            ),
        shape = RoundedCornerShape(8.dp),
        colors = com.cuso.mobile.view.composable.customOutlinedButtonColors(),
        border = null
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text)
    }
}

@Composable
private fun InsightRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF374151))
        trailing()
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 3 — Order & Payment (static, read-only — unaffected by edit mode)
// ─────────────────────────────────────────────────────────────
@Composable
private fun OrderPaymentStep() {
    var expandedSection by remember { mutableStateOf("") }

    Column {
        // ── Stats — always visible, not accordion ──
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox(modifier = Modifier.weight(1f), value = "28", label = "Total Orders")
            StatBox(modifier = Modifier.weight(1f), value = "Mar 2022", label = "First Order")
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox(modifier = Modifier.weight(1f), value = "Jan 2026", label = "Last orders")
            StatBox(modifier = Modifier.weight(1f), value = "15.6K", label = "Avg. order value", valueColor = Color(0xFF16A34A))
        }

        Spacer(Modifier.height(16.dp))

        // ── SECTION 1 — Payment Overview ──
        AccordionSectionCard(
            icon = Icons.Default.AccountBalanceWallet,
            title = "Payment Overview",
            subtitle = "",
            expanded = expandedSection == "payment",
            onToggle = { expandedSection = if (expandedSection == "payment") "" else "payment" }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECFDF5), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("Total Spend", fontSize = 12.sp, color = Color(0xFF16A34A))
                    Spacer(Modifier.height(4.dp))
                    Text("436,800", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF2F2), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("Pending Payment", fontSize = 12.sp, color = Color(0xFFDC2626))
                    Spacer(Modifier.height(4.dp))
                    Text("8,500", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── SECTION 2 — Order History (with Frequently Ordered Garments + table) ──
        AccordionSectionCard(
            icon = Icons.Default.History,
            title = "Order History",
            subtitle = "Complete order timeline",
            expanded = expandedSection == "history",
            onToggle = { expandedSection = if (expandedSection == "history") "" else "history" }
        ) {
            Text("Frequently Ordered Garments", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Formal Shirt", "Trousers", "Suit").forEach { Chip(it) }
            }

            Spacer(Modifier.height(16.dp))

            OrderHistoryTable(
                orders = listOf(
                    OrderHistoryRow("ORD-01", "Jan 6, 2026", "Wedding Sherwani", "3,500", "inactive"),
                    OrderHistoryRow("ORD-02", "Jan 6, 2026", "Designer Blouse", "3,500", "inactive"),
                    OrderHistoryRow("ORD-03", "Jan 6, 2026", "Custom Suit", "3,500", "inactive")
                )
            )
        }
    }
}

private data class OrderHistoryRow(
    val orderId: String,
    val date: String,
    val garment: String,
    val amount: String,
    val status: String
)

@Composable
private fun OrderHistoryTable(orders: List<OrderHistoryRow>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)) {
            Text("Order ID", fontSize = 10.sp, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1f))
            Text("Date", fontSize = 10.sp, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1f))
            Text("Garment", fontSize = 10.sp, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1.2f))
            Text("Amount", fontSize = 10.sp, color = Color(0xFF9CA3AF), modifier = Modifier.weight(0.8f))
            Text("Status", fontSize = 10.sp, color = Color(0xFF9CA3AF), modifier = Modifier.weight(0.8f))
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        orders.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.orderId, fontSize = 12.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(row.date, fontSize = 11.sp, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
                Text(row.garment, fontSize = 12.sp, color = Color(0xFF111827), modifier = Modifier.weight(1.2f))
                Text(row.amount, fontSize = 12.sp, color = Color(0xFF111827), modifier = Modifier.weight(0.8f))
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .background(Color(0xFFFEE2E2), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(row.status, fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                }
            }
            HorizontalDivider(color = Color(0xFFF5F5F5))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 4 — Preferences (static, read-only info — unaffected by edit mode)
// ─────────────────────────────────────────────────────────────

@Composable
private fun PreferencesStep() {
    var expandedSection by remember { mutableStateOf("fabric") }

    Column {
        // ── SECTION 1 — Fabric Preferences ──
        AccordionSectionCard(
            icon = Icons.Default.Checkroom,
            title = "Fabric Preferences",
            subtitle = "Customer's preferred fabric types",
            expanded = expandedSection == "fabric",
            onToggle = { expandedSection = if (expandedSection == "fabric") "" else "fabric" }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cotton", "Linen", "Silk", "Wool").forEach { Chip(it) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Blends")
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── SECTION 2 — Style Preferences ──
        AccordionSectionCard(
            icon = Icons.Default.Style,
            title = "Style Preferences",
            subtitle = "Customer's preferred styles",
            expanded = expandedSection == "style",
            onToggle = { expandedSection = if (expandedSection == "style") "" else "style" }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Slim Fit", "Regular Fit", "Mandarin Collar").forEach { Chip(it) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("French Cuffs")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Info pills — always visible ──
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoPill(modifier = Modifier.weight(1f), icon = Icons.Default.Star, label = "VIP", sub = "Loyalty Level", color = Color(0xFF9333EA))
            InfoPill(modifier = Modifier.weight(1f), icon = Icons.Default.Groups, label = "5", sub = "Referrals", color = Color(0xFF374151))
            InfoPill(modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.TrendingUp, label = "Upgrade Ready", sub = "Next tier eligible", color = Color(0xFF16A34A))
        }

        Spacer(Modifier.height(16.dp))

        // ── Special Privileges — always visible ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEF2FF), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Column {
                Text("Special Privileges", fontWeight = FontWeight.Bold, color = Color(0xFF3B3BF9), fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Priority booking, 10% discount on all suit orders, Free home delivery.",
                    fontSize = 12.sp,
                    color = Color(0xFF4B5563)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 5 — Notes & Tags (static content, "Add" actions gated by edit mode)
// ─────────────────────────────────────────────────────────────

@Composable
private fun NotesTagsStep(isEditMode: Boolean) {
    Column {
        Text("Internal Notes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
        Text("Staff-only notes (not visible to customer)", fontSize = 11.sp, color = Color(0xFF9CA3AF))
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEF9C3), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text("High Value Customer. Prefers evening appointments.", fontSize = 13.sp, color = Color(0xFF713F12))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedIconActionButton(
            text = "Add Internal Notes",
            icon = Icons.Default.Add,
            onClick = {},
            enabled = isEditMode
        )

        Spacer(Modifier.height(20.dp))

        Text("Customer Notes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
        Text("Customer's own preferences and notes", fontSize = 11.sp, color = Color(0xFF9CA3AF))
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text("Prefers delivery on weekends only.", fontSize = 13.sp, color = Color(0xFF374151))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedIconActionButton(
            text = "Add Customer Notes",
            icon = Icons.Default.Add,
            onClick = {},
            enabled = isEditMode
        )

        Spacer(Modifier.height(20.dp))

        Text("Custom Tags", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
        Text("Organize customer with custom tags", fontSize = 11.sp, color = Color(0xFF9CA3AF))
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TagChip("Wedding", Color(0xFFEC4899))
            TagChip("Premium", Color(0xFF9333EA))
            TagChip("Bulk Orders", Color(0xFF3B82F6))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedIconActionButton(
            text = "Add Tag",
            icon = Icons.Default.Add,
            onClick = {},
            enabled = isEditMode
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable small pieces
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
                if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = Color(0xFF9CA3AF))
            }
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
 fun LabeledField(label: String, field: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        field()
    }
}

@Composable
private fun StatBox(modifier: Modifier = Modifier, value: String, label: String, highlight: Boolean = false, valueColor: Color = Color(0xFF111827)) {
    Column(
        modifier = modifier
            .background(
                if (highlight) Color(0xFFEEF2FF) else Color.White,
                RoundedCornerShape(10.dp)
            )
            .padding(14.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF6B7280))
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFEEF2FF), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color(0xFF3B3BF9))
    }
}

@Composable
private fun TagChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = color)
    }
}

@Composable
private fun InfoPill(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, sub: String, color: Color) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        Text(sub, fontSize = 10.sp, color = Color(0xFF6B7280))
    }
}

//@Composable
//private fun ExpandableRow(title: String, subtitle: String? = null) {
//    var expanded by remember { mutableStateOf(false) }
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(Color.White, RoundedCornerShape(10.dp))
//            .clickable { expanded = !expanded }
//            .padding(14.dp)
//    ) {
//        Row(
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Column {
//                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827))
//                if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = Color(0xFF9CA3AF))
//            }
//            Icon(
//                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                null,
//                tint = Color(0xFF6B7280)
//            )
//        }
//    }
//}