package com.cuso.mobile.view.home.sales.pricing

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.sales.BulkRuleDto
import com.cuso.mobile.model.sales.PriceAdjustmentDto
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.home.LeadPrimary
import com.cuso.mobile.view.home.LeadmutedText
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.viewmodel.GarmentPricingDetailUiState
import com.cuso.mobile.viewmodel.PricingQuotationViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import kotlinx.coroutines.delay

// ── Simple row models for the dynamic lists ──
private data class PriceAdjustmentRow(val id: Int, val name: String, val price: String)
private data class DiscountRuleRow(val id: Int, val minQuantity: String, val discountPercent: String)

@Composable
fun AddGarmentPricingScreen(
    onClose: () -> Unit,
    onSave: () -> Unit = {},
    pricingId: String? = null
) {
    val isEditMode = pricingId != null

    val salesViewModel: SalesViewModel = hiltViewModel()
    val pricingViewModel: PricingQuotationViewModel = hiltViewModel()

    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()
    val saveState by pricingViewModel.saveState.collectAsStateWithLifecycle()
    val detailState by pricingViewModel.garmentPricingDetailState.collectAsStateWithLifecycle()

    val garmentOptions = garmentCategories.map { it.categoryId.categoryName }

    var showGarmentTypeError by remember { mutableStateOf(false) }
    var showBasePriceError by remember { mutableStateOf(false) }

    LaunchedEffect(pricingId) {
        if (garmentCategories.isEmpty()) salesViewModel.fetchGarmentCategories()
        if (isEditMode) {
            pricingViewModel.fetchGarmentPricingDetail(pricingId)
        }
    }

    // ── Basic Information ──
    var garmentType by remember { mutableStateOf("") }
    var selectedGarmentCategoryId by remember { mutableStateOf("") }
    var garmentTypeExpanded by remember { mutableStateOf(false) }
    var baseStitchingPrice by remember { mutableStateOf("") }

    // ── ID counter for dynamic rows ──
    var nextId by remember { mutableIntStateOf(1) }
    fun newId(): Int { val id = nextId; nextId++; return id }

    // ── Dynamic Rows ──
    var fabricRows by remember { mutableStateOf(emptyList<PriceAdjustmentRow>()) }
    var styleRows by remember { mutableStateOf(emptyList<PriceAdjustmentRow>()) }
    var additionalChargeRows by remember { mutableStateOf(emptyList<PriceAdjustmentRow>()) }
    var discountRules by remember { mutableStateOf(emptyList<DiscountRuleRow>()) }

    var expandedSection by remember { mutableStateOf("basic_info") }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var showSuccess by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var prefilled by remember { mutableStateOf(false) }
    var isLoadingDetail by remember { mutableStateOf(isEditMode) }

    // Helper to clean up any empty/blank rows across all dynamic sections
    fun cleanupEmptyRows() {
        fabricRows = fabricRows.filter { it.name.isNotBlank() }
        styleRows = styleRows.filter { it.name.isNotBlank() }
        additionalChargeRows = additionalChargeRows.filter { it.name.isNotBlank() }
        discountRules = discountRules.filter {
            it.minQuantity.isNotBlank() && it.minQuantity != "0" && it.discountPercent.isNotBlank()
        }
    }

    // ── Prefill fields once detail data arrives (edit mode only) ──
    LaunchedEffect(detailState, garmentCategories, pricingId) {
        if (isEditMode && !prefilled) {
            when (val ds = detailState) {
                is GarmentPricingDetailUiState.Success -> {
                    val detail = ds.detail
                    if (detail.id != pricingId) return@LaunchedEffect

                    selectedGarmentCategoryId = detail.applicableGarmentId
                    garmentType = garmentCategories
                        .firstOrNull { it.id == detail.applicableGarmentId }
                        ?.categoryId?.categoryName
                        ?: garmentType

                    baseStitchingPrice = detail.basePrice.toString()

                    fabricRows = detail.fabricAdjustments
                        .filter { it.name.isNotBlank() }
                        .map { PriceAdjustmentRow(newId(), it.name, it.price.toString()) }

                    styleRows = detail.designAdjustments
                        .filter { it.name.isNotBlank() }
                        .map { PriceAdjustmentRow(newId(), it.name, it.price.toString()) }

                    additionalChargeRows = detail.additionalCharges
                        .filter { it.name.isNotBlank() }
                        .map { PriceAdjustmentRow(newId(), it.name, it.price.toString()) }

                    discountRules = detail.bulkRules
                        .filter { it.minQuantity > 0 && it.discountPercent > 0 }
                        .map { DiscountRuleRow(newId(), it.minQuantity.toString(), it.discountPercent.toString()) }

                    if (garmentCategories.isNotEmpty()) {
                        prefilled = true
                    }
                    isLoadingDetail = false
                }
                is GarmentPricingDetailUiState.Error -> {
                    errorMessage = extractApiErrorMessage(ds.message)
                    showError = true
                    isLoadingDetail = false
                }
                else -> Unit
            }
        }
    }

    fun updatePrice(rows: List<PriceAdjustmentRow>, id: Int, newValue: String): List<PriceAdjustmentRow> =
        rows.map { if (it.id == id) it.copy(price = newValue) else it }

    fun updateName(rows: List<PriceAdjustmentRow>, id: Int, newValue: String): List<PriceAdjustmentRow> =
        rows.map { if (it.id == id) it.copy(name = newValue) else it }

    // ── Handle save / update action ──
    fun handleSave() {
        showGarmentTypeError = false
        showBasePriceError = false

        // Automatically clean up any rows left blank
        cleanupEmptyRows()

        var hasBasicError = false
        if (selectedGarmentCategoryId.isEmpty()) {
            showGarmentTypeError = true
            hasBasicError = true
        }
        if (baseStitchingPrice.isEmpty()) {
            showBasePriceError = true
            hasBasicError = true
        }

        if (hasBasicError) {
            errorMessage = "Please fill all required fields"
            showError = true
            expandedSection = "basic_info"
            return
        }

        // Validate only completed rules
        val invalidRules = discountRules.filter {
            it.minQuantity.isNotBlank() && (it.minQuantity.toIntOrNull() ?: 0) < 1
        }
        if (invalidRules.isNotEmpty()) {
            errorMessage = "Minimum quantity must be at least 1 for all discount rules"
            showError = true
            expandedSection = "discount_rules"
            return
        }

        // Convert UI data to DTOs (filters out any empty entries)
        val fabricAdjustments = fabricRows
            .filter { it.name.isNotBlank() }
            .map { PriceAdjustmentDto(name = it.name.trim(), price = it.price.toDoubleOrNull() ?: 0.0) }

        val designAdjustments = styleRows
            .filter { it.name.isNotBlank() }
            .map { PriceAdjustmentDto(name = it.name.trim(), price = it.price.toDoubleOrNull() ?: 0.0) }

        val additionalCharges = additionalChargeRows
            .filter { it.name.isNotBlank() }
            .map { PriceAdjustmentDto(name = it.name.trim(), price = it.price.toDoubleOrNull() ?: 0.0) }

        val bulkRules = discountRules.mapNotNull { rule ->
            val minQty = rule.minQuantity.toIntOrNull()
            val discount = rule.discountPercent.toDoubleOrNull()
            if (minQty != null && minQty >= 1 && discount != null && discount > 0) {
                BulkRuleDto(minQuantity = minQty, discountPercent = discount)
            } else null
        }

        if (isEditMode) {
            pricingViewModel.updatePricingQuotation(
                id = pricingId,
                garmentCategoryId = selectedGarmentCategoryId,
                basePrice = baseStitchingPrice.toDoubleOrNull() ?: 0.0,
                fabricAdjustments = fabricAdjustments,
                designAdjustments = designAdjustments,
                additionalCharges = additionalCharges,
                expressCharge = 0.0,
                bulkRules = bulkRules
            )
        } else {
            pricingViewModel.savePricingQuotation(
                garmentCategoryId = selectedGarmentCategoryId,
                basePrice = baseStitchingPrice.toDoubleOrNull() ?: 0.0,
                fabricAdjustments = fabricAdjustments,
                designAdjustments = designAdjustments,
                additionalCharges = additionalCharges,
                expressCharge = 0.0,
                bulkRules = bulkRules
            )
        }
    }

    // ── Observe save result ──
    LaunchedEffect(saveState) {
        when {
            saveState.isSuccess && saveState.response != null -> {
                successMessage = if (isEditMode) "Pricing updated successfully" else "Pricing saved successfully"
                showSuccess = true
                delay(1500)
                onSave()
                pricingViewModel.resetSaveState()
                showSuccess = false
            }
            saveState.errorMessage != null -> {
                errorMessage = extractApiErrorMessage(saveState.errorMessage!!)
                showError = true
                pricingViewModel.resetSaveState()
            }
        }
    }

    if (isEditMode && isLoadingDetail) {
        ListSkeleton()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TitleBar(
                    title = if (isEditMode) "Edit Pricing" else "Add New Garment Pricing",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    // ── 1. Basic Information ──
                    item {
                        AccordionSection(
                            title = "Basic Information",
                            expanded = expandedSection == "basic_info",
                            onHeaderClick = {
                                cleanupEmptyRows()
                                expandedSection = if (expandedSection == "basic_info") "" else "basic_info"
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (isEditMode) 0.6f else 1f)
                            ) {
                                FormDropdown(
                                    label = "Garment Type",
                                    value = garmentType.ifEmpty { "Select the type of garment" },
                                    expanded = garmentTypeExpanded,
                                    onExpandChange = { if (!isEditMode) garmentTypeExpanded = it },
                                    options = garmentOptions,
                                    onOptionSelected = { selectedName ->
                                        garmentType = selectedName
                                        selectedGarmentCategoryId = garmentCategories
                                            .firstOrNull { it.categoryId.categoryName == selectedName }
                                            ?.id ?: ""
                                        showGarmentTypeError = false
                                    },
                                    isRequired = true,
                                    isError = showGarmentTypeError,
                                    errorMessage = "Please select a garment type"
                                )
                                if (isEditMode) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) { }
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Base stitching price", isRequired = true)
                            FormTextField(
                                value = baseStitchingPrice,
                                onValueChange = {
                                    baseStitchingPrice = it
                                    showBasePriceError = false
                                },
                                keyboardType = KeyboardType.Number,
                                placeholder = "Standard stitching charge",
                                isError = showBasePriceError,
                                errorMessage = "Please enter base stitching price"
                            )
                        }
                        HorizontalDivider(color = title_border)
                    }

                    // ── 2. Fabric Price Adjustments ──
                    item {
                        AccordionSection(
                            title = "Fabric Price Adjustments",
                            expanded = expandedSection == "fabric",
                            onHeaderClick = {
                                cleanupEmptyRows()
                                expandedSection = if (expandedSection == "fabric") "" else "fabric"
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        fabricRows = fabricRows.filter { it.name.isNotBlank() } + PriceAdjustmentRow(newId(), "", "")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LeadPrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LeadPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add Row", fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                }
                                fabricRows.forEach { row ->
                                    PriceAdjustmentField(
                                        name = row.name,
                                        price = row.price,
                                        onNameChange = { fabricRows = updateName(fabricRows, row.id, it) },
                                        onPriceChange = { fabricRows = updatePrice(fabricRows, row.id, it) },
                                        onRemove = { fabricRows = fabricRows.filter { it.id != row.id } }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = title_border)
                    }

                    // ── 3. Design / Style Options ──
                    item {
                        AccordionSection(
                            title = "Design / Style Options",
                            expanded = expandedSection == "style",
                            onHeaderClick = {
                                cleanupEmptyRows()
                                expandedSection = if (expandedSection == "style") "" else "style"
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        styleRows = styleRows.filter { it.name.isNotBlank() } + PriceAdjustmentRow(newId(), "", "")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LeadPrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LeadPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add Row", fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                }
                                styleRows.forEach { row ->
                                    PriceAdjustmentField(
                                        name = row.name,
                                        price = row.price,
                                        onNameChange = { styleRows = updateName(styleRows, row.id, it) },
                                        onPriceChange = { styleRows = updatePrice(styleRows, row.id, it) },
                                        onRemove = { styleRows = styleRows.filter { it.id != row.id } }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = title_border)
                    }

                    // ── 4. Additional Charges ──
                    item {
                        AccordionSection(
                            title = "Additional Charges",
                            expanded = expandedSection == "charges",
                            onHeaderClick = {
                                cleanupEmptyRows()
                                expandedSection = if (expandedSection == "charges") "" else "charges"
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        additionalChargeRows = additionalChargeRows.filter { it.name.isNotBlank() } + PriceAdjustmentRow(newId(), "", "")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LeadPrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LeadPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add Row", fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                }
                                additionalChargeRows.forEach { row ->
                                    PriceAdjustmentField(
                                        name = row.name,
                                        price = row.price,
                                        onNameChange = { additionalChargeRows = updateName(additionalChargeRows, row.id, it) },
                                        onPriceChange = { additionalChargeRows = updatePrice(additionalChargeRows, row.id, it) },
                                        onRemove = { additionalChargeRows = additionalChargeRows.filter { it.id != row.id } }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = title_border)
                    }

                    // ── 5. Quantity Discount Rules ──
                    item {
                        AccordionSection(
                            title = "Quantity Discount Rules",
                            expanded = expandedSection == "discount_rules",
                            onHeaderClick = {
                                cleanupEmptyRows()
                                expandedSection = if (expandedSection == "discount_rules") "" else "discount_rules"
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (showError && errorMessage.isNotEmpty()) {
                                    Text(errorMessage, fontSize = 12.sp, color = Color(0xFFEF4444))
                                }
                                OutlinedButton(
                                    onClick = {
                                        // Filter out any blank rules first, then add a clean empty row
                                        discountRules = discountRules.filter {
                                            it.minQuantity.isNotBlank() && it.minQuantity != "0" && it.discountPercent.isNotBlank()
                                        } + DiscountRuleRow(newId(), "", "")
                                        showError = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LeadPrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LeadPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add Rule", fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                }
                                discountRules.forEach { rule ->
                                    DiscountRuleField(
                                        minQuantity = rule.minQuantity,
                                        discountPercent = rule.discountPercent,
                                        onMinQuantityChange = { newVal ->
                                            discountRules = discountRules.map {
                                                if (it.id == rule.id) it.copy(minQuantity = newVal) else it
                                            }
                                        },
                                        onDiscountPercentChange = { newVal ->
                                            discountRules = discountRules.map {
                                                if (it.id == rule.id) it.copy(discountPercent = newVal) else it
                                            }
                                        },
                                        onRemove = {
                                            discountRules = discountRules.filter { it.id != rule.id }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }

        // ── StepNavigationFab with save/update logic ──
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            trailingAction = TrailingFabAction.Update(
                isLoading = saveState.isLoading,
                label = if (isEditMode) "Update Pricing" else "Save",
                onClick = { handleSave() }
            )
        )

        // ── Dynamic Island success toast ──
        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = if (showSuccess) successMessage else null,
            onDismiss = { showSuccess = false }
        )

        // ── Dynamic Island error toast ──
        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = if (showError) errorMessage else null,
            onDismiss = { showError = false }
        )
    }
}

private fun extractApiErrorMessage(raw: String): String {
    return try {
        val regex = Regex("\"message\"\\s*:\\s*\"(.*?)\"")
        regex.find(raw)?.groupValues?.get(1) ?: raw
    } catch (_: Exception) {
        raw
    }
}

@Composable
private fun PriceAdjustmentField(
    name: String,
    price: String,
    onNameChange: (String) -> Unit = {},
    onPriceChange: (String) -> Unit,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, grey_border, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (name.isEmpty()) {
                    Text("Enter name", fontSize = 14.sp, color = LeadmutedText)
                }
                innerTextField()
            }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = LeadPrimary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("₹", fontSize = 13.sp, color = Color(0xFF374151))
            BasicTextField(
                value = price,
                onValueChange = onPriceChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF374151), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                modifier = Modifier.width(40.dp),
                decorationBox = { inner ->
                    if (price.isEmpty()) {
                        Text("0", fontSize = 13.sp, color = LeadmutedText)
                    }
                    inner()
                }
            )
            if (onRemove != null) {
                Spacer(Modifier.width(10.dp))
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove row",
                    tint = LeadmutedText,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRemove() }
                )
            }
        }
    }
}

@Composable
private fun DiscountRuleField(
    minQuantity: String,
    discountPercent: String,
    onMinQuantityChange: (String) -> Unit,
    onDiscountPercentChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val minQtyError = minQuantity.toIntOrNull()?.let { it < 1 } == true

        MiniNumberField(
            label = "Min Quantity",
            value = minQuantity,
            onValueChange = onMinQuantityChange,
            modifier = Modifier.weight(1f),
            isError = minQtyError,
            errorMessage = if (minQtyError) "Min: 1" else null
        )
        MiniNumberField(
            label = "Discount %",
            value = discountPercent,
            onValueChange = onDiscountPercentChange,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.Close,
            contentDescription = "Remove rule",
            tint = LeadmutedText,
            modifier = Modifier
                .size(18.dp)
                .clickable { onRemove() }
        )
    }
}

@Composable
private fun MiniNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isError) MaterialTheme.colorScheme.error else LeadmutedText
            )
            if (isError && errorMessage != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    errorMessage,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .border(
                    1.dp,
                    if (isError) MaterialTheme.colorScheme.error else grey_border,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF374151)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}