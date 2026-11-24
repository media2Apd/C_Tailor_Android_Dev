@file:Suppress("unused","AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.logistics.courier_partners

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.CreateUpdateCourierPartnerRequest
import com.cuso.tailor.model.logistics.ServiceabilityRequestDto
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.LogisticsViewModel

private data class IntegrationOption(val value: String, val label: String)
private data class CoverageOption(val key: String, val label: String, val helpText: String)

private val INTEGRATION_OPTIONS = listOf(
    IntegrationOption("MANUAL", "Manual"),
    IntegrationOption("SHIPROCKET", "Shiprocket"),
    IntegrationOption("DHL", "DHL"),
    IntegrationOption("FEDEX", "FedEx")
)

private val COVERAGE_OPTIONS = listOf(
    CoverageOption("GLOBAL", "Global", "Delivers worldwide to the countries listed below."),
    CoverageOption("DOMESTIC", "All India", "Delivers across all serviceable locations in India."),
    CoverageOption("REGIONAL", "Regional", "Delivers only to the countries listed below."),
    CoverageOption("SELECTED_POSTAL_CODES", "Selected Pincodes", "Delivers only to the postal codes listed below.")
)

private val COUNTRY_CODE_REGEX = Regex("^[A-Z]{2}$")
private val POSTAL_CODE_REGEX = Regex("^[A-Z0-9][A-Z0-9 -]{2,9}$")

/**
 * Add / Edit Courier Partner Screen.
 * Reuses core form components (FormTextField, FormDropdown, FormLabel, FormTextArea, MiniSwitch)
 * and supports creation, editing, chip inputs on Enter/Done, and field-level validation.
 */
@Composable
fun AddEditCourierPartnerScreen(
    courierIdToEdit: String?,
    viewModel: LogisticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    val isEditMode = !courierIdToEdit.isNullOrBlank()
    val isSubmitting by viewModel.isSubmittingCourier.collectAsState()
    val submitError by viewModel.courierSubmitError.collectAsState()
    val selectedDetail by viewModel.selectedCourierPartnerDetail.collectAsState()

    // Sync state for Shiprocket courier import
    val isSyncing by viewModel.isSyncingCouriers.collectAsState()

    // Form field states
    var courierName by remember { mutableStateOf("") }
    var isOperationalActive by remember { mutableStateOf(true) }
    var integrationType by remember { mutableStateOf("MANUAL") }
    var integrationDropdownExpanded by remember { mutableStateOf(false) }
    var shiprocketCourierId by remember { mutableStateOf("") }
    var reliabilityScore by remember { mutableStateOf("98.4") }

    val serviceTypes = remember { mutableStateListOf<String>() }
    var newServiceInput by remember { mutableStateOf("") }

    var coverageType by remember { mutableStateOf("GLOBAL") }
    val coveredCountries = remember { mutableStateListOf("IN", "US") }
    var newCountryInput by remember { mutableStateOf("") }

    val coveredPincodes = remember { mutableStateListOf<String>() }
    var newPincodeInput by remember { mutableStateOf("") }

    val primaryHubs = remember { mutableStateListOf<String>() }
    var newHubInput by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }

    // Field validation states
    var courierNameError by remember { mutableStateOf<String?>(null) }
    var shiprocketIdError by remember { mutableStateOf<String?>(null) }
    var reliabilityScoreError by remember { mutableStateOf<String?>(null) }
    var serviceTypesError by remember { mutableStateOf<String?>(null) }
    var countriesError by remember { mutableStateOf<String?>(null) }
    var postalCodesError by remember { mutableStateOf<String?>(null) }

    // Unsaved changes tracking
    var initialSnapshot by remember { mutableStateOf<String?>(null) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    fun currentSnapshot(): String = listOf(
        courierName,
        isOperationalActive.toString(),
        integrationType,
        shiprocketCourierId,
        reliabilityScore,
        serviceTypes.joinToString("|"),
        coverageType,
        coveredCountries.joinToString("|"),
        coveredPincodes.joinToString("|"),
        primaryHubs.joinToString("|"),
        notes
    ).joinToString("::")

    LaunchedEffect(Unit) {
        if (!isEditMode && initialSnapshot == null) {
            initialSnapshot = currentSnapshot()
        }
    }

    // Fetch existing partner details when editing
    LaunchedEffect(courierIdToEdit) {
        if (isEditMode) {
            viewModel.fetchCourierPartnerDetail(courierIdToEdit)
        }
    }

    LaunchedEffect(selectedDetail) {
        if (isEditMode && selectedDetail != null) {
            val d = selectedDetail!!
            courierName = d.courierName
            isOperationalActive = d.status.equals("Active", ignoreCase = true)
            integrationType = d.integrationType?.takeIf { it.isNotBlank() } ?: "MANUAL"
            shiprocketCourierId = d.shiprocketCourierId?.toString() ?: ""
            reliabilityScore = d.reliabilityScore.toString()
            notes = d.notes

            serviceTypes.clear()
            serviceTypes.addAll(d.serviceTypes)

            coverageType = d.serviceability?.coverageType?.takeIf { it.isNotBlank() } ?: "GLOBAL"

            coveredCountries.clear()
            if (!d.serviceability?.coveredCountries.isNullOrEmpty()) {
                coveredCountries.addAll(d.serviceability.coveredCountries)
            }

            coveredPincodes.clear()
            if (!d.serviceability?.coveredPostalCodes.isNullOrEmpty()) {
                coveredPincodes.addAll(d.serviceability.coveredPostalCodes)
            }

            primaryHubs.clear()
            if (!d.serviceability?.primaryHubs.isNullOrEmpty()) {
                primaryHubs.addAll(d.serviceability.primaryHubs)
            }

            initialSnapshot = currentSnapshot()
        }
    }

    val hasUnsavedChanges = initialSnapshot != null && initialSnapshot != currentSnapshot()

    val attemptLeave: () -> Unit = {
        if (hasUnsavedChanges) {
            showLeaveDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showLeaveDialog = true
    }

    val showCountries = coverageType != "SELECTED_POSTAL_CODES"
    val showPostal = coverageType == "SELECTED_POSTAL_CODES"
    val activeCoverageHelp = COVERAGE_OPTIONS.firstOrNull { it.key == coverageType }?.helpText ?: ""
    val selectedIntegrationLabel = INTEGRATION_OPTIONS.firstOrNull { it.value == integrationType }?.label ?: "Manual"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Courier Partner" else "Add Courier Partner",
                onClose = attemptLeave
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Sync Courier Partners action in create mode
            if (!isEditMode) {
                OutlinedButton(
                    onClick = {
                        if (!isSyncing) {
                            viewModel.syncShiprocketCouriers(
                                onSuccess = { message ->
                                    DynamicIslandManager.showSuccess(message)
                                },
                                onError = { message ->
                                    DynamicIslandManager.showError(message)
                                }
                            )
                        }
                    },
                    enabled = !isSyncing && !isSubmitting,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.dp, sectionBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = whiteBg,
                        contentColor = title_color
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = Primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))
                        Text(
                            text = "Syncing...",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Courier Partners",
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))
                        Text(
                            text = "Sync Courier Partners",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            }

            // 1. Basic Information Card
            FormSectionCard(
                title = "Basic Information",
                subtitle = "Identify the courier partner and their current status in your network."
            ) {
                FormLabel(text = "Courier Name", isRequired = true)
                FormTextField(
                    value = courierName,
                    onValueChange = {
                        courierName = it
                        courierNameError = null
                    },
                    placeholder = "e.g. SwiftExpress Logistics",
                    isError = courierNameError != null,
                    errorMessage = courierNameError,
                    enabled = !isSubmitting
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                FormLabel(text = "Operational Status")
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.6f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Text(
                        text = if (isOperationalActive) "Active" else "Inactive",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    MiniSwitch(
                        checked = isOperationalActive,
                        onCheckedChange = { isOperationalActive = it },
                        enabled = !isSubmitting
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                FormDropdown(
                    label = "Integration Type",
                    value = selectedIntegrationLabel,
                    expanded = integrationDropdownExpanded,
                    onExpandChange = { integrationDropdownExpanded = it },
                    options = INTEGRATION_OPTIONS.map { it.label },
                    onOptionSelected = { selectedLabel ->
                        val selectedValue = INTEGRATION_OPTIONS.firstOrNull { it.label == selectedLabel }?.value ?: "MANUAL"
                        integrationType = selectedValue
                        if (selectedValue != "SHIPROCKET") {
                            shiprocketCourierId = ""
                            shiprocketIdError = null
                        }
                    },
                    enabled = !isSubmitting
                )
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))
                FieldHint(text = "Manual couriers are managed here; others sync from the carrier.")

                if (integrationType == "SHIPROCKET") {
                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Shiprocket Courier ID", isRequired = true)
                    FormTextField(
                        value = shiprocketCourierId,
                        onValueChange = {
                            shiprocketCourierId = it.filter { ch -> ch.isDigit() }
                            shiprocketIdError = null
                        },
                        keyboardType = KeyboardType.Number,
                        placeholder = "e.g. 10",
                        isError = shiprocketIdError != null,
                        errorMessage = shiprocketIdError,
                        enabled = !isSubmitting
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                FormLabel(text = "Reliability Score (%)")
                FieldHint(text = "Delivery accuracy over the last 30 days (0–100).")
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))
                FormTextField(
                    value = reliabilityScore,
                    onValueChange = { input ->
                        // Allow digits and at most one decimal point
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            reliabilityScore = filtered
                            reliabilityScoreError = null
                        }
                    },
                    keyboardType = KeyboardType.Decimal,
                    placeholder = "e.g. 98.4",
                    trailingText = "%",
                    isError = reliabilityScoreError != null,
                    errorMessage = reliabilityScoreError,
                    enabled = !isSubmitting
                )
            }

            // 2. Service Type Card
            FormSectionCard(
                title = "Service Type",
                subtitle = "Select the delivery speeds supported by this partner."
            ) {
                FormLabel(text = "Select Services", isRequired = true)

                ChipInputContainer(
                    items = serviceTypes,
                    inputValue = newServiceInput,
                    onInputValueChange = { newServiceInput = it },
                    placeholder = "Add service...",
                    enabled = !isSubmitting,
                    externalError = serviceTypesError,
                    onAddItem = { item ->
                        if (item.isNotBlank() && serviceTypes.none { it.equals(item, ignoreCase = true) }) {
                            serviceTypes.add(item)
                        }
                        newServiceInput = ""
                        serviceTypesError = null
                    },
                    onRemoveItem = { item -> serviceTypes.remove(item) },
                    onRemoveLast = { if (serviceTypes.isNotEmpty()) serviceTypes.removeAt(serviceTypes.lastIndex) }
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))
                FieldHint(text = "Examples: Standard Domestic, Express International, Next-Day, Same-Day, Hyperlocal")
            }

            // 3. Serviceability Card
            FormSectionCard(
                title = "Serviceability",
                subtitle = "Define the geographic coverage for this partner."
            ) {
                // 2x2 coverage tab switcher with centered active indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(PanelBg)
                        .padding(tokens.extraPadding * 0.4f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                    ) {
                        COVERAGE_OPTIONS.take(2).forEach { option ->
                            CoverageTabButton(
                                label = option.label,
                                isSelected = coverageType == option.key,
                                onClick = {
                                    coverageType = option.key
                                    if (option.key == "DOMESTIC") {
                                        coveredCountries.clear()
                                        coveredCountries.add("IN")
                                    }
                                    countriesError = null
                                    postalCodesError = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                    ) {
                        COVERAGE_OPTIONS.drop(2).forEach { option ->
                            CoverageTabButton(
                                label = option.label,
                                isSelected = coverageType == option.key,
                                onClick = {
                                    coverageType = option.key
                                    countriesError = null
                                    postalCodesError = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.6f))
                Text(
                    text = activeCoverageHelp,
                    fontSize = tokens.caption,
                    color = TextSecondary
                )

                if (showCountries) {
                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Covered Countries", isRequired = true)
                    FieldHint(text = "ISO 2-letter codes, e.g. IN, US, AE, GB.")
                    Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))

                    ChipInputContainer(
                        items = coveredCountries,
                        inputValue = newCountryInput,
                        onInputValueChange = { newCountryInput = it },
                        placeholder = "Add country code...",
                        isSoftPill = true,
                        enabled = !isSubmitting,
                        externalError = countriesError,
                        transform = { it.uppercase() },
                        validate = { code ->
                            if (COUNTRY_CODE_REGEX.matches(code)) null else "Use a 2-letter country code"
                        },
                        onAddItem = { code ->
                            if (coveredCountries.none { it.equals(code, ignoreCase = true) }) {
                                coveredCountries.add(code)
                            }
                            newCountryInput = ""
                            countriesError = null
                        },
                        onRemoveItem = { code -> coveredCountries.remove(code) },
                        onRemoveLast = { if (coveredCountries.isNotEmpty()) coveredCountries.removeAt(coveredCountries.lastIndex) }
                    )
                }

                if (showPostal) {
                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Covered Postal Codes", isRequired = true)
                    FieldHint(text = "PIN, ZIP or postal codes, e.g. 110001, 90210, SW1A 1AA.")
                    Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))

                    ChipInputContainer(
                        items = coveredPincodes,
                        inputValue = newPincodeInput,
                        onInputValueChange = { newPincodeInput = it },
                        placeholder = "Add postal code...",
                        isSoftPill = true,
                        enabled = !isSubmitting,
                        externalError = postalCodesError,
                        transform = { it.uppercase() },
                        validate = { postal ->
                            if (POSTAL_CODE_REGEX.matches(postal)) null else "Enter a valid postal code"
                        },
                        onAddItem = { pincode ->
                            if (coveredPincodes.none { it.equals(pincode, ignoreCase = true) }) {
                                coveredPincodes.add(pincode)
                            }
                            newPincodeInput = ""
                            postalCodesError = null
                        },
                        onRemoveItem = { pincode -> coveredPincodes.remove(pincode) },
                        onRemoveLast = { if (coveredPincodes.isNotEmpty()) coveredPincodes.removeAt(coveredPincodes.lastIndex) }
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))
                FormLabel(text = "Primary Hubs")
                FieldHint(text = "Shown on the details page, e.g. 110001 (Delhi), 400001 (Mumbai).")
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))

                ChipInputContainer(
                    items = primaryHubs,
                    inputValue = newHubInput,
                    onInputValueChange = { newHubInput = it },
                    placeholder = "Add hub...",
                    isSoftPill = true,
                    enabled = !isSubmitting,
                    onAddItem = { hub ->
                        if (hub.isNotBlank() && primaryHubs.none { it.equals(hub, ignoreCase = true) }) {
                            primaryHubs.add(hub)
                        }
                        newHubInput = ""
                    },
                    onRemoveItem = { hub -> primaryHubs.remove(hub) },
                    onRemoveLast = { if (primaryHubs.isNotEmpty()) primaryHubs.removeAt(primaryHubs.lastIndex) }
                )
            }

            // 4. Notes Card
            FormSectionCard(
                title = "Notes",
                subtitle = "Any internal remarks or contractual details."
            ) {
                FormTextArea(
                    value = notes,
                    onValueChange = { notes = it },
                    enabled = !isSubmitting,
                    placeholder = "Enter optional notes here...",
                    minLines = 3,
                    maxLines = 4
                )
            }

            // API error message
            if (!submitError.isNullOrBlank()) {
                Text(
                    text = submitError ?: "",
                    fontSize = tokens.caption,
                    color = redText
                )
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.5f))

            // Primary Submit Button
            Button(
                onClick = {
                    val parsedScore = reliabilityScore.toDoubleOrNull()
                    val shiprocketIdInt = shiprocketCourierId.toIntOrNull()

                    var hasError = false
                    if (courierName.isBlank()) {
                        courierNameError = "Courier name is required"
                        hasError = true
                    }
                    if (integrationType == "SHIPROCKET" && (shiprocketCourierId.isBlank() || shiprocketIdInt == null)) {
                        shiprocketIdError = "Shiprocket courier ID is required"
                        hasError = true
                    }
                    if (reliabilityScore.isNotBlank() && (parsedScore == null || parsedScore < 0.0 || parsedScore > 100.0)) {
                        reliabilityScoreError = "Enter a score between 0 and 100"
                        hasError = true
                    }
                    if (serviceTypes.isEmpty()) {
                        serviceTypesError = "Add at least one service"
                        hasError = true
                    }
                    if (showCountries && coveredCountries.isEmpty()) {
                        countriesError = "Add at least one country code"
                        hasError = true
                    }
                    if (showPostal && coveredPincodes.isEmpty()) {
                        postalCodesError = "Add at least one postal code"
                        hasError = true
                    }

                    if (hasError) return@Button

                    val countriesPayload = when {
                        showCountries -> coveredCountries.toList()
                        coveredCountries.isNotEmpty() -> coveredCountries.toList()
                        else -> listOf("IN")
                    }

                    val requestPayload = CreateUpdateCourierPartnerRequest(
                        courierName = courierName.trim(),
                        status = if (isOperationalActive) "Active" else "Inactive",
                        integrationType = integrationType,
                        shiprocketCourierId = if (integrationType == "SHIPROCKET") shiprocketIdInt else null,
                        notes = notes.trim(),
                        reliabilityScore = parsedScore ?: 98.4,
                        serviceTypes = serviceTypes.toList(),
                        serviceability = ServiceabilityRequestDto(
                            coverageType = coverageType,
                            coveredCountries = countriesPayload,
                            coveredPostalCodes = if (showPostal) coveredPincodes.toList() else emptyList(),
                            primaryHubs = primaryHubs.toList()
                        )
                    )

                    if (isEditMode) {
                        viewModel.updateCourierPartner(courierIdToEdit, requestPayload) { successMsg ->
                            initialSnapshot = currentSnapshot()
                            DynamicIslandManager.showSuccess(successMsg)
                            onBack()
                        }
                    } else {
                        viewModel.createCourierPartner(requestPayload) { successMsg ->
                            initialSnapshot = currentSnapshot()
                            DynamicIslandManager.showSuccess(successMsg)
                            onBack()
                        }
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = whiteBg,
                    disabledContainerColor = disabled,
                    disabledContentColor = whiteBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = whiteBg,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Save Changes" else "Save Courier",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }

            // Cancel Action
            TextButton(
                onClick = attemptLeave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel Changes",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding))
        }
    }

    // Unsaved changes confirmation dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = whiteBg,
            title = {
                Text(
                    text = "Unsaved Changes",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Are you sure you want to leave this page? Your changes will be lost.",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        onBack()
                    }
                ) {
                    Text("Leave", color = redText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Stay", color = Primary, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

// =============================================================================
// REUSABLE SUB-COMPONENTS
// =============================================================================

@Composable
private fun FormSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Text(
                text = subtitle,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )
            Spacer(modifier = Modifier.height(tokens.extraPadding))
            content()
        }
    }
}

@Composable
private fun FieldHint(text: String) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.label,
        fontWeight = FontWeight.Normal,
        color = mutedText
    )
}

