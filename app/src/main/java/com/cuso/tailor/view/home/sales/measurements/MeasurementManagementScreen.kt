@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.cuso.tailor.view.home.sales.measurements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.GarmentItem
import com.cuso.tailor.model.settings.MeasurementFieldItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.MeasurementsViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel

// ─────────────────────────────────────────────────────────────────────────────
// DATA MODELS FOR UI PRESENTATION
// ─────────────────────────────────────────────────────────────────────────────
data class AvailableGarmentPreset(
    val id: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val isSelected: Boolean = false
)

data class MeasurementEntry(val label: String, val value: String)

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 1: MEASUREMENTS AVAILABLE DIALOG (DYNAMIC API DRIVEN VIA MEASUREMENTS VIEWMODEL)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MeasurementsAvailableDialog(
    customerId: String? = null,
    viewModel: MeasurementsViewModel = hiltViewModel(),
    onGarmentSelected: (AvailableGarmentPreset) -> Unit = {},
    onAddNew: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // 🟢 STEP 3 LOG: Verify customerId inside Dialog
    LaunchedEffect(customerId) {
        android.util.Log.d("MEASUREMENT_DEBUG", "STEP 3: MeasurementsAvailableDialog LaunchedEffect triggered! customerId = '$customerId'")
        if (!customerId.isNullOrBlank()) {
            viewModel.fetchAvailableGarments(customerId)
        } else {
            android.util.Log.e("MEASUREMENT_DEBUG", "❌ STEP 3 FAILED: customerId is NULL or BLANK! API call was not triggered.")
        }
    }

    val garments by viewModel.availableGarments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingAvailableGarments.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGarmentId by remember(garments) {
        mutableStateOf(garments.firstOrNull { it.isSelected }?.id ?: garments.firstOrNull()?.id.orEmpty())
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        FabScaffold(
            modifier = Modifier.fillMaxSize(),
            fab = FabConfig(
                label = "Add New",
                icon = Icons.Default.Add,
                onClick = onAddNew
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(
                    title = "Measurements Available",
                    onClose = onClose
                )
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Garments...",
                    borderColor = sectionBorder,
                    textSecondaryColor = close_color,
                    onFilterClick = {}
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    Spacer(Modifier.height(tokens.extraPadding))



                    Spacer(Modifier.height(tokens.screenPadding))

                    if (isLoading && garments.isEmpty()) {
                        SafeSkeletonList(count = 3)
                    } else if (garments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No measurements available for this customer.",
                                fontSize = tokens.bodyMedium,
                                color = close_color
                            )
                        }
                    } else {
                        val filteredList = remember(garments, searchQuery) {
                            garments.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(if (tokens.isTablet) 4 else 3),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredList) { item ->
                                val isSelected = item.id == selectedGarmentId
                                GarmentPresetCard(
                                    item = item,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedGarmentId = item.id
                                        onGarmentSelected(item)
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

@Composable
private fun GarmentPresetCard(
    item: AvailableGarmentPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val borderColor = if (isSelected) Primary else BorderGray
    val containerColor = if (isSelected) primary_light else whiteBg

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = tokens.extraPadding, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(tokens.fieldHeight)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .background(if (isSelected) Primary.copy(alpha = 0.08f) else Primary_background),
                contentAlignment = Alignment.Center
            ) {
                // Dynamically render Cloudinary image with fallback placeholder
                if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(tokens.iconSize * 2.8f)
                            .padding(4.dp),
                        placeholder = painterResource(R.drawable.ic_shirts),
                        error = painterResource(R.drawable.ic_amber)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_shirts),
                        contentDescription = null,
                        tint = if (isSelected) Primary else headerGrey,
                        modifier = Modifier.size(tokens.iconSize * 1.3f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = item.name,
                fontSize = tokens.bodySmall,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) Primary else title_color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 2: MEASUREMENT MANAGEMENT SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MeasurementManagementScreen(
    customerName: String = "Hameed Rahman",
    phone: String = "+1 (555) 123-4567",
    lastOrderDate: String = "Dec 20, 2025",
    onAddField: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Measurement 1", "Measurement 2", "Measurement 3")

    val columnMeasurements = listOf(
        MeasurementEntry("Collar", "40 cm"),
        MeasurementEntry("Chest", "40 cm"),
        MeasurementEntry("Waist", "40 cm"),
        MeasurementEntry("Shoulder", "40 cm"),
        MeasurementEntry("Shirt Length", "40 cm")
    )

    var fitType by remember { mutableStateOf("Slim") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Measurement management",
                onClose = onClose
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(tokens.extraPadding))

                AppUnderlineTabRow(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(tokens.fieldHeight)
                                .clip(CircleShape)
                                .background(light_grey),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = close_color,
                                modifier = Modifier.size(tokens.iconSize * 1.2f)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = customerName,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            Text(
                                text = phone,
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                            Text(
                                text = "Last Order: $lastOrderDate",
                                fontSize = tokens.label,
                                color = close_color
                            )
                        }
                    }

                    Button(
                        onClick = onAddField,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                    ) {
                        Text(
                            text = "Add field",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Add, null, tint = whiteBg, modifier = Modifier.size(14.dp))
                    }
                }

                HorizontalDivider(color = sectionBorder)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        columnMeasurements.forEach { item ->
                            MeasurementValueRow(label = item.label, value = item.value)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(200.dp)
                            .background(sectionBorder)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        columnMeasurements.forEach { item ->
                            MeasurementValueRow(label = item.label, value = item.value)
                        }
                    }
                }

                HorizontalDivider(color = sectionBorder)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Text(
                        text = "Fit & Preferences",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fit Type",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .background(light_grey)
                                .padding(2.dp)
                        ) {
                            listOf("Slim", "Regular", "Loose").forEach { option ->
                                val isSelected = option == fitType
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                        .background(if (isSelected) whiteBg else Color.Transparent)
                                        .clickable { fitType = option }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = tokens.caption,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = if (isSelected) title_color else headerGrey
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tailor Notes:", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                        Text("Prefers loose sleeves", fontSize = tokens.bodySmall, color = TextPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Customer Preferences:", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                        Text("No bold patterns", fontSize = tokens.bodySmall, color = TextPrimary)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun MeasurementValueRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = tokens.bodySmall, color = title_color)
            Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        HorizontalDivider(color = grey_border)
    }
}

@Composable
private fun SafeSkeletonList(count: Int = 3) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.fieldHeight)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                    .background(light_grey)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 3: DYNAMIC MEASUREMENT FIELDS CONFIG SCREEN
// ─────────────────────────────────────────────────────────────────────────────

private fun getCategoryForField(field: MeasurementFieldItem): String {
    val name = (field.displayName ?: field.name).lowercase()
    return when {
        name.contains("shoulder") || name.contains("back") -> "Shoulder & Back"
        name.contains("sleeve") || name.contains("cuff") || name.contains("arm") || name.contains("bicep") -> "Sleeve"
        else -> "Body Measurement"
    }
}

private fun isValidMongoObjectId(id: String?): Boolean {
    if (id.isNullOrBlank() || id.length != 24) return false
    return id.all { it in "0123456789abcdefABCDEF" }
}

@Composable
fun MeasurementFieldsConfigScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onAddNewCategory: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    LaunchedEffect(Unit) {
        viewModel.fetchGarments()
        viewModel.fetchDesigns()
        viewModel.fetchMeasurementFields()
    }

    val garments by viewModel.garments.collectAsStateWithLifecycle()
    val isLoadingGarments by viewModel.isLoadingGarments.collectAsStateWithLifecycle()
    var selectedGarment by remember { mutableStateOf<GarmentItem?>(null) }
    var garmentDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(garments) {
        if (selectedGarment == null && garments.isNotEmpty()) {
            selectedGarment = garments.firstOrNull()
        }
    }

    val designs by viewModel.designs.collectAsStateWithLifecycle()
    val isLoadingDesigns by viewModel.isLoadingDesigns.collectAsStateWithLifecycle()
    val groupedDesigns = remember(designs) {
        if (designs.isEmpty()) {
            listOf(
                "Neck Design" to listOf("Mandarin", "Spread", "Club", "Point", "Button Down", "Band"),
                "Sleeve Design" to listOf("Full Sleeve", "Half Sleeve", "Roll-up Sleeve"),
                "Cuff Style" to listOf("Rounded 2-Button", "French Cuff", "Single Button")
            )
        } else {
            designs.groupBy { it.designType.ifBlank { "General Design" } }
                .map { (type, items) -> type to items.map { it.name } }
        }
    }

    val garmentOptions = remember(garments) {
        if (garments.isEmpty()) emptyList()
        else garments.map { it.displayName ?: it.name }
    }

    val measurementFields by viewModel.measurementFields.collectAsStateWithLifecycle()
    val isLoadingFields by viewModel.isLoadingMeasurementFields.collectAsStateWithLifecycle()

    val groupedFields = remember(measurementFields) {
        measurementFields.groupBy { getCategoryForField(it) }
    }

    var activePropertyTab by remember { mutableStateOf("Basic") }
    var fieldNameInput by remember { mutableStateOf("") }
    var fieldTypeDropdown by remember { mutableStateOf("Number") }
    var unitTypeDropdown by remember { mutableStateOf("Inch") }
    var categoryMappingDropdown by remember { mutableStateOf("Body Measurement") }
    var fieldDescriptionInput by remember { mutableStateOf("") }

    var isFieldTypeExpanded by remember { mutableStateOf(false) }
    var isUnitTypeExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        FabScaffold(
            modifier = Modifier.fillMaxSize(),
            fab = FabConfig(
                label = "Add New Category",
                icon = Icons.Default.Add,
                onClick = {
                    selectedGarment?.let { garment ->
                        val segmentId = garment.applicableSegments.firstOrNull()?.id.orEmpty()
                        viewModel.setSelectedGarmentForDetail(
                            segmentId = segmentId,
                            garmentId = garment.id,
                            title = garment.displayName ?: garment.name
                        )
                    }
                    onAddNewCategory()
                }
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(
                    title = "Measurement Fields",
                    onClose = onClose
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp)
                ) {
                    Spacer(Modifier.height(tokens.screenPadding))

                    Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                        FormDropdown(
                            label = "SELECT GARMENT",
                            value = selectedGarment?.displayName ?: selectedGarment?.name
                            ?: if (isLoadingGarments) "Loading garments..." else "Select Garment",
                            expanded = garmentDropdownExpanded,
                            onExpandChange = { garmentDropdownExpanded = it },
                            options = garmentOptions,
                            onOptionSelected = { selectedName ->
                                selectedGarment =
                                    garments.firstOrNull { (it.displayName ?: it.name) == selectedName }
                            },
                            enabled = !isLoadingGarments && garments.isNotEmpty()
                        )
                    }

                    Spacer(Modifier.height(tokens.screenPadding))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Design Categories",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(light_grey)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${groupedDesigns.size} presets",
                                fontSize = tokens.label,
                                color = close_color,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    if (isLoadingDesigns && designs.isEmpty()) {
                        SafeSkeletonList(count = 2)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            groupedDesigns.forEach { (categoryName, presets) ->
                                DesignCategoryCard(title = categoryName, presets = presets)
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.screenPadding))

                    val currentGarmentName = selectedGarment?.displayName ?: selectedGarment?.name ?: "Shirt"
                    Text(
                        text = "Measurement Fields – $currentGarmentName",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color,
                        modifier = Modifier.padding(horizontal = tokens.screenPadding)
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    if (isLoadingFields && measurementFields.isEmpty()) {
                        SafeSkeletonList(count = 3)
                    } else if (groupedFields.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.screenPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No measurement fields found.",
                                fontSize = tokens.bodySmall,
                                color = close_color
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            groupedFields.forEach { (groupTitle, fields) ->
                                GroupedFieldsSection(
                                    title = groupTitle,
                                    fields = fields,
                                    onStatusToggle = { field, nextStatus ->
                                        if (isValidMongoObjectId(field.id)) {
                                            viewModel.changeMeasurementFieldStatus(
                                                fieldId = field.id,
                                                nextStatus = nextStatus
                                            )
                                        } else {
                                            viewModel.showError("Invalid Field ID. Cannot update status.")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.screenPadding * 1.5f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding)
                    ) {
                        Text(
                            text = "Edit Field Properties",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.fieldHeight)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .background(grey_border)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Basic", "Advanced", "System").forEach { tab ->
                                val isSelected = tab == activePropertyTab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                        .background(if (isSelected) whiteBg else Color.Transparent)
                                        .clickable { activePropertyTab = tab },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) title_color else headerGrey
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.screenPadding))

                        Text("Field Name", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                        Spacer(Modifier.height(6.dp))
                        FormTextField(
                            value = fieldNameInput,
                            onValueChange = { fieldNameInput = it },
                            placeholder = "Eg: Chest Round"
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormDropdown(
                            label = "Field Type",
                            value = fieldTypeDropdown,
                            expanded = isFieldTypeExpanded,
                            onExpandChange = { isFieldTypeExpanded = it },
                            options = listOf("Number", "Text", "Dropdown"),
                            onOptionSelected = { fieldTypeDropdown = it }
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormDropdown(
                            label = "Unit Type",
                            value = unitTypeDropdown,
                            expanded = isUnitTypeExpanded,
                            onExpandChange = { isUnitTypeExpanded = it },
                            options = listOf("Inch", "CM", "MM"),
                            onOptionSelected = { unitTypeDropdown = it }
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormDropdown(
                            label = "Category Mapping",
                            value = categoryMappingDropdown,
                            expanded = isCategoryExpanded,
                            onExpandChange = { isCategoryExpanded = it },
                            options = listOf("Body Measurement", "Shoulder & Back", "Sleeve"),
                            onOptionSelected = { categoryMappingDropdown = it }
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        Text("Field Description", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                        Spacer(Modifier.height(6.dp))
                        FormTextArea(
                            value = fieldDescriptionInput,
                            onValueChange = { fieldDescriptionInput = it },
                            placeholder = "Describe how to take this measure..."
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN CATEGORY ACCORDION CARD WITH CHIPS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DesignCategoryCard(title: String, presets: List<String>) {
    val tokens = LocalAppTokens.current
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .padding(tokens.extraPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = sectionBorder,
                modifier = Modifier.size(tokens.iconSize)
            )
        }
        Spacer(Modifier.height(5.dp))
        HorizontalDivider(color = sectionBorder)
        Spacer(Modifier.height(5.dp))

        AnimatedVisibility(visible = isExpanded) {
            Column {
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { chipName ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                .background(Primary_background)
                                .border(0.5.dp, PrimaryBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipName,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Normal,
                                color = textSubdued
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GROUPED FIELDS LIST COMPONENT (EDGE-TO-EDGE FULL WIDTH)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GroupedFieldsSection(
    title: String,
    fields: List<MeasurementFieldItem>,
    onStatusToggle: (MeasurementFieldItem, String) -> Unit = { _, _ -> }
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = title_color,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = null,
                tint = headerGrey,
                modifier = Modifier.size(tokens.iconSize)
            )
        }

        HorizontalDivider(
            color = sectionBorder,
            modifier = Modifier.fillMaxWidth()
        )

        fields.forEach { item ->
            val isActive = item.status?.equals("Active", ignoreCase = true) == true

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = item.displayName ?: item.name,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = title_color
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.25f))
                                .background(redBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Required",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Normal,
                                color = redText
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = item.inputType,
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Unit: ${item.unit ?: "Inch"}  •  Status: ${if (isActive) "Active" else "Inactive"}",
                        fontSize = tokens.caption,
                        color = close_color
                    )
                }

                MiniSwitch(
                    checked = isActive,
                    onCheckedChange = { isChecked ->
                        val nextStatus = if (isChecked) "Active" else "Inactive"
                        onStatusToggle(item, nextStatus)
                    }
                )
            }
            HorizontalDivider(
                color = sectionBorder,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ROUTER CONNECTOR FOR MEASUREMENT SUB-FLOWS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RenderMeasurementScreens(
    screen: String,
    selectedMeasurementItem: com.cuso.tailor.model.sales.MeasurementItem?,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit
) {
    when (screen) {
        "measurements_available_view" -> {
            MeasurementsAvailableDialog(
                customerId = selectedMeasurementItem?.customerId,
                onGarmentSelected = { onNavigate("measurement_management_view") },
                onAddNew = { onNavigate("measurement_management_view") },
                onClose = onGoBack
            )
        }
        "measurement_management_view" -> {
            MeasurementManagementScreen(
                customerName = selectedMeasurementItem?.customerName ?: "Hameed Rahman",
                phone = selectedMeasurementItem?.contact ?: "+1 (555) 123-4567",
                lastOrderDate = selectedMeasurementItem?.measuredDate ?: "Dec 20, 2025",
                onAddField = { onNavigate("measurement_fields_config") },
                onClose = onGoBack
            )
        }
        "measurement_fields_config" -> {
            MeasurementFieldsConfigScreen(
                onAddNewCategory = { onNavigate("sales_add_garment_category") },
                onClose = onGoBack
            )
        }
    }
}