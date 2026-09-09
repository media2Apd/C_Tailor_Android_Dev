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

package com.cuso.mobile.view.home.inventory.items.item_groups

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.CreateItemGroupRequest
import com.cuso.mobile.model.inventory.PricingDto
import com.cuso.mobile.model.inventory.VariantAttributeDto
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.PanelBg
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.TextPrimary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.background_light_purple
import com.cuso.mobile.ui.theme.light_grey
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.primary_light
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.AppButton
import com.cuso.mobile.view.composable.AppCheckbox
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ImageUploadSection
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.InventoryViewModel
import com.cuso.mobile.viewmodel.SettingsViewModel
import java.util.UUID

// =============================================================================
// SCREEN DATA MODELS
// =============================================================================

data class AttributeEntry(
    val id: String = UUID.randomUUID().toString(),
    val attributeType: String = "",
    val values: List<String> = emptyList()
)

data class VariantEntry(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    var sku: String,
    var cost: String = "0",
    var price: String = "0",
    var reOrderPoint: String = "0",
    var isActive: Boolean = true,
    var isExpanded: Boolean = false
)

// =============================================================================
// MAIN COMPOSABLE
// =============================================================================

@Composable
fun CreateItemGroupScreen(
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    // ── Adaptive Tokens & UI Setup ──
    val tokens = LocalAppTokens.current
    val fieldShape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f)

    // ── Load Live Categories ──
    LaunchedEffect(Unit) {
        settingsViewModel.fetchProductCategories()
    }
    val productCategories by settingsViewModel.productCategories.collectAsState()

    // ── ViewModel State Observers ──
    val isCreating by inventoryViewModel.isCreatingItemGroup.collectAsState()
    val successMessage by inventoryViewModel.createItemGroupSuccess.collectAsState()
    val errorMessage by inventoryViewModel.createItemGroupError.collectAsState()

    var validationError by remember { mutableStateOf<String?>(null) }
    var confirmationSuccessMessage by remember { mutableStateOf<String?>(null) }
    var expandedSection by remember { mutableStateOf("Item Group Information") }

    // ── Form State 1: Item Group Information ──
    var itemGroupName by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Pieces") }
    var unitExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    // ── Form State 2: Classification ──
    var brand by remember { mutableStateOf("Select Brand") }
    var brandExpanded by remember { mutableStateOf(false) }

    // Category Selection State
    var selectedCategoryName by remember { mutableStateOf("Select Category") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var status by remember { mutableStateOf(true) }
    var itemGroupImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // ── Form State 3: Attributes Input ──
    val attributesList = remember {
        mutableStateListOf(
            AttributeEntry(attributeType = "Color", values = listOf("Blue")),
            AttributeEntry(attributeType = "Size", values = listOf("M", "L", "XL"))
        )
    }

    // Confirmed attributes snapshot
    val confirmedAttributes = remember { mutableStateListOf<AttributeEntry>() }

    // ── Form State 4: Pricing & Tax ──
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }

    // ── Form State 5: Variant Matrix ──
    var matrixMode by remember { mutableStateOf("Manual") }
    val selectedMatrixValues = remember { mutableStateMapOf<String, MutableList<String>>() }

    // ── Form State 6: Generated Variants ──
    var trackInventory by remember { mutableStateOf(true) }
    var variantSearch by remember { mutableStateOf("") }
    val variants = remember { mutableStateListOf<VariantEntry>() }

    var bulkCost by remember { mutableStateOf("") }
    var bulkPrice by remember { mutableStateOf("") }
    var bulkReorder by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            itemGroupImages = itemGroupImages + uris
        }
    }

    // =========================================================================
    // HELPER: REGENERATE VARIANTS FROM MATRIX SELECTIONS
    // =========================================================================
    fun recalculateVariants() {
        val activeAttributes = confirmedAttributes.filter {
            it.attributeType.isNotBlank() && (selectedMatrixValues[it.attributeType]?.isNotEmpty() == true)
        }

        if (activeAttributes.isEmpty()) {
            variants.clear()
            return
        }

        // Generate Cartesian product of selected values
        var combinations = listOf<List<String>>()
        activeAttributes.forEach { attribute ->
            val values = selectedMatrixValues[attribute.attributeType] ?: emptyList()
            combinations = if (combinations.isEmpty()) {
                values.map { listOf(it) }
            } else {
                combinations.flatMap { combo ->
                    values.map { value -> combo + value }
                }
            }
        }

        // Update variants state
        variants.clear()
        combinations.forEach { combo ->
            val label = combo.joinToString(" / ")
            val skuPrefix = itemGroupName.filter { it.isLetter() }.take(3).uppercase().ifBlank { "GRP" }
            val skuSuffix = combo.joinToString("-") { it.take(3).uppercase() }
            val sku = "$skuPrefix-$skuSuffix"

            variants.add(
                VariantEntry(
                    label = label,
                    sku = sku,
                    cost = costPrice.ifBlank { "0" },
                    price = sellingPrice.ifBlank { "0" },
                    reOrderPoint = "0",
                    isActive = true,
                    isExpanded = false
                )
            )
        }
    }

    // =========================================================================
    // ACTION: CONFIRM ATTRIBUTES
    // =========================================================================
    val onConfirmAttributes = {
        val validEntries = attributesList.filter {
            it.attributeType.isNotBlank() && it.values.isNotEmpty()
        }

        if (validEntries.isEmpty()) {
            validationError = "Please add at least one attribute type with values"
        } else {
            confirmedAttributes.clear()
            confirmedAttributes.addAll(validEntries)

            selectedMatrixValues.clear()
            validEntries.forEach { entry ->
                selectedMatrixValues[entry.attributeType] = entry.values.toMutableList()
            }

            recalculateVariants()
            confirmationSuccessMessage = "Attributes confirmed! Matrix and variants updated."
            expandedSection = "Variant Matrix"
        }
    }

    // =========================================================================
    // ACTION: SUBMIT ITEM GROUP
    // =========================================================================
    val onSubmit = {
        if (itemGroupName.isBlank()) {
            validationError = "Please enter an Item Group Name"
        } else if (confirmedAttributes.isEmpty()) {
            validationError = "Please confirm your attributes before saving"
        } else {
            val variantAttributes = confirmedAttributes.map { entry ->
                VariantAttributeDto(
                    name = entry.attributeType,
                    values = entry.values,
                    required = false
                )
            }

            val request = CreateItemGroupRequest(
                name = itemGroupName.trim(),
                categoryId = selectedCategoryId,
                unit = if (unit == "Select Unit") "pcs" else unit.lowercase(),
                brand = if (brand == "Select Brand") null else brand,
                shortDescription = description.takeIf { it.isNotBlank() },
                longDescription = description.takeIf { it.isNotBlank() },
                variantAttributes = variantAttributes,
                pricing = PricingDto(
                    costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                    sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0
                ),
                status = if (status) "active" else "inactive"
            )

            inventoryViewModel.createItemGroup(
                request = request,
                onSuccessCallback = onSaveSuccess
            )
        }
    }

    // =========================================================================
    // ROOT UI
    // =========================================================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            TitleBar("Create Item Group", onClose = onDismiss)
            HorizontalDivider(color = title_border, thickness = 1.dp)

            // Scrollable Form Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = tokens.buttonHeight * 2.5f)
            ) {
                // ── 1. Item Group Information ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_info),
                    title = "Item Group Information",
                    expanded = expandedSection == "Item Group Information",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Item Group Information") "" else "Item Group Information"
                    }
                ) {
                    FormLabel("Item Group Name", isRequired = true)
                    FormTextField(
                        value = itemGroupName,
                        onValueChange = { itemGroupName = it },
                        placeholder = "Enter Item Group Name"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Unit")
                    FormDropdown(
                        value = unit,
                        expanded = unitExpanded,
                        onExpandChange = { unitExpanded = it },
                        options = listOf("Pieces", "Meters", "Centimeters", "Inches", "Kilograms", "Grams", "Liters", "Millimeters", "Boxes", "Packs", "Pairs", "Dozens", "Rolls", "Bundles", "Sets"),
                        onOptionSelected = { unit = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Description")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Add detailed information about this group..."
                    )
                }

                // ── 2. Classification ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.box),
                    iconTint = Primary,
                    title = "Classification",
                    expanded = expandedSection == "Classification",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Classification") "" else "Classification"
                    }
                ) {
                    FormLabel("Brand")
                    FormDropdown(
                        value = brand,
                        expanded = brandExpanded,
                        onExpandChange = { brandExpanded = it },
                        options = listOf("Brand A", "Brand B", "Brand C"),
                        onOptionSelected = { brand = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    // ── Dynamic Live Category Dropdown ──
                    FormLabel("Category")
                    FormDropdown(
                        value = selectedCategoryName,
                        expanded = categoryExpanded,
                        onExpandChange = { categoryExpanded = it },
                        options = if (productCategories.isEmpty()) {
                            listOf("No Categories Available")
                        } else {
                            productCategories.map { it.name }
                        },
                        onOptionSelected = { categoryName ->
                            selectedCategoryName = categoryName
                            val matchedCategory = productCategories.find { it.name == categoryName }
                            selectedCategoryId = matchedCategory?.id
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    Text(
                        "Item Group Image",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))

                    ImageUploadSection(
                        isImage = false,
                        selectedImages = itemGroupImages,
                        browseText = "Browse Files",
                        onBrowseClick = { imagePickerLauncher.launch("*/*") },
                        onCameraClick = null,
                        onRemoveImage = { removedImage ->
                            itemGroupImages = itemGroupImages.filter { it != removedImage }
                        },
                        previewHeaderTitle = "ATTACHED FILES"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Status",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (status) "Active" else "Inactive",
                                fontSize = tokens.bodySmall,
                                color = if (status) Primary else TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.width(8.dp))
                            MiniSwitch(
                                checked = status,
                                onCheckedChange = { status = it }
                            )
                        }
                    }
                }

                // ── 3. Attributes (Variants) ──
                AccordionSection(
                    icon = Icons.Filled.Sell,
                    title = "Attributes (Variants)",
                    expanded = expandedSection == "Attributes (Variants)",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Attributes (Variants)") "" else "Attributes (Variants)"
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(light_grey, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Max 3 Attributes", fontSize = tokens.label, color = TextSecondary)
                        }
                        Text(
                            "+ Add Attribute",
                            fontSize = tokens.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                if (attributesList.size < 3) attributesList.add(AttributeEntry())
                            }
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Add attribute types (e.g. Color, Size) and values, then tap 'Confirm Attributes'.",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )

                    attributesList.forEachIndexed { index, entry ->
                        Spacer(Modifier.height(tokens.extraPadding))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PanelBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(tokens.cardPadding * 0.5f)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = redText,
                                        modifier = Modifier
                                            .size(tokens.iconSize * 0.9f)
                                            .clickable { attributesList.removeAt(index) }
                                    )
                                }
                                FormLabel("Attribute Type")
                                FormTextField(
                                    value = entry.attributeType,
                                    onValueChange = { attributesList[index] = entry.copy(attributeType = it) },
                                    placeholder = "e.g. Color, Size"
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Values",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Spacer(Modifier.height(6.dp))
                                AttributeValuesInput(
                                    values = entry.values,
                                    onValuesChange = { attributesList[index] = entry.copy(values = it) },
                                    tokens = tokens
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AppButton(
                            text = "Confirm Attributes",
                            onClick = onConfirmAttributes
                        )
                    }
                }

                // ── 4. Pricing & Tax ──
                AccordionSection(
                    icon = Icons.Filled.LocalOffer,
                    title = "Pricing & Tax",
                    expanded = expandedSection == "Pricing & Tax",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Pricing & Tax") "" else "Pricing & Tax"
                    }
                ) {
                    FormLabel("Cost Price (Default)")
                    FormTextField(
                        value = costPrice,
                        onValueChange = {
                            costPrice = it
                            recalculateVariants()
                        },
                        placeholder = "₹0",
                        keyboardType = KeyboardType.Number
                    )
                    Text(
                        "Default cost for all generated variants",
                        fontSize = tokens.label,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Selling Price (Default)")
                    FormTextField(
                        value = sellingPrice,
                        onValueChange = {
                            sellingPrice = it
                            recalculateVariants()
                        },
                        placeholder = "₹0",
                        keyboardType = KeyboardType.Number
                    )
                    Text(
                        "Default selling price for all generated variants",
                        fontSize = tokens.label,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // ── 5. Variant Matrix ──
                AccordionSection(
                    icon = Icons.Filled.CreditCard,
                    title = "Variant Matrix",
                    expanded = expandedSection == "Variant Matrix",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Variant Matrix") "" else "Variant Matrix"
                    }
                ) {
                    if (confirmedAttributes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = tokens.extraPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Please add and confirm attributes in the section above to configure matrix.",
                                fontSize = tokens.bodySmall,
                                color = TextSecondary
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderGray, fieldShape)
                                .padding(3.dp)
                        ) {
                            listOf("Manual", "Auto All").forEach { option ->
                                val isSelected = matrixMode == option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) background_light_purple else Color.Transparent,
                                            RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                        )
                                        .clickable {
                                            matrixMode = option
                                            if (option == "Auto All") {
                                                confirmedAttributes.forEach { attr ->
                                                    selectedMatrixValues[attr.attributeType] = attr.values.toMutableList()
                                                }
                                                recalculateVariants()
                                            }
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        option,
                                        color = if (isSelected) Primary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = tokens.bodySmall
                                    )
                                }
                            }
                        }

                        confirmedAttributes.forEach { attribute ->
                            val currentSelected = selectedMatrixValues[attribute.attributeType] ?: mutableListOf()

                            Spacer(Modifier.height(tokens.extraPadding))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    attribute.attributeType,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Row {
                                    Text(
                                        "Select All",
                                        fontSize = tokens.caption,
                                        color = Primary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            selectedMatrixValues[attribute.attributeType] = attribute.values.toMutableList()
                                            recalculateVariants()
                                        }
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Clear All",
                                        fontSize = tokens.caption,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            selectedMatrixValues[attribute.attributeType] = mutableListOf()
                                            recalculateVariants()
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                attribute.values.forEach { value ->
                                    val isChecked = currentSelected.contains(value)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AppCheckbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val list = selectedMatrixValues[attribute.attributeType] ?: mutableListOf()
                                                if (checked) {
                                                    if (!list.contains(value)) list.add(value)
                                                } else {
                                                    list.remove(value)
                                                }
                                                selectedMatrixValues[attribute.attributeType] = ArrayList(list)
                                                recalculateVariants()
                                            }
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(value, fontSize = tokens.bodySmall, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 6. Generated Variants ──
                AccordionSection(
                    title = "Generated Variants (${variants.size})",
                    expanded = expandedSection == "Generated Variants",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Generated Variants") "" else "Generated Variants"
                    }
                ) {
                    if (variants.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = tokens.extraPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No variants generated. Please confirm attributes and select values in the matrix.",
                                fontSize = tokens.bodySmall,
                                color = TextSecondary
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppCheckbox(
                                    checked = trackInventory,
                                    onCheckedChange = { trackInventory = it }
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Track inventory for this group", fontSize = tokens.bodySmall, color = TextPrimary)
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.fieldHeight)
                                .background(light_grey, RoundedCornerShape(tokens.cardCornerRadius * 0.55f))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = mutedText,
                                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                                )
                                Spacer(Modifier.width(8.dp))
                                BasicTextField(
                                    value = variantSearch,
                                    onValueChange = { variantSearch = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    decorationBox = { inner ->
                                        if (variantSearch.isEmpty()) {
                                            Text("Search generated variants...", fontSize = tokens.bodySmall, color = mutedText)
                                        }
                                        inner()
                                    }
                                )
                            }
                        }

                        val filteredVariants = if (variantSearch.isBlank()) {
                            variants
                        } else {
                            variants.filter { it.label.contains(variantSearch, ignoreCase = true) || it.sku.contains(variantSearch, ignoreCase = true) }
                        }

                        filteredVariants.forEachIndexed { index, variant ->
                            Spacer(Modifier.height(14.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PanelBg, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { variants[index] = variant.copy(isExpanded = !variant.isExpanded) }
                                        .padding(tokens.cardPadding * 0.5f),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        variant.label,
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        if (variant.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }

                                AnimatedVisibility(visible = variant.isExpanded) {
                                    Column(modifier = Modifier.padding(horizontal = tokens.cardPadding * 0.5f, vertical = 8.dp)) {
                                        FormLabel("SKU")
                                        FormTextField(
                                            value = variant.sku,
                                            onValueChange = { variants[index] = variant.copy(sku = it) },
                                            placeholder = "SKU"
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Cost")
                                        FormTextField(
                                            value = variant.cost,
                                            onValueChange = { variants[index] = variant.copy(cost = it) },
                                            placeholder = "₹0",
                                            keyboardType = KeyboardType.Number
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Price")
                                        FormTextField(
                                            value = variant.price,
                                            onValueChange = { variants[index] = variant.copy(price = it) },
                                            placeholder = "₹0",
                                            keyboardType = KeyboardType.Number
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Re-Order Point")
                                        FormTextField(
                                            value = variant.reOrderPoint,
                                            onValueChange = { variants[index] = variant.copy(reOrderPoint = it) },
                                            placeholder = "0",
                                            keyboardType = KeyboardType.Number
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                MiniSwitch(
                                                    checked = variant.isActive,
                                                    onCheckedChange = { variants[index] = variant.copy(isActive = it) }
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    if (variant.isActive) "Active" else "Inactive",
                                                    fontSize = tokens.bodySmall,
                                                    color = Primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable { variants.removeAt(index) }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Delete,
                                                    contentDescription = null,
                                                    tint = redText,
                                                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "Delete Variant",
                                                    fontSize = tokens.bodySmall,
                                                    color = redText,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }
                        }

                        // Bulk update section
                        Spacer(Modifier.height(tokens.extraPadding))
                        Text(
                            "Bulk Update",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Cost")
                                FormTextField(
                                    value = bulkCost,
                                    onValueChange = { bulkCost = it },
                                    placeholder = "₹0",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Price")
                                FormTextField(
                                    value = bulkPrice,
                                    onValueChange = { bulkPrice = it },
                                    placeholder = "₹0",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Re-Order Point")
                                FormTextField(
                                    value = bulkReorder,
                                    onValueChange = { bulkReorder = it },
                                    placeholder = "0",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AppButton(
                                onClick = {
                                    variants.forEachIndexed { i, v ->
                                        variants[i] = v.copy(
                                            cost = bulkCost.ifBlank { v.cost },
                                            price = bulkPrice.ifBlank { v.price },
                                            reOrderPoint = bulkReorder.ifBlank { v.reOrderPoint }
                                        )
                                    }
                                },
                                text = "Apply to All Variants"
                            )
                        }
                    }
                }
            }
        }

        // ── Floating Action Buttons ──
        StepNavigationFab(
            showBack = true,
            onBack = onDismiss,
            showBackArrow = false,
            backLabel = "Cancel",
            trailingAction = TrailingFabAction.Update(
                label = if (isCreating) "Saving..." else "Save Item Group",
                onClick = { if (!isCreating) onSubmit() }
            )
        )

        // ── Dynamic Island Notifications ──
        DynamicIslandSuccess(
            message = successMessage ?: confirmationSuccessMessage,
            onDismiss = {
                inventoryViewModel.clearItemGroupAlerts()
                confirmationSuccessMessage = null
            }
        )

        DynamicIslandError(
            message = errorMessage ?: validationError,
            onDismiss = {
                inventoryViewModel.clearItemGroupAlerts()
                validationError = null
            }
        )
    }
}

// =============================================================================
// ATTRIBUTE VALUES CHIP INPUT
// =============================================================================

@SuppressLint("RememberInComposition")
@Composable
private fun AttributeValuesInput(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    tokens: AppDesignTokens
) {
    var inputText by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
            .padding(8.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalSpacing = 6.dp,
            verticalSpacing = 6.dp
        ) {
            values.forEach { value ->
                Box(
                    modifier = Modifier
                        .background(primary_light, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            value,
                            fontSize = tokens.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove",
                            tint = Primary,
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { onValuesChange(values - value) }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(
                        if (isFocused) whiteBg else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .width(120.dp)
                        .focusRequester(FocusRequester()),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = tokens.bodySmall,
                        color = TextPrimary
                    ),
                    decorationBox = { inner ->
                        if (inputText.isEmpty() && !isFocused) {
                            Text(
                                "Add..",
                                fontSize = tokens.bodySmall,
                                color = mutedText
                            )
                        }
                        inner()
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputText.isNotBlank()) {
                                onValuesChange(values + inputText.trim())
                                inputText = ""
                            }
                        }
                    )
                )
            }
        }
    }
}

// =============================================================================
// FLOW ROW LAYOUT HELPER
// =============================================================================

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val hSpacing = horizontalSpacing.roundToPx()
        val vSpacing = verticalSpacing.roundToPx()

        var currentX = 0
        var currentY = 0
        var rowMaxHeight = 0
        val placeables = measurables.map { it.measure(constraints) }
        val rows = mutableListOf<List<Placeable>>()
        val rowHeights = mutableListOf<Int>()
        var currentRow = mutableListOf<Placeable>()

        placeables.forEach { placeable ->
            if (currentX + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                rowHeights.add(rowMaxHeight)
                currentY += rowMaxHeight + vSpacing
                currentX = 0
                rowMaxHeight = 0
                currentRow = mutableListOf()
            }
            currentRow.add(placeable)
            currentX += placeable.width + hSpacing
            if (placeable.height > rowMaxHeight) rowMaxHeight = placeable.height
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowHeights.add(rowMaxHeight)
        }

        layout(constraints.maxWidth, currentY + (rowHeights.lastOrNull() ?: 0)) {
            var y = 0
            rows.forEachIndexed { index, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hSpacing
                }
                y += rowHeights[index] + vSpacing
            }
        }
    }
}