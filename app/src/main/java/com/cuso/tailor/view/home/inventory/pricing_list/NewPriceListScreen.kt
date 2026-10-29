@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.pricing_list

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.PriceListActionState

// Line item UI state for SKU rows
data class EditableSkuLineItem(
    val id: String,
    val name: String,
    val sku: String,
    val baseCost: Double,
    val sellingPrice: Double,
    val unit: String = "Pieces",
    val marginPercent: Double = 0.0,
    var adjustmentType: String = "MARKUP %",
    var adjustmentValue: String = "20",
    var basedOn: String = "SELLING_PRICE",
    var isSelected: Boolean = true
) {
    val computedPrice: Double
        get() {
            val v = adjustmentValue.toDoubleOrNull() ?: 0.0
            val base = if (basedOn == "COST_PRICE") baseCost else sellingPrice
            return when (adjustmentType) {
                "MARKUP %" -> base + (base * (v / 100.0))
                "MARKUP ${currencySymbol()}" -> base + v
                "DISCOUNT %" -> base - (base * (v / 100.0))
                "DISCOUNT ${currencySymbol()}" -> (base - v).coerceAtLeast(0.0)
                "FIXED PRICE" -> v
                else -> base
            }
        }
}

@Composable
fun NewPriceListScreen(
    editPriceListId: String? = null,
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val isEditMode = !editPriceListId.isNullOrBlank()

    // Observe inventory items and view-one detail from ViewModel
    val inventoryItems by inventoryViewModel.inventoryItems.collectAsState()
    val priceListDetail by inventoryViewModel.selectedPriceListDetail.collectAsState()
    val actionState by inventoryViewModel.priceListActionState.collectAsState()
    val isLoadingDetail by inventoryViewModel.isLoadingPriceListDetail.collectAsState()

    // Form inputs
    var name by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("SALES") }
    var description by remember { mutableStateOf("") }
    var priceListType by remember { mutableStateOf("INDIVIDUAL_ITEMS") }
    var pricingScheme by remember { mutableStateOf("UNIT_PRICING") }
    var currency by remember { mutableStateOf("INR") }
    var automaticConversion by remember { mutableStateOf(false) }
    var itemSearch by remember { mutableStateOf("") }

    // Universal pricing rule states for ALL_ITEMS
    var allItemsAdjustmentType by remember { mutableStateOf("MARKUP %") }
    var allItemsAdjustmentValue by remember { mutableStateOf("15") }

    // Dropdown choices matching your screenshots
    val roundOffOptions = remember {
        listOf("Select an option", "0.99 (Psychological)", "Nearest Whole", "Round Up", "Round Down")
    }
    var selectedRoundOff by remember { mutableStateOf("0.99 (Psychological)") }

    val basedOnOptions = remember {
        listOf("Select an option", "Selling Price", "Cost Price")
    }
    var selectedBasedOn by remember { mutableStateOf("Selling Price") }

    // Section collapse / expand states
    var basicExpanded by remember { mutableStateOf(true) }
    var typeExpanded by remember { mutableStateOf(true) }
    var schemeExpanded by remember { mutableStateOf(true) }
    var ruleExpanded by remember { mutableStateOf(true) }
    var currencyExpanded by remember { mutableStateOf(true) }

    // Bulk update dialog state
    var showBulkUpdateDialog by remember { mutableStateOf(false) }
    var bulkAdjustmentType by remember { mutableStateOf("MARKUP %") }
    var bulkAdjustmentValue by remember { mutableStateOf("20") }

    val skuItems = remember { mutableStateListOf<EditableSkuLineItem>() }

    // Listen for Save / Update Success or Error
    LaunchedEffect(actionState) {
        when (actionState) {
            is PriceListActionState.Success -> {
                Toast.makeText(context, (actionState as PriceListActionState.Success).message, Toast.LENGTH_SHORT).show()
                inventoryViewModel.resetPriceListActionState()
                inventoryViewModel.clearSelectedPriceListDetail()
                inventoryViewModel.fetchPriceLists() // Refresh pricing list
                onClose()
            }
            is PriceListActionState.Error -> {
                Toast.makeText(context, (actionState as PriceListActionState.Error).message, Toast.LENGTH_LONG).show()
                inventoryViewModel.resetPriceListActionState()
            }
            else -> Unit
        }
    }

    // 1. Initial Load: Fetch inventory items and fetch View-One if in edit mode
    LaunchedEffect(editPriceListId) {
        inventoryViewModel.fetchInventoryItems(page = 1, limit = 50)
        if (isEditMode && editPriceListId.isNotBlank()) {
            inventoryViewModel.fetchPriceListById(editPriceListId)
        } else {
            inventoryViewModel.clearSelectedPriceListDetail()
        }
    }

    // 2. Prefill form when View-One API responds in Edit Mode
    LaunchedEffect(priceListDetail) {
        priceListDetail?.let { detail ->
            name = detail.name
            transactionType = detail.transactionType
            description = detail.description
            priceListType = detail.priceListType
            pricingScheme = detail.pricingScheme
            currency = detail.currency
            automaticConversion = detail.automaticConversion

            // Prefill Round-off
            selectedRoundOff = when (detail.roundOff?.method) {
                "PSYCHOLOGICAL" -> "0.99 (Psychological)"
                "UP" -> "Round Up"
                "DOWN" -> "Round Down"
                "NEAREST_WHOLE" -> "Nearest Whole"
                else -> "0.99 (Psychological)"
            }

            // Prefill pricingRule for ALL_ITEMS from detail.pricingRule
            val rule = detail.pricingRule ?: detail.items.firstOrNull()?.let {
                PricingRuleDto(
                    adjustmentType = it.adjustmentType,
                    value = it.value,
                    basedOn = it.basedOn
                )
            }

            rule?.let {
                allItemsAdjustmentType = when (it.adjustmentType) {
                    "MARKUP_PERCENT" -> "MARKUP %"
                    "MARKUP_AMOUNT" -> "MARKUP ${currencySymbol()}"
                    "DISCOUNT_PERCENT" -> "DISCOUNT %"
                    "DISCOUNT_AMOUNT" -> "DISCOUNT ${currencySymbol()}"
                    "FIXED_PRICE" -> "FIXED PRICE"
                    else -> "MARKUP %"
                }
                allItemsAdjustmentValue = if (it.value % 1.0 == 0.0) {
                    it.value.toInt().toString()
                } else {
                    it.value.toString()
                }
                selectedBasedOn = if (it.basedOn == "COST_PRICE") "Cost Price" else "Selling Price"
            }

            // Prefill individual items from view-one API
            skuItems.clear()
            detail.items.forEach { itemDto ->
                val popItem = itemDto.item
                val adjDisplay = when (itemDto.adjustmentType) {
                    "MARKUP_PERCENT" -> "MARKUP %"
                    "MARKUP_AMOUNT" -> "MARKUP ${currencySymbol()}"
                    "DISCOUNT_PERCENT" -> "DISCOUNT %"
                    "DISCOUNT_AMOUNT" -> "DISCOUNT ${currencySymbol()}"
                    "FIXED_PRICE" -> "FIXED PRICE"
                    else -> "MARKUP %"
                }

                skuItems.add(
                    EditableSkuLineItem(
                        id = popItem?.id ?: itemDto.id,
                        name = popItem?.name ?: itemDto.itemName,
                        sku = popItem?.sku ?: itemDto.sku,
                        baseCost = popItem?.costPrice ?: itemDto.basePrice,
                        sellingPrice = popItem?.sellingPrice ?: itemDto.basePrice,
                        unit = popItem?.unit ?: "Pieces",
                        marginPercent = popItem?.marginPercent ?: 0.0,
                        adjustmentType = adjDisplay,
                        adjustmentValue = if (itemDto.value % 1.0 == 0.0) itemDto.value.toInt().toString() else itemDto.value.toString(),
                        basedOn = itemDto.basedOn,
                        isSelected = true
                    )
                )
            }
        }
    }

    // 3. Fallback: If Create Mode, populate the 1st inventory item into the SKU list
    LaunchedEffect(inventoryItems) {
        if (!isEditMode && inventoryItems.isNotEmpty() && skuItems.isEmpty()) {
            val firstItem = inventoryItems.first()
            skuItems.add(
                EditableSkuLineItem(
                    id = firstItem._id,
                    name = firstItem.name.ifBlank { "Untitled Item" },
                    sku = firstItem.sku.ifBlank { "SKU-001" },
                    baseCost = firstItem.costPrice,
                    sellingPrice = firstItem.sellingPrice,
                    unit = firstItem.unit,
                    adjustmentType = "MARKUP %",
                    adjustmentValue = "20",
                    basedOn = "SELLING_PRICE",
                    isSelected = true
                )
            )
        }
    }

    val isAllItems = priceListType == "ALL_ITEMS"

    // ── Build Payloads matching Network API Structure ──
    val onSavePriceList: () -> Unit = {
        if (name.isBlank()) {
            Toast.makeText(context, "Please enter Price List name", Toast.LENGTH_SHORT).show()
        } else {
            val methodVal = when (selectedRoundOff) {
                "0.99 (Psychological)" -> "PSYCHOLOGICAL"
                "Round Up" -> "UP"
                "Round Down" -> "DOWN"
                else -> "NEAREST_WHOLE"
            }
            val roundOffVal = if (methodVal == "PSYCHOLOGICAL") 0.99 else 1.0
            val roundOffObj = RoundOffDto(enabled = true, method = methodVal, value = roundOffVal)

            val universalApiAdjType = when (allItemsAdjustmentType) {
                "MARKUP %" -> "MARKUP_PERCENT"
                "MARKUP ${currencySymbol()}" -> "MARKUP_AMOUNT"
                "DISCOUNT %" -> "DISCOUNT_PERCENT"
                "DISCOUNT ${currencySymbol()}" -> "DISCOUNT_AMOUNT"
                "FIXED PRICE" -> "FIXED_PRICE"
                else -> "MARKUP_PERCENT"
            }
            val universalBasedOn = if (selectedBasedOn == "Cost Price") "COST_PRICE" else "SELLING_PRICE"
            val universalValue = allItemsAdjustmentValue.toDoubleOrNull() ?: 0.0

            // 1. Build Pricing Rule Object for ALL_ITEMS
            val pricingRuleObj = if (isAllItems) {
                PricingRuleDto(
                    adjustmentType = universalApiAdjType,
                    value = universalValue,
                    basedOn = universalBasedOn
                )
            } else {
                null
            }

            if (isEditMode) {
                // 2. In ALL_ITEMS, items array must be empty; for INDIVIDUAL_ITEMS, map selected SKUs
                val itemsPayload: List<Any> = if (isAllItems) {
                    emptyList()
                } else {
                    skuItems.filter { it.isSelected }.map { sku ->
                        val apiAdjType = when (sku.adjustmentType) {
                            "MARKUP %" -> "MARKUP_PERCENT"
                            "MARKUP ${currencySymbol()}" -> "MARKUP_AMOUNT"
                            "DISCOUNT %" -> "DISCOUNT_PERCENT"
                            "DISCOUNT ${currencySymbol()}" -> "DISCOUNT_AMOUNT"
                            "FIXED PRICE" -> "FIXED_PRICE"
                            else -> "MARKUP_PERCENT"
                        }

                        mapOf(
                            "adjustmentType" to apiAdjType,
                            "basedOn" to sku.basedOn,
                            "value" to (sku.adjustmentValue.toDoubleOrNull() ?: 0.0),
                            "itemId" to mapOf(
                                "_id" to sku.id,
                                "id" to sku.id,
                                "name" to sku.name,
                                "sku" to sku.sku,
                                "unit" to sku.unit,
                                "costPrice" to sku.baseCost,
                                "sellingPrice" to sku.sellingPrice,
                                "marginPercent" to sku.marginPercent,
                                "currentStockValue" to 0
                            )
                        )
                    }
                }

                val updatePayload = PriceListRequestPayload(
                    name = name.trim(),
                    transactionType = transactionType,
                    description = description.trim(),
                    priceListType = priceListType,
                    pricingScheme = pricingScheme,
                    currency = currency,
                    automaticConversion = automaticConversion,
                    status = "ACTIVE",
                    roundOff = roundOffObj,
                    items = itemsPayload,
                    pricingRule = pricingRuleObj
                )

                inventoryViewModel.updatePriceList(editPriceListId, updatePayload)
            } else {
                // 3. Create Mode Payload
                val itemsPayload: List<Any> = if (isAllItems) {
                    emptyList() // No items array for ALL_ITEMS
                } else {
                    skuItems.filter { it.isSelected }.map { sku ->
                        val apiAdjType = when (sku.adjustmentType) {
                            "MARKUP %" -> "MARKUP_PERCENT"
                            "MARKUP ${currencySymbol()}" -> "MARKUP_AMOUNT"
                            "DISCOUNT %" -> "DISCOUNT_PERCENT"
                            "DISCOUNT ${currencySymbol()}" -> "DISCOUNT_AMOUNT"
                            "FIXED PRICE" -> "FIXED_PRICE"
                            else -> "MARKUP_PERCENT"
                        }

                        CreatePriceListItemPayload(
                            itemId = sku.id,
                            adjustmentType = apiAdjType,
                            basedOn = sku.basedOn,
                            value = sku.adjustmentValue.toDoubleOrNull() ?: 0.0
                        )
                    }
                }

                val createPayload = PriceListRequestPayload(
                    name = name.trim(),
                    transactionType = transactionType,
                    description = description.trim(),
                    priceListType = priceListType,
                    pricingScheme = pricingScheme,
                    currency = currency,
                    automaticConversion = automaticConversion,
                    status = "ACTIVE",
                    roundOff = roundOffObj,
                    items = itemsPayload,
                    pricingRule = pricingRuleObj // ✅ Passes pricingRule object
                )

                inventoryViewModel.createPriceList(createPayload)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        if (isLoadingDetail) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CirculerProgressIndicatorSmall()
            }
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TitleBar(
                            title = if (isEditMode) "Edit Price List" else "New Price List",
                            onClose = onClose
                        )
                        HorizontalDivider(color = title_border)
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // SECTION 1: BASIC DETAILS
                    item {
                        AccordionSection(
                            icon = Icons.Default.Menu,
                            title = "1. Basic Details",
                            expanded = basicExpanded,
                            onHeaderClick = { basicExpanded = !basicExpanded }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(vertical = tokens.extraPadding)
                            ) {
                                FormLabel("Price List Name", isRequired = true)
                                FormTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    placeholder = "e.g. EU Wholesale 2026"
                                )

                                Spacer(Modifier.height(14.dp))
                                FormLabel("Transaction Type")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(light_grey, RoundedCornerShape(8.dp))
                                        .padding(4.dp)
                                ) {
                                    listOf("SALES" to "Sales", "PURCHASE" to "Purchase").forEach { (typeVal, label) ->
                                        val isSelected = transactionType == typeVal
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) whiteBg else Color.Transparent)
                                                .clickable { transactionType = typeVal }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) Primary else TextSecondary
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                FormLabel("Description")
                                FormTextArea(
                                    value = description,
                                    onValueChange = { description = it },
                                    placeholder = "Describe the scope or purpose of this price list..."
                                )
                            }
                        }
                    }

                    // SECTION 2: PRICE LIST TYPE
                    item {
                        AccordionSection(
                            icon = Icons.Default.ShoppingBag,
                            title = "2. Price List Type",
                            expanded = typeExpanded,
                            onHeaderClick = { typeExpanded = !typeExpanded }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(vertical = tokens.extraPadding)
                            ) {
                                val isAllSelected = priceListType == "ALL_ITEMS"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                        .border(
                                            width = if (isAllSelected) 2.dp else 1.dp,
                                            color = if (isAllSelected) Primary else sectionBorder,
                                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                                        )
                                        .background(if (isAllSelected) primary_light else whiteBg)
                                        .clickable { priceListType = "ALL_ITEMS" }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(if (isAllSelected) Primary else primary_light, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = if (isAllSelected) whiteBg else Primary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("All Items", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        Text("Apply a universal rule to all products", fontSize = tokens.caption, color = TextSecondary)
                                    }
                                    if (isAllSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                    } else {
                                        Box(modifier = Modifier.size(20.dp).border(1.dp, mutedText, CircleShape))
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                val isIndividualSelected = priceListType == "INDIVIDUAL_ITEMS"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                        .border(
                                            width = if (isIndividualSelected) 2.dp else 1.dp,
                                            color = if (isIndividualSelected) Primary else sectionBorder,
                                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                                        )
                                        .background(if (isIndividualSelected) primary_light else whiteBg)
                                        .clickable { priceListType = "INDIVIDUAL_ITEMS" }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(if (isIndividualSelected) Primary else light_grey, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = if (isIndividualSelected) whiteBg else TextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Individual Items", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        Text("Set specific prices for each SKU", fontSize = tokens.caption, color = TextSecondary)
                                    }
                                    if (isIndividualSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                    } else {
                                        Box(modifier = Modifier.size(20.dp).border(1.dp, mutedText, CircleShape))
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 3: PRICING SCHEME (Always displayed)
                    item {
                        AccordionSection(
                            icon = Icons.Default.LocalOffer,
                            title = "Pricing Scheme",
                            expanded = schemeExpanded,
                            onHeaderClick = { schemeExpanded = !schemeExpanded }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(vertical = tokens.extraPadding),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val isUnit = pricingScheme == "UNIT_PRICING"
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                        .border(
                                            width = if (isUnit) 2.dp else 1.dp,
                                            color = if (isUnit) Primary else sectionBorder,
                                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                                        )
                                        .background(if (isUnit) primary_light else whiteBg)
                                        .clickable { pricingScheme = "UNIT_PRICING" }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().height(16.dp), contentAlignment = Alignment.TopEnd) {
                                        if (isUnit) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Box(modifier = Modifier.size(40.dp).background(if (isUnit) Primary else light_grey, CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = if (isUnit) whiteBg else TextSecondary)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("Unit Pricing", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }

                                val isVolume = pricingScheme == "VOLUME_PRICING"
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                        .border(
                                            width = if (isVolume) 2.dp else 1.dp,
                                            color = if (isVolume) Primary else sectionBorder,
                                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                                        )
                                        .background(if (isVolume) primary_light else whiteBg)
                                        .clickable { pricingScheme = "VOLUME_PRICING" }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().height(16.dp), contentAlignment = Alignment.TopEnd) {
                                        if (isVolume) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Box(modifier = Modifier.size(40.dp).background(if (isVolume) Primary else light_grey, CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = if (isVolume) whiteBg else TextSecondary)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("Volume Pricing", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }
                            }
                        }
                    }

                    // SECTION 4: PRICING RULE (Only if All Items)
                    if (isAllItems) {
                        item {
                            var adjExpanded by remember { mutableStateOf(false) }
                            var basisExpanded by remember { mutableStateOf(false) }

                            AccordionSection(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                title = "3. Pricing Rule",
                                expanded = ruleExpanded,
                                onHeaderClick = { ruleExpanded = !ruleExpanded }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(whiteBg)
                                        .padding(vertical = tokens.extraPadding)
                                ) {
                                    FormDropdown(
                                        label = "Adjustment Type",
                                        value = allItemsAdjustmentType,
                                        expanded = adjExpanded,
                                        onExpandChange = { adjExpanded = it },
                                        options = listOf("MARKUP %", "MARKUP ${currencySymbol()}", "DISCOUNT %", "DISCOUNT ${currencySymbol()}"),
                                        onOptionSelected = { allItemsAdjustmentType = it }
                                    )

                                    Spacer(Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            FormLabel("Value")
                                            FormTextField(
                                                value = allItemsAdjustmentValue,
                                                onValueChange = { input ->
                                                    allItemsAdjustmentValue = input.filter { it.isDigit() || it == '.' }
                                                },
                                                placeholder = "15"
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            FormDropdown(
                                                label = "Based On",
                                                value = selectedBasedOn,
                                                expanded = basisExpanded,
                                                onExpandChange = { basisExpanded = it },
                                                options = basedOnOptions,
                                                onOptionSelected = { selectedBasedOn = it }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 5: CURRENCY & ROUNDING
                    item {
                        var currExpanded by remember { mutableStateOf(false) }
                        var roundExpanded by remember { mutableStateOf(false) }

                        AccordionSection(
                            icon = Icons.Default.Refresh,
                            title = "4. Currency & Rounding",
                            expanded = currencyExpanded,
                            onHeaderClick = { currencyExpanded = !currencyExpanded }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(vertical = tokens.extraPadding)
                            ) {
                                FormDropdown(
                                    label = "Currency",
                                    value = currency,
                                    expanded = currExpanded,
                                    onExpandChange = { currExpanded = it },
                                    options = listOf("INR", "USD", "EUR", "GBP"),
                                    onOptionSelected = { currency = it }
                                )

                                Spacer(Modifier.height(14.dp))
                                FormDropdown(
                                    label = "Round Off To",
                                    value = selectedRoundOff,
                                    expanded = roundExpanded,
                                    onExpandChange = { roundExpanded = it },
                                    options = roundOffOptions,
                                    onOptionSelected = { selectedRoundOff = it }
                                )

                                Spacer(Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                        .background(whiteBg)
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Automatic Conversion", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        Text("Use daily market exchange rates", fontSize = tokens.caption, color = TextSecondary)
                                    }

                                    MiniSwitch(
                                        checked = automaticConversion,
                                        onCheckedChange = { automaticConversion = it }
                                    )
                                }
                            }
                        }
                    }

                    // BOTTOM SKUS PRODUCT SECTION (When Individual Items is selected)
                    if (!isAllItems) {
                        item {
                            Column {
                                SearchFilterBar(
                                    query = itemSearch,
                                    onQueryChange = { itemSearch = it },
                                    placeholder = "Search items by name, SKU or category...",
                                    isSearchBarAlone = true
                                )
                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 15.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { showBulkUpdateDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, sectionBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg, contentColor = TextLog),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = headerGrey, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Bulk Update", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = TextLog)
                                    }

                                    Button(
                                        onClick = {
                                            val existingIds = skuItems.map { it.id }.toSet()
                                            val nextItem = inventoryItems.firstOrNull { it._id !in existingIds }
                                            if (nextItem != null) {
                                                skuItems.add(
                                                    EditableSkuLineItem(
                                                        id = nextItem._id,
                                                        name = nextItem.name.ifBlank { "Untitled Item" },
                                                        sku = nextItem.sku.ifBlank { "SKU" },
                                                        baseCost = nextItem.costPrice,
                                                        sellingPrice = nextItem.sellingPrice,
                                                        unit = nextItem.unit,
                                                        adjustmentType = "MARKUP %",
                                                        adjustmentValue = "20",
                                                        basedOn = "SELLING_PRICE"
                                                    )
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 5.dp)
                                    ) {
                                        Text("+ Add Item", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = whiteBg)
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }

                        val filteredSkus = skuItems.filter {
                            itemSearch.isBlank() || it.name.contains(itemSearch, ignoreCase = true) || it.sku.contains(itemSearch, ignoreCase = true)
                        }

                        items(filteredSkus, key = { it.id }) { itemSku ->
                            val index = skuItems.indexOf(itemSku)
                            ApiSkuProductCardRow(
                                itemSku = itemSku,
                                onItemUpdated = { updated ->
                                    if (index != -1) skuItems[index] = updated
                                }
                            )
                        }
                    }
                }
            }

            // ── Bulk Update Confirmation Dialog ──
            if (showBulkUpdateDialog) {
                val bulkAdjustmentOptions = remember {
                    listOf("Select an option", "MARKUP %", "MARKUP ${currencySymbol()}", "DISCOUNT %", "DISCOUNT ${currencySymbol()}", "FIXED PRICE")
                }
                var bulkDropdownExpanded by remember { mutableStateOf(false) }

                androidx.compose.ui.window.Dialog(onDismissRequest = { showBulkUpdateDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(tokens.extraPadding)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Bulk Update Items", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                IconButton(onClick = { showBulkUpdateDialog = false }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = iconMuted, modifier = Modifier.size(18.dp))
                                }
                            }

                            HorizontalDivider(color = sectionBorder, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.6f))

                            FormLabel("Adjustment Type", isRequired = true)
                            Spacer(Modifier.height(4.dp))
                            FormDropdown(
                                label = "",
                                value = bulkAdjustmentType,
                                expanded = bulkDropdownExpanded,
                                onExpandChange = { bulkDropdownExpanded = it },
                                options = bulkAdjustmentOptions,
                                onOptionSelected = { chosen ->
                                    if (chosen != "Select an option") bulkAdjustmentType = chosen
                                }
                            )

                            Spacer(Modifier.height(14.dp))

                            FormLabel("Adjustment Value", isRequired = true)
                            Spacer(Modifier.height(4.dp))
                            FormTextField(
                                value = bulkAdjustmentValue,
                                onValueChange = { input -> bulkAdjustmentValue = input.filter { it.isDigit() || it == '.' } },
                                placeholder = "e.g. 20"
                            )

                            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { showBulkUpdateDialog = false },
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                    border = BorderStroke(1.dp, sectionBorder),
                                    modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                                ) {
                                    Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }

                                Button(
                                    onClick = {
                                        skuItems.forEachIndexed { idx, item ->
                                            skuItems[idx] = item.copy(
                                                adjustmentType = bulkAdjustmentType,
                                                adjustmentValue = bulkAdjustmentValue
                                            )
                                        }
                                        showBulkUpdateDialog = false
                                    },
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    modifier = Modifier.weight(1.2f).height(tokens.buttonHeight)
                                ) {
                                    Text("Apply to All", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
                                }
                            }
                        }
                    }
                }
            }

            StepNavigationFab(
                showBack = true,
                backLabel = "Cancel",
                onBack = onClose,
                showBackArrow = false,
                trailingAction = TrailingFabAction.Update(
                    label = if (isEditMode) "Save Changes" else "Save Pricelist",
                    onClick = onSavePriceList
                ),
                backWidthFraction = 0.25f,
                trailingWidthFraction = 0.35f
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SKU CARD COMPONENT WITH DROPDOWN OPTIONS
// ─────────────────────────────────────────────────────────────
@Composable
private fun ApiSkuProductCardRow(
    itemSku: EditableSkuLineItem,
    onItemUpdated: (EditableSkuLineItem) -> Unit
) {
    val tokens = LocalAppTokens.current
    var dropdownExpanded by remember { mutableStateOf(false) }

    val markupOptions = remember {
        listOf("option", "MARKUP %", "MARKUP ${currencySymbol()}", "DISCOUNT %", "DISCOUNT ${currencySymbol()}", "FIXED PRICE")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 6.dp)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(whiteBg)
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AppCheckbox(
                checked = itemSku.isSelected,
                onCheckedChange = { checked -> onItemUpdated(itemSku.copy(isSelected = checked)) }
            )
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = itemSku.name, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(text = "SKU: ${itemSku.sku}", fontSize = tokens.caption, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Base Cost", fontSize = tokens.caption, color = TextSecondary)
                Text("${currencySymbol()}${"%.2f".format(itemSku.baseCost)}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
            }

            Box {
                Surface(
                    onClick = { dropdownExpanded = true },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, sectionBorder),
                    color = whiteBg
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = itemSku.adjustmentType, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = Primary)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = if (dropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    offset = DpOffset(0.dp, 4.dp),
                    modifier = Modifier.background(whiteBg, RoundedCornerShape(8.dp)).border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                ) {
                    markupOptions.forEach { opt ->
                        val isSelected = opt == itemSku.adjustmentType
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = opt,
                                    fontSize = tokens.caption,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isSelected) Primary else TextPrimary
                                )
                            },
                            modifier = Modifier.background(if (isSelected) primary_light else Color.Transparent),
                            onClick = {
                                dropdownExpanded = false
                                if (opt != "option") onItemUpdated(itemSku.copy(adjustmentType = opt))
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .background(light_grey, RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                    .padding(vertical = 5.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = itemSku.adjustmentValue,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        onItemUpdated(itemSku.copy(adjustmentValue = filtered))
                    },
                    textStyle = TextStyle(
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Computed", fontSize = tokens.caption, color = TextSecondary)
                Text(
                    text = "${currencySymbol()}${"%.2f".format(itemSku.computedPrice)}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}