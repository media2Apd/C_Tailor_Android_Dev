@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)

package com.cuso.tailor.view.home.warehouse

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.WarehouseItem
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardImage
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.ErrorMapper
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.view.composable.SmoothBottomSheet
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.WarehouseFormState

@Composable
fun WarehouseSettingsScreen(
    navController: NavController,
    viewModel: InventoryViewModel = hiltViewModel(),
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val uiState by viewModel.warehouseUiState.collectAsState()
    val warehouseForm by viewModel.warehouseForm.collectAsState()
    val isSubmitting by viewModel.isSubmittingWarehouse.collectAsState()
    val successMessage by viewModel.warehouseActionSuccessMessage.collectAsState()
    val errorMessage by viewModel.warehouseActionErrorMessage.collectAsState()

    var sheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var isEditMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadWarehouses()
    }

    LaunchedEffect(successMessage) {
        if (!successMessage.isNullOrBlank()) {
            sheetState = SheetValue.Hidden
            viewModel.loadWarehouses()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TitleBar(
                    title = "Warehouse",
                    onClose = onBack
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Transparent)
            ) {
                FabScaffold(
                    fab = FabConfig(
                        label = "Add Warehouse",
                        icon = Icons.Default.Add,
                        onClick = {
                            isEditMode = false
                            viewModel.resetWarehouseForm()
                            sheetState = SheetValue.Expanded
                        }
                    ),
                    fabVisible = sheetState == SheetValue.Hidden
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        SearchFilterBar(
                            query = uiState.searchQuery,
                            onQueryChange = { viewModel.onWarehouseSearchQueryChanged(it) },
                            placeholder = "Search Warehouse...",
                            showFilterIcon = true,
                            height = tokens.fieldHeight * 1.15f
                        )

                        HorizontalDivider(color = title_border)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (uiState.isLoading && uiState.warehouses.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Primary)
                                }
                            } else if (uiState.filteredWarehouses.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Warehouse,
                                            contentDescription = null,
                                            tint = mutedText,
                                            modifier = Modifier.size(tokens.iconSize * 2.5f)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = if (uiState.searchQuery.isNotBlank()) "No matching warehouses found" else "No warehouses found",
                                            color = mutedText,
                                            fontSize = tokens.bodyMedium
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    items(uiState.filteredWarehouses, key = { it.id }) { warehouse ->
                                        WarehouseCard(
                                            warehouse = warehouse,
                                            tokens = tokens,
                                            onEdit = {
                                                isEditMode = true
                                                viewModel.populateWarehouseFormForEdit(warehouse)
                                                sheetState = SheetValue.Expanded
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add / Edit Warehouse SmoothBottomSheet
        SmoothBottomSheet(
            state = sheetState,
            onStateChange = { sheetState = it },
            title = if (isEditMode) "Edit Warehouse" else "Add Warehouse",
            subtitle = if (isEditMode) "Warehouse Code: ${warehouseForm.code}" else "Configure warehouse parameters",
            expandedFraction = 0.95f,
            collapsedFraction = 0.60f,
            maxBlurRadius = 16.dp,
            maxScrimAlpha = 0.45f,
            scrollableContent = true,
            collapsedCornerRadius = tokens.cardCornerRadius,
            sheetBackgroundColor = whiteBg,
            onDismissRequest = {
                viewModel.clearWarehouseActionAlerts()
                sheetState = SheetValue.Hidden
            }
        ) {
            WarehouseFormSheetContent(
                formState = warehouseForm,
                isEditMode = isEditMode,
                isSubmitting = isSubmitting,
                tokens = tokens,
                onFormChange = { transform -> viewModel.updateWarehouseForm(transform) },
                onDismiss = { sheetState = SheetValue.Hidden },
                onSubmit = {
                    if (isEditMode) {
                        viewModel.updateWarehouse()
                    } else {
                        viewModel.createWarehouse()
                    }
                }
            )
        }

        // Dynamic Island Alerts
        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { viewModel.clearWarehouseActionAlerts() }
        )

        DynamicIslandError(
            message = errorMessage?.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it) },
            onDismiss = { viewModel.clearWarehouseActionAlerts() }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// DATA CARD COMPOSABLE
// ─────────────────────────────────────────────────────────────

@Composable
private fun WarehouseCard(
    warehouse: WarehouseItem,
    tokens: AppDesignTokens,
    onEdit: () -> Unit
) {
    val locality = warehouse.address.city.ifBlank { "Main Location" }
    val floorArea = if (warehouse.capacitySummary.totalFloorAreaSqft > 0) "${warehouse.capacitySummary.totalFloorAreaSqft.toInt()} sqft" else "—"
    val tempZone = warehouse.capacitySummary.defaultTemperatureZone.replaceFirstChar { it.uppercase() }

    DataCard(
        item = warehouse,
        image = DataCardImage(
            vector = ImageVector.vectorResource(id = R.drawable.ic_warehouse),
            tint = Primary,
            backgroundColor = primary_light,
            shape = RoundedCornerShape(14.dp),
            size = 44.dp
        ),
        smalltitle = warehouse.name,
        titleColor = title_color,
        subtitle = "${warehouse.code} • $locality",
        actions = listOf(
            MenuAction("Edit", Icons.Default.Edit) { onEdit() }
        ),
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                WarehouseStatsRow(
                    stats = listOf(
                        warehouse.hierarchyCounts.floors.toString() to "Floors",
                        warehouse.hierarchyCounts.sections.toString() to "Sections",
                        warehouse.hierarchyCounts.racks.toString() to "Racks",
                        warehouse.hierarchyCounts.bins.toString() to "Bins"
                    ),
                    tokens = tokens
                )

                Spacer(Modifier.height(14.dp))
                Text("Capacity Summary", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floor Area", fontSize = tokens.caption, color = mutedText)
                        Spacer(Modifier.height(2.dp))
                        Text(floorArea, fontSize = tokens.bodySmall, color = title_color)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Temp. Zone", fontSize = tokens.caption, color = mutedText)
                        Spacer(Modifier.height(2.dp))
                        Text(tempZone, fontSize = tokens.bodySmall, color = title_color)
                    }
                }
            }
        }
    )
}

