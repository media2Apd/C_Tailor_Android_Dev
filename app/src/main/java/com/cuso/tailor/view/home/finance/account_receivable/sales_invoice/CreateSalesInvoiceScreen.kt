@file:Suppress("unused", "unusedVariable", "AssignedValueIsNeverRead", "VariableNeverRead")
package com.cuso.tailor.view.home.finance.account_receivable.sales_invoice

import com.cuso.tailor.utils.currencySymbol

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.CreateInvoiceLineItemRequest
import com.cuso.tailor.model.sales.OrderOverviewData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.currencySymbol
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.*
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────
// Line Item Model for Itemized Sales Lines with API IDs
// ─────────────────────────────────────────────────────────────
data class SalesLineRowState(
    val id: String = UUID.randomUUID().toString(),
    var itemId: String = "",
    var itemName: String = "Select Item",
    var incomeAccountId: String = "",
    var incomeAccountName: String = "Select Account",
    var quantity: String = "1",
    var rate: String = "0",
    var taxGroupId: String = "",
    var taxGroupName: String = "Tax (0%)",
    var taxRatePercentage: Double = 0.0
) {
    val lineAmount: Double
        get() {
            val qty = quantity.toDoubleOrNull() ?: 0.0
            val r = rate.toDoubleOrNull() ?: 0.0
            return qty * r
        }

    val taxAmount: Double
        get() = lineAmount * (taxRatePercentage / 100.0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSalesInvoiceScreen(
    onClose: () -> Unit,
    prefillOrder: OrderOverviewData? = null,
    onInvoiceCreatedSuccessfully: () -> Unit = {},
    customerViewModel: CustomerViewModel = hiltViewModel(),
    financeViewModel: FinanceViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Date Formatters
    val calendar = remember { Calendar.getInstance() }
    val displayDateFormatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    // Helper: parse ISO dates to dd-MM-yyyy
    fun formatToDisplayDate(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) return ""
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
            )
            var parsed: Date? = null
            for (pattern in inputFormats) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    parsed = sdf.parse(rawDate)
                    if (parsed != null) break
                } catch (_: Exception) {}
            }
            parsed?.let { displayDateFormatter.format(it) } ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    // ──────────────── PREFILLED FORM STATES ────────────────
    var selectedCustomerId by remember(prefillOrder) {
        mutableStateOf<String?>(
            prefillOrder?.customerId?._id
                ?: prefillOrder?.customerSnapshot?.customerCode
                ?: prefillOrder?.order?.customerId?._id
        )
    }

    var customerDisplayName by remember(prefillOrder) {
        val name = prefillOrder?.customerSnapshot?.name
            ?: prefillOrder?.order?.customerId?.name
            ?: ""
        val phone = prefillOrder?.customerSnapshot?.phone
            ?: prefillOrder?.order?.customerId?.mobile
            ?: ""
        mutableStateOf(if (name.isNotBlank() && phone.isNotBlank()) "$name ($phone)" else name)
    }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var invoiceNumber by remember { mutableStateOf("INV-AUTO") }

    // Prefill Reference / Order Number
    var referenceOrderNumber by remember(prefillOrder) {
        mutableStateOf(
            prefillOrder?.orderCode?.takeIf { it.isNotBlank() }
                ?: prefillOrder?.order?.orderNumber
                ?: ""
        )
    }

    var selectedPaymentTermId by remember(prefillOrder) {
        mutableStateOf(prefillOrder?.paymentTermId?.id)
    }
    var paymentTermsDisplayName by remember(prefillOrder) {
        mutableStateOf(prefillOrder?.paymentTermId?.name ?: "")
    }
    var paymentTermsExpanded by remember { mutableStateOf(false) }

    // Prefill Due Date from order dueDate or deliveryDate
    var dueDate by remember(prefillOrder) {
        val prefillDueDate = formatToDisplayDate(prefillOrder?.dueDate ?: prefillOrder?.order?.deliveryDate)
        mutableStateOf(if (prefillDueDate.isNotBlank()) prefillDueDate else displayDateFormatter.format(calendar.time))
    }

    // Prefill Itemized Sales Lines from order items
    val salesLines = remember(prefillOrder) {
        val list = mutableStateListOf<SalesLineRowState>()
        if (prefillOrder != null && prefillOrder.items.isNotEmpty()) {
            prefillOrder.items.forEach { item ->
                list.add(
                    SalesLineRowState(
                        itemId = item._id,
                        itemName = item.categoryName.ifBlank { item.itemDescription.orEmpty() },
                        quantity = item.quantity.toString(),
                        rate = (item.unitPrice ?: item.stitchingCharge).toString(),
                        taxGroupId = item.taxGroupId?.id.orEmpty(),
                        taxGroupName = item.taxGroupId?.name ?: "GST 5% (CGST 2.5% + SGST 2.5%)",
                        taxRatePercentage = item.taxGroupId?.totalRate ?: 5.0,
                        incomeAccountId = "",
                        incomeAccountName = "Sales Revenue"
                    )
                )
            }
        } else {
            list.add(SalesLineRowState())
        }
        list
    }

    // ──────────────── API STATES OBSERVATION ────────────────
    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val customersList = remember(customerUiState) {
        (customerUiState as? CustomerUiState.Success)?.customers ?: emptyList()
    }

    val accountDropdownList by financeViewModel.accountDropdownList.collectAsStateWithLifecycle()
    val fabricItemList by inventoryViewModel.fabricItemList.collectAsStateWithLifecycle()
    val taxGroupsList by inventoryViewModel.taxGroups.collectAsStateWithLifecycle()
    val paymentTermsList by financeViewModel.paymentTermsList.collectAsStateWithLifecycle()

    val createInvoiceState by financeViewModel.createInvoiceState.collectAsStateWithLifecycle()

    // Fetch API Data on screen launch
    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers(isRefresh = true)
        financeViewModel.fetchChartOfAccountsDropdown(context = "sales_line")
        financeViewModel.fetchPaymentTerms()
        inventoryViewModel.fetchFabricList()
        inventoryViewModel.fetchTaxGroups()
    }

    // Match customer from customer list once loaded
    LaunchedEffect(customersList, prefillOrder) {
        if (customersList.isNotEmpty() && prefillOrder != null) {
            val targetId = prefillOrder.customerId?._id ?: prefillOrder.order.customerId?._id
            val targetCode = prefillOrder.customerSnapshot?.customerCode
            val targetName = prefillOrder.customerSnapshot?.name ?: prefillOrder.order.customerId?.name

            val matchedCustomer = customersList.find { c ->
                c.id == targetId || c.customerCode == targetCode || c.name.equals(targetName, ignoreCase = true)
            }

            if (matchedCustomer != null) {
                selectedCustomerId = matchedCustomer.id
                val mob = matchedCustomer.mobile?.trim().orEmpty()
                customerDisplayName = if (mob.isNotBlank()) "${matchedCustomer.name.trim()} ($mob)" else matchedCustomer.name.trim()
            }
        }
    }

    // Match default income accounts for item rows once loaded
    LaunchedEffect(accountDropdownList) {
        if (accountDropdownList.isNotEmpty()) {
            val defaultAccount = accountDropdownList.find { it.accountName.contains("Sales", ignoreCase = true) }
                ?: accountDropdownList.first()
            salesLines.forEach { line ->
                if (line.incomeAccountId.isBlank()) {
                    line.incomeAccountId = defaultAccount.id
                    line.incomeAccountName = defaultAccount.displayName.ifBlank { defaultAccount.accountName }
                }
            }
        }
    }

    // Match default tax groups for item rows once loaded
    LaunchedEffect(taxGroupsList) {
        if (taxGroupsList.isNotEmpty()) {
            val defaultTax = taxGroupsList.find { it.isDefault } ?: taxGroupsList.first()
            salesLines.forEach { line ->
                if (line.taxGroupId.isBlank()) {
                    line.taxGroupId = defaultTax.id
                    line.taxGroupName = defaultTax.name
                    line.taxRatePercentage = defaultTax.totalRate
                }
            }
        }
    }

    // Listen for invoice creation API response
    LaunchedEffect(createInvoiceState) {
        when (val state = createInvoiceState) {
            is CreateInvoiceState.Success -> {
                DynamicIslandManager.showSuccess(state.message)
                financeViewModel.resetCreateInvoiceState()
                onInvoiceCreatedSuccessfully()
                onClose()
            }
            is CreateInvoiceState.Error -> {
                DynamicIslandManager.showError(state.message)
                financeViewModel.resetCreateInvoiceState()
            }
            else -> Unit
        }
    }

    // Auto-select payment term from API if not already set by prefill
    LaunchedEffect(paymentTermsList) {
        if (paymentTermsDisplayName.isBlank() && paymentTermsList.isNotEmpty()) {
            val defaultTerm = paymentTermsList.find { it.isDefault } ?: paymentTermsList.first()
            selectedPaymentTermId = defaultTerm.id
            paymentTermsDisplayName = defaultTerm.name

            if (prefillOrder == null) {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, defaultTerm.dueDays) }
                dueDate = displayDateFormatter.format(cal.time)
            }
        }
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                dueDate = displayDateFormatter.format(chosenDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val itemDropdownExpandedMap = remember { mutableStateMapOf<String, Boolean>() }
    val incomeDropdownExpandedMap = remember { mutableStateMapOf<String, Boolean>() }
    val taxDropdownExpandedMap = remember { mutableStateMapOf<String, Boolean>() }

    // Notes & Terms
    var customerNotes by remember(prefillOrder) {
        mutableStateOf(prefillOrder?.orderNotes.orEmpty())
    }
    var termsAndConditions by remember {
        mutableStateOf("")
    }

    // Attachments
    var selectedAttachmentUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedAttachmentUris = selectedAttachmentUris + uris
    }

    // Financial Totals Calculation
    val subTotal = salesLines.sumOf { it.lineAmount }
    val totalTax = salesLines.sumOf { it.taxAmount }
    val totalDue = subTotal + totalTax

    // Helper: Build ISO Date string
    fun getIsoDateString(): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        return isoFormat.format(Date())
    }

    // Form Validator & Payload Builder
    fun preparePayload(): Triple<String, String, List<okhttp3.MultipartBody.Part>>? {
        if (selectedCustomerId.isNullOrBlank()) {
            DynamicIslandManager.showError("Please select a customer")
            return null
        }

        if (selectedPaymentTermId.isNullOrBlank()) {
            DynamicIslandManager.showError("Please select payment terms")
            return null
        }

        val invalidLine = salesLines.find {
            it.itemId.isBlank() || it.incomeAccountId.isBlank() || it.taxGroupId.isBlank() || it.lineAmount <= 0.0
        }
        if (invalidLine != null) {
            DynamicIslandManager.showError("Please complete all item line details with valid quantity and rate")
            return null
        }

        val lineRequests = salesLines.map { line ->
            CreateInvoiceLineItemRequest(
                lineType = "Product",
                itemId = line.itemId,
                itemName = line.itemName,
                itemDescription = line.itemName,
                unit = "Pieces",
                quantity = line.quantity.toDoubleOrNull() ?: 1.0,
                rate = line.rate.toDoubleOrNull() ?: 0.0,
                discountPercent = 0.0,
                taxGroupId = line.taxGroupId,
                incomeAccountId = line.incomeAccountId
            )
        }

        val linesJson = Gson().toJson(lineRequests)

        val binaryParts = selectedAttachmentUris.mapNotNull { uri ->
            uriToMultipartPart(context, uri, "attachments")
        }

        return Triple(selectedCustomerId!!, linesJson, binaryParts)
    }

    // Handles Save Draft Action
    fun handleSaveDraft() {
        if (createInvoiceState is CreateInvoiceState.Loading) return
        val prepared = preparePayload() ?: return
        val (customerId, linesJson, binaryParts) = prepared

        financeViewModel.createSalesInvoice(
            customerId = customerId,
            invoiceDate = getIsoDateString(),
            paymentTermId = selectedPaymentTermId!!,
            currency = "INR",
            exchangeRate = "1",
            priceIncludesTax = false,
            linesJson = linesJson,
            notes = customerNotes,
            termsAndConditions = termsAndConditions,
            attachmentParts = binaryParts
        )
    }

    // Handles Save and Send Action
    fun handleSaveAndSend() {
        if (createInvoiceState is CreateInvoiceState.Loading) return
        val prepared = preparePayload() ?: return
        val (customerId, linesJson, binaryParts) = prepared

        financeViewModel.createAndSendSalesInvoice(
            customerId = customerId,
            invoiceDate = getIsoDateString(),
            paymentTermId = selectedPaymentTermId!!,
            currency = "INR",
            exchangeRate = "1",
            priceIncludesTax = false,
            linesJson = linesJson,
            notes = customerNotes,
            termsAndConditions = termsAndConditions,
            attachmentParts = binaryParts
        )
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar(title = "Create Sales Invoice", onClose = onClose)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
        ) {
            // ──────────────── CARD 1: GENERAL INVOICE FIELDS ────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    val customerOptions = remember(customersList) {
                        customersList.map { c ->
                            val name = c.name.trim()
                            val mob = c.mobile?.trim().orEmpty()
                            if (mob.isNotBlank()) "$name ($mob)" else name
                        }
                    }

                    FormDropdown(
                        label = "Customer Name",
                        isRequired = true,
                        value = customerDisplayName.ifBlank { "Search or select Customer" },
                        expanded = customerDropdownExpanded,
                        onExpandChange = { customerDropdownExpanded = it },
                        options = customerOptions.ifEmpty { listOf("Loading customers...") },
                        onOptionSelected = { selectedName ->
                            customerDisplayName = selectedName
                            val foundCustomer = customersList.find { c ->
                                val name = c.name.trim()
                                val mob = c.mobile?.trim().orEmpty()
                                val opt = if (mob.isNotBlank()) "$name ($mob)" else name
                                opt == selectedName || c.name == selectedName
                            }
                            selectedCustomerId = foundCustomer?.id
                            customerDropdownExpanded = false
                        }
                    )

                    Column {
                        FormLabel(text = "Invoice #", isRequired = false)
                        FormTextField(
                            value = invoiceNumber,
                            onValueChange = { invoiceNumber = it },
                            placeholder = "INV-AUTO",
                            enabled = false
                        )
                    }

                    Column {
                        FormLabel(text = "Reference / Order Number", isRequired = false)
                        FormTextField(
                            value = referenceOrderNumber,
                            onValueChange = { referenceOrderNumber = it },
                            placeholder = "e.g. PO-8821"
                        )
                    }

                    val paymentTermOptions = remember(paymentTermsList) {
                        paymentTermsList.map { it.name }
                    }

                    FormDropdown(
                        label = "Payment Terms",
                        isRequired = true,
                        value = paymentTermsDisplayName.ifBlank { "Select Terms" },
                        expanded = paymentTermsExpanded,
                        onExpandChange = { paymentTermsExpanded = it },
                        options = paymentTermOptions.ifEmpty { listOf("Loading terms...") },
                        onOptionSelected = { chosenTermName ->
                            paymentTermsDisplayName = chosenTermName
                            val matchedTerm = paymentTermsList.find { it.name == chosenTermName }
                            if (matchedTerm != null) {
                                selectedPaymentTermId = matchedTerm.id
                                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, matchedTerm.dueDays) }
                                dueDate = displayDateFormatter.format(cal.time)
                            }
                            paymentTermsExpanded = false
                        }
                    )

                    Column {
                        FormLabel(text = "Due Date", isRequired = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.fieldHeight)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(light_grey)
                                .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = tokens.screenPadding),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dueDate,
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Select Date",
                                    tint = iconMuted,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }
                    }
                }
            }

            // ──────────────── CARD 2: ITEMIZED SALES LINES ────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ITEMIZED SALES LINES",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "+ Add Line Item",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary,
                            modifier = Modifier.clickable {
                                salesLines.add(SalesLineRowState())
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                Text(text = "ITEM & SKU", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.width(180.dp))
                                Text(text = "INCOME ACCOUNT", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.width(200.dp))
                                Text(text = "QTY", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.width(65.dp))
                                Text(text = "RATE (INR)", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.width(85.dp))
                                Text(text = "TAX GROUP", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.width(130.dp))
                                Text(text = "AMOUNT", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.width(80.dp))
                                Spacer(Modifier.width(tokens.iconSize * 1.5f))
                            }

                            salesLines.forEachIndexed { index, line ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                ) {
                                    val itemOptions = remember(fabricItemList) { fabricItemList.map { it.name } }
                                    Box(modifier = Modifier.width(180.dp)) {
                                        FormDropdown(
                                            value = line.itemName,
                                            expanded = itemDropdownExpandedMap[line.id] == true,
                                            onExpandChange = { itemDropdownExpandedMap[line.id] = it },
                                            options = itemOptions.ifEmpty { listOf(line.itemName.ifBlank { "Select Item" }) },
                                            onOptionSelected = { chosenName ->
                                                line.itemName = chosenName
                                                val matchedItem = fabricItemList.find { it.name == chosenName }
                                                if (matchedItem != null) {
                                                    line.itemId = matchedItem._id.ifBlank { matchedItem.id.orEmpty() }
                                                    if (matchedItem.sellingPrice > 0.0) {
                                                        line.rate = matchedItem.sellingPrice.toString()
                                                    }
                                                }
                                                itemDropdownExpandedMap[line.id] = false
                                            }
                                        )
                                    }

                                    val accountOptions = remember(accountDropdownList) {
                                        accountDropdownList.map { it.displayName.ifBlank { it.accountName } }
                                    }
                                    Box(modifier = Modifier.width(200.dp)) {
                                        FormDropdown(
                                            value = line.incomeAccountName,
                                            expanded = incomeDropdownExpandedMap[line.id] == true,
                                            onExpandChange = { incomeDropdownExpandedMap[line.id] = it },
                                            options = accountOptions.ifEmpty { listOf(line.incomeAccountName.ifBlank { "Select Account" }) },
                                            onOptionSelected = { chosenAccountName ->
                                                line.incomeAccountName = chosenAccountName
                                                val matchedAcc = accountDropdownList.find {
                                                    it.displayName == chosenAccountName || it.accountName == chosenAccountName
                                                }
                                                line.incomeAccountId = matchedAcc?.id.orEmpty()
                                                incomeDropdownExpandedMap[line.id] = false
                                            }
                                        )
                                    }

                                    Box(modifier = Modifier.width(65.dp)) {
                                        FormTextField(value = line.quantity, onValueChange = { line.quantity = it }, keyboardType = KeyboardType.Number)
                                    }

                                    Box(modifier = Modifier.width(85.dp)) {
                                        FormTextField(value = line.rate, onValueChange = { line.rate = it }, keyboardType = KeyboardType.Number)
                                    }

                                    val taxGroupOptions = remember(taxGroupsList) { taxGroupsList.map { it.name } }
                                    Box(modifier = Modifier.width(130.dp)) {
                                        FormDropdown(
                                            value = line.taxGroupName,
                                            expanded = taxDropdownExpandedMap[line.id] == true,
                                            onExpandChange = { taxDropdownExpandedMap[line.id] = it },
                                            options = taxGroupOptions.ifEmpty { listOf(line.taxGroupName.ifBlank { "Tax (0%)" }) },
                                            onOptionSelected = { chosenTaxName ->
                                                line.taxGroupName = chosenTaxName
                                                val matchedTax = taxGroupsList.find { it.name == chosenTaxName }
                                                if (matchedTax != null) {
                                                    line.taxGroupId = matchedTax.id
                                                    line.taxRatePercentage = matchedTax.totalRate
                                                }
                                                taxDropdownExpandedMap[line.id] = false
                                            }
                                        )
                                    }

                                    Text(
                                        text = "${currencySymbol()}${formatIndianNumber(line.lineAmount.toLong())}",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        modifier = Modifier.width(80.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (salesLines.size > 1) {
                                                salesLines.removeAt(index)
                                            }
                                        },
                                        enabled = salesLines.size > 1,
                                        modifier = Modifier.size(tokens.buttonHeight * 0.7f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Remove Row",
                                            tint = if (salesLines.size > 1) mutedText else BorderGray,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderGray)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .clickable { salesLines.add(SalesLineRowState()) }
                            .padding(vertical = tokens.extraPadding * 0.6f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Add New Row",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            }

            // ──────────────── CARD 3: NOTES & TERMS ────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column {
                        FormLabel(text = "Customer Notes")
                        FormTextArea(value = customerNotes, onValueChange = { customerNotes = it }, minLines = 3)
                    }

                    Column {
                        FormLabel(text = "Terms & Conditions")
                        FormTextArea(value = termsAndConditions, onValueChange = { termsAndConditions = it }, minLines = 3)
                    }
                }
            }

            // ──────────────── CARD 4: INVOICE SUMMARY ────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.cardPadding)
                ) {
                    Text(
                        text = "INVOICE SUMMARY",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sub Total:", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text(text = "${currencySymbol()}${formatIndianNumber(subTotal.toLong())}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tax Total:", fontSize = tokens.bodySmall, color = darkGreenBg)
                        Text(text = "+${currencySymbol()}${formatIndianNumber(totalTax.toLong())}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = darkGreenBg)
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    HorizontalDivider(color = TextPrimary, thickness = 1.dp)
                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Total Due:", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(text = "${currencySymbol()}${formatIndianNumber(totalDue.toLong())}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 1.5f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(primary_light)
                            .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(tokens.screenPadding * 0.8f)
                    ) {
                        Column {
                            Text(
                                text = "Ledger Integration Notice:",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                            Text(
                                text = "Clicking Save and Send will finalize the invoice, lock editing, and project balanced journal debit/credit lines into Accounts Receivable.",
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                lineHeight = tokens.bodySmall
                            )
                        }
                    }
                }
            }

            // ──────────────── CARD 5: ATTACHMENTS ────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.cardPadding)
                ) {
                    Text(
                        text = "ATTACHMENTS",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(tokens.extraPadding))

                    ImageUploadSection(
                        selectedImages = selectedAttachmentUris,
                        onBrowseClick = { attachmentPickerLauncher.launch("*/*") },
                        onRemoveImage = { uri ->
                            selectedAttachmentUris = selectedAttachmentUris.filter { it != uri }
                        },
                        maxFiles = 1,
                        subtitle = "Add your documents here, and you can upload up to 5 files max",
                        supportedFormatsText = "Only support .jpg, .png, .pdf and zip files up to 10MB",
                        browseText = "Browse files"
                    )
                }
            }

            // ──────────────── ACTION BUTTONS ────────────────
            FormActionButtons(
                modifier = Modifier.fillMaxWidth(),
                cancelText = "Cancel",
                draftText = "Save Draft",
                primaryText = "Save and Send",
                onCancel = onClose,
                onSaveDraft = { handleSaveDraft() },
                onPrimaryClick = { handleSaveAndSend() },
                isLoading = createInvoiceState is CreateInvoiceState.Loading,
                primaryColor = Primary,
                borderColor = BorderGray,
                textColor = TextPrimary
            )

            Spacer(Modifier.height(110.dp))
        }
    }
}