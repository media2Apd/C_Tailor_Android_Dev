package com.cuso.mobile.view.home.sales.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.customFieldOutlinedColors
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.ErrorMapper
import com.cuso.mobile.view.composable.PhoneInputField
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.reusablecomposables.StepNavigationFab
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.view.home.reusablecomposables.dashedBorder
import com.cuso.mobile.view.home.toIsoDate
import com.cuso.mobile.view.organization.OrgOptions
import com.cuso.mobile.view.organization.OrganizationDropdown
import com.cuso.mobile.viewmodel.CustomerDetailUiState
import com.cuso.mobile.viewmodel.CustomerUpdateState
import com.cuso.mobile.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.view.composable.customOutlinedButtonColors
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField

private val stepLabels = listOf(
    "Personal Information",
    "Measurements",
    "Order & Payment",
    "Preferences",
    "Notes & Tags"
)

private val customerSectionFieldMap = mapOf(
    "identity" to listOf("name", "gender", "dob", "type"),
    "details" to listOf("email", "mobile", "status", "language", "contact"),
    "location" to listOf("address", "areaZone", "city")
)

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

    var apiErrorMessage by remember { mutableStateOf<String?>(null) }
    var errorField by remember { mutableStateOf<String?>(null) }
    var errorSection by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }

    fun validateStep(step: Int): Boolean {
        if (!isEditMode) return true
        return when (step) {
            0 -> {
                var valid = true
                when {
                    formState.name.isBlank() -> {
                        errorField = "name"; apiErrorMessage = "Full Name is required"; valid = false
                    }
                    formState.email.isBlank() -> {
                        errorField = "email"; apiErrorMessage = "Email address is required"; valid = false
                    }
                    !formState.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")) -> {
                        errorField = "email"; apiErrorMessage = "Enter a valid email address"; valid = false
                    }
                    formState.addressLine.isBlank() -> {
                        errorField = "address"; apiErrorMessage = "Address is required"; valid = false
                    }
                    formState.area.isBlank() -> {
                        errorField = "areaZone"; apiErrorMessage = "Area/Zone is required"; valid = false
                    }
                    formState.city.isBlank() -> {
                        errorField = "city"; apiErrorMessage = "City is required"; valid = false
                    }
                }
                if (!valid) {
                    errorSection = customerSectionFieldMap.entries
                        .firstOrNull { (_, fields) -> errorField in fields }?.key
                } else {
                    errorField = null
                }
                valid
            }
            else -> true
        }
    }

    LaunchedEffect(customerId) { viewModel.loadCustomerDetail(customerId) }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is CustomerUpdateState.Success -> {
                coroutineScope.launch { snackbarHostState.showSnackbar("Customer updated successfully") }
                isEditMode = false
                errorField = null
                errorSection = null
                onUpdateSuccess()
                viewModel.resetUpdateState()
            }
            is CustomerUpdateState.Error -> {
                viewModel.resetUpdateState()
                apiErrorMessage = ErrorMapper.map(state.message)
                errorField = ErrorMapper.fieldFor(state.message)
                errorSection = customerSectionFieldMap.entries
                    .firstOrNull { (_, fields) -> errorField in fields }?.key
                currentStep = 0
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
            DynamicIslandError(
                message = apiErrorMessage,
                onDismiss = { apiErrorMessage = null },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // ── Header ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Create customer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF111827),
                        modifier = Modifier.clickable { onClose() }
                    )
                }

                // ── Stepper (Single-line label & center alignment) ──
                OrderStatusStepper(
                    stepLabels = stepLabels,
                    currentStep = currentStep
                )

                HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(top = 10.dp))

                // ── Body Content ──
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 90.dp)
                ) {
                    when (currentStep) {
                        0 -> PersonalInformationStep(
                            detailState, formState, viewModel, isEditMode,
                            errorField = errorField,
                            errorSection = errorSection,
                            email = email,
                            onEmailChange = { email = it }
                        )
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
                    currentStep < stepLabels.lastIndex -> TrailingFabAction.Next {
                        if (validateStep(currentStep)) {
                            currentStep++
                        }
                    }
                    !isEditMode -> TrailingFabAction.Edit { onRequestEdit() }
                    else -> TrailingFabAction.Update(
                        onClick = {
                            if (validateStep(0)) {
                                viewModel.updateCustomer(customerId)
                            }
                        },
                        isLoading = updateState is CustomerUpdateState.Loading
                    )
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ✅ STEPPER COMPOSABLE WITH PERFECT SINGLE-LINE CENTER ALIGNMENT
// ─────────────────────────────────────────────────────────────
@Composable
fun OrderStatusStepper(
    stepLabels: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            stepLabels.forEachIndexed { index, label ->
                val done = index < currentStep
                val active = index == currentStep

                // Circle + Label Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                when {
                                    done -> Color(0xFF22C55E)
                                    else -> Color.White
                                },
                                CircleShape
                            )
                            .border(
                                width = if (active) 2.dp else 1.5.dp,
                                color = when {
                                    done -> Color(0xFF22C55E)
                                    active -> Color(0xFF3B3BF9)
                                    else -> Color(0xFFE5E7EB)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            done -> Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            active -> Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF3B3BF9), CircleShape)
                            )
                            else -> Text(
                                "${index + 1}",
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Step Label — Strictly Single Line & Centered
                    if (active) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3B3BF9),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.wrapContentWidth()
                        )
                    } else {
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }

                // Connector line between steps
                if (index != stepLabels.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 13.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        ) {
                            drawLine(
                                color = if (index < currentStep) Color(0xFF22C55E) else Color(0xFFE5E7EB),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = size.height,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 1 — Personal Information
// ─────────────────────────────────────────────────────────────
@Composable
private fun PersonalInformationStep(
    detailState: CustomerDetailUiState,
    formState: com.cuso.mobile.viewmodel.CustomerFormState,
    viewModel: CustomerViewModel,
    isEditMode: Boolean,
    errorField: String? = null,
    errorSection: String? = null,
    email: String,
    onEmailChange: (String) -> Unit
) {
    when (detailState) {
        is CustomerDetailUiState.Loading -> {
            Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                CirculerProgressIndicatorReuse()
            }
        }
        is CustomerDetailUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text(detailState.message, color = Color.Red, fontSize = 14.sp)
            }
        }
        is CustomerDetailUiState.Success -> {
            var expandedSection by remember { mutableStateOf("identity") }
            LaunchedEffect(errorSection) {
                if (errorSection != null) expandedSection = errorSection
            }

            var preferredContact by remember {
                mutableStateOf(detailState.customer.preferences?.contactMethod.orEmpty())
            }
            var language by remember {
                mutableStateOf(detailState.customer.preferences?.language.orEmpty())
            }

            LaunchedEffect(detailState.customer) {
                onEmailChange(detailState.customer.email.orEmpty())
                preferredContact = detailState.customer.preferences?.contactMethod.orEmpty()
                language = detailState.customer.preferences?.language.orEmpty()
            }

            var typeExpanded by remember { mutableStateOf(false) }
            var genderExpanded by remember { mutableStateOf(false) }
            var statusExpanded by remember { mutableStateOf(false) }
            var contactExpanded by remember { mutableStateOf(false) }

            Column {
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
                        if (isEditMode) "Edit mode — update the details below."
                        else "Viewing customer details. Tap Edit to make changes.",
                        fontSize = 12.sp,
                        color = Color(0xFF1E40AF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                AccordionSectionCard(
                    iconPainter = painterResource(R.drawable.ic_person),
                    title = "Customer Identity",
                    subtitle = "Basic customer information",
                    expanded = expandedSection == "identity",
                    onToggle = { expandedSection = if (expandedSection == "identity") "" else "identity" }
                ) {
                    Spacer(Modifier.height(16.dp))

                    FormDropdown(
                        label = "Customer Type",
                        value = formState.type.replaceFirstChar { it.uppercase() }.ifEmpty { "Select an option" },
                        expanded = typeExpanded,
                        onExpandChange = { typeExpanded = it },
                        options = listOf("Individual", "Business", "Regular"),
                        onOptionSelected = { label -> viewModel.onTypeChange(label.lowercase()) },
                        isRequired = true,
                        enabled = isEditMode
                    )
                    Spacer(Modifier.height(12.dp))
                    FormLabel("Full Name")
                    FormTextField(
                        value= formState.name,
                        onValueChange = viewModel::onNameChange,
                        placeholder = "Enter Your Name",
                        enabled = isEditMode,
                        isError = errorField == "name",
                        errorMessage = if (errorField=="name") "Please Check the name " else null
                    )
                    Spacer(Modifier.height(12.dp))

                    FormDropdown(
                        label = "Gender",
                        value = formState.gender.ifEmpty { "Select an option" },
                        expanded = genderExpanded,
                        onExpandChange = { genderExpanded = it },
                        options = listOf("Male", "Female", "Other"),
                        onOptionSelected = viewModel::onGenderChange,
                        enabled = isEditMode
                    )

                    Spacer(Modifier.height(12.dp))
                    FormLabel("Date of Birth")
                    DatePickerField(
                        value = formState.dob.toDisplayDate().takeIf { it != "—" } ?: "Select date",
                        onDateSelected = { selected -> viewModel.onDobChange(selected.toIsoDate()) },
                        enabled = isEditMode
                    )
                }

                Spacer(Modifier.height(12.dp))

                AccordionSectionCard(
                    iconPainter = painterResource(R.drawable.ic_date_of_birth),
                    title = "Customer Details",
                    subtitle = "Communication Preferences",
                    expanded = expandedSection == "details",
                    onToggle = { expandedSection = if (expandedSection == "details") "" else "details" }
                ) {
                    Spacer(Modifier.height(16.dp))

                    FormLabel("Mobile No")
                    PhoneInputField(
                        phoneValue = formState.mobile,
                        onPhoneChange = viewModel::onMobileChange,
                        onCountryChange = { },
                        enabled = isEditMode
                    )
                    Spacer(Modifier.height(12.dp))

                    FormLabel("Email")
                    FormTextField(
                        value= formState.email,
                        onValueChange = viewModel::onEmailChange,
                        placeholder = "Enter Your email",
                        enabled = isEditMode,
                        isError = errorField == "email",
                        errorMessage = if (errorField=="email") "Please Check the email " else null
                    )
                    Spacer(Modifier.height(12.dp))

                    FormDropdown(
                        label = "Status",
                        value = formState.status.ifEmpty { "Select an option" },
                        expanded = statusExpanded,
                        onExpandChange = { statusExpanded = it },
                        options = listOf("Active", "Inactive"),
                        onOptionSelected = viewModel::onStatusChange,
                        enabled = isEditMode
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Preferred Language", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                    OrganizationDropdown(
                        items = OrgOptions.languages,
                        selected = language,
                        enabled = isEditMode
                    ) { language = it }
                    Spacer(Modifier.height(12.dp))
                    FormDropdown(
                        label = "Preferred Contact",
                        value = preferredContact.ifEmpty { "Select an option" },
                        expanded = contactExpanded,
                        onExpandChange = { contactExpanded = it },
                        options = listOf("Call", "WhatsApp", "Email"),
                        onOptionSelected = { preferredContact = it },
                        enabled = isEditMode
                    )
                }

                Spacer(Modifier.height(12.dp))

                AccordionSectionCard(
                    iconPainter = painterResource(R.drawable.ic_location),
                    title = "Location & Communication",
                    subtitle = "Contact details",
                    expanded = expandedSection == "location",
                    onToggle = { expandedSection = if (expandedSection == "location") "" else "location" }
                ) {
                    Spacer(Modifier.height(16.dp))
                    FormLabel("Address")
                    FormTextField(
                        value= formState.addressLine,
                        onValueChange = viewModel::onAddressLineChange,
                        placeholder = "Enter Your address",
                        enabled = isEditMode,
                        isError = errorField == "address",
                        errorMessage = if (errorField=="address") "Please Check the address " else null
                    )
                    Spacer(Modifier.height(12.dp))

                    FormLabel("Area / Zone")
                    FormTextField(
                        value= formState.area,
                        onValueChange = viewModel::onAreaChange,
                        placeholder = "Enter Area/Zone",
                        enabled = isEditMode,
                        isError = errorField == "areaZone",
                        errorMessage = if (errorField=="areaZone") "Please Check this field " else null
                    )
                    Spacer(Modifier.height(12.dp))

                    FormLabel("City")
                    FormTextField(
                        value= formState.city,
                        onValueChange = viewModel::onCityChange,
                        placeholder = "Enter Your City",
                        enabled = isEditMode,
                        isError = errorField == "city",
                        errorMessage = if (errorField=="city") "Please Check the city " else null
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Accordion Section Helper Card
// ─────────────────────────────────────────────────────────────
@Composable
 fun AccordionSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    iconTint: Color = Color(0xFF3B3BF9),
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val chevronRotation by androidx.compose.animation.core.animateFloatAsState(
        if (expanded) 180f else 0f, label = "accordion_chevron"
    )

    val hasIcon = icon != null || iconPainter != null

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasIcon) {
                when {
                    iconPainter != null -> Icon(painter = iconPainter, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                    icon != null -> Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(15.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, fontWeight = FontWeight.Normal, fontSize = 15.sp, color = Color(0xFF0F172A))
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(22.dp).rotate(chevronRotation)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(horizontal = 1.dp, vertical = 4.dp).padding(bottom = 14.dp)) {
                content()
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFFE5E7EB)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 2 — Measurements Step (Matching Screenshots)
// ─────────────────────────────────────────────────────────────
@Composable
private fun MeasurementsStep(isEditMode: Boolean) {
    var expandedSection by remember { mutableStateOf("profile") }

    Column {
        AccordionSectionCard(
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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, PrimaryBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text("LAST UPDATED", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                Text("15/12/2026", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }
            Spacer(Modifier.height(14.dp))
            Text("GARMENT TYPES COVERED", fontSize = 11.sp, color = Color.Black)
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
                enabled = true
            )
        }

        Spacer(Modifier.height(12.dp))

        AccordionSectionCard(
            title = "Measurement Insights",
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

        AccordionSectionCard(
            title = "Measurement Profile",
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
// STEP 3 — Order & Payment Step (Matching Screenshots)
// ─────────────────────────────────────────────────────────────
@Composable
private fun OrderPaymentStep() {
    var expandedSection by remember { mutableStateOf("") }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrderStatBox(modifier = Modifier.weight(1f), label = "Total Orders", value = "28")
            OrderStatBox(modifier = Modifier.weight(1f), label = "First Order", value = "Mar 2022")
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrderStatBox(modifier = Modifier.weight(1f), label = "Last orders", value = "Jan 2026")
            OrderStatBox(modifier = Modifier.weight(1f), label = "Avg. order value", value = "15.6K", highlight = true)
        }
        Spacer(Modifier.height(16.dp))

        AccordionSectionCard(
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

        AccordionSectionCard(
            title = "Order History",
            subtitle = "Complete order timeline",
            expanded = expandedSection == "history",
            onToggle = { expandedSection = if (expandedSection == "history") "" else "history" }
        ) {
            Text("Frequently Ordered Garments", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
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
    val scrollState = rememberScrollState() // header + rows share same scroll position

    // 👇 fixed column widths (weight() vela seiyadhu horizontal scroll la)
    val colOrderId = 90.dp
    val colDate = 110.dp
    val colGarment = 140.dp
    val colAmount = 90.dp
    val colStatus = 90.dp
    val totalWidth = colOrderId + colDate + colGarment + colAmount + colStatus

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .width(totalWidth)
                .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text("Order ID", fontSize = 14.sp, color = Color(0xFF374151), modifier = Modifier.width(colOrderId))
            Text("Date", fontSize = 14.sp, color = Color(0xFF374151), modifier = Modifier.width(colDate))
            Text("Garment", fontSize = 14.sp, color = Color(0xFF374151), modifier = Modifier.width(colGarment))
            Text("Amount", fontSize = 14.sp, color = Color(0xFF374151), modifier = Modifier.width(colAmount))
            Text("Status", fontSize = 14.sp, color = Color(0xFF374151), modifier = Modifier.width(colStatus))
        }

        // ── Rows ──
        orders.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .width(totalWidth)
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.orderId, fontSize = 15.sp, color = Color(0xFF111827), modifier = Modifier.width(colOrderId))
                Text(row.date, fontSize = 14.sp, color = Color(0xFF111827), modifier = Modifier.width(colDate))
                Text(
                    row.garment, fontSize = 15.sp, color = Color(0xFF111827),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(colGarment)
                )
                Text(row.amount, fontSize = 15.sp, color = Color(0xFF111827), modifier = Modifier.width(colAmount))
                Box(
                    modifier = Modifier
                        .width(colStatus)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(row.status, fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (index != orders.lastIndex) {
                HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.width(totalWidth))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 4 — Preferences Step (Matching Screenshots)
// ─────────────────────────────────────────────────────────────
@Composable
private fun PreferencesStep() {
    var expandedSection by remember { mutableStateOf("fabric") }

    Column {
        AccordionSectionCard(
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

        AccordionSectionCard(
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(IntrinsicSize.Min)   // 👈 idha add pannunga
        ) {
            InfoPill(
                modifier = Modifier.weight(1f).fillMaxHeight(),   // 👈 fillMaxHeight add pannunga
                iconPainter = painterResource(R.drawable.ic_background_purple_star),
                label = "VIP",
                sub = "Loyalty Level",
                labelColor = Color(0xFF9333EA),
                subColor = Color(0xFF6B7280),
                bgColor = Color(0xFFF5F0FF),
                borderColor = Color(0xFFE9D5FF)
            )
            InfoPill(
                modifier = Modifier.weight(1f).fillMaxHeight(),   // 👈 idhulayum
                iconPainter = painterResource(R.drawable.ic_background_white_people),
                label = "5",
                sub = "Referrals",
                labelColor = Color(0xFF111827),
                subColor = Color(0xFF6B7280),
                bgColor = Color(0xFFF9FAFB),
                borderColor = Color(0xFFE5E7EB)
            )
            InfoPill(
                modifier = Modifier.weight(1f).fillMaxHeight(),   // 👈 idhulayum
                iconPainter = painterResource(R.drawable.ic_background_green_check),
                label = "Upgrade Ready",
                sub = "Next tier eligible",
                labelColor = Color(0xFF16A34A),
                subColor = Color(0xFF15803D),
                bgColor = Color(0xFFDCFCE7),
                borderColor = null
            )
        }
        Spacer(Modifier.height(16.dp))

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
// STEP 5 — Notes & Tags Step
// ─────────────────────────────────────────────────────────────
@Composable
private fun NotesTagsStep(isEditMode: Boolean) {
    Column {
        SectionHeader("Internal Notes", "Staff-only notes (not visible to customer)")
        Spacer(Modifier.height(10.dp))
        NoteCard(
            text = "High Value Customer. Prefers evening appointments.",
            bgColor = Color(0xFFFEFCE8),
            borderColor = Color(0xFFFDE68A),
            textColor = Color(0xFF1F2937)
        )
        Spacer(Modifier.height(10.dp))
        DashedAddButton(text = "Add Internal Notes", onClick = {}, enabled = isEditMode)

        Spacer(Modifier.height(24.dp))

        SectionHeader("Customer Notes", "Customer's own preferences and notes")
        Spacer(Modifier.height(10.dp))
        NoteCard(
            text = "Prefers delivery on weekends only.",
            bgColor = Color(0xFFEEF2FF),
            borderColor = Color(0xFFC7D2FE),
            textColor = Color(0xFF1F2937)
        )
        Spacer(Modifier.height(10.dp))
        DashedAddButton(text = "Add Customer Notes", onClick = {}, enabled = isEditMode)

        Spacer(Modifier.height(24.dp))

        SectionHeader("Custom Tags", "Organize customer with custom tags")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TagChipOutlined("Wedding", Color(0xFFEC4899))
            TagChipOutlined("Premium", Color(0xFF9333EA))
            TagChipOutlined("Bulk Orders", Color(0xFF3B82F6))
        }
        Spacer(Modifier.height(10.dp))
        SolidAddButton(text = "Add Tag", onClick = {}, enabled = isEditMode)
    }
}

// ── Section title + subtitle ──
@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF111827))
        Spacer(Modifier.height(2.dp))
        Text(subtitle, fontSize = 13.sp, color = Color(0xFF9CA3AF))
    }
}

// ── Bordered note box (yellow / indigo) ──
@Composable
private fun NoteCard(text: String, bgColor: Color, borderColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(text, fontSize = 14.sp, color = textColor)
    }
}

// ── Dashed "+ Add ..." button (gray, for notes) ──
@Composable
private fun DashedAddButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(color = Color(0xFFD1D5DB), strokeWidth = 1.dp, cornerRadius = 10.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280)),
        border = null,
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color(0xFF6B7280))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 14.sp, color = Color(0xFF6B7280))
    }
}

// ── Solid-border "+ Add Tag" button (black text) ──
// ── Solid-border "+ Add Tag" button (compact, not full width) ──
@Composable
private fun SolidAddButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.wrapContentWidth(),   // 👈 fillMaxWidth remove pannitten
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF111827)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)  // 👈 chinna padding
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color(0xFF111827))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    }
}
// ── Tag chip with icon + pastel bg + colored border ──
@Composable
private fun TagChipOutlined(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(1.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(1.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Icon(Icons.Outlined.LocalOffer, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable Component Helpers
// ─────────────────────────────────────────────────────────────
@Composable
fun LabeledField(label: String, field: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        field()
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    highlight: Boolean = false,
    valueColor: Color = Color(0xFF111827)
) {
    Column(
        modifier = modifier
            .background(
                if (highlight) Color(0xFFEEF2FF) else Color(0xFFFAFAFB),
                RoundedCornerShape(14.dp)
            )
            .then(
                if (!highlight) Modifier.border(1.dp, Color(0xFFECECF1), RoundedCornerShape(14.dp))
                else Modifier
            )
            .padding(vertical = 18.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) Color(0xFF3B3BF9) else Color(0xFF0F172A)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = if (highlight) Color(0xFF6366F1) else Color(0xFF9CA3AF)
        )
    }
}

@Composable
private fun OrderStatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column(
        modifier = modifier
            .background(
                if (highlight) Color(0xFFDCFCE7) else Color(0xFFF3F4F6),
                RoundedCornerShape(14.dp)
            )
            .padding(vertical = 16.dp, horizontal = 14.dp)
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF6B7280))
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) Color(0xFF16A34A) else Color(0xFF111827)
        )
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE1E0FF), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF07006C))
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
private fun InfoPill(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    label: String,
    sub: String,
    labelColor: Color,
    subColor: Color = Color(0xFF6B7280),
    bgColor: Color,
    borderColor: Color? = null
) {
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(14.dp))
                else Modifier
            )
            .padding(vertical = 16.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            // 👇 Image use pannunga, Icon illa - idhu tint apply pannadhu,
            // drawable-oda own multi-colors (circle bg + icon) preserve aagum
            iconPainter != null -> androidx.compose.foundation.Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            icon != null -> Icon(imageVector = icon, contentDescription = null, tint = labelColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = labelColor, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(sub, fontSize = 11.sp, color = subColor, textAlign = TextAlign.Center)
    }
}

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
                color = Color.Gray,
                strokeWidth = 1.dp,
                cornerRadius = 8.dp
            ),
        shape = RoundedCornerShape(8.dp),
        colors = customOutlinedButtonColors(),
        border = null
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text)
    }
}