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
fun AddWorkPricingScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSaveSuccess: () -> Unit = onClose
) {
    val tokens = LocalAppTokens.current

    // Segments state from ViewModel
    val segments by viewModel.segments.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchSegments()
    }

    val segmentOptions = segments.map { it.name }

    var workType by remember { mutableStateOf("Aari Work") }

    var segmentExpanded by remember { mutableStateOf(false) }
    var selectedSegment by remember { mutableStateOf("") }

    LaunchedEffect(segmentOptions) {
        if (selectedSegment.isEmpty() && segmentOptions.isNotEmpty()) {
            selectedSegment = segmentOptions.first()
        }
    }

    var garmentExpanded by remember { mutableStateOf(false) }
    var selectedGarment by remember { mutableStateOf("Mens Shirt") }
    val garmentOptions = listOf("Mens Shirt", "Women's Blouse", "Lehenga", "Trouser")

    var variantExpanded by remember { mutableStateOf(false) }
    var selectedVariant by remember { mutableStateOf("Full Sleeve") }
    val variantOptions = listOf("Full Sleeve", "Half Sleeve", "Standard")

    var baseWorkPrice by remember { mutableStateOf("₹600") }
    var isStatusActive by remember { mutableStateOf(true) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "Add Work Pricing",
                    onClose = onClose
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
                    .padding(bottom = 100.dp)
            ) {
                // Section 1: Select Work Type
                Text(
                    text = "1. Select Work Type",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Spacer(Modifier.height(14.dp))

                FormLabel(text = "Work Type", isRequired = false)
                FormTextField(
                    value = workType,
                    onValueChange = { workType = it },
                    placeholder = "Aari Work"
                )

                Spacer(Modifier.height(24.dp))

                // Section 2: Garment Details
                Text(
                    text = "2. Garment Details",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Spacer(Modifier.height(14.dp))

                FormDropdown(
                    label = "Gender Segment",
                    value = selectedSegment,
                    expanded = segmentExpanded,
                    onExpandChange = { segmentExpanded = it },
                    options = segmentOptions,
                    onOptionSelected = { selectedSegment = it }
                )

                Spacer(Modifier.height(14.dp))

                FormDropdown(
                    label = "Garment",
                    value = selectedGarment,
                    expanded = garmentExpanded,
                    onExpandChange = { garmentExpanded = it },
                    options = garmentOptions,
                    onOptionSelected = { selectedGarment = it }
                )

                Spacer(Modifier.height(14.dp))

                FormDropdown(
                    label = "Variant",
                    value = selectedVariant,
                    expanded = variantExpanded,
                    onExpandChange = { variantExpanded = it },
                    options = variantOptions,
                    onOptionSelected = { selectedVariant = it }
                )

                Spacer(Modifier.height(24.dp))

                // Section 3: Pricing & Status
                Text(
                    text = "3. Pricing & Status",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Spacer(Modifier.height(14.dp))

                FormLabel(text = "Base Work Price", isRequired = false)
                FormTextField(
                    value = baseWorkPrice,
                    onValueChange = { baseWorkPrice = it },
                    placeholder = "₹600",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(18.dp))

                // Status Switch
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

        // Floating Bottom Navigation FAB
        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            showBackArrow = false,
            onBack = onClose,
            trailingAction = TrailingFabAction.Update(
                label = "Save Work Pricing",
                onClick = {
                    successMessage = "Work pricing saved successfully"
                    onSaveSuccess()
                }
            ),
            showTrailingArrow = false
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