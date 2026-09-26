@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter", "SameParameterValue"
)
package com.cuso.tailor.view.home.sales.customer

import android.annotation.SuppressLint
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
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.toIsoDate
import com.cuso.tailor.viewmodel.CustomerCreateState
import com.cuso.tailor.viewmodel.CustomerDetailUiState
import com.cuso.tailor.viewmodel.CustomerUpdateState
import com.cuso.tailor.viewmodel.CustomerViewModel
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
    "details" to listOf("email", "mobile", "status", "contact"),
    "location" to listOf("address", "areaZone", "city")
)

@Composable
fun CustomerDetailScreen(
    navController: NavController,
    customerId: String = "",
    isCreateMode: Boolean = false,
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
    val createState by viewModel.createState.collectAsState()

    var currentStep by remember { mutableIntStateOf(0) }
    var isEditMode by remember(startInEditMode) { mutableStateOf(startInEditMode || isCreateMode) }

    var apiErrorMessage by remember { mutableStateOf<String?>(null) }
    var apiSuccessMessage by remember { mutableStateOf<String?>(null) }
    var errorField by remember { mutableStateOf<String?>(null) }
    var errorSection by remember { mutableStateOf<String?>(null) }

    val isInteractionDisabled = updateState is CustomerUpdateState.Loading ||
            createState is CustomerCreateState.Loading ||
            apiSuccessMessage != null
    val isFieldEditable = (isEditMode || isCreateMode) && !isInteractionDisabled

    fun validateStep(step: Int): Boolean {
        if (!isFieldEditable) return true
        return when (step) {
            0 -> {
                var valid = true
                when {
                    formState.name.isBlank() -> {
                        errorField = "name"; apiErrorMessage = "Full Name is required"; valid = false
                    }
                    formState.email.isNotBlank() && !formState.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> {
                        errorField = "email"; apiErrorMessage = "Enter a valid email address"; valid = false
                    }
                    formState.mobile.isBlank() -> {
                        errorField = "mobile"; apiErrorMessage = "Mobile Number is required"; valid = false
                    }
                }
                if (!valid) {
                    errorSection = customerSectionFieldMap.entries
                        .firstOrNull { (_, fields) -> errorField in fields }?.key
                } else {
                    errorField = null
                    errorSection = null
                }
                valid
            }
            else -> true
        }
    }

    LaunchedEffect(customerId, isCreateMode) {
        if (!isCreateMode && customerId.isNotBlank()) {
            viewModel.loadCustomerDetail(customerId)
        } else if (isCreateMode) {
            viewModel.resetFormForNewCustomer()
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is CustomerUpdateState.Success -> {
                apiSuccessMessage = "Customer updated successfully"
                isEditMode = false
                errorField = null
                errorSection = null
                delay(1200)
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

    LaunchedEffect(createState) {
        when (val state = createState) {
            is CustomerCreateState.Success -> {
                apiSuccessMessage = "Customer created successfully"
                delay(1200)
                viewModel.resetCreateState()
                onUpdateSuccess()
            }
            is CustomerCreateState.Error -> {
                viewModel.resetCreateState()
                apiErrorMessage = ErrorMapper.map(state.message)
                currentStep = 0
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val title = when {
                        isCreateMode -> "Create Customer"
                        isEditMode -> "Edit Customer"
                        else -> "View Customer"
                    }
                    TitleBar(title, onClose = { if (!isInteractionDisabled) onClose() })
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
                    color = title_border,
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
                            customerId = customerId,
                            isCreateMode = isCreateMode,
                            detailState = detailState,
                            formState = formState,
                            viewModel = viewModel,
                            isEditMode = isFieldEditable,
                            errorField = errorField,
                            errorSection = errorSection
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
                        label = if (isCreateMode) "Create" else "Update",
                        onClick = {},
                        isLoading = updateState is CustomerUpdateState.Loading || createState is CustomerCreateState.Loading,
                        enabled = false
                    )
                    currentStep < stepLabels.lastIndex -> TrailingFabAction.Next {
                        if (validateStep(currentStep)) {
                            currentStep++
                        }
                    }
                    !isEditMode && !isCreateMode -> TrailingFabAction.Edit {
                        isEditMode = true
                        currentStep = 0
                        onRequestEdit()
                    }
                    else -> TrailingFabAction.Update(
                        label = if (isCreateMode) "Create" else "Update",
                        onClick = {
                            if (validateStep(0)) {
                                if (isCreateMode) {
                                    viewModel.createCustomer()
                                } else {
                                    viewModel.updateCustomer(customerId)
                                }
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
            modifier = Modifier.align(Alignment.TopCenter).zIndex(10f)
        )

        DynamicIslandError(
            message = apiErrorMessage,
            onDismiss = { apiErrorMessage = null },
            modifier = Modifier.align(Alignment.TopCenter).zIndex(10f)
        )
    }
}

@Composable
private fun PersonalInformationStep(
    customerId: String,
    isCreateMode: Boolean,
    detailState: CustomerDetailUiState,
    formState: com.cuso.tailor.viewmodel.CustomerFormState,
    viewModel: CustomerViewModel,
    isEditMode: Boolean,
    errorField: String? = null,
    errorSection: String? = null
) {
    val tokens = LocalAppTokens.current

    if (!isCreateMode && detailState is CustomerDetailUiState.Loading) {
        Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
            CirculerProgressIndicatorReuse()
        }
        return
    }

    if (!isCreateMode && detailState is CustomerDetailUiState.Error) {
        AppErrorState(
            title = "Failed to load customer profile",
            message = detailState.message,
            onRetry = { viewModel.loadCustomerDetail(customerId) }
        )
        return
    }

    var expandedSection by remember { mutableStateOf("identity") }
    LaunchedEffect(errorSection) {
        if (errorSection != null) expandedSection = errorSection
    }

    var typeExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var contactExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding * 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                    .padding(horizontal = tokens.screenPadding * 0.6f, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when {
                        isCreateMode -> "Create mode — enter new customer information."
                        isEditMode -> "Edit mode — modify customer details below."
                        else -> "Viewing customer profile (Read-only). Tap Edit to make changes."
                    },
                    fontSize = tokens.bodySmall,
                    color = BluePrimary,
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
            FormLabel("Customer Type", isRequired = true)
            FormDropdown(
                value = formState.type.replaceFirstChar { it.uppercase() }.ifEmpty { "Select an option" },
                expanded = typeExpanded && isEditMode,
                onExpandChange = { if (isEditMode) typeExpanded = it },
                options = listOf("Individual", "Corporate"),
                onOptionSelected = { label -> viewModel.onTypeChange(label) },
                isRequired = true,
                enabled = isEditMode
            )
            Spacer(Modifier.height(12.dp))
            FormLabel("Full Name", isRequired = true)
            FormTextField(
                value = formState.name,
                onValueChange = viewModel::onNameChange,
                placeholder = "Enter Your Name",
                enabled = isEditMode,
                isError = errorField == "name",
                errorMessage = if (errorField == "name") "Please check the name" else null
            )
            Spacer(Modifier.height(12.dp))
            FormLabel("Gender")
            FormDropdown(
                value = formState.gender.ifEmpty { "Select an option" },
                expanded = genderExpanded && isEditMode,
                onExpandChange = { if (isEditMode) genderExpanded = it },
                options = listOf("Male", "Female", "Other"),
                onOptionSelected = viewModel::onGenderChange,
                enabled = isEditMode
            )

            Spacer(Modifier.height(12.dp))
            FormLabel("Date of Birth")
            val dobDisplay = formState.dob.toDisplayDate()
            DatePickerField(
                value = if (dobDisplay != "—") dobDisplay else "Select date",
                onDateSelected = { selected -> if (isEditMode) viewModel.onDobChange(selected.toIsoDate()) },
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

            FormLabel("Mobile No", isRequired = true)
            PhoneInputField(
                phoneValue = formState.mobile,
                onPhoneChange = viewModel::onMobileChange,
                onCountryChange = { },
                enabled = isEditMode,
                isError = errorField == "mobile"
            )
            Spacer(Modifier.height(12.dp))

            FormLabel("Email")
            FormTextField(
                value = formState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "Enter Your email",
                enabled = isEditMode,
                isError = errorField == "email",
                errorMessage = if (errorField == "email") "Please enter a valid email" else null
            )
            Spacer(Modifier.height(12.dp))
            FormLabel("Status")
            FormDropdown(
                value = formState.status.ifEmpty { "Active" },
                expanded = statusExpanded && isEditMode,
                onExpandChange = { if (isEditMode) statusExpanded = it },
                options = listOf("Active", "Inactive"),
                onOptionSelected = viewModel::onStatusChange,
                enabled = isEditMode
            )
            Spacer(Modifier.height(12.dp))
            FormLabel("Preferred Contact")
            FormDropdown(
                value = formState.contactMethod.ifEmpty { "-" },
                expanded = contactExpanded && isEditMode,
                onExpandChange = { if (isEditMode) contactExpanded = it },
                options = listOf("Call", "Whatsapp", "Email", "SMS"),
                onOptionSelected = viewModel::onContactMethodChange,
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
                enabled = isEditMode
            )
            Spacer(Modifier.height(12.dp))

            FormLabel("Area / Zone")
            FormTextField(
                value = formState.area,
                onValueChange = viewModel::onAreaChange,
                placeholder = "Enter Area/Zone",
                enabled = isEditMode
            )
            Spacer(Modifier.height(12.dp))

            FormLabel("City")
            FormTextField(
                value = formState.city,
                onValueChange = viewModel::onCityChange,
                placeholder = "Enter Your City",
                enabled = isEditMode
            )
            Spacer(Modifier.height(12.dp))

            FormLabel("Pincode")
            FormTextField(
                value = formState.pincode,
                onValueChange = viewModel::onPincodeChange,
                placeholder = "Enter Pincode",
                enabled = isEditMode
            )
        }
    }
}

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
                                .background(primary_light, CircleShape)
                        )
                    }

                    val circleColor by animateColorAsState(
                        targetValue = when {
                            done -> darkGreenBg
                            active -> Primary
                            else -> whiteBg
                        },
                        animationSpec = tween(durationMillis = 300),
                        label = "circleColor"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = when {
                            done -> darkGreenBg
                            active -> Primary
                            else -> BorderGray
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
                                color = mutedText,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (index < stepLabels.lastIndex) {
                    val lineColor by animateColorAsState(
                        targetValue = if (index < currentStep) darkGreenBg else BorderGray,
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
                                .height(3.dp)
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
                            color = Primary,
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
                    .border(1.dp, PrimaryBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                    .padding(horizontal = tokens.screenPadding * 0.6f, vertical = 10.dp)
            ) {
                Text("LAST UPDATED", fontSize = tokens.bodySmall, color = mutedText)
                Text("15/12/2026", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = blackTitle)
            }
            Spacer(Modifier.height(14.dp))
            Text("GARMENT TYPES COVERED", fontSize = tokens.caption, color = blackTitle)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Shirt", "Pant", "Suit", "Kurta").forEach { Chip(it) }
            }

            if (isEditMode) {
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
                ) {
                    Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                    Spacer(Modifier.width(6.dp))
                    Text("Add New Measurement", color = Primary, fontSize = tokens.bodyMedium)
                }
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
                        .background(yellowBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = tokens.screenPadding * 0.75f, vertical = 4.dp)
                ) {
                    Text("MEDIUM", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = yellowText)
                }
            }
            Spacer(Modifier.height(12.dp))
            InsightRow(label = "Rework Flag") {
                Box(
                    modifier = Modifier
                        .background(greenBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = tokens.screenPadding * 0.75f, vertical = 4.dp)
                ) {
                    Text("NO", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = darkGreenBg)
                }
            }
        }
    }
}

@Composable
private fun OrderPaymentStep() {
    val tokens = LocalAppTokens.current
    var expandedSection by remember { mutableStateOf("payment") }

    Column {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OrderStatBox(modifier = Modifier.weight(1f), label = "Total Orders", value = "28")
                OrderStatBox(modifier = Modifier.weight(1f), label = "First Order", value = "Mar 2022")
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OrderStatBox(modifier = Modifier.weight(1f), label = "Last orders", value = "Jan 2026")
                OrderStatBox(modifier = Modifier.weight(1f), label = "Avg. order value", value = "₹15.6K", highlight = true)
            }
        }
        Spacer(Modifier.height(16.dp))

        AccordionSection(
            title = "Payment Overview",
            expanded = expandedSection == "payment",
            onHeaderClick = { expandedSection = if (expandedSection == "payment") "" else "payment" }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(greenBg, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                    .padding(horizontal = tokens.screenPadding * 0.85f, vertical = 14.dp)
            ) {
                Column {
                    Text("Total Spend", fontSize = tokens.bodySmall, color = greentext)
                    Spacer(Modifier.height(4.dp))
                    Text("₹4,36,800", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = greentext)
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(redBg, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                    .padding(horizontal = tokens.screenPadding * 0.85f, vertical = 14.dp)
            ) {
                Column {
                    Text("Pending Payment", fontSize = tokens.bodySmall, color = redText)
                    Spacer(Modifier.height(4.dp))
                    Text("₹8,500", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = redText)
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
            Text("Frequently Ordered Garments", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Formal Shirt", "Trousers", "Suit").forEach { Chip(it) }
            }

            Spacer(Modifier.height(16.dp))

            OrderHistoryTable(
                orders = listOf(
                    OrderHistoryRow("ORD-01", "Jan 6, 2026", "Wedding Sherwani", "₹3,500", "Completed"),
                    OrderHistoryRow("ORD-02", "Jan 6, 2026", "Designer Blouse", "₹3,500", "Pending"),
                    OrderHistoryRow("ORD-03", "Jan 6, 2026", "Custom Suit", "₹3,500", "In Progress")
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun OrderHistoryTable(orders: List<OrderHistoryRow>) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState)) {
            Row(
                modifier = Modifier
                    .background(light_grey, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                    .padding(horizontal = tokens.screenPadding * 0.75f, vertical = 14.dp)
            ) {
                Text("Order ID", fontSize = tokens.bodyMedium, color = TextSecondary, modifier = Modifier.width(90.dp))
                Text("Date", fontSize = tokens.bodyMedium, color = TextSecondary, modifier = Modifier.width(100.dp))
                Text("Garment", fontSize = tokens.bodyMedium, color = TextSecondary, modifier = Modifier.width(130.dp))
                Text("Amount", fontSize = tokens.bodyMedium, color = TextSecondary, modifier = Modifier.width(90.dp))
                Text("Status", fontSize = tokens.bodyMedium, color = TextSecondary, modifier = Modifier.width(100.dp))
            }

            orders.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = tokens.screenPadding * 0.75f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(row.orderId, fontSize = tokens.bodyMedium, color = TextPrimary, modifier = Modifier.width(90.dp))
                    Text(row.date, fontSize = tokens.bodySmall, color = TextPrimary, modifier = Modifier.width(100.dp))
                    Text(row.garment, fontSize = tokens.bodyMedium, color = TextPrimary, modifier = Modifier.width(130.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(row.amount, fontSize = tokens.bodyMedium, color = TextPrimary, modifier = Modifier.width(90.dp))
                    Box(modifier = Modifier.width(100.dp)) {
                        Box(
                            modifier = Modifier
                                .background(primary_light, RoundedCornerShape(50))
                                .padding(horizontal = tokens.screenPadding * 0.6f, vertical = 4.dp)
                        ) {
                            Text(row.status, fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (index != orders.lastIndex) {
                    HorizontalDivider(color = title_border)
                }
            }
        }
    }
}

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
    }
}

@Composable
private fun NotesTagsStep(isEditMode: Boolean) {
    val tokens = LocalAppTokens.current

    Column(
        Modifier
            .fillMaxWidth()
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
        if (isEditMode) {
            Spacer(Modifier.height(10.dp))
            DashedAddButton(text = "Add Internal Notes", onClick = {}, enabled = true)
        }

        Spacer(Modifier.height(24.dp))

        SectionHeader("Customer Notes", "Customer's own preferences and notes")
        Spacer(Modifier.height(10.dp))
        NoteCard(
            text = "Prefers delivery on weekends only.",
            bgColor = primary_light,
            borderColor = light_blue_border,
            textColor = TextPrimary
        )
        if (isEditMode) {
            Spacer(Modifier.height(10.dp))
            DashedAddButton(text = "Add Customer Notes", onClick = {}, enabled = true)
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    val tokens = LocalAppTokens.current
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = tokens.h2, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, fontSize = tokens.bodySmall, color = mutedText)
    }
}

@Composable
private fun NoteCard(text: String, bgColor: Color, borderColor: Color, textColor: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
            .border(1.dp, borderColor, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
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
            .dashedBorder(color = BorderGray, strokeWidth = 1.dp, cornerRadius = tokens.cardCornerRadius * 0.8f),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
        border = null,
        contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = 14.dp)
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(tokens.iconSize), tint = TextSecondary)
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = tokens.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    highlight: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .background(
                if (highlight) primary_light else PanelBg,
                RoundedCornerShape(tokens.cardCornerRadius)
            )
            .border(1.dp, if (highlight) Primary else BorderGray, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(vertical = 18.dp, horizontal = tokens.screenPadding * 0.6f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            value,
            fontSize = tokens.h2,
            fontWeight = FontWeight.Bold,
            color = if (highlight) Primary else TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = tokens.caption,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) Primary else mutedText
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
                RoundedCornerShape(tokens.cardCornerRadius)
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
            .background(primary_light, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .padding(horizontal = tokens.screenPadding * 0.9f, vertical = 8.dp)
    ) {
        Text(text, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Primary)
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
        Text(label, fontSize = tokens.bodySmall, color = TextSecondary)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
        Text(label, fontSize = tokens.bodySmall, color = TextSecondary)
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
                color = if (enabled) Primary else mutedText,
                strokeWidth = 1.dp,
                cornerRadius = tokens.cardCornerRadius * 0.8f
            )
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
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
                modifier = Modifier.size(tokens.iconSize),
                tint = if (enabled) Primary else mutedText
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = tokens.bodyMedium,
                color = if (enabled) TextPrimary else mutedText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}