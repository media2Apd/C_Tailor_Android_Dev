@file:Suppress("UNUSED_PARAMETER", "unused", "unusedVariable","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.sales.settings.garment

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.PrimaryIndicator
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
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.GarmentItem
import com.cuso.tailor.model.settings.SegmentItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.ErrorMapper
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.dashedBorder
import com.cuso.tailor.viewmodel.SettingsViewModel

@Composable
fun SalesSettingsScreen(
    navController: NavController? = null,
    onClose: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onAddSegmentClick: () -> Unit = {},
    onEditSegmentClick: (SegmentItem) -> Unit = {},
    onToggleSegmentStatusClick: (SegmentItem, String) -> Unit = { _, _ -> },
    onAddGarmentClick: () -> Unit = {},
    onEditGarmentClick: (GarmentItem) -> Unit = {},
    onCommonMeasurementsClick: (GarmentItem) -> Unit = {},
    onConfigureGarmentClick: (segmentId: String, garmentId: String, garmentTitle: String) -> Unit = { _, _, _ -> },
    viewModel: SettingsViewModel = hiltViewModel()
) {
    GarmentTypeContent(
        onClose = onClose,
        onAddSegmentClick = onAddSegmentClick,
        onEditSegmentClick = onEditSegmentClick,
        onToggleSegmentStatusClick = onToggleSegmentStatusClick,
        onAddGarmentClick = onAddGarmentClick,
        onEditGarmentClick = onEditGarmentClick,
        onCommonMeasurementsClick = onCommonMeasurementsClick,
        onConfigureGarmentClick = onConfigureGarmentClick,
        viewModel = viewModel
    )
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun GarmentTypeContent(
    onClose: () -> Unit = {},
    onAddSegmentClick: () -> Unit = {},
    onEditSegmentClick: (SegmentItem) -> Unit = {},
    onToggleSegmentStatusClick: (SegmentItem, String) -> Unit = { _, _ -> },
    onAddGarmentClick: () -> Unit = {},
    onEditGarmentClick: (GarmentItem) -> Unit = {},
    onCommonMeasurementsClick: (GarmentItem) -> Unit = {},
    onConfigureGarmentClick: (segmentId: String, garmentId: String, garmentTitle: String) -> Unit = { _, _, _ -> },
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val segments by viewModel.segments.collectAsState()
    val segmentsError by viewModel.segmentsError.collectAsState()
    val isLoadingSegments by viewModel.isLoadingSegments.collectAsState()

    val garments by viewModel.garments.collectAsState()
    val garmentsError by viewModel.garmentsError.collectAsState()
    val isLoadingGarments by viewModel.isLoadingGarments.collectAsState()

    var selectedSegmentIndex by remember { mutableIntStateOf(0) }
    var menuExpandedSegmentId by remember { mutableStateOf<String?>(null) }
    var segmentToDelete by remember { mutableStateOf<SegmentItem?>(null) }
    var garmentToDelete by remember { mutableStateOf<GarmentItem?>(null) }

    // ── Edit & Add Garment States ──
    var garmentToEdit by remember { mutableStateOf<GarmentItem?>(null) }
    var isAddGarmentOpen by remember { mutableStateOf(false) }

    // ── Common Measurements Navigation State ──
    var garmentForCommonMeasurements by remember { mutableStateOf<GarmentItem?>(null) }

    var segmentToToggleStatus by remember { mutableStateOf<SegmentItem?>(null) }
    var garmentToToggleStatus by remember { mutableStateOf<GarmentItem?>(null) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchSegments()
        viewModel.fetchGarments()
    }

    LaunchedEffect(segmentsError) {
        if (!segmentsError.isNullOrBlank()) {
            errorMessage = segmentsError
        }
    }

    LaunchedEffect(garmentsError) {
        if (!garmentsError.isNullOrBlank()) {
            errorMessage = garmentsError
        }
    }

    val selectedSegment = if (segments.isNotEmpty()) {
        val safeIndex = selectedSegmentIndex.coerceIn(0, segments.size - 1)
        segments[safeIndex]
    } else null

    val filteredGarments = remember(garments, selectedSegment, searchQuery) {
        garments.filter { garment ->
            val matchesSegment = if (selectedSegment == null) true else {
                garment.applicableSegments.any { it.id == selectedSegment.id }
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                garment.name.contains(searchQuery, ignoreCase = true) ||
                        (garment.displayName?.contains(searchQuery, ignoreCase = true) == true)
            }
            matchesSegment && matchesSearch
        }
    }

    // ── Show Common Measurements Screen when selected ──
    if (garmentForCommonMeasurements != null) {
        CommonMeasurementsScreen(
            garmentItem = garmentForCommonMeasurements,
            onClose = {
                garmentForCommonMeasurements = null
                viewModel.fetchGarments()
            },
            viewModel = viewModel
        )
        return
    }

    // ── Show AddNewGarmentScreen in Add or Edit Mode ──
    if (isAddGarmentOpen || garmentToEdit != null) {
        AddNewGarmentScreen(
            garmentToEdit = garmentToEdit,
            onClose = {
                isAddGarmentOpen = false
                garmentToEdit = null
            },
            onGarmentCreated = {
                isAddGarmentOpen = false
                garmentToEdit = null
                viewModel.fetchGarments() // Refresh garments list
            },
            viewModel = viewModel
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            fab = FabConfig(
                label = "Add Garment",
                icon = Icons.Default.Add,
                onClick = {
                    garmentToEdit = null
                    isAddGarmentOpen = true
                    onAddGarmentClick()
                },
                bottomPadding = 40.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                TitleBar(
                    title = "Garments",
                    onClose = onClose
                )

                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Garments...",
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
                )

                if (segments.isNotEmpty()) {
                    val safeIndex = selectedSegmentIndex.coerceIn(0, segments.size - 1)

                    PrimaryScrollableTabRow(
                        selectedTabIndex = safeIndex,
                        edgePadding = tokens.screenPadding,
                        containerColor = whiteBg,
                        divider = {
                            HorizontalDivider(
                                color = title_border,
                                thickness = 1.dp
                            )
                        },
                        indicator = {
                            PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(safeIndex),
                                color = Primary,
                                height = 2.5.dp
                            )
                        }
                    ) {
                        segments.forEachIndexed { index, segmentItem ->
                            val isSelected = safeIndex == index

                            Tab(
                                selected = isSelected,
                                onClick = {
                                    selectedSegmentIndex = index
                                },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = segmentItem.name,
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = if (isSelected) {
                                                FontWeight.SemiBold
                                            } else {
                                                FontWeight.Normal
                                            },
                                            color = if (isSelected) {
                                                Primary
                                            } else {
                                                TextSecondary
                                            }
                                        )

                                        if (isSelected) {
                                            Spacer(Modifier.width(4.dp))

                                            Box {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clickable(
                                                            indication = null,
                                                            interactionSource = remember {
                                                                MutableInteractionSource()
                                                            }
                                                        ) {
                                                            menuExpandedSegmentId = segmentItem.id
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = "Segment Options",
                                                        tint = Primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = menuExpandedSegmentId == segmentItem.id,
                                                    onDismissRequest = {
                                                        menuExpandedSegmentId = null
                                                    },
                                                    containerColor = whiteBg
                                                ) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = "Edit",
                                                                fontSize = tokens.bodyMedium,
                                                                color = title_color
                                                            )
                                                        },
                                                        onClick = {
                                                            menuExpandedSegmentId = null
                                                            onEditSegmentClick(segmentItem)
                                                        }
                                                    )

                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = "Delete",
                                                                fontSize = tokens.bodyMedium,
                                                                color = redText
                                                            )
                                                        },
                                                        onClick = {
                                                            menuExpandedSegmentId = null
                                                            segmentToDelete = segmentItem
                                                        }
                                                    )

                                                    val statusActionText =
                                                        when (segmentItem.status.lowercase()) {
                                                            "active" -> "Inactive"
                                                            "draft", "inactive" -> "Active"
                                                            else -> "Active"
                                                        }

                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = statusActionText,
                                                                fontSize = tokens.bodyMedium,
                                                                color = title_color
                                                            )
                                                        },
                                                        onClick = {
                                                            menuExpandedSegmentId = null
                                                            segmentToToggleStatus = segmentItem
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                                .dashedBorder(
                                    color = Primary,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    strokeWidth = 1.2.dp,
                                    cornerRadius = tokens.cardCornerRadius * 0.5f
                                )
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .clickable { onAddSegmentClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Add Segment",
                                    color = Primary,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (filteredGarments.isEmpty() && !isLoadingGarments) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No garments found in this segment",
                                    fontSize = tokens.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = filteredGarments,
                            key = { _, item -> item.id }
                        ) { _, item ->
                            val fieldsCount = item.measurementFields.size
                            val measurementsCount = item.measurementFields.count { it.isRequired }
                            val subtitleText = "$fieldsCount Fields · $measurementsCount Measurements"

                            GarmentCategoryCard(
                                title = item.displayName ?: item.name,
                                subtitle = subtitleText,
                                iconRes = R.drawable.ic_shirts,
                                garmentStatus = item.status,
                                onConfigureClick = {
                                    val segId = selectedSegment?.id ?: ""
                                    val garmId = item.id
                                    val title = "${selectedSegment?.name.orEmpty()} ${item.displayName ?: item.name}".trim()

                                    Log.d("NAV_PARAM", "Clicked segmentId: $segId, garmentId: $garmId")
                                    onConfigureGarmentClick(segId, garmId, title)
                                },
                                onCommonMeasurementsClick = {
                                    // ── Open Common Measurements Screen ──
                                    garmentForCommonMeasurements = item
                                    onCommonMeasurementsClick(item)
                                },
                                onEditGarmentClick = {
                                    garmentToEdit = item
                                    onEditGarmentClick(item)
                                },
                                onRemoveGarmentClick = {
                                    garmentToDelete = item
                                },
                                onToggleGarmentStatusClick = {
                                    garmentToToggleStatus = item
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Reusable Delete Model for Garment Deletion ──
        garmentToDelete?.let { garment ->
            DeleteModel(
                title = "You are about to delete a garment",
                message = "This will delete \"${garment.displayName ?: garment.name}\" from garments catalog.\nAre you sure?",
                onDismiss = { garmentToDelete = null },
                onDelete = {
                    val idToDelete = garment.id
                    garmentToDelete = null
                    viewModel.deleteGarment(
                        id = idToDelete,
                        onSuccess = { msg ->
                            successMessage = msg
                        },
                        onError = { err ->
                            errorMessage = ErrorMapper.map(err)
                        }
                    )
                }
            )
        }

        // ── Delete Segment Confirmation Dialog ──
        segmentToDelete?.let { segment ->
            DeleteModel(
                title = "You are about to delete a segment",
                message = "This will delete \"${segment.name}\" segment.\nAre you sure?",
                onDismiss = { segmentToDelete = null },
                onDelete = {
                    val idToDelete = segment.id
                    segmentToDelete = null
                    viewModel.deleteSegment(
                        id = idToDelete,
                        onSuccess = { msg ->
                            selectedSegmentIndex = 0
                            successMessage = msg
                        },
                        onError = { err ->
                            errorMessage = ErrorMapper.map(err)
                        }
                    )
                }
            )
        }

        segmentToToggleStatus?.let { segment ->
            val isCurrentlyActive = segment.status.equals("active", ignoreCase = true)
            val newStatus = if (isCurrentlyActive) "Inactive" else "Active"

            ToggleSegmentStatusDialog(
                isActivating = !isCurrentlyActive,
                segmentName = segment.name,
                entityLabel = "Segment",
                onDismiss = { segmentToToggleStatus = null },
                onConfirm = {
                    segmentToToggleStatus = null
                    viewModel.changeSegmentStatus(
                        id = segment.id,
                        status = newStatus,
                        onSuccess = { msg ->
                            successMessage = msg
                            onToggleSegmentStatusClick(segment, newStatus)
                        },
                        onError = { err ->
                            errorMessage = ErrorMapper.map(err)
                        }
                    )
                }
            )
        }

        garmentToToggleStatus?.let { garment ->
            val isCurrentlyActive = garment.status?.equals("active", ignoreCase = true) == true
            val newStatus = if (isCurrentlyActive) "Inactive" else "Active"

            ToggleSegmentStatusDialog(
                isActivating = !isCurrentlyActive,
                segmentName = garment.displayName ?: garment.name,
                entityLabel = "Garment",
                onDismiss = { garmentToToggleStatus = null },
                onConfirm = {
                    garmentToToggleStatus = null
                    viewModel.changeGarmentStatus(
                        id = garment.id,
                        status = newStatus,
                        onSuccess = { msg ->
                            successMessage = msg
                        },
                        onError = { err ->
                            errorMessage = ErrorMapper.map(err)
                        }
                    )
                }
            )
        }

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
// Toggle Segment Status Confirmation Dialog
// ─────────────────────────────────────────────────────────────
@Composable
fun ToggleSegmentStatusDialog(
    isActivating: Boolean,
    segmentName: String,
    entityLabel: String = "Segment",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val mainColor = if (isActivating) Color(0xFF3B32D1) else Color(0xFFD97706)
    val iconBg = if (isActivating) Color(0xFFF0F2FF) else Color(0xFFFEF3C7)
    val iconRes = if (isActivating) R.drawable.ic_tick_2 else R.drawable.ic_amber

    val dialogTitle = if (isActivating) "Activate Configuration?" else "Deactivate $entityLabel?"
    val confirmLabel = if (isActivating) "Activate" else "Deactivate"

    val dialogMessage = if (isActivating) {
        "This will make the $entityLabel configuration for $segmentName active. All new orders will use this configuration immediately."
    } else {
        "Deactivating this $entityLabel will remove it from all active configurations. Data collected using this field will no longer be accessible."
    }

    val warningMessage = if (isActivating) {
        "This action cannot be undone without creating a new revision."
    } else {
        "Note: Existing records using this $entityLabel will be archived."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = mainColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = dialogTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = dialogMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = if (isActivating) R.drawable.ic_amber else R.drawable.ic_info),
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = warningMessage,
                            fontSize = 13.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4B5563))
                    ) {
                        Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = confirmLabel,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  GarmentCategoryCard
// ─────────────────────────────────────────────────────────────
@Composable
fun GarmentCategoryCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    garmentStatus: String? = null,
    onConfigureClick: () -> Unit,
    onCommonMeasurementsClick: () -> Unit = {},
    onEditGarmentClick: () -> Unit = {},
    onRemoveGarmentClick: () -> Unit = {},
    onToggleGarmentStatusClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var menuExpanded by remember { mutableStateOf(false) }

    val isActive = garmentStatus?.equals("active", ignoreCase = true) == true
    val statusText = if (isActive) "ACTIVE" else "INACTIVE"
    val statusBg = if (isActive) greenBg else yellowBg
    val statusTextColor = if (isActive) darkGreenBg else yellowText

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.fieldHeight)
                        .background(background_light_purple, RoundedCornerShape(tokens.cardCornerRadius * 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                }

                if (!garmentStatus.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusTextColor,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Garment Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
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
                                    text = "Common Measurements",
                                    fontSize = tokens.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onCommonMeasurementsClick()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Edit Garment",
                                    fontSize = tokens.bodyMedium,
                                    color = TextPrimary
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEditGarmentClick()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Remove Garment",
                                    fontSize = tokens.bodyMedium,
                                    color = TextPrimary
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onRemoveGarmentClick()
                            }
                        )

                        val statusActionText = when (garmentStatus?.lowercase()) {
                            "active" -> "Inactive"
                            "draft", "inactive" -> "Active"
                            else -> "Active"
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = statusActionText,
                                    fontSize = tokens.bodyMedium,
                                    color = title_color
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleGarmentStatusClick()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onConfigureClick() }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configure →",
                    color = Primary,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CategoryStatusBadge(status: String) {
    val tokens = LocalAppTokens.current
    val (bgColor, textColor) = when (status.uppercase()) {
        "CONFIGURED" -> Pair(greenBg, darkGreenBg)
        "PENDING" -> Pair(yellowBg, yellowText)
        else -> Pair(primary_light, Primary)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = tokens.label,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
        )
    }
}