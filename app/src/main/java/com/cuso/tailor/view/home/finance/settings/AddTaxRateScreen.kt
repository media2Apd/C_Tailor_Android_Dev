@file:Suppress("UNUSED_PARAMETER", "AssignedValueIsNeverRead", "SpellCheckingInspection", "VariableNeverRead")

package com.cuso.tailor.view.home.finance.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.TaxRateItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Data Model for Tax Rate Form Payload
// ---------------------------------------------------------------------------

/**
 * Form state matching backend payload: { name, rate, taxType, effectiveFrom }
 */
data class AddTaxRateFormState(
    val name: String = "",
    val rate: String = "",
    val taxType: String = "GST",
    val effectiveFrom: String? = null
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun AddTaxRateScreen(
    taxRateToEdit: TaxRateItemDto? = null,
    isLoading: Boolean = false,
    viewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onSaveSuccess: () -> Unit = onClose,
    taxTypeOptions: List<String> = listOf("GST", "VAT", "Sales Tax"),
    onSaveTaxRate: ((AddTaxRateFormState) -> Unit)? = null
) {
    val tokens = LocalAppTokens.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val isEditMode = taxRateToEdit != null

    // Map UI labels to exact backend taxType enum values
    val taxTypePayloadMap = remember {
        linkedMapOf(
            "GST" to "GST",
            "VAT" to "VAT",
            "Sales Tax" to "SalesTax"
        )
    }

    var taxName by remember { mutableStateOf("") }
    var ratePercent by remember { mutableStateOf("") }
    var taxType by remember { mutableStateOf("GST") }
    var taxTypeExpanded by remember { mutableStateOf(false) }
    var effectiveDate by remember { mutableStateOf("") }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isSubmittingTaxRate by viewModel.isSubmittingTaxRate.collectAsStateWithLifecycle()
    val effectiveLoading = isLoading || isSubmittingTaxRate

    // Prefill form when editing an existing tax rate
    LaunchedEffect(taxRateToEdit) {
        if (taxRateToEdit != null) {
            taxName = taxRateToEdit.name
            ratePercent = taxRateToEdit.rate.toString()
            taxType = taxRateToEdit.taxType ?: "GST"
            effectiveDate = formatIsoToDisplayDate(taxRateToEdit.effectiveFrom)
        } else {
            taxName = ""
            ratePercent = ""
            taxType = "GST"
            effectiveDate = ""
        }
    }

    // Validation rule: name, rate, and taxType are mandatory
    val isFormValid = taxName.isNotBlank() && ratePercent.isNotBlank() && taxType.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column {
                    TitleBar(
                        title = if (isEditMode) "Edit Tax Rate" else "Create Tax Rate",
                        onClose = onClose
                    )
                    HorizontalDivider(color = grey_border)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = tokens.screenPadding, vertical = 16.dp)
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Tax Name Field
                Column {
                    FormLabel(text = "Tax Name", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = taxName,
                        onValueChange = {
                            taxName = it
                            errorMessage = null
                        },
                        placeholder = "e.g. GST 18% — Goods"
                    )
                }

                // 2. Rate (%) Field (editable in Edit mode to trigger revise endpoint if changed)
                Column {
                    FormLabel(text = "Rate (%)", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = ratePercent,
                        onValueChange = { input ->
                            ratePercent = input.filterIndexed { index, ch ->
                                ch.isDigit() || (ch == '.' && input.indexOf('.') == index)
                            }
                            errorMessage = null
                        },
                        placeholder = "e.g. 18",
                        trailingText = "%",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                // 3. Tax Type Dropdown
                Column {
                    FormLabel(text = "Tax Type", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormDropdown(
                        value = taxTypePayloadMap.entries.firstOrNull { it.value == taxType }?.key ?: taxType,
                        expanded = taxTypeExpanded && !isEditMode,
                        onExpandChange = { if (!isEditMode) taxTypeExpanded = it },
                        options = taxTypeOptions,
                        onOptionSelected = { label ->
                            taxType = taxTypePayloadMap[label] ?: label
                            errorMessage = null
                        },
                        enabled = !isEditMode
                    )
                }

                // 4. Effective Date Field
                Column {
                    FormLabel(text = "Effective Date", isRequired = false)
                    Spacer(Modifier.height(6.dp))
                    DatePickerField(
                        value = effectiveDate,
                        onDateSelected = {
                            effectiveDate = it
                            errorMessage = null
                        }
                    )
                }

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = tokens.caption,
                        color = redText
                    )
                }
            }
        }

        // Bottom Action Navigation Bar (Cancel & Save / Update Tax Rate)
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = when {
                    effectiveLoading -> "Processing..."
                    else -> "Save Tax Rate"
                },
                enabled = isFormValid && !effectiveLoading,
                onClick = {
                    val parsedRate = ratePercent.trim().toDoubleOrNull()
                    if (taxName.isBlank()) {
                        errorMessage = "Tax name is required."
                        return@Next
                    }
                    if (taxType.isBlank()) {
                        errorMessage = "Tax type is required."
                        return@Next
                    }
                    if (parsedRate == null || parsedRate < 0.0) {
                        errorMessage = "Please enter a valid tax rate."
                        return@Next
                    }

                    val formattedEffectiveFrom = formatDisplayDateToYyyyMmDd(effectiveDate)
                    val formState = AddTaxRateFormState(
                        name = taxName.trim(),
                        rate = ratePercent.trim(),
                        taxType = taxType,
                        effectiveFrom = formattedEffectiveFrom
                    )

                    if (taxRateToEdit != null) {
                        val originalRate = taxRateToEdit.rate.toString().toDoubleOrNull()
                        if (originalRate != null && originalRate != parsedRate) {
                            // Hit revise endpoint when rate percentage changed
                            viewModel.reviseTaxRate(
                                id = taxRateToEdit.id,
                                newRate = formState.rate,
                                effectiveFrom = formState.effectiveFrom,
                                onSuccess = { msg ->
                                    successMessage = msg
                                    onSaveTaxRate?.invoke(formState)
                                    scope.launch {
                                        delay(800)
                                        onSaveSuccess()
                                    }
                                },
                                onError = { errorMessage = it }
                            )
                        } else {
                            viewModel.updateTaxRate(
                                id = taxRateToEdit.id,
                                name = formState.name,
                                onSuccess = { msg ->
                                    successMessage = msg
                                    onSaveTaxRate?.invoke(formState)
                                    scope.launch {
                                        delay(800)
                                        onSaveSuccess()
                                    }
                                },
                                onError = { errorMessage = it }
                            )
                        }
                    } else {
                        viewModel.createTaxRate(
                            name = formState.name,
                            rate = formState.rate,
                            taxType = formState.taxType,
                            effectiveFrom = formState.effectiveFrom,
                            onSuccess = { msg ->
                                successMessage = msg
                                onSaveTaxRate?.invoke(formState)
                                scope.launch {
                                    delay(800)
                                    onSaveSuccess()
                                }
                            },
                            onError = { errorMessage = it }
                        )
                    }
                }
            )
        )
    }
}

private fun formatIsoToDisplayDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val datePart = iso.trim().take(10)
    val parts = datePart.split("-")
    return if (parts.size == 3 && parts[0].length == 4) {
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } else {
        datePart
    }
}

private fun formatDisplayDateToYyyyMmDd(display: String): String? {
    if (display.isBlank()) return null
    val parts = display.trim().split("-")
    return if (parts.size == 3 && parts[2].length == 4) {
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } else {
        display.trim()
    }
}