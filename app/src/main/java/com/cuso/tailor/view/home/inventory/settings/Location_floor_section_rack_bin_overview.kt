@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.tailor.view.home.inventory.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.BinItem
import com.cuso.tailor.model.settings.FloorItemSettings
import com.cuso.tailor.model.settings.RackItem
import com.cuso.tailor.model.settings.SectionItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel

// -------------------------------------------------------------
// Screen 1: Location Structure Overview
// -------------------------------------------------------------
@Composable
fun LocationStructureScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onAddFloor: () -> Unit = {},
    onFloorClick: (warehouseId: String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val warehouseUiState by inventoryViewModel.warehouseUiState.collectAsStateWithLifecycle()
    val warehouses = warehouseUiState.warehouses
    val isLoading = warehouseUiState.isLoading
    val errorMsg = warehouseUiState.errorMessage

    LaunchedEffect(Unit) {
        inventoryViewModel.loadWarehouses()
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
                TitleBar(title = "Location Structure", onClose = onClose)

                when {
                    isLoading && warehouses.isEmpty() -> {
                        ListSkeleton()
                    }
                    errorMsg != null && warehouses.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load warehouses",
                            message = errorMsg,
                            onRetry = { inventoryViewModel.loadWarehouses() }
                        )
                    }
                    warehouses.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No warehouses found",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Spacer(Modifier.padding(top = 10.dp))
                            }
                            items(items = warehouses, key = { it.id }) { wh ->
                                val locality = wh.address.city.ifBlank { "Main Location" }
                                val isActive = wh.status.equals("active", ignoreCase = true)

                                DataCard(
                                    item = wh,
                                    image = DataCardImage(
                                        vector = ImageVector.vectorResource(id = R.drawable.ic_warehouse),
                                        tint = Color.Transparent,
                                        backgroundColor = primary_light,
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                        size = 40.dp
                                    ),
                                    title = wh.name,
                                    titleFontWeight = FontWeight.Medium,
                                    subtitle = "${wh.code} • $locality",
                                    topBadgeText = if (isActive) "Active" else "Inactive",
                                    topBadgeBgColor = if (isActive) greenBg else redBg,
                                    topBadgeTextColor = if (isActive) darkGreenBg else redText,
                                    topBadgeInline = true,
                                    onClick = { onFloorClick(wh.id) },
                                    footerFields = listOf(
                                        DataCardField(label = "Floors", text = wh.hierarchyCounts.floors.toString(), asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Sections", text = wh.hierarchyCounts.sections.toString(), asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Racks", text = wh.hierarchyCounts.racks.toString(), asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Bins", text = wh.hierarchyCounts.bins.toString(), asColumn = true, valueFontWeight = FontWeight.Medium)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
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
    onAddSection: () -> Unit = {},
    onEditFloor: (FloorItemSettings) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()
    val isPaginating by viewModel.isPaginatingFloors.collectAsStateWithLifecycle()
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
                label = "Add Section",
                icon = Icons.Default.Add,
                onClick = onAddSection
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
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            item {
                                Spacer(Modifier.padding(top = 10.dp))
                            }
                            items(items = floors, key = { it.id }) { floor ->
                                val isActive = floor.status.equals("active", ignoreCase = true)

                                DataCard(
                                    item = floor,
                                    image = DataCardImage(
                                        vector = ImageVector.vectorResource(id = R.drawable.ic_hanker),
                                        tint = Color.Transparent,
                                        backgroundColor = primary_light,
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                        size = 38.dp
                                    ),
                                    title = floor.name,
                                    titleFontWeight = FontWeight.Medium,
                                    subtitle = "Code: ${floor.code.ifBlank { "N/A" }} • Seq: ${floor.sequenceOrder}",
                                    topBadgeText = if (isActive) "Active" else "Inactive",
                                    topBadgeBgColor = if (isActive) greenBg else redBg,
                                    topBadgeTextColor = if (isActive) darkGreenBg else redText,
                                    topBadgeInline = true,
                                    actions = listOf(
                                        MenuAction(label = "Edit", icon = Icons.Default.Edit, onClick = { onEditFloor(floor) }),
                                        MenuAction(label = "Delete", icon = Icons.Default.Delete, textColor = redText, tint = redText, onClick = { selectedFloorToDelete = floor })
                                    ),
                                    footerFields = listOf(
                                        DataCardField(label = "Sections", text = floor.sectionsCount.toString(), asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Racks", text = floor.racksCount.toString(), asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Bins", text = floor.binsCount.toString(), asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Area", text = "${floor.floorAreaSqft.toInt()} sqft", asColumn = true, valueFontWeight = FontWeight.Medium)
                                    )
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
                onDismiss = { selectedFloorToDelete = null },
                onDelete = {
                    val floorIdToDelete = floor.id
                    selectedFloorToDelete = null
                    viewModel.deleteFloor(floorId = floorIdToDelete, warehouseId = warehouseId)
                }
            )
        }
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
    onAddRack: () -> Unit = {},
    onEditSection: (SectionItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()
    val isPaginating by viewModel.isPaginatingSections.collectAsStateWithLifecycle()
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
                label = "Add Rack",
                icon = Icons.Default.Add,
                onClick = onAddRack
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
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            item {
                                Spacer(Modifier.padding(top = 10.dp))
                            }
                            items(sections, key = { it.id }) { section ->
                                val isActive = section.status.equals("active", ignoreCase = true)

                                DataCard(
                                    item = section,
                                    image = DataCardImage(
                                        vector = ImageVector.vectorResource(id = R.drawable.ic_hanker),
                                        tint = Color.Transparent,
                                        backgroundColor = primary_light,
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                        size = 38.dp
                                    ),
                                    title = section.name,
                                    titleFontWeight = FontWeight.Medium,
                                    subtitle = "Code: ${section.code.ifBlank { "N/A" }} • Floor: ${section.floorName.ifBlank { "—" }}",
                                    topBadgeText = if (isActive) "Active" else "Inactive",
                                    topBadgeBgColor = if (isActive) greenBg else redBg,
                                    topBadgeTextColor = if (isActive) darkGreenBg else redText,
                                    topBadgeInline = true,
                                    actions = listOf(
                                        MenuAction(label = "Edit", icon = Icons.Default.Edit, onClick = { onEditSection(section) }),
                                        MenuAction(label = "Delete", icon = Icons.Default.Delete, textColor = redText, tint = redText, onClick = { selectedSectionToDelete = section })
                                    ),
                                    footerFields = listOf(
                                        DataCardField(label = "Racks", text = section.racksCount.toString(), asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Bins", text = section.binsCount.toString(), asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Storage", text = section.storageType?.ifBlank { "Shelving" } ?: "Shelving", asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Climate", text = section.climateControl?.ifBlank { "AC" } ?: "AC", asColumn = true, valueFontWeight = FontWeight.Medium)
                                    )
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
                onDismiss = { selectedSectionToDelete = null },
                onDelete = {
                    val sectionIdToDelete = section.id
                    selectedSectionToDelete = null
                    viewModel.deleteSection(sectionId = sectionIdToDelete, warehouseId = warehouseId, floorId = floorId)
                }
            )
        }
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
    onAddBin: () -> Unit = {},
    onEditRack: (RackItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val racks by viewModel.racks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()
    val isPaginating by viewModel.isPaginatingRacks.collectAsStateWithLifecycle()
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
                label = "Add Bin",
                icon = Icons.Default.Add,
                onClick = onAddBin
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
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            item {
                                Spacer(Modifier.padding(top = 10.dp))
                            }
                            items(racks, key = { it.id }) { rack ->
                                val isActive = rack.status.equals("active", ignoreCase = true)

                                DataCard(
                                    item = rack,
                                    image = DataCardImage(
                                        vector = ImageVector.vectorResource(id = R.drawable.ic_hanker),
                                        tint = Color.Transparent,
                                        backgroundColor = primary_light,
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                        size = 38.dp
                                    ),
                                    title = rack.name,
                                    titleFontWeight = FontWeight.Medium,
                                    subtitle = "Code: ${rack.code.ifBlank { "N/A" }} • Type: ${rack.rackType?.ifBlank { "Shelf" } ?: "Shelf"}",
                                    topBadgeText = if (isActive) "Active" else "Inactive",
                                    topBadgeBgColor = if (isActive) greenBg else redBg,
                                    topBadgeTextColor = if (isActive) darkGreenBg else redText,
                                    topBadgeInline = true,
                                    actions = listOf(
                                        MenuAction(label = "Edit", icon = Icons.Default.Edit, onClick = { onEditRack(rack) }),
                                        MenuAction(label = "Delete", icon = Icons.Default.Delete, textColor = redText, tint = redText, onClick = { selectedRackToDelete = rack })
                                    ),
                                    footerFields = listOf(
                                        DataCardField(label = "Bins", text = rack.binsCount.toString(), asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Max Qty", text = "${rack.maxQuantityCapacity} pcs", asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Max Weight", text = "${rack.maxWeightCapacityKg.toInt()} kg", asColumn = true, valueFontWeight = FontWeight.Medium),
                                        DataCardField(label = "Seq", text = rack.sequenceOrder.toString(), asColumn = true, valueFontWeight = FontWeight.Medium)
                                    )
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
                onDismiss = { selectedRackToDelete = null },
                onDelete = {
                    val rackIdToDelete = rack.id
                    selectedRackToDelete = null
                    viewModel.deleteRack(rackId = rackIdToDelete)
                }
            )
        }
    }
}

// -------------------------------------------------------------
// Screen 8: Bin Overview (Infinite Scrolling - NO FAB)
// -------------------------------------------------------------
@Composable
fun BinOverviewScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "",
    rackId: String = "",
    onClose: () -> Unit = {},
    onEditBin: (BinItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val bins by viewModel.bins.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()
    val isPaginating by viewModel.isPaginatingBins.collectAsStateWithLifecycle()
    val errorMsg by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()

    var selectedBinToDelete by remember { mutableStateOf<BinItem?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(warehouseId, rackId) {
        viewModel.fetchBins(isRefresh = true)
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
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
                else -> {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        item {
                            Spacer(Modifier.padding(top = 10.dp))
                        }
                        items(bins, key = { it.id }) { bin ->
                            val isActive = bin.status.equals("active", ignoreCase = true)

                            DataCard(
                                item = bin,
                                image = DataCardImage(
                                    vector = ImageVector.vectorResource(id = R.drawable.ic_hanker),
                                    tint = Color.Transparent,
                                    backgroundColor = primary_light,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                    size = 38.dp
                                ),
                                title = bin.name,
                                titleFontWeight = FontWeight.Medium,
                                subtitle = "Rack: ${bin.rackDisplayName.ifBlank { bin.rackCode }} • Type: ${bin.binType?.ifBlank { "Regular" } ?: "Regular"}",
                                topBadgeText = if (isActive) "Active" else "Inactive",
                                topBadgeBgColor = if (isActive) greenBg else redBg,
                                topBadgeTextColor = if (isActive) darkGreenBg else redText,
                                topBadgeInline = true,
                                actions = listOf(
                                    MenuAction(label = "Edit", icon = Icons.Default.Edit, onClick = { onEditBin(bin) }),
                                    MenuAction(label = "Delete", icon = Icons.Default.Delete, textColor = redText, tint = redText, onClick = { selectedBinToDelete = bin })
                                ),
                                footerFields = listOf(
                                    DataCardField(label = "Max Qty", text = "${bin.maxQuantity} ${bin.defaultUOM?.ifBlank { "pcs" } ?: "pcs"}", asColumn = true, valueFontWeight = FontWeight.Medium),
                                    DataCardField(label = "Max Weight", text = "${bin.maxWeightKg.toInt()} kg", asColumn = true, valueFontWeight = FontWeight.Medium),
                                    DataCardField(label = "Seq", text = bin.sequenceOrder.toString(), asColumn = true, valueFontWeight = FontWeight.Medium)
                                )
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

        selectedBinToDelete?.let { bin ->
            DeleteModel(
                title = "Delete Bin",
                message = "Are you sure you want to delete '${bin.name}'? This action cannot be undone.",
                onDismiss = { selectedBinToDelete = null },
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
    }
}

// -------------------------------------------------------------
// Screen 3: Add/Edit Floor Form
// -------------------------------------------------------------
@Composable
fun AddFloorScreen(
    floorItem: FloorItemSettings? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    warehouseId: String = "",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = floorItem != null

    val warehouses by inventoryViewModel.warehouseDropdown.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        inventoryViewModel.loadWarehouseDropdown()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearDynamicErrorMessage() }
    }

    var selectedWarehouseId by remember(floorItem) {
        mutableStateOf(extractJsonId(floorItem?.warehouseElement).ifBlank { warehouseId })
    }
    var selectedWarehouseName by remember(warehouses, selectedWarehouseId) {
        mutableStateOf(warehouses.find { it.value == selectedWarehouseId }?.label ?: "Select Warehouse")
    }
    var isWarehouseExpanded by remember { mutableStateOf(false) }

    var floorName by remember(floorItem) { mutableStateOf(floorItem?.name ?: "") }
    var floorCode by remember(floorItem) { mutableStateOf(floorItem?.code ?: "") }
    var sequenceOrder by remember(floorItem) { mutableStateOf(floorItem?.sequenceOrder?.toString() ?: "1") }
    var description by remember(floorItem) { mutableStateOf(floorItem?.description ?: "") }

    val tempZoneOptions = listOf("Normal", "Cold Storage", "Frozen", "Climate Controlled")
    var temperatureZone by remember(floorItem) {
        mutableStateOf(floorItem?.temperatureZone?.replaceFirstChar { it.uppercase() } ?: "Normal")
    }
    var isTempZoneExpanded by remember { mutableStateOf(false) }

    var floorArea by remember(floorItem) {
        mutableStateOf(floorItem?.floorAreaSqft?.let { if (it > 0) it.toInt().toString() else "5000" } ?: "5000")
    }
    var maxWeight by remember(floorItem) {
        mutableStateOf(floorItem?.maxWeightCapacityKg?.let { if (it > 0) it.toInt().toString() else "10000" } ?: "10000")
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

                    FormLabel("Warehouse", isRequired = true)
                    FormDropdown(
                        value = selectedWarehouseName,
                        expanded = isWarehouseExpanded,
                        onExpandChange = { isWarehouseExpanded = it },
                        options = warehouses.map { it.label },
                        onOptionSelected = { chosenLabel ->
                            selectedWarehouseName = chosenLabel
                            selectedWarehouseId = warehouses.find { it.label == chosenLabel }?.value.orEmpty()
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Floor Name", isRequired = true)
                    FormTextField(
                        value = floorName,
                        onValueChange = { floorName = it },
                        placeholder = "e.g. Ground"
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Floor Code")
                    FormTextField(
                        value = floorCode,
                        onValueChange = { floorCode = it },
                        placeholder = "e.g. GRD"
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Sequence Order")
                    FormTextField(
                        value = sequenceOrder,
                        onValueChange = { sequenceOrder = it.filter { ch -> ch.isDigit() } },
                        placeholder = "1",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Description (Optional)")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Add any notes about this floor..."
                    )
                }

                item {
                    SectionHeader("Structure Settings")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Temperature Zone")
                    FormDropdown(
                        value = temperatureZone,
                        expanded = isTempZoneExpanded,
                        onExpandChange = { isTempZoneExpanded = it },
                        options = tempZoneOptions,
                        onOptionSelected = { temperatureZone = it }
                    )
                }

                item {
                    SectionHeader("Capacity Info")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Floor Area (sqft)")
                            FormTextField(
                                value = floorArea,
                                onValueChange = { floorArea = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                placeholder = "5000",
                                keyboardType = KeyboardType.Number
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Max Weight Capacity (kg)")
                            FormTextField(
                                value = maxWeight,
                                onValueChange = { maxWeight = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                placeholder = "10000",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    StatusCheckboxRow(
                        isActive = isActive,
                        onStatusChange = { isActive = it }
                    )
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
                    val finalWarehouseId = selectedWarehouseId.ifBlank { warehouseId }
                    if (floorName.isBlank() || finalWarehouseId.isBlank()) {
                        viewModel.showError("Please fill required fields")
                        return@Update
                    }

                    if (floorItem != null) {
                        viewModel.updateFloor(
                            floorId = floorItem.id,
                            warehouseId = finalWarehouseId,
                            name = floorName,
                            code = floorCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            temperatureZone = temperatureZone.lowercase(),
                            floorAreaSqft = floorArea.toDoubleOrNull() ?: 0.0,
                            maxWeightCapacityKg = maxWeight.toDoubleOrNull() ?: 0.0,
                            status = if (isActive) "active" else "inactive",
                            onSuccess = onSave,
                            onError = {}
                        )
                    } else {
                        viewModel.createFloor(
                            warehouseId = finalWarehouseId,
                            name = floorName,
                            code = floorCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            temperatureZone = temperatureZone.lowercase(),
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
    }
}

// -------------------------------------------------------------
// Screen 5: Add/Edit Section Form
// -------------------------------------------------------------
@Composable
fun AddSectionScreen(
    sectionItem: SectionItem? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    warehouseId: String = "",
    floorId: String = "",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = sectionItem != null
    val productCategories by viewModel.productCategories.collectAsStateWithLifecycle()

    val warehouses by inventoryViewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val floors by inventoryViewModel.floorDropdown.collectAsStateWithLifecycle()

    var selectedWarehouseId by remember(sectionItem) {
        mutableStateOf(extractJsonId(sectionItem?.warehouseElement).ifBlank { warehouseId })
    }
    var selectedWarehouseName by remember(warehouses, selectedWarehouseId) {
        mutableStateOf(warehouses.find { it.value == selectedWarehouseId }?.label ?: "Select Warehouse")
    }
    var isWarehouseExpanded by remember { mutableStateOf(false) }

    var selectedFloorId by remember(sectionItem) {
        mutableStateOf(extractJsonId(sectionItem?.floorElement).ifBlank { floorId })
    }
    var selectedFloorName by remember(floors, selectedFloorId) {
        mutableStateOf(floors.find { it.id == selectedFloorId }?.name ?: if (selectedWarehouseId.isBlank()) "Select warehouse first" else "Select Floor")
    }
    var isFloorExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        inventoryViewModel.loadWarehouseDropdown()
        viewModel.fetchProductCategories()
        if (selectedWarehouseId.isNotBlank()) {
            inventoryViewModel.loadFloorDropdown(selectedWarehouseId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearDynamicErrorMessage() }
    }

    var sectionName by remember(sectionItem) { mutableStateOf(sectionItem?.name ?: "") }
    var sectionCode by remember(sectionItem) { mutableStateOf(sectionItem?.code ?: "") }
    var sequenceOrder by remember(sectionItem) { mutableStateOf(sectionItem?.sequenceOrder?.toString() ?: "1") }
    var description by remember(sectionItem) { mutableStateOf(sectionItem?.description ?: "") }

    var selectedCategoryId by remember(sectionItem) {
        mutableStateOf(sectionItem?.allowedProductCategories?.firstOrNull()?.id ?: "")
    }
    var selectedCategoryName by remember(sectionItem) {
        mutableStateOf(sectionItem?.allowedProductCategories?.firstOrNull()?.name ?: "Select Product Categories")
    }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    val storageOptions = listOf("Shelving", "Flooring", "Bulk", "Hanging", "Cold Room")
    var storageType by remember(sectionItem) {
        mutableStateOf(sectionItem?.storageType?.replaceFirstChar { it.uppercase() } ?: "Shelving")
    }
    var isStorageExpanded by remember { mutableStateOf(false) }

    val climateOptions = listOf("None", "AC Standard", "Refrigerated", "Humidity Controlled")
    var climateControl by remember(sectionItem) {
        mutableStateOf(sectionItem?.climateControl?.replaceFirstChar { it.uppercase() } ?: "None")
    }
    var isClimateExpanded by remember { mutableStateOf(false) }

    var isActive by remember(sectionItem) { mutableStateOf(sectionItem?.status?.equals("active", ignoreCase = true) ?: true) }

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

                    FormLabel("Warehouse", isRequired = true)
                    FormDropdown(
                        value = selectedWarehouseName,
                        expanded = isWarehouseExpanded,
                        onExpandChange = { isWarehouseExpanded = it },
                        options = warehouses.map { it.label },
                        onOptionSelected = { chosenLabel ->
                            selectedWarehouseName = chosenLabel
                            val chosenId = warehouses.find { it.label == chosenLabel }?.value.orEmpty()
                            selectedWarehouseId = chosenId
                            selectedFloorId = ""
                            selectedFloorName = "Select Floor"
                            inventoryViewModel.loadFloorDropdown(chosenId)
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Floor", isRequired = true)
                    FormDropdown(
                        value = selectedFloorName,
                        expanded = isFloorExpanded,
                        onExpandChange = { if (selectedWarehouseId.isNotBlank()) isFloorExpanded = it },
                        options = floors.map { it.name },
                        onOptionSelected = { chosenName ->
                            selectedFloorName = chosenName
                            val chosenFlId = floors.find { it.name == chosenName }?.id.orEmpty()
                            selectedFloorId = chosenFlId
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Section Name", isRequired = true)
                    FormTextField(
                        value = sectionName,
                        onValueChange = { sectionName = it },
                        placeholder = "e.g. Menswear"
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Section Code")
                    FormTextField(
                        value = sectionCode,
                        onValueChange = { sectionCode = it },
                        placeholder = "e.g. MNW"
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Sequence Order")
                    FormTextField(
                        value = sequenceOrder,
                        onValueChange = { sequenceOrder = it.filter { ch -> ch.isDigit() } },
                        placeholder = "1",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Description (Optional)")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Add any notes about this section..."
                    )
                }

                item {
                    SectionHeader("Category Mapping")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Allowed Product Category")
                    FormDropdown(
                        value = selectedCategoryName,
                        expanded = isCategoryExpanded,
                        onExpandChange = { isCategoryExpanded = it },
                        options = productCategories.map { it.name },
                        onOptionSelected = { chosenName ->
                            selectedCategoryName = chosenName
                            selectedCategoryId = productCategories.find { it.name == chosenName }?.id.orEmpty()
                        }
                    )
                }

                item {

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Storage Type")
                            FormDropdown(
                                value = storageType,
                                expanded = isStorageExpanded,
                                onExpandChange = { isStorageExpanded = it },
                                options = storageOptions,
                                onOptionSelected = { storageType = it }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Climate Control")
                            FormDropdown(
                                value = climateControl,
                                expanded = isClimateExpanded,
                                onExpandChange = { isClimateExpanded = it },
                                options = climateOptions,
                                onOptionSelected = { climateControl = it }
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    StatusCheckboxRow(
                        isActive = isActive,
                        onStatusChange = { isActive = it }
                    )
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
                    val finalWhId = selectedWarehouseId.ifBlank { warehouseId }
                    val finalFlId = selectedFloorId.ifBlank { floorId }
                    if (sectionName.isBlank() || finalWhId.isBlank() || finalFlId.isBlank()) {
                        viewModel.showError("Please fill required fields")
                        return@Update
                    }

                    val categoriesPayload = if (selectedCategoryId.isNotBlank()) listOf(selectedCategoryId) else emptyList()

                    if (sectionItem != null) {
                        viewModel.updateSection(
                            sectionId = sectionItem.id,
                            warehouseId = finalWhId,
                            floorId = finalFlId,
                            name = sectionName,
                            code = sectionCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            allowedProductCategories = categoriesPayload,
                            storageType = storageType.lowercase(),
                            climateControl = if (climateControl == "None") "none" else climateControl.lowercase(),
                            status = if (isActive) "active" else "inactive",
                            onSuccess = onSave,
                            onError = {}
                        )
                    } else {
                        viewModel.createSection(
                            warehouseId = finalWhId,
                            floorId = finalFlId,
                            name = sectionName,
                            code = sectionCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            allowedProductCategories = categoriesPayload,
                            storageType = storageType.lowercase(),
                            climateControl = if (climateControl == "None") "none" else climateControl.lowercase(),
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
    }
}

// -------------------------------------------------------------
// Screen 7: Add/Edit Rack Form
// -------------------------------------------------------------
@Composable
fun AddRackScreen(
    rackItem: RackItem? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    warehouseId: String = "",
    floorId: String = "",
    sectionId: String = "",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = rackItem != null

    val warehouses by inventoryViewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val floors by inventoryViewModel.floorDropdown.collectAsStateWithLifecycle()
    val sections by inventoryViewModel.sectionDropdown.collectAsStateWithLifecycle()

    var selectedWarehouseId by remember(rackItem) {
        mutableStateOf(extractJsonId(rackItem?.warehouseElement).ifBlank { warehouseId })
    }
    var selectedWarehouseName by remember(warehouses, selectedWarehouseId) {
        mutableStateOf(warehouses.find { it.value == selectedWarehouseId }?.label ?: "Select Warehouse")
    }
    var isWarehouseExpanded by remember { mutableStateOf(false) }

    var selectedFloorId by remember(rackItem) {
        mutableStateOf(extractJsonId(rackItem?.floorElement).ifBlank { floorId })
    }
    var selectedFloorName by remember(floors, selectedFloorId) {
        mutableStateOf(floors.find { it.id == selectedFloorId }?.name ?: if (selectedWarehouseId.isBlank()) "Select warehouse first" else "Select Floor")
    }
    var isFloorExpanded by remember { mutableStateOf(false) }

    var selectedSectionId by remember(rackItem) {
        mutableStateOf(extractJsonId(rackItem?.sectionElement).ifBlank { sectionId })
    }
    var selectedSectionName by remember(sections, selectedSectionId) {
        mutableStateOf(sections.find { it.id == selectedSectionId }?.name ?: if (selectedFloorId.isBlank()) "Select floor first" else "Select Section")
    }
    var isSectionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        inventoryViewModel.loadWarehouseDropdown()
        if (selectedWarehouseId.isNotBlank()) inventoryViewModel.loadFloorDropdown(selectedWarehouseId)
        if (selectedFloorId.isNotBlank()) inventoryViewModel.loadSectionDropdown(selectedFloorId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearDynamicErrorMessage() }
    }

    var rackName by remember(rackItem) { mutableStateOf(rackItem?.name ?: "") }
    var rackCode by remember(rackItem) { mutableStateOf(rackItem?.code ?: "") }
    var sequenceOrder by remember(rackItem) { mutableStateOf(rackItem?.sequenceOrder?.toString() ?: "1") }
    var description by remember(rackItem) { mutableStateOf(rackItem?.description ?: "") }

    val rackTypeOptions = listOf("Shelf", "Pallet", "Hanging", "Bin Rack", "Cantilever")
    var rackType by remember(rackItem) {
        mutableStateOf(rackItem?.rackType?.replaceFirstChar { it.uppercase() } ?: "Shelf")
    }
    var isRackTypeExpanded by remember { mutableStateOf(false) }

    var maxQty by remember(rackItem) {
        mutableStateOf(rackItem?.maxQuantityCapacity?.let { if (it > 0) it.toString() else "500" } ?: "500")
    }
    var maxWeight by remember(rackItem) {
        mutableStateOf(rackItem?.maxWeightCapacityKg?.let { if (it > 0) it.toInt().toString() else "5000" } ?: "5000")
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

                    FormLabel("Warehouse", isRequired = true)
                    FormDropdown(
                        value = selectedWarehouseName,
                        expanded = isWarehouseExpanded,
                        onExpandChange = { isWarehouseExpanded = it },
                        options = warehouses.map { it.label },
                        onOptionSelected = { chosenLabel ->
                            selectedWarehouseName = chosenLabel
                            val chosenId = warehouses.find { it.label == chosenLabel }?.value.orEmpty()
                            selectedWarehouseId = chosenId
                            selectedFloorId = ""
                            selectedFloorName = "Select Floor"
                            selectedSectionId = ""
                            selectedSectionName = "Select floor first"
                            inventoryViewModel.loadFloorDropdown(chosenId)
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Floor", isRequired = true)
                    FormDropdown(
                        value = selectedFloorName,
                        expanded = isFloorExpanded,
                        onExpandChange = { if (selectedWarehouseId.isNotBlank()) isFloorExpanded = it },
                        options = floors.map { it.name },
                        onOptionSelected = { chosenName ->
                            selectedFloorName = chosenName
                            val chosenFlId = floors.find { it.name == chosenName }?.id.orEmpty()
                            selectedFloorId = chosenFlId
                            selectedSectionId = ""
                            selectedSectionName = "Select Section"
                            inventoryViewModel.loadSectionDropdown(chosenFlId)
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Section", isRequired = true)
                    FormDropdown(
                        value = selectedSectionName,
                        expanded = isSectionExpanded,
                        onExpandChange = { if (selectedFloorId.isNotBlank()) isSectionExpanded = it },
                        options = sections.map { it.name },
                        onOptionSelected = { chosenName ->
                            selectedSectionName = chosenName
                            val chosenSecId = sections.find { it.name == chosenName }?.id.orEmpty()
                            selectedSectionId = chosenSecId
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Rack Name", isRequired = true)
                    FormTextField(
                        value = rackName,
                        onValueChange = { rackName = it },
                        placeholder = "e.g. Rack R - 01"
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Rack Code")
                    FormTextField(
                        value = rackCode,
                        onValueChange = { rackCode = it },
                        placeholder = "e.g. R01"
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Sequence Order")
                    FormTextField(
                        value = sequenceOrder,
                        onValueChange = { sequenceOrder = it.filter { ch -> ch.isDigit() } },
                        placeholder = "1",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Description (Optional)")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Add any notes about this rack..."
                    )
                }

                item {
                    SectionHeader("Rack Specification")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Rack Type")
                    FormDropdown(
                        value = rackType,
                        expanded = isRackTypeExpanded,
                        onExpandChange = { isRackTypeExpanded = it },
                        options = rackTypeOptions,
                        onOptionSelected = { rackType = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Max Quantity Capacity")
                            FormTextField(
                                value = maxQty,
                                onValueChange = { maxQty = it.filter { ch -> ch.isDigit() } },
                                placeholder = "500",
                                keyboardType = KeyboardType.Number
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Max Weight Capacity (kg)")
                            FormTextField(
                                value = maxWeight,
                                onValueChange = { maxWeight = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                placeholder = "5000",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    StatusCheckboxRow(
                        isActive = isActive,
                        onStatusChange = { isActive = it }
                    )
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
                    val finalWhId = selectedWarehouseId.ifBlank { warehouseId }
                    val finalFlId = selectedFloorId.ifBlank { floorId }
                    val finalSecId = selectedSectionId.ifBlank { sectionId }

                    if (rackName.isBlank() || finalWhId.isBlank() || finalFlId.isBlank() || finalSecId.isBlank()) {
                        viewModel.showError("Please fill required fields")
                        return@Update
                    }

                    if (rackItem != null) {
                        viewModel.updateRack(
                            rackId = rackItem.id,
                            warehouseId = finalWhId,
                            floorId = finalFlId,
                            sectionId = finalSecId,
                            name = rackName,
                            code = rackCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            rackType = rackType.lowercase(),
                            maxQuantityCapacity = maxQty.toIntOrNull() ?: 0,
                            maxWeightCapacityKg = maxWeight.toDoubleOrNull() ?: 0.0,
                            status = if (isActive) "active" else "inactive",
                            onSuccess = onSave,
                            onError = {}
                        )
                    } else {
                        viewModel.createRack(
                            warehouseId = finalWhId,
                            floorId = finalFlId,
                            sectionId = finalSecId,
                            name = rackName,
                            code = rackCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            description = description,
                            rackType = rackType.lowercase(),
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
    }
}

// -------------------------------------------------------------
// Screen 9: Add/Edit Bin Form
// -------------------------------------------------------------
@Composable
fun AddBinScreen(
    binItem: BinItem? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    warehouseId: String = "",
    floorId: String = "",
    sectionId: String = "",
    rackId: String = "",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isEditMode = binItem != null

    val warehouses by inventoryViewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val floors by inventoryViewModel.floorDropdown.collectAsStateWithLifecycle()
    val sections by inventoryViewModel.sectionDropdown.collectAsStateWithLifecycle()
    val racks by inventoryViewModel.rackDropdown.collectAsStateWithLifecycle()

    var selectedWarehouseId by remember(binItem) {
        mutableStateOf(extractJsonId(binItem?.warehouseElement).ifBlank { warehouseId })
    }
    var selectedWarehouseName by remember(warehouses, selectedWarehouseId) {
        mutableStateOf(warehouses.find { it.value == selectedWarehouseId }?.label ?: "Select Warehouse")
    }
    var isWarehouseExpanded by remember { mutableStateOf(false) }

    var selectedFloorId by remember(binItem) {
        mutableStateOf(binItem?.floorId?.ifBlank { "" } ?: floorId)
    }
    var selectedFloorName by remember(floors, selectedFloorId) {
        mutableStateOf(floors.find { it.id == selectedFloorId }?.name ?: if (selectedWarehouseId.isBlank()) "Select warehouse first" else "Select Floor")
    }
    var isFloorExpanded by remember { mutableStateOf(false) }

    var selectedSectionId by remember(binItem) {
        mutableStateOf(binItem?.sectionId?.ifBlank { "" } ?: sectionId)
    }
    var selectedSectionName by remember(sections, selectedSectionId) {
        mutableStateOf(sections.find { it.id == selectedSectionId }?.name ?: if (selectedFloorId.isBlank()) "Select floor first" else "Select Section")
    }
    var isSectionExpanded by remember { mutableStateOf(false) }

    var selectedRackId by remember(binItem) {
        mutableStateOf(extractJsonId(binItem?.rackElement).ifBlank { rackId })
    }
    var selectedRackName by remember(racks, selectedRackId) {
        mutableStateOf(racks.find { it.id == selectedRackId }?.name ?: if (selectedSectionId.isBlank()) "Select section first" else "Select Rack")
    }
    var isRackExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        inventoryViewModel.loadWarehouseDropdown()
        if (selectedWarehouseId.isNotBlank()) inventoryViewModel.loadFloorDropdown(selectedWarehouseId)
        if (selectedFloorId.isNotBlank()) inventoryViewModel.loadSectionDropdown(selectedFloorId)
        if (selectedSectionId.isNotBlank()) inventoryViewModel.loadRackDropdown(selectedSectionId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearDynamicErrorMessage() }
    }

    var binName by remember(binItem) { mutableStateOf(binItem?.name ?: "") }
    var binCode by remember(binItem) { mutableStateOf(binItem?.code ?: "") }
    var sequenceOrder by remember(binItem) { mutableStateOf(binItem?.sequenceOrder?.toString() ?: "1") }
    var description by remember(binItem) { mutableStateOf(binItem?.description ?: "") }

    val binTypeOptions = listOf("Regular", "Bulk", "Pick Face", "Reserve", "Returns", "Damaged")
    var binType by remember(binItem) {
        mutableStateOf(binItem?.binType?.replaceFirstChar { it.uppercase() } ?: "Regular")
    }
    var isBinTypeExpanded by remember { mutableStateOf(false) }

    var maxQty by remember(binItem) {
        mutableStateOf(binItem?.maxQuantity?.let { if (it > 0) it.toString() else "500" } ?: "500")
    }
    var maxWeight by remember(binItem) {
        mutableStateOf(binItem?.maxWeightKg?.let { if (it > 0) it.toInt().toString() else "5000" } ?: "5000")
    }
    var defaultUom by remember(binItem) { mutableStateOf(binItem?.defaultUOM ?: "Kg,Piece") }
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

                    FormLabel("Warehouse", isRequired = true)
                    FormDropdown(
                        value = selectedWarehouseName,
                        expanded = isWarehouseExpanded,
                        onExpandChange = { isWarehouseExpanded = it },
                        options = warehouses.map { it.label },
                        onOptionSelected = { chosenLabel ->
                            selectedWarehouseName = chosenLabel
                            val chosenId = warehouses.find { it.label == chosenLabel }?.value.orEmpty()
                            selectedWarehouseId = chosenId
                            selectedFloorId = ""
                            selectedFloorName = "Select Floor"
                            selectedSectionId = ""
                            selectedSectionName = "Select floor first"
                            selectedRackId = ""
                            selectedRackName = "Select section first"
                            inventoryViewModel.loadFloorDropdown(chosenId)
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Floor", isRequired = true)
                    FormDropdown(
                        value = selectedFloorName,
                        expanded = isFloorExpanded,
                        onExpandChange = { if (selectedWarehouseId.isNotBlank()) isFloorExpanded = it },
                        options = floors.map { it.name },
                        onOptionSelected = { chosenName ->
                            selectedFloorName = chosenName
                            val chosenFlId = floors.find { it.name == chosenName }?.id.orEmpty()
                            selectedFloorId = chosenFlId
                            selectedSectionId = ""
                            selectedSectionName = "Select Section"
                            selectedRackId = ""
                            selectedRackName = "Select section first"
                            inventoryViewModel.loadSectionDropdown(chosenFlId)
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Section", isRequired = true)
                    FormDropdown(
                        value = selectedSectionName,
                        expanded = isSectionExpanded,
                        onExpandChange = { if (selectedFloorId.isNotBlank()) isSectionExpanded = it },
                        options = sections.map { it.name },
                        onOptionSelected = { chosenName ->
                            selectedSectionName = chosenName
                            val chosenSecId = sections.find { it.name == chosenName }?.id.orEmpty()
                            selectedSectionId = chosenSecId
                            selectedRackId = ""
                            selectedRackName = "Select Rack"
                            inventoryViewModel.loadRackDropdown(chosenSecId)
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Rack", isRequired = true)
                    FormDropdown(
                        value = selectedRackName,
                        expanded = isRackExpanded,
                        onExpandChange = { if (selectedSectionId.isNotBlank()) isRackExpanded = it },
                        options = racks.map { it.name },
                        onOptionSelected = { chosenName ->
                            selectedRackName = chosenName
                            val chosenRkId = racks.find { it.name == chosenName }?.id.orEmpty()
                            selectedRackId = chosenRkId
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Bin Name", isRequired = true)
                    FormTextField(
                        value = binName,
                        onValueChange = { binName = it },
                        placeholder = "e.g. Bin B - 01"
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Bin Code")
                    FormTextField(
                        value = binCode,
                        onValueChange = { binCode = it },
                        placeholder = "e.g. B01"
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Sequence Order")
                    FormTextField(
                        value = sequenceOrder,
                        onValueChange = { sequenceOrder = it.filter { ch -> ch.isDigit() } },
                        placeholder = "1",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Description (Optional)")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Add any notes about this bin..."
                    )
                }

                item {
                    SectionHeader("Bin Type")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    FormLabel("Bin Type")
                    FormDropdown(
                        value = binType,
                        expanded = isBinTypeExpanded,
                        onExpandChange = { isBinTypeExpanded = it },
                        options = binTypeOptions,
                        onOptionSelected = { binType = it }
                    )
                }

                item {
                    SectionHeader("Capacity Settings")
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Max Quantity")
                            FormTextField(
                                value = maxQty,
                                onValueChange = { maxQty = it.filter { ch -> ch.isDigit() } },
                                placeholder = "500",
                                keyboardType = KeyboardType.Number
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Max Weight (kg)")
                            FormTextField(
                                value = maxWeight,
                                onValueChange = { maxWeight = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                placeholder = "5000",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    FormLabel("Default UOM")
                    FormTextField(
                        value = defaultUom,
                        onValueChange = { defaultUom = it },
                        placeholder = "e.g. Kg,Piece"
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    StatusCheckboxRow(
                        isActive = isActive,
                        onStatusChange = { isActive = it }
                    )
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
                    val finalWhId = selectedWarehouseId.ifBlank { warehouseId }
                    val finalFlId = selectedFloorId.ifBlank { floorId }
                    val finalSecId = selectedSectionId.ifBlank { sectionId }
                    val finalRkId = selectedRackId.ifBlank { rackId }

                    if (binName.isBlank() || finalWhId.isBlank() || finalFlId.isBlank() || finalSecId.isBlank() || finalRkId.isBlank()) {
                        viewModel.showError("Please fill required fields")
                        return@Update
                    }

                    if (binItem != null) {
                        viewModel.updateBin(
                            binId = binItem.id,
                            warehouseId = finalWhId,
                            floorId = finalFlId,
                            sectionId = finalSecId,
                            rackId = finalRkId,
                            name = binName,
                            code = binCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            binType = binType.lowercase(),
                            maxQuantity = maxQty.toIntOrNull() ?: 0,
                            maxWeightKg = maxWeight.toDoubleOrNull() ?: 0.0,
                            defaultUOM = defaultUom.ifBlank { "pcs" },
                            status = if (isActive) "active" else "inactive",
                            onSuccess = onSave,
                            onError = {}
                        )
                    } else {
                        viewModel.createBin(
                            warehouseId = finalWhId,
                            floorId = finalFlId,
                            sectionId = finalSecId,
                            rackId = finalRkId,
                            name = binName,
                            code = binCode,
                            sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                            binType = binType.lowercase(),
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
    }
}

// -------------------------------------------------------------
// Reusable Component: [x] Active   [ ] Inactive checkbox row
// -------------------------------------------------------------
@Composable
private fun StatusCheckboxRow(
    isActive: Boolean,
    onStatusChange: (Boolean) -> Unit
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onStatusChange(true) }
        ) {
            AppCheckbox(
                checked = isActive,
                onCheckedChange = { onStatusChange(true) }
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Active",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onStatusChange(false) }
        ) {
            AppCheckbox(
                checked = !isActive,
                onCheckedChange = { onStatusChange(false) }
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Inactive",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
    }
}

// Helper function to extract String ID from JsonElement safely
private fun extractJsonId(element: com.google.gson.JsonElement?): String {
    if (element == null || element.isJsonNull) return ""
    return try {
        if (element.isJsonObject) {
            element.asJsonObject.get("_id")?.asString
                ?: element.asJsonObject.get("id")?.asString
                ?: ""
        } else if (element.isJsonPrimitive) {
            element.asString
        } else ""
    } catch (_: Exception) { "" }
}