@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.mobile.view.home.inventory.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.sales.lead.MiniSwitch

// -------------------------------------------------------------
// Screen 1: Location Structure Overview
// -------------------------------------------------------------
@Composable
fun LocationStructureScreen(
    onClose: () -> Unit = {},
    onAddLocation: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WarehouseDetailCard(
                        icon = Icons.Default.Warehouse,
                        title = "Central WH",
                        subtitle = "WH-001 · Madhavaram",
                        sequenceOrder = "1",
                        totalSections = "4",
                        totalRacks = "32",
                        totalBins = "483",
                        showFourGridBoxes = true,
                        capacityMetrics = listOf("Floor Area" to "5,000 sqft", "Temperature Zone" to "Normal")
                    )
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
    onClose: () -> Unit = {},
    onAddFloor: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WarehouseDetailCard(
                        icon = Icons.Default.Checkroom,
                        title = "Ground Floor",
                        status = "ACTIVE",
                        locationLabel = "Floor",
                        locationName = "Central WH",
                        sequenceOrder = "1",
                        totalSections = "4",
                        totalRacks = "32",
                        totalBins = "483",
                        capacityMetrics = listOf("Storage Type" to "Shelving", "Climate Control" to "AC Standard")
                    )
                }
                item {
                    WarehouseDetailCard(
                        icon = Icons.Default.Checkroom,
                        title = "First Floor",
                        status = "ACTIVE",
                        locationLabel = "Floor",
                        locationName = "Central WH",
                        sequenceOrder = "1",
                        totalSections = "4",
                        totalRacks = "32",
                        totalBins = "483",
                        capacityMetrics = listOf("Storage Type" to "Shelving", "Climate Control" to "AC Standard")
                    )
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
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var floorName by remember { mutableStateOf("Ground") }
    var floorCode by remember { mutableStateOf("GRD") }
    var sequenceOrder by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var temperatureZone by remember { mutableStateOf("Normal") }
    var floorArea by remember { mutableStateOf("5000") }
    var maxWeight by remember { mutableStateOf("50000") }
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
                    FormTextField(value = floorName, onValueChange = { floorName = it }, placeholder = "Ground")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Floor Code")
                    FormTextField(value = floorCode, onValueChange = { floorCode = it }, placeholder = "GRD")

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
                    FormTextField(value = temperatureZone, onValueChange = { temperatureZone = it }, placeholder = "Normal")
                }

                item {
                    SectionHeader("Capacity Info")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Floor Area (sqft)")
                    FormTextField(value = floorArea, onValueChange = { floorArea = it }, placeholder = "5000", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Max Weight Capacity (kg)")
                    FormTextField(value = maxWeight, onValueChange = { maxWeight = it }, placeholder = "50000", keyboardType = KeyboardType.Number)

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
                onClick = onSave
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
    onClose: () -> Unit = {},
    onAddSection: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WarehouseDetailCard(
                        icon = Icons.Default.Checkroom,
                        title = "Menswear",
                        status = "ACTIVE",
                        locationLabel = "Floor",
                        locationName = "Central WH",
                        sequenceOrder = "1",
                        linkedCategory = "Menswear",
                        totalRacks = "32",
                        totalBins = "483",
                        capacityMetrics = listOf("Storage Type" to "Shelving", "Climate Control" to "AC Standard")
                    )
                }
                item {
                    WarehouseDetailCard(
                        icon = Icons.Default.Checkroom,
                        title = "Womenswear",
                        status = "ACTIVE",
                        locationLabel = "Floor",
                        locationName = "Central WH",
                        sequenceOrder = "1",
                        linkedCategory = "Menswear",
                        totalRacks = "32",
                        totalBins = "483",
                        capacityMetrics = listOf("Storage Type" to "Shelving", "Climate Control" to "AC Standard")
                    )
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
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var sectionName by remember { mutableStateOf("Ground") }
    var sectionCode by remember { mutableStateOf("GRD") }
    var sequenceOrder by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var allowedCategory by remember { mutableStateOf("All") }
    var storageType by remember { mutableStateOf("Open Area") }
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
                    FormTextField(value = sectionName, onValueChange = { sectionName = it }, placeholder = "Ground")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Section Code")
                    FormTextField(value = sectionCode, onValueChange = { sectionCode = it }, placeholder = "GRD")

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

                    FormLabel("Allowed Product Category")
                    FormTextField(value = allowedCategory, onValueChange = { allowedCategory = it }, placeholder = "All")
                }

                item {
                    SectionHeader("Storage Type")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Storage Type")
                    FormTextField(value = storageType, onValueChange = { storageType = it }, placeholder = "Open Area")

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
                onClick = onSave
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
    onClose: () -> Unit = {},
    onAddRack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WarehouseDetailCard(
                        icon = Icons.Default.Checkroom,
                        title = "Rack R - 01",
                        status = "ACTIVE",
                        locationLabel = "Section",
                        locationName = "Central WH",
                        sequenceOrder = "1",
                        rackType = "Shelf",
                        totalRacks = "-",
                        totalBins = "483",
                        capacityMetrics = listOf("Max Capacity" to "500 pcs", "Max Weight" to "300 kg")
                    )
                }
                item {
                    WarehouseDetailCard(
                        icon = Icons.Default.Checkroom,
                        title = "Rack R - 02",
                        status = "ACTIVE",
                        locationLabel = "Section",
                        locationName = "Central WH",
                        sequenceOrder = "1",
                        rackType = "Shelf",
                        totalRacks = "-",
                        totalBins = "483",
                        capacityMetrics = listOf("Max Capacity" to "500 pcs", "Max Weight" to "300 kg")
                    )
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
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var rackName by remember { mutableStateOf("Ground") }
    var rackCode by remember { mutableStateOf("GRD") }
    var sequenceOrder by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var rackType by remember { mutableStateOf("All") }
    var maxQty by remember { mutableStateOf("500") }
    var maxWeight by remember { mutableStateOf("5000") }
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
                    FormTextField(value = rackName, onValueChange = { rackName = it }, placeholder = "Ground")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Rack Code")
                    FormTextField(value = rackCode, onValueChange = { rackCode = it }, placeholder = "GRD")

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
                    FormTextField(value = rackType, onValueChange = { rackType = it }, placeholder = "All")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Max Quantity Capacity")
                    FormTextField(value = maxQty, onValueChange = { maxQty = it }, placeholder = "500", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Max Weight Capacity (kg)")
                    FormTextField(value = maxWeight, onValueChange = { maxWeight = it }, placeholder = "5000", keyboardType = KeyboardType.Number)

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
                onClick = onSave
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
    onClose: () -> Unit = {},
    onAddBin: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WarehouseDetailCard(
                        icon = Icons.Default.Checkroom,
                        title = "Bin B - 01",
                        status = "ACTIVE",
                        locationLabel = "Rack",
                        locationName = "R - 01",
                        sequenceOrder = "1",
                        binType = "Regular",
                        totalRacks = "-",
                        totalBins = "483",
                        capacityMetrics = listOf("Max Capacity" to "500 pcs", "Max Weight" to "300 kg")
                    )
                }
                item {
                    WarehouseDetailCard(
                        icon = Icons.Default.Checkroom,
                        title = "Bin B - 01",
                        status = "ACTIVE",
                        locationLabel = "Rack",
                        locationName = "R - 01",
                        sequenceOrder = "1",
                        binType = "Regular",
                        totalRacks = "-",
                        totalBins = "483",
                        capacityMetrics = listOf("Max Capacity" to "500 pcs", "Max Weight" to "300 kg")
                    )
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
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var binName by remember { mutableStateOf("Ground") }
    var binCode by remember { mutableStateOf("GRD") }
    var sequenceOrder by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var binType by remember { mutableStateOf("All") }
    var maxQty by remember { mutableStateOf("500") }
    var maxWeight by remember { mutableStateOf("5000") }
    var defaultUom by remember { mutableStateOf("Box") }
    var isActive by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Bin Rack", onClose = onClose)

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
                    FormTextField(value = binName, onValueChange = { binName = it }, placeholder = "Ground")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Bin Code")
                    FormTextField(value = binCode, onValueChange = { binCode = it }, placeholder = "GRD")

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Sequence Orders")
                    FormTextField(value = sequenceOrder, onValueChange = { sequenceOrder = it }, placeholder = "1", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Description (Optional)")
                    FormTextArea(value = description, onValueChange = { description = it }, placeholder = "Add floor layout or special remarks...")
                }

                item {
                    SectionHeader("Bin Type")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Bin Type")
                    FormTextField(value = binType, onValueChange = { binType = it }, placeholder = "All")
                }

                item {
                    SectionHeader("Capacity Settings")
                    Spacer(Modifier.height(10.dp))

                    FormLabel("Max Quantity Capacity")
                    FormTextField(value = maxQty, onValueChange = { maxQty = it }, placeholder = "500", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Max Weight Capacity (kg)")
                    FormTextField(value = maxWeight, onValueChange = { maxWeight = it }, placeholder = "5000", keyboardType = KeyboardType.Number)

                    Spacer(Modifier.height(8.dp))
                    FormLabel("Default UOM")
                    FormTextField(value = defaultUom, onValueChange = { defaultUom = it }, placeholder = "Box")

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
                onClick = onSave
            ),
            backWidthFraction = 0.35f,
            trailingWidthFraction = 0.55f
        )
    }
}