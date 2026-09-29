@file:Suppress("UNUSED_PARAMETER", "unused", "unusedVariable", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.sales.settings.design

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.ApplicableGarmentPayload
import com.cuso.tailor.model.settings.DesignItem
import com.cuso.tailor.model.settings.GarmentItem
import com.cuso.tailor.model.settings.SegmentItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.SettingsViewModel

// ─────────────────────────────────────────────────────────────
// Local UI Model for Garment Mappings
// ─────────────────────────────────────────────────────────────

data class GarmentMappingEntry(
    val id: String,
    val segmentId: String,
    val segmentName: String,
    val garmentId: String,
    val garmentName: String,
    val categoryName: String = "All Categories"
)

// ─────────────────────────────────────────────────────────────
// Screen 1: Design Setup List Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun DesignSetupScreen(
    onClose: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var isAddDesignOpen by remember { mutableStateOf(false) }
    var designToEdit by remember { mutableStateOf<DesignItem?>(null) }
    var designToDelete by remember { mutableStateOf<DesignItem?>(null) }

    // API Data Streams from SettingsViewModel
    val designs by viewModel.designs.collectAsState()
    val isLoadingDesigns by viewModel.isLoadingDesigns.collectAsState()
    val designError by viewModel.designError.collectAsState()

    val dynamicSuccess by viewModel.dynamicSuccessMessage.collectAsState()
    val dynamicError by viewModel.dynamicErrorMessage.collectAsState()

    // Fetch designs initially
    LaunchedEffect(Unit) {
        viewModel.fetchDesigns()
        viewModel.fetchSegments()
        viewModel.fetchGarments()
    }

    // Trigger debounced search when user types
    LaunchedEffect(searchQuery) {
        viewModel.fetchDesigns(search = searchQuery.takeIf { it.isNotBlank() })
    }

    if (isAddDesignOpen || designToEdit != null) {
        AddEditDesignScreen(
            designToEdit = designToEdit,
            onClose = {
                isAddDesignOpen = false
                designToEdit = null
                viewModel.clearSelectedDesign()
            },
            onSaveSuccess = {
                isAddDesignOpen = false
                designToEdit = null
                viewModel.clearSelectedDesign()
                viewModel.fetchDesigns()
            },
            viewModel = viewModel
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            fab = FabConfig(
                label = "Add Design",
                icon = Icons.Default.Add,
                onClick = {
                    designToEdit = null
                    isAddDesignOpen = true
                },
                bottomPadding = 40.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Primary_background)
            ) {
                // Header Bar
                TitleBar(
                    title = "Design Setup",
                    onClose = onClose
                )

                HorizontalDivider(color = title_border)
//                Row(
//                    Modifier.fillMaxWidth()
//                        .padding(horizontal =tokens.screenPadding)
//                ) {
//                    Text(
//                        "Manage reusable garment designs and define where each design can be used.",
//                        fontSize = tokens.bodySmall,
//                        color = title_color
//                    )
//                }
//                HorizontalDivider(color = title_border)
                // Search Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search SO number, customer...",
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
                )

                Spacer(Modifier.height(8.dp))

                when {
                    isLoadingDesigns && designs.isEmpty() -> {
                        ListSkeleton()
                    }

                    designError != null && designs.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load designs",
                            message = designError ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchDesigns() }
                        )
                    }

                    designs.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(tokens.screenPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No designs found.",
                                fontSize = tokens.bodyMedium,
                                color = headerGrey
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(designs, key = { it.id }) { design ->
                                DesignCardItem(
                                    design = design,
                                    onEdit = {
                                        designToEdit = design
                                        viewModel.fetchDesignById(design.id)
                                    },
                                    onDelete = {
                                        designToDelete = design
                                    },
                                    onToggleStatus = {
                                        viewModel.changeDesignStatus(design.id, design.status)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Modal
        designToDelete?.let { item ->
            DeleteModel(
                title = "Delete Design?",
                message = "You are about to delete \"${item.name}\". Are you sure?",
                onDismiss = { designToDelete = null },
                onDelete = {
                    val idToDelete = item.id
                    designToDelete = null
                    viewModel.deleteDesign(
                        id = idToDelete,
                        onSuccess = { viewModel.fetchDesigns() }
                    )
                }
            )
        }

        // Real-time Success Notification
        DynamicIslandSuccess(
            modifier = Modifier.padding(top = tokens.fieldHeight * 1.5f),
            message = dynamicSuccess,
            onDismiss = { viewModel.clearSuccessMessage() }
        )

        // Real-time Error Notification
        DynamicIslandError(
            modifier = Modifier.padding(top = tokens.fieldHeight * 1.5f),
            message = dynamicError?.let { ErrorMapper.map(it) },
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Design Card Item Composable
// ─────────────────────────────────────────────────────────────

@Composable
private fun DesignCardItem(
    design: DesignItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var menuExpanded by remember { mutableStateOf(false) }

    val isActive = design.status.equals("Active", ignoreCase = true)
    val displayType = design.designType.replace("_", " ")

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Upper Box: Circular Preview Container with More Options
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(cardBgLight),
                contentAlignment = Alignment.Center
            ) {
                if (!design.imageUrl.isNullOrBlank()) {
                    // Circular cropped preview for image
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(1.dp, grey_border, CircleShape)
                            .background(whiteBg),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = design.imageUrl,
                            contentDescription = design.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Circular container for fallback icon
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(1.dp, grey_border, CircleShape)
                            .background(whiteBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shirts),
                            contentDescription = design.name,
                            tint = TextSecondary,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isActive) greenBg else redBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isActive) "Active" else "Inactive",
                            fontSize = tokens.caption,
                            color = if (isActive) greentext else redText,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 2. Options Menu (3-Dots)
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = iconMuted,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = whiteBg,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Edit",
                                        fontSize = tokens.bodySmall,
                                        color = TextPrimary
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isActive) "Deactivate" else "Activate",
                                        fontSize = tokens.bodySmall,
                                        color = TextPrimary
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onToggleStatus()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Delete",
                                        fontSize = tokens.bodySmall,
                                        color = redText
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = grey_border)

            // Lower Box: Text Information and Status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = design.name,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Design Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(light_grey)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = displayType,
                            fontSize = tokens.caption,
                            color = textSubdued,
                            fontWeight = FontWeight.Normal
                        )
                    }


                }

                // Applicable Garments Summary Text
                val garmentCount = design.applicableGarments.size
                val summaryText = if (garmentCount > 0) {
                    "Applicable to $garmentCount Garment(s)"
                } else {
                    "Available for All Garments"
                }

                Text(
                    text = summaryText,
                    fontSize = tokens.caption,
                    color = headerGrey
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Screen 2: Add / Edit Design Screen (Wired to API)
// ─────────────────────────────────────────────────────────────

@Composable
fun AddEditDesignScreen(
    designToEdit: DesignItem? = null,
    onClose: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val segments by viewModel.segments.collectAsState()
    val garments by viewModel.garments.collectAsState()
    val isSubmitting by viewModel.isSubmittingDesign.collectAsState()
    val selectedDetail by viewModel.selectedDesign.collectAsState()

    val isEdit = designToEdit != null

    // Form fields
    var designName by remember { mutableStateOf(designToEdit?.name.orEmpty()) }
    var designType by remember { mutableStateOf(designToEdit?.designType?.replace("_", " ") ?: "Front Neck") }
    var designTypeExpanded by remember { mutableStateOf(false) }
    var designCode by remember { mutableStateOf(designToEdit?.code.orEmpty()) }
    var description by remember { mutableStateOf(designToEdit?.description.orEmpty()) }
    var statusActive by remember { mutableStateOf(designToEdit?.status?.equals("Active", ignoreCase = true) ?: true) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf(designToEdit?.imageUrl) }

    val mappings = remember { mutableStateListOf<GarmentMappingEntry>() }
    var isAddGarmentModalOpen by remember { mutableStateOf(false) }

    val designTypes = listOf("Front Neck", "Back Neck", "Collar Design", "Sleeve Design", "Embroidery Pattern", "Other")

    // Populate data when editing and detailed response arrives
    LaunchedEffect(selectedDetail) {
        selectedDetail?.let { detail ->
            if (isEdit && detail.id == designToEdit.id) {
                designName = detail.name
                designType = detail.designType.replace("_", " ")
                designCode = detail.code
                description = detail.description.orEmpty()
                statusActive = detail.status.equals("Active", ignoreCase = true)
                existingImageUrl = detail.imageUrl
            }
        }
    }

    // Gallery Picker Launcher with 2 MB Size Validation
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val maxSizeBytes = 2L * 1024 * 1024 // 2 MB in bytes
            val fileSize = getFileSizeInBytes(context, uri)

            if (fileSize in 1..maxSizeBytes) {
                selectedImageUri = uri
                existingImageUrl = null
            } else if (fileSize > maxSizeBytes) {
                viewModel.showError("Selected image exceeds 2 MB limit. Please choose a smaller image.")
            } else {
                // Fallback for cases where file descriptor cannot determine size
                selectedImageUri = uri
                existingImageUrl = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Top Bar
        TitleBar(
            title = if (isEdit) "Edit Design" else "Add Design",
            onClose = onClose
        )

        HorizontalDivider(color = title_border)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Design Information
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Design Information",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )

                    Column {
                        FormLabel("Design Name", isRequired = true)
                        FormTextField(
                            value = designName,
                            onValueChange = { designName = it },
                            placeholder = "Enter design name"
                        )
                    }

                    Column {
                        FormDropdown(
                            label = "Design Type",
                            value = designType,
                            expanded = designTypeExpanded,
                            onExpandChange = { designTypeExpanded = it },
                            options = designTypes,
                            onOptionSelected = { designType = it }
                        )
                    }

                    Column {
                        FormLabel("Design Code")
                        FormTextField(
                            value = designCode,
                            onValueChange = { designCode = it },
                            placeholder = "e.g. FD-FN-001"
                        )
                    }

                    Column {
                        FormLabel("Description")
                        FormTextArea(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Optional short details about this design template...",
                            minLines = 3,
                            maxLines = 5
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Status:",
                                fontSize = tokens.bodyMedium,
                                color = textSubdued
                            )
                            MiniSwitch(
                                checked = statusActive,
                                onCheckedChange = { statusActive = it }
                            )
                            Text(
                                text = if (statusActive) "Active" else "Inactive",
                                fontSize = tokens.bodySmall,
                                color = if (statusActive) Primary else headerGrey,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Card 2: Design Image (Circular Crop Preview with 2 MB Limit)
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Design Image",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Text(
                            text = "Max: 2 MB",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }

                    // Image Display Box Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                            .background(cardBgLight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null || !existingImageUrl.isNullOrBlank()) {
                            // Circular cropped preview for uploaded image
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, grey_border, CircleShape)
                                    .background(whiteBg),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = selectedImageUri ?: existingImageUrl,
                                    contentDescription = "Selected Design Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            // "No Image" dashed circular placeholder
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .dashedBorder(
                                        color = iconMuted.copy(alpha = 0.5f),
                                        shape = CircleShape,
                                        strokeWidth = 1.5.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BrokenImage,
                                        contentDescription = "No Image",
                                        tint = iconMuted.copy(alpha = 0.7f),
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "NO IMAGE",
                                        fontSize = tokens.label,
                                        fontWeight = FontWeight.Medium,
                                        color = iconMuted
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons
                    val hasImage = selectedImageUri != null || !existingImageUrl.isNullOrBlank()
                    if (!hasImage) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Upload Image",
                                color = Primary,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { galleryLauncher.launch("image/*") }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Replace Image",
                                color = Primary,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { galleryLauncher.launch("image/*") }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            )

                            Text(
                                text = "Remove",
                                color = redText,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        selectedImageUri = null
                                        existingImageUrl = null
                                    }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }
            }

            // Card 3: Applicable For Garments
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Applicable For Garments",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )

                    Text(
                        text = "Select the garments and categories where this design should be available.",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )

                    // List of Mapping Cards
                    mappings.forEachIndexed { index, mapping ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            border = BorderStroke(1.dp, sectionBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mapping ${index + 1}",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = textSubdued
                                    )

                                    IconButton(
                                        onClick = { mappings.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Mapping",
                                            tint = redText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Segment: ${mapping.segmentName}",
                                    fontSize = tokens.caption,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Garment: ${mapping.garmentName}",
                                    fontSize = tokens.caption,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Category: ${mapping.categoryName}",
                                    fontSize = tokens.caption,
                                    color = headerGrey
                                )
                            }
                        }
                    }

                    // Add Another Garment Button
                    Row(
                        modifier = Modifier
                            .clickable { isAddGarmentModalOpen = true }
                            .padding(vertical = 6.dp),
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
                            text = "Add Another Garment",
                            color = Primary,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Card 4: Summary Card
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available for",
                        fontSize = tokens.bodySmall,
                        color = headerGrey
                    )
                    Text(
                        text = "${mappings.size} Garment(s) Configured",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }

        // Bottom Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = whiteBg,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
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
                    onClick = {
                        val backendType = designType.replace(" ", "_")
                        val codeVal = designCode.ifBlank {
                            "FD-" + designName.trim().uppercase().take(3) + "-001"
                        }

                        val applicableGarmentsPayload = mappings.map { mapping ->
                            ApplicableGarmentPayload(
                                segmentId = mapping.segmentId,
                                garmentId = mapping.garmentId,
                                garmentCategoryId = null,
                                allCategories = mapping.categoryName == "All Categories"
                            )
                        }

                        if (isEdit) {
                            viewModel.updateDesign(
                                context = context,
                                id = designToEdit.id,
                                name = designName,
                                designType = backendType,
                                code = codeVal,
                                description = description,
//                                status = if (statusActive) "Active" else "Inactive",
                                applicableGarments = applicableGarmentsPayload,
                                imageUri = selectedImageUri,
                                onSuccess = { onSaveSuccess() },
                                onError = { }
                            )
                        } else {
                            viewModel.createDesign(
                                context = context,
                                name = designName,
                                designType = backendType,
                                code = codeVal,
                                description = description,
                                status = if (statusActive) "Active" else "Inactive",
                                applicableGarments = applicableGarmentsPayload,
                                imageUri = selectedImageUri,
                                onSuccess = { onSaveSuccess() },
                                onError = { }
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = !isSubmitting && designName.isNotBlank()
                ) {
                    Text(
                        text = if (isSubmitting) "Saving..." else "Save Design",
                        color = whiteBg,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Modal Dialog to Add a Garment Mapping
    if (isAddGarmentModalOpen) {
        AddGarmentMappingDialog(
            segments = segments,
            garments = garments,
            onDismiss = { isAddGarmentModalOpen = false },
            onAdd = { entry ->
                mappings.add(entry)
                isAddGarmentModalOpen = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Helper Function: Retrieve File Size in Bytes
// ─────────────────────────────────────────────────────────────

private fun getFileSizeInBytes(context: android.content.Context, uri: Uri): Long {
    var size = 0L
    try {
        context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                size = cursor.getLong(sizeIndex)
            }
        }
        if (size == 0L) {
            context.contentResolver.openFileDescriptor(uri, "r")?.use {
                size = it.statSize
            }
        }
    } catch (_: Exception) { }
    return size
}

// ─────────────────────────────────────────────────────────────
// Sub-component: Add Garment Mapping Dialog Modal (Integrated with API Segments/Garments)
// ─────────────────────────────────────────────────────────────

@Composable
fun AddGarmentMappingDialog(
    segments: List<SegmentItem>,
    garments: List<GarmentItem>,
    onDismiss: () -> Unit,
    onAdd: (GarmentMappingEntry) -> Unit
) {
    val tokens = LocalAppTokens.current

    var selectedSegment by remember { mutableStateOf(segments.firstOrNull()) }
    var selectedGarment by remember { mutableStateOf<GarmentItem?>(null) }
    var selectedCategory by remember { mutableStateOf("All Categories") }

    var segmentExpanded by remember { mutableStateOf(false) }
    var garmentExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Filter garments based on chosen segment
    val filteredGarments = remember(selectedSegment, garments) {
        if (selectedSegment != null) {
            garments.filter { g ->
                g.applicableSegments.isEmpty() || g.applicableSegments.any { it.id == selectedSegment?.id }
            }
        } else {
            garments
        }
    }

    LaunchedEffect(filteredGarments) {
        if (selectedGarment == null || !filteredGarments.contains(selectedGarment)) {
            selectedGarment = filteredGarments.firstOrNull()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add Another Garment",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Select the garment and category where this design should be available.",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(light_grey)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = close_color,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                HorizontalDivider(color = title_border)

                // Segment Dropdown
                FormDropdown(
                    label = "Segment",
                    value = selectedSegment?.name ?: "Select segment",
                    expanded = segmentExpanded,
                    onExpandChange = { segmentExpanded = it },
                    options = segments.map { it.name },
                    onOptionSelected = { name ->
                        selectedSegment = segments.find { it.name == name }
                    }
                )

                // Garment Dropdown
                FormDropdown(
                    label = "Garment",
                    value = selectedGarment?.name ?: "Select garment",
                    expanded = garmentExpanded,
                    onExpandChange = { garmentExpanded = it },
                    options = filteredGarments.map { it.name },
                    onOptionSelected = { name ->
                        selectedGarment = filteredGarments.find { it.name == name }
                    }
                )

                // Category Dropdown
                FormDropdown(
                    label = "Category",
                    value = selectedCategory,
                    expanded = categoryExpanded,
                    onExpandChange = { categoryExpanded = it },
                    options = listOf("All Categories", "Standard Pattern", "Designer Blouse"),
                    onOptionSelected = { selectedCategory = it }
                )

                Spacer(Modifier.height(8.dp))

                // Bottom Buttons
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
                        onClick = {
                            val seg = selectedSegment
                            val garm = selectedGarment
                            if (seg != null && garm != null) {
                                onAdd(
                                    GarmentMappingEntry(
                                        id = System.currentTimeMillis().toString(),
                                        segmentId = seg.id,
                                        segmentName = seg.name,
                                        garmentId = garm.id,
                                        garmentName = garm.name,
                                        categoryName = selectedCategory
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = selectedSegment != null && selectedGarment != null
                    ) {
                        Text(
                            text = "Add Garment",
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