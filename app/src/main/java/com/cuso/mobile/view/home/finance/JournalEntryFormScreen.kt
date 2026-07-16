package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.home.FormDateField
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.home.reusablecomposables.BackFabButton
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabButton
import com.cuso.mobile.view.home.toIsoDate
import com.cuso.mobile.viewmodel.BranchUiState
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.CreateJournalState
import com.cuso.mobile.viewmodel.FinanceViewModel

private val BorderGray = Color(0xFFE8E8ED)
private val TextSecondary = Color(0xFF9A9AA8)
private val BluePrimary = Color(0xFF3A2FCB)
private val RedText = Color(0xFFDC2626)
private val RedBg = Color(0xFFFDECEC)
private val PanelBg = Color(0xFFF7F7FA)
private val GreenText = Color(0xFF1FA751)

data class JournalLineDraft(
    val id: String,
    var account: String = "",
    var description: String = "",
    var debit: String = "",
    var credit: String = "",
    var ref: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryFormScreen(
    onClose: () -> Unit = {},
    onSaved: () -> Unit = {},
    financeViewModel: FinanceViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {

    val accounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { financeViewModel.fetchChartOfAccounts() }
    // Replace with this null-safe version:
    val accountOptions = remember(accounts) {
        accounts.mapNotNull { account ->
            val code = account.accountCode
            val name = account.accountName
            if (code.isNotBlank() || name.isNotBlank()) {
                "$code - $name"
            } else {
                null
            }
        }
    }
// AFTER (correct — real BranchViewModel uses sealed BranchUiState):
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()

    val branches = remember(branchState) {
        (branchState as? BranchUiState.Success)?.branches ?: emptyList()
    }
    val branchOptions = remember(branches) {
        branches.map { it.name!!.ifBlank { it.branchId } }
    }
    val branchLoadError = remember(branchState) {
        (branchState as? BranchUiState.Error)?.message
    }

    var expenseDetailsExpanded by remember { mutableStateOf(true) }

    var journalNo by remember { mutableStateOf("") }
    var financialYear by remember { mutableStateOf("") }

    // BEFORE: var branch by remember { mutableStateOf("Head Office") }   // hardcoded default
    // AFTER: default blank, filled once branches load
    var branch by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf("") }
    var branchExpanded by remember { mutableStateOf(false) }

    // BEFORE: var currency by remember { mutableStateOf("INR") }  // hardcoded
    // AFTER: still needs a settings/org API to set this; kept as state, no fake fixed value assumed elsewhere
    var currency by remember { mutableStateOf("") }

    var date by remember { mutableStateOf("") }

    // BEFORE: var company by remember { mutableStateOf("Select an option") }, options = listOf("Main Company")
    // AFTER: TODO — wire to real Company/Organization API. Kept placeholder state only.
    var company by remember { mutableStateOf("Select an option") }
    var companyExpanded by remember { mutableStateOf(false) }

    // BEFORE: options = listOf("Accounting", "Sales", "Operations") hardcoded
    // AFTER: TODO — wire to real Departments API. Kept placeholder state only.
    var department by remember { mutableStateOf("Select an option") }
    var departmentExpanded by remember { mutableStateOf(false) }

    // BEFORE: options = listOf("Manual", "Adjustment", "Opening Balance") hardcoded
    // AFTER: TODO — wire to real Journal Type config API. Kept placeholder state only.
    var journalType by remember { mutableStateOf("Select an option") }
    var journalTypeExpanded by remember { mutableStateOf(false) }

    var lines by remember {
        mutableStateOf(listOf(JournalLineDraft(id = "line_1")))
    }

    var notes by remember { mutableStateOf("") }

    // ── State: add a header-level ref (near top with other state vars) ──
    var journalRef by remember { mutableStateOf("") }

    val totalDebit = remember(lines) { lines.sumOf { it.debit.toDoubleOrNull() ?: 0.0 } }
    val totalCredit = remember(lines) { lines.sumOf { it.credit.toDoubleOrNull() ?: 0.0 } }
    val isBalanced = remember(totalDebit, totalCredit) {
        kotlin.math.abs(totalDebit - totalCredit) < 0.001
    }
    val createJournalState by financeViewModel.createJournalState.collectAsStateWithLifecycle()

    LaunchedEffect(createJournalState) {
        if (createJournalState is CreateJournalState.Success) {
            financeViewModel.resetCreateJournalState()
            onSaved()
        }
    }

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        financeViewModel.fetchChartOfAccounts()
        // AFTER: TODO — call financeViewModel.fetchNextJournalNo() when that API exists
        // AFTER: TODO — call financeViewModel.fetchActiveFinancialYear() when that API exists
    }

    // AFTER: once branches arrive, auto-select main branch instead of a hardcoded string
    LaunchedEffect(branches) {
        if (branch.isBlank() && branches.isNotEmpty()) {
            val mainBranch = branches.firstOrNull { it.isMainBranch } ?: branches.firstOrNull()
            branch = mainBranch?.name ?: ""
            selectedBranchId = mainBranch?.id ?: ""
        }
    }

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
            Text("Journal Entry Form", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier.size(22.dp).clickable { onClose() }
            )
        }
        HorizontalDivider(color = BorderGray)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(PanelBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { expenseDetailsExpanded = !expenseDetailsExpanded }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Expense Details", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                Icon(
                    imageVector = if (expenseDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF6B7280)
                )
            }
            HorizontalDivider(color = BorderGray)

            if (expenseDetailsExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    FormLabel("Journal No")
                    FormTextField(
                        value = journalNo,
                        onValueChange = { journalNo = it },
                        // BEFORE: placeholder = "JE-2026-000045"   // fake literal
                        // AFTER: empty until real API sets journalNo; placeholder just hints format
                        placeholder = "Auto-generated on save",
                        enabled = false
                    )
                    Spacer(Modifier.height(14.dp))

//                    FormLabel("Financial year")
//                    FormTextField(
//                        value = financialYear,
//                        onValueChange = { financialYear = it },
//                        // BEFORE: placeholder = "FY2026-27"   // fake literal
//                        // AFTER: empty until real API sets financialYear
//                        placeholder = "Loading...",
//                        enabled = false
//                    )
                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Branch",
                        value = branch.ifBlank { "Select an option" },
                        expanded = branchExpanded,
                        onExpandChange = { branchExpanded = it },
                        options = branchOptions as List<String>,
                        onOptionSelected = { selected ->
                            branch = selected
                            selectedBranchId = branches.firstOrNull { it.name?.ifBlank { it.branchId } == selected }?.id ?: ""
                        }
                    )

