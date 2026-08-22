@file:Suppress(
    "UNUSED_PARAMETER",
    "UNUSED",
    "RedundantSuppression",
    "unused_variable",
    "AssignedValueIsNeverRead",
    "VariableNeverRead"
)

package com.cuso.mobile.view.home.finance

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.BranchItem
import com.cuso.mobile.model.finance.ChartOfAccountItem
import com.cuso.mobile.model.finance.ExpenseItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.ErrorFieldWrapper
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.FieldValidator
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.PlanLimitDialog
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.ValidationField
import com.cuso.mobile.viewmodel.BranchUiState
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.CreateExpenseState
import com.cuso.mobile.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ExpensePrimary = Color(0xFF3B3BF9)
private val ExpenseBg = Color(0xFFF5F5F5)
private val ExpenseBorder = Color(0xFFE5E7EB)

@Composable
fun ExpensesScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val financeViewModel: FinanceViewModel = hiltViewModel()

    var showAddExpense by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedExpenseForView by remember { mutableStateOf<ExpenseItem?>(null) }

    val expenses by financeViewModel.expenseList.collectAsStateWithLifecycle()
    val isLoadingExpenses by financeViewModel.isLoadingExpenses.collectAsStateWithLifecycle()
    val expenseError by financeViewModel.expenseError.collectAsStateWithLifecycle()

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
                financeViewModel.fetchExpenses()
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
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleBar("All Expense", onClose = onClose)
        }

        Column(Modifier.fillMaxWidth()) {
            ScreenBreadcrumb(
                segments = listOf("Finance", "Expenses"),
                onClick = { onBreadCrumbClick() }
            )

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
        HorizontalDivider(color = Color(0xFFF0F0F0))

        val filtered = expenses.filter {
            val query = searchQuery.trim()
            query.isBlank() || it.accountId.accountName.contains(query, ignoreCase = true) || it.expenseNumber.contains(query, ignoreCase = true) || (it.referenceNumber?.contains(query, ignoreCase = true) == true)
        }

        when {
            isLoadingExpenses -> {
                ListSkeleton()
            }

            expenseError != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(tokens.iconSize * 2.4f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Something went wrong, Please try again later",
                            color = Color.Red,
                            fontSize = tokens.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { financeViewModel.fetchExpenses() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                            shape = RoundedCornerShape(tokens.cardCornerRadius / 2)
                        ) {
                            Text("Retry", color = whiteBg, fontSize = tokens.bodyMedium)
                        }
                    }
                }
            }

            filtered.isEmpty() -> {
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
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        items(filtered) { expense ->
                            val status = expense.status.ifBlank { "Paid" }
                            val (statusFg, statusBg) = expenseStatusColors(status)
                            DataCard(
                                item = expense,
                                dateText = formatExpenseDate(expense.expenseDate),
                                topBadgeText = status,
                                topBadgeTextColor = statusFg,
                                topBadgeBgColor = statusBg,
                                title = expense.accountId.accountName.ifBlank { "Expense" },
                                subtitle = "Paid via ${expense.paymentAccountId.accountName.ifBlank { "Account" }}",
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

@Composable
fun ExpenseDetailScreen(
    expense: ExpenseItem,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val status = expense.status.ifBlank { "Paid" }
    val (statusFg, statusBg) = expenseStatusColors(status)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(whiteBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Expense Details", fontSize = tokens.h1, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier
                    .size(tokens.iconSize)
                    .clickable { onClose() }
            )
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            ExpenseDetailField(
                label = "Expense Number",
                value = expense.expenseNumber.ifBlank { "—" }
            )
            ExpenseDetailField(
                label = "Expense Date",
                value = formatExpenseDate(expense.expenseDate)
            )
            ExpenseDetailField(
                label = "Expense Account",
                value = expense.accountId.accountName.ifBlank { "—" }
            )
            ExpenseDetailField(
                label = "Payment Mode",
                value = expense.paymentAccountId.accountName.ifBlank { "—" }
            )
            ExpenseDetailField(
                label = "Amount",
                value = "₹${formatAmount(expense.amount)}"
            )

            if (!expense.referenceNumber.isNullOrBlank()) {
                ExpenseDetailField(
                    label = "Reference Number",
                    value = expense.referenceNumber
                )
            }

            if (!expense.notes.isNullOrBlank()) {
                ExpenseDetailField(
                    label = "Notes",
                    value = expense.notes
                )
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
        "paid" -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
        "pending" -> Color(0xFFCA8A04) to Color(0xFFFEF3C7)
        else -> Color(0xFF6B7280) to Color(0xFFF3F4F6)
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

@Composable
fun AddExpenseScreen(
    financeViewModel: FinanceViewModel,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val chartOfAccounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle()
    val isLoadingAccounts by financeViewModel.isLoadingChartOfAccounts.collectAsStateWithLifecycle()
    val createExpenseState by financeViewModel.createExpenseState.collectAsStateWithLifecycle()
    val createAccountState by financeViewModel.createAccountState.collectAsStateWithLifecycle()

    val categoryAccounts = remember(chartOfAccounts) {
        chartOfAccounts.filter { it.accountType == "Expense" && !it.isGroup }
    }
    val paymentAccounts = remember(chartOfAccounts) {
        chartOfAccounts.filter { it.accountType == "Asset" && it.allowManualEntry }
    }

    var expenseDate by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
    }

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

    var referenceNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isSaving = createExpenseState is CreateExpenseState.Loading
    val isSavingAccount = createAccountState is com.cuso.mobile.viewmodel.CreateAccountState.Loading

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var errorField by remember { mutableStateOf<String?>(null) }

    var showPlanLimitDialog by remember { mutableStateOf(false) }
    val currentPlanName = "starter"
    val isUploadRestricted = currentPlanName.equals("starter", ignoreCase = true) ||
            currentPlanName.equals("light", ignoreCase = true)

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

    LaunchedEffect(createExpenseState) {
        when (val state = createExpenseState) {
            is CreateExpenseState.Success -> {
                financeViewModel.resetCreateExpenseState()
                onSaved()
            }
            is CreateExpenseState.Error -> {
                errorMessage = state.message
                showError = true
            }
            else -> Unit
        }
    }

    LaunchedEffect(createAccountState) {
        when (createAccountState) {
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
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar("Add Expense", onClose = onClose)
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
            ) {
                Text(
                    "Expense Details",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(14.dp))

                FormLabel("Expense Date", isRequired = true)
                ErrorFieldWrapper(
                    isError = errorField == "expenseDate",
                    errorMessage = if (errorField == "expenseDate") "Expense date is required" else null
                ) {
                    DatePickerField(
                        value = expenseDate,
                        onDateSelected = {
                            expenseDate = it
                            errorField = null
                        }
                    )
                }
                Spacer(Modifier.height(14.dp))

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
                                errorField = null
                            },
                            enabled = !isLoadingAccounts,
                            isRequired = true,
                            isError = errorField == "expenseAccount",
                            errorMessage = if (errorField == "expenseAccount") "Expense account is required" else null
                        )
                    }
                    Box(
                        Modifier.background(Primary, RoundedCornerShape(5.dp))
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Expense Account",
                            tint = whiteBg,
                            modifier = Modifier
                                .padding(5.dp)
                                .size(tokens.iconSize)
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
                        errorField = null
                    },
                    enabled = !isLoadingBranches,
                    isRequired = true,
                    isError = errorField == "branch",
                    errorMessage = if (errorField == "branch") "Branch is required" else null
                )
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Amount", isRequired = true)
                        FormTextField(
                            value = amount,
                            onValueChange = { input ->
                                val filtered = input.filterIndexed { index, c ->
                                    c.isDigit() || (c == '.' && input.indexOf('.') == index)
                                }
                                amount = filtered
                                errorField = null
                            },
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                            placeholder = "Enter Amount",
                            isError = errorField == "amount",
                            errorMessage = if (errorField == "amount") "Amount is required" else null
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
                                errorField = null
                            },
                            enabled = !isLoadingAccounts,
                            isRequired = true,
                            isError = errorField == "paymentMode",
                            errorMessage = if (errorField == "paymentMode") "Payment mode is required" else null
                        )
                    }
                }
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
                        .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius / 2))
                        .border(1.dp, ExpenseBorder, RoundedCornerShape(tokens.cardCornerRadius / 2))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = tokens.bodyMedium, color = Color(0xFF374151)),
                        decorationBox = { inner ->
                            if (notes.isEmpty()) Text("Add Notes", fontSize = tokens.bodyMedium, color = Color(0xFF9CA3AF))
                            inner()
                        }
                    )
                }
                Spacer(Modifier.height(20.dp))

                Text(
                    "Documentation & Receipts",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(10.dp))

                if (selectedDocumentName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF7F7FE), RoundedCornerShape(tokens.cardCornerRadius / 1.8f))
                            .border(1.dp, Color(0xFFD6D3FB), RoundedCornerShape(tokens.cardCornerRadius / 1.8f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = ExpensePrimary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                selectedDocumentName.orEmpty(),
                                fontSize = tokens.bodySmall,
                                color = Color(0xFF374151),
                                maxLines = 1
                            )
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier
                                .size(tokens.iconSize)
                                .clickable {
                                    selectedDocumentUri = null
                                    selectedDocumentName = null
                                }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFFF7F7FE), RoundedCornerShape(tokens.cardCornerRadius / 1.8f))
                            .border(1.dp, Color(0xFFD6D3FB), RoundedCornerShape(tokens.cardCornerRadius / 1.8f))
                            .clickable {
                                if (isUploadRestricted) {
                                    showPlanLimitDialog = true
                                } else {
                                    filePickerLauncher.launch("*/*")
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = ExpensePrimary,
                            modifier = Modifier.size(tokens.iconSize * 1.4f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Drag and drop files here", fontSize = tokens.caption, color = Color(0xFF9CA3AF))
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            showBackArrow = false,
            backLabel = "Cancel",
            backEnabled = !isSaving,
            trailingAction = TrailingFabAction.Update(
                isLoading = isSaving,
                label = "Save",
                onClick = {
                    val fields = listOf(
                        ValidationField("expenseDate", expenseDate, "Expense date is required"),
                        ValidationField(
                            "expenseAccount",
                            selectedCategory?.accountName.orEmpty(),
                            "Expense account is required"
                        ),
                        ValidationField("branch", selectedBranch?.name.orEmpty(), "Branch is required"),
                        ValidationField("amount", amount, "Amount is required"),
                        ValidationField("paymentMode", selectedPaymentAccount?.accountName.orEmpty(), "Payment mode is required")
                    )

                    val result = FieldValidator.validate(fields)
                    if (result != null) {
                        errorField = result.fieldKey
                        errorMessage = result.message
                        showError = true
                        return@Update
                    }
                    errorField = null

                    val category = selectedCategory ?: return@Update
                    val paymentAccount = selectedPaymentAccount ?: return@Update
                    val branchItem = selectedBranch ?: return@Update

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
            )
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = if (showError) errorMessage else null,
            onDismiss = { showError = false }
        )

        if (showPlanLimitDialog) {
            PlanLimitDialog(
                title = "Feature restricted",
                message = "You're on the ${currentPlanName.replaceFirstChar { it.uppercase() }} plan and can't upload documents or receipts. Upgrade your plan to unlock this feature.",
                onDismiss = { showPlanLimitDialog = false },
                onUpgrade = { showPlanLimitDialog = false }
            )
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
                        OutlinedButton(
                            onClick = {
                                showAddAccountDialog = false
                                newAccountName = ""
                            },
                            enabled = !isSavingAccount,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(tokens.cardCornerRadius / 1.8f)
                        ) {
                            Text("Cancel", color = Color(0xFF374151), fontSize = tokens.bodyMedium)
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
                            colors = ButtonDefaults.buttonColors(containerColor = ExpensePrimary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius / 1.8f)
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