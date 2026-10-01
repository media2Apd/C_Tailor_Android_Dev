package com.cuso.tailor.view.home.inventory.auto_reorder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.CreateAutoReorderRuleRequest
import com.cuso.tailor.model.inventory.TriggerConditionPayload
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.InventoryViewModel

@Composable
fun CreateAutoReorderRuleScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCreateRuleSuccess: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // API state observers
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val warehouseList by viewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val supplierList by viewModel.supplierDropdown.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingAutoReorder.collectAsStateWithLifecycle()
    val actionError by viewModel.autoReorderActionError.collectAsStateWithLifecycle()

    // Handle API error messages
    LaunchedEffect(actionError) {
        actionError?.let {
            DynamicIslandManager.showError(it)
            viewModel.clearAutoReorderActionAlerts()
        }
    }

    // Fetch initial dropdown data
    LaunchedEffect(Unit) {
        viewModel.fetchInventoryItems(page = 1, limit = 100)
        viewModel.loadWarehouseDropdown()
        viewModel.fetchSupplierDropdown()
    }

    // Form inputs state
    var ruleName by remember { mutableStateOf("") }

    // Product state
    var selectedProductName by remember { mutableStateOf("") }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var isProductExpanded by remember { mutableStateOf(false) }

    // Warehouse state
    var selectedWarehouseName by remember { mutableStateOf("") }
    var selectedWarehouseId by remember { mutableStateOf<String?>(null) }
    var isWarehouseExpanded by remember { mutableStateOf(false) }

    // Select default warehouse when list is loaded
    LaunchedEffect(warehouseList) {
        if (selectedWarehouseName.isEmpty() && warehouseList.isNotEmpty()) {
            val defaultWh = warehouseList.first()
            selectedWarehouseName = defaultWh.label
            selectedWarehouseId = defaultWh.value
        }
    }

    // Trigger condition states
    var triggerOperator by remember { mutableStateOf("") }
    var isTriggerExpanded by remember { mutableStateOf(false) }
    var thresholdQty by remember { mutableStateOf("") }
    var reorderQty by remember { mutableStateOf("") }

    // Preferred supplier state
    val defaultSupplierLabel = "Falls back to item's preferred vendor"
    var selectedSupplierName by remember { mutableStateOf(defaultSupplierLabel) }
    var selectedSupplierId by remember { mutableStateOf<String?>(null) }
    var isSupplierExpanded by remember { mutableStateOf(false) }

    var isRuleActive by remember { mutableStateOf(true) }

    // Dropdown list representations
    val productOptions = remember(inventoryItems) {
        inventoryItems.map { it.name }
    }

    val warehouseOptions = remember(warehouseList) {
        warehouseList.map { it.label }
    }

    val supplierOptions = remember(supplierList) {
        listOf(defaultSupplierLabel) + supplierList.map { it.label }
    }

    val operatorOptions = remember {
        listOf("Less than (<)", "Less than or equal (<=)")
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            Row(Modifier.fillMaxWidth()) {
                TitleBar(
                    title = "Create Auto Re-Order",
                    onClose = onClose
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = tokens.screenPadding,
                    vertical = tokens.extraPadding
                ),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // 1. Rule Name
            Column {
                FormLabel(text = "Rule Name", isRequired = true)
                FormTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    placeholder = "e.g. Linen Shirt — Primary Hub",
                    textColor = TextLog,
                    placeholderColor = mutedText
                )
            }

            // 2. Product Dropdown
            FormDropdown(
                label = "Product",
                value = selectedProductName.ifEmpty { "Select product" },
                expanded = isProductExpanded,
                onExpandChange = { isProductExpanded = it },
                options = productOptions,
                isRequired = true,
                onOptionSelected = { chosenName ->
                    selectedProductName = chosenName
                    selectedItemId = inventoryItems.find { it.name == chosenName }?._id
                    if (ruleName.isBlank()) {
                        ruleName = "$chosenName — ${selectedWarehouseName.ifBlank { "Primary Hub" }}"
                    }
                }
            )

            // 3. Warehouse Dropdown
            FormDropdown(
                label = "Warehouse",
                value = selectedWarehouseName.ifEmpty { "Select warehouse" },
                expanded = isWarehouseExpanded,
                onExpandChange = { isWarehouseExpanded = it },
                options = warehouseOptions,
                isRequired = true,
                onOptionSelected = { chosenWarehouse ->
                    selectedWarehouseName = chosenWarehouse
                    selectedWarehouseId = warehouseList.find { it.label == chosenWarehouse }?.value
                }
            )

            // 4. Trigger Operator & Threshold Qty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FormDropdown(
                        label = "Trigger Operator",
                        value = triggerOperator,
                        expanded = isTriggerExpanded,
                        onExpandChange = { isTriggerExpanded = it },
                        options = operatorOptions,
                        onOptionSelected = { triggerOperator = it }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        FormLabel(text = "Threshold Qty", isRequired = true)
                        FormTextField(
                            value = thresholdQty,
                            onValueChange = { thresholdQty = it },
                            keyboardType = KeyboardType.Number,
                            placeholder = "0",
                            textColor = TextLog,
                            placeholderColor = mutedText
                        )
                    }
                }
            }

            // 5. Reorder Qty
            Column {
                FormLabel(text = "Reorder Qty", isRequired = true)
                FormTextField(
                    value = reorderQty,
                    onValueChange = { reorderQty = it },
                    keyboardType = KeyboardType.Number,
                    placeholder = "0",
                    textColor = TextLog,
                    placeholderColor = mutedText
                )
            }

            // 6. Preferred Supplier Dropdown
            FormDropdown(
                label = "Preferred Supplier (optional)",
                value = selectedSupplierName,
                expanded = isSupplierExpanded,
                onExpandChange = { isSupplierExpanded = it },
                options = supplierOptions,
                onOptionSelected = { chosenSupplier ->
                    selectedSupplierName = chosenSupplier
                    selectedSupplierId = if (chosenSupplier == defaultSupplierLabel) {
                        null
                    } else {
                        supplierList.find { it.label == chosenSupplier }?.value
                    }
                }
            )

            // 7. Rule Active Toggle Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        badgeGrey,
                        RoundedCornerShape(tokens.cardCornerRadius * 0.45f)
                    )
                    .padding(horizontal = tokens.screenPadding * 0.8f, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Rule Active",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Text(
                        text = "Inactive rules are skipped by the auto reorder engine",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                }

                Spacer(modifier = Modifier.width(tokens.extraPadding))

                MiniSwitch(
                    checked = isRuleActive,
                    onCheckedChange = { isRuleActive = it }
                )
            }

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            // Bottom Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = tokens.screenPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackFabButton(
                    label = "Cancel",
                    onClick = onClose,
                    showArrow = false
                )

                Spacer(Modifier.weight(1f))

                TrailingFabButton(
                    action = TrailingFabAction.Update(
                        isLoading = isSubmitting,
                        label = "Create Rule",
                        onClick = {
                            // Validations
                            if (ruleName.isBlank()) {
                                DynamicIslandManager.showError("Please enter rule name")
                                return@Update
                            }
                            if (selectedItemId.isNullOrBlank()) {
                                DynamicIslandManager.showError("Please select product")
                                return@Update
                            }
                            if (selectedWarehouseId.isNullOrBlank()) {
                                DynamicIslandManager.showError("Please select warehouse")
                                return@Update
                            }

                            val parsedThreshold = thresholdQty.trim().toIntOrNull() ?: 0
                            val parsedReorderQty = reorderQty.trim().toIntOrNull() ?: 0

                            if (parsedReorderQty <= 0) {
                                DynamicIslandManager.showError("Reorder Qty must be greater than 0")
                                return@Update
                            }

                            // Operator string to API symbol mapping
                            val operatorSymbol = when {
                                triggerOperator.contains("<=") -> "<="
                                triggerOperator.contains("<") -> "<"
                                triggerOperator.contains(">=") -> ">="
                                triggerOperator.contains(">") -> ">"
                                triggerOperator.contains("=") -> "=="
                                else -> "<"
                            }

                            // Payload matching exact API schema
                            val payload = CreateAutoReorderRuleRequest(
                                ruleName = ruleName.trim(),
                                itemId = selectedItemId!!,
                                warehouseId = selectedWarehouseId!!,
                                triggerCondition = TriggerConditionPayload(
                                    operator = operatorSymbol,
                                    thresholdQty = parsedThreshold
                                ),
                                reorderQty = parsedReorderQty,
                                supplierId = selectedSupplierId,
                                isActive = isRuleActive
                            )

                            // Trigger API request
                            viewModel.createAutoReorderRule(
                                request = payload,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Auto reorder rule created successfully")
                                    onCreateRuleSuccess()
                                }
                            )
                        }
                    ),
                    showArrow = false
                )
            }
        }
    }
}