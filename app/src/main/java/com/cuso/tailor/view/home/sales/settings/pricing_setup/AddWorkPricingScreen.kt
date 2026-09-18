package com.cuso.tailor.view.home.sales.settings.pricing_setup

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
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.WorkPricingRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddWorkPricingScreen(
    workId: String? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    financeViewModel: FinanceViewModel = hiltViewModel(),
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

    // --- Income Account State ---
    var incomeAccountExpanded by remember { mutableStateOf(false) }
    var selectedIncomeAccountId by remember { mutableStateOf<String?>(null) }
    var selectedIncomeAccountName by remember { mutableStateOf("") }

    val accountDropdownList by financeViewModel.accountDropdownList.collectAsStateWithLifecycle()
    val isLoadingAccounts by financeViewModel.isLoadingAccountDropdown.collectAsStateWithLifecycle()

    // --- Initial Fetch ---
    LaunchedEffect(Unit) {
        viewModel.fetchSegments()
        viewModel.fetchGarments()
        financeViewModel.fetchChartOfAccountsDropdown(context = "sales_line")
        if (workId != null) {
            viewModel.fetchWorkPricingDetail(workId)
        }
    }

    // Populate fields when editing
    LaunchedEffect(workDetail, accountDropdownList) {
        workDetail?.let { detail ->
            workType = detail.workType
            selectedSegment = detail.segment?.name ?: ""
            selectedGarment = detail.garment?.name ?: ""
            selectedVariant = detail.garmentCategory?.displayName ?: detail.garmentCategory?.name ?: ""
            baseWorkPrice = detail.basePrice.toString()
            isStatusActive = detail.status.equals("Active", ignoreCase = true)

            // Pre-fill Income Account when editing
            detail.incomeAccount?.let { acc ->
                selectedIncomeAccountId = acc.id
                selectedIncomeAccountName = if (acc.displayName.isNotBlank()) {
                    acc.displayName
                } else {
                    accountDropdownList.find { it.id == acc.id }?.displayName ?: ""
                }
            }
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
                        placeholder = "Enter Work Type",
                        enabled = !isEditMode
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
                        },
                        enabled = !isEditMode
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
                        },
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(14.dp))
                    FormLabel(text = "Variant")
                    FormDropdown(
                        value = selectedVariant,
                        expanded = variantExpanded,
                        onExpandChange = { variantExpanded = it },
                        options = garmentStyles.map { it.displayName ?: it.name },
                        onOptionSelected = { selectedVariant = it },
                        enabled = !isEditMode
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

                    Spacer(Modifier.height(14.dp))

                    // Income Account Dropdown Field
                    FormLabel(text = "Income Account", isRequired = true)
                    FormDropdown(
                        value = selectedIncomeAccountName.ifEmpty {
                            if (isLoadingAccounts) "Loading accounts..." else "Select Income Account"
                        },
                        expanded = incomeAccountExpanded,
                        onExpandChange = { incomeAccountExpanded = it },
                        options = accountDropdownList.map { it.displayName },
                        onOptionSelected = { selectedDisplayName ->
                            selectedIncomeAccountName = selectedDisplayName
                            val matched = accountDropdownList.find { it.displayName == selectedDisplayName }
                            selectedIncomeAccountId = matched?.id
                        }
                    )

                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(text = "Status", fontSize = 13.sp, color = title_color)
                        MiniSwitch(
                            checked = isStatusActive,
                            onCheckedChange = { isStatusActive = it },
                            enabled = !isEditMode
                        )
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

                    // Validate Income Account before sending request
                    if (selectedIncomeAccountId.isNullOrBlank()) {
                        errorMessage = "Please select an Income Account"
                        return@Update
                    }

                    val request = WorkPricingRequest(
                        workType = workType,
                        segmentId = segmentId,
                        garmentId = garmentId,
                        garmentCategoryId = variantId,
                        basePrice = baseWorkPrice.toDoubleOrNull() ?: 0.0,
                        isTaxable = false,
                        incomeAccount = selectedIncomeAccountId
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