@file:Suppress("UNUSED_PARAMETER", "unused","AssignedValueIsNeverRead","unusedVariable","VariableNeverRead")


package com.cuso.tailor.view.home.inventory.items.item_groups

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.CreateItemGroupRequest
import com.cuso.tailor.model.inventory.PricingDto
import com.cuso.tailor.model.inventory.VariantAttributeDto
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.background_light_purple
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.view.composable.AccordionSection
import com.cuso.tailor.view.composable.AppButton
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.AppRadioButton
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ImageUploadSection
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel
import java.util.UUID
import androidx.core.net.toUri
import com.cuso.tailor.utils.formatCurrency
import com.cuso.tailor.view.composable.AttributeTypeSelector
import com.cuso.tailor.view.composable.AttributeValueSelector


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
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        settingsViewModel.fetchProductCategories()
        inventoryViewModel.fetchTaxGroups()
    }
    val productCategories by settingsViewModel.productCategories.collectAsState()
    val taxGroups by inventoryViewModel.taxGroups.collectAsState()
    val editDetail by inventoryViewModel.selectedItemGroupDetail.collectAsState()

    val isCreating by inventoryViewModel.isCreatingItemGroup.collectAsState()
    val successMessage by inventoryViewModel.createItemGroupSuccess.collectAsState()
    val errorMessage by inventoryViewModel.createItemGroupError.collectAsState()

    var validationError by remember { mutableStateOf<String?>(null) }
    var confirmationSuccessMessage by remember { mutableStateOf<String?>(null) }
    var expandedSection by remember { mutableStateOf("Item Group Information") }

    val isEditMode = editDetail != null

    // Display label -> backend enum value mapping matching React ItemGroupForm
    val unitsList = remember {
        listOf(
            "Pieces (Pcs)" to "Pieces",
            "Meters (m)" to "Meters",
            "Centimeters (cm)" to "Centimeters",
            "Inches (in)" to "Inches",
            "Kilograms (kg)" to "Kilograms",
            "Grams (g)" to "Grams",
            "Liters (L)" to "Liters",
            "Milliliters (ml)" to "Milliliters",
            "Boxes" to "Boxes",
            "Packs" to "Packs",
            "Pairs" to "Pairs",
            "Dozens" to "Dozens",
            "Rolls" to "Rolls",
            "Bundles" to "Bundles",
            "Sets" to "Sets"
        )
    }

    // Form field states aligned with React ItemGroupForm
    var itemGroupName by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("goods") } // "goods" | "service"
    var hsnCode by remember { mutableStateOf("") }
    var sacCode by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Select Unit") }
    var unitExpanded by remember { mutableStateOf(false) }
    var isFabric by remember { mutableStateOf(false) }
    var fabricType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var manufacturer by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("Select Category") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(true) } // true -> "active", false -> "draft"
    var itemGroupImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var taxPreference by remember { mutableStateOf("taxable") } // "taxable" | "non_taxable"
    var selectedTaxGroupId by remember { mutableStateOf("") }
    var taxGroupExpanded by remember { mutableStateOf(false) }

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

    // Calculates all variant permutations based on selected matrix values
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

    // Prefill form states when editDetail changes
    LaunchedEffect(editDetail) {
        editDetail?.let { detail ->
            itemGroupName = detail.name
            // Match backend enum value to display label in unitsList
            unit = unitsList.find {
                it.second.equals(detail.unit, ignoreCase = true)
            }?.first ?: detail.unit?.takeIf { it.isNotBlank() } ?: "Select Unit"
            brand = detail.brand.orEmpty()
            description = detail.longDescription ?: detail.shortDescription.orEmpty()
            status = detail.status.equals("active", ignoreCase = true)
            selectedCategoryId = detail.categoryId

            costPrice = (detail.pricingTax?.costPrice ?: detail.pricing?.costPrice)?.takeIf { it > 0 }?.toString() ?: ""
            sellingPrice = (detail.pricingTax?.sellingPrice ?: detail.pricing?.sellingPrice)?.takeIf { it > 0 }?.toString() ?: ""

            // Populate images from API response
            val remoteImages = detail.media?.images?.mapNotNull { img ->
                img.fileUrl.takeIf { it.isNotBlank() }?.toUri()
            } ?: emptyList()
            itemGroupImages = remoteImages

            // Populate variant attributes
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

            // Populate existing variants
            variants.clear()
            if (detail.variants.isNotEmpty()) {
                detail.variants.forEach { v ->
                    variants.add(
                        VariantEntry(
                            id = v.id,
                            label = v.variantLabel ?: v.name,
                            sku = v.sku,
                            cost = if (v.costPrice > 0) v.costPrice.toString() else costPrice.ifBlank { "0" },
                            price = if (v.sellingPrice > 0) v.sellingPrice.toString() else sellingPrice.ifBlank { "0" },
                            reOrderPoint = v.reorderLevel.toString(),
                            isActive = v.status.equals("active", ignoreCase = true),
                            isExpanded = false
                        )
                    )
                }
            }

            // Automatically generate variants if API returned empty variants list
            if (variants.isEmpty() && confirmedAttributes.isNotEmpty()) {
                recalculateVariants()
            }
        }
    }

    // Resolve matching category title
    LaunchedEffect(selectedCategoryId, productCategories) {
        selectedCategoryId?.let { catId ->
            val match = productCategories.find { it.id == catId }
            if (match != null) {
                selectedCategoryName = match.name
            }
        }
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

            val validTypes = validEntries.map { it.attributeType }.toSet()
            selectedMatrixValues.keys.retainAll(validTypes)

            recalculateVariants()
            confirmationSuccessMessage = "Attributes confirmed! Variant Matrix & Generated list updated."
            expandedSection = "Variant Matrix"
        }
    }

    val onSubmit = {
        val resolvedUnitEnum = unitsList.find { it.first == unit }?.second
            ?: unit.takeIf { it != "Select Unit" }

        if (itemGroupName.isBlank()) {
            validationError = "Item Group Name is required"
        } else if (resolvedUnitEnum.isNullOrBlank()) {
            validationError = "Unit is required"
        } else if (selectedCategoryId.isNullOrBlank()) {
            validationError = "Category is required"
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
                unit = resolvedUnitEnum,
                brand = brand.trim().takeIf { it.isNotBlank() },
                shortDescription = description.takeIf { it.isNotBlank() },
                longDescription = description.takeIf { it.isNotBlank() },
                variantAttributes = variantAttributes,
                pricing = PricingDto(
                    costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                    sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0
                ),
                status = if (status) "active" else "draft"
            )

            if (isEditMode && editDetail != null) {
                inventoryViewModel.updateItemGroup(
                    context = context,
                    id = editDetail!!.id,
                    request = request,
                    imagesUris = itemGroupImages,
                    onSuccessCallback = {
                        inventoryViewModel.clearSelectedItemGroupDetail()
                        onSaveSuccess()
                    }
                )
            } else {
                inventoryViewModel.createItemGroup(
                    context = context,
                    request = request,
                    imagesUris = itemGroupImages,
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
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = if (isEditMode) "Edit Item Group" else "Create Item Group",
                onClose = handleDismiss
            )
            HorizontalDivider(color = title_border, thickness = 2.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = tokens.buttonHeight * 2.5f)
            ) {
                // Section 1: Item Group Information
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
                        placeholder = "e.g., Premium Cotton Twill"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Item Type", isRequired = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(3.dp)
                    ) {
                        listOf("Goods" to "goods", "Service" to "service").forEach { (label, value) ->
                            val isSelected = itemType == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) background_light_purple else Color.Transparent,
                                        RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                    )
                                    .clickable { itemType = value }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Primary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = tokens.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    if (itemType == "goods") {
                        FormLabel("HSN Code")
                        FormTextField(
                            value = hsnCode,
                            onValueChange = { hsnCode = it },
                            placeholder = "e.g. 8471"
                        )
                    } else {
                        FormLabel("SAC Code")
                        FormTextField(
                            value = sacCode,
                            onValueChange = { sacCode = it },
                            placeholder = "e.g. 9983"
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Unit", isRequired = true)
                    FormDropdown(
                        value = unit,
                        expanded = unitExpanded,
                        onExpandChange = { unitExpanded = it },
                        options = unitsList.map { it.first },
                        onOptionSelected = { unit = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Fabric")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(3.dp)
                    ) {
                        listOf("No" to false, "Yes" to true).forEach { (label, value) ->
                            val isSelected = isFabric == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) background_light_purple else Color.Transparent,
                                        RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                    )
                                    .clickable { isFabric = value }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Primary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = tokens.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Fabric Type")
                    FormTextField(
                        value = fabricType,
                        onValueChange = { fabricType = it },
                        placeholder = "e.g. Cotton, Silk, Polyester"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Description")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Add detailed information about this fabric or garment group..."
                    )
                }

                // Section 2: Classification
                AccordionSection(
                    iconPainter = painterResource(R.drawable.box),
                    iconTint = Primary,
                    title = "Classification",
                    expanded = expandedSection == "Classification",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Classification") "" else "Classification"
                    }
                ) {
                    FormLabel("Category", isRequired = true)
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
                    FormLabel("Manufacturer")
                    FormTextField(
                        value = manufacturer,
                        onValueChange = { manufacturer = it },
                        placeholder = "e.g. ABC Textiles Pvt Ltd"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Brand")
                    FormTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        placeholder = "e.g. Raymond"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    Text("Item Group Image", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))

                    ImageUploadSection(
                        isImage = true,
                        selectedImages = itemGroupImages,
                        browseText = "Browse Images",
                        onBrowseClick = { imagePickerLauncher.launch("image/*") },
                        onCameraClick = null,
                        onRemoveImage = { removedImage ->
                            itemGroupImages = itemGroupImages.filter { it != removedImage }
                        },
                        previewHeaderTitle = "ATTACHED IMAGES"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Status", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("Visibility state", fontSize = tokens.label, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (status) "Active" else "Draft",
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

                // Section 3: Attributes (Variants)
                AccordionSection(
                    icon = Icons.Filled.Sell,
                    title = "Attributes (Variants)",
                    expanded = expandedSection == "Attributes (Variants)",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Attributes (Variants)") "" else "Attributes (Variants)"
                    }
                ) {
                    Text(
                        "Select the attribute types you want, add values, then tap 'Confirm Attributes'.",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(14.dp))
                    FormLabel("Attribute Type")
                    AttributeTypeSelector(
                        selectedTypes = attributesList.map { it.attributeType }.toSet(),
                        onTypeToggled = { type, isChecked ->
                            if (isChecked) {
                                if (attributesList.none { it.attributeType.equals(type, ignoreCase = true) }) {
                                    attributesList.add(AttributeEntry(attributeType = type))
                                }
                            } else {
                                attributesList.removeAll { it.attributeType.equals(type, ignoreCase = true) }
                            }
                        },
                        tokens = tokens
                    )

                    attributesList.forEachIndexed { index, entry ->
                        Spacer(Modifier.height(tokens.extraPadding))
                        Column {
                            Text("${entry.attributeType} Values", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextSecondary)
                            Spacer(Modifier.height(6.dp))
                            AttributeValueSelector(
                                attributeType = entry.attributeType,
                                values = entry.values,
                                onValuesChange = { newValues -> attributesList[index] = entry.copy(values = newValues) },
                                tokens = tokens
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        AppButton(text = "Confirm Attributes", onClick = onConfirmAttributes)
                    }
                }

                // Section 4: Pricing & Tax
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
                        placeholder = "${formatCurrency()}0",
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
                        placeholder = "${formatCurrency()}0",
                        keyboardType = KeyboardType.Number
                    )
                    Text("Default selling price for all generated variants", fontSize = tokens.label, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Tax Preference")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { taxPreference = "taxable" }
                        ) {
                            AppRadioButton(
                                selected = taxPreference == "taxable",
                                onClick = { taxPreference = "taxable" }
                            )
                            Text("Taxable", fontSize = tokens.bodySmall, color = TextPrimary)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                taxPreference = "non_taxable"
                                selectedTaxGroupId = ""
                            }
                        ) {
                            AppRadioButton(
                                selected = taxPreference == "non_taxable",
                                onClick = {
                                    taxPreference = "non_taxable"
                                    selectedTaxGroupId = ""
                                }
                            )
                            Text("Non-Taxable", fontSize = tokens.bodySmall, color = TextPrimary)
                        }
                    }

                    // Show Tax Rate (GST) dropdown only when Taxable is selected
                    if (taxPreference == "taxable") {
                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Tax Rate (GST)")
                        val taxGroupOptions = remember(taxGroups) {
                            taxGroups.map { tg -> "${tg.name} — ${tg.totalRate}%" }
                        }
                        val selectedTaxGroupLabel = remember(selectedTaxGroupId, taxGroups) {
                            taxGroups.find { it.id == selectedTaxGroupId }
                                ?.let { "${it.name} — ${it.totalRate}%" }
                                ?: "Select Tax Group"
                        }
                        FormDropdown(
                            value = selectedTaxGroupLabel,
                            expanded = taxGroupExpanded,
                            onExpandChange = { taxGroupExpanded = it },
                            options = taxGroupOptions.ifEmpty { listOf("No Tax Groups Available") },
                            onOptionSelected = { chosenLabel ->
                                val matched = taxGroups.find { "${it.name} — ${it.totalRate}%" == chosenLabel }
                                selectedTaxGroupId = matched?.id.orEmpty()
                            }
                        )
                    }
                }

                // Section 5: Variant Matrix
                @OptIn(ExperimentalLayoutApi::class)
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
                                text = "Please add and confirm attributes above to configure matrix.",
                                fontSize = tokens.bodySmall,
                                color = TextSecondary
                            )
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
                                        text = option,
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
                                    text = attribute.attributeType,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Row {
                                    Text(
                                        text = "Select All",
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
                                        text = "Clear All",
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

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                attribute.values.forEach { value ->
                                    val isChecked = currentSelected.contains(value)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
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
                                        Text(
                                            text = value,
                                            fontSize = tokens.bodySmall,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 6: Generated Variants
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
                                            placeholder = "${formatCurrency()}0",
                                            keyboardType = KeyboardType.Number
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Price")
                                        FormTextField(
                                            value = variant.price,
                                            onValueChange = {
                                                if (actualIndex != -1) variants[actualIndex] = variant.copy(price = it)
                                            },
                                            placeholder = "${formatCurrency()}0",
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
                                FormTextField(value = bulkCost, onValueChange = { bulkCost = it }, placeholder = "${formatCurrency()}0", keyboardType = KeyboardType.Number)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Price")
                                FormTextField(value = bulkPrice, onValueChange = { bulkPrice = it }, placeholder = "${formatCurrency()}0", keyboardType = KeyboardType.Number)
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
    }
}
