@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "unused_variable",
    "unused_parameter",
    "DEPRECATION",
    "AssignedValueIsNeverRead"
)

package com.cuso.tailor.view.home.hr.payroll_management.salary_template

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.SalaryTemplateComponentItem
import com.cuso.tailor.model.hr.SaveSalaryTemplateRequest
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.UiState
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.SalarySectionCard
import com.cuso.tailor.view.composable.SalarySectionLineItem
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel

/**
 * Domain item representing a selected template component.
 */
data class TemplateLineItem(
    val componentId: String,
    val name: String,
    val calculationType: String = "Fixed",
    val isEarning: Boolean,
    var amount: Double
)

@SuppressLint("DefaultLocale")
@Composable
fun SalaryTemplateFormScreen(
    templateId: String? = null,
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onPreviewPayslipClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !templateId.isNullOrBlank()

    // Form inputs state
    var templateName by remember { mutableStateOf("") }
    var annualCTC by remember { mutableStateOf("") }
    var payFrequency by remember { mutableStateOf("Monthly") }
    var isFrequencyExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    // Dynamic component lists
    val earnings = remember { mutableStateListOf<TemplateLineItem>() }
    val deductions = remember { mutableStateListOf<TemplateLineItem>() }

    // API state observers
    val availableComponents by viewModel.salaryComponents.collectAsState()
    val detailState by viewModel.templateDetailState.collectAsState()
    val saveState by viewModel.saveTemplateState.collectAsState()

    // Fetch available salary components
    LaunchedEffect(Unit) {
        viewModel.fetchSalaryComponents(limit = 100)
    }

    // Load template details when in edit mode
    LaunchedEffect(templateId) {
        if (!templateId.isNullOrBlank()) {
            viewModel.fetchSalaryTemplateDetail(templateId)
        } else {
            viewModel.clearTemplateDetail()
        }
    }

    // Prefill form from API
    LaunchedEffect(detailState, availableComponents) {
        if (detailState is UiState.Success) {
            val template = (detailState as UiState.Success).data.data
            templateName = template.name
            description = template.description.orEmpty()
            annualCTC = template.annualCTC?.toString().orEmpty()

            earnings.clear()
            deductions.clear()

            template.components.forEach { comp ->
                val compId = comp.salaryComponentId.orEmpty()
                val matched = availableComponents.find { it.id == compId }

                val name = matched?.name ?: "Component (${compId.takeLast(6)})"
                val isDeduction = matched?.type?.contains("deduct", ignoreCase = true) == true
                val calculationType = matched?.calculationType?.replace("_", " ")?.capitalize() ?: "Fixed"
                val amount = comp.defaultAmount ?: 0.0

                val item = TemplateLineItem(
                    componentId = compId,
                    name = name,
                    calculationType = calculationType,
                    isEarning = !isDeduction,
                    amount = amount
                )

                if (isDeduction) {
                    deductions.add(item)
                } else {
                    earnings.add(item)
                }
            }
        }
    }

    // Calculations
    val grossSalary = remember(earnings.toList()) { earnings.sumOf { it.amount } }
    val totalDeductions = remember(deductions.toList()) { deductions.sumOf { it.amount } }
    val netSalary = (grossSalary - totalDeductions).coerceAtLeast(0.0)

    val addedComponentIds = remember(earnings.toList(), deductions.toList()) {
        (earnings.map { it.componentId } + deductions.map { it.componentId }).toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(
            title = if (isEditMode) "Edit Salary Template" else "Create Template",
            onClose = onClose
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (detailState is UiState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Card 1: Template Information ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius))
                        .background(whiteBg)
                        .padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Template Information",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    Column {
                        FormLabel("Template Name", isRequired = true)
                        FormTextField(
                            value = templateName,
                            onValueChange = { templateName = it },
                            placeholder = "e.g. Standard Executive Template",
                            containerColor = light_grey,
                            borderColor = grey_border
                        )
                    }

                    Column {
                        FormLabel("Annual CTC")
                        FormTextField(
                            value = annualCTC,
                            onValueChange = { annualCTC = it },
                            placeholder = "e.g. 600000",
                            containerColor = light_grey,
                            borderColor = grey_border
                        )
                    }

                    Column {
                        FormLabel("Pay Frequency")
                        FormDropdown(
                            value = payFrequency,
                            expanded = isFrequencyExpanded,
                            onExpandChange = { isFrequencyExpanded = it },
                            options = listOf("Monthly", "Weekly", "Bi-Weekly"),
                            onOptionSelected = {
                                payFrequency = it
                                isFrequencyExpanded = false
                            }
                        )
                    }

                    Column {
                        FormLabel("Description")
                        FormTextArea(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Enter corporate salary structure details...",
                            minLines = 3,
                            maxLines = 4
                        )
                    }
                }

                // ── Card 2: Reusable Earnings Section Card ──
                SalarySectionCard(
                    title = "Earnings",
                    isEarning = true,
                    items = earnings.map {
                        SalarySectionLineItem(
                            id = it.componentId,
                            name = it.name,
                            valueOrAmount = "$ ${String.format("%,.2f", it.amount)}",
                            isEarning = true,
                            tag = it.calculationType
                        )
                    },
                    availableComponents = availableComponents.filter {
                        !it.type.contains("deduct", ignoreCase = true) && !addedComponentIds.contains(it.id)
                    },
                    onAddItem = { _, valueStr, selectedComp ->
                        selectedComp?.let { comp ->
                            val parsedAmount = valueStr.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                            earnings.add(
                                TemplateLineItem(
                                    componentId = comp.id,
                                    name = comp.name,
                                    calculationType = comp.calculationType?.replace("_", " ")?.capitalize() ?: "Fixed",
                                    isEarning = true,
                                    amount = parsedAmount
                                )
                            )
                        }
                    },
                    onDeleteItem = { index -> earnings.removeAt(index) }
                )

                // ── Card 3: Reusable Deductions Section Card ──
                SalarySectionCard(
                    title = "Deductions",
                    isEarning = false,
                    items = deductions.map {
                        SalarySectionLineItem(
                            id = it.componentId,
                            name = it.name,
                            valueOrAmount = "$ ${String.format("%,.2f", it.amount)}",
                            isEarning = false,
                            tag = it.calculationType
                        )
                    },
                    availableComponents = availableComponents.filter {
                        it.type.contains("deduct", ignoreCase = true) && !addedComponentIds.contains(it.id)
                    },
                    onAddItem = { _, valueStr, selectedComp ->
                        selectedComp?.let { comp ->
                            val parsedAmount = valueStr.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                            deductions.add(
                                TemplateLineItem(
                                    componentId = comp.id,
                                    name = comp.name,
                                    calculationType = comp.calculationType?.replace("_", " ")?.capitalize() ?: "Fixed",
                                    isEarning = false,
                                    amount = parsedAmount
                                )
                            )
                        }
                    },
                    onDeleteItem = { index -> deductions.removeAt(index) }
                )

                // ── Card 4: Live Summary Card ──
                TemplateLiveSummaryCard(
                    grossSalary = grossSalary,
                    totalDeductions = totalDeductions,
                    netSalary = netSalary,
                    onPreviewPayslipClick = onPreviewPayslipClick
                )

                // ── Save / Update Template Button ──
                Button(
                    onClick = {
                        if (templateName.isBlank()) {
                            DynamicIslandManager.showError("Please enter template name")
                            return@Button
                        }

                        val componentsPayload = (earnings + deductions).map {
                            SalaryTemplateComponentItem(
                                salaryComponentId = it.componentId,
                                defaultAmount = it.amount
                            )
                        }

                        val request = SaveSalaryTemplateRequest(
                            name = templateName.trim(),
                            description = description.ifBlank { null },
                            annualCTC = annualCTC.toDoubleOrNull() ?: (grossSalary * 12),
                            components = componentsPayload
                        )

                        if (isEditMode && templateId.isNotBlank()) {
                            viewModel.updateSalaryTemplate(
                                templateId = templateId,
                                request = request,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Template updated successfully")
                                    onClose()
                                },
                                onError = { DynamicIslandManager.showError(it) }
                            )
                        } else {
                            viewModel.createSalaryTemplate(
                                request = request,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Template created successfully")
                                    onClose()
                                },
                                onError = { DynamicIslandManager.showError(it) }
                            )
                        }
                    },
                    enabled = saveState !is UiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
                ) {
                    if (saveState is UiState.Loading) {
                        CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = if (isEditMode) "Update Template" else "Save Template",
                            color = whiteBg,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Live breakdown and payslip preview card.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun TemplateLiveSummaryCard(
    grossSalary: Double,
    totalDeductions: Double,
    netSalary: Double,
    onPreviewPayslipClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .padding(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Live Summary",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gross Salary",
                fontSize = tokens.bodySmall,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$ ${String.format("%,.2f", grossSalary)}",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Deductions",
                fontSize = tokens.bodySmall,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "-$ ${String.format("%,.2f", totalDeductions)}",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = redText
            )
        }

        HorizontalDivider(color = BorderGray, thickness = 0.8.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Net Salary",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Text(
                    text = "ESTIMATED PER MONTH",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = mutedText
                )
            }

            Text(
                text = "$ ${String.format("%,.2f", netSalary)}",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(light_grey)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Visual Breakdown",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                val totalSum = (grossSalary + totalDeductions).coerceAtLeast(1.0)
                val earningFraction = (grossSalary / totalSum).toFloat().coerceIn(0.05f, 0.95f)
                val deductionFraction = (totalDeductions / totalSum).toFloat().coerceIn(0.05f, 0.95f)

                Box(modifier = Modifier.weight(earningFraction).fillMaxHeight().background(Primary))
                Box(modifier = Modifier.weight(deductionFraction).fillMaxHeight().background(redText))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary))
                    Text(text = "Earnings", fontSize = tokens.caption, color = TextPrimary)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(redText))
                    Text(text = "Deductions", fontSize = tokens.caption, color = TextPrimary)
                }
            }
        }

        OutlinedButton(
            onClick = onPreviewPayslipClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.buttonHeight),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(Primary)
            )
        ) {
            Text(
                text = "Preview Payslip",
                fontSize = tokens.bodySmall,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}