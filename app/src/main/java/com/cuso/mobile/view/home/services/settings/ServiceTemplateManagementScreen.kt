@file:Suppress("UNUSED_PARAMETER", "unused", "UNUSED_VALUE", "ASSIGNED_VALUE_IS_NEVER_READ")

package com.cuso.mobile.view.home.services.settings

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterList
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.settings.SegmentItem
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.AppCheckbox
import com.cuso.mobile.view.composable.AppUnderlineTabRow
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.view.home.sales.customer.OrderStatusStepper
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.SettingsViewModel

// ─────────────────────────────────────────────────────────────
// Models
// ─────────────────────────────────────────────────────────────

data class WorkflowStepItem(
    val sequence: Int,
    val stage: String,
    val workType: String = stage,
    val isRequired: Boolean = true,
    val allowRework: Boolean = true,
    val instructions: String = ""
)

data class OptionalWorkItem(
    val name: String,
    val workType: String = name,
    val isRequired: Boolean = false,
    val notes: String = "",
    val status: String = "Active"
)

data class ServiceTemplateItem(
    val id: String,
    val name: String,
    val serviceType: String,
    val description: String = "",
    val garmentCount: Int = 1,
    val stepCount: Int = 7,
    val timeAgo: String = "2 hours ago",
    val status: String = "ACTIVE"
)

