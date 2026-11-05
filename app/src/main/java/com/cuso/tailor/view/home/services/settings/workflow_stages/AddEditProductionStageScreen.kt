package com.cuso.tailor.view.home.services.settings.workflow_stages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.SaveProductionStageRequest
import com.cuso.tailor.model.settings.ServicesProductionStageDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.viewmodel.SettingsViewModel

private val WorkTypeOptions = listOf("Cutting", "Stitching", "QC", "Trial", "Embroidery", "Finishing", "Ironing", "Custom")
private val StatusOptions = listOf("Active", "Inactive")

@Composable
fun AddEditProductionStageScreen(
    stageToEdit: ServicesProductionStageDto? = null,
    viewModel: SettingsViewModel,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isEditMode = stageToEdit != null
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    var stageName by remember { mutableStateOf(stageToEdit?.name ?: "") }
    var stageCode by remember { mutableStateOf(stageToEdit?.code ?: "") }
    var displayLabel by remember { mutableStateOf(stageToEdit?.displayName ?: "") }
    var workType by remember { mutableStateOf(stageToEdit?.workType ?: "Stitching") }
    var workTypeExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(stageToEdit?.description ?: "") }

    var isAllocationRequired by remember { mutableStateOf(stageToEdit?.isAllocationRequired ?: true) }
    var allowRework by remember { mutableStateOf(stageToEdit?.allowRework ?: true) }

    var status by remember { mutableStateOf(stageToEdit?.status ?: "Active") }
    var statusExpanded by remember { mutableStateOf(false) }

    val isFormValid = stageName.isNotBlank() && stageCode.isNotBlank() && workType.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Scaffold(
            containerColor = Primary_background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                ) {
                    TitleBar(
                        title = if (isEditMode) "Edit Production Stage" else "New Production Stage",
                        onClose = onClose
                    )
                    HorizontalDivider(color = grey_border, thickness = 1.dp)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 16.dp)
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Subtitle
                Text(
                    text = "Register a new workshop production step",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )

                // 1. Stage Name
                Column {
                    FormLabel(text = "Stage Name", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = stageName,
                        onValueChange = {
                            stageName = it
                            if (!isEditMode && stageCode.isBlank()) {
                                stageCode = it.trim().uppercase().replace(" ", "_")
                            }
                        },
                        placeholder = "e.g., Fabric Cutting"
                    )
                }

                // 2. Stage Code
                Column {
                    FormLabel(text = "Stage Code", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = stageCode,
                        onValueChange = { stageCode = it.uppercase() },
                        placeholder = "CUTTING"
                    )
                }

                // 3. Display Label
                Column {
                    FormLabel(text = "Display Label", isRequired = false)
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = displayLabel,
                        onValueChange = { displayLabel = it },
                        placeholder = "Text staff see on app"
                    )
                }

                // 4. Work Type
                Column {
                    FormLabel(text = "Work Type", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormDropdown(
                        value = workType,
                        expanded = workTypeExpanded,
                        onExpandChange = { workTypeExpanded = it },
                        options = WorkTypeOptions,
                        onOptionSelected = { workType = it }
                    )
                }

                // 5. Description / SOP Notes
                Column {
                    FormLabel(text = "Description / SOP Notes", isRequired = false)
                    Spacer(Modifier.height(6.dp))
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Brief instructions on how this stage is executed..."
                    )
                }

                // 6. Checkbox Container Card (Worker Allocation & Allow Rework)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(whiteBg)
                        .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius))
                        .padding(14.dp)
                ) {
                    // Checkbox 1: Worker Allocation Required
                    CheckboxRow(
                        title = "Worker Allocation Required",
                        subtitle = "Must assign specific tailor or operator to this stage",
                        checked = isAllocationRequired,
                        onCheckedChange = { isAllocationRequired = it }
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = grey_border, thickness = 1.dp)
                    Spacer(Modifier.height(10.dp))

                    // Checkbox 2: Allow Rework
                    CheckboxRow(
                        title = "Allow Rework",
                        subtitle = "Can be flagged and sent back for correction",
                        checked = allowRework,
                        onCheckedChange = { allowRework = it }
                    )
                }

                // 7. Status Dropdown
                Column {
                    FormLabel(text = "Status", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormDropdown(
                        value = status,
                        expanded = statusExpanded,
                        onExpandChange = { statusExpanded = it },
                        options = StatusOptions,
                        onOptionSelected = { status = it }
                    )
                }
            }
        }

        // Bottom Navigation Bar using StepNavigationFab
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = when {
                    isSubmitting -> "Saving..."
                    isEditMode -> "Update Stage"
                    else -> "Create Stage"
                },
                enabled = isFormValid && !isSubmitting,
                onClick = {
                    if (isFormValid && !isSubmitting) {
                        val payload = SaveProductionStageRequest(
                            name = stageName.trim(),
                            displayName = displayLabel.trim().ifBlank { stageName.trim() },
                            code = stageCode.trim().uppercase(),
                            description = description.trim(),
                            workType = workType,
                            isAllocationRequired = isAllocationRequired,
                            allowRework = allowRework,
                            status = status
                        )

                        if (isEditMode) {
                            viewModel.updateStage(
                                stageId = stageToEdit.id,
                                request = payload,
                                onSuccess = onClose,
                                onError = { /* Error handled in VM */ }
                            )
                        } else {
                            viewModel.createStage(
                                request = payload,
                                onSuccess = onClose,
                                onError = { /* Error handled in VM */ }
                            )
                        }
                    }
                }
            )
        )
    }
}

@Composable
private fun CheckboxRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = tokens.caption,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Primary,
                checkmarkColor = whiteBg,
                uncheckedColor = iconMuted
            )
        )
    }
}