@file:Suppress("UNUSED_PARAMETER", "AssignedValueIsNeverRead", "SpellCheckingInspection")

package com.cuso.tailor.view.home.finance.settings

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
import java.util.Calendar

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
    onClose: () -> Unit,
    taxTypeOptions: List<String> = listOf("GST", "CGST", "SGST", "IGST", "CESS", "VAT", "Custom"),
    onSaveTaxRate: (AddTaxRateFormState) -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isEditMode = taxRateToEdit != null

    var taxName by remember { mutableStateOf("") }
    var ratePercent by remember { mutableStateOf("") }
    var taxType by remember { mutableStateOf("GST") }
    var taxTypeExpanded by remember { mutableStateOf(false) }
    var effectiveDate by remember { mutableStateOf("") }

    // Prefill form if editing an existing tax rate
    LaunchedEffect(taxRateToEdit) {
        if (taxRateToEdit != null) {
            taxName = taxRateToEdit.name
            ratePercent = taxRateToEdit.rate.toString()
            taxType = taxRateToEdit.taxType ?: "GST"
            effectiveDate = taxRateToEdit.effectiveFrom?.take(10) ?: ""
        }
    }

    // Date Picker Dialog setup (formats to YYYY-MM-DD for backend compatibility)
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val formattedMonth = (month + 1).toString().padStart(2, '0')
                val formattedDay = dayOfMonth.toString().padStart(2, '0')
                effectiveDate = "$year-$formattedMonth-$formattedDay"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Validation rule: name and rate are mandatory
    val isFormValid = taxName.isNotBlank() && ratePercent.isNotBlank()

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
                        onValueChange = { taxName = it },
                        placeholder = "e.g. GST 18% or CGST 9%"
                    )
                }

                // 2. Rate (%) Field
                Column {
                    FormLabel(text = "Rate (%)", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = ratePercent,
                        onValueChange = { ratePercent = it },
                        placeholder = "e.g. 18",
                        keyboardType = KeyboardType.Number,
                        enabled = !isEditMode // Rate is locked when editing to preserve ledger consistency
                    )
                    if (isEditMode) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Rate percentage cannot be altered after creation.",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }

                // 3. Tax Type Dropdown
                Column {
                    FormLabel(text = "Tax Type", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormDropdown(
                        value = taxType,
                        expanded = taxTypeExpanded,
                        onExpandChange = { taxTypeExpanded = it },
                        options = taxTypeOptions,
                        onOptionSelected = { taxType = it }
                    )
                }

                // 4. Effective Date Field with Calendar Picker
                Column {
                    FormLabel(text = "Effective From", isRequired = false)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { datePickerDialog.show() }
                    ) {
                        DatePickerField(
                            value = effectiveDate,
                            onDateSelected = { effectiveDate = it }
                        )
                    }
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
                    isLoading -> "Saving..."
                    isEditMode -> "Update Tax Rate"
                    else -> "Save Tax Rate"
                },
                enabled = isFormValid && !isLoading,
                onClick = {
                    if (isFormValid && !isLoading) {
                        onSaveTaxRate(
                            AddTaxRateFormState(
                                name = taxName.trim(),
                                rate = ratePercent.trim(),
                                taxType = taxType,
                                effectiveFrom = effectiveDate.ifBlank { null }
                            )
                        )
                    }
                }
            )
        )
    }
}