// ─────────────────────────────────────────────────────────────
// Screen 1: Service Templates List Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun ServiceTemplateListScreen(
    onClose: () -> Unit = {},
    onAddNewTemplate: () -> Unit = {},
    onViewTemplate: (ServiceTemplateItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val sampleTemplates = remember {
        listOf(
            ServiceTemplateItem(
                id = "1",
                name = "Men's Shirt – Standard",
                serviceType = "Custom Tailoring",
                garmentCount = 1,
                stepCount = 7,
                timeAgo = "2 hours ago",
                status = "ACTIVE"
            ),
            ServiceTemplateItem(
                id = "2",
                name = "Men's Shirt – Standard",
                serviceType = "Custom Tailoring",
                garmentCount = 1,
                stepCount = 6,
                timeAgo = "2 hours ago",
                status = "ACTIVE"
            ),
            ServiceTemplateItem(
                id = "3",
                name = "Men's Shirt – Standard",
                serviceType = "Custom Tailoring",
                garmentCount = 1,
                stepCount = 8,
                timeAgo = "2 hours ago",
                status = "ACTIVE"
            ),
            ServiceTemplateItem(
                id = "4",
                name = "Men's Shirt – Standard",
                serviceType = "Custom Tailoring",
                garmentCount = 3,
                stepCount = 9,
                timeAgo = "Just now",
                status = "DRAFT"
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Service Templates",
                onClose = onClose
            )

            HorizontalDivider(color = title_border)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Customers...",
                        accentColor = BluePrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(whiteBg)
                        .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filter",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Service Templates",
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Create and manage reusable service workflows for garments.",
                        fontSize = tokens.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }

                Button(
                    onClick = onAddNewTemplate,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = whiteBg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add New", color = whiteBg, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(sampleTemplates) { item ->
                    val isDraft = item.status.equals("DRAFT", ignoreCase = true)
                    val badgeBg = if (isDraft) Color(0xFFF3E8FF) else Color(0xFFDCFCE7)
                    val badgeTextColor = if (isDraft) Color(0xFF9333EA) else Color(0xFF16A34A)

                    DataCard<ServiceTemplateItem>(
                        item = item,
                        title = item.name,
                        titleColor = Color(0xFF0F172A),
                        topBadgeText = item.status,
                        topBadgeTextColor = badgeTextColor,
                        topBadgeBgColor = badgeBg,
                        topBadgeShowDot = false,
                        topBadgeInline = true,
                        showHeaderDivider = false,
                        actions = listOf(
                            MenuAction("View", Icons.Default.Visibility) { onViewTemplate(item) },
                            MenuAction("Edit", Icons.Default.Edit) { }
                        ),
                        content = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFEEF2FF))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = item.serviceType,
                                        fontSize = 11.sp,
                                        color = Color(0xFF4F46E5),
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
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text("Garment: ", fontSize = tokens.bodySmall, color = Color(0xFF64748B))
                                        Text("${item.garmentCount}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text("${item.stepCount} Steps", fontSize = tokens.bodySmall, color = Color(0xFF64748B))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccessTime,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(item.timeAgo, fontSize = tokens.caption, color = Color(0xFF94A3B8))
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

// ─────────────────────────────────────────────────────────────
// Screen 2: Create Service Template Wizard Screen (5 Steps)
// ─────────────────────────────────────────────────────────────

@Composable
fun CreateServiceTemplateWizardScreen(
    onClose: () -> Unit = {},
    onTemplateCreated: () -> Unit = onClose,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var currentStep by remember { mutableIntStateOf(0) }
    var isAddingWorkflowStepPage by remember { mutableStateOf(false) }
    var isAddingOptionalWorkPage by remember { mutableStateOf(false) }

    var addGarmentSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var backgroundBlur by remember { mutableStateOf(0.dp) }

    val isSheetOpen = addGarmentSheetState != SheetValue.Hidden

    val wizardStepLabels = listOf(
        "Basic Info",
        "Garment",
        "Workflow",
        "Optional Work",
        "Review"
    )

    val segments by settingsViewModel.segments.collectAsState()
    val garments by settingsViewModel.garments.collectAsState()

    LaunchedEffect(Unit) {
        if (segments.isEmpty()) {
            settingsViewModel.fetchSegments()
        }
        if (garments.isEmpty()) {
            settingsViewModel.fetchGarments()
        }
    }

    // Step 1: Basic Info
    var templateName by remember { mutableStateOf("Men's Shirt - Standard Tailoring") }
    var serviceType by remember { mutableStateOf("Custom Tailoring") }
    var serviceTypeExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var statusActive by remember { mutableStateOf(true) }

    // Step 2: Garment & Measurement
    var applyToGroup by remember { mutableStateOf(false) }
    var selectedCategoryTabIndex by remember { mutableIntStateOf(0) }
    var selectedGarment by remember { mutableStateOf("") }
    var garmentDropdownExpanded by remember { mutableStateOf(false) }
    var garmentGroup by remember { mutableStateOf("Shirts") }
    var garmentGroupExpanded by remember { mutableStateOf(false) }

    var groupFormalShirt by remember { mutableStateOf(true) }
    var groupCasualShirt by remember { mutableStateOf(true) }
    var groupDesignerShirt by remember { mutableStateOf(true) }
    var groupHalfSleeve by remember { mutableStateOf(false) }
    var groupFullSleeve by remember { mutableStateOf(false) }
    var groupLinenShirt by remember { mutableStateOf(false) }

    val currentSegment = segments.getOrNull(selectedCategoryTabIndex)
    val filteredGarments = remember(currentSegment, garments) {
        if (currentSegment != null) {
            garments.filter { garment ->
                garment.applicableSegments.any { it.id == currentSegment.id }
            }
        } else {
            garments
        }
    }

    val garmentOptions = remember(filteredGarments) {
        filteredGarments.map { it.name }.ifEmpty { listOf("Formal Shirt", "Casual Shirt", "Kurta", "Blazer", "Trousers") }
    }

    LaunchedEffect(garmentOptions) {
        if (selectedGarment.isBlank() || selectedGarment !in garmentOptions) {
            selectedGarment = garmentOptions.firstOrNull() ?: "Formal Shirt"
        }
    }

    // Step 3: Workflow
    var workflowSteps by remember {
        mutableStateOf(
            listOf(
                WorkflowStepItem(1, "Measurement"),
                WorkflowStepItem(2, "Cutting"),
                WorkflowStepItem(3, "Stitching"),
                WorkflowStepItem(4, "Trial"),
                WorkflowStepItem(5, "Finishing"),
                WorkflowStepItem(6, "Quality Check"),
                WorkflowStepItem(7, "Ready for Delivery")
            )
        )
    }

    // Step 4: Optional Work
    var optionalWorks by remember {
        mutableStateOf(
            listOf(
                OptionalWorkItem("Aari Work", notes = "Additional decorative work if requested by customer."),
                OptionalWorkItem("Embroidery", notes = "Custom embroidery detailing."),
                OptionalWorkItem("Stone Work", notes = "Stone/bead embellishment work.")
            )
        )
    }

    // Step 5: Activate Dialog
    var showActivateConfirmDialog by remember { mutableStateOf(false) }

    if (isAddingWorkflowStepPage) {
        AddWorkflowStepPage(
            sequenceNumber = workflowSteps.size + 1,
            onBack = { isAddingWorkflowStepPage = false },
            onAddStep = { newStep ->
                workflowSteps = workflowSteps + newStep
                isAddingWorkflowStepPage = false
            }
        )
        return
    }

    if (isAddingOptionalWorkPage) {
        AddOptionalWorkPage(
            onBack = { isAddingOptionalWorkPage = false },
            onAddWork = { newWork ->
                optionalWorks = optionalWorks + newWork
                isAddingOptionalWorkPage = false
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
                    title = "Create Service Template",
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

                if (currentStep > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Template: $templateName",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Garment: ${selectedGarment.ifBlank { "Men's Shirt" }}",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
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
                            selectedCategoryTabIndex = selectedCategoryTabIndex,
                            onCategoryTabSelect = { selectedCategoryTabIndex = it },
                            selectedGarment = selectedGarment,
                            garmentOptions = garmentOptions,
                            garmentDropdownExpanded = garmentDropdownExpanded,
                            onGarmentDropdownExpandedChange = { garmentDropdownExpanded = it },
                            onGarmentSelect = { selectedGarment = it },
                            garmentGroup = garmentGroup,
                            garmentGroupExpanded = garmentGroupExpanded,
                            onGarmentGroupExpandedChange = { garmentGroupExpanded = it },
                            onGarmentGroupSelect = { garmentGroup = it },
                            groupFormalShirt = groupFormalShirt,
                            onGroupFormalShirtChange = { groupFormalShirt = it },
                            groupCasualShirt = groupCasualShirt,
                            onGroupCasualShirtChange = { groupCasualShirt = it },
                            groupDesignerShirt = groupDesignerShirt,
                            onGroupDesignerShirtChange = { groupDesignerShirt = it },
                            groupHalfSleeve = groupHalfSleeve,
                            onGroupHalfSleeveChange = { groupHalfSleeve = it },
                            groupFullSleeve = groupFullSleeve,
                            onGroupFullSleeveChange = { groupFullSleeve = it },
                            groupLinenShirt = groupLinenShirt,
                            onGroupLinenShirtChange = { groupLinenShirt = it },
                            onAddGarmentClick = { addGarmentSheetState = SheetValue.Expanded }
                        )

                        2 -> StepThreeWorkflow(
                            workflowSteps = workflowSteps,
                            onAddStepClick = { isAddingWorkflowStepPage = true }
                        )

                        3 -> StepFourOptionalWork(
                            optionalWorks = optionalWorks,
                            onAddOptionalWorkClick = { isAddingOptionalWorkPage = true }
                        )

                        4 -> StepFiveReviewAndActivate(
                            workflowSteps = workflowSteps,
                            optionalWorks = optionalWorks,
                            onEditWorkflow = { currentStep = 2 },
                            onEditOptionalWork = { currentStep = 3 }
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
                    label = if (currentStep == 4) "Activate Template" else "Next",
                    onClick = {
                        if (currentStep < 4) {
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
            val currentSegmentName = segments.getOrNull(selectedCategoryTabIndex)?.name ?: "Men"
            AddGarmentSheetContent(
                category = currentSegmentName,
                garmentGroup = garmentGroup,
                onDismiss = {
                    backgroundBlur = 0.dp
                    addGarmentSheetState = SheetValue.Hidden
                },
                onCreate = { name, code, desc ->
                    backgroundBlur = 0.dp
                    addGarmentSheetState = SheetValue.Hidden
                }
            )
        }
    }

    if (showActivateConfirmDialog) {
        ActivateTemplateConfirmDialog(
            onDismiss = { showActivateConfirmDialog = false },
            onConfirm = {
                showActivateConfirmDialog = false
                onTemplateCreated()
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Basic Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        HorizontalDivider(color = Color(0xFFE2E8F0))

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
                options = listOf("Custom Tailoring", "Alteration", "Dry Cleaning", "Embroidery Only"),
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
            Text("Status", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
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

@Composable
private fun StepTwoGarmentAndMeasurement(
    applyToGroup: Boolean,
    onApplyToGroupChange: (Boolean) -> Unit,
    segments: List<SegmentItem>,
    selectedCategoryTabIndex: Int,
    onCategoryTabSelect: (Int) -> Unit,
    selectedGarment: String,
    garmentOptions: List<String>,
    garmentDropdownExpanded: Boolean,
    onGarmentDropdownExpandedChange: (Boolean) -> Unit,
    onGarmentSelect: (String) -> Unit,
    garmentGroup: String,
    garmentGroupExpanded: Boolean,
    onGarmentGroupExpandedChange: (Boolean) -> Unit,
    onGarmentGroupSelect: (String) -> Unit,
    groupFormalShirt: Boolean,
    onGroupFormalShirtChange: (Boolean) -> Unit,
    groupCasualShirt: Boolean,
    onGroupCasualShirtChange: (Boolean) -> Unit,
    groupDesignerShirt: Boolean,
    onGroupDesignerShirtChange: (Boolean) -> Unit,
    groupHalfSleeve: Boolean,
    onGroupHalfSleeveChange: (Boolean) -> Unit,
    groupFullSleeve: Boolean,
    onGroupFullSleeveChange: (Boolean) -> Unit,
    groupLinenShirt: Boolean,
    onGroupLinenShirtChange: (Boolean) -> Unit,
    onAddGarmentClick: () -> Unit
) {
    val tabNames = remember(segments) {
        if (segments.isNotEmpty()) segments.map { it.name } else listOf("Men", "Women", "Kids", "Uniform")
    }

    val selectedGarmentsList = remember(
        groupFormalShirt,
        groupCasualShirt,
        groupDesignerShirt,
        groupHalfSleeve,
        groupFullSleeve,
        groupLinenShirt
    ) {
        buildList {
            if (groupFormalShirt) add("Formal Shirt")
            if (groupCasualShirt) add("Casual Shirt")
            if (groupDesignerShirt) add("Designer Shirt")
            if (groupHalfSleeve) add("Half Sleeve Shirt")
            if (groupFullSleeve) add("Full Sleeve Shirt")
            if (groupLinenShirt) add("Linen Shirt")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Garment & Measurement", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = title_color)
        HorizontalDivider(color = Color(0xFFE2E8F0))

        Text("Apply Template To", fontSize = 13.sp, color = Color(0xFF475569))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onApplyToGroupChange(false) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!applyToGroup) Color(0xFFE0E7FF) else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (!applyToGroup) Color(0xFF818CF8) else Color(0xFFE2E8F0))
            ) {
                Text(
                    text = "Single Garment",
                    color = if (!applyToGroup) Color(0xFF3730A3) else Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { onApplyToGroupChange(true) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (applyToGroup) Color(0xFFE0E7FF) else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (applyToGroup) Color(0xFF818CF8) else Color(0xFFE2E8F0))
            ) {
                Text(
                    text = "Garment Group",
                    color = if (applyToGroup) Color(0xFF3730A3) else Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        AppUnderlineTabRow(
            tabs = tabNames,
            selectedIndex = selectedCategoryTabIndex.coerceIn(0, (tabNames.size - 1).coerceAtLeast(0)),
            onTabSelected = onCategoryTabSelect
        )

        HorizontalDivider(color = Color(0xFFF1F5F9))

        if (!applyToGroup) {
            FormLabel("Garment")
            FormDropdown(
                value = selectedGarment,
                expanded = garmentDropdownExpanded,
                onExpandChange = onGarmentDropdownExpandedChange,
                options = garmentOptions,
                onOptionSelected = onGarmentSelect
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Selected Garment Config", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Garment:", fontSize = 13.sp, color = Color(0xFF64748B))
                        Text(selectedGarment.ifBlank { "Formal Shirt" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Configuration:", fontSize = 13.sp, color = Color(0xFF64748B))
                        Text("${selectedGarment.ifBlank { "Formal Shirt" }} Standard", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Fields:", fontSize = 13.sp, color = Color(0xFF64748B))
                        Text("12 Measurement Fields", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                }
            }
        } else {
            FormLabel("Garment Group")
            FormDropdown(
                value = garmentGroup,
                expanded = garmentGroupExpanded,
                onExpandChange = onGarmentGroupExpandedChange,
                options = listOf("Shirts", "Trousers", "Suits", "Ethnic"),
                onOptionSelected = onGarmentGroupSelect
            )
            Text("Available Garments", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))

            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CheckboxOptionRow("Formal Shirt", groupFormalShirt, onGroupFormalShirtChange)
                            CheckboxOptionRow("Casual Shirt", groupCasualShirt, onGroupCasualShirtChange)
                            CheckboxOptionRow("Designer Shirt", groupDesignerShirt, onGroupDesignerShirtChange)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CheckboxOptionRow("Half Sleeve Shirt", groupHalfSleeve, onGroupHalfSleeveChange)
                            CheckboxOptionRow("Full Sleeve Shirt", groupFullSleeve, onGroupFullSleeveChange)
                            CheckboxOptionRow("Linen Shirt", groupLinenShirt, onGroupLinenShirtChange)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .clickable { onAddGarmentClick() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color(0xFF4338CA), modifier = Modifier.size(16.dp))
                        Text("Add Garment", color = Color(0xFF4338CA), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (selectedGarmentsList.isNotEmpty()) {
                Text(
                    text = "Selected Garments (${selectedGarmentsList.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )

                selectedGarmentsList.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                Text(item, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("$item Standard", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Configured", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                                }
                                Text("View", fontSize = 12.sp, color = Color(0xFF4338CA), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                    Text("Measurement Fields", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("Standard Pattern", fontSize = 12.sp, color = Color(0xFF4338CA), fontWeight = FontWeight.Medium)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                MeasurementGroup("BODY", listOf("Chest Round", "Waist Round", "Seat / Hip Round"))
                MeasurementGroup("SHOULDER & BACK", listOf("Shoulder Width", "Back Width"))
                MeasurementGroup("SLEEVE", listOf("Sleeve Length", "Bicep Round", "Armhole Round"))
                MeasurementGroup("NECK & COLLAR", listOf("Neck Round"))
                MeasurementGroup("LENGTH", listOf("Shirt Length"))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step 3: Workflow Steps Content
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepThreeWorkflow(
    workflowSteps: List<WorkflowStepItem>,
    onAddStepClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Service Workflow", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("Define the sequence of work.", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Button(
                onClick = onAddStepClick,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = whiteBg, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Workflow Step", color = whiteBg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                        Icon(Icons.Default.DragIndicator, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "%02d".format(item.sequence),
                            color = Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = item.stage,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(if (item.isRequired) "Required" else "Optional", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.MoreVert, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                    if (index < workflowSteps.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step 4: Optional Work Content
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepFourOptionalWork(
    optionalWorks: List<OptionalWorkItem>,
    onAddOptionalWorkClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Optional Work", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("Define the sequence of work.", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Button(
                onClick = onAddOptionalWorkClick,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = whiteBg, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Optional Work", color = whiteBg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                optionalWorks.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            Text("Optional", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(item.status, fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.MoreVert, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                    if (index < optionalWorks.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Screen: Add Optional Work Page (Full Page)
// ─────────────────────────────────────────────────────────────

@Composable
fun AddOptionalWorkPage(
    onBack: () -> Unit,
    onAddWork: (OptionalWorkItem) -> Unit
) {
    var workType by remember { mutableStateOf("Finishing") }
    var isRequired by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("Additional decorative work if requested by customer.") }

    var workTypeError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBFBFB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Optional Work",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = close_color,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onBack() }
                    )
                }
                HorizontalDivider(color = title_border)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    FormLabel("Work Type", isRequired = true)
                    FormTextField(
                        value = workType,
                        onValueChange = {
                            workType = it
                            workTypeError = false
                        },
                        placeholder = "e.g. Finishing",
                        isError = workTypeError,
                        errorMessage = if (workTypeError) "Work Type is required" else null
                    )
                }

                Column {
                    Text("Requirement", fontSize = 13.sp, color = Color(0xFF475569))
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isRequired = true }
                        ) {
                            RadioButton(
                                selected = isRequired,
                                onClick = { isRequired = true }
                            )
                            Text("Required", fontSize = 13.sp, color = Color(0xFF0F172A))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isRequired = false }
                        ) {
                            RadioButton(
                                selected = !isRequired,
                                onClick = { isRequired = false }
                            )
                            Text("Optional", fontSize = 13.sp, color = Color(0xFF0F172A))
                        }
                    }
                }

                Column {
                    FormLabel("Notes")
                    FormTextArea(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "Additional decorative work if requested by customer.",
                        minLines = 4,
                        maxLines = 6
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
                label = "Add Work",
                onClick = {
                    if (workType.isBlank()) {
                        workTypeError = true
                        return@Next
                    }
                    onAddWork(
                        OptionalWorkItem(
                            name = workType,
                            workType = workType,
                            isRequired = isRequired,
                            notes = notes,
                            status = "Active"
                        )
                    )
                }
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Step 5: Review & Activate Content
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepFiveReviewAndActivate(
    workflowSteps: List<WorkflowStepItem>,
    optionalWorks: List<OptionalWorkItem>,
    onEditWorkflow: () -> Unit,
    onEditOptionalWork: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text("Review & Activate", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text("Verify the service template configuration details before activating.", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Workflow Configuration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Row(
                modifier = Modifier.clickable { onEditWorkflow() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Edit, null, tint = Color(0xFF4338CA), modifier = Modifier.size(14.dp))
                Text("Edit Workflow", fontSize = 12.sp, color = Color(0xFF4338CA), fontWeight = FontWeight.Medium)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("%02d".format(step.sequence), fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(step.stage, fontSize = 13.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(if (step.isRequired) "Required" else "Optional", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.MoreVert, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                    if (index < workflowSteps.lastIndex) HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Optional Add-on Work", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Row(
                modifier = Modifier.clickable { onEditOptionalWork() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Edit, null, tint = Color(0xFF4338CA), modifier = Modifier.size(14.dp))
                Text("Edit Optional Work", fontSize = 12.sp, color = Color(0xFF4338CA), fontWeight = FontWeight.Medium)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                optionalWorks.forEachIndexed { index, work ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(work.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            Text("Optional", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(work.status, fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.MoreVert, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                    if (index < optionalWorks.lastIndex) HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Screen: Add Workflow Step Page (Full Page)
// ─────────────────────────────────────────────────────────────

@Composable
fun AddWorkflowStepPage(
    sequenceNumber: Int,
    onBack: () -> Unit,
    onAddStep: (WorkflowStepItem) -> Unit
) {
    var stage by remember { mutableStateOf("Cutting") }
    var workType by remember { mutableStateOf("Cutting") }
    var sequence by remember { mutableStateOf("$sequenceNumber") }
    var isRequired by remember { mutableStateOf(true) }
    var allowRework by remember { mutableStateOf(true) }
    var instructions by remember { mutableStateOf("Complete the stage according to the approved service requirements.") }

    var stageError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBFBFB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Workflow Step",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = close_color,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onBack() }
                    )
                }
                HorizontalDivider(color = title_border)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    FormLabel("Stage", isRequired = true)
                    FormTextField(
                        value = stage,
                        onValueChange = {
                            stage = it
                            stageError = false
                        },
                        placeholder = "e.g. Cutting",
                        isError = stageError,
                        errorMessage = if (stageError) "Stage is required" else null
                    )
                }

                Column {
                    FormLabel("Work Type")
                    FormTextField(
                        value = workType,
                        onValueChange = { workType = it },
                        placeholder = "e.g. Cutting"
                    )
                }

                Column {
                    FormLabel("Sequence")
                    FormTextField(
                        value = sequence,
                        onValueChange = { sequence = it },
                        placeholder = "e.g. 4"
                    )
                }

                Column {
                    Text("Requirement", fontSize = 13.sp, color = Color(0xFF475569))
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isRequired = true }
                        ) {
                            RadioButton(
                                selected = isRequired,
                                onClick = { isRequired = true }
                            )
                            Text("Required", fontSize = 13.sp, color = Color(0xFF0F172A))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isRequired = false }
                        ) {
                            RadioButton(
                                selected = !isRequired,
                                onClick = { isRequired = false }
                            )
                            Text("Optional", fontSize = 13.sp, color = Color(0xFF0F172A))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Allow Rework",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "Can be sent back to this stage",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    MiniSwitch(
                        checked = allowRework,
                        onCheckedChange = { allowRework = it }
                    )
                }

                Column {
                    FormLabel("Instructions")
                    FormTextArea(
                        value = instructions,
                        onValueChange = { instructions = it },
                        placeholder = "Complete the stage according to the approved service requirements.",
                        minLines = 4,
                        maxLines = 6
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
                label = "Add Step",
                onClick = {
                    if (stage.isBlank()) {
                        stageError = true
                        return@Next
                    }
                    onAddStep(
                        WorkflowStepItem(
                            sequence = sequence.toIntOrNull() ?: sequenceNumber,
                            stage = stage,
                            workType = workType,
                            isRequired = isRequired,
                            allowRework = allowRework,
                            instructions = instructions
                        )
                    )
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
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ADD GARMENT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Create a new garment under the selected category and garment group.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
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
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )

        Text(
            text = "Garment Information",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
                Text(
                    text = "Active garments can be selected in Service Templates and Orders.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
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
                    .height(46.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF334155),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = { onCreate(name, code, desc) },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "Create Garment",
                    color = whiteBg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ActivateTemplateConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
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
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(24.dp))
                }

                Text("Activate Service Template?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                Text(
                    text = "Once activated, this template will be available for service order creation.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel", color = Color(0xFF334155))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Activate Template", color = whiteBg)
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
    template: ServiceTemplateItem,
    onClose: () -> Unit = {},
    onDeactivate: () -> Unit = onClose
) {
    val tokens = LocalAppTokens.current

    val workflowSteps = listOf(
        "Measurement",
        "Cutting",
        "Stitching",
        "Trial",
        "Finishing",
        "Quality Check",
        "Ready for Delivery"
    )

    val optionalWorks = listOf(
        "Aari Work",
        "Embroidery",
        "Stone Work"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = template.name,
                onClose = onClose
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Service Type: ${template.serviceType}", fontSize = 11.sp, color = Color(0xFF475569))
                    Text("Garment: Men's Shirt", fontSize = 11.sp, color = Color(0xFF475569))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Model: Men's Shirt - Standard Tailoring", fontSize = 11.sp, color = Color(0xFF475569))
                    Text("Total Steps: ${template.stepCount} Steps", fontSize = 11.sp, color = Color(0xFF475569))
                }
            }

            HorizontalDivider(color = title_border)

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
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "This template is currently used by existing orders. Changes will apply only to future orders.",
                        color = Color(0xFF1D4ED8),
                        fontSize = 12.sp
                    )
                }

                Text("Workflow Configuration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Card(
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column {
                        workflowSteps.forEachIndexed { index, stepName ->
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
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("%02d".format(index + 1), fontSize = 10.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(stepName, fontSize = 13.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(1f))
                                Icon(Icons.Default.MoreVert, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                            }
                            if (index < workflowSteps.lastIndex) HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }

                Text("Optional Work", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Card(
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column {
                        optionalWorks.forEachIndexed { index, workName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(workName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                    Text("Optional", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Icon(Icons.Default.MoreVert, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                            }
                            if (index < optionalWorks.lastIndex) HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = whiteBg,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text("Cancel", color = Color(0xFF334155))
                }
                Button(
                    onClick = onDeactivate,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEE2E2),
                        contentColor = Color(0xFFDC2626)
                    )
                ) {
                    Text("Deactivate", color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
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
        Text(title, fontSize = 12.sp, color = title_color)
    }
}

@Composable
private fun MeasurementGroup(
    title: String,
    fields: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
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
                    Text(field, fontSize = 12.sp, color = Color(0xFF334155))
                }
            }
        }
    }
}