// AFTER: show loading/error feedback under the dropdown (optional but recommended)
                    when (branchState) {
                        is BranchUiState.Loading -> {
                            Spacer(Modifier.height(4.dp))
                            Text("Loading branches...", fontSize = 12.sp, color = TextSecondary)
                        }
                        is BranchUiState.Error -> {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                branchLoadError ?: "Failed to load branches",
                                fontSize = 12.sp,
                                color = RedText
                            )
                        }
                        else -> {}
                    }
                    Spacer(Modifier.height(14.dp))

//                    FormLabel("Currency")
//                    FormTextField(
//                        value = currency,
//                        onValueChange = { currency = it },
//                        // TODO: fetch from org/company settings API instead of blank/static
//                        placeholder = "Loading...",
//                        enabled = false
//                    )
                    Spacer(Modifier.height(14.dp))

                    FormLabel("End Date")
                    DatePickerField(
                        value = date,
                        onDateSelected = { date = it }
                    )
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Ref")
                    FormTextField(
                        value = journalRef,
                        onValueChange = { journalRef = it },
                        placeholder = "Ref"
                    )
                    Spacer(Modifier.height(14.dp))

//                    FormDropdown(
//                        label = "Company",
//                        value = company,
//                        expanded = companyExpanded,
//                        onExpandChange = { companyExpanded = it },
//                        // TODO: options should come from Company/Organization list API
//                        options = listOf("Main Company"),
//                        onOptionSelected = { company = it }
//                    )
                    Spacer(Modifier.height(14.dp))

