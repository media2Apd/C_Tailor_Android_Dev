package com.cuso.tailor.view.home.finance.credit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.CreateCreditNoteLineRequest
import com.cuso.tailor.model.finance.CreateCreditNoteRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.toIsoDateTime
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.CustomerUiState
import com.cuso.tailor.viewmodel.CustomerViewModel
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Dynamic state model for each line item
data class LineItemFormState(
    val itemType: String = "", // "Retail Product" or "Garment Material / Fabric"
    val selectedItemId: String? = null,
    val itemName: String = "",
    val itemDescription: String = "",
    val reversalAccountId: String? = "",
    val reversalAccountName: String = "",
    val warehouseId: String? = null,
    val warehouseName: String = "",
    val binId: String? = null,
    val binName: String = "",
    val qty: String = "",
    val rate: String = "",
    val selectedTaxGroupId: String? = null,
    val taxGroupName: String = "",
    val taxRatePercent: Double = 0.0,
    val unit: String = "",
    val discountPercent: Double = 0.0
)

@Composable
fun CreateCreditNoteScreen(
    viewModel: FinanceViewModel,
    inventoryViewModel: InventoryViewModel,
    customerViewModel: CustomerViewModel,
    branchViewModel: BranchViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    // 1. Fetch dynamic data on launch
    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers(isRefresh = true)
        branchViewModel.loadBranches()
        inventoryViewModel.fetchAccessoryList() // Retail products (fabric = false)
        inventoryViewModel.fetchFabricList()    // Garment Materials / Fabrics (fabric = true)
        inventoryViewModel.fetchTaxGroups()
        inventoryViewModel.loadWarehouses()     // Loads Warehouses into warehouseUiState
        viewModel.fetchChartOfAccountsDropdown("sales_line")
    }

    // 2. Unpack UI states
    val customerUiState by customerViewModel.uiState.collectAsState()
    val customersList = remember(customerUiState) {
        (customerUiState as? CustomerUiState.Success)?.customers.orEmpty()
    }

    val branchUiState by branchViewModel.uiState.collectAsState()
    val branchesList = remember(branchUiState) {
        (branchUiState as? BranchUiState.Success)?.branches.orEmpty()
    }

    val accessoryItems by inventoryViewModel.accessoryItemList.collectAsState()
    val fabricItems by inventoryViewModel.fabricItemList.collectAsState()

    val warehouseUiState by inventoryViewModel.warehouseUiState.collectAsState()
    val warehousesList = remember(warehouseUiState) {
        warehouseUiState.warehouses
    }

    val taxGroups by inventoryViewModel.taxGroups.collectAsState()
    val accountsList by viewModel.accountDropdownList.collectAsState()

    // 3. Selection States
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    var selectedCustomerName by remember { mutableStateOf("Select Customer") }
    var isCustomerDropdownOpen by remember { mutableStateOf(false) }

    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var selectedBranchName by remember { mutableStateOf("Select Branch") }
    var isBranchDropdownOpen by remember { mutableStateOf(false) }

    val todayDateFormatted = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
    }
    var creditNoteDate by remember { mutableStateOf(todayDateFormatted) }
    var remarks by remember { mutableStateOf("") }

    val lineItems = remember { mutableStateListOf(LineItemFormState()) }

    // 4. Safe options lists for dropdowns
    val customerOptions = remember(customersList) {
        val names = customersList.map { it.name }
        names.ifEmpty { listOf("Select Customer") }
    }

    val branchOptions = remember(branchesList) {
        val names = branchesList.mapNotNull { it.name }
        names.ifEmpty { listOf("Select Branch") }
    }

    val itemTypeOptions = listOf("Retail Product", "Garment Material / Fabric")

    val accessoryOptions = remember(accessoryItems) {
        val names = accessoryItems.map { it.name }
        names.ifEmpty { listOf("Select Item") }
    }

    val fabricOptions = remember(fabricItems) {
        val names = fabricItems.map { it.name }
        names.ifEmpty { listOf("Select Fabric") }
    }

    val warehouseOptions = remember(warehousesList) {
        val names = warehousesList.map { it.name }
        names.ifEmpty { listOf("Select Warehouse") }
    }

    val taxGroupOptions = remember(taxGroups) {
        val names = taxGroups.map { it.name }
        names.ifEmpty { listOf("Tax (0%)") }
    }

    val accountOptions = remember(accountsList) {
        val names = accountsList.mapNotNull { it.accountName.ifBlank { null } }
        names.ifEmpty { listOf("Select Account") }
    }

    // 5. Dynamic Calculations
    val subtotal = lineItems.sumOf {
        val q = it.qty.toDoubleOrNull() ?: 0.0
        val r = it.rate.toDoubleOrNull() ?: 0.0
        q * r
    }
    val totalTax = lineItems.sumOf {
        val q = it.qty.toDoubleOrNull() ?: 0.0
        val r = it.rate.toDoubleOrNull() ?: 0.0
        (q * r) * (it.taxRatePercent / 100.0)
    }
    val totalCredits = subtotal + totalTax

    // Helper to construct the CreateCreditNoteRequest payload
    fun buildCreditNotePayload(): CreateCreditNoteRequest? {
        if (selectedCustomerId.isNullOrBlank()) return null

        return CreateCreditNoteRequest(
            customerId = selectedCustomerId!!,
            branchId = selectedBranchId,
            invoiceId = null,
            creditNoteDate = toIsoDateTime(creditNoteDate) ?: "",
            currency = "INR",
            exchangeRate = 1.0,
            notes = remarks,
            priceIncludesTax = false,
            lines = lineItems.map { line ->
                CreateCreditNoteLineRequest(
                    lineType = if (line.itemType == "Garment Material / Fabric") "Garment_Material" else "Retail_Product",
                    itemId = line.selectedItemId,
                    itemName = if (line.itemName != "Select Item" && line.itemName != "Select Fabric") line.itemName else "",
                    itemDescription = line.itemDescription.ifBlank { line.itemName },
                    discountPercent = line.discountPercent,
                    quantity = line.qty.toDoubleOrNull() ?: 1.0,
                    rate = line.rate.toDoubleOrNull() ?: 0.0,
                    reversalAccountId = line.reversalAccountId ?: "",
                    taxGroupId = line.selectedTaxGroupId,
                    unit = line.unit,
                    warehouseId = line.warehouseId,
                    warehouseBinId = line.binId
                )
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "Create Credit",
                onClose = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                .padding(bottom = tokens.screenPadding * 1.5f),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // =================================================================
            // Section 1: Customer, Branch, and Credit Note Date
            // =================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding * 0.7f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    FormDropdown(
                        label = "Customer",
                        isRequired = true,
                        value = selectedCustomerName,
                        expanded = isCustomerDropdownOpen,
                        onExpandChange = { isCustomerDropdownOpen = it },
                        options = customerOptions,
                        onOptionSelected = { chosenName ->
                            val chosenObj = customersList.find { it.name == chosenName }
                            selectedCustomerName = chosenName
                            selectedCustomerId = chosenObj?.id
                        }
                    )

                    FormDropdown(
                        label = "Branch Jurisdiction",
                        value = selectedBranchName,
                        expanded = isBranchDropdownOpen,
                        onExpandChange = { isBranchDropdownOpen = it },
                        options = branchOptions,
                        onOptionSelected = { chosenBranch ->
                            val chosenObj = branchesList.find { it.name == chosenBranch }
                            selectedBranchName = chosenBranch
                            selectedBranchId = chosenObj?.id
                        }
                    )

                    FormLabel(text = "Credit Note Date", isRequired = true)
                    DatePickerField(
                        value = creditNoteDate,
                        onDateSelected = { creditNoteDate = it }
                    )
                }
            }

            // =================================================================
            // Section 2: Credited Items & Services (Stacked Mobile Design)
            // =================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding * 0.7f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CREDITED ITEMS & SERVICES",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )
                        Text(
                            text = "+ Add Line Item",
                            fontSize = tokens.caption,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { lineItems.add(LineItemFormState()) }
                        )
                    }

                    // Dynamic Line Items rendered vertically as mobile sub-cards
                    lineItems.forEachIndexed { index, item ->
                        var itemTypeDropOpen by remember { mutableStateOf(false) }
                        var itemDropOpen by remember { mutableStateOf(false) }
                        var revAccountDropOpen by remember { mutableStateOf(false) }
                        var warehouseDropOpen by remember { mutableStateOf(false) }
                        var binDropOpen by remember { mutableStateOf(false) }
                        var taxDropOpen by remember { mutableStateOf(false) }

                        // Selected inventory item and associated warehouses & bins
                        val selectedInvItem = remember(accessoryItems, fabricItems, item.itemType, item.selectedItemId) {
                            if (item.itemType == "Retail Product") {
                                accessoryItems.find { it._id == item.selectedItemId || it.id == item.selectedItemId }
                            } else {
                                fabricItems.find { it._id == item.selectedItemId || it.id == item.selectedItemId }
                            }
                        }

                        val itemWarehouseDtos = remember(selectedInvItem) {
                            selectedInvItem?.warehouses.orEmpty()
                        }

                        val currentWarehouseOptions = remember(itemWarehouseDtos, warehouseOptions) {
                            val names = itemWarehouseDtos.mapNotNull { it.warehouseName.ifBlank { null } }
                            names.ifEmpty { warehouseOptions }
                        }

                        val selectedWarehouseDto = remember(itemWarehouseDtos, item.warehouseId, item.warehouseName) {
                            itemWarehouseDtos.find { it.warehouseId == item.warehouseId || it.warehouseName == item.warehouseName }
                        }

                        val binOptions = remember(selectedWarehouseDto) {
                            val bNames = selectedWarehouseDto?.bins?.mapNotNull { it.binName.ifBlank { null } }.orEmpty()
                            bNames.ifEmpty { listOf("Select Bin") }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            colors = CardDefaults.cardColors(containerColor = badgeGrey),
                            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(grey_border))
                        ) {
                            Column(
                                modifier = Modifier.padding(tokens.cardPadding * 0.6f),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                            ) {
                                // Item Card Header with Delete action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Item #${index + 1}",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Primary
                                    )

                                    if (lineItems.size > 1) {
                                        IconButton(
                                            onClick = { lineItems.removeAt(index) },
                                            modifier = Modifier.size(tokens.buttonHeight * 0.6f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove item",
                                                tint = redText,
                                                modifier = Modifier.size(tokens.iconSize)
                                            )
                                        }
                                    }
                                }

                                // 1. Item Type Dropdown
                                FormDropdown(
                                    label = "Item Type",
                                    isRequired = true,
                                    value = item.itemType,
                                    expanded = itemTypeDropOpen,
                                    onExpandChange = { itemTypeDropOpen = it },
                                    options = itemTypeOptions,
                                    onOptionSelected = { chosenType ->
                                        lineItems[index] = item.copy(
                                            itemType = chosenType,
                                            selectedItemId = null,
                                            itemName = if (chosenType == "Retail Product") "Select Item" else "Select Fabric",
                                            rate = "0"
                                        )
                                    }
                                )

                                // 2. Item Name Dropdown
                                val currentProductOptions = if (item.itemType == "Retail Product") accessoryOptions else fabricOptions
                                FormDropdown(
                                    label = if (item.itemType == "Retail Product") "Retail Product" else "Garment Material / Fabric",
                                    isRequired = true,
                                    value = item.itemName,
                                    expanded = itemDropOpen,
                                    onExpandChange = { itemDropOpen = it },
                                    options = currentProductOptions,
                                    onOptionSelected = { chosenName ->
                                        if (item.itemType == "Retail Product") {
                                            val chosen = accessoryItems.find { it.name == chosenName }
                                            val defaultWh = chosen?.warehouses?.firstOrNull()
                                            val defaultBin = defaultWh?.bins?.firstOrNull()
                                            lineItems[index] = item.copy(
                                                itemName = chosenName,
                                                selectedItemId = chosen?._id ?: chosen?.id,
                                                itemDescription = chosenName,
                                                rate = chosen?.sellingPrice?.toString() ?: "0",
                                                unit = chosen?.unit ?: "Pieces",
                                                warehouseId = defaultWh?.warehouseId ?: item.warehouseId,
                                                warehouseName = defaultWh?.warehouseName ?: item.warehouseName,
                                                binId = defaultBin?.binId ?: item.binId,
                                                binName = defaultBin?.binName ?: item.binName
                                            )
                                        } else {
                                            val chosen = fabricItems.find { it.name == chosenName }
                                            val defaultWh = chosen?.warehouses?.firstOrNull()
                                            val defaultBin = defaultWh?.bins?.firstOrNull()
                                            lineItems[index] = item.copy(
                                                itemName = chosenName,
                                                selectedItemId = chosen?._id ?: chosen?.id,
                                                itemDescription = chosenName,
                                                rate = chosen?.sellingPrice?.toString() ?: "0",
                                                unit = chosen?.unit ?: "Meters",
                                                warehouseId = defaultWh?.warehouseId ?: item.warehouseId,
                                                warehouseName = defaultWh?.warehouseName ?: item.warehouseName,
                                                binId = defaultBin?.binId ?: item.binId,
                                                binName = defaultBin?.binName ?: item.binName
                                            )
                                        }
                                    }
                                )

                                // 3. Revenue Reversal Account Dropdown
                                FormDropdown(
                                    label = "Revenue Reversal Acct",
                                    isRequired = true,
                                    value = item.reversalAccountName,
                                    expanded = revAccountDropOpen,
                                    onExpandChange = { revAccountDropOpen = it },
                                    options = accountOptions,
                                    onOptionSelected = { chosenAccName ->
                                        val chosenAcc = accountsList.find { it.accountName == chosenAccName }
                                        lineItems[index] = item.copy(
                                            reversalAccountName = chosenAccName,
                                            reversalAccountId = chosenAcc?.id
                                        )
                                    }
                                )

                                // 4. Warehouse & Bin Dropdowns
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Warehouse",
                                            value = item.warehouseName,
                                            expanded = warehouseDropOpen,
                                            onExpandChange = { warehouseDropOpen = it },
                                            options = currentWarehouseOptions,
                                            onOptionSelected = { chosenWhName ->
                                                val chosenWhDto = itemWarehouseDtos.find { it.warehouseName == chosenWhName }
                                                if (chosenWhDto != null) {
                                                    val firstBin = chosenWhDto.bins.firstOrNull()
                                                    lineItems[index] = item.copy(
                                                        warehouseName = chosenWhName,
                                                        warehouseId = chosenWhDto.warehouseId,
                                                        binId = firstBin?.binId,
                                                        binName = firstBin?.binName ?: "Select Bin"
                                                    )
                                                } else {
                                                    val chosenWhGlobal = warehousesList.find { it.name == chosenWhName }
                                                    lineItems[index] = item.copy(
                                                        warehouseName = chosenWhName,
                                                        warehouseId = chosenWhGlobal?.id,
                                                        binId = null,
                                                        binName = "Select Bin"
                                                    )
                                                }
                                            }
                                        )
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Bin",
                                            value = item.binName,
                                            expanded = binDropOpen,
                                            onExpandChange = { binDropOpen = it },
                                            options = binOptions,
                                            onOptionSelected = { chosenBinName ->
                                                val chosenBin = selectedWarehouseDto?.bins?.find { it.binName == chosenBinName }
                                                lineItems[index] = item.copy(
                                                    binName = chosenBinName,
                                                    binId = chosenBin?.binId
                                                )
                                            }
                                        )
                                    }
                                }

                                // 5. Quantity & Rate Fields
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormLabel(text = "Qty", isRequired = true)
                                        FormTextField(
                                            value = item.qty,
                                            onValueChange = { newQty ->
                                                lineItems[index] = item.copy(qty = newQty)
                                            },
                                            keyboardType = KeyboardType.Number,
                                            borderColor = grey_border,
                                            containerColor = whiteBg
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        FormLabel(text = "Rate (₹)", isRequired = true)
                                        FormTextField(
                                            value = item.rate,
                                            onValueChange = { newRate ->
                                                lineItems[index] = item.copy(rate = newRate)
                                            },
                                            keyboardType = KeyboardType.Number,
                                            borderColor = grey_border,
                                            containerColor = whiteBg
                                        )
                                    }
                                }

                                // 6. Tax Group Dropdown
                                FormDropdown(
                                    label = "Tax Group",
                                    value = item.taxGroupName,
                                    expanded = taxDropOpen,
                                    onExpandChange = { taxDropOpen = it },
                                    options = taxGroupOptions,
                                    onOptionSelected = { chosenTaxName ->
                                        val chosenTax = taxGroups.find { it.name == chosenTaxName }
                                        lineItems[index] = item.copy(
                                            taxGroupName = chosenTaxName,
                                            selectedTaxGroupId = chosenTax?.id,
                                            taxRatePercent = chosenTax?.totalRate ?: 0.0
                                        )
                                    }
                                )

                                // Item Line Total
                                val lineTotal = (item.qty.toDoubleOrNull() ?: 0.0) * (item.rate.toDoubleOrNull() ?: 0.0)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = tokens.extraPadding * 0.4f),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Item Total: ₹${String.format(Locale.US, "%.2f", lineTotal)}",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = TitleColor
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "+ Add New Line",
                        fontSize = tokens.bodySmall,
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { lineItems.add(LineItemFormState()) }
                            .padding(vertical = tokens.extraPadding * 0.4f)
                    )
                }
            }

            // =================================================================
            // Section 3: Reason for Credit / Remarks
            // =================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
            ) {
                Column(modifier = Modifier.padding(tokens.cardPadding * 0.7f)) {
                    Text(
                        text = "Reason for Credit / Remarks",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    FormTextArea(
                        value = remarks,
                        onValueChange = { remarks = it },
                        placeholder = "State reason for credit note (defective fabric, alteration discount, billing error)...",
                        borderColor = grey_border,
                        textColor = TitleColor
                    )
                }
            }

            // =================================================================
            // Section 4: Credit Note Summary Card & GL Info
            // =================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
            ) {
                Column(modifier = Modifier.padding(tokens.cardPadding * 0.7f)) {
                    Text(
                        text = "CREDIT NOTE SUMMARY",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sub Total:", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", subtotal)}",
                            fontSize = tokens.bodySmall,
                            color = TitleColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax:", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text(
                            text = "+₹${String.format(Locale.US, "%.2f", totalTax)}",
                            fontSize = tokens.bodySmall,
                            color = greentext,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = tokens.extraPadding), color = dividerColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL CREDITS:",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", totalCredits)}",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                    // Ledger Info Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(primary_light)
                            .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(tokens.cardPadding * 0.6f)
                    ) {
                        Text(
                            text = "Posting this credit note debits Income & Tax liabilities and credits Accounts Receivable in the General Ledger.",
                            fontSize = tokens.caption,
                            color = darkPurple,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.4f))

            // =================================================================
            // Section 5: Form Action Buttons (Cancel, Save Draft, Create & Send)
            // =================================================================
            FormActionButtons(
                cancelText = "Cancel",
                draftText = "Save Draft",
                primaryText = "Create & Send",
                onCancel = onBack,
                onSaveDraft = {
                    val payload = buildCreditNotePayload() ?: return@FormActionButtons
                    viewModel.createCreditNote(payload) { onBack() }
                },
                onPrimaryClick = {
                    val payload = buildCreditNotePayload() ?: return@FormActionButtons
                    viewModel.createAndSendCreditNote(payload) { onBack() }
                },
                isLoading = isSubmitting,
                isDraftEnabled = !selectedCustomerId.isNullOrBlank() && !isSubmitting,
                isPrimaryEnabled = !selectedCustomerId.isNullOrBlank() && !isSubmitting,
                buttonHeight = tokens.buttonHeight,
                cornerRadius = tokens.cardCornerRadius * 0.6f,
                borderColor = BorderGray,
                primaryColor = Primary
            )
        }
    }
}