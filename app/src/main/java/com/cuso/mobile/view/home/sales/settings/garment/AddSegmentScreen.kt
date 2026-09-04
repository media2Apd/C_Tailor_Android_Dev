@file:Suppress("UNUSED_PARAMETER")

package com.cuso.mobile.view.home.sales.settings.garment

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
import com.cuso.mobile.model.settings.SegmentItem
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorMapper
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.viewmodel.SettingsViewModel
import androidx.compose.ui.platform.LocalLocale

@Composable
fun AddSegmentScreen(
    segmentToEdit: SegmentItem? = null,
    onClose: () -> Unit = {},
    onSegmentSaved: () -> Unit = onClose,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    // Observe single segment details fetched from the API
    val fetchedSegmentDetail by viewModel.selectedSegmentDetail.collectAsState()

    // Determine the active segment source
    val activeSegment = fetchedSegmentDetail ?: segmentToEdit
    val isEditMode = activeSegment != null

    // Form field states
    var segmentName by remember { mutableStateOf(activeSegment?.name ?: "") }
    var segmentCode by remember { mutableStateOf(activeSegment?.code ?: "") }
    var description by remember { mutableStateOf(activeSegment?.description ?: "") }
    var displayOrder by remember { mutableStateOf(activeSegment?.displayOrder?.toString() ?: "5") }
    var status by remember { mutableStateOf(activeSegment?.status ?: "Active") }
    // Pre-fill form fields whenever segment data is available
    LaunchedEffect(activeSegment) {
        activeSegment?.let { segment ->
            segmentName = segment.name
            segmentCode = segment.code
            description = segment.description ?: ""
            displayOrder = segment.displayOrder.toString()
            status = segment.status ?: "Active"
        }
    }

    var nameError by remember { mutableStateOf(false) }

    // Dynamic Island State
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Auto-generate code from segment name
    fun autoGenerateCode() {
        if (segmentName.isNotBlank()) {
            segmentCode = segmentName.trim().uppercase().replace(" ", "_")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = if (isEditMode) "Edit Segment" else "Add Segment",
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

                // Segment Name Field
                Column {
                    FormLabel(text = "Segment Name", isRequired = true)
                    FormTextField(
                        value = segmentName,
                        onValueChange = {
                            segmentName = it
                            if (it.isNotBlank()) nameError = false
                        },
                        placeholder = "Enter Segment name",
                        isError = nameError,
                        errorMessage = if (nameError) "Segment name is required" else null
                    )
                }

                // Segment Code Field
                Column {
                    FormLabel(text = "Segment Code")
                    FormTextField(
                        value = segmentCode.uppercase(LocalLocale.current.platformLocale),
                        onValueChange = { segmentCode = it },
                        placeholder = "Segment code will auto generate",
                        enabled = false
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Auto-generate from name",
                        color = Primary,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { autoGenerateCode() }
                    )
                }

                // Description Field
                Column {
                    FormLabel(text = "Description")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Corporate and institutional garments",
                        minLines = 3,
                        maxLines = 5
                    )
                }

                // Display Order Field
                Column {
                    FormLabel(text = "Display Order")
                    FormTextField(
                        value = displayOrder,
                        onValueChange = { displayOrder = it },
                        placeholder = "5",
                        keyboardType = KeyboardType.Number
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
                label = if (isEditMode) "Update Segment" else "Create Segment",
                onClick = {
                    if (segmentName.isBlank()) {
                        nameError = true
                        errorMessage = "Please enter a segment name"
                        return@Next
                    }

                    val targetId = activeSegment?.id ?: segmentToEdit?.id

                    if (isEditMode && targetId != null) {
                        viewModel.updateSegment(
                            id = targetId,
                            name = segmentName,
                            code = segmentCode.ifBlank { segmentName.trim().uppercase().replace(" ", "_") },
                            description = description,
                            displayOrder = displayOrder.toIntOrNull() ?: 0,
                            status = status,
                            onSuccess = {
                                successMessage = "Segment updated successfully"
                                onSegmentSaved()
                            },
                            onError = { rawError ->
                                errorMessage = ErrorMapper.map(rawError)
                            }
                        )
                    } else {
                        viewModel.createSegment(
                            name = segmentName,
                            code = segmentCode.ifBlank { segmentName.trim().uppercase().replace(" ", "_") },
                            description = description,
                            displayOrder = displayOrder.toIntOrNull() ?: 0,
                            status = status.equals("Active", ignoreCase = true),                            onSuccess = {
                                successMessage = "Segment created successfully"
                                onSegmentSaved()
                            },
                            onError = { rawError ->
                                errorMessage = ErrorMapper.map(rawError)
                            }
                        )
                    }
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