////                    FormDropdown(
////                        label = "Department",
////                        value = department,
////                        expanded = departmentExpanded,
////                        onExpandChange = { departmentExpanded = it },
////                        // TODO: options should come from Departments API
////                        options = listOf("Accounting", "Sales", "Operations"),
////                        onOptionSelected = { department = it }
////                    )
////                    Spacer(Modifier.height(14.dp))
////
////                    FormDropdown(
////                        label = "Journal Type",
////                        value = journalType,
////                        expanded = journalTypeExpanded,
////                        onExpandChange = { journalTypeExpanded = it },
////                        // TODO: options should come from Journal Type config API
////                        options = listOf("Manual", "Adjustment", "Opening Balance"),
////                        onOptionSelected = { journalType = it }
////                    )
//                    Spacer(Modifier.height(18.dp))

                    lines.forEachIndexed { index, line ->
                        JournalLineRow(
                            line = line,
                            accountOptions = accountOptions,
                            onLineChange = { updated ->
                                lines = lines.toMutableList().also { it[index] = updated }
                            },
                            onRemove = if (lines.size > 1) {
                                { lines = lines.toMutableList().also { it.removeAt(index) } }
                            } else null
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                lines = lines + JournalLineDraft(id = "line_${lines.size + 1}")
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Entry", color = BluePrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                HorizontalDivider(color = BorderGray)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                FormLabel("Notes")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(PanelBg, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                        decorationBox = { inner ->
                            if (notes.isEmpty()) {
                                Text("Note the scope or purpose of this price list...", fontSize = 14.sp, color = TextSecondary)
                            }
                            inner()
                        }
                    )
                }
            }
            HorizontalDivider(color = BorderGray)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                FormLabel("Documentation & Receipts")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(PanelBg, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(6.dp))
                        Text("Drag and drop files here", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
            HorizontalDivider(color = BorderGray)

            // BEFORE: entire per-line breakdown block using lines.forEachIndexed { ... }
// AFTER: Total Debit / Total Credit / Difference summary like the image

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text("Balance Summary", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = BorderGray)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Debit", fontSize = 14.sp, color = TextSecondary)
                    Text(
                        "₹${"%.2f".format(totalDebit)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                }
                HorizontalDivider(color = BorderGray)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Credit", fontSize = 14.sp, color = TextSecondary)
                    Text(
                        "₹${"%.2f".format(totalCredit)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                }
                HorizontalDivider(color = BorderGray)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Difference", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Text(
                        "₹${"%.2f".format(kotlin.math.abs(totalDebit - totalCredit))}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isBalanced) GreenText else RedText
                    )
                }

                if (!isBalanced) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RedBg)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "Journal not balance - cannot post",
                            color = RedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = BorderGray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackFabButton(
                onClick = onClose,
                label = "Cancel"
            )
            Spacer(Modifier.width(12.dp))
            TrailingFabButton(
                action = TrailingFabAction.Update(
                    isLoading = createJournalState is CreateJournalState.Loading,
                    label = "Post Journal",
                    onClick = {
                        if (isBalanced) {
                            val lineRequests = lines.mapNotNull { line ->
                                val matchedAccount = accounts.find { "${it.accountCode} - ${it.accountName}" == line.account }
                                val accountId = matchedAccount?._id
                                if (accountId.isNullOrBlank()) return@mapNotNull null
                                com.cuso.mobile.model.finance.JournalEntryLineRequest(
                                    accountId = accountId,
                                    debit = line.debit.toDoubleOrNull() ?: 0.0,
                                    credit = line.credit.toDoubleOrNull() ?: 0.0,
                                    description = line.description.ifBlank { null }
                                )
                            }
                            financeViewModel.createJournal(
                                branchId = selectedBranchId,
                                entryDate = date.toIsoDate(),
                                reference = journalRef.ifBlank { null },
                                notes = notes.ifBlank { null },
                                status = "Posted",
                                lines = lineRequests
                            )
                        }
                    }
                )
            )

            if (createJournalState is CreateJournalState.Error) {
                Text(
                    (createJournalState as CreateJournalState.Error).message,
                    color = RedText,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ── REPLACE the whole JournalLineRow function with this ──
@Composable
private fun JournalLineRow(
    line: JournalLineDraft,
    accountOptions: List<String>,
    onLineChange: (JournalLineDraft) -> Unit,
    onRemove: (() -> Unit)?
) {
    var accountExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBg, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        if (onRemove != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove line",
                    tint = RedText,
                    modifier = Modifier.size(18.dp).clickable { onRemove() }
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        // ── Account: label left, dropdown right ──
        LineFieldRow(label = "Account") {
            FormDropdown(
                label = null,
                value = line.account.ifBlank { "Select an option" },
                expanded = accountExpanded,
                onExpandChange = { accountExpanded = it },
                options = accountOptions,
                onOptionSelected = { onLineChange(line.copy(account = it)) }
            )
        }
        Spacer(Modifier.height(10.dp))

        // ── Description: label left, text field right ──
        LineFieldRow(label = "Description") {
            FormTextField(
                value = line.description,
                onValueChange = { onLineChange(line.copy(description = it)) },
                placeholder = "Description"
            )
        }
        Spacer(Modifier.height(10.dp))

        // ── Debit: label left, text field right ──
        // ── Debit: label left, text field right ──
        LineFieldRow(label = "Debit") {
            FormTextField(
                value = line.debit,
                onValueChange = {
                    // BEFORE: onLineChange(line.copy(debit = it))
                    // AFTER: typing in Debit clears Credit — only one side can be filled
                    onLineChange(line.copy(debit = it, credit = if (it.isNotBlank()) "" else line.credit))
                },
                placeholder = "0.00",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        }
        Spacer(Modifier.height(10.dp))

// ── Credit: label left, text field right ──
        LineFieldRow(label = "Credit") {
            FormTextField(
                value = line.credit,
                onValueChange = {
                    // BEFORE: onLineChange(line.copy(credit = it))
                    // AFTER: typing in Credit clears Debit — only one side can be filled
                    onLineChange(line.copy(credit = it, debit = if (it.isNotBlank()) "" else line.debit))
                },
                placeholder = "0.00",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        }
        Spacer(Modifier.height(10.dp))

        // ── Ref: label left, text field right ──
//        LineFieldRow(label = "Ref") {
//            FormTextField(
//                value = line.ref,
//                onValueChange = { onLineChange(line.copy(ref = it)) },
//                placeholder = "Ref"
//            )
//        }
    }
}

// ── Reusable: label on the left, any input field on the right (fixed width like image) ──
@Composable
private fun LineFieldRow(
    label: String,
    field: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Box(modifier = Modifier.width(180.dp)) {
            field()
        }
    }
}