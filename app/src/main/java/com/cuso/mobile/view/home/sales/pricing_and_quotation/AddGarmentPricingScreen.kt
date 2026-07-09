package com.cuso.mobile.view.home.sales.pricing_and_quotation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.BulkRuleDto
import com.cuso.mobile.model.PriceAdjustmentDto
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.home.LeadAccordionSection
import com.cuso.mobile.view.home.LeadFormTopBar
import com.cuso.mobile.view.home.LeadPrimary
import com.cuso.mobile.view.home.LeadTextMuted
import com.cuso.mobile.view.home.reusablecomposables.StepNavigationFab
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.viewmodel.PricingQuotationViewModel
import com.cuso.mobile.viewmodel.SalesViewModel

// ── Simple row models for the dynamic lists ──
private data class PriceAdjustmentRow(val id: Int, val name: String, val price: String)
private data class DiscountRuleRow(val id: Int, val minQuantity: String, val discountPercent: String)

@Composable
fun AddGarmentPricingScreen(
    onClose: () -> Unit,
    onSave: () -> Unit = {}
) {
    val salesViewModel: SalesViewModel = hiltViewModel()
    val pricingViewModel: PricingQuotationViewModel = hiltViewModel()

    // Collect state from ViewModels
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()
    val saveState by pricingViewModel.saveState.collectAsStateWithLifecycle()

    val garmentOptions = garmentCategories.map { it.categoryId.categoryName }

    LaunchedEffect(Unit) {
        if (garmentCategories.isEmpty()) salesViewModel.fetchGarmentCategories()
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
        mutableStateOf(
            listOf(
                PriceAdjustmentRow(newId(), "Linen", "150"),
                PriceAdjustmentRow(newId(), "Silk", "150")
            )
        )
    }

    // ── Design / Style Options ──
    var styleRows by remember {
        mutableStateOf(
            listOf(
                PriceAdjustmentRow(newId(), "Slim Fit", "150"),
                PriceAdjustmentRow(newId(), "Designer Collar", "150"),
                PriceAdjustmentRow(newId(), "Embroidery Work", "150")
            )
        )
    }

    // ── Additional Charges ──
    var additionalChargeRows by remember {
        mutableStateOf(
            listOf(PriceAdjustmentRow(newId(), "Express Delivery Charges", "150"))
        )
    }

    // ── Quantity Discount Rules ──
    var discountRules by remember {
        mutableStateOf(
            listOf(
                DiscountRuleRow(newId(), "1", "0"),
                DiscountRuleRow(newId(), "1", "0")
            )
        )
    }

    var expandedSection by remember { mutableStateOf("basic_info") }

    // ── Show error snackbar state ──
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun updatePrice(rows: List<PriceAdjustmentRow>, id: Int, newValue: String): List<PriceAdjustmentRow> =
        rows.map { if (it.id == id) it.copy(price = newValue) else it }

    // ── Handle save action ──
// ── Handle save action ──
    fun handleSave() {
        // Validate required fields
        if (selectedGarmentCategoryId.isEmpty()) {
            errorMessage = "Please select a garment type"
            showError = true
            return
        }
        if (baseStitchingPrice.isEmpty()) {
            errorMessage = "Please enter base stitching price"
            showError = true
            return
        }

        // Validate discount rules - ensure minQuantity is at least 1
        val invalidRules = discountRules.filter {
            it.minQuantity.toIntOrNull()?.let { it < 1 } ?: true
        }
        if (invalidRules.isNotEmpty()) {
            errorMessage = "Minimum quantity must be at least 1 for all discount rules"
            showError = true
            return
        }

        // Convert UI data to DTOs
        val fabricAdjustments = fabricRows.map {
            PriceAdjustmentDto(
                name = it.name,
                price = it.price.toDoubleOrNull() ?: 0.0
            )
        }

        val designAdjustments = styleRows.map {
            PriceAdjustmentDto(
                name = it.name,
                price = it.price.toDoubleOrNull() ?: 0.0
            )
        }

        val additionalCharges = additionalChargeRows.map {
            PriceAdjustmentDto(
                name = it.name,
                price = it.price.toDoubleOrNull() ?: 0.0
            )
        }

        val bulkRules = discountRules.mapNotNull { rule ->
            val minQty = rule.minQuantity.toIntOrNull()
            val discount = rule.discountPercent.toDoubleOrNull()

            // Only include rules with valid values
            if (minQty != null && minQty >= 1 && discount != null) {
                BulkRuleDto(
                    minQuantity = minQty,
                    discountPercent = discount
                )
            } else {
                null // Skip invalid rules
            }
        }

        // Don't proceed if there are no valid bulk rules
        if (bulkRules.isEmpty()) {
            errorMessage = "Please add at least one valid discount rule"
            showError = true
            return
        }

        // Call ViewModel to save
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
    // ── Observe save result ──
    LaunchedEffect(saveState) {
        when {
            saveState.isSuccess && saveState.response != null -> {
                onSave() // Navigate back or show success
                pricingViewModel.resetSaveState()
            }
            saveState.errorMessage != null -> {
                errorMessage = saveState.errorMessage!!
                showError = true
                pricingViewModel.resetSaveState()
            }
        }
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
                    title = "Add New Garment Pricing",
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
                            FormDropdown(
                                label = "Garment Type",
                                value = garmentType.ifEmpty { "Select the type of garment" },
                                expanded = garmentTypeExpanded,
                                onExpandChange = { garmentTypeExpanded = it },
                                options = garmentOptions,
                                onOptionSelected = { selectedName ->
                                    garmentType = selectedName
                                    selectedGarmentCategoryId = garmentCategories
                                        .firstOrNull { it.categoryId.categoryName == selectedName }
                                        ?.categoryId?.id ?: ""
                                }
                            )
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Base stitching price")
                            FormTextField(
                                value = baseStitchingPrice,
                                onValueChange = { baseStitchingPrice = it },
                                keyboardType = KeyboardType.Number,
                                placeholder = "Standard stitching charge"
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
                                fabricRows.forEach { row ->
                                    PriceAdjustmentField(
                                        name = row.name,
                                        price = row.price,
                                        onPriceChange = { fabricRows = updatePrice(fabricRows, row.id, it) }
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
                                styleRows.forEach { row ->
                                    PriceAdjustmentField(
                                        name = row.name,
                                        price = row.price,
                                        onPriceChange = { styleRows = updatePrice(styleRows, row.id, it) }
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
                                additionalChargeRows.forEach { row ->
                                    PriceAdjustmentField(
                                        name = row.name,
                                        price = row.price,
                                        onPriceChange = { additionalChargeRows = updatePrice(additionalChargeRows, row.id, it) }
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
                                OutlinedButton(
                                    onClick = {
                                        discountRules = discountRules + DiscountRuleRow(newId(), "0", "0")
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

        // ── StepNavigationFab with save logic ──
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            trailingAction = TrailingFabAction.Update(
                isLoading = saveState.isLoading,
                label = "Save",
                onClick = { handleSave() }
            )
        )
    }

    // ── Error Snackbar ──
    if (showError) {
        Snackbar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            action = {
                TextButton(onClick = { showError = false }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(errorMessage)
        }
    }
}

// ── PriceAdjustmentField ──
@Composable
private fun PriceAdjustmentField(
    name: String,
    price: String,
    onPriceChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontSize = 14.sp, color = Color(0xFF374151))
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
        }
    }
}

// ── DiscountRuleField ──
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
        // Validate min quantity
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

// ── MiniNumberField ──
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
            Text(label, fontSize = 11.sp, color = if (isError) MaterialTheme.colorScheme.error else LeadTextMuted)
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