@Composable
private fun WarehouseStatsRow(
    stats: List<Pair<String, String>>,
    tokens: AppDesignTokens
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        stats.forEachIndexed { index, (value, label) ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .width(1.dp)
                        .height(34.dp)
                        .background(grey_border)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                Spacer(Modifier.height(2.dp))
                Text(label, fontSize = tokens.caption, color = mutedText)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ACCORDION FORM CONTAINER
// ─────────────────────────────────────────────────────────────

@Composable
private fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    tokens: AppDesignTokens,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = mutedText
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
        HorizontalDivider(color = title_border)
    }
}

// ─────────────────────────────────────────────────────────────
// MODAL BOTTOM SHEET CONTENT
// ─────────────────────────────────────────────────────────────

@Composable
private fun WarehouseFormSheetContent(
    formState: WarehouseFormState,
    isEditMode: Boolean,
    isSubmitting: Boolean,
    tokens: AppDesignTokens,
    onFormChange: ((WarehouseFormState) -> WarehouseFormState) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var basicExpanded by remember { mutableStateOf(true) }
    var addressExpanded by remember { mutableStateOf(false) }
    var controlExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    var typeExpanded by remember { mutableStateOf(false) }
    val warehouseTypeOptions = listOf("main", "showroom", "transit")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Section 1: Basic Information
        CollapsibleSection(
            title = "Basic Information",
            expanded = basicExpanded,
            tokens = tokens,
            onToggle = { basicExpanded = !basicExpanded }
        ) {
            Column {
                FormLabel("Warehouse Name", isRequired = true)
                FormTextField(
                    value = formState.name,
                    onValueChange = {
                        onFormChange { state -> state.copy(name = it) }
                        nameError = false
                    },
                    placeholder = "Enter warehouse name",
                    isError = nameError,
                    errorMessage = "Warehouse name is required"
                )
            }

            Column {
                FormLabel("Warehouse Code", isRequired = true)
                FormTextField(
                    value = formState.code,
                    onValueChange = {
                        onFormChange { state -> state.copy(code = it) }
                        codeError = false
                    },
                    placeholder = "Enter warehouse code",
                    isError = codeError,
                    errorMessage = "Warehouse code is required"
                )
            }

            FormDropdown(
                label = "Warehouse Type",
                value = formState.type.replaceFirstChar { it.uppercase() },
                expanded = typeExpanded,
                onExpandChange = { typeExpanded = it },
                options = warehouseTypeOptions.map { it.replaceFirstChar { char -> char.uppercase() } },
                onOptionSelected = { selected ->
                    onFormChange { state -> state.copy(type = selected.lowercase()) }
                }
            )

            Column {
                FormLabel("Contact Person")
                FormTextField(
                    value = formState.contactPerson,
                    onValueChange = { onFormChange { state -> state.copy(contactPerson = it) } },
                    placeholder = "Enter contact person"
                )
            }

            Column {
                FormLabel("Contact Phone")
                FormTextField(
                    value = formState.contactPhone,
                    onValueChange = { onFormChange { state -> state.copy(contactPhone = it) } },
                    placeholder = "Enter contact phone",
                    keyboardType = KeyboardType.Phone
                )
            }

            Column {
                FormLabel("Description (Optional)")
                FormTextField(
                    value = formState.description,
                    onValueChange = { onFormChange { state -> state.copy(description = it) } },
                    placeholder = "Enter description"
                )
            }
        }

        // Section 2: Address Details
        CollapsibleSection(
            title = "Address Details",
            expanded = addressExpanded,
            tokens = tokens,
            onToggle = { addressExpanded = !addressExpanded }
        ) {
            Column {
                FormLabel("Address Line")
                FormTextField(
                    value = formState.address,
                    onValueChange = { onFormChange { state -> state.copy(address = it) } },
                    placeholder = "Enter street address"
                )
            }

            Column {
                FormLabel("City")
                FormTextField(
                    value = formState.city,
                    onValueChange = { onFormChange { state -> state.copy(city = it) } },
                    placeholder = "Enter city"
                )
            }

            Column {
                FormLabel("State")
                FormTextField(
                    value = formState.state,
                    onValueChange = { onFormChange { state -> state.copy(state = it) } },
                    placeholder = "Enter state"
                )
            }

            Column {
                FormLabel("Pincode")
                FormTextField(
                    value = formState.pincode,
                    onValueChange = { onFormChange { state -> state.copy(pincode = it) } },
                    placeholder = "Enter pincode",
                    keyboardType = KeyboardType.Number
                )
            }
        }

        // Section 3: Control Settings
        CollapsibleSection(
            title = "Control Settings",
            expanded = controlExpanded,
            tokens = tokens,
            onToggle = { controlExpanded = !controlExpanded }
        ) {
            Column {
                FormLabel("Total Floor Area (sqft)")
                FormTextField(
                    value = formState.totalFloorAreaSqft,
                    onValueChange = { onFormChange { state -> state.copy(totalFloorAreaSqft = it) } },
                    placeholder = "Enter floor area in sqft",
                    keyboardType = KeyboardType.Number
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = formState.status.equals("active", ignoreCase = true),
                        onCheckedChange = { checked ->
                            onFormChange { state -> state.copy(status = if (checked) "active" else "inactive") }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                    )
                    Text("Active", fontSize = tokens.bodySmall, color = TextPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = formState.isDefault,
                        onCheckedChange = { checked ->
                            onFormChange { state -> state.copy(isDefault = checked) }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                    )
                    Text("Default Warehouse", fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action Buttons Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
            }

            Button(
                onClick = {
                    if (formState.name.isBlank()) {
                        nameError = true
                        basicExpanded = true
                        return@Button
                    }
                    if (formState.code.isBlank()) {
                        codeError = true
                        basicExpanded = true
                        return@Button
                    }
                    onSubmit()
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(tokens.iconSize))
                } else {
                    Text(
                        text = if (isEditMode) "Update Warehouse" else "Add Warehouse",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }
    }
}