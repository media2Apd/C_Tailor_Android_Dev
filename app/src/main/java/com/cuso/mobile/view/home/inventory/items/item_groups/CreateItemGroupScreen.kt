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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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

@Composable
fun CreateItemGroupScreen(
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    LaunchedEffect(Unit) {
        settingsViewModel.fetchProductCategories()
    }
    val productCategories by settingsViewModel.productCategories.collectAsState()
    val editDetail by inventoryViewModel.selectedItemGroupDetail.collectAsState()

    val isCreating by inventoryViewModel.isCreatingItemGroup.collectAsState()
    val successMessage by inventoryViewModel.createItemGroupSuccess.collectAsState()
    val errorMessage by inventoryViewModel.createItemGroupError.collectAsState()

    var validationError by remember { mutableStateOf<String?>(null) }
    var confirmationSuccessMessage by remember { mutableStateOf<String?>(null) }
    var expandedSection by remember { mutableStateOf("Item Group Information") }

    val isEditMode = editDetail != null

    // Form Field States
    var itemGroupName by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Select Piece") }
    var unitExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("Select Brand") }
    var brandExpanded by remember { mutableStateOf(false) }
    var selectedCategoryName by remember { mutableStateOf("Select Category") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(true) }
    var itemGroupImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val attributesList = remember { mutableStateListOf<AttributeEntry>() }
    val confirmedAttributes = remember { mutableStateListOf<AttributeEntry>() }

    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var matrixMode by remember { mutableStateOf("Manual") }
    val selectedMatrixValues = remember { mutableStateMapOf<String, MutableList<String>>() }

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

    // Prefill data for Edit Mode
    LaunchedEffect(editDetail) {
        editDetail?.let { detail ->
            itemGroupName = detail.name
            unit = detail.unit ?: "Pieces"
            brand = detail.brand ?: "Select Brand"
            description = detail.longDescription ?: detail.shortDescription.orEmpty()
            status = detail.status.equals("active", ignoreCase = true)
            selectedCategoryId = detail.categoryId

            costPrice = (detail.pricingTax?.costPrice ?: detail.pricing?.costPrice)?.takeIf { it > 0 }?.toString() ?: ""
            sellingPrice = (detail.pricingTax?.sellingPrice ?: detail.pricing?.sellingPrice)?.takeIf { it > 0 }?.toString() ?: ""

            attributesList.clear()
            confirmedAttributes.clear()
            selectedMatrixValues.clear()

            detail.variantAttributes.forEach { attr ->
                val entry = AttributeEntry(
                    attributeType = attr.name,
                    values = attr.values
                )
                attributesList.add(entry)
                confirmedAttributes.add(entry)
                selectedMatrixValues[attr.name] = attr.values.toMutableList()
            }

            variants.clear()
            detail.variants.forEach { v ->
                variants.add(
                    VariantEntry(
                        id = v.id,
                        label = v.variantLabel ?: v.name,
                        sku = v.sku,
                        cost = if (v.costPrice > 0) v.costPrice.toString() else "0",
                        price = if (v.sellingPrice > 0) v.sellingPrice.toString() else "0",
                        reOrderPoint = v.reorderLevel.toString(),
                        isActive = v.status.equals("active", ignoreCase = true),
                        isExpanded = false
                    )
                )
            }
        }
    }

    // Match category display name
    LaunchedEffect(selectedCategoryId, productCategories) {
        selectedCategoryId?.let { catId ->
            val match = productCategories.find { it.id == catId }
            if (match != null) {
                selectedCategoryName = match.name
            }
        }
    }

    fun recalculateVariants() {
        val activeAttributes = confirmedAttributes.filter {
            it.attributeType.isNotBlank() && (selectedMatrixValues[it.attributeType]?.isNotEmpty() == true)
        }

        if (activeAttributes.isEmpty()) {
            variants.clear()
            return
        }

        var combinations = listOf<List<String>>()
        activeAttributes.forEach { attribute ->
            val values = selectedMatrixValues[attribute.attributeType] ?: emptyList()
            if (values.isNotEmpty()) {
                combinations = if (combinations.isEmpty()) {
                    values.map { listOf(it) }
                } else {
                    combinations.flatMap { combo ->
                        values.map { value -> combo + value }
                    }
                }
            }
        }

        val existingVariantsMap = variants.associateBy { it.label }
        val newVariants = mutableListOf<VariantEntry>()

        combinations.forEach { combo ->
            val label = combo.joinToString(" / ")
            val existing = existingVariantsMap[label]

            if (existing != null) {
                newVariants.add(existing)
            } else {
                val skuPrefix = itemGroupName.filter { it.isLetter() }.take(3).uppercase().ifBlank { "GRP" }
                val skuSuffix = combo.joinToString("-") { it.take(3).uppercase() }
                val sku = "$skuPrefix-$skuSuffix"

                newVariants.add(
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

        variants.clear()
        variants.addAll(newVariants)
    }

    val onConfirmAttributes = {
        val validEntries = attributesList.filter {
            it.attributeType.isNotBlank() && it.values.isNotEmpty()
        }

        if (validEntries.isEmpty()) {
            validationError = "Please add at least one attribute type with values"
        } else {
            confirmedAttributes.clear()
            confirmedAttributes.addAll(validEntries)

            validEntries.forEach { entry ->
                val current = selectedMatrixValues[entry.attributeType] ?: mutableListOf()
                val mergedValues = (current + entry.values).distinct().toMutableList()
                selectedMatrixValues[entry.attributeType] = mergedValues
            }

            // Remove unconfirmed attributes from matrix
            val validTypes = validEntries.map { it.attributeType }.toSet()
            selectedMatrixValues.keys.retainAll(validTypes)

            recalculateVariants()
            confirmationSuccessMessage = "Attributes confirmed! Variant Matrix & Generated list updated."
            expandedSection = "Variant Matrix"
        }
    }

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

            if (isEditMode && editDetail != null) {
                inventoryViewModel.updateItemGroup(
                    id = editDetail!!.id,
                    request = request,
                    onSuccessCallback = {
                        inventoryViewModel.clearSelectedItemGroupDetail()
                        onSaveSuccess()
                    }
                )
            } else {
                inventoryViewModel.createItemGroup(
                    request = request,
                    onSuccessCallback = {
                        inventoryViewModel.clearSelectedItemGroupDetail()
                        onSaveSuccess()
                    }
                )
            }
        }
    }

    val handleDismiss = {
        inventoryViewModel.clearSelectedItemGroupDetail()
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            TitleBar(
                title = if (isEditMode) "Edit Item Group" else "Create Item Group",
                onClose = handleDismiss
            )
            HorizontalDivider(color = title_border, thickness = 1.dp)

            // Scrollable Sections
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
                        options = listOf("Pieces", "Meters", "Centimeters", "Inches", "Kilograms", "Grams", "Liters", "Boxes", "Packs", "Pairs", "Rolls"),
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
                        options = listOf("Rajasthani", "Brand A", "Brand B", "Brand C"),
                        onOptionSelected = { brand = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Category")
                    FormDropdown(
                        value = selectedCategoryName,
                        expanded = categoryExpanded,
                        onExpandChange = { categoryExpanded = it },
                        options = if (productCategories.isEmpty()) listOf("No Categories Available") else productCategories.map { it.name },
                        onOptionSelected = { categoryName ->
                            selectedCategoryName = categoryName
                            val matched = productCategories.find { it.name == categoryName }
                            selectedCategoryId = matched?.id
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    Text("Item Group Image", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextSecondary)
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
                        Text("Status", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
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
                        modifier = Modifier.fillMaxWidth().background(whiteBg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(whiteBg, RoundedCornerShape(6.dp))
                                .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
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
                        "Add attribute types and values, then tap 'Confirm Attributes'.",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )

                    attributesList.forEachIndexed { index, entry ->
                        Spacer(Modifier.height(tokens.extraPadding))
                        Box(modifier = Modifier.fillMaxWidth()) {
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
                                Text("Values", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextSecondary)
                                Spacer(Modifier.height(6.dp))
                                AttributeValuesInput(
                                    values = entry.values,
                                    onValuesChange = { newValues ->
                                        attributesList[index] = entry.copy(values = newValues)
                                    },
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
                    Text("Default cost for all generated variants", fontSize = tokens.label, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

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
                    Text("Default selling price for all generated variants", fontSize = tokens.label, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
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
                            Text("Please add and confirm attributes above to configure matrix.", fontSize = tokens.bodySmall, color = TextSecondary)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(3.dp)
                        ) {
                            listOf("Manual", "Auto All").forEach { option ->
                                val isSelected = matrixMode == option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) background_light_purple else Color.Transparent, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
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
                                    Text(option, color = if (isSelected) Primary else TextSecondary, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium, fontSize = tokens.bodySmall)
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
                                Text(attribute.attributeType, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Row {
                                    Text("Select All", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium, modifier = Modifier.clickable {
                                        selectedMatrixValues[attribute.attributeType] = attribute.values.toMutableList()
                                        recalculateVariants()
                                    })
                                    Spacer(Modifier.width(12.dp))
                                    Text("Clear All", fontSize = tokens.caption, color = TextSecondary, fontWeight = FontWeight.Medium, modifier = Modifier.clickable {
                                        selectedMatrixValues[attribute.attributeType] = mutableListOf()
                                        recalculateVariants()
                                    })
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
                            Text("No variants generated. Please confirm attributes in the matrix.", fontSize = tokens.bodySmall, color = TextSecondary)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppCheckbox(checked = trackInventory, onCheckedChange = { trackInventory = it })
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
                                Icon(Icons.Filled.Search, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.9f))
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
                            val actualIndex = variants.indexOf(variant)
                            Spacer(Modifier.height(14.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PanelBg, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (actualIndex != -1) {
                                                variants[actualIndex] = variant.copy(isExpanded = !variant.isExpanded)
                                            }
                                        }
                                        .padding(tokens.cardPadding * 0.5f),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(variant.label, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
                                            onValueChange = {
                                                if (actualIndex != -1) variants[actualIndex] = variant.copy(sku = it)
                                            },
                                            placeholder = "SKU"
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Cost")
                                        FormTextField(
                                            value = variant.cost,
                                            onValueChange = {
                                                if (actualIndex != -1) variants[actualIndex] = variant.copy(cost = it)
                                            },
                                            placeholder = "₹0",
                                            keyboardType = KeyboardType.Number
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Price")
                                        FormTextField(
                                            value = variant.price,
                                            onValueChange = {
                                                if (actualIndex != -1) variants[actualIndex] = variant.copy(price = it)
                                            },
                                            placeholder = "₹0",
                                            keyboardType = KeyboardType.Number
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Re-Order Point")
                                        FormTextField(
                                            value = variant.reOrderPoint,
                                            onValueChange = {
                                                if (actualIndex != -1) variants[actualIndex] = variant.copy(reOrderPoint = it)
                                            },
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
                                                    onCheckedChange = {
                                                        if (actualIndex != -1) variants[actualIndex] = variant.copy(isActive = it)
                                                    }
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
                                                modifier = Modifier.clickable {
                                                    if (actualIndex != -1) variants.removeAt(actualIndex)
                                                }
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = null, tint = redText, modifier = Modifier.size(tokens.iconSize * 0.8f))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Delete Variant", fontSize = tokens.bodySmall, color = redText, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                        Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))
                        Text("Bulk Update", fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Cost")
                                FormTextField(value = bulkCost, onValueChange = { bulkCost = it }, placeholder = "₹0", keyboardType = KeyboardType.Number)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Price")
                                FormTextField(value = bulkPrice, onValueChange = { bulkPrice = it }, placeholder = "₹0", keyboardType = KeyboardType.Number)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Re-Order Point")
                                FormTextField(value = bulkReorder, onValueChange = { bulkReorder = it }, placeholder = "0", keyboardType = KeyboardType.Number)
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

        // Floating Action Buttons (Save vs Update)
        StepNavigationFab(
            showBack = true,
            onBack = handleDismiss,
            showBackArrow = false,
            backLabel = "Cancel",
            trailingAction = TrailingFabAction.Update(
                label = if (isCreating) "Saving..." else if (isEditMode) "Update Item Group" else "Save Item Group",
                onClick = { if (!isCreating) onSubmit() }
            )
        )

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
// ATTRIBUTE VALUES CHIP INPUT (WRAPS CONTENT TIGHTLY)
// =============================================================================

@Composable
private fun AttributeValuesInput(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    tokens: AppDesignTokens
) {
    var inputText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .border(
                width = 1.dp,
                color = BorderGray,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalSpacing = 6.dp,
            verticalSpacing = 6.dp
        ) {
            values.forEach { value ->
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(
                            color = background_light_purple,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Primary.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = value,
                            fontSize = tokens.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove",
                            tint = Primary.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onValuesChange(values - value) }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.widthIn(min = 45.dp, max = 80.dp),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = tokens.bodySmall,
                        color = TextPrimary
                    ),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Add..",
                                    fontSize = tokens.bodySmall,
                                    color = mutedText
                                )
                            }
                            innerTextField()
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputText.isNotBlank()) {
                                val trimmed = inputText.trim()
                                if (!values.contains(trimmed)) {
                                    onValuesChange(values + trimmed)
                                }
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
// FLOW ROW HELPER
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

        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }

        val rows = mutableListOf<List<Placeable>>()
        val rowHeights = mutableListOf<Int>()
        var currentRow = mutableListOf<Placeable>()
        var currentX = 0
        var currentY = 0
        var rowMaxHeight = 0

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

        val totalHeight = currentY + (rowHeights.lastOrNull() ?: 0)

        layout(constraints.maxWidth, totalHeight) {
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