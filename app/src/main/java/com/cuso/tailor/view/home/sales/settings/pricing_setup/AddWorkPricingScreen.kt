package com.cuso.tailor.view.home.sales.settings.pricing_setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.WorkPricingRequest
import com.cuso.tailor.ui.theme.*

import com.cuso.tailor.utils.formatCurrency
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddWorkPricingScreen(
    workId: String? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSaveSuccess: () -> Unit = onClose
) {
    val tokens = LocalAppTokens.current
    val scope = rememberCoroutineScope()
    val isEditMode = workId != null

    // --- State Observables ---
    val segments by viewModel.segments.collectAsStateWithLifecycle()
    val garments by viewModel.garments.collectAsStateWithLifecycle()
    val garmentStyles by viewModel.garmentStyles.collectAsStateWithLifecycle()
    val taxGroups by inventoryViewModel.taxGroups.collectAsStateWithLifecycle()
    val workDetail by viewModel.selectedWorkDetail.collectAsStateWithLifecycle()
    val isFetchingDetail by viewModel.isFetchingWorkDetail.collectAsStateWithLifecycle()

    // --- Form State (Aligned with React WorkPricingForm) ---
    var workType by remember { mutableStateOf("") }
    var selectedSegmentId by remember { mutableStateOf<String?>(null) }
    var selectedGarmentId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var sacCode by remember { mutableStateOf("") }
    var baseWorkPrice by remember { mutableStateOf("") }
    var isTaxable by remember { mutableStateOf(true) }
    var selectedTaxGroupId by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf("Active") }

    // --- Dropdown Expansion States ---
    var segmentExpanded by remember { mutableStateOf(false) }
    var garmentExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var taxGroupExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    // --- Feedback Banner State ---
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val statusOptions = remember { listOf("Active", "Draft", "Inactive") }

    // Resolve display labels from selected IDs
    val selectedSegmentName = remember(segments, selectedSegmentId) {
        segments.find { it.id == selectedSegmentId }?.name.orEmpty()
    }
    val selectedGarmentName = remember(garments, selectedGarmentId) {
        garments.find { it.id == selectedGarmentId }?.name.orEmpty()
    }
    val selectedCategoryName = remember(garmentStyles, selectedCategoryId) {
        garmentStyles.find { it.id == selectedCategoryId }?.let { it.displayName ?: it.name }.orEmpty()
    }
    val selectedTaxGroupName = remember(taxGroups, selectedTaxGroupId) {
        taxGroups.find { it.id == selectedTaxGroupId }?.name.orEmpty()
    }

    // --- Initial Data Fetch ---
    LaunchedEffect(Unit) {
        viewModel.fetchSegments()
        viewModel.fetchGarments()
        inventoryViewModel.fetchTaxGroups()
        if (workId != null) {
            viewModel.fetchWorkPricingDetail(workId)
        }
    }

    // Populate form fields when editing an existing record
    LaunchedEffect(workDetail) {
        workDetail?.let { detail ->
            workType = detail.workType
            selectedSegmentId = detail.segment?.id
            selectedGarmentId = detail.garment?.id
            selectedCategoryId = detail.garmentCategory?.id
            sacCode = detail.sacCode ?: ""
            baseWorkPrice = detail.basePrice.toString()
            isTaxable = detail.isTaxable
            selectedTaxGroupId = if (detail.isTaxable) detail.taxGroup?.id else null
            selectedStatus = detail.status.ifBlank { "Active" }
        }
    }

    // Fetch categories when both segment and garment are selected
    LaunchedEffect(selectedSegmentId, selectedGarmentId) {
        val segId = selectedSegmentId
        val garId = selectedGarmentId
        if (segId != null && garId != null) {
            viewModel.fetchGarmentStyles(segId, garId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = if (isEditMode) "Edit Work Pricing" else "Add Work Pricing",
                    onClose = {
                        viewModel.clearWorkPricingDetail()
                        onClose()
                    }
                )
            }
        ) { padding ->
            if (isFetchingDetail) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
                        .padding(bottom = 100.dp)
                ) {
                    Text(
                        text = "Configure craft work pricing override rules (Aari, Embroidery, Zari, etc.)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    // --- Section 1: Work Type Identification ---
                    SectionHeader("1. Work Type Identification")
                    FormLabel(text = "Work Type", isRequired = true)
                    FormTextField(
                        value = workType,
                        onValueChange = { workType = it },
                        placeholder = "e.g. Aari Work, Zari Embroidery, Stone Embellishment"
                    )

                    Spacer(Modifier.height(24.dp))

                    // --- Section 2: Target Garment Application (Optional) ---
                    SectionHeader("2. Target Garment Application (Optional)")

                    FormLabel(text = "Segment")
                    FormDropdown(
                        value = selectedSegmentName.ifEmpty { "Select Segment" },
                        expanded = segmentExpanded,
                        onExpandChange = { segmentExpanded = it },
                        options = segments.map { it.name },
                        onOptionSelected = { name ->
                            selectedSegmentId = segments.find { it.name == name }?.id
                            selectedGarmentId = null
                            selectedCategoryId = null
                        }
                    )

                    Spacer(Modifier.height(14.dp))
                    FormLabel(text = "Garment")
                    FormDropdown(
                        value = selectedGarmentName.ifEmpty { "Select Garment" },
                        expanded = garmentExpanded,
                        onExpandChange = { garmentExpanded = it },
                        options = garments.map { it.name },
                        onOptionSelected = { name ->
                            selectedGarmentId = garments.find { it.name == name }?.id
                            selectedCategoryId = null
                        }
                    )

                    Spacer(Modifier.height(14.dp))
                    FormLabel(text = "Category")
                    FormDropdown(
                        value = when {
                            selectedGarmentId == null -> "Select garment first"
                            selectedCategoryName.isNotEmpty() -> selectedCategoryName
                            else -> "Select Category"
                        },
                        expanded = categoryExpanded,
                        onExpandChange = {
                            if (selectedGarmentId != null) {
                                categoryExpanded = it
                            }
                        },
                        options = garmentStyles.map { it.displayName ?: it.name },
                        onOptionSelected = { name ->
                            selectedCategoryId = garmentStyles.find {
                                (it.displayName ?: it.name) == name
                            }?.id
                        },
                        enabled = selectedGarmentId != null
                    )

                    Spacer(Modifier.height(24.dp))

                    // --- Section 3: Pricing, Taxation & Status ---
                    SectionHeader("3. Pricing & Taxation")

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            FormLabel(
                                text = "Base Work Price (${formatCurrency()})",
                                isRequired = true
                            )
                            FormTextField(
                                value = baseWorkPrice,
                                onValueChange = { baseWorkPrice = it },
                                placeholder = "${formatCurrency()}600",
                                keyboardType = KeyboardType.Decimal
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel(text = "SAC Code (Service)")
                            FormTextField(
                                value = sacCode,
                                onValueChange = { sacCode = it },
                                placeholder = "e.g. 998812"
                            )

                            // Status is configurable only during creation, matching React
                            if (!isEditMode) {
                                Spacer(Modifier.height(14.dp))
                                FormLabel(text = "Status")
                                FormDropdown(
                                    value = selectedStatus,
                                    expanded = statusExpanded,
                                    onExpandChange = { statusExpanded = it },
                                    options = statusOptions,
                                    onOptionSelected = { selectedStatus = it }
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Taxable Work Service Toggle Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = "Taxable Work Service",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = title_color
                                        )
                                        Text(
                                            text = "Apply tax group on craft work charges",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    MiniSwitch(
                                        checked = isTaxable,
                                        onCheckedChange = { checked ->
                                            isTaxable = checked
                                            if (!checked) {
                                                selectedTaxGroupId = null
                                            }
                                        }
                                    )
                                }
                            }

                            // Conditional Tax Group Selector
                            AnimatedVisibility(visible = isTaxable) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel(text = "Tax Group")
                                    FormDropdown(
                                        value = selectedTaxGroupName.ifEmpty { "Select Tax Group" },
                                        expanded = taxGroupExpanded,
                                        onExpandChange = { taxGroupExpanded = it },
                                        options = taxGroups.map { it.name },
                                        onOptionSelected = { name ->
                                            selectedTaxGroupId = taxGroups.find { it.name == name }?.id
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Feedback Overlays ---
        errorMessage?.let { msg ->
            DynamicIslandError(
                message = msg,
                onDismiss = { errorMessage = null }
            )
        }
        successMessage?.let { msg ->
            DynamicIslandSuccess(
                message = msg,
                onDismiss = { successMessage = null }
            )
        }

        // --- Bottom Navigation & Save Action ---
        StepNavigationFab(
            showBack = true,
            onBack = {
                viewModel.clearWorkPricingDetail()
                onClose()
            },
            trailingAction = TrailingFabAction.Update(
                label = if (isEditMode) "Update Pricing" else "Save Work Pricing",
                onClick = {
                    val trimmedWorkType = workType.trim()
                    val parsedPrice = baseWorkPrice.toDoubleOrNull()

                    if (trimmedWorkType.isEmpty()) {
                        errorMessage = "Work type is required."
                        return@Update
                    }
                    if (parsedPrice == null || parsedPrice < 0.0) {
                        errorMessage = "Enter a valid base work price."
                        return@Update
                    }

                    val request = WorkPricingRequest(
                        workType = trimmedWorkType,
                        segmentId = selectedSegmentId,
                        garmentId = selectedGarmentId,
                        garmentCategoryId = selectedCategoryId,
                        sacCode = sacCode.trim().ifEmpty { null },
                        basePrice = parsedPrice,
                        isTaxable = isTaxable,
                        taxGroupId = if (isTaxable) selectedTaxGroupId else null,
                        status = if (!isEditMode) selectedStatus else null
                    )

                    if (workId == null) {
                        viewModel.createWorkPricing(
                            request = request,
                            onSuccess = { msg ->
                                successMessage = msg
                                scope.launch {
                                    delay(1000)
                                    onSaveSuccess()
                                }
                            },
                            onError = { errorMessage = it }
                        )
                    } else {
                        viewModel.updateWorkPricing(
                            id = workId,
                            request = request,
                            onSuccess = { msg ->
                                successMessage = msg
                                scope.launch {
                                    delay(1000)
                                    onSaveSuccess()
                                }
                            },
                            onError = { errorMessage = it }
                        )
                    }
                }
            )
        )
    }
}

@Composable
fun SectionHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = title_color
        )
    }
}