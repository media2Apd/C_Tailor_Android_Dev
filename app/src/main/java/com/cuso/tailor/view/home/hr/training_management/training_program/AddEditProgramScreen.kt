@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection")

package com.cuso.tailor.view.home.hr.training_management.training_program

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.SaveTrainingProgramRequest
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.UiState
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel

@Composable
fun AddEditProgramScreen(
    programId: String? = null,
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !programId.isNullOrBlank()

    // Form inputs state
    var programTitle by remember { mutableStateOf("") }
    var programCode by remember { mutableStateOf("") }
    var trainingType by remember { mutableStateOf("") }
    var isTypeExpanded by remember { mutableStateOf(false) }
    var durationHours by remember { mutableStateOf("") }
    var deliveryMode by remember { mutableStateOf("") }
    var selectedTrainerId by remember { mutableStateOf("") }
    var trainerNameInput by remember { mutableStateOf("") }
    var isTrainerExpanded by remember { mutableStateOf(false) }
    var programDescription by remember { mutableStateOf("") }

    val detailState by viewModel.trainingDetailState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveTrainingState.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()

    // 1. Fetch employees to populate Trainer dropdown
    LaunchedEffect(Unit) {
        viewModel.fetchMembers(limit = 10)
    }

    // 2. Fetch program detail in Edit Mode
    LaunchedEffect(programId) {
        if (!programId.isNullOrBlank()) {
            viewModel.fetchTrainingProgramDetail(programId)
        } else {
            viewModel.clearTrainingProgramDetail()
            programCode = "TRN-004"
        }
    }

    // 3. Prefill form when detail response arrives
    LaunchedEffect(detailState) {
        if (detailState is UiState.Success) {
            val program = (detailState as UiState.Success).data.data
            if (program != null) {
                programTitle = program.programTitle
                programCode = program.programId
                trainingType = program.trainingType
                durationHours = program.duration.toInt().toString()
                deliveryMode = when (program.modeOfDelivery) {
                    "On_Site" -> "On-site"
                    else -> program.modeOfDelivery
                }
                selectedTrainerId = program.getTrainerId()
                trainerNameInput = program.getTrainerDisplayName()
                programDescription = program.programDescription.orEmpty()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(
            title = if (isEditMode) "Edit Program" else "Add Program",
            onClose = onClose
        )

        // Breadcrumbs
        Row(
            modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "HR", fontSize = tokens.caption, color = mutedText)
            Text(text = ">", fontSize = tokens.caption, color = mutedText)
            Text(text = "Training Management", fontSize = tokens.caption, color = mutedText)
            Text(text = ">", fontSize = tokens.caption, color = mutedText)
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (detailState is UiState.Loading && isEditMode) {
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Program Configuration",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Text(
                        text = "Update or create training program specifications",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                // PROGRAM TITLE
                Column {
                    FormLabel("PROGRAM TITLE", isRequired = true)
                    FormTextField(
                        value = programTitle,
                        onValueChange = { programTitle = it },
                        placeholder = "e.g. Data Science Advanced",
                        containerColor = light_grey,
                        borderColor = grey_border
                    )
                }

                // PROGRAM ID
                Column {
                    FormLabel("PROGRAM ID", isRequired = true)
                    FormTextField(
                        value = programCode,
                        onValueChange = { programCode = it },
                        placeholder = "e.g. TRN-004",
                        containerColor = light_grey,
                        borderColor = grey_border
                    )
                }

                // TRAINING TYPE
                Column {
                    FormLabel("TRAINING TYPE")
                    FormTextField(
                        value = trainingType,
                        onValueChange = { trainingType = it },
                        placeholder = "Training Type"
                    )
                }

                // DURATION (HOURS)
                Column {
                    FormLabel("DURATION (HOURS)")
                    FormTextField(
                        value = durationHours,
                        onValueChange = { durationHours = it },
                        placeholder = "24",
                        containerColor = light_grey,
                        borderColor = grey_border
                    )
                }

                // MODE OF DELIVERY
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FormLabel("MODE OF DELIVERY")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("On-site", "Remote", "Hybrid").forEach { mode ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { deliveryMode = mode }
                            ) {
                                RadioButton(
                                    selected = deliveryMode == mode,
                                    onClick = { deliveryMode = mode },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Primary,
                                        unselectedColor = mutedText
                                    )
                                )
                                Text(
                                    text = mode,
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // TRAINER NAME
                Column {
                    FormLabel("TRAINER NAME", isRequired = true)
                    if (members.isNotEmpty()) {
                        FormDropdown(
                            value = trainerNameInput.ifBlank { "Select Trainer..." },
                            expanded = isTrainerExpanded,
                            onExpandChange = { isTrainerExpanded = it },
                            options = members.map { "${it.firstName} ${it.lastName}".trim() },
                            onOptionSelected = { selectedName ->
                                val matched = members.find { "${it.firstName} ${it.lastName}".trim() == selectedName }
                                if (matched != null) {
                                    selectedTrainerId = matched._id
                                    trainerNameInput = selectedName
                                }
                                isTrainerExpanded = false
                            }
                        )
                    } else {
                        FormTextField(
                            value = trainerNameInput,
                            onValueChange = {
                                trainerNameInput = it
                                selectedTrainerId = it
                            },
                            placeholder = "Search trainer...",
                            containerColor = light_grey,
                            borderColor = grey_border
                        )
                    }
                }

                // PROGRAM DESCRIPTION
                Column {
                    FormLabel("PROGRAM DESCRIPTION")
                    FormTextArea(
                        value = programDescription,
                        onValueChange = { programDescription = it },
                        placeholder = "Describe the curriculum and learning outcomes...",
                        minLines = 4,
                        maxLines = 6
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (programTitle.isBlank()) {
                            DynamicIslandManager.showError("Please enter program title")
                            return@Button
                        }
                        if (programCode.isBlank()) {
                            DynamicIslandManager.showError("Please enter program ID")
                            return@Button
                        }

                        val trainerIdToSubmit = selectedTrainerId.ifBlank {
                            members.firstOrNull()?._id.orEmpty()
                        }

                        if (trainerIdToSubmit.isBlank()) {
                            DynamicIslandManager.showError("Please select a trainer")
                            return@Button
                        }

                        val request = SaveTrainingProgramRequest(
                            programTitle = programTitle.trim(),
                            programId = programCode.trim(),
                            trainingType = trainingType,
                            durationMethod = "Hour",
                            duration = durationHours.toDoubleOrNull() ?: 1.0,
                            modeOfDelivery = if (deliveryMode == "On-site") "On_Site" else deliveryMode,
                            TrainerName = trainerIdToSubmit,
                            programDescription = programDescription.trim(),
                            isActive = true
                        )

                        if (isEditMode && programId.isNotBlank()) {
                            viewModel.updateTrainingProgram(
                                id = programId,
                                request = request,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Training program updated successfully")
                                    onSaveSuccess()
                                },
                                onError = { DynamicIslandManager.showError(it) }
                            )
                        } else {
                            viewModel.createTrainingProgram(
                                request = request,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Training program created successfully")
                                    onSaveSuccess()
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
                        CirculerProgressIndicatorSmall()
                    } else {
                        Text(
                            text = if (isEditMode) "Update Program" else "Save Program",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}