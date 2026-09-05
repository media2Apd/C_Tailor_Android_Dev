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

@Composable
fun AddNewGarmentPricingScreen(
    garmentId: String? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSaveSuccess: () -> Unit = onClose
) {
    val tokens = LocalAppTokens.current

    // Observe ViewModel States
    val segments by viewModel.segments.collectAsStateWithLifecycle()
    val selectedGarmentDetail by viewModel.selectedGarment.collectAsStateWithLifecycle()
    val isFetchingDetail by viewModel.isFetchingDetail.collectAsStateWithLifecycle()

    // Wire up the Updating state for the loading indicator
    val isUpdating by viewModel.isUpdatingPrice.collectAsStateWithLifecycle()

    // Form States
    var selectedSegment by remember { mutableStateOf("") }
    var selectedGarmentName by remember { mutableStateOf("") }
    var selectedVariant by remember { mutableStateOf("") }
    var basePrice by remember { mutableStateOf("") }
    var isStatusActive by remember { mutableStateOf(true) }

    // Dropdown UI States
    var segmentExpanded by remember { mutableStateOf(false) }
    var garmentExpanded by remember { mutableStateOf(false) }
    var variantExpanded by remember { mutableStateOf(false) }

    val segmentOptions = segments.map { it.name }
    val garmentOptions = listOf("Men's Shirt", "Trouser", "Kurta", "Blazer")
    val variantOptions = listOf("Full Sleeve", "Half Sleeve", "Sleeveless")

    // 1. Initial Load: Fetch segments and specific detail if in Edit Mode
    LaunchedEffect(Unit) {
        viewModel.fetchSegments()
        if (garmentId != null) {
            viewModel.fetchGarmentDetail(garmentId)
        }
    }

    // 2. Wire Up: Populate fields when API response is received
    LaunchedEffect(selectedGarmentDetail) {
        selectedGarmentDetail?.let { detail ->
            selectedSegment = detail.applicableSegments.firstOrNull()?.name ?: ""
            selectedGarmentName = detail.displayName ?: detail.name
            // Populate stitching charge from API
            basePrice = detail.baseStitchingCharge.toInt().toString()
            // Map status string to Boolean
            isStatusActive = detail.status.equals("Active", ignoreCase = true)
        }
    }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = if (garmentId == null) "Add Basic Garment Pricing" else "Edit Garment Pricing",
                    onClose = {
                        viewModel.clearSelectedGarment()
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
                        .verticalScroll(scrollState)
                        .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
                        .padding(bottom = 100.dp)
                ) {
                    SectionHeader("1. Category Selection")
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Segment")
                    FormDropdown(
                        value = selectedSegment,
                        expanded = segmentExpanded,
                        onExpandChange = { if (garmentId == null) segmentExpanded = it },
                        options = segmentOptions,
                        onOptionSelected = { selectedSegment = it },
                        enabled = garmentId == null // Disable in edit mode
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Garment")
                    FormDropdown(
                        value = selectedGarmentName,
                        expanded = garmentExpanded,
                        onExpandChange = { if (garmentId == null) garmentExpanded = it },
                        options = garmentOptions,
                        onOptionSelected = { selectedGarmentName = it },
                        enabled = garmentId == null // Disable in edit mode
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Garment Variant")
                    FormDropdown(
                        value = selectedVariant,
                        expanded = variantExpanded,
                        onExpandChange = { variantExpanded = it },
                        options = variantOptions,
                        onOptionSelected = { selectedVariant = it }
                    )

                    Spacer(Modifier.height(24.dp))

                    SectionHeader("2. Base Pricing Configuration")
                    Spacer(Modifier.height(14.dp))

                    FormLabel(text = "Base Price")
                    FormTextField(
                        value = basePrice,
                        onValueChange = { basePrice = it },
                        placeholder = "₹1,200",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel(text = "Pricing Unit")
                    FormTextField(
                        value = "",
                        onValueChange = {},
                        enabled = false,
                        placeholder = "Per Garment (Read-Only)"
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        MiniSwitch(
                            checked = isStatusActive,
                            onCheckedChange = { isStatusActive = it }
                        )
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            showBackArrow = false,
            onBack = onClose,
            isLoading = isUpdating, // Show loading on FAB during API call
            trailingAction = TrailingFabAction.Update(
                label = if (garmentId == null) "Save Pricing" else "Update Pricing",
                onClick = {
                    val priceVal = basePrice.toDoubleOrNull() ?: 0.0

                    if (garmentId != null) {
                        // --- WIRE UP: UPDATE CALL ---
                        viewModel.updateGarmentBasicPrice(
                            id = garmentId,
                            price = priceVal,
                            isActive = isStatusActive,
                            onSuccess = { msg ->
                                successMessage = msg
                                onSaveSuccess()
                            },
                            onError = { err ->
                                errorMessage = err
                            }
                        )
                    } else {
                        // Handle Create logic here if needed
                    }
                }
            ),
            showTrailingArrow = false
        )

        DynamicIslandSuccess(message = successMessage, onDismiss = { successMessage = null })
        DynamicIslandError(message = errorMessage, onDismiss = { errorMessage = null })
    }
}