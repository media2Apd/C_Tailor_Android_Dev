package com.cuso.mobile.view.home.sales.settings.pricing_setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.settings.WorkPricingRequest
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddWorkPricingScreen(
    workId: String? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSaveSuccess: () -> Unit = onClose
) {
    val tokens = LocalAppTokens.current
    val scope = rememberCoroutineScope()

    // --- State Observables ---
    val segments by viewModel.segments.collectAsStateWithLifecycle()
    val garments by viewModel.garments.collectAsStateWithLifecycle()
    val garmentStyles by viewModel.garmentStyles.collectAsStateWithLifecycle()
    val workDetail by viewModel.selectedWorkDetail.collectAsStateWithLifecycle()
    val isFetchingDetail by viewModel.isFetchingWorkDetail.collectAsStateWithLifecycle()

    // --- Form State ---
    var workType by remember { mutableStateOf("") }
    var selectedSegment by remember { mutableStateOf("") }
    var selectedGarment by remember { mutableStateOf("") }
    var selectedVariant by remember { mutableStateOf("") }
    var baseWorkPrice by remember { mutableStateOf("") }
    var isStatusActive by remember { mutableStateOf(true) }

    var segmentExpanded by remember { mutableStateOf(false) }
    var garmentExpanded by remember { mutableStateOf(false) }
    var variantExpanded by remember { mutableStateOf(false) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- Initial Fetch ---
    LaunchedEffect(Unit) {
        viewModel.fetchSegments()
        viewModel.fetchGarments()
        if (workId != null) {
            viewModel.fetchWorkPricingDetail(workId)
        }
    }

    // Populate fields when editing
    LaunchedEffect(workDetail) {
        workDetail?.let { detail ->
            workType = detail.workType
            selectedSegment = detail.segment?.name ?: ""
            selectedGarment = detail.garment?.name ?: ""
            selectedVariant = detail.garmentCategory?.displayName ?: detail.garmentCategory?.name ?: ""
            baseWorkPrice = detail.basePrice.toString()
            isStatusActive = detail.status.equals("Active", ignoreCase = true)
        }
    }

    // Fetch variants based on selection
    LaunchedEffect(selectedSegment, selectedGarment) {
        val segId = segments.find { it.name == selectedSegment }?.id
        val garId = garments.find { it.name == selectedGarment }?.id
        if (segId != null && garId != null) {
            viewModel.fetchGarmentStyles(segId, garId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = if (workId == null) "Add Work Pricing" else "Edit Work Pricing",
                    onClose = {
                        viewModel.clearWorkPricingDetail()
                        onClose()
                    }
                )
            }
        ) { padding ->
            if (isFetchingDetail) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    SectionHeader("1.Select Work Type")
                    FormLabel(text = "Work Type")
                    FormTextField(
                        value = workType,
                        onValueChange = { workType = it },
                        placeholder = "Enter Work Type"
                    )

                    Spacer(Modifier.height(24.dp))
                    SectionHeader("2.Garment Details")

                    FormLabel(text = "Gender Segment")
                    FormDropdown(
                        value = selectedSegment,
                        expanded = segmentExpanded,
                        onExpandChange = { segmentExpanded = it },
                        options = segments.map { it.name },
                        onOptionSelected = {
                            selectedSegment = it
                            selectedVariant = ""
                        }
                    )

                    Spacer(Modifier.height(14.dp))
                    FormLabel(text = "Garment")
                    FormDropdown(
                        value = selectedGarment,
                        expanded = garmentExpanded,
                        onExpandChange = { garmentExpanded = it },
                        options = garments.map { it.name },
                        onOptionSelected = {
                            selectedGarment = it
                            selectedVariant = ""
                        }
                    )

                    Spacer(Modifier.height(14.dp))
                    FormLabel(text = "Variant")
                    FormDropdown(
                        value = selectedVariant,
                        expanded = variantExpanded,
                        onExpandChange = { variantExpanded = it },
                        options = garmentStyles.map { it.displayName ?: it.name },
                        onOptionSelected = { selectedVariant = it }
                    )

                    Spacer(Modifier.height(24.dp))
                    SectionHeader("3.Pricing & Status")

                    FormLabel(text = "Base Work Price")
                    FormTextField(
                        value = baseWorkPrice,
                        onValueChange = { baseWorkPrice = it },
                        placeholder = "₹600",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(text = "Status", fontSize = 13.sp, color = title_color)
                        MiniSwitch(checked = isStatusActive, onCheckedChange = { isStatusActive = it })
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            trailingAction = TrailingFabAction.Update(
                label = if (workId == null) "Save Work Pricing" else "Update Pricing",
                onClick = {
                    val segmentId = segments.find { it.name == selectedSegment }?.id
                    val garmentId = garments.find { it.name == selectedGarment }?.id
                    val variantId = garmentStyles.find { (it.displayName ?: it.name) == selectedVariant }?.id

                    if (workType.isBlank() || segmentId == null || garmentId == null || baseWorkPrice.isBlank()) {
                        errorMessage = "Please fill in all required fields"
                        return@Update
                    }

                    val request = WorkPricingRequest(
                        workType = workType,
                        segmentId = segmentId,
                        garmentId = garmentId,
                        garmentCategoryId = variantId,
                        basePrice = baseWorkPrice.toDoubleOrNull() ?: 0.0,
                        status = if (isStatusActive) "Active" else "Inactive",
                        isTaxable = false
                    )

                    if (workId == null) {
                        viewModel.createWorkPricing(
                            request = request,
                            onSuccess = { msg ->
                                successMessage = msg
                                scope.launch { delay(1000); onSaveSuccess() }
                            },
                            onError = { errorMessage = it }
                        )
                    } else {
                        viewModel.updateWorkPricing(
                            id = workId,
                            request = request,
                            onSuccess = { msg ->
                                successMessage = msg
                                scope.launch { delay(1000); onSaveSuccess() }
                            },
                            onError = { errorMessage = it }
                        )
                    }
                }
            )
        )

        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}

@Composable
fun SectionHeader(text: String) {
    Row(
        Modifier.fillMaxWidth()
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