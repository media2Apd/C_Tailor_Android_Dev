@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.mobile.view.home.inventory.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.SettingsViewModel
import com.cuso.mobile.R

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

    LaunchedEffect(warehouseId) {
        viewModel.fetchFloors(warehouseId)
    }

    val totalSections = floors.sumOf { it.sectionsCount }
    val totalRacks = floors.sumOf { it.racksCount }
    val totalBins = floors.sumOf { it.binsCount }

    FabScaffold(
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        fab = FabConfig(
            label = "Add Location",
            icon = Icons.Default.Add,
            onClick = onAddLocation
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Location Structure", onClose = onClose)

            if (isLoading && floors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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

// -------------------------------------------------------------
// Screen 2: Floor Overview
// -------------------------------------------------------------
@Composable
fun FloorOverviewScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "6a8d5643f685905f29057664",
    onClose: () -> Unit = {},
    onAddFloor: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()

    LaunchedEffect(warehouseId) {
        viewModel.fetchFloors(warehouseId)
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        fab = FabConfig(
            label = "Add Floor",
            icon = Icons.Default.Add,
            onClick = onAddFloor
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Floor Overview", onClose = onClose)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(floors, key = { it.id }) { floor ->
                        WarehouseDetailCard(
                            icon = R.drawable.ic_hanker,
                            title = floor.name,
                            status = floor.status.uppercase(),
                            locationLabel = "Floor",
                            locationName = floor.code,
                            sequenceOrder = floor.sequenceOrder.toString(),
                            totalSections = floor.sectionsCount.toString(),
                            totalRacks = floor.racksCount.toString(),
                            totalBins = floor.binsCount.toString(),
                            capacityMetrics = listOf(
                                "Temperature" to (floor.temperatureZone ?: "Normal"),
                                "Area" to "${floor.floorAreaSqft} sqft"
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Screen 3: Add Floor Form
// -------------------------------------------------------------
@Composable
fun AddFloorScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "6a8d5643f685905f29057664",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var floorName by remember { mutableStateOf("Ground Floor") }
    var floorCode by remember { mutableStateOf("GF") }
    var sequenceOrder by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var temperatureZone by remember { mutableStateOf("normal") }
    var floorArea by remember { mutableStateOf("5000") }
    var maxWeight by remember { mutableStateOf("20000") }
    var isActive by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Add Floor", onClose = onClose)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    SectionHeader("Basic Information")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Floor Name", isRequired = true)
                    FormTextField(value = floorName, onValueChange = { floorName = it }, placeholder = "Ground Floor")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Floor Code")
                    FormTextField(value = floorCode, onValueChange = { floorCode = it }, placeholder = "GF")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Sequence Orders")
                    FormTextField(value = sequenceOrder, onValueChange = { sequenceOrder = it }, placeholder = "1", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Description (Optional)")
                    FormTextArea(value = description, onValueChange = { description = it }, placeholder = "Add floor layout or special remarks...")
                }

                item {
                    SectionHeader("Structure Settings")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Temperature Zone")
                    FormTextField(value = temperatureZone, onValueChange = { temperatureZone = it }, placeholder = "normal")
                }

                item {
                    SectionHeader("Capacity Info")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Floor Area (sqft)")
                    FormTextField(value = floorArea, onValueChange = { floorArea = it }, placeholder = "5000", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Max Weight Capacity (kg)")
                    FormTextField(value = maxWeight, onValueChange = { maxWeight = it }, placeholder = "20000", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontSize = tokens.bodySmall, color = Color(0xFF6B7280))
                        MiniSwitch(checked = isActive, onCheckedChange = { isActive = it })
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
                label = "Save Floor",
                onClick = {
                    if (floorName.isBlank() || floorCode.isBlank()) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        return@Update
                    }
                    viewModel.createFloor(
                        warehouseId = warehouseId,
                        name = floorName,
                        code = floorCode,
                        sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                        description = description,
                        temperatureZone = temperatureZone,
                        floorAreaSqft = floorArea.toDoubleOrNull() ?: 0.0,
                        maxWeightCapacityKg = maxWeight.toDoubleOrNull() ?: 0.0,
                        status = if (isActive) "active" else "inactive",
                        onSuccess = {
                            Toast.makeText(context, "Floor created successfully!", Toast.LENGTH_SHORT).show()
                            onSave()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            ),
            backWidthFraction = 0.35f,
            trailingWidthFraction = 0.55f
        )
    }
}

// -------------------------------------------------------------
// Screen 4: Section Overview
// -------------------------------------------------------------
@Composable
fun SectionOverviewScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "6a8d5643f685905f29057664",
    floorId: String = "6a8d5762f685905f290576b3",
    onClose: () -> Unit = {},
    onAddSection: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()

    LaunchedEffect(warehouseId, floorId) {
        viewModel.fetchSections(warehouseId, floorId)
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        fab = FabConfig(
            label = "Add Section",
            icon = Icons.Default.Add,
            onClick = onAddSection
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Section Overview", onClose = onClose)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sections, key = { it.id }) { section ->
                        WarehouseDetailCard(
                            icon = R.drawable.ic_hanker,
                            title = section.name,
                            status = section.status.uppercase(),
                            locationLabel = "Section",
                            locationName = section.code,
                            sequenceOrder = section.sequenceOrder.toString(),
                            linkedCategory = section.allowedProductCategories.firstOrNull() ?: "General",
                            totalRacks = section.racksCount.toString(),
                            totalBins = section.binsCount.toString(),
                            capacityMetrics = listOf(
                                "Storage Type" to (section.storageType ?: "shelving"),
                                "Climate Control" to (section.climateControl ?: "ac_standard")
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Screen 5: Add Section Form
// -------------------------------------------------------------
@Composable
fun AddSectionScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "6a8d5643f685905f29057664",
    floorId: String = "6a8d5762f685905f290576b3",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var sectionName by remember { mutableStateOf("Textiles Section") }
    var sectionCode by remember { mutableStateOf("SEC-A") }
    var sequenceOrder by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("Section for fabric and textile items") }
    var allowedCategory by remember { mutableStateOf("6a8c3b4c8b122b97dd1a75e7") }
    var storageType by remember { mutableStateOf("shelving") }
    var climateControl by remember { mutableStateOf("ac_standard") }
    var isActive by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Add Section", onClose = onClose)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    SectionHeader("Basic Information")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Section Name", isRequired = true)
                    FormTextField(value = sectionName, onValueChange = { sectionName = it }, placeholder = "Textiles Section")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Section Code")
                    FormTextField(value = sectionCode, onValueChange = { sectionCode = it }, placeholder = "SEC-A")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Sequence Orders")
                    FormTextField(value = sequenceOrder, onValueChange = { sequenceOrder = it }, placeholder = "1", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Description (Optional)")
                    FormTextArea(value = description, onValueChange = { description = it }, placeholder = "Add floor layout or special remarks...")
                }

                item {
                    SectionHeader("Category Mapping")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Allowed Product Category ID")
                    FormTextField(value = allowedCategory, onValueChange = { allowedCategory = it }, placeholder = "Product Category ID")
                }

                item {
                    SectionHeader("Storage Type")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Storage Type")
                    FormTextField(value = storageType, onValueChange = { storageType = it }, placeholder = "shelving")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Climate Control")
                    FormTextField(value = climateControl, onValueChange = { climateControl = it }, placeholder = "ac_standard")

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontSize = tokens.bodySmall, color = Color(0xFF6B7280))
                        MiniSwitch(checked = isActive, onCheckedChange = { isActive = it })
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
                label = "Save Section",
                onClick = {
                    if (sectionName.isBlank() || sectionCode.isBlank()) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        return@Update
                    }
                    viewModel.createSection(
                        warehouseId = warehouseId,
                        floorId = floorId,
                        name = sectionName,
                        code = sectionCode,
                        sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                        description = description,
                        allowedProductCategories = if (allowedCategory.isNotBlank()) listOf(allowedCategory.trim()) else emptyList(),
                        storageType = storageType,
                        climateControl = climateControl,
                        status = if (isActive) "active" else "inactive",
                        onSuccess = {
                            Toast.makeText(context, "Section created successfully!", Toast.LENGTH_SHORT).show()
                            onSave()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            ),
            backWidthFraction = 0.35f,
            trailingWidthFraction = 0.55f
        )
    }
}

// -------------------------------------------------------------
// Screen 6: Rack Overview
// -------------------------------------------------------------
@Composable
fun RackOverviewScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "6a8d5643f685905f29057664",
    floorId: String = "6a8d5762f685905f290576b3",
    sectionId: String = "6a8d57f3f685905f290576bf",
    onClose: () -> Unit = {},
    onAddRack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val racks by viewModel.racks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()

    LaunchedEffect(warehouseId, floorId, sectionId) {
        viewModel.fetchRacks(warehouseId, floorId, sectionId)
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        fab = FabConfig(
            label = "Add Rack",
            icon = Icons.Default.Add,
            onClick = onAddRack
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Rack Overview", onClose = onClose)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(racks, key = { it.id }) { rack ->
                        WarehouseDetailCard(
                            icon = R.drawable.ic_hanker,
                            title = rack.name,
                            status = rack.status.uppercase(),
                            locationLabel = "Section",
                            locationName = rack.code,
                            sequenceOrder = rack.sequenceOrder.toString(),
                            rackType = rack.rackType ?: "shelf",
                            totalRacks = "-",
                            totalBins = rack.binsCount.toString(),
                            capacityMetrics = listOf(
                                "Max Capacity" to "${rack.maxQuantityCapacity} pcs",
                                "Max Weight" to "${rack.maxWeightCapacityKg} kg"
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Screen 7: Add Rack Form
// -------------------------------------------------------------
@Composable
fun AddRackScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "6a8d5643f685905f29057664",
    floorId: String = "6a8d5762f685905f290576b3",
    sectionId: String = "6a8d57f3f685905f290576bf",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var rackName by remember { mutableStateOf("Rack A1") }
    var rackCode by remember { mutableStateOf("RA01") }
    var sequenceOrder by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("First rack in Textiles Section") }
    var rackType by remember { mutableStateOf("shelf") }
    var maxQty by remember { mutableStateOf("1000") }
    var maxWeight by remember { mutableStateOf("500") }
    var isActive by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Add Rack", onClose = onClose)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    SectionHeader("Basic Information")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Rack Name", isRequired = true)
                    FormTextField(value = rackName, onValueChange = { rackName = it }, placeholder = "Rack A1")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Rack Code")
                    FormTextField(value = rackCode, onValueChange = { rackCode = it }, placeholder = "RA01")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Sequence Orders")
                    FormTextField(value = sequenceOrder, onValueChange = { sequenceOrder = it }, placeholder = "1", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Description (Optional)")
                    FormTextArea(value = description, onValueChange = { description = it }, placeholder = "Add floor layout or special remarks...")
                }

                item {
                    SectionHeader("Rack Specification")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Rack Type")
                    FormTextField(value = rackType, onValueChange = { rackType = it }, placeholder = "shelf")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Max Quantity Capacity")
                    FormTextField(value = maxQty, onValueChange = { maxQty = it }, placeholder = "1000", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Max Weight Capacity (kg)")
                    FormTextField(value = maxWeight, onValueChange = { maxWeight = it }, placeholder = "500", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontSize = tokens.bodySmall, color = Color(0xFF6B7280))
                        MiniSwitch(checked = isActive, onCheckedChange = { isActive = it })
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
                label = "Save Rack",
                onClick = {
                    if (rackName.isBlank() || rackCode.isBlank()) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        return@Update
                    }
                    viewModel.createRack(
                        warehouseId = warehouseId,
                        floorId = floorId,
                        sectionId = sectionId,
                        name = rackName,
                        code = rackCode,
                        sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                        description = description,
                        rackType = rackType,
                        maxQuantityCapacity = maxQty.toIntOrNull() ?: 0,
                        maxWeightCapacityKg = maxWeight.toDoubleOrNull() ?: 0.0,
                        status = if (isActive) "active" else "inactive",
                        onSuccess = {
                            Toast.makeText(context, "Rack created successfully!", Toast.LENGTH_SHORT).show()
                            onSave()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            ),
            backWidthFraction = 0.35f,
            trailingWidthFraction = 0.55f
        )
    }
}

// -------------------------------------------------------------
// Screen 8: Bin Overview
// -------------------------------------------------------------
@Composable
fun BinOverviewScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "",
    rackId: String = "",
    onClose: () -> Unit = {},
    onAddBin: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val bins by viewModel.bins.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLocationStructure.collectAsStateWithLifecycle()

    LaunchedEffect(warehouseId, rackId) {
        viewModel.fetchBins(warehouseId, rackId)
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        fab = FabConfig(
            label = "Add Bin",
            icon = Icons.Default.Add,
            onClick = onAddBin
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Bin Overview", onClose = onClose)

            if (isLoading) {
                    ListSkeleton()

            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bins, key = { it.id }) { bin ->
                        WarehouseDetailCard(
                            icon = R.drawable.ic_hanker,
                            title = bin.name,
                            status = bin.status.uppercase(),
                            locationLabel = "Rack",
                            locationName = bin.rackDisplayName.ifBlank { bin.rackCode },
                            sequenceOrder = bin.sequenceOrder.toString(),
                            binType = bin.binType ?: "regular",
                            totalRacks = "-",
                            totalBins =  "-",
                            capacityMetrics = listOf(
                                "Max Capacity" to "${bin.maxQuantity} ${bin.defaultUOM ?: "pcs"}",
                                "Max Weight" to "${bin.maxWeightKg} kg"
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Screen 9: Add Bin Form
// -------------------------------------------------------------
@Composable
fun AddBinScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    warehouseId: String = "6a8d5643f685905f29057664",
    floorId: String = "6a8d5762f685905f290576b3",
    sectionId: String = "6a8d57f3f685905f290576bf",
    rackId: String = "6a8d583bf685905f290576cd",
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var binName by remember { mutableStateOf("Bin B01") }
    var binCode by remember { mutableStateOf("B01") }
    var sequenceOrder by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var binType by remember { mutableStateOf("regular") }
    var maxQty by remember { mutableStateOf("300") }
    var maxWeight by remember { mutableStateOf("200") }
    var defaultUom by remember { mutableStateOf("pcs") }
    var isActive by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Add Bin", onClose = onClose)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    SectionHeader("Basic Information")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Bin Name", isRequired = true)
                    FormTextField(value = binName, onValueChange = { binName = it }, placeholder = "Bin B01")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Bin Code")
                    FormTextField(value = binCode, onValueChange = { binCode = it }, placeholder = "B01")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Sequence Orders")
                    FormTextField(value = sequenceOrder, onValueChange = { sequenceOrder = it }, placeholder = "1", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Description (Optional)")
                    FormTextArea(value = description, onValueChange = { description = it }, placeholder = "Add layout or special remarks...")
                }

                item {
                    SectionHeader("Bin Type")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Bin Type")
                    FormTextField(value = binType, onValueChange = { binType = it }, placeholder = "regular")
                }

                item {
                    SectionHeader("Capacity Settings")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Max Quantity Capacity")
                    FormTextField(value = maxQty, onValueChange = { maxQty = it }, placeholder = "300", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Max Weight Capacity (kg)")
                    FormTextField(value = maxWeight, onValueChange = { maxWeight = it }, placeholder = "200", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Default UOM")
                    FormTextField(value = defaultUom, onValueChange = { defaultUom = it }, placeholder = "pcs")

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontSize = tokens.bodySmall, color = Color(0xFF6B7280))
                        MiniSwitch(checked = isActive, onCheckedChange = { isActive = it })
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
                label = "Save Bin",
                onClick = {
                    if (binName.isBlank() || binCode.isBlank()) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        return@Update
                    }
                    viewModel.createBin(
                        warehouseId = warehouseId,
                        floorId = floorId,
                        sectionId = sectionId,
                        rackId = rackId,
                        name = binName,
                        code = binCode,
                        sequenceOrder = sequenceOrder.toIntOrNull() ?: 1,
                        binType = binType,
                        maxQuantity = maxQty.toIntOrNull() ?: 0,
                        maxWeightKg = maxWeight.toDoubleOrNull() ?: 0.0,
                        defaultUOM = defaultUom,
                        status = if (isActive) "active" else "inactive",
                        onSuccess = {
                            Toast.makeText(context, "Bin created successfully!", Toast.LENGTH_SHORT).show()
                            onSave()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            ),
            backWidthFraction = 0.35f,
            trailingWidthFraction = 0.55f
        )
    }
}