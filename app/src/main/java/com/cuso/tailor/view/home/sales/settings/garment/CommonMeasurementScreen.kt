@file:Suppress("UNUSED_PARAMETER", "AssignedValueIsNeverRead", "DEPRECATION", "unusedVariable")

package com.cuso.tailor.view.home.sales.settings.garment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.database.entities.GarmentMeasurement
import com.cuso.tailor.model.settings.GarmentItem
import com.cuso.tailor.model.settings.MeasurementFieldItem
import com.cuso.tailor.model.settings.StyleMeasurementFieldDetail
import com.cuso.tailor.model.settings.StyleMeasurementFieldEntry
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.settings.garment.garment_category_detail.*
import com.cuso.tailor.viewmodel.SettingsViewModel

enum class CommonMeasurementStep {
    MEASUREMENTS_LIST,
    ADD_EXISTING_FIELD,
    CREATE_MEASUREMENT_FIELD,
    ADD_MEASUREMENT_GROUP
}

@Composable
fun CommonMeasurementsScreen(
    garmentItem: GarmentItem? = null,
    garmentTitle: String = garmentItem?.displayName ?: garmentItem?.name ?: "Garment",
    onClose: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var currentStep by remember { mutableStateOf(CommonMeasurementStep.MEASUREMENTS_LIST) }

    // Active measurement fields state
    val activeMeasurementFields = remember { mutableStateListOf<StyleMeasurementFieldEntry>() }

    // Dynamic measurement groups state
    val defaultGroups = listOf(
        MeasurementGroupItem("1", "All Fields", "ALL", displayOrder = 1),
        MeasurementGroupItem("2", "Body", "BODY", displayOrder = 2),
        MeasurementGroupItem("3", "Shoulder", "SHOULDER", displayOrder = 3),
        MeasurementGroupItem("4", "Sleeve", "SLEEVE", displayOrder = 4)
    )
    val dynamicGroups = remember { mutableStateListOf<MeasurementGroupItem>().apply { addAll(defaultGroups) } }

    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    var fieldToDelete by remember { mutableStateOf<StyleMeasurementFieldEntry?>(null) }
    var expandedMenuFieldId by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isSaving by viewModel.isLoadingGarments.collectAsState()

    // Initialize measurement fields from garmentItem
    LaunchedEffect(garmentItem) {
        garmentItem?.let { garment ->
            if (activeMeasurementFields.isEmpty()) {
                val entries = garment.measurementFields.mapIndexed { index, fieldItem ->
                    StyleMeasurementFieldEntry(
                        id = fieldItem.id ?: fieldItem.field?.id,
                        isRequired = fieldItem.isRequired,
                        displayOrder = fieldItem.displayOrder.takeIf { it > 0 } ?: (index + 1),
                        fieldDetail = fieldItem.field?.let { detail ->
                            StyleMeasurementFieldDetail(
                                id = detail.id ?: "",
                                name = detail.name ?: "",
                                displayName = detail.displayName ?: detail.name,
                                inputType = detail.inputType ?: "Number",
                                unit = detail.unit ?: "inch"
                            )
                        }
                    )
                }
                activeMeasurementFields.addAll(entries)
            }
        }
    }

    // Handle back button per step
    BackHandler {
        when (currentStep) {
            CommonMeasurementStep.MEASUREMENTS_LIST -> onClose()
            CommonMeasurementStep.ADD_EXISTING_FIELD,
            CommonMeasurementStep.CREATE_MEASUREMENT_FIELD,
            CommonMeasurementStep.ADD_MEASUREMENT_GROUP -> {
                currentStep = CommonMeasurementStep.MEASUREMENTS_LIST
            }
        }
    }

    when (currentStep) {
        CommonMeasurementStep.MEASUREMENTS_LIST -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        TitleBar(
                            title = "Common Measurements",
                            onClose = onClose,
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF10B981), CircleShape)
                                    )
                                    Text(
                                        text = garmentTitle.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        // Measurement Groups Scrollable Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dynamicGroups.forEachIndexed { index, group ->
                                val isSelected = selectedGroupIndex == index
                                val count = if (index == 0) activeMeasurementFields.size else 0

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

                        // Action Header Row (+ Add Existing / Add Field)
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
                                modifier = Modifier.clickable {
                                    currentStep = CommonMeasurementStep.ADD_EXISTING_FIELD
                                }
                            )

                            AddActionOutlinedButton(
                                text = "Add Field",
                                onClick = {
                                    currentStep = CommonMeasurementStep.CREATE_MEASUREMENT_FIELD
                                }
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Measurements Fields List
                        if (activeMeasurementFields.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No common measurement fields configured yet.",
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
                                    items = activeMeasurementFields,
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
                                            // Icon Box
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

                                            // Field Info Column
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
                                                                .background(grey_border, RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("OPTIONAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = headerGrey)
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

                                            // Active Badge
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFE6F7ED), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                            }

                                            // More Options Menu
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
                                        HorizontalDivider(color = dividerColor, thickness = 2.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Save Step Navigation FAB
                StepNavigationFab(
                    showBack = true,
                    onBack = onClose,
                    backLabel = "Cancel",
                    showBackArrow = false,
                    showTrailingArrow = false,
                    trailingAction = TrailingFabAction.Next(
                        label = if (isSaving) "Saving..." else "Save Changes",
                        onClick = {
                            if (isSaving) return@Next
                            garmentItem?.let { garment ->
                                val mappedMeasurements = activeMeasurementFields.mapIndexed { index, entry ->
                                    GarmentMeasurement(
                                        id = entry.fieldDetail?.id ?: entry.id ?: "",
                                        label = entry.fieldDetail?.displayName ?: entry.fieldDetail?.name ?: "",
                                        unit = entry.fieldDetail?.unit ?: "inch",
                                        inputType = entry.fieldDetail?.inputType ?: "Number",
                                        isRequired = entry.isRequired,
                                        displayOrder = index + 1
                                    )
                                }

                                // Save locally or trigger API update
                                viewModel.saveSelectedFieldsToLocal(
                                    categoryId = garment.id,
                                    categoryName = garment.name,
                                    selectedFields = activeMeasurementFields.mapNotNull { entry ->
                                        entry.fieldDetail?.let { detail ->
                                            MeasurementFieldItem(
                                                id = detail.id,
                                                name = detail.name,
                                                displayName = detail.displayName,
                                                inputType = detail.inputType,
                                                unit = detail.unit,
                                                options = detail.options
                                            )
                                        }
                                    },
                                    onComplete = {
                                        successMessage = "Common measurements saved successfully"
                                        viewModel.fetchGarments()
                                    }
                                )
                            }
                        }
                    )
                )

                // Dynamic Island Success Message
                DynamicIslandSuccess(
                    message = successMessage,
                    onDismiss = { successMessage = null }
                )

                // Dynamic Island Error Message
                DynamicIslandError(
                    message = errorMessage,
                    onDismiss = { errorMessage = null }
                )
            }

            // Delete Confirmation Dialog
            fieldToDelete?.let { field ->
                DeleteMeasurementFieldDialog(
                    onDismiss = { fieldToDelete = null },
                    onConfirmDelete = {
                        activeMeasurementFields.removeAll {
                            it.id == field.id || (it.fieldDetail?.id != null && it.fieldDetail.id == field.fieldDetail?.id)
                        }
                        fieldToDelete = null
                        successMessage = "Measurement field removed"
                    }
                )
            }
        }

        CommonMeasurementStep.ADD_EXISTING_FIELD -> {
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
                onClose = { currentStep = CommonMeasurementStep.MEASUREMENTS_LIST },
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
                    currentStep = CommonMeasurementStep.MEASUREMENTS_LIST
                }
            )
        }

        CommonMeasurementStep.CREATE_MEASUREMENT_FIELD -> {
            CreateMeasurementFieldScreen(
                viewModel = viewModel,
                onClose = { currentStep = CommonMeasurementStep.MEASUREMENTS_LIST },
                onSave = { newCreatedField ->
                    activeMeasurementFields.add(
                        StyleMeasurementFieldEntry(
                            id = newCreatedField.id,
                            isRequired = true,
                            displayOrder = activeMeasurementFields.size + 1,
                            fieldDetail = newCreatedField.toStyleFieldDetail()
                        )
                    )
                    currentStep = CommonMeasurementStep.MEASUREMENTS_LIST
                }
            )
        }

        CommonMeasurementStep.ADD_MEASUREMENT_GROUP -> {
            AddMeasurementGroupScreen(
                initialOrder = dynamicGroups.size + 1,
                onClose = { currentStep = CommonMeasurementStep.MEASUREMENTS_LIST },
                onCreateGroup = { newGroup ->
                    dynamicGroups.add(newGroup)
                    currentStep = CommonMeasurementStep.MEASUREMENTS_LIST
                }
            )
        }
    }
}