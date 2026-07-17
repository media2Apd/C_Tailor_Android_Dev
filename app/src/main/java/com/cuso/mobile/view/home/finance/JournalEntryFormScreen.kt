package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
    mode: String = "create",   // "create" | "view" | "edit"
    entryId: String? = null,
    existingEntry: com.cuso.mobile.model.finance.JournalEntryItem? = null,
    onClose: () -> Unit = {},
    onSaved: () -> Unit = {},
    financeViewModel: FinanceViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val isReadOnly = mode == "view"

    val isPrefillMode = mode == "view" || mode == "edit"

    val journalDetail by financeViewModel.journalEntryDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by financeViewModel.isLoadingJournalDetail.collectAsStateWithLifecycle()
    val journalDetailError by financeViewModel.journalDetailError.collectAsStateWithLifecycle()

    LaunchedEffect(entryId) {
        if (isPrefillMode && entryId != null) {
            financeViewModel.fetchJournalEntryDetail(entryId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { financeViewModel.clearJournalEntryDetail() }
    }

    val accounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { financeViewModel.fetchChartOfAccounts() }
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

    var branch: String? by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf("") }
    var branchExpanded by remember { mutableStateOf(false) }

    var currency by remember { mutableStateOf("") }

    var date by remember { mutableStateOf("") }

    var company by remember { mutableStateOf("Select an option") }
    var companyExpanded by remember { mutableStateOf(false) }

    var department by remember { mutableStateOf("Select an option") }
    var departmentExpanded by remember { mutableStateOf(false) }

    var journalType by remember { mutableStateOf("Select an option") }
    var journalTypeExpanded by remember { mutableStateOf(false) }

    var lines by remember {
        mutableStateOf(listOf(JournalLineDraft(id = "line_1")))
    }

    var notes by remember { mutableStateOf("") }

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

    // ✅ NEW — mirrors createJournalState handling above, but for edit/update
    val updateJournalState by financeViewModel.updateJournalState.collectAsStateWithLifecycle()
    LaunchedEffect(updateJournalState) {
        if (updateJournalState is com.cuso.mobile.viewmodel.UpdateJournalState.Success) {
            financeViewModel.resetUpdateJournalState()
            onSaved()
        }
    }

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        financeViewModel.fetchChartOfAccounts()
        // TODO — call financeViewModel.fetchNextJournalNo() when that API exists
        // TODO — call financeViewModel.fetchActiveFinancialYear() when that API exists
    }

    LaunchedEffect(branches) {
        if (branch?.isBlank() == true && branches.isNotEmpty()) {
            val mainBranch = branches.firstOrNull { it.isMainBranch } ?: branches.firstOrNull()
            branch = mainBranch?.name ?: ""
            selectedBranchId = mainBranch?.id ?: ""
        }
    }

    // waits for both the full detail AND the branch list, so branch
    // name/id can be matched correctly (detail only has branchId, not the name).
    LaunchedEffect(journalDetail, branches) {
        val entry = journalDetail ?: return@LaunchedEffect
        journalNo = entry.entryNumber
        journalRef = entry.reference.orEmpty()
        notes = entry.notes.orEmpty()
        date = formatIsoToDDMMYYYY(entry.entryDate)

        val matchedBranch = branches.firstOrNull { it.id == entry.branchId }
        if (matchedBranch != null) {
            branch = matchedBranch.name ?: matchedBranch.branchId
            selectedBranchId = matchedBranch.id
        }

        lines = entry.lines.map { line ->
            JournalLineDraft(
                id = line.id,
                account = "${line.accountId.accountCode} - ${line.accountId.accountName}",
                description = line.description.orEmpty(),
                debit = if (line.debit > 0) line.debit.toString() else "",
                credit = if (line.credit > 0) line.credit.toString() else ""
            )
        }.ifEmpty { listOf(JournalLineDraft(id = "line_1")) }
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
            Text(
                when (mode) {
                    "view" -> "View Journal Entry"
                    "edit" -> "Edit Journal Entry"
                    else -> "Journal Entry Form"
                },
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)
            )
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier.size(22.dp).clickable { onClose() }
            )
        }
        HorizontalDivider(color = BorderGray)

        if (isPrefillMode && isLoadingDetail) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        }
        if (isPrefillMode && journalDetailError != null) {
            Text(
                journalDetailError ?: "Failed to load journal entry",
                color = RedText,
                fontSize = 13.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        val readyToShowForm = !isPrefillMode || (journalDetail != null && !isLoadingDetail)

        // ✅ FIX — SCROLL BUG.
        // Before: this Column(weight(1f).verticalScroll(...)) was closed right after
        // just the "Expense Details" header Row, so the scroll modifier only ever
        // wrapped that single collapsible header. All the real content below it —
        // the field block, journal lines, Notes, Documentation & Receipts, and the
        // Balance Summary — lived in their own separate Column()s *outside* this
        // scrollable container, so none of that content could scroll at all.
        //
        // After: everything that should scroll together now lives inside this one
        // Column. Only the top title bar and the bottom Cancel/Post footer stay
        // outside, so they remain fixed while the body scrolls.
        if (readyToShowForm) {
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
                        .clickable(enabled = !isReadOnly) {
                            expenseDetailsExpanded = !expenseDetailsExpanded
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Expense Details",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    )
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
                            placeholder = "Auto-generated on save",
                            enabled = false
                        )
                        Spacer(Modifier.height(14.dp))

                        Spacer(Modifier.height(14.dp))

                        FormDropdown(
                            label = "Branch",
                            value = branch?.ifBlank { "Select an option" } ?: "",
                            expanded = branchExpanded,
                            onExpandChange = { if (!isReadOnly) branchExpanded = it },
                            options = branchOptions as List<String>,
                            onOptionSelected = { selected ->
                                branch = selected
                                selectedBranchId = branches.firstOrNull { it.name?.ifBlank { it.branchId } == selected }?.id ?: ""
                            }
                        )

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

                        Spacer(Modifier.height(14.dp))

                        FormLabel("End Date")
                        // ✅ FIX — DATE NOT RESPECTING VIEW MODE.
                        // Before: only the onDateSelected callback was guarded with
                        // `if (!isReadOnly)`, but the field itself was never told it
                        // was disabled, so tapping it in View mode still opened the
                        // calendar picker dialog (it just silently ignored the pick
                        // afterwards — confusing since the field still looked editable).
                        // After: pass `enabled = !isReadOnly` straight into DatePickerField
                        // so the field itself is visually + functionally disabled in
                        // View mode and the picker can't even be opened.
                        //
                        // NOTE: This assumes DatePickerField exposes an `enabled: Boolean`
                        // parameter (same pattern as FormTextField/FormDropdown below).
                        // If your DatePickerField composable doesn't have that param yet,
                        // share it and I'll add it — the fix is to gate the onClick that
                        // opens the dialog behind `enabled`.
                        DatePickerField(
                            value = date,
                            enabled = !isReadOnly,   // ✅ NEW
                            onDateSelected = { if (!isReadOnly) date = it }
                        )
                        Spacer(Modifier.height(14.dp))

                        FormLabel("Ref")
                        FormTextField(
                            value = journalRef,
                            onValueChange = { journalRef = it },
                            placeholder = "Ref",
                            enabled = !isReadOnly
                        )
                        Spacer(Modifier.height(14.dp))

                        Spacer(Modifier.height(14.dp))

                        lines.forEachIndexed { index, line ->
                            JournalLineRow(
                                line = line,
                                accountOptions = accountOptions,
                                isReadOnly = isReadOnly,
                                onLineChange = { updated -> lines = lines.toMutableList().also { it[index] = updated } },
                                onRemove = if (!isReadOnly && lines.size > 1) { { lines = lines.toMutableList().also { it.removeAt(index) } } } else null
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        if (!isReadOnly) {   // hide "Add Entry" entirely in view mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { lines = lines + JournalLineDraft(id = "line_${lines.size + 1}") },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add Entry", color = BluePrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
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
                        BasicTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            readOnly = isReadOnly,
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

                // small bottom padding so the last card isn't flush against the footer divider
                Spacer(Modifier.height(12.dp))
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
            if (!isReadOnly) {   // hide Post/Update button entirely in view mode
                TrailingFabButton(
                    action = TrailingFabAction.Update(
                        // ✅ CHANGED — loading now reflects whichever operation is in flight
                        isLoading = createJournalState is CreateJournalState.Loading ||
                                updateJournalState is com.cuso.mobile.viewmodel.UpdateJournalState.Loading,
                        label = if (mode == "edit") "Update Journal" else "Post Journal",
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
                                if (mode == "edit" && entryId != null) {
                                    // ✅ CHANGED — was a TODO, now wired to the real update API
                                    financeViewModel.updateJournal(
                                        id = entryId,
                                        branchId = selectedBranchId,
                                        entryDate = date.toIsoDate(),
                                        reference = journalRef.ifBlank { null },
                                        notes = notes.ifBlank { null },
                                        status = "Posted",
                                        lines = lineRequests
                                    )
                                } else {
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
                        }
                    )
                )
            }

            if (createJournalState is CreateJournalState.Error) {
                Text(
                    (createJournalState as CreateJournalState.Error).message,
                    color = RedText,
                    fontSize = 12.sp
                )
            }
            // ✅ NEW — surfaces update-specific errors the same way create errors are shown
            if (updateJournalState is com.cuso.mobile.viewmodel.UpdateJournalState.Error) {
                Text(
                    (updateJournalState as com.cuso.mobile.viewmodel.UpdateJournalState.Error).message,
                    color = RedText,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun JournalLineRow(
    line: JournalLineDraft,
    accountOptions: List<String>,
    isReadOnly: Boolean = false,
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

        LineFieldRow(label = "Account") {
            FormDropdown(
                label = null,
                value = line.account.ifBlank { "Select an option" },
                expanded = accountExpanded,
                onExpandChange = { if (!isReadOnly) accountExpanded = it },
                options = accountOptions,
                onOptionSelected = { onLineChange(line.copy(account = it)) },
                enabled = !isReadOnly
            )
        }
        Spacer(Modifier.height(10.dp))

        LineFieldRow(label = "Description") {
            FormTextField(
                value = line.description,
                onValueChange = { onLineChange(line.copy(description = it)) },
                placeholder = "Description",
                enabled = !isReadOnly
            )
        }
        Spacer(Modifier.height(10.dp))

        LineFieldRow(label = "Debit") {
            FormTextField(
                value = line.debit,
                onValueChange = { onLineChange(line.copy(debit = it, credit = if (it.isNotBlank()) "" else line.credit)) },
                placeholder = "0.00",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                enabled = !isReadOnly
            )
        }
        Spacer(Modifier.height(10.dp))

        LineFieldRow(label = "Credit") {
            FormTextField(
                value = line.credit,
                onValueChange = { onLineChange(line.copy(credit = it, debit = if (it.isNotBlank()) "" else line.debit)) },
                placeholder = "0.00",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                enabled = !isReadOnly
            )
        }
        Spacer(Modifier.height(10.dp))
    }
}

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

private fun formatIsoToDDMMYYYY(iso: String): String {
    return try {
        val datePart = iso.take(10) // "2026-07-16"
        val parts = datePart.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else iso
    } catch (_: Exception) {
        iso
    }
}