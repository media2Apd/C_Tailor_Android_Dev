@file:Suppress("UNUSED_PARAMETER", "unusedVariable")

package com.cuso.mobile.view.home.sales.settings.garment

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorMapper
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ImageUploadSection
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.viewmodel.SettingsViewModel

// ─────────────────────────────────────────────────────────────
// 1. Add New Garment Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun AddNewGarmentScreen(
    onClose: () -> Unit = {},
    onGarmentCreated: () -> Unit = onClose,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    // Observe available segments from ViewModel
    val segments by viewModel.segments.collectAsState()

    LaunchedEffect(Unit) {
        if (segments.isEmpty()) {
            viewModel.fetchSegments()
        }
    }

    var garmentName by remember { mutableStateOf("") }
    var garmentCode by remember { mutableStateOf("") }
    var baseStitchingCharge by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Multi-segment selection state
    var segmentExpanded by remember { mutableStateOf(false) }
    var selectedSegmentIds by remember { mutableStateOf<List<String>>(emptyList()) }

    val selectedSegmentNames = remember(selectedSegmentIds, segments) {
        segments.filter { it.id in selectedSegmentIds }.map { it.name }
    }

    var selectedImagesList by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImagesList = selectedImagesList + uris
        }
    }

    // Dynamic Island State
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Helper function to generate uppercase code from garment name
    fun updateGarmentCodeFromName(name: String) {
        garmentName = name
        garmentCode = name.trim().uppercase().replace("\\s+".toRegex(), "_")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "Add New Garment",
                    onClose = onClose
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = tokens.screenPadding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Garment Name Field with Real-Time Capitalized Code Generation
                Column {
                    FormLabel(text = "Garment Name", isRequired = true)
                    FormTextField(
                        value = garmentName,
                        onValueChange = { newName ->
                            updateGarmentCodeFromName(newName)
                        },
                        placeholder = "Enter Garment name here"
                    )
                }

                // Garment Code Field (Automatically populated in uppercase)
                Column {
                    FormLabel(text = "Garment Code")
                    FormTextField(
                        value = garmentCode,
                        onValueChange = { newCode ->
                            garmentCode = newCode.uppercase().replace("\\s+".toRegex(), "_")
                        },
                        placeholder = "Garment Code will be automatically generated",
                        enabled = false
                    )
                }

                // Multi-Segment Selection Dropdown
                Column {
                    FormDropdown(
                        label = "Segment",
                        expanded = segmentExpanded,
                        onExpandChange = { segmentExpanded = it },
                        options = segments.map { it.name },
                        isMultiSelect = true,
                        selectedOptions = selectedSegmentNames,
                        onMultiOptionSelected = { chosenNames ->
                            selectedSegmentIds = segments
                                .filter { it.name in chosenNames }
                                .map { it.id }
                        },
                        isRequired = true
                    )
                }

                // Base Stitching Charge Field
                Column {
                    FormLabel(text = "Base Stitching Charge (₹)")
                    FormTextField(
                        value = baseStitchingCharge,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                baseStitchingCharge = input
                            }
                        },
                        placeholder = "Enter base stitching charge",
                        keyboardType = KeyboardType.Number
                    )
                }

                // Image Upload Section
                Column {
                    FormLabel(text = "Garment Images")
                    ImageUploadSection(
                        isImage = true,
                        selectedImages = selectedImagesList,
                        browseText = "Browse Images",
                        onBrowseClick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        onCameraClick = null,
                        onRemoveImage = { removedUri ->
                            selectedImagesList = selectedImagesList.filter { it != removedUri }
                        },
                        uploadBoxHeight = 90.dp,
                        imagePreviewSize = 90.dp,
                        previewHeaderTitle = "ATTACHED IMAGES"
                    )
                }

                // Description Field
                Column {
                    FormLabel(text = "Description")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Description here",
                        minLines = 4,
                        maxLines = 6
                    )
                }

                Spacer(Modifier.height(100.dp))
            }
        }

        // Floating Step Navigation FAB
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = "Create Garment",
                onClick = {
                    if (garmentName.isBlank()) {
                        errorMessage = "Please enter a garment name"
                        return@Next
                    }
                    if (selectedSegmentIds.isEmpty()) {
                        errorMessage = "Please select at least one segment"
                        return@Next
                    }

                    val finalCode = garmentCode.ifBlank {
                        garmentName.trim().uppercase().replace("\\s+".toRegex(), "_")
                    }
                    val stitchingCharge = baseStitchingCharge.toDoubleOrNull() ?: 0.0

                    viewModel.createGarment(
                        name = garmentName,
                        code = finalCode,
                        description = description,
                        applicableSegmentIds = selectedSegmentIds,
                        baseStitchingCharge = stitchingCharge,
                        onSuccess = { response ->
                            successMessage = "Garment created successfully"
                            onGarmentCreated()
                        },
                        onError = { error ->
                            errorMessage = ErrorMapper.map(error)
                        }
                    )
                }
            )
        )

        // Dynamic Island Overlay
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

