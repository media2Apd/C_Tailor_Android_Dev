@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.tailor.view.home.inventory.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.BinItem
import com.cuso.tailor.model.settings.FloorItemSettings
import com.cuso.tailor.model.settings.RackItem
import com.cuso.tailor.model.settings.SectionItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.SettingsViewModel
import com.cuso.tailor.R

// -------------------------------------------------------------
// Screen 1: Location Structure Overview
// -------------------------------------------------------------
@Composable
fun LocationStructureScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "6a8d5643f685905f29057664",
    onClose: () -> Unit = {},
    onAddLocation: () -> Unit = {},
    onFloorClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()
    val successMsg by viewModel.dynamicSuccessMessage.collectAsStateWithLifecycle()
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(warehouseId) {
        viewModel.fetchFloors(warehouseId, isRefresh = true)
    }

    val totalSections = floors.sumOf { it.sectionsCount }
    val totalRacks = floors.sumOf { it.racksCount }
    val totalBins = floors.sumOf { it.binsCount }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            fab = FabConfig(
                label = "Add Location",
                icon = Icons.Default.Add,
                onClick = onAddLocation
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(title = "Location Structure", onClose = onClose)

                when {
                    isLoading && floors.isEmpty() -> {
                        ListSkeleton()
                    }
                    errorMsg != null && floors.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load location structure",
                            message = errorMsg ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchFloors(warehouseId, isRefresh = true) }
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = tokens.extraPadding,
                                bottom = tokens.buttonHeight * 2
                            ),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            item {
                                WarehouseDetailCard(
                                    icon = R.drawable.ic_hanker,
                                    title = "Central WH",
                                    subtitle = "WH-001 · Madhavaram",
                                    sequenceOrder = "1",
                                    totalSections = totalSections.toString(),
                                    totalRacks = totalRacks.toString(),
                                    totalBins = totalBins.toString(),
                                    showFourGridBoxes = true,
                                    capacityMetrics = listOf(
                                        "Floor Area" to "5,000 sqft",
                                        "Temperature Zone" to "Normal"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        DynamicIslandSuccess(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = successMsg,
            onDismiss = { viewModel.clearSuccessMessage() }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMsg?.takeIf { floors.isNotEmpty() },
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

// -------------------------------------------------------------
// Screen 2: Floor Overview (Infinite Scrolling)
// -------------------------------------------------------------
@Composable
fun FloorOverviewScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "",
    onClose: () -> Unit = {},
    onAddFloor: () -> Unit = {},
    onEditFloor: (FloorItemSettings) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()
    val isPaginating by viewModel.isPaginatingFloors.collectAsStateWithLifecycle()
    val successMsg by viewModel.dynamicSuccessMessage.collectAsStateWithLifecycle()
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()

    var selectedFloorToDelete by remember { mutableStateOf<FloorItemSettings?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(warehouseId) {
        viewModel.fetchFloors(warehouseId, isRefresh = true)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= floors.size - 2) {
                    viewModel.fetchFloors(warehouseId, isRefresh = false)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            fab = FabConfig(
                label = "Add Floor",
                icon = Icons.Default.Add,
                onClick = onAddFloor
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(title = "Floor Overview", onClose = onClose)

                when {
                    isLoading && floors.isEmpty() -> {
                        ListSkeleton()
                    }
                    errorMsg != null && floors.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load floors",
                            message = errorMsg ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchFloors(warehouseId, isRefresh = true) }
                        )
                    }
                    floors.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No floors found",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = tokens.screenPadding),
                            contentPadding = PaddingValues(
                                top = tokens.extraPadding,
                                bottom = tokens.buttonHeight * 2
                            ),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            items(items = floors, key = { it.id }) { floor ->
                                WarehouseDetailCard(
                                    icon = R.drawable.ic_hanker,
                                    title = floor.name,
                                    status = floor.status.uppercase(),
                                    locationLabel = "Floor",
                                    locationName = floor.code.ifBlank { "N/A" },
                                    sequenceOrder = floor.sequenceOrder.toString(),
                                    totalSections = floor.sectionsCount.toString(),
                                    totalRacks = floor.racksCount.toString(),
                                    totalBins = floor.binsCount.toString(),
                                    capacityMetrics = listOf(
                                        "Temperature" to (floor.temperatureZone?.ifBlank { "Normal" } ?: "Normal"),
                                        "Area" to "${floor.floorAreaSqft.toInt()} sqft"
                                    ),
                                    onEditClick = { onEditFloor(floor) },
                                    onDeleteClick = {
                                        selectedFloorToDelete = floor
                                    }
                                )
                            }

                            if (isPaginating) {
                                item {
                                    ThreeDotLoading()
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedFloorToDelete?.let { floor ->
            DeleteModel(
                title = "Delete Floor",
                message = "Are you sure you want to delete '${floor.name}'? This action cannot be undone.",
                onDismiss = {
                    selectedFloorToDelete = null
                },
                onDelete = {
                    val floorIdToDelete = floor.id
                    selectedFloorToDelete = null
                    viewModel.deleteFloor(
                        floorId = floorIdToDelete,
                        warehouseId = warehouseId
                    )
                }
            )
        }

        DynamicIslandSuccess(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = successMsg,
            onDismiss = { viewModel.clearSuccessMessage() }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMsg?.takeIf { floors.isNotEmpty() },
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

// -------------------------------------------------------------
// Screen 3: Add/Edit Floor Form
// -------------------------------------------------------------
@Composable
fun AddFloorScreen(
    floorItem: FloorItemSettings? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = floorItem != null
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearDynamicErrorMessage()
        }
    }

    var floorName by remember(floorItem) { mutableStateOf(floorItem?.name ?: "") }
    var floorCode by remember(floorItem) { mutableStateOf(floorItem?.code ?: "") }
    var sequenceOrder by remember(floorItem) { mutableStateOf(floorItem?.sequenceOrder?.toString() ?: "") }
    var description by remember(floorItem) { mutableStateOf(floorItem?.description ?: "") }
    var temperatureZone by remember(floorItem) { mutableStateOf(floorItem?.temperatureZone ?: "") }
    var floorArea by remember(floorItem) {
        mutableStateOf(floorItem?.floorAreaSqft?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
    }
    var maxWeight by remember(floorItem) {
        mutableStateOf(floorItem?.maxWeightCapacityKg?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
    }
    var isActive by remember(floorItem) { mutableStateOf(floorItem?.status?.equals("active", ignoreCase = true) ?: true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = if (isEditMode) "Edit Floor" else "Add Floor",
                onClose = onClose
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                contentPadding = PaddingValues(bottom = tokens.buttonHeight * 2)
            ) {
                item {
                    SectionHeader("Basic Information")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Floor Name", isRequired = true)
                    FormTextField(
                        value = floorName,
                        onValueChange = { floorName = it },
                        placeholder = "e.g., Ground Floor, Mezzanine Floor",
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Floor Code")
                    FormTextField(
                        value = floorCode,
                        onValueChange = { floorCode = it },
                        placeholder = "e.g., FL-01, GF",
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Sequence Orders")
                    FormTextField(
                        value = sequenceOrder,
                        onValueChange = { sequenceOrder = it },
                        placeholder = "e.g., 1",
                        keyboardType = KeyboardType.Number,
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Description (Optional)")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Enter floor layout remarks, special remarks, or handling instructions...",
                        enabled = !isEditMode
                    )
                }

                item {
                    SectionHeader("Structure Settings")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Temperature Zone")
                    FormTextField(
                        value = temperatureZone,
                        onValueChange = { temperatureZone = it },
                        placeholder = "e.g., Ambient, Normal, Cold Storage (2-8°C)",
                        enabled = true
                    )
                }

                item {
                    SectionHeader("Capacity Info")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Floor Area (sqft)")
                    FormTextField(
                        value = floorArea,
                        onValueChange = { floorArea = it },
                        placeholder = "e.g., 5000",
                        keyboardType = KeyboardType.Number,
                        enabled = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Max Weight Capacity (kg)")
                    FormTextField(
                        value = maxWeight,
                        onValueChange = { maxWeight = it },
                        placeholder = "e.g., 20000",
                        keyboardType = KeyboardType.Number,
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontSize = tokens.bodySmall, color = TextSecondary)
                        MiniSwitch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            onBack = onClose,
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Update(
                label = if (isEditMode) "Update Floor" else "Save Floor",
                onClick = {
                    if (floorName.isBlank() || floorCode.isBlank()) {
                        viewModel.showError("Please fill required fields")
                        return@Update
                    }

                    if (isEditMode) {
                        viewModel.updateFloor(
                            floorId = floorItem.id,
                            warehouseId = warehouseId,
                            name = floorName,
                            code = floorCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            temperatureZone = temperatureZone.ifBlank { "normal" },
                            floorAreaSqft = floorArea.toDoubleOrNull() ?: 0.0,
                            maxWeightCapacityKg = maxWeight.toDoubleOrNull() ?: 0.0,
                            status = if (isActive) "active" else "inactive",
                            onSuccess = onSave,
                            onError = {}
                        )
                    } else {
                        viewModel.createFloor(
                            warehouseId = warehouseId,
                            name = floorName,
                            code = floorCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            temperatureZone = temperatureZone.ifBlank { "normal" },
                            floorAreaSqft = floorArea.toDoubleOrNull() ?: 0.0,
                            maxWeightCapacityKg = maxWeight.toDoubleOrNull() ?: 0.0,
                            status = if (isActive) "active" else "inactive",
                            onSuccess = { onSave() },
                            onError = {}
                        )
                    }
                }
            ),
            backWidthFraction = 0.35f,
            trailingWidthFraction = 0.55f
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMsg,
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

// -------------------------------------------------------------
// Screen 4: Section Overview (Infinite Scrolling)
// -------------------------------------------------------------
@Composable
fun SectionOverviewScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "",
    floorId: String = "",
    onClose: () -> Unit = {},
    onAddSection: () -> Unit = {},
    onEditSection: (SectionItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()
    val isPaginating by viewModel.isPaginatingSections.collectAsStateWithLifecycle()
    val successMsg by viewModel.dynamicSuccessMessage.collectAsStateWithLifecycle()
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()

    var selectedSectionToDelete by remember { mutableStateOf<SectionItem?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(warehouseId, floorId) {
        viewModel.fetchSections(warehouseId, floorId, isRefresh = true)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= sections.size - 2) {
                    viewModel.fetchSections(warehouseId, floorId, isRefresh = false)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            fab = FabConfig(
                label = "Add Section",
                icon = Icons.Default.Add,
                onClick = onAddSection
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(title = "Section Overview", onClose = onClose)

                when {
                    isLoading && sections.isEmpty() -> {
                        ListSkeleton()
                    }
                    errorMsg != null && sections.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load sections",
                            message = errorMsg ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchSections(warehouseId, floorId, isRefresh = true) }
                        )
                    }
                    sections.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sections found",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = tokens.screenPadding),
                            contentPadding = PaddingValues(
                                top = tokens.extraPadding,
                                bottom = tokens.buttonHeight * 2
                            ),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            items(sections, key = { it.id }) { section ->
                                WarehouseDetailCard(
                                    icon = R.drawable.ic_hanker,
                                    title = section.name,
                                    subtitle = section.floorName.ifBlank { null },
                                    status = section.status.uppercase(),
                                    locationLabel = "Section Code",
                                    locationName = section.code.ifBlank { "N/A" },
                                    sequenceOrder = section.sequenceOrder.toString(),
                                    linkedCategory = section.allowedProductCategories.firstOrNull()?.name ?: "General",
                                    totalRacks = section.racksCount.toString(),
                                    totalBins = section.binsCount.toString(),
                                    capacityMetrics = listOf(
                                        "Storage Type" to (section.storageType ?: "shelving"),
                                        "Climate Control" to (section.climateControl ?: "ac_standard")
                                    ),
                                    onEditClick = { onEditSection(section) },
                                    onDeleteClick = {
                                        selectedSectionToDelete = section
                                    }
                                )
                            }

                            if (isPaginating) {
                                item {
                                    ThreeDotLoading()
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedSectionToDelete?.let { section ->
            DeleteModel(
                title = "Delete Section",
                message = "Are you sure you want to delete '${section.name}'? This action cannot be undone.",
                onDismiss = {
                    selectedSectionToDelete = null
                },
                onDelete = {
                    val sectionIdToDelete = section.id
                    selectedSectionToDelete = null
                    viewModel.deleteSection(
                        sectionId = sectionIdToDelete,
                        warehouseId = warehouseId,
                        floorId = floorId
                    )
                }
            )
        }

        DynamicIslandSuccess(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = successMsg,
            onDismiss = { viewModel.clearSuccessMessage() }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMsg?.takeIf { sections.isNotEmpty() },
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

// -------------------------------------------------------------
// Screen 5: Add/Edit Section Form
// -------------------------------------------------------------
@Composable
fun AddSectionScreen(
    sectionItem: SectionItem? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "",
    floorId: String = "",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = sectionItem != null
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()
    val productCategories by viewModel.productCategories.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearDynamicErrorMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchProductCategories()
    }

    var sectionName by remember(sectionItem) { mutableStateOf(sectionItem?.name ?: "") }
    var sectionCode by remember(sectionItem) { mutableStateOf(sectionItem?.code ?: "") }
    var sequenceOrder by remember(sectionItem) { mutableStateOf(sectionItem?.sequenceOrder?.toString() ?: "") }
    var description by remember(sectionItem) { mutableStateOf(sectionItem?.description ?: "") }

    var storageType by remember(sectionItem) { mutableStateOf(sectionItem?.storageType ?: "") }
    var climateControl by remember(sectionItem) { mutableStateOf(sectionItem?.climateControl ?: "") }
    var isActive by remember(sectionItem) { mutableStateOf(sectionItem?.status?.equals("active", ignoreCase = true) ?: true) }

    var selectedCategoryId by remember(sectionItem) {
        mutableStateOf(sectionItem?.allowedProductCategories?.firstOrNull()?.id ?: "")
    }
    var selectedCategoryName by remember(sectionItem) {
        mutableStateOf(sectionItem?.allowedProductCategories?.firstOrNull()?.name ?: "")
    }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = if (isEditMode) "Edit Section" else "Add Section",
                onClose = onClose
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                contentPadding = PaddingValues(bottom = tokens.buttonHeight * 2)
            ) {
                item {
                    SectionHeader("Basic Information")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Section Name", isRequired = true)
                    FormTextField(
                        value = sectionName,
                        onValueChange = { sectionName = it },
                        placeholder = "e.g., Fabric & Textiles Section, Men's Section",
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Section Code")
                    FormTextField(
                        value = sectionCode,
                        onValueChange = { sectionCode = it },
                        placeholder = "e.g., SEC-A, SEC-01",
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Sequence Orders")
                    FormTextField(
                        value = sequenceOrder,
                        onValueChange = { sequenceOrder = it },
                        placeholder = "e.g., 1",
                        keyboardType = KeyboardType.Number,
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Description (Optional)")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Enter section layout, special remarks, or category restrictions...",
                        enabled = !isEditMode
                    )
                }

                item {
                    SectionHeader("Category Mapping")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Allowed Product Category")
                    FormDropdown(
                        value = selectedCategoryName,
                        expanded = isCategoryDropdownExpanded,
                        onExpandChange = { isCategoryDropdownExpanded = it },
                        options = productCategories.map { it.name },
                        onOptionSelected = { chosenName ->
                            selectedCategoryName = chosenName
                            val matchedCategory = productCategories.find { it.name == chosenName }
                            selectedCategoryId = matchedCategory?.id ?: ""
                        }
                    )
                }

                item {
                    SectionHeader("Storage Type")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormTextField(
                        value = storageType,
                        onValueChange = { storageType = it },
                        placeholder = "e.g., Shelving, Pallet Racking, Hanging",
                        enabled = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Climate Control")
                    FormTextField(
                        value = climateControl,
                        onValueChange = { climateControl = it },
                        placeholder = "e.g., AC Standard, Temperature Controlled, Dry",
                        enabled = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontSize = tokens.bodySmall, color = TextSecondary)
                        MiniSwitch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            onBack = onClose,
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Update(
                label = if (isEditMode) "Update Section" else "Save Section",
                onClick = {
                    if (sectionName.isBlank() || sectionCode.isBlank()) {
                        viewModel.showError("Please fill required fields")
                        return@Update
                    }

                    val categoriesPayload = if (selectedCategoryId.isNotBlank()) listOf(selectedCategoryId) else emptyList()

                    if (isEditMode) {
                        viewModel.updateSection(
                            sectionId = sectionItem.id,
                            warehouseId = warehouseId,
                            floorId = floorId,
                            name = sectionName,
                            code = sectionCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            allowedProductCategories = categoriesPayload,
                            storageType = storageType.ifBlank { "shelving" },
                            climateControl = climateControl.ifBlank { "ac_standard" },
                            status = if (isActive) "active" else "inactive",
                            onSuccess = onSave,
                            onError = {}
                        )
                    } else {
                        viewModel.createSection(
                            warehouseId = warehouseId,
                            floorId = floorId,
                            name = sectionName,
                            code = sectionCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            allowedProductCategories = categoriesPayload,
                            storageType = storageType.ifBlank { "shelving" },
                            climateControl = climateControl.ifBlank { "ac_standard" },
                            status = if (isActive) "active" else "inactive",
                            onSuccess = { onSave() },
                            onError = {}
                        )
                    }
                }
            ),
            backWidthFraction = 0.25f,
            trailingWidthFraction = 0.35f
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMsg,
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

// -------------------------------------------------------------
// Screen 6: Rack Overview (Infinite Scrolling)
// -------------------------------------------------------------
@Composable
fun RackOverviewScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "",
    floorId: String = "",
    sectionId: String = "",
    onClose: () -> Unit = {},
    onAddRack: () -> Unit = {},
    onEditRack: (RackItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val racks by viewModel.racks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()
    val isPaginating by viewModel.isPaginatingRacks.collectAsStateWithLifecycle()
    val successMsg by viewModel.dynamicSuccessMessage.collectAsStateWithLifecycle()
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()

    var selectedRackToDelete by remember { mutableStateOf<RackItem?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(warehouseId, floorId, sectionId) {
        viewModel.fetchRacks(warehouseId, floorId, sectionId, isRefresh = true)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= racks.size - 2) {
                    viewModel.fetchRacks(warehouseId, floorId, sectionId, isRefresh = false)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            fab = FabConfig(
                label = "Add Rack",
                icon = Icons.Default.Add,
                onClick = onAddRack
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(title = "Rack Overview", onClose = onClose)

                when {
                    isLoading && racks.isEmpty() -> {
                        ListSkeleton()
                    }
                    errorMsg != null && racks.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load racks",
                            message = errorMsg ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchRacks(warehouseId, floorId, sectionId, isRefresh = true) }
                        )
                    }
                    racks.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No racks found",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = tokens.screenPadding),
                            contentPadding = PaddingValues(
                                top = tokens.extraPadding,
                                bottom = tokens.buttonHeight * 2
                            ),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            items(racks, key = { it.id }) { rack ->
                                WarehouseDetailCard(
                                    icon = R.drawable.ic_hanker,
                                    title = rack.name,
                                    subtitle = rack.warehouseName.ifBlank { null },
                                    status = rack.status.uppercase(),
                                    locationLabel = "Section",
                                    locationName = rack.sectionDisplayName,
                                    sequenceOrder = rack.sequenceOrder.toString(),
                                    rackType = rack.rackType?.ifBlank { "shelf" },
                                    totalRacks = "-",
                                    totalBins = rack.binsCount.toString(),
                                    capacityMetrics = listOf(
                                        "Max Capacity" to "${rack.maxQuantityCapacity} pcs",
                                        "Max Weight" to "${rack.maxWeightCapacityKg.toInt()} kg"
                                    ),
                                    onEditClick = { onEditRack(rack) },
                                    onDeleteClick = {
                                        selectedRackToDelete = rack
                                    }
                                )
                            }

                            if (isPaginating) {
                                item {
                                    ThreeDotLoading()
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedRackToDelete?.let { rack ->
            DeleteModel(
                title = "Delete Rack",
                message = "Are you sure you want to delete '${rack.name}'? This action cannot be undone.",
                onDismiss = {
                    selectedRackToDelete = null
                },
                onDelete = {
                    val rackIdToDelete = rack.id
                    selectedRackToDelete = null
                    viewModel.deleteRack(rackId = rackIdToDelete)
                }
            )
        }

        DynamicIslandSuccess(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = successMsg,
            onDismiss = { viewModel.clearSuccessMessage() }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMsg?.takeIf { racks.isNotEmpty() },
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

// -------------------------------------------------------------
// Screen 7: Add/Edit Rack Form
// -------------------------------------------------------------
@Composable
fun AddRackScreen(
    rackItem: RackItem? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "",
    floorId: String = "",
    sectionId: String = "",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = rackItem != null
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearDynamicErrorMessage()
        }
    }

    var rackName by remember(rackItem) { mutableStateOf(rackItem?.name ?: "") }
    var rackCode by remember(rackItem) { mutableStateOf(rackItem?.code ?: "") }
    var sequenceOrder by remember(rackItem) { mutableStateOf(rackItem?.sequenceOrder?.toString() ?: "") }
    var description by remember(rackItem) { mutableStateOf(rackItem?.description ?: "") }
    var rackType by remember(rackItem) { mutableStateOf(rackItem?.rackType ?: "") }
    var maxQty by remember(rackItem) {
        mutableStateOf(rackItem?.maxQuantityCapacity?.let { if (it > 0) it.toString() else "" } ?: "")
    }
    var maxWeight by remember(rackItem) {
        mutableStateOf(rackItem?.maxWeightCapacityKg?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
    }
    var isActive by remember(rackItem) { mutableStateOf(rackItem?.status?.equals("active", ignoreCase = true) ?: true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = if (isEditMode) "Edit Rack" else "Add Rack",
                onClose = onClose
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                contentPadding = PaddingValues(bottom = tokens.buttonHeight * 2)
            ) {
                item {
                    SectionHeader("Basic Information")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Rack Name", isRequired = true)
                    FormTextField(
                        value = rackName,
                        onValueChange = { rackName = it },
                        placeholder = "e.g., Heavy Duty Rack A, Shelf 01",
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Rack Code")
                    FormTextField(
                        value = rackCode,
                        onValueChange = { rackCode = it },
                        placeholder = "e.g., RA-01, RCK-001",
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Sequence Orders")
                    FormTextField(
                        value = sequenceOrder,
                        onValueChange = { sequenceOrder = it },
                        placeholder = "e.g., 1",
                        keyboardType = KeyboardType.Number,
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Description (Optional)")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Enter rack specifications, tier setup, or remarks...",
                        enabled = !isEditMode
                    )
                }

                item {
                    SectionHeader("Rack Specification")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Rack Type")
                    FormTextField(
                        value = rackType,
                        onValueChange = { rackType = it },
                        placeholder = "e.g., Shelf, Bin Rack, Cantilever, Pallet Rack",
                        enabled = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Max Quantity Capacity")
                    FormTextField(
                        value = maxQty,
                        onValueChange = { maxQty = it },
                        placeholder = "e.g., 1000",
                        keyboardType = KeyboardType.Number,
                        enabled = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Max Weight Capacity (kg)")
                    FormTextField(
                        value = maxWeight,
                        onValueChange = { maxWeight = it },
                        placeholder = "e.g., 500",
                        keyboardType = KeyboardType.Number,
                        enabled = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontSize = tokens.bodySmall, color = TextSecondary)
                        MiniSwitch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            onBack = onClose,
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Update(
                label = if (isEditMode) "Update Rack" else "Save Rack",
                onClick = {
                    if (rackName.isBlank() || rackCode.isBlank()) {
                        viewModel.showError("Please fill required fields")
                        return@Update
                    }

                    val targetWarehouseId = rackItem?.warehouseIdValue?.takeIf { it.isNotBlank() } ?: warehouseId
                    val targetFloorId = rackItem?.floorIdValue?.takeIf { it.isNotBlank() } ?: floorId
                    val targetSectionId = rackItem?.sectionIdValue?.takeIf { it.isNotBlank() } ?: sectionId

                    if (isEditMode) {
                        viewModel.updateRack(
                            rackId = rackItem.id,
                            warehouseId = targetWarehouseId,
                            floorId = targetFloorId,
                            sectionId = targetSectionId,
                            name = rackName,
                            code = rackCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            rackType = rackType.ifBlank { "shelf" },
                            maxQuantityCapacity = maxQty.toIntOrNull() ?: 0,
                            maxWeightCapacityKg = maxWeight.toDoubleOrNull() ?: 0.0,
                            status = if (isActive) "active" else "inactive",
                            onSuccess = onSave,
                            onError = {}
                        )
                    } else {
                        viewModel.createRack(
                            warehouseId = targetWarehouseId,
                            floorId = targetFloorId,
                            sectionId = targetSectionId,
                            name = rackName,
                            code = rackCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            rackType = rackType.ifBlank { "shelf" },
                            maxQuantityCapacity = maxQty.toIntOrNull() ?: 0,
                            maxWeightCapacityKg = maxWeight.toDoubleOrNull() ?: 0.0,
                            status = if (isActive) "active" else "inactive",
                            onSuccess = { onSave() },
                            onError = {}
                        )
                    }
                }
            ),
            backWidthFraction = 0.25f,
            trailingWidthFraction = 0.35f
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMsg,
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

// -------------------------------------------------------------
// Screen 8: Bin Overview (Infinite Scrolling)
// -------------------------------------------------------------
@Composable
fun BinOverviewScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "",
    rackId: String = "",
    onClose: () -> Unit = {},
    onAddBin: () -> Unit = {},
    onEditBin: (BinItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val bins by viewModel.bins.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()
    val isPaginating by viewModel.isPaginatingBins.collectAsStateWithLifecycle()
    val successMsg by viewModel.dynamicSuccessMessage.collectAsStateWithLifecycle()
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()

    var selectedBinToDelete by remember { mutableStateOf<BinItem?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(warehouseId, rackId) {
        viewModel.fetchBins(
            isRefresh = true
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= bins.size - 2) {
                    viewModel.fetchBins(
                        warehouseId = warehouseId,
                        rackId = rackId,
                        isRefresh = false
                    )
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            fab = FabConfig(
                label = "Add Bin",
                icon = Icons.Default.Add,
                onClick = onAddBin
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(title = "Bin Overview", onClose = onClose)

                when {
                    isLoading && bins.isEmpty() -> {
                        ListSkeleton()
                    }
                    errorMsg != null && bins.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load bins",
                            message = errorMsg ?: "Something went wrong. Please check your connection.",
                            onRetry = {
                                viewModel.fetchBins(
                                    warehouseId = warehouseId,
                                    rackId = rackId,
                                    isRefresh = true
                                )
                            }
                        )
                    }
                    bins.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No bins found",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = tokens.screenPadding),
                            contentPadding = PaddingValues(
                                top = tokens.extraPadding,
                                bottom = tokens.buttonHeight * 2
                            ),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            items(bins, key = { it.id }) { bin ->
                                WarehouseDetailCard(
                                    icon = R.drawable.ic_hanker,
                                    title = bin.name,
                                    status = bin.status.uppercase(),
                                    locationLabel = "Rack",
                                    locationName = bin.rackDisplayName.ifBlank { bin.rackCode },
                                    sequenceOrder = bin.sequenceOrder.toString(),
                                    binType = bin.binType?.ifBlank { "regular" } ?: "regular",
                                    totalRacks = "-",
                                    totalBins = "-",
                                    capacityMetrics = listOf(
                                        "Max Capacity" to "${bin.maxQuantity} ${bin.defaultUOM?.ifBlank { "pcs" } ?: "pcs"}",
                                        "Max Weight" to "${bin.maxWeightKg.toInt()} kg"
                                    ),
                                    onEditClick = { onEditBin(bin) },
                                    onDeleteClick = {
                                        selectedBinToDelete = bin
                                    }
                                )
                            }

                            if (isPaginating) {
                                item {
                                    ThreeDotLoading()
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedBinToDelete?.let { bin ->
            DeleteModel(
                title = "Delete Bin",
                message = "Are you sure you want to delete '${bin.name}'? This action cannot be undone.",
                onDismiss = {
                    selectedBinToDelete = null
                },
                onDelete = {
                    val binIdToDelete = bin.id
                    selectedBinToDelete = null
                    viewModel.deleteBin(
                        binId = binIdToDelete,
                        warehouseId = warehouseId,
                        rackId = rackId
                    )
                }
            )
        }

        DynamicIslandSuccess(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = successMsg,
            onDismiss = { viewModel.clearSuccessMessage() }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMsg?.takeIf { bins.isNotEmpty() },
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

// -------------------------------------------------------------
// Screen 9: Add/Edit Bin Form
// -------------------------------------------------------------
@Composable
fun AddBinScreen(
    binItem: BinItem? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "6a8d5643f685905f29057664",
    floorId: String = "6a8d5762f685905f290576b3",
    sectionId: String = "6a8d57f3f685905f290576bf",
    rackId: String = "6a8d583bf685905f290576cd",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = binItem != null
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearDynamicErrorMessage()
        }
    }

    var binName by remember(binItem) { mutableStateOf(binItem?.name ?: "") }
    var binCode by remember(binItem) { mutableStateOf(binItem?.code ?: "") }
    var sequenceOrder by remember(binItem) { mutableStateOf(binItem?.sequenceOrder?.toString() ?: "") }
    var description by remember(binItem) { mutableStateOf(binItem?.description ?: "") }
    var binType by remember(binItem) { mutableStateOf(binItem?.binType ?: "") }
    var maxQty by remember(binItem) {
        mutableStateOf(binItem?.maxQuantity?.let { if (it > 0) it.toString() else "" } ?: "")
    }
    var maxWeight by remember(binItem) {
        mutableStateOf(binItem?.maxWeightKg?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
    }
    var defaultUom by remember(binItem) { mutableStateOf(binItem?.defaultUOM ?: "") }
    var isActive by remember(binItem) { mutableStateOf(binItem?.status?.equals("active", ignoreCase = true) ?: true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = if (isEditMode) "Edit Bin" else "Add Bin",
                onClose = onClose
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                contentPadding = PaddingValues(bottom = tokens.buttonHeight * 2)
            ) {
                item {
                    SectionHeader("Basic Information")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Bin Name", isRequired = true)
                    FormTextField(
                        value = binName,
                        onValueChange = { binName = it },
                        placeholder = "e.g., Storage Bin A-01, Small Parts Bin",
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Bin Code")
                    FormTextField(
                        value = binCode,
                        onValueChange = { binCode = it },
                        placeholder = "e.g., B01, BIN-001",
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Sequence Orders")
                    FormTextField(
                        value = sequenceOrder,
                        onValueChange = { sequenceOrder = it },
                        placeholder = "e.g., 1",
                        keyboardType = KeyboardType.Number,
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Description (Optional)")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Enter bin position, compartment notes, or remarks...",
                        enabled = !isEditMode
                    )
                }

                item {
                    SectionHeader("Bin Type")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Bin Type")
                    FormTextField(
                        value = binType,
                        onValueChange = { binType = it },
                        placeholder = "e.g., Regular, Plastic Tote, Open Front, Box",
                        enabled = true
                    )
                }

                item {
                    SectionHeader("Capacity Settings")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Max Quantity Capacity")
                    FormTextField(
                        value = maxQty,
                        onValueChange = { maxQty = it },
                        placeholder = "e.g., 300",
                        keyboardType = KeyboardType.Number,
                        enabled = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Max Weight Capacity (kg)")
                    FormTextField(
                        value = maxWeight,
                        onValueChange = { maxWeight = it },
                        placeholder = "e.g., 200",
                        keyboardType = KeyboardType.Number,
                        enabled = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Default UOM")
                    FormTextField(
                        value = defaultUom,
                        onValueChange = { defaultUom = it },
                        placeholder = "e.g., pcs, rolls, meters, boxes",
                        enabled = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontSize = tokens.bodySmall, color = TextSecondary)
                        MiniSwitch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            onBack = onClose,
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Update(
                label = if (isEditMode) "Update Bin" else "Save Bin",
                onClick = {
                    if (binName.isBlank() || binCode.isBlank()) {
                        viewModel.showError("Please fill required fields")
                        return@Update
                    }

                    val targetWarehouseId = binItem?.warehouseIdValue?.takeIf { it.isNotBlank() } ?: warehouseId
                    val targetFloorId = binItem?.floorId?.takeIf { it.isNotBlank() } ?: floorId
                    val targetSectionId = binItem?.sectionId?.takeIf { it.isNotBlank() } ?: sectionId
                    val targetRackId = binItem?.rackIdValue?.takeIf { it.isNotBlank() } ?: rackId

                    if (isEditMode) {
                        viewModel.updateBin(
                            binId = binItem.id,
                            warehouseId = targetWarehouseId,
                            floorId = targetFloorId,
                            sectionId = targetSectionId,
                            rackId = targetRackId,
                            name = binName,
                            code = binCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            binType = binType.ifBlank { "regular" },
                            maxQuantity = maxQty.toIntOrNull() ?: 0,
                            maxWeightKg = maxWeight.toDoubleOrNull() ?: 0.0,
                            defaultUOM = defaultUom.ifBlank { "pcs" },
                            status = if (isActive) "active" else "inactive",
                            onSuccess = onSave,
                            onError = {}
                        )
                    } else {
                        viewModel.createBin(
                            warehouseId = targetWarehouseId,
                            floorId = targetFloorId,
                            sectionId = targetSectionId,
                            rackId = targetRackId,
                            name = binName,
                            code = binCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            binType = binType.ifBlank { "regular" },
                            maxQuantity = maxQty.toIntOrNull() ?: 0,
                            maxWeightKg = maxWeight.toDoubleOrNull() ?: 0.0,
                            defaultUOM = defaultUom.ifBlank { "pcs" },
                            status = if (isActive) "active" else "inactive",
                            onSuccess = { onSave() },
                            onError = {}
                        )
                    }
                }
            ),
            backWidthFraction = 0.25f,
            trailingWidthFraction = 0.35f
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMsg,
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}