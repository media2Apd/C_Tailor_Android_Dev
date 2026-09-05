@file:Suppress("UNUSED_PARAMETER", "AssignedValueIsNeverRead", "DEPRECATION")

package com.cuso.mobile.view.home.sales.settings.garment.garment_category_detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.database.entities.GarmentMeasurement
import com.cuso.mobile.model.settings.GarmentStyleItem
import com.cuso.mobile.model.settings.MeasurementFieldItem
import com.cuso.mobile.model.settings.StyleMeasurementFieldDetail
import com.cuso.mobile.model.settings.StyleMeasurementFieldEntry
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.SettingsViewModel

enum class GarmentConfigStep {
    GARMENT_LIST,
    GARMENT_PROFILE,
    ADD_EXISTING_FIELD,
    CREATE_MEASUREMENT_FIELD,
    CONFIGURATION_PREVIEW,
    ADD_MEASUREMENT_GROUP
}

data class MeasurementGroupItem(
    val id: String,
    val name: String,
    val code: String,
    val description: String = "",
    val displayOrder: Int = 1,
    val isActive: Boolean = true
)

fun MeasurementFieldItem.toStyleFieldDetail(): StyleMeasurementFieldDetail {
    return StyleMeasurementFieldDetail(
        id = this.id,
        name = this.name,
        displayName = this.displayName ?: this.name,
        inputType = this.inputType,
        unit = this.unit ?: "inch",
        options = this.options
    )
}

