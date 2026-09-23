@file:Suppress("SpellCheckingInspection", "unused")

package com.cuso.tailor.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.CreateBillRequest
import com.cuso.tailor.model.inventory.ExtraBillLineRequest
import com.cuso.tailor.model.inventory.PurchaseReceiveItem
import com.cuso.tailor.model.inventory.ReceiveItemDetail
import com.cuso.tailor.model.inventory.SelectedReceiveItemRequest
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.iconMuted
import com.cuso.tailor.ui.theme.modelGray
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.FormActionButtons
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ServiceItemState(
    val id: String = UUID.randomUUID().toString(),
    var description: String = "",
    var quantity: String = "1",
    var rate: String = "0"
)

@Composable
fun NewBillScreen(
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    financeViewModel: FinanceViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCancel: () -> Unit = onClose,
    @Suppress("UNUSED_PARAMETER") onSaveDraft: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // Observe inventory state
    val multipleReceives: List<PurchaseReceiveItem> by inventoryViewModel.multipleReceivesData.collectAsStateWithLifecycle()
    val billData by inventoryViewModel.generatedBill.collectAsStateWithLifecycle()

    // Observe dropdowns & submission state
    val accountDropdownList by financeViewModel.accountDropdownList.collectAsStateWithLifecycle()
    val paymentTerms by inventoryViewModel.paymentTerms.collectAsStateWithLifecycle()
    val taxGroups by inventoryViewModel.taxGroups.collectAsStateWithLifecycle()
    val isSubmittingBill by inventoryViewModel.isSubmittingBill.collectAsStateWithLifecycle()
    val successMessage by inventoryViewModel.createBillSuccessMessage.collectAsStateWithLifecycle()
    val errorMessage by inventoryViewModel.createBillErrorMessage.collectAsStateWithLifecycle()

    // Fetch dynamic options on launch
    LaunchedEffect(Unit) {
        financeViewModel.fetchChartOfAccountsDropdown(context = "expense_line")
        inventoryViewModel.fetchPaymentTerms()
        inventoryViewModel.fetchTaxGroups()
    }

    // Dynamic Form States
    var supplierName by remember { mutableStateOf("-") }
    var orderNumber by remember { mutableStateOf("-") }
    var supplierBillReference by remember { mutableStateOf("") }
    var billDate by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()))
    }

    // Dynamic Dropdown Selections
    var paymentTermExpanded by remember { mutableStateOf(false) }
    var selectedPaymentTermId by remember { mutableStateOf("") }

    var dueDate by remember { mutableStateOf("-") }

    var expenseAccountExpanded by remember { mutableStateOf(false) }
    var selectedExpenseAccountId by remember { mutableStateOf("") }

    var taxGroupExpanded by remember { mutableStateOf(false) }
    var selectedTaxGroupId by remember { mutableStateOf("") }

    var rateIncludesTax by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("") }

    val serviceItems = remember { mutableStateListOf<ServiceItemState>() }

    // Dynamic pre-selections
    LaunchedEffect(paymentTerms) {
        if (selectedPaymentTermId.isBlank() && paymentTerms.isNotEmpty()) {
            selectedPaymentTermId = paymentTerms.firstOrNull { it.isDefault }?.id ?: paymentTerms.first().id
        }
    }

    LaunchedEffect(taxGroups) {
        if (selectedTaxGroupId.isBlank() && taxGroups.isNotEmpty()) {
            selectedTaxGroupId = taxGroups.firstOrNull { it.isDefault }?.id ?: taxGroups.first().id
        }
    }

    LaunchedEffect(accountDropdownList) {
        if (selectedExpenseAccountId.isBlank() && accountDropdownList.isNotEmpty()) {
            selectedExpenseAccountId = accountDropdownList.first().id
        }
    }

    // Populate data from received PO items
    LaunchedEffect(multipleReceives, billData) {
        if (multipleReceives.isNotEmpty()) {
            val firstReceive = multipleReceives.first()
            supplierName = firstReceive.poId?.supplierId?.name ?: "-"
            orderNumber = firstReceive.poId?.poNumber ?: "-"
            billDate = formatToDisplayDate(firstReceive.receiveDate)
        } else if (billData != null) {
            val bill = billData!!
            supplierName = bill.supplierId?.name ?: "-"
            orderNumber = bill.poId?.poNumber ?: "-"
            billDate = formatToDisplayDate(bill.billDate)
            dueDate = formatToDisplayDate(bill.dueDate)
        }
    }

    // Navigation on save success
    LaunchedEffect(successMessage) {
        if (!successMessage.isNullOrBlank()) {
            delay(1000)
            inventoryViewModel.clearBillAlerts()
            onSave()
            onClose()
        }
    }

    val selectedPaymentTermName = paymentTerms.find { it.id == selectedPaymentTermId }?.name ?: "Select Payment Term"
    val selectedExpenseAccountName = accountDropdownList.find { it.id == selectedExpenseAccountId }?.accountName ?: "Select Expense Account"
    val selectedTaxGroupObj = taxGroups.find { it.id == selectedTaxGroupId }
    val selectedTaxGroupName = selectedTaxGroupObj?.name ?: "Select Tax Group"

    val items: List<ReceiveItemDetail> = remember(multipleReceives) {
        multipleReceives.flatMap { it.items }
    }

    // Financial calculations
    val productSubtotal = remember(multipleReceives, billData) {
        if (multipleReceives.isNotEmpty()) multipleReceives.sumOf { it.subtotal }
        else billData?.subtotal ?: 0.0
    }

    val serviceSubtotal = serviceItems.sumOf { item ->
        val qty = item.quantity.toDoubleOrNull() ?: 0.0
        val rate = item.rate.toDoubleOrNull() ?: 0.0
        qty * rate
    }

    val subtotal = productSubtotal + serviceSubtotal
    val taxPercentage = selectedTaxGroupObj?.totalRate ?: 0.0
    val taxTotal = subtotal * (taxPercentage / 100.0)

    val grandTotal = remember(subtotal, taxTotal, discountInput) {
        val discount = discountInput.toDoubleOrNull() ?: 0.0
        (subtotal + taxTotal - discount).coerceAtLeast(0.0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Primary_background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "New Bill",
                    onClose = onClose
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.8f)
            ) {
                // ── Section 1: General Details ──
                Text(
                    text = "GENERAL DETAILS",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = close_color
                )

                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Supplier Name")
                        FormTextField(
                            value = supplierName,
                            onValueChange = { supplierName = it },
                            enabled = false
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Order Number (PO)")
                        FormTextField(
                            value = orderNumber,
                            onValueChange = { orderNumber = it },
                            enabled = false
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Supplier Bill Reference")
                        FormTextField(
                            value = supplierBillReference,
                            onValueChange = { supplierBillReference = it },
                            placeholder = "Supplier invoice/bill number"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Bill Date *")
                        DatePickerField(
                            value = billDate,
                            onDateSelected = { billDate = it }
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormDropdown(
                            label = "Payment Term *",
                            value = selectedPaymentTermName,
                            expanded = paymentTermExpanded,
                            onExpandChange = { paymentTermExpanded = it },
                            options = paymentTerms.map { it.name }.ifEmpty { listOf("No Terms Found") },
                            onOptionSelected = { chosenName ->
                                selectedPaymentTermId = paymentTerms.find { it.name == chosenName }?.id.orEmpty()
                            }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Due Date (estimated)")
                        DatePickerField(
                            value = dueDate,
                            onDateSelected = { dueDate = it },
                            enabled = false
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormDropdown(
                            label = "Expense / Asset Account *",
                            value = selectedExpenseAccountName,
                            expanded = expenseAccountExpanded,
                            onExpandChange = { expenseAccountExpanded = it },
                            options = accountDropdownList.map { it.accountName }.ifEmpty { listOf("No Accounts Found") },
                            onOptionSelected = { chosenAccountName ->
                                selectedExpenseAccountId = accountDropdownList.find { it.accountName == chosenAccountName }?.id.orEmpty()
                            }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormDropdown(
                            label = "Tax Group *",
                            value = selectedTaxGroupName,
                            expanded = taxGroupExpanded,
                            onExpandChange = { taxGroupExpanded = it },
                            options = taxGroups.map { it.name }.ifEmpty { listOf("No Tax Groups Found") },
                            onOptionSelected = { chosenName ->
                                selectedTaxGroupId = taxGroups.find { it.name == chosenName }?.id.orEmpty()
                            }
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rateIncludesTax = !rateIncludesTax }
                ) {
                    AppCheckbox(
                        checked = rateIncludesTax,
                        onCheckedChange = { rateIncludesTax = it }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Rate already includes tax",
                        fontSize = tokens.bodySmall,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.height(tokens.screenPadding * 1.2f))

                // ── Section 2: Items List (Products) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ITEMS LIST (${items.size.coerceAtLeast(1)})",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Account: Product",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                if (items.isNotEmpty()) {
                    items.forEach { item ->
                        BillItemRow(
                            title = item.itemId?.name ?: "Product",
                            quantity = "${item.qtyReceived}",
                            rate = String.format(Locale.US, "₹%,.2f", item.rate),
                            amount = String.format(Locale.US, "₹%,.2f", item.total),
                            tokens = tokens
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 1.4f))
                    }
                } else if (!billData?.items.isNullOrEmpty()) {
                    billData!!.items.forEach { item ->
                        BillItemRow(
                            title = item.itemId?.name ?: "Product",
                            quantity = "${item.qty}",
                            rate = String.format(Locale.US, "₹%,.2f", item.rate),
                            amount = String.format(Locale.US, "₹%,.2f", item.total),
                            tokens = tokens
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 1.4f))
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding * 1.2f))
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Spacer(Modifier.height(tokens.screenPadding * 1.2f))

                // ── Section 3: Extra Service Items ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Service Items",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "+ Add Service",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.clickable {
                            serviceItems.add(ServiceItemState())
                        }
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding))

                if (serviceItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(modelGray, shape = RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Service", fontSize = tokens.caption, color = mutedText, modifier = Modifier.weight(2f))
                        Text("Quantity", fontSize = tokens.caption, color = mutedText, modifier = Modifier.weight(1f))
                        Text("Rate", fontSize = tokens.caption, color = mutedText, modifier = Modifier.weight(1f))
                        Text("Amount", fontSize = tokens.caption, color = mutedText, modifier = Modifier.weight(1.2f))
                        Spacer(Modifier.width(32.dp))
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    serviceItems.forEachIndexed { index, serviceItem ->
                        val qty = serviceItem.quantity.toDoubleOrNull() ?: 0.0
                        val rt = serviceItem.rate.toDoubleOrNull() ?: 0.0
                        val rowAmount = qty * rt

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FormTextField(
                                value = serviceItem.description,
                                onValueChange = { serviceItems[index] = serviceItem.copy(description = it) },
                                placeholder = "Service description",
                                modifier = Modifier.weight(2f)
                            )

                            FormTextField(
                                value = serviceItem.quantity,
                                onValueChange = { serviceItems[index] = serviceItem.copy(quantity = it) },
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )

                            FormTextField(
                                value = serviceItem.rate,
                                onValueChange = { serviceItems[index] = serviceItem.copy(rate = it) },
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = String.format(Locale.US, "₹%.2f", rowAmount),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1.2f)
                            )

                            IconButton(
                                onClick = { serviceItems.removeAt(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete Service",
                                    tint = close_color
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding * 1.2f))

                // ── Section 4: Notes ──
                FormLabel("Notes")
                FormTextArea(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Add notes or remarks"
                )

                Spacer(Modifier.height(tokens.screenPadding * 1.25f))
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Spacer(Modifier.height(tokens.screenPadding))

                // ── Section 5: Order Summary ──
                Text(
                    text = "Order Summary",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                SummaryRow(
                    label = "Subtotal",
                    value = String.format(Locale.US, "₹%,.2f", subtotal),
                    tokens = tokens
                )
                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                SummaryRow(
                    label = "Tax Total (${taxPercentage.toInt()}%)",
                    value = String.format(Locale.US, "₹%,.2f", taxTotal),
                    tokens = tokens
                )
                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                FormLabel("Discount")
                FormTextField(
                    value = discountInput,
                    onValueChange = { discountInput = it },
                    placeholder = "Enter discount amount",
                    keyboardType = KeyboardType.Decimal
                )

                Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grand Total",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = String.format(Locale.US, "₹%,.2f", grandTotal),
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.height(tokens.screenPadding * 1.75f))

                // ── Section 6: Action Buttons with Dynamic Payload ──
                FormActionButtons(
                    cancelText = "Cancel",
                    primaryText = "Save Bill",
                    isLoading = isSubmittingBill,
                    onCancel = onCancel,
                    onPrimaryClick = {
                        val firstReceive = multipleReceives.firstOrNull()

                        // 1. Dynamic receive IDs list from multipleReceives
                        val resolvedReceiveIds: List<String> = multipleReceives
                            .mapNotNull { it.id.ifBlank { null } }
                            .distinct()

                        // 2. Dynamic selected receive items
                        val resolvedSelectedItems: List<SelectedReceiveItemRequest> = multipleReceives.flatMap { receive ->
                            receive.items.mapNotNull { itm ->
                                if (receive.id.isNotBlank() && itm.id.isNotBlank()) {
                                    SelectedReceiveItemRequest(
                                        receiveId = receive.id,
                                        receiveItemId = itm.id
                                    )
                                } else null
                            }
                        }

                        // 3. Dynamic extra lines mapped from user-entered service items
                        val dynamicExtraLines: List<ExtraBillLineRequest> = serviceItems.map { service ->
                            ExtraBillLineRequest(
                                lineType = "Service",
                                itemDescription = service.description.ifBlank { "Service" },
                                quantity = service.quantity.toDoubleOrNull() ?: 1.0,
                                rate = service.rate.toDoubleOrNull() ?: 0.0,
                                discountPercent = 0.0,
                                expenseAccountId = selectedExpenseAccountId,
                                taxGroupId = selectedTaxGroupId
                            )
                        }

                        // 4. Dynamic supplier and warehouse IDs
                        val resolvedSupplierId = firstReceive?.poId?.supplierId?.id
                            ?: billData?.supplierId?.id
                            ?: ""

                        val resolvedWarehouseId = firstReceive?.warehouseId ?: ""

                        // 5. Construct payload strictly with requested dynamic fields
                        val requestPayload = CreateBillRequest(
                            billDate = formatToYyyyMmDd(billDate),
                            defaultExpenseAccountId = selectedExpenseAccountId,
                            defaultTaxGroupId = selectedTaxGroupId,
                            extraLines = dynamicExtraLines,
                            notes = notes.ifBlank { null },
                            paymentTermId = selectedPaymentTermId,
                            priceIncludesTax = rateIncludesTax,
                            receiveIds = resolvedReceiveIds,
                            selectedReceiveItems = resolvedSelectedItems,
                            supplierBillReference = supplierBillReference.trim(),
                            supplierId = resolvedSupplierId,
                            warehouseId = resolvedWarehouseId
                        )

                        inventoryViewModel.submitCreateBill(request = requestPayload)
                    }
                )

                Spacer(Modifier.height(tokens.screenPadding * 1.5f))
            }
        }

        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { inventoryViewModel.clearBillAlerts() }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { inventoryViewModel.clearBillAlerts() }
        )
    }
}

