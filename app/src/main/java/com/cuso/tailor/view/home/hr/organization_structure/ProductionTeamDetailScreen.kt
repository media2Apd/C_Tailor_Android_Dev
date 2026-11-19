package com.cuso.tailor.view.home.hr.organization_structure

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.CreateUpdateProductionTeamRequest
import com.cuso.tailor.model.hr.ProductionShiftDto
import com.cuso.tailor.model.hr.ProductionTeamDto
import com.cuso.tailor.model.hr.ProductionTeamLeadDto
import com.cuso.tailor.model.hr.ProductionTeamMemberDto
import com.cuso.tailor.model.hr.TEAM_ROLE_OPTIONS
import com.cuso.tailor.model.hr.activeMembers
import com.cuso.tailor.model.hr.extractRefId
import com.cuso.tailor.model.hr.extractRefName
import com.cuso.tailor.model.hr.workloadValue
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.ActionDropdownMenu
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardCaptionText
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.DataCardProgressBar
import com.cuso.tailor.view.composable.DataCardStat
import com.cuso.tailor.view.composable.DataCardStatsRow
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.DepartmentUiState
import com.cuso.tailor.viewmodel.DepartmentViewModel
import com.cuso.tailor.viewmodel.HrViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductionTeamDetailScreen(
    teamId: String,
    hrViewModel: HrViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEditTeam: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val team by hrViewModel.selectedProductionTeam.collectAsStateWithLifecycle()
    val assignedWork by hrViewModel.productionTeamAssignedWork.collectAsStateWithLifecycle()
    val isLoading by hrViewModel.isLoadingProductionTeamDetail.collectAsStateWithLifecycle()
    val departmentState by departmentViewModel.uiState.collectAsStateWithLifecycle()
    val directoryMembers by hrViewModel.orgMembers.collectAsStateWithLifecycle()

    val departments = remember(departmentState) {
        (departmentState as? DepartmentUiState.Success)?.departments.orEmpty()
    }

    var selectedTab by rememberSaveable { mutableStateOf("Overview") }
    var showDeleteTeamDialog by remember { mutableStateOf(false) }
    var memberToRemove by remember { mutableStateOf<ProductionTeamMemberDto?>(null) }

    LaunchedEffect(teamId) {
        hrViewModel.fetchProductionTeamById(teamId)
        hrViewModel.fetchProductionTeamAssignedWork(teamId)
        hrViewModel.fetchOrgStructure()
        departmentViewModel.loadDepartments()
    }

    val currentTeam = team
    val isActive = currentTeam?.operationalStatus.equals("Active", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = currentTeam?.name ?: "Production Team",
                subtitle = currentTeam?.teamCode?.takeIf { it.isNotBlank() },
                onClose = onClose,
                trailingContent = {
                    if (currentTeam != null) {
                        StatusBadge(
                            text = if (isActive) "Active Unit" else "Inactive Unit",
                            bgColor = if (isActive) greenBg else light_grey,
                            textColor = if (isActive) greentext else TextSecondary
                        )
                        ActionDropdownMenu(
                            icon = Icons.Default.MoreVert,
                            actions = listOf(
                                MenuAction(
                                    label = "Edit Team",
                                    icon = Icons.Default.Edit,
                                    onClick = { onEditTeam(currentTeam.id) }
                                ),
                                MenuAction(
                                    label = if (isActive) "Mark Inactive" else "Activate Team",
                                    icon = Icons.Default.PowerSettingsNew,
                                    tint = if (isActive) yellowText else greentext,
                                    onClick = {
                                        val next = if (isActive) "Inactive" else "Active"
                                        hrViewModel.updateProductionTeamStatus(
                                            id = currentTeam.id,
                                            newStatus = next,
                                            onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                                            onError = { err -> DynamicIslandManager.showError(err) }
                                        )
                                    }
                                ),
                                MenuAction(
                                    label = "Delete Team",
                                    icon = Icons.Default.Delete,
                                    tint = redText,
                                    textColor = redText,
                                    onClick = { showDeleteTeamDialog = true }
                                )
                            )
                        )
                    }
                }
            )
            HorizontalDivider(color = title_border)

            if (isLoading && currentTeam == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading team details...", fontSize = tokens.bodySmall, color = TextSecondary)
                }
            } else if (currentTeam == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Production team not found.", fontSize = tokens.bodySmall, color = TextSecondary)
                }
            } else {
                val activeMembers = currentTeam.activeMembers()
                val workload = currentTeam.workloadValue()
                val requiredSpecs = currentTeam.specializations.map { it.code }
                val coveredSpecs = activeMembers.map { it.primarySkill }.toSet()
                val depthPercent = if (requiredSpecs.isEmpty()) 0 else {
                    (requiredSpecs.count { it in coveredSpecs } * 100) / requiredSpecs.size
                }

                val resolvedDeptName = remember(currentTeam, departments) {
                    val directName = currentTeam.department.extractRefName("")
                    if (directName.isNotBlank()) directName
                    else departments.find { it._id == currentTeam.department.extractRefId() || it.id == currentTeam.department.extractRefId() }?.name ?: "—"
                }

                val resolvedCoordinatorName = remember(currentTeam, directoryMembers) {
                    val directName = currentTeam.secondaryCoordinator.extractRefName("")
                    if (directName.isNotBlank()) directName
                    else {
                        val coordId = currentTeam.secondaryCoordinator.extractRefId()
                        directoryMembers.find { it.id == coordId }?.fullName ?: "Not set"
                    }
                }

                // Tab bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.8f),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                ) {
                    listOf(
                        "Overview",
                        "Members (${activeMembers.size})",
                        "Assigned Work"
                    ).forEach { tabLabel ->
                        val tabKey = tabLabel.substringBefore(" (")
                        val isSelected = selectedTab == tabKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tabKey },
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            color = if (isSelected) Primary else light_grey
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = tokens.extraPadding * 0.7f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabLabel,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) whiteBg else TextSecondary
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = tokens.extraPadding)
                ) {
                    // Summary KPI Card
                    DataCard(
                        item = currentTeam,
                        title = "Team Summary",
                        topBadgeText = if (workload >= 100) "Full" else if (workload >= 80) "Near Capacity" else "Optimal",
                        topBadgeTextColor = if (workload >= 100) redText else if (workload >= 80) yellowText else greentext,
                        topBadgeBgColor = if (workload >= 100) redBg else if (workload >= 80) yellowBg else greenBg,
                        topBadgeInline = true,
                        content = {
                            DataCardStatsRow(
                                stats = listOf(
                                    DataCardStat("Members", "${activeMembers.size}"),
                                    DataCardStat("Workload", "$workload%"),
                                    DataCardStat("Depth", "$depthPercent%")
                                )
                            )
                            Spacer(Modifier.height(tokens.extraPadding))
                            DataCardProgressBar(
                                progress = workload / 100f,
                                progressColor = if (workload >= 100) redText else if (workload >= 80) yellowText else greentext
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                            DataCardCaptionText("${100 - workload}% buffer capacity available")
                        }
                    )

                    when (selectedTab) {
                        "Overview" -> {
                            OverviewTabContent(
                                team = currentTeam,
                                deptName = resolvedDeptName,
                                coordinatorName = resolvedCoordinatorName
                            )
                        }

                        "Members" -> {
                            MembersTabContent(
                                team = currentTeam,
                                activeMembers = activeMembers,
                                onSetAsTeamLead = { member ->
                                    val updatedMembers = currentTeam.members.map {
                                        when {
                                            it.empId == member.empId -> it.copy(role = "Team_lead")
                                            it.role == "Team_lead" && it.leftOn.isNullOrBlank() -> it.copy(role = "Member")
                                            else -> it
                                        }
                                    }
                                    val req = buildRosterPatchRequest(currentTeam, updatedMembers)
                                    hrViewModel.updateProductionTeam(
                                        id = currentTeam.id,
                                        request = req,
                                        onSuccess = { DynamicIslandManager.showSuccess("Team Lead updated") },
                                        onError = { err -> DynamicIslandManager.showError(err) }
                                    )
                                },
                                onRequestRemove = { member ->
                                    if (member.role == "Team_lead") {
                                        DynamicIslandManager.showError("Assign another Team Lead before removing this member")
                                    } else {
                                        memberToRemove = member
                                    }
                                }
                            )
                        }

                        "Assigned Work" -> {
                            AssignedWorkTabContent(
                                orders = assignedWork?.orders.orEmpty()
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteTeamDialog && currentTeam != null) {
            DeleteModel(
                title = "Delete Production Team",
                message = "Delete \"${currentTeam.name}\" (${currentTeam.teamCode})? This cannot be undone.",
                onDismiss = { showDeleteTeamDialog = false },
                onDelete = {
                    showDeleteTeamDialog = false
                    hrViewModel.deleteProductionTeam(
                        id = currentTeam.id,
                        onSuccess = { msg ->
                            DynamicIslandManager.showSuccess(msg)
                            onClose()
                        },
                        onError = { err -> DynamicIslandManager.showError(err) }
                    )
                }
            )
        }

        memberToRemove?.let { member ->
            if (currentTeam != null) {
                DeleteModel(
                    title = "Remove Team Member",
                    message = "Remove ${member.name} (${member.empId}) from ${currentTeam.name}?",
                    onDismiss = { memberToRemove = null },
                    onDelete = {
                        memberToRemove = null
                        val nowIso = java.time.Instant.now().toString()
                        val updatedMembers = currentTeam.members.map {
                            if (it.empId == member.empId) it.copy(leftOn = nowIso) else it
                        }
                        val req = buildRosterPatchRequest(currentTeam, updatedMembers)
                        hrViewModel.updateProductionTeam(
                            id = currentTeam.id,
                            request = req,
                            onSuccess = { DynamicIslandManager.showSuccess("Member removed from team") },
                            onError = { err -> DynamicIslandManager.showError(err) }
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverviewTabContent(
    team: ProductionTeamDto,
    deptName: String,
    coordinatorName: String
) {
    val tokens = LocalAppTokens.current

    // Team Master Information Card
    DataCard(
        item = team,
        title = "Team Master Information",
        subtitle = "Primary governance and studio designation",
        footerAsRows = true,
        footerFields = listOf(
            DataCardField(label = "Team Name", text = team.name),
            DataCardField(label = "Team Code", text = team.teamCode.ifBlank { "—" }),
            DataCardField(label = "Department", text = deptName),
            DataCardField(label = "Location", text = team.location.ifBlank { "—" }),
            DataCardField(label = "Scope", text = team.scopeDescription.ifBlank { "—" }),
            DataCardField(label = "Revision", text = "v${team.revision}")
        )
    )

    // Appointed Atelier Lead Card
    val lead = team.teamLead
    DataCard(
        item = lead ?: team,
        title = "Appointed Atelier Lead",
        topBadgeText = "Leadership",
        topBadgeTextColor = Primary,
        topBadgeBgColor = primary_light,
        topBadgeInline = true,
        footerAsRows = true,
        footerFields = if (lead != null && lead.name.isNotBlank()) {
            listOf(
                DataCardField(label = "Name", text = lead.name),
                DataCardField(label = "Employee Code", text = lead.empId.ifBlank { "—" }),
                DataCardField(label = "Designation", text = lead.designation.ifBlank { "—" })
            )
        } else {
            listOf(DataCardField(label = "Team Lead", text = "Not Assigned"))
        }
    )

    // Production Stage Competencies Card
    DataCard(
        item = team,
        title = "Production Stage Competencies",
        topBadgeText = "${team.specializations.size} Stages",
        topBadgeTextColor = TextSecondary,
        topBadgeBgColor = grey_border,
        topBadgeInline = true,
        content = {
            if (team.specializations.isEmpty()) {
                Text("No specializations selected.", fontSize = tokens.bodySmall, color = mutedText)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)) {
                    team.specializations.forEachIndexed { index, spec ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            color = Primary_background,
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(modifier = Modifier.padding(tokens.extraPadding)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = spec.name,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Stage ${String.format(java.util.Locale.US, "%02d", index + 1)}",
                                        fontSize = tokens.label,
                                        color = mutedText
                                    )
                                }
                                if (spec.description.isNotBlank()) {
                                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
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
            }
        }
    )

    // Supported Garment Profiles Card
    DataCard(
        item = team,
        title = "Supported Garment Profiles",
        topBadgeText = "${team.garmentProfiles.size} Profiles",
        topBadgeTextColor = TextSecondary,
        topBadgeBgColor = grey_border,
        topBadgeInline = true,
        content = {
            if (team.garmentProfiles.isEmpty()) {
                Text("No garment profiles assigned.", fontSize = tokens.bodySmall, color = mutedText)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                ) {
                    team.garmentProfiles.forEach { profile ->
                        StatusBadge(
                            text = profile,
                            bgColor = primary_light,
                            textColor = Primary
                        )
                    }
                }
            }
        }
    )

    // Working Arrangement Card
    val shiftText = team.defaultShift?.let { shift ->
        if (shift.name.isNotBlank()) "${shift.name} (${shift.startTime} - ${shift.endTime})" else "Not set"
    } ?: "Not set"

    DataCard(
        item = team,
        title = "Working Arrangement",
        topBadgeText = if (team.operationalStatus == "Active") "Active Shift" else "Inactive",
        topBadgeTextColor = if (team.operationalStatus == "Active") greentext else TextSecondary,
        topBadgeBgColor = if (team.operationalStatus == "Active") greenBg else light_grey,
        topBadgeInline = true,
        footerAsRows = true,
        footerFields = listOf(
            DataCardField(label = "Operating Shift", text = shiftText),
            DataCardField(label = "Work Center", text = team.workCenter.ifBlank { "Not set" }),
            DataCardField(label = "QC Coordinator", text = coordinatorName)
        )
    )
}

@Composable
private fun MembersTabContent(
    team: ProductionTeamDto,
    activeMembers: List<ProductionTeamMemberDto>,
    onSetAsTeamLead: (ProductionTeamMemberDto) -> Unit,
    onRequestRemove: (ProductionTeamMemberDto) -> Unit
) {
    val tokens = LocalAppTokens.current
    var memberSearch by remember { mutableStateOf("") }

    SearchFilterBar(
        query = memberSearch,
        onQueryChange = { memberSearch = it },
        placeholder = "Search employee name or code...",
        isSearchBarAlone = true
    )

    val filtered = remember(activeMembers, memberSearch) {
        val q = memberSearch.trim().lowercase()
        if (q.isBlank()) activeMembers
        else activeMembers.filter {
            it.name.lowercase().contains(q) || it.empId.lowercase().contains(q)
        }
    }

    if (filtered.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding * 2),
            contentAlignment = Alignment.Center
        ) {
            Text("No matching members found.", fontSize = tokens.bodySmall, color = mutedText)
        }
    } else {
        filtered.forEach { member ->
            val roleLabel = TEAM_ROLE_OPTIONS.find { it.first == member.role }?.second ?: member.role
            val skillName = team.specializations.find { it.code == member.primarySkill }?.name
                ?: member.primarySkill

            DataCard(
                item = member,
                title = member.name,
                code = member.empId,
                subtitle = member.designation.ifBlank { "Staff Member" },
                topBadgeText = roleLabel,
                topBadgeTextColor = if (member.role == "Team_lead") Primary else TextSecondary,
                topBadgeBgColor = if (member.role == "Team_lead") primary_light else grey_border,
                topBadgeInline = true,
                footerTags = listOfNotNull(
                    skillName.takeIf { it.isNotBlank() }
                ) + member.subSkills,
                actions = listOf(
                    MenuAction(
                        label = "Set as Team Lead",
                        icon = Icons.Default.Star,
                        enabled = member.role != "Team_lead",
                        onClick = { onSetAsTeamLead(member) }
                    ),
                    MenuAction(
                        label = "Remove Member",
                        icon = Icons.Default.Delete,
                        tint = redText,
                        textColor = redText,
                        onClick = { onRequestRemove(member) }
                    )
                )
            )
        }
    }
}

@Composable
private fun AssignedWorkTabContent(
    orders: List<com.cuso.tailor.model.hr.ProductionAssignedOrderDto>
) {
    val tokens = LocalAppTokens.current
    var workSearch by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All Statuses") }
    var statusDropdownOpen by remember { mutableStateOf(false) }
    var selectedScheduleDate by remember { mutableStateOf("") }

    SearchFilterBar(
        query = workSearch,
        onQueryChange = { workSearch = it },
        placeholder = "Search order, garment or customer...",
        isSearchBarAlone = true
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.5f),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        FormDropdown(
            label = "Status Filter",
            value = statusFilter,
            expanded = statusDropdownOpen,
            onExpandChange = { statusDropdownOpen = it },
            options = listOf("All Statuses", "In Progress", "Scheduled", "Pending QC", "Completed"),
            onOptionSelected = { statusFilter = it }
        )

        FormLabel("Scheduled Date Filter")
        DatePickerField(
            value = selectedScheduleDate,
            onDateSelected = { selectedScheduleDate = it }
        )
    }

    val filteredOrders = remember(orders, workSearch, statusFilter, selectedScheduleDate) {
        val q = workSearch.trim().lowercase()
        orders.filter { order ->
            val matchesSearch = q.isBlank() ||
                    order.orderNo.lowercase().contains(q) ||
                    order.garment.lowercase().contains(q) ||
                    order.customerName.lowercase().contains(q)

            val normalizedStatus = when (statusFilter) {
                "In Progress" -> "in_progress"
                "Scheduled" -> "scheduled"
                "Pending QC" -> "pending_qc"
                "Completed" -> "completed"
                else -> ""
            }
            val matchesStatus = normalizedStatus.isBlank() ||
                    order.status.equals(normalizedStatus, ignoreCase = true)

            matchesSearch && matchesStatus
        }
    }

    if (filteredOrders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding * 2),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No assigned production orders found.",
                fontSize = tokens.bodySmall,
                color = mutedText
            )
        }
    } else {
        filteredOrders.forEach { order ->
            val priorityColor = when (order.priority.lowercase()) {
                "urgent" -> Pair(yellowText, yellowBg)
                "high" -> Pair(redText, redBg)
                else -> Pair(TextSecondary, grey_border)
            }

            DataCard(
                item = order,
                title = order.garment,
                code = order.orderNo,
                subtitle = "Customer: ${order.customerName}",
                topBadgeText = order.priority.replaceFirstChar { it.uppercase() },
                topBadgeTextColor = priorityColor.first,
                topBadgeBgColor = priorityColor.second,
                topBadgeInline = true,
                footerAsRows = true,
                footerFields = listOf(
                    DataCardField(label = "Stage", text = order.stage.ifBlank { "—" }),
                    DataCardField(label = "Assigned Staff", text = order.assignedStaff?.name?.ifBlank { "—" } ?: "—"),
                    DataCardField(label = "Status", text = order.status.replace("_", " ").replaceFirstChar { it.uppercase() })
                )
            )
        }
    }
}

private fun buildRosterPatchRequest(
    team: ProductionTeamDto,
    updatedMembers: List<ProductionTeamMemberDto>
): CreateUpdateProductionTeamRequest {
    val activeLead = updatedMembers.find { it.role == "Team_lead" && it.leftOn.isNullOrBlank() }
    return CreateUpdateProductionTeamRequest(
        name = team.name,
        scopeDescription = team.scopeDescription,
        location = team.location,
        department = team.department.extractRefId().ifBlank { null },
        operationalStatus = team.operationalStatus,
        specializations = team.specializations,
        garmentProfiles = team.garmentProfiles,
        members = updatedMembers,
        teamLead = activeLead?.let {
            ProductionTeamLeadDto(empId = it.empId, name = it.name, designation = it.designation)
        },
        defaultShift = team.defaultShift ?: ProductionShiftDto(),
        workCenter = team.workCenter,
        secondaryCoordinator = team.secondaryCoordinator.extractRefId().ifBlank { null },
        lifecycleStatus = team.lifecycleStatus,
        sourceTemplateName = team.sourceTemplateName,
        duplicatedFromCode = team.duplicatedFromCode
    )
}