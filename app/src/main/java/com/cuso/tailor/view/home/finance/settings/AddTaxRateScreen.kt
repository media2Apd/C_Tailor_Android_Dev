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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import java.util.Calendar

// ---------------------------------------------------------------------------
// Data Model for Add Tax Rate Form
// ---------------------------------------------------------------------------

data class AddTaxRateFormState(
    val taxName: String = "",
    val ratePercent: String = "",
    val taxType: String = "GST",
    val applicableFor: String = "Services",
    val taxTreatment: String = "Taxable",
    val effectiveDate: String = "",
    val isActive: Boolean = true
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun AddTaxRateScreen(
    onClose: () -> Unit,
    onSaveTaxRate: (AddTaxRateFormState) -> Unit,
    taxTypeOptions: List<String> = listOf("GST", "VAT", "Custom"),
    applicableForOptions: List<String> = listOf("Services", "Products", "Both"),
    taxTreatmentOptions: List<String> = listOf("Taxable", "Exempt", "Nil Rated", "Non-GST")
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var taxName by remember { mutableStateOf("") }
    var ratePercent by remember { mutableStateOf("") }

    var taxType by remember { mutableStateOf("GST") }
    var taxTypeExpanded by remember { mutableStateOf(false) }

    var applicableFor by remember { mutableStateOf("Services") }
    var applicableForExpanded by remember { mutableStateOf(false) }

    var taxTreatment by remember { mutableStateOf("Taxable") }
    var taxTreatmentExpanded by remember { mutableStateOf(false) }

    var effectiveDate by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    // Date Picker Dialog setup
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val formattedMonth = (month + 1).toString().padStart(2, '0')
                val formattedDay = dayOfMonth.toString().padStart(2, '0')
                effectiveDate = "$formattedDay/$formattedMonth/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val isFormValid = taxName.isNotBlank() && ratePercent.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column {
                    TitleBar(title = "Add Tax Rate", onClose = onClose)
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
                        placeholder = "e.g. GST 18% — Stitching Services"
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
                        keyboardType = KeyboardType.Number
                    )
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

                // 4. Applicable For Dropdown
                Column {
                    FormLabel(text = "Applicable For", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormDropdown(
                        value = applicableFor,
                        expanded = applicableForExpanded,
                        onExpandChange = { applicableForExpanded = it },
                        options = applicableForOptions,
                        onOptionSelected = { applicableFor = it }
                    )
                }

                // 5. Tax Treatment Dropdown
                Column {
                    FormLabel(text = "Tax Treatment", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormDropdown(
                        value = taxTreatment,
                        expanded = taxTreatmentExpanded,
                        onExpandChange = { taxTreatmentExpanded = it },
                        options = taxTreatmentOptions,
                        onOptionSelected = { taxTreatment = it }
                    )
                }

                // 6. Effective Date Field with Calendar Picker
                Column {
                    FormLabel(text = "Effective Date", isRequired = false)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { datePickerDialog.show() }
                    ) {
//                        FormTextField(
//                            value = effectiveDate,
//                            onValueChange = { effectiveDate = it },
//                            placeholder = "e.g. 18",
//                            trailingIcon = {
//                                Icon(
//                                    imageVector = Icons.Outlined.CalendarToday,
//                                    contentDescription = "Select Date",
//                                    tint = close_color,
//                                    modifier = Modifier
//                                        .size(20.dp)
//                                        .clickable { datePickerDialog.show() }
//                                )
//                            }
//                        )
                        DatePickerField(
                            value = effectiveDate,
                            onDateSelected = { effectiveDate = it }
                        )
                    }
                }

                // 7. Status Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    MiniSwitch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        }

        // Bottom Action Navigation Bar (Cancel & Save Tax Rate)
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = "Save Tax Rate",
                enabled = isFormValid,
                onClick = {
                    if (isFormValid) {
                        onSaveTaxRate(
                            AddTaxRateFormState(
                                taxName = taxName.trim(),
                                ratePercent = ratePercent.trim(),
                                taxType = taxType,
                                applicableFor = applicableFor,
                                taxTreatment = taxTreatment,
                                effectiveDate = effectiveDate,
                                isActive = isActive
                            )
                        )
                    }
                }
            )
        )
    }
}