/**
 * Converts formatted dates to "YYYY-MM-DD" format required by the API.
 */
private fun formatToYyyyMmDd(dateStr: String): String {
    if (dateStr.isBlank()) return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    return try {
        val clean = dateStr.substringBefore("T").trim()
        val parts = clean.split("-")
        if (parts.size == 3) {
            if (parts[0].length == 4) {
                clean
            } else if (parts[2].length == 4) {
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else {
                clean
            }
        } else {
            clean
        }
    } catch (_: Exception) {
        dateStr
    }
}

/**
 * Converts ISO/UTC date into displayable dd-MM-yyyy format.
 */
private fun formatToDisplayDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
    return try {
        val clean = isoDate.substringBefore("T")
        val parts = clean.split("-")
        if (parts.size == 3 && parts[0].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            clean
        }
    } catch (_: Exception) {
        SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
    }
}

@Composable
fun BillItemRow(
    title: String,
    quantity: String,
    rate: String,
    amount: String,
    tokens: AppDesignTokens
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 0.45f)
                        .background(Primary, CircleShape)
                )
                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.3f),
                color = modelGray
            ) {
                Text(
                    text = "Product",
                    fontSize = tokens.caption,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "QUANTITY", fontSize = tokens.label, color = iconMuted)
                Text(
                    text = quantity,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "RATE", fontSize = tokens.label, color = iconMuted)
                Text(
                    text = rate,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "AMOUNT", fontSize = tokens.label, color = iconMuted)
                Text(
                    text = amount,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = TextSecondary)
        Text(text = value, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}