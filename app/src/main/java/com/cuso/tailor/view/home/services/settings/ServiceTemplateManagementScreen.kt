@file:Suppress("UNUSED_PARAMETER", "unused", "unusedVariable", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.services.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.view.home.sales.customer.OrderStatusStepper
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.ServicesViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel

// ─────────────────────────────────────────────────────────────
// Local Helper UI Models for Wizard Pages
// ─────────────────────────────────────────────────────────────

data class WorkflowStepItem(
    val sequence: Int,
    val stage: String,
    val workType: String = stage,
    val isRequired: Boolean = true,
    val allowRework: Boolean = true,
    val instructions: String = "",
    val stageId: String = ""
)

data class OptionalWorkItem(
    val name: String,
    val workType: String = name,
    val isRequired: Boolean = false,
    val notes: String = "",
    val status: String = "Active"
)

// ─────────────────────────────────────────────────────────────
// Screen 1: Service Templates List Screen
// ─────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ServiceTemplateListScreen(
    onClose: () -> Unit = {},
    onAddNewTemplate: () -> Unit = {},
    onViewTemplate: (ProductionTemplateDto) -> Unit = {},
    onEditTemplate: (ProductionTemplateDto) -> Unit = {},
    templateViewModel: ServicesViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val uiState by templateViewModel.uiState.collectAsState()

    var templateToDelete by remember { mutableStateOf<ProductionTemplateDto?>(null) }

    LaunchedEffect(Unit) {
        templateViewModel.loadTemplates()
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        fab = FabConfig(
            label = "Add New",
            icon = Icons.Default.Add,
            onClick = onAddNewTemplate,
            endPadding = 16.dp,
            bottomPadding = 50.dp,
            draggable = true
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Service Templates",
                onClose = onClose
            )

            HorizontalDivider(color = title_border)

            Row(modifier = Modifier.fillMaxWidth()) {
                SearchFilterBar(
                    query = uiState.searchQuery,
                    onQueryChange = { templateViewModel.onSearchQueryChanged(it) },
                    placeholder = "Search Templates...",
                    accentColor = Primary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 6.dp)
            ) {
                Text(
                    text = "Service Templates",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "Create and manage reusable service workflows for garments.",
                    fontSize = tokens.bodySmall,
                    color = headerGrey
                )
            }

            Spacer(Modifier.height(8.dp))

            if (uiState.isLoading && uiState.templates.isEmpty()) {
               ListSkeleton()
            } else if (!uiState.errorMessage.isNullOrBlank() && uiState.templates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage ?: "Failed to load templates",
                            fontSize = tokens.bodyMedium,
                            color = redText
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { templateViewModel.loadTemplates() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Retry", color = whiteBg, fontSize = tokens.bodySmall)
                        }
                    }
                }
            } else if (uiState.templates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No templates found.",
                        fontSize = tokens.bodyMedium,
                        color = headerGrey
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(uiState.templates, key = { it.id }) { item ->
                        val isDraft = item.status.equals("DRAFT", ignoreCase = true)
                        val badgeBg = if (isDraft) primary_light else greenBg
                        val badgeTextColor = if (isDraft) Primary else darkGreenBg
                        val garmentCount = item.safeGarmentCount
                        val stepCount = item.safeStepCount
                        val relativeTime = formatRelativeTime(item.updatedAt ?: item.createdAt)

                        DataCard(
                            item = item,
                            title = item.name ?: "Untitled Template",
                            titleColor = TextPrimary,
                            topBadgeText = item.status,
                            topBadgeTextColor = badgeTextColor,
                            topBadgeBgColor = badgeBg,
                            topBadgeShowDot = false,
                            topBadgeInline = true,
                            showHeaderDivider = false,
                            onClick = {
                                templateViewModel.setSelectedTemplateDirect(item)
                                onViewTemplate(item)
                            },
                            actions = listOf(
                                MenuAction("View", Icons.Default.Visibility) {
                                    templateViewModel.setSelectedTemplateDirect(item)
                                    onViewTemplate(item)
                                },
                                MenuAction("Edit", Icons.Default.Edit) {
                                    templateViewModel.setSelectedTemplateDirect(item)
                                    onEditTemplate(item)
                                },
                                MenuAction(
                                    label = "Delete",
                                    icon = Icons.Default.Delete,
                                    tint = redText,
                                    textColor = redText
                                ) {
                                    templateToDelete = item
                                }
                            ),
                            content = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            templateViewModel.setSelectedTemplateDirect(item)
                                            onViewTemplate(item)
                                        },
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(primary_light)
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = item.segment?.displayName
                                                ?: item.segment?.name
                                                ?: "All Segments",
                                            fontSize = tokens.caption,
                                            color = Primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_shopping_bag),
                                                contentDescription = null,
                                                tint = iconMuted,
                                                modifier = Modifier.size(tokens.iconSize)
                                            )
                                            Text(
                                                text = "Garment: ",
                                                fontSize = tokens.bodySmall,
                                                color = headerGrey
                                            )
                                            Text(
                                                text = "$garmentCount",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = null,
                                                tint = iconMuted,
                                                modifier = Modifier.size(tokens.iconSize)
                                            )
                                            Text(
                                                text = "$stepCount Steps",
                                                fontSize = tokens.bodySmall,
                                                color = headerGrey
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccessTime,
                                                contentDescription = null,
                                                tint = iconMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = relativeTime,
                                                fontSize = tokens.caption,
                                                color = iconMuted
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    templateToDelete?.let { template ->
        DeleteModel(
            title = "Delete Service Template?",
            message = "You are about to delete \"${template.name ?: "this template"}\".\nThis will remove it from future order creation.",
            onDismiss = { templateToDelete = null },
            onDelete = {
                val idToDelete = template.id
                templateToDelete = null
                templateViewModel.deleteTemplate(idToDelete) {
                    templateViewModel.loadTemplates()
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatRelativeTime(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "Just now"
    return try {
        val instant = java.time.Instant.parse(dateString)
        val now = java.time.Instant.now()
        val duration = java.time.Duration.between(instant, now)

        val seconds = duration.seconds
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 30 -> "${days / 7}w ago"
            days < 365 -> "${days / 30}mo ago"
            else -> "${days / 365}y ago"
        }
    } catch (_: Exception) {
        "Just now"
    }
}

// ─────────────────────────────────────────────────────────────
// Screen 2: Create / Edit Service Template Wizard Screen (4 Steps)
// ─────────────────────────────────────────────────────────────

@Composable
fun CreateServiceTemplateWizardScreen(
    templateIdToEdit: String? = null,
    onClose: () -> Unit = {},
    onTemplateCreated: () -> Unit = onClose,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    templateViewModel: ServicesViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var currentStep by remember { mutableIntStateOf(0) }
    var isAddingWorkflowStepPage by remember { mutableStateOf(false) }

    var addGarmentSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var backgroundBlur by remember { mutableStateOf(0.dp) }

    val isSheetOpen = addGarmentSheetState != SheetValue.Hidden

    val wizardStepLabels = listOf(
        "Basic Info",
        "Garment",
        "Workflow",
        "Review"
    )

    // Data streams from ViewModels
    val segments by settingsViewModel.segments.collectAsState()
    val garments by settingsViewModel.garments.collectAsState()
    val apiStages by templateViewModel.stages.collectAsState()
    val isLoadingStages by templateViewModel.isLoadingStages.collectAsState()
    val selectedDetail by templateViewModel.selectedTemplate.collectAsState()
    val isLoadingDetail by templateViewModel.isLoadingDetail.collectAsState()

    // Initial API loads
    LaunchedEffect(Unit) {
        if (segments.isEmpty()) settingsViewModel.fetchSegments()
        if (garments.isEmpty()) settingsViewModel.fetchGarments()
        templateViewModel.loadStages()
    }

    // Trigger View-One API if in Edit Mode
    LaunchedEffect(templateIdToEdit) {
        if (!templateIdToEdit.isNullOrBlank()) {
            templateViewModel.fetchTemplateDetail(templateIdToEdit)
        }
    }

    // Step 1: Basic Info States
    var templateName by remember { mutableStateOf("") }
    var templateCode by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("Custom Tailoring") }
    var serviceTypeExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var statusActive by remember { mutableStateOf(true) }

    // Step 2: Garment & Measurement States
    var applyToGroup by remember { mutableStateOf(false) }
    var selectedCategoryTabIndex by remember { mutableIntStateOf(0) }
    var selectedGarmentId by remember { mutableStateOf("") }
    var selectedGroupGarmentIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var garmentGroup by remember { mutableStateOf("") }

    val currentSegment = segments.getOrNull(selectedCategoryTabIndex)
    val filteredGarments = remember(currentSegment, garments) {
        if (currentSegment != null) {
            garments.filter { garment ->
                garment.applicableSegments.isEmpty() ||
                        garment.applicableSegments.any { it.id == currentSegment.id }
            }
        } else {
            garments
        }
    }

    // Auto-select garment for Create Mode
    LaunchedEffect(filteredGarments) {
        if (templateIdToEdit.isNullOrBlank() && selectedGarmentId.isBlank() && filteredGarments.isNotEmpty()) {
            selectedGarmentId = filteredGarments.first().id
        }
    }

    // Step 3: Workflow States (Backed by API stages)
    var workflowSteps by remember { mutableStateOf<List<WorkflowStepItem>>(emptyList()) }

    // Synchronize initial workflow steps from API stages when in Create Mode
    LaunchedEffect(apiStages) {
        if (templateIdToEdit.isNullOrBlank() && workflowSteps.isEmpty() && apiStages.isNotEmpty()) {
            workflowSteps = apiStages.mapIndexed { index, stageDto ->
                WorkflowStepItem(
                    sequence = index + 1,
                    stage = stageDto.effectiveTitle,
                    workType = stageDto.workType ?: stageDto.effectiveTitle,
                    isRequired = !stageDto.workType.equals("Trial", ignoreCase = true),
                    allowRework = stageDto.allowRework,
                    instructions = stageDto.description.orEmpty(),
                    stageId = stageDto.id
                )
            }
        }
    }

    // Step 4: Activation Dialog
    var showActivateConfirmDialog by remember { mutableStateOf(false) }

    // Prefill data when in Edit Mode
    var isPrefilled by remember { mutableStateOf(false) }
    LaunchedEffect(selectedDetail, segments, garments) {
        val detail = selectedDetail
        if (!templateIdToEdit.isNullOrBlank() && detail != null && detail.id == templateIdToEdit && !isPrefilled) {
            templateName = detail.name.orEmpty()
            templateCode = detail.code.orEmpty()
            description = detail.description.orEmpty()
            statusActive = detail.status.equals("Active", ignoreCase = true)

            // Segment tab prefill
            detail.segment?.let { seg ->
                val foundIdx = segments.indexOfFirst { it.id == seg.id }
                if (foundIdx >= 0) selectedCategoryTabIndex = foundIdx
            }

            // Single Garment Prefill
            if (detail.garment != null) {
                applyToGroup = false
                selectedGarmentId = detail.garment.id
            }

            // Multiple Garments (Group) Prefill
            val rawIds = detail.rawGarmentIds.orEmpty().mapNotNull { el ->
                if (el.isJsonObject) el.asJsonObject.get("_id")?.asString
                else if (el.isJsonPrimitive) el.asString
                else null
            }
            if (rawIds.isNotEmpty()) {
                applyToGroup = true
                selectedGroupGarmentIds = rawIds.toSet()
                selectedGarmentId = rawIds.first()
            }

            // Stages Prefill from existing template
            val detailStages = detail.stages.orEmpty()
            if (detailStages.isNotEmpty()) {
                workflowSteps = detailStages.map { st ->
                    WorkflowStepItem(
                        sequence = st.displayOrder,
                        stage = st.stageDetail?.displayName ?: st.stageDetail?.name ?: "Stage ${st.displayOrder}",
                        workType = st.stageDetail?.workType ?: "General",
                        isRequired = st.isMandatory,
                        allowRework = st.allowRework,
                        instructions = st.instructions.orEmpty(),
                        stageId = st.stageDetail?.id ?: st.id.orEmpty()
                    )
                }
            }
            isPrefilled = true
        }
    }

    // Display loader if template details are being fetched
    if (isLoadingDetail && !isPrefilled && !templateIdToEdit.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(whiteBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary, modifier = Modifier.size(36.dp))
        }
        return
    }

    // Add Step Sub-page
    if (isAddingWorkflowStepPage) {
        AddWorkflowStepPage(
            sequenceNumber = workflowSteps.size + 1,
            onBack = { isAddingWorkflowStepPage = false },
            onCreateNewStage = { newStageRequest, onDone ->
                templateViewModel.createStage(newStageRequest) { createdStage ->
                    val newStep = WorkflowStepItem(
                        sequence = workflowSteps.size + 1,
                        stage = createdStage.effectiveTitle,
                        workType = createdStage.workType ?: createdStage.effectiveTitle,
                        isRequired = true,
                        allowRework = createdStage.allowRework,
                        instructions = createdStage.description.orEmpty(),
                        stageId = createdStage.id
                    )
                    workflowSteps = workflowSteps + newStep
                    onDone()
                    isAddingWorkflowStepPage = false
                }
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                TitleBar(
                    title = if (templateIdToEdit.isNullOrBlank()) "Create Service Template" else "Edit Service Template",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .blurScrim(backgroundBlur)
            ) {
                OrderStatusStepper(
                    stepLabels = wizardStepLabels,
                    currentStep = currentStep,
                    modifier = Modifier
                        .background(whiteBg)
                        .padding(vertical = 12.dp)
                )

                HorizontalDivider(color = title_border)

                val selectedGarmentObj = garments.find { it.id == selectedGarmentId }
                val displayGarmentName = selectedGarmentObj?.displayName ?: selectedGarmentObj?.name ?: "All Garments"

                if (currentStep > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(grey_border)
                            .padding(horizontal = tokens.screenPadding, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Template: $templateName",
                            fontSize = tokens.caption,
                            color = textSubdued,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(grey_border)
                            .padding(horizontal = tokens.screenPadding, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Garment: $displayGarmentName",
                            fontSize = tokens.caption,
                            color = textSubdued,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
                        .padding(bottom = 90.dp)
                ) {
                    when (currentStep) {
                        0 -> StepOneBasicInfo(
                            templateName = templateName,
                            onTemplateNameChange = { templateName = it },
                            serviceType = serviceType,
                            serviceTypeExpanded = serviceTypeExpanded,
                            onServiceTypeExpandedChange = { serviceTypeExpanded = it },
                            onServiceTypeSelect = { serviceType = it },
                            description = description,
                            onDescriptionChange = { description = it },
                            statusActive = statusActive,
                            onStatusChange = { statusActive = it }
                        )

                        1 -> StepTwoGarmentAndMeasurement(
                            applyToGroup = applyToGroup,
                            onApplyToGroupChange = { applyToGroup = it },
                            segments = segments,
                            garments = garments,
                            selectedCategoryTabIndex = selectedCategoryTabIndex,
                            onCategoryTabSelect = { selectedCategoryTabIndex = it },
                            selectedGarmentId = selectedGarmentId,
                            onGarmentSelect = { chosen -> selectedGarmentId = chosen.id },
                            selectedGroupGarmentIds = selectedGroupGarmentIds,
                            onToggleGroupGarment = { gid ->
                                selectedGroupGarmentIds = if (gid in selectedGroupGarmentIds) {
                                    selectedGroupGarmentIds - gid
                                } else {
                                    selectedGroupGarmentIds + gid
                                }
                            },
                            onAddGarmentClick = { addGarmentSheetState = SheetValue.Expanded }
                        )

                        2 -> StepThreeWorkflow(
                            workflowSteps = workflowSteps,
                            isLoadingStages = isLoadingStages,
                            onStepsChange = { workflowSteps = it },
                            onAddStepClick = { isAddingWorkflowStepPage = true }
                        )

                        3 -> StepFiveReviewAndActivate(
                            workflowSteps = workflowSteps,
                            onEditWorkflow = { currentStep = 2 }
                        )
                    }
                }
            }
        }

        if (!isSheetOpen) {
            StepNavigationFab(
                showBack = true,
                onBack = {
                    if (currentStep > 0) currentStep-- else onClose()
                },
                backLabel = "Back",
                showBackArrow = true,
                showTrailingArrow = true,
                trailingAction = TrailingFabAction.Next(
                    label = if (currentStep == 3) {
                        if (templateIdToEdit.isNullOrBlank()) "Activate Template" else "Update Template"
                    } else "Next",
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            showActivateConfirmDialog = true
                        }
                    }
                )
            )
        }

        SmoothBottomSheet(
            state = addGarmentSheetState,
            onStateChange = { newState ->
                addGarmentSheetState = newState
                if (newState == SheetValue.Hidden) {
                    backgroundBlur = 0.dp
                }
            },
            peekHeight = 520.dp,
            topInset = 66.dp,
            sheetBackgroundColor = whiteBg,
            collapsedCornerRadius = 24.dp,
            dragCloseEnabled = true,
            scrollableContent = true,
            onDismissRequest = {
                backgroundBlur = 0.dp
                addGarmentSheetState = SheetValue.Hidden
            },
            onBlurScrimChange = { r, _ ->
                if (addGarmentSheetState != SheetValue.Hidden) {
                    backgroundBlur = r
                }
            }
        ) {
            val currentSegmentName = segments.getOrNull(selectedCategoryTabIndex)?.displayName ?: "General"
            AddGarmentSheetContent(
                category = currentSegmentName,
                garmentGroup = garmentGroup.ifBlank { "Standard" },
                onDismiss = {
                    backgroundBlur = 0.dp
                    addGarmentSheetState = SheetValue.Hidden
                },
                onCreate = { _, _, _ ->
                    backgroundBlur = 0.dp
                    addGarmentSheetState = SheetValue.Hidden
                    settingsViewModel.fetchGarments()
                }
            )
        }
    }

    if (showActivateConfirmDialog) {
        ActivateTemplateConfirmDialog(
            isEdit = !templateIdToEdit.isNullOrBlank(),
            onDismiss = { showActivateConfirmDialog = false },
            onConfirm = {
                showActivateConfirmDialog = false
                val generatedCode = templateCode.ifBlank {
                    templateName.trim().uppercase().replace(Regex("[^A-Z0-9]+"), "_") + "_WF"
                }

                val stagePayloads = workflowSteps.mapIndexed { idx, st ->
                    CreateTemplateStageItem(
                        stageId = st.stageId.ifBlank { "STAGE_${idx + 1}" },
                        displayOrder = idx + 1,
                        isMandatory = st.isRequired,
                        isAllocationRequired = true,
                        allowRework = st.allowRework,
                        instructions = st.instructions.ifBlank { "Complete according to approved specs." }
                    )
                }
                val isEdit = !templateIdToEdit.isNullOrBlank()

                val request = CreateTemplateRequest(
                    name = templateName.ifBlank { "Standard Pipeline" },
                    code = generatedCode,
                    description = description.ifBlank { "Automated production workflow." },
                    segmentId = if (!isEdit) null else currentSegment?.id,
                    garmentId = if (isEdit || applyToGroup) null else selectedGarmentId.takeIf { it.isNotBlank() },
                    stages = stagePayloads,
                    status = if (statusActive) "Active" else "Inactive"
                )

                if (templateIdToEdit.isNullOrBlank()) {
                    templateViewModel.createTemplate(request) {
                        onTemplateCreated()
                    }
                } else {
                    templateViewModel.updateTemplate(templateIdToEdit, request) {
                        onTemplateCreated()
                    }
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Step 1: Basic Information Content
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepOneBasicInfo(
    templateName: String,
    onTemplateNameChange: (String) -> Unit,
    serviceType: String,
    serviceTypeExpanded: Boolean,
    onServiceTypeExpandedChange: (Boolean) -> Unit,
    onServiceTypeSelect: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    statusActive: Boolean,
    onStatusChange: (Boolean) -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Basic Information",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        HorizontalDivider(color = sectionBorder)

        Column {
            FormLabel("Template Name", isRequired = true)
            FormTextField(
                value = templateName,
                onValueChange = onTemplateNameChange,
                placeholder = "Enter template name"
            )
        }

        Column {
            FormDropdown(
                label = "Service Type",
                value = serviceType,
                expanded = serviceTypeExpanded,
                onExpandChange = onServiceTypeExpandedChange,
                options = listOf("Custom Tailoring", "General Service / Alteration"),
                onOptionSelected = onServiceTypeSelect
            )
        }

        Column {
            FormLabel("Description (Optional)")
            FormTextArea(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = "Add floor layout or special remarks...",
                minLines = 4,
                maxLines = 6
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Status",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = textSubdued
            )
            MiniSwitch(
                checked = statusActive,
                onCheckedChange = onStatusChange
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step 2: Garment & Measurement Content
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepTwoGarmentAndMeasurement(
    applyToGroup: Boolean,
    onApplyToGroupChange: (Boolean) -> Unit,
    segments: List<SegmentItem>,
    garments: List<GarmentItem>,
    selectedCategoryTabIndex: Int,
    onCategoryTabSelect: (Int) -> Unit,
    selectedGarmentId: String,
    onGarmentSelect: (GarmentItem) -> Unit,
    selectedGroupGarmentIds: Set<String>,
    onToggleGroupGarment: (String) -> Unit,
    onAddGarmentClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var garmentDropdownExpanded by remember { mutableStateOf(false) }

    val tabNames = remember(segments) {
        segments.map { it.displayName.ifBlank { it.name } }
    }

    val currentSegment = remember(segments, selectedCategoryTabIndex) {
        segments.getOrNull(selectedCategoryTabIndex)
    }

    val filteredGarments = remember(currentSegment, garments) {
        if (currentSegment != null) {
            garments.filter { garment ->
                garment.applicableSegments.any { applicable ->
                    applicable.id.equals(currentSegment.id, ignoreCase = true)
                }
            }
        } else {
            emptyList()
        }
    }

    LaunchedEffect(filteredGarments) {
        if (!applyToGroup && filteredGarments.isNotEmpty()) {
            val existsInFiltered = filteredGarments.any { it.id == selectedGarmentId }
            if (!existsInFiltered) {
                onGarmentSelect(filteredGarments.first())
            }
        }
    }

    val activeSingleGarment = remember(selectedGarmentId, filteredGarments) {
        filteredGarments.find { it.id == selectedGarmentId } ?: filteredGarments.firstOrNull()
    }

    val activeGroupGarments = remember(selectedGroupGarmentIds, filteredGarments) {
        filteredGarments.filter { it.id in selectedGroupGarmentIds }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Garment & Measurement",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
        HorizontalDivider(color = sectionBorder)

        Text(
            text = "Apply Template To",
            fontSize = tokens.bodySmall,
            color = TextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onApplyToGroupChange(false) },
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!applyToGroup) primary_light else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (!applyToGroup) Primary else sectionBorder)
            ) {
                Text(
                    text = "Single Garment",
                    color = if (!applyToGroup) Primary else headerGrey,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { onApplyToGroupChange(true) },
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (applyToGroup) primary_light else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (applyToGroup) Primary else sectionBorder)
            ) {
                Text(
                    text = "Garment Group",
                    color = if (applyToGroup) Primary else headerGrey,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (tabNames.isNotEmpty()) {
            AppUnderlineTabRow(
                tabs = tabNames,
                selectedIndex = selectedCategoryTabIndex.coerceIn(0, tabNames.lastIndex),
                onTabSelected = onCategoryTabSelect
            )
        }

        HorizontalDivider(color = grey_border)

        if (!applyToGroup) {
            FormLabel("Garment", isRequired = true)

            val currentGarmentName = activeSingleGarment?.displayName ?: activeSingleGarment?.name ?: "Select Garment"
            val garmentNames = filteredGarments.map { it.displayName ?: it.name }

            FormDropdown(
                value = if (filteredGarments.isEmpty()) "No garments available" else currentGarmentName,
                expanded = garmentDropdownExpanded,
                onExpandChange = { if (filteredGarments.isNotEmpty()) garmentDropdownExpanded = it },
                options = garmentNames,
                onOptionSelected = { chosenName ->
                    val chosen = filteredGarments.find { (it.displayName ?: it.name) == chosenName }
                    if (chosen != null) {
                        onGarmentSelect(chosen)
                    }
                }
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Selected Garment Config",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Garment:", fontSize = tokens.bodySmall, color = headerGrey)
                        Text(
                            text = activeSingleGarment?.displayName ?: activeSingleGarment?.name ?: "None",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Code:", fontSize = tokens.bodySmall, color = headerGrey)
                        Text(
                            text = activeSingleGarment?.code ?: "—",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Fields:", fontSize = tokens.bodySmall, color = headerGrey)
                        Text(
                            text = "${activeSingleGarment?.measurementFields?.size ?: 0} Measurement Fields",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Available Garments (${filteredGarments.size})",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (filteredGarments.isEmpty()) {
                        Text(
                            text = "No garments found under this segment.",
                            fontSize = tokens.bodySmall,
                            color = headerGrey,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        val half = (filteredGarments.size + 1) / 2
                        val col1 = filteredGarments.take(half)
                        val col2 = filteredGarments.drop(half)

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                col1.forEach { item ->
                                    val isChecked = item.id in selectedGroupGarmentIds
                                    CheckboxOptionRow(
                                        title = item.displayName ?: item.name,
                                        checked = isChecked,
                                        onCheckedChange = { onToggleGroupGarment(item.id) }
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                col2.forEach { item ->
                                    val isChecked = item.id in selectedGroupGarmentIds
                                    CheckboxOptionRow(
                                        title = item.displayName ?: item.name,
                                        checked = isChecked,
                                        onCheckedChange = { onToggleGroupGarment(item.id) }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = grey_border, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .clickable { onAddGarmentClick() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Text(
                            text = "Add Garment",
                            color = Primary,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (activeGroupGarments.isNotEmpty()) {
                Text(
                    text = "Selected Garments (${activeGroupGarments.size})",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                activeGroupGarments.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, sectionBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = item.displayName ?: item.name,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Code: ${item.code}",
                                    fontSize = tokens.caption,
                                    color = headerGrey
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(greenBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Configured",
                                        fontSize = tokens.label,
                                        color = darkGreenBg,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "View",
                                    fontSize = tokens.caption,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        val dynamicMeasurementFields = remember(activeSingleGarment) {
            activeSingleGarment?.measurementFields?.mapNotNull { it.field?.displayName ?: it.field?.name } ?: emptyList()
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Measurement Fields",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (dynamicMeasurementFields.isNotEmpty()) "${dynamicMeasurementFields.size} Fields" else "Standard Pattern",
                        fontSize = tokens.caption,
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = grey_border)

                if (dynamicMeasurementFields.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dynamicMeasurementFields.forEach { fieldName ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Primary_background)
                                    .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = fieldName,
                                    fontSize = tokens.bodySmall,
                                    color = textSubdued
                                )
                            }
                        }
                    }
                } else {
                    MeasurementGroup("BODY", listOf("Chest Round", "Waist Round", "Seat / Hip Round"))
                    MeasurementGroup("SHOULDER & BACK", listOf("Shoulder Width", "Back Width"))
                    MeasurementGroup("SLEEVE", listOf("Sleeve Length", "Bicep Round", "Armhole Round"))
                    MeasurementGroup("LENGTH", listOf("Garment Length"))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step 3: Workflow Steps Content (Integrated with API Stages)
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepThreeWorkflow(
    workflowSteps: List<WorkflowStepItem>,
    isLoadingStages: Boolean,
    onStepsChange: (List<WorkflowStepItem>) -> Unit,
    onAddStepClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Service Workflow",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "Define the sequence of work for this template.",
                    fontSize = tokens.caption,
                    color = headerGrey
                )
            }
            Button(
                onClick = onAddStepClick,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Add Stage",
                    color = whiteBg,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (isLoadingStages && workflowSteps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
            }
        } else if (workflowSteps.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No production stages added. Click '+ Add Stage' to create or add steps.",
                        fontSize = tokens.bodySmall,
                        color = headerGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column {
                    workflowSteps.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragIndicator,
                                contentDescription = null,
                                tint = iconMuted,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "%02d".format(index + 1),
                                color = headerGrey,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.stage,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                if (item.workType.isNotBlank()) {
                                    Text(
                                        text = "Type: ${item.workType}",
                                        fontSize = tokens.caption,
                                        color = headerGrey
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (item.isRequired) greenBg else quickaccessBg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .clickable {
                                        val updated = workflowSteps.toMutableList()
                                        updated[index] = item.copy(isRequired = !item.isRequired)
                                        onStepsChange(updated)
                                    }
                            ) {
                                Text(
                                    text = if (item.isRequired) "Required" else "Optional",
                                    fontSize = tokens.label,
                                    color = if (item.isRequired) darkGreenBg else Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val updated = workflowSteps.toMutableList()
                                    updated.removeAt(index)
                                    onStepsChange(updated)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Step",
                                    tint = redText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (index < workflowSteps.lastIndex) {
                            HorizontalDivider(color = grey_border)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step 4: Review & Activate Content
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepFiveReviewAndActivate(
    workflowSteps: List<WorkflowStepItem>,
    onEditWorkflow: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = "Review & Activate",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "Verify the service template configuration details before activating.",
                fontSize = tokens.caption,
                color = headerGrey
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workflow Configuration (${workflowSteps.size} Stages)",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Row(
                modifier = Modifier.clickable { onEditWorkflow() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Edit Workflow",
                    fontSize = tokens.caption,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                workflowSteps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(quickaccessBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "%02d".format(index + 1),
                                fontSize = tokens.label,
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = step.stage,
                            fontSize = tokens.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(greenBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (step.isRequired) "Required" else "Optional",
                                fontSize = tokens.label,
                                color = darkGreenBg,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (index < workflowSteps.lastIndex) {
                        HorizontalDivider(color = grey_border)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Screen: Add Workflow Step / Create Stage Page
// ─────────────────────────────────────────────────────────────

@Composable
fun AddWorkflowStepPage(
    sequenceNumber: Int,
    onBack: () -> Unit,
    onCreateNewStage: (CreateStageRequest, () -> Unit) -> Unit
) {
    val tokens = LocalAppTokens.current
    var stageName by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("Cutting") }
    var workTypeExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var isAllocationRequired by remember { mutableStateOf(true) }
    var allowRework by remember { mutableStateOf(true) }
    var stageError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val workTypes = listOf("Cutting", "Stitching", "QC", "Trial", "Finishing", "Embroidery")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Production Stage (Step #$sequenceNumber)",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = close_color,
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .clickable { onBack() }
                    )
                }
                HorizontalDivider(color = title_border)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 20.dp)
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    FormLabel("Stage Name", isRequired = true)
                    FormTextField(
                        value = stageName,
                        onValueChange = {
                            stageName = it
                            stageError = false
                        },
                        placeholder = "e.g. Fabric Cutting",
                        isError = stageError,
                        errorMessage = if (stageError) "Stage name is required" else null
                    )
                }

                Column {
                    FormLabel("Display Name (Optional)")
                    FormTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        placeholder = "e.g. Cutting Master Work"
                    )
                }

                Column {
                    FormDropdown(
                        label = "Work Type",
                        value = workType,
                        expanded = workTypeExpanded,
                        onExpandChange = { workTypeExpanded = it },
                        options = workTypes,
                        onOptionSelected = { workType = it }
                    )
                }

                Column {
                    FormLabel("Description (Optional)")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Explain what tasks are performed in this stage...",
                        minLines = 3,
                        maxLines = 5
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Allocation Required",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = textSubdued
                        )
                        Text(
                            text = "Assign worker/tailor to this stage",
                            fontSize = tokens.caption,
                            color = iconMuted
                        )
                    }
                    MiniSwitch(
                        checked = isAllocationRequired,
                        onCheckedChange = { isAllocationRequired = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Allow Rework",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = textSubdued
                        )
                        Text(
                            text = "Can be sent back to this stage during QC",
                            fontSize = tokens.caption,
                            color = iconMuted
                        )
                    }
                    MiniSwitch(
                        checked = allowRework,
                        onCheckedChange = { allowRework = it }
                    )
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = onBack,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = if (isSubmitting) "Saving..." else "Save Stage",
                onClick = {
                    if (stageName.isBlank()) {
                        stageError = true
                        return@Next
                    }
                    isSubmitting = true
                    val code = stageName.trim().uppercase().replace(Regex("[^A-Z0-9]+"), "_")
                    val request = CreateStageRequest(
                        name = stageName.trim(),
                        displayName = displayName.ifBlank { stageName.trim() },
                        code = code,
                        description = description,
                        workType = workType,
                        isAllocationRequired = isAllocationRequired,
                        allowRework = allowRework,
                        status = "Active"
                    )
                    onCreateNewStage(request) {
                        isSubmitting = false
                    }
                }
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Modals & Bottom Sheets
// ─────────────────────────────────────────────────────────────

@Composable
fun AddGarmentSheetContent(
    category: String = "Men",
    garmentGroup: String = "Shirts",
    onDismiss: () -> Unit,
    onCreate: (name: String, code: String, desc: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ADD GARMENT",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Create a new garment under the selected category and garment group.",
                fontSize = tokens.caption,
                color = headerGrey
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FormLabel("Category")
                FormTextField(
                    value = category,
                    onValueChange = {},
                    enabled = false
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                FormLabel("Garment Group")
                FormTextField(
                    value = garmentGroup,
                    onValueChange = {},
                    enabled = false
                )
            }
        }

        Text(
            text = "This garment will be added to the $garmentGroup group under $category.",
            fontSize = tokens.caption,
            color = iconMuted
        )

        Text(
            text = "Garment Information",
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Column {
            FormLabel("Garment Name", isRequired = true)
            FormTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Enter garment name"
            )
        }

        Column {
            FormLabel("Garment Code", isRequired = true)
            FormTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = "Enter unique garment code"
            )
        }

        Column {
            FormLabel("Description (Optional)")
            FormTextArea(
                value = desc,
                onValueChange = { desc = it },
                placeholder = "Describe the garment or its usage.",
                minLines = 3,
                maxLines = 5
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Status",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = textSubdued
                )
                Text(
                    text = "Active garments can be selected in Service Templates and Orders.",
                    fontSize = tokens.caption,
                    color = iconMuted
                )
            }
            MiniSwitch(
                checked = active,
                onCheckedChange = { active = it }
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Text(
                    text = "Cancel",
                    color = textSubdued,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = { onCreate(name, code, desc) },
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "Create Garment",
                    color = whiteBg,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ActivateTemplateConfirmDialog(
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = whiteBg)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Text(
                    text = if (isEdit) "Update Service Template?" else "Activate Service Template?",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                Text(
                    text = if (isEdit) "The modified workflow changes will be applied to this template." else "Once activated, this template will be available for service order creation.",
                    fontSize = tokens.bodySmall,
                    color = headerGrey,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Text(
                            text = "Cancel",
                            color = textSubdued,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = if (isEdit) "Update   " else "Activate",
                            color = whiteBg,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Screen 3: Template Details / Deactivate View Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun ServiceTemplateDetailViewScreen(
    templateId: String,
    onClose: () -> Unit = {},
    onDeactivate: () -> Unit = onClose,
    templateViewModel: ServicesViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val selectedDetail by templateViewModel.selectedTemplate.collectAsState()
    val isLoadingDetail by templateViewModel.isLoadingDetail.collectAsState()

    LaunchedEffect(templateId) {
        if (templateId.isNotBlank()) {
            templateViewModel.fetchTemplateDetail(templateId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (isLoadingDetail && selectedDetail == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            val template = selectedDetail
            val stages = template?.stages.orEmpty()
            val segmentName = template?.segment?.displayName
                ?: template?.segment?.name
                ?: "General"
            val garmentName = template?.garment?.displayName
                ?: template?.garment?.name
                ?: "Standard Tailoring"

            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(
                    title = template?.name ?: "Template Details",
                    onClose = onClose
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(badgeGrey)
                        .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Segment: $segmentName",
                            fontSize = tokens.caption,
                            color = close_color
                        )
                        Text(
                            text = "Garment: $garmentName",
                            fontSize = tokens.caption,
                            color = close_color
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Code: ${template?.code ?: "—"}",
                            fontSize = tokens.caption,
                            color = close_color
                        )
                        Text(
                            text = "Total Steps: ${stages.size} Steps",
                            fontSize = tokens.caption,
                            color = close_color
                        )
                    }
                }

                HorizontalDivider(color = grey_border)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
                        .padding(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(activity_purple_bg)
                            .border(1.dp, light_blue_border, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "This template is currently used by existing orders. Changes will apply only to future orders.",
                            color = darkPurple,
                            fontSize = tokens.caption
                        )
                    }

                    Text(
                        text = "Workflow Configuration",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, sectionBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column {
                            if (stages.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No workflow stages configured.",
                                        fontSize = tokens.bodySmall,
                                        color = headerGrey
                                    )
                                }
                            } else {
                                stages.forEachIndexed { index, stepItem ->
                                    val stageName = stepItem.stageDetail?.displayName
                                        ?: stepItem.stageDetail?.name
                                        ?: "Stage ${stepItem.displayOrder}"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(greenBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "%02d".format(stepItem.displayOrder),
                                                fontSize = tokens.label,
                                                color = darkGreenBg,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = stageName,
                                            fontSize = tokens.bodySmall,
                                            color = TextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(greenBg)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (stepItem.isMandatory) "Mandatory" else "Optional",
                                                fontSize = tokens.label,
                                                color = darkGreenBg,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = null,
                                            tint = iconMuted,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                    }
                                    if (index < stages.lastIndex) {
                                        HorizontalDivider(color = grey_border)
                                    }
                                }
                            }
                        }
                    }

                    if (!template?.description.isNullOrBlank()) {
                        Text(
                            text = "Description",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            border = BorderStroke(1.dp, sectionBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = template.description,
                                fontSize = tokens.bodySmall,
                                color = textSubdued,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .background(whiteBg)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Text(
                            text = "Close",
                            color = textSubdued,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Button(
                        onClick = {
//                            template?.id?.let { id ->
//                                templateViewModel.deleteTemplate(id) {
//                                    onDeactivate()
//                                }
//                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = redBg,
                            contentColor = redText
                        )
                    ) {
                        Text(
                            text = "Deactivate Template",
                            color = redText,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Shared Sub-composables
// ─────────────────────────────────────────────────────────────

@Composable
private fun CheckboxOptionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tokens = LocalAppTokens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = title,
            fontSize = tokens.bodySmall,
            color = title_color
        )
    }
}

@Composable
private fun MeasurementGroup(
    title: String,
    fields: List<String>
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = headerGrey
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fields.forEach { field ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary_background)
                        .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = field,
                        fontSize = tokens.bodySmall,
                        color = textSubdued
                    )
                }
            }
        }
    }
}