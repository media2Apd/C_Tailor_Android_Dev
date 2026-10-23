package com.cuso.tailor.view.home.inventory.expense_management.expense_type

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.ExpenseTypeDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel

@Composable
fun AddExpenseTypeScreen(
    expenseTypeId: String? = null,
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSavedSuccess: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    val expenseTypes by viewModel.expenseTypes.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingExpenseType.collectAsStateWithLifecycle()

    val isEditMode = !expenseTypeId.isNullOrBlank()
    val existingType: ExpenseTypeDto? = remember(expenseTypeId, expenseTypes) {
        if (isEditMode) expenseTypes.find { it.id == expenseTypeId } else null
    }

    var name by remember { mutableStateOf(existingType?.name.orEmpty()) }
    var description by remember { mutableStateOf(existingType?.description.orEmpty()) }
    var defaultLimit by remember {
        mutableStateOf(
            if (existingType != null && existingType.defaultLimit > 0) {
                if (existingType.defaultLimit % 1.0 == 0.0) existingType.defaultLimit.toLong().toString()
                else existingType.defaultLimit.toString()
            } else ""
        )
    }
    var isActive by remember {
        mutableStateOf(existingType?.status?.equals("Active", ignoreCase = true) ?: true)
    }

    LaunchedEffect(existingType) {
        if (existingType != null) {
            name = existingType.name.orEmpty()
            description = existingType.description.orEmpty()
            defaultLimit = if (existingType.defaultLimit > 0) {
                if (existingType.defaultLimit % 1.0 == 0.0) existingType.defaultLimit.toLong().toString()
                else existingType.defaultLimit.toString()
            } else ""
            isActive = existingType.status?.equals("Active", ignoreCase = true) ?: true
        }
    }

    Scaffold(
        containerColor = whiteBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Expense Type" else "Add Expense Type",
                onClose = onClose
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text(
                            text = "Discard",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                DynamicIslandManager.showError("Please enter Expense Type Name")
                                return@Button
                            }

                            val limitNumber = defaultLimit.toDoubleOrNull() ?: 0.0
                            val statusValue = if (isActive) "Active" else "Inactive"

                            viewModel.createOrUpdateExpenseType(
                                id = expenseTypeId,
                                name = name.trim(),
                                description = description.trim(),
                                defaultLimit = limitNumber,
                                status = statusValue,
                                onSuccess = { message ->
                                    DynamicIslandManager.showSuccess(message)
                                    onSavedSuccess()
                                },
                                onError = { errorMsg ->
                                    DynamicIslandManager.showError(errorMsg)
                                }
                            )
                        },
                        enabled = name.isNotBlank() && !isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        } else {
                            Text(
                                text = "Save Type",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PanelBg)
                .verticalScroll(scrollState)
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Column {
                Text(
                    text = "Add Expense Type",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Configure a new category for Reimbursement",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
            }

            // Top Card: Name and Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding)
                ) {
                    Text(
                        text = "Expense Type Name",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(whiteBg)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (name.isEmpty()) {
                            Text(
                                text = "e.g. Software Subscription",
                                fontSize = tokens.bodySmall,
                                color = mutedText
                            )
                        }
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = tokens.bodySmall,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(Primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Description",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(whiteBg)
                            .padding(12.dp)
                    ) {
                        if (description.isEmpty()) {
                            Text(
                                text = "Reimbursement rules for online tools & platform licensing...",
                                fontSize = tokens.bodySmall,
                                color = mutedText
                            )
                        }
                        BasicTextField(
                            value = description,
                            onValueChange = { description = it },
                            textStyle = TextStyle(
                                fontSize = tokens.bodySmall,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(Primary),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Bottom Card: Default Limit & Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding)
                ) {
                    Text(
                        text = "Default Limit (USD)",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(whiteBg)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (defaultLimit.isEmpty()) {
                                    Text(
                                        text = "2,500.00",
                                        fontSize = tokens.bodySmall,
                                        color = mutedText
                                    )
                                }
                                BasicTextField(
                                    value = defaultLimit,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == '.' }) {
                                            defaultLimit = input
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = TextStyle(
                                        fontSize = tokens.bodySmall,
                                        color = TextPrimary
                                    ),
                                    cursorBrush = SolidColor(Primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = "USD",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = headerGrey
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Status",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Active categories can be selected",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = whiteBg,
                                checkedTrackColor = Primary,
                                uncheckedThumbColor = whiteBg,
                                uncheckedTrackColor = BorderGray,
                                uncheckedBorderColor = BorderGray
                            )
                        )
                    }
                }
            }
        }
    }
}