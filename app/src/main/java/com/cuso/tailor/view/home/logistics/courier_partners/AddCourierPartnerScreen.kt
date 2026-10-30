package com.cuso.tailor.view.home.logistics.courier_partners

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.CreateUpdateCourierPartnerRequest
import com.cuso.tailor.model.logistics.ServiceabilityRequestDto
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.LogisticsViewModel

/**
 * Add / Edit Courier Partner Screen matching mobile screenshot design.
 * Handles both creation and prefilled editing with validation.
 */
@Composable
fun AddEditCourierPartnerScreen(
    courierIdToEdit: String?,
    viewModel: LogisticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val isEditMode = !courierIdToEdit.isNullOrBlank()
    val isSubmitting by viewModel.isSubmittingCourier.collectAsState()
    val submitError by viewModel.courierSubmitError.collectAsState()

    // Form states
    var courierName by remember { mutableStateOf("") }
    var isOperationalActive by remember { mutableStateOf(true) }

    val serviceTypes = remember { mutableStateListOf("Standard", "Express") }
    var newServiceInput by remember { mutableStateOf("") }

    var isAllIndiaCoverage by remember { mutableStateOf(false) }

    val coveredPincodes = remember { mutableStateListOf("110001", "400001", "560001", "600001", "700001") }
    var newPincodeInput by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Prefill data if editing
    LaunchedEffect(courierIdToEdit) {
        if (isEditMode) {
            viewModel.fetchCourierPartnerDetail(courierIdToEdit)
        }
    }

    val selectedDetail by viewModel.selectedCourierPartnerDetail.collectAsState()

    LaunchedEffect(selectedDetail) {
        if (isEditMode && selectedDetail != null) {
            val d = selectedDetail!!
            courierName = d.courierName
            isOperationalActive = d.status.equals("Active", ignoreCase = true)
            notes = d.notes

            if (d.serviceTypes.isNotEmpty()) {
                serviceTypes.clear()
                serviceTypes.addAll(d.serviceTypes)
            }

            // Coverage prefill
            val isSelectedPincodes = d.serviceability?.coverageType?.equals("SELECTED_POSTAL_CODES", ignoreCase = true) == true
            isAllIndiaCoverage = !isSelectedPincodes

            if (!d.serviceability?.coveredPostalCodes.isNullOrEmpty()) {
                coveredPincodes.clear()
                coveredPincodes.addAll(d.serviceability.coveredPostalCodes)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Courier Partner" else "Add Courier Partner",
                onClose = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. Basic Information Card ──
            FormSectionCard {
                Text(
                    text = "Basic Information",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Text(
                    text = "Identify the courier partner and their current status in your network.",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormLabel(text = "Courier Name")
                FormTextField(
                    value = courierName,
                    onValueChange = {
                        courierName = it
                        validationError = null
                    },
                    placeholder = "e.g. SwiftExpress Logistics"
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Operational Status",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Text(
                    text = "Partner can receive new orders immediately",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Active",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    MiniSwitch(
                        checked = isOperationalActive,
                        onCheckedChange = { isOperationalActive = it }
                    )
                }
            }

            // ── 2. Select Services Card ──
            FormSectionCard {
                Text(
                    text = "Select Services",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Multi-chip entry box for Services
                ChipInputContainer(
                    items = serviceTypes,
                    inputValue = newServiceInput,
                    onInputValueChange = { newServiceInput = it },
                    placeholder = "Add service...",
                    onAddItem = { item ->
                        if (item.isNotBlank() && !serviceTypes.contains(item.trim())) {
                            serviceTypes.add(item.trim())
                            newServiceInput = ""
                        }
                    },
                    onRemoveItem = { item -> serviceTypes.remove(item) }
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Available: Next-Day, Same-Day, Hyperlocal",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Coverage Segmented Switcher: All India vs Selected Pincodes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                        .background(PanelBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CoverageTabButton(
                        label = "All India",
                        isSelected = isAllIndiaCoverage,
                        onClick = { isAllIndiaCoverage = true },
                        modifier = Modifier.weight(1f)
                    )
                    CoverageTabButton(
                        label = "Selected Pincodes",
                        isSelected = !isAllIndiaCoverage,
                        onClick = { isAllIndiaCoverage = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Covered Pincodes section when "Selected Pincodes" is active
                if (!isAllIndiaCoverage) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Covered Pincodes",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    ChipInputContainer(
                        items = coveredPincodes,
                        inputValue = newPincodeInput,
                        onInputValueChange = { newPincodeInput = it },
                        placeholder = "Add pincode...",
                        isSoftPill = true,
                        onAddItem = { pincode ->
                            if (pincode.isNotBlank() && !coveredPincodes.contains(pincode.trim())) {
                                coveredPincodes.add(pincode.trim())
                                newPincodeInput = ""
                            }
                        },
                        onRemoveItem = { pincode -> coveredPincodes.remove(pincode) }
                    )
                }
            }

            // ── 3. Notes Card ──
            FormSectionCard {
                Text(
                    text = "Notes",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Text(
                    text = "Any internal remarks or contractual details.",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormTextArea(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Enter optional notes here...",
                    minLines = 3,
                    maxLines = 4
                )
            }

            // Error display
            if (validationError != null || submitError != null) {
                Text(
                    text = validationError ?: submitError ?: "",
                    fontSize = tokens.caption,
                    color = redText
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Action Buttons ──
            Button(
                onClick = {
                    if (courierName.isBlank()) {
                        validationError = "Please enter courier name"
                        return@Button
                    }
                    if (serviceTypes.isEmpty()) {
                        validationError = "Please add at least one service type"
                        return@Button
                    }

                    validationError = null

                    val requestPayload = CreateUpdateCourierPartnerRequest(
                        courierName = courierName.trim(),
                        status = if (isOperationalActive) "Active" else "Inactive",
                        integrationType = "MANUAL",
                        shiprocketCourierId = null,
                        notes = notes.trim(),
                        reliabilityScore = selectedDetail?.reliabilityScore ?: 98.4,
                        serviceTypes = serviceTypes.toList(),
                        serviceability = ServiceabilityRequestDto(
                            coverageType = if (isAllIndiaCoverage) "DOMESTIC" else "SELECTED_POSTAL_CODES",
                            coveredCountries = listOf("IN"),
                            coveredPostalCodes = if (isAllIndiaCoverage) emptyList() else coveredPincodes.toList(),
                            primaryHubs = selectedDetail?.serviceability?.primaryHubs ?: listOf("Hub")
                        )
                    )

                    if (isEditMode) {
                        viewModel.updateCourierPartner(courierIdToEdit, requestPayload) { successMsg ->
                            Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    } else {
                        viewModel.createCourierPartner(requestPayload) { successMsg ->
                            Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
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
                    CircularProgressIndicator(color = whiteBg, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        text = if (isEditMode) "Save Changes" else "Save Courier",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel Changes",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =============================================================================
// SUB-COMPONENTS
// =============================================================================

@Composable
private fun FormSectionCard(content: @Composable ColumnScope.() -> Unit) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            content()
        }
    }
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
            .height(tokens.buttonHeight * 0.85f)
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
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
    isSoftPill: Boolean = false
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .padding(8.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                RemovableChip(
                    text = item,
                    isSoft = isSoftPill,
                    onRemove = { onRemoveItem(item) }
                )
            }

            // Input to add new item on comma or enter
            BasicTextField(
                value = inputValue,
                onValueChange = { input ->
                    if (input.endsWith(",") || input.endsWith("\n")) {
                        onAddItem(input.trimEnd(',', '\n'))
                    } else {
                        onInputValueChange(input)
                    }
                },
                modifier = Modifier
                    .width(120.dp)
                    .align(Alignment.CenterVertically)
                    .padding(vertical = 4.dp),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = title_color
                ),
                decorationBox = { inner ->
                    if (inputValue.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun RemovableChip(
    text: String,
    isSoft: Boolean,
    onRemove: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val bg = if (isSoft) light_blue else Primary
    val textColor = if (isSoft) title_color else whiteBg
    val closeIconTint = if (isSoft) headerGrey else whiteBg

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                    .size(12.dp)
                    .clickable { onRemove() }
            )
        }
    }
}