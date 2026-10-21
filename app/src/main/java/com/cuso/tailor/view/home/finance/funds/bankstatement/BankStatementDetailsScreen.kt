@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection", "AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.finance.funds.bankstatement

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.ManualMatchRequest
import com.cuso.tailor.model.finance.StatementLineItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.viewmodel.FinanceViewModel
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.SettingsTabs
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.OrderUiState
import com.cuso.tailor.viewmodel.SalesOrderViewModel

@SuppressLint("DefaultLocale")
@Composable
fun BankStatementDetailScreen(
    statementId: String,
    viewModel: FinanceViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val statement by viewModel.statementDetail.collectAsState()
    val isLoading by viewModel.isLoadingStatementDetail.collectAsState()
    val isActionRunning by viewModel.isReconciling.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Unmatched, 2: Matched

    // Dialog state for Manual Match
    var matchingLine by remember { mutableStateOf<StatementLineItemDto?>(null) }
    var showArchiveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statementId) {
        viewModel.fetchBankStatementDetail(statementId)
    }

    val lines = statement?.lines.orEmpty()
    val totalLines = lines.size
    val matchedLines = lines.count { it.reconciliationStatus.equals("reconciled", ignoreCase = true) }
    val unmatchedLines = totalLines - matchedLines
    val progressPercent = if (totalLines > 0) ((matchedLines.toFloat() / totalLines) * 100).toInt() else 0

    // Filter lines based on active Tab and search input
    val filteredLines = remember(lines, selectedFilterTab, searchQuery) {
        lines.filter { line ->
            val matchesTab = when (selectedFilterTab) {
                1 -> !line.reconciliationStatus.equals("reconciled", ignoreCase = true)
                2 -> line.reconciliationStatus.equals("reconciled", ignoreCase = true)
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    line.description.contains(searchQuery, ignoreCase = true) ||
                    line.referenceNumber.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesSearch
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "Statement Details",
//                subtitle = statement?.statementNumber,
                onClose = onBack,
                trailingContent = {
                    // Run RBS Auto-Reconciliation Button
                    Button(
                        onClick = {
                            viewModel.runRbsAutoReconciliation(
                                statementId = statementId,
                                onSuccess = { DynamicIslandManager.showSuccess(it) },
                                onError = { DynamicIslandManager.showError(it) }
                            )
                        },
                        enabled = !isActionRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Run RBS",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }

                    // Archive Statement Button
                    IconButton(
                        onClick = { showArchiveDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Archive",
                            tint = redText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading && statement == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── 1. TWO-COLUMN METRIC CARDS (2 per row) ──
                item {
                    val st = statement
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric Row 1: Passbook Balances & Deposits
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatementMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Passbook Balances",
                                primaryText = "Open: ₹${String.format(LocalLocale.current.platformLocale, "%,.0f", st?.openingBalance ?: 0.0)}",
                                secondaryText = "Close: ₹${String.format(LocalLocale.current.platformLocale, "%,.0f", st?.closingBalance ?: 0.0)}",
                                icon = Icons.Default.CreditCard,
                                iconTint = TextSecondary,
                                primaryTextColor = TextPrimary
                            )

                            StatementMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Total Deposits (Credits)",
                                primaryText = "+₹${String.format(LocalLocale.current.platformLocale, "%,.0f", st?.totalDeposits ?: 0.0)}",
                                secondaryText = "Money In",
                                icon = Icons.Default.CheckCircle,
                                iconTint = greentext,
                                primaryTextColor = greentext
                            )
                        }

                        // Metric Row 2: Withdrawals & Reconciliation Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatementMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Total Withdrawals",
                                primaryText = "-₹${String.format(LocalLocale.current.platformLocale, "%,.2f", st?.totalWithdrawals ?: 0.0)}",
                                secondaryText = "Money Out",
                                icon = Icons.Default.ErrorOutline,
                                iconTint = redText,
                                primaryTextColor = redText
                            )

                            StatementMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Reconciliation Progress",
                                primaryText = "$progressPercent%",
                                secondaryText = "$matchedLines / $totalLines Lines",
                                icon = Icons.Default.Check,
                                iconTint = BluePrimary,
                                primaryTextColor = BluePrimary,
                                showProgress = true,
                                progress = if (totalLines > 0) matchedLines.toFloat() / totalLines else 0f
                            )
                        }
                    }
                }

                // ── 2. FILTER TABS (SettingsTabs Reused) ──
                item {
                    val filterTabs = remember(totalLines, unmatchedLines, matchedLines) {
                        listOf(
                            TabItem(label = "All ($totalLines)"),
                            TabItem(label = "Unmatched ($unmatchedLines)"),
                            TabItem(label = "Matched ($matchedLines)")
                        )
                    }

                    SettingsTabs(
                        tabs = filterTabs,
                        selectedIndex = selectedFilterTab,
                        onTabSelected = { index -> selectedFilterTab = index },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding),
                        height = 42.dp,
                        containerColor = light_grey,
                        selectedBackgroundColor = whiteBg,
                        selectedTextColor = Primary,
                        unselectedTextColor = TextSecondary,
                        cornerRadius = tokens.cardCornerRadius * 0.7f,
                        selectedCornerRadius = tokens.cardCornerRadius * 0.5f
                    )
                }

                // ── 3. SEARCH BAR ──
                item {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search description or ref...",
                        showFilterIcon = false
                    )
                }

                // ── 4. LINES TABLE (Scrollable for mobile fit) ──
                item {
                    val horizontalScrollState = rememberScrollState()

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding),
                        color = whiteBg,
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .width(960.dp)
                                    .clip(RoundedCornerShape(topStart = tokens.cardCornerRadius, topEnd = tokens.cardCornerRadius))
                                    .background(headerBg)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("DATE", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.width(110.dp))
                                Text("DESCRIPTION", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.width(220.dp))
                                Text("REFERENCE", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.width(130.dp))
                                Text("DEBIT", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.width(100.dp))
                                Text("CREDIT", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.width(100.dp))
                                Text("RUN BALANCE", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.width(110.dp))
                                Text("MATCH STATUS", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.width(120.dp))
                                Text("Action", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.width(60.dp))
                            }

                            HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                            // Empty State
                            if (filteredLines.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .width(960.dp)
                                        .padding(vertical = 36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No matching transactions found.",
                                        fontSize = tokens.bodySmall,
                                        color = mutedText
                                    )
                                }
                            } else {
                                // Table Data Rows from `lines`
                                filteredLines.forEachIndexed { index, line ->
                                    StatementTableRow(
                                        line = line,
                                        onManualMatch = { matchingLine = line },
                                        onUnmatch = {
                                            viewModel.unmatchStatementLine(
                                                statementId = statementId,
                                                statementLineId = line.id,
                                                onSuccess = { DynamicIslandManager.showSuccess(it) },
                                                onError = { DynamicIslandManager.showError(it) }
                                            )
                                        }
                                    )

                                    if (index < filteredLines.lastIndex) {
                                        HorizontalDivider(color = grey_border, thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }

    // ── MANUAL MATCH MODAL DIALOG ──
    matchingLine?.let { line ->
        ManualMatchDialog(
            line = line,
            isSubmitting = isActionRunning,
            onDismiss = { matchingLine = null },
            onConfirm = { entityType, entityId, notes ->
                viewModel.manualMatchStatementLine(
                    statementId = statementId,
                    request = ManualMatchRequest(
                        statementLineId = line.id,
                        matchedEntityType = entityType,
                        matchedEntityId = entityId,
                        notes = notes
                    ),
                    onSuccess = {
                        DynamicIslandManager.showSuccess(it)
                        matchingLine = null
                    },
                    onError = { DynamicIslandManager.showError(it) }
                )
            }
        )
    }

    // ── ARCHIVE STATEMENT CONFIRMATION DIALOG ──
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            containerColor = whiteBg,
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            title = {
                Text(
                    text = "Archive Bank Statement",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to archive this bank statement? This will remove all open lines from active reconciliation.",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showArchiveDialog = false
                        viewModel.archiveBankStatement(
                            statementId = statementId,
                            onSuccess = {
                                DynamicIslandManager.showSuccess(it)
                                onBack()
                            },
                            onError = { DynamicIslandManager.showError(it) }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = redText)
                ) {
                    Text("Archive", color = whiteBg, fontSize = tokens.bodySmall)
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Cancel", color = TextSecondary, fontSize = tokens.bodySmall)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// ── TABLE ROW COMPONENT ──
// ─────────────────────────────────────────────────────────────
@SuppressLint("DefaultLocale")
@Composable
private fun StatementTableRow(
    line: StatementLineItemDto,
    onManualMatch: () -> Unit,
    onUnmatch: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isMatched = line.reconciliationStatus.equals("reconciled", ignoreCase = true)
    var showActionMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .width(960.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. DATE
        Text(
            text = formatStatementDisplayDate(line.transactionDate),
            fontSize = tokens.bodySmall,
            color = TextPrimary,
            modifier = Modifier.width(110.dp)
        )

        // 2. DESCRIPTION
        Column(modifier = Modifier.width(220.dp).padding(end = 8.dp)) {
            Text(
                text = line.description.ifBlank { "Bank Transaction" },
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isMatched) {
                Text(
                    text = "Linked to: ${line.matchedEntityType ?: "Transaction"} (${line.matchType ?: "Manual"})",
                    fontSize = 11.sp,
                    color = Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 3. REFERENCE
        Text(
            text = line.referenceNumber.ifBlank { "—" },
            fontSize = tokens.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(130.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 4. DEBIT
        Text(
            text = if (line.withdrawal > 0) "₹${String.format(LocalLocale.current.platformLocale, "%,.2f", line.withdrawal)}" else "—",
            fontSize = tokens.bodySmall,
            color = if (line.withdrawal > 0) redText else TextSecondary,
            modifier = Modifier.width(100.dp)
        )

        // 5. CREDIT
        Text(
            text = if (line.deposit > 0) "₹${String.format(LocalLocale.current.platformLocale, "%,.0f", line.deposit)}" else "—",
            fontSize = tokens.bodySmall,
            fontWeight = if (line.deposit > 0) FontWeight.Medium else FontWeight.Normal,
            color = if (line.deposit > 0) greentext else TextSecondary,
            modifier = Modifier.width(100.dp)
        )

        // 6. RUN BALANCE
        Text(
            text = line.runningBalance?.let { "₹${String.format(LocalLocale.current.platformLocale, "%,.2f", it)}" } ?: "—",
            fontSize = tokens.bodySmall,
            color = TextPrimary,
            modifier = Modifier.width(110.dp)
        )

        // 7. MATCH STATUS BADGE
        Box(modifier = Modifier.width(120.dp)) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isMatched) greenBg else redBg)
                    .padding(horizontal = 8.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isMatched) greentext else redText)
                )
                Text(
                    text = if (isMatched) "Matched" else "Unmatched",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isMatched) greentext else redText
                )
            }
        }

        // 8. ACTION 3-DOTS MENU
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
            IconButton(onClick = { showActionMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Actions",
                    tint = iconMuted
                )
            }

            DropdownMenu(
                expanded = showActionMenu,
                onDismissRequest = { showActionMenu = false },
                modifier = Modifier.background(whiteBg)
            ) {
                if (!isMatched) {
                    DropdownMenuItem(
                        text = { Text("Manual Match", fontSize = tokens.bodySmall, color = TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.CallMade, null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                        },
                        onClick = {
                            showActionMenu = false
                            onManualMatch()
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Unmatch Line", fontSize = tokens.bodySmall, color = orangeText) },
                        leadingIcon = {
                            Icon(Icons.Default.Refresh, null, tint = orangeText, modifier = Modifier.size(16.dp))
                        },
                        onClick = {
                            showActionMenu = false
                            onUnmatch()
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ── TWO-COLUMN METRIC CARD ──
// ─────────────────────────────────────────────────────────────
@Composable
private fun StatementMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    primaryText: String,
    secondaryText: String,
    icon: ImageVector,
    iconTint: Color,
    primaryTextColor: Color = TextPrimary,
    showProgress: Boolean = false,
    progress: Float = 0f
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = primaryText,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (showProgress) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = Primary,
                    trackColor = light_grey
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = secondaryText,
                fontSize = tokens.caption,
                color = mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

//// ─────────────────────────────────────────────────────────────
//// ── FILTER PILL TAB ──
//// ─────────────────────────────────────────────────────────────
//@Composable
//private fun StatementFilterPill(
//    modifier: Modifier = Modifier,
//    label: String,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    val tokens = LocalAppTokens.current
//
//    Box(
//        modifier = modifier
//            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
//            .background(if (isSelected) whiteBg else Color.Transparent)
//            .clickable { onClick() }
//            .padding(vertical = 6.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = label,
//            fontSize = tokens.caption,
//            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
//            color = if (isSelected) Primary else TextSecondary,
//            maxLines = 1
//        )
//    }
//}

// ─────────────────────────────────────────────────────────────
// ── MANUAL MATCH MODAL DIALOG (SALES ORDERS DROPDOWN) ──
// ─────────────────────────────────────────────────────────────
@Composable
private fun ManualMatchDialog(
    line: StatementLineItemDto,
    isSubmitting: Boolean,
    salesOrderViewModel: SalesOrderViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onConfirm: (matchedEntityType: String, matchedEntityId: String, notes: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val platformLocale = LocalLocale.current.platformLocale

    // 1. Fetch Sales Orders list when dialog opens
    LaunchedEffect(Unit) {
        salesOrderViewModel.fetchOrders(page = 1, limit = 50)
    }

    val orderState by salesOrderViewModel.orderState.collectAsState()

    // 2. Target ERP Transaction Types
    val optionMapping = listOf(
        "Contra Transfer (BankTransaction)" to "BankTransaction",
        "Customer Receipt (SalesPayment)" to "SalesPayment",
        "Vendor Payment (PurchasePayment)" to "PurchasePayment",
        "Direct Expense (Expense)" to "Expense"
    )
    val transactionTypeOptions = optionMapping.map { it.first }
    var selectedTransactionType by remember { mutableStateOf(transactionTypeOptions[0]) }
    var isTypeDropdownExpanded by remember { mutableStateOf(false) }

    // 3. Extract Orders into Dropdown list: Map "ORD-2026-0035 - CustomerName" to "_id"
    val orderList = remember(orderState) {
        if (orderState is OrderUiState.Success) {
            (orderState as OrderUiState.Success).orders
        } else emptyList()
    }

    val orderOptions = remember(orderList) {
        orderList.map { order ->
            val custName = order.customerId?.name?.takeIf { it.isNotBlank() } ?: "Unknown Customer"
            val orderCode = order.orderNumber
            "$orderCode - $custName"
        }
    }

    var selectedOrderDisplay by remember { mutableStateOf("") }
    var selectedOrderId by remember { mutableStateOf("") }
    var isOrderDropdownExpanded by remember { mutableStateOf(false) }
    var orderError by remember { mutableStateOf<String?>(null) }

    var notes by remember { mutableStateOf("") }

    // Wide modal display across mobile screen
    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f) // Wide mobile width
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding)
            ) {
                // Header with Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Manual Match Transaction",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Link line to double-entry general ledger posting",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = iconMuted
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Statement Line Summary Box
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = light_blue,
                    border = BorderStroke(1.dp, light_blue_border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Statement Line:",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                            Text(
                                text = line.description.ifBlank { "Bank Transaction" },
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Amount:",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                            val isDeposit = line.deposit > 0
                            val amt = if (isDeposit) {
                                "+₹${String.format(platformLocale, "%,.0f", line.deposit)}"
                            } else {
                                "-₹${String.format(platformLocale, "%,.0f", line.withdrawal)}"
                            }
                            Text(
                                text = amt,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isDeposit) greentext else redText
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // 1. Target ERP Transaction Type Dropdown
                FormDropdown(
                    label = "Target ERP Transaction Type",
                    isRequired = true,
                    value = selectedTransactionType,
                    expanded = isTypeDropdownExpanded,
                    onExpandChange = { isTypeDropdownExpanded = it },
                    options = transactionTypeOptions,
                    onOptionSelected = { selectedTransactionType = it }
                )

                Spacer(Modifier.height(12.dp))

                // 2. Select Sales Order Dropdown (Replaces the text field)
                FormDropdown(
                    label = "Select Sales Order",
                    isRequired = true,
                    value = if (selectedOrderDisplay.isNotBlank()) selectedOrderDisplay else "Select order...",
                    expanded = isOrderDropdownExpanded,
                    onExpandChange = { isOrderDropdownExpanded = it },
                    options = if (orderOptions.isNotEmpty()) orderOptions else listOf("Loading orders..."),
                    isError = orderError != null,
                    errorMessage = orderError,
                    onOptionSelected = { selectedLabel ->
                        selectedOrderDisplay = selectedLabel
                        orderError = null
                        // Match the selected text to find the exact MongoDB _id
                        val matchedOrder = orderList.find { order ->
                            val custName = order.customerId?.name?.takeIf { it.isNotBlank() } ?: "Unknown Customer"
                            val orderCode = order.orderNumber
                            val label = "$orderCode - $custName"
                            label == selectedLabel
                        }
                        selectedOrderId = matchedOrder?.id.orEmpty()
                    }
                )

                Spacer(Modifier.height(12.dp))

                // 3. Reconciliation Notes
                FormLabel(
                    text = "Reconciliation Notes (Max 300 chars)",
                    isRequired = false
                )
                FormTextArea(
                    value = notes,
                    onValueChange = { if (it.length <= 300) notes = it },
                    placeholder = "Verified via bank statement reference...",
                    minLines = 3,
                    maxLines = 4
                )

                Spacer(Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting
                    ) {
                        Text(
                            text = "Cancel",
                            color = TextSecondary,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (selectedOrderId.isBlank()) {
                                orderError = "Please select an order to match"
                            } else {
                                val entityType = optionMapping.find { it.first == selectedTransactionType }?.second ?: "BankTransaction"
                                // Sends the MongoDB _id into the payload
                                onConfirm(entityType, selectedOrderId, notes)
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Confirm Match",
                                    color = whiteBg,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ── DATE FORMATTER HELPER ──
// ─────────────────────────────────────────────────────────────
private fun formatStatementDisplayDate(isoDate: String): String {
    if (isoDate.isBlank()) return "—"
    return try {
        val datePart = isoDate.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthIdx = (parts[1].toIntOrNull() ?: 1) - 1
            val monthName = months.getOrElse(monthIdx) { parts[1] }
            "$monthName ${parts[2]}, ${parts[0]}"
        } else isoDate
    } catch (_: Exception) {
        isoDate
    }
}