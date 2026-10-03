@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "unused_variable",
    "unused_parameter"
)

package com.cuso.tailor.view.home.hr.payroll_management.salary_components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.SalaryComponentRequest
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.darkPurple
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.SalarySectionCard
import com.cuso.tailor.view.composable.SalarySectionLineItem
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel

data class SalaryComponentRowItem(
    var name: String,
    var valueOrFormula: String
)

@Composable
fun SalaryComponentFormScreen(
    componentId: String? = null,
    onClose: () -> Unit,
    viewModel: HrViewModel
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !componentId.isNullOrBlank()

    val componentDetail by viewModel.salaryComponentDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingSalaryComponentDetail.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingSalaryComponent.collectAsStateWithLifecycle()

    val earningsList = remember { mutableStateListOf<SalaryComponentRowItem>() }
    val deductionsList = remember { mutableStateListOf<SalaryComponentRowItem>() }

    LaunchedEffect(componentId) {
        if (isEditMode) {
            viewModel.fetchSalaryComponentDetail(componentId)
        } else {
            viewModel.clearSalaryComponentDetail()
        }
    }

    LaunchedEffect(componentDetail) {
        componentDetail?.let { detail ->
            val formattedValue = detail.formula?.ifBlank { null }
                ?: detail.value?.let { "$it" }
                ?: ""

            if (detail.type.equals("deduction", ignoreCase = true)) {
                deductionsList.clear()
                deductionsList.add(SalaryComponentRowItem(detail.name, formattedValue))
                earningsList.clear()
            } else {
                earningsList.clear()
                earningsList.add(SalaryComponentRowItem(detail.name, formattedValue))
                deductionsList.clear()
            }
        }
    }

    val basicPayAmount = remember(earningsList.toList()) {
        val basicRow = earningsList.find { it.name.contains("Basic", ignoreCase = true) }
            ?: earningsList.firstOrNull()
        basicRow?.valueOrFormula?.filter { it.isDigit() || it == '.' }?.toDoubleOrNull() ?: 0.0
    }

    val liveGrossSalary = remember(earningsList.toList(), basicPayAmount) {
        earningsList.sumOf { item ->
            val text = item.valueOrFormula.trim()
            if (text.contains("*")) {
                val multiplier = text.substringAfter("*").filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                basicPayAmount * multiplier
            } else {
                text.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
            }
        }
    }

    val liveTotalDeductions = remember(deductionsList.toList(), basicPayAmount) {
        deductionsList.sumOf { item ->
            val text = item.valueOrFormula.trim()
            if (text.contains("*")) {
                val multiplier = text.substringAfter("*").filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                basicPayAmount * multiplier
            } else {
                text.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
            }
        }
    }

    val liveTakeHomeAmount = (liveGrossSalary - liveTotalDeductions).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(
            title = if (isEditMode) "Edit Component" else "Salary Component",
            onClose = onClose
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoadingDetail && isEditMode) {
            ListSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── 1. Reusable Earnings Section Card ──
                SalarySectionCard(
                    title = "Earnings",
                    isEarning = true,
                    items = earningsList.map {
                        SalarySectionLineItem(
                            name = it.name,
                            valueOrAmount = it.valueOrFormula,
                            isEarning = true
                        )
                    },
                    availableComponents = null, // Free text input mode
                    onAddItem = { name, value, _ ->
                        earningsList.add(SalaryComponentRowItem(name, value))
                    },
                    onDeleteItem = { index -> earningsList.removeAt(index) }
                )

                // ── 2. Reusable Deductions Section Card ──
                SalarySectionCard(
                    title = "Deductions",
                    isEarning = false,
                    items = deductionsList.map {
                        SalarySectionLineItem(
                            name = it.name,
                            valueOrAmount = it.valueOrFormula,
                            isEarning = false
                        )
                    },
                    availableComponents = null, // Free text input mode
                    onAddItem = { name, value, _ ->
                        deductionsList.add(SalaryComponentRowItem(name, value))
                    },
                    onDeleteItem = { index -> deductionsList.removeAt(index) }
                )

                // ── 3. Live Summary Card ──
                SalaryLiveSummaryCard(
                    grossSalary = liveGrossSalary,
                    totalDeductions = liveTotalDeductions,
                    takeHomeAmount = liveTakeHomeAmount
                )

                // ── 4. Save Button ──
                Button(
                    onClick = {
                        val filledEarning = earningsList.firstOrNull { it.name.isNotBlank() }
                        val filledDeduction = deductionsList.firstOrNull { it.name.isNotBlank() }
                        val isDeduction = filledDeduction != null && filledEarning == null
                        val activeItem = if (isDeduction) filledDeduction else (filledEarning ?: earningsList.firstOrNull())

                        val enteredName = activeItem?.name?.trim().orEmpty()
                        if (enteredName.isBlank()) {
                            DynamicIslandManager.showError("Please enter component name")
                            return@Button
                        }

                        val rawValueOrFormula = activeItem?.valueOrFormula?.replace("$", "")?.trim().orEmpty()
                        if (rawValueOrFormula.isBlank()) {
                            DynamicIslandManager.showError("Please enter an amount or formula")
                            return@Button
                        }

                        val hasFormula = rawValueOrFormula.contains("*") || rawValueOrFormula.any { it.isLetter() }
                        val dynamicCalculationType = if (hasFormula) "percentage_of_basic" else "fixed"
                        val dynamicFormula = if (hasFormula) rawValueOrFormula else null
                        val dynamicValue = rawValueOrFormula.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0

                        val request = SalaryComponentRequest(
                            name = enteredName,
                            nameInPayslip = enteredName,
                            type = if (isDeduction) "deduction" else "earning",
                            category = componentDetail?.category ?: "custom",
                            calculationType = dynamicCalculationType,
                            value = dynamicValue,
                            formula = dynamicFormula,
                            wageCeiling = componentDetail?.wageCeiling,
                            eligibilityWaitingPeriodMonths = componentDetail?.eligibilityWaitingPeriodMonths ?: 0,
                            isTaxable = componentDetail?.isTaxable ?: false,
                            isProRata = componentDetail?.isProRata ?: false,
                            isFlexibleBenefit = componentDetail?.isFlexibleBenefit ?: false,
                            maxFlexibleAmount = componentDetail?.maxFlexibleAmount,
                            countryCode = componentDetail?.countryCode ?: "IN",
                            region = componentDetail?.region ?: "Tamil Nadu",
                            displayOrder = componentDetail?.displayOrder ?: 1,
                            isActive = componentDetail?.isActive ?: true
                        )

                        if (isEditMode) {
                            viewModel.updateSalaryComponent(
                                id = componentId,
                                request = request,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Salary component updated successfully")
                                    onClose()
                                },
                                onError = { errorMsg -> DynamicIslandManager.showError(errorMsg) }
                            )
                        } else {
                            viewModel.createSalaryComponent(
                                request = request,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Salary component created successfully")
                                    onClose()
                                },
                                onError = { errorMsg -> DynamicIslandManager.showError(errorMsg) }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CirculerProgressIndicatorSmall()
                    } else {
                        Text(
                            text = if (isEditMode) "Update Component" else "Save Component",
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
 * Live Summary Card showing gross, deductions, and calculated net take-home salary.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun SalaryLiveSummaryCard(
    grossSalary: Double,
    totalDeductions: Double,
    takeHomeAmount: Double
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(Primary)
            .padding(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Live Summary",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = whiteBg
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gross Salary",
                fontSize = tokens.bodySmall,
                color = whiteBg.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$${String.format("%,.2f", grossSalary)}",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = whiteBg
            )
        }

        HorizontalDivider(color = whiteBg.copy(alpha = 0.2f), thickness = 0.8.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Deductions",
                fontSize = tokens.bodySmall,
                color = whiteBg.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "($${String.format("%,.2f", totalDeductions)})",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = whiteBg
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "TAKE-HOME AMOUNT",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = whiteBg.copy(alpha = 0.7f)
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$${String.format("%,.2f", takeHomeAmount)}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = whiteBg
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "/ mo",
                    fontSize = tokens.caption,
                    color = whiteBg.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(darkPurple)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = whiteBg.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = "Calculations update in real-time as you modify formulas and amounts above.",
                fontSize = tokens.caption,
                color = whiteBg.copy(alpha = 0.85f),
                fontWeight = FontWeight.Normal
            )
        }
    }
}