@Composable
fun GarmentCategoryDetailScreen(
    categoryTitle: String = "Garment Categories",
    segmentId: String? = null,
    garmentId: String? = null,
    onClose: () -> Unit = {},
    onAddGarmentClick: () -> Unit = {},
    onAddGarmentCategoryClick: () -> Unit = onAddGarmentClick,
    onConfigureGarmentClick: (String) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(GarmentConfigStep.GARMENT_LIST) }
    var selectedStyle by remember { mutableStateOf<GarmentStyleItem?>(null) }
    var isConfigurationActive by remember { mutableStateOf(true) }

    // In-memory list of measurement fields for active style configuration
    val activeMeasurementFields = remember { mutableStateListOf<StyleMeasurementFieldEntry>() }

    // Dynamic measurement groups state
    val defaultGroups = listOf(
        MeasurementGroupItem("1", "All Fields", "ALL", displayOrder = 1),
        MeasurementGroupItem("2", "Body", "BODY", displayOrder = 2),
        MeasurementGroupItem("3", "Shoulder", "SHOULDER", displayOrder = 3),
        MeasurementGroupItem("4", "Sleeve", "SLEEVE", displayOrder = 4)
    )
    val dynamicGroups = remember { mutableStateListOf<MeasurementGroupItem>().apply { addAll(defaultGroups) } }

    fun resetStateToGarmentList() {
        activeMeasurementFields.clear()
        selectedStyle = null
        isConfigurationActive = false
        dynamicGroups.clear()
        dynamicGroups.addAll(defaultGroups)
        currentStep = GarmentConfigStep.GARMENT_LIST
    }

    BackHandler {
        when (currentStep) {
            GarmentConfigStep.GARMENT_LIST -> onClose()
            GarmentConfigStep.GARMENT_PROFILE -> resetStateToGarmentList()
            GarmentConfigStep.ADD_EXISTING_FIELD,
            GarmentConfigStep.CREATE_MEASUREMENT_FIELD,
            GarmentConfigStep.CONFIGURATION_PREVIEW,
            GarmentConfigStep.ADD_MEASUREMENT_GROUP -> {
                currentStep = GarmentConfigStep.GARMENT_PROFILE
            }
        }
    }

    LaunchedEffect(segmentId, garmentId) {
        if (!segmentId.isNullOrBlank() || !garmentId.isNullOrBlank()) {
            resetStateToGarmentList()
            viewModel.fetchGarmentStyles(segmentId = segmentId, garmentId = garmentId)
        }
    }

    val garmentStyles by viewModel.garmentStyles.collectAsState()
    val isLoading by viewModel.isLoadingStyles.collectAsState()
    val apiError by viewModel.errorMessage.collectAsState()

    val filteredStyles = remember(garmentStyles, segmentId, garmentId) {
        val normSegmentId = segmentId?.trim()?.takeIf { it.isNotBlank() }
        val normGarmentId = garmentId?.trim()?.takeIf { it.isNotBlank() }

        garmentStyles.filter { item ->
            val itemSegmentId = item.segment?.id?.trim()
            val itemGarmentId = item.garment?.id?.trim()

            val matchSegment = normSegmentId == null || itemSegmentId == normSegmentId
            val matchGarment = normGarmentId == null || itemGarmentId == normGarmentId

            matchSegment && matchGarment
        }
    }

    when (currentStep) {
        GarmentConfigStep.GARMENT_LIST -> {
            GarmentCategoryListView(
                categoryTitle = categoryTitle,
                styles = filteredStyles,
                isLoading = isLoading,
                onClose = onClose,
                onAddGarmentCategoryClick = onAddGarmentCategoryClick,
                segmentId = segmentId,
                garmentId = garmentId,
                onConfigureGarmentClick = { styleItem ->
                    selectedStyle = styleItem
                    isConfigurationActive = styleItem.status.equals("ACTIVE", ignoreCase = true)
                    activeMeasurementFields.clear()
                    activeMeasurementFields.addAll(mergeAllMeasurementFields(styleItem))
                    viewModel.fetchGarmentCategoryById(styleItem.id)
                    onConfigureGarmentClick(styleItem.id)
                    currentStep = GarmentConfigStep.GARMENT_PROFILE
                }
            )
        }

        GarmentConfigStep.GARMENT_PROFILE -> {
            GarmentProfileConfigScreen(
                garmentStyle = selectedStyle,
                activeFields = activeMeasurementFields,
                isActive = isConfigurationActive,
                viewModel = viewModel,
                groupsList = dynamicGroups,
                onClose = { resetStateToGarmentList() },
                onAddGroupClick = { currentStep = GarmentConfigStep.ADD_MEASUREMENT_GROUP },
                onAddExistingClick = { currentStep = GarmentConfigStep.ADD_EXISTING_FIELD },
                onAddFieldClick = { currentStep = GarmentConfigStep.CREATE_MEASUREMENT_FIELD },
                onPreviewClick = { currentStep = GarmentConfigStep.CONFIGURATION_PREVIEW },
                onToggleActiveState = { isConfigurationActive = it },
                onRemoveField = { fieldEntry ->
                    activeMeasurementFields.removeAll {
                        it.id == fieldEntry.id || (it.fieldDetail?.id != null && it.fieldDetail.id == fieldEntry.fieldDetail?.id)
                    }
                }
            )
        }

        GarmentConfigStep.ADD_MEASUREMENT_GROUP -> {
            AddMeasurementGroupScreen(
                initialOrder = dynamicGroups.size + 1,
                onClose = { currentStep = GarmentConfigStep.GARMENT_PROFILE },
                onCreateGroup = { newGroup ->
                    dynamicGroups.add(newGroup)
                    currentStep = GarmentConfigStep.GARMENT_PROFILE
                }
            )
        }

        GarmentConfigStep.CONFIGURATION_PREVIEW -> {
            ConfigurationPreviewScreen(
                garmentStyle = selectedStyle,
                activeFields = activeMeasurementFields,
                garmentTitle = selectedStyle?.displayName ?: selectedStyle?.name ?: "Garment Style",
                viewModel = viewModel,
                onClose = { currentStep = GarmentConfigStep.GARMENT_PROFILE },
                onBackToEdit = { currentStep = GarmentConfigStep.GARMENT_PROFILE },
                onActivateConfirmed = {
                    isConfigurationActive = true
                    currentStep = GarmentConfigStep.GARMENT_PROFILE
                }
            )
        }

        GarmentConfigStep.ADD_EXISTING_FIELD -> {
            val existingIds = remember(activeMeasurementFields.size) {
                activeMeasurementFields.mapNotNull { it.fieldDetail?.id ?: it.id }.toSet()
            }
            val existingNames = remember(activeMeasurementFields.size) {
                activeMeasurementFields.mapNotNull {
                    (it.fieldDetail?.displayName ?: it.fieldDetail?.name)?.trim()?.lowercase()
                }.toSet()
            }

            AddExistingFieldScreen(
                existingFieldIds = existingIds,
                existingFieldNames = existingNames,
                viewModel = viewModel,
                onClose = { currentStep = GarmentConfigStep.GARMENT_PROFILE },
                onAddSelected = { selectedFieldsList ->
                    selectedFieldsList.forEach { fieldItem ->
                        val alreadyExists = activeMeasurementFields.any {
                            val fieldId = it.fieldDetail?.id ?: it.id
                            fieldId == fieldItem.id ||
                                    it.fieldDetail?.name.equals(fieldItem.name, ignoreCase = true) ||
                                    (it.fieldDetail?.displayName != null && it.fieldDetail.displayName.equals(fieldItem.displayName, ignoreCase = true))
                        }
                        if (!alreadyExists) {
                            activeMeasurementFields.add(
                                StyleMeasurementFieldEntry(
                                    id = fieldItem.id,
                                    isRequired = true,
                                    displayOrder = activeMeasurementFields.size + 1,
                                    fieldDetail = fieldItem.toStyleFieldDetail()
                                )
                            )
                        }
                    }
                    currentStep = GarmentConfigStep.GARMENT_PROFILE
                }
            )
        }

        GarmentConfigStep.CREATE_MEASUREMENT_FIELD -> {
            CreateMeasurementFieldScreen(
                viewModel = viewModel,
                onClose = { currentStep = GarmentConfigStep.GARMENT_PROFILE },
                onSave = { newCreatedField ->
                    activeMeasurementFields.add(
                        StyleMeasurementFieldEntry(
                            id = newCreatedField.id,
                            isRequired = true,
                            displayOrder = activeMeasurementFields.size + 1,
                            fieldDetail = newCreatedField.toStyleFieldDetail()
                        )
                    )
                    currentStep = GarmentConfigStep.GARMENT_PROFILE
                }
            )
        }
    }

    DynamicIslandError(
        message = apiError,
        onDismiss = { viewModel.clearErrorMessage() }
    )
}
@Composable
 fun GarmentCategoryListView(
    categoryTitle: String,
    styles: List<GarmentStyleItem>,
    isLoading: Boolean,
    onClose: () -> Unit,
    onAddGarmentCategoryClick: () -> Unit,
    onConfigureGarmentClick: (GarmentStyleItem) -> Unit,
    segmentId: String? = null,
    garmentId: String? = null
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val tokens = LocalAppTokens.current
    var expandedCardMenuId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TitleBar(title = "$categoryTitle Category", onClose = onClose) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${styles.size} Styles / Categories",
                    fontSize = tokens.bodyMedium,
                    color = TextPrimary
                )
                AddActionOutlinedButton(
                    text = "Add Garment Category",
                    onClick = onAddGarmentCategoryClick
                )
            }

            if (isLoading) {
                ListSkeleton()
            } else if (styles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No styles or categories found for this garment.",
                        fontSize = tokens.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(styles, key = { it.id }) { item ->
                        val isActive = item.status.equals("ACTIVE", ignoreCase = true)
                        val statusText = if (isActive) "ACTIVE" else "DRAFT"
                        val statusBg = if (isActive) greenBg else yellowBg
                        val statusTextColor = if (isActive) darkGreenBg else yellowText
                        val fieldsCount = item.measurementFields.size
                        val requiredCount = item.measurementFields.count { it.isRequired }
                        val charge = item.stitchingCharge.toInt()
                        val metaInfoText = "$fieldsCount Fields · $requiredCount Required · Stitching Charge: ₹$charge"

                        Card(
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                        ) {
                            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.displayName ?: item.name,
                                            fontSize = tokens.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        if (!item.sku.isNullOrBlank()) {
                                            Text(
                                                text = "SKU: ${item.sku}",
                                                fontSize = 11.sp,
                                                color = iconMuted
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Status Badge
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

                                        // 3-Dot More Menu
                                        Box {
                                            IconButton(
                                                onClick = { expandedCardMenuId = item.id },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = iconMuted,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = expandedCardMenuId == item.id,
                                                onDismissRequest = { expandedCardMenuId = null },
                                                containerColor = whiteBg
                                            ) {
                                                val isCurrentActive = item.status.equals("ACTIVE", ignoreCase = true)
                                                val actionLabel = when {
                                                    isCurrentActive -> "Deactivate"
                                                    item.status.equals("DRAFT", ignoreCase = true) -> "Make Active"
                                                    else -> "Activate"
                                                }

                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = actionLabel,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (isCurrentActive) redText else darkGreenBg
                                                        )
                                                    },
                                                    onClick = {
                                                        expandedCardMenuId = null
                                                        viewModel.changeGarmentCategoryStatus(
                                                            categoryId = item.id,
                                                            currentStatus = item.status,
                                                            segmentId = segmentId,
                                                            garmentId = garmentId
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!item.description.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = item.description,
                                        fontSize = tokens.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = title_border, thickness = 1.dp)
                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = metaInfoText,
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "Configure →",
                                    color = Primary,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { onConfigureGarmentClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to merge fields from both category and parent garment without duplicates
fun mergeAllMeasurementFields(styleItem: GarmentStyleItem): List<StyleMeasurementFieldEntry> {
    val mergedList = mutableListOf<StyleMeasurementFieldEntry>()
    val seenFieldIds = mutableSetOf<String>()

    // 1. First add the category specific measurement fields
    styleItem.measurementFields.forEach { entry ->
        val fId = entry.fieldDetail?.id ?: entry.id
        if (fId != null && seenFieldIds.add(fId)) {
            mergedList.add(entry)
        }
    }

    // 2. Then add the parent garment measurement fields if not already present
    styleItem.garment?.measurementFields?.forEach { entry ->
        val fId = entry.fieldDetail?.id ?: entry.id
        if (fId != null && seenFieldIds.add(fId)) {
            mergedList.add(entry.copy(displayOrder = mergedList.size + 1))
        }
    }

    return mergedList
}

// ─────────────────────────────────────────────────────────────
// Garment Profile Config Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun GarmentProfileConfigScreen(
    garmentStyle: GarmentStyleItem? = null,
    activeFields: MutableList<StyleMeasurementFieldEntry> = remember { mutableStateListOf() },
    styleId: String? = garmentStyle?.id,
    profileTitle: String = garmentStyle?.displayName ?: garmentStyle?.name ?: "Garment Profile",
    isActive: Boolean = garmentStyle?.status.equals("ACTIVE", ignoreCase = true),
    viewModel: SettingsViewModel = hiltViewModel(),
    groupsList: List<MeasurementGroupItem> = emptyList(),
    onClose: () -> Unit,
    onAddGroupClick: () -> Unit = {},
    onAddExistingClick: () -> Unit = {},
    onAddFieldClick: () -> Unit = {},
    onPreviewClick: () -> Unit = {},
    onToggleActiveState: (Boolean) -> Unit = {},
    onRemoveField: (StyleMeasurementFieldEntry) -> Unit = { field ->
        activeFields.removeAll {
            it.id == field.id || (it.fieldDetail?.id != null && it.fieldDetail.id == field.fieldDetail?.id)
        }
    }
) {
    val tokens = LocalAppTokens.current

    val styleDetail by viewModel.selectedStyleDetail.collectAsState()
    val isLoadingDetail by viewModel.isLoadingStyleDetail.collectAsState()
    val isSaving by viewModel.isLoadingStyles.collectAsState()

    // Populate activeFields ONLY IF it is empty when styleDetail loads
    LaunchedEffect(styleDetail) {
        styleDetail?.let { detail ->
            if (detail.id == styleId && activeFields.isEmpty()) {
                activeFields.addAll(mergeAllMeasurementFields(detail))
            }
        }
    }

    val currentEffectiveStyle = styleDetail ?: garmentStyle
    val effectiveTitle = currentEffectiveStyle?.displayName ?: currentEffectiveStyle?.name ?: profileTitle

    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    var fieldToDelete by remember { mutableStateOf<StyleMeasurementFieldEntry?>(null) }
    var expandedMenuFieldId by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = effectiveTitle,
                    onClose = onClose,
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isEffectiveActive = currentEffectiveStyle?.status.equals("ACTIVE", ignoreCase = true)
                            val badgeColor = if (isEffectiveActive) Color(0xFF10B981) else Color(0xFFF59E0B)
                            val badgeText = if (isEffectiveActive) "ACTIVE MODE" else "DRAFT MODE"
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(badgeColor, CircleShape)
                            )
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                )
            }
        ) { padding ->
            if (isLoadingDetail && currentEffectiveStyle == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Measurement Groups Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupsList.forEachIndexed { index, group ->
                            val isSelected = selectedGroupIndex == index
                            val count = if (index == 0) activeFields.size else 0

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) primary_light else whiteBg,
                                border = BorderStroke(1.dp, if (isSelected) Primary else sectionBorder),
                                modifier = Modifier.clickable { selectedGroupIndex = index }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.name,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Primary else close_color,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSelected) background_light_purple else modelGray, CircleShape)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 10.sp,
                                            color = if (isSelected) Primary else close_color,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+ Add Existing",
                            color = Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onAddExistingClick() }
                        )

                        AddActionOutlinedButton(
                            text = "Add Field",
                            onClick = onAddFieldClick
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    if (activeFields.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No measurement fields configured yet.",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            itemsIndexed(
                                items = activeFields,
                                key = { index, item -> item.id ?: item.fieldDetail?.id ?: index.toString() }
                            ) { _, fieldEntry ->
                                val fieldDetail = fieldEntry.fieldDetail
                                val fieldName = fieldDetail?.displayName ?: fieldDetail?.name ?: "Field ${fieldEntry.displayOrder}"
                                val inputType = fieldDetail?.inputType ?: "Number"
                                val unitText = if (!fieldDetail?.unit.isNullOrBlank()) " · ${fieldDetail.unit}" else ""

                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(primary_light, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_ruler),
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = fieldName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = title_color
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                if (fieldEntry.isRequired) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFFFEBEB), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("REQUIRED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = redText)
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("OPTIONAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "$inputType$unitText",
                                                fontSize = 12.sp,
                                                color = iconMuted
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE6F7ED), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        }

                                        Box {
                                            IconButton(onClick = { expandedMenuFieldId = fieldEntry.id ?: fieldDetail?.id }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = iconMuted)
                                            }
                                            DropdownMenu(
                                                expanded = expandedMenuFieldId == (fieldEntry.id ?: fieldDetail?.id),
                                                onDismissRequest = { expandedMenuFieldId = null },
                                                containerColor = whiteBg
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Delete", color = title_color) },
                                                    onClick = {
                                                        expandedMenuFieldId = null
                                                        fieldToDelete = fieldEntry
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = onPreviewClick,
            backLabel = "Preview Sizing",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = if (isSaving) "Saving..." else "Save",
                onClick = {
                    if (isSaving) return@Next
                    currentEffectiveStyle?.let { style ->
                        val mappedMeasurements = activeFields.mapIndexed { index, entry ->
                            GarmentMeasurement(
                                id = entry.fieldDetail?.id ?: entry.id ?: "",
                                label = entry.fieldDetail?.displayName ?: entry.fieldDetail?.name ?: "",
                                unit = entry.fieldDetail?.unit ?: "inch",
                                inputType = entry.fieldDetail?.inputType ?: "Number",
                                isRequired = entry.isRequired,
                                displayOrder = index + 1
                            )
                        }

                        viewModel.saveGarmentProfileMeasurements(
                            style = style,
                            measurements = mappedMeasurements,
                            onSuccess = { _ ->
                                successMessage = "Configuration saved successfully"
                                if (!styleId.isNullOrBlank()) {
                                    viewModel.fetchGarmentCategoryById(styleId)
                                }
                            },
                            onError = { err ->
                                errorMessage = err
                            }
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

    fieldToDelete?.let { field ->
        DeleteMeasurementFieldDialog(
            onDismiss = { fieldToDelete = null },
            onConfirmDelete = {
                onRemoveField(field)
                fieldToDelete = null
                successMessage = "Measurement field removed"
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Add Existing Field Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun AddExistingFieldScreen(
    existingFieldIds: Set<String> = emptySet(),
    existingFieldNames: Set<String> = emptySet(),
    existingFieldCodes: Set<String> = emptySet(),
    onClose: () -> Unit,
    onAddSelected: (List<MeasurementFieldItem>) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    var unitExpanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("All Units") }
    val unitOptions = listOf("All Units", "inch", "cm", "meter")

    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("All Types") }
    val typeOptions = listOf("All Types", "Number", "Select", "Text")

    val availableFields by viewModel.measurementFields.collectAsState()
    val isLoading by viewModel.isLoadingMeasurementFields.collectAsState()
    val apiError by viewModel.errorMessage.collectAsState()

    val selectedFields = remember { mutableStateListOf<MeasurementFieldItem>() }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchMeasurementFields()
    }

    val filteredFields = remember(availableFields, searchQuery, selectedUnit, selectedType) {
        availableFields.filter { field ->
            val matchesSearch = searchQuery.isBlank() ||
                    field.name.contains(searchQuery, ignoreCase = true) ||
                    (field.displayName?.contains(searchQuery, ignoreCase = true) == true) ||
                    (field.code?.contains(searchQuery, ignoreCase = true) == true)

            val matchesUnit = selectedUnit == "All Units" ||
                    field.unit.equals(selectedUnit, ignoreCase = true)

            val matchesType = selectedType == "All Types" ||
                    field.inputType.equals(selectedType, ignoreCase = true)

            matchesSearch && matchesUnit && matchesType
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar("Add Existing Field", onClose)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Fields...",
                    showFilterIcon = false
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormDropdown(
                            value = selectedType,
                            expanded = typeExpanded,
                            onExpandChange = { typeExpanded = it },
                            options = typeOptions,
                            onOptionSelected = { selectedType = it }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        FormDropdown(
                            value = selectedUnit,
                            expanded = unitExpanded,
                            onExpandChange = { unitExpanded = it },
                            options = unitOptions,
                            onOptionSelected = { selectedUnit = it }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredFields.size} available fields",
                        fontSize = 12.sp,
                        color = close_color
                    )
                    Text(
                        text = "${selectedFields.size} Selected",
                        fontSize = 12.sp,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else if (filteredFields.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No measurement fields found.",
                            fontSize = tokens.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(filteredFields, key = { it.id }) { item ->
                            val isAlreadyAdded = existingFieldIds.contains(item.id) ||
                                    existingFieldNames.contains(item.name.trim().lowercase()) ||
                                    (item.displayName != null && existingFieldNames.contains(item.displayName.trim().lowercase())) ||
                                    (item.code != null && existingFieldCodes.contains(item.code.trim().uppercase()))

                            val isChecked = isAlreadyAdded || selectedFields.any { it.id == item.id }
                            val fieldDisplayName = item.displayName ?: item.name
                            val unitText = if (!item.unit.isNullOrBlank()) " · ${item.unit}" else ""
                            val inputTypeText = item.inputType

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isAlreadyAdded -> Color(0xFFF8FAFC)
                                    isChecked -> primary_light
                                    else -> whiteBg
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isAlreadyAdded -> Color(0xFFE2E8F0)
                                        isChecked -> Primary
                                        else -> sectionBorder
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isAlreadyAdded) {
                                        if (isChecked) {
                                            selectedFields.removeAll { it.id == item.id }
                                        } else {
                                            selectedFields.add(item)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppCheckbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (!isAlreadyAdded) {
                                                if (checked) {
                                                    if (!selectedFields.any { it.id == item.id }) selectedFields.add(item)
                                                } else {
                                                    selectedFields.removeAll { it.id == item.id }
                                                }
                                            }
                                        }
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fieldDisplayName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isAlreadyAdded) iconMuted else title_color
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "$inputTypeText$unitText",
                                            fontSize = 12.sp,
                                            color = if (isAlreadyAdded) Color(0xFF94A3B8) else TextSecondary
                                        )
                                    }

                                    if (isAlreadyAdded) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "ADDED",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = "Add Selected Fields (${selectedFields.size})",
                onClick = {
                    if (selectedFields.isEmpty()) {
                        errorMessage = "Please select at least one field"
                    } else {
                        successMessage = "${selectedFields.size} fields added"
                        onAddSelected(selectedFields.toList())
                    }
                }
            )
        )

        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            message = errorMessage ?: apiError,
            onDismiss = {
                errorMessage = null
                viewModel.clearErrorMessage()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Create Measurement Field Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun CreateMeasurementFieldScreen(
    onClose: () -> Unit,
    onSave: (MeasurementFieldItem) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    var fieldName by remember { mutableStateOf("") }
    var displayLabel by remember { mutableStateOf("") }
    var fieldCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }

    var fieldTypeExpanded by remember { mutableStateOf(false) }
    var selectedFieldType by remember { mutableStateOf("") }
    val fieldTypeOptions = listOf("Number", "Text", "Select", "Formula")

    var unitExpanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("") }
    val unitOptions = listOf("inch", "cm", "meter")

    var isRequired by remember { mutableStateOf(true) }
    var minValue by remember { mutableStateOf("") }
    var maxValue by remember { mutableStateOf("") }

    val isCreating by viewModel.isLoadingMeasurementFields.collectAsState()

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    var visibilityStatus by remember { mutableStateOf("Always Visible") }
    val visibilityOptions = listOf("Always Visible", "Conditional", "Hidden by Default")

    var dependentFieldExpanded by remember { mutableStateOf(false) }
    var selectedDependentField by remember { mutableStateOf("") }
    val triggerFieldOptions = listOf("Shirt Type", "Fit Preference", "Collar Type", "Sleeve Style")

    var conditionExpanded by remember { mutableStateOf(false) }
    var selectedCondition by remember { mutableStateOf("") }
    val conditionOptions = listOf("Equals", "Not Equals", "Contains", "Is Greater Than")

    val selectedGarments = remember { mutableStateListOf("Shirt", "Formal Shirt") }

    fun onFieldNameChange(name: String) {
        fieldName = name
        displayLabel = name
        fieldCode = name.uppercase().replace(" ", "_")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "Create Measurement Field",
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
                    .padding(bottom = 90.dp)
            ) {
                SectionHeader("Section 1 - Basic Information")
                Spacer(Modifier.height(14.dp))

                FormLabel(text = "Field Name", isRequired = true)
                FormTextField(
                    value = fieldName,
                    onValueChange = { onFieldNameChange(it) },
                    placeholder = "e.g. Chest"
                )

                Spacer(Modifier.height(12.dp))

                FormLabel(text = "Display Label", isRequired = false)
                FormTextField(
                    value = displayLabel,
                    onValueChange = { displayLabel = it },
                    placeholder = "e.g., Chest"
                )

                Spacer(Modifier.height(12.dp))

                FormLabel(text = "Field Code", isRequired = true)
                FormTextField(
                    value = fieldCode,
                    onValueChange = { fieldCode = it.uppercase().replace(" ", "_") },
                    placeholder = "CHEST",
                    enabled = false
                )

                Spacer(Modifier.height(12.dp))

                FormLabel(text = "Group Name", isRequired = false)
                FormTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = "e.g., Upper Body"
                )

                Spacer(Modifier.height(12.dp))

                FormLabel(text = "Description", isRequired = false)
                FormTextArea(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Measure around the fullest part of the chest...",
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(Modifier.height(24.dp))

                SectionHeader("Section 2 - Field Configuration")
                Spacer(Modifier.height(14.dp))

                FormLabel(text = "Field Type")
                FormDropdown(
                    value = selectedFieldType,
                    expanded = fieldTypeExpanded,
                    onExpandChange = { fieldTypeExpanded = it },
                    options = fieldTypeOptions,
                    onOptionSelected = { selectedFieldType = it }
                )

                Spacer(Modifier.height(12.dp))

                FormLabel(text = "Unit")
                FormDropdown(
                    value = selectedUnit,
                    expanded = unitExpanded,
                    onExpandChange = { unitExpanded = it },
                    options = unitOptions,
                    onOptionSelected = { selectedUnit = it }
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Required Field",
                        fontSize = 13.sp,
                        color = title_color,
                        fontWeight = FontWeight.Medium
                    )
                    MiniSwitch(
                        checked = isRequired,
                        onCheckedChange = { isRequired = it }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Min Value", isRequired = false)
                        FormTextField(
                            value = minValue,
                            onValueChange = { minValue = it },
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Max Value", isRequired = false)
                        FormTextField(
                            value = maxValue,
                            onValueChange = { maxValue = it },
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                SectionHeader("Section 3 — Visibility & Rules")
                Spacer(Modifier.height(14.dp))

                FormLabel(text = "Visibility Status", isRequired = false)
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    visibilityOptions.forEach { option ->
                        val isSelected = visibilityStatus == option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { visibilityStatus = option }
                        ) {
                            AppRadioButton(
                                selected = isSelected,
                                onClick = { visibilityStatus = option }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = option,
                                fontSize = 11.sp,
                                color = title_color,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                FormLabel(text = "Dependent Field")
                FormDropdown(
                    value = selectedDependentField.ifEmpty { "Select trigger field" },
                    expanded = dependentFieldExpanded,
                    onExpandChange = { dependentFieldExpanded = it },
                    options = triggerFieldOptions,
                    onOptionSelected = { selectedDependentField = it }
                )

                Spacer(Modifier.height(16.dp))

                FormLabel(text = "Condition")
                FormDropdown(
                    value = selectedCondition.ifEmpty { "Select condition" },
                    expanded = conditionExpanded,
                    onExpandChange = { conditionExpanded = it },
                    options = conditionOptions,
                    onOptionSelected = { selectedCondition = it }
                )

                Spacer(Modifier.height(24.dp))

                SectionHeader("Section 4 — Assignment")
                Spacer(Modifier.height(14.dp))

                FormLabel(text = "Available for Garments", isRequired = false)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    selectedGarments.forEach { garment ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFEEF2FF)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = garment,
                                    fontSize = 13.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    painter = painterResource(R.drawable.ic_close_circle),
                                    contentDescription = "Remove",
                                    tint = Primary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { selectedGarments.remove(garment) }
                                )
                            }
                        }
                    }

                    Text(
                        text = "+ Add more",
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {}
                    )
                }

                Spacer(Modifier.height(28.dp))
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = if (isCreating) "Saving..." else "Save Field",
                onClick = {
                    if (isCreating) return@Next

                    if (fieldName.isBlank()) {
                        errorMessage = "Please enter a field name"
                        return@Next
                    }
                    val finalCode = fieldCode.ifBlank {
                        fieldName.trim().uppercase().replace(" ", "_")
                    }

                    viewModel.createMeasurementField(
                        name = fieldName,
                        displayName = displayLabel.ifBlank { fieldName },
                        code = finalCode,
                        grpName = groupName,
                        description = description,
                        inputType = selectedFieldType,
                        unit = selectedUnit,
                        minValue = minValue.toDoubleOrNull(),
                        maxValue = maxValue.toDoubleOrNull(),
                        onSuccess = { createdFieldItem ->
                            successMessage = "Measurement field saved successfully"
                            onSave(createdFieldItem)
                        },
                        onError = { err ->
                            errorMessage = ErrorMapper.map(err)
                        }
                    )
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
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = title_color
    )
    Spacer(Modifier.height(5.dp))
    HorizontalDivider(color = grey_border, thickness = 1.dp)
}

// ─────────────────────────────────────────────────────────────
// Add Measurement Group Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun AddMeasurementGroupScreen(
    initialOrder: Int = 1,
    onClose: () -> Unit,
    onCreateGroup: (MeasurementGroupItem) -> Unit
) {
    val tokens = LocalAppTokens.current

    var groupName by remember { mutableStateOf("") }
    var groupCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var displayOrder by remember { mutableStateOf(initialOrder.toString()) }
    var isStatusActive by remember { mutableStateOf(true) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    fun autoGenerateCode(name: String) {
        groupName = name
        if (name.isNotBlank()) {
            groupCode = name.trim().uppercase().replace(" ", "_")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "Add Measurement Group",
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
                    .padding(bottom = 90.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                FormLabel(text = "Group Name", isRequired = true)
                Spacer(Modifier.height(4.dp))
                FormTextField(
                    value = groupName,
                    onValueChange = { autoGenerateCode(it) },
                    placeholder = "Cuff & Wrist"
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Use a clear name that describes the measurements in this group.",
                    fontSize = 11.sp,
                    color = iconMuted
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormLabel(text = "Group Code", isRequired = true)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = "Auto-generated from Group Name",
                            fontSize = 10.sp,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                FormTextField(
                    value = groupCode,
                    onValueChange = { groupCode = it },
                    placeholder = "CUFF_WRIST"
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Unique code used to identify this measurement group.",
                    fontSize = 11.sp,
                    color = iconMuted
                )

                Spacer(Modifier.height(18.dp))

                FormLabel(text = "Description", isRequired = false)
                Spacer(Modifier.height(4.dp))
                FormTextArea(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Describe what measurements this group contains.",
                    minLines = 4,
                    maxLines = 6
                )

                Spacer(Modifier.height(18.dp))

                FormLabel(text = "Display Order", isRequired = false)
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.width(120.dp)) {
                    FormTextField(
                        value = displayOrder,
                        onValueChange = { displayOrder = it },
                        keyboardType = KeyboardType.Number,
                        placeholder = "6"
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Controls where this group appears in the measurement form.",
                    fontSize = 11.sp,
                    color = iconMuted
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

                Spacer(Modifier.height(28.dp))
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = "Create Group",
                onClick = {
                    if (groupName.isBlank()) {
                        errorMessage = "Please enter a group name"
                        return@Next
                    }
                    val finalCode = groupCode.ifBlank {
                        groupName.trim().uppercase().replace(" ", "_")
                    }
                    val order = displayOrder.toIntOrNull() ?: initialOrder

                    successMessage = "Measurement group created successfully"
                    onCreateGroup(
                        MeasurementGroupItem(
                            id = System.currentTimeMillis().toString(),
                            name = groupName.trim(),
                            code = finalCode,
                            description = description.trim(),
                            displayOrder = order,
                            isActive = isStatusActive
                        )
                    )
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
fun DeleteMeasurementFieldDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    var deleteConfirmText by remember { mutableStateOf("") }
    val isDeleteEnabled = deleteConfirmText.trim().equals("DELETE", ignoreCase = false)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFFFE5E5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_alert_shield),
                        contentDescription = null,
                        tint = redText,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Delete Measurement Field?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "This action is permanent. Deleting this measurement field will affect all active orders and customer profiles that reference it.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_amber),
                            contentDescription = null,
                            tint = redText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "This action cannot be undone. Historical measurement data will be orphaned.",
                            fontSize = 11.sp,
                            color = Color(0xFFB91C1C),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Type DELETE to confirm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = deleteConfirmText,
                        onValueChange = { deleteConfirmText = it },
                        placeholder = "Delete",
                        borderColor = redText,
                        textColor = redText,
                        placeholderColor = redText
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text("Cancel", color = title_color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onConfirmDelete,
                        enabled = isDeleteEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = redText,
                            disabledContainerColor = Color(0xFFFCA5A5)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text("Delete", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}