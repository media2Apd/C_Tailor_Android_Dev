package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.BranchItem
import com.cuso.mobile.model.finance.ChartOfAccountItem
import com.cuso.mobile.model.finance.ExpenseItem
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.home.FormDateField
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.StepNavigationFab
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.viewmodel.BranchUiState
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.CreateExpenseState
import com.cuso.mobile.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.InsertDriveFile

private val ExpensePrimary = Color(0xFF3B3BF9)
private val ExpenseBg = Color(0xFFF5F5F5)
private val ExpenseBorder = Color(0xFFE5E7EB)

// ─────────────────────────────────────────────────────────────
// 🧾 EXPENSES SCREEN (List / Empty state) — now wired to real API
// ─────────────────────────────────────────────────────────────

@Composable
fun ExpensesScreen(
    onClose: () -> Unit
) {
    val financeViewModel: FinanceViewModel = hiltViewModel()

    var showAddExpense by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedExpenseForView by remember { mutableStateOf<ExpenseItem?>(null) }

    val expenses by financeViewModel.expenseList.collectAsStateWithLifecycle()
    val isLoadingExpenses by financeViewModel.isLoadingExpenses.collectAsStateWithLifecycle()
    val expenseError by financeViewModel.expenseError.collectAsStateWithLifecycle()

    // ✅ Fetch real expenses + chart of accounts when screen opens
    LaunchedEffect(Unit) {
        financeViewModel.fetchExpenses()
        financeViewModel.fetchChartOfAccounts()
    }

    if (showAddExpense) {
        AddExpenseScreen(
            financeViewModel = financeViewModel,
            onClose = { showAddExpense = false },
            onSaved = {
                showAddExpense = false
                financeViewModel.fetchExpenses()   // refresh list after add
            }
        )
        return
    }
    selectedExpenseForView?.let { expense ->
        ExpenseDetailScreen(
            expense = expense,
            onClose = { selectedExpenseForView = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("All Expenses", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onClose() }
            )
        }

        // ── Breadcrumb ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Finance", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
            Text("Expenses", fontSize = 13.sp, color = ExpensePrimary, fontWeight = FontWeight.SemiBold)
        }

        // ── Search + Filter ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(ExpenseBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text("Search Customers...", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                        }
                        inner()
                    }
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, ExpenseBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF374151), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Transaction History",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))

        val filtered = expenses.filter {
            searchQuery.isBlank() ||
                    it.accountId.accountName.contains(searchQuery, ignoreCase = true) ||
                    it.expenseNumber.contains(searchQuery, ignoreCase = true)
        }

        when {
            isLoadingExpenses -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(ExpenseBg), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ExpensePrimary)
                }
            }

            expenseError != null -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(ExpenseBg), contentAlignment = Alignment.Center) {
                    Text(expenseError ?: "Failed to load expenses", color = Color.Red, fontSize = 13.sp)
                }
            }

            filtered.isEmpty() -> {
                // ── Empty state ──
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ExpenseBg)
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFE7E5FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF9B96F5), modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No Expenses Found", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Start by creating your first expense record",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { showAddExpense = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpensePrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Expenses", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // AFTER
            else -> {
                FabScaffold(
                    modifier = Modifier.fillMaxSize(),
                    fab = FabConfig(
                        label = "Add Expenses",
                        icon = Icons.Default.Add,
                        onClick = { showAddExpense = true }
                    )
                ) {
                    // ── List (real API data) ──
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ExpenseBg)
                    ) {
                        items(filtered) { expense ->
                            val (statusFg, statusBg) = expenseStatusColors(expense.status)
                            DataCard(
                                item = expense,
                                dateText = formatExpenseDate(expense.expenseDate),
                                topBadgeText = expense.status,
                                topBadgeTextColor = statusFg,
                                topBadgeBgColor = statusBg,
                                title = expense.accountId.accountName,
                                subtitle = "Paid via ${expense.paymentAccountId.accountName}",
                                footerFields = listOf(
                                    DataCardField(
                                        asRow = true,
                                        label = "Amount",
                                        text = "₹${formatAmount(expense.amount)}"
                                    )
                                ),
                                actions = listOf(
                                    MenuAction(
                                        label = "View",
                                        icon = Icons.Default.Visibility,
                                        onClick = { selectedExpenseForView = expense }
                                    )
                                )
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────
// 👁️ EXPENSE DETAIL SCREEN — read-only view
// ─────────────────────────────────────────────────────────────

@Composable
fun ExpenseDetailScreen(
    expense: ExpenseItem,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Expense Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onClose() }
            )
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ExpenseDetailField("Expense Number", expense.expenseNumber)
            ExpenseDetailField("Expense Date", formatExpenseDate(expense.expenseDate))
            ExpenseDetailField("Expense Account", expense.accountId.accountName)
            ExpenseDetailField("Payment Mode", expense.paymentAccountId.accountName)
            ExpenseDetailField("Amount", "₹${formatAmount(expense.amount)}")

            FormLabel("Status")
            Spacer(Modifier.height(6.dp))
            val (statusFg, statusBg) = expenseStatusColors(expense.status)
            Box(
                modifier = Modifier
                    .background(statusBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(expense.status, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusFg)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExpenseDetailField(label: String, value: String) {
    FormLabel(label)
    Spacer(Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ExpenseBg, RoundedCornerShape(8.dp))
            .border(1.dp, ExpenseBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(value, fontSize = 14.sp, color = Color(0xFF374151))
    }
    Spacer(Modifier.height(14.dp))
}
private fun expenseStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "paid" -> Color(0xFF16A34A) to Color(0xFFDCFCE7)      // text, bg
        "pending" -> Color(0xFFCA8A04) to Color(0xFFFEF3C7)
        else -> Color(0xFF6B7280) to Color(0xFFF3F4F6)
    }
}

private fun formatAmount(amount: Double): String {
    val value = amount.toLong()
    val s = value.toString()
    if (s.length <= 3) return s
    val last3 = s.takeLast(3)
    val rest = s.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "$grouped,$last3"
}

private fun formatExpenseDate(iso: String): String {
    return try {
        val datePart = iso.take(10) // "2026-07-15"
        val parts = datePart.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else iso
    } catch (_: Exception) {
        iso
    }
}

// ─────────────────────────────────────────────────────────────
// ➕ ADD EXPENSE SCREEN — now wired to real Chart of Accounts + create API
// ─────────────────────────────────────────────────────────────

@Composable
fun AddExpenseScreen(
    financeViewModel: FinanceViewModel,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    // AFTER
    val chartOfAccounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle()
    val isLoadingAccounts by financeViewModel.isLoadingChartOfAccounts.collectAsStateWithLifecycle()
    val createExpenseState by financeViewModel.createExpenseState.collectAsStateWithLifecycle()
    val createAccountState by financeViewModel.createAccountState.collectAsStateWithLifecycle()
    // ✅ Category options = leaf Expense accounts (e.g. Salary, Rent, Electricity...)
    val categoryAccounts = remember(chartOfAccounts) {
        chartOfAccounts.filter { it.accountType == "Expense" && !it.isGroup }
    }
    // ✅ Payment Mode options = leaf Asset accounts you can pay FROM (Cash, Bank...)
    val paymentAccounts = remember(chartOfAccounts) {
        chartOfAccounts.filter { it.accountType == "Asset" && it.allowManualEntry }
    }

    var expenseDate by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
    }

    // AFTER
    var selectedCategory by remember { mutableStateOf<ChartOfAccountItem?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var newAccountName by remember { mutableStateOf("") }

    var selectedPaymentAccount by remember { mutableStateOf<ChartOfAccountItem?>(null) }
    var paymentModeExpanded by remember { mutableStateOf(false) }

    val branchViewModel: BranchViewModel = hiltViewModel()
    val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
    }

    val branchList = (branchUiState as? BranchUiState.Success)?.branches ?: emptyList()
    val isLoadingBranches = branchUiState is BranchUiState.Loading

    var selectedBranch by remember { mutableStateOf<BranchItem?>(null) }
    var branchExpanded by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }

    var expenseType by remember { mutableStateOf("Operational") }
    var expenseTypeExpanded by remember { mutableStateOf(false) }
    val expenseTypeOptions = listOf("Operational", "Capital", "Administrative", "Miscellaneous")

    var referenceNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // AFTER
    val isSaving = createExpenseState is CreateExpenseState.Loading
    val isSavingAccount = createAccountState is com.cuso.mobile.viewmodel.CreateAccountState.Loading

    val context = LocalContext.current
    var selectedDocumentUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDocumentName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedDocumentUri = uri
            selectedDocumentName = getFileNameFromUri(context, uri)
        }
    }

    // ✅ Handle save result
    LaunchedEffect(createExpenseState) {
        when (val state = createExpenseState) {
            is CreateExpenseState.Success -> {
                financeViewModel.resetCreateExpenseState()
                onSaved()
            }
            else -> Unit
        }
    }

    // ✅ Handle add-account result — same pattern as expense save
    LaunchedEffect(createAccountState) {
        when (val state = createAccountState) {
            is com.cuso.mobile.viewmodel.CreateAccountState.Success -> {
                financeViewModel.resetCreateAccountState()
                showAddAccountDialog = false
                newAccountName = ""
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // ── Header ──
            Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Add Expense", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier
                    .size(22.dp)
                    .clickable(enabled = !isSaving) { onClose() }
            )
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        if ((createExpenseState as? CreateExpenseState.Error) != null) {
            Text(
                (createExpenseState as CreateExpenseState.Error).message,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Expense Details", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Spacer(Modifier.height(14.dp))

            FormLabel("Expense Date")
            DatePickerField(
                value = expenseDate,
                onDateSelected = { expenseDate = it }
            )
            Spacer(Modifier.height(14.dp))


            // AFTER
// ── Category — real Chart of Accounts (Expense accounts) + Add New ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Box(modifier = Modifier.weight(1f)) {
                    FormDropdown(
                        label = "Expense Account",
                        value = selectedCategory?.accountName ?: if (isLoadingAccounts) "Loading..." else "Select An Option",
                        expanded = categoryExpanded,
                        onExpandChange = { categoryExpanded = it },
                        options = categoryAccounts.map { it.accountName },
                        onOptionSelected = { name ->
                            selectedCategory = categoryAccounts.find { it.accountName == name }
                        },
                        enabled = !isLoadingAccounts
                    )
                }
                Box(
                    Modifier
                        .background(Primary, RoundedCornerShape(5.dp))
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Expense Account",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(5.dp)
                            .size(28.dp)
                            .clickable { showAddAccountDialog = true }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            FormDropdown(
                label = "Branch",
                value = selectedBranch?.name ?: if (isLoadingBranches) "Loading..." else "Select An Option",
                expanded = branchExpanded,
                onExpandChange = { branchExpanded = it },
                options = branchList.map { it.name.orEmpty() },
                onOptionSelected = { name ->
                    selectedBranch = branchList.find { it.name == name }
                },
                enabled = !isLoadingBranches
            )
            Spacer(Modifier.height(14.dp))

            // ── Amount + Payment Mode ──
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Amount")
                    FormTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        placeholder = "Enter Amount"
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormDropdown(
                        label = "Payment Mode",
                        value = selectedPaymentAccount?.accountName ?: if (isLoadingAccounts) "Loading..." else "Select An Option",
                        expanded = paymentModeExpanded,
                        onExpandChange = { paymentModeExpanded = it },
                        options = paymentAccounts.map { it.accountName },
                        onOptionSelected = { name ->
                            selectedPaymentAccount = paymentAccounts.find { it.accountName == name }
                        },
                        enabled = !isLoadingAccounts
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            FormDropdown(
                label = "Expense Type",
                value = expenseType,
                expanded = expenseTypeExpanded,
                onExpandChange = { expenseTypeExpanded = it },
                options = expenseTypeOptions,
                onOptionSelected = { expenseType = it }
            )
            Spacer(Modifier.height(14.dp))

            FormLabel("Reference Number")
            FormTextField(
                value = referenceNumber,
                onValueChange = { referenceNumber = it },
                placeholder = "Enter reference number"
            )
            Spacer(Modifier.height(14.dp))

            FormLabel("Notes")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, ExpenseBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                    decorationBox = { inner ->
                        if (notes.isEmpty()) Text("Add Notes", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                        inner()
                    }
                )
            }
            Spacer(Modifier.height(20.dp))

            Text("Documentation & Receipts", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Spacer(Modifier.height(10.dp))

            if (selectedDocumentName != null) {
                // ── Uploaded file shown with remove button ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F7FE), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFD6D3FB), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = ExpensePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            selectedDocumentName ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFF374151),
                            maxLines = 1
                        )
                    }
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                selectedDocumentUri = null
                                selectedDocumentName = null
                            }
                    )
                }
            } else {
                // ── Empty upload box ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Color(0xFFF7F7FE), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFD6D3FB), RoundedCornerShape(10.dp))
                        .clickable { filePickerLauncher.launch("*/*") },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ExpensePrimary, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Drag and drop files here", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            }
            Spacer(Modifier.height(80.dp))
        }

        // ── Cancel / Save footer ──
        }   // ← closes the inner Column (header + scrollable form)

        // ── Cancel / Save footer (StepNavigationFab) ──
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            backEnabled = !isSaving,
            trailingAction = TrailingFabAction.Update(
                isLoading = isSaving,
                label = "Save",
                onClick = {
                    val category = selectedCategory
                    val paymentAccount = selectedPaymentAccount
                    val branchItem = selectedBranch
                    if (category != null && paymentAccount != null && branchItem != null && amount.isNotBlank()) {
                        financeViewModel.createExpense(
                            branch = branchItem.id,
                            expenseDate = expenseDate.toIsoDateFromDDMMYYYY(),
                            accountId = category._id,
                            paymentAccountId = paymentAccount._id,
                            amount = amount,
                            referenceNumber = referenceNumber.ifBlank { null },
                            notes = notes.ifBlank { null },
                            status = "Paid"
                        )
                    }
                }
            )
        )

        if (showAddAccountDialog) {
            Dialog(onDismissRequest = { showAddAccountDialog = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        "Add Expense Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
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
                        // AFTER
                        OutlinedButton(
                            onClick = {
                                showAddAccountDialog = false
                                newAccountName = ""
                            },
                            enabled = !isSavingAccount,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", color = Color(0xFF374151))
                        }
                        // AFTER
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
                            colors = ButtonDefaults.buttonColors(containerColor = ExpensePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isSavingAccount) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Save", color = Color.White)
                            }
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

private fun getFileNameFromUri(context: android.content.Context, uri: Uri): String {
    var name = "Unknown file"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}