// ─────────────────────────────────────────────────────────────
// 2. Add New Garment Category Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun AddNewGarmentCategoryScreen(
    onClose: () -> Unit = {},
    onGarmentCategoryCreated: () -> Unit = onClose,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val currentSegmentId by viewModel.selectedSegmentIdForStyle.collectAsState()
    val currentGarmentId by viewModel.selectedGarmentIdForStyle.collectAsState()
    val isCreating by viewModel.isCreatingStyle.collectAsState()

    var categoryName by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var stitchingCharge by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "Add New Garment Category",
                    onClose = onClose
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = tokens.screenPadding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Category Name Field
                Column {
                    FormLabel(text = "Garment Category Name", isRequired = true)
                    FormTextField(
                        value = categoryName,
                        onValueChange = {
                            categoryName = it
                            if (displayName.isBlank() || displayName == categoryName.dropLast(1)) {
                                displayName = it
                            }
                        },
                        placeholder = "e.g. Men's Casual Shirt"
                    )
                }

                // Display Name Field
                Column {
                    FormLabel(text = "Display Name", isRequired = true)
                    FormTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        placeholder = "e.g. Casual Shirt"
                    )
                }

                // Stitching Charge Field
                Column {
                    FormLabel(text = "Base Template (Optional)", isRequired = false)
                    FormTextField(
                        value = stitchingCharge,
                        onValueChange = { stitchingCharge = it },
                        placeholder = "Enter Amount",
                        keyboardType = KeyboardType.Number
                    )
                }

                // Description Field
                Column {
                    FormLabel(text = "Description", isRequired = false)
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Brief description of this garment style/category.",
                        minLines = 4,
                        maxLines = 6
                    )
                }

                Spacer(Modifier.height(100.dp))
            }
        }

        // Floating Step Navigation FAB
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = if (isCreating) "Creating..." else "Create Garment Category",
                onClick = {
                    if (isCreating) return@Next

                    if (categoryName.isBlank()) {
                        errorMessage = "Please enter a garment category name"
                        return@Next
                    }
                    if (displayName.isBlank()) {
                        errorMessage = "Please enter a display name"
                        return@Next
                    }
                    if (currentSegmentId.isNullOrBlank() || currentGarmentId.isNullOrBlank()) {
                        errorMessage = "Invalid Segment or Garment reference. Please go back and select again."
                        return@Next
                    }

                    viewModel.createGarmentStyle(
                        name = categoryName,
                        displayName = displayName,
                        description = description,
                        segmentId = currentSegmentId!!,
                        garmentId = currentGarmentId!!,
                        stitchingCharge = stitchingCharge.toDoubleOrNull() ?: 0.0,
                        onSuccess = {
                            successMessage = "Garment category created successfully"
                            onGarmentCategoryCreated()
                        },
                        onError = { err ->
                            errorMessage = ErrorMapper.map(err)
                        }
                    )
                }
            )
        )

        // Dynamic Island Overlay
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