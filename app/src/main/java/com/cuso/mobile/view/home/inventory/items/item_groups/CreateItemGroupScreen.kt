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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.light_grey
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.AppButton
import com.cuso.mobile.view.composable.AppCheckbox
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ImageUploadSection
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import java.util.UUID

// ── Design tokens ──
private val AccentColor = Color(0xFF4F39F6)
private val BorderColor = Color(0xFFE3E4E8)
private val LabelColor = Color(0xFF6B7280)
private val TitleColor = Color(0xFF111827)
private val ChipBg = Color(0xFFEDE9FE)
private val ChipText = Color(0xFF4F39F6)

// ── Static data models for this screen ──
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
    onDismiss: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val fieldShape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f)

    var expandedSection by remember { mutableStateOf("Item Group Information") }

    // ── Item Group Information ──
    var itemGroupName by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Select Unit") }
    var unitExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    // ── Classification ──
    var brand by remember { mutableStateOf("Select Brand") }
    var brandExpanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(true) }

    // ── Attributes (Variants) ──
    val attributesList = remember {
        mutableStateListOf(
            AttributeEntry(attributeType = "Color", values = listOf("Blue")),
            AttributeEntry(attributeType = "Size", values = listOf("M", "L", "XL"))
        )
    }

    // ── Pricing & Tax ──
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }

    // ── Variant Matrix ──
    var matrixMode by remember { mutableStateOf("") }
    val colorOptions = listOf("Blue")
    val sizeOptions = listOf("M", "L", "XL")
    val selectedSizes = remember { mutableStateListOf("", "") }

    // ── Generated Variants ──
    var trackInventory by remember { mutableStateOf(true) }
    var variantSearch by remember { mutableStateOf("") }
    val variants = remember {
        mutableStateListOf(
            VariantEntry(label = "", sku = "", isExpanded = true),
            VariantEntry(label = "", sku = "")
        )
    }
    var itemGroupImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            itemGroupImages = itemGroupImages + uris
        }
    }
    var bulkCost by remember { mutableStateOf("") }
    var bulkPrice by remember { mutableStateOf("") }
    var bulkReorder by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // ── Header ──
            TitleBar("Create Item Group", onClose = onDismiss)
            HorizontalDivider(color = BorderColor)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = tokens.buttonHeight * 2.2f)
            ) {
                // ── 1. Item Group Information ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_info),
                    title = "Item Group Information",
                    expanded = expandedSection == "Item Group Information",
                    onHeaderClick = { expandedSection = if (expandedSection == "Item Group Information") "" else "Item Group Information" }
                ) {
                    FormLabel("Item Group Name")
                    FormTextField(
                        value = itemGroupName,
                        onValueChange = { itemGroupName = it },
                        placeholder = "Enter Item Group Name"
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormDropdown(
                        label = "Unit",
                        value = unit,
                        expanded = unitExpanded,
                        onExpandChange = { unitExpanded = it },
                        options = listOf("Pieces", "Meters", "Kilograms", "Boxes"),
                        onOptionSelected = { unit = it }
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormLabel("Description")
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Add detailed information about this fabric group..", color = Color(0xFF9CA3AF), fontSize = tokens.bodySmall) },
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderColor,
                            focusedBorderColor = AccentColor
                        ),
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }

                // ── 2. Classification ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.box),
                    iconTint = Primary,
                    title = "Classification",
                    expanded = expandedSection == "Classification",
                    onHeaderClick = { expandedSection = if (expandedSection == "Classification") "" else "Classification" }
                ) {
                    FormDropdown(
                        label = "Brand",
                        value = brand,
                        expanded = brandExpanded,
                        onExpandChange = { brandExpanded = it },
                        options = listOf("Brand A", "Brand B", "Brand C"),
                        onOptionSelected = { brand = it }
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.9f))
                    Text("Item Group Image", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = LabelColor)
                    Spacer(Modifier.height(8.dp))

                    ImageUploadSection(
                        isImage = false,
                        selectedImages = itemGroupImages,
                        browseText = "Browse Files",
                        onBrowseClick = {
                            imagePickerLauncher.launch("*/*")
                        },
                        onCameraClick = null,
                        onRemoveImage = { removedImage ->
                            itemGroupImages = itemGroupImages.filter { it != removedImage }
                        },
                        previewHeaderTitle = "ATTACHED FILES"
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.9f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TitleColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (status) "Active" else "Inactive",
                                fontSize = tokens.bodySmall,
                                color = if (status) AccentColor else LabelColor,
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
                    onHeaderClick = { expandedSection = if (expandedSection == "Attributes (Variants)") "" else "Attributes (Variants)" }
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
                            Text("Max 3 Attributes", fontSize = tokens.label, color = LabelColor)
                        }
                        Text(
                            "+ Add Attribute",
                            fontSize = tokens.bodySmall,
                            color = AccentColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                if (attributesList.size < 3) attributesList.add(AttributeEntry())
                            }
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Each combination of attributes creates a separate item automatically.",
                        fontSize = tokens.caption,
                        color = LabelColor
                    )

                    attributesList.forEachIndexed { index, entry ->
                        Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF7F7FA))
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
                                        tint = Color(0xFFDC2626),
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
                                Text("Values", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = LabelColor)
                                Spacer(Modifier.height(6.dp))
                                AttributeValuesInput(
                                    values = entry.values,
                                    onValuesChange = { attributesList[index] = entry.copy(values = it) }
                                )
                            }
                        }
                    }
                }

                // ── 4. Pricing & Tax ──
                AccordionSection(
                    icon = Icons.Filled.LocalOffer,
                    title = "Pricing & Tax",
                    expanded = expandedSection == "Pricing & Tax",
                    onHeaderClick = { expandedSection = if (expandedSection == "Pricing & Tax") "" else "Pricing & Tax" }
                ) {
                    FormLabel("Cost Price (Default)")
                    FormTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        placeholder = "₹0",
                        keyboardType = KeyboardType.Number
                    )
                    Text(
                        "Updates cost for all generated variants",
                        fontSize = tokens.label,
                        color = LabelColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormLabel("Selling Price (Default)")
                    FormTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        placeholder = "₹0",
                        keyboardType = KeyboardType.Number
                    )
                    Text(
                        "Updates cost for all generated variants",
                        fontSize = tokens.label,
                        color = LabelColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // ── 5. Variant Matrix ──
                AccordionSection(
                    icon = Icons.Filled.CreditCard,
                    title = "Variant Matrix",
                    expanded = expandedSection == "Variant Matrix",
                    onHeaderClick = { expandedSection = if (expandedSection == "Variant Matrix") "" else "Variant Matrix" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderColor, fieldShape)
                            .padding(3.dp)
                    ) {
                        listOf("Manual", "Auto All").forEach { option ->
                            val isSelected = matrixMode == option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) Color(0xFFE3E0FB) else Color.Transparent, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .clickable { matrixMode = option }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    option,
                                    color = if (isSelected) AccentColor else LabelColor,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = tokens.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${selectedSizes.size}/${sizeOptions.size} Selected",
                            fontSize = tokens.caption,
                            color = LabelColor
                        )
                        Row {
                            Text(
                                "Select All",
                                fontSize = tokens.caption,
                                color = AccentColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    selectedSizes.clear()
                                    selectedSizes.addAll(sizeOptions)
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Clear All",
                                fontSize = tokens.caption,
                                color = LabelColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { selectedSizes.clear() }
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    Text("Color", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = LabelColor)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        colorOptions.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .background(ChipBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(color, fontSize = tokens.bodySmall, color = ChipText, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    Text("Size", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = LabelColor)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        sizeOptions.forEach { size ->
                            val isChecked = selectedSizes.contains(size)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedSizes.add(size) else selectedSizes.remove(size)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = AccentColor)
                                )
                                Text(size, fontSize = tokens.bodySmall, color = TitleColor)
                            }
                        }
                    }
                }

                // ── 6. Generated Variants ──
                AccordionSection(
                    title = "Generated Variants",
                    expanded = expandedSection == "Generated Variants",
                    onHeaderClick = { expandedSection = if (expandedSection == "Generated Variants") "" else "Generated Variants" }
                ) {
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
                            Text("Track inventory for this group", fontSize = tokens.bodySmall, color = TitleColor)
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
                            Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(tokens.iconSize * 0.9f))
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = variantSearch,
                                onValueChange = { variantSearch = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (variantSearch.isEmpty()) {
                                        Text("Search", fontSize = tokens.bodySmall, color = Color(0xFF9CA3AF))
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    variants.forEachIndexed { index, variant ->
                        Spacer(Modifier.height(14.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF7F7FA), RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { variants[index] = variant.copy(isExpanded = !variant.isExpanded) }
                                    .padding(tokens.cardPadding * 0.5f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(variant.label, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TitleColor)
                                Icon(
                                    if (variant.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = LabelColor
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
                                                color = AccentColor,
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
                                                tint = AccentColor,
                                                modifier = Modifier.size(tokens.iconSize * 0.8f)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Delete Variant", fontSize = tokens.bodySmall, color = AccentColor, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.screenPadding))
                    Text("Bulk Update", fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = TitleColor)
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
                                        cost = bulkCost,
                                        price = bulkPrice,
                                        reOrderPoint = bulkReorder
                                    )
                                }
                            },
                            text = "Apply to All Variant"
                        )
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
                label = "Save Item Group",
                onClick = onSave
            )
        )
    }
}

// ── Chip-tag input for attribute values ──
@SuppressLint("RememberInComposition")
@Composable
private fun AttributeValuesInput(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit
) {
    val tokens = LocalAppTokens.current
    var inputText by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
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
                        .background(ChipBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            value,
                            fontSize = tokens.bodySmall,
                            color = ChipText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove",
                            tint = ChipText,
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
                        color = blackTitle
                    ),
                    decorationBox = { inner ->
                        if (inputText.isEmpty() && !isFocused) {
                            Text(
                                "Add..",
                                fontSize = tokens.bodySmall,
                                color = Color(0xFF9CA3AF)
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

// ── FlowRow Layout Helper ──
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