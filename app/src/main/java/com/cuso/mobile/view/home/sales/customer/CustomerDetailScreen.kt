@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter", "SameParameterValue"
)
package com.cuso.mobile.view.home.sales.customer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorMapper
import com.cuso.mobile.view.composable.PhoneInputField
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.dashedBorder
import com.cuso.mobile.view.home.toIsoDate
import com.cuso.mobile.view.organization.OrgOptions
import com.cuso.mobile.view.organization.OrganizationDropdown
import com.cuso.mobile.viewmodel.CustomerDetailUiState
import com.cuso.mobile.viewmodel.CustomerUpdateState
import com.cuso.mobile.viewmodel.CustomerViewModel
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.ui.theme.light_blue_border
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.primary_light
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.ui.theme.yellowBg
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import kotlinx.coroutines.delay

private val stepLabels = listOf(
    "Personal \nInformation",
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
    val tokens = LocalAppTokens.current

    val detailState by viewModel.detailState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var currentStep by remember { mutableIntStateOf(0) }
    var isEditMode by remember(startInEditMode) { mutableStateOf(startInEditMode) }

    var apiErrorMessage by remember { mutableStateOf<String?>(null) }
    var apiSuccessMessage by remember { mutableStateOf<String?>(null) }
    var errorField by remember { mutableStateOf<String?>(null) }
    var errorSection by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }

    // Disable all inputs and button actions while updating or while showing success notification
    val isInteractionDisabled = updateState is CustomerUpdateState.Loading || apiSuccessMessage != null
    val isFieldEditable = isEditMode && !isInteractionDisabled

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
                    !formState.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> {
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
                apiSuccessMessage = "Customer updated successfully"
                isEditMode = false
                errorField = null
                errorSection = null

                // 1.5 seconds delay before navigating away
                delay(1500)

                viewModel.resetUpdateState()
                onUpdateSuccess()
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TitleBar("Create customer", onClose = { if (!isInteractionDisabled) onClose() })
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Transparent)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    Spacer(Modifier.padding(top = 10.dp))
                    OrderStatusStepper(
                        stepLabels = stepLabels,
                        currentStep = currentStep
                    )
                }

                HorizontalDivider(
                    color = Color(0xFFF0F0F0),
                    modifier = Modifier.padding(top = 10.dp)
                )

                // Body Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = tokens.screenPadding)
                        .padding(bottom = 90.dp)
                ) {
                    when (currentStep) {
                        0 -> PersonalInformationStep(
                            detailState = detailState,
                            formState = formState,
                            viewModel = viewModel,
                            isEditMode = isFieldEditable,
                            errorField = errorField,
                            errorSection = errorSection,
                            email = email,
                            onEmailChange = { email = it }
                        )
                        1 -> MeasurementsStep(isEditMode = isFieldEditable)
                        2 -> OrderPaymentStep()
                        3 -> PreferencesStep()
                        4 -> NotesTagsStep(isEditMode = isFieldEditable)
                    }
                }
            }

            StepNavigationFab(
                showBack = currentStep > 0 && !isInteractionDisabled,
                onBack = { if (!isInteractionDisabled) currentStep-- },
                backEnabled = !isInteractionDisabled,
                trailingAction = when {
                    isInteractionDisabled -> TrailingFabAction.Update(
                        onClick = {},
                        isLoading = updateState is CustomerUpdateState.Loading,
                        enabled = false
                    )
                    currentStep < stepLabels.lastIndex -> TrailingFabAction.Next {
                        if (validateStep(currentStep)) {
                            currentStep++
                        }
                    }
                    !isEditMode -> TrailingFabAction.Edit {
                        isEditMode = true
                        currentStep = 0
                        onRequestEdit()
                    }
                    else -> TrailingFabAction.Update(
                        onClick = {
                            if (validateStep(0)) {
                                viewModel.updateCustomer(customerId)
                            }
                        },
                        isLoading = false,
                        enabled = true
                    )
                }
            )
        }

        // Dynamic Island Overlays
        DynamicIslandSuccess(
            message = apiSuccessMessage,
            onDismiss = { apiSuccessMessage = null },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
        )

        DynamicIslandError(
            message = apiErrorMessage,
            onDismiss = { apiErrorMessage = null },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// STEPPER COMPOSABLE WITH PERFECT SINGLE-LINE CENTER ALIGNMENT
// ─────────────────────────────────────────────────────────────

@Composable
fun OrderStatusStepper(
    stepLabels: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepLabels.forEachIndexed { index, _ ->
                val done = index < currentStep
                val active = index == currentStep

                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (active) {
                        val haloScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                            label = "haloScale"
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    scaleX = haloScale
                                    scaleY = haloScale
                                    alpha = haloScale
                                }
                                .background(Color(0xFFECEBFF), CircleShape)
                        )
                    }

                    val circleColor by animateColorAsState(
                        targetValue = when {
                            done -> Color(0xFF22C55E)
                            active -> Color(0xFF3F37F3)
                            else -> whiteBg
                        },
                        animationSpec = tween(durationMillis = 300),
                        label = "circleColor"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = when {
                            done -> Color(0xFF22C55E)
                            active -> Color(0xFF3F37F3)
                            else -> Color(0xFFE5E7EB)
                        },
                        animationSpec = tween(durationMillis = 300),
                        label = "borderColor"
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(circleColor, CircleShape)
                            .border(
                                width = if (active || done) 0.dp else 1.5.dp,
                                color = borderColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            done -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(14.dp)
                            )
                            active -> Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(whiteBg, CircleShape)
                            )
                            else -> Text(
                                text = "${index + 1}",
                                color = Color(0xFF9CA3AF),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (index < stepLabels.lastIndex) {
                    val lineColor by animateColorAsState(
                        targetValue = if (index < currentStep) Color(0xFF22C55E) else Color(0xFFE5E7EB),
                        animationSpec = tween(durationMillis = 300),
                        label = "lineColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(lineColor, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
            verticalAlignment = Alignment.Top
        ) {
            stepLabels.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier.size(width = 36.dp, height = 34.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (index == currentStep) {
                        Text(
                            text = label,
                            fontSize = tokens.caption,
                            lineHeight = tokens.caption * 1.25f,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3F37F3),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            softWrap = true,
                            modifier = Modifier.wrapContentWidth(unbounded = true)
                        )
                    }
                }

                if (index < stepLabels.lastIndex) {
                    Spacer(modifier = Modifier.weight(1f))
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
    val tokens = LocalAppTokens.current

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
                Text(detailState.message, color = Color.Red, fontSize = tokens.bodyMedium)
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

            Column(
                Modifier.fillMaxSize()
                    .background(Color.Transparent)
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding * 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(light_blue_border, RoundedCornerShape(12.dp))
                            .padding(horizontal = tokens.screenPadding * 0.6f, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isEditMode) "Edit mode — update the details below."
                            else "Viewing customer details. Tap Edit to make changes.",
                            fontSize = tokens.bodySmall,
                            color = Color(0xFF1E40AF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_person),
                    title = "Customer Identity",
                    subtitle = "Basic customer information",
                    expanded = expandedSection == "identity",
                    onHeaderClick = { expandedSection = if (expandedSection == "identity") "" else "identity" }
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
                        value = formState.name,
                        onValueChange = viewModel::onNameChange,
                        placeholder = "Enter Your Name",
                        enabled = isEditMode,
                        isError = errorField == "name",
                        errorMessage = if (errorField == "name") "Please Check the name " else null
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

                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_date_of_birth),
                    title = "Customer Details",
                    subtitle = "Communication Preferences",
                    expanded = expandedSection == "details",
                    onHeaderClick = { expandedSection = if (expandedSection == "details") "" else "details" }
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
                        value = formState.email,
                        onValueChange = viewModel::onEmailChange,
                        placeholder = "Enter Your email",
                        enabled = isEditMode,
                        isError = errorField == "email",
                        errorMessage = if (errorField == "email") "Please Check the email " else null
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
                    Text("Preferred Language", color = Color(0xFF9CA3AF), fontSize = tokens.bodySmall)
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

                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_location),
                    title = "Location & Communication",
                    subtitle = "Contact details",
                    expanded = expandedSection == "location",
                    onHeaderClick = { expandedSection = if (expandedSection == "location") "" else "location" }
                ) {
                    Spacer(Modifier.height(16.dp))
                    FormLabel("Address")
                    FormTextField(
                        value = formState.addressLine,
                        onValueChange = viewModel::onAddressLineChange,
                        placeholder = "Enter Your address",
                        enabled = isEditMode,
                        isError = errorField == "address",
                        errorMessage = if (errorField == "address") "Please Check the address " else null
                    )
                    Spacer(Modifier.height(12.dp))

                    FormLabel("Area / Zone")
                    FormTextField(
                        value = formState.area,
                        onValueChange = viewModel::onAreaChange,
                        placeholder = "Enter Area/Zone",
                        enabled = isEditMode,
                        isError = errorField == "areaZone",
                        errorMessage = if (errorField == "areaZone") "Please Check this field " else null
                    )
                    Spacer(Modifier.height(12.dp))

                    FormLabel("City")
                    FormTextField(
                        value = formState.city,
                        onValueChange = viewModel::onCityChange,
                        placeholder = "Enter Your City",
                        enabled = isEditMode,
                        isError = errorField == "city",
                        errorMessage = if (errorField == "city") "Please Check the city " else null
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 2 — Measurements Step
// ─────────────────────────────────────────────────────────────
@Composable
private fun MeasurementsStep(isEditMode: Boolean) {
    val tokens = LocalAppTokens.current
    var expandedSection by remember { mutableStateOf("profile") }

    Column {
        AccordionSection(
            title = "Measurement Profile",
            subtitle = "Linked measurement records (read-only)",
            expanded = expandedSection == "profile",
            onHeaderClick = { expandedSection = if (expandedSection == "profile") "" else "profile" }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox(modifier = Modifier.weight(1f), value = "3", label = "ACTIVE ORDERS", highlight = true)
                StatBox(modifier = Modifier.weight(1f), value = "4 Types", label = "GARMENT TYPES")
            }
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = tokens.screenPadding * 0.6f, vertical = 10.dp)
            ) {
                Text("LAST UPDATED", fontSize = tokens.bodySmall, color = Color(0xFF9CA3AF))
                Text("15/12/2026", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = blackTitle)
            }
            Spacer(Modifier.height(14.dp))
            Text("GARMENT TYPES COVERED", fontSize = tokens.caption, color = blackTitle)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Shirt", "Pant", "Suit", "Kurta").forEach { Chip(it) }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = {},
                enabled = isEditMode,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B3BF9))
            ) {
                Icon(Icons.Default.Add, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add New Measurement", color = Color(0xFF3B3BF9), fontSize = tokens.bodyMedium)
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

        AccordionSection(
            title = "Measurement Insights",
            expanded = expandedSection == "insights",
            onHeaderClick = { expandedSection = if (expandedSection == "insights") "" else "insights" }
        ) {
            InsightRow(label = "Total Alterations", value = "12")
            Spacer(Modifier.height(12.dp))
            InsightRow(label = "Frequency") {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(20.dp))
                        .padding(horizontal = tokens.screenPadding * 0.75f, vertical = 4.dp)
                ) {
                    Text("MEDIUM", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                }
            }
            Spacer(Modifier.height(12.dp))
            InsightRow(label = "Rework Flag") {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(20.dp))
                        .padding(horizontal = tokens.screenPadding * 0.75f, vertical = 4.dp)
                ) {
                    Text("NO", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        AccordionSection(
            title = "Measurement Profile",
            expanded = expandedSection == "notes",
            onHeaderClick = { expandedSection = if (expandedSection == "notes") "" else "notes" }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF9C3), RoundedCornerShape(12.dp))
                    .padding(horizontal = tokens.screenPadding * 0.6f, vertical = 12.dp)
            ) {
                Text("Prefers Slightly Loose Fitting", fontSize = tokens.bodyMedium, color = Color(0xFF713F12))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 3 — Order & Payment Step
// ─────────────────────────────────────────────────────────────
@Composable
private fun OrderPaymentStep() {
    val tokens = LocalAppTokens.current
    var expandedSection by remember { mutableStateOf("") }

    Column {
        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OrderStatBox(modifier = Modifier.weight(1f), label = "Total Orders", value = "28")
                OrderStatBox(
                    modifier = Modifier.weight(1f),
                    label = "First Order",
                    value = "Mar 2022"
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OrderStatBox(
                    modifier = Modifier.weight(1f),
                    label = "Last orders",
                    value = "Jan 2026"
                )
                OrderStatBox(
                    modifier = Modifier.weight(1f),
                    label = "Avg. order value",
                    value = "15.6K",
                    highlight = true
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        AccordionSection(
            title = "Payment Overview",
            subtitle = "",
            expanded = expandedSection == "payment",
            onHeaderClick = { expandedSection = if (expandedSection == "payment") "" else "payment" }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(greenBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = tokens.screenPadding * 0.85f, vertical = 14.dp)
            ) {
                Column {
                    Text("Total Spend", fontSize = tokens.bodySmall, color = greentext)
                    Spacer(Modifier.height(4.dp))
                    Text("436,800", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = greentext)
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                    .padding(horizontal = tokens.screenPadding * 0.85f, vertical = 14.dp)
            ) {
                Column {
                    Text("Pending Payment", fontSize = tokens.bodySmall, color = Color(0xFFDC2626))
                    Spacer(Modifier.height(4.dp))
                    Text("8,500", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        AccordionSection(
            title = "Order History",
            subtitle = "Complete order timeline",
            expanded = expandedSection == "history",
            onHeaderClick = { expandedSection = if (expandedSection == "history") "" else "history" }
        ) {
            Text("Frequently Ordered Garments", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
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
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    val weightOrderId = 0.9f
    val weightDate = 1.1f
    val weightGarment = 1.6f
    val weightAmount = 0.9f
    val weightStatus = 1.1f

    val minOrderId = 80.dp
    val minDate = 95.dp
    val minGarment = 110.dp
    val minAmount = 75.dp
    val minStatus = 85.dp
    val minContentWidth = minOrderId + minDate + minGarment + minAmount + minStatus

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tableWidth = maxOf(minContentWidth, this.maxWidth)
        val needsScroll = this.maxWidth < minContentWidth

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (needsScroll) Modifier.horizontalScroll(scrollState) else Modifier)
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .width(tableWidth)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                    .padding(horizontal = tokens.screenPadding * 0.75f, vertical = 14.dp)
            ) {
                Text("Order ID", fontSize = tokens.bodyMedium, color = Color(0xFF374151),
                    modifier = Modifier.weight(weightOrderId).widthIn(min = minOrderId))
                Text("Date", fontSize = tokens.bodyMedium, color = Color(0xFF374151),
                    modifier = Modifier.weight(weightDate).widthIn(min = minDate))
                Text("Garment", fontSize = tokens.bodyMedium, color = Color(0xFF374151),
                    modifier = Modifier.weight(weightGarment).widthIn(min = minGarment))
                Text("Amount", fontSize = tokens.bodyMedium, color = Color(0xFF374151),
                    modifier = Modifier.weight(weightAmount).widthIn(min = minAmount))
                Text("Status", fontSize = tokens.bodyMedium, color = Color(0xFF374151),
                    modifier = Modifier.weight(weightStatus).widthIn(min = minStatus))
            }

            // Data rows
            orders.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .width(tableWidth)
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(row.orderId, fontSize = tokens.bodyMedium, color = Color(0xFF111827),
                        modifier = Modifier.weight(weightOrderId).widthIn(min = minOrderId))
                    Text(row.date, fontSize = tokens.bodySmall, color = Color(0xFF111827),
                        modifier = Modifier.weight(weightDate).widthIn(min = minDate))
                    Text(
                        row.garment, fontSize = tokens.bodyMedium, color = Color(0xFF111827),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(weightGarment).widthIn(min = minGarment)
                    )
                    Text(row.amount, fontSize = tokens.bodyMedium, color = Color(0xFF111827),
                        modifier = Modifier.weight(weightAmount).widthIn(min = minAmount))

                    Box(
                        modifier = Modifier
                            .weight(weightStatus)
                            .widthIn(min = minStatus)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEE2E2), RoundedCornerShape(50))
                                .padding(horizontal = tokens.screenPadding * 0.6f, vertical = 5.dp)
                        ) {
                            Text(
                                row.status,
                                fontSize = tokens.caption,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (index != orders.lastIndex) {
                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.width(tableWidth))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 4 — Preferences Step
// ─────────────────────────────────────────────────────────────
@Composable
private fun PreferencesStep() {
    val tokens = LocalAppTokens.current
    var expandedSection by remember { mutableStateOf("fabric") }

    Column {
        AccordionSection(
            title = "Fabric Preferences",
            subtitle = "Customer's preferred fabric types",
            expanded = expandedSection == "fabric",
            onHeaderClick = { expandedSection = if (expandedSection == "fabric") "" else "fabric" }
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

        AccordionSection(
            title = "Style Preferences",
            subtitle = "Customer's preferred styles",
            expanded = expandedSection == "style",
            onHeaderClick = { expandedSection = if (expandedSection == "style") "" else "style" }
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

        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                InfoPill(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    iconPainter = painterResource(R.drawable.ic_background_purple_star),
                    label = "VIP",
                    sub = "Loyalty Level",
                    labelColor = Color(0xFF9333EA),
                    subColor = Color(0xFF6B7280),
                    bgColor = Color(0xFFF5F0FF),
                    borderColor = Color(0xFFE9D5FF)
                )
                InfoPill(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    iconPainter = painterResource(R.drawable.ic_background_white_people),
                    label = "5",
                    sub = "Referrals",
                    labelColor = Color(0xFF111827),
                    subColor = Color(0xFF6B7280),
                    bgColor = Color(0xFFF9FAFB),
                    borderColor = Color(0xFFE5E7EB)
                )
                InfoPill(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
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
                    .background(primary_light, RoundedCornerShape(12.dp))
                    .padding(horizontal = tokens.screenPadding * 0.85f, vertical = 14.dp)
            ) {
                Column {
                    Text(
                        "Special Privileges",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B3BF9),
                        fontSize = tokens.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Priority booking, 10% discount on all suit orders, Free home delivery.",
                        fontSize = tokens.caption,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 5 — Notes & Tags Step
// ─────────────────────────────────────────────────────────────
@Composable
private fun NotesTagsStep(isEditMode: Boolean) {
    val tokens = LocalAppTokens.current

    Column(
        Modifier.fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
    ) {
        SectionHeader("Internal Notes", "Staff-only notes (not visible to customer)")
        Spacer(Modifier.height(10.dp))
        NoteCard(
            text = "High Value Customer. Prefers evening appointments.",
            bgColor = yellowBg,
            borderColor = yellowBg,
            textColor = title_color
        )
        Spacer(Modifier.height(10.dp))
        DashedAddButton(text = "Add Internal Notes", onClick = {}, enabled = isEditMode)

        Spacer(Modifier.height(24.dp))

        SectionHeader("Customer Notes", "Customer's own preferences and notes")
        Spacer(Modifier.height(10.dp))
        NoteCard(
            text = "Prefers delivery on weekends only.",
            bgColor = primary_light,
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

// ── Helpers ──

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    val tokens = LocalAppTokens.current
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = tokens.h2, color = Color(0xFF111827))
        Spacer(Modifier.height(2.dp))
        Text(subtitle, fontSize = tokens.bodySmall, color = Color(0xFF9CA3AF))
    }
}

@Composable
private fun NoteCard(text: String, bgColor: Color, borderColor: Color, textColor: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = tokens.screenPadding * 0.9f, vertical = 14.dp)
    ) {
        Text(text, fontSize = tokens.bodyMedium, color = textColor)
    }
}

@Composable
private fun DashedAddButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    val tokens = LocalAppTokens.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(color = Color(0xFFD1D5DB), strokeWidth = 1.dp, cornerRadius = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280)),
        border = null,
        contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = 14.dp)
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color(0xFF6B7280))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = tokens.bodyMedium, color = Color(0xFF6B7280))
    }
}

@Composable
private fun SolidAddButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    val tokens = LocalAppTokens.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.wrapContentWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF111827)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
        contentPadding = PaddingValues(horizontal = tokens.screenPadding * 0.85f, vertical = 10.dp)
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color(0xFF111827))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    }
}

@Composable
private fun TagChipOutlined(text: String, color: Color) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = tokens.screenPadding * 0.75f, vertical = 7.dp)
    ) {
        Icon(Icons.Outlined.LocalOffer, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun LabeledField(label: String, field: @Composable () -> Unit) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontSize = tokens.bodySmall, color = blackTitle, fontWeight = FontWeight.Medium)
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
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .background(
                if (highlight) primary_light else Color(0xFFFAFAFB),
                RoundedCornerShape(16.dp)
            )
            .then(
                if (!highlight) Modifier.border(1.dp, Color(0xFFECECF1), RoundedCornerShape(16.dp))
                else Modifier
            )
            .padding(vertical = 18.dp, horizontal = tokens.screenPadding * 0.6f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            value,
            fontSize = tokens.h2,
            fontWeight = FontWeight.Bold,
            color = if (highlight) Color(0xFF3B3BF9) else Color(0xFF0F172A)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = tokens.caption,
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
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .background(
                if (highlight) greenBg else lightGray,
                RoundedCornerShape(16.dp)
            )
            .padding(vertical = 16.dp, horizontal = tokens.screenPadding * 0.85f)
    ) {
        Text(label, fontSize = tokens.bodySmall, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontSize = tokens.h2,
            fontWeight = FontWeight.Bold,
            color = if (highlight) greentext else mutedText
        )
    }
}

@Composable
private fun Chip(text: String) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .background(Color(0xFFE1E0FF), RoundedCornerShape(8.dp))
            .padding(horizontal = tokens.screenPadding * 0.9f, vertical = 8.dp)
    ) {
        Text(text, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF07006C))
    }
}

@Composable
private fun TagChip(text: String, color: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = tokens.screenPadding * 0.75f, vertical = 6.dp)
    ) {
        Text(text, fontSize = tokens.bodySmall, color = color)
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
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(16.dp))
                else Modifier
            )
            .padding(vertical = 16.dp, horizontal = tokens.screenPadding * 0.6f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            iconPainter != null -> androidx.compose.foundation.Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            icon != null -> Icon(imageVector = icon, contentDescription = null, tint = labelColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = labelColor, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(sub, fontSize = tokens.caption, color = subColor, textAlign = TextAlign.Center)
    }
}

@Composable
private fun InsightRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, color = Color(0xFF374151))
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
    }
}

@Composable
private fun InsightRow(label: String, trailing: @Composable () -> Unit) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, color = Color(0xFF374151))
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
    val tokens = LocalAppTokens.current
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .dashedBorder(
                color = if (enabled) Color(0xFF9CA3AF) else Color(0xFFD1D5DB),
                strokeWidth = 1.dp,
                cornerRadius = 12.dp
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (enabled) Color(0xFF3B3BF9) else Color.Gray
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = tokens.bodyMedium,
                color = if (enabled) Color(0xFF374151) else Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
