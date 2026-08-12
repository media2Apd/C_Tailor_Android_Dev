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
// NOTE: package must match the folder this file is placed in, e.g.
// app/src/main/java/com/cuso/mobile/view/home/warehouse/WarehouseSettingsScreen.kt
package com.cuso.mobile.view.home.warehouse

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardImage
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.R

// ─────────────────────────────────────────────────────────────
// Local data model for this screen (no network / repository layer)
// ─────────────────────────────────────────────────────────────

data class WarehouseItem(
    val id: String,
    val name: String,
    val warehouseCode: String,
    val locality: String,
    val seqOrder: Int,
    val sections: Int,
    val racks: Int,
    val bins: Int,
    val floorArea: String,
    val tempZone: String
)

data class WarehouseFormData(
    val name: String = "",
    val warehouseCode: String = "",
    val branch: String = "",
    val warehouseType: String = "",
    val description: String = "",
    val addressLine: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val contactNumber: String = "",
    val warehouseManager: String = "",
    val isActive: Boolean = true,
    val isInactive: Boolean = true
)

private fun sampleWarehouses(): List<WarehouseItem> = listOf(
    WarehouseItem(
        id = "1",
        name = "Central WH",
        warehouseCode = "WH-001",
        locality = "Madhavaram",
        seqOrder = 1,
        sections = 4,
        racks = 32,
        bins = 483,
        floorArea = "5,000 sqft",
        tempZone = "Normal"
    )
)