@Composable
private fun CoverageTabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .height(tokens.fieldHeight - tokens.extraPadding * 0.6f)
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
            .background(if (isSelected) whiteBg else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Primary else headerGrey
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipInputContainer(
    items: List<String>,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    placeholder: String,
    onAddItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onRemoveLast: () -> Unit,
    isSoftPill: Boolean = false,
    enabled: Boolean = true,
    externalError: String? = null,
    transform: ((String) -> String)? = null,
    validate: ((String) -> String?)? = null
) {
    val tokens = LocalAppTokens.current
    var chipError by remember { mutableStateOf<String?>(null) }
    var wasFocused by remember { mutableStateOf(false) }
    val activeError = chipError ?: externalError

    fun tryAdd(raw: String) {
        var value = raw.trim()
        if (value.isEmpty()) return
        if (transform != null) value = transform(value)
        val errMsg = validate?.invoke(value)
        if (errMsg != null) {
            chipError = errMsg
            return
        }
        chipError = null
        onAddItem(value)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Matches tokens.fieldHeight (40.dp) with chips and text cursor indicator vertically centered
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = tokens.fieldHeight)
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                .background(if (enabled) whiteBg else light_grey)
                .border(
                    width = 1.dp,
                    color = if (activeError != null) redText else grey_border,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .padding(horizontal = tokens.cardPadding * 0.5f, vertical = tokens.extraPadding * 0.4f),
            contentAlignment = Alignment.CenterStart
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEach { item ->
                    RemovableChip(
                        text = item,
                        isSoft = isSoftPill,
                        enabled = enabled,
                        onRemove = { onRemoveItem(item) },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                // Adds item on Enter/Done, comma, newline, or focus loss; Backspace on empty removes last chip
                BasicTextField(
                    value = inputValue,
                    onValueChange = { input ->
                        chipError = null
                        if (input.endsWith(",") || input.endsWith("\n")) {
                            tryAdd(input.trimEnd(',', '\n'))
                        } else {
                            onInputValueChange(input)
                        }
                    },
                    enabled = enabled,
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier
                        .width(tokens.imageDp * 2.2f)
                        .heightIn(min = tokens.iconSize * 1.35f)
                        .align(Alignment.CenterVertically)
                        .onFocusChanged { focusState ->
                            if (wasFocused && !focusState.isFocused && inputValue.isNotBlank()) {
                                tryAdd(inputValue)
                            }
                            wasFocused = focusState.isFocused
                        }
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.Enter, Key.NumPadEnter -> {
                                        tryAdd(inputValue)
                                        true
                                    }
                                    Key.Backspace -> {
                                        if (inputValue.isEmpty()) {
                                            onRemoveLast()
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                    else -> false
                                }
                            } else {
                                false
                            }
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { tryAdd(inputValue) }
                    ),
                    textStyle = TextStyle(
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    ),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier.heightIn(min = tokens.iconSize * 1.35f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (inputValue.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontSize = tokens.bodySmall,
                                    color = mutedText
                                )
                            }
                            inner()
                        }
                    }
                )
            }
        }

        if (!activeError.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.3f))
            Text(
                text = activeError,
                fontSize = tokens.label,
                color = redText
            )
        }
    }
}

@Composable
private fun RemovableChip(
    text: String,
    isSoft: Boolean,
    enabled: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val bg = if (isSoft) light_blue else Primary
    val textColor = if (isSoft) title_color else whiteBg
    val closeIconTint = if (isSoft) headerGrey else whiteBg

    Box(
        modifier = modifier
            .heightIn(min = tokens.iconSize * 1.35f)
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(bg)
            .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.25f),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
        ) {
            Text(
                text = text,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = closeIconTint,
                modifier = Modifier
                    .size(tokens.iconSize * 0.65f)
                    .clickable(enabled = enabled) { onRemove() }
            )
        }
    }
}