package com.cuso.tailor.view.home.hr.document_management.document_category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.SaveDocumentCategoryRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel

@Composable
fun AddDocumentCategoryScreen(
    categoryId: String? = null,
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()
    val isEditMode = !categoryId.isNullOrBlank()

    // Form inputs state
    var categoryName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var validityRequired by remember { mutableStateOf(true) }
    var autoExpiryAlerts by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("Active") }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }

    val categoryDetail by viewModel.categoryDetailCat.collectAsState()
    val isLoadingDetail by viewModel.isLoadingCategoryDetailCat.collectAsState()
    val isSubmitting by viewModel.isSubmittingCategoryCat.collectAsState()

    // Fetch category details if in Edit Mode
    LaunchedEffect(categoryId) {
        if (isEditMode) {
            viewModel.fetchCategoryByIdCat(
                id = categoryId,
                onError = { errorMessage -> DynamicIslandManager.showError(errorMessage) }
            )
        } else {
            // Reset for create mode
            categoryName = ""
            description = ""
            validityRequired = true
            autoExpiryAlerts = true
            status = "Active"
        }
    }

    // Clean up detail state when navigating away
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCategoryDetailCat()
        }
    }

    // Pre-fill form fields when View-One response is received
    LaunchedEffect(categoryDetail) {
        categoryDetail?.let { detail ->
            if (isEditMode) {
                categoryName = detail.categoryName
                description = detail.description.orEmpty()
                validityRequired = detail.validityRequired
                autoExpiryAlerts = detail.autoExpiryAlerts
                status = detail.status.ifBlank { "Active" }
            }
        }
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Category" else "Add Categories",
                onClose = onClose
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text("Cancel", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium)
                    }

                    // Save / Update Category Button
                    Button(
                        onClick = {
                            val request = SaveDocumentCategoryRequest(
                                categoryName = categoryName.trim(),
                                description = description.trim(),
                                validityRequired = validityRequired,
                                autoExpiryAlerts = autoExpiryAlerts,
                                expiryAlertDays = 30,
                                status = status
                            )

                            if (isEditMode) {
                                viewModel.updateCategoryCat(
                                    id = categoryId,
                                    request = request,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Document category updated successfully")
                                        onClose()
                                    },
                                    onError = { DynamicIslandManager.showError(it) }
                                )
                            } else {
                                viewModel.createCategoryCat(
                                    request = request,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Document category created successfully")
                                        onClose()
                                    },
                                    onError = { DynamicIslandManager.showError(it) }
                                )
                            }
                        },
                        enabled = !isSubmitting && categoryName.isNotBlank() && !isLoadingDetail,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        )
                    ) {
                        if (isSubmitting) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Text(
                                text = if (isEditMode) "Update category" else "Save category",
                                color = whiteBg,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoadingDetail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // Breadcrumbs matching design
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("HR", fontSize = tokens.label, color = mutedText)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
                    Text("Training Management", fontSize = tokens.label, color = mutedText)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (isEditMode) "Edit category" else "Add category",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = title_color,
                    modifier = Modifier.padding(horizontal = tokens.screenPadding)
                )

                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    // Category Name Field
                    FormLabel(text = "CATEGORY NAME")
                    FormTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        placeholder = "e.g. Health Records",
                        containerColor = headerBg,
                        borderColor = sectionBorder
                    )

                    Spacer(Modifier.height(16.dp))

                    // Description Area
                    FormLabel(text = "DESCRIPTION")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Enter brief category description...",
                        minLines = 4,
                        maxLines = 6,
                        borderColor = sectionBorder,
                        focusedBorderColor = Primary,
                        textColor = TextPrimary,
                        placeholderColor = mutedText
                    )

                    Spacer(Modifier.height(16.dp))

                    // Validity Required Toggle (Yes / No)
                    FormLabel(text = "VALIDITY REQUIRED")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { validityRequired = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (validityRequired) light_blue_border else headerBg,
                                contentColor = if (validityRequired) Primary else TextPrimary
                            ),
                            border = if (validityRequired) BorderStroke(1.5.dp, Primary) else BorderStroke(1.dp, sectionBorder)
                        ) {
                            Text("Yes", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { validityRequired = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!validityRequired) light_blue_border else headerBg,
                                contentColor = if (!validityRequired) Primary else TextPrimary
                            ),
                            border = if (!validityRequired) BorderStroke(1.5.dp, Primary) else BorderStroke(1.dp, sectionBorder)
                        ) {
                            Text("No", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Auto Expiry Alerts Switch Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        color = headerBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto Expiry Alerts",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                                Text(
                                    text = "Send notifications 30 days before.",
                                    fontSize = tokens.label,
                                    color = mutedText
                                )
                            }
                            Switch(
                                checked = autoExpiryAlerts,
                                onCheckedChange = { autoExpiryAlerts = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = whiteBg,
                                    checkedTrackColor = Primary
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Status Dropdown
                    FormDropdown(
                        label = "STATUS",
                        value = status,
                        expanded = isStatusDropdownExpanded,
                        onExpandChange = { isStatusDropdownExpanded = it },
                        options = listOf("Active", "Inactive"),
                        onOptionSelected = { status = it }
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}