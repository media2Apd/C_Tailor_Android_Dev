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
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.home.sales.lead.LeadAccordionSection
import com.cuso.mobile.view.home.sales.lead.LeadFormTopBar
import com.cuso.mobile.view.home.LeadPrimary
import com.cuso.mobile.view.home.LeadTextMuted
import com.cuso.mobile.view.home.reusablecomposables.StepNavigationFab
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.viewmodel.GarmentPricingDetailUiState
import com.cuso.mobile.viewmodel.PricingQuotationViewModel
import com.cuso.mobile.viewmodel.SalesViewModel

// ── Simple row models for the dynamic lists ──
private data class PriceAdjustmentRow(val id: Int, val name: String, val price: String)
private data class DiscountRuleRow(val id: Int, val minQuantity: String, val discountPercent: String)

@Composable
fun AddGarmentPricingScreen(
    onClose: () -> Unit,
    onSave: () -> Unit = {},
    pricingId: String? = null   // ✅ null = Add mode, non-null = Edit mode
) {
    val isEditMode = pricingId != null

    val salesViewModel: SalesViewModel = hiltViewModel()
    val pricingViewModel: PricingQuotationViewModel = hiltViewModel()

    // Collect state from ViewModels
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()
    val saveState by pricingViewModel.saveState.collectAsStateWithLifecycle()
    val detailState by pricingViewModel.garmentPricingDetailState.collectAsStateWithLifecycle()

    val garmentOptions = garmentCategories.map { it.categoryId.categoryName }
    // ── Show error snackbar state ──


// ── Field-level validation errors ──   ✅ ADD THESE TWO LINES
    var showGarmentTypeError by remember { mutableStateOf(false) }
    var showBasePriceError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
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

    // ── id counter for dynamic rows ──
    var nextId by remember { mutableIntStateOf(1) }
    fun newId(): Int { val id = nextId; nextId++; return id }

    // ── Fabric Price Adjustments ──
    var fabricRows by remember {
        mutableStateOf(emptyList<PriceAdjustmentRow>())
    }

    // ── Design / Style Options ──
    var styleRows by remember {
        mutableStateOf(emptyList<PriceAdjustmentRow>())
    }

    // ── Additional Charges ──
    var additionalChargeRows by remember {
        mutableStateOf(emptyList<PriceAdjustmentRow>())
    }

    // ── Quantity Discount Rules ──
    var discountRules by remember {
        mutableStateOf(emptyList<DiscountRuleRow>())
    }

    var expandedSection by remember { mutableStateOf("basic_info") }

    // ── Show error snackbar state ──
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // ── Track whether we've already prefilled fields from detail response ──
    var prefilled by remember { mutableStateOf(false) }
    var isLoadingDetail by remember { mutableStateOf(isEditMode) }

    // ── Prefill fields once detail data arrives (edit mode only) ──
    LaunchedEffect(detailState, garmentCategories) {
        if (isEditMode && !prefilled) {
            when (val ds = detailState) {
                is GarmentPricingDetailUiState.Success -> {
                    val detail = ds.detail

                    selectedGarmentCategoryId = detail.applicableGarmentId
                    // ✅ Category label resolves once garmentCategories loads;
                    // falls back gracefully instead of blocking the whole prefill.
                    garmentType = garmentCategories
                        .firstOrNull { it.id == detail.applicableGarmentId }
                        ?.categoryId?.categoryName
                        ?: garmentType

                    baseStitchingPrice = detail.basePrice.toString()

                    if (detail.fabricAdjustments.isNotEmpty()) {
                        fabricRows = detail.fabricAdjustments.map {
                            PriceAdjustmentRow(newId(), it.name, it.price.toString())
                        }
                    }
                    if (detail.designAdjustments.isNotEmpty()) {
                        styleRows = detail.designAdjustments.map {
                            PriceAdjustmentRow(newId(), it.name, it.price.toString())
                        }
                    }
                    if (detail.additionalCharges.isNotEmpty()) {
                        additionalChargeRows = detail.additionalCharges.map {
                            PriceAdjustmentRow(newId(), it.name, it.price.toString())
                        }
                    }
                    if (detail.bulkRules.isNotEmpty()) {
                        discountRules = detail.bulkRules.map {
                            DiscountRuleRow(newId(), it.minQuantity.toString(), it.discountPercent.toString())
                        }
                    }

                    // ✅ Only mark fully-prefilled (and stop the loader) once categories
                    // are also available, so the dropdown label gets one more chance
                    // to resolve on the next recomposition if it isn't ready yet.
                    if (garmentCategories.isNotEmpty()) {
                        prefilled = true
                    }
                    isLoadingDetail = false
                }
                is GarmentPricingDetailUiState.Error -> {
                    errorMessage = ds.message
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
        // ✅ Reset field errors first
        showGarmentTypeError = false
        showBasePriceError = false

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
            expandedSection = "basic_info"   // ✅ auto-open Basic Information
            return
        }

        // Validate discount rules - ensure minQuantity is at least 1
        // ✅ Discount rules are OPTIONAL — only validate rows the user actually added.
// Rows left at "0" or otherwise incomplete are silently dropped, not blocked.
        val invalidRules = discountRules.filter {
            it.minQuantity.isNotBlank() && (it.minQuantity.toIntOrNull() ?: 0) < 1
        }
        if (invalidRules.isNotEmpty()) {
            errorMessage = "Minimum quantity must be at least 1 for all discount rules"
            showError = true
            expandedSection = "discount_rules"
            return
        }

// Convert UI data to DTOs
        val fabricAdjustments = fabricRows.map {
            PriceAdjustmentDto(name = it.name, price = it.price.toDoubleOrNull() ?: 0.0)
        }
        val designAdjustments = styleRows.map {
            PriceAdjustmentDto(name = it.name, price = it.price.toDoubleOrNull() ?: 0.0)
        }
        val additionalCharges = additionalChargeRows.map {
            PriceAdjustmentDto(name = it.name, price = it.price.toDoubleOrNull() ?: 0.0)
        }
        val bulkRules = discountRules.mapNotNull { rule ->
            val minQty = rule.minQuantity.toIntOrNull()
            val discount = rule.discountPercent.toDoubleOrNull()
            if (minQty != null && minQty >= 1 && discount != null) {
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
                onSave()
                pricingViewModel.resetSaveState()
            }
            saveState.errorMessage != null -> {
                errorMessage = saveState.errorMessage!!
                showError = true
                pricingViewModel.resetSaveState()
            }
        }
    }

    // ── Full-screen loader while fetching detail in edit mode ──
    if (isEditMode && isLoadingDetail) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CirculerProgressIndicatorReuse()
                Spacer(Modifier.height(8.dp))
                Text("Loading pricing details...", color = Color.Gray, fontSize = 14.sp)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            contentWindowInsets = WindowInsets(0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LeadFormTopBar(
                    title = if (isEditMode) "Edit Pricing" else "Add New Garment Pricing",
                    badgeText = "",
                    onClose = onClose
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    // ── 1. Basic Information ──
                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Description,
                            title = "Basic Information",
                            subtitle = "",
                            expanded = expandedSection == "basic_info",
                            onExpandChange = { expandedSection = if (expandedSection == "basic_info") "" else "basic_info" }
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
                                        showGarmentTypeError = false   // ✅ clear error once user fixes it
                                    },
                                    isRequired = true,
                                    isError = showGarmentTypeError,
                                    errorMessage = "Please select a garment type"
                                )
                                // ✅ Edit mode: block all touches on the dropdown so it can't be changed
                                if (isEditMode) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) { /* consume click, do nothing */ }
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Base stitching price", isRequired = true)
                            FormTextField(
                                value = baseStitchingPrice,
                                onValueChange = {
                                    baseStitchingPrice = it
                                    showBasePriceError = false   // ✅ clear error once user types
                                },
                                keyboardType = KeyboardType.Number,
                                placeholder = "Standard stitching charge",
                                isError = showBasePriceError,
                                errorMessage = "Please enter base stitching price"
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }

                    // ── 2. Fabric Price Adjustments ──
                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Checkroom,
                            title = "Fabric Price Adjustments",
                            subtitle = "",
                            expanded = expandedSection == "fabric",
                            onExpandChange = { expandedSection = if (expandedSection == "fabric") "" else "fabric" }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        fabricRows = fabricRows + PriceAdjustmentRow(newId(), "", "0")
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
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }

                    // ── 3. Design / Style Options ──
                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Style,
                            title = "Design / Style Options",
                            subtitle = "",
                            expanded = expandedSection == "style",
                            onExpandChange = { expandedSection = if (expandedSection == "style") "" else "style" }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        styleRows = styleRows + PriceAdjustmentRow(newId(), "", "0")
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
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }

                    // ── 4. Additional Charges ──
                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.AttachMoney,
                            title = "Additional Charges",
                            subtitle = "",
                            expanded = expandedSection == "charges",
                            onExpandChange = { expandedSection = if (expandedSection == "charges") "" else "charges" }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        additionalChargeRows = additionalChargeRows + PriceAdjustmentRow(newId(), "", "0")
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
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }

                    // ── 5. Quantity Discount Rules ──
                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.LocalOffer,
                            title = "Quantity Discount Rules",
                            subtitle = "",
                            expanded = expandedSection == "discount_rules",
                            onExpandChange = { expandedSection = if (expandedSection == "discount_rules") "" else "discount_rules" }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (showError && errorMessage.isNotEmpty()) {
                                    Text(errorMessage, fontSize = 12.sp, color = Color(0xFFEF4444))
                                }
                                OutlinedButton(
                                    onClick = {
                                        discountRules = discountRules + DiscountRuleRow(newId(), "0", "0")
                                        showError = false   // ✅ clear once user adds a rule

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
    }



}

// ── PriceAdjustmentField with null-safety ──
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
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
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
                    Text("Enter name", fontSize = 14.sp, color = LeadTextMuted)
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
                modifier = Modifier.width(40.dp)
            )
            if (onRemove != null) {
                Spacer(Modifier.width(10.dp))
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove row",
                    tint = LeadTextMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRemove() }
                )
            }
        }
    }
}

// ── DiscountRuleField with null-safety ──
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
            tint = LeadTextMuted,
            modifier = Modifier
                .size(18.dp)
                .clickable { onRemove() }
        )
    }
}

// ── MiniNumberField with null-safety ──
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
            // ✅ Null-safe handling for label
            Text(
                text = label ,
                fontSize = 11.sp,
                color = if (isError) MaterialTheme.colorScheme.error else LeadTextMuted
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
                    if (isError) MaterialTheme.colorScheme.error else Color(0xFFE5E7EB),
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