@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection", "DEPRECATION")

package com.cuso.tailor.view.home.hr.training_management.assign_training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedButton
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
import com.cuso.tailor.model.hr.SaveAssignedTrainingRequest
import com.cuso.tailor.model.settings.DepartmentItem
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.UiState
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.DepartmentUiState
import com.cuso.tailor.viewmodel.DepartmentViewModel
import com.cuso.tailor.viewmodel.HrViewModel

/**
 * Screen allowing users to assign training programs to departments and employees.
 * Fetches programs, departments, and members dynamically from their respective ViewModels.
 */
@Composable
fun AssignTrainingScreen(
    assignmentId: String? = null,
    viewModel: HrViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onAssignSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !assignmentId.isNullOrBlank()

    // 1. Program State
    var selectedProgramId by remember { mutableStateOf("") }
    var selectedProgramTitle by remember { mutableStateOf("") }
    var isProgramExpanded by remember { mutableStateOf(false) }

    // 2. Department State
    var selectedDepartmentId by remember { mutableStateOf("") }
    var selectedDepartmentName by remember { mutableStateOf("") }
    var isDepartmentExpanded by remember { mutableStateOf(false) }

    // 3. Employee / Member State (List for multi-selection)
    var selectedEmployeeIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedEmployeeNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var isEmployeeExpanded by remember { mutableStateOf(false) }

    // 4. Schedule Dates State
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    // 5. Mode State
    var deliveryMode by remember { mutableStateOf("") }
    var isModeExpanded by remember { mutableStateOf(false) }

    // 6. Notes State
    var notesText by remember { mutableStateOf("") }

    // Observers
    val detailState by viewModel.assignedTrainingDetailState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveAssignedTrainingState.collectAsStateWithLifecycle()
    val availablePrograms by viewModel.trainingPrograms.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val departmentUiState by departmentViewModel.uiState.collectAsStateWithLifecycle()

    val departmentList: List<DepartmentItem> = remember(departmentUiState) {
        if (departmentUiState is DepartmentUiState.Success) {
            (departmentUiState as DepartmentUiState.Success).departments
        } else {
            emptyList()
        }
    }

    // Load initial data (Programs, Members, Departments)
    LaunchedEffect(Unit) {
        viewModel.fetchTrainingPrograms()
        viewModel.fetchMembers(limit = 10)
        departmentViewModel.loadDepartments()
    }

    // Load assignment details if in edit mode
    LaunchedEffect(assignmentId) {
        if (!assignmentId.isNullOrBlank()) {
            viewModel.fetchAssignedTrainingDetail(assignmentId)
        } else {
            viewModel.clearAssignedTrainingDetail()
        }
    }

    // Prefill form in edit mode
    LaunchedEffect(detailState) {
        if (detailState is UiState.Success) {
            val item = (detailState as UiState.Success).data.data
            if (item != null) {
                selectedProgramId = item.getProgramId()
                selectedProgramTitle = item.getProgramTitle()

                selectedDepartmentId = item.departmentIds.firstOrNull().orEmpty()
                val matchedDept = departmentList.find { it._id == selectedDepartmentId }
                if (matchedDept != null) {
                    selectedDepartmentName = matchedDept.name
                }

                selectedEmployeeIds = item.getMemberIds()
                selectedEmployeeNames = members
                    .filter { it._id in selectedEmployeeIds }
                    .map { "${it.firstName} ${it.lastName}".trim() }

                startDate = item.startDate?.take(10).orEmpty()
                endDate = item.endDate?.take(10).orEmpty()

                deliveryMode = when (item.mode) {
                    "On_Site" -> "On-site Workshop"
                    "Virtual" -> "Virtual (MS Teams)"
                    else -> item.mode
                }
                notesText = item.notes.orEmpty()
            }
        }
    }

    // Sync employee names when members list finishes loading
    LaunchedEffect(members, selectedEmployeeIds) {
        if (selectedEmployeeIds.isNotEmpty() && selectedEmployeeNames.isEmpty()) {
            selectedEmployeeNames = members
                .filter { it._id in selectedEmployeeIds }
                .map { "${it.firstName} ${it.lastName}".trim() }
        }
    }
    // Sync department name if department list loads after detail arrives
    LaunchedEffect(departmentList, selectedDepartmentId) {
        if (selectedDepartmentId.isNotBlank() && selectedDepartmentName.isBlank()) {
            departmentList.find { it._id == selectedDepartmentId }?.let {
                selectedDepartmentName = it.name
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(
            title = if (isEditMode) "Edit Training Assignment" else "Training Assignment",
            onClose = onClose
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (detailState is UiState.Loading && isEditMode) {
            ListSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── 1. SELECT PROGRAM ──
                Column {
                    FormLabel("SELECT PROGRAM", isRequired = true)
                    FormDropdown(
                        value = selectedProgramTitle.ifBlank { "Choose a training program" },
                        expanded = isProgramExpanded,
                        onExpandChange = { isProgramExpanded = it },
                        options = if (availablePrograms.isNotEmpty()) {
                            availablePrograms.map { it.programTitle }
                        } else {
                            listOf("Choose a training program")
                        },
                        onOptionSelected = { title ->
                            selectedProgramTitle = title
                            selectedProgramId = availablePrograms.find { it.programTitle == title }?.id.orEmpty()
                            isProgramExpanded = false
                        }
                    )
                }

                // ── 2. DEPARTMENTS ──
                Column {
                    FormLabel("DEPARTMENTS")
                    FormDropdown(
                        value = selectedDepartmentName.ifBlank { "Select departments" },
                        expanded = isDepartmentExpanded,
                        onExpandChange = { isDepartmentExpanded = it },
                        options = if (departmentList.isNotEmpty()) {
                            departmentList.map { it.name }
                        } else {
                            listOf("Select departments")
                        },
                        onOptionSelected = { name ->
                            selectedDepartmentName = name
                            selectedDepartmentId = departmentList.find { it.name == name }?._id.orEmpty()
                            isDepartmentExpanded = false
                        }
                    )
                }

                // ── 3. EMPLOYEES ──
                // ── 3. EMPLOYEES ──
                Column {
                    FormLabel("EMPLOYEES", isRequired = true)
                    FormDropdown(
                        expanded = isEmployeeExpanded,
                        onExpandChange = { isEmployeeExpanded = it },
                        options = members.map { "${it.firstName} ${it.lastName}".trim() },
                        isMultiSelect = true,
                        selectedOptions = selectedEmployeeNames,
                        onMultiOptionSelected = { updatedNames ->
                            selectedEmployeeNames = updatedNames
                            // Map selected names to their IDs for the payload
                            selectedEmployeeIds = members
                                .filter { "${it.firstName} ${it.lastName}".trim() in updatedNames }
                                .map { it._id }
                        }
                    )
                }

                // ── 4. SCHEDULE (START & END) SIDE-BY-SIDE ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Schedule (Start)
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("SCHEDULE (START)", isRequired = true)
                        DatePickerField(
                            value = startDate,
                            onDateSelected = { startDate = it }
                        )
                    }

                    // Schedule (End)
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("SCHEDULE (END)", isRequired = true)
                        DatePickerField(
                            value = endDate,
                            onDateSelected = { endDate = it }
                        )
                    }
                }

                // ── 5. MODE ──
                Column {
                    FormLabel("MODE")
                    FormDropdown(
                        value = deliveryMode,
                        expanded = isModeExpanded,
                        onExpandChange = { isModeExpanded = it },
                        options = listOf("Virtual (MS Teams)", "On-site", "Hybrid"),
                        onOptionSelected = {
                            deliveryMode = it
                            isModeExpanded = false
                        }
                    )
                }

                // ── 6. NOTES ──
                Column {
                    FormLabel("NOTES")
                    FormTextArea(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = "Specific instructions for the trainee...",
                        minLines = 4,
                        maxLines = 6
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── Actions: Discard & Assign Program ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(BorderGray)
                        )
                    ) {
                        Text(
                            text = "Discard",
                            fontSize = tokens.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            val programIdToSubmit = selectedProgramId.ifBlank {
                                availablePrograms.firstOrNull()?.id.orEmpty()
                            }
                            if (selectedEmployeeIds.isEmpty()) {
                                DynamicIslandManager.showError("Please select at least one employee")
                                return@Button
                            }

                            if (programIdToSubmit.isBlank()) {
                                DynamicIslandManager.showError("Please select a training program")
                                return@Button
                            }
                            if (startDate.isBlank()) {
                                DynamicIslandManager.showError("Please select a start date")
                                return@Button
                            }
                            if (endDate.isBlank()) {
                                DynamicIslandManager.showError("Please select an end date")
                                return@Button
                            }

                            val apiMode = when {
                                deliveryMode.contains("On-site", ignoreCase = true) -> "On_Site"
                                deliveryMode.contains("Hybrid", ignoreCase = true) -> "Hybrid"
                                else -> "Virtual"
                            }

                            val formattedStart = if (startDate.contains("T")) startDate else startDate
                            val formattedEnd = if (endDate.contains("T")) endDate else endDate

                            val request = SaveAssignedTrainingRequest(
                                trainingProgramId = programIdToSubmit,
                                departmentIds = if (selectedDepartmentId.isNotBlank()) listOf(selectedDepartmentId) else emptyList(),
                                organizationMemberIds = selectedEmployeeIds,
                                startDate = formattedStart,
                                endDate = formattedEnd,
                                mode = apiMode,
                                notes = notesText.trim(),
                                status = "Assigned"
                            )

                            if (isEditMode && assignmentId.isNotBlank()) {
                                viewModel.updateAssignedTraining(
                                    id = assignmentId,
                                    request = request,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Training assignment updated successfully")
                                        onAssignSuccess()
                                    },
                                    onError = { DynamicIslandManager.showError(it) }
                                )
                            } else {
                                viewModel.createAssignedTraining(
                                    request = request,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Training program assigned successfully")
                                        onAssignSuccess()
                                    },
                                    onError = { DynamicIslandManager.showError(it) }
                                )
                            }
                        },
                        enabled = saveState !is UiState.Loading,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
                    ) {
                        if (saveState is UiState.Loading) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Text(
                                text = if (isEditMode) "Update Program" else "Assign Program",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}