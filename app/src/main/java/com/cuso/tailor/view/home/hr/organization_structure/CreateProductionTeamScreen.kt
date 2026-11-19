@file:Suppress("unused","AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.hr.organization_structure

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.CreateUpdateProductionTeamRequest
import com.cuso.tailor.model.hr.GARMENT_PROFILE_SUGGESTIONS
import com.cuso.tailor.model.hr.PRODUCTION_SHIFTS
import com.cuso.tailor.model.hr.PRODUCTION_SPECIALIZATIONS
import com.cuso.tailor.model.hr.ProductionShiftDto
import com.cuso.tailor.model.hr.ProductionSpecializationDto
import com.cuso.tailor.model.hr.ProductionTeamDto
import com.cuso.tailor.model.hr.ProductionTeamLeadDto
import com.cuso.tailor.model.hr.ProductionTeamMemberDto
import com.cuso.tailor.model.hr.TEAM_ROLE_OPTIONS
import com.cuso.tailor.model.hr.activeMembers
import com.cuso.tailor.model.hr.extractRefId
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.AccordionSection
import com.cuso.tailor.view.composable.AppButton
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.AppRadioButton
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.viewmodel.DepartmentUiState
import com.cuso.tailor.viewmodel.DepartmentViewModel
import com.cuso.tailor.viewmodel.HrViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateProductionTeamScreen(
    teamIdToEdit: String? = null,
    hrViewModel: HrViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSavedSuccess: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !teamIdToEdit.isNullOrBlank()

    val selectedTeam by hrViewModel.selectedProductionTeam.collectAsStateWithLifecycle()
    val allTeams by hrViewModel.allProductionTeams.collectAsStateWithLifecycle()
    val isSubmitting by hrViewModel.isSubmittingProductionTeam.collectAsStateWithLifecycle()
    val directoryMembers by hrViewModel.orgMembers.collectAsStateWithLifecycle()
    val departmentState by departmentViewModel.uiState.collectAsStateWithLifecycle()

    val departments = remember(departmentState) {
        (departmentState as? DepartmentUiState.Success)?.departments.orEmpty()
    }

    LaunchedEffect(teamIdToEdit) {
        hrViewModel.fetchOrgStructure()
        hrViewModel.fetchProductionTeams()
        departmentViewModel.loadDepartments()
        if (isEditMode && teamIdToEdit.isNotBlank()) {
            hrViewModel.fetchProductionTeamById(teamIdToEdit)
        } else {
            hrViewModel.clearSelectedProductionTeam()
        }
    }

    // ── Form States ──
    var teamName by rememberSaveable { mutableStateOf("") }
    var teamCode by rememberSaveable { mutableStateOf("") }
    var scopeDescription by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var selectedDeptId by rememberSaveable { mutableStateOf("") }
    var selectedDeptName by rememberSaveable { mutableStateOf("") }
    var operationalStatus by rememberSaveable { mutableStateOf("Active") }
    var lifecycleStatus by rememberSaveable { mutableStateOf("Published") }
    var duplicatedFromCode by rememberSaveable { mutableStateOf("") }

    var selectedSpecs by remember { mutableStateOf<List<ProductionSpecializationDto>>(emptyList()) }
    var garmentProfiles by remember { mutableStateOf<List<String>>(emptyList()) }
    var rosterMembers by remember { mutableStateOf<List<ProductionTeamMemberDto>>(emptyList()) }
    var pastMembers by remember { mutableStateOf<List<ProductionTeamMemberDto>>(emptyList()) }

    var selectedShiftName by rememberSaveable { mutableStateOf("") }
    var workCenter by rememberSaveable { mutableStateOf("") }
    var coordinatorId by rememberSaveable { mutableStateOf("") }
    var coordinatorName by rememberSaveable { mutableStateOf("") }

    var isPrefilled by rememberSaveable(teamIdToEdit) { mutableStateOf(false) }

    // ── Dropdown & Modal States ──
    var expandedSection by rememberSaveable { mutableStateOf("01") }
    var deptDropdownOpen by remember { mutableStateOf(false) }
    var shiftDropdownOpen by remember { mutableStateOf(false) }
    var coordinatorDropdownOpen by remember { mutableStateOf(false) }
    var leadPickerOpen by remember { mutableStateOf(false) }

    var showAddMembersDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var customProfileInput by remember { mutableStateOf("") }

    // Helper to apply a team DTO (for edit mode or duplicate)
    fun applyTeamData(team: ProductionTeamDto, isDuplicate: Boolean) {
        teamName = if (isDuplicate) "${team.name} (Copy)" else team.name
        teamCode = if (isDuplicate) "" else team.teamCode
        scopeDescription = team.scopeDescription
        location = team.location
        selectedDeptId = team.department.extractRefId()
        operationalStatus = team.operationalStatus.ifBlank { "Active" }
        lifecycleStatus = if (isDuplicate) "Published" else team.lifecycleStatus.ifBlank { "Published" }
        duplicatedFromCode = if (isDuplicate) team.teamCode else team.duplicatedFromCode
        selectedSpecs = team.specializations
        garmentProfiles = team.garmentProfiles
        rosterMembers = team.activeMembers()
        pastMembers = if (isDuplicate) emptyList() else team.members.filter { !it.leftOn.isNullOrBlank() }
        selectedShiftName = team.defaultShift?.name.orEmpty()
        workCenter = team.workCenter
        coordinatorId = team.secondaryCoordinator.extractRefId()
    }

    LaunchedEffect(selectedTeam, isEditMode) {
        if (isEditMode && !isPrefilled && selectedTeam != null) {
            applyTeamData(selectedTeam!!, isDuplicate = false)
            isPrefilled = true
        }
    }

    // Resolve department and coordinator display names once dropdown lists load
    LaunchedEffect(selectedDeptId, departments) {
        if (selectedDeptId.isNotBlank()) {
            val match = departments.find { it._id == selectedDeptId || it.id == selectedDeptId }
            if (match != null) selectedDeptName = match.name
        }
    }

    LaunchedEffect(coordinatorId, directoryMembers) {
        if (coordinatorId.isNotBlank()) {
            val match = directoryMembers.find { it.id == coordinatorId }
            if (match != null) {
                val display = match.fullName.ifBlank { listOfNotNull(match.firstName, match.lastName).joinToString(" ") }
                coordinatorName = display
            }
        }
    }

    val teamLead = remember(rosterMembers) {
        rosterMembers.find { it.role == "Team_lead" }
    }

    // Concurrent membership lookup across other active teams
    val concurrentTeamsByEmp = remember(allTeams, teamIdToEdit) {
        val map = mutableMapOf<String, MutableList<String>>()
        allTeams.filter { it.id != teamIdToEdit && !it.isDeleted && it.lifecycleStatus != "Archived" }
            .forEach { team ->
                team.activeMembers().forEach { member ->
                    if (member.empId.isNotBlank()) {
                        map.getOrPut(member.empId) { mutableListOf() }.add(team.name)
                    }
                }
            }
        map
    }

    // Coverage & readiness metrics
    val requiredSpecCodes = remember(selectedSpecs) { selectedSpecs.map { it.code } }
    val coveredSpecCodes = remember(rosterMembers) { rosterMembers.map { it.primarySkill }.toSet() }
    val missingSpecCodes = remember(requiredSpecCodes, coveredSpecCodes) {
        requiredSpecCodes.filter { it !in coveredSpecCodes }
    }
    val coveragePercent = remember(requiredSpecCodes, missingSpecCodes) {
        if (requiredSpecCodes.isEmpty()) 0
        else (((requiredSpecCodes.size - missingSpecCodes.size) * 100f) / requiredSpecCodes.size).toInt()
    }
    val fullCoverage = requiredSpecCodes.isNotEmpty() && missingSpecCodes.isEmpty()
    val hasSingleLead = rosterMembers.count { it.role == "Team_lead" } == 1
    val concurrentMembers = remember(rosterMembers, concurrentTeamsByEmp) {
        rosterMembers.filter { !concurrentTeamsByEmp[it.empId].isNullOrEmpty() }
    }

    val isPublishedEdit = isEditMode && !lifecycleStatus.equals("Draft", ignoreCase = true)

    fun buildPayload(targetLifecycle: String): CreateUpdateProductionTeamRequest {
        val shiftObj = PRODUCTION_SHIFTS.find { it.name == selectedShiftName }
        return CreateUpdateProductionTeamRequest(
            name = teamName.trim(),
            scopeDescription = scopeDescription.trim(),
            location = location.trim(),
            department = selectedDeptId.ifBlank { null },
            operationalStatus = operationalStatus,
            specializations = selectedSpecs,
            garmentProfiles = garmentProfiles,
            members = rosterMembers + pastMembers,
            teamLead = teamLead?.let {
                ProductionTeamLeadDto(empId = it.empId, name = it.name, designation = it.designation)
            },
            defaultShift = ProductionShiftDto(
                name = shiftObj?.name.orEmpty(),
                startTime = shiftObj?.startTime.orEmpty(),
                endTime = shiftObj?.endTime.orEmpty()
            ),
            workCenter = workCenter.trim(),
            secondaryCoordinator = coordinatorId.ifBlank { null },
            lifecycleStatus = targetLifecycle,
            duplicatedFromCode = duplicatedFromCode
        )
    }

    fun submitTeam(asDraft: Boolean) {
        if (teamName.trim().isBlank()) {
            DynamicIslandManager.showError("Team Name is required")
            return
        }
        if (location.trim().isBlank()) {
            DynamicIslandManager.showError("Location / Atelier Studio is required")
            return
        }
        if (selectedDeptId.isBlank()) {
            DynamicIslandManager.showError("Department Scope is required")
            return
        }
        if (coordinatorId.isBlank()) {
            DynamicIslandManager.showError("Secondary Coordinator is required")
            return
        }

        if (!asDraft) {
            if (selectedSpecs.isEmpty()) {
                DynamicIslandManager.showError("Select at least one specialization")
                return
            }
            if (rosterMembers.isEmpty()) {
                DynamicIslandManager.showError("Add at least one team member")
                return
            }
            if (!hasSingleLead) {
                DynamicIslandManager.showError("Assign exactly one Team Lead")
                return
            }
            if (!fullCoverage) {
                DynamicIslandManager.showError("All selected specializations must be covered by a member primary skill")
                return
            }
        } else if (rosterMembers.isNotEmpty() && !hasSingleLead) {
            DynamicIslandManager.showError("Assign exactly one Team Lead to save the roster")
            return
        }

        val targetLifecycle = if (asDraft) "Draft" else if (isPublishedEdit) lifecycleStatus else "Published"
        val request = buildPayload(targetLifecycle)

        if (isEditMode && teamIdToEdit.isNotBlank()) {
            hrViewModel.updateProductionTeam(
                id = teamIdToEdit,
                request = request,
                onSuccess = {
                    DynamicIslandManager.showSuccess(if (asDraft) "Draft updated" else "Production team updated")
                    onSavedSuccess()
                },
                onError = { err -> DynamicIslandManager.showError(err) }
            )
        } else {
            hrViewModel.createProductionTeam(
                request = request,
                onSuccess = {
                    DynamicIslandManager.showSuccess(if (asDraft) "Draft saved" else "Production team created")
                    onSavedSuccess()
                },
                onError = { err -> DynamicIslandManager.showError(err) }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = if (isEditMode) "Edit Production Team" else "Create Production Team",
                subtitle = "Department: ${selectedDeptName.ifBlank { "Not selected" }}",
                onClose = {
                    if (!isSubmitting) onClose()
                },
                trailingContent = {
                    StatusBadge(
                        text = if (isPublishedEdit) "Published" else "Draft In Progress",
                        bgColor = primary_light,
                        textColor = Primary
                    )
                }
            )
            HorizontalDivider(color = title_border)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = tokens.buttonHeight * 2.5f)
            ) {
                if (!isEditMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showDuplicateDialog = true },
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize * 0.9f)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                            Text(
                                text = "Duplicate Existing Team",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // 01. TEAM INFORMATION
                AccordionSection(
                    title = "01. Team Information",
                    expanded = expandedSection == "01",
                    onHeaderClick = { expandedSection = if (expandedSection == "01") "" else "01" }
                ) {
                    FormLabel("Team Name", isRequired = true)
                    FormTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        placeholder = "e.g. Bridal Couture Team A"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Team Code", isRequired = true)
                    FormTextField(
                        value = teamCode.ifBlank { "Assigned on save" },
                        onValueChange = {},
                        enabled = false
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Team Scope & Functional Description")
                    FormTextArea(
                        value = scopeDescription,
                        onValueChange = { scopeDescription = it },
                        placeholder = "Describe the specialization and scope of this unit..."
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Location / Atelier Studio", isRequired = true)
                    FormTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = "e.g. Main Studio HQ - Atelier Floor 2"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormDropdown(
                        label = "Department Scope",
                        isRequired = true,
                        value = selectedDeptName.ifBlank { "Select Department" },
                        expanded = deptDropdownOpen,
                        onExpandChange = { deptDropdownOpen = it },
                        options = departments.map { it.name }.ifEmpty { listOf("No departments found") },
                        onOptionSelected = { chosenName ->
                            val match = departments.find { it.name == chosenName }
                            if (match != null) {
                                selectedDeptId = match._id
                                selectedDeptName = match.name
                            }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Initial Operational Status")
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
                        listOf(
                            "Active" to "Active Production Unit",
                            "Inactive" to "Inactive / Staging Reserve"
                        ).forEach { (statusValue, statusLabel) ->
                            val isSelected = operationalStatus == statusValue
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { operationalStatus = statusValue },
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                color = if (isSelected) primary_light else whiteBg,
                                border = BorderStroke(1.dp, if (isSelected) Primary else BorderGray)
                            ) {
                                Row(
                                    modifier = Modifier.padding(tokens.extraPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppRadioButton(
                                        selected = isSelected,
                                        onClick = { operationalStatus = statusValue }
                                    )
                                    Text(
                                        text = statusLabel,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 02. SPECIALIZATIONS & GARMENT SCOPE
                AccordionSection(
                    title = "02. Specializations & Garment Scope",
                    expanded = expandedSection == "02",
                    onHeaderClick = { expandedSection = if (expandedSection == "02") "" else "02" }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
                        PRODUCTION_SPECIALIZATIONS.forEach { spec ->
                            val isChecked = selectedSpecs.any { it.code == spec.code }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked && rosterMembers.any { it.primarySkill == spec.code }) {
                                            DynamicIslandManager.showError("Reassign members covering ${spec.name} first")
                                        } else {
                                            selectedSpecs = if (isChecked) {
                                                selectedSpecs.filterNot { it.code == spec.code }
                                            } else {
                                                selectedSpecs + ProductionSpecializationDto(
                                                    code = spec.code,
                                                    name = spec.name,
                                                    tagLabel = spec.tagLabel,
                                                    description = spec.description
                                                )
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                color = if (isChecked) primary_light else whiteBg,
                                border = BorderStroke(1.dp, if (isChecked) Primary else BorderGray)
                            ) {
                                Row(
                                    modifier = Modifier.padding(tokens.extraPadding),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                ) {
                                    AppCheckbox(
                                        checked = isChecked,
                                        onCheckedChange = null
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = spec.name,
                                                fontSize = tokens.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                            StatusBadge(
                                                text = spec.tagLabel,
                                                bgColor = grey_border,
                                                textColor = Primary
                                            )
                                        }
                                        Spacer(Modifier.height(tokens.extraPadding * 0.25f))
                                        Text(
                                            text = spec.description,
                                            fontSize = tokens.caption,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 1.4f))
                    FormLabel("Supported Garment Cut Profiles")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                    ) {
                        garmentProfiles.forEach { profile ->
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                color = title_color
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = tokens.extraPadding,
                                        vertical = tokens.extraPadding * 0.5f
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)
                                ) {
                                    Text(
                                        text = profile,
                                        fontSize = tokens.caption,
                                        color = whiteBg,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = whiteBg,
                                        modifier = Modifier
                                            .size(tokens.iconSize * 0.75f)
                                            .clickable {
                                                garmentProfiles = garmentProfiles.filterNot { it == profile }
                                            }
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.clickable { showAddProfileDialog = true },
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            color = primary_light,
                            border = BorderStroke(1.dp, Primary)
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = tokens.extraPadding,
                                    vertical = tokens.extraPadding * 0.5f
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                                )
                                Text(
                                    text = "Add Profile",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                // 03. MEMBERS ROSTER
                AccordionSection(
                    title = "03. Members Roster (${rosterMembers.size} Enrolled)",
                    expanded = expandedSection == "03",
                    onHeaderClick = { expandedSection = if (expandedSection == "03") "" else "03" }
                ) {
                    AppButton(
                        text = "Add Members",
                        onClick = {
                            if (selectedSpecs.isEmpty()) {
                                DynamicIslandManager.showError("Select specializations in Section 02 first")
                            } else {
                                showAddMembersDialog = true
                            }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    if (rosterMembers.isEmpty()) {
                        Text(
                            text = "No members added yet. Tap Add Members to build the roster.",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            rosterMembers.forEach { member ->
                                var skillOpen by remember { mutableStateOf(false) }
                                var roleOpen by remember { mutableStateOf(false) }
                                val currentSpecName = selectedSpecs.find { it.code == member.primarySkill }?.name
                                    ?: member.primarySkill
                                val currentRoleLabel = TEAM_ROLE_OPTIONS.find { it.first == member.role }?.second
                                    ?: member.role
                                val concurrentList = concurrentTeamsByEmp[member.empId].orEmpty()

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                                    color = whiteBg,
                                    border = BorderStroke(1.dp, BorderGray)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(tokens.extraPadding),
                                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = member.name,
                                                    fontSize = tokens.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "${member.empId} • ${member.designation.ifBlank { "Staff" }}",
                                                    fontSize = tokens.caption,
                                                    color = TextSecondary
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    rosterMembers = rosterMembers.filterNot { it.empId == member.empId }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Remove Member",
                                                    tint = redText,
                                                    modifier = Modifier.size(tokens.iconSize)
                                                )
                                            }
                                        }

                                        if (concurrentList.isNotEmpty()) {
                                            StatusBadge(
                                                text = "Also in: ${concurrentList.joinToString(", ")}",
                                                bgColor = yellowBg,
                                                textColor = yellowText
                                            )
                                        }

                                        FormDropdown(
                                            label = "Primary Certified Skill",
                                            value = currentSpecName.ifBlank { "Select Skill" },
                                            expanded = skillOpen,
                                            onExpandChange = { skillOpen = it },
                                            options = selectedSpecs.map { it.name },
                                            onOptionSelected = { chosenName ->
                                                val spec = selectedSpecs.find { it.name == chosenName }
                                                if (spec != null) {
                                                    rosterMembers = rosterMembers.map {
                                                        if (it.empId == member.empId) it.copy(primarySkill = spec.code) else it
                                                    }
                                                }
                                            }
                                        )

                                        FormDropdown(
                                            label = "Assigned Role",
                                            value = currentRoleLabel,
                                            expanded = roleOpen,
                                            onExpandChange = { roleOpen = it },
                                            options = TEAM_ROLE_OPTIONS.map { it.second },
                                            onOptionSelected = { chosenRole ->
                                                val roleCode = TEAM_ROLE_OPTIONS.find { it.second == chosenRole }?.first ?: "Member"
                                                rosterMembers = rosterMembers.map {
                                                    when {
                                                        it.empId == member.empId -> it.copy(role = roleCode)
                                                        roleCode == "Team_lead" && it.role == "Team_lead" -> it.copy(role = "Member")
                                                        else -> it
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedSpecs.isNotEmpty()) {
                        Spacer(Modifier.height(tokens.extraPadding))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            color = if (fullCoverage) greenBg else yellowBg,
                            border = BorderStroke(1.dp, if (fullCoverage) greentext.copy(alpha = 0.3f) else yellowText.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(tokens.extraPadding)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (fullCoverage) "Complete Skill Coverage" else "Incomplete Skill Coverage",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (fullCoverage) greentext else yellowText
                                    )
                                    StatusBadge(
                                        text = "$coveragePercent% Match",
                                        bgColor = whiteBg,
                                        textColor = if (fullCoverage) greentext else yellowText
                                    )
                                }
                                Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                                Text(
                                    text = if (fullCoverage) {
                                        "All ${requiredSpecCodes.size} selected team specializations are covered by member primary skills."
                                    } else {
                                        val missingNames = missingSpecCodes.map { code ->
                                            PRODUCTION_SPECIALIZATIONS.find { it.code == code }?.name ?: code
                                        }
                                        "Missing coverage: ${missingNames.joinToString(", ")}"
                                    },
                                    fontSize = tokens.caption,
                                    color = if (fullCoverage) greentext else yellowText
                                )
                            }
                        }
                    }
                }

                // 04. LEADERSHIP & WORK ALLOCATION
                AccordionSection(
                    title = "04. Leadership & Shift",
                    expanded = expandedSection == "04",
                    onHeaderClick = { expandedSection = if (expandedSection == "04") "" else "04" }
                ) {
                    if (rosterMembers.isNotEmpty()) {
                        FormDropdown(
                            label = "Designated Team Lead",
                            value = teamLead?.let { "${it.name} (${it.empId})" } ?: "Select Team Lead",
                            expanded = leadPickerOpen,
                            onExpandChange = { leadPickerOpen = it },
                            options = rosterMembers.map { "${it.name} (${it.empId})" },
                            onOptionSelected = { chosen ->
                                val selectedMember = rosterMembers.find { "${it.name} (${it.empId})" == chosen }
                                if (selectedMember != null) {
                                    rosterMembers = rosterMembers.map {
                                        if (it.empId == selectedMember.empId) it.copy(role = "Team_lead")
                                        else if (it.role == "Team_lead") it.copy(role = "Member")
                                        else it
                                    }
                                }
                            }
                        )
                        Spacer(Modifier.height(tokens.extraPadding))
                    }

                    val currentShiftDisplay = PRODUCTION_SHIFTS.find { it.name == selectedShiftName }?.displayLabel
                        ?: "Select Shift"
                    FormDropdown(
                        label = "Default Operating Shift",
                        value = currentShiftDisplay,
                        expanded = shiftDropdownOpen,
                        onExpandChange = { shiftDropdownOpen = it },
                        options = PRODUCTION_SHIFTS.map { it.displayLabel },
                        onOptionSelected = { chosenDisplay ->
                            val shift = PRODUCTION_SHIFTS.find { it.displayLabel == chosenDisplay }
                            selectedShiftName = shift?.name.orEmpty()
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Floor & Work Center")
                    FormTextField(
                        value = workCenter,
                        onValueChange = { workCenter = it },
                        placeholder = "e.g. Atelier Unit #4 - Cutting Bay B"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    val coordinatorOptions = remember(directoryMembers) {
                        directoryMembers.map {
                            val name = it.fullName.ifBlank {
                                listOfNotNull(it.firstName, it.lastName).joinToString(" ")
                            }
                            val code = it.memberId.orEmpty()
                            if (code.isBlank()) name else "$name ($code)"
                        }
                    }
                    FormDropdown(
                        label = "Secondary Coordinator / Handover",
                        isRequired = true,
                        value = coordinatorName.ifBlank { "Select Coordinator" },
                        expanded = coordinatorDropdownOpen,
                        onExpandChange = { coordinatorDropdownOpen = it },
                        options = coordinatorOptions.ifEmpty { listOf("No staff available") },
                        onOptionSelected = { chosen ->
                            val idx = coordinatorOptions.indexOf(chosen)
                            if (idx >= 0) {
                                val chosenMember = directoryMembers[idx]
                                coordinatorId = chosenMember.id
                                coordinatorName = chosen
                            }
                        }
                    )
                }

                // 05. TEAM READINESS PRE-FLIGHT
                AccordionSection(
                    title = "05. Team Readiness Pre-Flight",
                    expanded = expandedSection == "05",
                    onHeaderClick = { expandedSection = if (expandedSection == "05") "" else "05" }
                ) {
                    val checks = listOf(
                        Triple(teamName.isNotBlank(), if (teamCode.isNotBlank()) "Team Name & Code Validated ($teamCode)" else "Team code assigned on save", false),
                        Triple(location.isNotBlank(), if (location.isNotBlank()) "Studio Facility Assigned ($location)" else "Assign a studio facility", false),
                        Triple(rosterMembers.isNotEmpty(), "${rosterMembers.size} Qualified Craftspeople Enrolled (Min. 1 required)", false),
                        Triple(hasSingleLead, if (hasSingleLead) "Primary Team Lead Appointed (${teamLead?.name}, ${teamLead?.empId})" else "Appoint exactly one Primary Team Lead", false),
                        Triple(fullCoverage, if (fullCoverage) "100% Specialization Competency Match" else "$coveragePercent% Specialization Competency Match", false),
                        Triple(
                            concurrentMembers.isEmpty(),
                            if (concurrentMembers.isEmpty()) "No concurrent memberships"
                            else "${concurrentMembers.size} Member(s) with concurrent memberships",
                            concurrentMembers.isNotEmpty()
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)) {
                        checks.forEach { (isOk, label, isWarning) ->
                            val bg = when {
                                isWarning -> yellowBg
                                isOk -> greenBg
                                else -> redBg
                            }
                            val fg = when {
                                isWarning -> yellowText
                                isOk -> greentext
                                else -> redText
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                color = bg
                            ) {
                                Row(
                                    modifier = Modifier.padding(tokens.extraPadding),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)
                                ) {
                                    Icon(
                                        imageVector = if (isOk && !isWarning) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = fg,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = tokens.bodySmall,
                                        color = fg,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isPublishedEdit) {
                    Spacer(Modifier.height(tokens.extraPadding))
                    Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                        OutlinedButton(
                            onClick = { if (!isSubmitting) submitTeam(asDraft = true) },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            border = BorderStroke(1.dp, Primary)
                        ) {
                            Text(
                                text = if (isSubmitting) "Saving Draft..." else "Save as Draft",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = { if (!isSubmitting) onClose() },
            showBackArrow = false,
            backLabel = "Cancel",
            trailingAction = TrailingFabAction.Update(
                label = when {
                    isSubmitting -> "Saving..."
                    isPublishedEdit -> "Save Changes"
                    else -> "Create Team"
                },
                onClick = { if (!isSubmitting) submitTeam(asDraft = false) }
            )
        )
    }

    // ── Add Members Picker Dialog ──
    if (showAddMembersDialog) {
        AddProductionMembersDialog(
            directoryMembers = directoryMembers,
            existingEmpIds = rosterMembers.map { it.empId }.toSet(),
            specializations = selectedSpecs,
            onDismiss = { showAddMembersDialog = false },
            onAddMembers = { added ->
                var leadAlreadyAssigned = rosterMembers.any { it.role == "Team_lead" }
                val normalized = added.map { member ->
                    val role = if (!leadAlreadyAssigned) {
                        leadAlreadyAssigned = true
                        "Team_lead"
                    } else {
                        "Member"
                    }
                    member.copy(role = role)
                }
                rosterMembers = rosterMembers + normalized
                showAddMembersDialog = false
            }
        )
    }

    // ── Duplicate Existing Team Dialog ──
    if (showDuplicateDialog) {
        DuplicateTeamDialog(
            teams = allTeams.filter { !it.isDeleted && !it.isTemplate },
            onDismiss = { showDuplicateDialog = false },
            onSelectTeam = { team ->
                showDuplicateDialog = false
                hrViewModel.fetchProductionTeamForDuplicate(
                    id = team.id,
                    onLoaded = { fullTeam ->
                        applyTeamData(fullTeam, isDuplicate = true)
                        DynamicIslandManager.showSuccess("Team duplicated")
                    },
                    onError = { err -> DynamicIslandManager.showError(err) }
                )
            }
        )
    }

    // ── Add Garment Profile Dialog ──
    if (showAddProfileDialog) {
        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
            title = {
                Text(
                    text = "Add Garment Cut Profile",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    FormTextField(
                        value = customProfileInput,
                        onValueChange = { customProfileInput = it },
                        placeholder = "Enter profile name..."
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)
                    ) {
                        GARMENT_PROFILE_SUGGESTIONS.filter { it !in garmentProfiles }.forEach { suggestion ->
                            Surface(
                                modifier = Modifier.clickable { customProfileInput = suggestion },
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                color = primary_light
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = tokens.caption,
                                    color = Primary,
                                    modifier = Modifier.padding(
                                        horizontal = tokens.extraPadding * 0.8f,
                                        vertical = tokens.extraPadding * 0.4f
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customProfileInput.trim()
                        if (trimmed.isNotBlank() && trimmed !in garmentProfiles) {
                            garmentProfiles = garmentProfiles + trimmed
                        }
                        customProfileInput = ""
                        showAddProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Add", color = whiteBg)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AddProductionMembersDialog(
    directoryMembers: List<com.cuso.tailor.model.hr.OrgMemberDto>,
    existingEmpIds: Set<String>,
    specializations: List<ProductionSpecializationDto>,
    onDismiss: () -> Unit,
    onAddMembers: (List<ProductionTeamMemberDto>) -> Unit
) {
    val tokens = LocalAppTokens.current
    var search by remember { mutableStateOf("") }
    var selectedMap by remember { mutableStateOf<Map<String, ProductionTeamMemberDto>>(emptyMap()) }
    val defaultSkillCode = specializations.firstOrNull()?.code.orEmpty()

    val available = remember(directoryMembers, existingEmpIds, search) {
        val q = search.trim().lowercase()
        directoryMembers.filter { member ->
            val code = member.memberId.orEmpty().ifBlank { member.id }
            val fullName = member.fullName.ifBlank {
                listOfNotNull(member.firstName, member.lastName).joinToString(" ")
            }
            code.isNotBlank() && code !in existingEmpIds && (
                    q.isBlank() || fullName.lowercase().contains(q) || code.lowercase().contains(q)
                    )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(
                    title = "Add Team Members",
                    onClose = onDismiss
                )
                SearchFilterBar(
                    query = search,
                    onQueryChange = { search = it },
                    placeholder = "Search by employee name or code...",
                    isSearchBarAlone = true
                )
                HorizontalDivider(color = title_border)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    items(available, key = { it.id }) { member ->
                        val empCode = member.memberId.orEmpty().ifBlank { member.id }
                        val fullName = member.fullName.ifBlank {
                            listOfNotNull(member.firstName, member.lastName).joinToString(" ")
                        }
                        val isPicked = selectedMap.containsKey(empCode)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMap = if (isPicked) {
                                        selectedMap - empCode
                                    } else {
                                        selectedMap + (
                                                empCode to ProductionTeamMemberDto(
                                                    empId = empCode,
                                                    name = fullName,
                                                    designation = member.designationId?.name.orEmpty(),
                                                    primarySkill = defaultSkillCode
                                                )
                                                )
                                    }
                                },
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            color = if (isPicked) primary_light else whiteBg,
                            border = BorderStroke(1.dp, if (isPicked) Primary else BorderGray)
                        ) {
                            Row(
                                modifier = Modifier.padding(tokens.extraPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                AppCheckbox(checked = isPicked, onCheckedChange = null)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fullName,
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$empCode • ${member.designationId?.name.orEmpty().ifBlank { "Staff" }}",
                                        fontSize = tokens.caption,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = title_border)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onAddMembers(selectedMap.values.toList()) },
                        enabled = selectedMap.isNotEmpty(),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(tokens.buttonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                        Text("Add (${selectedMap.size})", color = whiteBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicateTeamDialog(
    teams: List<ProductionTeamDto>,
    onDismiss: () -> Unit,
    onSelectTeam: (ProductionTeamDto) -> Unit
) {
    val tokens = LocalAppTokens.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(
                    title = "Duplicate Existing Team",
                    onClose = onDismiss
                )
                HorizontalDivider(color = title_border)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                ) {
                    items(teams, key = { it.id }) { team ->
                        DataCard(
                            item = team,
                            title = team.name,
                            code = team.teamCode,
                            subtitle = team.location.ifBlank { "No location" },
                            onClick = { onSelectTeam(team) }
                        )
                    }
                }
            }
        }
    }
}