@file:Suppress(
    "UNUSED_PARAMETER",
    "UNUSED",
    "RedundantSuppression",
    "unused_variable",
    "AssignedValueIsNeverRead",
    "VariableNeverRead"
)

package com.cuso.tailor.view.home.finance.expenses

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.model.finance.ChartOfAccountItem
import com.cuso.tailor.model.finance.ExpenseItem
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.formatCurrency
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.ErrorFieldWrapper
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ImageUploadSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.MiniSwitch
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.math.roundToLong

import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.CreateAccountState
import com.cuso.tailor.viewmodel.CreateExpenseState
import com.cuso.tailor.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ExpensePrimary = Color(0xFF3B3BF9)
private val ExpenseBg = Color(0xFFF5F5F5)
private val ExpenseBorder = grey_border

@Composable
fun ExpensesScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val financeViewModel: FinanceViewModel = hiltViewModel()

    var showAddExpense by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedExpenseForView by remember { mutableStateOf<ExpenseItem?>(null) }

    val expensesPagingState by financeViewModel.expensesPagingState.collectAsStateWithLifecycle()
    val expenseError by financeViewModel.expenseError.collectAsStateWithLifecycle()
    val chartOfAccounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        financeViewModel.fetchExpenses()
        financeViewModel.fetchChartOfAccounts(paginate = false, includeDeleted = false)
    }

    LaunchedEffect(searchQuery) {
        delay(400)
        financeViewModel.fetchExpenses(search = searchQuery.trim().ifBlank { null })
    }

    if (showAddExpense || editingExpense != null) {
        AddExpenseScreen(
            financeViewModel = financeViewModel,
            initialData = editingExpense,
            mode = if (editingExpense != null) "edit" else "create",
            onClose = {
                showAddExpense = false
                editingExpense = null
            },
            onSaved = {
                showAddExpense = false
                editingExpense = null
                financeViewModel.fetchExpenses()
            }
        )
        return
    }

    selectedExpenseForView?.let { expense ->
        ExpenseDetailScreen(
            expense = expense,
            accountList = chartOfAccounts,
            onClose = { selectedExpenseForView = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleBar(title = "All Expense", onClose = onClose)
        }

        Column(Modifier.fillMaxWidth()) {

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Expenses...",
                accentColor = BluePrimary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = { }
            )
        }
        HorizontalDivider(color = title_border)

        when {
            expenseError != null && expensesPagingState.items.isEmpty() && !expensesPagingState.isInitialLoading -> {
                AppErrorState(
                    title = "Failed to load Expenses",
                    message = "Something went wrong. Please check your connection and try again.",
                    onRetry = { financeViewModel.fetchExpenses(search = searchQuery.trim().ifBlank { null }) }
                )
            }

            !expensesPagingState.isInitialLoading && expensesPagingState.items.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(Color(0xFFE7E5FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF9B96F5),
                            modifier = Modifier.size(tokens.iconSize * 1.6f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No Expenses Found",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Start by creating your first expense record",
                        fontSize = tokens.bodySmall,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { showAddExpense = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpensePrimary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Add Expenses",
                            color = whiteBg,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            else -> {
                FabScaffold(
                    modifier = Modifier.fillMaxSize(),
                    fab = FabConfig(
                        label = "Add Expenses",
                        icon = Icons.Default.Add,
                        onClick = { showAddExpense = true }
                    ),
                    fabVisible = !expensesPagingState.isInitialLoading
                ) {
                    PaginatedDataList(
                        state = expensesPagingState,
                        onLoadMore = { financeViewModel.loadMoreExpenses() },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) { expense ->
                        val status = expense.status.ifBlank { "Draft" }
                        val (statusFg, statusBg) = expenseStatusColors(status)

                        val firstLineAccount = expense.lines.firstOrNull()?.expenseAccountId
                        val baseAccountName = firstLineAccount?.accountName?.ifBlank { null }
                            ?: expense.accountId?.accountName?.ifBlank { null }
                            ?: chartOfAccounts.find { it._id == (firstLineAccount?._id ?: expense.accountId?._id) }?.accountName
                            ?: expense.expenseNumber.ifBlank { "Expense" }

                        val accountTitle = if (expense.lines.size > 1) {
                            "$baseAccountName (+${expense.lines.size - 1} more)"
                        } else {
                            baseAccountName
                        }

                        val paymentAccountRef = expense.paidFromAccountId ?: expense.paymentAccountId
                        val paymentTitle = paymentAccountRef?.accountName?.ifBlank { null }
                            ?: chartOfAccounts.find { it._id == paymentAccountRef?._id }?.accountName
                            ?: expense.paymentMode?.ifBlank { null }
                            ?: "Account"

                        val displayAmount = if (expense.grandTotal > 0.0) expense.grandTotal else expense.amount

                        DataCard(
                            item = expense,
                            dateText = formatExpenseDate(expense.expenseDate),
                            topBadgeText = status,
                            topBadgeTextColor = statusFg,
                            topBadgeBgColor = statusBg,
                            title = accountTitle,
                            subtitle = "Paid via $paymentTitle",
                            footerFields = listOf(
                                DataCardField(
                                    asRow = true,
                                    label = "Amount",
                                    text = "${formatCurrency()}${formatAmount(displayAmount)}"
                                )
                            ),
                            actions = buildList {
                                add(
                                    MenuAction(
                                        label = "View",
                                        icon = Icons.Default.Visibility,
                                        onClick = { selectedExpenseForView = expense }
                                    )
                                )
                                if (status.equals("Draft", ignoreCase = true)) {
                                    add(
                                        MenuAction(
                                            label = "Edit",
                                            icon = Icons.Default.Edit,
                                            onClick = { editingExpense = expense }
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailScreen(
    expense: ExpenseItem,
    accountList: List<ChartOfAccountItem> = emptyList(),
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val status = expense.status.ifBlank { "Draft" }
    val (statusFg, statusBg) = expenseStatusColors(status)

    val primaryLineAccount = expense.lines.firstOrNull()?.expenseAccountId ?: expense.accountId
    val expenseAccountName = primaryLineAccount?.accountName?.ifBlank { null }
        ?: accountList.find { it._id == primaryLineAccount?._id }?.let { "${it.accountCode} - ${it.accountName}" }
        ?: primaryLineAccount?._id
        ?: "—"

    val paidFromRef = expense.paidFromAccountId ?: expense.paymentAccountId
    val paymentAccountName = paidFromRef?.accountName?.ifBlank { null }
        ?: accountList.find { it._id == paidFromRef?._id }?.let { "${it.accountCode} - ${it.accountName}" }
        ?: paidFromRef?._id
        ?: "—"

    val displayAmount = if (expense.grandTotal > 0.0) expense.grandTotal else expense.amount


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
        ) {
            TitleBar(title = "Expense Details", onClose = onClose)
        }
        HorizontalDivider(color = title_border)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            ExpenseDetailField(label = "Expense Number", value = expense.expenseNumber.ifBlank { "—" })
            ExpenseDetailField(label = "Expense Date", value = formatExpenseDate(expense.expenseDate))
            ExpenseDetailField(label = "Paid From Account", value = paymentAccountName)
            if (!expense.paymentMode.isNullOrBlank()) {
                ExpenseDetailField(label = "Payment Mode", value = expense.paymentMode)
            }
            if (expense.lines.isNotEmpty()) {
                FormLabel("Line Items")
                Spacer(Modifier.height(4.dp))
                expense.lines.forEach { line ->
                    val lineAcc = line.expenseAccountId?.accountName?.ifBlank { null } ?: "Expense"
                    ExpenseDetailField(
                        label = "$lineAcc (${line.quantity} x ${formatCurrency()}${formatAmount(line.rate)})",
                        value = "${line.itemDescription.ifBlank { "—" }} • ${formatCurrency()}${formatAmount(line.lineTotal)}"
                    )
                }
            } else {
                ExpenseDetailField(label = "Expense Account", value = expenseAccountName)
            }
            ExpenseDetailField(label = "Total Amount", value = "${formatCurrency()}${formatAmount(displayAmount)}")

            if (!expense.referenceNumber.isNullOrBlank()) {
                ExpenseDetailField(label = "Reference Number", value = expense.referenceNumber)
            }

            if (!expense.notes.isNullOrBlank()) {
                ExpenseDetailField(label = "Notes", value = expense.notes)
            }

            FormLabel("Status")
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .background(statusBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = status,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = statusFg
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExpenseDetailField(label: String, value: String?) {
    val tokens = LocalAppTokens.current
    FormLabel(label)
    Spacer(Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ExpenseBg, RoundedCornerShape(tokens.cardCornerRadius / 2))
            .border(1.dp, ExpenseBorder, RoundedCornerShape(tokens.cardCornerRadius / 2))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = value?.ifBlank { "—" } ?: "—",
            fontSize = tokens.bodyMedium,
            color = Color(0xFF374151)
        )
    }
    Spacer(Modifier.height(14.dp))
}

private fun expenseStatusColors(status: String?): Pair<Color, Color> {
    return when (status?.lowercase()) {
        "paid", "posted" -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
        "pending", "draft" -> Color(0xFFCA8A04) to Color(0xFFFEF3C7)
        else -> Color(0xFF6B7280) to light_grey
    }
}

private fun formatAmount(amount: Double?): String {
    val value = (amount ?: 0.0).toLong()
    val s = value.toString()
    if (s.length <= 3) return s
    val last3 = s.takeLast(3)
    val rest = s.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "$grouped,$last3"
}

private fun formatExpenseDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val datePart = iso.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else iso
    } catch (_: Exception) {
        iso
    }
}

data class ExpenseSupplierOption(
    val id: String,
    val label: String
)

data class ExpenseTaxRateSubItem(
    val name: String,
    val rate: Double
)

data class ExpenseTaxGroupOption(
    val id: String,
    val label: String,
    val totalRate: Double,
    val taxRates: List<ExpenseTaxRateSubItem> = emptyList()
)

data class ExpenseLineFormRow(
    val rowKey: String = "line_${System.nanoTime()}",
    val expenseAccountId: String = "",
    val lineType: String = "Service",
    val itemDescription: String = "",
    val quantity: String = "1",
    val rate: String = "0",
    val taxTreatment: String = "EXEMPT",
    val taxGroupId: String = "",
    val taxExemptionReason: String = "No_Tax_Registration"
)

data class ExpenseLinePayload(
    val lineType: String,
    val expenseAccountId: String,
    val itemDescription: String,
    val quantity: Double,
    val rate: Double,
    val taxGroupId: String?,
    val taxExemptionReason: String?
)

private val EXPENSE_PAYMENT_MODES = linkedMapOf(
    "Cash" to "Cash",
    "Bank Transfer" to "Bank",
    "Corporate Card" to "Card",
    "UPI" to "UPI",
    "Cheque" to "Cheque"
)

private val EXPENSE_TAX_EXEMPTION_REASONS = linkedMapOf(
    "Unregistered Supplier / No Tax" to "No_Tax_Registration",
    "Exempt Goods / Services" to "Exempt_Goods",
    "Zero Rated Export" to "Zero_Rated_Export",
    "Reverse Charge (RCM)" to "Reverse_Charge",
    "Other Exemption Reason" to "Other"
)

private fun round2(num: Double): Double = (num * 100.0).roundToLong() / 100.0

@Composable
fun AddExpenseScreen(
    financeViewModel: FinanceViewModel,
    initialData: ExpenseItem? = null,
    mode: String = "create",
    supplierOptions: List<ExpenseSupplierOption> = emptyList(),
    taxGroupOptions: List<ExpenseTaxGroupOption> = emptyList(),
    orgCurrency: String = "INR",
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val isViewMode = mode == "view"
    val isEditMode = mode == "edit"
    val isEditable = !isViewMode

    val chartOfAccounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle()
    val isLoadingAccounts by financeViewModel.isLoadingChartOfAccounts.collectAsStateWithLifecycle()
    val createExpenseState by financeViewModel.createExpenseState.collectAsStateWithLifecycle()
    val createAccountState by financeViewModel.createAccountState.collectAsStateWithLifecycle()

    val branchViewModel: BranchViewModel = hiltViewModel()
    val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branchList = (branchUiState as? BranchUiState.Success)?.branches ?: emptyList()
    val isLoadingBranches = branchUiState is BranchUiState.Loading

    LaunchedEffect(Unit) {
        // Fetch full unpaginated chart of accounts for expense and payment dropdowns
        financeViewModel.fetchChartOfAccounts(paginate = false, includeDeleted = false)
        branchViewModel.loadBranches()
    }

    val expenseAccounts = remember(chartOfAccounts) {
        chartOfAccounts.filter {
            it.accountType?.trim().equals("Expense", ignoreCase = true) && !it.isGroup
        }
    }
    val paymentAccounts = remember(chartOfAccounts) {
        chartOfAccounts.filter {
            !it.isGroup && (
                    it.accountType?.trim().equals("Asset", ignoreCase = true) ||
                            it.accountName.contains("Cash", ignoreCase = true) ||
                            it.accountName.contains("Bank", ignoreCase = true)
                    )
        }
    }

    var expenseDate by remember {
        mutableStateOf(
            initialData?.expenseDate?.let { formatExpenseDate(it) }?.takeIf { it != "—" }
                ?: SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        )
    }
    var selectedBranch by remember { mutableStateOf<BranchItem?>(null) }
    var selectedSupplierId by remember { mutableStateOf(initialData?.supplierId?.id.orEmpty()) }
    var selectedPaidFromAccount by remember {
        mutableStateOf<ChartOfAccountItem?>(
            (initialData?.paidFromAccountId?._id ?: initialData?.paymentAccountId?._id)
                ?.let { id -> chartOfAccounts.find { it._id == id } }
        )
    }
    var paymentMode by remember { mutableStateOf(initialData?.paymentMode?.ifBlank { null } ?: "Bank") }
    var referenceNumber by remember { mutableStateOf(initialData?.referenceNumber ?: "") }
    var priceIncludesTax by remember { mutableStateOf(initialData?.priceIncludesTax ?: false) }
    var currency by remember { mutableStateOf(initialData?.currency?.ifBlank { null } ?: orgCurrency.ifBlank { "INR" }) }
    var notes by remember { mutableStateOf(initialData?.notes ?: "") }
    var lines by remember {
        mutableStateOf(
            if (!initialData?.lines.isNullOrEmpty()) {
                initialData.lines.mapIndexed { index, l ->
                    val taxGroup = l.taxGroupId?.id.orEmpty()
                    ExpenseLineFormRow(
                        rowKey = l._id.ifBlank { "line_${index}_${System.nanoTime()}" },
                        expenseAccountId = l.expenseAccountId?._id.orEmpty(),
                        lineType = l.lineType.ifBlank { "Service" },
                        itemDescription = l.itemDescription,
                        quantity = l.quantity.toString(),
                        rate = l.rate.toString(),
                        taxTreatment = if (taxGroup.isNotBlank()) "TAXABLE" else "EXEMPT",
                        taxGroupId = taxGroup,
                        taxExemptionReason = l.taxExemptionReason ?: if (taxGroup.isNotBlank()) "" else "No_Tax_Registration"
                    )
                }
            } else {
                listOf(
                    ExpenseLineFormRow(
                        expenseAccountId = initialData?.accountId?._id.orEmpty(),
                        rate = initialData?.amount?.takeIf { it > 0 }?.toString() ?: "0"
                    )
                )
            }
        )
    }
    var selectedDocuments by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Dropdown visibility states
    var branchExpanded by remember { mutableStateOf(false) }
    var supplierExpanded by remember { mutableStateOf(false) }
    var paidFromExpanded by remember { mutableStateOf(false) }
    var paymentModeExpanded by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var newAccountName by remember { mutableStateOf("") }

    // Validation error state map (e.g. "expenseDate", "paidFromAccountId", "lines[0].expenseAccountId")
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val isSaving = createExpenseState is CreateExpenseState.Loading
    val isSavingAccount = createAccountState is CreateAccountState.Loading

    // Auto-select single branch or prefill existing branchId in edit mode
    LaunchedEffect(branchList, initialData) {
        if (initialData != null && selectedBranch == null) {
            val targetBranchId = initialData.branchId?.id ?: initialData.branch
            selectedBranch = branchList.find { it.id == targetBranchId }
        } else if (selectedBranch == null && branchList.size == 1) {
            selectedBranch = branchList.first()
        }
    }

    LaunchedEffect(chartOfAccounts, initialData) {
        if (initialData != null && selectedPaidFromAccount == null) {
            val targetPaidId = initialData.paidFromAccountId?._id ?: initialData.paymentAccountId?._id
            selectedPaidFromAccount = chartOfAccounts.find { it._id == targetPaidId }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedDocuments = (selectedDocuments + uris).distinct().take(5)
        }
    }

    LaunchedEffect(createExpenseState) {
        when (createExpenseState) {
            is CreateExpenseState.Success -> {
                financeViewModel.resetCreateExpenseState()
                onSaved()
            }
            else -> Unit
        }
    }

    LaunchedEffect(createAccountState) {
        when (createAccountState) {
            is CreateAccountState.Success -> {
                financeViewModel.resetCreateAccountState()
                showAddAccountDialog = false
                newAccountName = ""
                financeViewModel.fetchChartOfAccounts()
            }
            else -> Unit
        }
    }

    // Real-time tax and subtotal computation matching React calculationResults
    val calculationSummary = remember(lines, priceIncludesTax, taxGroupOptions) {
        var subtotal = 0.0
        var totalTax = 0.0
        val breakdown = linkedMapOf<String, Double>()

        val computedLineTotals = lines.map { line ->
            val qty = line.quantity.toDoubleOrNull() ?: 0.0
            val rate = line.rate.toDoubleOrNull() ?: 0.0
            val gross = round2(qty * rate)
            val matchedGroup = taxGroupOptions.find { it.id == line.taxGroupId }
            val totalRate = if (line.taxTreatment == "TAXABLE" && matchedGroup != null) matchedGroup.totalRate else 0.0

            val taxable: Double
            val lineTax: Double
            if (priceIncludesTax && totalRate > 0.0) {
                taxable = round2(gross / (1.0 + totalRate / 100.0))
                lineTax = round2(gross - taxable)
            } else {
                taxable = gross
                lineTax = round2((gross * totalRate) / 100.0)
            }

            subtotal += taxable
            totalTax += lineTax

            if (matchedGroup != null && lineTax > 0.0 && totalRate > 0.0) {
                matchedGroup.taxRates.forEach { subTax ->
                    if (subTax.rate > 0.0) {
                        val subAmount = round2((lineTax * subTax.rate) / totalRate)
                        val key = "${subTax.name} (${subTax.rate}%)"
                        breakdown[key] = (breakdown[key] ?: 0.0) + subAmount
                    }
                }
            }

            Triple(taxable, lineTax, round2(taxable + lineTax))
        }

        object {
            val lineTotals = computedLineTotals
            val computedSubtotal = round2(subtotal)
            val computedTotalTax = round2(totalTax)
            val computedGrandTotal = round2(subtotal + totalTax)
            val taxBreakdown = breakdown
        }
    }

    fun validateForm(): Boolean {
        val err = linkedMapOf<String, String>()
        if (expenseDate.isBlank()) err["expenseDate"] = "Date is required"
        if (selectedPaidFromAccount == null) err["paidFromAccountId"] = "Paid-from account required"
        if (paymentMode.isBlank()) err["paymentMode"] = "Payment mode required"
        if (lines.isEmpty()) err["lines"] = "Add at least one expense line"

        lines.forEachIndexed { idx, line ->
            if (line.expenseAccountId.isBlank()) err["lines[$idx].expenseAccountId"] = "Select account"
            if (line.itemDescription.trim().isBlank()) err["lines[$idx].itemDescription"] = "Description required"
            if ((line.quantity.toDoubleOrNull() ?: 0.0) <= 0.0) err["lines[$idx].quantity"] = "Must be > 0"
            val parsedRate = line.rate.toDoubleOrNull()
            if (line.rate.isBlank() || parsedRate == null || parsedRate < 0.0) {
                err["lines[$idx].rate"] = "Invalid rate"
            }
            if (line.taxTreatment == "TAXABLE" && line.taxGroupId.isBlank()) {
                err["lines[$idx].taxGroupId"] = "Select applicable tax group"
            }
            if (line.taxTreatment == "EXEMPT" && line.taxExemptionReason.isBlank()) {
                err["lines[$idx].taxExemptionReason"] = "Exemption reason required"
            }
        }

        errors = err
        return err.isEmpty()
    }

    fun submitExpense(targetStatus: String) {
        if (isViewMode || !validateForm()) return

        val formattedLines = lines.map { l ->
            val isTaxable = l.taxTreatment == "TAXABLE" && l.taxGroupId.isNotBlank()
            ExpenseLinePayload(
                lineType = l.lineType,
                expenseAccountId = l.expenseAccountId,
                itemDescription = l.itemDescription.trim(),
                quantity = l.quantity.toDoubleOrNull() ?: 1.0,
                rate = l.rate.toDoubleOrNull() ?: 0.0,
                taxGroupId = if (isTaxable) l.taxGroupId else null,
                taxExemptionReason = if (isTaxable) null else l.taxExemptionReason.ifBlank { "No_Tax_Registration" }
            )
        }

        val gson = Gson()
        val fields = linkedMapOf<String, String>().apply {
            put("expenseDate", expenseDate.toIsoDateFromDDMMYYYY())
            put("paidFromAccountId", selectedPaidFromAccount?._id.orEmpty())
            put("paymentMode", paymentMode)
            put("referenceNumber", referenceNumber.trim())
            selectedBranch?.id?.takeIf { it.isNotBlank() }?.let { put("branchId", it) }
            if (selectedSupplierId.isNotBlank()) put("supplierId", selectedSupplierId)
            put("currency", currency.uppercase(Locale.ENGLISH))
            put("exchangeRate", "1")
            put("priceIncludesTax", priceIncludesTax.toString())
            put("status", targetStatus)
            put("notes", notes.trim())
            put("lines", gson.toJson(formattedLines))
        }
        val attachmentParts = buildExpenseAttachmentParts(context, selectedDocuments)

        // Call your FinanceViewModel multipart/JSON submit method here
        financeViewModel.submitExpenseVoucher(
            expenseId = if (isEditMode) initialData?._id else null,
            fields = fields,
            attachmentParts = attachmentParts
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = when {
                isViewMode -> "Expense: ${initialData?.expenseNumber.orEmpty()}"
                isEditMode -> "Edit Expense: ${initialData?.expenseNumber.orEmpty()}"
                else -> "Record New Expense"
            },
            subtitle = "Direct posting or voucher entry with tax computation",
            onClose = onClose
        )
        HorizontalDivider(color = title_border)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Group 1: Expense Date, Assigned Branch, Supplier
            Column {
                FormLabel("Expense Date", isRequired = true)
                ErrorFieldWrapper(
                    isError = errors.containsKey("expenseDate"),
                    errorMessage = errors["expenseDate"]
                ) {
                    DatePickerField(
                        value = expenseDate,
                        onDateSelected = {
                            expenseDate = it
                            errors = errors - "expenseDate"
                        },
                        enabled = isEditable
                    )
                }
            }

            FormDropdown(
                label = "Assigned Branch",
                value = selectedBranch?.name ?: if (isLoadingBranches) "Loading..." else "Select Branch",
                expanded = branchExpanded && isEditable,
                onExpandChange = { if (isEditable) branchExpanded = it },
                options = branchList.mapNotNull { it.name },
                onOptionSelected = { name ->
                    selectedBranch = branchList.find { it.name == name }
                },
                enabled = isEditable && !isLoadingBranches
            )

            FormDropdown(
                label = "Supplier / Beneficiary",
                value = supplierOptions.find { it.id == selectedSupplierId }?.label ?: "Select Supplier (Optional)",
                expanded = supplierExpanded && isEditable,
                onExpandChange = { if (isEditable) supplierExpanded = it },
                options = supplierOptions.map { it.label },
                onOptionSelected = { label ->
                    selectedSupplierId = supplierOptions.find { it.label == label }?.id.orEmpty()
                },
                enabled = isEditable
            )

            // Header Group 2: Paid From Account, Payment Mode, Reference #
            FormDropdown(
                label = "Paid From Account",
                value = selectedPaidFromAccount?.accountName ?: if (isLoadingAccounts) "Loading accounts..." else "Select Cash/Bank Asset",
                expanded = paidFromExpanded && isEditable,
                onExpandChange = { if (isEditable) paidFromExpanded = it },
                options = paymentAccounts.map { it.accountName },
                onOptionSelected = { name ->
                    selectedPaidFromAccount = paymentAccounts.find { it.accountName == name }
                    errors = errors - "paidFromAccountId"
                },
                isRequired = true,
                isError = errors.containsKey("paidFromAccountId"),
                errorMessage = errors["paidFromAccountId"],
                enabled = isEditable && !isLoadingAccounts
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormDropdown(
                        label = "Payment Mode",
                        value = EXPENSE_PAYMENT_MODES.entries.firstOrNull { it.value == paymentMode }?.key ?: paymentMode,
                        expanded = paymentModeExpanded && isEditable,
                        onExpandChange = { if (isEditable) paymentModeExpanded = it },
                        options = EXPENSE_PAYMENT_MODES.keys.toList(),
                        onOptionSelected = { label ->
                            paymentMode = EXPENSE_PAYMENT_MODES[label] ?: "Bank"
                            errors = errors - "paymentMode"
                        },
                        isRequired = true,
                        isError = errors.containsKey("paymentMode"),
                        errorMessage = errors["paymentMode"],
                        enabled = isEditable
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Reference # (Cheque / Txn ID)")
                    FormTextField(
                        value = referenceNumber,
                        onValueChange = { referenceNumber = it },
                        placeholder = "e.g. UTR-9832712",
                        enabled = isEditable
                    )
                }
            }

            // Tax Inclusive Toggle Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    MiniSwitch(
                        checked = priceIncludesTax,
                        onCheckedChange = { if (isEditable) priceIncludesTax = it },
                        enabled = isEditable
                    )
                    Text(
                        text = "Line Item Rates are Tax Inclusive",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Currency: $currency",
                    fontSize = tokens.caption,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Expense Line Items Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXPENSE LINE ITEMS",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (isEditable) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { showAddAccountDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+ New Account", fontSize = tokens.caption, color = TextSecondary)
                        }
                        TextButton(
                            onClick = { lines = lines + ExpenseLineFormRow() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Line Item", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            lines.forEachIndexed { idx, line ->
                var lineAccountExpanded by remember(line.rowKey) { mutableStateOf(false) }
                var taxGroupExpanded by remember(line.rowKey) { mutableStateOf(false) }
                var exemptionExpanded by remember(line.rowKey) { mutableStateOf(false) }
                val isTaxable = line.taxTreatment == "TAXABLE"
                val lineCalc = calculationSummary.lineTotals.getOrNull(idx) ?: Triple(0.0, 0.0, 0.0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Line Item #${idx + 1}",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (isEditable && lines.size > 1) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove line",
                                    tint = redText,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            lines = lines.filterIndexed { i, _ -> i != idx }
                                        }
                                )
                            }
                        }

                        FormDropdown(
                            label = "Expense Account",
                            value = expenseAccounts.find { it._id == line.expenseAccountId }?.accountName
                                ?: if (isLoadingAccounts) "Loading..." else "Select Account",
                            expanded = lineAccountExpanded && isEditable,
                            onExpandChange = { if (isEditable) lineAccountExpanded = it },
                            options = expenseAccounts.map { it.accountName },
                            onOptionSelected = { name ->
                                val pickedId = expenseAccounts.find { it.accountName == name }?._id.orEmpty()
                                lines = lines.mapIndexed { i, row ->
                                    if (i == idx) row.copy(expenseAccountId = pickedId) else row
                                }
                                errors = errors - "lines[$idx].expenseAccountId"
                            },
                            isRequired = true,
                            isError = errors.containsKey("lines[$idx].expenseAccountId"),
                            errorMessage = errors["lines[$idx].expenseAccountId"],
                            enabled = isEditable && !isLoadingAccounts
                        )

                        Column {
                            FormLabel("Description", isRequired = true)
                            FormTextField(
                                value = line.itemDescription,
                                onValueChange = { desc ->
                                    lines = lines.mapIndexed { i, row ->
                                        if (i == idx) row.copy(itemDescription = desc) else row
                                    }
                                    errors = errors - "lines[$idx].itemDescription"
                                },
                                placeholder = "e.g. Office stationery",
                                isError = errors.containsKey("lines[$idx].itemDescription"),
                                errorMessage = errors["lines[$idx].itemDescription"],
                                enabled = isEditable
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Qty", isRequired = true)
                                FormTextField(
                                    value = line.quantity,
                                    onValueChange = { qty ->
                                        val filtered = qty.filterIndexed { index, c ->
                                            c.isDigit() || (c == '.' && qty.indexOf('.') == index)
                                        }
                                        lines = lines.mapIndexed { i, row ->
                                            if (i == idx) row.copy(quantity = filtered) else row
                                        }
                                        errors = errors - "lines[$idx].quantity"
                                    },
                                    keyboardType = KeyboardType.Decimal,
                                    isError = errors.containsKey("lines[$idx].quantity"),
                                    errorMessage = errors["lines[$idx].quantity"],
                                    enabled = isEditable
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Rate (${formatCurrency()})", isRequired = true)
                                FormTextField(
                                    value = line.rate,
                                    onValueChange = { rateStr ->
                                        val filtered = rateStr.filterIndexed { index, c ->
                                            c.isDigit() || (c == '.' && rateStr.indexOf('.') == index)
                                        }
                                        lines = lines.mapIndexed { i, row ->
                                            if (i == idx) row.copy(rate = filtered) else row
                                        }
                                        errors = errors - "lines[$idx].rate"
                                    },
                                    keyboardType = KeyboardType.Decimal,
                                    isError = errors.containsKey("lines[$idx].rate"),
                                    errorMessage = errors["lines[$idx].rate"],
                                    enabled = isEditable
                                )
                            }
                        }

                        // Tax Treatment & Rule Selector
                        Column {
                            FormLabel("Tax Treatment & Rule", isRequired = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(light_grey, RoundedCornerShape(8.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("TAXABLE" to "Tax Applicable", "EXEMPT" to "Zero / Exempt").forEach { (id, label) ->
                                    val selected = line.taxTreatment == id
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selected) whiteBg else Color.Transparent)
                                            .clickable(enabled = isEditable) {
                                                lines = lines.mapIndexed { i, row ->
                                                    if (i == idx) {
                                                        if (id == "TAXABLE") {
                                                            row.copy(
                                                                taxTreatment = "TAXABLE",
                                                                taxGroupId = taxGroupOptions.firstOrNull()?.id.orEmpty(),
                                                                taxExemptionReason = ""
                                                            )
                                                        } else {
                                                            row.copy(
                                                                taxTreatment = "EXEMPT",
                                                                taxGroupId = "",
                                                                taxExemptionReason = "No_Tax_Registration"
                                                            )
                                                        }
                                                    } else row
                                                }
                                            }
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = if (selected) Primary else TextSecondary
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))

                            if (isTaxable) {
                                FormDropdown(
                                    value = taxGroupOptions.find { it.id == line.taxGroupId }?.label ?: "Select Tax Group",
                                    expanded = taxGroupExpanded && isEditable,
                                    onExpandChange = { if (isEditable) taxGroupExpanded = it },
                                    options = taxGroupOptions.map { it.label },
                                    onOptionSelected = { label ->
                                        val pickedId = taxGroupOptions.find { it.label == label }?.id.orEmpty()
                                        lines = lines.mapIndexed { i, row ->
                                            if (i == idx) row.copy(taxGroupId = pickedId) else row
                                        }
                                        errors = errors - "lines[$idx].taxGroupId"
                                    },
                                    isError = errors.containsKey("lines[$idx].taxGroupId"),
                                    errorMessage = errors["lines[$idx].taxGroupId"],
                                    enabled = isEditable
                                )
                            } else {
                                FormDropdown(
                                    value = EXPENSE_TAX_EXEMPTION_REASONS.entries.firstOrNull { it.value == line.taxExemptionReason }?.key
                                        ?: "Select Exemption Reason",
                                    expanded = exemptionExpanded && isEditable,
                                    onExpandChange = { if (isEditable) exemptionExpanded = it },
                                    options = EXPENSE_TAX_EXEMPTION_REASONS.keys.toList(),
                                    onOptionSelected = { label ->
                                        val reasonId = EXPENSE_TAX_EXEMPTION_REASONS[label] ?: "No_Tax_Registration"
                                        lines = lines.mapIndexed { i, row ->
                                            if (i == idx) row.copy(taxExemptionReason = reasonId) else row
                                        }
                                        errors = errors - "lines[$idx].taxExemptionReason"
                                    },
                                    isError = errors.containsKey("lines[$idx].taxExemptionReason"),
                                    errorMessage = errors["lines[$idx].taxExemptionReason"],
                                    enabled = isEditable
                                )
                            }
                        }

                        // Line breakdown summary footer
                        HorizontalDivider(color = grey_border)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Taxable: ${formatCurrency()}${String.format(Locale.US, "%.2f", lineCalc.first)}",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                            Text(
                                text = "Tax: ${formatCurrency()}${String.format(Locale.US, "%.2f", lineCalc.second)}",
                                fontSize = tokens.caption,
                                color = greentext
                            )
                            Text(
                                text = "Total: ${formatCurrency()}${String.format(Locale.US, "%.2f", lineCalc.third)}",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Attachments Section
            Column {
                FormLabel("Expense Bills & Proof Receipts")
                ImageUploadSection<Uri>(
                    selectedImages = selectedDocuments,
                    browseText = "Browse Files",
                    onBrowseClick = {
                        if (isEditable && selectedDocuments.size < 5) {
                            filePickerLauncher.launch("*/*")
                        }
                    },
                    onRemoveImage = { removedUri ->
                        if (isEditable) {
                            selectedDocuments = selectedDocuments.filterNot { it == removedUri }
                        }
                    },
                    maxFiles = 5,
                    title = "Upload Invoices or Payment Vouchers",
                    subtitle = "PNG, JPG, WEBP or PDF (up to 5 files max)",
                    supportedFormatsText = "Supports .png, .jpg, .webp and .pdf files"
                )
            }

            // Notes
            Column {
                FormLabel("Internal Remarks & Audit Notes")
                FormTextArea(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Details of expense purpose, authorized approvals, or voucher cross-references...",
                    enabled = isEditable
                )
            }

            // Financial Summary Card with Sub-Tax Breakdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal (Taxable Amount):", fontSize = tokens.bodySmall, color = TextSecondary)
                    Text(
                        text = "${formatCurrency()}${String.format(Locale.US, "%.2f", calculationSummary.computedSubtotal)}",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                if (calculationSummary.taxBreakdown.isNotEmpty()) {
                    HorizontalDivider(color = grey_border)
                    calculationSummary.taxBreakdown.forEach { (label, amt) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, fontSize = tokens.caption, color = mutedText)
                            Text(
                                text = "${formatCurrency()}${String.format(Locale.US, "%.2f", amt)}",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        }
                    }
                    HorizontalDivider(color = grey_border)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Tax:", fontSize = tokens.bodySmall, color = TextSecondary)
                    Text(
                        text = "+${formatCurrency()}${String.format(Locale.US, "%.2f", calculationSummary.computedTotalTax)}",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = greentext
                    )
                }

                HorizontalDivider(color = sectionBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Grand Total:", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(
                        text = "${formatCurrency()}${String.format(Locale.US, "%.2f", calculationSummary.computedGrandTotal)}",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // Bottom Action Footer matching React Draft / Post / Update buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .border(1.dp, title_border)
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onClose,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            ) {
                Text("Close", fontSize = tokens.bodySmall, color = TextSecondary)
            }

            if (isEditable && isEditMode) {
                Button(
                    onClick = { submitExpense("Draft") },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1.4f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                ) {
                    Text(
                        text = if (isSaving) "Saving..." else "Update Draft Voucher",
                        fontSize = tokens.bodySmall,
                        color = whiteBg,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (isEditable) {
                OutlinedButton(
                    onClick = { submitExpense("Draft") },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                ) {
                    Text(
                        text = if (isSaving) "Saving..." else "Save as Draft",
                        fontSize = tokens.bodySmall,
                        color = Primary
                    )
                }
                Button(
                    onClick = { submitExpense("Posted") },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                ) {
                    Text(
                        text = if (isSaving) "Posting..." else "Post to Ledger",
                        fontSize = tokens.bodySmall,
                        color = whiteBg,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showAddAccountDialog) {
        Dialog(onDismissRequest = { showAddAccountDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(20.dp)
            ) {
                Text(
                    "Add Expense Account",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(16.dp))

                FormLabel("Account Name")
                FormTextField(
                    value = newAccountName,
                    onValueChange = { newAccountName = it },
                    placeholder = "Enter account name"
                )
                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            showAddAccountDialog = false
                            newAccountName = ""
                        },
                        enabled = !isSavingAccount,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                    ) {
                        Text("Cancel", color = TextSecondary, fontSize = tokens.bodyMedium)
                    }

                    Button(
                        onClick = {
                            if (newAccountName.isNotBlank()) {
                                financeViewModel.createChartOfAccount(
                                    accountName = newAccountName,
                                    accountType = "Expense"
                                )
                            }
                        },
                        enabled = !isSavingAccount,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                    ) {
                        if (isSavingAccount) {
                            CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save", color = whiteBg, fontSize = tokens.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

private fun String.toIsoDateFromDDMMYYYY(): String {
    return try {
        val parts = this.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}T00:00:00.000Z" else this
    } catch (_: Exception) {
        this
    }
}

private fun buildExpenseAttachmentParts(
    context: Context,
    uris: List<Uri>
): List<MultipartBody.Part> {
    return uris.mapIndexedNotNull { index, uri ->
        try {
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            var fileName = "receipt_$index"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
            }
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@mapIndexedNotNull null
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("attachments", fileName, requestBody)
        } catch (_: Exception) {
            null
        }
    }
}