@Composable
fun WarehouseSettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var warehouses by remember { mutableStateOf(sampleWarehouses()) }
    var editingWarehouse by remember { mutableStateOf<WarehouseItem?>(null) }

    var addSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var editSheetState by remember { mutableStateOf(SheetValue.Hidden) }

    // Only the blur amount is actually used to drive blurScrim(); the scrim
    // alpha value from the callback is intentionally not stored to avoid an
    // unused-variable warning. Add it back only if it is used elsewhere.
    var addSheetBlur by remember { mutableStateOf(0.dp) }
    var editSheetBlur by remember { mutableStateOf(0.dp) }

    val isAnySheetOpen = addSheetState != SheetValue.Hidden || editSheetState != SheetValue.Hidden

    var searchQuery by remember { mutableStateOf("") }

    val filteredWarehouses = warehouses.filter { w ->
        searchQuery.isBlank() ||
                w.name.contains(searchQuery, ignoreCase = true) ||
                w.warehouseCode.contains(searchQuery, ignoreCase = true)
    }

    // Using Scaffold topBar slot ensures TitleBar is on top of BottomSheets and Scrims
    Scaffold(
        topBar = {
            TitleBar("Warehouse", onClose = onBack)
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

            FabScaffold(
                fab = FabConfig(
                    label = "Add Warehouse",
                    icon = Icons.Default.Add,
                    onClick = { addSheetState = SheetValue.Expanded }
                ),
                fabVisible = !isAnySheetOpen
            ) {
                // Blur is applied only to this Column, not the TitleBar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color.Transparent)
                        .blurScrim(addSheetBlur.coerceAtLeast(editSheetBlur))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ScreenBreadcrumb(segments = listOf("Settings", "Warehouse"), onClick = {})

                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                            placeholder = "Search Warehouse...",
                            accentColor = BluePrimary
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        if (filteredWarehouses.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Warehouse, null, tint = Color.LightGray, modifier = Modifier.size(tokens.iconSize * 2.5f))
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        if (searchQuery.isNotBlank()) "No matching warehouses found" else "No warehouses found",
                                        color = Color.Gray,
                                        fontSize = tokens.bodyMedium
                                    )
                                }
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredWarehouses) { warehouse ->
                                    WarehouseCard(
                                        warehouse = warehouse,
                                        onEdit = {
                                            editingWarehouse = warehouse
                                            editSheetState = SheetValue.Expanded
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Warehouse BottomSheet - topInset 60.dp matches TitleBar height
        SmoothBottomSheet(
            state = addSheetState,
            onStateChange = { newState ->
                addSheetState = newState
                if (newState == SheetValue.Hidden) addSheetBlur = 0.dp
            },
            peekHeight = 420.dp,
            topInset = 66.dp,
            sheetBackgroundColor = whiteBg,
            collapsedCornerRadius = 24.dp,
            dragCloseEnabled = true,
            scrollableContent = true,
            onDismissRequest = {
                addSheetState = SheetValue.Hidden
                addSheetBlur = 0.dp
            },
            onBlurScrimChange = { r, _ -> addSheetBlur = r }
        ) {
            WarehouseFormSheetContent(
                title = "Add warehouse",
                submitLabel = "Add Warehouse",
                initialData = WarehouseFormData(),
                onDismiss = { addSheetState = SheetValue.Hidden },
                onSubmit = { formData ->
                    val nextSeq = (warehouses.maxOfOrNull { it.seqOrder } ?: 0) + 1
                    warehouses = warehouses + WarehouseItem(
                        id = nextSeq.toString(),
                        name = formData.name,
                        warehouseCode = formData.warehouseCode,
                        locality = formData.city,
                        seqOrder = nextSeq,
                        sections = 0,
                        racks = 0,
                        bins = 0,
                        floorArea = "—",
                        tempZone = "—"
                    )
                    addSheetState = SheetValue.Hidden
                    addSheetBlur = 0.dp
                }
            )
        }

        // Edit Warehouse BottomSheet
        editingWarehouse?.let { warehouse ->
            SmoothBottomSheet(
                state = editSheetState,
                onStateChange = { newState ->
                    editSheetState = newState
                    if (newState == SheetValue.Hidden) {
                        editingWarehouse = null
                        editSheetBlur = 0.dp
                    }
                },
                peekHeight = 420.dp,
                topInset = 66.dp,
                sheetBackgroundColor = whiteBg,
                collapsedCornerRadius = 24.dp,
                dragCloseEnabled = true,
                scrollableContent = true,
                onDismissRequest = {
                    editSheetState = SheetValue.Hidden
                    editingWarehouse = null
                    editSheetBlur = 0.dp
                },
                onBlurScrimChange = { r, _ -> editSheetBlur = r }
            ) {
                WarehouseFormSheetContent(
                    title = "Edit warehouse",
                    submitLabel = "Update Warehouse",
                    initialData = WarehouseFormData(
                        name = warehouse.name,
                        warehouseCode = warehouse.warehouseCode,
                        city = warehouse.locality
                    ),
                    onDismiss = {
                        editSheetState = SheetValue.Hidden
                        editingWarehouse = null
                    },
                    onSubmit = { formData ->
                        warehouses = warehouses.map {
                            if (it.id == warehouse.id) {
                                it.copy(name = formData.name, warehouseCode = formData.warehouseCode, locality = formData.city)
                            } else it
                        }
                        editSheetState = SheetValue.Hidden
                        editingWarehouse = null
                        editSheetBlur = 0.dp
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// List Card
// ─────────────────────────────────────────────────────────────

@Composable
private fun WarehouseCard(
    warehouse: WarehouseItem,
    onEdit: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // DataCard's real signature has `content` followed by `showDateIcon: Boolean`
    // as the actual last parameter, so a trailing lambda `DataCard(...) { ... }`
    // binds to showDateIcon (Boolean) instead of content — that caused the
    // earlier "Function0<Unit> but Boolean expected" error. Passing content
    // as an explicit named argument avoids that entirely.
    //
    // smalltitle -> "Central WH" (rendered inline next to the icon)
    // subtitle -> "WH-001 • Madhavaram" (rendered on its own line below) —
    // DataCard already scales both of these with its own internal tokens,
    // so no font-size overrides are needed here.
    // DataCardImage's `painter` path uses ContentScale.Crop + fillMaxSize()
    // internally (meant for photo/avatar images), which made the icon
    // overflow the rounded box edge-to-edge. The `vector` path instead
    // renders it as an Icon sized to half the box and centered, so it
    // sits neatly inside the box with breathing room on all sides —
    // ic_warehouse must be a vector drawable for vectorResource to work.
    DataCard(
        item = warehouse,
        image = DataCardImage(
            vector = ImageVector.vectorResource(id = R.drawable.ic_warehouse),
            tint = BluePrimary,
            backgroundColor = Color(0xFFEEF0FF),
            shape = RoundedCornerShape(14.dp),
            size = 44.dp
        ),
        smalltitle = warehouse.name,
        titleColor = title_color,
        subtitle = "${warehouse.warehouseCode} • ${warehouse.locality}",
        actions = listOf(
            MenuAction("Edit", Icons.Default.Edit) { onEdit() }
        ),
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                WarehouseStatsRow(
                    stats = listOf(
                        warehouse.seqOrder.toString() to "Seq. Order",
                        warehouse.sections.toString() to "Sections",
                        warehouse.racks.toString() to "Racks",
                        warehouse.bins.toString() to "Bins"
                    )
                )

                Spacer(Modifier.height(16.dp))
                Text("Capacity Summary", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floor Area", fontSize = tokens.caption, color = mutedText)
                        Spacer(Modifier.height(2.dp))
                        Text(warehouse.floorArea, fontSize = tokens.bodySmall, color = title_color)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Temp. Zone", fontSize = tokens.caption, color = mutedText)
                        Spacer(Modifier.height(2.dp))
                        Text(warehouse.tempZone, fontSize = tokens.bodySmall, color = title_color)
                    }
                }
            }
        }
    )
}

/**
 * Renders the 4-column stat row (value on top, label below) with thin
 * vertical dividers between columns. Both the value and the label use
 * tokens.bodySmall with no fontWeight override, so they scale together
 * with the rest of the adaptive typography and stay visually even.
 */
@Composable
private fun WarehouseStatsRow(stats: List<Pair<String, String>>) {
    val tokens = LocalAppTokens.current

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        stats.forEachIndexed { index, (value, label) ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .width(1.dp)
                        .height(34.dp)
                        .background(Color(0xFFE5E7EB))
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(value, fontSize = tokens.bodySmall, color = title_color)
                Spacer(Modifier.height(2.dp))
                Text(label, fontSize = tokens.bodySmall, color = mutedText)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable Collapsible Section (accordion) for the Add/Edit sheets
// ─────────────────────────────────────────────────────────────

@Composable
private fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF6B7280)
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
        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}

// ─────────────────────────────────────────────────────────────
// Shared Add/Edit form sheet content (accordion: Basic Information,
// Address Details, Control Settings), matching the provided screens
// ─────────────────────────────────────────────────────────────

@Composable
private fun WarehouseFormSheetContent(
    title: String,
    submitLabel: String,
    initialData: WarehouseFormData,
    onDismiss: () -> Unit,
    onSubmit: (WarehouseFormData) -> Unit
) {
    val tokens = LocalAppTokens.current

    var name by remember { mutableStateOf(initialData.name) }
    var warehouseCode by remember { mutableStateOf(initialData.warehouseCode) }
    var branch by remember { mutableStateOf(initialData.branch) }
    var warehouseType by remember { mutableStateOf(initialData.warehouseType) }
    var description by remember { mutableStateOf(initialData.description) }

    var addressLine by remember { mutableStateOf(initialData.addressLine) }
    var city by remember { mutableStateOf(initialData.city) }
    var state by remember { mutableStateOf(initialData.state) }
    var pincode by remember { mutableStateOf(initialData.pincode) }
    var contactNumber by remember { mutableStateOf(initialData.contactNumber) }

    var warehouseManager by remember { mutableStateOf(initialData.warehouseManager) }
    var isActive by remember { mutableStateOf(initialData.isActive) }
    var isInactive by remember { mutableStateOf(initialData.isInactive) }

    var branchExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    // Section 1 open by default, matching the reference flow
    var basicExpanded by remember { mutableStateOf(true) }
    var addressExpanded by remember { mutableStateOf(false) }
    var controlExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    val branchOptions = listOf("YES", "NO")
    val warehouseTypeOptions = listOf("Showroom", "Storage", "Distribution Center", "Cold Storage")

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            Text(title, fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }

        CollapsibleSection(title = "Basic Information", expanded = basicExpanded, onToggle = { basicExpanded = !basicExpanded }) {
            Column {
                FormLabel("Warehouse Name", isRequired = true)
                FormTextField(value = name, onValueChange = { name = it; nameError = false }, placeholder = "Enter warehouse name", isError = nameError, errorMessage = "Warehouse name is required")
            }
            Column {
                FormLabel("Warehouse Code", isRequired = true)
                FormTextField(value = warehouseCode, onValueChange = { warehouseCode = it; codeError = false }, placeholder = "Enter warehouse code", isError = codeError, errorMessage = "Warehouse code is required")
            }
            FormDropdown(
                label = "Branch",
                value = branch.ifBlank { "Select an option" },
                expanded = branchExpanded,
                onExpandChange = { branchExpanded = it },
                options = branchOptions,
                onOptionSelected = { branch = it }
            )
            FormDropdown(
                label = "Warehouse Types",
                value = warehouseType.ifBlank { "Select an option" },
                expanded = typeExpanded,
                onExpandChange = { typeExpanded = it },
                options = warehouseTypeOptions,
                onOptionSelected = { warehouseType = it }
            )
            Column {
                FormLabel("Description (Optional)")
                // NOTE: FormTextField does not expose singleLine / minLines params,
                // so the default (single-line) behaviour is used here.
                FormTextField(value = description, onValueChange = { description = it }, placeholder = "Enter description")
            }
        }

        CollapsibleSection(title = "Address Details", expanded = addressExpanded, onToggle = { addressExpanded = !addressExpanded }) {
            Column {
                FormLabel("Address Line")
                FormTextField(value = addressLine, onValueChange = { addressLine = it }, placeholder = "Enter address line")
            }
            Column {
                FormLabel("City")
                FormTextField(value = city, onValueChange = { city = it }, placeholder = "Enter city")
            }
            Column {
                FormLabel("State")
                FormTextField(value = state, onValueChange = { state = it }, placeholder = "Enter state")
            }
            Column {
                FormLabel("Pincode")
                FormTextField(value = pincode, onValueChange = { pincode = it }, placeholder = "Enter pincode", keyboardType = KeyboardType.Number)
            }
            Column {
                FormLabel("Contact Number")
                FormTextField(value = contactNumber, onValueChange = { contactNumber = it }, placeholder = "Enter contact number", keyboardType = KeyboardType.Phone)
            }
        }

        CollapsibleSection(title = "Control Settings", expanded = controlExpanded, onToggle = { controlExpanded = !controlExpanded }) {
            Column {
                FormLabel("Warehouse Manager")
                FormTextField(value = warehouseManager, onValueChange = { warehouseManager = it }, placeholder = "Enter warehouse manager")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it }, colors = CheckboxDefaults.colors(checkedColor = BluePrimary))
                    Text("Active", fontSize = tokens.bodySmall, color = Color(0xFF374151))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isInactive, onCheckedChange = { isInactive = it }, colors = CheckboxDefaults.colors(checkedColor = BluePrimary))
                    Text("Inactive", fontSize = tokens.bodySmall, color = Color(0xFF374151))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
            ) {
                Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
            }
            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; basicExpanded = true; return@Button }
                    if (warehouseCode.isBlank()) { codeError = true; basicExpanded = true; return@Button }
                    onSubmit(
                        WarehouseFormData(
                            name = name,
                            warehouseCode = warehouseCode,
                            branch = branch,
                            warehouseType = warehouseType,
                            description = description,
                            addressLine = addressLine,
                            city = city,
                            state = state,
                            pincode = pincode,
                            contactNumber = contactNumber,
                            warehouseManager = warehouseManager,
                            isActive = isActive,
                            isInactive = isInactive
                        )
                    )
                },
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(submitLabel, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
            }
        }
    }
}