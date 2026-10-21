@file:Suppress("unused", "SpellCheckingInspection", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.finance.funds.bankstatement

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.ImportBankStatementLineRequest
import com.cuso.tailor.model.finance.ImportBankStatementRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.ImportBankStatementState
import android.os.Environment
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipInputStream

// Unified preview table model
data class StatementPreviewRow(
    val id: String,
    val isManual: Boolean,
    val date: String,
    val description: String,
    val reference: String,
    val isCredit: Boolean,
    val debit: Double,
    val credit: Double,
    val balance: Double?
)

@Composable
fun ImportBankStatementScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Observe bank accounts and submission state from ViewModel
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val importState by viewModel.importStatementState.collectAsState()

    var selectedBankAccountId by remember { mutableStateOf("") }

    // Fetch bank accounts from API when entering screen
    LaunchedEffect(Unit) {
        viewModel.fetchBankAccounts()
    }

    // Automatically pre-select default bank account (or first available) once fetched
    LaunchedEffect(bankAccounts) {
        if (bankAccounts.isNotEmpty() && selectedBankAccountId.isBlank()) {
            val defaultAcc = bankAccounts.find { it.isDefault } ?: bankAccounts.first()
            selectedBankAccountId = defaultAcc.id
        }
    }

    // Opening and Closing Balances
    var openingBalance by remember { mutableStateOf("0") }
    var closingBalance by remember { mutableStateOf("0") }

    // Uploaded File States
    val selectedFiles = remember { mutableStateListOf<Uri>() }
    var uploadedFileName by remember { mutableStateOf<String?>(null) }
    var csvHeaders by remember { mutableStateOf<List<String>>(emptyList()) }
    var rawParsedRows by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    // Column Mapping Dropdown States
    var dateCol by remember { mutableStateOf("") }
    var descCol by remember { mutableStateOf("") }
    var refCol by remember { mutableStateOf("") }
    var withdrawalCol by remember { mutableStateOf("") }
    var depositCol by remember { mutableStateOf("") }
    var balanceCol by remember { mutableStateOf("") }

    // Dropdown Toggles
    var bankAccountDropdownExpanded by remember { mutableStateOf(false) }
    var manualTypeDropdownExpanded by remember { mutableStateOf(false) }
    var dateColExpanded by remember { mutableStateOf(false) }
    var descColExpanded by remember { mutableStateOf(false) }
    var refColExpanded by remember { mutableStateOf(false) }
    var withColExpanded by remember { mutableStateOf(false) }
    var depColExpanded by remember { mutableStateOf(false) }
    var balColExpanded by remember { mutableStateOf(false) }

    // Manual Entry Form Field States
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var manualDate by remember { mutableStateOf(todayDate) }
    var manualDesc by remember { mutableStateOf("") }
    var manualAmount by remember { mutableStateOf("") }
    var manualType by remember { mutableStateOf("Debit (-)") }
    var manualRef by remember { mutableStateOf("") }

    // Preview rows collection
    var previewRows by remember { mutableStateOf<List<StatementPreviewRow>>(emptyList()) }

    // Converts date string to YYYY-MM-DD
    fun formatToStrictDate(raw: String): String {
        val clean = raw.trim()
        if (clean.isBlank()) return todayDate

        if (clean.matches(Regex("""\d{4}-\d{2}-\d{2}.*"""))) return clean.take(10)
        if (clean.matches(Regex("""\d{2}-\d{2}-\d{4}"""))) {
            val parts = clean.split("-")
            return "${parts[2]}-${parts[1]}-${parts[0]}"
        }
        if (clean.matches(Regex("""\d{4}/\d{2}/\d{2}"""))) {
            return clean.replace("/", "-")
        }

        val digits = clean.filter { it.isDigit() }
        return when {
            digits.length >= 8 -> {
                val y = digits.take(4)
                val m = digits.substring(4, 6).toIntOrNull()?.coerceIn(1, 12)?.let { String.format(Locale.US, "%02d", it) } ?: "01"
                val d = digits.substring(6, 8).toIntOrNull()?.coerceIn(1, 28)?.let { String.format(Locale.US, "%02d", it) } ?: "01"
                "$y-$m-$d"
            }
            else -> todayDate
        }
    }

    // Formats date into ISO UTC 00:00:00.000Z
    fun formatToIsoUtc(rawDate: String): String {
        val yyyyMmDd = formatToStrictDate(rawDate)
        return "${yyyyMmDd}T00:00:00.000Z"
    }

    fun formatToDescription(raw: String, defaultName: String): String {
        val clean = raw.trim()
        if (clean.isBlank()) return defaultName
        if (clean.matches(Regex("""\d{4}[-/]\d{2}[-/]\d{2}""")) || clean.matches(Regex("""\d{2}[-/]\d{2}[-/]\d{4}"""))) {
            return clean.filter { it.isDigit() }
        }
        return clean
    }

    fun extractPureNumber(raw: String?): Double {
        if (raw.isNullOrBlank()) return 0.0
        val sanitized = raw.replace(Regex("[^0-9.]"), "")
        return sanitized.toDoubleOrNull() ?: 0.0
    }

    // Parse and map rows
    fun reapplyMapping(
        rows: List<Map<String, String>>,
        dCol: String,
        sCol: String,
        rCol: String,
        wCol: String,
        dpCol: String,
        bCol: String
    ) {
        if (rows.isEmpty()) return

        val parsed = rows.mapIndexedNotNull { index, row ->
            val formattedDate = formatToStrictDate(row[dCol].orEmpty())
            val formattedDesc = formatToDescription(row[sCol].orEmpty(), "Transaction #${index + 1}")
            val formattedRef = row[rCol]?.trim().orEmpty()

            val debitNum = extractPureNumber(row[wCol])
            val creditNum = extractPureNumber(row[dpCol])
            val rawBalStr = row[bCol]?.trim()
            val balanceNum = if (!rawBalStr.isNullOrBlank()) extractPureNumber(rawBalStr) else null

            val isCredit = creditNum > 0

            if (formattedDate.isNotBlank() || formattedDesc.isNotBlank() || debitNum > 0 || creditNum > 0) {
                StatementPreviewRow(
                    id = "file-$index",
                    isManual = false,
                    date = formattedDate,
                    description = formattedDesc,
                    reference = formattedRef,
                    isCredit = isCredit,
                    debit = debitNum,
                    credit = creditNum,
                    balance = balanceNum
                )
            } else null
        }

        previewRows = previewRows.filter { it.isManual } + parsed
    }

    // Native file picker contract
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFiles.clear()
            selectedFiles.add(uri)
            val name = getFileName(context, uri) ?: "statement.xlsx"
            uploadedFileName = name

            try {
                val isExcel = name.endsWith(".xlsx", ignoreCase = true)
                val (headers, rows) = if (isExcel) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        parseXlsxDirectly(stream)
                    } ?: Pair(emptyList(), emptyList())
                } else {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        parseCsvDirectly(stream)
                    } ?: Pair(emptyList(), emptyList())
                }

                if (headers.isNotEmpty() && rows.isNotEmpty()) {
                    csvHeaders = headers
                    rawParsedRows = rows

                    dateCol = headers.firstOrNull { it.contains("date", ignoreCase = true) } ?: headers.getOrElse(0) { "" }
                    descCol = headers.firstOrNull { it.contains("desc", ignoreCase = true) || it.contains("narrat", ignoreCase = true) } ?: headers.getOrElse(1) { "" }
                    refCol = headers.firstOrNull { it.contains("ref", ignoreCase = true) || it.contains("chq", ignoreCase = true) || it.contains("utr", ignoreCase = true) } ?: headers.getOrElse(2) { "" }
                    withdrawalCol = headers.firstOrNull { it.contains("withdr", ignoreCase = true) || it.contains("debit", ignoreCase = true) } ?: headers.getOrElse(3) { "" }
                    depositCol = headers.firstOrNull { it.contains("deposit", ignoreCase = true) || it.contains("credit", ignoreCase = true) } ?: headers.getOrElse(4) { "" }
                    balanceCol = headers.firstOrNull { it.contains("bal", ignoreCase = true) } ?: headers.getOrElse(5) { "" }

                    reapplyMapping(rows, dateCol, descCol, refCol, withdrawalCol, depositCol, balanceCol)
                } else {
                    Toast.makeText(context, "No rows found in file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle submission result
    LaunchedEffect(importState) {
        when (importState) {
            is ImportBankStatementState.Success -> {
                Toast.makeText(context, (importState as ImportBankStatementState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetImportBankStatementState()
                onBack()
            }
            is ImportBankStatementState.Error -> {
                Toast.makeText(context, (importState as ImportBankStatementState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetImportBankStatementState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = "Import Bank Transactions",
                onClose = onBack
            )
        },
        containerColor = Primary_background,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                ) {
                    // Valid items and error count
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(greentext))
                            Spacer(Modifier.width(6.dp))
                            Text("${previewRows.size} items valid", fontSize = tokens.caption, color = TextPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(redText))
                            Spacer(Modifier.width(6.dp))
                            Text("0 errors", fontSize = tokens.caption, color = TextPrimary)
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, BorderGray),
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight)
                        ) {
                            Text("Cancel", fontSize = tokens.bodySmall, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                if (selectedBankAccountId.isBlank()) {
                                    Toast.makeText(context, "Please select a Target Bank Account", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (previewRows.isEmpty()) {
                                    Toast.makeText(context, "Please add transaction lines to queue", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Build lines matching backend ISO date format
                                val payloadLines = previewRows.map { row ->
                                    ImportBankStatementLineRequest(
                                        transactionDate = formatToIsoUtc(row.date),
                                        description = row.description,
                                        referenceNumber = row.reference,
                                        withdrawal = row.debit,
                                        deposit = row.credit,
                                        runningBalance = row.balance
                                    )
                                }

                                viewModel.importBankStatement(
                                    ImportBankStatementRequest(
                                        bankAccountId = selectedBankAccountId,
                                        fileName = uploadedFileName ?: "statement.xlsx",
                                        openingBalance = openingBalance.toDoubleOrNull() ?: 0.0,
                                        closingBalance = closingBalance.toDoubleOrNull() ?: 0.0,
                                        lines = payloadLines
                                    )
                                )
                            },
                            enabled = previewRows.isNotEmpty() && importState !is ImportBankStatementState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BluePrimary,
                                disabledContainerColor = disabled
                            ),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight)
                        ) {
                            if (importState is ImportBankStatementState.Loading) {
                                CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Complete Import", fontSize = tokens.bodySmall, color = whiteBg, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
        ) {
            // Format bank account options from real API data: "bankName - accountDisplayName (accountNumberMasked)"
            val bankOptions = remember(bankAccounts) {
                bankAccounts.map { acc ->
                    "${acc.bankName} - ${acc.accountDisplayName} (${acc.accountNumberMasked})"
                }
            }

            // Current display label resolved from selected ID
            val currentSelectedBankLabel = remember(selectedBankAccountId, bankAccounts) {
                val current = bankAccounts.find { it.id == selectedBankAccountId }
                if (current != null) {
                    "${current.bankName} - ${current.accountDisplayName} (${current.accountNumberMasked})"
                } else {
                    "Select Bank Account"
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, grey_border)
            ) {
                Column(modifier = Modifier.padding(tokens.extraPadding)) {
                    // Target Bank Account FormDropdown populated from fetchBankAccounts
                    FormDropdown(
                        label = "Target Bank Account",
                        isRequired = true,
                        value = currentSelectedBankLabel,
                        expanded = bankAccountDropdownExpanded,
                        onExpandChange = { bankAccountDropdownExpanded = it },
                        options = bankOptions,
                        onOptionSelected = { chosenString ->
                            val foundAcc = bankAccounts.find { acc ->
                                "${acc.bankName} - ${acc.accountDisplayName} (${acc.accountNumberMasked})" == chosenString
                            }
                            if (foundAcc != null) {
                                selectedBankAccountId = foundAcc.id
                            }
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Passbook Opening Balance")
                            FormTextField(
                                value = openingBalance,
                                onValueChange = { openingBalance = it },
                                keyboardType = KeyboardType.Number,
                                placeholder = "0.00"
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Passbook Closing Balance")
                            FormTextField(
                                value = closingBalance,
                                onValueChange = { closingBalance = it },
                                keyboardType = KeyboardType.Number,
                                placeholder = "0.00"
                            )
                        }
                    }
                }
            }

            // ─── SAMPLE TEMPLATE DOWNLOAD BUTTON (NEW) ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.2.dp, Color(0xFFD1D5DB)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            downloadSampleTemplate(context)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Download Icon
                        Icon(
                            painter = painterResource(R.drawable.ic_download),
                            contentDescription = "Sample Template",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Sample Template",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }
            }

            // Image and File Upload Section
            ImageUploadSection(
                selectedImages = selectedFiles.toList(),
                onBrowseClick = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-excel",
                            "text/csv",
                            "text/comma-separated-values"
                        )
                    )
                },
                onRemoveImage = { uri ->
                    selectedFiles.remove(uri)
                    uploadedFileName = null
                    csvHeaders = emptyList()
                    rawParsedRows = emptyList()
                    previewRows = previewRows.filter { it.isManual }
                },
                maxFiles = 1,
                title = "Upload Statement File",
                subtitle = "Upload your Bank Statement CSV or Excel spreadsheet",
                supportedFormatsText = "Supports Excel (.xlsx, .xls) and CSV (.csv) files up to 10MB",
                browseText = "Browse files"
            )

            // Manual Entry Form
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                border = BorderStroke(1.dp, grey_border),
                color = whiteBg
            ) {
                Column(
                    modifier = Modifier.padding(tokens.extraPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Manual Entry", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }

                    // Transaction Date Picker
                    Column {
                        FormLabel(text = "Transaction Date", isRequired = true)
                        DatePickerField(
                            value = manualDate,
                            onDateSelected = { manualDate = it }
                        )
                    }

                    // Description Input
                    Column {
                        FormLabel(text = "Description", isRequired = true)
                        FormTextField(
                            value = manualDesc,
                            onValueChange = { manualDesc = it },
                            placeholder = "e.g. AWS Cloud Platform"
                        )
                    }

                    // Amount & Type Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Amount", isRequired = true)
                            FormTextField(
                                value = manualAmount,
                                onValueChange = { manualAmount = it },
                                placeholder = "0.00",
                                keyboardType = KeyboardType.Number
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Type",
                                value = manualType,
                                expanded = manualTypeDropdownExpanded,
                                onExpandChange = { manualTypeDropdownExpanded = it },
                                options = listOf("Debit (-)", "Credit (+)"),
                                onOptionSelected = { manualType = it }
                            )
                        }
                    }

                    // Reference Number
                    Column {
                        FormLabel(text = "Reference #")
                        FormTextField(
                            value = manualRef,
                            onValueChange = { manualRef = it },
                            placeholder = "UTR / Cheque #"
                        )
                    }

                    // Add to Queue Button
                    Button(
                        onClick = {
                            val amt = manualAmount.toDoubleOrNull() ?: 0.0
                            if (manualDesc.isNotBlank() && amt > 0.0) {
                                val isCredit = manualType.startsWith("Credit")
                                val newManualItem = StatementPreviewRow(
                                    id = "manual-${System.currentTimeMillis()}",
                                    isManual = true,
                                    date = manualDate,
                                    description = manualDesc,
                                    reference = manualRef,
                                    isCredit = isCredit,
                                    debit = if (!isCredit) amt else 0.0,
                                    credit = if (isCredit) amt else 0.0,
                                    balance = null
                                )
                                previewRows = listOf(newManualItem) + previewRows
                                manualDesc = ""
                                manualAmount = ""
                                manualRef = ""
                            } else {
                                Toast.makeText(context, "Please enter Description and Amount", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add to Queue", color = whiteBg, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Data Mapping Configuration Section
            if (csvHeaders.isNotEmpty()) {
                val headerOptions = listOf("— None —") + csvHeaders

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, grey_border)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.extraPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("DATA MAPPING", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)

                        // Mapping Row 1
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "DATE",
                                    value = dateCol.ifBlank { "Select" },
                                    expanded = dateColExpanded,
                                    onExpandChange = { dateColExpanded = it },
                                    options = headerOptions,
                                    onOptionSelected = {
                                        dateCol = if (it == "— None —") "" else it
                                        reapplyMapping(rawParsedRows, dateCol, descCol, refCol, withdrawalCol, depositCol, balanceCol)
                                    }
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "DESCRIPTION",
                                    value = descCol.ifBlank { "Select" },
                                    expanded = descColExpanded,
                                    onExpandChange = { descColExpanded = it },
                                    options = headerOptions,
                                    onOptionSelected = {
                                        descCol = if (it == "— None —") "" else it
                                        reapplyMapping(rawParsedRows, dateCol, descCol, refCol, withdrawalCol, depositCol, balanceCol)
                                    }
                                )
                            }
                        }

                        // Mapping Row 2
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "REFERENCE",
                                    value = refCol.ifBlank { "Select" },
                                    expanded = refColExpanded,
                                    onExpandChange = { refColExpanded = it },
                                    options = headerOptions,
                                    onOptionSelected = {
                                        refCol = if (it == "— None —") "" else it
                                        reapplyMapping(rawParsedRows, dateCol, descCol, refCol, withdrawalCol, depositCol, balanceCol)
                                    }
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "DEBIT (OUT)",
                                    value = withdrawalCol.ifBlank { "Select" },
                                    expanded = withColExpanded,
                                    onExpandChange = { withColExpanded = it },
                                    options = headerOptions,
                                    onOptionSelected = {
                                        withdrawalCol = if (it == "— None —") "" else it
                                        reapplyMapping(rawParsedRows, dateCol, descCol, refCol, withdrawalCol, depositCol, balanceCol)
                                    }
                                )
                            }
                        }

                        // Mapping Row 3
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "CREDIT (IN)",
                                    value = depositCol.ifBlank { "Select" },
                                    expanded = depColExpanded,
                                    onExpandChange = { depColExpanded = it },
                                    options = headerOptions,
                                    onOptionSelected = {
                                        depositCol = if (it == "— None —") "" else it
                                        reapplyMapping(rawParsedRows, dateCol, descCol, refCol, withdrawalCol, depositCol, balanceCol)
                                    }
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "BALANCE",
                                    value = balanceCol.ifBlank { "Select" },
                                    expanded = balColExpanded,
                                    onExpandChange = { balColExpanded = it },
                                    options = headerOptions,
                                    onOptionSelected = {
                                        balanceCol = if (it == "— None —") "" else it
                                        reapplyMapping(rawParsedRows, dateCol, descCol, refCol, withdrawalCol, depositCol, balanceCol)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Real Data Preview Table with dedicated column widths and gaps
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                border = BorderStroke(1.dp, grey_border),
                color = whiteBg
            ) {
                Column {
                    val tableScrollState = rememberScrollState()

                    Column(modifier = Modifier.horizontalScroll(tableScrollState)) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .width(880.dp)
                                .background(headerBg)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DATE", fontSize = tokens.label, color = headerGrey, fontWeight = FontWeight.Medium, modifier = Modifier.width(105.dp))
                            Text("DESCRIPTION", fontSize = tokens.label, color = headerGrey, fontWeight = FontWeight.Medium, modifier = Modifier.width(190.dp))
                            Text("REFERENCE", fontSize = tokens.label, color = headerGrey, fontWeight = FontWeight.Medium, modifier = Modifier.width(115.dp))
                            Text("TYPE", fontSize = tokens.label, color = headerGrey, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.width(70.dp))
                            Text("DEBIT", fontSize = tokens.label, color = headerGrey, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.width(95.dp))
                            Text("CREDIT", fontSize = tokens.label, color = headerGrey, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.width(95.dp))
                            Text("BALANCE", fontSize = tokens.label, color = headerGrey, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.width(110.dp))
                            Spacer(Modifier.width(60.dp))
                        }

                        HorizontalDivider(color = grey_border)

                        if (previewRows.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .width(880.dp)
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No transactions in queue. Upload a file or add rows manually.", fontSize = tokens.bodySmall, color = mutedText)
                            }
                        } else {
                            previewRows.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .width(880.dp)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // DATE
                                    Text(item.date, fontSize = tokens.caption, color = TextPrimary, modifier = Modifier.width(105.dp))

                                    // DESCRIPTION
                                    Text(
                                        text = item.description,
                                        fontSize = tokens.caption,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(190.dp)
                                    )

                                    // REFERENCE
                                    Text(
                                        text = item.reference.ifBlank { "—" },
                                        fontSize = tokens.caption,
                                        color = mutedText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(115.dp)
                                    )

                                    // TYPE BADGE
                                    Box(
                                        modifier = Modifier.width(70.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(if (item.isCredit) Color(0xFFE8FDF0) else Color(0xFFFEECEB))
                                                .padding(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text(
                                                text = if (item.isCredit) "Credit" else "Debit",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (item.isCredit) darkGreenBg else redText
                                            )
                                        }
                                    }

                                    // DEBIT
                                    Text(
                                        text = if (item.debit > 0) "₹${String.format(Locale.US, "%,.0f", item.debit)}" else "—",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = if (item.debit > 0) redText else mutedText,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(95.dp)
                                    )

                                    // CREDIT
                                    Text(
                                        text = if (item.credit > 0) "₹${String.format(Locale.US, "%,.0f", item.credit)}" else "—",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = if (item.credit > 0) darkGreenBg else mutedText,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(95.dp)
                                    )

                                    // BALANCE
                                    Text(
                                        text = if (item.balance != null) "₹${String.format(Locale.US, "%,.2f", item.balance)}" else "—",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(110.dp)
                                    )

                                    Spacer(Modifier.width(18.dp))

                                    // Delete Action Icon
                                    Box(
                                        modifier = Modifier.width(42.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = { previewRows = previewRows.filter { it.id != item.id } },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_delete),
                                                contentDescription = "Delete Row",
                                                tint = mutedText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = grey_border)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// NATIVE PARSERS FOR EXCEL (.xlsx) & CSV
// ─────────────────────────────────────────────────────────────

private fun parseXlsxDirectly(stream: InputStream): Pair<List<String>, List<Map<String, String>>> {
    val zip = ZipInputStream(stream)
    var entry = zip.nextEntry
    val sharedStrings = mutableListOf<String>()
    var sheetStreamBytes: ByteArray? = null

    while (entry != null) {
        if (entry.name.equals("xl/sharedStrings.xml", ignoreCase = true)) {
            val xml = zip.readBytes().toString(Charsets.UTF_8)
            val regex = "<t[^>]*>(.*?)</t>".toRegex()
            regex.findAll(xml).forEach { match ->
                sharedStrings.add(match.groupValues[1])
            }
        } else if (entry.name.equals("xl/worksheets/sheet1.xml", ignoreCase = true)) {
            sheetStreamBytes = zip.readBytes()
        }
        entry = zip.nextEntry
    }

    if (sheetStreamBytes == null) return Pair(emptyList(), emptyList())

    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(sheetStreamBytes.inputStream(), "UTF-8")

    val rowsData = mutableListOf<List<String>>()
    var currentRow = mutableListOf<String>()
    var currentCellType = ""
    val currentVal = StringBuilder()

    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                when (parser.name) {
                    "row" -> currentRow = mutableListOf()
                    "c" -> {
                        currentCellType = parser.getAttributeValue(null, "t") ?: ""
                        currentVal.clear()
                    }
                }
            }
            XmlPullParser.TEXT -> currentVal.append(parser.text)
            XmlPullParser.END_TAG -> {
                when (parser.name) {
                    "v" -> {
                        val textVal = currentVal.toString().trim()
                        val cellString = if (currentCellType == "s") {
                            val sIndex = textVal.toIntOrNull()
                            if (sIndex != null && sIndex in sharedStrings.indices) sharedStrings[sIndex] else textVal
                        } else textVal
                        currentRow.add(cellString)
                    }
                    "row" -> {
                        if (currentRow.isNotEmpty()) rowsData.add(currentRow)
                    }
                }
            }
        }
        eventType = parser.next()
    }

    if (rowsData.isEmpty()) return Pair(emptyList(), emptyList())

    val headers = rowsData[0].map { it.trim() }
    val mappedRows = mutableListOf<Map<String, String>>()
    for (r in 1 until rowsData.size) {
        val row = rowsData[r]
        val map = mutableMapOf<String, String>()
        headers.forEachIndexed { idx, h ->
            map[h] = row.getOrElse(idx) { "" }
        }
        mappedRows.add(map)
    }

    return Pair(headers, mappedRows)
}

private fun parseCsvDirectly(stream: InputStream): Pair<List<String>, List<Map<String, String>>> {
    val reader = BufferedReader(InputStreamReader(stream))
    val allLines = reader.readLines().map { it.trim().trim('\uFEFF') }.filter { it.isNotBlank() }
    if (allLines.isEmpty()) return Pair(emptyList(), emptyList())

    val headers = parseCsvLine(allLines[0])
    val rows = mutableListOf<Map<String, String>>()
    for (i in 1 until allLines.size) {
        val values = parseCsvLine(allLines[i])
        val map = mutableMapOf<String, String>()
        headers.forEachIndexed { hIdx, hName ->
            map[hName] = values.getOrElse(hIdx) { "" }
        }
        rows.add(map)
    }
    return Pair(headers, rows)
}

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false

    var i = 0
    while (i < line.length) {
        val c = line[i]
        when (c) {
            '\"' -> {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            }
            ',' -> {
                if (inQuotes) {
                    current.append(c)
                } else {
                    result.add(current.toString().trim().removeSurrounding("\""))
                    current.clear()
                }
            }
            '\r' -> {}
            else -> current.append(c)
        }
        i++
    }
    result.add(current.toString().trim().removeSurrounding("\""))
    return result
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex != -1) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}

private fun downloadSampleTemplate(context: Context) {
    val csvContent = buildString {
        appendLine("Date,Description,Reference,Withdrawal,Deposit,Balance")
        appendLine("2026-10-14,Customer Payment,UTR983749,0,15000,115000")
        appendLine("2026-10-15,Office Stationery,CHQ-00293,1250,0,113750")
        appendLine("2026-10-16,AWS Cloud Platform,TXN-88192,3400.5,0,110349.5")
    }

    val fileName = "bank_statement_sample_template.csv"

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Sample Template downloaded to Downloads folder", Toast.LENGTH_SHORT).show()
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(downloadsDir, fileName)
            file.writeText(csvContent, Charsets.UTF_8)
            Toast.makeText(context, "Saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}