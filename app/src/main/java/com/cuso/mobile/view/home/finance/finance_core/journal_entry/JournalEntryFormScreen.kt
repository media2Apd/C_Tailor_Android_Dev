@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)

package com.cuso.mobile.view.home.finance.finance_core.journal_entry

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.finance.JournalEntryItem
import com.cuso.mobile.model.finance.JournalEntryLineRequest
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.AppErrorState
import com.cuso.mobile.view.composable.BackFabButton
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ImageUploadSection
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.PlanLimitDialog
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.TrailingFabButton
import com.cuso.mobile.view.home.toIsoDate
import com.cuso.mobile.viewmodel.BranchUiState
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.CreateJournalState
import com.cuso.mobile.viewmodel.FinanceViewModel
import com.cuso.mobile.viewmodel.UpdateJournalState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private val BorderGray = Color(0xFFE8E8ED)
private val TextSecondary = Color(0xFF9A9AA8)
private val RedText = Color(0xFFDC2626)
private val RedBg = Color(0xFFFDECEC)
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
    mode: String = "create",
    entryId: String? = null,
    existingEntry: JournalEntryItem? = null,
    onClose: () -> Unit = {},
    onSaved: () -> Unit = {},
    financeViewModel: FinanceViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val isReadOnly = mode == "view"
    val isPrefillMode = mode == "view" || mode == "edit"

    // Collect Journal Entry Details State
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

    // ── Fetch and Observe Accounts Dropdown with "journal_entry" Context ──
    val dropdownAccounts by financeViewModel.accountDropdownList.collectAsStateWithLifecycle()
    val isLoadingDropdown by financeViewModel.isLoadingAccountDropdown.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        financeViewModel.fetchChartOfAccountsDropdown(context = "journal_entry")
    }

    val accountOptions = remember(dropdownAccounts) {
        dropdownAccounts.map { it.displayName }
    }

    // Collect Branches State
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branches = remember(branchState) {
        (branchState as? BranchUiState.Success)?.branches ?: emptyList()
    }
    val branchOptions = remember(branches) {
        branches.map { it.name?.ifBlank { "Select Branch" } ?: it.branchId }
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

    var date by remember {
        mutableStateOf(
            if (mode == "create")
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            else ""
        )
    }

    var lines by remember {
        mutableStateOf(
            listOf(
                JournalLineDraft(id = "line_1"),
                JournalLineDraft(id = "line_2")
            )
        )
    }

    var notes by remember { mutableStateOf("") }
    var journalRef by remember { mutableStateOf("") }
    var journalType by remember { mutableStateOf("") }

    var uploadedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pendingActionType by remember { mutableStateOf<String?>(null) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uploadedFiles = uploadedFiles + uris
        }
    }

    var showPlanLimitDialog by remember { mutableStateOf(false) }
    val currentPlanName = "starter"
    val isUploadRestricted = currentPlanName.equals("starter", ignoreCase = true) ||
            currentPlanName.equals("light", ignoreCase = true)

    // Calculate totals and validation
    val totalDebit = remember(lines) { lines.sumOf { it.debit.toDoubleOrNull() ?: 0.0 } }
    val totalCredit = remember(lines) { lines.sumOf { it.credit.toDoubleOrNull() ?: 0.0 } }
    val isBalanced = remember(totalDebit, totalCredit) {
        abs(totalDebit - totalCredit) < 0.001
    }
    val allLinesHaveAccount = remember(lines) {
        lines.isNotEmpty() && lines.all { it.account.isNotBlank() }
    }
    val canPost = remember(totalDebit, totalCredit, isBalanced, allLinesHaveAccount) {
        isBalanced && (totalDebit > 0.0 || totalCredit > 0.0) && allLinesHaveAccount
    }

    val createJournalState by financeViewModel.createJournalState.collectAsStateWithLifecycle()
    val updateJournalState by financeViewModel.updateJournalState.collectAsStateWithLifecycle()

    val isApiLoading = createJournalState is CreateJournalState.Loading ||
            updateJournalState is UpdateJournalState.Loading

    // Handle Create Journal Response State
    LaunchedEffect(createJournalState) {
        when (createJournalState) {
            is CreateJournalState.Success -> {
                pendingActionType = null
                financeViewModel.resetCreateJournalState()
                onSaved()
            }
            is CreateJournalState.Error -> {
                pendingActionType = null
            }
            else -> Unit
        }
    }

    // Handle Update Journal Response State
    LaunchedEffect(updateJournalState) {
        when (updateJournalState) {
            is UpdateJournalState.Success -> {
                pendingActionType = null
                financeViewModel.resetUpdateJournalState()
                onSaved()
            }
            is UpdateJournalState.Error -> {
                pendingActionType = null
            }
            else -> Unit
        }
    }

    // Load initial branches
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
    }

    // Default branch assignment
    LaunchedEffect(branches) {
        if (branch?.isBlank() == true && branches.isNotEmpty()) {
            val mainBranch = branches.firstOrNull { it.isMainBranch } ?: branches.firstOrNull()
            branch = mainBranch?.name ?: ""
            selectedBranchId = mainBranch?.id ?: ""
        }
    }

    // Prefill form when in View or Edit mode
    LaunchedEffect(journalDetail, branches, dropdownAccounts) {
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

        val mappedLines = entry.lines.map { line ->
            val matchedAccountName = dropdownAccounts.find { it.id == line.accountId.id }?.displayName
                ?: "${line.accountId.accountCode} - ${line.accountId.accountName}"

            JournalLineDraft(
                id = line.id,
                account = matchedAccountName,
                description = line.description.orEmpty(),
                debit = if (line.debit > 0) line.debit.toString() else "",
                credit = if (line.credit > 0) line.credit.toString() else ""
            )
        }

        lines = if (mappedLines.size < 2) {
            mappedLines + List(2 - mappedLines.size) { idx ->
                JournalLineDraft(id = "line_${mappedLines.size + idx + 1}")
            }
        } else {
            mappedLines
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Title Bar
        Row(modifier = Modifier.fillMaxWidth()) {
            TitleBar(
                title = when (mode) {
                    "view" -> "View Journal Entry"
                    "edit" -> "Edit Journal Entry"
                    else -> "Journal Entry Form"
                },
                onClose = onClose
            )
        }
        HorizontalDivider(color = BorderGray)

        if (isPrefillMode && isLoadingDetail) {
            ListSkeleton()
        }
        if (isPrefillMode && journalDetailError != null) {
            AppErrorState(
                title = "Failed to load Journal entry form",
                message = "Something went wrong. Please check your connection and try again.",
                onRetry = {  }
            )
        }

        val readyToShowForm = !isPrefillMode || (journalDetail != null && !isLoadingDetail)

        if (readyToShowForm) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .background(Color.Transparent)
            ) {
                // Section: Expense Details
                AccordionSection(
                    title = "Expense Details",
                    expanded = expenseDetailsExpanded,
                    onHeaderClick = {
                        expenseDetailsExpanded = !expenseDetailsExpanded
                    }
                ) {
                    FormLabel("Journal No")
                    FormTextField(
                        value = journalNo,
                        onValueChange = { journalNo = it },
                        placeholder = "Auto-generated on save",
                        enabled = false
                    )
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Financial Year")
                    FormTextField(
                        value = financialYear,
                        onValueChange = { financialYear = it },
                        placeholder = "Enter Financial Year",
                        enabled = !isReadOnly
                    )
                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Branch",
                        value = branch?.ifBlank { "Select an option" } ?: "",
                        expanded = branchExpanded,
                        onExpandChange = { if (!isReadOnly) branchExpanded = it },
                        options = branchOptions as List<String>,
                        onOptionSelected = { selected ->
                            branch = selected
                            selectedBranchId =
                                branches.firstOrNull { (it.name?.ifBlank { it.branchId } ?: it.branchId) == selected }?.id
                                    ?: ""
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

                        else -> Unit
                    }
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Date")
                    DatePickerField(
                        value = date,
                        enabled = !isReadOnly,
                        onDateSelected = { if (!isReadOnly) date = it }
                    )
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Ref.")
                    FormTextField(
                        value = journalRef,
                        onValueChange = { journalRef = it },
                        placeholder = "Enter Journal Ref.",
                        enabled = !isReadOnly
                    )
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Journal Type")
                    FormTextField(
                        value = journalType,
                        onValueChange = { journalType = it },
                        placeholder = "Enter Journal Type",
                        enabled = !isReadOnly
                    )
                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider(color = BorderGray)
                    Spacer(Modifier.height(16.dp))

                    // Line Items
                    lines.forEachIndexed { index, line ->
                        JournalLineRow(
                            line = line,
                            accountOptions = accountOptions,
                            isReadOnly = isReadOnly,
                            onLineChange = { updated -> lines = lines.toMutableList().also { it[index] = updated } },
                            onRemove = if (!isReadOnly && lines.size > 2) {
                                { lines = lines.toMutableList().also { it.removeAt(index) } }
                            } else null
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    if (!isReadOnly) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { lines = lines + JournalLineDraft(id = "line_${lines.size + 1}") },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Add Entry",
                                tint = Color(0xFF4B5563),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Add Entry",
                                color = Color(0xFF374151),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // Section: Notes
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    FormLabel("Notes")
                    Spacer(Modifier.height(8.dp))
                    FormTextArea(
                        value = notes,
                        enabled = !isReadOnly,
                        onValueChange = { if (!isReadOnly) notes = it },
                        placeholder = "Note the scope or purpose of this journal entry...",
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider(color = BorderGray)

                // Section: Documentation & Receipts
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    FormLabel("Documentation & Receipts")
                    Spacer(Modifier.height(8.dp))

                    ImageUploadSection(
                        isImage = false,
                        selectedImages = uploadedFiles,
                        browseText = if (isReadOnly) "No files attached" else "Browse Files",
                        onBrowseClick = {
                            if (!isReadOnly) {
                                if (isUploadRestricted) {
                                    showPlanLimitDialog = true
                                } else {
                                    documentPickerLauncher.launch(arrayOf("*/*"))
                                }
                            }
                        },
                        onCameraClick = null,
                        onRemoveImage = { removedFile ->
                            if (!isReadOnly) {
                                uploadedFiles = uploadedFiles.filter { it != removedFile }
                            }
                        },
                        previewHeaderTitle = "ATTACHED FILES"
                    )
                }
                HorizontalDivider(color = BorderGray)

                // Section: Balance Summary Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text("Balance Summary", fontSize = 15.sp, color = title_color)
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
                            "₹${"%.2f".format(abs(totalDebit - totalCredit))}",
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
                    } else if (!allLinesHaveAccount) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RedBg)
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "Select an account for every line before posting",
                                color = RedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }

        HorizontalDivider(color = BorderGray)
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 25.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackFabButton(
                    showArrow = false,
                    onClick = onClose,
                    label = "Cancel"
                )
                Spacer(Modifier.weight(1f))
                if (!isReadOnly) {

                    // Save as Draft Button
                    val isDraftLoading = isApiLoading && pendingActionType == "draft"
                    TrailingFabButton(
                        action = TrailingFabAction.Update(
                            isLoading = isDraftLoading,
                            enabled = !isApiLoading,
                            label = "Save as Draft",
                            onClick = {
                                pendingActionType = "draft"
                                val lineRequests = lines.mapNotNull { line ->
                                    val matchedAccount = dropdownAccounts.find { it.displayName == line.account }
                                    val accountId = matchedAccount?.id ?: return@mapNotNull null
                                    JournalEntryLineRequest(
                                        accountId = accountId,
                                        debit = line.debit.toDoubleOrNull() ?: 0.0,
                                        credit = line.credit.toDoubleOrNull() ?: 0.0,
                                        description = line.description.ifBlank { null }
                                    )
                                }
                                if (mode == "edit" && entryId != null) {
                                    financeViewModel.updateJournal(
                                        id = entryId,
                                        branchId = selectedBranchId,
                                        entryDate = date.toIsoDate(),
                                        reference = journalRef.ifBlank { null },
                                        notes = notes.ifBlank { null },
                                        status = "Draft",
                                        lines = lineRequests
                                    )
                                } else {
                                    financeViewModel.createJournal(
                                        branchId = selectedBranchId,
                                        entryDate = date.toIsoDate(),
                                        reference = journalRef.ifBlank { null },
                                        notes = notes.ifBlank { null },
                                        status = "Draft",
                                        lines = lineRequests
                                    )
                                }
                            }
                        )
                    )

                    Spacer(Modifier.weight(1f))

                    // Post or Update Button
                    val isPostLoading = isApiLoading && pendingActionType == "post"
                    TrailingFabButton(
                        action = TrailingFabAction.Update(
                            isLoading = isPostLoading,
                            enabled = canPost && !isApiLoading,
                            label = if (mode == "edit") "Update" else "Post",
                            onClick = {
                                if (canPost) {
                                    pendingActionType = "post"
                                    val lineRequests = lines.mapNotNull { line ->
                                        val matchedAccount = dropdownAccounts.find { it.displayName == line.account }
                                        val accountId = matchedAccount?.id ?: return@mapNotNull null
                                        JournalEntryLineRequest(
                                            accountId = accountId,
                                            debit = line.debit.toDoubleOrNull() ?: 0.0,
                                            credit = line.credit.toDoubleOrNull() ?: 0.0,
                                            description = line.description.ifBlank { null }
                                        )
                                    }
                                    if (mode == "edit" && entryId != null) {
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
            }
            if (updateJournalState is UpdateJournalState.Error) {
                Text(
                    (updateJournalState as UpdateJournalState.Error).message,
                    color = RedText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }

    if (showPlanLimitDialog) {
        PlanLimitDialog(
            title = "Feature restricted",
            message = "You're on the ${currentPlanName.replaceFirstChar { it.uppercase() }} plan and can't upload documents or receipts. Upgrade your plan to unlock this feature.",
            onDismiss = { showPlanLimitDialog = false },
            onUpgrade = { showPlanLimitDialog = false }
        )
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

    Column(modifier = Modifier.fillMaxWidth()) {
        if (onRemove != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove line",
                    tint = RedText,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onRemove() }
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
                enabled = !isReadOnly,
                maxLines = 1
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
                onValueChange = {
                    onLineChange(line.copy(debit = it, credit = if (it.isNotBlank()) "" else line.credit))
                },
                placeholder = "0.00",
                keyboardType = KeyboardType.Number,
                enabled = !isReadOnly
            )
        }
        Spacer(Modifier.height(10.dp))

        LineFieldRow(label = "Credit") {
            FormTextField(
                value = line.credit,
                onValueChange = {
                    onLineChange(line.copy(credit = it, debit = if (it.isNotBlank()) "" else line.debit))
                },
                placeholder = "0.00",
                keyboardType = KeyboardType.Number,
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
        val datePart = iso.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else iso
    } catch (_: Exception) {
        iso
    }
}