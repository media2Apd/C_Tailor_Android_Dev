@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.hr.settings.piece_rate_setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.SavePieceRateRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.currentCurrency
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SettingsViewModel

private val STATUS_OPTIONS = listOf("Active", "Scheduled", "Inactive")

@Composable
fun AddEditPieceRateScreen(
    rateId: String? = null,
    viewModel: SettingsViewModel,
    onClose: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val currency = currentCurrency()
    val isEdit = !rateId.isNullOrBlank()

    // Required collections for selection
    val stages by viewModel.stages.collectAsState()
    val garments by viewModel.garments.collectAsState()
    val isSubmitting by viewModel.isSubmittingPieceRate.collectAsState()

    var selectedWorkflowId by remember { mutableStateOf("") }
    var selectedWorkflowName by remember { mutableStateOf("") }
    var workflowExpanded by remember { mutableStateOf(false) }

    var selectedGarmentId by remember { mutableStateOf("") }
    var selectedGarmentName by remember { mutableStateOf("") }
    var garmentExpanded by remember { mutableStateOf(false) }

    var regularRateText by remember { mutableStateOf("") }
    var otRateText by remember { mutableStateOf("") }

    var effectiveFrom by remember { mutableStateOf("") }
    var effectiveUntil by remember { mutableStateOf("") }

    var status by remember { mutableStateOf("Active") }
    var statusExpanded by remember { mutableStateOf(false) }

    var notesText by remember { mutableStateOf("") }

    // Fetch dependencies
    LaunchedEffect(Unit) {
        viewModel.fetchStages()
        viewModel.fetchGarments()
    }

    // Prefill in edit mode
    LaunchedEffect(rateId) {
        if (isEdit) {
            viewModel.fetchPieceRateById(rateId) { item ->
                selectedWorkflowId = item.workflowId?.id.orEmpty()
                selectedWorkflowName = item.workflowId?.displayName ?: item.workflowId?.name.orEmpty()

                selectedGarmentId = item.garmentId?.id.orEmpty()
                selectedGarmentName = item.garmentId?.displayName ?: item.garmentId?.name.orEmpty()

                regularRateText = item.regularRatePerPiece.toInt().toString()
                otRateText = item.otRatePerPiece.toInt().toString()

                effectiveFrom = item.effectiveFrom?.take(10).orEmpty()
                effectiveUntil = item.effectiveUntil?.take(10).orEmpty()

                status = item.status
                notesText = item.notes.orEmpty()
            }
        }
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = if (isEdit) "Edit Piece Rate" else "Add Piece Rate",
                subtitle = "Configure regular and overtime piece rate for tailor work",
                onClose = onClose
            )
        },
        bottomBar = {
            Surface(
                color = whiteBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text("Cancel", color = TextPrimary, fontSize = tokens.bodySmall)
                    }

                    Button(
                        onClick = {
                            val regRate = regularRateText.toDoubleOrNull() ?: 0.0
                            val otRate = otRateText.toDoubleOrNull() ?: 0.0

                            if (selectedWorkflowId.isBlank()) {
                                viewModel.showError("Please select a workflow stage")
                                return@Button
                            }
                            if (effectiveFrom.isBlank()) {
                                viewModel.showError("Effective from date is required")
                                return@Button
                            }

                            val request = SavePieceRateRequest(
                                workflowId = selectedWorkflowId,
                                garmentId = selectedGarmentId.ifBlank { null },
                                regularRatePerPiece = regRate,
                                otRatePerPiece = otRate,
                                effectiveFrom = effectiveFrom,
                                effectiveUntil = effectiveUntil.ifBlank { null },
                                status = status,
                                notes = notesText.ifBlank { null }
                            )

                            if (isEdit) {
                                viewModel.updatePieceRate(
                                    id = rateId,
                                    request = request,
                                    onSuccess = { onSavedSuccess() }
                                )
                            } else {
                                viewModel.createPieceRate(
                                    request = request,
                                    onSuccess = { onSavedSuccess() }
                                )
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = if (isSubmitting) "Saving..." else "Save Rate",
                            color = whiteBg,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        containerColor = Primary_background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Informational Notice Banner
            item {
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = primary_light.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Creating a new rate does not overwrite existing rates. Historical earnings will continue using the rate that was active when the work was completed.",
                            fontSize = tokens.caption,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Form Fields Card
            item {
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Workflow dropdown
                        FormDropdown(
                            label = "Workflow",
                            isRequired = true,
                            value = selectedWorkflowName.ifBlank { "Select Workflow" },
                            expanded = workflowExpanded,
                            onExpandChange = { workflowExpanded = it },
                            options = stages.map { it.displayName },
                            onOptionSelected = { chosenName ->
                                val match = stages.find { it.displayName == chosenName }
                                selectedWorkflowId = match?.id.orEmpty()
                                selectedWorkflowName = chosenName
                            }
                        )

                        // Garment dropdown
                        FormDropdown(
                            label = "Garment",
                            isRequired = true,
                            value = selectedGarmentName.ifBlank { "Select Garment" },
                            expanded = garmentExpanded,
                            onExpandChange = { garmentExpanded = it },
                            options = garments.map { it.displayName ?: it.name },
                            onOptionSelected = { chosenName ->
                                val match = garments.find { (it.displayName ?: it.name) == chosenName }
                                selectedGarmentId = match?.id.orEmpty()
                                selectedGarmentName = chosenName
                            }
                        )

                        // Rates Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(text = "Regular Rate / Piece", isRequired = true)
                                FormTextField(
                                    value = regularRateText,
                                    onValueChange = { regularRateText = it },
                                    placeholder = "${currency}0",
                                    trailingText = "/ piece"
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(text = "OT Rate / Piece", isRequired = true)
                                FormTextField(
                                    value = otRateText,
                                    onValueChange = { otRateText = it },
                                    placeholder = "${currency}0",
                                    trailingText = "/ piece"
                                )
                            }
                        }

                        // Effective Dates
                        FormLabel(text = "Effective From", isRequired = true)
                        DatePickerField(
                            value = effectiveFrom,
                            onDateSelected = { effectiveFrom = it }
                        )

                        FormLabel(text = "Effective Until (Optional)")
                        DatePickerField(
                            value = effectiveUntil,
                            onDateSelected = { effectiveUntil = it }
                        )

                        // Status dropdown
                        FormDropdown(
                            label = "Status",
                            isRequired = true,
                            value = status,
                            expanded = statusExpanded,
                            onExpandChange = { statusExpanded = it },
                            options = STATUS_OPTIONS,
                            onOptionSelected = { status = it }
                        )

                        // Notes
                        FormLabel(text = "Notes (Optional)")
                        FormTextArea(
                            value = notesText,
                            onValueChange = { notesText = it },
                            placeholder = "Add any additional notes or description..."
                        )
                    }
                }
            }

            // Rate History Section (in Edit mode)
            if (isEdit) {
                item {
                    Text(
                        text = "Rate History",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = whiteBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$effectiveFrom – ${effectiveUntil.ifBlank { "Present" }}",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = greenBg
                                ) {
                                    Text(
                                        text = status,
                                        fontSize = 10.sp,
                                        color = greentext,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Regular: $currency$regularRateText   •   OT Rate: $currency$otRateText",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}